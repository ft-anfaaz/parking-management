package com.parking.system.model;

/** A make/model entry (e.g. "Swift" under brand "Maruti Suzuki"), used to populate the Vehicles screen dropdowns. */
public class VehicleModel {

    /** Sentinel id used for the "Other" entry appended to the model dropdown. */
    public static final int OTHER_ID = -1;
    public static final VehicleModel OTHER = new VehicleModel(OTHER_ID, OTHER_ID, "Other");

    private final int id;
    private final int brandId;
    private final String name;

    public VehicleModel(int id, int brandId, String name) {
        this.id = id;
        this.brandId = brandId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getBrandId() {
        return brandId;
    }

    public String getName() {
        return name;
    }

    public boolean isOther() {
        return id == OTHER_ID;
    }

    @Override
    public String toString() {
        return name;
    }
}
