package com.parking.system.ui.panels;

import com.parking.system.dao.BrandDAO;
import com.parking.system.dao.CustomerDAO;
import com.parking.system.dao.VehicleDAO;
import com.parking.system.dao.VehicleModelDAO;
import com.parking.system.model.Brand;
import com.parking.system.model.Customer;
import com.parking.system.model.Vehicle;
import com.parking.system.model.VehicleModel;
import com.parking.system.model.VehicleType;
import com.parking.system.ui.ComboBoxFilter;
import com.parking.system.util.Validators;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehiclePanel extends JPanel {

    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BrandDAO brandDAO = new BrandDAO();
    private final VehicleModelDAO modelDAO = new VehicleModelDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Vehicle No.", "Type", "Model", "Owner"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private final JComboBox<Customer> customerCombo = new JComboBox<>();
    private final JTextField vehicleNumberField = new JTextField(14);
    private final JComboBox<VehicleType> typeCombo = new JComboBox<>(VehicleType.values());

    private final JComboBox<Brand> brandCombo = new JComboBox<>();
    private final JComboBox<VehicleModel> modelCombo = new JComboBox<>();
    private final JTextField otherBrandField = new JTextField(14);
    private final JTextField otherModelField = new JTextField(14);
    private final JLabel newBrandLabel = new JLabel("New Brand:");
    private final JLabel newModelLabel = new JLabel("New Model:");

    private List<Customer> allCustomers = new ArrayList<>();
    private List<Brand> allBrands = new ArrayList<>();
    private List<VehicleModel> currentModels = new ArrayList<>();

    private Integer selectedId = null;

    public VehiclePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Vehicles");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(formPanel(), BorderLayout.SOUTH);

        ComboBoxFilter.install(customerCombo, () -> allCustomers, Customer::toString);
        ComboBoxFilter.install(brandCombo, this::brandItemsWithOther, Brand::getName);
        ComboBoxFilter.install(modelCombo, this::modelItemsWithOther, VehicleModel::getName);
        ComboBoxFilter.onCommit(brandCombo, this::onBrandChanged);
        ComboBoxFilter.onCommit(modelCombo, this::onModelChanged);
        typeCombo.addActionListener(e -> loadBrandsForType(selectedType()));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                loadSelectedIntoForm();
            }
        });

        refresh();
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Vehicle Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("Owner:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(customerCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("Vehicle No.:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        panel.add(vehicleNumberField, gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 1.0;
        panel.add(hintLabel("e.g. KA19EF9001"), gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(new JLabel("Brand:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(brandCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        panel.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        panel.add(newBrandLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(otherBrandField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        panel.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(modelCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        panel.add(newModelLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(otherModelField, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> addVehicle());
        JButton updateBtn = new JButton("Update Selected");
        updateBtn.addActionListener(e -> updateVehicle());
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteVehicle());
        JButton clearBtn = new JButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());
        JButton refreshOwnersBtn = new JButton("Refresh Owners");
        refreshOwnersBtn.addActionListener(e -> loadCustomers());
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);
        buttons.add(refreshOwnersBtn);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 4; gbc.weightx = 0;
        panel.add(buttons, gbc);

        otherBrandField.setVisible(false);
        otherModelField.setVisible(false);
        newBrandLabel.setVisible(false);
        newModelLabel.setVisible(false);

        return panel;
    }

    private static JLabel hintLabel(String text) {
        JLabel label = new JLabel("<html><div style='width:260px'>" + text + "</div></html>");
        label.setFont(label.getFont().deriveFont(Font.ITALIC, 11f));
        label.setForeground(Color.GRAY);
        return label;
    }

    // ---- Brand / Model dropdown wiring -------------------------------------------------

    private List<Brand> brandItemsWithOther() {
        List<Brand> items = new ArrayList<>(allBrands);
        items.add(Brand.OTHER);
        return items;
    }

    private List<VehicleModel> modelItemsWithOther() {
        List<VehicleModel> items = new ArrayList<>(currentModels);
        items.add(VehicleModel.OTHER);
        return items;
    }

    private VehicleType selectedType() {
        Object sel = typeCombo.getSelectedItem();
        return sel instanceof VehicleType type ? type : VehicleType.values()[0];
    }

    private void loadBrandsForType(VehicleType type) {
        try {
            allBrands = brandDAO.findByType(type);
        } catch (SQLException ex) {
            showDbError(ex);
            allBrands = new ArrayList<>();
        }
        List<Brand> items = brandItemsWithOther();
        ComboBoxFilter.resetItems(brandCombo, items, items.get(0));
    }

    private void loadModelsForBrand(int brandId) {
        try {
            currentModels = modelDAO.findByBrand(brandId);
        } catch (SQLException ex) {
            showDbError(ex);
            currentModels = new ArrayList<>();
        }
        List<VehicleModel> items = modelItemsWithOther();
        ComboBoxFilter.resetItems(modelCombo, items, items.get(0));
    }

    private void onBrandChanged() {
        Object sel = brandCombo.getSelectedItem();
        boolean brandIsOther = sel instanceof Brand brand && brand.isOther();
        otherBrandField.setVisible(brandIsOther);
        newBrandLabel.setVisible(brandIsOther);

        if (sel instanceof Brand brand && brand.isOther()) {
            modelCombo.setVisible(false);
            currentModels = new ArrayList<>();
        } else if (sel instanceof Brand brand) {
            modelCombo.setVisible(true);
            loadModelsForBrand(brand.getId());
        } else {
            modelCombo.setVisible(false);
        }
        updateOtherModelVisibility();
        revalidate();
        repaint();
    }

    private void onModelChanged() {
        updateOtherModelVisibility();
        revalidate();
        repaint();
    }

    private void updateOtherModelVisibility() {
        boolean brandIsOther = brandCombo.getSelectedItem() instanceof Brand b && b.isOther();
        boolean modelIsOther = modelCombo.isVisible()
                && modelCombo.getSelectedItem() instanceof VehicleModel m && m.isOther();
        boolean visible = brandIsOther || modelIsOther;
        otherModelField.setVisible(visible);
        newModelLabel.setVisible(visible);
    }

    // ---- Data loading --------------------------------------------------------------------

    private void loadCustomers() {
        try {
            allCustomers = customerDAO.findAll();
        } catch (SQLException ex) {
            showDbError(ex);
            allCustomers = new ArrayList<>();
        }
        ComboBoxFilter.resetItems(customerCombo, allCustomers, allCustomers.isEmpty() ? null : allCustomers.get(0));
        if (allCustomers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No customers found. Add a customer first on the Customers screen.");
        }
    }

    private void refresh() {
        loadCustomers();
        loadBrandsForType(selectedType());
        try {
            populate(vehicleDAO.findAll());
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void populate(List<Vehicle> vehicles) {
        tableModel.setRowCount(0);
        for (Vehicle v : vehicles) {
            tableModel.addRow(new Object[]{v.getId(), v.getVehicleNumber(), v.getVehicleType(),
                    v.getModel(), v.getCustomerName()});
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        selectedId = (Integer) tableModel.getValueAt(row, 0);
        vehicleNumberField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        VehicleType type = (VehicleType) tableModel.getValueAt(row, 2);
        typeCombo.setSelectedItem(type);
        loadBrandsForType(type); // explicit: setSelectedItem above is a no-op event if the type is unchanged
        String ownerName = String.valueOf(tableModel.getValueAt(row, 4));
        for (Customer c : allCustomers) {
            if (c.getFullName().equals(ownerName)) {
                customerCombo.setSelectedItem(c);
                break;
            }
        }
        selectBrandAndModelFromStored(String.valueOf(tableModel.getValueAt(row, 3)));
    }

    /** Reverse-engineers the Brand/Model combo selection from a stored "Brand Model" string. */
    private void selectBrandAndModelFromStored(String storedModel) {
        for (Brand brand : allBrands) {
            String prefix = brand.getName() + " ";
            if (!storedModel.startsWith(prefix)) {
                continue;
            }
            String modelPart = storedModel.substring(prefix.length());
            brandCombo.setSelectedItem(brand);
            for (VehicleModel model : currentModels) {
                if (model.getName().equals(modelPart)) {
                    modelCombo.setSelectedItem(model);
                    return;
                }
            }
            modelCombo.setSelectedItem(VehicleModel.OTHER);
            otherModelField.setText(modelPart);
            return;
        }
        brandCombo.setSelectedItem(Brand.OTHER);
        otherBrandField.setText("");
        otherModelField.setText(storedModel);
    }

    // ---- Add / update / delete ------------------------------------------------------------

    private void addVehicle() {
        if (!(customerCombo.getSelectedItem() instanceof Customer owner)) {
            JOptionPane.showMessageDialog(this, "Pick an owner from the list.",
                    "Missing owner", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) return;
        try {
            String model = resolveBrandAndModel();
            Vehicle v = new Vehicle();
            v.setCustomerId(owner.getId());
            v.setVehicleNumber(vehicleNumberField.getText().trim().toUpperCase());
            v.setVehicleType((VehicleType) typeCombo.getSelectedItem());
            v.setModel(model);
            vehicleDAO.insert(v);
            clearForm();
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot save vehicle", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void updateVehicle() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a vehicle from the table first.");
            return;
        }
        if (!(customerCombo.getSelectedItem() instanceof Customer owner)) {
            JOptionPane.showMessageDialog(this, "Pick an owner from the list.",
                    "Missing owner", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) return;
        try {
            String model = resolveBrandAndModel();
            Vehicle v = new Vehicle();
            v.setId(selectedId);
            v.setCustomerId(owner.getId());
            v.setVehicleNumber(vehicleNumberField.getText().trim().toUpperCase());
            v.setVehicleType((VehicleType) typeCombo.getSelectedItem());
            v.setModel(model);
            vehicleDAO.update(v);
            clearForm();
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot save vehicle", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    /**
     * Resolves the Brand/Model selection into a "Brand Model" string, creating and
     * persisting any newly typed "Other" brand/model so they appear in the dropdowns
     * next time. Throws IllegalStateException for anything the user needs to fix.
     */
    private String resolveBrandAndModel() throws SQLException {
        Object brandSel = brandCombo.getSelectedItem();
        if (!(brandSel instanceof Brand brand)) {
            throw new IllegalStateException("Pick a brand from the list, or choose \"Other\".");
        }

        if (brand.isOther()) {
            String newBrandName = otherBrandField.getText().trim();
            String newModelName = otherModelField.getText().trim();
            if (newBrandName.isEmpty() || newModelName.isEmpty()) {
                throw new IllegalStateException("Enter both the new brand name and the model name.");
            }
            if (!Validators.isValidBrandName(newBrandName)) {
                throw new IllegalStateException(
                        "Brand name must start with a capital letter, e.g. Maruti Suzuki.");
            }
            if (!Validators.isValidModelName(newModelName)) {
                throw new IllegalStateException(
                        "Model name must contain only letters, numbers, spaces, and hyphens, e.g. 911.");
            }
            Brand createdBrand = brandDAO.findOrCreate(newBrandName, selectedType());
            VehicleModel createdModel = modelDAO.findOrCreate(createdBrand.getId(), newModelName);
            return createdBrand.getName() + " " + createdModel.getName();
        }

        Object modelSel = modelCombo.getSelectedItem();
        if (!(modelSel instanceof VehicleModel model)) {
            throw new IllegalStateException("Pick a model from the list, or choose \"Other\".");
        }

        if (model.isOther()) {
            String newModelName = otherModelField.getText().trim();
            if (newModelName.isEmpty()) {
                throw new IllegalStateException("Enter the new model name.");
            }
            if (!Validators.isValidModelName(newModelName)) {
                throw new IllegalStateException(
                        "Model name must contain only letters, numbers, spaces, and hyphens, e.g. 911.");
            }
            VehicleModel createdModel = modelDAO.findOrCreate(brand.getId(), newModelName);
            return brand.getName() + " " + createdModel.getName();
        }

        return brand.getName() + " " + model.getName();
    }

    private void deleteVehicle() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a vehicle from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this vehicle?", "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            vehicleDAO.delete(selectedId);
            clearForm();
            refresh();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot delete vehicle",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private boolean validateForm() {
        if (vehicleNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vehicle number is required.",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!Validators.isValidVehicleNumber(vehicleNumberField.getText().trim().toUpperCase())) {
            JOptionPane.showMessageDialog(this,
                    "Vehicle number must be 2 letters, 2 digits, 1-2 letters, then 4 digits, e.g. KA19EF9001.",
                    "Invalid vehicle number", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearForm() {
        selectedId = null;
        table.clearSelection();
        vehicleNumberField.setText("");
        typeCombo.setSelectedIndex(0);
        otherBrandField.setText("");
        otherModelField.setText("");
        loadBrandsForType(selectedType());
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
