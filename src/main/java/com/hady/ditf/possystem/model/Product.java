package com.hady.ditf.possystem.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Product {
    private final SimpleStringProperty serialNo;
    private final SimpleDoubleProperty mrp;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty total;
    private final SimpleStringProperty productName;

    public Product(String serialNo, double mrp, int quantity, String productName) {
        this.serialNo = new SimpleStringProperty(serialNo);
        this.mrp = new SimpleDoubleProperty(mrp);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.total = new SimpleDoubleProperty(mrp * quantity);
        this.productName = new SimpleStringProperty(productName);
    }

    // Getters
    public String getSerialNo() { return serialNo.get(); }
    public double getMrp() { return mrp.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getTotal() { return total.get(); }

    public String getProductName() {
        return productName.get();
    }

    public SimpleStringProperty productNameProperty() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    // Setters
    public void setSerialNo(String serialNo) { this.serialNo.set(serialNo); }
    public void setMrp(double mrp) { this.mrp.set(mrp); }
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
        this.total.set(this.mrp.get() * quantity); // Update total when quantity changes
    }
    public void setTotal(double total) { this.total.set(total); }

    // Property getters (required for PropertyValueFactory to work)
    public SimpleStringProperty serialNoProperty() { return serialNo; }
    public SimpleDoubleProperty mrpProperty() { return mrp; }
    public SimpleIntegerProperty quantityProperty() { return quantity; }
    public SimpleDoubleProperty totalProperty() { return total; }
    public SimpleStringProperty productName() {
        return productName;
    }
}
