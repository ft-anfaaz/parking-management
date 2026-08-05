package com.parking.system.dao;

import com.parking.system.db.DatabaseConnection;
import com.parking.system.model.VehicleModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VehicleModelDAO {

    public List<VehicleModel> findByBrand(int brandId) throws SQLException {
        List<VehicleModel> models = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM vehicle_models WHERE brand_id = ? ORDER BY name")) {
            ps.setInt(1, brandId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    models.add(map(rs));
                }
            }
        }
        return models;
    }

    /** Looks up a model under the given brand by name (case-insensitive), creating it if needed. */
    public VehicleModel findOrCreate(int brandId, String name) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement lookup = conn.prepareStatement(
                    "SELECT * FROM vehicle_models WHERE brand_id = ? AND LOWER(name) = LOWER(?)")) {
                lookup.setInt(1, brandId);
                lookup.setString(2, name);
                try (ResultSet rs = lookup.executeQuery()) {
                    if (rs.next()) {
                        return map(rs);
                    }
                }
            }
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO vehicle_models (brand_id, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                insert.setInt(1, brandId);
                insert.setString(2, name);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    keys.next();
                    return new VehicleModel(keys.getInt(1), brandId, name);
                }
            }
        }
    }

    private VehicleModel map(ResultSet rs) throws SQLException {
        return new VehicleModel(rs.getInt("id"), rs.getInt("brand_id"), rs.getString("name"));
    }
}
