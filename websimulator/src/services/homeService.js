import { ref, onValue, update, } from "firebase/database";
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

  const devicePath = `homes/${homeId}/floors/${floorId}/rooms/${roomId}/devices/${deviceId}`;

  await update(ref(db, devicePath), {
    on: isOn,
    status: isOn ? "ON" : "OFF",
  });
};