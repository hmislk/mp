/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.hr;

import com.divudi.data.hr.DayType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.Transient;

/**
 *
 * @author safrin
 */
@Entity
public class DayShift implements Serializable {

    @OneToMany(mappedBy = "dayShift", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StaffShift> staffShifts = new ArrayList<>();
    @ManyToOne
    private Shift shift;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private DayType dayType;
    @Temporal(javax.persistence.TemporalType.TIME)
    private Date startingTime;
    @Temporal(javax.persistence.TemporalType.TIME)
    private Date endingTime;
    private boolean grouped;
    private int repeatedDay;
    private boolean dayOff;
    private int count;
    @Transient
    private int durationHour;
    @Transient
    private int durationMin;

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
        
        if (!(object instanceof DayShift)) {
            return false;
        }
        DayShift other = (DayShift) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.hr.DayShift[ id=" + id + " ]";
    }

    public DayType getDayType() {
        return dayType;
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }

    public Date getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(Date startingTime) {
        this.startingTime = startingTime;
    }

    public Date getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(Date endingTime) {
        this.endingTime = endingTime;
    }

    public boolean isGrouped() {
        return grouped;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }

    public int getRepeatedDay() {
        return repeatedDay;
    }

    public void setRepeatedDay(int repeatedDay) {
        this.repeatedDay = repeatedDay;
    }

    public boolean isDayOff() {
        return dayOff;
    }

    public void setDayOff(boolean dayOff) {
        this.dayOff = dayOff;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<StaffShift> getStaffShifts() {

        return staffShifts;
    }

    public void setStaffShifts(List<StaffShift> staffShifts) {
        this.staffShifts = staffShifts;
    }

    public int getDurationHour() {
        if (getStartingTime() == null && getEndingTime() == null) {
            return 0;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(getStartingTime());
        int sHour = cal.get(Calendar.HOUR_OF_DAY);
        cal.setTime(getEndingTime());
        int eHour = cal.get(Calendar.HOUR_OF_DAY);

        if (sHour < eHour) {
            durationHour = eHour - sHour;
        } else {
            durationHour = sHour - 12;
            durationHour += 12 - eHour;
        }

        return durationHour;
    }

    public int getDurationMin() {
      
        if (getStartingTime() == null && getEndingTime() == null) {
            return 0;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(getStartingTime());
        int sMin = cal.get(Calendar.MINUTE);
        cal.setTime(getEndingTime());
        int eMin = cal.get(Calendar.MINUTE);

        if (sMin < eMin) {
            durationMin = eMin - sMin;
        } else if (sMin == eMin) {
            durationMin=0;
        } else {
            durationMin = sMin - eMin;
        }

        return durationMin;
    }

    
    public void setDurationHour(int durationHour) {
        this.durationHour = durationHour;
    }

    public void setDurationMin(int durationMin) {
        this.durationMin = durationMin;
    }

}
