package com.parking.system.model;

public class Vehicle {
    private int id;
    private int customerId;
    private String customerName; // convenience field, populated by joins
    private String vehicleNumber;
    private VehicleType vehicleType;
    private String model;

    public Vehicle() {
    }

    public Vehicle(int id, int customerId, String customerName, String vehicleNumber,
                    VehicleType vehicleType, String model) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.model = model;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    @Override
    public String toString() {
        return vehicleNumber + " - " + model;
    }
}
