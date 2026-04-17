package com.example.eventmanagement.service;

import com.example.eventmanagement.model.Booking;
import com.example.eventmanagement.model.Event;
import com.example.eventmanagement.model.User;
import com.example.eventmanagement.repository.BookingRepository;
import com.example.eventmanagement.repository.EventRepository;
import com.example.eventmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketEmailService ticketEmailService;

    public BookingService(BookingRepository bookingRepository,
                          EventRepository eventRepository,
                          UserRepository userRepository,
                          TicketEmailService ticketEmailService) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.ticketEmailService = ticketEmailService;
    }

    public Booking bookEvent(String username, Long eventId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Prevent duplicate active booking
        if (bookingRepository.existsByUserIdAndEventIdAndStatus(user.getId(), eventId, "CONFIRMED")) {
            throw new RuntimeException("You already have a confirmed booking for this event.");
        }

        // Check capacity
        long confirmedCount = bookingRepository.countByEventIdAndStatus(eventId, "CONFIRMED");
        if (confirmedCount >= event.getCapacity()) {
            throw new RuntimeException("Event is fully booked.");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("CONFIRMED");
        // Unique ticket code: eventId + userId + UUID
        booking.setTicketCode("TKT-" + eventId + "-" + user.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        Booking saved = bookingRepository.save(booking);
        ticketEmailService.sendTicketEmail(saved); // async — won't block the response
        return saved;
    }

    public void cancelBooking(Long bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    public List<Booking> getBookingsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findByUserId(user.getId());
    }

    public List<Booking> getBookingsForEvent(Long eventId) {
        return bookingRepository.findByEventId(eventId);
    }

    /**
     * Validates a ticket by its code. Marks it as USED if valid.
     * Returns the booking if valid, throws if invalid/already used/cancelled.
     * Also checks if the admin scanning is the event creator.
     */
    public Booking validateAndUseTicket(String ticketCode, String adminUsername) {
        Booking booking = bookingRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RuntimeException("Invalid ticket code."));

        // Check if the admin is the creator of this event
        if (!booking.getEvent().getCreatedBy().equals(adminUsername)) {
            throw new RuntimeException("You can only scan tickets for your own events.");
        }

        if ("USED".equals(booking.getStatus())) {
            throw new RuntimeException("Ticket has already been used.");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Ticket is cancelled.");
        }

        booking.setStatus("USED");
        return bookingRepository.save(booking);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
}
