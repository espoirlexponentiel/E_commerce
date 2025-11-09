package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    // 🛒 Transformer le panier en commande
    @Transactional
    public Order placeOrder(User user) {
        List<CartItem> cartItems = cartService.getCartItems(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Le panier est vide");
        }

        double total = 0.0;
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Stock insuffisant pour le produit : " + product.getNom() +
                        ". Stock disponible : " + product.getStock());
            }
            total += product.getPrix() * item.getQuantity();
        }

        Order order = Order.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.EN_ATTENTE)
                .totalAmount(total)
                .build();

        order = orderRepository.save(order);

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(product.getPrix())
                    .build();

            orderItemRepository.save(orderItem);
        }

        cartService.clearCart(user); // ✅ panier vidé proprement
        return order;
    }

    // 👤 Voir les commandes d’un utilisateur
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    // 👨‍💼 Voir toutes les commandes (admin)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // 🔄 Modifier le statut d’une commande (admin)
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    // ❌ Annuler une commande (user, < 10 min)
    @Transactional
    public void cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Accès refusé");
        }

        Duration duration = Duration.between(order.getCreatedAt(), LocalDateTime.now());
        if (duration.toMinutes() > 10) {
            throw new RuntimeException("Impossible d'annuler après 10 minutes");
        }

        order.setStatus(OrderStatus.ANNULEE);
        orderRepository.save(order);
    }
}
