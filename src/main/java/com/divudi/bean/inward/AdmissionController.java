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
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.Title;
import com.divudi.data.dataStructure.YearMonthDay;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.InwardCalculation;
import com.divudi.entity.Appointment;
import com.divudi.entity.Bill;
import com.divudi.entity.Patient;
import com.divudi.entity.Person;
import com.divudi.entity.inward.Admission;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.facade.AdmissionFacade;
import com.divudi.facade.AppointmentFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PatientRoomFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.RoomFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class AdmissionController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    ////////////
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
    ////////////////////////////
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private InwardCalculation inwardCalculation;
    ///////////////////////
    List<Admission> selectedItems;
    private Admission current;
    private PatientRoom patientRoom;
    private List<Admission> items = null;
    private List<Patient> patientList;
    ///////////////////////////
    String selectText = "";
    private String ageText = "";
    private String bhtText = "";
    private String patientTabId = "tabNewPt";
    private Patient newPatient;
    private YearMonthDay yearMonthDay;
    private Bill appointmentBill;

    public void dateChangeListen() {
        getNewPatient().getPerson().setDob(getCommonFunctions().guessDob(yearMonthDay));

    }

    public PaymentMethod[] getPaymentMethods() {
        PaymentMethod[] tmp = {PaymentMethod.Cash, PaymentMethod.Credit, PaymentMethod.Credit };
        return tmp;
    }

    public Title[] getTitle() {
        return Title.values();
    }

    public Sex[] getSex() {
        return Sex.values();
    }

//    public List<Admission> completePatientBht(String query) {
//        List<Admission> suggestions;
//        String sql;
//        if (query == null) {
//            suggestions = new ArrayList<>();
//        } else {
//            sql = "select c from Admission c where c.retired=false and "
//                    + " c.discharged=false and (upper(c.bhtNo) like '%" + query.toUpperCase() + "%' "
//                    + "or upper(c.patient.person.name) like '%" + query.toUpperCase() + "%') "
//                    + "order by c.bhtNo";
//            //System.out.println(sql);
//            suggestions = getFacade().findBySQL(sql);
//        }
//        return suggestions;
//    }
    public List<Admission> getSelectedItems() {
        selectedItems = getFacade().findBySQL("select c from Admission c where c.retired=false and upper(c.name) like '%" + getSelectText().toUpperCase() + "%' order by c.name");
        return selectedItems;
    }

    public void onTabChange(TabChangeEvent event) {
        setPatientTabId(event.getTab().getId());

    }

    public List<Admission> completePatient(String query) {
        List<Admission> suggestions;
        String sql;
        HashMap hm = new HashMap();
        sql = "select c from Admission c where c.retired=false and c.discharged=false "
                + " and (upper(c.bhtNo) like :q or"
                + " upper(c.patient.person.name) like :q ) order by c.bhtNo";
        hm.put("q", "%" + query.toUpperCase() + "%");
        suggestions = getFacade().findBySQL(sql, hm);

        return suggestions;
    }

    public List<Admission> completePatientCredit(String query) {
        List<Admission> suggestions;
        String sql;
        HashMap hm = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Admission c where c.retired=false and c.paymentMethod=:pm  and (upper(c.bhtNo) like '%" + query.toUpperCase() + "%' or upper(c.patient.person.name) like '%" + query.toUpperCase() + "%') order by c.bhtNo";
            hm.put("pm", PaymentMethod.Credit);
            //System.out.println(sql);
            suggestions = getFacade().findBySQL(sql, hm, TemporalType.TIME);
        }
        return suggestions;
    }

    public List<Admission> completePatient2(String query) {
        List<Admission> suggestions;
        String sql;
        HashMap h = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Admission c where c.retired=false and "
                    + " ( c.paymentFinalized is null or c.paymentFinalized=false )"
                    + " and ( (upper(c.bhtNo) like :q )or (upper(c.patient.person.name)"
                    + " like :q) ) order by c.bhtNo";
            //System.out.println(sql);
            //      h.put("btp", BillType.InwardPaymentBill);
            h.put("q", "%" + query.toUpperCase() + "%");
            suggestions = getFacade().findBySQL(sql, h);
        }
        return suggestions;
    }

    public List<Admission> completeDishcahrgedPatient(String query) {
        List<Admission> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Admission c where c.retired=false and c.discharged=true and (upper(c.bhtNo) like '%" + query.toUpperCase() + "%' or upper(c.patient.person.name) like '%" + query.toUpperCase() + "%') order by c.bhtNo";
            //System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        return suggestions;
    }

    public void prepareAdd() {
        current = new Admission();
    }

    public List<Admission> completeBht(String query) {
        List<Admission> suggestions;
        String sql;
        if (query == null || query.trim().equals("")) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select p from Admission p where p.retired=false and upper(p.bhtNo) like '%" + query.toUpperCase() + "%'";
            //System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        if (suggestions == null) {
            suggestions = new ArrayList<>();
        }
        return suggestions;
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
        makeNull();
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

    public void makeNull() {
        current = null;
        patientRoom = null;
        items = null;
        bhtText = "";
        ageText = null;
        patientList = null;
        patientTabId = "tabNewPt";
        selectText = "";
        selectedItems = null;
        newPatient = null;
        yearMonthDay = null;
        deposit = 0;
        bhtNumberCalculation();
    }

    public void discharge() {
        if (getCurrent().getId() == null || getCurrent().getId() == 0) {
            UtilityController.addSuccessMessage("No Patient Data Found");
        } else {
            getCurrent().setDischarged(Boolean.TRUE);
            getCurrent().setDateOfDischarge(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            getEjbFacade().edit(current);
        }

    }

    private void savePatient() {
        getNewPatient().getPerson().setCreatedAt(Calendar.getInstance().getTime());
        getNewPatient().getPerson().setCreater(getSessionController().getLoggedUser());
        getPersonFacade().create(getNewPatient().getPerson());

        getNewPatient().setCreatedAt(Calendar.getInstance().getTime());
        getNewPatient().setCreater(getSessionController().getLoggedUser());
        getPatientFacade().create(getNewPatient());
    }

    private void saveGuardian() {
        Person temG = getCurrent().getGuardian();
        temG.setCreatedAt(Calendar.getInstance().getTime());
        temG.setCreater(getSessionController().getLoggedUser());

        if (temG.getId() == null || temG.getId() == 0) {
            getPersonFacade().create(temG);
        } else {
            getPersonFacade().edit(temG);
        }

    }

//        private boolean checkBht() {
//            String sql = "SELECT a FROM Admission a Where a.retired=false";
//            List<Admission> lst = getFacade().findBySQL(sql);
//
//            for (Admission an : lst) {
//                if (an.getBhtNo()!=null && getBhtText()!=null && an.getBhtNo().trim().toUpperCase().equals(getBhtText().trim().toUpperCase())) {
//                    UtilityController.addErrorMessage("Bht Number Already Exist");
//                    return true;
//                }
//
//            }
//
//            return false;
//
//        }
    private boolean errorCheck() {

        if (getCurrent().getAdmissionType() == null) {
            UtilityController.addErrorMessage("Please select Admission Type");
            return true;
        }

        if (getCurrent().getPaymentMethod() == null) {
            UtilityController.addErrorMessage("Select Paymentmethod");
            return true;
        }

        if (getCurrent().getPaymentMethod() == PaymentMethod.Credit) {
            if (getCurrent().getCreditCompany() == null) {
                UtilityController.addErrorMessage("Select Credit Company");
                return true;
            }

        }

        if (getCurrent().getAdmissionType().isRoomChargesAllowed()) {
            if (getPatientRoom().getRoomFacilityCharge() == null) {
                UtilityController.addErrorMessage("Select Room");
                return true;
            }
            if (getPatientRoom().getRoomFacilityCharge().getRoom().isFilled()) {
                UtilityController.addErrorMessage("Select Empty Room");
                return true;
            }
        }

        if (getPatientTabId().toString().equals("tabNewPt")) {
            if ("".equals(getAgeText())) {
                UtilityController.addErrorMessage("Patient Age Should be Typed");
                return true;
            }
            if (getNewPatient().getPerson().getName() == null || getNewPatient().getPerson().getName().trim().equals("") || getNewPatient().getPerson().getSex() == null || getAgeText() == null) {
                UtilityController.addErrorMessage("Can not admit without Patient Name, Age or Sex.");
                return true;
            }
        }

        if (getPatientTabId().toString().trim().equals("tabSearchPt")) {
            if (getCurrent().getPatient() == null) {
                UtilityController.addErrorMessage("Select Patient");
                return true;
            }
        }

        return false;
    }

    private double deposit;
    @Inject
    private InwardPaymentController inwardPaymentController;
    @EJB
    private AppointmentFacade appointmentFacade;
    @EJB
    private BillFacade billFacade;

    private void updateAppointment() {
        String sql = "Select s from Appointment s where s.retired=false and s.bill=:b";
        HashMap hm = new HashMap();
        hm.put("b", getAppointmentBill());
        Appointment apt = getAppointmentFacade().findFirstBySQL(sql, hm);
        apt.setPatientEncounter(getCurrent());
        getAppointmentFacade().edit(apt);

    }

    private void updateAppointmentBill() {
        getAppointmentBill().setRefunded(true);
        getBillFacade().edit(getAppointmentBill());

    }

    public void saveSelected() {

        if (errorCheck()) {
            return;
        }

        if (getPatientTabId().toString().equals("tabNewPt")) {
            savePatient();
            getCurrent().setPatient(getNewPatient());
        }

        saveGuardian();
        getCurrent().setBhtNo(getBhtText());

        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("savedOldSuccessfully");
        } else {
            current.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setCreater(getSessionController().getLoggedUser());
            //      getCurrent().setDateOfAdmission(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            getFacade().create(current);
            UtilityController.addSuccessMessage("Patient Admitted Succesfully");
        }

        savePatientRoom(getCurrent().getDateOfAdmission());

        double appointmentFee = 0;
        if (getAppointmentBill() != null) {
            appointmentFee = getAppointmentBill().getTotal();
            updateAppointment();
            updateAppointmentBill();
        }

        if (appointmentFee != 0) {
            System.err.println("Appoint ");
            getInwardPaymentController().getCurrent().setPaymentMethod(getCurrent().getPaymentMethod());
            getInwardPaymentController().getCurrent().setPatientEncounter(current);
            getInwardPaymentController().getCurrent().setTotal(appointmentFee);
            getInwardPaymentController().pay();
            getInwardPaymentController().makeNull();
        }

        if (getDeposit() != 0) {
            System.err.println("Deposit ");
            getInwardPaymentController().pay(getCurrent().getPaymentMethod(), getCurrent(), getDeposit());
            //     getInwardPaymentController().setPrintPreview(true);
        }

        if (getDeposit() == 0) {
            makeNull();
        }

    }

    private void makeRoomFilled(PatientRoom pr) {

        pr.getRoom().setFilled(true);
        getRoomFacade().edit(pr.getRoom());

    }

    private void savePatientRoom(Date admittedAt) {
        getPatientRoom().setCurrentLinenCharge(getPatientRoom().getRoomFacilityCharge().getLinenCharge());
        getPatientRoom().setCurrentMaintananceCharge(getPatientRoom().getRoomFacilityCharge().getMaintananceCharge());
        getPatientRoom().setCurrentMoCharge(getPatientRoom().getRoomFacilityCharge().getMoCharge());
        getPatientRoom().setCurrentNursingCharge(getPatientRoom().getRoomFacilityCharge().getNursingCharge());
        getPatientRoom().setCurrentRoomCharge(getPatientRoom().getRoomFacilityCharge().getRoomCharge());

        getPatientRoom().setAddmittedBy(getSessionController().getLoggedUser());
        getPatientRoom().setAdmittedAt(admittedAt);
        getPatientRoom().setCreatedAt(Calendar.getInstance().getTime());
        getPatientRoom().setCreater(getSessionController().getLoggedUser());
        getPatientRoom().setPatientEncounter(getCurrent());
        getPatientRoom().setRoom(getPatientRoom().getRoomFacilityCharge().getRoom());

        if (getPatientRoom().getId() == null || getPatientRoom().getId() == 0) {
            getPatientRoomFacade().create(getPatientRoom());
        } else {
            getPatientRoomFacade().edit(getPatientRoom());
        }

        makeRoomFilled(getPatientRoom());
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

    public AdmissionController() {
    }

    public Admission getCurrent() {
        if (current == null) {
            current = new Admission();
            current.setDateOfAdmission(new Date());
        }
        return current;
    }

    public void setCurrent(Admission current) {
        this.current = current;
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
                items = new ArrayList<Admission>();
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

    public PatientRoom getPatientRoom() {
        if (patientRoom == null) {
            patientRoom = new PatientRoom();
        }
        return patientRoom;
    }

    public void setPatientRoom(PatientRoom patientRoom) {
        this.patientRoom = patientRoom;
    }

    public String getAgeText() {
        ageText = getNewPatient().getAge();
        return ageText;
    }

    public void setAgeText(String ageText) {
        this.ageText = ageText;
        getNewPatient().getPerson().setDob(getCommonFunctions().guessDob(ageText));
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public PatientRoomFacade getPatientRoomFacade() {
        return patientRoomFacade;
    }

    public void setPatientRoomFacade(PatientRoomFacade patientRoomFacade) {
        this.patientRoomFacade = patientRoomFacade;
    }

    public void bhtNumberCalculation() {
        if (getCurrent() == null || getCurrent().getAdmissionType() == null) {
//            UtilityController.addErrorMessage("Please Set Admission Type DayCase/Admission For this this Admission ");
            return;
        }

        if (getCurrent().getAdmissionType().getAdmissionTypeEnum() == null) {
            UtilityController.addErrorMessage("Please Set Admission Type DayCase/Admission For this this Admission ");
            return;
        }

        bhtText = getInwardCalculation().getBhtText(getCurrent().getAdmissionType());
    }

    public String getBhtText() {
        return bhtText;
    }

    public void setBhtText(String bhtText) {
        this.bhtText = bhtText;
    }

    public RoomFacade getRoomFacade() {
        return roomFacade;
    }

    public void setRoomFacade(RoomFacade roomFacade) {
        this.roomFacade = roomFacade;
    }

    public String getPatientTabId() {
        return patientTabId;
    }

    public void setPatientTabId(String patientTabId) {
        this.patientTabId = patientTabId;
    }

    public Patient getNewPatient() {
        if (newPatient == null) {
            Person p = new Person();
            newPatient = new Patient();
            newPatient.setPerson(p);
        }
        return newPatient;
    }

    public void setNewPatient(Patient newPatient) {
        this.newPatient = newPatient;
    }

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;
    }

    public YearMonthDay getYearMonthDay() {
        if (yearMonthDay == null) {
            yearMonthDay = new YearMonthDay();
        }
        return yearMonthDay;
    }

    public void setYearMonthDay(YearMonthDay yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }

    public Bill getAppointmentBill() {
        return appointmentBill;
    }

    public void setAppointmentBill(Bill appointmentBill) {
        this.appointmentBill = appointmentBill;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public InwardPaymentController getInwardPaymentController() {
        return inwardPaymentController;
    }

    public void setInwardPaymentController(InwardPaymentController inwardPaymentController) {
        this.inwardPaymentController = inwardPaymentController;
    }

    public AppointmentFacade getAppointmentFacade() {
        return appointmentFacade;
    }

    public void setAppointmentFacade(AppointmentFacade appointmentFacade) {
        this.appointmentFacade = appointmentFacade;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    /**
     *
     */
    @FacesConverter("admis")
    public static class AdmissionConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AdmissionController controller = (AdmissionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "admissionController");
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
                        + object.getClass().getName() + "; expected type: " + AdmissionController.class.getName());
            }
        }
    }

    @FacesConverter(forClass = Admission.class)
    public static class AdmissionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AdmissionController controller = (AdmissionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "admissionController");
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
                        + object.getClass().getName() + "; expected type: " + AdmissionController.class.getName());
            }
        }
    }
}
