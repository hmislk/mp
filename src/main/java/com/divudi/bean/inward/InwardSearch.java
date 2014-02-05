/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.inward;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.bean.WebUserController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.EjbApplication;
import com.divudi.entity.Bill;
import com.divudi.entity.BillComponent;
import com.divudi.entity.BillEntry;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.RefundBill;
import com.divudi.entity.WebUser;
import com.divudi.entity.lab.PatientInvestigation;
import com.divudi.facade.BillComponentFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PatientInvestigationFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.faces.view.ViewScoped;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Buddhika
 */
@Named
@SessionScoped
public class InwardSearch implements Serializable {

    @EJB
    BillFeeFacade billFeeFacade;
    @EJB
    private BillItemFacade billItemFacede;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillComponentFacade billCommponentFacade;
    @EJB
    private PatientInvestigationFacade patientInvestigationFacade;
    /////////////////
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private BillBean billBean;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    EjbApplication ejbApplication;
    ////////////////////
    private Bill bill;
    PaymentMethod paymentMethod;
    private boolean printPreview = false;
    private double refundAmount;
    private RefundBill billForRefund;
    @Temporal(TemporalType.TIME)
    private Date fromDate;
    @Temporal(TemporalType.TIME)
    private Date toDate;
    private String comment;
    WebUser user;
    ////////////////////
    List<BillEntry> billEntrys;
    List<BillItem> billItems;
    List<BillComponent> billComponents;
    List<BillFee> billFees;
    List<BillItem> refundingItems;
    List<Bill> bills;
    private List<BillItem> tempbillItems;
    private List<Bill> filterBill;
    /////////////////////
    @Inject
    SessionController sessionController;
    @Inject
    private WebUserController webUserController;

    public void makeNull() {
        bill = null;
        paymentMethod = null;

        printPreview = false;
        refundAmount = 0.0;
        billForRefund = null;
        fromDate = null;
        toDate = null;
        comment = null;
        user = null;
        billEntrys = null;
        billItems = null;
        billComponents = null;
        billFees = null;
        refundingItems = null;
        bills = null;
        tempbillItems = null;
        filterBill = null;
    }

    public WebUser getUser() {
        return user;
    }

    public void setUser(WebUser user) {
        // recreateModel();
        this.user = user;
        recreateModel();
    }

    public EjbApplication getEjbApplication() {
        return ejbApplication;
    }

    public void setEjbApplication(EjbApplication ejbApplication) {
        this.ejbApplication = ejbApplication;
    }

    public boolean calculateRefundTotal() {
        Double d = 0.0;
        //billItems=null;
        tempbillItems = null;
        for (BillItem i : getRefundingItems()) {
            if (checkPaidIndividual(i)) {
                UtilityController.addErrorMessage("Doctor Payment Already Paid So Cant Refund Bill");
                return false;
            }

            if (i.isRefunded() == null) {
                d = d + i.getNetValue();
                getTempbillItems().add(i);
            }

        }
        refundAmount = d;
        return true;
    }

    public List<BillItem> getRefundingItems() {
        return refundingItems;
    }

    public void setRefundingItems(List<BillItem> refundingItems) {
        this.refundingItems = refundingItems;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setBillFees(List<BillFee> billFees) {
        this.billFees = billFees;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public String toReprint() {
        return "inward_bill_reprint";
    }

    public String toCancel() {
        return "inward_bill_cancel";
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    private boolean checkInvestigation(BillItem bit) {
        HashMap hm = new HashMap();
        String sql = "SELECT p FROM PatientInvestigation p where p.retired=false and p.billItem=:bi";
        hm.put("bi", bit);
        PatientInvestigation tmp = getPatientInvestigationFacade().findFirstBySQL(sql, hm);

        if (tmp.getDataEntered()) {
            return true;
        }

        return false;
    }

    public String refundBill() {
        if (refundingItems.isEmpty()) {
            UtilityController.addErrorMessage("There is no item to Refund");
            return "";

        }

        for (BillItem b : refundingItems) {
            if (checkInvestigation(b)) {
                UtilityController.addErrorMessage("Lab Report was already Entered .you cant Cancel");
                return "";
            }
        }

        if (refundAmount == 0.0) {
            UtilityController.addErrorMessage("There is no item to Refund");
            return "";
        }
        if (comment == null || comment.trim().equals("")) {
            UtilityController.addErrorMessage("Please enter a comment");
            return "";

        }

        if (getBill() != null && getBill().getId() != null && getBill().getId() != 0) {
            if (getBill().isCancelled()) {
                UtilityController.addErrorMessage("Already Cancelled. Can not Refund again");
                return "";
            }
            if (!calculateRefundTotal()) {
                return "";
            }

            RefundBill rb = new RefundBill();
            rb.setBilledBill(getBill());
            Date bd = Calendar.getInstance().getTime();
            rb.setBillDate(bd);
            rb.setBillTime(bd);
            rb.setBillType(getBill().getBillType());
            rb.setBilledBill(getBill());
            rb.setCatId(getBill().getCatId());
            rb.setCollectingCentre(getBill().getCollectingCentre());
            rb.setCreatedAt(bd);
            rb.setComments(comment);
            rb.setCreater(getSessionController().getLoggedUser());
            rb.setCreditCompany(getBill().getCreditCompany());
            rb.setDepartment(getSessionController().getLoggedUser().getDepartment());
            rb.setDiscount(0.00);
            rb.setDiscountPercent(0.0);

            rb.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getLoggedUser().getInstitution(), rb,getBill().getBillType(), BillNumberSuffix.INWREF));
            rb.setDeptId(getBillNumberBean().departmentRefundBill(getSessionController().getLoggedUser().getDepartment(), getBill().getToDepartment(), BillNumberSuffix.INWREF));

            rb.setToDepartment(getBill().getToDepartment());
            rb.setToInstitution(getBill().getToInstitution());

            rb.setFromDepartment(getBill().getFromDepartment());
            rb.setFromInstitution(getBill().getFromInstitution());

            rb.setInstitution(getSessionController().getLoggedUser().getInstitution());
            rb.setDepartment(getSessionController().getDepartment());

            rb.setNetTotal(refundAmount);
            rb.setPatient(getBill().getPatient());
            rb.setPatientEncounter(getBill().getPatientEncounter());
            rb.setPaymentMethod(paymentMethod);
            rb.setReferredBy(getBill().getReferredBy());
            rb.setTotal(0 - refundAmount);
            rb.setNetTotal(0 - refundAmount);

            getBillFacade().create(rb);

            refundBillItems(rb);

            calculateRefundBillFees(rb);

            getBill().setRefunded(true);
            getBill().setRefundedBill(rb);
            getBillFacade().edit((BilledBill) getBill());

            printPreview = true;
            //UtilityController.addSuccessMessage("Refunded");

        } else {
            UtilityController.addErrorMessage("No Bill to refund");
            return "";
        }
        //  recreateModel();
        return "";
    }

    public void calculateRefundBillFees(RefundBill rb) {
        double s = 0.0;
        double b = 0.0;
        double p = 0.0;
        for (BillItem bi : refundingItems) {
            HashMap hm = new HashMap();
            String sql = "select c from BillFee c where c.billItem=:b";
            hm.put("b", bi);
            List<BillFee> rbf = getBillFeeFacade().findBySQL(sql, hm);
            for (BillFee bf : rbf) {
                if (bf.getFee().getStaff() == null) {
                    p = p + bf.getFeeValue();
                } else {
                    s = s + bf.getFeeValue();
                }
            }

        }
        rb.setStaffFee(0 - s);
        rb.setPerformInstitutionFee(0 - p);
        getBillFacade().edit(rb);
    }

    public void refundBillItems(RefundBill rb) {
        for (BillItem bi : refundingItems) {
            //set Bill Item as Refunded

            BillItem rbi = new BillItem();
            rbi.setBill(rb);
            rbi.setCreatedAt(Calendar.getInstance().getTime());
            rbi.setCreater(getSessionController().getLoggedUser());
            rbi.setDiscount(0 - bi.getDiscount());
            rbi.setGrossValue(0 - bi.getGrossValue());
            rbi.setItem(bi.getItem());
            rbi.setNetValue(0 - bi.getNetValue());
            rbi.setQty(0 - bi.getQty());
            rbi.setRate(bi.getRate());
            rbi.setRefunded(Boolean.TRUE);
            rbi.setReferanceBillItem(bi);
            getBillItemFacede().create(rbi);

            bi.setRefunded(Boolean.TRUE);
            bi.setReferanceBillItem(rbi);
            getBillItemFacede().edit(bi);

            String sql;
            sql = "select c from BillComponent c where c.billItem.id = " + bi.getId();
            List<BillComponent> rbs = getBillCommponentFacade().findBySQL(sql);
            for (BillComponent bc : rbs) {
                BillComponent rbc = new BillComponent();
                rbc.setBill(rb);
                rbc.setBillItem(rbi);
                rbc.setCatId(bc.getCatId());
                rbc.setCreatedAt(Calendar.getInstance().getTime());
                rbc.setCreater(getSessionController().getLoggedUser());
                rbc.setDepartment(getSessionController().getLoggedUser().getDepartment());
                rbc.setInstitution(getSessionController().getLoggedUser().getInstitution());
                rbc.setItem(bc.getItem());
                rbc.setPackege(bc.getPackege());
                rbc.setSpeciality(bc.getSpeciality());
                rbc.setStaff(rb.getStaff());
                getBillCommponentFacade().create(rbc);
            }
            sql = "select c from BillFee c where c.billItem.id = " + bi.getId();
            List<BillFee> rbf = getBillFeeFacade().findBySQL(sql);
            for (BillFee bf : rbf) {
                BillFee rbc = new BillFee();
                rbc.setBill(rb);
                rbc.setBillItem(rbi);
                rbc.setCreatedAt(Calendar.getInstance().getTime());
                rbc.setCreater(getSessionController().getLoggedUser());
                rbc.setDepartment(getSessionController().getLoggedUser().getDepartment());
                rbc.setInstitution(getSessionController().getLoggedUser().getInstitution());
                rbc.setSpeciality(bf.getSpeciality());
                rbc.setFee(bf.getFee());
                rbc.setFeeValue(0 - bf.getFeeValue());
                rbc.setPatienEncounter(bf.getPatienEncounter());
                rbc.setPatient(bf.getPatient());
                rbc.setStaff(bf.getStaff());
                getBillFeeFacade().create(rbc);
            }

        }
    }

    private void recreateModel() {
        billForRefund = null;
        refundAmount = 0.0;
        billFees = null;
        billComponents = null;
        billForRefund = null;
        billItems = null;
        bills = null;
        printPreview = false;
        tempbillItems = null;
        comment = null;
    }

    private void cancelBillComponents(Bill can, BillItem bt) {
        for (BillComponent nB : getBillComponents()) {
            BillComponent bC = new BillComponent();
            bC.setCatId(nB.getCatId());
            bC.setDeptId(nB.getDeptId());
            bC.setInsId(nB.getInsId());
            bC.setDepartment(nB.getDepartment());
            bC.setDeptId(nB.getDeptId());
            bC.setInstitution(nB.getInstitution());
            bC.setItem(nB.getItem());
            bC.setName(nB.getName());
            bC.setPackege(nB.getPackege());
            bC.setSpeciality(nB.getSpeciality());
            bC.setStaff(nB.getStaff());

            bC.setBill(can);
            bC.setBillItem(bt);
            bC.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            bC.setCreater(getSessionController().getLoggedUser());
            getBillCommponentFacade().create(bC);
        }

    }

    private boolean checkPaid() {
        String sql = "SELECT bf FROM BillFee bf where bf.retired=false and bf.bill.id=" + getBill().getId();
        List<BillFee> tempFe = getBillFeeFacade().findBySQL(sql);

        for (BillFee f : tempFe) {
            if (f.getPaidValue() != 0.0) {
                return true;
            }

        }
        return false;
    }

    private boolean checkPaidIndividual(BillItem bi) {
        String sql = "SELECT bf FROM BillFee bf where bf.retired=false and bf.billItem.id=" + bi.getId();
        List<BillFee> tempFe = getBillFeeFacade().findBySQL(sql);

        for (BillFee f : tempFe) {
            if (f.getPaidValue() != 0.0) {
                return true;
            }

        }
        return false;
    }

    private boolean check() {
        if (getBill().isCancelled()) {
            UtilityController.addErrorMessage("Already Cancelled. Can not cancel again");
            return true;
        }
        if (getBill().isRefunded()) {
            UtilityController.addErrorMessage("Already Returned. Can not cancel.");
            return true;
        }

        if (checkPaid()) {
            UtilityController.addErrorMessage("Doctor Payment Already Paid So Cant Cancel Bill");
            return true;
        }
        if (getPaymentMethod() == null) {
            UtilityController.addErrorMessage("Please select a payment Method.");
            return true;
        }
        if (getComment() == null || getComment().trim().equals("")) {
            UtilityController.addErrorMessage("Please enter a comment");
            return true;
        }
        if (checkInvestigation()) {
            UtilityController.addErrorMessage("Lab Report was already Entered .you cant Cancel");
            return true;
        }

        return false;
    }

    private boolean checkInvestigation() {
        String sql = "SELECT p FROM PatientInvestigation p where p.retired=false and p.billItem.bill.id=" + getBill().getId();
        List<PatientInvestigation> tmp = getPatientInvestigationFacade().findBySQL(sql);

        for (PatientInvestigation p : tmp) {
            if (p.getDataEntered()) {
                return true;
            }
        }

        return false;
    }

    public void cancelBill() {
        if (getBill() != null && getBill().getId() != null && getBill().getId() != 0) {

            if (check()) {
                return;
            }

            CancelledBill cb = new CancelledBill();
            cb.setBilledBill(getBill());
            cb.setBalance(0.0);

            cb.setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            cb.setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            cb.setBillType(getBill().getBillType());
            cb.setCatId(getBill().getCatId());
            cb.setCollectingCentre(getBill().getCollectingCentre());
            cb.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            cb.setCreater(getSessionController().getLoggedUser());
            cb.setCreditCompany(getBill().getCreditCompany());
            cb.setStaffFee(0 - getBill().getStaffFee());
            cb.setPerformInstitutionFee(0 - getBill().getPerformInstitutionFee());
            cb.setBillerFee(0 - getBill().getBillerFee());

            cb.setPaymentMethod(paymentMethod);
            //TODO: Find null Point Exception

            cb.setToDepartment(getBill().getToDepartment());
            cb.setToInstitution(getBill().getToInstitution());

            cb.setFromDepartment(getBill().getDepartment());
            cb.setFromInstitution(getBill().getFromInstitution());

            cb.setDepartment(getSessionController().getLoggedUser().getDepartment());
            cb.setInstitution(getSessionController().getInstitution());

            cb.setDeptId(getBillNumberBean().departmentCancelledBill(getSessionController().getLoggedUser().getDepartment(), getBill().getBillType(), BillNumberSuffix.INWCAN));

            cb.setDiscount(0 - getBill().getDiscount());

            cb.setDiscountPercent(getBill().getDiscountPercent());
            cb.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getLoggedUser().getInstitution(),cb, getBill().getBillType(), BillNumberSuffix.INWCAN));

//            TODO: FIND NULL POINT EXCEPTION
            cb.setInstitution(getSessionController().getLoggedUser().getInstitution());
            cb.setNetTotal(0 - getBill().getNetTotal());
            cb.setPatient(getBill().getPatient());
            cb.setPatientEncounter(getBill().getPatientEncounter());
            cb.setComments(comment);
            cb.setPaymentMethod(paymentMethod);
            cb.setReferredBy(getBill().getReferredBy());
            cb.setReferringDepartment(getBill().getReferringDepartment());
            cb.setTotal(0 - getBill().getTotal());

            //Copy & paste
            if (webUserController.hasPrivilege("LabBillCancelling")) {
                getBillFacade().create(cb);
                cancelBillItems(cb);
                getBill().setCancelled(true);
                getBill().setCancelledBill(cb);
                getBillFacade().edit((BilledBill) getBill());
                UtilityController.addSuccessMessage("Cancelled");

                printPreview = true;
            } else {
                getEjbApplication().getBillsToCancel().add(cb);
                UtilityController.addSuccessMessage("Awaiting Cancellation");
            }

        } else {
            UtilityController.addErrorMessage("No Bill to cancel");
            return;
        }

    }
    List<Bill> billsToApproveCancellation;
    List<Bill> billsApproving;
    private CancelledBill billForCancel;

    public void approveCancellation() {

        if (billsApproving == null) {
            UtilityController.addErrorMessage("Select Bill to Approve Cancell");
            return;
        }
        for (Bill b : billsApproving) {

            b.setApproveUser(getSessionController().getCurrent());
            b.setApproveAt(Calendar.getInstance().getTime());
            getBillFacade().create(b);

            cancelBillItems(b);
            b.getBilledBill().setCancelled(true);
            b.getBilledBill().setCancelledBill(b);

            getBillFacade().edit((BilledBill) getBill());

            ejbApplication.getBillsToCancel().remove(b);

            UtilityController.addSuccessMessage("Cancelled");

        }

        billForCancel = null;
    }

    public List<Bill> getBillsToApproveCancellation() {
        //System.out.println("1");
        billsToApproveCancellation = ejbApplication.getBillsToCancel();
        return billsToApproveCancellation;
    }

    public void setBillsToApproveCancellation(List<Bill> billsToApproveCancellation) {
        this.billsToApproveCancellation = billsToApproveCancellation;
    }

    public List<Bill> getBillsApproving() {
        return billsApproving;
    }

    public void setBillsApproving(List<Bill> billsApproving) {
        this.billsApproving = billsApproving;
    }

    private void cancelBillItems(Bill can) {
        for (BillItem nB : getBillItems()) {
            BillItem b = new BillItem();
            b.setBill(can);
            b.setNetValue(-nB.getNetValue());
            b.setGrossValue(-nB.getGrossValue());
//            b.setRate(-nB.getRate());

            b.setCatId(nB.getCatId());
            b.setDeptId(nB.getDeptId());
            b.setInsId(nB.getInsId());
            b.setDiscount(nB.getDiscount());
            b.setQty(1.0);
            //   b.setRate(nB.getRate());

            b.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            b.setCreater(getSessionController().getLoggedUser());

            getBillItemFacede().create(b);

            cancelBillComponents(can, b);
            cancelBillFee(can, b);

        }
    }

    private void cancelBillFee(Bill can, BillItem bt) {
        for (BillFee nB : getBillFees()) {
            BillFee bf = new BillFee();
            bf.setFee(nB.getFee());
            bf.setFeeValue(nB.getFeeValue());
            bf.setPatienEncounter(nB.getPatienEncounter());
            bf.setPatient(nB.getPatient());
            bf.setDepartment(nB.getDepartment());
            bf.setInstitution(nB.getInstitution());
            bf.setSpeciality(nB.getSpeciality());
            bf.setStaff(nB.getStaff());

            bf.setBill(can);
            bf.setBillItem(bt);
            bf.setFeeValue(-nB.getFeeValue());

            bf.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            bf.setCreater(getSessionController().getLoggedUser());

            getBillFeeFacade().create(bf);
        }
    }

   

   

   
    

   

   

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public Bill getBill() {
        //recreateModel();
        if (bill == null) {
            bill = new BilledBill();
        }
        return bill;
    }

    public void setBill(BilledBill bill) {
        recreateModel();
        this.bill = bill;
    }

    public List<BillEntry> getBillEntrys() {
        return billEntrys;
    }

    public void setBillEntrys(List<BillEntry> billEntrys) {
        this.billEntrys = billEntrys;
    }

    public List<BillItem> getBillItems() {
        if (getBill() != null) {
            String sql = "SELECT b FROM BillItem b WHERE b.retired=false and b.bill.id=" + getBill().getId();
            billItems = getBillItemFacede().findBySQL(sql);
            if (billItems == null) {
                billItems = new ArrayList<BillItem>();
            }
        }

        return billItems;
    }

    public List<BillComponent> getBillComponents() {
        if (getBill() != null) {
            String sql = "SELECT b FROM BillComponent b WHERE b.retired=false and b.bill.id=" + getBill().getId();
            billComponents = getBillCommponentFacade().findBySQL(sql);
            if (billComponents == null) {
                billComponents = new ArrayList<BillComponent>();
            }
        }
        return billComponents;
    }

    public List<BillFee> getBillFees() {
        if (getBill() != null) {
            if (billFees == null || billForRefund == null) {
                String sql = "SELECT b FROM BillFee b WHERE b.retired=false and b.bill.id=" + getBill().getId();
                billFees = getBillFeeFacade().findBySQL(sql);
                if (billFees == null) {
                    billFees = new ArrayList<BillFee>();
                }
            }
        }

        return billFees;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public void setBillComponents(List<BillComponent> billComponents) {
        this.billComponents = billComponents;
    }

    /**
     * Creates a new instance of BillSearch
     */
    public InwardSearch() {
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public BillItemFacade getBillItemFacede() {
        return billItemFacede;
    }

    public void setBillItemFacede(BillItemFacade billItemFacede) {
        this.billItemFacede = billItemFacede;
    }

    public BillComponentFacade getBillCommponentFacade() {
        return billCommponentFacade;
    }

    public void setBillCommponentFacade(BillComponentFacade billCommponentFacade) {
        this.billCommponentFacade = billCommponentFacade;
    }

    private void setRefundAttribute() {
        billForRefund.setBalance(getBill().getBalance());

        billForRefund.setBillDate(Calendar.getInstance().getTime());
        billForRefund.setBillTime(Calendar.getInstance().getTime());
        billForRefund.setCreater(getSessionController().getLoggedUser());
        billForRefund.setCreatedAt(Calendar.getInstance().getTime());

        billForRefund.setBillType(getBill().getBillType());
        billForRefund.setBilledBill(getBill());

        billForRefund.setCatId(getBill().getCatId());
        billForRefund.setCollectingCentre(getBill().getCollectingCentre());
        billForRefund.setCreditCardRefNo(getBill().getCreditCardRefNo());
        billForRefund.setCreditCompany(getBill().getCreditCompany());

        billForRefund.setDepartment(getBill().getDepartment());
        billForRefund.setDeptId(getBill().getDeptId());
        billForRefund.setDiscount(getBill().getDiscount());

        billForRefund.setDiscountPercent(getBill().getDiscountPercent());
        billForRefund.setFromDepartment(getBill().getFromDepartment());
        billForRefund.setFromInstitution(getBill().getFromInstitution());
        billForRefund.setFromStaff(getBill().getFromStaff());

        billForRefund.setInsId(getBill().getInsId());
        billForRefund.setInstitution(getBill().getInstitution());

        billForRefund.setPatient(getBill().getPatient());
        billForRefund.setPatientEncounter(getBill().getPatientEncounter());
        billForRefund.setPaymentScheme(getBill().getPaymentScheme());
        billForRefund.setPaymentSchemeInstitution(getBill().getPaymentSchemeInstitution());

        billForRefund.setReferredBy(getBill().getReferredBy());
        billForRefund.setReferringDepartment(getBill().getReferringDepartment());

        billForRefund.setStaff(getBill().getStaff());

        billForRefund.setToDepartment(getBill().getToDepartment());
        billForRefund.setToInstitution(getBill().getToInstitution());
        billForRefund.setToStaff(getBill().getToStaff());
        billForRefund.setTotal(calTot());
        //Need To Add Net Total Logic
        billForRefund.setNetTotal(billForRefund.getTotal());
    }

    public double calTot() {
        if (getBillFees() == null) {
            return 0.0;
        }
        double tot = 0.0;
        for (BillFee f : getBillFees()) {
            //System.out.println("Tot" + f.getFeeValue());
            tot += f.getFeeValue();
        }
        getBillForRefund().setTotal(tot);
        return tot;
    }

    public RefundBill getBillForRefund() {

        if (billForRefund == null) {
            billForRefund = new RefundBill();
            setRefundAttribute();
        }

        return billForRefund;
    }

    public void setBillForRefund(RefundBill billForRefund) {
        this.billForRefund = billForRefund;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
    }

    public CancelledBill getBillForCancel() {
        return billForCancel;
    }

    public void setBillForCancel(CancelledBill billForCancel) {
        this.billForCancel = billForCancel;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public List<BillItem> getTempbillItems() {
        if (tempbillItems == null) {
            tempbillItems = new ArrayList<BillItem>();
        }
        return tempbillItems;
    }

    public void setTempbillItems(List<BillItem> tempbillItems) {
        this.tempbillItems = tempbillItems;
    }

    public void resetLists() {
        recreateModel();
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        bills = null;
        filterBill = null;
        this.toDate = toDate;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        bills = null;
        filterBill = null;
        this.fromDate = fromDate;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public PatientInvestigationFacade getPatientInvestigationFacade() {
        return patientInvestigationFacade;
    }

    public void setPatientInvestigationFacade(PatientInvestigationFacade patientInvestigationFacade) {
        this.patientInvestigationFacade = patientInvestigationFacade;
    }

    public List<Bill> getFilterBill() {
        return filterBill;
    }

    public void setFilterBill(List<Bill> filterBill) {
        this.filterBill = filterBill;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }
}
