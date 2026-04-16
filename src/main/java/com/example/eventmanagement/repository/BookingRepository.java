package com.example.eventmanagement.repository;

import com.example.eventmanagement.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByEventId(Long eventId);
    Optional<Booking> findByTicketCode(String ticketCode);
    long countByEventIdAndStatus(Long eventId, String status);
    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, String status);
    void deleteByEventId(Long eventId);
}
