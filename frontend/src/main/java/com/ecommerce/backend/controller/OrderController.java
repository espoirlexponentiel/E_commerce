package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.OrderSummaryDTO;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.service.OrderService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 🛒 Valider le panier et créer une commande
    @PostMapping
    public ResponseEntity<?> placeOrder(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Connexion requise pour passer une commande"));
        }

        Order order = orderService.placeOrder(user);
        return ResponseEntity.ok(Map.of(
            "orderId", order.getId(),
            "status", order.getStatus(),
            "totalAmount", order.getTotalAmount()
        ));
    }

    // 👤 Voir ses propres commandes
    @GetMapping
    public ResponseEntity<?> getUserOrders(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Connexion requise"));
        }

        List<Order> orders = orderService.getOrdersByUser(user);
        return ResponseEntity.ok(orders);
    }

    // 👨‍💼 Voir toutes les commandes (admin)
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderSummaryDTO> summaries = orders.stream()
            .map(OrderSummaryDTO::new)
            .toList();
        return ResponseEntity.ok(summaries);
    }


    // 🔄 Modifier le statut d’une commande (admin)
    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
                                               @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        orderService.updateOrderStatus(orderId, OrderStatus.valueOf(status));
        return ResponseEntity.ok(Map.of("message", "Statut mis à jour"));
    }

    // ❌ Annuler une commande (user, < 10 min)
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cancelOrder(@AuthenticationPrincipal User user,
                                         @PathVariable Long orderId) {
        try {
            orderService.cancelOrder(user, orderId);
            return ResponseEntity.ok(Map.of("message", "Commande annulée"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
