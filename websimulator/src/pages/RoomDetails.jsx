import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { rooms, devices } from "../data/dummyData";

import Sidebar from "../components/Sidebar";
import Header from "../components/Header";

function RoomDetails() {

  const { roomId } = useParams();
  const navigate = useNavigate();

  const room = rooms.find(
    (room) => room.id === roomId
  );

  const roomDevices = devices.filter(
    (device) => device.roomId === roomId
  );

  const [deviceStates, setDeviceStates] = useState(
    Object.fromEntries(
        roomDevices.map((device) => [
            device.id, device.status,
        ])
    )
  );

  const toggleDevice = (deviceId) => {
        setDeviceStates((previousStates) => ({
          ...previousStates,
        [deviceId]: !previousStates[deviceId],
    }));
  };

  if (!room) {
    return (
      <div>
        <h2>Room not found</h2>

        <button onClick={() => navigate("/rooms")}>
          Back to Rooms
        </button>
      </div>
    );
  }

  const activeDevices = roomDevices.filter(
    (device) => deviceStates[device.id]
  ).length;

  return (
    <div className="app-layout">

      <Sidebar />

      <main className="main-content">

        <Header homeName="My Home" />

        <section className="dashboard-content">

          {/* Back Button */}

          <button
            className="back-button"
            onClick={() => navigate("/rooms")}
          >
            ← Back to Rooms
          </button>

          {/* Room Header */}

          <div className="room-details-header">

            <div>
              <div className="room-details-icon">
                🏠
              </div>

              <div>
                <h1>{room.name}</h1>

                <p>
                  {roomDevices.length}{" "}
                  {roomDevices.length === 1
                    ? "device"
                    : "devices"}{" "}
                  · {activeDevices} active
                </p>
              </div>
            </div>

          </div>

          {/* Devices */}

          <section className="section">

            <div className="section-header">

              <div>
                <h2>Devices</h2>

                <p>
                  Monitor and control devices in this room.
                </p>
              </div>

            </div>

            {roomDevices.length === 0 ? (

              <div className="empty-state">
                <div>🔌</div>

                <h3>No devices found</h3>

                <p>
                  There are no devices assigned to this room.
                </p>
              </div>

            ) : (

              <div className="devices-grid">

                {roomDevices.map((device) => (

                  <div
                    className="device-card"
                    key={device.id}
                  >

                    <div className="device-top">

                      <span className="device-icon">

                        {device.type === "light"
                          ? "💡"
                          : device.type === "fan"
                          ? "🌀"
                          : device.type === "iron"
                          ? "🔥"
                          : device.type === "camera"
                          ? "📷"
                          : "🔌"}

                      </span>

                      <span
                        className={
                          deviceStates[device.id]
                            ? "status-badge on"
                            : "status-badge off"
                        }
                      >
                        {deviceStates[device.id] ? "ON" : "OFF"}
                      </span>

                    </div>

                    <h3>{device.name}</h3>

                    <p className="device-type">
                      {device.type.toUpperCase()}
                    </p>

                    <button
                      className={
                        deviceStates[device.id]
                          ? "device-control off-button"
                          : "device-control on-button"
                      }
                      onClick={() => toggleDevice(device.id)}
                    >
                      {deviceStates[device.id]
                        ? "Turn OFF"
                        : "Turn ON"}
                    </button>

                  </div>

                ))}

              </div>

            )}

          </section>

        </section>

      </main>

    </div>
  );
}

export default RoomDetails;