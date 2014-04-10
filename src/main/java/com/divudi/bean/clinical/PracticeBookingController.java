/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.clinical;

import com.divudi.bean.PatientController;
import com.divudi.bean.channel.*;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillType;
import com.divudi.data.inward.PatientEncounterType;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.ChannelBean;
import com.divudi.ejb.ServiceSessionBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BillSession;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Doctor;
import com.divudi.entity.Patient;
import com.divudi.entity.Person;
import com.divudi.entity.ServiceSession;
import com.divudi.entity.Speciality;
import com.divudi.entity.Staff;
import com.divudi.entity.PatientEncounter;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.BillSessionFacade;
import com.divudi.facade.DoctorFacade;
import com.divudi.facade.InstitutionFacade;
import com.divudi.facade.PatientEncounterFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.ServiceSessionFacade;
import com.divudi.facade.StaffFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class PracticeBookingController implements Serializable {

    private Speciality speciality;
    private ServiceSession selectedServiceSession;
    private BillSession selectedBillSession;
    Doctor doctor;
    ////////////////////
    private List<ServiceSession> serviceSessions;
    private List<BillSession> billSessions;
    ////////////////////
    @Inject
    private SessionController sessionController;
    @Inject
    private ChannelBillController channelCancelController;
    @Inject
    private ChannelReportController channelReportController;
    @Inject
    private ChannelSearchController channelSearchController;
    @Inject
    private PatientController patientController;
    ///////////////////
    @EJB
    DoctorFacade doctorFacade;
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private ServiceSessionFacade serviceSessionFacade;
    @EJB
    private BillSessionFacade billSessionFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private PatientFacade patientFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private ChannelBean channelBean;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    ServiceSessionBean serviceSessionBean;
    @EJB
    PatientEncounterFacade patientEncounterFacade;
    //
    private boolean foreigner;

    BillSession billSession;
    PatientEncounter opdVisit;

    public List<Doctor> completeDoctorsOfSelectedSpeciality(String qry) {
        String sql;
        Map m = new HashMap();
        List<Doctor> docs;
        if (speciality == null) {
            sql = "select d from Doctor d where d.retired=false order by d.person.name";
            docs = getDoctorFacade().findBySQL(sql);
        } else {
            sql = "select d from Doctor d where d.retired=false and d.speciality=:sp order by d.person.name";
            m.put("sp", speciality);
            docs = getDoctorFacade().findBySQL(sql, m);
        }
        return docs;
    }

    public PatientEncounterFacade getPatientEncounterFacade() {
        return patientEncounterFacade;
    }

    public void setPatientEncounterFacade(PatientEncounterFacade patientEncounterFacade) {
        this.patientEncounterFacade = patientEncounterFacade;
    }

    public BillSession getBillSession() {
        return billSession;
    }

    public void setBillSession(BillSession billSession) {
        this.billSession = billSession;
    }

    public PatientEncounter getOpdVisit() {
        return opdVisit;
    }

    public void setOpdVisit(PatientEncounter opdVisit) {
        this.opdVisit = opdVisit;
    }

    public String opdVisitFromQueue() {
        if (billSession == null) {
            UtilityController.addErrorMessage("Please select encounter");
            opdVisit = null;
            return "";
        }
        opdVisitFromServiceSession();
        getPatientEncounterController().setCurrent(opdVisit);
        return "clinical_new_opd_visit";
    }

    @Inject
    PatientEncounterController patientEncounterController;

    public PatientEncounterController getPatientEncounterController() {
        return patientEncounterController;
    }

    public void setPatientEncounterController(PatientEncounterController patientEncounterController) {
        this.patientEncounterController = patientEncounterController;
    }

    public void opdVisitFromServiceSession() {
        if (billSession == null) {
            UtilityController.addErrorMessage("Please select encounter");
            opdVisit = null;
            return;
        }
        Map m = new HashMap();
        m.put("bs", getBillSession());
        String sql;
        sql = "select pe from PatientEncounter pe where pe.billSession=:bs";
        opdVisit = getPatientEncounterFacade().findFirstBySQL(sql, m);
        if (opdVisit == null) {
            newOpdVisit();
        }
    }

    public void newOpdVisit() {
        if (billSession == null) {
            UtilityController.addErrorMessage("Please select encounter");
            opdVisit = null;
            return;
        }
        opdVisit = new PatientEncounter();
        opdVisit.setCreatedAt(Calendar.getInstance().getTime());
        opdVisit.setCreater(getSessionController().getLoggedUser());
        opdVisit.setPatient(getBillSession().getBill().getPatient());
        opdVisit.setPatientEncounterType(PatientEncounterType.OpdVisit);
        opdVisit.setBillSession(billSession);
        opdVisit.setOpdDoctor(doctor);
        getPatientEncounterFacade().create(opdVisit);
    }

    public void addToQueue() {
        if (getPatientController().getCurrent() == null || getPatientController().getCurrent().getId() == null) {
            UtilityController.addErrorMessage("Please select a patient");
            return;
        }
        if (doctor == null) {
            UtilityController.addErrorMessage("Please select a doctor");
            return;
        }
        if (getSelectedServiceSession() == null) {
            UtilityController.addErrorMessage("Please select session");
            return;
        }

        addToSession(addToBilledItem(addToBill()));
        listBillSessions();
        UtilityController.addSuccessMessage("Added to the queue");
    }

    private BillItem addToBilledItem(Bill b) {
        BillItem bi = new BillItem();
        bi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        bi.setCreater(getSessionController().getLoggedUser());
        bi.setBill(b);
        bi.setNetValue(b.getTotal());
        bi.setSessionDate(getSelectedServiceSession().getSessionDate());
        getBillItemFacade().create(bi);
        return bi;
    }

    private void addToSession(BillItem bi) {
        Bill b = bi.getBill();
        BillSession bs = new BillSession();
        bs.setBill(b);
        bs.setBillItem(bi);
        bs.setCreatedAt(Calendar.getInstance().getTime());
        bs.setCreater(getSessionController().getLoggedUser());
        bs.setServiceSession(getSelectedServiceSession());
        bs.setSessionDate(getSelectedServiceSession().getSessionDate());
        int count = getServiceSessionBean().getSessionNumber(getSelectedServiceSession(), Calendar.getInstance().getTime());
        bs.setSerialNo(count);

        getBillSessionFacade().create(bs);

    }

    private Bill addToBill() {
        Bill bi = new Bill();
        bi.setBookingId(getBillNumberBean().gpBookingIdGenerator());
        bi.setStaff(getDoctor());
        bi.setBillType(BillType.ChannelPaid);
        if (foreigner) {
            bi.setTotal(getSelectedServiceSession().getTotalFfee());
        } else {
            bi.setTotal(getSelectedServiceSession().getTotalFee());
        }
        bi.setPatient(getPatientController().getCurrent());
        bi.setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        bi.setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        bi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        bi.setCreater(getSessionController().getLoggedUser());
        getBillFacade().create(bi);
        return bi;
    }

    public void updatePatient() {
        getBillSessionFacade().edit(getSelectedBillSession());

        getPatientFacade().edit(getSelectedBillSession().getBill().getPatient());
        UtilityController.addSuccessMessage("Patient Updated");
    }

    public void makeNull() {
        speciality = null;
        doctor = null;
        selectedServiceSession = null;
        /////////////////////
        serviceSessions = null;
        billSessions = null;
    }

    public List<Staff> completeStaff(String query) {
        List<Staff> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<Staff>();
        } else {
            if (getSpeciality() != null) {
                sql = "select p from Staff p where p.retired=false and (upper(p.person.name) like '%" + query.toUpperCase() + "%'or  upper(p.code) like '%" + query.toUpperCase() + "%' ) and p.speciality.id = " + getSpeciality().getId() + " order by p.person.name";
            } else {
                sql = "select p from Staff p where p.retired=false and (upper(p.person.name) like '%" + query.toUpperCase() + "%'or  upper(p.code) like '%" + query.toUpperCase() + "%' ) order by p.person.name";
            }
            //System.out.println(sql);
            suggestions = getStaffFacade().findBySQL(sql);
        }
        return suggestions;
    }

    public List<Staff> getConsultants() {
        List<Staff> suggestions;
        String sql;

        if (getSpeciality() != null) {
            sql = "select p from Staff p where p.retired=false and p.speciality.id = " + getSpeciality().getId() + " order by p.person.name";
        } else {
            sql = "select p from Doctor p where p.retired=false order by p.person.name";
        }
        //System.out.println(sql);
        suggestions = getStaffFacade().findBySQL(sql);

        return suggestions;
    }

    /**
     * Creates a new instance of BookingController
     */
    public PracticeBookingController() {
        serviceSessions = new ArrayList<ServiceSession>();
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        makeNull();
        this.speciality = speciality;
    }

    public StaffFacade getStaffFacade() {
        return staffFacade;
    }

    public void setStaffFacade(StaffFacade staffFacade) {
        this.staffFacade = staffFacade;
    }

    public List<ServiceSession> getServiceSessions() {
        //System.out.println("gettint service sessions");

        if (serviceSessions == null) {
            serviceSessions = new ArrayList<>();
            String sql;

            if (doctor != null) {
                try {
                    //System.out.println("staff is " + staff);
                    sql = "Select s From ServiceSession s where s.retired=false and s.staff.id=" + getDoctor().getId() + " order by s.sessionWeekday";
                    List<ServiceSession> tmp = getServiceSessionFacade().findBySQL(sql);
                    //System.out.println("tmp is " + tmp.size());
                    if (!tmp.isEmpty()) {
                        serviceSessions = getChannelBean().setSessionAt(tmp);
                    }
                } catch (Exception e) {
                    //System.out.println("error 11 + " + e.getMessage());
                }
            }
        }

        return serviceSessions;
    }

    public void setServiceSessions(List<ServiceSession> serviceSessions) {

        this.serviceSessions = serviceSessions;
    }

    public ServiceSessionFacade getServiceSessionFacade() {
        return serviceSessionFacade;
    }

    public void setServiceSessionFacade(ServiceSessionFacade serviceSessionFacade) {
        this.serviceSessionFacade = serviceSessionFacade;
    }

    public void listBillSessions() {
        String sql = "Select bs From BillSession bs where bs.retired=false and bs.serviceSession=:ss and bs.sessionDate= :ssDate";
        HashMap hh = new HashMap();
        hh.put("ssDate", Calendar.getInstance().getTime());
        hh.put("ss", getSelectedServiceSession());
        System.out.println("hh = " + hh);
        System.out.println("sql = " + sql);
        billSessions = getBillSessionFacade().findBySQL(sql, hh, TemporalType.DATE);
    }

    public List<BillSession> getBillSessions() {
        return billSessions;
    }

    public void setBillSessions(List<BillSession> billSessions) {
        this.billSessions = billSessions;
    }

    public ServiceSession getSelectedServiceSession() {
        if (selectedServiceSession == null) {
            String sql;
            Map m = new HashMap();
            m.put("s", doctor);
            m.put("d", Calendar.getInstance().getTime());
            sql = "select ss from ServiceSession ss where ss.staff=:s and ss.sessionDate=:d";
            selectedServiceSession = getServiceSessionFacade().findFirstBySQL(sql, m, TemporalType.DATE);
            if (selectedServiceSession == null) {
                selectedServiceSession = new ServiceSession();
                selectedServiceSession.setSessionDate(Calendar.getInstance().getTime());
                selectedServiceSession.setStaff(doctor);
                selectedServiceSession.setSessionTime(Calendar.getInstance().getTime());
                getServiceSessionFacade().create(selectedServiceSession);
            }
        }
        return selectedServiceSession;
    }

    public void setSelectedServiceSession(ServiceSession selectedServiceSession) {
        this.selectedServiceSession = selectedServiceSession;
    }

    public void makeBillSessionNull() {
        billSessions = null;
    }

    public BillSessionFacade getBillSessionFacade() {
        return billSessionFacade;
    }

    public void setBillSessionFacade(BillSessionFacade billSessionFacade) {
        this.billSessionFacade = billSessionFacade;
    }

    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

    public void setInstitutionFacade(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
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

    public BillSession getSelectedBillSession() {
        if (selectedBillSession == null) {
            selectedBillSession = new BillSession();
            Bill b = new BilledBill();
            Patient p = new Patient();
            p.setPerson(new Person());
            b.setPatient(p);
            selectedBillSession.setBill(b);
        }
        return selectedBillSession;
    }

    public void setSelectedBillSession(BillSession selectedBillSession) {
        this.selectedBillSession = selectedBillSession;
        getChannelCancelController().makeNull();
        getChannelCancelController().setBillSession(selectedBillSession);
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public ChannelBillController getChannelCancelController() {
        return channelCancelController;
    }

    public void setChannelCancelController(ChannelBillController channelCancelController) {
        this.channelCancelController = channelCancelController;
    }

    public ChannelReportController getChannelReportController() {
        return channelReportController;
    }

    public void setChannelReportController(ChannelReportController channelReportController) {
        this.channelReportController = channelReportController;
    }

    public Boolean preSet() {
        if (getSelectedServiceSession() == null) {
            UtilityController.addErrorMessage("Please select Service Session");
            return false;
        }

        getChannelReportController().setServiceSession(selectedServiceSession);
        return true;
    }

    public ChannelSearchController getChannelSearchController() {
        return channelSearchController;
    }

    public void setChannelSearchController(ChannelSearchController channelSearchController) {
        this.channelSearchController = channelSearchController;
    }

    public ChannelBean getChannelBean() {
        return channelBean;
    }

    public void setChannelBean(ChannelBean channelBean) {
        this.channelBean = channelBean;
    }

    public PatientController getPatientController() {
        return patientController;
    }

    public void setPatientController(PatientController patientController) {
        this.patientController = patientController;
    }

    /**
     *
     * @return
     */
    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public boolean isForeigner() {
        return foreigner;
    }

    public void setForeigner(boolean foreigner) {
        this.foreigner = foreigner;
    }

    public ServiceSessionBean getServiceSessionBean() {
        return serviceSessionBean;
    }

    public void setServiceSessionBean(ServiceSessionBean serviceSessionBean) {
        this.serviceSessionBean = serviceSessionBean;
    }

    public DoctorFacade getDoctorFacade() {
        return doctorFacade;
    }

    public void setDoctorFacade(DoctorFacade doctorFacade) {
        this.doctorFacade = doctorFacade;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

}
