package com.parking.system.model;

public class ParkingSlot {
    public enum Status { AVAILABLE, OCCUPIED }

    private int id;
    private String slotNumber;
    private int floor;
    private VehicleType slotType;
    private Status status;

    public ParkingSlot() {
    }

    public ParkingSlot(int id, String slotNumber, int floor, VehicleType slotType, Status status) {
        this.id = id;
        this.slotNumber = slotNumber;
        this.floor = floor;
        this.slotType = slotType;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public VehicleType getSlotType() { return slotType; }
    public void setSlotType(VehicleType slotType) { this.slotType = slotType; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
