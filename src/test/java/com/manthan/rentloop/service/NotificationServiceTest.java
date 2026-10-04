package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.NotificationResponse;
import com.manthan.rentloop.model.Notification;
import com.manthan.rentloop.model.NotificationType;
import com.manthan.rentloop.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setRecipientEmail("user@example.com");
        notification.setMessage("Test Notification");
        notification.setType(NotificationType.SYSTEM_ALERT);
        notification.setRead(false);
        notification.setTimestamp(Instant.now());
    }

    @Test
    void createAndSend_Success_PersistsAndPushesToWebSocket() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(1L);
            return n;
        });

        NotificationResponse response = notificationService.createAndSend("user@example.com", "Hello!", NotificationType.SYSTEM_ALERT);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("user@example.com", response.getRecipientEmail());

        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("user@example.com"),
                eq("/queue/notifications"),
                any(NotificationResponse.class)
        );
    }

    @Test
    void getNotifications_Success() {
        when(notificationRepository.findByRecipientEmailOrderByTimestampDesc("user@example.com"))
                .thenReturn(List.of(notification));

        List<NotificationResponse> list = notificationService.getNotifications("user@example.com");

        assertEquals(1, list.size());
        assertEquals("Test Notification", list.get(0).getMessage());
    }

    @Test
    void getUnreadCount_Success() {
        when(notificationRepository.countByRecipientEmailAndIsReadFalse("user@example.com")).thenReturn(3L);

        long count = notificationService.getUnreadCount("user@example.com");

        assertEquals(3L, count);
    }

    @Test
    void markAsRead_Success_ByRecipient() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = notificationService.markAsRead(1L, "user@example.com");

        assertTrue(response.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void markAsRead_UnauthorizedUser_ThrowsAccessDeniedException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class, () ->
                notificationService.markAsRead(1L, "stranger@example.com")
        );
    }

    @Test
    void markAllAsRead_Success() {
        notificationService.markAllAsRead("user@example.com");

        verify(notificationRepository, times(1)).markAllAsReadByRecipientEmail("user@example.com");
    }
}
