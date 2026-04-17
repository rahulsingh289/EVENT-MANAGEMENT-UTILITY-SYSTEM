# QR Scanning Quick Start Guide

## 🚀 5-Minute Setup

### Option 1: Test on Same Computer (Easiest)

```
1. Start app: mvn spring-boot:run
2. Login as admin → Create event
3. Login as user → Book event → View ticket
4. Copy ticket code (e.g., TKT-1-2-ABCD1234)
5. Open new tab → Login as admin → Go to /admin/scan
6. Paste ticket code → Click "Validate Ticket"
7. ✅ See green success screen!
```

### Option 2: Test with Phone (Real Scenario)

```
1. Find your computer's IP address:
   Mac: ifconfig | grep "inet " | grep -v 127.0.0.1
   Windows: ipconfig
   Example: 192.168.1.100

2. Update application.properties:
   app.base-url=http://192.168.1.100:8080

3. Restart app: mvn spring-boot:run

4. Create booking (as user) → View ticket

5. On phone:
   - Open Camera app
   - Point at QR code on screen
   - Tap notification
   - Login as admin
   - ✅ See validation result!
```

---

## 📱 Phone Scanning - Visual Guide

```
┌─────────────────────────────────────────────────────────────┐
│                    COMPUTER SCREEN                           │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Your Ticket                              │  │
│  │                                                       │  │
│  │  Event: Test Concert                                 │  │
│  │  Venue: City Hall                                    │  │
│  │                                                       │  │
│  │         ┌─────────────────────┐                      │  │
│  │         │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │                      │  │
│  │         │  ▓▓           ▓▓▓  │                      │  │
│  │         │  ▓▓  QR CODE  ▓▓▓  │  ← Point camera here │  │
│  │         │  ▓▓           ▓▓▓  │                      │  │
│  │         │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │                      │  │
│  │         └─────────────────────┘                      │  │
│  │                                                       │  │
│  │  Ticket Code: TKT-1-2-ABCD1234                      │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ 📱 Scan with phone camera
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    PHONE SCREEN                              │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  📷 Camera App                                        │  │
│  │                                                       │  │
│  │  ┌────────────────────────────────────────────────┐ │  │
│  │  │ 🔗 Open "192.168.1.100:8080/admin/scan..."    │ │  │
│  │  └────────────────────────────────────────────────┘ │  │
│  │                                                       │  │
│  │         [QR code detected in viewfinder]             │  │
│  │                                                       │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  Tap the notification ↑                                     │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Tap notification
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  PHONE BROWSER                               │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  🌐 192.168.1.100:8080/admin/scan?ticketCode=...    │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │                                                       │  │
│  │  ┌─────────────────────────────────────────────┐    │  │
│  │  │  ✅ Entry Granted — Ticket Valid           │    │  │
│  │  ├─────────────────────────────────────────────┤    │  │
│  │  │                                              │    │  │
│  │  │  👤 Attendee: user1                         │    │  │
│  │  │  📅 Event: Test Concert                     │    │  │
│  │  │  📍 Venue: City Hall                        │    │  │
│  │  │  🎫 Ticket: TKT-1-2-ABCD1234               │    │  │
│  │  │                                              │    │  │
│  │  └─────────────────────────────────────────────┘    │  │
│  │                                                       │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Common Scenarios

### Scenario 1: First Time Scan
```
User shows QR code
    ↓
Admin scans
    ↓
✅ GREEN SCREEN
    ↓
"Entry Granted"
    ↓
Ticket marked as USED
```

### Scenario 2: Second Scan (Already Used)
```
User shows same QR code
    ↓
Admin scans
    ↓
❌ RED SCREEN
    ↓
"Ticket has already been used"
    ↓
Entry denied
```

### Scenario 3: Cancelled Ticket
```
User cancelled booking
    ↓
User shows QR code
    ↓
Admin scans
    ↓
❌ RED SCREEN
    ↓
"Ticket is cancelled"
```

---

## 🔧 Network Setup

### Same WiFi Required!

```
┌─────────────────┐         ┌─────────────────┐
│   WiFi Router   │         │   WiFi Router   │
│  192.168.1.1    │         │  192.168.1.1    │
└────────┬────────┘         └────────┬────────┘
         │                           │
    ┌────┴────┐                 ┌────┴────┐
    │         │                 │         │
┌───▼───┐ ┌──▼───┐         ┌───▼───┐ ┌──▼───┐
│Computer│ │Phone │         │Computer│ │Phone │
│.1.100  │ │.1.50 │         │.1.100  │ │.2.50 │
└────────┘ └──────┘         └────────┘ └──────┘
                                       
✅ WORKS              ❌ WON'T WORK
Same network          Different networks
```

---

## 📋 Pre-Event Checklist

Before your event starts:

```
Setup:
□ App running on server
□ Database connected
□ Admin account ready
□ Test booking created
□ QR code displays correctly

Network:
□ WiFi available at venue
□ Admin phone connected to WiFi
□ Test scan from phone works
□ Backup 4G/5G available

Equipment:
□ Admin phone charged (>80%)
□ Backup power bank
□ Good lighting at entrance
□ Backup laptop for manual entry

Testing:
□ Scan valid ticket → Green screen
□ Scan used ticket → Red screen
□ Scan invalid code → Error message
□ Manual entry works
```

---

## 🎬 Demo Script

Use this to demonstrate the system:

```
1. SETUP (30 seconds)
   "Let me show you our ticketing system..."
   - Open app on computer
   - Show event list

2. BOOKING (30 seconds)
   "When a user books an event..."
   - Click "Book Now"
   - Show confirmation
   - Go to "My Bookings"

3. TICKET (30 seconds)
   "They receive a ticket with QR code..."
   - Click "View Ticket"
   - Show QR code
   - Explain ticket code

4. SCANNING (60 seconds)
   "At the venue entrance..."
   - Take out phone
   - Open camera
   - Scan QR code
   - Show green success screen
   - Explain attendee info

5. SECURITY (30 seconds)
   "Each ticket can only be used once..."
   - Scan same QR code again
   - Show red error screen
   - Explain "already used"

Total: 3 minutes
```

---

## 💡 Tips for Smooth Scanning

### For Best Results:

**Lighting:**
```
✅ Good: Bright, even lighting
❌ Bad: Direct sunlight, shadows, glare
```

**Distance:**
```
✅ Good: 6-12 inches from screen
❌ Bad: Too close or too far
```

**Screen Brightness:**
```
✅ Good: Maximum brightness
❌ Bad: Dim screen
```

**Camera Focus:**
```
✅ Good: Hold steady, let camera focus
❌ Bad: Moving too fast
```

---

## 🐛 Quick Troubleshooting

### QR Code Won't Scan

```
Try this:
1. Increase screen brightness
2. Move phone closer/farther
3. Clean camera lens
4. Try different angle
5. Use manual entry instead
```

### Phone Can't Connect

```
Check:
1. Same WiFi? → Settings > WiFi
2. Correct IP? → Verify in app.base-url
3. Firewall? → Allow port 8080
4. App running? → Check terminal
```

### Wrong Admin Error

```
Problem: "You can only scan tickets for your own events"

Solution:
- Login as the admin who created the event
- Each admin can only scan their own events
```

---

## 📞 Emergency Procedures

### If QR Scanning Fails at Event:

**Plan A: Manual Entry**
```
1. Ask user for ticket code
2. Type into scan page
3. Click "Validate Ticket"
4. Check result
```

**Plan B: Check My Bookings**
```
1. Login as admin
2. Go to event bookings page
3. Find user's name in list
4. Verify booking status
5. Manually mark as attended
```

**Plan C: Temporary Access**
```
1. Let user in
2. Note their name/email
3. Verify booking later
4. Update status in database
```

---

## ✅ Success Indicators

You'll know it's working when:

```
✅ QR code appears on ticket page
✅ Phone camera detects QR code
✅ Notification appears on phone
✅ Browser opens scan URL
✅ Green screen shows on first scan
✅ Red screen shows on second scan
✅ Database status changes to USED
```

---

## 🎓 Training for Event Staff

Quick training script for volunteers:

```
"Here's how to scan tickets:

1. Open the scan page on your phone
2. When someone arrives, ask them to show their ticket
3. Point your camera at the QR code
4. Wait for the page to load
5. GREEN = Let them in
6. RED = Don't let them in, call supervisor

If QR doesn't work:
- Ask for their ticket code
- Type it in the box
- Click validate

Questions? Call [supervisor number]"
```

---

## 🚀 You're Ready!

Follow these steps and your QR scanning will work perfectly. Start with Option 1 (same computer) to verify everything works, then move to Option 2 (phone scanning) for the real experience.

**Need more details?** Check HOW_TO_USE_QR_SCANNING.md for the complete guide!
