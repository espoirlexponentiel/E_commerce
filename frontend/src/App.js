import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import HomePage from "./pages/Homepage";
import LoginPage from "./pages/Loginpage";
import RegisterPage from "./pages/Registerpage";
import CategoriesPage from "./pages/CategoriesPage";
import AdminCommandesPage from "./pages/AdminCommandesPage";


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/categories" element={<CategoriesPage />} />
        <Route path="/admin/commandes" element={<AdminCommandesPage />} />
      </Routes>
    </Router>
  );
}

export default App;
