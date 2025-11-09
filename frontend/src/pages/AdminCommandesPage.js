import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "../api/axios";

export default function AdminCommandesPage() {
  const [commandes, setCommandes] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");

    if (!token || role !== "ADMIN") {
      navigate("/login"); // 🔐 redirige si non connecté ou non admin
      return;
    }

    axios
      .get("/orders", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => {
        setCommandes(res.data);
      })
      .catch((err) => {
        console.error("Erreur chargement commandes :", err);
        alert("Impossible de charger les commandes");
      });
  }, [navigate]);

  return (
    <div>
      <h2>Commandes reçues</h2>
      <ul>
        {commandes.map((cmd) => (
          <li key={cmd.id}>
            <strong>Commande #{cmd.id}</strong> — {cmd.status} — {cmd.total} €
          </li>
        ))}
      </ul>
    </div>
  );
}
