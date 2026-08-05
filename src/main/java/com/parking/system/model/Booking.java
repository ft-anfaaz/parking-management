package com.parking.system.model;

import java.time.LocalDateTime;

public class Booking {
    public enum Status { ACTIVE, COMPLETED }

    private int id;
    private int vehicleId;
    private String vehicleNumber;  // convenience, populated by joins
    private VehicleType vehicleType; // convenience, populated by joins
    private String customerName;   // convenience, populated by joins
    private String customerPhone;  // convenience, populated by joins
    private int slotId;
    private String slotNumber;     // convenience, populated by joins
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Status status;

    public Booking() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
