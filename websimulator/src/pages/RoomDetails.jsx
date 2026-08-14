import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

import Sidebar from "../components/Sidebar";
import Header from "../components/Header";

import {
  listenToHomes,
  updateDeviceStatus,
} from "../services/homeService";

function RoomDetails() {

  const { homeId, roomId } = useParams();
  const navigate = useNavigate();

  const [room, setRoom] = useState(null);
  const [floorId, setFloorId] = useState(null);
  const [roomDevices, setRoomDevices] = useState([]);
  const [homeName, setHomeName] = useState("My Home");
  const [loading, setLoading] = useState(true);


  // Listen to all Firebase homes
  useEffect(() => {

    const unsubscribe = listenToHomes((allHomes) => {

      if (!allHomes) {

        setRoom(null);
        setFloorId(null);
        setRoomDevices([]);
        setLoading(false);

        return;
      }


      // Get selected home
      const selectedHome = allHomes[homeId];


      if (!selectedHome) {

        setRoom(null);
        setFloorId(null);
        setRoomDevices([]);
        setLoading(false);

        return;
      }


      // Get home name
      setHomeName(
        selectedHome.name || "My Home"
      );


      let foundRoom = null;
      let foundFloorId = null;


      // Search rooms inside selected home
      Object.entries(
        selectedHome.floors || {}
      ).forEach(
        ([currentFloorId, floor]) => {

          Object.entries(
            floor.rooms || {}
          ).forEach(
            ([currentRoomId, currentRoom]) => {

              if (currentRoomId === roomId) {

                foundRoom = {
                  id: currentRoomId,
                  ...currentRoom,
                };

                foundFloorId =
                  currentFloorId;

              }

            }
          );

        }
      );


      if (foundRoom) {

        setRoom(foundRoom);

        setFloorId(foundFloorId);


        // Convert Firebase devices object to array
        const firebaseDevices =
          Object.entries(
            foundRoom.devices || {}
          ).map(
            ([deviceId, device]) => ({
              id: deviceId,
              ...device,
            })
          );


        setRoomDevices(
          firebaseDevices
        );

      } else {

        setRoom(null);
        setFloorId(null);
        setRoomDevices([]);

      }


      setLoading(false);

    });


    // Remove Firebase listener
    return () => unsubscribe();

  }, [homeId, roomId]);


  // Toggle device ON / OFF
  const toggleDevice = async (
    deviceId,
    currentStatus
  ) => {

    if (
      !homeId ||
      !floorId ||
      !roomId
    ) {
      return;
    }


    try {

      await updateDeviceStatus(
        homeId,
        floorId,
        roomId,
        deviceId,
        !currentStatus
      );

    } catch (error) {

      console.error(
        "Failed to update device status:",
        error
      );

    }

  };


  // Loading state
  if (loading) {

    return (
      <div className="app-layout">

        <Sidebar />

        <main className="main-content">

          <Header
            homeName={homeName}
          />

          <section className="dashboard-content">

            <div className="welcome-section">

              <h1>
                Room Details
              </h1>

              <p>
                Loading room...
              </p>

            </div>

          </section>

        </main>

      </div>
    );

  }


  // Room not found
  if (!room) {

    return (
      <div className="app-layout">

        <Sidebar />

        <main className="main-content">

          <Header
            homeName={homeName}
          />

          <section className="dashboard-content">

            <button
              className="back-button"
              onClick={() =>
                navigate("/floors")
              }
            >
              ← Back to Floors
            </button>


            <div className="empty-state">

              <div>
                🏠
              </div>

              <h3>
                Room not found
              </h3>

              <p>
                This room could not be found
                in Firebase.
              </p>

            </div>

          </section>

        </main>

      </div>
    );

  }


  // Count active devices
  const activeDevices =
    roomDevices.filter(
      (device) => device.on === true
    ).length;


  return (
    <div className="app-layout">

      <Sidebar />

      <main className="main-content">

        <Header
          homeName={homeName}
        />


        <section className="dashboard-content">


          {/* Back Button */}

          <button
            className="back-button"
            onClick={() =>
              navigate(
                `/floors/${homeId}/${floorId}`
              )
            }
          >
            ← Back to Floor
          </button>


          {/* Room Header */}

          <div className="room-details-header">

            <div>

              <div className="room-details-icon">
                🏠
              </div>


              <div>

                <h1>
                  {room.name}
                </h1>


                <p>

                  {roomDevices.length}{" "}

                  {roomDevices.length === 1
                    ? "device"
                    : "devices"}

                  {" · "}

                  {activeDevices} active

                </p>

              </div>

            </div>

          </div>


          {/* Devices Section */}

          <section className="section">

            <div className="section-header">

              <div>

                <h2>
                  Devices
                </h2>

                <p>
                  Monitor and control devices
                  in this room.
                </p>

              </div>

            </div>


            {/* No Devices */}

            {roomDevices.length === 0 ? (

              <div className="empty-state">

                <div>
                  🔌
                </div>


                <h3>
                  No devices found
                </h3>


                <p>
                  There are no devices assigned
                  to this room.
                </p>

              </div>

            ) : (


              /* Device Cards */

              <div className="devices-grid">

                {roomDevices.map(
                  (device) => {

                    const isOn =
                      device.on === true;


                    return (
                      <div
                        className="device-card"
                        key={device.id}
                      >


                        {/* Device Header */}

                        <div className="device-top">

                          <span className="device-icon">

                            {device.type === "LIGHT"
                              ? "💡"
                              : device.type === "FAN"
                              ? "🌀"
                              : device.type === "IRON"
                              ? "🔥"
                              : device.type === "CAMERA"
                              ? "📷"
                              : device.type === "OUTLET"
                              ? "🔌"
                              : "🔌"}

                          </span>


                          {/* Status */}

                          <span
                            className={
                              isOn
                                ? "status-badge on"
                                : "status-badge off"
                            }
                          >

                            {isOn
                              ? "ON"
                              : "OFF"}

                          </span>

                        </div>


                        {/* Device Name */}

                        <h3>
                          {device.name}
                        </h3>


                        {/* Device Type */}

                        <p className="device-type">
                          {device.type}
                        </p>


                        {/* Schedule */}

                        {device.scheduleEnabled && (

                          <p className="device-schedule">

                            Schedule:{" "}

                            {device.scheduleStart}

                            {" - "}

                            {device.scheduleEnd}

                          </p>

                        )}


                        {/* Device Control */}

                        <button
                          className={
                            isOn
                              ? "device-control off-button"
                              : "device-control on-button"
                          }

                          onClick={() =>
                            toggleDevice(
                              device.id,
                              isOn
                            )
                          }
                        >

                          {isOn
                            ? "Turn OFF"
                            : "Turn ON"}

                        </button>

                      </div>
                    );

                  }
                )}

              </div>

            )}

          </section>

        </section>

      </main>

    </div>
  );
}

export default RoomDetails;