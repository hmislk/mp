/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data.dataStructure;

import com.divudi.entity.inward.PatientRoom;
import java.io.Serializable;

/**
 *
 * @author Buddhika
 */
public class RoomChargeData implements Serializable {

    private PatientRoom patientRoom;
    private Double chargeTot = 0.0;
    Double maintananceTot = 0.0;
    private Double linenTot = 0.0;
    private Double nursingTot = 0.0;
    private Double moChargeTot = 0.0;

    public PatientRoom getPatientRoom() {
        return patientRoom;
    }

    public Double getChargeTot() {
        return chargeTot;
    }

    public void setChargeTot(Double chargeTot) {
        this.chargeTot = chargeTot;
    }

    public Double getMaintananceTot() {
        return maintananceTot;
    }

    public void setMaintananceTot(Double maintananceTot) {
        this.maintananceTot = maintananceTot;
    }

    public void setPatientRoom(PatientRoom patientRoom) {
        this.patientRoom = patientRoom;
    }

    public Double getLinenTot() {
        return linenTot;
    }

    public void setLinenTot(Double linenTot) {
        this.linenTot = linenTot;
    }

    public Double getNursingTot() {
        return nursingTot;
    }

    public void setNursingTot(Double nursingTot) {
        this.nursingTot = nursingTot;
    }

    public Double getMoChargeTot() {
        return moChargeTot;
    }

    public void setMoChargeTot(Double moChargeTot) {
        this.moChargeTot = moChargeTot;
    }
}
