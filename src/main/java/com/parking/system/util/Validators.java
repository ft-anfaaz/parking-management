package com.parking.system.util;

import java.util.regex.Pattern;

/** Format rules for user-entered fields, shared by the Customers and Vehicles screens. */
public final class Validators {

    public static final Pattern NAME = Pattern.compile("^[A-Z][a-zA-Z]*(\\s[a-zA-Z]+)*$");
    public static final Pattern PHONE = Pattern.compile("^[0-9]{10}$");
    public static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");
    public static final Pattern VEHICLE_NUMBER = Pattern.compile("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,2}[0-9]{4}$");
    public static final Pattern ADDRESS = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9,./#\\-\\s]{9,149}$");
    public static final Pattern LICENSE_NO = Pattern.compile("^[A-Z]{2}[0-9]{2}\\s[0-9]{4}\\s[0-9]{7}$");
    // Brand names always start with a letter (Maruti Suzuki, Honda, ...).
    public static final Pattern BRAND_NAME = Pattern.compile("^[A-Z][A-Za-z0-9-]*(\\s[A-Za-z0-9-]+)*$");
    // Model names may start with a digit too (e.g. Porsche's "911").
    public static final Pattern MODEL_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9-]*(\\s[A-Za-z0-9-]+)*$");
    public static final Pattern USERNAME = Pattern.compile("^[a-z][a-z0-9_]{2,19}$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    private Validators() {
    }

    public static boolean isValidName(String value) {
        return value != null && NAME.matcher(value.trim()).matches();
    }

    public static boolean isValidPhone(String value) {
        return value != null && PHONE.matcher(value.trim()).matches();
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL.matcher(value.trim()).matches();
    }

    public static boolean isValidVehicleNumber(String value) {
        return value != null && VEHICLE_NUMBER.matcher(value.trim()).matches();
    }

    public static boolean isValidAddress(String value) {
        return value != null && ADDRESS.matcher(value.trim()).matches();
    }

    public static boolean isValidLicenseNo(String value) {
        return value != null && LICENSE_NO.matcher(value.trim()).matches();
    }

    public static boolean isValidBrandName(String value) {
        return value != null && BRAND_NAME.matcher(value.trim()).matches();
    }

    public static boolean isValidModelName(String value) {
        return value != null && MODEL_NAME.matcher(value.trim()).matches();
    }

    public static boolean isValidUsername(String value) {
        return value != null && USERNAME.matcher(value.trim()).matches();
    }

    public static boolean isValidPassword(String value) {
        return value != null && value.length() >= MIN_PASSWORD_LENGTH;
    }
}
