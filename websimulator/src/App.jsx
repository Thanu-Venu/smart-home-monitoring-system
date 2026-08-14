import{ BrowserRouter, Routes, Route, Navigate, } from "react-router-dom";

import Dashboard from "./pages/Dashboard";
import Rooms from "./pages/Rooms"
import RoomDetails from "./pages/RoomDetails";
import Alerts from "./pages/Alerts";
function App() {
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
          path="/rooms"
          element={<Rooms />}
      />

      <Route
          path="/rooms/:roomId"
          element={<RoomDetails />}
      />

      <Route 
          path="/alerts"
          element={<Alerts />}
      />
      
    </Routes>
    </BrowserRouter>
  );
}

export default App;