package com.parking.system.dao;

import com.parking.system.db.DatabaseConnection;
import com.parking.system.model.Brand;
import com.parking.system.model.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BrandDAO {

    public List<Brand> findAll() throws SQLException {
        List<Brand> brands = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM vehicle_brands ORDER BY name")) {
            while (rs.next()) {
                brands.add(map(rs));
            }
        }
        return brands;
    }

    /** Brands sold as the given vehicle type, e.g. only two-wheeler brands. */
    public List<Brand> findByType(VehicleType type) throws SQLException {
        List<Brand> brands = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM vehicle_brands WHERE vehicle_type = ? ORDER BY name")) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    brands.add(map(rs));
                }
            }
        }
        return brands;
    }

    /** Looks up a brand by name+type (case-insensitive on name), creating it if it doesn't exist yet. */
    public Brand findOrCreate(String name, VehicleType type) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement lookup = conn.prepareStatement(
                    "SELECT * FROM vehicle_brands WHERE LOWER(name) = LOWER(?) AND vehicle_type = ?")) {
                lookup.setString(1, name);
                lookup.setString(2, type.name());
                try (ResultSet rs = lookup.executeQuery()) {
                    if (rs.next()) {
                        return map(rs);
                    }
                }
            }
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO vehicle_brands (name, vehicle_type) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, name);
                insert.setString(2, type.name());
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    keys.next();
                    return new Brand(keys.getInt(1), name, type);
                }
            }
        }
    }

    private Brand map(ResultSet rs) throws SQLException {
        return new Brand(rs.getInt("id"), rs.getString("name"), VehicleType.valueOf(rs.getString("vehicle_type")));
    }
}
