import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import StatCard from "../components/StatCard";

import { listenToHomes } from "../services/homeService";

function Dashboard() {
  const navigate = useNavigate();

  const [homes, setHomes] = useState([]);
  const [floors, setFloors] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);


  // Firebase listener
  useEffect(() => {

    const unsubscribe = listenToHomes((allHomes) => {

      if (!allHomes) {

        setHomes([]);
        setFloors([]);
        setRooms([]);
        setDevices([]);
        setLoading(false);

        return;
      }


      const firebaseHomes = [];
      const firebaseFloors = [];
      const firebaseRooms = [];
      const firebaseDevices = [];


      // Loop through ALL homes
      Object.entries(allHomes).forEach(
        ([homeId, home]) => {

          // Store home
          firebaseHomes.push({
            id: homeId,
            name: home.name || "My Home",
            ownerId: home.ownerId,
          });


          // Loop through floors
          Object.entries(
            home.floors || {}
          ).forEach(
            ([floorId, floor]) => {

              let floorRoomCount = 0;
              let floorDeviceCount = 0;


              // Loop through rooms
              Object.entries(
                floor.rooms || {}
              ).forEach(
                ([roomId, room]) => {

                  const roomDevices =
                    Object.entries(
                      room.devices || {}
                    ).map(
                      ([deviceId, device]) => ({
                        id: deviceId,
                        ...device,
                        homeId,
                        floorId,
                        roomId,
                        homeName:
                          home.name,
                        floorName:
                          floor.name,
                        roomName:
                          room.name,
                      })
                    );


                  floorRoomCount++;

                  floorDeviceCount +=
                    roomDevices.length;


                  // Add room
                  firebaseRooms.push({
                    id: roomId,
                    name:
                      room.name ||
                      "Unnamed Room",
                    homeId,
                    homeName:
                      home.name,
                    floorId,
                    floorName:
                      floor.name,
                    deviceCount:
                      roomDevices.length,
                  });


                  // Add devices
                  firebaseDevices.push(
                    ...roomDevices
                  );

                }
              );


              // Add floor
              firebaseFloors.push({
                id: floorId,
                name:
                  floor.name ||
                  "Unnamed Floor",
                homeId,
                homeName:
                  home.name,
                roomCount:
                  floorRoomCount,
                deviceCount:
                  floorDeviceCount,
              });

            }
          );

        }
      );


      setHomes(firebaseHomes);
      setFloors(firebaseFloors);
      setRooms(firebaseRooms);
      setDevices(firebaseDevices);

      setLoading(false);

    });


    return () => unsubscribe();

  }, []);


  // Loading
  if (loading) {

    return (
      <div className="app-layout">

        <Sidebar />

        <main className="main-content">

          <Header homeName="My Home" />

          <section className="dashboard-content">

            <div className="welcome-section">

              <h1>
                Welcome 👋
              </h1>

              <p>
                Loading your smart home...
              </p>

            </div>

          </section>

        </main>

      </div>
    );

  }


  // Statistics

  const totalFloors =
    floors.length;

  const totalRooms =
    rooms.length;

  const totalDevices =
    devices.length;

  const onlineDevices =
    devices.filter(
      (device) => device.on === true
    ).length;


  // Active iron alerts

  const activeAlerts =
    devices.filter(
      (device) =>
        device.type === "IRON" &&
        device.on === true
    );


  // Use first home name for header
  const homeName =
    homes[0]?.name || "My Home";


  return (
    <div className="app-layout">

      <Sidebar />

      <main className="main-content">

        <Header
          homeName={homeName}
        />


        <section className="dashboard-content">


          {/* Welcome */}

          <div className="welcome-section">

            <div>

              <h1>
                Welcome to {homeName} 👋
              </h1>

              <p>
                Monitor your smart home
                environment.
              </p>

            </div>

          </div>


          {/* Statistics */}

          <div className="stats-grid">

            <StatCard
              title="Total Floors"
              value={totalFloors}
              icon="🏢"
            />

            <StatCard
              title="Total Rooms"
              value={totalRooms}
              icon="🏠"
            />

            <StatCard
              title="Total Devices"
              value={totalDevices}
              icon="🔌"
            />

            <StatCard
              title="Online Devices"
              value={onlineDevices}
              icon="🟢"
            />

          </div>


          {/* Floors */}

          <section className="section">

            <div className="section-header">

              <div>

                <h2>
                  Floors
                </h2>

                <p>
                  Select a floor to view
                  its rooms and devices.
                </p>

              </div>

            </div>


            {floors.length === 0 ? (

              <div className="empty-state">

                <div>
                  🏢
                </div>

                <h3>
                  No floors found
                </h3>

                <p>
                  No floors are available
                  for this home.
                </p>

              </div>

            ) : (

              <div className="rooms-grid">

                {floors.map(
                  (floor) => (

                    <div
                      className="room-card"
                      key={`${floor.homeId}-${floor.id}`}
                      onClick={() =>
                        navigate(
                          `/floors/${floor.homeId}/${floor.id}`
                        )
                      }
                    >

                      <div className="room-icon">
                        🏢
                      </div>


                      <div>

                        <h3>
                          {floor.name}
                        </h3>


                        <p>
                          {floor.roomCount}{" "}
                          {floor.roomCount === 1
                            ? "room"
                            : "rooms"}{" "}
                          ·{" "}
                          {floor.deviceCount}{" "}
                          {floor.deviceCount === 1
                            ? "device"
                            : "devices"}
                        </p>

                      </div>


                      <span className="arrow">
                        →
                      </span>

                    </div>

                  )
                )}

              </div>

            )}

          </section>


          {/* Active Alerts */}

          {activeAlerts.length > 0 && (

            <section className="section">

              <div className="section-header">

                <div>

                  <h2>
                    Active Alerts
                  </h2>

                  <p>
                    Important safety warnings
                    requiring your attention.
                  </p>

                </div>

              </div>


              <div className="alerts-dashboard-list">

                {activeAlerts.map(
                  (device) => (

                    <div
                      className="alert-card"
                      key={device.id}
                    >

                      <div className="alert-icon">
                        🔥
                      </div>


                      <div className="alert-content">

                        <div className="alert-header">

                          <div>

                            <h2>
                              {device.name}
                            </h2>

                            <p>
                              {device.homeName}
                              {" · "}
                              {device.floorName}
                              {" · "}
                              {device.roomName}
                            </p>

                          </div>


                          <span className="alert-status">
                            ACTIVE
                          </span>

                        </div>


                        <div className="alert-warning">

                          ⚠️ This iron is currently
                          switched ON. Please check
                          the appliance and turn it
                          OFF when it is no longer
                          needed.

                        </div>


                        <button
                          className="alert-action-button"
                          onClick={() =>
                            navigate("/alerts")
                          }
                        >
                          View Alert
                        </button>

                      </div>

                    </div>

                  )
                )}

              </div>

            </section>

          )}

        </section>

      </main>

    </div>
  );
}

export default Dashboard;