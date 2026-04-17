# QR Code Scanning Troubleshooting Guide

## 🔍 Common Issues and Solutions

---

## Issue 1: QR Code Not Displaying on Ticket Page

### Symptoms:
- Ticket page loads but no QR code appears
- Blank space where QR code should be
- Broken image icon

### Solutions:

**Check 1: Verify Dependencies**
```bash
# Check if ZXing is in pom.xml
cat pom.xml | grep zxing

# Should see:
# <artifactId>core</artifactId>
# <artifactId>javase</artifactId>
```

**Check 2: Rebuild Project**
```bash
mvn clean install
mvn spring-boot:run
```

**Check 3: Check Console Logs**
```bash
# Look for errors like:
# "Failed to generate QR code"
# "ClassNotFoundException: com.google.zxing"
```

**Check 4: Verify QrCodeService**
```bash
# Check if file exists:
ls Good_copy/src/main/java/com/example/eventmanagement/service/QrCodeService.java
```

**Check 5: Browser Console**
```
1. Open ticket page
2. Press F12
3. Go to Console tab
4. Look for errors like:
   - "Failed to load resource"
   - "Invalid image data"
```

---

## Issue 2: Phone Can't Access the Application

### Symptoms:
- Phone shows "Can't reach this page"
- "Connection timed out"
- "Site can't be reached"

### Solutions:

**Solution 1: Verify Same WiFi Network**
```
Computer:
1. Check WiFi name in system tray
2. Note the network name (e.g., "HomeWiFi")

Phone:
1. Settings > WiFi
2. Verify connected to same network
3. Must be EXACTLY the same network
```

**Solution 2: Find Correct IP Address**
```bash
# Mac/Linux:
ifconfig | grep "inet " | grep -v 127.0.0.1

# Windows:
ipconfig

# Look for:
# inet 192.168.1.100  ← This is your IP
# NOT 127.0.0.1 (that's localhost)
```

**Solution 3: Test IP from Phone**
```
1. Open browser on phone
2. Type: http://192.168.1.100:8080
3. Should see login page
4. If not, IP is wrong or firewall blocking
```

**Solution 4: Check Firewall**
```
Mac:
1. System Preferences > Security & Privacy
2. Firewall tab
3. Firewall Options
4. Allow Java/Spring Boot

Windows:
1. Control Panel > Windows Defender Firewall
2. Advanced Settings
3. Inbound Rules
4. Allow port 8080
```

**Solution 5: Disable Firewall Temporarily**
```
⚠️ Only for testing!

Mac:
System Preferences > Security > Firewall > Turn Off

Windows:
Control Panel > Firewall > Turn off

Test if it works, then turn firewall back on
```

**Solution 6: Try Different Port**
```properties
# In application.properties, change:
server.port=8080

# To:
server.port=8081

# Then update app.base-url:
app.base-url=http://192.168.1.100:8081

# Restart app
```

---

## Issue 3: QR Code Scans But Shows Login Page

### Symptoms:
- QR code scans successfully
- Browser opens
- Shows login page instead of validation result

### Solutions:

**This is NORMAL behavior!**

The admin needs to be logged in on their phone:

```
1. QR code scans → Opens browser
2. Not logged in → Shows login page
3. Login as admin
4. After login → Redirects to scan page
5. Shows validation result
```

**To avoid repeated logins:**
```
1. Login as admin on phone once
2. Check "Remember me" (if available)
3. Keep browser open
4. Subsequent scans will work directly
```

---

## Issue 4: "Invalid ticket code" for Valid Ticket

### Symptoms:
- Ticket exists in database
- Shows red error: "Invalid ticket code"

### Solutions:

**Check 1: Verify Ticket in Database**
```sql
-- Connect to MySQL
mysql -u root -p

-- Use database
USE eventdb;

-- Check if ticket exists
SELECT * FROM bookings WHERE ticket_code = 'TKT-1-2-ABCD1234';

-- Should return one row
```

**Check 2: Verify Admin is Event Creator**
```sql
-- Check who created the event
SELECT e.title, e.created_by, b.ticket_code
FROM bookings b
JOIN events e ON b.event_id = e.id
WHERE b.ticket_code = 'TKT-1-2-ABCD1234';

-- created_by must match logged-in admin username
```

**Check 3: Verify Ticket Status**
```sql
SELECT ticket_code, status FROM bookings 
WHERE ticket_code = 'TKT-1-2-ABCD1234';

-- Status should be 'CONFIRMED'
-- If 'USED' → Already scanned
-- If 'CANCELLED' → User cancelled
```

**Check 4: Login as Correct Admin**
```
Problem: Admin A trying to scan Admin B's event

Solution:
1. Logout
2. Login as the admin who created the event
3. Try scanning again
```

---

## Issue 5: QR Code Contains Wrong URL

### Symptoms:
- QR code scans but URL is wrong
- URL shows localhost instead of IP
- URL shows old IP address

### Solutions:

**Check 1: Verify app.base-url**
```properties
# In application.properties
app.base-url=http://192.168.1.100:8080

# Must match your current IP address
```

**Check 2: Restart Application**
```bash
# Stop app (Ctrl+C)
# Start again
mvn spring-boot:run

# Changes to application.properties require restart
```

**Check 3: Create New Booking**
```
Old bookings have old QR codes!

1. Create a new booking after changing app.base-url
2. View the new ticket
3. New QR code will have correct URL
```

**Check 4: Decode QR Code to Verify**
```
1. Right-click QR code image
2. "Save image as..."
3. Go to: https://zxing.org/w/decode
4. Upload the image
5. Check the decoded URL
6. Should match: app.base-url + /admin/scan?ticketCode=...
```

---

## Issue 6: Camera Won't Scan QR Code

### Symptoms:
- Camera opens but doesn't detect QR code
- No notification appears
- Nothing happens when pointing at QR code

### Solutions:

**Solution 1: Check Camera Permissions**
```
iPhone:
Settings > Safari > Camera > Allow

Android:
Settings > Apps > Camera > Permissions > Allow
```

**Solution 2: Improve Lighting**
```
✅ Do:
- Use bright, even lighting
- Avoid shadows
- Increase screen brightness

❌ Don't:
- Direct sunlight (causes glare)
- Very dim lighting
- Reflective surfaces
```

**Solution 3: Adjust Distance**
```
Too close: Camera can't focus
Too far: QR code too small

Sweet spot: 6-12 inches (15-30 cm)
```

**Solution 4: Clean Camera Lens**
```
Use soft cloth to clean phone camera lens
Smudges can prevent scanning
```

**Solution 5: Try QR Scanner App**
```
If native camera doesn't work:

iPhone:
- Download "QR Code Reader" from App Store

Android:
- Download "QR Code Scanner" from Play Store

Use app instead of camera
```

**Solution 6: Use Manual Entry**
```
If all else fails:
1. User tells you ticket code
2. Type it manually in scan page
3. Click "Validate Ticket"
```

---

## Issue 7: "Ticket has already been used"

### Symptoms:
- Red error screen
- Message: "Ticket has already been used"
- User insists they haven't entered yet

### Solutions:

**Check 1: Verify in Database**
```sql
SELECT ticket_code, status, booking_date 
FROM bookings 
WHERE ticket_code = 'TKT-1-2-ABCD1234';

-- If status = 'USED', it was scanned before
```

**Check 2: Check Scan History**
```sql
-- If you added logging:
SELECT * FROM scan_logs 
WHERE ticket_code = 'TKT-1-2-ABCD1234'
ORDER BY scan_time DESC;
```

**Check 3: Possible Causes**
```
1. User scanned twice by mistake
2. Someone else used their ticket
3. Ticket was tested earlier
4. Database error (rare)
```

**Check 4: Manual Override (Emergency Only)**
```sql
-- ⚠️ Only if you're SURE it's a mistake!
UPDATE bookings 
SET status = 'CONFIRMED' 
WHERE ticket_code = 'TKT-1-2-ABCD1234';

-- Then scan again
```

---

## Issue 8: Application Won't Start

### Symptoms:
- `mvn spring-boot:run` fails
- Port already in use
- Database connection error

### Solutions:

**Solution 1: Port Already in Use**
```bash
# Check what's using port 8080
lsof -i :8080  # Mac/Linux
netstat -ano | findstr :8080  # Windows

# Kill the process
kill -9 [PID]  # Mac/Linux
taskkill /PID [PID] /F  # Windows

# Or change port in application.properties
server.port=8081
```

**Solution 2: Database Not Running**
```bash
# Check if MySQL is running
mysql -u root -p

# If not, start MySQL:
# Mac: brew services start mysql
# Windows: Services > MySQL > Start
# Linux: sudo systemctl start mysql
```

**Solution 3: Wrong Database Credentials**
```properties
# In application.properties
spring.datasource.username=root
spring.datasource.password=your_password

# Make sure these match your MySQL credentials
```

**Solution 4: Database Doesn't Exist**
```sql
-- Create database if it doesn't exist
mysql -u root -p
CREATE DATABASE IF NOT EXISTS eventdb;
exit;
```

---

## Issue 9: QR Code Too Small/Blurry

### Symptoms:
- QR code displays but is tiny
- Blurry or pixelated
- Hard to scan

### Solutions:

**Solution 1: Increase QR Code Size**
```java
// In EventController.java, change:
String qrBase64 = qrCodeService.generateQrBase64(scanUrl, 280, 280);

// To larger size:
String qrBase64 = qrCodeService.generateQrBase64(scanUrl, 400, 400);
```

**Solution 2: Zoom Browser**
```
1. On ticket page
2. Press Ctrl + (Windows) or Cmd + (Mac)
3. Zoom in until QR code is larger
4. Scan the larger QR code
```

**Solution 3: Print Ticket**
```
1. Click "Print Ticket" button
2. Print to PDF or paper
3. QR code will be larger
4. Easier to scan
```

---

## Issue 10: Multiple Admins Can't Scan

### Symptoms:
- Admin A can scan their events
- Admin B gets "You can only scan tickets for your own events"

### Solutions:

**This is CORRECT behavior!**

Security feature: Each admin can only scan tickets for events they created.

**Workaround if needed:**
```
Option 1: Share admin account
- All staff use same admin login
- Not recommended for security

Option 2: Transfer event ownership
- Update database:
  UPDATE events SET created_by = 'admin2' WHERE id = 5;

Option 3: Create super-admin role
- Modify code to allow SUPER_ADMIN to scan all events
- Requires code changes
```

---

## 🔧 Diagnostic Commands

### Check Application Status
```bash
# Is app running?
curl http://localhost:8080

# Should return HTML or redirect
```

### Check Database Connection
```bash
# Connect to database
mysql -u root -p eventdb

# List tables
SHOW TABLES;

# Check bookings
SELECT COUNT(*) FROM bookings;
```

### Check Network Connectivity
```bash
# From phone, test connection
# Open browser on phone
# Go to: http://192.168.1.100:8080

# Should see login page
```

### Check QR Code Generation
```bash
# In application logs, look for:
grep -i "qr" application.log

# Should see:
# "QR code generated successfully"
```

---

## 📊 Status Codes Reference

| Status | Meaning | Can Scan? |
|--------|---------|-----------|
| CONFIRMED | Valid ticket, not used yet | ✅ Yes |
| USED | Already scanned once | ❌ No |
| CANCELLED | User cancelled booking | ❌ No |

---

## 🆘 Emergency Contacts

If you can't resolve the issue:

1. **Check Documentation:**
   - HOW_TO_USE_QR_SCANNING.md
   - QR_AND_EMAIL_EXPLANATION.md
   - SYSTEM_FLOW_DIAGRAM.md

2. **Check Console Logs:**
   - Terminal where app is running
   - Browser console (F12)

3. **Check Database:**
   - Verify data exists
   - Check status values

4. **Restart Everything:**
   - Stop application
   - Restart MySQL
   - Clear browser cache
   - Start application
   - Try again

---

## ✅ Verification Checklist

Use this to verify everything is working:

```
□ Application starts without errors
□ Can login as admin
□ Can login as user
□ Can create event
□ Can book event
□ Ticket page loads
□ QR code displays
□ QR code can be scanned (or manually entered)
□ First scan shows green success
□ Second scan shows red error
□ Database status updates to USED
```

---

## 💡 Prevention Tips

To avoid issues:

1. **Test before event:**
   - Full end-to-end test
   - Multiple phones
   - Different scenarios

2. **Have backups:**
   - Manual entry ready
   - Printed attendee list
   - Backup admin account

3. **Document setup:**
   - Note IP address
   - Note admin credentials
   - Note WiFi details

4. **Train staff:**
   - Show them how to scan
   - Show them manual entry
   - Give them this guide

---

**Still stuck?** Review the detailed documentation or check the console logs for specific error messages!
