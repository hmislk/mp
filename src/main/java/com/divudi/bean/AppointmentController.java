/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean;

import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.Title;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Appointment;
import com.divudi.facade.BillFacade;
import com.divudi.entity.Bill;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Institution;
import com.divudi.entity.Patient;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.Person;
import com.divudi.facade.AppointmentFacade;
import com.divudi.facade.BillComponentFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PatientInvestigationFacade;
import com.divudi.facade.PersonFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.TemporalType;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class AppointmentController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;

    @EJB
    private PatientInvestigationFacade patientInvestigationFacade;
    @EJB
    private BillBean billBean;
    @EJB
    CommonFunctions commonFunctions;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private PatientFacade patientFacade;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private BillComponentFacade billComponentFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private AppointmentFacade appointmentFacade;
    //Temprory Variable
    //   private Patient tmpPatient;
    private String comment = "";
    //  Date sessionDate;
    private boolean printPreview;
    //  private Date chequeDate;
    private Date slipDate;
    //private PaymentScheme paymentScheme;
    private Patient newPatient;
    private Patient searchedPatient;
    //private String creditCardRefNo;
    //  private String chequeRefNo;
    private String patientTabId = "tabNewPt";
    private String ageText = "";
    private Institution chequeBank;
    private Institution slipBank;
    private Institution creditBank;
    //private BillItem currentBillItem;
    private Bill currentBill;
    private Appointment currentAppointment;

    public Title[] getTitle() {
        return Title.values();
    }

    public Sex[] getSex() {
        return Sex.values();
    }

    public String prepareLabBill() {
        //    clearBillItemValues();
        //   clearBillValues();
        currentBill=null;
        currentAppointment=null;
        setPrintPreview(true);
        printPreview = false;
        return "lab_bill";
    }

    public List<Bill> completeOpdCreditBill(String qry) {
        List<Bill> a = null;
        String sql;
        HashMap hash = new HashMap();
        if (qry != null) {
            sql = "select c from BilledBill c where c.paidAmount is null and c.billType= :btp and c.paymentScheme.paymentMethod= :pm and c.cancelledBill is null and c.refundedBill is null and c.retired=false and upper(c.insId) like '%" + qry.toUpperCase() + "%' order by c.creditCompany.name";
            hash.put("btp", BillType.OpdBill);
            hash.put("pm", PaymentMethod.Credit);
            a = getFacade().findBySQL(sql, hash, TemporalType.TIME);
        }
        if (a == null) {
            a = new ArrayList<Bill>();
        }
        return a;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    private Patient savePatient() {
        if (getPatientTabId().equals("tabNewPt")) {
            getNewPatient().setCreater(getSessionController().getLoggedUser());
            getNewPatient().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

            getNewPatient().getPerson().setCreater(getSessionController().getLoggedUser());
            getNewPatient().getPerson().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

            getPersonFacade().create(getNewPatient().getPerson());
            getPatientFacade().create(getNewPatient());
            return getNewPatient();

        } else if (getPatientTabId().equals("tabSearchPt")) {
            return getSearchedPatient();
        }

        return null;
    }
    
    private void saveAppointment(Patient p){
        getCurrentAppointment().setCreatedAt(new Date());
        getCurrentAppointment().setCreater(getSessionController().getLoggedUser());
        getCurrentAppointment().setPatient(p);
        getCurrentAppointment().setBill(getCurrentBill());
        getAppointmentFacade().create(getCurrentAppointment());
  //      currentAppointment=null;
    }

    public void settleBill() {
        if (errorCheck()) {
            return;
        }
        
        Patient p=savePatient();
        
        

        saveBill(p);
        saveAppointment(p);
        //  getBillBean().saveBillItems(b, getLstBillEntries(), getSessionController().getLoggedUser());
        // getBillBean().calculateBillItems(b, getLstBillEntries());
        //     getBills().add(b);

        UtilityController.addSuccessMessage("Bill Saved");
        printPreview = true;
    }

    private void saveBill(Patient p) {

        //getCurrentBill().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment()));
//        getCurrentBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), BillType.Appointment));
      //  getCurrentBill().setBillType(BillType.OpdBill);

        getCurrentBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getCurrentBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getCurrentBill().setPatient(p);
       // getCurrentBill().setAppointment(getCurrentAppointment());
        //     getCurrentBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        //    getCurrentBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
        if (getCurrentBill().getPaymentMethod().equals(PaymentMethod.Cheque)) {
            getCurrentBill().setBank(getChequeBank());
            //temp.setChequeRefNo(chequeRefNo);          
        }

        if (getCurrentBill().getPaymentMethod().equals(PaymentMethod.Slip)) {
            getCurrentBill().setBank(getSlipBank());
            getCurrentBill().setChequeDate(getSlipDate());
        }

        if (getCurrentBill().getPaymentMethod().equals(PaymentMethod.Card)) {
            // temp.setCreditCardRefNo(creditCardRefNo);
            getCurrentBill().setBank(creditBank);
        }
        getCurrentBill().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrentBill().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        //   getCurrentBill().setPatient(tmpPatient);
//        temp.setPatientEncounter(patientEncounter);
        //   temp.setPaymentScheme(getPaymentScheme());

        getCurrentBill().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrentBill().setCreater(sessionController.getLoggedUser());
        getFacade().create(getCurrentBill());
        //return getCurrentBill();

    }

    private boolean checkPatientAgeSex() {

        if (getPatientTabId().toString().equals("tabNewPt")) {

            if (getNewPatient().getPerson().getName() == null || getNewPatient().getPerson().getName().trim().equals("") || getNewPatient().getPerson().getSex() == null || getAgeText() == null) {
                UtilityController.addErrorMessage("Can not bill without Patient Name, Age or Sex.");
                return true;
            }

            if (!getCommonFunctions().checkAgeSex(getNewPatient().getPerson().getDob(), getNewPatient().getPerson().getSex(), getNewPatient().getPerson().getTitle())) {
                UtilityController.addErrorMessage("Check Title,Age,Sex");
                return true;
            }

            if (getNewPatient().getPerson().getPhone().length() < 1) {
                UtilityController.addErrorMessage("Phone Number is Required it should be fill");
                return true;
            }

        }

        return false;

    }

    private boolean errorCheck() {

        if (checkPatientAgeSex()) {
            return true;
        }

        if (getPatientTabId().toString().equals("tabSearchPt")) {
            if (getSearchedPatient() == null) {
                UtilityController.addErrorMessage("Plese Select Patient");
            }
        }

        if (getPatientTabId().toString().equals("tabNewPt")) {

            if (getNewPatient().getPerson().getName() == null || getNewPatient().getPerson().getName().trim().equals("")) {
                UtilityController.addErrorMessage("Can not bill without Patient Name");
                return true;
            }

        }

        if (getCurrentBill().getPaymentMethod() != null && getCurrentBill().getPaymentMethod() == PaymentMethod.Cheque) {
            if (getChequeBank() == null || getCurrentBill().getChequeRefNo() == null || getCurrentBill().getChequeDate() == null) {
                UtilityController.addErrorMessage("Please select Cheque Number,Bank and Cheque Date");
                return true;
            }

        }

        if (getCurrentBill().getPaymentMethod() != null && getCurrentBill().getPaymentMethod() == PaymentMethod.Slip) {
            if (getSlipBank() == null || getComment() == null || getSlipDate() == null) {
                UtilityController.addErrorMessage("Please Fill Memo,Bank and Slip Date ");
                return true;
            }

        }

        if (getCurrentBill().getPaymentMethod() != null && getCurrentBill().getPaymentMethod() == PaymentMethod.Card) {
            if (getCreditBank() == null || getCurrentBill().getCreditCardRefNo() == null) {
                UtilityController.addErrorMessage("Please Fill Credit Card Number and Bank");
                return true;
            }

            if (getCurrentBill().getCreditCardRefNo().trim().length() < 16) {
                UtilityController.addErrorMessage("Enter 16 Digit");
                return true;
            }

        }

//       
        return false;
    }

    public String prepareNewBill() {
        currentBill = null;

        setPrintPreview(true);
        printPreview = false;
        return "";
    }

    public void onTabChange(TabChangeEvent event) {
        setPatientTabId(event.getTab().getId());

    }

    public BillFacade getEjbFacade() {
        return billFacade;
    }

    public void setEjbFacade(BillFacade ejbFacade) {
        this.billFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public AppointmentController() {
    }

    private BillFacade getFacade() {
        return billFacade;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public List<Bill> completeAppointment(String query) {
        List<Bill> suggestions;
        String sql;
        HashMap hm = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<Bill>();
        } else {
            sql = "select p from BilledBill p where p.retired=false and "
                    + "p.cancelled=false and p.refunded=false and p.billType=:btp and (upper(p.patient.person.name)  "
                    + "like '%" + query.toUpperCase() + "%' or upper(p.insId)  "
                    + "like '%" + query.toUpperCase() + "%' ) order by p.insId";
            //System.out.println(sql);
            hm.put("btp", BillType.Appointment);
            suggestions = getFacade().findBySQL(sql, hm);
        }
        return suggestions;
    }

    public String getPatientTabId() {
        return patientTabId;
    }

    public void setPatientTabId(String patientTabId) {
        this.patientTabId = patientTabId;
    }

    public Patient getNewPatient() {
        if (newPatient == null) {
            newPatient = new Patient();
            Person p = new Person();

            newPatient.setPerson(p);
        }
        return newPatient;
    }

    public void setNewPatient(Patient newPatient) {
        this.newPatient = newPatient;
    }

    public Patient getSearchedPatient() {
        return searchedPatient;
    }

    public void setSearchedPatient(Patient searchedPatient) {
        this.searchedPatient = searchedPatient;
    }

    public Institution getChequeBank() {

        return chequeBank;
    }

    public void setChequeBank(Institution chequeBank) {
        this.chequeBank = chequeBank;
    }

    public String getAgeText() {
        ageText = getNewPatient().getAge();
        if (ageText.startsWith("0 days")) {
            return "";
        }
        return ageText;
    }

    public void setAgeText(String ageText) {
        this.ageText = ageText;
        getNewPatient().getPerson().setDob(getCommonFunctions().guessDob(ageText));
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;

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

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;

    }

    public BillComponentFacade getBillComponentFacade() {
        return billComponentFacade;
    }

    public void setBillComponentFacade(BillComponentFacade billComponentFacade) {
        this.billComponentFacade = billComponentFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public PatientInvestigationFacade getPatientInvestigationFacade() {
        return patientInvestigationFacade;
    }

    public void setPatientInvestigationFacade(PatientInvestigationFacade patientInvestigationFacade) {
        this.patientInvestigationFacade = patientInvestigationFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;

    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Institution getSlipBank() {
        return slipBank;
    }

    public void setSlipBank(Institution slipBank) {
        this.slipBank = slipBank;
    }

    public Institution getCreditBank() {
        return creditBank;
    }

    public void setCreditBank(Institution creditBank) {
        this.creditBank = creditBank;
    }

    public Bill getCurrentBill() {
        if (currentBill == null) {
            currentBill=new BilledBill();
            currentBill.setBillType(BillType.Appointment);
        }
        return currentBill;
    }

    public void setCurrentBill(Bill currentBill) {
        this.currentBill = currentBill;
    }

    public Date getSlipDate() {
        return slipDate;
    }

    public void setSlipDate(Date slipDate) {
        this.slipDate = slipDate;
    }

    public Appointment getCurrentAppointment() {
        if(currentAppointment==null){
            currentAppointment=new Appointment();
        }
        return currentAppointment;
    }

    public void setCurrentAppointment(Appointment currentAppointment) {
        this.currentAppointment = currentAppointment;
    }

    public AppointmentFacade getAppointmentFacade() {
        return appointmentFacade;
    }

    public void setAppointmentFacade(AppointmentFacade appointmentFacade) {
        this.appointmentFacade = appointmentFacade;
    }

    /**
     *
     */
    @FacesConverter(forClass = Bill.class)
    public static class BillControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AppointmentController controller = (AppointmentController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "appointmentController");
            return controller.getBillFacade().find(getKey(value));
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
            if (object instanceof Bill) {
                Bill o = (Bill) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + AppointmentController.class.getName());
            }
        }
    }
}
