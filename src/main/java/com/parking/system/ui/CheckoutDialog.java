package com.parking.system.ui;

import com.parking.system.dao.PaymentDAO;
import com.parking.system.model.Booking;
import com.parking.system.model.Payment;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Shown after a booking is checked out: computes the fee and records a payment. */
public class CheckoutDialog extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final Booking booking;
    private final JComboBox<Payment.Mode> modeCombo = new JComboBox<>(Payment.Mode.values());
    private boolean confirmed = false;

    public CheckoutDialog(Frame owner, Booking booking) {
        super(owner, "Checkout - " + booking.getVehicleNumber(), true);
        this.booking = booking;

        BigDecimal rate;
        int hours;
        BigDecimal amount;
        try {
            rate = paymentDAO.getRate(booking.getVehicleType());
        } catch (SQLException e) {
            rate = BigDecimal.ZERO;
        }
        LocalDateTime exit = booking.getExitTime() != null ? booking.getExitTime() : LocalDateTime.now();
        hours = PaymentDAO.hoursCharged(booking.getEntryTime(), exit);
        amount = rate.multiply(BigDecimal.valueOf(hours));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addRow(panel, gbc, 0, "Vehicle:", booking.getVehicleNumber() + " (" + booking.getVehicleType() + ")");
        addRow(panel, gbc, 1, "Slot:", booking.getSlotNumber());
        addRow(panel, gbc, 2, "Entry Time:", booking.getEntryTime().format(FMT));
        addRow(panel, gbc, 3, "Exit Time:", exit.format(FMT));
        addRow(panel, gbc, 4, "Hours Charged:", String.valueOf(hours));
        addRow(panel, gbc, 5, "Rate / Hour:", "₹" + rate.toPlainString());

        JLabel amountLabel = new JLabel("₹" + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
        amountLabel.setFont(amountLabel.getFont().deriveFont(Font.BOLD, 16f));
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Amount Due:"), gbc);
        gbc.gridx = 1;
        panel.add(amountLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("Payment Mode:"), gbc);
        gbc.gridx = 1;
        panel.add(modeCombo, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmBtn = new JButton("Confirm Payment");
        JButton cancelBtn = new JButton("Cancel");
        confirmBtn.addActionListener(e -> {
            try {
                paymentDAO.recordPayment(booking, paymentDAO.getRate(booking.getVehicleType()),
                        (Payment.Mode) modeCombo.getSelectedItem());
                confirmed = true;
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Could not record payment:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dispose());
        buttons.add(cancelBtn);
        buttons.add(confirmBtn);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        panel.add(buttons, gbc);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(value), gbc);
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
