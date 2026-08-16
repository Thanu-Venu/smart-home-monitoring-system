import { ref, onValue, update, query, orderByChild,equalTo } from "firebase/database";
import { onAuthStateChanged, } from "firebase/auth";
import { db, auth, } from "../firebase/firebaseConfig";



export const listenToHomes = (callback) => {
  let databaseUnsubscribe = null;

    const authUnsubscribe =
    onAuthStateChanged(
      auth,
      (user) => {
         console.log("AUTH USER:", user);

        // No user logged in
        if (!user) {
           console.log("NO USER LOGGED IN");

          if (databaseUnsubscribe) {
            databaseUnsubscribe();
            databaseUnsubscribe = null;
          }

          callback(null);
          return;
        }
        
        console.log(
          "Logged-in User UID:",
          user.uid
        );


        // Query homes using ownerId
        const homesQuery = query(
          ref(db, "homes"),
          orderByChild("ownerId"),
          equalTo(user.uid)
        );
        
        console.log("QUERY OWNER ID:", user.uid);

        // Listen for realtime changes
        databaseUnsubscribe =
          onValue(
            homesQuery,
            (snapshot) => {

              const data =
                snapshot.val();

              console.log(
                "User Homes:",
                data
              );

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

// Update Light/Fan schedule
export const updateDeviceSchedule = async (
  homeId,
  floorId,
  roomId,
  deviceId,
  scheduleEnabled,
  scheduleStart,
  scheduleEnd
) => {

  const devicePath =
    `homes/${homeId}/floors/${floorId}/rooms/${roomId}/devices/${deviceId}`;

  const updates = {
    scheduleEnabled,
    scheduleStart,
    scheduleEnd,
  };

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
// Update schedule for an individual Multi-Switch
export const updateMultiSwitchSchedule = async (
  homeId,
  floorId,
  roomId,
  deviceId,
  switchId,
  scheduleEnabled,
  scheduleStart,
  scheduleEnd
) => {

  const switchPath =
    `homes/${homeId}/floors/${floorId}/rooms/${roomId}/devices/${deviceId}/switches/${switchId}`;

  const updates = {
    scheduleEnabled,
    scheduleStart,
    scheduleEnd,
  };

  await update(
    ref(db, switchPath),
    updates
  );
};