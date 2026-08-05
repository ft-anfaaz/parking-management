package com.parking.system.ui.panels;

import com.parking.system.dao.PaymentDAO;
import com.parking.system.model.Payment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class PaymentPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final PaymentDAO paymentDAO = new PaymentDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Payment ID", "Vehicle No.", "Hours", "Rate/Hr", "Amount", "Mode", "Paid At"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel summaryLabel = new JLabel();

    public PaymentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Payments");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        header.add(title, BorderLayout.WEST);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(summaryLabel, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        try {
            tableModel.setRowCount(0);
            for (Payment p : paymentDAO.findAll()) {
                tableModel.addRow(new Object[]{p.getId(), p.getVehicleNumber(), p.getHoursCharged(),
                        "₹" + p.getRatePerHour().toPlainString(), "₹" + p.getAmount().toPlainString(),
                        p.getPaymentMode(), p.getPaymentTime().format(FMT)});
            }
            summaryLabel.setText(String.format("  Today's Revenue: ₹%s   |   Total Revenue: ₹%s",
                    paymentDAO.todayRevenue().toPlainString(), paymentDAO.totalRevenue().toPlainString()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
