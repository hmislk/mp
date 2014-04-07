/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity;

import com.divudi.data.InOutType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author safrin
 */
@Entity
public class CashTransaction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private InOutType inOutType;
    @ManyToOne
    private Bill bill;
    @ManyToOne
    private CashierDrawer cashierDrawer;
    /////////////
    private double tenderedAmount;
    private double ballanceAmount;
    /////////////////
    private double qty1;
    private double qty2;
    private double qty5;
    private double qty10;
    private double qty20;
    private double qty50;
    private double qty100;
    private double qty200;
    private double qty500;
    private double qty1000;
    private double qty2000;
    private double qty5000;
    //Created Properties
    @ManyToOne
    private WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    //Retairing properties
    private boolean retired;
    @ManyToOne
    private WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

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
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CashTransaction)) {
            return false;
        }
        CashTransaction other = (CashTransaction) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.CashTransaction[ id=" + id + " ]";
    }

    public double getTenderedAmount() {
        return tenderedAmount;
    }

    public void setTenderedAmount(double tenderedAmount) {
        this.tenderedAmount = tenderedAmount;
    }

    public double getBallanceAmount() {
        return ballanceAmount;
    }

    public void setBallanceAmount(double ballanceAmount) {
        this.ballanceAmount = ballanceAmount;
    }

    public double getQty1() {
        return qty1;
    }

    public void setQty1(double qty1) {
        this.qty1 = qty1;
    }

    public double getQty2() {
        return qty2;
    }

    public void setQty2(double qty2) {
        this.qty2 = qty2;
    }

    public double getQty5() {
        return qty5;
    }

    public void setQty5(double qty5) {
        this.qty5 = qty5;
    }

    public double getQty10() {
        return qty10;
    }

    public void setQty10(double qty10) {
        this.qty10 = qty10;
    }

    public double getQty20() {
        return qty20;
    }

    public void setQty20(double qty20) {
        this.qty20 = qty20;
    }

    public double getQty50() {
        return qty50;
    }

    public void setQty50(double qty50) {
        this.qty50 = qty50;
    }

    public double getQty100() {
        return qty100;
    }

    public void setQty100(double qty100) {
        this.qty100 = qty100;
    }

    public double getQty200() {
        return qty200;
    }

    public void setQty200(double qty200) {
        this.qty200 = qty200;
    }

    public double getQty500() {
        return qty500;
    }

    public void setQty500(double qty500) {
        this.qty500 = qty500;
    }

    public double getQty1000() {
        return qty1000;
    }

    public void setQty1000(double qty1000) {
        this.qty1000 = qty1000;
    }

    public double getQty2000() {
        return qty2000;
    }

    public void setQty2000(double qty2000) {
        this.qty2000 = qty2000;
    }

    public double getQty5000() {
        return qty5000;
    }

    public void setQty5000(double qty5000) {
        this.qty5000 = qty5000;
    }

    public InOutType getInOutType() {
        return inOutType;
    }

    public void setInOutType(InOutType inOutType) {
        this.inOutType = inOutType;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public CashierDrawer getCashierDrawer() {
        return cashierDrawer;
    }

    public void setCashierDrawer(CashierDrawer cashierDrawer) {
        this.cashierDrawer = cashierDrawer;
    }

}
