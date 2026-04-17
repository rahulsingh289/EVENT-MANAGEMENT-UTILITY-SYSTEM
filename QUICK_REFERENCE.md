# Quick Reference Guide

## 🎯 QR Code & Email Service - At a Glance

### QR Code System

**What it does:**
- Generates unique ticket codes for each booking
- Creates QR codes that can be scanned at venue entrance
- Validates tickets and prevents fraud

**Key Components:**
```
QrCodeService.java       → Generates QR codes
BookingService.java      → Creates tickets & validates
EventController.java     → Handles scan requests
```

**Ticket Code Format:**
```
TKT-{eventId}-{userId}-{UUID}
Example: TKT-5-12-A3B4C5D6
```

**QR Code Contains:**
```
http://localhost:8080/admin/scan?ticketCode=TKT-5-12-A3B4C5D6
```

**How to Scan:**
1. Open phone camera
2. Point at QR code
3. Tap notification to open link
4. Ticket validates automatically

---

### Email Service

**What it does:**
- Sends ticket emails automatically after booking
- Includes QR code and event details
- Works asynchronously (doesn't slow down app)

**Key Components:**
```
TicketEmailService.java  → Sends emails
application.properties   → Email configuration
```

**Current Status:**
```
❌ DISABLED (app.mail.enabled=false)
✅ App works perfectly without it
```

**To Enable:**
1. Get Gmail App Password
2. Update application.properties:
   ```properties
   app.mail.enabled=true
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   ```
3. Restart app

---

## 📋 Common Tasks

### Generate QR Code
```java
String scanUrl = baseUrl + "/admin/scan?ticketCode=" + ticketCode;
String qrBase64 = qrCodeService.generateQrBase64(scanUrl, 280, 280);
```

### Validate Ticket
```java
Booking booking = bookingService.validateAndUseTicket(ticketCode, adminUsername);
// Throws exception if invalid
// Returns booking if valid
// Updates status to USED
```

### Send Email
```java
ticketEmailService.sendTicketEmail(booking);
// Runs asynchronously
// Skips if email disabled
```

---

## 🔍 Validation Rules

| Check | Rule | Error Message |
|-------|------|---------------|
| Exists | Ticket must be in database | "Invalid ticket code" |
| Owner | Admin must be event creator | "You can only scan tickets for your own events" |
| Status | Must be CONFIRMED | "Ticket has already been used" or "Ticket is cancelled" |
| One-time | Can only be used once | "Ticket has already been used" |

---

## 🛠️ Configuration

### application.properties
```properties
# Base URL for QR codes
app.base-url=http://localhost:8080

# Email settings
app.mail.enabled=false
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### Dependencies (pom.xml)
```xml
<!-- QR Code -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.1</version>
</dependency>

<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## 🎬 User Journey

### Booking
1. User clicks "Book Now" on event
2. System creates booking with unique ticket code
3. QR code generated automatically
4. Email sent (if enabled)
5. User sees "Booking confirmed!"

### Viewing Ticket
1. User goes to "My Bookings"
2. Clicks "View Ticket"
3. QR code displayed on screen
4. Can print or show on phone

### Scanning at Venue
1. Admin opens `/admin/scan`
2. User shows QR code
3. Admin scans with camera
4. System validates automatically
5. Green screen = Entry granted
6. Red screen = Entry denied

---

## 🐛 Troubleshooting

### QR Code Not Working
```
Problem: QR code doesn't scan
Solution: 
- Check if camera has permission
- Ensure good lighting
- Try manual entry instead
```

### Email Not Sending
```
Problem: No email received
Solution:
- Check app.mail.enabled=true
- Verify Gmail app password (not regular password)
- Check spam folder
- Look at console for errors
```

### Ticket Already Used
```
Problem: "Ticket has already been used"
Solution:
- This is correct behavior
- Each ticket can only be used once
- User needs to contact admin if mistake
```

---

## 📱 API Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/events/{id}/book` | POST | Book an event |
| `/bookings/{id}/ticket` | GET | View ticket with QR |
| `/admin/scan` | GET | Scan page (auto-validate if ticketCode param) |
| `/admin/scan` | POST | Manual ticket validation |
| `/bookings/{id}/cancel` | POST | Cancel booking |

---

## 🔐 Security Features

✅ **Unique Codes** - UUID prevents guessing
✅ **Owner Check** - Admins can only scan their events
✅ **One-Time Use** - Tickets marked as USED
✅ **Status Validation** - CONFIRMED, USED, CANCELLED
✅ **Database Verification** - All tickets checked against DB

---

## 💡 Tips

**For Users:**
- Save ticket email for easy access
- Screenshot QR code as backup
- Arrive early to avoid queue

**For Admins:**
- Test scanning before event
- Have backup manual entry ready
- Keep phone charged
- Use good lighting for scanning

**For Developers:**
- Change `app.base-url` in production
- Use environment variables for secrets
- Enable HTTPS for security
- Add logging for audit trail

---

## 📊 Status Flow

```
BOOKING CREATED
    ↓
CONFIRMED (can be scanned)
    ↓
USED (already scanned) ← Can't scan again
    
OR

CONFIRMED
    ↓
CANCELLED (user cancelled) ← Can't scan
```

---

## 🎓 Key Concepts

**QR Code:**
- 2D barcode that stores text (URL in our case)
- Scanned by phone cameras
- Contains link to validation page

**Base64:**
- Encoding format for binary data
- Allows embedding images in HTML
- Used for QR code images

**Async:**
- Runs in background thread
- Doesn't block main application
- Used for email sending

**SMTP:**
- Protocol for sending emails
- Gmail uses port 587 with TLS
- Requires app password (not regular password)

---

## 📞 Support

**Documentation:**
- QR_AND_EMAIL_EXPLANATION.md - Detailed explanation
- SYSTEM_FLOW_DIAGRAM.md - Visual diagrams
- TESTING_CHECKLIST.md - Testing guide

**Common Questions:**

Q: Do I need email to use the app?
A: No! App works perfectly without email.

Q: Can users scan their own tickets?
A: No, only admins can scan tickets for their events.

Q: What if someone loses their ticket?
A: They can view it again in "My Bookings".

Q: Can tickets be transferred?
A: No, tickets are tied to the booking user.

Q: How do I test without a real event?
A: Create a test event and book it yourself.

---

## 🚀 Production Checklist

Before deploying:
- [ ] Change `app.base-url` to production domain
- [ ] Enable HTTPS
- [ ] Set up proper email credentials
- [ ] Use environment variables for secrets
- [ ] Test QR scanning with production URL
- [ ] Add rate limiting
- [ ] Set up monitoring/logging
- [ ] Test email delivery
- [ ] Backup database regularly

---

**Need more help?** Check the detailed documentation files!
