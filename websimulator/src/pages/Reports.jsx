import { useEffect, useState } from "react";

import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import StatCard from "../components/StatCard";

import { listenToHomes } from "../services/homeService";

function Reports() {
  const [floors, setFloors] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = listenToHomes((allHomes) => {
      if (!allHomes) {
        setFloors([]);
        setRooms([]);
        setDevices([]);
        setLoading(false);
        return;
      }

      const firebaseFloors = [];
      const firebaseRooms = [];
      const firebaseDevices = [];

      // Loop through ALL homes
      Object.entries(allHomes).forEach(
        ([homeId, home]) => {

          // Loop through ALL floors
          Object.entries(home.floors || {}).forEach(
            ([floorId, floor]) => {

              let floorRoomCount = 0;
              let floorDeviceCount = 0;
              let floorActiveCount = 0;

              // Loop through rooms
              Object.entries(floor.rooms || {}).forEach(
                ([roomId, room]) => {

                  const roomDevices = Object.entries(
                    room.devices || {}
                  ).map(
                    ([deviceId, device]) => ({
                      id: deviceId,
                      ...device,

                      homeId,
                      homeName:
                        home.name || "My Home",

                      floorId,
                      floorName:
                        floor.name || "Unnamed Floor",

                      roomId,
                      roomName:
                        room.name || "Unnamed Room",
                    })
                  );

                  const activeDevices =
                    roomDevices.filter(
                      (device) =>
                        device.on === true
                    ).length;

                  floorRoomCount++;

                  floorDeviceCount +=
                    roomDevices.length;

                  floorActiveCount +=
                    activeDevices;


                  // Room
                  firebaseRooms.push({
                    id: roomId,
                    name:
                      room.name ||
                      "Unnamed Room",

                    homeId,
                    homeName:
                      home.name || "My Home",

                    floorId,
                    floorName:
                      floor.name ||
                      "Unnamed Floor",

                    deviceCount:
                      roomDevices.length,

                    activeDevices,
                  });


                  // Devices
                  firebaseDevices.push(
                    ...roomDevices
                  );
                }
              );


              // Floor
              firebaseFloors.push({
                id: floorId,

                name:
                  floor.name ||
                  "Unnamed Floor",

                homeId,

                homeName:
                  home.name ||
                  "My Home",

                roomCount:
                  floorRoomCount,

                deviceCount:
                  floorDeviceCount,

                activeDevices:
                  floorActiveCount,
              });

            }
          );

        }
      );


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
                Reports
              </h1>

              <p>
                Loading report data...
              </p>

            </div>

          </section>

        </main>

      </div>
    );
  }


  // Summary
  const totalFloors =
    floors.length;

  const totalRooms =
    rooms.length;

  const totalDevices =
    devices.length;

  const onlineDevices =
    devices.filter(
      (device) =>
        device.on === true
    ).length;

  const offlineDevices =
    totalDevices - onlineDevices;


  return (
    <div className="app-layout">

      <Sidebar />

      <main className="main-content">

        <Header homeName="My Home" />

        <section className="dashboard-content">


          {/* Page Header */}

          <div className="welcome-section">

            <div>

              <h1>
                Reports
              </h1>

              <p>
                View your smart home
                status and device summary.
              </p>

            </div>

          </div>


          {/* Summary Cards */}

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


          {/* Floor Summary */}

          <section className="section">

            <div className="section-header">

              <div>

                <h2>
                  Floor Summary
                </h2>

                <p>
                  Device and room status
                  for each floor.
                </p>

              </div>

            </div>


            {floors.length === 0 ? (

              <div className="empty-state">

                <div>
                  🏢
                </div>

                <h3>
                  No floor data
                </h3>

                <p>
                  No floors are available
                  to generate a report.
                </p>

              </div>

            ) : (

              <div className="report-table-wrapper">

                <table className="report-table">

                  <thead>

                    <tr>

                      <th>
                        Floor
                      </th>

                      <th>
                        Home
                      </th>

                      <th>
                        Rooms
                      </th>

                      <th>
                        Devices
                      </th>

                      <th>
                        Active
                      </th>

                    </tr>

                  </thead>


                  <tbody>

                    {floors.map(
                      (floor) => (

                        <tr
                          key={`${floor.homeId}-${floor.id}`}
                        >

                          <td>
                            <strong>
                              {floor.name}
                            </strong>
                          </td>

                          <td>
                            {floor.homeName}
                          </td>

                          <td>
                            {floor.roomCount}
                          </td>

                          <td>
                            {floor.deviceCount}
                          </td>

                          <td>
                            <span className="report-online">
                              {floor.activeDevices}
                            </span>
                          </td>

                        </tr>

                      )
                    )}

                  </tbody>

                </table>

              </div>

            )}

          </section>


          {/* Device Status */}

          <section className="section">

            <div className="section-header">

              <div>

                <h2>
                  Device Status
                </h2>

                <p>
                  Current status of all
                  smart home devices.
                </p>

              </div>

            </div>


            {devices.length === 0 ? (

              <div className="empty-state">

                <div>
                  🔌
                </div>

                <h3>
                  No devices found
                </h3>

                <p>
                  There are no devices
                  available.
                </p>

              </div>

            ) : (

              <div className="report-table-wrapper">

                <table className="report-table">

                  <thead>

                    <tr>

                      <th>
                        Device
                      </th>

                      <th>
                        Type
                      </th>

                      <th>
                        Home
                      </th>

                      <th>
                        Floor
                      </th>

                      <th>
                        Room
                      </th>

                      <th>
                        Status
                      </th>

                    </tr>

                  </thead>


                  <tbody>

                    {devices.map(
                      (device) => {

                        const isOn =
                          device.on === true;

                        return (
                          <tr
                            key={`${device.homeId}-${device.floorId}-${device.roomId}-${device.id}`}
                          >

                            <td>
                              <strong>
                                {device.name}
                              </strong>
                            </td>

                            <td>
                              {device.type}
                            </td>

                            <td>
                              {device.homeName}
                            </td>

                            <td>
                              {device.floorName}
                            </td>

                            <td>
                              {device.roomName}
                            </td>

                            <td>

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

                            </td>

                          </tr>
                        );
                      }
                    )}

                  </tbody>

                </table>

              </div>

            )}

          </section>


          {/* Offline Summary */}

          <section className="section">

            <div className="section-header">

              <div>

                <h2>
                  Device Availability
                </h2>

                <p>
                  Current online and offline
                  device count.
                </p>

              </div>

            </div>


            <div className="stats-grid">

              <StatCard
                title="Online"
                value={onlineDevices}
                icon="🟢"
              />

              <StatCard
                title="Offline"
                value={offlineDevices}
                icon="⚫"
              />

            </div>

          </section>

        </section>

      </main>

    </div>
  );
}

export default Reports;