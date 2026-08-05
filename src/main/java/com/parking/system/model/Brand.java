package com.parking.system.model;

public class Brand {

    /** Sentinel id used for the "Other" entry appended to the brand dropdown. */
    public static final int OTHER_ID = -1;
    public static final Brand OTHER = new Brand(OTHER_ID, "Other", null);

    private final int id;
    private final String name;
    private final VehicleType vehicleType;

    public Brand(int id, String name, VehicleType vehicleType) {
        this.id = id;
        this.name = name;
        this.vehicleType = vehicleType;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public boolean isOther() {
        return id == OTHER_ID;
    }

    @Override
    public String toString() {
        return name;
    }
}
