function Header({ homeName }) {
  const avatarLetter =
    homeName?.trim()?.charAt(0)?.toUpperCase() || "H";
  return (
    <header className="header">
      <div>
        <span className="breadcrumb">Dashboard</span>
        <h2>{homeName}</h2>
      </div>

      <div className="header-actions">
        <button className="icon-button">🔔</button>

        <div className="profile">
          <div className="avatar">{avatarLetter}</div>

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