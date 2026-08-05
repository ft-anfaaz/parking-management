package com.parking.system.dao;

import com.parking.system.db.DatabaseConnection;
import com.parking.system.model.Booking;
import com.parking.system.model.Vehicle;
import com.parking.system.model.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    private final BookingDAO bookingDAO = new BookingDAO();

    private static final String SELECT_JOIN =
            "SELECT v.*, c.full_name AS customer_name FROM vehicles v " +
            "JOIN customers c ON c.id = v.customer_id ";

    public List<Vehicle> findAll() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT_JOIN + "ORDER BY v.vehicle_number")) {
            while (rs.next()) {
                vehicles.add(map(rs));
            }
        }
        return vehicles;
    }

    public Vehicle findByNumber(String vehicleNumber) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + "WHERE v.vehicle_number = ?")) {
            ps.setString(1, vehicleNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public void insert(Vehicle v) throws SQLException {
        String sql = "INSERT INTO vehicles (customer_id, vehicle_number, vehicle_type, model) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getCustomerId());
            ps.setString(2, v.getVehicleNumber());
            ps.setString(3, v.getVehicleType().name());
            ps.setString(4, v.getModel());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    v.setId(keys.getInt(1));
                }
            }
        }
    }

    public void update(Vehicle v) throws SQLException {
        String sql = "UPDATE vehicles SET customer_id=?, vehicle_number=?, vehicle_type=?, model=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, v.getCustomerId());
            ps.setString(2, v.getVehicleNumber());
            ps.setString(3, v.getVehicleType().name());
            ps.setString(4, v.getModel());
            ps.setInt(5, v.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        Booking active = bookingDAO.findActiveByVehicleId(id);
        if (active != null) {
            throw new IllegalStateException(active.getVehicleNumber() + " is currently parked in slot "
                    + active.getSlotNumber() + ". Check it out before deleting this vehicle.");
        }
        String sql = "DELETE FROM vehicles WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Vehicle map(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("vehicle_number"),
                VehicleType.valueOf(rs.getString("vehicle_type")),
                rs.getString("model")
        );
    }
}
