package com.parking.system.dao;

import com.parking.system.db.DatabaseConnection;
import com.parking.system.model.ParkingSlot;
import com.parking.system.model.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SlotDAO {

    public List<ParkingSlot> findAll() throws SQLException {
        List<ParkingSlot> slots = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM parking_slots ORDER BY slot_number")) {
            while (rs.next()) {
                slots.add(map(rs));
            }
        }
        return slots;
    }

    /** First free slot matching the vehicle type, or null if the lot is full for that type. */
    public ParkingSlot findFirstAvailable(VehicleType type) throws SQLException {
        String sql = "SELECT * FROM parking_slots WHERE slot_type = ? AND status = 'AVAILABLE' " +
                     "ORDER BY slot_number LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public void updateStatus(int slotId, ParkingSlot.Status status) throws SQLException {
        String sql = "UPDATE parking_slots SET status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, slotId);
            ps.executeUpdate();
        }
    }

    public int countByStatus(ParkingSlot.Status status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE status=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private ParkingSlot map(ResultSet rs) throws SQLException {
        return new ParkingSlot(
                rs.getInt("id"),
                rs.getString("slot_number"),
                rs.getInt("floor"),
                VehicleType.valueOf(rs.getString("slot_type")),
                ParkingSlot.Status.valueOf(rs.getString("status"))
        );
    }
}
