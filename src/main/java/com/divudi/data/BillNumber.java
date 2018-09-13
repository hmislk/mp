/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data;

import com.divudi.entity.Bill;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;

/**
 *
 * @author buddhika_ari
 */
public class BillNumber {
    private Department department;
    private Institution institution;
    private BillType billType;
    private Class<?> billClass;
    private Long lastNumber;

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public BillType getBillType() {
        return billType;
    }

    public void setBillType(BillType billType) {
        this.billType = billType;
    }

    public Class<?> getBillClass() {
        return billClass;
    }

    public void setBillClass(Class<?> billClass) {
        this.billClass = billClass;
    }

    public Long getLastNumber() {
        return lastNumber;
    }

    public void setLastNumber(Long lastNumber) {
        this.lastNumber = lastNumber;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }
    
    
    
}
