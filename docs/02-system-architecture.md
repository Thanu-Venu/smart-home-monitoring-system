# System Architecture

## High Level Architecture

```
                Android Application
                        |
                        |
          Firebase Authentication
                        |
                        |
         Firebase Realtime Database
          /                      \
         /                        \
Cloud Functions         Hardware Simulator
         |
         |
Notifications
```

## Components

### Android Application

Provides the user interface for monitoring and controlling smart home devices.

### Firebase Realtime Database

Acts as the central source of truth.

### Cloud Functions

Executes backend automation such as:

- Auto turn OFF iron
- Scheduled lighting
- Notification generation

### Hardware Simulator

Simulates IoT devices using a web dashboard.
