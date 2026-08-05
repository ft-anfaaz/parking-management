package com.parking.system.ui.panels;

import com.parking.system.dao.CustomerDAO;
import com.parking.system.model.Customer;
import com.parking.system.util.Validators;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class CustomerPanel extends JPanel {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Phone", "Email", "Address", "License No."}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField nameField = new JTextField(18);
    private final JTextField phoneField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField addressField = new JTextField(18);
    private final JTextField licenseField = new JTextField(18);
    private final JTextField searchField = new JTextField(18);

    private Integer selectedId = null;

    public CustomerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(header(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(formPanel(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                loadSelectedIntoForm();
            }
        });

        refresh();
    }

    private JPanel header() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Customers");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        panel.add(title, BorderLayout.WEST);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchBox.add(new JLabel("Search:"));
        searchBox.add(searchField);
        JButton searchBtn = new JButton("Go");
        searchBtn.addActionListener(e -> search());
        searchBox.add(searchBtn);
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        searchBox.add(clearBtn);
        panel.add(searchBox, BorderLayout.EAST);
        return panel;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, gbc, 0, 0, "Full Name:", nameField, null);
        addField(panel, gbc, 2, 0, "Phone:", phoneField, "Exactly 10 digits, e.g. 9900011122");
        addField(panel, gbc, 0, 2, "Email:", emailField, "e.g. athulya@example.com");
        addField(panel, gbc, 2, 2, "License No.:", licenseField, "e.g. KA20 2020 0012345");
        addField(panel, gbc, 0, 4, "Address:", addressField, null);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> addCustomer());
        JButton updateBtn = new JButton("Update Selected");
        updateBtn.addActionListener(e -> updateCustomer());
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteCustomer());
        JButton clearBtn = new JButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 4; gbc.weightx = 0;
        panel.add(buttons, gbc);

        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int x, int y, String label, JTextField field,
                           String hint) {
        gbc.gridx = x; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = x + 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
        if (hint != null) {
            JLabel hintLabel = new JLabel("<html><div style='width:280px'>" + hint + "</div></html>");
            hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 11f));
            hintLabel.setForeground(Color.GRAY);
            gbc.gridx = x + 1; gbc.gridy = y + 1; gbc.weightx = 1.0;
            panel.add(hintLabel, gbc);
        }
    }

    private void refresh() {
        try {
            populate(customerDAO.findAll());
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void search() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            refresh();
            return;
        }
        try {
            populate(customerDAO.search(keyword));
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void populate(List<Customer> customers) {
        tableModel.setRowCount(0);
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{c.getId(), c.getFullName(), c.getPhone(), c.getEmail(),
                    c.getAddress(), c.getLicenseNo()});
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        selectedId = (Integer) tableModel.getValueAt(row, 0);
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        phoneField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        emailField.setText(valueOrEmpty(tableModel.getValueAt(row, 3)));
        addressField.setText(valueOrEmpty(tableModel.getValueAt(row, 4)));
        licenseField.setText(valueOrEmpty(tableModel.getValueAt(row, 5)));
    }

    private String valueOrEmpty(Object o) {
        return o == null ? "" : o.toString();
    }

    private void addCustomer() {
        if (!validateForm()) return;
        try {
            Customer c = new Customer();
            applyFormTo(c);
            customerDAO.insert(c);
            clearForm();
            refresh();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void updateCustomer() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a customer from the table first.");
            return;
        }
        if (!validateForm()) return;
        try {
            Customer c = new Customer();
            c.setId(selectedId);
            applyFormTo(c);
            customerDAO.update(c);
            clearForm();
            refresh();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void deleteCustomer() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a customer from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this customer and all of their vehicles?", "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            customerDAO.delete(selectedId);
            clearForm();
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot delete customer",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and phone are required.",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!Validators.isValidName(nameField.getText())) {
            JOptionPane.showMessageDialog(this,
                    "Name must start with a capital letter and contain only letters, e.g. Athulya Venugopal.",
                    "Invalid name", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!Validators.isValidPhone(phoneField.getText())) {
            JOptionPane.showMessageDialog(this,
                    "Phone number must be exactly 10 digits, e.g. 9900011122.",
                    "Invalid phone number", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!emailField.getText().trim().isEmpty() && !Validators.isValidEmail(emailField.getText())) {
            JOptionPane.showMessageDialog(this,
                    "Email must look like name@example.com.",
                    "Invalid email", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!licenseField.getText().trim().isEmpty() && !Validators.isValidLicenseNo(licenseField.getText())) {
            JOptionPane.showMessageDialog(this,
                    "License number must be state+RTO code, issue year, then a 7-digit number, " +
                    "e.g. KA20 2020 0012345.",
                    "Invalid license number", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!addressField.getText().trim().isEmpty() && !Validators.isValidAddress(addressField.getText())) {
            JOptionPane.showMessageDialog(this,
                    "Address must be at least 10 characters (letters, numbers, spaces, , . / # -).",
                    "Invalid address", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void applyFormTo(Customer c) {
        c.setFullName(nameField.getText().trim());
        c.setPhone(phoneField.getText().trim());
        c.setEmail(emailField.getText().trim());
        c.setAddress(addressField.getText().trim());
        c.setLicenseNo(licenseField.getText().trim());
    }

    private void clearForm() {
        selectedId = null;
        table.clearSelection();
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        licenseField.setText("");
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
