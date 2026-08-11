import { NavLink } from "react-router-dom";

function Sidebar() {
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
          to="/rooms"
          className={({ isActive }) =>
            `nav-item ${isActive ? "active" : ""}`
          }
        >
          <span>🏠</span>
          Rooms
        </NavLink>

        <a className="nav-item">
          <span>🔌</span>
          Devices
        </a>

        <a className="nav-item">
          <span>🚨</span>
          Alerts
        </a>

        <a className="nav-item">
          <span>📊</span>
          Reports
        </a>
      </nav>

      <div className="sidebar-bottom">
        <div className="connection-status">
          <span className="online-dot"></span>

          <div>
            <strong>System Online</strong>
            <small>All systems operational</small>
          </div>
        </div>
      </div>
    </aside>
  );
}

export default Sidebar;