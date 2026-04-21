package es.companeros.repository;

import es.companeros.model.Notification;
import es.companeros.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);

    List<Notification> findByHouseIdAndTypeAndCreatedAtAfterAndReadFalse(
            Long houseId, NotificationType type, LocalDateTime after);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId")
    void markAllReadByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.houseId = :houseId")
    void deleteByHouseId(@Param("houseId") Long houseId);
}
