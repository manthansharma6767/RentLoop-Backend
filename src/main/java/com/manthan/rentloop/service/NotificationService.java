package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.NotificationResponse;
import com.manthan.rentloop.model.Notification;
import com.manthan.rentloop.model.NotificationType;
import com.manthan.rentloop.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Create and persist a notification, then push it in real-time
     * to the recipient's private WebSocket queue.
     */
    @Transactional
    public NotificationResponse createAndSend(String recipientEmail, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipientEmail(recipientEmail);
        notification.setMessage(message);
        notification.setType(type);

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = toResponse(saved);

        // Push real-time via STOMP
        messagingTemplate.convertAndSendToUser(
                recipientEmail,
                "/queue/notifications",
                response
        );

        return response;
    }

    /**
     * Fetch all notifications for a user, newest first.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String recipientEmail) {
        return notificationRepository.findByRecipientEmailOrderByTimestampDesc(recipientEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get the count of unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(String recipientEmail) {
        return notificationRepository.countByRecipientEmailAndIsReadFalse(recipientEmail);
    }

    /**
     * Mark a single notification as read.
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String userEmail) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        if (!notification.getRecipientEmail().equals(userEmail)) {
            throw new RuntimeException("You can only mark your own notifications as read");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    /**
     * Mark all notifications for a user as read.
     */
    @Transactional
    public void markAllAsRead(String recipientEmail) {
        notificationRepository.markAllAsReadByRecipientEmail(recipientEmail);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setRecipientEmail(notification.getRecipientEmail());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType().name());
        response.setRead(notification.isRead());
        response.setTimestamp(notification.getTimestamp());
        return response;
    }
}
