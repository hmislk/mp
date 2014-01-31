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
    private String patientName;
    private String patientPhone;
    private String total;
    private String netTotal;
    private String itemName;

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
}
