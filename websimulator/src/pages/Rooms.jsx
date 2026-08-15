import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import Sidebar from "../components/Sidebar";
import Header from "../components/Header";

import { listenToHomes } from "../services/homeService";

function Rooms() {
  const navigate = useNavigate();

  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);

useEffect(() => {

  const unsubscribe = listenToHomes((allHomes) => {

    if (!allHomes) {
      setRooms([]);
      setLoading(false);
      return;
    }

    const firebaseRooms = [];

    // Loop through ALL homes
    Object.entries(allHomes).forEach(
      ([homeId, home]) => {

        // Loop through ALL floors
        Object.entries(home.floors || {}).forEach(
          ([floorId, floor]) => {

            // Loop through ALL rooms
            Object.entries(floor.rooms || {}).forEach(
              ([roomId, room]) => {

                const roomDevices =
                  Object.values(
                    room.devices || {}
                  );

                firebaseRooms.push({
                  id: roomId,
                  name: room.name,
                  type: room.type,
                  homeId: homeId,
                  homeName: home.name,
                  floorId: floorId,
                  devices: roomDevices,
                });

              }
            );

          }
        );

      }
    );

    setRooms(firebaseRooms);
    setLoading(false);

  });

  return () => unsubscribe();

}, []);

  if (loading) {
    return (
      <div className="app-layout">

        <Sidebar />

        <main className="main-content">

          <Header homeName="My Home" />

          <section className="dashboard-content">

            <div className="welcome-section">
              <h1>Rooms</h1>
              <p>Loading rooms...</p>
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

          <div className="welcome-section">
            <h1>Rooms</h1>

            <p>
              Select a room to view and control its devices.
            </p>
          </div>

          <div className="rooms-grid rooms-page-grid">

            {rooms.map((room) => (

              <div
                className="room-card large-room-card"
                key={room.id}
              >

                <div className="room-card-top">

                  <div className="room-icon">
                    🏠
                  </div>

                  <span className="arrow">
                    →
                  </span>

                </div>

                <h3>{room.name}</h3>

                <p>
                  {room.devices.length}{" "}
                  {room.devices.length === 1
                    ? "Device"
                    : "Devices"}
                </p>

                <button
                  className="view-room-button"
                  onClick={() =>
                    navigate(`/rooms/${homeId}/${room.id}`)
                  }
                >
                  View Room
                </button>

              </div>

            ))}

          </div>

        </section>

      </main>

    </div>
  );
}

export default Rooms;