package com.parking.system.model;

public enum VehicleType {
    TWO_WHEELER("Two Wheeler"),
    FOUR_WHEELER("Four Wheeler"),
    HEAVY("Heavy Vehicle");

    private final String label;

    VehicleType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
