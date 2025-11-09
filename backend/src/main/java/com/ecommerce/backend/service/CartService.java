package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // 🔄 Récupérer ou créer le panier de l'utilisateur
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    // ➕ Ajouter un produit au panier
    @Transactional
    public void addToCart(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(CartItem.builder().cart(cart).product(product).quantity(0).build());

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);
    }

    // ✏️ Modifier la quantité
    @Transactional
    public void updateQuantity(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Article non trouvé dans le panier"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    // ❌ Supprimer un article
    @Transactional
    public void removeFromCart(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Article non trouvé dans le panier"));

        cartItemRepository.delete(item);
    }

    // 📦 Voir le contenu du panier
    public List<CartItem> getCartItems(User user) {
        Cart cart = getOrCreateCart(user);
        return cartItemRepository.findByCart(cart);
    }

    // 🧹 Vider le panier
    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteAllByCart(cart); // ✅ méthode dédiée
    }
}
