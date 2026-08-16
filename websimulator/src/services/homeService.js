import { ref, onValue, update } from "firebase/database";
import { db } from "../firebase/firebaseConfig";



export const listenToHomes = (callback) => {
  const homeRef = ref(db, "homes");

  return onValue(homeRef, (snapshot) => {
    const data = snapshot.val();
    callback(data);
  });
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