package com.parking.system.ui;

import com.parking.system.model.User;
import com.parking.system.ui.panels.BookingPanel;
import com.parking.system.ui.panels.CustomerPanel;
import com.parking.system.ui.panels.PaymentPanel;
import com.parking.system.ui.panels.ReportPanel;
import com.parking.system.ui.panels.SlotPanel;
import com.parking.system.ui.panels.VehiclePanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    public MainFrame(User user) {
        super("Parking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);

        content.add(new CustomerPanel(), "customers");
        content.add(new VehiclePanel(), "vehicles");
        content.add(new SlotPanel(), "slots");
        content.add(new BookingPanel(), "bookings");
        content.add(new PaymentPanel(), "payments");
        content.add(new ReportPanel(), "reports");

        JPanel sidebar = buildSidebar(user);

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        cardLayout.show(content, "bookings");
    }

    private JPanel buildSidebar(User user) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        sidebar.setPreferredSize(new Dimension(190, 0));
        sidebar.setBackground(new Color(38, 45, 58));

        JLabel who = new JLabel("<html><b>" + user.getFullName() + "</b><br/>" + user.getRole() + "</html>");
        who.setForeground(Color.WHITE);
        who.setAlignmentX(Component.LEFT_ALIGNMENT);
        who.setBorder(BorderFactory.createEmptyBorder(0, 4, 16, 0));
        sidebar.add(who);

        sidebar.add(navButton("Bookings", "bookings"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(navButton("Customers", "customers"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(navButton("Vehicles", "vehicles"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(navButton("Parking Slots", "slots"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(navButton("Payments", "payments"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(navButton("Reports", "reports"));

        sidebar.add(Box.createVerticalGlue());

        JButton logout = new JButton("Log Out");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        logout.setForeground(Color.WHITE);
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        sidebar.add(logout);

        return sidebar;
    }

    private JButton navButton(String label, String cardName) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.addActionListener(e -> cardLayout.show(content, cardName));
        button.setBorder(BorderFactory.createCompoundBorder(button.getBorder(),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return button;
    }
}
