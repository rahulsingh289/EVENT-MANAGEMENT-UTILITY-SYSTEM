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
- Ticket marked as **USED** on first scan (prevents re-entry)

---

## Project Structure

```
src/main/java/com/example/eventmanagement/
├── EventManagementApplication.java   # Entry point + admin promotion runner
├── config/
│   └── SecurityConfig.java           # Spring Security rules
├── controller/
│   ├── AuthController.java           # /login, /register, /register/admin
│   ├── DashboardController.java      # /dashboard, /my-bookings
│   └── EventController.java          # All event, booking, and scan routes
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
    ├── QrCodeService.java            # ZXing QR generation as Base64 PNG
    └── TicketEmailService.java       # Async email with embedded QR (optional)

src/main/resources/
├── application.properties
└── templates/
    ├── login.html
    ├── register.html
    ├── dashboard.html
    ├── event-list.html
    ├── event-detail.html
    ├── my-bookings.html
    ├── ticket.html
    ├── fragments/navbar.html
    └── admin/
        ├── event-management.html
        ├── event-form.html
        ├── event-bookings.html
        └── scan.html
```

---

## Prerequisites

- Java 17
- MySQL 8
- Maven 3.6+ (or use the jar directly)

---

## Setup & Running

### 1. Create the database

```sql
CREATE DATABASE IF NOT EXISTS eventdb;
```

### 2. Configure application.properties

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/eventdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 3. Build

```bash
mvn clean package -DskipTests
```

### 4. Run

```bash
java -jar target/event-management-0.0.1-SNAPSHOT.jar
```

App starts at **http://localhost:8080**

---

## Creating an Admin Account

### Option A — Promote on startup (recommended for first run)

1. Register a normal account at `/register`
2. Add this line to `application.properties`:
   ```properties
   app.init-admin-username=your_username
   ```
3. Restart the app — the console will print:
   ```
   [Init] Promoted 'your_username' to ROLE_ADMIN
   ```
4. Remove the line after promotion (optional but clean)

### Option B — Secret registration endpoint

Visit:
```
http://localhost:8080/register/admin?secret=admin123
```
Register a new account — it will be created with `ROLE_ADMIN` directly.

Change the secret in `application.properties`:
```properties
app.admin-secret=admin123
```

---

## QR Ticket Flow

1. User books an event → booking created with a unique ticket code (`TKT-{eventId}-{userId}-{uuid}`)
2. User opens **My Bookings** → clicks **View Ticket**
3. Ticket page shows a QR code — the QR encodes the full URL:
   ```
   http://localhost:8080/admin/scan?ticketCode=TKT-...
   ```
4. Admin scans the QR with their phone's native camera app
5. Phone opens the scan URL in the browser → ticket is validated and marked **USED**
6. Admin can also type the ticket code manually on the `/admin/scan` page

---

## Email Tickets (Optional)

Email is disabled by default. To enable:

1. Set up a Gmail App Password at https://myaccount.google.com/apppasswords
2. Update `application.properties`:
   ```properties
   app.mail.enabled=true
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   ```

When enabled, a ticket email with embedded QR is sent automatically after booking.

---

## Routes Reference

| Method | URL | Access | Description |
|---|---|---|---|
| GET | `/login` | Public | Login page |
| GET/POST | `/register` | Public | User registration |
| GET/POST | `/register/admin?secret=...` | Public | Admin registration |
| GET | `/dashboard` | Authenticated | Dashboard with stats |
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

## Resetting MySQL Root Password (macOS)

If you get `Access denied for user 'root'@'localhost'`:

```bash
# Stop MySQL
sudo /usr/local/mysql/support-files/mysql.server stop

# Start in safe mode
sudo /usr/local/mysql/bin/mysqld_safe --skip-grant-tables &

# Connect and reset
/usr/local/mysql/bin/mysql -u root
```

```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'your_new_password';
FLUSH PRIVILEGES;
EXIT;
```

```bash
# Restart normally
sudo killall mysqld_safe mysqld
sudo /usr/local/mysql/support-files/mysql.server start
```

Then update `application.properties` with the new password.
