import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "../api/axios";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post("/users/login", { email, password });

      const token = res.data.token;
      const role = res.data.role;

      localStorage.setItem("token", token);
      localStorage.setItem("role", role);
      console.log("Rôle reçu :", res.data.role);


      // ✅ Redirection selon le rôle
      if (role === "ADMIN") {
        navigate("/admin/commandes");
      } else {
        navigate("/categories");
      }
    } catch (err) {
      alert("❌ Erreur de connexion");
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <h2>Connexion</h2>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
        required
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Mot de passe"
        required
      />
      <button type="submit">Se connecter</button>
    </form>
  );
}
