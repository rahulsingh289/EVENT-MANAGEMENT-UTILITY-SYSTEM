package com.example.eventmanagement.service;

import com.example.eventmanagement.model.Event;
import com.example.eventmanagement.repository.BookingRepository;
import com.example.eventmanagement.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public EventService(EventRepository eventRepository, BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getEventsByCreator(String username) {
        return eventRepository.findByCreatedBy(username);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
    }

    public Event createEvent(Event event) {
        if (eventRepository.existsByTitleAndCreatedBy(event.getTitle(), event.getCreatedBy())) {
            throw new RuntimeException("You already have an event with this title. Please use a different title.");
        }
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, Event updated, String username) {
        Event existing = getEventById(id);
        if (!existing.getCreatedBy().equals(username)) {
            throw new RuntimeException("You can only edit your own events.");
        }
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setVenue(updated.getVenue());
        existing.setEventDate(updated.getEventDate());
        existing.setCapacity(updated.getCapacity());
        existing.setPrice(updated.getPrice());
        return eventRepository.save(existing);
    }

    @Transactional
    public void deleteEvent(Long id, String username) {
        Event event = getEventById(id);
        if (!event.getCreatedBy().equals(username)) {
            throw new RuntimeException("You can only delete your own events.");
        }
        // Remove all bookings for this event first to avoid FK constraint violation
        bookingRepository.deleteByEventId(id);
        eventRepository.deleteById(id);
    }

    public List<Event> searchEvents(String query) {
        return eventRepository.findByTitleContainingIgnoreCase(query);
    }
}
