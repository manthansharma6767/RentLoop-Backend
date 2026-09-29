package com.manthan.rentloop.repository;

import com.manthan.rentloop.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientEmailOrderByTimestampDesc(String recipientEmail);

    long countByRecipientEmailAndIsReadFalse(String recipientEmail);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientEmail = :email AND n.isRead = false")
    int markAllAsReadByRecipientEmail(@Param("email") String recipientEmail);
}
