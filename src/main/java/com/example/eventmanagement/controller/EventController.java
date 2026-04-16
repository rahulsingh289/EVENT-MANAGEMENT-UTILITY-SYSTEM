package com.example.eventmanagement.controller;

import com.example.eventmanagement.model.Booking;
import com.example.eventmanagement.model.Event;
import com.example.eventmanagement.service.BookingService;
import com.example.eventmanagement.service.EventService;
import com.example.eventmanagement.service.QrCodeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EventController {

    private final EventService eventService;
    private final BookingService bookingService;
    private final QrCodeService qrCodeService;

    public EventController(EventService eventService,
                           BookingService bookingService,
                           QrCodeService qrCodeService) {
        this.eventService = eventService;
        this.bookingService = bookingService;
        this.qrCodeService = qrCodeService;
    }

    // ─── Public / User ───────────────────────────────────────────────────────

    @GetMapping("/events")
    public String listEvents(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("events", eventService.searchEvents(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("events", eventService.getAllEvents());
        }
        return "event-list";
    }

    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.getEventById(id));
        return "event-detail";
    }

    @PostMapping("/events/{id}/book")
    public String bookEvent(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            bookingService.bookEvent(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("success", "Booking confirmed! Check your tickets.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my-bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Booking cancelled.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my-bookings";
    }

    @GetMapping("/bookings/{id}/ticket")
    public String viewTicket(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             jakarta.servlet.http.HttpServletRequest request,
                             Model model) {
        Booking booking = bookingService.getBookingById(id);
        if (!booking.getUser().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/my-bookings";
        }
        // Build full scan URL so the QR code opens the validation page when scanned
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() != 80 && request.getServerPort() != 443
                    ? ":" + request.getServerPort() : "");
        String scanUrl = baseUrl + "/admin/scan?ticketCode=" + booking.getTicketCode();
        String qrBase64 = qrCodeService.generateQrBase64(scanUrl, 250, 250);
        model.addAttribute("booking", booking);
        model.addAttribute("qrBase64", qrBase64);
        return "ticket";
    }

    // ─── Admin ───────────────────────────────────────────────────────────────

    @GetMapping("/admin/events")
    public String adminEventList(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "admin/event-management";
    }

    @GetMapping("/admin/events/new")
    public String newEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "admin/event-form";
    }

    @PostMapping("/admin/events/new")
    public String createEvent(@ModelAttribute Event event,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        event.setCreatedBy(userDetails.getUsername());
        eventService.createEvent(event);
        redirectAttributes.addFlashAttribute("success", "Event created successfully.");
        return "redirect:/admin/events";
    }

    @GetMapping("/admin/events/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.getEventById(id));
        return "admin/event-form";
    }

    @PostMapping("/admin/events/{id}/edit")
    public String updateEvent(@PathVariable Long id,
                              @ModelAttribute Event event,
                              RedirectAttributes redirectAttributes) {
        eventService.updateEvent(id, event);
        redirectAttributes.addFlashAttribute("success", "Event updated.");
        return "redirect:/admin/events";
    }

    @PostMapping("/admin/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("success", "Event deleted.");
        return "redirect:/admin/events";
    }

    @GetMapping("/admin/events/{id}/bookings")
    public String eventBookings(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.getEventById(id));
        model.addAttribute("bookings", bookingService.getBookingsForEvent(id));
        return "admin/event-bookings";
    }

    // ─── QR Scan ─────────────────────────────────────────────────────────────

    @GetMapping("/admin/scan")
    public String scanPage(@RequestParam(required = false) String ticketCode, Model model) {
        // Phone camera scans QR → opens this URL → auto-validates
        if (ticketCode != null && !ticketCode.isBlank()) {
            try {
                Booking booking = bookingService.validateAndUseTicket(ticketCode);
                model.addAttribute("booking", booking);
            } catch (RuntimeException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "admin/scan";
    }

    @PostMapping("/admin/scan")
    public String processTicket(@RequestParam String ticketCode, Model model) {
        try {
            Booking booking = bookingService.validateAndUseTicket(ticketCode);
            model.addAttribute("booking", booking);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "admin/scan";
    }
}
