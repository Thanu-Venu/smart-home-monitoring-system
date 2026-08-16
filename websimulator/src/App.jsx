import { useEffect, useState } from "react";
import{ BrowserRouter, Routes, Route, Navigate, } from "react-router-dom";

import Dashboard from "./pages/Dashboard";
import Floors from "./pages/Floors";
import FloorDetails from "./pages/FloorDetails";
import Rooms from "./pages/Rooms"
import RoomDetails from "./pages/RoomDetails";
import Alerts from "./pages/Alerts";
import Reports from "./pages/Reports";
import { ensureSignedIn } from "./firebase/firebaseConfig";

function App() {

  // The Realtime Database rules require auth != null on every
  // homes read/write. Every page in this app starts listening to
  // Firebase as soon as it mounts, so we have to wait for the
  // anonymous sign-in to finish *before* any route renders —
  // otherwise the very first render fires a read that the rules
  // reject and every page would show "not found" / stay empty.
  const [authReady, setAuthReady] = useState(false);
  const [authError, setAuthError] = useState(null);

  useEffect(() => {

    ensureSignedIn()
      .then(() => setAuthReady(true))
      .catch((error) => {
        console.error("Firebase sign-in failed:", error);
        setAuthError(error.message || "Could not connect to Firebase");
      });

  }, []);

  if (authError) {
    return (
      <div style={{ padding: 40, textAlign: "center" }}>
        <h2>Couldn't connect to Firebase</h2>
        <p>{authError}</p>
        <p>
          Check that Anonymous sign-in is enabled for this project under
          Firebase Console → Authentication → Sign-in method.
        </p>
      </div>
    );
  }

  if (!authReady) {
    return (
      <div style={{ padding: 40, textAlign: "center" }}>
        <p>Connecting to Firebase…</p>
      </div>
    );
  }

  return (
    <BrowserRouter>
    <Routes>
      <Route
          path="/"
          element={<Navigate to="/dashboard" replace />}
      />
      <Route
          path="/dashboard"
          element={<Dashboard />}
      />

      <Route
          path="/floors"
          element={<Floors />}
      />
        <Route
           path="/floors/:homeId/:floorId"
           element={<FloorDetails />}
        />

      <Route
          path="/rooms"
          element={<Rooms />}
      />

      <Route
          path="/rooms/:homeId/:roomId"
          element={<RoomDetails />}
      />

      <Route
          path="/alerts"
          element={<Alerts />}
      />

      <Route
           path="/reports"
           element={<Reports />}
      />

    </Routes>
    </BrowserRouter>
  );
}

export default App;