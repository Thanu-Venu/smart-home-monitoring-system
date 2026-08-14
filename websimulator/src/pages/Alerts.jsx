import { useEffect, useState } from "react";

import Sidebar from "../components/Sidebar";
import Header from "../components/Header";

import {
  listenToHomes,
  updateDeviceStatus,
} from "../services/homeService";

function Alerts() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);


  // Listen to all homes
  useEffect(() => {

    const unsubscribe = listenToHomes((allHomes) => {

      if (!allHomes) {
        setAlerts([]);
        setLoading(false);
        return;
      }


      const activeAlerts = [];


      // Search ALL homes
      Object.entries(allHomes).forEach(
        ([homeId, home]) => {

          // Search ALL floors
          Object.entries(
            home.floors || {}
          ).forEach(
            ([floorId, floor]) => {

              // Search ALL rooms
              Object.entries(
                floor.rooms || {}
              ).forEach(
                ([roomId, room]) => {

                  // Search ALL devices
                  Object.entries(
                    room.devices || {}
                  ).forEach(
                    ([deviceId, device]) => {

                      const deviceType =
                        String(
                          device.type || ""
                        ).toUpperCase();


                      const isOn =
                        device.on === true;


                      // Iron ON = Safety Alert
                      if (
                        deviceType === "IRON" &&
                        isOn
                      ) {

                        activeAlerts.push({

                          id: deviceId,

                          name:
                            device.name ||
                            "Iron",

                          type:
                            deviceType,

                          homeId,

                          homeName:
                            home.name ||
                            "My Home",

                          floorId,

                          floorName:
                            floor.name ||
                            "Unknown Floor",

                          roomId,

                          roomName:
                            room.name ||
                            "Unknown Room",

                          on: true,

                          maxOnDurationMinutes:
                            device.maxOnDurationMinutes,

                        });

                      }

                    }
                  );

                }
              );

            }
          );

        }
      );


      setAlerts(activeAlerts);
      setLoading(false);

    });


    return () => unsubscribe();

  }, []);


  // Turn OFF specific iron
  const turnOffIron = async (alert) => {

    try {

      await updateDeviceStatus(
        alert.homeId,
        alert.floorId,
        alert.roomId,
        alert.id,
        false
      );

    } catch (error) {

      console.error(
        "Failed to turn off iron:",
        error
      );

    }

  };


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
                Alerts
              </h1>

              <p>
                Loading safety information...
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

            <h1>
              Alerts
            </h1>

            <p>
              Monitor safety alerts and important
              device warnings.
            </p>

          </div>


          {/* Alert Summary */}

          <div className="alert-summary">

            <div className="alert-summary-icon">
              ⚠️
            </div>

            <div>

              <p>
                Active Safety Alerts
              </p>

              <h2>
                {alerts.length}
              </h2>

            </div>

          </div>


          {/* Active Alerts */}

          {alerts.length > 0 ? (

            <section className="section">

              <div className="section-header">

                <div>

                  <h2>
                    Active Safety Alerts
                  </h2>

                  <p>
                    Devices that require your attention.
                  </p>

                </div>

              </div>


              <div className="alerts-dashboard-list">

                {alerts.map((alert) => (

                  <div
                    className="alert-card"
                    key={`${alert.homeId}-${alert.floorId}-${alert.roomId}-${alert.id}`}
                  >

                    {/* Alert Icon */}

                    <div className="alert-icon">
                      🔥
                    </div>


                    <div className="alert-content">


                      {/* Header */}

                      <div className="alert-header">

                        <div>

                          <h2>
                            {alert.name}
                          </h2>

                          <p>
                            {alert.roomName}
                          </p>

                        </div>


                        <span className="alert-status">
                          ACTIVE
                        </span>

                      </div>


                      {/* Warning */}

                      <div className="alert-warning">

                        ⚠️ The iron is currently
                        switched ON. Please check
                        the appliance and turn it OFF
                        when it is no longer needed.

                      </div>


                      {/* Details */}

                      <div className="alert-details">


                        <div>

                          <span>
                            Status
                          </span>

                          <strong>
                            🟢 ON
                          </strong>

                        </div>


                        <div>

                          <span>
                            Device Type
                          </span>

                          <strong>
                            IRON
                          </strong>

                        </div>


                        <div>

                          <span>
                            Home
                          </span>

                          <strong>
                            {alert.homeName}
                          </strong>

                        </div>


                        <div>

                          <span>
                            Floor
                          </span>

                          <strong>
                            {alert.floorName}
                          </strong>

                        </div>


                        <div>

                          <span>
                            Room
                          </span>

                          <strong>
                            {alert.roomName}
                          </strong>

                        </div>

                      </div>


                      {/* Maximum Duration */}

                      {alert.maxOnDurationMinutes && (

                        <div className="alert-details">

                          <div>

                            <span>
                              Maximum ON Duration
                            </span>

                            <strong>
                              {
                                alert.maxOnDurationMinutes
                              }{" "}
                              minutes
                            </strong>

                          </div>

                        </div>

                      )}


                      {/* Turn OFF */}

                      <button
                        className="alert-action-button"
                        onClick={() =>
                          turnOffIron(alert)
                        }
                      >
                        Turn OFF Iron
                      </button>


                    </div>

                  </div>

                ))}

              </div>

            </section>

          ) : (

            /* No Alerts */

            <div className="no-alerts">

              <div className="no-alerts-icon">
                ✓
              </div>

              <h2>
                No Active Alerts
              </h2>

              <p>
                All monitored devices are currently
                operating safely.
              </p>

            </div>

          )}

        </section>

      </main>

    </div>
  );
}

export default Alerts;