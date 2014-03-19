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
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.FeeType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.ChargeItemTotal;
import com.divudi.data.dataStructure.DepartmentBillItems;
import com.divudi.data.dataStructure.InwardBillItem;
import com.divudi.data.dataStructure.RoomChargeData;
import com.divudi.data.inward.InwardChargeType;
import static com.divudi.data.inward.InwardChargeType.AdmissionFee;
import static com.divudi.data.inward.InwardChargeType.LinenCharges;
import static com.divudi.data.inward.InwardChargeType.MOCharges;
import static com.divudi.data.inward.InwardChargeType.MaintainCharges;
import static com.divudi.data.inward.InwardChargeType.Medicine;
import static com.divudi.data.inward.InwardChargeType.NursingCharges;
import static com.divudi.data.inward.InwardChargeType.OtherCharges;
import static com.divudi.data.inward.InwardChargeType.ProfessionalCharge;
import static com.divudi.data.inward.InwardChargeType.RoomCharges;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.InwardCalculation;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Department;
import com.divudi.entity.InwardPriceAdjustment;
import com.divudi.entity.PatientItem;
import com.divudi.entity.inward.Admission;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.inward.TimedItemFee;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.PatientEncounterFacade;
import com.divudi.facade.PatientItemFacade;
import com.divudi.facade.PatientRoomFacade;
import com.divudi.facade.ServiceFacade;
import com.divudi.facade.TimedItemFeeFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class BhtSummeryController implements Serializable {

    private static final long serialVersionUID = 1L;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private PatientRoomFacade patientRoomFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private PatientItemFacade patientItemFacade;
    @EJB
    private TimedItemFeeFacade timedItemFeeFacade;
    @EJB
    private PatientEncounterFacade patientEncounterFacade;
    ////////////////////////////
    @EJB
    private InwardCalculation inwardCalculation;
    @EJB
    private BillNumberBean billNumberBean;
    //////////////////////////
    @Inject
    private SessionController sessionController;
    @Inject
    private InwardTimedItemController inwardTimedItemController;
    @Inject
    private DischargeController dischargeController;
    ////////////////////////
    private List<RoomChargeData> roomChargeDatas;
    private List<DepartmentBillItems> departmentBillItems;
    private List<BillFee> profesionallFee;
    private List<Bill> paymentBill;
    private List<BillItem> issues;
    List<PatientItem> patientItems;
    private List<ChargeItemTotal> chargeItemTotals;
    //////////////////////////
    private double grantTotal = 0.0;
    private double discount;
    private double due;
    private double paid;
    private PatientItem tmpPI;
    private Admission patientEncounter;
    private Bill current;
    private Date currentTime;
    private Date toTime;
    private boolean printPreview;
    @Inject
    private InwardMemberShipDiscount inwardMemberShipDiscount;

    public void changePaymentmethodListener() {
        if (getCurrent().getPaymentMethod() == null) {
            return;
        }

        for (ChargeItemTotal cit : chargeItemTotals) {
            InwardPriceAdjustment ipa = getInwardMemberShipDiscount().getMemberDisCount(getCurrent().getPaymentMethod(), getPatientEncounter().getPatient().getPerson().getMembershipScheme(), cit.getInwardChargeType());

            if (ipa == null || ipa.getDiscountPercent() == 0 || cit.getTotal() == 0) {
                cit.setDiscount(0);
                cit.setNetTotal(cit.getTotal());
                cit.setAdjustedTotal(cit.getTotal());
                continue;
            }

            double discountPercent = ipa.getDiscountPercent();
            double total = cit.getTotal();
            double dis = (total * discountPercent) / 100;
            double net = total - dis;

            cit.setDiscount(dis);
            cit.setNetTotal(net);
            cit.setAdjustedTotal(net);
        }

        calFinalValue();
    }

    public void updatePatientItem(PatientItem patientItem) {
        getInwardTimedItemController().finalizeService(patientItem);
        createPatientItems();
        createChargeItemTotals();

    }

    public void updatePatientRoom(RoomChargeData roomChargeData) {
        getPatientRoomFacade().edit(roomChargeData.getPatientRoom());
        createRoomChargeDatas();
        createChargeItemTotals();
    }

    public void checkDate() {
        if (getPatientEncounter() != null && getPatientEncounter().getDateOfAdmission().after(getPatientEncounter().getDateOfDischarge())) {
            UtilityController.addErrorMessage("Check Discharge Time should be after Admitted Time");
        }

        if (getPatientEncounter() != null && getPatientEncounter().getDateOfDischarge().after(new Date())) {
            UtilityController.addErrorMessage("Check Discharge Time can't be set after than now");
        }

        makeNull();
        createTables();
    }

    public List<BillItem> getBillItems() {
        HashMap hm = new HashMap();
        String sql = "Select b From BillItem b where b.retired=false and b.bill=:b";
        hm.put("b", getCurrent());
        return getBillItemFacade().findBySQL(sql, hm);
    }

    @Inject
    private InwardPaymentController inwardPaymentController;

    public void settle() {

        if (errorCheck()) {
            return;
        }

        if (getPatientEncounter().getCreditLimit() != 0) {
            updateCreditDetail();
        }

        Bill refBill = getInwardPaymentController().pay(getCurrent().getPaymentMethod(), patientEncounter, due);

        saveBill(refBill);
        saveBillItem();
        UtilityController.addSuccessMessage("Bill Saved");

//        if (!getPatientEncounter().isDischarged()) {
//            getDischargeController().setCurrent((Admission) getPatientEncounter());
//            getDischargeController().discharge();
//        }
        getBillFacade().edit(getCurrent());

        getPatientEncounter().setPaymentFinalized(true);
        getPatientEncounterFacade().edit(getPatientEncounter());

        printPreview = true;
    }

    private void updateCreditDetail() {
        if (getPatientEncounter().getCreditLimit() <= getNetCharge()) {
            getPatientEncounter().setCreditUsedAmount(getPatientEncounter().getCreditLimit());
        } else {
            getPatientEncounter().setCreditUsedAmount(getNetCharge());
        }

        getPatientEncounterFacade().edit(getPatientEncounter());
    }

    private boolean checkRoomIsDischarged() {
        for (RoomChargeData rcd : getRoomChargeDatas()) {
            if (rcd.getPatientRoom().getDischargedAt() == null) {
                return true;
            }
        }

        return false;
    }

    private boolean checkPatientItems() {
        for (PatientItem pi : patientItems) {
            if (pi.getFinalize() == false) {
                return true;
            }
        }

        return false;
    }

    public void dischargeCancel() {
        patientEncounter.setDischarged(false);
        patientEncounter.setDateOfDischarge(null);
        getPatientEncounterFacade().edit(patientEncounter);
    }

    public void discharge() {
        if (patientEncounter.isDischarged()) {
            UtilityController.addErrorMessage("Patient Already Discharged");
            return;
        }

        if (checkRoomIsDischarged()) {
            UtilityController.addErrorMessage("Please Discharged From Room");
            return;
        }

        if (checkPatientItems()) {
            UtilityController.addErrorMessage("Please Finalize Patient Timed Service");
            return;
        }

        getDischargeController().setCurrent((Admission) patientEncounter);
        getDischargeController().discharge();
        UtilityController.addSuccessMessage("Patient  Discharged");

        setPatientEncounter(getPatientEncounter());
        createTables();
    }

    public String dischargeLink() {
        getPatientEncounter().setDateOfDischarge(new Date());
        return "inward_discharge";
    }

    private boolean errorCheck() {
        if (getCurrent().getPaymentMethod() == null) {
            UtilityController.addErrorMessage("Select Paymentmethod");
            return true;
        }

        if (getPatientEncounter() == null) {
            return true;
        }

        if (getPatientEncounter().isPaymentFinalized()) {
            UtilityController.addErrorMessage("Payment is Finalized U need to cancel Previuios Final Bill of This Bht");
            return true;
        }

        if (getCurrent().getPaymentMethod() == PaymentMethod.Cheque) {
            if (getCurrent().getBank().getId() == null || getCurrent().getChequeRefNo() == null) {
                UtilityController.addErrorMessage("Please select Cheque Number and Bank");
                return true;
            }
        }

        if (getCurrent().getPaymentMethod() == PaymentMethod.Slip) {
            if (getCurrent().getBank().getId() == null || getCurrent().getComments() == null) {
                UtilityController.addErrorMessage("Please Fill Memo and Bank");
                return true;
            }
        }

        if (getCurrent().getPaymentMethod() == PaymentMethod.Card) {
            if (getCurrent().getBank() == null || getCurrent().getCreditCardRefNo() == null) {
                UtilityController.addErrorMessage("Please Fill Credit Card Number and Bank");
                return true;
            }
            if (getCurrent().getCreditCardRefNo().trim().length() < 16) {
                UtilityController.addErrorMessage("Enter 16 Digit");
                return true;
            }
        }

        if (getCurrent().getPaymentMethod() == PaymentMethod.Credit) {
            if (getCurrent().getCreditCompany() == null) {
                UtilityController.addErrorMessage("Please Select Credit Company");
                return true;
            }
        }

        if (checkCatTotal()) {
            return true;
        }

        return false;

    }

    private boolean checkCatTotal() {
        double tot = 0.0;
        double tot2 = 0.0;
        for (ChargeItemTotal cit : getChargeItemTotals()) {
            tot += cit.getNetTotal();
            tot2 += cit.getAdjustedTotal();
        }

        System.err.println("Total " + tot);
        System.err.println("Total 2 " + tot2);

//        if (tot != tot2) {
//            UtilityController.addErrorMessage("Please Adjust category amount correctly");
//            return true;
//        }
        return false;
    }

    public String toSettleBill() {
        if (!getPatientEncounter().isDischarged()) {
            UtilityController.addErrorMessage("Please Discharge This Patient");
            return "";
        }

        createTables();
        calFinalValue();

        return "inward_bill_final";
    }

    private void saveBill(Bill refBill) {

        getCurrent().setGrantTotal(grantTotal);
        getCurrent().setDiscount(discount);
        getCurrent().setNetTotal(netCharge);
        getCurrent().setInstitution(getSessionController().getInstitution());

        getCurrent().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), BillType.InwardFinalBill, BillNumberSuffix.INWFINAL));
        getCurrent().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getCurrent(), BillType.InwardFinalBill, BillNumberSuffix.INWFINAL));

        getCurrent().setReferenceBill(refBill);
        getCurrent().setBillType(BillType.InwardFinalBill);
        getCurrent().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setPatientEncounter(patientEncounter);
        getCurrent().setPatient(patientEncounter.getPatient());
        getCurrent().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setCreater(getSessionController().getLoggedUser());
        getBillFacade().create(getCurrent());
    }

    // private void saveAdmissionBillFee
    private void saveBillItem() {
        double temProfFee = 0;
        double temHosFee = 0.0;
        for (ChargeItemTotal cit : chargeItemTotals) {
            BillItem temBi = new BillItem();
            temBi.setBill(getCurrent());
            temBi.setInwardChargeType(cit.getInwardChargeType());
            temBi.setGrossValue(cit.getTotal());
            temBi.setDiscount(cit.getDiscount());
            temBi.setNetValue(cit.getNetTotal());
            temBi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            temBi.setCreater(getSessionController().getLoggedUser());

            if (cit.getInwardChargeType() == InwardChargeType.ProfessionalCharge) {
                //    saveProBillFee(temBi);
                temProfFee += cit.getNetTotal();
            } else {
                temHosFee += cit.getNetTotal();
            }

            if (cit.getInwardChargeType() == InwardChargeType.OtherCharges) {
                saveOtherBillFee(temBi);
            }

            getBillItemFacade().create(temBi);

            getCurrent().getBillItems().add(temBi);
        }

        getCurrent().setProfessionalFee(temProfFee);
        getCurrent().setHospitalFee(temHosFee);

        getBillFacade().edit(getCurrent());
    }

    private void saveProBillFee(BillItem bItem) {
        for (BillFee bf : getProfesionallFee()) {
            BillFee tmp = new BillFee();
            tmp.setBill(bItem.getBill());
            tmp.setBillItem(bItem);
            tmp.setReferenceBillFee(bf);

            bItem.getBillFees().add(tmp);
            getCurrent().getBillFees().add(tmp);
            // getBillFeeFacade().create(tmp);
        }

    }

    private List<BillFee> getAdditionalFee() {

        String sql = "Select bf from BillFee bf where bf.retired=false"
                + " and bf.patienEncounter=:pe and bf.bill.billType=:btp and bf.fee.feeType=:fn";
        HashMap m = new HashMap();
        m.put("btp", BillType.InwardBill);
        m.put("pe", getPatientEncounter());
        m.put("fn", FeeType.Additional);
        return getBillFeeFacade().findBySQL(sql, m, TemporalType.DATE);

        //       return bill;
    }

    private void saveOtherBillFee(BillItem bItem) {
        for (BillFee bf : getAdditionalFee()) {
            BillFee tmp = new BillFee();
            tmp.setBill(bItem.getBill());
            tmp.setBillItem(bItem);
            tmp.setReferenceBillFee(bf);

            bItem.getBillFees().add(tmp);

            // getBillFeeFacade().create(tmp);
        }

    }

//    private void savePayment() {
//        BilledBill payment = new BilledBill();
//
//        payment.setInstitution(getSessionController().getInstitution());
//
//        payment.setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), BillType.InwardPaymentBill, "inwP"));
//        payment.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), BillType.InwardPaymentBill, "inwP"));
//
//        payment.setBillType(BillType.InwardPaymentBill);
//        payment.setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//        payment.setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//        payment.setPatientEncounter(patientEncounter);
//        payment.setPatient(patientEncounter.getPatient());
//        payment.setPaymentScheme(getCurrent().getPaymentScheme());
//        payment.setNetTotal();
//        getCurrent().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//        getCurrent().setCreater(sessionController.getLoggedUser());
//        getBillFacade().create(getCurrent());
//
//
//
//        BillItem temBi = new BillItem();
//        temBi.setBill(getCurrent());
//        temBi.setGrossValue(getCurrent().getTotal());
//        temBi.setNetValue(getCurrent().getTotal());
//        temBi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//        temBi.setCreater(getSessionController().getLoggedUser());
//        getBillItemFacade().create(temBi);
//
//    }
    public void createTables() {
        createRoomChargeDatas();
        createPatientItems();
        createIssueTable();
        createDepartmentBillItems();
        createAdditionalChargeBill();
        createProfesionallFee();
        createPaymentBill();
        createChargeItemTotals();
    }

    private List<PatientItem> createPatientItems() {
        patientItems = new ArrayList<>();
        HashMap hm = new HashMap();
        String sql = "SELECT i FROM PatientItem i where Type(i.item)=TimedItem and "
                + " i.retired=false and i.patientEncounter=:pe";
        hm.put("pe", getPatientEncounter());
        patientItems = getPatientItemFacade().findBySQL(sql, hm);

        if (patientItems == null) {
            return new ArrayList<>();
        }

        for (PatientItem pi : patientItems) {
            if (pi.getFinalize() == null) {
                double serviceTot = getInwardCalculation().calTimedServiceCharge(pi, getPatientEncounter().getDateOfDischarge());
                pi.setServiceValue(serviceTot);
                pi.setTmpConsumedTime(getDuration(pi.getFromTime(), pi.getToTime()));
            }
        }

        return patientItems;
    }

    public List<PatientItem> getPatientItems() {
        if (patientItems == null) {
            patientItems = createPatientItems();
        }

        return patientItems;
    }

    public void finalizeService(PatientItem patientItem) {
        if (patientItem.getToTime() != null) {
            if (patientItem.getToTime().before(patientItem.getFromTime())) {
                UtilityController.addErrorMessage("Service Not Finalize check Service Start Time & End Time");
                return;
            }
        }

        if (patientItem.getToTime() == null) {
            patientItem.setToTime(Calendar.getInstance().getTime());
        }

        double serviceTot = getInwardCalculation().calTimedServiceCharge(patientItem, patientItem.getToTime());
        patientItem.setServiceValue(serviceTot);

        patientItem.setFinalize(Boolean.TRUE);
        getPatientItemFacade().edit(tmpPI);

        createPatientItems();

    }

    public void makeNull() {
        chargeItemTotals = null;
        grantTotal = 0.0;
        discount = 0.0;
        due = 0.0;
        paid = 0.0;
        profesionallFee = null;
        patientItems = null;
        paymentBill = null;
        departmentBillItems = null;
        printPreview = false;
        current = null;
        tmpPI = null;
        currentTime = null;
        toTime = null;
    }

    public Admission getPatientEncounter() {
        return patientEncounter;
    }

    public void setPatientEncounter(Admission patientEncounter) {
        makeNull();
        this.patientEncounter = patientEncounter;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    private List<RoomChargeData> createRoomChargeDatas() {
        roomChargeDatas = new ArrayList<>();

        HashMap hm = new HashMap();
        String sql = "SELECT pr FROM PatientRoom pr where pr.retired=false"
                + " and pr.patientEncounter=:pe order by pr.createdAt";
        hm.put("pe", getPatientEncounter());
        List<PatientRoom> tmp = getPatientRoomFacade().findBySQL(sql, hm);

        setRoomChargeData(tmp);
        // totalLinen = getInwardCalculation().calTotalLinen(tmp);

        return roomChargeDatas;
    }

    public List<PatientRoom> getBreakDownPatientRoom() {

        if (getPatientEncounter() == null) {
            return new ArrayList<>();
        }

        HashMap hm = new HashMap();
        String sql = "SELECT pr FROM PatientRoom pr where pr.retired=false"
                + " and pr.patientEncounter=:pe order by pr.createdAt";
        hm.put("pe", getPatientEncounter());
        List<PatientRoom> tmp = getPatientRoomFacade().findBySQL(sql, hm);

        for (PatientRoom pt : tmp) {
            pt.setTmpStayedTime(getDuration(pt.getAdmittedAt(), pt.getDischargedAt()));
            pt.setTmpTotalRoomCharge(getRoomCharge(pt));
        }

        return tmp;

    }

    private long getDuration(Date from, Date to) {
        if (from == null || to == null) {
            return 0l;
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(from);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(to);

        long bal = cal2.getTimeInMillis() - cal1.getTimeInMillis();
        //    cal1.setTimeInMillis(bal);
        return (bal / (1000 * 60 * 60));

    }

    private void setRoomChargeData(List<PatientRoom> tmp) {

        for (PatientRoom p : tmp) {

            RoomChargeData rcd = new RoomChargeData();
            rcd.setPatientRoom(p);
            addRoomCharge(rcd, p);
            addLinenCharge(rcd, p);
            addMaintananceCharge(rcd, p);
            addNursingCharge(rcd, p);
            addMoCharge(rcd, p);

            roomChargeDatas.add(rcd);
        }
    }

    private void addLinenCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;

        if (p.getRoomFacilityCharge() == null || p.getCurrentLinenCharge() == 0.0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double linen = p.getCurrentLinenCharge();
        //long servicedPeriod;
        if (p.getDischargedAt() != null) {
            charge = linen * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = linen * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }

        rcd.setLinenTot(charge + p.getAddedLinenCharge());
    }

    private void addMoCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;

        if (p.getRoomFacilityCharge() == null || p.getCurrentMoCharge() == 0.0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double mo = p.getCurrentMoCharge();
        //long servicedPeriod;
        if (p.getDischargedAt() != null) {
            charge = mo * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = mo * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }
        rcd.setMoChargeTot(charge);
    }

    private void addNursingCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;

        if (p.getRoomFacilityCharge() == null || p.getCurrentNursingCharge() == 0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double nursing = p.getCurrentNursingCharge();
        //long servicedPeriod;
        if (p.getDischargedAt() != null) {
            charge = nursing * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = nursing * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }
        rcd.setNursingTot(charge);
    }

    private void addRoomCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;
        System.err.println("1 " + p.getRoomFacilityCharge());
        System.err.println("2 " + p.getCurrentRoomCharge());
        if (p.getRoomFacilityCharge() == null || p.getCurrentRoomCharge() == 0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double roomCharge = p.getCurrentRoomCharge();

        rcd.setPatientRoom(p);

        if (p.getDischargedAt() != null) {
            charge = roomCharge * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = roomCharge * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }
        System.err.println("Room Charge " + roomCharge);
        System.out.println("calculated " + charge);
        rcd.setChargeTot(charge);
    }

    private double getRoomCharge(PatientRoom p) {
        double charge;
        if (p.getRoomFacilityCharge() == null || p.getCurrentRoomCharge() == 0) {
            return 0;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double roomCharge = p.getCurrentRoomCharge();
        //  System.out.println("ssssssssssssssssssssss " + roomCharge);
        //  rcd.setPatientRoom(p);

        if (p.getDischargedAt() != null) {
            charge = roomCharge * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = roomCharge * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }

        return charge;
    }

    private void addMaintananceCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;

        if (p.getRoomFacilityCharge() == null || p.getCurrentMaintananceCharge() == 0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double maintanance = p.getCurrentMaintananceCharge();
        //long servicedPeriod;
        if (p.getDischargedAt() != null) {
            charge = maintanance * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = maintanance * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }
        rcd.setMaintananceTot(charge);
    }

//    private void calTotalCharge(List<RoomChargeData> pr) {
//        double tmpC, tmpM, tmpN, tmpMo;
//        tmpN = tmpC = tmpM = tmpMo = 0.0;
//        for (RoomChargeData rcd : pr) {
//            tmpC += rcd.getChargeTot();
//            tmpM += rcd.getMaintananceTot();
//            tmpN += rcd.getNursingTot();
//            tmpMo += rcd.getMoChargeTot();
//        }
//        totalRoomCharges = tmpC;
//        totalMaintanance = tmpM;
//        totalNursing = tmpN;
//        totalMOCharge = tmpMo;
//
//
//    }
    public PatientRoomFacade getPatientRoomFacade() {
        return patientRoomFacade;
    }

    public void setPatientRoomFacade(PatientRoomFacade patientRoomFacade) {
        this.patientRoomFacade = patientRoomFacade;
    }

    @EJB
    private DepartmentFacade departmentFacade;

    private List<DepartmentBillItems> createDepartmentBillItems() {
        departmentBillItems = new ArrayList<>();

        String sql;
        HashMap hm;

        sql = "SELECT  distinct(b.bill.toDepartment) FROM BillItem b WHERE "
                + "  b.retired=false  and b.bill.billType=:btp and"
                + " Type(b.item)!=TimedItem  and b.bill.patientEncounter=:pe ";
        hm = new HashMap();
        hm.put("btp", BillType.InwardBill);
        hm.put("pe", getPatientEncounter());

        List<Department> deptList = getDepartmentFacade().findBySQL(sql, hm, TemporalType.TIME);
        hm.clear();

        for (Department dep : deptList) {
            DepartmentBillItems table = new DepartmentBillItems();
            sql = "SELECT  b FROM BillItem b WHERE b.retired=false"
                    + "  and b.bill.billType=:btp and"
                    + " Type(b.item)!=TimedItem  and "
                    + " b.bill.patientEncounter=:pe and"
                    + " b.bill.toDepartment=:dep"
                    + "  order by b.item.name ";
            hm = new HashMap();
            hm.put("btp", BillType.InwardBill);
            hm.put("pe", getPatientEncounter());
            hm.put("dep", dep);
            List<BillItem> billItems = getBillItemFacade().findBySQL(sql, hm, TemporalType.TIME);

            table.setDepartment(dep);
            table.setBillItems(billItems);

            departmentBillItems.add(table);

        }

//        calServiceTot(departmentBillItems);
        return departmentBillItems;

    }

    public void createIssueTable() {
        String sql;
        HashMap hm;
        sql = "SELECT  b FROM BillItem b WHERE b.retired=false "
                + " and b.bill.billType=:btp  "
                + " and  b.bill.patientEncounter=:pe"
                + " and type(b.bill)=:class ";
        hm = new HashMap();
        hm.put("btp", BillType.PharmacyBhtIssue);
        hm.put("class", BilledBill.class);
        hm.put("pe", getPatientEncounter());
        issues = getBillItemFacade().findBySQL(sql, hm);

    }

    public List<BillItem> getService(InwardChargeType inwardChargeType) {

        String sql = "SELECT  b FROM BillItem b WHERE b.retired=false  and b.bill.billType=:btp"
                + " and Type(b.item)!=TimedItem  and b.bill.patientEncounter=:pe "
                + " and b.bill.cancelled=false and b.item.inwardChargeType=:inw ";
        HashMap hm = new HashMap();
        hm.put("btp", BillType.InwardBill);
        hm.put("pe", getPatientEncounter());
        hm.put("inw", inwardChargeType);
        return getBillItemFacade().findBySQL(sql, hm, TemporalType.TIME);

    }

//    private double calServiceTot(List<DepartmentBillItems> sl) {
//        double temp = 0.0;
//        for (DepartmentBillItems depB : sl) {
//            for (BillItem s : depB.getBillItems()) {
//                temp += s.getNetValue();
//            }
//        }
//        return temp;
//
//    }
    public ServiceFacade getServiceFacade() {
        return serviceFacade;
    }

    public void setServiceFacade(ServiceFacade serviceFacade) {
        this.serviceFacade = serviceFacade;
    }

    private List<BillFee> createProfesionallFee() {

        HashMap hm = new HashMap();
        String sql = "SELECT bt FROM BillFee bt WHERE bt.retired=false and "
                + " bt.fee is null and "
                + " bt.bill.billType=:btp and bt.bill.patientEncounter=:pe ";
        hm.put("btp", BillType.InwardBill);
        hm.put("pe", getPatientEncounter());

        profesionallFee = getBillFeeFacade().findBySQL(sql, hm, TemporalType.TIME);
        //System.out.println("Size : " + profesionallFee.size());

        if (profesionallFee == null) {
            return new ArrayList<>();
        }

        return profesionallFee;
    }

    private double calculateProfessionalCharges() {

        HashMap hm = new HashMap();
        String sql = "SELECT sum(bt.feeValue) FROM BillFee bt WHERE bt.retired=false and bt.fee is null and  bt.bill.id in "
                + "(SELECT  b.id FROM Bill b WHERE b.retired=false  and b.billType=:btp and b.patientEncounter=:pe)";
        hm.put("btp", BillType.InwardBill);
        hm.put("pe", getPatientEncounter());

        double val = getBillFeeFacade().findDoubleByJpql(sql, hm, TemporalType.TIME);

        return val;
    }

    public List<BillFee> getProfesionallFee() {
        if (profesionallFee == null) {
            profesionallFee = createProfesionallFee();
        }
        return profesionallFee;
    }

    public void setProfesionallFee(List<BillFee> profesionallFee) {
        this.profesionallFee = profesionallFee;
    }

    private List<Bill> createPaymentBill() {

        HashMap hm = new HashMap();
        String sql = "SELECT  b FROM Bill b WHERE b.retired=false  and b.billType=:btp "
                + " and b.patientEncounter=:pe ";
        hm.put("btp", BillType.InwardPaymentBill);
        hm.put("pe", getPatientEncounter());
        paymentBill = getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

        if (paymentBill == null) {
            return new ArrayList<>();
        }

        return paymentBill;

    }

    public List<Bill> getPaymentBill() {
        if (paymentBill == null) {
            paymentBill = createPaymentBill();
        }
        return paymentBill;
    }

    public void setPaymentBill(List<Bill> paymentBill) {
        this.paymentBill = paymentBill;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public double getPaid() {
        return paid;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public double calTotalRoomCharges() {
        double tmp = 0;
        for (RoomChargeData rcd : getRoomChargeDatas()) {
            tmp += rcd.getChargeTot();
        }

        return tmp;
    }

    public double getTotalMaintanance() {
        double tmp = 0;
        for (RoomChargeData rcd : getRoomChargeDatas()) {
            tmp += rcd.getMaintananceTot();
        }

        return tmp;
    }

    public double getTotalLinen() {
        double tmp = 0;
        for (RoomChargeData rcd : getRoomChargeDatas()) {
            tmp += rcd.getLinenTot();
        }
        return tmp;
    }

    public void calFinalValue() {
        grantTotal = 0;
        discount = 0;
        netCharge = 0;

        for (ChargeItemTotal c : getChargeItemTotals()) {
            grantTotal += c.getTotal();
            discount += c.getDiscount();
            netCharge += c.getNetTotal();
        }
    }

    public double getGrantTotal() {
        return grantTotal;
    }
    private double netCharge = 0.0;

    public void setGrantTotal(double grantTotal) {
        this.grantTotal = grantTotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getDue() {
        if (patientEncounter == null) {
            return 0.0;
        }

        due = netCharge - paid;

        if (getPatientEncounter().getCreditLimit() != 0) {
            due -= getPatientEncounter().getCreditLimit();

        }

        return due;
    }

    public void setDue(double due) {
        this.due = due;
    }

    public double getTotalNursing() {
        double tmp = 0;
        for (RoomChargeData rcd : getRoomChargeDatas()) {
            tmp += rcd.getNursingTot();
        }

        return tmp;
    }

    public Date getCurrentTime() {
        currentTime = Calendar.getInstance().getTime();

        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public PatientItemFacade getPatientItemFacade() {
        return patientItemFacade;
    }

    public void setPatientItemFacade(PatientItemFacade patientItemFacade) {
        this.patientItemFacade = patientItemFacade;
    }

    public TimedItemFeeFacade getTimedItemFeeFacade() {
        return timedItemFeeFacade;
    }

    public void setTimedItemFeeFacade(TimedItemFeeFacade timedItemFeeFacade) {
        this.timedItemFeeFacade = timedItemFeeFacade;
    }

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;
    }

    private void createChargeItemTotals() {
        chargeItemTotals = new ArrayList<>();

        for (InwardChargeType i : InwardChargeType.values()) {
            ChargeItemTotal cit = new ChargeItemTotal();
            cit.setInwardChargeType(i);

            chargeItemTotals.add(cit);
        }

        if (getPatientEncounter() != null) {
            setKnownChargeTot(chargeItemTotals);
            setServiceTotCategoryWise(chargeItemTotals);
            setTimedServiceTotCategoryWise(chargeItemTotals);

        }

        for (ChargeItemTotal cit : chargeItemTotals) {
            cit.setNetTotal(cit.getTotal());
            cit.setAdjustedTotal(cit.getTotal());
        }
    }

    public List<ChargeItemTotal> getChargeItemTotals() {
        if (chargeItemTotals == null) {
            chargeItemTotals = new ArrayList<>();
        }
        return chargeItemTotals;
    }

    public void onEdit(RowEditEvent event) {
    }

//    private void setOtherCharge(List<ChargeItemTotal> tmp) {
//        double tmpTot = 0.0;
//
//        for (ChargeItemTotal cit : tmp) {
//            if (cit.getInwardChargeType() == InwardChargeType.AdmissionFee || cit.getInwardChargeType() == InwardChargeType.RoomCharges) {
//                continue;
//            }
//            tmpTot += cit.getTotal();
//        }
//
//        if (tmpTot != 0.0) {
//            String sql = "Select i From Bill i where i.retired=false and i.billType=:btp and i.patientEncounter.id=" + getPatientEncounter().getId();
//            HashMap m = new HashMap();
//            m.put("btp", BillType.InwardBill);
//            List<Bill> bill = getBillFacade().findBySQL(sql, m, TemporalType.DATE);
//
//            double dtot = 0.0;
//
//            for (Bill b : bill) {
//                dtot += b.getNetTotal();
//            }
//
//            if (tmpTot != dtot) {
//                for (ChargeItemTotal cit : tmp) {
//                    if (cit.getInwardChargeType() == InwardChargeType.OtherCharges) {
//                        cit.setTotal(dtot - tmpTot);
//                    }
//                }
//            }
//        }
//
//    }
    private List<Bill> createAdditionalChargeBill() {
        additionalChargeBill = new ArrayList<>();
        String sql = "Select i From Bill i where i.retired=false and i.billType=:btp "
                + "and i.patientEncounter=:pe and i.id in "
                + "(Select bf.bill.id from BillFee bf where bf.retired=false and bf.patienEncounter=:pe and bf.fee.feeType=:fn)";
        HashMap m = new HashMap();
        m.put("btp", BillType.InwardBill);
        m.put("pe", getPatientEncounter());
        m.put("fn", FeeType.Additional);
        additionalChargeBill = getBillFacade().findBySQL(sql, m, TemporalType.DATE);

        return additionalChargeBill;
    }

    private double calculateAdditionalChargeTotal() {
        additionalChargeBill = new ArrayList<>();
        String sql = "Select sum(i.netTotal) From Bill i where i.retired=false and i.billType=:btp "
                + "and i.patientEncounter=:pe and i.id in "
                + "(Select bf.bill.id from BillFee bf where bf.retired=false and bf.patienEncounter=:pe and bf.fee.feeType=:fn)";
        HashMap m = new HashMap();
        m.put("btp", BillType.InwardBill);
        m.put("pe", getPatientEncounter());
        m.put("fn", FeeType.Additional);
        double val = getBillFacade().findDoubleByJpql(sql, m, TemporalType.DATE);

        return val;
    }

    private List<Bill> additionalChargeBill;

    private void setKnownChargeTot(List<ChargeItemTotal> tmp) {
        for (ChargeItemTotal i : tmp) {
            switch (i.getInwardChargeType()) {
                case AdmissionFee:
                    if (getPatientEncounter().getAdmissionType() != null) {
                        i.setTotal(getPatientEncounter().getAdmissionType().getAdmissionFee());
                    }
                    break;
                case RoomCharges:
                    i.setTotal(calTotalRoomCharges());
                    break;
                case MOCharges:
                    i.setTotal(getTotalMOCharge());
                    break;
                case NursingCharges:
                    i.setTotal(getTotalNursing());
                    break;
                case LinenCharges:
                    i.setTotal(getTotalLinen());
                    break;
                case MaintainCharges:
                    i.setTotal(getTotalMaintanance());
                    break;
                case Medicine:
                    i.setTotal(calCostOfMadicine());
                    break;
                case ProfessionalCharge:
                    i.setTotal(calculateProfessionalCharges());
                    break;
                case OtherCharges:
                    i.setTotal(calculateAdditionalChargeTotal());

            }
        }
    }

    private double calCostOfMadicine() {
        String sql;
        HashMap hm;
        sql = "SELECT  sum(b.adjustedValue) FROM BillItem b WHERE b.retired=false "
                + " and b.bill.billType=:btp  "
                + " and  b.bill.patientEncounter=:pe";
        hm = new HashMap();
        hm.put("btp", BillType.PharmacyBhtIssue);
        hm.put("pe", getPatientEncounter());
        return getBillItemFacade().findDoubleByJpql(sql, hm);
    }

    private void setServiceTotCategoryWise(List<ChargeItemTotal> tmp) {
        for (DepartmentBillItems depB : getDepartmentBillItems()) {
            for (BillItem b : depB.getBillItems()) {
                for (ChargeItemTotal ch : tmp) {
                    if (b.getItem().getInwardChargeType() != null) {
                        if (b.getItem().getInwardChargeType() == ch.getInwardChargeType()) {
                            ch.setTotal(ch.getTotal() + b.getNetValue());
                            break;
                        }
                    }
                }
            }
        }
    }

    public List<InwardBillItem> getInwardBillItemByType() {
        List<InwardBillItem> inwardBillItems = new ArrayList<>();
        for (InwardChargeType i : InwardChargeType.values()) {
            InwardBillItem tmp = new InwardBillItem();
            tmp.setInwardChargeType(i);
            tmp.setBillItems(getService(i));
            inwardBillItems.add(tmp);
        }

        return inwardBillItems;

    }

    private void setTimedServiceTotCategoryWise(List<ChargeItemTotal> tmp) {
        for (PatientItem b : getPatientItems()) {
            for (ChargeItemTotal ch : tmp) {
                if (b.getItem().getInwardChargeType() != null) {
                    if (b.getItem().getInwardChargeType() == ch.getInwardChargeType()) {
                        ch.setTotal(ch.getTotal() + b.getServiceValue());
                        break;
                    }
                }
            }
        }
    }

    public void setChargeItemTotals(List<ChargeItemTotal> chargeItemTotals) {
        this.chargeItemTotals = chargeItemTotals;
    }

    public double getTotalMOCharge() {
        double tmp = 0;
        for (RoomChargeData rcd : getRoomChargeDatas()) {
            tmp += rcd.getMoChargeTot();
        }

        return tmp;
    }

    public Bill getCurrent() {
        if (current == null) {
            current = new BilledBill();
        }
        return current;
    }

    public void setCurrent(Bill current) {
        this.current = current;
    }

    public String prepareNewBill() {
        patientEncounter = null;
        makeNull();
        return "inward_bill_intrim";
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public DischargeController getDischargeController() {
        return dischargeController;
    }

    public void setDischargeController(DischargeController dischargeController) {
        this.dischargeController = dischargeController;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public InwardTimedItemController getInwardTimedItemController() {
        return inwardTimedItemController;
    }

    public void setInwardTimedItemController(InwardTimedItemController inwardTimedItemController) {
        this.inwardTimedItemController = inwardTimedItemController;
    }

    public PatientEncounterFacade getPatientEncounterFacade() {
        return patientEncounterFacade;
    }

    public void setPatientEncounterFacade(PatientEncounterFacade patientEncounterFacade) {
        this.patientEncounterFacade = patientEncounterFacade;
    }

    public List<Bill> getAdditionalChargeBill() {
        if (additionalChargeBill == null) {
            additionalChargeBill = createAdditionalChargeBill();
        }
        return additionalChargeBill;
    }

    public void setAdditionalChargeBill(List<Bill> additionalChargeBill) {
        this.additionalChargeBill = additionalChargeBill;
    }

    public PatientItem getTmpPI() {
        return tmpPI;
    }

    public void setTmpPI(PatientItem tmpPI) {
        this.tmpPI = tmpPI;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    public List<DepartmentBillItems> getDepartmentBillItems() {
        if (departmentBillItems == null) {
            departmentBillItems = createDepartmentBillItems();
        }
        return departmentBillItems;
    }

    public void setDepartmentBillItems(List<DepartmentBillItems> departmentBillItems) {
        this.departmentBillItems = departmentBillItems;
    }

    public DepartmentFacade getDepartmentFacade() {
        return departmentFacade;
    }

    public void setDepartmentFacade(DepartmentFacade departmentFacade) {
        this.departmentFacade = departmentFacade;
    }

    public List<RoomChargeData> getRoomChargeDatas() {
        if (roomChargeDatas == null) {
            roomChargeDatas = createRoomChargeDatas();
        }
        return roomChargeDatas;
    }

    public void setRoomChargeDatas(List<RoomChargeData> roomChargeDatas) {
        this.roomChargeDatas = roomChargeDatas;
    }

    public List<BillItem> getIssues() {
        return issues;
    }

    public void setIssues(List<BillItem> issues) {
        this.issues = issues;
    }

    public InwardMemberShipDiscount getInwardMemberShipDiscount() {
        return inwardMemberShipDiscount;
    }

    public void setInwardMemberShipDiscount(InwardMemberShipDiscount inwardMemberShipDiscount) {
        this.inwardMemberShipDiscount = inwardMemberShipDiscount;
    }

    public double getNetCharge() {
        return netCharge;
    }

    public void setNetCharge(double netCharge) {
        this.netCharge = netCharge;
    }

    public InwardPaymentController getInwardPaymentController() {
        return inwardPaymentController;
    }

    public void setInwardPaymentController(InwardPaymentController inwardPaymentController) {
        this.inwardPaymentController = inwardPaymentController;
    }
}
