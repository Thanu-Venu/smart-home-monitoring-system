function Header({ homeName }) {
  return (
    <header className="header">
      <div>
        <span className="breadcrumb">Dashboard</span>
        <h2>{homeName}</h2>
      </div>

      <div className="header-actions">
        <button className="icon-button">🔔</button>

        <div className="profile">
          <div className="avatar">P</div>

          <div>
            <strong>Owner</strong>
            <span>Administrator</span>
          </div>
        </div>
      </div>
    </header>
  );
}

export default Header;