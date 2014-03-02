/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.inward;

import com.divudi.entity.PatientEncounter;
import com.divudi.entity.WebUser;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;

/**
 *
 * @author Buddhika
 */
@Entity
public class PatientRoom implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
//Main Properties
    private String name;
    private String tName;
    private String sName;
    private String description;
    private int orderNo;
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
    private double addedLinenCharge = 0.0;
    @ManyToOne
    private Room room;
    @ManyToOne
    private RoomFacilityCharge roomFacilityCharge;
    @ManyToOne
    private PatientEncounter patientEncounter;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date admittedAt;
    @ManyToOne
    private WebUser addmittedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dischargedAt;
    @ManyToOne
    private WebUser dischargedBy;
    @ManyToOne
    private PatientRoom previousRoom;

    private double currentMaintananceCharge = 0.0;
    private double currentLinenCharge = 0.0;
    private double currentNursingCharge = 0.0;
    private double currentMoCharge = 0.0;
    private double currentRoomCharge;

    @Transient
    private long tmpStayedTime;
    @Transient
    private double tmpTotalRoomCharge;

    public long getTmpStayedTime() {
        return tmpStayedTime;
    }

    public void setTmpStayedTime(long tmpStayedTime) {
        this.tmpStayedTime = tmpStayedTime;
    }

    public double getTmpTotalRoomCharge() {
        return tmpTotalRoomCharge;
    }

    public void setTmpTotalRoomCharge(double tmpTotalRoomCharge) {
        this.tmpTotalRoomCharge = tmpTotalRoomCharge;
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

        if (!(object instanceof PatientRoom)) {
            return false;
        }
        PatientRoom other = (PatientRoom) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.inward.PatientRoom[ id=" + id + " ]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
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

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public RoomFacilityCharge getRoomFacilityCharge() {
        return roomFacilityCharge;
    }

    public void setRoomFacilityCharge(RoomFacilityCharge roomFacilityCharge) {
        this.roomFacilityCharge = roomFacilityCharge;
    }

    public PatientEncounter getPatientEncounter() {
        return patientEncounter;
    }

    public void setPatientEncounter(PatientEncounter patientEncounter) {
        this.patientEncounter = patientEncounter;
    }

    public Date getAdmittedAt() {
        return admittedAt;
    }

    public void setAdmittedAt(Date admittedAt) {
        this.admittedAt = admittedAt;
    }

    public WebUser getAddmittedBy() {
        return addmittedBy;
    }

    public void setAddmittedBy(WebUser addmittedBy) {
        this.addmittedBy = addmittedBy;
    }

    public Date getDischargedAt() {
        return dischargedAt;
    }

    public void setDischargedAt(Date dischargedAt) {
        this.dischargedAt = dischargedAt;
    }

    public WebUser getDischargedBy() {
        return dischargedBy;
    }

    public void setDischargedBy(WebUser dischargedBy) {
        this.dischargedBy = dischargedBy;
    }

    public PatientRoom getPreviousRoom() {
        return previousRoom;
    }

    public void setPreviousRoom(PatientRoom previousRoom) {
        this.previousRoom = previousRoom;
    }

    public String gettName() {
        return tName;
    }

    public void settName(String tName) {
        this.tName = tName;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public double getAddedLinenCharge() {

        return addedLinenCharge;
    }

    public void setAddedLinenCharge(double addedLinenCharge) {
        this.addedLinenCharge = addedLinenCharge;
    }

    public double getCurrentMaintananceCharge() {
        return currentMaintananceCharge;
    }

    public void setCurrentMaintananceCharge(double currentMaintananceCharge) {
        this.currentMaintananceCharge = currentMaintananceCharge;
    }

    public double getCurrentLinenCharge() {
        return currentLinenCharge;
    }

    public void setCurrentLinenCharge(double currentLinenCharge) {
        this.currentLinenCharge = currentLinenCharge;
    }

    public double getCurrentNursingCharge() {
        return currentNursingCharge;
    }

    public void setCurrentNursingCharge(double currentNursingCharge) {
        this.currentNursingCharge = currentNursingCharge;
    }

    public double getCurrentMoCharge() {
        return currentMoCharge;
    }

    public void setCurrentMoCharge(double currentMoCharge) {
        this.currentMoCharge = currentMoCharge;
    }

    public double getCurrentRoomCharge() {
        return currentRoomCharge;
    }

    public void setCurrentRoomCharge(double currentRoomCharge) {
        this.currentRoomCharge = currentRoomCharge;
    }
}
