package com.parking.system.model;

public class Customer {
    private int id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String licenseNo;

    public Customer() {
    }

    public Customer(int id, String fullName, String phone, String email, String address, String licenseNo) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.licenseNo = licenseNo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }

    @Override
    public String toString() {
        return fullName + " (" + phone + ")";
    }
}
