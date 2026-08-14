import { useState } from "react";
import { devices, rooms } from "../data/dummyData";

import Sidebar from "../components/Sidebar";
import Header from "../components/Header";

function Alerts() {
  const [ironStatus, setIronStatus] = useState(
    devices.find((device) => device.type === "iron")?.status || false
  );

  const iron = devices.find(
    (device) => device.type === "iron"
  );

  const ironRoom = rooms.find(
    (room) => room.id === iron?.roomId
  );

  return (
    <div className="app-layout">

      <Sidebar />

      <main className="main-content">

        <Header homeName="My Home" />

        <section className="dashboard-content">

          <div className="welcome-section">
            <h1>Alerts</h1>
            <p>
              Monitor safety alerts and important device warnings.
            </p>
          </div>

          {/* Summary */}

          <div className="alert-summary">

            <div className="alert-summary-icon">
              ⚠️
            </div>

            <div>
              <p>Active Safety Alerts</p>
              <h2>{ironStatus ? "1" : "0"}</h2>
            </div>

          </div>

          {/* Iron Alert */}

          {ironStatus ? (

            <div className="alert-card">

              <div className="alert-icon">
                🔥
              </div>

              <div className="alert-content">

                <div className="alert-header">

                  <div>
                    <h2>{iron.name}</h2>

                    <p>
                      {ironRoom?.name || "Unknown Room"}
                    </p>
                  </div>

                  <span className="alert-status">
                    ACTIVE
                  </span>

                </div>

                <div className="alert-warning">
                  ⚠️ The iron is currently switched ON.
                  Please check the appliance and turn it OFF
                  when it is no longer needed.
                </div>

                <div className="alert-details">

                  <div>
                    <span>Status</span>
                    <strong>🟢 ON</strong>
                  </div>

                  <div>
                    <span>Device Type</span>
                    <strong>IRON</strong>
                  </div>

                  <div>
                    <span>Room</span>
                    <strong>
                      {ironRoom?.name || "Unknown"}
                    </strong>
                  </div>

                </div>

                <button
                  className="alert-action-button"
                  onClick={() => setIronStatus(false)}
                >
                  Turn OFF Iron
                </button>

              </div>

            </div>

          ) : (

            <div className="no-alerts">

              <div className="no-alerts-icon">
                ✓
              </div>

              <h2>No Active Alerts</h2>

              <p>
                All monitored devices are currently operating safely.
              </p>

            </div>

          )}

        </section>

      </main>

    </div>
  );
}

export default Alerts;