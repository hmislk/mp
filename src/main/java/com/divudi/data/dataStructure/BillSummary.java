/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data.dataStructure;

import com.divudi.entity.Item;
import java.util.Date;

/**
 *
 * @author buddhika
 */
public class BillSummary implements Comparable<BillSummary> {

    
    private Long id;
    private String billNo;
    private String departmentName;
    private String billedBy;
    private Date billedAt;
    private Boolean cancelled;
    private Boolean refunded;
    private Double grossValue;
    private Double discount;
    private Double netValue;

    public BillSummary(Long id, String billNo, 
            String departmentName, 
            String billedBy, 
            Date billedAt, 
            Boolean cancelled, 
            Boolean refunded, 
            Double grossValue, Double discount, Double netValue) {
        this.id = id;
        this.billNo = billNo;
        this.departmentName = departmentName;
        this.billedBy = billedBy;
        this.billedAt = billedAt;
        this.cancelled = cancelled;
        this.refunded = refunded;
        this.grossValue = grossValue;
        this.discount = discount;
        this.netValue = netValue;
    }
    
    
    
   
    

    public BillSummary() {
    }

    

    @Override
    public int compareTo(BillSummary t) {
        if (id == null) {
            return 0;
        }
        if (t == null || t.id == null) {
            return 1;
        }
        return getId().compareTo(t.getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getBilledBy() {
        return billedBy;
    }

    public void setBilledBy(String billedBy) {
        this.billedBy = billedBy;
    }

    public Date getBilledAt() {
        return billedAt;
    }

    public void setBilledAt(Date billedAt) {
        this.billedAt = billedAt;
    }

    public Boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Boolean getRefunded() {
        return refunded;
    }

    public void setRefunded(Boolean refunded) {
        this.refunded = refunded;
    }

    public Double getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(Double grossValue) {
        this.grossValue = grossValue;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getNetValue() {
        return netValue;
    }

    public void setNetValue(Double netValue) {
        this.netValue = netValue;
    }

}
