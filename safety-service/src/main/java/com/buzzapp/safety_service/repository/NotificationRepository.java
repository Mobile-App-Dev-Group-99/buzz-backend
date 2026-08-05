package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByParentIdAndSchoolIdOrderBySentAtDesc(Long parentId, Long schoolId);

    List<Notification> findByRecipientRoleAndRecipientIdAndSchoolIdOrderBySentAtDesc(
            String recipientRole, Long recipientId, Long schoolId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.parentId = :parentId AND n.schoolId = :schoolId AND n.isRead = false")
    long countUnread(@Param("parentId") Long parentId, @Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientRole = :recipientRole AND n.recipientId = :recipientId AND n.schoolId = :schoolId AND n.isRead = false")
    long countUnreadByRecipient(@Param("recipientRole") String recipientRole, @Param("recipientId") Long recipientId, @Param("schoolId") Long schoolId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.parentId = :parentId AND n.schoolId = :schoolId")
    void markAllRead(@Param("parentId") Long parentId, @Param("schoolId") Long schoolId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientRole = :recipientRole AND n.recipientId = :recipientId AND n.schoolId = :schoolId")
    void markAllReadByRecipient(@Param("recipientRole") String recipientRole, @Param("recipientId") Long recipientId, @Param("schoolId") Long schoolId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markReadById(@Param("id") Long id);
}
