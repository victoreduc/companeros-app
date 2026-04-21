package es.companeros.controller;

import es.companeros.model.Notification;
import es.companeros.model.User;
import es.companeros.repository.UserRepository;
import es.companeros.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Autowired
    public NotificationController(NotificationService notificationService,
                                   UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/notifications")
    public List<Notification> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return notificationService.getForUser(user.getId());
    }

    @GetMapping("/api/notifications/unread-count")
    public Map<String, Long> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return Map.of("count", notificationService.getUnreadCount(user.getId()));
    }

    @PostMapping("/api/notifications/mark-all-read")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        notificationService.markAllRead(user.getId());
        return ResponseEntity.ok().build();
    }
}
