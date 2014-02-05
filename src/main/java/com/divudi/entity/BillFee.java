/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity;

import com.divudi.data.FeeType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author www.divudi.com
 */
@Entity
public class BillFee implements Serializable {

    static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
     Long id;
    //Created Properties
    @ManyToOne
     WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     Date createdAt;
    //Retairing properties
     boolean retired;
    @ManyToOne
     WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     Date retiredAt;
     String retireComments;
    @ManyToOne
     Fee fee;
    @ManyToOne
     Patient patient;
    @ManyToOne
     PatientEncounter patienEncounter;
    @ManyToOne
     PatientEncounter childEncounter;
    @ManyToOne
     BillItem billItem;
    @ManyToOne
     Bill bill;
     double feeValue = 0.0;
     double feeGrossValue;
     double feeDiscount;
     double feeMargin;
    @ManyToOne
     Staff staff;
    @ManyToOne
     Institution institution;
    @ManyToOne
     Department department;
    @ManyToOne
     Speciality speciality;
     double paidValue = 0.0;
    //FeeDate, FeeTime
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     Date FeeAt;

    public BillFee() {
    }

  

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        
        if (!(object instanceof BillFee)) {
            return false;
        }
        BillFee other = (BillFee) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.BillFee[ id=" + id + " ]";
    }

  

    public void setFeeValue(boolean foriegn) {
        if (foriegn) {
            feeValue = getFee().getFfee();
        } else {
            feeValue = getFee().getFee();
        }
        //    //System.out.println("Setting fee value as " + feeValue);
    }

    public void setFeeValue(boolean foriegn, double discountPercent) {

        if (getFee().getFeeType() != FeeType.Staff) {
            if (foriegn) {
                this.feeValue = getFee().getFfee() / 100 * (100 - discountPercent);
            } else {
                this.feeValue = getFee().getFee() / 100 * (100 - discountPercent);
            }
        } else {
            if (foriegn) {
                this.feeValue = getFee().getFfee();
            } else {
                this.feeValue = getFee().getFee();
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Fee getFee() {
        return fee;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public PatientEncounter getPatienEncounter() {
        return patienEncounter;
    }

    public void setPatienEncounter(PatientEncounter patienEncounter) {
        this.patienEncounter = patienEncounter;
    }

    public PatientEncounter getChildEncounter() {
        return childEncounter;
    }

    public void setChildEncounter(PatientEncounter childEncounter) {
        this.childEncounter = childEncounter;
    }

    public BillItem getBillItem() {
        return billItem;
    }

    public void setBillItem(BillItem billItem) {
        this.billItem = billItem;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public double getFeeValue() {
        return feeValue;
    }

    public void setFeeValue(double feeValue) {
        this.feeValue = feeValue;
    }

    public double getFeeGrossValue() {
        return feeGrossValue;
    }

    public void setFeeGrossValue(double feeGrossValue) {
        this.feeGrossValue = feeGrossValue;
    }

    public double getFeeDiscount() {
        return feeDiscount;
    }

    public void setFeeDiscount(double feeDiscount) {
        this.feeDiscount = feeDiscount;
    }

    public double getFeeMargin() {
        return feeMargin;
    }

    public void setFeeMargin(double feeMargin) {
        this.feeMargin = feeMargin;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    public double getPaidValue() {
        return paidValue;
    }

    public void setPaidValue(double paidValue) {
        this.paidValue = paidValue;
    }

    public Date getFeeAt() {
        return FeeAt;
    }

    public void setFeeAt(Date FeeAt) {
        this.FeeAt = FeeAt;
    }


}
