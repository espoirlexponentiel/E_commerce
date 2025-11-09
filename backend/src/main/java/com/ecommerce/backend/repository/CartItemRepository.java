package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 🔍 Tous les articles d’un panier
    List<CartItem> findByCart(Cart cart);

    // 🔍 Un article spécifique dans un panier
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    // ✅ Vérifie si un produit est déjà dans le panier
    boolean existsByCartAndProduct(Cart cart, Product product);

    // ❌ Supprime tous les articles d’un panier
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart")
    void deleteAllByCart(@Param("cart") Cart cart);

    // 🔢 Compte le nombre d’articles dans un panier
    long countByCart(Cart cart);
}
