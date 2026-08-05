package com.parking.system.ui.panels;

import com.parking.system.dao.BookingDAO;
import com.parking.system.dao.VehicleDAO;
import com.parking.system.model.Booking;
import com.parking.system.model.Vehicle;
import com.parking.system.ui.CheckoutDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookingPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final BookingDAO bookingDAO = new BookingDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    private final JComboBox<Vehicle> vehicleCombo = new JComboBox<>();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Booking ID", "Vehicle No.", "Owner", "Slot", "Entry Time"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public BookingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(checkInPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(checkOutPanel(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel checkInPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Check In"));
        panel.add(new JLabel("Vehicle No.:"));
        panel.add(vehicleCombo);
        JButton checkInBtn = new JButton("Check In");
        checkInBtn.addActionListener(e -> checkIn());
        panel.add(checkInBtn);
        JButton refreshVehiclesBtn = new JButton("Refresh Vehicles");
        refreshVehiclesBtn.addActionListener(e -> loadVehicles());
        panel.add(refreshVehiclesBtn);
        return panel;
    }

    private JPanel checkOutPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Check Out"));
        JButton checkOutBtn = new JButton("Check Out Selected");
        checkOutBtn.addActionListener(e -> checkOutSelected());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        panel.add(checkOutBtn);
        panel.add(refreshBtn);
        return panel;
    }

    private void checkIn() {
        Vehicle vehicle = (Vehicle) vehicleCombo.getSelectedItem();
        if (vehicle == null) {
            JOptionPane.showMessageDialog(this,
                    "No vehicles available to check in.\nEither register one on the Vehicles screen, " +
                    "or every registered vehicle is already parked.",
                    "Nothing to check in", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Booking booking = bookingDAO.checkIn(vehicle);
            JOptionPane.showMessageDialog(this,
                    vehicle.getVehicleNumber() + " checked in to slot " + booking.getSlotNumber() + ".");
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot check in", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void loadVehicles() {
        try {
            Set<Integer> activeVehicleIds = new HashSet<>();
            for (Booking b : bookingDAO.findActive()) {
                activeVehicleIds.add(b.getVehicleId());
            }
            Vehicle previouslySelected = (Vehicle) vehicleCombo.getSelectedItem();
            vehicleCombo.removeAllItems();
            List<Vehicle> vehicles = vehicleDAO.findAll();
            for (Vehicle v : vehicles) {
                if (!activeVehicleIds.contains(v.getId())) {
                    vehicleCombo.addItem(v);
                }
            }
            if (previouslySelected != null) {
                for (int i = 0; i < vehicleCombo.getItemCount(); i++) {
                    if (vehicleCombo.getItemAt(i).getId() == previouslySelected.getId()) {
                        vehicleCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void checkOutSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an active booking from the table first.");
            return;
        }
        int bookingId = (Integer) tableModel.getValueAt(row, 0);
        String vehicleNo = String.valueOf(tableModel.getValueAt(row, 1));

        int confirm = JOptionPane.showConfirmDialog(this, "Check out " + vehicleNo + " now?",
                "Confirm check-out", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Booking completed = bookingDAO.checkOut(bookingId);
            refresh();
            Window owner = SwingUtilities.getWindowAncestor(this);
            CheckoutDialog dialog = new CheckoutDialog((Frame) owner, completed);
            dialog.setVisible(true);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot check out", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void refresh() {
        loadVehicles();
        try {
            tableModel.setRowCount(0);
            for (Booking b : bookingDAO.findActive()) {
                tableModel.addRow(new Object[]{b.getId(), b.getVehicleNumber(), b.getCustomerName(),
                        b.getSlotNumber(), b.getEntryTime().format(FMT)});
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
