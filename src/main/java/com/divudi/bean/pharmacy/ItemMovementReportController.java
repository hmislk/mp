/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.divudi.bean.pharmacy;

import com.divudi.ejb.ItemMovementReportEjb;
import com.divudi.entity.BillItem;
import com.divudi.entity.Item;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;

/**
 *
 * @author ruhunu
 */
@Named(value = "itemMovementReportController")
@SessionScoped
public class ItemMovementReportController implements Serializable {
@EJB
        ItemMovementReportEjb ejb;
        
    List<BillItem> billItems;
    Date fromDate;
    Date toDate;
    Item item;
    

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }
    
    public void listPharmacyMovement(){
        billItems = getEjb().allBillItems(item,fromDate, toDate);
    }
    
    /**
     * Creates a new instance of ItemMovementReportController
     */
    public ItemMovementReportController() {
    }

    public ItemMovementReportEjb getEjb() {
        return ejb;
    }

    public void setEjb(ItemMovementReportEjb ejb) {
        this.ejb = ejb;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
    
    
    
    
}
