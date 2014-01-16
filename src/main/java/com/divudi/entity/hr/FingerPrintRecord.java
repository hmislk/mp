/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.hr;

import com.divudi.data.hr.FingerPrintRecordType;
import com.divudi.data.hr.Times;
import com.divudi.entity.Staff;
import com.divudi.entity.WebUser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;

/**
 *
 * @author Buddhika
 */
@Entity
public class FingerPrintRecord implements Serializable {

    @OneToOne(mappedBy = "loggedRecord")
    private FingerPrintRecord verifiedRecord;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    FingerPrintRecordType fingerPrintRecordType;
    @OneToOne
    FingerPrintRecord loggedRecord;
    @ManyToOne
    Staff staff;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date recordTimeStamp;

    @OneToOne
    private StaffShift staffShift;

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
    //private Boolean begining;
    @Enumerated(EnumType.STRING)
    private Times times;
    

    public FingerPrintRecord getVerifiedRecord() {
        return verifiedRecord;
    }

    public void setVerifiedRecord(FingerPrintRecord verifiedRecord) {
        this.verifiedRecord = verifiedRecord;
    }

    public FingerPrintRecordType getFingerPrintRecordType() {
        return fingerPrintRecordType;
    }

    public void setFingerPrintRecordType(FingerPrintRecordType fingerPrintRecordType) {
        this.fingerPrintRecordType = fingerPrintRecordType;
    }

    public FingerPrintRecord getLoggedRecord() {
        return loggedRecord;
    }

    public void setLoggedRecord(FingerPrintRecord loggedRecord) {
        this.loggedRecord = loggedRecord;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Date getRecordTimeStamp() {
        return recordTimeStamp;
    }

    public void setRecordTimeStamp(Date recordTimeStamp) {
        this.recordTimeStamp = recordTimeStamp;
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
        
        if (!(object instanceof FingerPrintRecord)) {
            return false;
        }
        FingerPrintRecord other = (FingerPrintRecord) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.hr.FingerPrintRecord[ id=" + id + " ]";
    }

    public StaffShift getStaffShift() {
        return staffShift;
    }

    public void setStaffShift(StaffShift staffShift) {
        this.staffShift = staffShift;
    }

   
    public Times getTimes() {
        return times;
    }

    public void setTimes(Times times) {
        this.times = times;
    }
 

}
