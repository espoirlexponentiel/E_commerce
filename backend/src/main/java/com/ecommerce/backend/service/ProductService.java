package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // 🔹 Récupérer tous les produits
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 🔹 Récupérer les produits par nom de catégorie (insensible à la casse)
    public List<Product> getProductsByCategory(String nomCategorie) {
        if (nomCategorie == null || nomCategorie.isBlank()) return List.of();
        String normalized = nomCategorie.trim().toLowerCase();
        return productRepository.findByCategoryNomIgnoreCase(normalized);
    }

    // 🔹 Récupérer un produit par ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // 🔹 Créer un nouveau produit
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    // 🔹 Supprimer un produit par ID (avec vérification)
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Produit introuvable avec l'ID : " + id);
        }
        productRepository.deleteById(id);
    }

    // 🔹 Mettre à jour un produit existant
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existing -> {
                    existing.setNom(updatedProduct.getNom());
                    existing.setDescription(updatedProduct.getDescription());
                    existing.setPrix(updatedProduct.getPrix());
                    existing.setStock(updatedProduct.getStock());
                    existing.setImageUrl(updatedProduct.getImageUrl());
                    existing.setCategory(updatedProduct.getCategory());
                    return productRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable avec l'ID : " + id));
    }
}
