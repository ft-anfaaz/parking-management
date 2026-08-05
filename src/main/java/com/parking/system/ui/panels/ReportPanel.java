package com.parking.system.ui.panels;

import com.parking.system.dao.BookingDAO;
import com.parking.system.dao.PaymentDAO;
import com.parking.system.dao.SlotDAO;
import com.parking.system.model.Booking;
import com.parking.system.model.ParkingSlot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ReportPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final SlotDAO slotDAO = new SlotDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    private final JPanel statsPanel = new JPanel(new GridLayout(1, 5, 10, 0));
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Vehicle No.", "Owner", "Slot", "Entry Time", "Exit Time", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Reports");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        header.add(title, BorderLayout.WEST);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.add(header, BorderLayout.NORTH);
        top.add(statsPanel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            int available = slotDAO.countByStatus(ParkingSlot.Status.AVAILABLE);
            int occupied = slotDAO.countByStatus(ParkingSlot.Status.OCCUPIED);
            int activeBookings = bookingDAO.findActive().size();

            statsPanel.removeAll();
            statsPanel.add(statCard("Available Slots", String.valueOf(available)));
            statsPanel.add(statCard("Occupied Slots", String.valueOf(occupied)));
            statsPanel.add(statCard("Active Bookings", String.valueOf(activeBookings)));
            statsPanel.add(statCard("Today's Revenue", "₹" + paymentDAO.todayRevenue().toPlainString()));
            statsPanel.add(statCard("Total Revenue", "₹" + paymentDAO.totalRevenue().toPlainString()));
            statsPanel.revalidate();
            statsPanel.repaint();

            tableModel.setRowCount(0);
            for (Booking b : bookingDAO.findAll()) {
                tableModel.addRow(new Object[]{
                        b.getVehicleNumber(), b.getCustomerName(), b.getSlotNumber(),
                        b.getEntryTime().format(FMT),
                        b.getExitTime() != null ? b.getExitTime().format(FMT) : "-",
                        b.getStatus()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel statCard(String label, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 20f));
        JLabel captionLabel = new JLabel(label);
        captionLabel.setForeground(Color.GRAY);

        card.add(valueLabel);
        card.add(captionLabel);
        return card;
    }
}
