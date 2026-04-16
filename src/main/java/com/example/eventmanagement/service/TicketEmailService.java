package com.example.eventmanagement.service;

import com.example.eventmanagement.model.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class TicketEmailService {

    private final JavaMailSender mailSender;
    private final QrCodeService qrCodeService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    public TicketEmailService(JavaMailSender mailSender, QrCodeService qrCodeService) {
        this.mailSender = mailSender;
        this.qrCodeService = qrCodeService;
    }

    @Async
    public void sendTicketEmail(Booking booking) {
        if (!mailEnabled || fromAddress.isBlank() || fromAddress.equals("your-email@gmail.com")) {
            System.out.println("[Mail] Skipped — not configured. Ticket code: " + booking.getTicketCode());
            return;
        }
        try {
            String scanUrl = baseUrl + "/admin/scan?ticketCode=" + booking.getTicketCode();
            byte[] qrBytes = Base64.getDecoder().decode(
                    qrCodeService.generateQrBase64(scanUrl, 280, 280));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Your Ticket: " + booking.getEvent().getTitle());

            String eventDate = booking.getEvent().getEventDate()
                    .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy 'at' HH:mm"));

            helper.setText(buildEmailHtml(booking, eventDate), true);
            helper.addInline("qrcode",
                    new org.springframework.core.io.ByteArrayResource(qrBytes),
                    "image/png");

            mailSender.send(message);
            System.out.println("[Mail] Ticket sent to " + booking.getUser().getEmail());
        } catch (MessagingException e) {
            System.err.println("[Mail] Failed to send ticket email: " + e.getMessage());
        }
    }

    private String buildEmailHtml(Booking booking, String eventDate) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"/></head>
            <body style="margin:0;padding:0;background:#f1f5f9;font-family:'Segoe UI',Arial,sans-serif;">
              <div style="max-width:520px;margin:32px auto;background:white;border-radius:20px;overflow:hidden;box-shadow:0 8px 40px rgba(0,0,0,0.10);">
                <div style="background:linear-gradient(135deg,#667eea,#764ba2);padding:32px;color:white;">
                  <div style="font-size:13px;opacity:.8;margin-bottom:6px;">&#127915; Event Ticket</div>
                  <h2 style="margin:0 0 6px;font-size:22px;font-weight:700;">%s</h2>
                  <div style="opacity:.85;font-size:14px;">%s</div>
                </div>
                <div style="padding:28px 32px;">
                  <table style="width:100%%;border-collapse:collapse;">
                    <tr>
                      <td style="padding:10px 0;border-bottom:1px solid #f1f5f9;font-size:13px;color:#94a3b8;text-transform:uppercase;letter-spacing:.5px;width:110px;">Attendee</td>
                      <td style="padding:10px 0;border-bottom:1px solid #f1f5f9;font-weight:600;color:#1e293b;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:10px 0;border-bottom:1px solid #f1f5f9;font-size:13px;color:#94a3b8;text-transform:uppercase;letter-spacing:.5px;">Venue</td>
                      <td style="padding:10px 0;border-bottom:1px solid #f1f5f9;font-weight:600;color:#1e293b;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:10px 0;font-size:13px;color:#94a3b8;text-transform:uppercase;letter-spacing:.5px;">Ticket Code</td>
                      <td style="padding:10px 0;font-family:monospace;font-size:13px;color:#475569;">%s</td>
                    </tr>
                  </table>
                </div>
                <div style="border-top:2px dashed #e2e8f0;margin:0 32px;"></div>
                <div style="text-align:center;padding:28px 32px 32px;">
                  <p style="margin:0 0 12px;font-size:13px;color:#64748b;">&#128241; Show this QR code at the venue entrance</p>
                  <img src="cid:qrcode" alt="Ticket QR Code"
                       style="width:200px;height:200px;border-radius:12px;border:3px solid #f1f5f9;padding:8px;"/>
                  <p style="margin:16px 0 0;font-size:12px;color:#94a3b8;">This ticket is valid for one entry only.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                booking.getEvent().getTitle(), eventDate,
                booking.getUser().getUsername(),
                booking.getEvent().getVenue(),
                booking.getTicketCode()
        );
    }
}
