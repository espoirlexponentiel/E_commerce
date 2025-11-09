package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.service.CategoryService;
import com.ecommerce.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    private static final String UPLOAD_DIR = "uploads/";

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
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(
            @RequestParam("name") String nom,
            @RequestParam("desc") String description,
            @RequestParam("price") Double prix,
            @RequestParam("stoc") Integer stock,
            @RequestParam("category") String categoryNom,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {

        String normalizedCategoryName = categoryNom.trim().toLowerCase();

        Category category = categoryService.getCategoryByNom(normalizedCategoryName)
                .orElseGet(() -> categoryService.createCategory(new Category(null, normalizedCategoryName, null)));

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String originalName = imageFile.getOriginalFilename();
            if (originalName != null && !originalName.isBlank()) {
                String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(originalName);
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                File destination = new File(uploadDir, fileName);
                imageFile.transferTo(destination);
                imageUrl = "/uploads/" + fileName;
            }
        }

        Product product = new Product();
        product.setNom(nom);
        product.setDescription(description);
        product.setPrix(prix);
        product.setStock(stock);
        product.setImageUrl(imageUrl);
        product.setCategory(category);

        Product saved = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
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
