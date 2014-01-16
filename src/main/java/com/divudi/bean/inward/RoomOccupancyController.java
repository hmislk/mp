/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.inward;

import com.divudi.entity.inward.PatientRoom;
import com.divudi.facade.PatientRoomFacade;
import javax.inject.Named; import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named; import javax.ejb.EJB;

/**
 *
 * @author Buddhika
 */
@Named
@SessionScoped
public class RoomOccupancyController implements Serializable {

    @EJB
    private PatientRoomFacade patientRoomFacade;
    private List<PatientRoom> patientRooms;

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

    public List<PatientRoom> getPatientRooms() {
        String sql = "SELECT pr FROM PatientRoom pr where pr.retired=false and pr.room.filled=true and pr.room.retired=false and pr.dischargedAt is null";
        patientRooms = getPatientRoomFacade().findBySQL(sql);

        
        if (patientRooms == null) {
            patientRooms=new ArrayList<PatientRoom>();
        }

        return patientRooms;
    }

    public void setPatientRooms(List<PatientRoom> patientRooms) {
        this.patientRooms = patientRooms;
    }
}
