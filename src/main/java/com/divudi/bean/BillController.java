/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean;

import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.DepartmentType;
import com.divudi.data.InstitutionType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.Title;
import com.divudi.data.dataStructure.PaymentMethodData;
import com.divudi.data.dataStructure.YearMonthDay;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CashTransactionBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.ServiceSessionBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillComponent;
import com.divudi.entity.BillEntry;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BillSession;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Department;
import com.divudi.entity.Doctor;
import com.divudi.entity.Institution;
import com.divudi.entity.Patient;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.Person;
import com.divudi.entity.Staff;
import com.divudi.entity.WebUser;

import com.divudi.facade.BatchBillFacade;
import com.divudi.facade.BillComponentFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.BillSessionFacade;
import com.divudi.facade.InstitutionFacade;
import com.divudi.facade.PatientFacade;

import com.divudi.facade.PersonFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
public class BillController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @Inject
    PaymentSchemeController paymentSchemeController;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    @Inject
    private EnumController enumController;
    private boolean printPreview;
    private String patientTabId = "tabNewPt";
    //Interface Data
    private PaymentScheme paymentScheme;
    private Patient newPatient;
    private Patient searchedPatient;
    private Doctor referredBy;
    private Institution creditCompany;
    private Staff staff;
    private double total;
    private double discount;
    private double netTotal;
    private double cashPaid;
    private double cashBalance;
    private BillItem currentBillItem;
    //Bill Items
    private List<BillComponent> lstBillComponents;
    private List<BillFee> lstBillFees;
    private List<BillItem> lstBillItems;
    private List<BillEntry> lstBillEntries;
    private Integer index;
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
    //Temprory Variable
    private Patient tmpPatient;
    List<Bill> bills;
    boolean foreigner = false;
    Date sessionDate;
    String strTenderedValue;
    private YearMonthDay yearMonthDay;
    private PaymentMethodData paymentMethodData;
    @EJB
    private CashTransactionBean cashTransactionBean;

    public void searchPatientListener() {
        //System.err.println("1");
        createPaymentSchemeItems();
        calTotals();
    }

    private void createPaymentSchemeItems() {
        getPaymentSchemeController().createList();
        if (!getPaymentSchemeController().getList().isEmpty()) {
            setPaymentScheme(getPaymentSchemeController().getList().get(0));
        }
    }

    public boolean findByFilter(String property, String value) {
        String sql = "Select b From Bill b where b.retired=false and upper(b." + property + ") like '%" + value.toUpperCase() + " %'";
        Bill b = getBillFacade().findFirstBySQL(sql);
        ////System.err.println("SQL " + sql);
        ////System.err.println("Bill " + b);
        if (b != null) {
            return true;
        } else {
            return false;
        }
    }

    public void feeChangeListener(BillFee bf) {
        lstBillItems = null;
        getLstBillItems();
        bf.setTmpChangedValue(bf.getFeeValue());
        calTotals();
    }

    public String getStrTenderedValue() {
        return strTenderedValue;
    }

    public void setStrTenderedValue(String strTenderedValue) {
        this.strTenderedValue = strTenderedValue;
        try {
            cashPaid = Double.parseDouble(strTenderedValue);
        } catch (NumberFormatException e) {
            ////System.out.println("Error in converting tendered value. \n " + e.getMessage());
        }
    }

    public Title[] getTitle() {
        return Title.values();
    }

    public Sex[] getSex() {
        return Sex.values();
    }

    public List<Bill> completeOpdCreditBill(String qry) {
        List<Bill> a = null;
        String sql;
        HashMap hash = new HashMap();
        if (qry != null) {
            sql = "select c from BilledBill c "
                    + " where abs(c.netTotal)-abs(c.paidAmount)>:val "
                    + " and c.billType= :btp "
                    + " and c.paymentMethod= :pm "
                    + " and c.cancelledBill is null "
                    + " and c.refundedBill is null "
                    + " and c.retired=false "
                    + " and (upper(c.insId) like :q or"
                    + " upper(c.patient.person.name) like :q "
                    + " or upper(c.creditCompany.name) like :q ) "
                    + " order by c.creditCompany.name";
            hash.put("btp", BillType.OpdBill);
            hash.put("pm", PaymentMethod.Credit);
            hash.put("val", 0.1);
            hash.put("q", "%" + qry.toUpperCase() + "%");
            a = getFacade().findBySQL(sql, hash);
        }
        if (a == null) {
            a = new ArrayList<>();
        }
        return a;
    }

    public List<Bill> completeBillFromDealor(String qry) {
        List<Bill> a = null;
        String sql;
        HashMap hash = new HashMap();
        if (qry != null) {
            sql = "select c from BilledBill c "
                    + "where  abs(c.netTotal)-abs(c.paidAmount)>:val "
                    + " and (c.billType= :btp1 or c.billType= :btp2  )"
                    + " and c.createdAt is not null "
                    + " and c.deptId is not null "
                    + " and c.cancelledBill is null "
                    + " and c.retired=false "
                    + " and c.paymentMethod=:pm  "
                    + " and c.fromInstitution.institutionType=:insTp  "
                    + " and ((upper(c.deptId) like :q ) "
                    + " or (upper(c.fromInstitution.name)  like :q ))"
                    + " order by c.fromInstitution.name";
            hash.put("btp1", BillType.PharmacyGrnBill);
            hash.put("btp2", BillType.PharmacyPurchaseBill);
            hash.put("pm", PaymentMethod.Credit);
            hash.put("insTp", InstitutionType.Dealer);
            hash.put("val", 0.1);
            hash.put("q", "%" + qry.toUpperCase() + "%");
            //     hash.put("pm", PaymentMethod.Credit);
            a = getFacade().findBySQL(sql, hash, 10);
        }
        if (a == null) {
            a = new ArrayList<>();
        }
        return a;
    }

    public List<Bill> completeBillFromDealorStore(String qry) {
        List<Bill> a = null;
        String sql;
        HashMap hash = new HashMap();
        if (qry != null) {
            sql = "select c from BilledBill c "
                    + "where  abs(c.netTotal)-abs(c.paidAmount)>:val "
                    + " and (c.billType= :btp1 or c.billType= :btp2  )"
                    + " and c.createdAt is not null "
                    + " and c.deptId is not null "
                    + " and c.cancelledBill is null "
                    + " and c.retired=false "
                    + " and c.paymentMethod=:pm  "
                    + " and c.fromInstitution.institutionType=:insTp  "
                    + " and ((upper(c.deptId) like :q ) "
                    + " or (upper(c.fromInstitution.name)  like :q ))"
                    + " order by c.fromInstitution.name";
            hash.put("btp1", BillType.PharmacyGrnBill);
            hash.put("btp2", BillType.PharmacyPurchaseBill);
            hash.put("pm", PaymentMethod.Credit);
            hash.put("insTp", InstitutionType.StoreDealor);
            hash.put("val", 0.1);
            hash.put("q", "%" + qry.toUpperCase() + "%");
            //     hash.put("pm", PaymentMethod.Credit);
            a = getFacade().findBySQL(sql, hash, 10);
        }
        if (a == null) {
            a = new ArrayList<>();
        }
        return a;
    }

    public List<Bill> completeSurgeryBills(String qry) {

        String sql;
        Map temMap = new HashMap();
        sql = "select b from Bill b "
                + " where b.billType = :billType "
                + " and b.retired=false "
                + " and b.patientEncounter.paymentFinalized=false ";

        sql += " and  ((upper(b.patientEncounter.patient.person.name) like :q )";
        sql += " or  (upper(b.patientEncounter.bhtNo) like :q )";
        sql += " or  (upper(b.insId) like :q )";
        sql += " or  (upper(b.procedure.item.name) like :q ))";
        sql += " order by b.insId desc  ";

        temMap.put("billType", BillType.SurgeryBill);
        temMap.put("q", "%" + qry.toUpperCase() + "%");
        List<Bill> tmps = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 20);

        return tmps;
    }

    public List<Bill> getDealorBills(Institution institution) {
        String sql;
        HashMap hash = new HashMap();

        sql = "select c from BilledBill c where "
                + " abs(c.netTotal)-abs(c.paidAmount)>:val"
                + " and (c.billType= :btp1 or c.billType= :btp2 )"
                + " and c.createdAt is not null "
                + " and c.deptId is not null "
                + " and c.cancelled=false"
                + " and c.retired=false"
                + " and c.paymentMethod=:pm  "
                + " and c.fromInstitution=:ins "
                + " order by c.id ";
        hash.put("btp1", BillType.PharmacyGrnBill);
        hash.put("btp2", BillType.PharmacyPurchaseBill);
        hash.put("pm", PaymentMethod.Credit);
        hash.put("val", 0.1);
        hash.put("ins", institution);
        //     hash.put("pm", PaymentMethod.Credit);
        List<Bill> bill = getFacade().findBySQL(sql, hash);

        if (bill == null) {
            bill = new ArrayList<>();
        }

        return bill;
    }

    public List<Bill> getCreditBills(Institution institution) {
        String sql;
        HashMap hash = new HashMap();

        sql = "select c from BilledBill c  where"
                + " abs(c.netTotal)-abs(c.paidAmount)>:val "
                + " and c.billType= :btp"
                + " and c.createdAt is not null "
                + " and c.deptId is not null "
                + " and c.cancelled=false"
                + " and c.retired=false"
                + " and c.paymentMethod=:pm  "
                + " and c.creditCompany=:ins "
                + " order by c.id ";
        hash.put("btp", BillType.OpdBill);
        hash.put("pm", PaymentMethod.Credit);
        hash.put("val", 0.1);
        hash.put("ins", institution);
        //     hash.put("pm", PaymentMethod.Credit);
        List<Bill> bill = getFacade().findBySQL(sql, hash);

        if (bill == null) {
            bill = new ArrayList<>();
        }

        return bill;
    }

    public List<Bill> getBills(Date fromDate, Date toDate, BillType billType1, BillType billType2, Institution institution) {
        String sql;
        HashMap hm = new HashMap();
        sql = "Select b From Bill b where"
                + "  b.retired=false"
                + "  and b.createdAt between :frm and :to"
                + " and (b.fromInstitution=:ins "
                + " or b.toInstitution=:ins) "
                + " and (b.billType=:tp1"
                + " or b.billType=:tp2)";
        hm.put("frm", fromDate);
        hm.put("to", toDate);
        hm.put("ins", institution);
        hm.put("tp1", billType1);
        hm.put("tp2", billType2);
        return getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public Date getSessionDate() {
        if (sessionDate == null) {
            sessionDate = Calendar.getInstance().getTime();
        }
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    public boolean isForeigner() {
        return foreigner;
    }

    public void setForeigner(boolean foreigner) {
        this.foreigner = foreigner;
    }

    public List<Bill> getBills() {
        if (bills == null) {
            bills = new ArrayList<>();
        }
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    private void savePatient() {
        switch (getPatientTabId()) {
            case "tabNewPt":
                getNewPatient().setCreater(getSessionController().getLoggedUser());
                getNewPatient().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
                getNewPatient().getPerson().setCreater(getSessionController().getLoggedUser());
                getNewPatient().getPerson().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
                getPersonFacade().create(getNewPatient().getPerson());
                getPatientFacade().create(getNewPatient());
                tmpPatient = getNewPatient();
                break;
            case "tabSearchPt":
                tmpPatient = getSearchedPatient();
                break;
        }
    }

    public void putToBills() {
        bills = new ArrayList<>();
        Set<Department> billDepts = new HashSet<>();
        for (BillEntry e : lstBillEntries) {
            billDepts.add(e.getBillItem().getItem().getDepartment());
        }

        for (Department d : billDepts) {
            BilledBill myBill = new BilledBill();
            saveBill(d, myBill);

            List<BillEntry> tmp = new ArrayList<>();

            for (BillEntry e : lstBillEntries) {
                if (Objects.equals(e.getBillItem().getItem().getDepartment().getId(), d.getId())) {
                    BillItem bi = getBillBean().saveBillItem(myBill, e, getSessionController().getLoggedUser());
                    //getBillBean().calculateBillItem(myBill, e);
                    myBill.getBillItems().add(bi);
                    tmp.add(e);
                }
            }

            getBillFacade().edit(myBill);

            getBillBean().calculateBillItems(myBill, tmp);
            bills.add(myBill);
        }
    }

    public void settleBill() {
        if (errorCheck()) {
            return;
        }

        savePatient();

        if (getBillBean().checkDepartment(getLstBillEntries()) == 1) {
            BilledBill temp = new BilledBill();
            Bill b = saveBill(lstBillEntries.get(0).getBillItem().getItem().getDepartment(), temp);
            b.setBillItems(getBillBean().saveBillItems(b, getLstBillEntries(), getSessionController().getLoggedUser()));

            getBillFacade().edit(b);
            getBillBean().calculateBillItems(b, getLstBillEntries());
            getBills().add(b);

        } else {
            putToBills();
        }

        saveBatchBill();
        saveBillItemSessions();

        UtilityController.addSuccessMessage("Bill Saved");
        printPreview = true;
    }

    private void saveBillItemSessions() {
        for (BillEntry be : lstBillEntries) {
            be.getBillItem().setBillSession(getServiceSessionBean().createBillSession(be.getBillItem()));

            if (be.getBillItem().getBillSession() != null) {
                getBillSessionFacade().create(be.getBillItem().getBillSession());

            }

        }
    }
    @EJB
    private BatchBillFacade batchBillFacade;

    private void saveBatchBill() {
        Bill tmp = new BilledBill();
        tmp.setBillType(BillType.OpdBathcBill);
        tmp.setPaymentScheme(paymentScheme);
        tmp.setCreatedAt(new Date());
        tmp.setCreater(getSessionController().getLoggedUser());
        getBillFacade().create(tmp);

        double dbl = 0;
        for (Bill b : bills) {
            b.setBackwardReferenceBill(tmp);
            dbl += b.getNetTotal();
            getBillFacade().edit(b);

            tmp.getForwardReferenceBills().add(b);
        }

        tmp.setNetTotal(dbl);
        getBillFacade().edit(tmp);

        WebUser wb = getCashTransactionBean().saveBillCashInTransaction(tmp, getSessionController().getLoggedUser());
        getSessionController().setLoggedUser(wb);
    }
    @Inject
    private BillSearch billSearch;

    public void cancellAll() {
        Bill tmp = new CancelledBill();
        tmp.setCreatedAt(new Date());
        tmp.setCreater(getSessionController().getLoggedUser());
        getBillFacade().create(tmp);

        Bill billedBill = null;
        for (Bill b : bills) {
            billedBill = b.getBackwardReferenceBill();
            getBillSearch().setBill((BilledBill) b);
            getBillSearch().setPaymentScheme(b.getPaymentScheme());
            getBillSearch().setComment("Batch Cancell");
            ////System.out.println("ggg : " + getBillSearch().getComment());
            getBillSearch().cancelBill();
        }

        tmp.copy(billedBill);
        tmp.setBilledBill(billedBill);

        WebUser wb = getCashTransactionBean().saveBillCashOutTransaction(tmp, getSessionController().getLoggedUser());
        getSessionController().setLoggedUser(wb);
    }

    public void dateChangeListen() {
        getNewPatient().getPerson().setDob(getCommonFunctions().guessDob(yearMonthDay));

    }

    private Bill saveBill(Department bt, BilledBill temp) {

        temp.setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), bt, BillType.OpdBill));
        temp.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), bt, new BilledBill(), BillType.OpdBill, BillNumberSuffix.NONE));
        temp.setBillType(BillType.OpdBill);

        temp.setDepartment(getSessionController().getLoggedUser().getDepartment());
        temp.setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        temp.setToDepartment(bt);
        temp.setToInstitution(bt.getInstitution());

        temp.setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        temp.setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        temp.setStaff(staff);
        temp.setReferredBy(referredBy);
        temp.setCreditCompany(creditCompany);

        getBillBean().setPaymentMethodData(temp, getPaymentScheme().getPaymentMethod(), getPaymentMethodData());

        temp.setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        temp.setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        temp.setPatient(tmpPatient);
        temp.setPaymentScheme(getPaymentScheme());
        temp.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        temp.setCreater(getSessionController().getLoggedUser());
        getFacade().create(temp);
        return temp;

    }

    private boolean checkPatientAgeSex() {

        if (getPatientTabId().toString().equals("tabNewPt")) {

            if (getNewPatient().getPerson().getName() == null || getNewPatient().getPerson().getName().trim().equals("") || getNewPatient().getPerson().getSex() == null || getNewPatient().getPerson().getDob() == null) {
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

        if (getLstBillEntries().isEmpty()) {
            UtilityController.addErrorMessage("No investigations are added to the bill to settle");
            return true;
        }

        if (!getLstBillEntries().get(0).getBillItem().getItem().isPatientNotRequired()) {
            if (getPatientTabId().toString().equals("tabSearchPt")) {
                if (getSearchedPatient() == null) {
                    UtilityController.addErrorMessage("Plese Select Patient");
                    return true;
                }
            }

            if (getPatientTabId().toString().equals("tabNewPt")) {
                if (getNewPatient().getPerson().getName() == null
                        || getNewPatient().getPerson().getName().trim().equals("")) {
                    UtilityController.addErrorMessage("Can not bill without Patient Name");
                    return true;
                }
            }

            boolean checkAge = false;
            for (BillEntry be : getLstBillEntries()) {
                if (be.getBillItem().getItem().getDepartment().getDepartmentType() == DepartmentType.Lab) {
                    //  ////System.err.println("ttttt");
                    checkAge = true;
                    break;
                }
            }

            if (checkAge && checkPatientAgeSex()) {
                return true;
            }
        }

        if (getPaymentScheme() == null) {
            UtilityController.addErrorMessage("Select Payment Scheme");
            return true;
        }

        if (getPaymentSchemeController().errorCheckPaymentScheme(getPaymentScheme().getPaymentMethod(), getPaymentMethodData())) {
            return true;
        }

        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Credit) {
            if (getCreditCompany() == null) {
                UtilityController.addErrorMessage("Please Select Credit Company");
                return true;
            }

        }

        if (getCreditCompany() != null && (getPaymentScheme().getPaymentMethod() != PaymentMethod.Credit && getPaymentScheme().getPaymentMethod() != PaymentMethod.Cheque)) {
            UtilityController.addErrorMessage("Please Select Payment Scheme with Credit or Cheque");
            return true;
        }

        if (getPaymentSchemeController().checkPaid(paymentScheme.getPaymentMethod(), getCashPaid(), getNetTotal())) {
            return true;
        }

        return false;
    }
    List<BillSession> billSessions;
    @EJB
    BillSessionFacade billSessionFacade;
    @EJB
    ServiceSessionBean serviceSessionBean;

    public List<BillSession> getBillSessions() {
        //   ////System.out.println("getting bill sessions 1");
        if (lastBillItem != null && lastBillItem.getItem() != null) {
            billSessions = getServiceSessionBean().getBillSessions(lastBillItem.getItem(), getSessionDate());
            //     ////System.out.println("bill sessions - 2" + billSessions);
        } else {
            ////System.out.println("items null");
        }
        return billSessions;
    }

    public ServiceSessionBean getServiceSessionBean() {
        return serviceSessionBean;
    }

    public void setServiceSessionBean(ServiceSessionBean serviceSessionBean) {
        this.serviceSessionBean = serviceSessionBean;
    }

    public BillSessionFacade getBillSessionFacade() {
        return billSessionFacade;
    }

    public void setBillSessionFacade(BillSessionFacade billSessionFacade) {
        this.billSessionFacade = billSessionFacade;
    }

    public void setBillSessions(List<BillSession> billSessions) {
        this.billSessions = billSessions;
    }

    public void addToBill() {

        if (getCurrentBillItem() == null) {
            UtilityController.addErrorMessage("Nothing to add");
            return;
        }
        if (getCurrentBillItem().getItem() == null) {
            UtilityController.addErrorMessage("Please select an Item");
            return;
        }
        if (getCurrentBillItem().getItem().getTotal() == 0.0) {
            UtilityController.addErrorMessage("Please corect item fee");
            return;
        }

        if (getCurrentBillItem().getItem().getDepartment() == null) {
            UtilityController.addErrorMessage("Please set Department to Item");
            return;
        }

        if (getCurrentBillItem().getItem().getCategory() == null) {
            UtilityController.addErrorMessage("Please set Category to Item");
            return;
        }

        getCurrentBillItem().setSessionDate(sessionDate);

//        New Session
        //   getCurrentBillItem().setBillSession(getServiceSessionBean().createBillSession(getCurrentBillItem()));
        lastBillItem = getCurrentBillItem();
        BillEntry addingEntry = new BillEntry();
        addingEntry.setBillItem(getCurrentBillItem());
        addingEntry.setLstBillComponents(getBillBean().billComponentsFromBillItem(getCurrentBillItem()));
        addingEntry.setLstBillFees(getBillBean().billFeefromBillItem(getCurrentBillItem()));
        addingEntry.setLstBillSessions(getBillBean().billSessionsfromBillItem(getCurrentBillItem()));
        getLstBillEntries().add(addingEntry);
        getCurrentBillItem().setRate(getBillBean().billItemRate(addingEntry));
        getCurrentBillItem().setQty(1.0);
        getCurrentBillItem().setNetValue(getCurrentBillItem().getRate() * getCurrentBillItem().getQty()); // Price == Rate as Qty is 1 here

        calTotals();
        if (getCurrentBillItem().getNetValue() == 0.0) {
            UtilityController.addErrorMessage("Please enter the rate");
            return;
        }
        clearBillItemValues();
        //UtilityController.addSuccessMessage("Item Added");
    }

    public void clearBillItemValues() {
        currentBillItem = null;

        recreateBillItems();
    }

    private void clearBillValues() {
        setNewPatient(null);
        setSearchedPatient(null);
        setReferredBy(null);
        setSessionDate(null);
        setCreditCompany(null);
        setYearMonthDay(null);
        setBills(null);
        setPaymentScheme(null);
        paymentMethodData = null;
        currentBillItem = null;
        setLstBillComponents(null);
        setLstBillEntries(null);
        setLstBillFees(null);
        setStaff(null);
        lstBillEntries = new ArrayList<>();
        setForeigner(false);
        setSessionDate(Calendar.getInstance().getTime());
        calTotals();

        setCashPaid(0.0);
        setDiscount(0.0);
        setCashBalance(0.0);

        setStrTenderedValue("");

        patientTabId = "tabNewPt";
    }

    private void recreateBillItems() {
        //Only remove Total and BillComponenbts,Fee and Sessions. NOT bill Entries
        lstBillComponents = null;
        lstBillFees = null;
        lstBillItems = null;

        //billTotal = 0.0;
    }

    public void calTotals() {
        //System.out.println("calculating totals");
        if (paymentScheme == null) {
            return;
        }

        //System.out.println("calculating totals 222 " + paymentScheme.getName());
        double disPercent = 0.0;
        double billGross = 0.0;
        double billNet = 0.0;

        for (BillEntry be : getLstBillEntries()) {
            ////System.out.println("bill item entry");
            double entryGross = 0.0;
            double entryDis = 0.0;
            double entryNet = 0.0;
            BillItem bi = be.getBillItem();

            for (BillFee bf : be.getLstBillFees()) {

                getBillBean().setBillFees(bf, isForeigner(), getPaymentScheme(), bi.getItem());

                entryGross += bf.getFeeGrossValue();
                entryNet += bf.getFeeValue();
                entryDis += (entryGross - entryNet);
                ////System.out.println("fee net is " + bf.getFeeValue());

            }

            bi.setDiscount(entryDis);
            bi.setGrossValue(entryGross);
            bi.setNetValue(entryNet);

            //   ////System.out.println("item is " + bi.getItem().getName());
            //    ////System.out.println("item gross is " + bi.getGrossValue());
            //   ////System.out.println("item net is " + bi.getNetValue());
            //    ////System.out.println("item dis is " + bi.getDiscount());
            billGross += +entryGross;
            billNet += entryNet;
            //     billDis = billDis + entryDis;
        }
        setDiscount(billGross - billNet);
        setTotal(billGross);
        setNetTotal(billNet);

        //      ////System.out.println("bill tot is " + billGross);
    }

    public void feeChanged() {
        lstBillItems = null;
        getLstBillItems();
        calTotals();
        //  feeChanged = false;

    }

    public void markAsForeigner() {
        setForeigner(true);
        calTotals();
    }

    public void markAsLocal() {
        setForeigner(false);
        calTotals();
    }

    public void prepareNewBill() {
        clearBillItemValues();
        clearBillValues();
        setPrintPreview(true);
        printPreview = false;
        paymentMethodData = null;

        createPaymentSchemeItems();
        //  return "opd_bill";
    }

    public String prepareLabBill() {
        clearBillItemValues();
        clearBillValues();
        setPrintPreview(true);
        printPreview = false;
        return "lab_bill";
    }

    public void makeNull() {
        clearBillItemValues();
        clearBillValues();
        printPreview = false;

    }

    public void removeBillItem() {

        //TODO: Need to add Logic
        ////System.out.println(getIndex());
        if (getIndex() != null) {
            //  boolean remove;
            BillEntry temp = getLstBillEntries().get(getIndex());
            ////System.out.println("Removed Item:" + temp.getBillItem().getNetValue());
            recreateList(temp);
            // remove = getLstBillEntries().remove(getIndex());

            //  getLstBillEntries().remove(index);
            //////System.out.println("Is Removed:" + remove);
            calTotals();

        }

    }

    public void recreateList(BillEntry r) {
        List<BillEntry> temp = new ArrayList<>();
        for (BillEntry b : getLstBillEntries()) {
            if (b.getBillItem().getItem() != r.getBillItem().getItem()) {
                temp.add(b);
                ////System.out.println(b.getBillItem().getNetValue());
            }
        }
        lstBillEntries = temp;
        lstBillComponents = getBillBean().billComponentsFromBillEntries(lstBillEntries);
        lstBillFees = getBillBean().billFeesFromBillEntries(lstBillEntries);
    }

    public void onTabChange(TabChangeEvent event) {
        setPatientTabId(event.getTab().getId());

        if (!getPatientTabId().toString().equals("tabSearchPt")) {
            setSearchedPatient(null);
        }

        createPaymentSchemeItems();
        calTotals();
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

    public BillController() {
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

    public PaymentScheme getPaymentScheme() {
        if (paymentScheme == null) {
            paymentScheme = new PaymentScheme();
        }
        return paymentScheme;
    }

    public void setPaymentScheme(PaymentScheme paymentScheme) {
        this.paymentScheme = paymentScheme;
        getEnumController().setPaymentScheme(paymentScheme);
        calTotals();
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

    public Doctor getReferredBy() {

        return referredBy;
    }

    public void setReferredBy(Doctor referredBy) {
        this.referredBy = referredBy;
    }

    public Institution getCreditCompany() {

        return creditCompany;
    }

    public void setCreditCompany(Institution creditCompany) {
        this.creditCompany = creditCompany;

    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public List<BillComponent> getLstBillComponents() {
        if (lstBillComponents == null) {
            lstBillComponents = getBillBean().billComponentsFromBillEntries(getLstBillEntries());
        }

        return lstBillComponents;
    }

    public void setLstBillComponents(List<BillComponent> lstBillComponents) {
        this.lstBillComponents = lstBillComponents;
    }

    public List<BillFee> getLstBillFees() {
        if (lstBillFees == null) {
            lstBillFees = getBillBean().billFeesFromBillEntries(getLstBillEntries());
        }

        return lstBillFees;
    }

    public void setLstBillFees(List<BillFee> lstBillFees) {
        this.lstBillFees = lstBillFees;
    }

    public List<BillItem> getLstBillItems() {
        if (lstBillItems == null) {
            lstBillItems = new ArrayList<>();
        }
        return lstBillItems;
    }

    public void setLstBillItems(List<BillItem> lstBillItems) {
        this.lstBillItems = lstBillItems;
    }

    public List<BillEntry> getLstBillEntries() {
        if (lstBillEntries == null) {
            lstBillEntries = new ArrayList<>();
        }
        return lstBillEntries;
    }

    public void setLstBillEntries(List<BillEntry> lstBillEntries) {
        this.lstBillEntries = lstBillEntries;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public double getCashPaid() {
        return cashPaid;
    }

    public void setCashPaid(double cashPaid) {
        this.cashPaid = cashPaid;
//        cashBalance = cashPaid - getNetTotal();
    }

    public double getCashBalance() {
        cashBalance = cashPaid - netTotal;
        return cashBalance;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
    }

    public BillItem getCurrentBillItem() {
        if (currentBillItem == null) {
            currentBillItem = new BillItem();
        }

        return currentBillItem;
    }
    BillItem lastBillItem;

    public BillItem getLastBillItem() {
        return lastBillItem;
    }

    public void setLastBillItem(BillItem lastBillItem) {
        this.lastBillItem = lastBillItem;
    }

    public void setCurrentBillItem(BillItem currentBillItem) {
        this.currentBillItem = currentBillItem;
        lastBillItem = currentBillItem;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
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

    private Patient getTmpPatient() {
        return tmpPatient;
    }

    public void setTmpPatient(Patient tmpPatient) {
        this.tmpPatient = tmpPatient;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;

    }

    public BatchBillFacade getBatchBillFacade() {
        return batchBillFacade;
    }

    public void setBatchBillFacade(BatchBillFacade batchBillFacade) {
        this.batchBillFacade = batchBillFacade;
    }

    public BillSearch getBillSearch() {
        return billSearch;
    }

    public void setBillSearch(BillSearch billSearch) {
        this.billSearch = billSearch;
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

    public EnumController getEnumController() {
        return enumController;
    }

    public void setEnumController(EnumController enumController) {
        this.enumController = enumController;
    }

    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

    public void setInstitutionFacade(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
    }

    public PaymentMethodData getPaymentMethodData() {
        if (paymentMethodData == null) {
            paymentMethodData = new PaymentMethodData();
        }
        return paymentMethodData;
    }

    public void setPaymentMethodData(PaymentMethodData paymentMethodData) {
        this.paymentMethodData = paymentMethodData;
    }

    public PaymentSchemeController getPaymentSchemeController() {
        return paymentSchemeController;
    }

    public void setPaymentSchemeController(PaymentSchemeController paymentSchemeController) {
        this.paymentSchemeController = paymentSchemeController;
    }

    public List<Bill> completeAppointmentBill(String query) {
        List<Bill> suggestions;
        String sql;
        HashMap hm = new HashMap();

        sql = "select p from BilledBill p where p.retired=false and "
                + "p.cancelled=false and p.refunded=false and p.billType=:btp "
                + " and (upper(p.patient.person.name)  "
                + "like :q or upper(p.insId)  "
                + "like :q) order by p.insId";
        ////System.out.println(sql);
        hm.put("q", "%" + query.toUpperCase() + "%");
        hm.put("btp", BillType.InwardAppointmentBill);
        suggestions = getFacade().findBySQL(sql, hm);

        return suggestions;

    }


    public CashTransactionBean getCashTransactionBean() {
        return cashTransactionBean;
    }

    public void setCashTransactionBean(CashTransactionBean cashTransactionBean) {
        this.cashTransactionBean = cashTransactionBean;
    }

    /**
     *
     */
    @FacesConverter("bill")
    public static class BillControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BillController controller = (BillController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "billController");
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
                        + object.getClass().getName() + "; expected type: " + BillController.class.getName());
            }
        }
    }

    @FacesConverter(forClass = Bill.class)
    public static class BillConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BillController controller = (BillController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "billController");
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
                        + object.getClass().getName() + "; expected type: " + BillController.class.getName());
            }
        }
    }
}
