import { BrowserRouter, Routes, Route, Navigate, } from "react-router-dom";

import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Floors from "./pages/Floors";
import FloorDetails from "./pages/FloorDetails";
import Rooms from "./pages/Rooms"
import RoomDetails from "./pages/RoomDetails";
import Alerts from "./pages/Alerts";
import Reports from "./pages/Reports";

function App() {

  // Auth is now handled per-user via the Login page (real
  // email/password sign-in, shared with the Android app's accounts)
  // instead of an anonymous sign-in gate before any route rendered.
  // homeService.js's listenToHomes already reacts to the current
  // Firebase Auth state itself (via onAuthStateChanged) and calls
  // back with null until someone is signed in, so routes can render
  // immediately -- there's nothing left here to wait on.

  return (
    <BrowserRouter>
    <Routes>

      <Route
          path="/"
          element={<Navigate to="/login" replace />}
      />
      <Route
        path="/login"
        element={<Login />}
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