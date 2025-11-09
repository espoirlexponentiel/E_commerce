import { useEffect, useState } from "react";
import axios from "../api/axios";
import { Link } from "react-router-dom";

export default function HomePage() {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    axios.get("/products")
      .then(res => setProducts(res.data))
      .catch(err => console.error("Erreur chargement produits", err));
  }, []);

  return (
    <div>
      <h1>Bienvenue sur notre boutique 🛍️</h1>

      <div style={{ display: "flex", flexWrap: "wrap", gap: "20px" }}>
        {products.map(product => (
          <div key={product.id} style={{ border: "1px solid #ccc", padding: "10px", width: "200px" }}>
            <img src={product.imageUrl} alt={product.nom} style={{ width: "100%" }} />
            <h3>{product.nom}</h3>
            <p>{product.description}</p>
            <p><strong>{product.prix} FCFA</strong></p>
            <Link to={`/product/${product.id}`}>
              <button>Voir</button>
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
}
