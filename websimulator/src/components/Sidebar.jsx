import { signOut } from "firebase/auth";
import { useNavigate, NavLink } from "react-router-dom";

import { auth } from "../firebase/firebaseConfig";


function Sidebar() {
  const navigate = useNavigate();

const handleLogout = async () => {
  try {
    await signOut(auth);

    console.log("User logged out");

    navigate("/login");

  } catch (error) {
    console.error("Logout failed:", error);
  }
};

  return (
    <aside className="sidebar">
      <div className="logo">
        <div className="logo-icon">🏠</div>

        <div>
          <h2>Smart Home</h2>
          <span>Simulator</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        <p className="nav-title">MAIN MENU</p>

        <NavLink
          to="/dashboard"
          className={({ isActive }) =>
            `nav-item ${isActive ? "active" : ""}`
          }
        >
          <span>📊</span>
          Dashboard
        </NavLink>

        <NavLink
            to="/floors"
            className={({ isActive }) =>
                `nav-item ${isActive ? "active" : ""}`
            }
        >
            <span>🏢</span>
                Floors
        </NavLink>

        <NavLink
            to="/alerts"
            className={({ isActive }) =>
            `nav-item ${isActive ? "active" : ""}`
          }
        >
          <span>🚨</span>
            Alerts
        </NavLink>

        <NavLink
            to="/reports"
            className={({ isActive }) =>
                `nav-item ${isActive ? "active" : ""}`
            }
        >
          <span>📊</span>
            Reports
        </NavLink>

      </nav>

      <div className="sidebar-bottom">
        <div className="connection-status">
          <span className="online-dot"></span>

          <div>
            <strong>System Online</strong>
            <small>All systems operational</small>
          </div>
        </div>
            <button
                className="logout-button"
                onClick={handleLogout}
            >
                <span className="logout-icon">↪</span>
                  Logout
            </button>
      </div>
    </aside>
  );
}

export default Sidebar;