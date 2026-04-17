# QR Code Scanning & Email Service Explanation

## Overview
Your Event Management System has two key features:
1. **QR Code Generation & Scanning** - For ticket validation at events
2. **Email Service** - For sending tickets to users automatically

---

## 🎫 QR Code System

### How It Works

#### 1. **Ticket Code Generation** (BookingService.java)
When a user books an event:

```java
// Unique ticket code format: TKT-{eventId}-{userId}-{randomUUID}
// Example: TKT-5-12-A3B4C5D6
booking.setTicketCode("TKT-" + eventId + "-" + user.getId() + "-" + 
    UUID.randomUUID().toString().substring(0, 8).toUpperCase());
```

**Why this format?**
- `TKT-` prefix identifies it as a ticket
- `eventId` links to the specific event
- `userId` links to the attendee
- `UUID` ensures uniqueness (prevents duplicates)

#### 2. **QR Code Generation** (QrCodeService.java)
The QR code contains a URL that points to the scan validation page:

```
URL Format: http://localhost:8080/admin/scan?ticketCode=TKT-5-12-A3B4C5D6
```

**Technical Implementation:**
- Uses **ZXing (Zebra Crossing)** library
- Generates a PNG image
- Converts to Base64 string for embedding in HTML
- Size: 280x280 pixels (configurable)

```java
public String generateQrBase64(String content, int width, int height) {
    // 1. Create QR code writer
    QRCodeWriter writer = new QRCodeWriter();
    
    // 2. Encode the URL into a QR matrix
    BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
    
    // 3. Convert to PNG image
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
    
    // 4. Encode as Base64 for HTML embedding
    return Base64.getEncoder().encodeToString(baos.toByteArray());
}
```

#### 3. **Displaying the QR Code** (ticket.html)
The QR code is displayed on the ticket page:

```html
<!-- Base64 image embedded directly in HTML -->
<img th:src="'data:image/png;base64,' + ${qrBase64}" 
     alt="Ticket QR Code" 
     width="210" 
     height="210"/>
```

#### 4. **Scanning Process** (EventController.java)

**Step-by-step flow:**

1. **User shows QR code** on their phone
2. **Admin scans** with phone camera (native camera app)
3. **Camera reads URL** from QR code
4. **Browser opens** the scan validation page automatically
5. **System validates** the ticket code
6. **Result displayed** - Entry granted or denied

**Two ways to scan:**

**A. Automatic (QR Scan):**
```java
@GetMapping("/admin/scan")
public String scanPage(@RequestParam(required = false) String ticketCode) {
    if (ticketCode != null) {
        // QR code was scanned - validate automatically
        Booking booking = bookingService.validateAndUseTicket(ticketCode, username);
        model.addAttribute("booking", booking);
    }
    return "admin/scan";
}
```

**B. Manual Entry:**
```java
@PostMapping("/admin/scan")
public String processTicket(@RequestParam String ticketCode) {
    // Admin typed the code manually
    Booking booking = bookingService.validateAndUseTicket(ticketCode, username);
    model.addAttribute("booking", booking);
    return "admin/scan";
}
```

#### 5. **Ticket Validation Logic** (BookingService.java)

```java
public Booking validateAndUseTicket(String ticketCode, String adminUsername) {
    // 1. Find booking by ticket code
    Booking booking = bookingRepository.findByTicketCode(ticketCode)
        .orElseThrow(() -> new RuntimeException("Invalid ticket code."));
    
    // 2. Security check - admin must be event creator
    if (!booking.getEvent().getCreatedBy().equals(adminUsername)) {
        throw new RuntimeException("You can only scan tickets for your own events.");
    }
    
    // 3. Check if already used
    if ("USED".equals(booking.getStatus())) {
        throw new RuntimeException("Ticket has already been used.");
    }
    
    // 4. Check if cancelled
    if ("CANCELLED".equals(booking.getStatus())) {
        throw new RuntimeException("Ticket is cancelled.");
    }
    
    // 5. Mark as USED and save
    booking.setStatus("USED");
    return bookingRepository.save(booking);
}
```

**Validation Rules:**
- ✅ Ticket must exist in database
- ✅ Admin must be the event creator (security)
- ✅ Ticket must be CONFIRMED (not USED or CANCELLED)
- ✅ Each ticket can only be used once

---

## 📧 Email Service

### How It Works

#### 1. **Email Configuration** (application.properties)

```properties
# Enable/disable email feature
app.mail.enabled=false  # Set to true to enable

# Gmail SMTP settings
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password  # NOT your regular password!

# SMTP authentication
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Important Notes:**
- Currently **DISABLED** (`app.mail.enabled=false`)
- App works perfectly without email
- When disabled, ticket codes are printed to console

#### 2. **Automatic Email Sending** (BookingService.java)

When a booking is created:

```java
public Booking bookEvent(String username, Long eventId) {
    // ... create booking ...
    
    Booking saved = bookingRepository.save(booking);
    
    // Send email asynchronously (doesn't block the response)
    ticketEmailService.sendTicketEmail(saved);
    
    return saved;
}
```

**Key Point:** Email sending is **asynchronous** using `@Async`
- User doesn't wait for email to be sent
- Booking completes immediately
- Email sends in background

#### 3. **Email Service Implementation** (TicketEmailService.java)

```java
@Async  // Runs in separate thread
public void sendTicketEmail(Booking booking) {
    // 1. Check if email is enabled
    if (!mailEnabled || fromAddress.isBlank()) {
        System.out.println("Email skipped - not configured");
        return;
    }
    
    // 2. Generate QR code for email
    String scanUrl = baseUrl + "/admin/scan?ticketCode=" + booking.getTicketCode();
    byte[] qrBytes = Base64.getDecoder().decode(
        qrCodeService.generateQrBase64(scanUrl, 280, 280)
    );
    
    // 3. Create email with HTML content
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    
    helper.setFrom(fromAddress);
    helper.setTo(booking.getUser().getEmail());
    helper.setSubject("Your Ticket: " + booking.getEvent().getTitle());
    helper.setText(buildEmailHtml(booking, eventDate), true);
    
    // 4. Attach QR code image
    helper.addInline("qrcode", 
        new ByteArrayResource(qrBytes), 
        "image/png");
    
    // 5. Send email
    mailSender.send(message);
}
```

#### 4. **Email Template** (HTML)

The email includes:
- **Header** with event name and date (gradient background)
- **Attendee details** (name, venue, ticket code)
- **QR code image** embedded inline
- **Instructions** for using the ticket

```html
<!DOCTYPE html>
<html>
<body style="background:#f1f5f9;">
  <div style="max-width:520px;margin:32px auto;background:white;border-radius:20px;">
    <!-- Gradient header -->
    <div style="background:linear-gradient(135deg,#667eea,#764ba2);padding:32px;color:white;">
      <h2>Event Name</h2>
      <div>Date and Time</div>
    </div>
    
    <!-- Booking details -->
    <div style="padding:28px 32px;">
      <table>
        <tr><td>Attendee</td><td>John Doe</td></tr>
        <tr><td>Venue</td><td>Convention Center</td></tr>
        <tr><td>Ticket Code</td><td>TKT-5-12-A3B4C5D6</td></tr>
      </table>
    </div>
    
    <!-- QR code -->
    <div style="text-align:center;">
      <p>Show this QR code at the venue entrance</p>
      <img src="cid:qrcode" alt="QR Code" width="200" height="200"/>
    </div>
  </div>
</body>
</html>
```

---

## 🔧 How to Enable Email Service

### Step 1: Get Gmail App Password

1. Go to your Google Account: https://myaccount.google.com/
2. Enable 2-Factor Authentication (required)
3. Go to: https://myaccount.google.com/apppasswords
4. Create an app password for "Mail"
5. Copy the 16-character password

### Step 2: Update application.properties

```properties
# Enable email
app.mail.enabled=true

# Your Gmail credentials
spring.mail.username=your-email@gmail.com
spring.mail.password=xxxx xxxx xxxx xxxx  # 16-char app password
```

### Step 3: Restart Application

```bash
mvn spring-boot:run
```

### Step 4: Test

1. Book an event
2. Check your email inbox
3. You should receive a ticket with QR code

---

## 📱 Complete User Flow

### Booking Flow:
1. **User books event** → Click "Book Now"
2. **System creates booking** → Generates unique ticket code
3. **System generates QR code** → Encodes scan URL
4. **System sends email** (if enabled) → With ticket and QR code
5. **User receives confirmation** → "Booking confirmed!"

### Ticket Viewing Flow:
1. **User goes to "My Bookings"**
2. **Clicks "View Ticket"**
3. **QR code displayed** on screen
4. **User can print** or show on phone

### Scanning Flow:
1. **Admin opens scan page** → `/admin/scan`
2. **User shows QR code** on phone
3. **Admin scans with camera** → Camera reads URL
4. **Browser opens scan page** → With ticket code in URL
5. **System validates ticket** → Checks status, ownership
6. **Result displayed** → Green (valid) or Red (invalid)
7. **Ticket marked as USED** → Can't be used again

---

## 🔒 Security Features

### 1. **Unique Ticket Codes**
- UUID ensures no duplicates
- Impossible to guess valid codes

### 2. **Event Creator Validation**
- Admins can only scan tickets for their own events
- Prevents unauthorized access

### 3. **One-Time Use**
- Tickets marked as USED after scanning
- Prevents ticket sharing/reuse

### 4. **Status Checking**
- CONFIRMED → Valid for entry
- USED → Already scanned
- CANCELLED → Invalid

### 5. **Database Validation**
- All tickets verified against database
- No offline validation (prevents fraud)

---

## 🛠️ Technical Stack

### QR Code:
- **Library**: ZXing (Zebra Crossing)
- **Format**: QR_CODE
- **Output**: Base64-encoded PNG
- **Size**: 280x280 pixels

### Email:
- **Protocol**: SMTP
- **Provider**: Gmail (configurable)
- **Port**: 587 (TLS)
- **Format**: HTML with inline images
- **Async**: Yes (non-blocking)

### Dependencies (pom.xml):
```xml
<!-- QR Code Generation -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.1</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.1</version>
</dependency>

<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## 🐛 Troubleshooting

### QR Code Issues:

**Problem**: QR code not displaying
- Check if QrCodeService is generating Base64 correctly
- Verify ZXing dependencies are in pom.xml
- Check browser console for errors

**Problem**: QR code scans but doesn't validate
- Verify `app.base-url` in application.properties
- Check if URL format is correct
- Ensure admin is logged in

### Email Issues:

**Problem**: Emails not sending
- Check `app.mail.enabled=true`
- Verify Gmail app password (not regular password)
- Check console for error messages
- Ensure 2FA is enabled on Gmail account

**Problem**: Email sent but not received
- Check spam folder
- Verify recipient email address
- Check Gmail sending limits (500/day)

---

## 💡 Best Practices

### For Production:

1. **Change base URL**:
   ```properties
   app.base-url=https://yourdomain.com
   ```

2. **Use environment variables** for sensitive data:
   ```properties
   spring.mail.username=${MAIL_USERNAME}
   spring.mail.password=${MAIL_PASSWORD}
   ```

3. **Enable HTTPS** for secure QR scanning

4. **Add rate limiting** to prevent spam

5. **Log all scan attempts** for audit trail

6. **Add email templates** for different event types

7. **Implement retry logic** for failed emails

---

## 📊 Database Schema

### Bookings Table:
```sql
CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_date DATETIME,
    status VARCHAR(20),  -- CONFIRMED, USED, CANCELLED
    ticket_code VARCHAR(50) UNIQUE,  -- TKT-5-12-A3B4C5D6
    user_id BIGINT,
    event_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);
```

---

## 🎯 Summary

**QR Code System:**
- Generates unique ticket codes
- Creates QR codes with scan URLs
- Validates tickets at venue entrance
- Prevents duplicate/fraudulent entries

**Email Service:**
- Sends tickets automatically after booking
- Includes QR code and event details
- Works asynchronously (non-blocking)
- Optional (app works without it)

**Both systems work together** to provide a seamless ticketing experience from booking to venue entry!
