/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.pharmacy;

import com.divudi.entity.BillItem;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;


/**
 *
 * @author Buddhika
 */
@Entity
public class PharmaceuticalBillItem implements Serializable {

    @OneToOne(mappedBy = "pbItem")
    private StockHistory stockHistory;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    BillItem billItem;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date doe;
    @ManyToOne
    ItemBatch itemBatch;
    private String stringValue;
    double qty;
    double freeQty;
    double purchaseRate;
    private double lastPurchaseRate;
    double retailRate;
    double wholesaleRate;
    @ManyToOne
    Stock stock;
    @ManyToOne
    private Stock staffStock;

    public void copy(PharmaceuticalBillItem ph) {
        qty = ph.qty;
        freeQty = ph.freeQty;
        purchaseRate = ph.purchaseRate;        
        doe = ph.getDoe();
        stringValue = ph.getStringValue();
        itemBatch = ph.getItemBatch();
        retailRate = ph.getRetailRate();
        stock = ph.getStock();
        staffStock=ph.getStaffStock();
        stringValue = ph.getStringValue();
        //  remainingQty=ph.getRemainingQty();

    }
    
    public void invertValue(PharmaceuticalBillItem ph){
        qty=0-ph.qty;
       // ////System.err.println("QTY "+qty);
        freeQty=0-ph.freeQty;
      //  purchaseRate=0-ph.purchaseRate;
      //  lastPurchaseRate=0-ph.lastPurchaseRate;
       // retailRate=0-ph.retailRate;
      //  wholesaleRate=0-ph.wholesaleRate;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public StockHistory getStockHistory() {
        return stockHistory;
    }

    public void setStockHistory(StockHistory stockHistory) {
        this.stockHistory = stockHistory;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BillItem getBillItem() {
        return billItem;
    }

    public void setBillItem(BillItem billItem) {
        this.billItem = billItem;
    }

    public Date getDoe() {
        return doe;
    }

    public void setDoe(Date doe) {
        this.doe = doe;
    }

    public ItemBatch getItemBatch() {
        return itemBatch;
    }

    public void setItemBatch(ItemBatch itemBatch) {
        this.itemBatch = itemBatch;
    }

    public double getQty() {
        if (getBillItem() != null && getBillItem().getItem() instanceof Ampp
                || getBillItem() != null && getBillItem().getItem() instanceof Vmpp) {
            return qty / getBillItem().getItem().getDblValue();
        } else {
            return qty;
        }

    }

    public void setQty(double qty) {
        if (getBillItem() != null && getBillItem().getItem() instanceof Ampp
                || getBillItem() != null && getBillItem().getItem() instanceof Vmpp) {
            this.qty = qty * getBillItem().getItem().getDblValue();
        } else {
            this.qty = qty;
        }
    }

    public double getQtyInUnit() {
        return qty;
    }

    public void setQtyInUnit(double qty) {
        this.qty = qty;
    }

    public double getFreeQtyInUnit() {
        return freeQty;
    }

    public void setFreeQtyInUnit(double freeQty) {
        this.freeQty = freeQty;
    }

    public double getFreeQty() {
        if (getBillItem() != null && getBillItem().getItem() instanceof Ampp
                || getBillItem() != null && getBillItem().getItem() instanceof Vmpp) {
            return freeQty / getBillItem().getItem().getDblValue();
        } else {
            return freeQty;
        }
    }

    public void setFreeQty(double freeQty) {
        if (getBillItem() != null && getBillItem().getItem() instanceof Ampp
                || getBillItem() != null && getBillItem().getItem() instanceof Vmpp) {
            this.freeQty = freeQty * getBillItem().getItem().getDblValue();
        } else {
            this.freeQty = freeQty;
        }
    }

    public double getPurchaseRate() {
        if (getBillItem() != null && getBillItem().getItem() instanceof Ampp
                || getBillItem() != null && getBillItem().getItem() instanceof Vmpp) {
            return purchaseRate * getBillItem().getItem().getDblValue();
        } else {
            return purchaseRate;
        }

    }

    public double getPurchaseRateInUnit() {
        return purchaseRate;
    }

    public void setPurchaseRateInUnit(double purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

    public void setPurchaseRate(double purchaseRate) {
        if (getBillItem() != null && getBillItem().getItem() instanceof Ampp
                || getBillItem() != null && getBillItem().getItem() instanceof Vmpp) {
            this.purchaseRate = purchaseRate / getBillItem().getItem().getDblValue();
        } else {
            this.purchaseRate = purchaseRate;
        }
    }

    public double getRetailRateInUnit() {
        return retailRate;
    }

    public void setRetailRateInUnit(double retailRate) {
        this.retailRate = retailRate;
    }

    public double getRetailRate() {
        if (getBillItem() != null && getBillItem().getItem() instanceof Ampp
                || getBillItem() != null && getBillItem().getItem() instanceof Vmpp) {
            return retailRate * getBillItem().getItem().getDblValue();
        } else {
            return retailRate;
        }
    }

    public void setRetailRate(double retailRate) {
        if (getBillItem() != null && getBillItem().getItem() instanceof Ampp
                || getBillItem() != null && getBillItem().getItem() instanceof Vmpp) {
            this.retailRate = retailRate / getBillItem().getItem().getDblValue();
        } else {
            this.retailRate = retailRate;
        }
    }

    public double getWholesaleRate() {
        return wholesaleRate;
    }

    public void setWholesaleRate(double wholesaleRate) {
        this.wholesaleRate = wholesaleRate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof PharmaceuticalBillItem)) {
            return false;
        }
        PharmaceuticalBillItem other = (PharmaceuticalBillItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.pharmacy.PharmaceuticalBillItem[ id=" + id + " ]";
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public double getLastPurchaseRateInUnit() {

        return lastPurchaseRate;

    }

    public void setLastPurchaseRateInUnit(double lastPurchaseRate) {

        this.lastPurchaseRate = lastPurchaseRate;

    }

    public double getLastPurchaseRate() {
        if (getBillItem() != null && getBillItem().getItem() instanceof Ampp
                || getBillItem() != null && getBillItem().getItem() instanceof Vmpp) {
            return lastPurchaseRate * getBillItem().getItem().getDblValue();
        } else {
            return lastPurchaseRate;
        }
    }

    public void setLastPurchaseRate(double lastPurchaseRate) {
        if (getBillItem() != null && getBillItem().getItem() instanceof Ampp
                || getBillItem() != null && getBillItem().getItem() instanceof Vmpp) {
            this.lastPurchaseRate = lastPurchaseRate / getBillItem().getItem().getDblValue();
        } else {
            this.lastPurchaseRate = lastPurchaseRate;
        }

    }

    public Stock getStaffStock() {
        return staffStock;
    }

    public void setStaffStock(Stock staffStock) {
        this.staffStock = staffStock;
    }

}
