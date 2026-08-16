import { ref, onValue, update, get, query, orderByChild, equalTo } from "firebase/database";
import { onAuthStateChanged, } from "firebase/auth";
import { db, auth, } from "../firebase/firebaseConfig";



export const listenToHomes = (callback) => {
  let databaseUnsubscribe = null;

  const authUnsubscribe =
    onAuthStateChanged(
      auth,
      (user) => {

        // A previous user's query listener (if any) is always torn
        // down before doing anything else, whether we're now logging
        // out or switching to a newly logged-in user. Without this,
        // signing out and back in (or switching accounts) on the same
        // tab leaves the old user's onValue listener running forever
        // -- it keeps firing this callback with the old user's homes
        // alongside the new one's, so the page can end up showing a
        // mix of two different users' data.
        if (databaseUnsubscribe) {
          databaseUnsubscribe();
          databaseUnsubscribe = null;
        }

        // No user logged in
        if (!user) {
          callback(null);
          return;
        }

        // Query homes using ownerId
        const homesQuery = query(
          ref(db, "homes"),
          orderByChild("ownerId"),
          equalTo(user.uid)
        );

        // Listen for realtime changes
        databaseUnsubscribe =
          onValue(
            homesQuery,
            (snapshot) => {

              const data =
                snapshot.val();

              callback(data);

            }
          );

      }
    );


  // Cleanup
  return () => {

    authUnsubscribe();

    if (databaseUnsubscribe) {
      databaseUnsubscribe();
    }

  };
};

// Update device ON/OFF status
//
// Mirrors the Android app's DeviceViewModel.toggleDevice(): a manual
// toggle from either client should stamp turnedOnAt (so SafetyMonitor's
// maxOnDurationMinutes cutoff is timed correctly) and clear any
// previous CRITICAL condition/alert, since the user taking control of
// the device again means the old safety alert no longer applies. If
// this simulator only wrote on/status like before, a device the
// Android app auto-shut-off for safety would still show a stale
// CRITICAL alert on the Android side even after being switched back on
// here.
export const updateDeviceStatus = async (
  homeId,
  floorId,
  roomId,
  deviceId,
  isOn
) => {

  const devicePath =
    `homes/${homeId}/floors/${floorId}/rooms/${roomId}/devices/${deviceId}`;

  const updates = {
    on: isOn,
    status: isOn ? "ON" : "OFF",
    turnedOnAt: isOn ? Date.now() : 0,
    condition: "NORMAL",
    alert: "",
  };

  await update(ref(db, devicePath), updates);
};

// Toggle one switch on a MULTI_SWITCH gang-box device.
//
// A MULTI_SWITCH device stores its individually-addressable switches
// as a "switches" array/object under the device node (see the Android
// app's DeviceViewModel.toggleSwitch()). The device-level "on"/"status"
// fields are derived from those switches (on = true if ANY switch is
// on), so this writes the whole switches list back plus the
// recomputed device-level fields in one update — never just the
// device-level on/status — otherwise the Android app's per-switch UI
// would show switches that don't match the card's overall ON/OFF
// state.
export const updateDeviceSwitch = async (
  homeId,
  floorId,
  roomId,
  deviceId,
  switches,
  switchId,
  isOn
) => {

  const devicePath = `homes/${homeId}/floors/${floorId}/rooms/${roomId}/devices/${deviceId}`;

  const updatedSwitches = switches.map((deviceSwitch) =>
    deviceSwitch.id === switchId
      ? { ...deviceSwitch, on: isOn, status: isOn ? "ON" : "OFF" }
      : deviceSwitch
  );

  const anyOn = updatedSwitches.some((deviceSwitch) => deviceSwitch.on);

  await update(ref(db, devicePath), {
    switches: updatedSwitches,
    on: anyOn,
    status: anyOn ? "ON" : "OFF",
  });
};

// Toggle one switch on a MULTI_SWITCH gang-box device, by switch key only.
//
// Unlike updateDeviceSwitch above (which needs the full switches array
// passed in), RoomDetails.jsx only has the switchId and the new on/off
// value at the point it calls this — it reads switches the same way it
// renders them, via Object.entries(device.switches), so switchId here is
// whatever key that produced (an array index if Firebase stored the
// switches as a list, or a real key if stored as a map). This reads the
// current switches once, flips only the target switch, and — same as
// updateDeviceSwitch and the Android app's DeviceViewModel.toggleSwitch()
// — recomputes the device-level on/status from ALL switches in the same
// update, so the device card never shows a state that disagrees with its
// switches.
export const updateMultiSwitch = async (
  homeId,
  floorId,
  roomId,
  deviceId,
  switchId,
  isOn
) => {

  const devicePath =
    `homes/${homeId}/floors/${floorId}/rooms/${roomId}/devices/${deviceId}`;

  const switchesSnapshot = await get(ref(db, `${devicePath}/switches`));
  const switches = switchesSnapshot.val() || {};

  const updates = {};
  let anyOn = false;

  Object.entries(switches).forEach(([key, switchData]) => {

    const isTarget = key === switchId;
    const switchOn = isTarget ? isOn : switchData.on === true;

    if (isTarget) {
      updates[`switches/${key}/on`] = isOn;
      updates[`switches/${key}/status`] = isOn ? "ON" : "OFF";
    }

    if (switchOn) {
      anyOn = true;
    }
  });

  updates.on = anyOn;
  updates.status = anyOn ? "ON" : "OFF";

  await update(ref(db, devicePath), updates);
};

// Enable/disable and set the ON/OFF times for a device-level schedule
// (LIGHT / FAN devices). Matches the Android app's Device.scheduleEnabled
// / scheduleStart / scheduleEnd fields, which ScheduleMonitor.kt polls
// and enforces every 30s.
export const updateDeviceSchedule = async (
  homeId,
  floorId,
  roomId,
  deviceId,
  enabled,
  startTime,
  endTime
) => {

  const devicePath =
    `homes/${homeId}/floors/${floorId}/rooms/${roomId}/devices/${deviceId}`;

  await update(ref(db, devicePath), {
    scheduleEnabled: enabled,
    scheduleStart: startTime,
    scheduleEnd: endTime,
  });
};

// Same as updateDeviceSchedule, but scoped to a single switch on a
// MULTI_SWITCH device. Matches the Android app's DeviceSwitch.scheduleEnabled
// / scheduleStart / scheduleEnd fields, which ScheduleMonitor.kt enforces
// per-switch (composite key "deviceId/switches/switchId").
export const updateMultiSwitchSchedule = async (
  homeId,
  floorId,
  roomId,
  deviceId,
  switchId,
  enabled,
  startTime,
  endTime
) => {

  const devicePath =
    `homes/${homeId}/floors/${floorId}/rooms/${roomId}/devices/${deviceId}`;

  await update(ref(db, devicePath), {
    [`switches/${switchId}/scheduleEnabled`]: enabled,
    [`switches/${switchId}/scheduleStart`]: startTime,
    [`switches/${switchId}/scheduleEnd`]: endTime,
  });
};