import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import Sidebar from "../components/Sidebar";
import Header from "../components/Header";

import { listenToHomes } from "../services/homeService";

function Floors() {
  const navigate = useNavigate();

  const [floors, setFloors] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = listenToHomes((allHomes) => {
      if (!allHomes) {
        setFloors([]);
        setLoading(false);
        return;
      }

      const firebaseFloors = [];

      // Loop through ALL homes
      Object.entries(allHomes).forEach(
        ([homeId, home]) => {

          // Loop through ALL floors
          Object.entries(home.floors || {}).forEach(
            ([floorId, floor]) => {

              let totalRooms = 0;
              let totalDevices = 0;

              // Count rooms and devices
              Object.entries(floor.rooms || {}).forEach(
                ([roomId, room]) => {

                  totalRooms++;

                  totalDevices += Object.keys(
                    room.devices || {}
                  ).length;
                }
              );

              firebaseFloors.push({
                id: floorId,
                name: floor.name || "Unnamed Floor",
                homeId: homeId,
                homeName: home.name || "Unknown Home",
                roomCount: totalRooms,
                deviceCount: totalDevices,
              });

            }
          );

        }
      );

      setFloors(firebaseFloors);
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
              <h1>Floors</h1>

              <p>
                Loading floors...
              </p>
            </div>

          </section>

        </main>

      </div>
    );
  }

  return (
    <div className="app-layout">

      <Sidebar />

      <main className="main-content">

        <Header homeName="My Home" />

        <section className="dashboard-content">

          {/* Page Header */}

          <div className="welcome-section">

            <div>

              <h1>Floors</h1>

              <p>
                Select a floor to view its rooms and devices.
              </p>

            </div>

          </div>


          {/* Floors */}

          {floors.length === 0 ? (

            <div className="empty-state">

              <div>🏢</div>

              <h3>
                No floors found
              </h3>

              <p>
                There are no floors available for this home.
              </p>

            </div>

          ) : (

            <div className="rooms-grid rooms-page-grid">

              {floors.map((floor) => (

                <div
                  className="room-card large-room-card"
                  key={`${floor.homeId}-${floor.id}`}
                  onClick={() =>
                    navigate(
                      `/floors/${floor.homeId}/${floor.id}`
                    )
                  }
                >

                  {/* Top */}

                  <div className="room-card-top">

                    <div className="room-icon">
                      🏢
                    </div>

                    <span className="arrow">
                      →
                    </span>

                  </div>


                  {/* Floor Name */}

                  <h3>
                    {floor.name}
                  </h3>


                  {/* Home Name */}

                  <p className="floor-home-name">
                    {floor.homeName}
                  </p>


                  {/* Room / Device Count */}

                  <p>
                    {floor.roomCount}{" "}
                    {floor.roomCount === 1
                      ? "Room"
                      : "Rooms"}{" "}
                    ·{" "}
                    {floor.deviceCount}{" "}
                    {floor.deviceCount === 1
                      ? "Device"
                      : "Devices"}
                  </p>


                  {/* Button */}

                  <button
                    className="view-room-button"
                    onClick={(event) => {
                      event.stopPropagation();

                      navigate(
                        `/floors/${floor.homeId}/${floor.id}`
                      );
                    }}
                  >
                    View Floor
                  </button>

                </div>

              ))}

            </div>

          )}

        </section>

      </main>

    </div>
  );
}

export default Floors;