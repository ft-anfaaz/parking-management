package com.parking.system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    public enum Mode { CASH, CARD, UPI }

    private int id;
    private int bookingId;
    private String vehicleNumber; // convenience, populated by joins
    private int hoursCharged;
    private BigDecimal ratePerHour;
    private BigDecimal amount;
    private Mode paymentMode;
    private LocalDateTime paymentTime;

    public Payment() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public int getHoursCharged() { return hoursCharged; }
    public void setHoursCharged(int hoursCharged) { this.hoursCharged = hoursCharged; }

    public BigDecimal getRatePerHour() { return ratePerHour; }
    public void setRatePerHour(BigDecimal ratePerHour) { this.ratePerHour = ratePerHour; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Mode getPaymentMode() { return paymentMode; }
    public void setPaymentMode(Mode paymentMode) { this.paymentMode = paymentMode; }

    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
}
