/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.inward;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillType;
import com.divudi.ejb.InwardBean;
import com.divudi.entity.inward.Admission;
import com.divudi.entity.Patient;
import com.divudi.entity.Person;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.inward.RoomFacilityCharge;
import com.divudi.facade.AdmissionFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PatientRoomFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.RoomFacade;
import com.divudi.facade.RoomFacilityChargeFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class RoomChangeController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @Inject
    private RoomFacilityChargeController roomFacilityChargeController;
    @EJB
    private AdmissionFacade ejbFacade;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private PatientFacade patientFacade;
    @EJB
    private PatientRoomFacade patientRoomFacade;
    @EJB
    private RoomFacade roomFacade;
    private List<PatientRoom> patientRoom;
    List<Admission> selectedItems;
    private Admission current;
    private List<Admission> items = null;
    private List<Patient> patientList;
    String selectText = "";
    private PatientRoom currentPatientRoom;
    private RoomFacilityCharge newRoomFacilityCharge;
    @Temporal(TemporalType.TIMESTAMP)
    private Date changeAt;
    private double addLinenCharge = 0.0;

    public void update(PatientRoom pR) {
        getPatientRoomFacade().edit(pR);
    }

    private void recreate() {
        patientRoom = null;
        selectedItems = null;
        current = null;
        currentPatientRoom = null;
        items = null;
        patientList = null;
        changeAt = null;
        newRoomFacilityCharge = null;
    }

    private boolean updatePatientRoom() {
        if (currentPatientRoom.getAdmittedAt().getTime() > getChangeAt().getTime()) {
            UtilityController.addErrorMessage("U cant discharge early date than admitted");
            return false;
        }
        currentPatientRoom.setDischargedAt(getChangeAt());
        currentPatientRoom.setDischargedBy(getSessionController().getLoggedUser());
        getPatientRoomFacade().edit(currentPatientRoom);
        return true;
    }

    @EJB
    private InwardBean inwardBean;

    public void change() {
        Date cur = Calendar.getInstance().getTime();

        if ((getChangeAt().getTime()) > cur.getTime()) {
            UtilityController.addErrorMessage("Check Time");
            return;
        }

        if (!updatePatientRoom()) {
            return;
        }

        getInwardBean().makeRoomVacant(getCurrent());

        PatientRoom cuPatientRoom = getInwardBean().savePatientRoom(getNewRoomFacilityCharge(), addLinenCharge, changeAt, current, getSessionController().getLoggedUser());
        getCurrent().setCurrentPatientRoom(cuPatientRoom);
        getEjbFacade().edit(getCurrent());
        recreate();
        UtilityController.addSuccessMessage("Successfully Room Changed");
    }

    public List<Admission> getSelectedItems() {
        selectedItems = getFacade().findBySQL("select c from Admission c where c.retired=false and c.discharged!=true and upper(c.bhtNo) like '%" + getSelectText().toUpperCase() + "%' or upper(c.patient.person.name) like '%" + getSelectText().toUpperCase() + "%' order by c.bhtNo");
        return selectedItems;
    }

    @EJB
    private RoomFacilityChargeFacade roomFacilityChargeFacade;

    public void prepareAdd() {
        current = new Admission();
    }

    public void delete() {

        if (getCurrent() != null) {
            getCurrent().setRetired(true);
            getCurrent().setRetiredAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            getCurrent().setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(getCurrent());
            UtilityController.addSuccessMessage("DeleteSuccessfull");
        } else {
            UtilityController.addSuccessMessage("NothingToDelete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    public void setSelectedItems(List<Admission> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    private void recreateModel() {
        current = null;
        items = null;
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public AdmissionFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(AdmissionFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public RoomChangeController() {
    }

    public Admission getCurrent() {
        if (current == null) {
            Person p = new Person();
            Patient tPatient = new Patient();
            tPatient.setPerson(p);

            current = new Admission();
            current.setPatient(tPatient);

            Person g = new Person();
            current.setGuardian(g);

            addLinenCharge = 0.0;
        }
        return current;
    }

    public void setCurrent(Admission current) {
        this.current = current;
        createPatientRoom();
    }

    private void createPatientRoom() {

        HashMap hm = new HashMap();
        String sql = "SELECT pr FROM PatientRoom pr where pr.retired=false"
                + " and pr.patientEncounter=:pe order by pr.createdAt";
        hm.put("pe", getCurrent());
        patientRoom = getPatientRoomFacade().findBySQL(sql, hm);

    }

    private AdmissionFacade getFacade() {
        return ejbFacade;
    }

    public List<Admission> getItems() {
        if (items == null) {
            String temSql;
            temSql = "SELECT i FROM Admission i where i.retired=false and i.discharged=false order by i.bhtNo";
            items = getFacade().findBySQL(temSql);
            if (items == null) {
                items = new ArrayList<>();
            }
        }

        return items;
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    public PatientFacade getPatientFacade() {
        return patientFacade;
    }

    public void setPatientFacade(PatientFacade patientFacade) {
        this.patientFacade = patientFacade;
    }

    public List<Patient> getPatientList() {
        if (patientList == null) {
            String temSql;
            temSql = "SELECT i FROM Patient i where i.retired=false ";
            patientList = getPatientFacade().findBySQL(temSql);
        }
        return patientList;
    }

    public void setPatientList(List<Patient> patientList) {
        this.patientList = patientList;
    }

    public List<PatientRoom> getPatientRoom() {
        if (patientRoom == null) {
            patientRoom = new ArrayList<>();
        }
        return patientRoom;
    }

    public void setPatientRoom(List<PatientRoom> patientRoom) {
        this.patientRoom = patientRoom;
    }

    public PatientRoomFacade getPatientRoomFacade() {
        return patientRoomFacade;
    }

    public void setPatientRoomFacade(PatientRoomFacade patientRoomFacade) {
        this.patientRoomFacade = patientRoomFacade;
    }

    public RoomFacade getRoomFacade() {

        return roomFacade;
    }

    public void setRoomFacade(RoomFacade roomFacade) {
        this.roomFacade = roomFacade;
    }

    public PatientRoom getCurrentPatientRoom() {
        if (getPatientRoom().size() > 0) {
            currentPatientRoom = patientRoom.get(getPatientRoom().size() - 1);
            getRoomFacilityChargeController().setCurrent(currentPatientRoom.getRoomFacilityCharge());
        } else {
            currentPatientRoom = new PatientRoom();
            currentPatientRoom.setRoomFacilityCharge(new RoomFacilityCharge());
        }

        return currentPatientRoom;
    }

    public void setCurrentPatientRoom(PatientRoom currentPatientRoom) {
        this.currentPatientRoom = currentPatientRoom;

    }

    public RoomFacilityCharge getNewRoomFacilityCharge() {
        return newRoomFacilityCharge;
    }

    public void setNewRoomFacilityCharge(RoomFacilityCharge newRoomFacilityCharge) {
        this.newRoomFacilityCharge = newRoomFacilityCharge;
    }

    public RoomFacilityChargeFacade getRoomFacilityChargeFacade() {
        return roomFacilityChargeFacade;
    }

    public void setRoomFacilityChargeFacade(RoomFacilityChargeFacade roomFacilityChargeFacade) {
        this.roomFacilityChargeFacade = roomFacilityChargeFacade;
    }

    public RoomFacilityChargeController getRoomFacilityChargeController() {
        return roomFacilityChargeController;
    }

    public void setRoomFacilityChargeController(RoomFacilityChargeController roomFacilityChargeController) {
        this.roomFacilityChargeController = roomFacilityChargeController;
    }

    public Date getChangeAt() {
        if (changeAt == null) {
            changeAt = Calendar.getInstance().getTime();
        }
        return changeAt;
    }

    public void setChangeAt(Date changeAt) {
        this.changeAt = changeAt;
    }

    public double getAddLinenCharge() {
        return addLinenCharge;
    }

    public void setAddLinenCharge(double addLinenCharge) {
        this.addLinenCharge = addLinenCharge;
    }

    public InwardBean getInwardBean() {
        return inwardBean;
    }

    public void setInwardBean(InwardBean inwardBean) {
        this.inwardBean = inwardBean;
    }

    /**
     *
     */
    @FacesConverter("rcc")
    public static class DischargeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            RoomChangeController controller = (RoomChangeController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "dischargeController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Admission) {
                Admission o = (Admission) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + RoomChangeController.class.getName());
            }
        }
    }
}
