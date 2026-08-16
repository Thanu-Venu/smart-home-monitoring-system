import {
  ref,
  update,
  push
} from "firebase/database";

import { db } from "../firebase/firebaseConfig";

export const checkIronSafety = async (
  homeId,
  floorId,
  roomId,
  deviceId,
  temperature,
  maxOnDurationMinutes,
  turnedOnAt,
  deviceName
) => {

  const devicePath =
    `homes/${homeId}/floors/${floorId}/rooms/${roomId}/devices/${deviceId}`;

  let condition = "NORMAL";
  let shouldTurnOff = false;
  let alertMessage = "";

  // --------------------------------
  // Calculate how long iron is ON
  // --------------------------------

  let elapsedMinutes = 0;

  if (turnedOnAt) {
    elapsedMinutes =
      (Date.now() - turnedOnAt) / 60000;
  }

  // --------------------------------
  // CRITICAL CONDITIONS
  // --------------------------------

  if (temperature >= 200) {

    condition = "CRITICAL";
    shouldTurnOff = true;

    alertMessage =
      `Iron automatically turned OFF due to critical temperature (${temperature}°C)`;

  }

  else if (
    maxOnDurationMinutes &&
    elapsedMinutes >= maxOnDurationMinutes
  ) {

    condition = "CRITICAL";
    shouldTurnOff = true;

    alertMessage =
      `Iron automatically turned OFF because maximum ON duration (${maxOnDurationMinutes} minutes) was exceeded`;

  }


  // --------------------------------
  // WARNING CONDITIONS
  // --------------------------------

  else if (temperature >= 150) {

    condition = "WARNING";

  }

  else if (
    maxOnDurationMinutes &&
    elapsedMinutes >= maxOnDurationMinutes * 0.8
  ) {

    condition = "WARNING";

  }


  // --------------------------------
  // CRITICAL → AUTOMATIC OFF
  // --------------------------------

  if (shouldTurnOff) {

    // Turn Iron OFF

    await update(
      ref(db, devicePath),
      {

        on: false,

        status: "OFF",

        condition: "CRITICAL",

        // Mirrors the device-level "alert" field the Android app's
        // SafetyMonitor sets on its own cutoffs. Without this, a cutoff
        // triggered from the web simulator would leave the device's
        // alert field blank — and the Android Reports screen only
        // counts a device as an active alert when BOTH
        // condition == "CRITICAL" AND alert is non-blank (see
        // ReportRepository.observeHomeReport), so the event would
        // silently never show up there.
        alert: alertMessage,

        turnedOnAt: null

      }
    );


    // --------------------------------
    // Create Firebase Alert
    // --------------------------------

    const alertsRef =
      ref(db, "alerts");

    const newAlertRef =
      push(alertsRef);


    await update(
      newAlertRef,
      {

        type: "IRON_SAFETY",

        device: deviceName || "Iron",

        deviceId: deviceId,

        message: alertMessage,

        severity: "CRITICAL",

        temperature: temperature,

        elapsedMinutes:
          Math.round(elapsedMinutes),

        maxOnDurationMinutes:
          maxOnDurationMinutes,

        timestamp: Date.now(),

        read: false

      }
    );

  }

  else {

    // --------------------------------
    // NORMAL / WARNING
    // --------------------------------

    await update(
      ref(db, devicePath),
      {

        condition: condition

      }
    );

  }


  return condition;

};