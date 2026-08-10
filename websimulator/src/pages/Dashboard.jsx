import { useEffect, useState } from "react";
import { listenToHome } from "../services/homeService";

function Dashboard() {

  const [home, setHome] = useState(null);

  useEffect(() => {

    const unsubscribe = listenToHome((data) => {
      setHome(data);
    });

    return () => unsubscribe();

  }, []);

  if (!home) {
    return <h2>Loading home...</h2>;
  }

  return (
    <div>
      <h1>Smart Home Simulator</h1>

      <h2>{home.name}</h2>

      <p>Home ID: {home.id}</p>

      <p>Owner ID: {home.ownerId}</p>
    </div>
  );
}

export default Dashboard;