/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.data.BillType;
import com.divudi.data.FeeType;
import com.divudi.data.dataStructure.DepartmentBillItems;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Category;
import com.divudi.entity.Department;
import com.divudi.entity.Fee;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.ItemFee;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.PriceMatrix;
import com.divudi.entity.RefundBill;
import com.divudi.entity.WebUser;
import com.divudi.entity.inward.AdmissionType;
import com.divudi.entity.inward.GuardianRoom;
import com.divudi.entity.inward.InwardFee;
import com.divudi.entity.inward.InwardPriceAdjustment;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.inward.RoomFacilityCharge;
import com.divudi.entity.inward.TimedItem;
import com.divudi.entity.inward.TimedItemFee;
import com.divudi.entity.lab.Investigation;
import com.divudi.facade.AdmissionFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.FeeFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.ItemFeeFacade;
import com.divudi.facade.PatientRoomFacade;
import com.divudi.facade.PriceMatrixFacade;
import com.divudi.facade.RoomFacade;
import com.divudi.facade.TimedItemFeeFacade;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Stateless
public class InwardBean {

    @EJB
    private PatientRoomFacade patientRoomFacade;
    @EJB
    private RoomFacade roomFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private FeeFacade feeFacade;
    @EJB
    private DepartmentFacade departmentFacade;
    @EJB
    private ItemFacade itemFacade;
    @EJB
    private BillItemFacade billItemFacade;

    @EJB
    private TimedItemFeeFacade timedItemFeeFacade;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private ItemFeeFacade itemFeeFacade;
    @EJB
    private PriceMatrixFacade priceMatrixFacade;
    @EJB
    private AdmissionFacade admissionFacade;
    @EJB
    BillBean billBean;

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public List<BillFee> billFeeFromBillItemWithMatrix(BillItem billItem) {

        List<ItemFee> itemFee = getBillBean().getItemFee(billItem);

        List<BillFee> t = new ArrayList<>();
        for (Fee i : itemFee) {
            BillFee billFee = getBillBean().createBillFee(billItem, i);
            t.add(billFee);
        }

        BillFee bf = calInwardMargin(billItem);

        if (bf.getFeeValue() != 0.0) {
            t.add(bf);
        }

        return t;
    }

    private List<Department> getToDepartmentList(PatientEncounter patientEncounter, Bill forwardRefBill) {
        String sql;
        HashMap hm = new HashMap();

        sql = "SELECT  distinct(b.bill.toDepartment) FROM BillItem b "
                + " WHERE   b.retired=false "
                + " and b.bill.billType=:btp ";

        if (forwardRefBill != null) {
            sql += " and b.bill.forwardReferenceBill=:fB";
            hm.put("fB", forwardRefBill);
        }

        sql += " and Type(b.item)!=TimedItem "
                + " and b.bill.patientEncounter=:pe ";

        hm.put("btp", BillType.InwardBill);
        hm.put("pe", patientEncounter);

        return getDepartmentFacade().findBySQL(sql, hm, TemporalType.TIME);
    }

    private List<Item> getToDepartmentItems(PatientEncounter patientEncounter, Department department, Bill forwardBill) {
        HashMap hm = new HashMap();
        String sql = "SELECT  distinct(b.item) FROM BillItem b "
                + " WHERE b.retired=false"
                + " and b.bill.billType=:btp";

        if (forwardBill != null) {
            sql += " and b.bill.forwardReferenceBill=:fB";
            hm.put("fB", forwardBill);
        }

        sql += " and Type(b.item)!=TimedItem"
                + "  and b.bill.patientEncounter=:pe"
                + " and b.bill.toDepartment=:dep "
                + "  order by b.item.name ";

        hm.put("btp", BillType.InwardBill);
        hm.put("pe", patientEncounter);
        hm.put("dep", department);

        return getItemFacade().findBySQL(sql, hm, TemporalType.TIME);
    }

    private double calBillItemCount(Bill bill, Item item, PatientEncounter patientEncounter, Bill forwardBill) {
        HashMap hm = new HashMap();
        String sql = "SELECT  count(b) FROM BillItem b "
                + " WHERE b.retired=false "
                + "  and b.bill.billType=:btp ";

        if (forwardBill != null) {
            sql += " and b.bill.forwardReferenceBill=:fB";
            hm.put("fB", forwardBill);
        }

        sql += " and b.bill.patientEncounter=:pe "
                + " and b.item=:itm "
                + " and type(b.bill)=:cls";

        hm.put("btp", BillType.InwardBill);
        hm.put("pe", patientEncounter);
        hm.put("itm", item);
        hm.put("cls", bill.getClass());
        double dbl = getBillItemFacade().countBySql(sql, hm, TemporalType.TIME);

        return dbl;
    }

    public List<DepartmentBillItems> createDepartmentBillItems(PatientEncounter patientEncounter, Bill forwardRefBill) {
        List<DepartmentBillItems> list = new ArrayList<>();

        List<Department> deptList = getToDepartmentList(patientEncounter, forwardRefBill);

        for (Department dep : deptList) {
            DepartmentBillItems table = new DepartmentBillItems();

            List<Item> items = getToDepartmentItems(patientEncounter, dep, forwardRefBill);

            for (Item itm : items) {
                double billed = calBillItemCount(new BilledBill(), itm, patientEncounter, forwardRefBill);
                double cancelld = calBillItemCount(new CancelledBill(), itm, patientEncounter, forwardRefBill);
                double refund = calBillItemCount(new RefundBill(), itm, patientEncounter, forwardRefBill);
//                System.err.println("Billed " + billed);
//                System.err.println("Cancelled " + cancelld);
//                System.err.println("Refun " + refund);
                itm.setTransBillItemCount(billed - (cancelld + refund));
            }

            table.setDepartment(dep);
            table.setItems(items);

            list.add(table);

        }

//        calServiceTot(departmentBillItems);
        return list;

    }

    public Fee getStaffFeeForInward(WebUser webUser) {
        String sql = "Select f From InwardFee f "
                + " where f.retired=false "
                + " and f.feeType=:st ";

        HashMap hm = new HashMap();
        hm.put("st", FeeType.Staff);

        Fee fee = getFeeFacade().findFirstBySQL(sql, hm);
        if (fee == null) {
            fee = new InwardFee();
            fee.setCreatedAt(new Date());
            fee.setCreater(webUser);
            fee.setFeeType(FeeType.Staff);
            getFeeFacade().create(fee);
        }

        return fee;

    }

    public void updateFinalFill(PatientEncounter patientEncounter) {
        String sql = "Select b From BilledBill b where b.retired=false and b.cancelled=false "
                + " and b.billType=:btp and b.patientEncounter=:pe";
        HashMap hm = new HashMap();
        hm.put("btp", BillType.InwardFinalBill);
        hm.put("pe", patientEncounter);

        Bill b = getBillFacade().findFirstBySQL(sql, hm);

        double paid = getPaidValue(patientEncounter);
//        System.err.println("NET " + b.getNetTotal());
//        System.err.println("PAID " + paid);

        b.setPaidAmount(0 - paid);
        getBillFacade().edit(b);

    }

    public double getPaidValue(PatientEncounter patientEncounter) {

        HashMap hm = new HashMap();
        String sql = "SELECT  sum(b.netTotal) FROM Bill b WHERE b.retired=false  and b.billType=:btp "
                + " and b.patientEncounter=:pe ";
        hm.put("btp", BillType.InwardPaymentBill);
        hm.put("pe", patientEncounter);
        double dbl = getBillFacade().findDoubleByJpql(sql, hm, TemporalType.TIMESTAMP);

        return dbl;

    }

    public PatientRoom savePatientRoom(RoomFacilityCharge newRoomFacilityCharge, double addLinenCharge, Date addmittedAt, PatientEncounter patientEncounter, WebUser webUser) {
        PatientRoom pr = new PatientRoom();

        //  pr.setCurrentLinenCharge(newRoomFacilityCharge.getLinenCharge());
        pr.setCurrentMaintananceCharge(newRoomFacilityCharge.getMaintananceCharge());
        pr.setCurrentMoCharge(newRoomFacilityCharge.getMoCharge());
        pr.setCurrentNursingCharge(newRoomFacilityCharge.getNursingCharge());
        pr.setCurrentRoomCharge(newRoomFacilityCharge.getRoomCharge());

        pr.setAddedLinenCharge(addLinenCharge);
        pr.setAdmittedAt(addmittedAt);

        PatientRoom currentPatientRoom = getCurrentPatientRoom(patientEncounter);

        pr.setPreviousRoom(currentPatientRoom);
        pr.setCreatedAt(Calendar.getInstance().getTime());
        pr.setCreater(webUser);
        pr.setAddmittedBy(webUser);
        pr.setPatientEncounter(patientEncounter);
        pr.setRoomFacilityCharge(newRoomFacilityCharge);

        getPatientRoomFacade().create(pr);

        if (patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            makeRoomFilled(pr);
        }

        return pr;
    }

    public PatientRoom saveGurdianRoom(RoomFacilityCharge newRoomFacilityCharge, double addLinenCharge, Date addmittedAt, PatientEncounter patientEncounter, WebUser webUser) {
        PatientRoom pr = new GuardianRoom();

        //  pr.setCurrentLinenCharge(newRoomFacilityCharge.getLinenCharge());
        pr.setCurrentMaintananceCharge(newRoomFacilityCharge.getMaintananceCharge());
        pr.setCurrentMoCharge(newRoomFacilityCharge.getMoCharge());
        pr.setCurrentNursingCharge(newRoomFacilityCharge.getNursingCharge());
        pr.setCurrentRoomCharge(newRoomFacilityCharge.getRoomCharge());

        pr.setAddedLinenCharge(addLinenCharge);
        pr.setAdmittedAt(addmittedAt);

        PatientRoom currentPatientRoom = getCurrentGuardianRoom(patientEncounter);

        pr.setPreviousRoom(currentPatientRoom);
        pr.setCreatedAt(Calendar.getInstance().getTime());
        pr.setCreater(webUser);
        pr.setAddmittedBy(webUser);
        pr.setPatientEncounter(patientEncounter);
        pr.setRoomFacilityCharge(newRoomFacilityCharge);

        getPatientRoomFacade().create(pr);

        if (patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            makeRoomFilled(pr);
        }

        return pr;
    }

    public PatientRoom savePatientRoom(PatientRoom patientRoom, PatientEncounter patientEncounter, Date admittedAt, WebUser webUser) {
        //     patientRoom.setCurrentLinenCharge(patientRoom.getRoomFacilityCharge().getLinenCharge());
        patientRoom.setCurrentMaintananceCharge(patientRoom.getRoomFacilityCharge().getMaintananceCharge());
        patientRoom.setCurrentMoCharge(patientRoom.getRoomFacilityCharge().getMoCharge());
        patientRoom.setCurrentNursingCharge(patientRoom.getRoomFacilityCharge().getNursingCharge());
        patientRoom.setCurrentRoomCharge(patientRoom.getRoomFacilityCharge().getRoomCharge());

        patientRoom.setAddmittedBy(webUser);
        patientRoom.setAdmittedAt(admittedAt);
        patientRoom.setCreatedAt(Calendar.getInstance().getTime());
        patientRoom.setCreater(webUser);
        patientRoom.setPatientEncounter(patientEncounter);

        if (patientRoom.getId() == null || patientRoom.getId() == 0) {
            getPatientRoomFacade().create(patientRoom);
        } else {
            getPatientRoomFacade().edit(patientRoom);
        }

        if (patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            makeRoomFilled(patientRoom);
        }

        return patientRoom;
    }

    public void makeRoomFilled(PatientRoom pr) {

        pr.getRoomFacilityCharge().getRoom().setFilled(true);
        getRoomFacade().edit(pr.getRoomFacilityCharge().getRoom());

    }

    public void makeRoomVacant(PatientEncounter patientEncounter) {
        if (!patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            return;
        }

        PatientRoom pr = getCurrentPatientRoom(patientEncounter);
        pr.getRoomFacilityCharge().getRoom().setFilled(false);
        getRoomFacade().edit(pr.getRoomFacilityCharge().getRoom());
    }

    public void makeRoomVacantGurdian(PatientEncounter patientEncounter) {
        if (!patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            return;
        }

        PatientRoom pr = getCurrentGuardianRoom(patientEncounter);
        if (pr != null) {
            pr.getRoomFacilityCharge().getRoom().setFilled(false);
            getRoomFacade().edit(pr.getRoomFacilityCharge().getRoom());
        }
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
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

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public FeeFacade getFeeFacade() {
        return feeFacade;
    }

    public void setFeeFacade(FeeFacade feeFacade) {
        this.feeFacade = feeFacade;
    }

    public DepartmentFacade getDepartmentFacade() {
        return departmentFacade;
    }

    public void setDepartmentFacade(DepartmentFacade departmentFacade) {
        this.departmentFacade = departmentFacade;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    @EJB
    PriceMatrixBean priceMatrixBean;

    public PriceMatrixBean getPriceMatrixBean() {
        return priceMatrixBean;
    }

    public void setPriceMatrixBean(PriceMatrixBean priceMatrixBean) {
        this.priceMatrixBean = priceMatrixBean;
    }

    private BillFee calInwardMargin(BillItem i) {

        BillFee f = new BillFee();

        Fee matrix = getBillBean().getFee(FeeType.Matrix);

        if (matrix == null) {
            matrix = new Fee();
            matrix.setName("Matrix");
            matrix.setFeeType(FeeType.Matrix);
        }

        f.setBillItem(i);
        f.setFee(matrix);
        f.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

        if (i.getItem().getCategory() == null || i.getItem().getDepartment() == null || i.getItem().getDepartment().getInstitution() == null) {
            f.setFeeValue(0.0);
            return f;
        }

        InwardPriceAdjustment inwardPriceAdjustment = getPriceMatrixBean().getInwardPriceAdjustment(i);

        if (inwardPriceAdjustment == null) {
            f.setFeeValue(0.0);
            return f;

        }

        matrix.setFee(inwardPriceAdjustment.getMargin());

        ////System.out.println("Margin : " + is.get(0).getMargin());
        f.setInstitution(i.getItem().getDepartment().getInstitution());
        f.setFeeValue(matrix.getFee());
        ////System.out.println("Margin : " + is.get(0).getMargin());

        if (matrix.getId() == null) {
            getFeeFacade().create(matrix);
        } else {
            getFeeFacade().edit(matrix);
        }

        return f;
    }

    public List<PatientRoom> getPatientRooms(PatientEncounter patientEncounter) {
        HashMap hm = new HashMap();
        String sql = "SELECT pr FROM PatientRoom pr where pr.retired=false"
                + " and pr.patientEncounter=:pe order by pr.createdAt";
        hm.put("pe", patientEncounter);
        List<PatientRoom> tmp = getPatientRoomFacade().findBySQL(sql, hm);

        if (tmp == null) {
            tmp = new ArrayList<>();
        }

        return tmp;
    }

    public PatientRoom getCurrentPatientRoom(PatientEncounter patientEncounter) {
        String sql = "SELECT pr FROM PatientRoom pr "
                + " where pr.retired=false "
                + " and pr.patientEncounter=:pe "
                + " and type(pr)!=:class "
                + " order by pr.admittedAt desc ";
        HashMap hm = new HashMap();
        hm.put("pe", patientEncounter);
        hm.put("class", GuardianRoom.class);
        PatientRoom patientRoom = getPatientRoomFacade().findFirstBySQL(sql, hm);

        return patientRoom;

    }

    public PatientRoom getCurrentGuardianRoom(PatientEncounter patientEncounter) {
        String sql = "SELECT pr FROM GuardianRoom pr "
                + "where pr.retired=false "
                + "and pr.patientEncounter=:pe "
                + "order by pr.admittedAt desc ";
        HashMap hm = new HashMap();
        hm.put("pe", patientEncounter);
        PatientRoom patientRoom = getPatientRoomFacade().findFirstBySQL(sql, hm);

        return patientRoom;

    }

    public String getBhtText(AdmissionType admissionType) {
        String bhtText;
        String sql = "SELECT count(a.id) FROM Admission a where "
                + " a.retired=false and a.admissionType.admissionTypeEnum=:adType ";

        HashMap hm = new HashMap();
        hm.put("adType", admissionType.getAdmissionTypeEnum());
        long temp = getAdmissionFacade().countBySql(sql, hm);

        temp++;
        bhtText = admissionType.getCode().trim() + Long.toString(temp);

        return bhtText;
    }

    public Fee createAdditionalFee() {
        String sql = "Select f from Fee f where f.retired=false and f.feeType=:nm";
        HashMap hm = new HashMap();
        hm.put("nm", FeeType.Additional);
        List<Fee> fee = getFeeFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);
        Fee additional;

        if (fee.isEmpty()) {
            additional = new Fee();
            additional.setName("Additional");
            additional.setFeeType(FeeType.Additional);
            additional.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            getFeeFacade().create(additional);
            return additional;
        } else {
            return fee.get(0);
        }
    }

    public List<BillFee> billFeeFromBillItemWithMatrix(BillItem billItem, PatientEncounter patientEncounter, Institution institution) {

        List<BillFee> billFeeList = new ArrayList<>();
        List<ItemFee> itemFee = getBillBean().getItemFee(billItem);

        for (Fee i : itemFee) {
            BillFee billFee = getBillBean().createBillFee(billItem, i);
            billFeeList.add(billFee);
        }

        BillFee bf = null;

        bf = getBillFeeMatrix(billItem, institution);
        double serviceValue = getHospitalFeeByItem(billItem.getItem());
        PatientRoom currentRoom = getCurrentPatientRoom(patientEncounter);
        bf.setFeeValue(calInwardMargin(billItem, serviceValue, currentRoom.getRoomFacilityCharge().getDepartment()));

        if (bf != null && bf.getFeeValue() != 0.0) {
            billFeeList.add(bf);
        }

        return billFeeList;
    }

    public double getHospitalFeeByItem(Item item) {
        double dbl = 0;
        HashMap hm = new HashMap();
        String sql = "select sum(f.fee) from Fee f where f.retired=false and f.feeType=:ftp "
                + " and f.item=:itm ";
        hm.put("ftp", FeeType.OwnInstitution);
        hm.put("itm", item);
        dbl = getFeeFacade().findDoubleByJpql(sql, hm);

        return dbl;

    }

    public double getHospitalFeeByBillItem(BillItem billItem) {
        double dbl = 0;
        HashMap hm = new HashMap();
        String sql = "select sum(f.feeValue) from BillFee f where f.retired=false and f.fee.feeType=:ftp "
                + " and f.billItem=:itm ";
        hm.put("ftp", FeeType.OwnInstitution);
        hm.put("itm", billItem);
        dbl = getBillFeeFacade().findDoubleByJpql(sql, hm);

        return dbl;

    }

    @EJB
    private BillFeeFacade billFeeFacade;

    public BillFee getBillFeeMatrix(BillItem billItem, Institution institution) {
        String sql = "Select bf from BillFee bf where bf.retired=false and "
                + " bf.billItem=:bItem and bf.fee.feeType=:ftp ";
        HashMap hm = new HashMap();
        hm.put("bItem", billItem);
        hm.put("ftp", FeeType.Matrix);

        BillFee matrixFee = getBillFeeFacade().findFirstBySQL(sql, hm);

        if (matrixFee == null) {
            matrixFee = new BillFee();
            Fee matrix = getMatrixFee();
            matrixFee.setBillItem(billItem);
            matrixFee.setFee(matrix);
            matrixFee.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            matrixFee.setInstitution(institution);
//            getBillFeeFacade().create(billFee);
        }

        return matrixFee;
    }

    public BillFee getIssueBillFee(BillItem billItem, Institution institution) {
        String sql = "Select bf from BillFee bf where bf.retired=false and "
                + " bf.billItem=:bItem and bf.fee.feeType=:ftp ";
        HashMap hm = new HashMap();
        hm.put("bItem", billItem);
        hm.put("ftp", FeeType.Issue);

        BillFee billtItemFee = getBillFeeFacade().findFirstBySQL(sql, hm);

        if (billtItemFee == null) {
            billtItemFee = new BillFee();

            Fee issueFee = getIssueFee();

            billtItemFee.setBillItem(billItem);
            billtItemFee.setFee(issueFee);
            billtItemFee.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            billtItemFee.setInstitution(institution);
//            getBillFeeFacade().create(billFee);
        }

        return billtItemFee;
    }

    public double calInwardMargin(BillItem billItem, double serviceValue, Department department) {

        String sql;
        HashMap hm = new HashMap();
        PriceMatrix inwardPriceAdjustment;
        Category category;
        if (billItem.getItem() instanceof Investigation) {
            category = ((Investigation) billItem.getItem()).getInvestigationCategory();
        } else {
            category = billItem.getItem().getCategory();
        }

        inwardPriceAdjustment = getPriceMatrixBean().getInwardPriceAdjustment(department, serviceValue, category);

        if (inwardPriceAdjustment == null && category != null) {
            inwardPriceAdjustment = getPriceMatrixBean().getInwardPriceAdjustment(department, serviceValue, category.getParentCategory());
        }

        if (inwardPriceAdjustment == null) {
            return 0;
        }

        double dbl = ((inwardPriceAdjustment.getMargin() * serviceValue) / 100);
        return dbl;

    }

    private Fee getMatrixFee() {
        String sql = "Select f from Fee f where f.retired=false and f.feeType=:nm";
        HashMap hm = new HashMap();
        hm.put("nm", FeeType.Matrix);
        Fee matrix = getFeeFacade().findFirstBySQL(sql, hm);

        if (matrix == null) {
            matrix = new Fee();
            matrix.setName("Matrix");
            matrix.setFeeType(FeeType.Matrix);
            matrix.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            getFeeFacade().create(matrix);

        }

        return matrix;
    }

    private Fee getIssueFee() {
        String sql = "Select f from Fee f where f.retired=false and f.feeType=:nm";
        HashMap hm = new HashMap();
        hm.put("nm", FeeType.Issue);
        Fee issue = getFeeFacade().findFirstBySQL(sql, hm);

        if (issue == null) {
            issue = new Fee();
            issue.setName("Issue");
            issue.setFeeType(FeeType.Issue);
            issue.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            getFeeFacade().create(issue);

        }

        return issue;
    }

    public TimedItemFee getTimedItemFee(TimedItem ti) {
        TimedItemFee tmp = new TimedItemFee();
        if (ti.getId() != null) {
            String sql = "SELECT tif FROM TimedItemFee tif where tif.retired=false AND tif.item.id=" + ti.getId();
            tmp = getTimedItemFeeFacade().findFirstBySQL(sql);
        }

        if (tmp == null) {
            tmp = new TimedItemFee();
            tmp.setDurationHours(0);
            tmp.setOverShootHours(0);
        }
        return tmp;
    }

    public TimedItemFeeFacade getTimedItemFeeFacade() {
        return timedItemFeeFacade;
    }

    public void setTimedItemFeeFacade(TimedItemFeeFacade timedItemFeeFacade) {
        this.timedItemFeeFacade = timedItemFeeFacade;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public double calTotalLinen(PatientEncounter patientEncounter) {

        if (patientEncounter == null || patientEncounter.getAdmissionType() == null) {
            return 0;
        }

        double linen = 0.0;

        Long dayCount = getCommonFunctions().getDayCount(patientEncounter.getDateOfAdmission(), patientEncounter.getDateOfDischarge());

        for (PatientRoom pr : getPatientRooms(patientEncounter)) {
            linen += pr.getAddedLinenCharge();
        }

        if (patientEncounter.getAdmissionType().getDblValue() != null) {
            if (dayCount != 0) {
                linen += (patientEncounter.getAdmissionType().getDblValue() * dayCount);
            } else {
                linen += (patientEncounter.getAdmissionType().getDblValue() * 1);
            }
        }

        return linen;
    }

    public double calCountWithoutOverShoot(TimedItemFee tif, PatientRoom pr) {

        double duration = tif.getDurationHours() * 60;
        double consumeTimeM = 0L;

        consumeTimeM = getCommonFunctions().calculateDurationMin(pr.getAdmittedAt(), pr.getDischargedAt());

        double count = 0;

        if (tif.isBooleanValue()) {
            //For Minute Calculation
            count = (consumeTimeM / duration);
        } else {
            //For Hour Calculation
            count = (long) (consumeTimeM / duration);
        }

        //  System.err.println("Min " + duration);
        //     System.err.println("Consume " + consumeTimeM);
        //   System.err.println("Count " + count);
        if (0 != (consumeTimeM % duration)) {
            count++;
        }

        return count;
    }

    public double calCount(TimedItemFee tif, Date admittedDate, Date dischargedDate) {

        double duration = tif.getDurationHours() * 60;
        double overShoot = tif.getOverShootHours() * 60;
        //  double tempFee = tif.getFee();
        double consumeTime = 0;

        if (dischargedDate == null) {
            dischargedDate = new Date();
        }

        consumeTime = getCommonFunctions().calculateDurationMin(admittedDate, dischargedDate);

        double count = 0;
        double calculation = 0;

        if (consumeTime != 0 && duration != 0) {
            if (tif.isBooleanValue()) {
                //For Minut Calculation (Theatre Charges)
                count = (consumeTime / duration);
            } else {
                //For Room Calculation Hour
                count = (long) (consumeTime / duration);
            }

            calculation = (consumeTime - (count * duration));
            if (overShoot != 0 && overShoot <= (calculation)) {
                count++;
            }
        }

//        System.err.println("Duration " + duration);
//        System.err.println("OverShoot " + overShoot);
//        System.err.println("Consume " + consumeTime);
//        System.err.println("Count " + count);
//        System.err.println("Calcualtion " + calculation);
        return count;
    }

    public ItemFeeFacade getItemFeeFacade() {
        return itemFeeFacade;
    }

    public void setItemFeeFacade(ItemFeeFacade itemFeeFacade) {
        this.itemFeeFacade = itemFeeFacade;
    }

    public AdmissionFacade getAdmissionFacade() {
        return admissionFacade;
    }

    public void setAdmissionFacade(AdmissionFacade admissionFacade) {
        this.admissionFacade = admissionFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public PriceMatrixFacade getPriceMatrixFacade() {
        return priceMatrixFacade;
    }

    public void setPriceMatrixFacade(PriceMatrixFacade priceMatrixFacade) {
        this.priceMatrixFacade = priceMatrixFacade;
    }

}
