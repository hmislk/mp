/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.inward;

import com.divudi.entity.inward.PatientRoom;
import com.divudi.facade.PatientRoomFacade;
import com.divudi.facade.RoomFacade;
import static com.lowagie.text.SpecialSymbol.get;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import javax.inject.Named;
import javax.ejb.EJB;

/**
 *
 * @author Buddhika
 */
@Named
@SessionScoped
public class RoomOccupancyController implements Serializable {

    @EJB
    private PatientRoomFacade patientRoomFacade;
    @EJB
    RoomFacade roomFacade;
    private List<PatientRoom> patientRooms;

    public RoomFacade getRoomFacade() {
        return roomFacade;
    }

    public void setRoomFacade(RoomFacade roomFacade) {
        this.roomFacade = roomFacade;
    }

    /**
     * Creates a new instance of RoomOccupancyController
     */
    public RoomOccupancyController() {
    }

    public PatientRoomFacade getPatientRoomFacade() {
        return patientRoomFacade;
    }

    public void setPatientRoomFacade(PatientRoomFacade patientRoomFacade) {
        this.patientRoomFacade = patientRoomFacade;
    }

    public void update(PatientRoom patientRoom) {        
        getRoomFacade().edit(patientRoom.getRoomFacilityCharge().getRoom());
    }

    public void createPatientRoom() {
        String sql = "SELECT pr FROM PatientRoom pr "
                + " where pr.retired=false"
                + " and pr.room.filled=true "
                + " and pr.room.retired=false "
                + " and pr.dischargedAt is null";
        patientRooms = getPatientRoomFacade().findBySQL(sql);

    }

    public List<PatientRoom> getPatientRooms() {

        return patientRooms;
    }

    public void setPatientRooms(List<PatientRoom> patientRooms) {
        this.patientRooms = patientRooms;
    }
}
