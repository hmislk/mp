/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data.dataStructure;

import com.divudi.entity.hr.StaffShift;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author safrin
 */
public class ShiftTable {

    private List<StaffShift> staffShift;
    private Date date;
    private boolean firstDate = false;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<StaffShift> getStaffShift() {

        return staffShift;
    }

    public void setStaffShift(List<StaffShift> staffShift) {
        this.staffShift = staffShift;
    }

    public boolean isFirstDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
            firstDate = true;
        }

        return firstDate;
    }

    public void setFirstDate(boolean firstDate) {
        this.firstDate = firstDate;
    }
}
