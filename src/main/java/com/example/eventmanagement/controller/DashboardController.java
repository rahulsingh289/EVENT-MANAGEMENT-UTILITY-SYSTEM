package com.example.eventmanagement.controller;

import com.example.eventmanagement.model.User;
import com.example.eventmanagement.repository.UserRepository;
import com.example.eventmanagement.service.BookingService;
import com.example.eventmanagement.service.EventService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final EventService eventService;
    private final BookingService bookingService;

    public DashboardController(UserRepository userRepository,
                               EventService eventService,
                               BookingService bookingService) {
        this.userRepository = userRepository;
        this.eventService = eventService;
        this.bookingService = bookingService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        model.addAttribute("currentUser", user);

        // Stats for dashboard cards
        List<?> allEvents = eventService.getAllEvents();
        List<?> myBookings = bookingService.getBookingsForUser(userDetails.getUsername());
        model.addAttribute("totalEvents", allEvents.size());
        model.addAttribute("myBookingCount", myBookings.size());

        return "dashboard";
    }

    @GetMapping("/my-bookings")
    public String showMyBookings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("bookings", bookingService.getBookingsForUser(userDetails.getUsername()));
        return "my-bookings";
    }
}
