package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // 🔓 Inscription classique avec gestion d'erreur
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            System.out.println("Reçu : " + user); // 🔍 log de debug
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
         System.out.println("Erreur : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 🔍 log complet
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Erreur d'inscription"));
        }
    }


    // 🔓 Connexion classique + génération du token JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Optional<User> optionalUser = userService.findByEmail(user.getEmail());

        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();

            if ("google".equalsIgnoreCase(existingUser.getProvider())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Veuillez vous connecter avec Google"));
            }

            if (userService.checkPassword(user.getPassword(), existingUser.getPassword())) {
                String token = jwtUtil.generateToken(existingUser.getUsername(), existingUser.getRole());

                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("username", existingUser.getUsername());
                response.put("email", existingUser.getEmail());

                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Email ou mot de passe incorrect"));
    }

    // 🔐 Récupérer l'utilisateur connecté
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal(expression = "username") String username) {
        Optional<User> user = userService.findByUsername(username);
        return user.map(u -> ResponseEntity.ok(Map.of(
                "username", u.getUsername(),
                "email", u.getEmail(),
                "role", u.getRole()
        ))).orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Utilisateur non authentifié")));
    }

    // 🔓 Callback OAuth2 Google
    @GetMapping("/oauth2/success")
    public String oauth2Success(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        Optional<User> existingUser = userService.findByEmail(email);
        User user;

        if (existingUser.isEmpty()) {
            user = new User();
            user.setEmail(email);
            user.setUsername(name);
            user.setRole("USER");
            user.setProvider("google");
            userService.save(user);
        } else {
            user = existingUser.get();
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return "<html><body style='font-family:sans-serif; padding:2rem'>" +
               "<h2>Connexion Google réussie ✅</h2>" +
               "<p><strong>Nom d'utilisateur :</strong> " + user.getUsername() + "</p>" +
               "<p><strong>Email :</strong> " + user.getEmail() + "</p>" +
               "<p><strong>Token JWT :</strong><br><code>" + token + "</code></p>" +
               "</body></html>";
    }
}
