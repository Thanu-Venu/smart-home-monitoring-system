import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import Sidebar from "../components/Sidebar";
import Header from "../components/Header";

import { listenToHomes } from "../services/homeService";

function FloorDetails() {
  const { homeId, floorId } = useParams();
  const navigate = useNavigate();

  const [floor, setFloor] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = listenToHomes((allHomes) => {

      if (!allHomes) {
        setFloor(null);
        setRooms([]);
        setLoading(false);
        return;
      }

      const selectedHome = allHomes[homeId];

      if (!selectedHome) {
        setFloor(null);
        setRooms([]);
        setLoading(false);
        return;
      }

      const selectedFloor =
        selectedHome.floors?.[floorId];

      if (!selectedFloor) {
        setFloor(null);
        setRooms([]);
        setLoading(false);
        return;
      }

      // Store floor information
      setFloor({
        id: floorId,
        ...selectedFloor,
        homeId,
        homeName: selectedHome.name,
      });

      // Convert rooms object into array
      const firebaseRooms = Object.entries(
        selectedFloor.rooms || {}
      ).map(([roomId, room]) => {

        const roomDevices = Object.values(
          room.devices || {}
        );

        const activeDevices = roomDevices.filter(
          (device) => device.on === true
        ).length;

        return {
          id: roomId,
          name: room.name || "Unnamed Room",
          type: room.type || "ROOM",
          devices: roomDevices,
          deviceCount: roomDevices.length,
          activeDevices,
        };
      });

      setRooms(firebaseRooms);
      setLoading(false);
    });

    return () => unsubscribe();

  }, [homeId, floorId]);


  // Loading state

  if (loading) {
    return (
      <div className="app-layout">

        <Sidebar />

        <main className="main-content">

          <Header homeName="My Home" />

          <section className="dashboard-content">

            <div className="welcome-section">

              <h1>
                Floor Details
              </h1>

              <p>
                Loading floor...
              </p>

            </div>

          </section>

        </main>

      </div>
    );
  }


  // Floor not found

  if (!floor) {
    return (
      <div className="app-layout">

        <Sidebar />

        <main className="main-content">

          <Header homeName="My Home" />

          <section className="dashboard-content">

            <button
              className="back-button"
              onClick={() => navigate("/floors")}
            >
              ← Back to Floors
            </button>

            <div className="empty-state">

              <div>
                🏢
              </div>

              <h3>
                Floor not found
              </h3>

              <p>
                This floor could not be found in Firebase.
              </p>

            </div>

          </section>

        </main>

      </div>
    );
  }


  // Total devices in floor

  const totalDevices = rooms.reduce(
    (total, room) =>
      total + room.deviceCount,
    0
  );


  // Active devices in floor

  const activeDevices = rooms.reduce(
    (total, room) =>
      total + room.activeDevices,
    0
  );


  return (
    <div className="app-layout">

      <Sidebar />

      <main className="main-content">

        <Header homeName={floor.homeName} />

        <section className="dashboard-content">


          {/* Back Button */}

          <button
            className="back-button"
            onClick={() => navigate("/floors")}
          >
            ← Back to Floors
          </button>


          {/* Floor Header */}

          <div className="room-details-header">

            <div>

              <div className="room-details-icon">
                🏢
              </div>

              <div>

                <h1>
                  {floor.name}
                </h1>

                <p>
                  {rooms.length}{" "}
                  {rooms.length === 1
                    ? "room"
                    : "rooms"}{" "}
                  ·{" "}
                  {totalDevices}{" "}
                  {totalDevices === 1
                    ? "device"
                    : "devices"}{" "}
                  ·{" "}
                  {activeDevices} active
                </p>

              </div>

            </div>

          </div>


          {/* Rooms Section */}

          <section className="section">

            <div className="section-header">

              <div>

                <h2>
                  Rooms
                </h2>

                <p>
                  Select a room to view and control its devices.
                </p>

              </div>

            </div>


            {rooms.length === 0 ? (

              <div className="empty-state">

                <div>
                  🏠
                </div>

                <h3>
                  No rooms found
                </h3>

                <p>
                  There are no rooms assigned to this floor.
                </p>

              </div>

            ) : (

              <div className="rooms-grid rooms-page-grid">

                {rooms.map((room) => (

                  <div
                    className="room-card large-room-card"
                    key={`${homeId}-${floorId}-${room.id}`}
                    onClick={() =>
                      navigate(
                        `/rooms/${homeId}/${room.id}`
                      )
                    }
                  >

                    {/* Top */}

                    <div className="room-card-top">

                      <div className="room-icon">
                        🏠
                      </div>

                      <span className="arrow">
                        →
                      </span>

                    </div>


                    {/* Room Name */}

                    <h3>
                      {room.name}
                    </h3>


                    {/* Room Type */}

                    <p className="floor-home-name">
                      {room.type}
                    </p>


                    {/* Device Count */}

                    <p>

                      {room.deviceCount}{" "}
                      {room.deviceCount === 1
                        ? "Device"
                        : "Devices"}

                      {" · "}

                      {room.activeDevices} active

                    </p>


                    {/* View Room */}

                    <button
                      className="view-room-button"
                      onClick={(event) => {

                        event.stopPropagation();

                        navigate(
                          `/rooms/${homeId}/${room.id}`
                        );

                      }}
                    >
                      View Room
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

export default FloorDetails;