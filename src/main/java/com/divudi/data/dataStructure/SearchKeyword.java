/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data.dataStructure;

/**
 *
 * @author safrin
 */
public class SearchKeyword {

    private String billNo;
    private String refBillNo;
    private String patientName;
    private String patientPhone;
    private String total;
    private String netTotal;
    private String itemName;
    private String speciality;
    private String staffName;
    private String personName;
    private String fromInstitution;
    private String toInstitution;
    private String creator;
    private String bank;
    private String number;
    private String department;

    public boolean checkKeyword() {
        if (billNo != null && !billNo.trim().equals("")) {
            return true;
        }

        if (patientName != null && !patientName.trim().equals("")) {
            return true;
        }

        if (patientPhone != null && !patientPhone.trim().equals("")) {
            return true;
        }

        if (total != null && !total.trim().equals("")) {
            return true;
        }

        if (netTotal != null && !netTotal.trim().equals("")) {
            return true;
        }

        return false;

    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(String netTotal) {
        this.netTotal = netTotal;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getFromInstitution() {
        return fromInstitution;
    }

    public void setFromInstitution(String fromInstitution) {
        this.fromInstitution = fromInstitution;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getRefBillNo() {
        return refBillNo;
    }

    public void setRefBillNo(String refBillNo) {
        this.refBillNo = refBillNo;
    }

    public String getToInstitution() {
        return toInstitution;
    }

    public void setToInstitution(String toInstitution) {
        this.toInstitution = toInstitution;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
