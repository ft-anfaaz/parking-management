package com.parking.system.dao;

import com.parking.system.db.DatabaseConnection;
import com.parking.system.model.Booking;
import com.parking.system.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private final BookingDAO bookingDAO = new BookingDAO();

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY full_name";
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(map(rs));
            }
        }
        return customers;
    }

    public List<Customer> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM customers WHERE full_name LIKE ? OR phone LIKE ? ORDER BY full_name";
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(map(rs));
                }
            }
        }
        return customers;
    }

    public void insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (full_name, phone, email, address, license_no) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getLicenseNo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        }
    }

    public void update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET full_name=?, phone=?, email=?, address=?, license_no=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getLicenseNo());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        Booking active = bookingDAO.findActiveByCustomerId(id);
        if (active != null) {
            throw new IllegalStateException("This customer's vehicle " + active.getVehicleNumber()
                    + " is currently parked in slot " + active.getSlotNumber()
                    + ". Check it out before deleting this customer.");
        }
        String sql = "DELETE FROM customers WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getString("license_no")
        );
    }
}
