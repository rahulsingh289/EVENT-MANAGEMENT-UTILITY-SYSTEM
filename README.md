# Event Management System

A full-stack web application for managing events, bookings, and QR-based ticket validation. Built with Spring Boot 3, Thymeleaf, MySQL, and Spring Security.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.4.0 |
| Security | Spring Security 6 (form login, BCrypt) |
| Database | MySQL 8, Spring Data JPA / Hibernate |
| Frontend | Thymeleaf, Bootstrap 5.3.2, Bootstrap Icons 1.11.3 |
| QR Codes | Google ZXing 3.5.3 |
| Email | Spring Mail (Gmail SMTP, optional) |

---

## Features

### User
- Register and log in securely
- Browse and search upcoming events
- Book events (capacity-checked, duplicate-prevented)
- View personal bookings with status (Confirmed / Cancelled / Used)
- View and print QR ticket for each confirmed booking
- Cancel a booking

### Admin
- Create, edit, and delete events
- View all bookings per event
- Scan QR tickets via phone camera — auto-validates on scan
- Manual ticket code entry for validation
- Ticket marked as USED on first scan (prevents re-entry)

---

## Project Structure

```
src/main/java/com/example/eventmanagement/
├── EventManagementApplication.java
├── config/SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java
│   └── EventController.java
├── model/
│   ├── User.java
│   ├── Event.java
│   └── Booking.java
├── repository/
│   ├── UserRepository.java
│   ├── EventRepository.java
│   └── BookingRepository.java
└── service/
    ├── UserService.java
    ├── CustomUserDetailsService.java
    ├── EventService.java
    ├── BookingService.java
    ├── QrCodeService.java
    └── TicketEmailService.java

src/main/resources/
├── application.properties
└── templates/
    ├── login.html / register.html / dashboard.html
    ├── event-list.html / event-detail.html
    ├── my-bookings.html / ticket.html
    ├── fragments/navbar.html
    └── admin/
        ├── event-management.html / event-form.html
        ├── event-bookings.html / scan.html
```

---

## Prerequisites

- Java 17
- MySQL 8
- Maven 3.6+

---

## Setup & Running

### 1. Create the database

```sql
CREATE DATABASE IF NOT EXISTS eventdb;
```

### 2. Configure application.properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/eventdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 3. Build & Run

```bash
mvn clean package -DskipTests
java -jar target/event-management-0.0.1-SNAPSHOT.jar
```

App starts at **http://localhost:8080**

---

## Creating an Admin Account

**Option A — Promote on startup (recommended)**

1. Register a normal account at `/register`
2. Add to `application.properties`:
   ```properties
   app.init-admin-username=your_username
   ```
3. Restart — console will print `[Init] Promoted 'your_username' to ROLE_ADMIN`

**Option B — Secret registration endpoint**

```
http://localhost:8080/register/admin?secret=admin123
```

Change the secret in `application.properties`:
```properties
app.admin-secret=admin123
```

---

## QR Ticket Flow

1. User books an event → booking created with a unique ticket code (`TKT-{eventId}-{userId}-{uuid}`)
2. User opens My Bookings → clicks View Ticket
3. Ticket page shows a QR code encoding the full scan URL:
   ```
   http://localhost:8080/admin/scan?ticketCode=TKT-...
   ```
4. Admin scans the QR with their phone's native camera app
5. Phone opens the scan URL → ticket is validated and marked USED
6. Admin can also type the ticket code manually on `/admin/scan`

### Scanning with a Phone

To scan from a phone, your computer and phone must be on the same WiFi network.

1. Find your computer's local IP:
   ```bash
   ifconfig | grep "inet " | grep -v 127.0.0.1
   ```
2. Update `application.properties`:
   ```properties
   app.base-url=http://192.168.1.100:8080
   ```
3. Restart the app and create a new booking (old QR codes have the old URL)
4. Open the phone camera, point at the QR code, tap the notification
5. Log in as admin on the phone — the scan result will load automatically

### Validation Rules

| Check | Rule | Error |
|---|---|---|
| Exists | Ticket must be in database | "Invalid ticket code" |
| Owner | Admin must be event creator | "You can only scan tickets for your own events" |
| Status | Must be CONFIRMED | "Ticket has already been used" / "Ticket is cancelled" |
| One-time | Can only be used once | "Ticket has already been used" |

### Ticket Status Flow

```
CONFIRMED → USED (after first scan)
CONFIRMED → CANCELLED (user cancels)
```

---

## Email Tickets (Optional)

Email is disabled by default. To enable:

1. Get a Gmail App Password at https://myaccount.google.com/apppasswords
2. Update `application.properties`:
   ```properties
   app.mail.enabled=true
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   ```

When enabled, a ticket email with embedded QR is sent automatically after booking (async, non-blocking).

---

## Routes Reference

| Method | URL | Access | Description |
|---|---|---|---|
| GET | `/login` | Public | Login page |
| GET/POST | `/register` | Public | User registration |
| GET/POST | `/register/admin?secret=...` | Public | Admin registration |
| GET | `/dashboard` | Authenticated | Dashboard |
| GET | `/events` | Authenticated | Browse/search events |
| GET | `/events/{id}` | Authenticated | Event detail |
| POST | `/events/{id}/book` | Authenticated | Book an event |
| GET | `/my-bookings` | Authenticated | User's bookings |
| GET | `/bookings/{id}/ticket` | Authenticated | View QR ticket |
| POST | `/bookings/{id}/cancel` | Authenticated | Cancel booking |
| GET | `/admin/events` | Admin | Manage all events |
| GET/POST | `/admin/events/new` | Admin | Create event |
| GET/POST | `/admin/events/{id}/edit` | Admin | Edit event |
| POST | `/admin/events/{id}/delete` | Admin | Delete event |
| GET | `/admin/events/{id}/bookings` | Admin | View event bookings |
| GET/POST | `/admin/scan` | Admin | Scan / validate ticket |

---

## Troubleshooting

### QR code not displaying
- Verify ZXing dependencies are in `pom.xml`
- Run `mvn clean install` and restart

### Phone can't reach the app
- Confirm both devices are on the same WiFi
- Verify the IP in `app.base-url` matches your machine's IP
- Check that port 8080 is allowed through your firewall

### "Invalid ticket code" for a valid ticket
- Confirm you're logged in as the admin who created the event
- Check ticket status in the database: `SELECT status FROM bookings WHERE ticket_code = '...';`

### Resetting MySQL root password (macOS)

```bash
sudo /usr/local/mysql/support-files/mysql.server stop
sudo /usr/local/mysql/bin/mysqld_safe --skip-grant-tables &
/usr/local/mysql/bin/mysql -u root
```

```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
EXIT;
```

```bash
sudo killall mysqld_safe mysqld
sudo /usr/local/mysql/support-files/mysql.server start
```

---

## Security Features

- Unique UUID-based ticket codes (impossible to guess)
- Admins can only validate tickets for their own events
- One-time use enforcement (CONFIRMED → USED)
- All validation done server-side against the database
- CSRF protection on all forms
- BCrypt password hashing
