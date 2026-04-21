package es.companeros.service;

import es.companeros.model.*;
import es.companeros.repository.NotificationRepository;
import es.companeros.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                                UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notifyUser(Long userId, Long houseId, String message, NotificationType type) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setHouseId(houseId);
        n.setMessage(message);
        n.setType(type);
        notificationRepository.save(n);
    }

    public void notifyHouse(House house, Long excludeUserId, String message, NotificationType type) {
        userRepository.findByHouse(house).forEach(member -> {
            if (!member.getId().equals(excludeUserId)) {
                notifyUser(member.getId(), house.getId(), message, type);
            }
        });
    }

    public void notifyTaskAssigned(Long assignedUserId, Long createdByUserId, Long houseId, String taskDescription) {
        if (assignedUserId.equals(createdByUserId)) return;
        notifyUser(assignedUserId, houseId,
                "Se te ha asignado una tarea: " + taskDescription,
                NotificationType.TASK_ASSIGNED);
    }

    public void notifyShoppingAdded(House house, Long addedByUserId) {
        List<Notification> recent = notificationRepository
                .findByHouseIdAndTypeAndCreatedAtAfterAndReadFalse(
                        house.getId(),
                        NotificationType.SHOPPING_ADDED,
                        LocalDateTime.now().minusMinutes(5));
        if (!recent.isEmpty()) return;
        notifyHouse(house, addedByUserId,
                "Se han añadido productos a la lista de la compra.",
                NotificationType.SHOPPING_ADDED);
    }

    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }
}
