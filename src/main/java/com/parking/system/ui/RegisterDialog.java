package com.parking.system.ui;

import com.parking.system.dao.UserDAO;
import com.parking.system.util.Validators;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** "Create Account" dialog, opened from the login screen. */
public class RegisterDialog extends JDialog {

    private final UserDAO userDAO = new UserDAO();

    private final JTextField fullNameField = new JTextField(16);
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JPasswordField confirmPasswordField = new JPasswordField(16);
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[]{"OPERATOR", "ADMIN"});

    private String registeredUsername;

    public RegisterDialog(Frame owner) {
        super(owner, "Create Account", true);
        setResizable(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create a New Account");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(title, gbc);
        gbc.gridwidth = 1;

        addRow(form, gbc, 1, "Full Name:", fullNameField);
        addRow(form, gbc, 2, "Username:", usernameField);
        addHint(form, gbc, 3, "e.g. jdoe");
        addRow(form, gbc, 4, "Password:", passwordField);
        addHint(form, gbc, 5, "At least 6 characters");
        addRow(form, gbc, 6, "Confirm Password:", confirmPasswordField);
        addRow(form, gbc, 7, "Role:", roleCombo);

        JButton createBtn = new JButton("Create Account");
        createBtn.addActionListener(e -> register());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelBtn);
        buttons.add(createBtn);
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        form.add(buttons, gbc);

        setContentPane(form);
        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addHint(JPanel panel, GridBagConstraints gbc, int y, String text) {
        JLabel hint = new JLabel(text);
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        hint.setForeground(Color.GRAY);
        gbc.gridx = 1; gbc.gridy = y;
        panel.add(hint, gbc);
    }

    private void register() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name, username, and password are required.",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Validators.isValidName(fullName)) {
            JOptionPane.showMessageDialog(this,
                    "Full name must start with a capital letter, e.g. Athulya Venugopal.",
                    "Invalid name", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Validators.isValidUsername(username)) {
            JOptionPane.showMessageDialog(this,
                    "Username must be 3-20 characters: lowercase letters, digits, or underscore, "
                    + "starting with a letter, e.g. jdoe.",
                    "Invalid username", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Validators.isValidPassword(password)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.",
                    "Invalid password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.",
                    "Password mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (userDAO.usernameExists(username)) {
                JOptionPane.showMessageDialog(this,
                        "\"" + username + "\" is already taken. Choose a different username.",
                        "Username taken", JOptionPane.WARNING_MESSAGE);
                return;
            }
            userDAO.insert(username, password, fullName, role);
            registeredUsername = username;
            JOptionPane.showMessageDialog(this, "Account created. You can now log in as " + username + ".");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** The username just registered, or null if the dialog was cancelled/closed without success. */
    public String getRegisteredUsername() {
        return registeredUsername;
    }
}
