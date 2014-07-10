/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data.dataStructure;

/**
 *
 * @author pdhs
 */
public class PharmacyStockRow {

    String code;
    String name;
    Double qty;
    Double purchaseValue;
    Double saleValue;

    public PharmacyStockRow() {
    }

    public PharmacyStockRow(String name, Double qty, Double purchaseValue, Double saleValue) {
        this.name = name;
        this.qty = qty;
        this.purchaseValue = purchaseValue;
        this.saleValue = saleValue;
    }

    
    
    public PharmacyStockRow(String code, String name, Double qty, Double purchaseValue, Double saleValue) {
        this.code = code;
        this.name = name;
        this.qty = qty;
        this.purchaseValue = purchaseValue;
        this.saleValue = saleValue;
    }

    
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public Double getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(Double purchaseValue) {
        this.purchaseValue = purchaseValue;
    }

    public Double getSaleValue() {
        return saleValue;
    }

    public void setSaleValue(Double saleValue) {
        this.saleValue = saleValue;
    }
}
