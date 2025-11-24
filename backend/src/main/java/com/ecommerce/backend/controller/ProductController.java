package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.service.CategoryService;
import com.ecommerce.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    // 📂 Dossier d’upload (dans le projet)
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // 🔓 Récupérer tous les produits
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // 🔓 Récupérer les produits par nom de catégorie
    @GetMapping("/category/{nom}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String nom) {
        return ResponseEntity.ok(productService.getProductsByCategory(nom));
    }

    // 🔓 Récupérer un produit par ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔐 Créer un nouveau produit (image facultative)
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(
            @RequestParam("nom") String productNom,
            @RequestParam("description") String productDescription,
            @RequestParam("prix") Double productPrix,
            @RequestParam("stock") Integer productStock,
            @RequestParam("category") String categoryNom,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {

        // 🔄 Normaliser le nom de catégorie
        String normalizedCategoryName = categoryNom.trim().toLowerCase();

        Category category = categoryService.getCategoryByNom(normalizedCategoryName)
                .orElseGet(() -> categoryService.createCategory(
                        new Category(null, normalizedCategoryName, null)
                ));

        // 📂 Gestion de l’image
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String originalName = imageFile.getOriginalFilename();
            if (originalName != null && !originalName.isBlank()) {
                String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(originalName);
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                File destination = new File(uploadDir, fileName);
                imageFile.transferTo(destination);
                imageUrl = "http://localhost:8080/uploads/" + fileName;
            }
        }

        // 🛒 Création du produit
        Product product = new Product();
        product.setNom(productNom);
        product.setDescription(productDescription);
        product.setPrix(productPrix);
        product.setStock(productStock);
        product.setImageUrl(imageUrl);
        product.setCategory(category);

        Product saved = productService.createProduct(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "✅ Produit créé avec succès", "product", saved)
        );
    }

    // 🔐 Supprimer un produit par ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // 🔐 Mettre à jour un produit existant
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product updatedProduct) {
        Product saved = productService.updateProduct(id, updatedProduct);
        return ResponseEntity.ok(saved);
    }
}
