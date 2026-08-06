# Parking Management System

A college mini-project (2020-21, Sahyadri College of Engineering & Management):
a Java Swing desktop app, backed by MySQL, that manages parking-space
allocation, customer registration, vehicle records, bookings, and payments.

Also available as a website: [parking-management-webapp](https://github.com/ft-anfaaz/parking-management-webapp)
— **live at https://parking-manager.duckdns.org** (login: `admin` / `admin123`).

## Features

- **Login** — staff authentication against a `users` table (SHA-256 hashed passwords).
- **Customers** — register/search/edit/delete customer records.
- **Vehicles** — register vehicles against a customer, tag them by type
  (two-wheeler / four-wheeler / heavy).
- **Parking Slots** — live availability tracking per slot, colour-coded
  available/occupied.
- **Bookings** — check a vehicle in by number; the system auto-allocates the
  first free slot matching its type (row-locked so two staff can't double-book
  the same slot) and frees the slot again on check-out.
- **Payments** — check-out computes the parked duration, looks up the
  per-vehicle-type hourly rate, and records a payment (cash/card/UPI).
- **Reports** — a small dashboard (available/occupied slots, active bookings,
  today's and total revenue) plus a full booking history table.

## Tech Stack

Java (Swing + JDBC), MySQL, built with Maven. Opens directly in NetBeans
(File → Open Project — NetBeans has native Maven support) or any other IDE.

## Setup

1. **Create the database.** With MySQL running locally:

   ```bash
   mysql -u root -p < database/schema.sql
   ```

   This creates the `parking_management` database, all tables, seed parking
   slots and rates, and a default login `admin` / `admin123`.

2. **Point the app at your database.** Edit the constants in
   [`DatabaseConnection.java`](src/main/java/com/parking/system/db/DatabaseConnection.java),
   or set environment variables before running:

   ```bash
   export PARKING_DB_URL="jdbc:mysql://localhost:3306/parking_management?useSSL=false&serverTimezone=UTC"
   export PARKING_DB_USER=root
   export PARKING_DB_PASSWORD=yourpassword
   ```

3. **Run it.**

   ```bash
   mvn compile exec:java
   ```

   Or build a self-contained jar and run that:

   ```bash
   mvn package
   java -jar target/parking-management-system-1.0-jar-with-dependencies.jar
   ```

   In NetBeans: File → Open Project → select this folder → Run.

## Project Structure

```
src/main/java/com/parking/system/
  Main.java                 entry point
  db/DatabaseConnection.java   JDBC connection factory
  model/                     Customer, Vehicle, ParkingSlot, Booking, Payment, User, VehicleType
  dao/                       one DAO per table; BookingDAO owns the check-in/check-out
                             transaction (slot row-lock, allocate/free, open/close booking)
  ui/                        LoginFrame, MainFrame (sidebar + CardLayout), CheckoutDialog
  ui/panels/                 CustomerPanel, VehiclePanel, SlotPanel, BookingPanel,
                             PaymentPanel, ReportPanel
database/schema.sql          tables + seed data (rates, slots, default admin login)
```

## Original Synopsis

This rebuild is based on a synopsis submitted for a Database Management mini
project (Computer Science & Engineering, Sahyadri College) titled "Parking
Management System" — allocating and tracking parking slots by vehicle to cut
down on the time and congestion caused by manually searching for a free spot.
