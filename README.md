# Smart Home Monitoring & Control System

A real-time Smart Home Monitoring and Control System with two clients — a native **Android app** and a **web-based hardware simulator** — sharing a single **Firebase Realtime Database**. Built as a mini project for **SCS 3311**.

Monitor and control a home's floors, rooms, and devices (lights, fans, outlets, an iron with an automatic safety cutoff, cameras, and multi-switch gang-boxes) from either client, with changes reflected on both in real time.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [1. Firebase Project Setup](#1-firebase-project-setup)
  - [2. Android App Setup](#2-android-app-setup)
  - [3. Web Simulator Setup](#3-web-simulator-setup)
- [Firebase Data Model](#firebase-data-model)
- [Security Rules](#security-rules)
- [Usage](#usage)
- [Documentation](#documentation)
- [Known Limitations](#known-limitations)
- [Team](#team)

---

## Overview

The system represents a physical home as a **Home → Floor → Room → Device** hierarchy (a `MULTI_SWITCH` device additionally contains independent **Switches**). Both the Android app and the web simulator read and write the exact same Firebase Realtime Database tree, so a change made on one client — toggling a device, editing a schedule, simulating a fault — appears on the other client live, with no manual refresh or sync step.

The web simulator exists to stand in for physical smart-home hardware: it lets you exercise and demo the Android app's real-time behavior (schedules, safety cutoffs, multi-switch control) without owning actual IoT devices.

## Features

**Core**
- Home / Floor / Room / Device hierarchy, fully CRUD-able from the Android app
- Six device types: `LIGHT`, `FAN`, `OUTLET`, `IRON`, `CAMERA`, `MULTI_SWITCH`
- Real-time synchronization between the Android app and the web simulator via Firebase Realtime Database
- Email/password authentication (Firebase Auth), shared between both clients — logging in with the same account on the web simulator shows that account's own homes
- Live dashboard, per-floor/room drill-down, and a reports screen aggregating device counts and active alerts

**Multi-Switch (gang-box) devices**
- Configurable number of switches (2–5) per device
- Each switch has its own name (e.g. "Fan Switch", "Light Switch") so switches on the same panel can be told apart
- Each switch can be toggled independently; the device's overall ON/OFF state is derived automatically from its switches

**Automation**
- Device-level and per-switch **scheduling** — set an ON time and OFF time and the device (or individual switch) is automatically switched at those times
- Manual overrides between schedule boundaries are respected — the schedule only takes control again at the next boundary, not on every check

**Safety**
- Automatic safety cutoff for `IRON` devices: switches off automatically after a configurable maximum ON duration, flags the device `CRITICAL`, and records a human-readable alert message
- Safety events are visible from whichever client triggered them, and surfaced in the Android Reports screen's Active Alerts list

**Web Simulator**
- Dashboard, Floors/Rooms browser, Room device control panel, Alerts, and Reports pages
- Device toggle, multi-switch toggle, and schedule-editing controls that write directly to Firebase
- Login/Logout using the same Firebase Auth accounts as the Android app

## Tech Stack

| Layer | Technology |
|---|---|
| Android app | Kotlin, Jetpack Compose (Material3), MVVM |
| Web simulator | React 19, Vite, React Router |
| Backend | Firebase Realtime Database, Firebase Authentication |
| Language/build tooling | Kotlin 2.2, Android Gradle Plugin 9.2, Node.js / npm |

The project has no custom backend server — both clients talk to Firebase directly, and two client-side background workers (see [Architecture](#architecture)) enforce schedules and the safety cutoff.

## Project Structure

```
smart-home-monitoring-system/
├── android-app/                    # Native Android client
│   └── app/src/main/java/com/thanu/smarthome/
│       ├── model/                  # Data classes (Home, Floor, Room, Device, DeviceSwitch, ...)
│       ├── repository/             # Firebase read/write layer
│       ├── viewmodel/              # MVVM ViewModels, one per screen
│       ├── ui/                     # Jetpack Compose screens
│       ├── worker/                 # ScheduleMonitor, SafetyMonitor background workers
│       └── navigation/             # Compose Navigation graph
├── websimulator/                   # React web hardware simulator
│   └── src/
│       ├── pages/                  # Login, Dashboard, Floors, FloorDetails, Rooms, RoomDetails, Alerts, Reports
│       ├── components/             # Sidebar, Header, StatCard
│       ├── services/                # homeService.js, ironSafetyService.js (Firebase read/write)
│       └── firebase/                # Firebase app initialization
├── backend/
│   └── firebase/
│       ├── rules/database.rules.json
│       └── indexes/firestore.indexes.json
├── docs/                           # Design notes (architecture, data model, security rules)
└── README.md
```

## Architecture

Both clients are independent, symmetric consumers of one Firebase Realtime Database tree — they never talk to each other directly.

- **Android app** — a Jetpack Compose screen observes a `StateFlow` exposed by a `ViewModel`, which delegates all Firebase access to a repository class. Two singleton coroutine workers run independently of the UI:
  - `ScheduleMonitor` polls every 30s and edge-triggers devices/switches on/off at their scheduled boundaries, without fighting a manual override made between boundaries.
  - `SafetyMonitor` polls every 10s and enforces the iron's maximum-on-duration cutoff.
- **Web simulator** — every page subscribes to one listener covering the whole home tree (scoped to the logged-in user's own homes) and re-derives what it needs from it; writes go through a small service layer that mirrors the same field-consistency rules the Android app uses (e.g. recomputing a multi-switch device's overall on/off state from all of its switches in the same write).
- **Firebase Realtime Database** — the single source of truth, gated by Firebase Authentication (`auth != null`) and indexed on `ownerId` so each user only sees their own homes.

For a full write-up of the synchronization mechanism, data model, and simulator operations, see the project's technical documentation (generated separately as a PDF/LaTeX report) and the design notes under [`docs/`](docs/).

## Getting Started

### Prerequisites

- **Android:** Android Studio (recent stable), JDK 17+, an Android device or emulator (minSdk 26)
- **Web simulator:** Node.js 18+ and npm
- A Firebase project with **Realtime Database** and **Authentication (Email/Password)** enabled

### 1. Firebase Project Setup

1. Create a project at the [Firebase Console](https://console.firebase.google.com/).
2. Enable **Realtime Database** (start in a region close to you).
3. Enable **Authentication → Sign-in method → Email/Password**.
4. Deploy the security rules in [`backend/firebase/rules/database.rules.json`](backend/firebase/rules/database.rules.json) to your database (Realtime Database → Rules tab in the console).
5. Register an **Android app** in the project (package name `com.thanu.smarthome`) and download `google-services.json`.
6. Register a **Web app** in the same project and copy its config object.

### 2. Android App Setup

1. Place the downloaded `google-services.json` into `android-app/app/`.
2. Open the `android-app/` folder in Android Studio and let Gradle sync.
3. Run the app on a device or emulator (`Run ▶`), or build an APK via **Build → Build APK(s)**.
4. On first launch, use the in-app **Sign Up** screen to create an account — this is the same account you'll use to log into the web simulator.

### 3. Web Simulator Setup

1. Open `websimulator/src/firebase/firebaseConfig.js` and paste in your web app's Firebase config (`apiKey`, `authDomain`, `databaseURL`, `projectId`, etc.).
2. Install dependencies and start the dev server:
   ```bash
   cd websimulator
   npm install
   npm run dev
   ```
3. Open the printed local URL in your browser and log in with the same account you created in the Android app.
4. To build a production bundle: `npm run build` (output in `websimulator/dist/`).

## Firebase Data Model

All data lives under a single `homes` root, addressed by nested IDs:

```
homes/
  <homeId>/
    ownerId: "<firebase-auth-uid>"
    name: "My House"
    floors/
      <floorId>/
        name: "Ground Floor"
        floorNumber: 1
        rooms/
          <roomId>/
            name: "Living Room"
            devices/
              <deviceId>/
                name: "Wall Panel"
                type: "MULTI_SWITCH"
                on: true
                status: "ON"
                condition: "NORMAL"
                alert: ""
                switches/
                  switch1/
                    name: "Fan Switch"
                    on: true
                    scheduleEnabled: false
                  switch2/
                    name: "Light Switch"
                    on: false
                    scheduleEnabled: true
                    scheduleStart: "18:00"
                    scheduleEnd: "23:00"
```

An `IRON` device additionally carries `maxOnDurationMinutes` and `turnedOnAt`, used by the safety cutoff. A `LIGHT`/`FAN` device carries its own top-level `scheduleEnabled`/`scheduleStart`/`scheduleEnd`.

## Security Rules

```json
{
  "rules": {
    "homes": {
      ".read": "auth != null",
      ".write": "auth != null",
      ".indexOn": ["ownerId"]
    },
    ".read": false,
    ".write": false
  }
}
```

Every read/write under `homes` requires an authenticated user; everything outside `homes` is denied by default.

## Usage

1. **Sign up / log in** on the Android app.
2. Create a **Home**, then add **Floors**, **Rooms**, and **Devices** to it.
3. Toggle devices on/off from either client and watch the change appear on the other.
4. For a `MULTI_SWITCH` device, name each switch and optionally give it its own schedule.
5. Enable a schedule on a `LIGHT`/`FAN` device (or a switch) — it will automatically switch at the configured times without either app needing to be in the foreground continuously.
6. Create an `IRON` device with a maximum ON duration, switch it on, and leave it running past that duration to see the automatic safety cutoff and the resulting alert in the Reports screen.
7. Log into the **web simulator** with the same account to control the same home from the browser.

## Documentation

- [`docs/01-project-overview.md`](docs/01-project-overview.md)
- [`docs/02-system-architecture.md`](docs/02-system-architecture.md)
- [`docs/03-firebase-data-model.md`](docs/03-firebase-data-model.md)
- [`docs/04-realtime-synchronization.md`](docs/04-realtime-synchronization.md)
- [`docs/05-security-rules.md`](docs/05-security-rules.md)
- [`docs/06-cloud-functions.md`](docs/06-cloud-functions.md)

A more detailed technical report covering the synchronization mechanism, data representation, and simulator operations is also available as a separate PDF.

## Known Limitations

- Both clients duplicate their own schedule/safety enforcement logic rather than sharing a backend — a scheduled or safety-critical change is only as timely as whichever client's worker next polls.
- The Reports screen is a live re-aggregation of current state, not a persisted time-series log.
- The web simulator's temperature-based iron cutoff path exists in code but isn't reachable through the current UI, since nothing yet writes a simulated temperature value.

## Team

| Name | Index Number |
|---|---|
| K. Parmila | 23001331 |
| S. Shaganjaly | 23001895 |
| T. Venugoban | 23002093 |

**Course:** SCS 3311 — Mini Project
