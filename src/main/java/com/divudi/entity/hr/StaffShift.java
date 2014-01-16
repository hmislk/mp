/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.hr;

import com.divudi.bean.SessionController;
import com.divudi.data.hr.FingerPrintRecordType;
import com.divudi.data.hr.WorkingType;
import com.divudi.entity.Staff;
import com.divudi.entity.WebUser;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 *
 * @author safrin
 */
@Entity
public class StaffShift implements Serializable {

    @ManyToOne
    private DayShift dayShift;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne
    private Staff staff;
    @Temporal(TemporalType.DATE)
    private Date shiftDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date shiftStartTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date shiftEndTime;
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
    private int repeatedCount;
    @Enumerated(EnumType.STRING)
    private WorkingType workingType;
    private boolean finalized;

    @OneToOne
    FingerPrintRecord startRecord;
    @OneToOne
    FingerPrintRecord endRecord;

    @OneToOne
    StaffShift continuedFrom;
    @OneToOne
    StaffShift continuedTo;

    private boolean dayOff;
    private boolean sleepingDay;

    public FingerPrintRecord getStartRecord() {       
        return startRecord;
    }

    public void setStartRecord(FingerPrintRecord startRecord) {
        this.startRecord = startRecord;
    }

    public FingerPrintRecord getEndRecord() {      
        return endRecord;
    }

    public void setEndRecord(FingerPrintRecord endRecord) {
        this.endRecord = endRecord;
    }

    public StaffShift getContinuedFrom() {
        return continuedFrom;
    }

    public void setContinuedFrom(StaffShift continuedFrom) {
        this.continuedFrom = continuedFrom;
        if (getStaff() == null) {
            this.continuedFrom = null;
        }
    }

    public StaffShift getContinuedTo() {
        return continuedTo;
    }

    public void setContinuedTo(StaffShift continuedTo) {
        this.continuedTo = continuedTo;
        if (getStaff() == null) {
            this.continuedTo = null;
        }
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
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof StaffShift)) {
            return false;
        }
        StaffShift other = (StaffShift) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.hr.StaffShift[ id=" + id + " ]";
    }

    public Date getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(Date shiftDate) {
        this.shiftDate = shiftDate;

        Calendar sTime = Calendar.getInstance();
        Calendar sDate = Calendar.getInstance();
        sTime.setTime(getDayShift().getStartingTime());
        sDate.setTime(shiftDate);
        sDate.set(Calendar.HOUR_OF_DAY, sTime.get(Calendar.HOUR_OF_DAY));
        sDate.set(Calendar.MINUTE, sTime.get(Calendar.MINUTE));
        setShiftStartTime(sDate.getTime());

        Calendar eDate = Calendar.getInstance();
        eDate.setTime(getShiftStartTime());
        eDate.add(Calendar.HOUR_OF_DAY, getDayShift().getDurationHour());
        //  eDate.set(Calendar.MINUTE,  0);

        setShiftEndTime(eDate.getTime());

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

    public DayShift getDayShift() {
        return dayShift;
    }

    public void setDayShift(DayShift dayShift) {
        this.dayShift = dayShift;
    }

    public int getRepeatedCount() {
        return repeatedCount;
    }

    public void setRepeatedCount(int repeatedCount) {
        this.repeatedCount = repeatedCount;
    }

    public WorkingType getWorkingType() {
        return workingType;
    }

    public void setWorkingType(WorkingType workingType) {
        this.workingType = workingType;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
        if (this.staff == null) {
            continuedTo = null;
            continuedFrom = null;
        }
    }

    public boolean isFinalized() {
        return finalized;
    }

    public boolean getFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public Date getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(Date shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public Date getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(Date shiftEndTime) {
        //  System.err.println("setting End Time");
        if (getId() == null) {
            this.shiftEndTime = shiftEndTime;
        }
    }

    public boolean isDayOff() {
        return dayOff;
    }

    public void setDayOff(boolean dayOff) {
        this.dayOff = dayOff;
    }

    public boolean isSleepingDay() {
        return sleepingDay;
    }

    public void setSleepingDay(boolean sleepingDay) {
        this.sleepingDay = sleepingDay;
    }
    
    @Transient
    private List<FingerPrintRecord> fingerPrintRecordList;

    public List<FingerPrintRecord> getFingerPrintRecordList() {
        return fingerPrintRecordList;
    }

    public void setFingerPrintRecordList(List<FingerPrintRecord> fingerPrintRecordList) {
        this.fingerPrintRecordList = fingerPrintRecordList;
    }

}
