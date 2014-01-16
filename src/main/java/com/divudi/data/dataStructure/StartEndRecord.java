/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.divudi.data.dataStructure;

import com.divudi.entity.hr.FingerPrintRecord;
import com.divudi.entity.hr.StaffShift;

/**
 *
 * @author safrin
 */
public class StartEndRecord {
    private FingerPrintRecord startRecord;
    private FingerPrintRecord endRecord;
    private StaffShift staffShift;

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

    public StaffShift getStaffShift() {
        return staffShift;
    }

    public void setStaffShift(StaffShift staffShift) {
        this.staffShift = staffShift;
    }
}
