package com.parking.system.dao;

import com.parking.system.db.DatabaseConnection;
import com.parking.system.model.Booking;
import com.parking.system.model.ParkingSlot;
import com.parking.system.model.Vehicle;
import com.parking.system.model.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    private static final String SELECT_JOIN =
            "SELECT b.*, v.vehicle_number, v.vehicle_type, c.full_name AS customer_name, " +
            "c.phone AS customer_phone, s.slot_number " +
            "FROM bookings b " +
            "JOIN vehicles v ON v.id = b.vehicle_id " +
            "JOIN customers c ON c.id = v.customer_id " +
            "JOIN parking_slots s ON s.id = b.slot_id ";

    /**
     * Allocates the first free slot matching the vehicle's type and opens a booking.
     * Throws IllegalStateException if the lot is full for that vehicle type, or if
     * the vehicle already has an active booking.
     */
    public Booking checkIn(Vehicle vehicle) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement dup = conn.prepareStatement(
                    "SELECT id FROM bookings WHERE vehicle_id=? AND status='ACTIVE'")) {
                dup.setInt(1, vehicle.getId());
                try (ResultSet rs = dup.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalStateException(
                                vehicle.getVehicleNumber() + " already has an active booking.");
                    }
                }
            }

            int slotId;
            String slotNumber;
            try (PreparedStatement slotPs = conn.prepareStatement(
                    "SELECT id, slot_number FROM parking_slots WHERE slot_type=? AND status='AVAILABLE' " +
                    "ORDER BY slot_number LIMIT 1 FOR UPDATE")) {
                slotPs.setString(1, vehicle.getVehicleType().name());
                try (ResultSet rs = slotPs.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException(
                                "No available slot for " + vehicle.getVehicleType() + ".");
                    }
                    slotId = rs.getInt("id");
                    slotNumber = rs.getString("slot_number");
                }
            }

            try (PreparedStatement occupy = conn.prepareStatement(
                    "UPDATE parking_slots SET status='OCCUPIED' WHERE id=?")) {
                occupy.setInt(1, slotId);
                occupy.executeUpdate();
            }

            LocalDateTime entryTime = LocalDateTime.now();
            int bookingId;
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO bookings (vehicle_id, slot_id, entry_time, status) VALUES (?, ?, ?, 'ACTIVE')",
                    Statement.RETURN_GENERATED_KEYS)) {
                insert.setInt(1, vehicle.getId());
                insert.setInt(2, slotId);
                insert.setTimestamp(3, Timestamp.valueOf(entryTime));
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    keys.next();
                    bookingId = keys.getInt(1);
                }
            }

            conn.commit();

            Booking booking = new Booking();
            booking.setId(bookingId);
            booking.setVehicleId(vehicle.getId());
            booking.setVehicleNumber(vehicle.getVehicleNumber());
            booking.setVehicleType(vehicle.getVehicleType());
            booking.setSlotId(slotId);
            booking.setSlotNumber(slotNumber);
            booking.setEntryTime(entryTime);
            booking.setStatus(Booking.Status.ACTIVE);
            return booking;
        } catch (SQLException | IllegalStateException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /** Closes the booking, frees its slot, and returns the updated booking (exit time set). */
    public Booking checkOut(int bookingId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Booking booking = findById(conn, bookingId);
            if (booking == null || booking.getStatus() != Booking.Status.ACTIVE) {
                throw new IllegalStateException("Booking is not active.");
            }

            LocalDateTime exitTime = LocalDateTime.now();
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE bookings SET exit_time=?, status='COMPLETED' WHERE id=?")) {
                update.setTimestamp(1, Timestamp.valueOf(exitTime));
                update.setInt(2, bookingId);
                update.executeUpdate();
            }

            try (PreparedStatement free = conn.prepareStatement(
                    "UPDATE parking_slots SET status='AVAILABLE' WHERE id=?")) {
                free.setInt(1, booking.getSlotId());
                free.executeUpdate();
            }

            conn.commit();

            booking.setExitTime(exitTime);
            booking.setStatus(Booking.Status.COMPLETED);
            return booking;
        } catch (SQLException | IllegalStateException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public List<Booking> findActive() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT_JOIN + "WHERE b.status='ACTIVE' ORDER BY b.entry_time")) {
            while (rs.next()) {
                bookings.add(map(rs));
            }
        }
        return bookings;
    }

    /** The active booking for this vehicle, or null if it isn't currently checked in. */
    public Booking findActiveByVehicleId(int vehicleId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_JOIN + "WHERE b.vehicle_id=? AND b.status='ACTIVE'")) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** The active booking for any vehicle owned by this customer, or null if none. */
    public Booking findActiveByCustomerId(int customerId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_JOIN + "WHERE v.customer_id=? AND b.status='ACTIVE'")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** The active booking currently occupying this slot, or null if the slot is free. */
    public Booking findActiveBySlotId(int slotId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_JOIN + "WHERE b.slot_id=? AND b.status='ACTIVE'")) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Booking> findAll() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT_JOIN + "ORDER BY b.entry_time DESC")) {
            while (rs.next()) {
                bookings.add(map(rs));
            }
        }
        return bookings;
    }

    private Booking findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + "WHERE b.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Booking map(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setVehicleId(rs.getInt("vehicle_id"));
        b.setVehicleNumber(rs.getString("vehicle_number"));
        b.setVehicleType(VehicleType.valueOf(rs.getString("vehicle_type")));
        b.setCustomerName(rs.getString("customer_name"));
        b.setCustomerPhone(rs.getString("customer_phone"));
        b.setSlotId(rs.getInt("slot_id"));
        b.setSlotNumber(rs.getString("slot_number"));
        Timestamp entry = rs.getTimestamp("entry_time");
        b.setEntryTime(entry != null ? entry.toLocalDateTime() : null);
        Timestamp exit = rs.getTimestamp("exit_time");
        b.setExitTime(exit != null ? exit.toLocalDateTime() : null);
        b.setStatus(Booking.Status.valueOf(rs.getString("status")));
        return b;
    }
}
