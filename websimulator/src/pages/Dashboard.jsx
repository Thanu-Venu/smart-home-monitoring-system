import { home, rooms, devices } from "../data/dummyData";
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";
import StatCard from "../components/StatCard";

function Dashboard() {
  const totalRooms = rooms.length;
  const totalDevices = devices.length;
  const onlineDevices = devices.filter((device) => device.status).length;
  const alerts = 1;

  return (
    <div className="app-layout">
      <Sidebar />

      <main className="main-content">
        <Header homeName={home.name} />

        <section className="dashboard-content">
          <div className="welcome-section">
            <div>
              <h1>Welcome to {home.name} 👋</h1>
              <p>Monitor and control your smart home devices.</p>
            </div>
          </div>

          <div className="stats-grid">
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

            <StatCard
              title="Active Alerts"
              value={alerts}
              icon="⚠️"
            />
          </div>

          <section className="section">
            <div className="section-header">
              <div>
                <h2>Rooms</h2>
                <p>Manage devices by room</p>
              </div>
            </div>

            <div className="rooms-grid">
              {rooms.map((room) => (
                <div className="room-card" key={room.id}>
                  <div className="room-icon">🏠</div>

                  <div>
                    <h3>{room.name}</h3>
                    <p>
                      {
                        devices.filter(
                          (device) => device.roomId === room.id
                        ).length
                      }{" "}
                      devices
                    </p>
                  </div>

                  <span className="arrow">→</span>
                </div>
              ))}
            </div>
          </section>

          <section className="section">
            <div className="section-header">
              <div>
                <h2>Devices</h2>
                <p>Current device status</p>
              </div>
            </div>

            <div className="devices-grid">
              {devices.slice(0, 4).map((device) => (
                <div className="device-card" key={device.id}>
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
                        device.status
                          ? "status-badge on"
                          : "status-badge off"
                      }
                    >
                      {device.status ? "ON" : "OFF"}
                    </span>
                  </div>

                  <h3>{device.name}</h3>

                  <p>
                    {rooms.find(
                      (room) => room.id === device.roomId
                    )?.name}
                  </p>
                </div>
              ))}
            </div>
          </section>
        </section>
      </main>
    </div>
  );
}

export default Dashboard;