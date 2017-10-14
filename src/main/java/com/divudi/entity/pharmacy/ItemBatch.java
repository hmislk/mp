/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.pharmacy;

import com.divudi.entity.Item;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Entity
public class ItemBatch implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateOfManufacture;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateOfExpire;
    private String batchNo;
    @ManyToOne
    private Item item;
    double purcahseRate;
    double retailsaleRate;
    double retailMargin;
    double wholesaleRate;
    double wholesaleFreeFor;
    double wholesaleFreeQty;
    double wholeSaleMargin;

    public double getRetailMargin() {
        return retailMargin;
    }

    public void setRetailMargin(double retailMargin) {
        this.retailMargin = retailMargin;
    }

    public double getWholeSaleMargin() {
        return wholeSaleMargin;
    }

    public void setWholeSaleMargin(double wholeSaleMargin) {
        this.wholeSaleMargin = wholeSaleMargin;
    }
    
    
    

    public double getWholesaleFreeFor() {
        return wholesaleFreeFor;
    }

    public void setWholesaleFreeFor(double wholesaleFreeFor) {
        this.wholesaleFreeFor = wholesaleFreeFor;
    }

    public double getWholesaleFreeQty() {
        return wholesaleFreeQty;
    }

    public void setWholesaleFreeQty(double wholesaleFreeQty) {
        this.wholesaleFreeQty = wholesaleFreeQty;
    }

    public double getPurcahseRate() {
        return purcahseRate;
    }

    public void setPurcahseRate(double purcahseRate) {
        this.purcahseRate = purcahseRate;
    }

    public double getRetailsaleRate() {
        return retailsaleRate;
    }

    public void setRetailsaleRate(double retailsaleRate) {
        this.retailsaleRate = retailsaleRate;
    }

    public double getWholesaleRate() {
        return wholesaleRate;
    }

    public void setWholesaleRate(double wholesaleRate) {
        this.wholesaleRate = wholesaleRate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof ItemBatch)) {
            return false;
        }
        ItemBatch other = (ItemBatch) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.pharmacy.ItemBatch[ id=" + id + " ]";
    }

    public Date getDateOfManufacture() {
        return dateOfManufacture;
    }

    public void setDateOfManufacture(Date dateOfManufacture) {
        this.dateOfManufacture = dateOfManufacture;
    }

    public Date getDateOfExpire() {
        return dateOfExpire;
    }

    public void setDateOfExpire(Date dateOfExpire) {
        this.dateOfExpire = dateOfExpire;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
