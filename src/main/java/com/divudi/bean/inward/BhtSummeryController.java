/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.inward;

import com.divudi.bean.memberShip.InwardMemberShipDiscount;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.FeeType;
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
import static com.divudi.data.inward.InwardChargeType.ProfessionalCharge;
import static com.divudi.data.inward.InwardChargeType.RoomCharges;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.InwardBean;
import com.divudi.ejb.PriceMatrixBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.PriceMatrix;
import com.divudi.entity.Item;
import com.divudi.entity.PatientItem;
import com.divudi.entity.PreBill;
import com.divudi.entity.RefundBill;
import com.divudi.entity.inward.Admission;
import com.divudi.entity.inward.GuardianRoom;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.inward.TimedItem;
import com.divudi.entity.inward.TimedItemFee;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.ItemFacade;
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
    private BillNumberBean billNumberBean;
    @EJB
    PriceMatrixBean priceMatrixBean;
    //////////////////////////
    @Inject
    private SessionController sessionController;
    @Inject
    private InwardTimedItemController inwardTimedItemController;
    @Inject
    private DischargeController dischargeController;
    ////////////////////////
    private List<RoomChargeData> roomChargeDatas;
    List<RoomChargeData> guadianRoomChargeDatas;
    private List<DepartmentBillItems> departmentBillItems;
    private List<BillFee> profesionallFee;
    private List<Bill> paymentBill;
    private List<Bill> surgeryBill;
    private List<Bill> pharmacyIssues;
    List<Bill> storeIssues;
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
    private Item item;

    public List<RoomChargeData> getGuadianRoomChargeDatas() {
        if (guadianRoomChargeDatas == null) {
            guadianRoomChargeDatas = createGuardianRoomChargeDatas();
        }
        return guadianRoomChargeDatas;
    }

    public void setGuadianRoomChargeDatas(List<RoomChargeData> guadianRoomChargeDatas) {
        this.guadianRoomChargeDatas = guadianRoomChargeDatas;
    }

    public PriceMatrixBean getPriceMatrixBean() {
        return priceMatrixBean;
    }

    public void setPriceMatrixBean(PriceMatrixBean priceMatrixBean) {
        this.priceMatrixBean = priceMatrixBean;
    }

    public void createBillItems(Item item) {
        String sql = "SELECT  b FROM BillItem b "
                + " WHERE b.retired=false "
                + " and b.bill.billType=:btp "
                + " and b.item=:itm"
                + " and b.bill.patientEncounter=:pe ";
        HashMap hm = new HashMap();
        hm.put("btp", BillType.InwardBill);
        hm.put("pe", getPatientEncounter());
        hm.put("itm", item);
        billItems = getBillItemFacade().findBySQL(sql, hm, TemporalType.TIME);
    }

    public void changeDiscountListener(ChargeItemTotal chargeItemTotal) {
        double net = chargeItemTotal.getTotal() - chargeItemTotal.getDiscount();
        chargeItemTotal.setNetTotal(net);
    }

    public void calculateDiscount() {
        for (ChargeItemTotal cit : chargeItemTotals) {
            PriceMatrix ipa = getPriceMatrixBean().getInwardMemberDisCount(getPatientEncounter().getPaymentMethod(), getPatientEncounter().getPatient().getPerson().getMembershipScheme(), getPatientEncounter().getCreditCompany(), cit.getInwardChargeType());

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

    public void updatePatientRoom(PatientRoom patientRoom) {

        if (patientRoom.getId() != null) {
            getPatientRoomFacade().edit(patientRoom);
        } else {
            getPatientRoomFacade().create(patientRoom);
        }

        createRoomChargeDatas();
        createGuardianRoomChargeDatas();
        createChargeItemTotals();

        updateTotal();
    }

    public void updatePrintingPatientRoom(PatientRoom patientRoom) {
        if (patientRoom.getId() != null) {
            getPatientRoomFacade().edit(patientRoom);
        } else {
            getPatientRoomFacade().create(patientRoom);
        }

        patientRoom.setCurrentRoomCharge(patientRoom.getRoomFacilityCharge().getRoomCharge());

        calCulateRoomCharge(patientRoom);

        updatePaitentRoomAdjustedTotal();
    }

    private void updatePaitentRoomAdjustedTotal() {
        for (ChargeItemTotal cit : chargeItemTotals) {
            if (cit.getInwardChargeType() == InwardChargeType.RoomCharges) {
                double dbl = 0;
                for (PatientRoom pr : cit.getPatientRooms()) {
                    if (pr.getReferencePatientRoom() != null) {
                        dbl += pr.getReferencePatientRoom().getCalculatedRoomCharge();
                    }
                }
                cit.setAdjustedTotal(dbl);
            }
        }
    }

    private void calCulateRoomCharge(PatientRoom p) {
        double charge;
        //    System.err.println("1 " + p.getRoomFacilityCharge());
        //   System.err.println("2 " + p.getCurrentRoomCharge());
        if (p.getRoomFacilityCharge() == null || p.getCurrentRoomCharge() == 0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double roomCharge = p.getCurrentRoomCharge();

        charge = roomCharge * getInwardBean().calCount(timedFee, p.getAdmittedAt(), p.getDischargedAt());

        p.setCalculatedRoomCharge(charge);
    }

    private boolean checkRoomDischarge(Date date) {
        HashMap hm = new HashMap();
        String sql = "SELECT pr FROM PatientRoom pr "
                + " where pr.retired=false"
                + " and pr.dischargedAt>:dt "
                + " and pr.patientEncounter=:pe ";
        hm.put("pe", getPatientEncounter());
        hm.put("dt", date);
        PatientRoom tmp = getPatientRoomFacade().findFirstBySQL(sql, hm, TemporalType.TIMESTAMP);

        if (tmp != null) {
            return true;
        }

        return false;
    }

    private boolean checkDischargeTime() {
        if (getPatientEncounter() != null && getPatientEncounter().getDateOfAdmission().after(getPatientEncounter().getDateOfDischarge())) {
            UtilityController.addErrorMessage("Check Discharge Time should be after Admitted Time");
            return true;
        }

        if (checkRoomIsDischarged()) {
            UtilityController.addErrorMessage("Please Discharged From Room");
            return true;
        }

        if (checkRoomDischarge(getPatientEncounter().getDateOfDischarge())) {
            UtilityController.addErrorMessage("Check Discharge Time should be after Room Discharge Time");
            return true;
        }

        return false;

    }

    public void checkDate() {
        if (checkDischargeTime()) {
            return;
        }

        makeNull();
        createTables();
    }

    private List<BillItem> billItems;

    public void settle() {
        if (errorCheck()) {
            return;
        }

        if (getPatientEncounter().getCreditLimit() != 0) {
            updateCreditDetail();
        }

        saveBill();
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
        double netCharge = (getGrantTotal() - getDiscount());
        if (getPatientEncounter().getCreditLimit() <= netCharge) {
            getPatientEncounter().setCreditUsedAmount(getPatientEncounter().getCreditLimit());
        } else {
            getPatientEncounter().setCreditUsedAmount(netCharge);
        }

        getPatientEncounterFacade().edit(getPatientEncounter());
    }

    private boolean checkRoomIsDischarged() {
        for (RoomChargeData rcd : getRoomChargeDatas()) {
            if (rcd != null && rcd.getPatientRoom() != null && rcd.getPatientRoom().getDischargedAt() == null) {
                return true;
            }
        }

        return false;
    }

    private boolean checkPatientItems() {
        List<PatientItem> lst = createPatientItems();

        for (PatientItem pi : lst) {
            if (pi != null && pi.getToTime() == null) {
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
        if (getPatientEncounter().isDischarged()) {
            UtilityController.addErrorMessage("Patient Already Discharged");
            return;
        }

        if (checkDischargeTime()) {
            return;
        }

        if (checkPatientItems()) {
            UtilityController.addErrorMessage("Please Finalize Patient Timed Service");
            return;
        }

        getDischargeController().setCurrent((Admission) getPatientEncounter());
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
        if (getPatientEncounter() == null) {
            return true;
        }

        if (getPatientEncounter().isPaymentFinalized()) {
            UtilityController.addErrorMessage("Payment is Finalized U need to cancel Previuios Final Bill of This Bht");
            return true;
        }

        if (!getPatientEncounter().isClaimable()) {
            if (checkCatTotal()) {
                return true;
            }
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

        //   System.err.println("Total " + tot);
        //    System.err.println("Total 2 " + tot2);
        double different = Math.abs((tot - tot2));

        if (different > 0.1) {
            UtilityController.addErrorMessage("Please Adjust category amount correctly");
            return true;
        }
        return false;
    }

    public void toSettleBill() {
        if (!getPatientEncounter().isDischarged()) {
            UtilityController.addErrorMessage("Please Discharge This Patient");
            return;
        }

 
        createTables();
        calFinalValue();
        calculateDiscount();

    }

    private void saveBill() {

        getCurrent().setGrantTotal(grantTotal);
        getCurrent().setDiscount(discount);
        getCurrent().setNetTotal(grantTotal - discount);
        getCurrent().setPaidAmount(paid);
        getCurrent().setClaimableTotal(claimableTotal);
        getCurrent().setInstitution(getSessionController().getInstitution());

        getCurrent().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), BillType.InwardFinalBill, BillNumberSuffix.INWFINAL));
        getCurrent().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getCurrent(), BillType.InwardFinalBill, BillNumberSuffix.INWFINAL));

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
            temBi.setAdjustedValue(cit.getAdjustedTotal());
            temBi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            temBi.setCreater(getSessionController().getLoggedUser());

            getBillItemFacade().create(temBi);

            if (cit.getInwardChargeType() == InwardChargeType.ProfessionalCharge) {
                updateProBillFee(temBi);
                temProfFee += cit.getNetTotal();
            } else {
                temHosFee += cit.getNetTotal();
            }

            if (cit.getInwardChargeType() == InwardChargeType.RoomCharges) {
                saveRoomBillFee(cit.getPatientRooms(), temBi);
            }

            getBillItemFacade().edit(temBi);
            getCurrent().getBillItems().add(temBi);
        }

        getCurrent().setProfessionalFee(temProfFee);
        getCurrent().setHospitalFee(temHosFee);

        getBillFacade().edit(getCurrent());
    }

    private void updateProBillFee(BillItem bItem) {
        for (BillFee bf : getProfesionallFee()) {
            bf.setReferenceBillItem(bItem);
            getBillFeeFacade().edit(bf);

            bItem.getProFees().add(bf);

        }

    }

    private void saveRefencePatientRoom(PatientRoom pr) {
        if (pr.getId() == null) {
            getPatientRoomFacade().create(pr);
        } else {
            getPatientRoomFacade().edit(pr);
        }
    }

    private void saveRoomBillFee(List<PatientRoom> patientRooms, BillItem bItem) {
        for (PatientRoom pt : patientRooms) {
            BillFee tmp = new BillFee();
            tmp.setBill(bItem.getBill());
            tmp.setBillItem(bItem);

            saveRefencePatientRoom(pt);

            tmp.setReferencePatientRoom(pt);

            getBillFeeFacade().create(tmp);

            bItem.getBillFees().add(tmp);

        }

    }

    public void createTables() {
        makeNull();
        createRoomChargeDatas();
        createGuardianRoomChargeDatas();
        createPatientItems();
        createIssueTable();
        createStoreTable();
        getInwardBean().createDepartmentBillItems(patientEncounter, null);
        createAdditionalChargeBill();
        createProfesionallFee();
        createPaymentBill();
        createSurgeryBill();
        createChargeItemTotals();

        updateTotal();

        roomChargeDatas = null;
        guadianRoomChargeDatas = null;
    }

    private List<PatientItem> createPatientItems() {
        patientItems = new ArrayList<>();
        HashMap hm = new HashMap();
        String sql = "SELECT i FROM PatientItem i where Type(i.item)=TimedItem and "
                + " i.retired=false and i.patientEncounter=:pe";
        hm.put("pe", getPatientEncounter());
        patientItems = getPatientItemFacade().findBySQL(sql, hm);

        if (patientItems == null) {
            patientItems = new ArrayList<>();
        }

        for (PatientItem pi : patientItems) {
            TimedItemFee timedItemFee = getInwardBean().getTimedItemFee((TimedItem) pi.getItem());
            double count = getInwardBean().calCount(timedItemFee, pi.getFromTime(), pi.getToTime());
            pi.setServiceValue(count * timedItemFee.getFee());
            pi.setTmpConsumedTime(getDuration(pi.getFromTime(), pi.getToTime()));

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

        TimedItemFee timedItemFee = getInwardBean().getTimedItemFee((TimedItem) patientItem.getItem());
        double count = getInwardBean().calCount(timedItemFee, patientItem.getFromTime(), patientItem.getToTime());
        patientItem.setServiceValue(count * timedItemFee.getFee());

        getPatientItemFacade().edit(patientItem);

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
        guadianRoomChargeDatas = null;
        roomChargeDatas = null;
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
        String sql = "SELECT pr FROM PatientRoom pr "
                + " where pr.retired=false"
                + " and pr.patientEncounter=:pe "
                + " and type(pr)!=:class"
                + " order by pr.createdAt";
        hm.put("pe", getPatientEncounter());
        hm.put("class", GuardianRoom.class);
        List<PatientRoom> tmp = getPatientRoomFacade().findBySQL(sql, hm);
        System.err.println("PATIENTROOM " + tmp.size());
        setRoomChargeData(tmp);
        // totalLinen = getInwardBean().calTotalLinen(tmp);

        return roomChargeDatas;
    }

    private List<RoomChargeData> createGuardianRoomChargeDatas() {
        guadianRoomChargeDatas = new ArrayList<>();

        HashMap hm = new HashMap();
        String sql = "SELECT pr FROM GuardianRoom pr "
                + " where pr.retired=false"
                + " and pr.patientEncounter=:pe "
                + " order by pr.createdAt";
        hm.put("pe", getPatientEncounter());
        List<PatientRoom> tmp = getPatientRoomFacade().findBySQL(sql, hm);
        System.err.println("GUARDIANROOM " + tmp.size());
        setGuardianRoomChargeData(tmp);
        // totalLinen = getInwardBean().calTotalLinen(tmp);

        return guadianRoomChargeDatas;
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
            System.err.println("PATIENT ROOM " + p);
            RoomChargeData rcd = new RoomChargeData();
            rcd.setPatientRoom(p);
            addRoomCharge(rcd, p);
            addMaintananceCharge(rcd, p);
            addNursingCharge(rcd, p);
            addMoCharge(rcd, p);

            roomChargeDatas.add(rcd);
        }
    }

    private void setGuardianRoomChargeData(List<PatientRoom> tmp) {

        for (PatientRoom p : tmp) {
            System.err.println("Gaurdian ROOM " + p);
            RoomChargeData rcd = new RoomChargeData();
            rcd.setPatientRoom(p);
            addRoomCharge(rcd, p);
            addMaintananceCharge(rcd, p);
            //  addNursingCharge(rcd, p);
            //   addMoCharge(rcd, p);

            guadianRoomChargeDatas.add(rcd);
        }
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
            charge = mo * getInwardBean().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = mo * getInwardBean().calCount(timedFee, p.getAdmittedAt(), p.getDischargedAt());
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
            charge = nursing * getInwardBean().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = nursing * getInwardBean().calCount(timedFee, p.getAdmittedAt(), p.getDischargedAt());
        }
        rcd.setNursingTot(charge);
    }

    private void addRoomCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;
        //      System.err.println("1 " + p.getRoomFacilityCharge());
        //     System.err.println("2 " + p.getCurrentRoomCharge());
        if (p.getRoomFacilityCharge() == null || p.getCurrentRoomCharge() == 0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double roomCharge = p.getCurrentRoomCharge();

        rcd.setPatientRoom(p);

//        if (p.getDischargedAt() != null) {
//            charge = roomCharge * getInwardBean().calCountWithoutOverShoot(timedFee, p);
//        } else {
//            charge = roomCharge * getInwardBean().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
//        }
        charge = roomCharge * getInwardBean().calCount(timedFee, p.getAdmittedAt(), p.getDischargedAt());

        //      System.err.println("Room Charge " + roomCharge);
        //     System.out.println("calculated " + charge);
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
            charge = roomCharge * getInwardBean().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = roomCharge * getInwardBean().calCount(timedFee, p.getAdmittedAt(), p.getDischargedAt());
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
            charge = maintanance * getInwardBean().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = maintanance * getInwardBean().calCount(timedFee, p.getAdmittedAt(), p.getDischargedAt());
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
    @EJB
    private ItemFacade itemFacade;

    public void createIssueTable() {
        pharmacyIssues = new ArrayList<>();
        String sql;
        HashMap hm;
        sql = "SELECT  b FROM Bill b WHERE b.retired=false "
                + " and b.billType=:btp and "
                + " (b.billedBill is null )  "
                + " and  b.patientEncounter=:pe"
                + " and (type(b)=:class) ";
        hm = new HashMap();
        hm.put("btp", BillType.PharmacyBhtPre);
        hm.put("class", PreBill.class);
        hm.put("pe", getPatientEncounter());

        List<Bill> bills = getBillFacade().findBySQL(sql, hm);

        hm.clear();
        sql = "SELECT  b FROM Bill b WHERE b.retired=false "
                + " and b.billType=:btp"
                + " and type(b.billedBill)=:billedClass "
                + " and  b.patientEncounter=:pe"
                + " and (type(b)=:class) ";
        hm = new HashMap();
        hm.put("btp", BillType.PharmacyBhtPre);
        hm.put("class", RefundBill.class);
        hm.put("billedClass", PreBill.class);
        hm.put("pe", getPatientEncounter());

        List<Bill> bills2 = getBillFacade().findBySQL(sql, hm);

        pharmacyIssues.addAll(bills);
        pharmacyIssues.addAll(bills2);

    }

    public void createStoreTable() {
        String sql;
        HashMap hm;
        sql = "SELECT  b FROM Bill b WHERE b.retired=false "
                + " and b.billType=:btp  "
                + " and  b.patientEncounter=:pe"
                + " and type(b)=:class ";
        hm = new HashMap();
        hm.put("btp", BillType.StoreBhtPre);
        hm.put("class", PreBill.class);
        hm.put("pe", getPatientEncounter());
        storeIssues = getBillFacade().findBySQL(sql, hm);

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
        String sql = "SELECT bt FROM BillFee bt WHERE "
                + " bt.retired=false "
                + " and bt.fee.feeType=:ftp "
                + " and (bt.bill.billType=:btp or bt.bill.billType=:btp2 )"
                + " and bt.bill.patientEncounter=:pe ";
        hm.put("ftp", FeeType.Staff);
        hm.put("btp", BillType.InwardBill);
        hm.put("btp2", BillType.InwardProfessional);
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
        String sql = "SELECT sum(bt.feeValue) FROM BillFee bt WHERE"
                + " bt.retired=false "
                + " and bt.fee.feeType=:ftp  "
                + " and (bt.bill.billType=:btp or bt.bill.billType=:btp2 ) "
                + " and bt.bill.patientEncounter=:pe";
        hm.put("ftp", FeeType.Staff);
        hm.put("btp", BillType.InwardBill);
        hm.put("btp2", BillType.InwardProfessional);
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

    private List<Bill> createSurgeryBill() {

        HashMap hm = new HashMap();
        String sql = "SELECT  b FROM Bill b WHERE b.retired=false  and b.billType=:btp "
                + " and b.patientEncounter=:pe ";
        hm.put("btp", BillType.SurgeryBill);
        hm.put("pe", getPatientEncounter());
        surgeryBill = getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

        if (surgeryBill == null) {
            return new ArrayList<>();
        }

        return surgeryBill;

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

    public void calFinalValue() {
        grantTotal = 0;
        discount = 0;

        for (ChargeItemTotal c : getChargeItemTotals()) {
            grantTotal += c.getTotal();
            discount += c.getDiscount();
            claimableTotal += c.getAdjustedTotal();
        }
    }

    double claimableTotal = 0;

    public double getClaimableTotal() {
        return claimableTotal;
    }

    public void setClaimableTotal(double claimableTotal) {
        this.claimableTotal = claimableTotal;
    }

    public double getGrantTotal() {
        return grantTotal;
    }

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
        return due;
    }

    public void setDue(double due) {
        this.due = due;
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

    private void createChargeItemTotals() {
        chargeItemTotals = new ArrayList<>();

        for (InwardChargeType i : InwardChargeType.values()) {
            ChargeItemTotal cit = new ChargeItemTotal();
            cit.setInwardChargeType(i);

            chargeItemTotals.add(cit);
        }

        if (getPatientEncounter() != null) {
            setKnownChargeTot();
            setServiceTotCategoryWise();
            setTimedServiceTotCategoryWise();
            setChargeValueFromAdditional();
            setRoomChargeList();
        }

        setNetAdjustValue();

    }

    private void setNetAdjustValue() {
        for (ChargeItemTotal cit : chargeItemTotals) {
            cit.setNetTotal(cit.getTotal());
            cit.setAdjustedTotal(cit.getTotal());

        }
    }

    private void setChargeValueFromAdditional() {
        for (ChargeItemTotal cit : chargeItemTotals) {
            double adj = getValueFromAdditionalCharge(cit.getInwardChargeType());
            double tot = cit.getTotal();

            cit.setTotal(tot + adj);
        }
    }

    private void setPrintingPatientRoom(RoomChargeData rcd, ChargeItemTotal cit) {
        PatientRoom pr = new PatientRoom();
        pr.setReferencePatientRoom(rcd.getPatientRoom());
        pr.setAdmittedAt(rcd.getPatientRoom().getAdmittedAt());
        pr.setDischargedAt(rcd.getPatientRoom().getDischargedAt());
        pr.setCalculatedRoomCharge(rcd.getChargeTot());
        pr.setRoomFacilityCharge(rcd.getPatientRoom().getRoomFacilityCharge());
        pr.setCurrentRoomCharge(rcd.getPatientRoom().getCurrentRoomCharge());
        cit.getPatientRooms().add(pr);
    }

    private void setRoomChargeList() {
        for (ChargeItemTotal cit : chargeItemTotals) {
            if (cit.getInwardChargeType() == InwardChargeType.RoomCharges) {
                //  System.err.println("Inside Room Charges");
                roomChargeDatas = null;
                for (RoomChargeData rcd : getRoomChargeDatas()) {
                    setPrintingPatientRoom(rcd, cit);
                }

                guadianRoomChargeDatas = null;
                for (RoomChargeData rcd : getGuadianRoomChargeDatas()) {
                    setPrintingPatientRoom(rcd, cit);
                }

                //  System.err.println("Room Charges Size " + cit.getPatientRooms().size());
            }
        }
    }

    @EJB
    private InwardBean inwardBean;

    private void updateTotal() {
        grantTotal = 0;
        for (ChargeItemTotal cit : chargeItemTotals) {
            grantTotal += cit.getTotal();
        }

        paid = getInwardBean().getPaidValue(getPatientEncounter());

        due = grantTotal - paid;

        if (getPatientEncounter().getCreditLimit() != 0) {
            due -= getPatientEncounter().getCreditLimit();
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
                + " and i.patientEncounter=:pe ";

        HashMap m = new HashMap();
        m.put("btp", BillType.InwardAdditionalBill);
        m.put("pe", getPatientEncounter());
        additionalChargeBill = getBillFacade().findBySQL(sql, m, TemporalType.DATE);

        return additionalChargeBill;
    }

    private double getValueFromAdditionalCharge(InwardChargeType inwardChargeType) {
        //   additionalChargeBill = new ArrayList<>();
        String sql = "Select sum(i.netValue) From BillItem i where i.retired=false and i.bill.billType=:btp "
                + "and i.bill.patientEncounter=:pe and i.inwardChargeType=:inwCh ";
        HashMap m = new HashMap();
        m.put("btp", BillType.InwardAdditionalBill);
        m.put("pe", getPatientEncounter());
        m.put("inwCh", inwardChargeType);
        double val = getBillFacade().findDoubleByJpql(sql, m, TemporalType.DATE);

        return val;
    }

    private List<Bill> additionalChargeBill;

    private void setKnownChargeTot() {
        List<RoomChargeData> list = new ArrayList<>();
        list = getRoomChargeDatas();
        list.addAll(getGuadianRoomChargeDatas());

        double roomCharge = 0;
        double moCharge = 0;
        double nursingCharge = 0;
        double maintanance = 0;
        for (RoomChargeData rcd : list) {
            roomCharge += rcd.getChargeTot();
            moCharge += rcd.getMoChargeTot();
            nursingCharge += rcd.getNursingTot();
            maintanance += rcd.getMaintananceTot();
        }

        list = null;

        for (ChargeItemTotal i : chargeItemTotals) {
            switch (i.getInwardChargeType()) {
                case AdmissionFee:
                    if (getPatientEncounter().getAdmissionType() != null) {
                        i.setTotal(getPatientEncounter().getAdmissionType().getAdmissionFee());
                    }
                    break;
                case RoomCharges:
                    i.setTotal(roomCharge);
                    break;
                case MOCharges:
                    i.setTotal(moCharge);
                    break;
                case NursingCharges:
                    i.setTotal(nursingCharge);
                    break;
                case MaintainCharges:
                    i.setTotal(maintanance);
                    break;
                case LinenCharges:
                    i.setTotal(getInwardBean().calTotalLinen(getPatientEncounter()));
                    break;
                case Medicine:
                    i.setTotal(calCostOfMadicine());
                    break;
                case ProfessionalCharge:
                    i.setTotal(calculateProfessionalCharges());
                    break;

            }
        }
    }

    private double calCostOfMadicine() {
        String sql;
        HashMap hm;
        sql = "SELECT  sum(b.adjustedValue)"
                + " FROM BillItem b "
                + " WHERE b.retired=false "
                + " and (b.bill.billType=:btp1 "
                + " or  b.bill.billType=:btp2) "
                + " and  b.bill.patientEncounter=:pe";
        hm = new HashMap();
        hm.put("btp1", BillType.PharmacyBhtPre);
        hm.put("btp2", BillType.StoreBhtPre);
        hm.put("pe", getPatientEncounter());
        return getBillItemFacade().findDoubleByJpql(sql, hm);

    }

//    private double calHosFee(BillItem billItem) {
//        String sql = "Select sum(s.feeValue) From BillFee s"
//                + " where s.retired=false "
//                + " and s.billItem=:bItm "
//                + " and s.fee.feeType!=:st ";
//        HashMap hm = new HashMap();
//        hm.put("bItm", billItem);
//        hm.put("st", FeeType.Staff);
//
//        double dbl = getBillFeeFacade().findDoubleByJpql(sql, hm);
//
//        return dbl;
//    }
    private double getServiceBillItemsTotalByInwardChargeType(InwardChargeType inwardChargeType) {
        String sql = "Select sum(s.feeValue) From BillFee s"
                + " where s.retired=false "
                + " and s.billItem.bill.billType=:btp "
                + " and s.billItem.bill.patientEncounter=:pe"
                + " and s.billItem.item.inwardChargeType=:inw "
                + " and s.fee.feeType!=:st ";
        HashMap hm = new HashMap();
        hm.put("btp", BillType.InwardBill);
        hm.put("pe", getPatientEncounter());
        hm.put("inw", inwardChargeType);
        hm.put("st", FeeType.Staff);

        double dbl = getBillFeeFacade().findDoubleByJpql(sql, hm);

        return dbl;

    }

    private double getTimedItemFeeTotalByInwardChargeType(InwardChargeType inwardChargeType) {

        HashMap hm = new HashMap();
        String sql = " SELECT sum(i.serviceValue) "
                + " FROM PatientItem i where "
                + " type(i.item)=:cls "
                + " and i.retired=false "
                + " and i.patientEncounter=:pe "
                + " and i.item.inwardChargeType=:inw ";
        hm.put("pe", getPatientEncounter());
        hm.put("cls", TimedItem.class);
        hm.put("inw", inwardChargeType);
        double dbl = getPatientItemFacade().findDoubleByJpql(sql, hm);

        return dbl;
    }

    private void setServiceTotCategoryWise() {
        for (ChargeItemTotal ch : chargeItemTotals) {
            ch.setTotal(ch.getTotal() + getServiceBillItemsTotalByInwardChargeType(ch.getInwardChargeType()));
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

    private void setTimedServiceTotCategoryWise() {

        for (ChargeItemTotal ch : chargeItemTotals) {
            ch.setTotal(ch.getTotal() + getTimedItemFeeTotalByInwardChargeType(ch.getInwardChargeType()));
        }

    }

    public void setChargeItemTotals(List<ChargeItemTotal> chargeItemTotals) {
        this.chargeItemTotals = chargeItemTotals;
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
            departmentBillItems = getInwardBean().createDepartmentBillItems(patientEncounter, null);
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

    public InwardMemberShipDiscount getInwardMemberShipDiscount() {
        return inwardMemberShipDiscount;
    }

    public void setInwardMemberShipDiscount(InwardMemberShipDiscount inwardMemberShipDiscount) {
        this.inwardMemberShipDiscount = inwardMemberShipDiscount;
    }

    public List<Bill> getSurgeryBill() {
        return surgeryBill;
    }

    public void setSurgeryBill(List<Bill> surgeryBill) {
        this.surgeryBill = surgeryBill;
    }

    public List<Bill> getPharmacyIssues() {
        return pharmacyIssues;
    }

    public void setPharmacyIssues(List<Bill> pharmacyIssues) {
        this.pharmacyIssues = pharmacyIssues;
    }

    public List<Bill> getStoreIssues() {
        return storeIssues;
    }

    public void setStoreIssues(List<Bill> storeIssues) {
        this.storeIssues = storeIssues;
    }

    public InwardBean getInwardBean() {
        return inwardBean;
    }

    public void setInwardBean(InwardBean inwardBean) {
        this.inwardBean = inwardBean;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        createBillItems(item);
        this.item = item;
    }
}
