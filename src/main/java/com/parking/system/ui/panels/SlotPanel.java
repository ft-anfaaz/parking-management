package com.parking.system.ui.panels;

import com.parking.system.dao.BookingDAO;
import com.parking.system.dao.SlotDAO;
import com.parking.system.model.Booking;
import com.parking.system.model.ParkingSlot;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class SlotPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final SlotDAO slotDAO = new SlotDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Slot No.", "Floor", "Type", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel summaryLabel = new JLabel();

    private final JLabel detailSlot = new JLabel("-");
    private final JLabel detailStatus = new JLabel("-");
    private final JLabel detailVehicleNo = new JLabel("-");
    private final JLabel detailVehicleType = new JLabel("-");
    private final JLabel detailOwner = new JLabel("-");
    private final JLabel detailPhone = new JLabel("-");
    private final JLabel detailEntryTime = new JLabel("-");
    private final JLabel detailDuration = new JLabel("-");
    private final JLabel detailHint = new JLabel("Select a slot above to see who's parked there.");

    public SlotPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Parking Slots");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);

        JPanel south = new JPanel(new BorderLayout(0, 10));
        south.add(summaryLabel, BorderLayout.NORTH);
        south.add(occupantPanel(), BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedSlotDetails();
            }
        });

        refresh();
    }

    private JPanel occupantPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Selected Slot"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 3, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        panel.add(detailHint, gbc);
        gbc.gridwidth = 1;

        addRow(panel, gbc, 0, "Slot:", detailSlot, "Status:", detailStatus);
        addRow(panel, gbc, 1, "Vehicle No.:", detailVehicleNo, "Vehicle Type:", detailVehicleType);
        addRow(panel, gbc, 2, "Owner:", detailOwner, "Phone:", detailPhone);
        addRow(panel, gbc, 3, "Entry Time:", detailEntryTime, "Parked For:", detailDuration);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label1, JLabel value1,
                         String label2, JLabel value2) {
        int y = row + 1;
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel(label1), gbc);
        gbc.gridx = 1;
        panel.add(value1, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel(label2), gbc);
        gbc.gridx = 3;
        panel.add(value2, gbc);
    }

    public void refresh() {
        try {
            tableModel.setRowCount(0);
            for (ParkingSlot slot : slotDAO.findAll()) {
                tableModel.addRow(new Object[]{slot.getId(), slot.getSlotNumber(), slot.getFloor(),
                        slot.getSlotType(), slot.getStatus()});
            }
            int available = slotDAO.countByStatus(ParkingSlot.Status.AVAILABLE);
            int occupied = slotDAO.countByStatus(ParkingSlot.Status.OCCUPIED);
            summaryLabel.setText(String.format("  Available: %d   |   Occupied: %d   |   Total: %d",
                    available, occupied, available + occupied));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        table.clearSelection();
        clearOccupantDetails();
    }

    private void showSelectedSlotDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            clearOccupantDetails();
            return;
        }

        int slotId = (Integer) tableModel.getValueAt(row, 0);
        String slotNumber = String.valueOf(tableModel.getValueAt(row, 1));
        String status = String.valueOf(tableModel.getValueAt(row, 4));

        detailSlot.setText(slotNumber);
        detailStatus.setText(status);

        if (!"OCCUPIED".equals(status)) {
            detailHint.setText("This slot is available - no vehicle is parked here.");
            clearVehicleFields();
            return;
        }

        try {
            Booking booking = bookingDAO.findActiveBySlotId(slotId);
            if (booking == null) {
                detailHint.setText("No active booking found for this slot - try Refresh.");
                clearVehicleFields();
                return;
            }
            detailHint.setText(" ");
            detailVehicleNo.setText(booking.getVehicleNumber());
            detailVehicleType.setText(String.valueOf(booking.getVehicleType()));
            detailOwner.setText(booking.getCustomerName());
            detailPhone.setText(booking.getCustomerPhone());
            detailEntryTime.setText(booking.getEntryTime().format(FMT));
            detailDuration.setText(formatDuration(booking.getEntryTime()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatDuration(java.time.LocalDateTime entryTime) {
        Duration elapsed = Duration.between(entryTime, java.time.LocalDateTime.now());
        long hours = elapsed.toHours();
        long minutes = elapsed.toMinutesPart();
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }

    private void clearOccupantDetails() {
        detailSlot.setText("-");
        detailStatus.setText("-");
        detailHint.setText("Select a slot above to see who's parked there.");
        clearVehicleFields();
    }

    private void clearVehicleFields() {
        detailVehicleNo.setText("-");
        detailVehicleType.setText("-");
        detailOwner.setText("-");
        detailPhone.setText("-");
        detailEntryTime.setText("-");
        detailDuration.setText("-");
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                if ("OCCUPIED".equals(String.valueOf(value))) {
                    c.setBackground(new Color(255, 214, 214));
                } else {
                    c.setBackground(new Color(214, 255, 219));
                }
            }
            return c;
        }
    }
}
