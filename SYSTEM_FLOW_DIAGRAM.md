# System Flow Diagrams

## 🎫 Complete Booking & Scanning Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         USER BOOKS AN EVENT                              │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │   BookingService.bookEvent()  │
                    └───────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
        ┌─────────────────────┐         ┌─────────────────────┐
        │  Generate Ticket    │         │  Check Capacity &   │
        │  Code (UUID)        │         │  Duplicate Booking  │
        │                     │         │                     │
        │  TKT-5-12-A3B4C5D6 │         │  ✓ Validation Pass  │
        └─────────────────────┘         └─────────────────────┘
                    │                               │
                    └───────────────┬───────────────┘
                                    ▼
                        ┌───────────────────────┐
                        │  Save to Database     │
                        │  Status: CONFIRMED    │
                        └───────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
        ┌─────────────────────┐         ┌─────────────────────┐
        │  Generate QR Code   │         │  Send Email         │
        │  (QrCodeService)    │         │  (TicketEmailService│
        │                     │         │   @Async)           │
        │  Base64 PNG Image   │         │                     │
        └─────────────────────┘         └─────────────────────┘
                    │                               │
                    ▼                               ▼
        ┌─────────────────────┐         ┌─────────────────────┐
        │  Display on Ticket  │         │  Email with QR Code │
        │  Page (/ticket)     │         │  Sent to User       │
        └─────────────────────┘         └─────────────────────┘
```

---

## 📱 QR Code Scanning Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    ADMIN SCANS TICKET AT VENUE                           │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
        ┌─────────────────────┐         ┌─────────────────────┐
        │  Method 1:          │         │  Method 2:          │
        │  QR Code Scan       │         │  Manual Entry       │
        │                     │         │                     │
        │  📱 Camera App      │         │  ⌨️  Type Code     │
        └─────────────────────┘         └─────────────────────┘
                    │                               │
                    └───────────────┬───────────────┘
                                    ▼
                    ┌───────────────────────────────┐
                    │  Browser Opens:               │
                    │  /admin/scan?ticketCode=      │
                    │  TKT-5-12-A3B4C5D6           │
                    └───────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  EventController.scanPage()   │
                    └───────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  BookingService               │
                    │  .validateAndUseTicket()      │
                    └───────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
        ┌─────────────────────┐         ┌─────────────────────┐
        │  Validation Checks: │         │  Database Lookup    │
        │                     │         │                     │
        │  1. Ticket exists?  │         │  SELECT * FROM      │
        │  2. Admin is owner? │         │  bookings WHERE     │
        │  3. Status valid?   │         │  ticket_code = ?    │
        │  4. Not used yet?   │         │                     │
        └─────────────────────┘         └─────────────────────┘
                    │                               │
                    └───────────────┬───────────────┘
                                    ▼
                        ┌───────────────────────┐
                        │  All Checks Pass?     │
                        └───────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
        ┌─────────────────────┐         ┌─────────────────────┐
        │  ✅ SUCCESS         │         │  ❌ FAILURE         │
        │                     │         │                     │
        │  Update Status:     │         │  Show Error:        │
        │  CONFIRMED → USED   │         │  - Invalid code     │
        │                     │         │  - Already used     │
        │  Display:           │         │  - Cancelled        │
        │  - Attendee name    │         │  - Wrong event      │
        │  - Event details    │         │                     │
        │  - Green banner     │         │  Display:           │
        │  "Entry Granted"    │         │  - Red banner       │
        │                     │         │  - Error message    │
        └─────────────────────┘         └─────────────────────┘
```

---

## 📧 Email Service Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    BOOKING CREATED (Trigger)                             │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  BookingService calls:        │
                    │  ticketEmailService           │
                    │  .sendTicketEmail(booking)    │
                    └───────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  @Async Method Starts         │
                    │  (Runs in Background Thread)  │
                    └───────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  Check if Email Enabled?      │
                    │  app.mail.enabled=true?       │
                    └───────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
        ┌─────────────────────┐         ┌─────────────────────┐
        │  ❌ DISABLED        │         │  ✅ ENABLED         │
        │                     │         │                     │
        │  Print to Console:  │         │  Continue...        │
        │  "Email skipped"    │         │                     │
        │                     │         │                     │
        │  App continues      │         │                     │
        │  normally           │         │                     │
        └─────────────────────┘         └─────────────────────┘
                                                    │
                                                    ▼
                                    ┌───────────────────────────────┐
                                    │  Generate QR Code             │
                                    │  (QrCodeService)              │
                                    │                               │
                                    │  URL: /admin/scan?            │
                                    │  ticketCode=TKT-5-12-A3B4C5D6│
                                    │                               │
                                    │  Output: Base64 PNG           │
                                    └───────────────────────────────┘
                                                    │
                                                    ▼
                                    ┌───────────────────────────────┐
                                    │  Build HTML Email Template    │
                                    │                               │
                                    │  - Event name & date          │
                                    │  - Attendee details           │
                                    │  - Venue information          │
                                    │  - Ticket code                │
                                    │  - Embedded QR code image     │
                                    └───────────────────────────────┘
                                                    │
                                                    ▼
                                    ┌───────────────────────────────┐
                                    │  Create MIME Message          │
                                    │                               │
                                    │  From: your-email@gmail.com   │
                                    │  To: user@example.com         │
                                    │  Subject: Your Ticket: Event  │
                                    │  Body: HTML content           │
                                    │  Attachment: QR code (inline) │
                                    └───────────────────────────────┘
                                                    │
                                                    ▼
                                    ┌───────────────────────────────┐
                                    │  Send via SMTP                │
                                    │                               │
                                    │  Host: smtp.gmail.com         │
                                    │  Port: 587                    │
                                    │  TLS: Enabled                 │
                                    │  Auth: App Password           │
                                    └───────────────────────────────┘
                                                    │
                                    ┌───────────────┴───────────────┐
                                    ▼                               ▼
                        ┌─────────────────────┐         ┌─────────────────────┐
                        │  ✅ SUCCESS         │         │  ❌ FAILURE         │
                        │                     │         │                     │
                        │  Email Delivered    │         │  Log Error          │
                        │  to User's Inbox    │         │  (App continues)    │
                        │                     │         │                     │
                        │  Console:           │         │  Console:           │
                        │  "Ticket sent to    │         │  "Failed to send    │
                        │   user@example.com" │         │   email: [error]"   │
                        └─────────────────────┘         └─────────────────────┘
```

---

## 🔄 QR Code Generation Process

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    QR CODE GENERATION PROCESS                            │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  Input: Ticket Code           │
                    │  TKT-5-12-A3B4C5D6           │
                    └───────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  Build Scan URL               │
                    │                               │
                    │  baseUrl + "/admin/scan?      │
                    │  ticketCode=" + ticketCode    │
                    │                               │
                    │  Result:                      │
                    │  http://localhost:8080/       │
                    │  admin/scan?ticketCode=       │
                    │  TKT-5-12-A3B4C5D6           │
                    └───────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  QrCodeService                │
                    │  .generateQrBase64()          │
                    │                               │
                    │  Parameters:                  │
                    │  - content: URL               │
                    │  - width: 280px               │
                    │  - height: 280px              │
                    └───────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  ZXing Library Processing     │
                    └───────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
        ┌─────────────────────┐         ┌─────────────────────┐
        │  Step 1:            │         │  Step 2:            │
        │  Create QRCodeWriter│         │  Encode URL to      │
        │                     │         │  BitMatrix          │
        │  QRCodeWriter       │         │                     │
        │  writer = new       │         │  Binary matrix      │
        │  QRCodeWriter()     │         │  representing QR    │
        └─────────────────────┘         └─────────────────────┘
                    │                               │
                    └───────────────┬───────────────┘
                                    ▼
                    ┌───────────────────────────────┐
                    │  Step 3:                      │
                    │  Convert BitMatrix to PNG     │
                    │                               │
                    │  MatrixToImageWriter          │
                    │  .writeToStream()             │
                    │                               │
                    │  Output: PNG byte array       │
                    └───────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  Step 4:                      │
                    │  Encode to Base64             │
                    │                               │
                    │  Base64.getEncoder()          │
                    │  .encodeToString(bytes)       │
                    │                               │
                    │  Output: Base64 string        │
                    └───────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  Result:                      │
                    │  iVBORw0KGgoAAAANSUhEUgAA... │
                    │  (Long Base64 string)         │
                    └───────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  Usage in HTML:               │
                    │                               │
                    │  <img src="data:image/png;    │
                    │  base64,iVBORw0KGgo..."       │
                    │  alt="QR Code"/>              │
                    └───────────────────────────────┘
```

---

## 🔐 Security Validation Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    TICKET VALIDATION SECURITY                            │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  Input: Ticket Code           │
                    │  Admin Username               │
                    └───────────────────────────────┘
                                    │
                                    ▼
        ┌───────────────────────────────────────────────────┐
        │  CHECK 1: Does ticket exist in database?          │
        └───────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
        ┌─────────────────────┐         ┌─────────────────────┐
        │  ❌ NOT FOUND       │         │  ✅ FOUND           │
        │                     │         │                     │
        │  Error:             │         │  Continue to        │
        │  "Invalid ticket    │         │  Check 2...         │
        │   code"             │         │                     │
        │                     │         │                     │
        │  STOP ⛔           │         │                     │
        └─────────────────────┘         └─────────────────────┘
                                                    │
                                                    ▼
                        ┌───────────────────────────────────────┐
                        │  CHECK 2: Is admin the event creator? │
                        │                                        │
                        │  booking.event.createdBy ==            │
                        │  adminUsername?                        │
                        └───────────────────────────────────────┘
                                                    │
                                    ┌───────────────┴───────────────┐
                                    ▼                               ▼
                        ┌─────────────────────┐         ┌─────────────────────┐
                        │  ❌ NOT OWNER       │         │  ✅ IS OWNER        │
                        │                     │         │                     │
                        │  Error:             │         │  Continue to        │
                        │  "You can only scan │         │  Check 3...         │
                        │   tickets for your  │         │                     │
                        │   own events"       │         │                     │
                        │                     │         │                     │
                        │  STOP ⛔           │         │                     │
                        └─────────────────────┘         └─────────────────────┘
                                                                    │
                                                                    ▼
                                        ┌───────────────────────────────────┐
                                        │  CHECK 3: Is ticket already used? │
                                        │                                    │
                                        │  booking.status == "USED"?         │
                                        └───────────────────────────────────┘
                                                                    │
                                                    ┌───────────────┴───────────────┐
                                                    ▼                               ▼
                                        ┌─────────────────────┐         ┌─────────────────────┐
                                        │  ❌ ALREADY USED    │         │  ✅ NOT USED        │
                                        │                     │         │                     │
                                        │  Error:             │         │  Continue to        │
                                        │  "Ticket has        │         │  Check 4...         │
                                        │   already been      │         │                     │
                                        │   used"             │         │                     │
                                        │                     │         │                     │
                                        │  STOP ⛔           │         │                     │
                                        └─────────────────────┘         └─────────────────────┘
                                                                                    │
                                                                                    ▼
                                                                ┌───────────────────────────────┐
                                                                │  CHECK 4: Is ticket cancelled?│
                                                                │                                │
                                                                │  booking.status == "CANCELLED"?│
                                                                └───────────────────────────────┘
                                                                                    │
                                                                    ┌───────────────┴───────────────┐
                                                                    ▼                               ▼
                                                        ┌─────────────────────┐         ┌─────────────────────┐
                                                        │  ❌ CANCELLED       │         │  ✅ CONFIRMED       │
                                                        │                     │         │                     │
                                                        │  Error:             │         │  ALL CHECKS PASS!   │
                                                        │  "Ticket is         │         │                     │
                                                        │   cancelled"        │         │  Mark as USED       │
                                                        │                     │         │  Grant Entry        │
                                                        │  STOP ⛔           │         │                     │
                                                        └─────────────────────┘         └─────────────────────┘
```

---

## 📊 Data Flow Summary

```
USER                    SYSTEM                      DATABASE                EMAIL
  │                       │                            │                      │
  │  1. Book Event        │                            │                      │
  ├──────────────────────>│                            │                      │
  │                       │  2. Create Booking         │                      │
  │                       ├───────────────────────────>│                      │
  │                       │  3. Generate Ticket Code   │                      │
  │                       │     TKT-5-12-A3B4C5D6     │                      │
  │                       │  4. Save Booking           │                      │
  │                       ├───────────────────────────>│                      │
  │                       │  5. Generate QR Code       │                      │
  │                       │     (Base64 PNG)           │                      │
  │  6. Show Confirmation │                            │                      │
  │<──────────────────────┤                            │                      │
  │                       │  7. Send Email (Async)     │                      │
  │                       ├────────────────────────────┼─────────────────────>│
  │                       │                            │  8. Email Delivered  │
  │<──────────────────────┼────────────────────────────┼──────────────────────┤
  │                       │                            │                      │
  │  9. View Ticket       │                            │                      │
  ├──────────────────────>│                            │                      │
  │                       │  10. Fetch Booking         │                      │
  │                       ├───────────────────────────>│                      │
  │                       │  11. Generate QR           │                      │
  │  12. Display QR Code  │                            │                      │
  │<──────────────────────┤                            │                      │
  │                       │                            │                      │

ADMIN                   SYSTEM                      DATABASE
  │                       │                            │
  │  13. Scan QR Code     │                            │
  ├──────────────────────>│                            │
  │                       │  14. Validate Ticket       │
  │                       ├───────────────────────────>│
  │                       │  15. Check Status          │
  │                       │  16. Update to USED        │
  │                       ├───────────────────────────>│
  │  17. Show Result      │                            │
  │<──────────────────────┤                            │
```

---

This visual representation should help you understand how all the components work together!
