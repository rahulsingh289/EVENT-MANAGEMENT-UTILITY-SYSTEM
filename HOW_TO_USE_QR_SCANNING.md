# How to Make QR Code Scanning Work - Step by Step Guide

## 🎯 Overview
This guide will walk you through testing and using the QR code scanning feature in your Event Management System.

---

## 📋 Prerequisites

Before you start, make sure:
- ✅ Application is running (`mvn spring-boot:run`)
- ✅ MySQL database is running
- ✅ You have at least one admin account
- ✅ You have at least one user account
- ✅ You have a smartphone with a camera

---

## 🚀 Step-by-Step Setup

### Step 1: Start the Application

```bash
cd Good_copy
mvn spring-boot:run
```

Wait for the message:
```
Started EventManagementApplication in X.XXX seconds
```

### Step 2: Create Test Accounts (if not already done)

**Create Admin Account:**
1. Go to: http://localhost:8080/register
2. Select "Admin" role
3. Fill in:
   - Username: `admin`
   - Email: `admin@test.com`
   - Password: `admin123`
4. Click "Create Admin Account"

**Create User Account:**
1. Go to: http://localhost:8080/register
2. Select "User" role
3. Fill in:
   - Username: `user1`
   - Email: `user1@test.com`
   - Password: `user123`
4. Click "Create Account"

### Step 3: Create an Event (as Admin)

1. **Login as Admin:**
   - Go to: http://localhost:8080/login
   - Username: `admin`
   - Password: `admin123`

2. **Create Event:**
   - Click "Manage Events" (or go to http://localhost:8080/admin/events)
   - Click "New Event"
   - Fill in event details:
     ```
     Title: Test Concert
     Description: This is a test event for QR scanning
     Venue: City Hall
     Date & Time: [Select any future date]
     Capacity: 100
     Price: 0 (or any amount)
     ```
   - Click "Save Event"

3. **Verify Event Created:**
   - You should see your event in the list
   - Note the event ID (shown in the # column)

### Step 4: Book the Event (as User)

1. **Logout from Admin:**
   - Click "Logout" button

2. **Login as User:**
   - Username: `user1`
   - Password: `user123`

3. **Book the Event:**
   - Click "Events" in navigation
   - Find "Test Concert"
   - Click "Book Now"
   - You should see: "Booking confirmed! Check your tickets."

4. **View Your Ticket:**
   - Click "My Bookings" in navigation
   - Find your booking
   - Click "View Ticket"
   - **You should now see a QR code!** 🎉

---

## 📱 How to Scan the QR Code

### Method 1: Using Your Phone (Recommended)

This is the **real-world scenario** - how it would work at an actual event.

**Setup:**
1. Make sure your computer and phone are on the **same WiFi network**
2. Find your computer's local IP address:

   **On Mac:**
   ```bash
   ifconfig | grep "inet " | grep -v 127.0.0.1
   ```
   Look for something like: `192.168.1.100`

   **On Windows:**
   ```bash
   ipconfig
   ```
   Look for "IPv4 Address" like: `192.168.1.100`

3. **Update application.properties:**
   ```properties
   # Change this line:
   app.base-url=http://localhost:8080
   
   # To your IP address:
   app.base-url=http://192.168.1.100:8080
   ```

4. **Restart the application:**
   - Stop the app (Ctrl+C)
   - Run again: `mvn spring-boot:run`

5. **Create a new booking** (the old QR code has the old URL):
   - Login as user
   - Book the event again (or book a different event)
   - View the new ticket

**Scanning Process:**

1. **On your phone:**
   - Open the native Camera app (iPhone or Android)
   - Point camera at the QR code on your computer screen
   - A notification will appear at the top
   - Tap the notification

2. **What happens:**
   - Your phone's browser opens
   - URL looks like: `http://192.168.1.100:8080/admin/scan?ticketCode=TKT-1-2-ABCD1234`
   - You'll see a login page (because you're not logged in on phone)

3. **Login as Admin on your phone:**
   - Username: `admin`
   - Password: `admin123`

4. **After login:**
   - The scan page will load automatically
   - You'll see a **GREEN screen** with:
     - ✅ "Entry Granted — Ticket Valid"
     - Attendee name
     - Event details
     - Ticket code

5. **Try scanning again:**
   - Point camera at the same QR code
   - Tap notification
   - Now you'll see a **RED screen** with:
     - ❌ "Invalid Ticket"
     - "Ticket has already been used"

**🎉 Success! The QR scanning is working!**

---

### Method 2: Manual Testing (Without Phone)

If you don't have a phone or can't get the WiFi setup working, you can test manually:

1. **View the ticket:**
   - Login as user
   - Go to "My Bookings"
   - Click "View Ticket"

2. **Copy the ticket code:**
   - You'll see something like: `TKT-1-2-ABCD1234`
   - Copy this code

3. **Open scan page as admin:**
   - Open a new browser tab (or incognito window)
   - Go to: http://localhost:8080/login
   - Login as admin

4. **Go to scan page:**
   - Click "Scan" button in navbar
   - Or go to: http://localhost:8080/admin/scan

5. **Enter ticket code manually:**
   - Paste the ticket code in the input field
   - Click "Validate Ticket"

6. **See the result:**
   - ✅ Green screen = Valid ticket
   - ❌ Red screen = Invalid/Used/Cancelled ticket

---

## 🔍 Testing Different Scenarios

### Scenario 1: Valid Ticket (First Scan)
```
Status: CONFIRMED
Result: ✅ Entry Granted
Message: Shows attendee details
Action: Ticket marked as USED
```

### Scenario 2: Already Used Ticket
```
Status: USED
Result: ❌ Invalid Ticket
Message: "Ticket has already been used"
Action: Entry denied
```

### Scenario 3: Cancelled Ticket
```
1. Login as user
2. Go to "My Bookings"
3. Click cancel button (X)
4. Try to scan the ticket
Result: ❌ "Ticket is cancelled"
```

### Scenario 4: Wrong Admin
```
1. Create another admin account (admin2)
2. Admin2 creates an event
3. User books admin2's event
4. Try to scan with admin1 account
Result: ❌ "You can only scan tickets for your own events"
```

### Scenario 5: Invalid Ticket Code
```
1. Go to scan page
2. Enter: TKT-999-999-INVALID
3. Click validate
Result: ❌ "Invalid ticket code"
```

---

## 🎥 Video Tutorial Steps

If you want to record a demo, follow this sequence:

1. **Show the booking process:**
   - Login as user
   - Browse events
   - Book an event
   - Go to "My Bookings"
   - Click "View Ticket"
   - Show the QR code

2. **Show the scanning process:**
   - Open phone camera
   - Scan QR code
   - Show the URL opening
   - Login as admin
   - Show the green success screen

3. **Show the security:**
   - Try scanning the same ticket again
   - Show the red error screen
   - Explain "already used"

---

## 🐛 Troubleshooting

### Problem: QR Code Not Displaying

**Check:**
```bash
# In terminal where app is running, look for:
"QR code generated successfully"
```

**Solution:**
- Check if ZXing dependencies are in pom.xml
- Run: `mvn clean install`
- Restart application

### Problem: Phone Can't Access the URL

**Symptoms:**
- Phone shows "Can't reach this page"
- Connection timeout

**Solutions:**

1. **Check same WiFi:**
   ```bash
   # On computer, check WiFi name
   # On phone, check WiFi name
   # Must be the same network!
   ```

2. **Check firewall:**
   ```bash
   # Mac: System Preferences > Security > Firewall
   # Windows: Control Panel > Windows Defender Firewall
   # Allow Java/Spring Boot through firewall
   ```

3. **Verify IP address:**
   ```bash
   # Test from phone browser:
   http://192.168.1.100:8080
   # Should show login page
   ```

4. **Check port 8080:**
   ```bash
   # Make sure nothing else is using port 8080
   lsof -i :8080  # Mac/Linux
   netstat -ano | findstr :8080  # Windows
   ```

### Problem: "Invalid ticket code" for Valid Ticket

**Check:**
1. Is the ticket in the database?
   ```sql
   SELECT * FROM bookings WHERE ticket_code = 'TKT-1-2-ABCD1234';
   ```

2. Is the admin logged in?
   - Must be logged in as admin
   - Must be the event creator

3. Is the ticket status CONFIRMED?
   ```sql
   SELECT status FROM bookings WHERE ticket_code = 'TKT-1-2-ABCD1234';
   ```

### Problem: QR Code Scans but Nothing Happens

**Check:**
1. **URL in QR code:**
   - View ticket page
   - Right-click QR code > "Open image in new tab"
   - Use online QR decoder: https://zxing.org/w/decode
   - Verify URL is correct

2. **Base URL setting:**
   ```properties
   # In application.properties
   app.base-url=http://192.168.1.100:8080
   # Must match your IP address
   ```

3. **Recreate booking:**
   - If you changed app.base-url
   - Old QR codes have old URL
   - Create new booking to get new QR code

---

## 📱 Alternative: Using QR Code Scanner App

If your phone's camera doesn't scan QR codes automatically:

1. **Download a QR scanner app:**
   - iPhone: "QR Code Reader" (free)
   - Android: "QR Code Scanner" (free)

2. **Use the app:**
   - Open the QR scanner app
   - Point at QR code
   - Tap the URL that appears
   - Browser will open

---

## 🎯 Quick Test Checklist

Use this checklist to verify everything works:

- [ ] Application running on http://localhost:8080
- [ ] Admin account created
- [ ] User account created
- [ ] Event created by admin
- [ ] Event booked by user
- [ ] Ticket page shows QR code
- [ ] QR code can be scanned (or manually entered)
- [ ] First scan shows green success screen
- [ ] Ticket marked as USED in database
- [ ] Second scan shows red error screen
- [ ] Cancelled ticket shows error
- [ ] Invalid code shows error

---

## 🌐 For Production/Real Events

When deploying for real events:

1. **Use a proper domain:**
   ```properties
   app.base-url=https://yourdomain.com
   ```

2. **Enable HTTPS:**
   - Get SSL certificate
   - Configure Spring Boot for HTTPS
   - QR codes will have https:// URLs

3. **Test before event:**
   - Create test bookings
   - Scan with multiple phones
   - Verify all scenarios work

4. **At the event:**
   - Have backup manual entry ready
   - Keep admin phone charged
   - Use good lighting for scanning
   - Have WiFi/data connection

---

## 💡 Pro Tips

1. **Print QR codes:**
   - Users can print tickets at home
   - Easier to scan than phone screens sometimes

2. **Backup codes:**
   - Always have ticket code visible
   - Can manually enter if QR fails

3. **Multiple admins:**
   - Each admin can only scan their own events
   - Create separate admin accounts for different organizers

4. **Test early:**
   - Test scanning setup before event day
   - Verify WiFi/network works at venue

5. **User instructions:**
   - Tell users to have ticket ready
   - Brightness up on phone screen
   - Have backup email/screenshot

---

## 📞 Need Help?

If you're still having issues:

1. **Check console logs:**
   ```bash
   # Look for errors in terminal where app is running
   ```

2. **Check browser console:**
   ```
   F12 > Console tab
   Look for JavaScript errors
   ```

3. **Check database:**
   ```sql
   -- Verify booking exists
   SELECT * FROM bookings;
   
   -- Check ticket status
   SELECT ticket_code, status FROM bookings;
   ```

4. **Review documentation:**
   - QR_AND_EMAIL_EXPLANATION.md
   - SYSTEM_FLOW_DIAGRAM.md
   - QUICK_REFERENCE.md

---

## ✅ Success Criteria

You'll know it's working when:
- ✅ QR code displays on ticket page
- ✅ Phone camera can scan the QR code
- ✅ Browser opens the scan URL
- ✅ Admin can login and see validation result
- ✅ First scan shows green success
- ✅ Second scan shows red error
- ✅ Ticket status changes to USED in database

**That's it! Your QR code scanning system is now fully functional!** 🎉
