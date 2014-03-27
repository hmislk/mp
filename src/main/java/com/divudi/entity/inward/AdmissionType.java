/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.inward;

import com.divudi.data.inward.AdmissionTypeEnum;
import com.divudi.entity.Category;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 *
 * @author www.divudi.com
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AdmissionType extends Category implements Serializable {

    private static final long serialVersionUID = 1L;
    //Admission Fee
    private double admissionFee=0.0;
    private boolean inwardPackage;
    boolean roomChargesAllowed;
    @Enumerated(EnumType.STRING)
    private AdmissionTypeEnum admissionTypeEnum;
   

    public boolean isRoomChargesAllowed() {
        return roomChargesAllowed;
    }

    public void setRoomChargesAllowed(boolean roomChargesAllowed) {
        this.roomChargesAllowed = roomChargesAllowed;
    }

  
    public double getAdmissionFee() {
        return admissionFee;
    }

    public void setAdmissionFee(double admissionFee) {
        this.admissionFee = admissionFee;
    }

    public boolean isInwardPackage() {
        return inwardPackage;
    }

    public void setInwardPackage(boolean inwardPackage) {
        this.inwardPackage = inwardPackage;
    }

    public AdmissionTypeEnum getAdmissionTypeEnum() {
        return admissionTypeEnum;
    }

    public void setAdmissionTypeEnum(AdmissionTypeEnum admissionTypeEnum) {
        this.admissionTypeEnum = admissionTypeEnum;
    }

  
}
