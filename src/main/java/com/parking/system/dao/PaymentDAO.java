package com.parking.system.dao;

import com.parking.system.db.DatabaseConnection;
import com.parking.system.model.Booking;
import com.parking.system.model.Payment;
import com.parking.system.model.VehicleType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public BigDecimal getRate(VehicleType type) throws SQLException {
        String sql = "SELECT rate_per_hour FROM rates WHERE vehicle_type=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    /** Whole hours parked, rounded up, minimum 1 hour. */
    public static int hoursCharged(LocalDateTime entry, LocalDateTime exit) {
        long minutes = Duration.between(entry, exit).toMinutes();
        int hours = (int) Math.ceil(minutes / 60.0);
        return Math.max(hours, 1);
    }

    public Payment recordPayment(Booking booking, BigDecimal ratePerHour, Payment.Mode mode) throws SQLException {
        int hours = hoursCharged(booking.getEntryTime(), booking.getExitTime());
        BigDecimal amount = ratePerHour.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);
        LocalDateTime paymentTime = LocalDateTime.now();

        String sql = "INSERT INTO payments (booking_id, hours_charged, rate_per_hour, amount, payment_mode, payment_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getId());
            ps.setInt(2, hours);
            ps.setBigDecimal(3, ratePerHour);
            ps.setBigDecimal(4, amount);
            ps.setString(5, mode.name());
            ps.setTimestamp(6, Timestamp.valueOf(paymentTime));
            ps.executeUpdate();

            Payment payment = new Payment();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getInt(1));
                }
            }
            payment.setBookingId(booking.getId());
            payment.setVehicleNumber(booking.getVehicleNumber());
            payment.setHoursCharged(hours);
            payment.setRatePerHour(ratePerHour);
            payment.setAmount(amount);
            payment.setPaymentMode(mode);
            payment.setPaymentTime(paymentTime);
            return payment;
        }
    }

    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT p.*, v.vehicle_number FROM payments p " +
                     "JOIN bookings b ON b.id = p.booking_id " +
                     "JOIN vehicles v ON v.id = b.vehicle_id " +
                     "ORDER BY p.payment_time DESC";
        List<Payment> payments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                payments.add(map(rs));
            }
        }
        return payments;
    }

    public BigDecimal totalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    public BigDecimal todayRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE DATE(payment_time) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setBookingId(rs.getInt("booking_id"));
        p.setVehicleNumber(rs.getString("vehicle_number"));
        p.setHoursCharged(rs.getInt("hours_charged"));
        p.setRatePerHour(rs.getBigDecimal("rate_per_hour"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentMode(Payment.Mode.valueOf(rs.getString("payment_mode")));
        Timestamp ts = rs.getTimestamp("payment_time");
        p.setPaymentTime(ts != null ? ts.toLocalDateTime() : null);
        return p;
    }
}
