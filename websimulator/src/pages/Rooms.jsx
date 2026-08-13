import { useNavigate } from "react-router-dom";
import { rooms, devices } from "../data/dummyData";
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";

function Rooms() {
  const navigate = useNavigate();
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

            {rooms.map((room) => {

              const roomDevices = devices.filter(
                (device) => device.roomId === room.id
              );

              return (
                <div className="room-card large-room-card" key={room.id}>

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
                    {roomDevices.length}{" "}
                    {roomDevices.length === 1
                      ? "Device"
                      : "Devices"}
                  </p>

                  <button className="view-room-button" onClick={() => navigate(`/rooms/${room.id}`)}>
                    View Room
                  </button>

                </div>
              );
            })}

          </div>

        </section>
      </main>
    </div>
  );
}

export default Rooms;