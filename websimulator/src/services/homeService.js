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
  };

  if (isOn) {

    updates.condition = "NORMAL";

    // Store the time when iron/device was turned ON
    updates.turnedOnAt = Date.now();

  } else {

    updates.condition = "NORMAL";

    updates.turnedOnAt = null;

  }

  await update(
    ref(db, devicePath),
    updates
  );
};

// Update an individual switch inside a multi-switch board
export const updateMultiSwitch = async (
  homeId,
  floorId,
  roomId,
  deviceId,
  switchId,
  isOn
) => {

  const switchPath =
    `homes/${homeId}/floors/${floorId}/rooms/${roomId}/devices/${deviceId}/switches/${switchId}`;

  const updates = {
    on: isOn,
    status: isOn ? "ON" : "OFF",
  };

  await update(
    ref(db, switchPath),
    updates
  );
};