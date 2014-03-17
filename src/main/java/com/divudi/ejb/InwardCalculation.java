/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.data.FeeType;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.Department;
import com.divudi.entity.Fee;
import com.divudi.entity.Institution;
import com.divudi.entity.InwardPriceAdjustment;
import com.divudi.entity.Item;
import com.divudi.entity.ItemFee;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.PatientItem;
import com.divudi.entity.inward.AdmissionType;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.inward.TimedItem;
import com.divudi.entity.inward.TimedItemFee;
import com.divudi.entity.lab.Investigation;
import com.divudi.facade.AdmissionFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.FeeFacade;
import com.divudi.facade.InwardPriceAdjustmentFacade;
import com.divudi.facade.ItemFeeFacade;
import com.divudi.facade.PatientRoomFacade;
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
 * @author Buddhika
 */
@Stateless
public class InwardCalculation {

    @EJB
    private TimedItemFeeFacade timedItemFeeFacade;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private ItemFeeFacade itemFeeFacade;
    @EJB
    private InwardPriceAdjustmentFacade inwardPriceAdjustmentFacade;
    @EJB
    private FeeFacade feeFacade;
    @EJB
    private AdmissionFacade admissionFacade;
    @EJB
    private PatientRoomFacade patientRoomFacade;

    public PatientRoom getCurrentPatientRoom(PatientEncounter patientEncounter) {
        String sql = "SELECT pr FROM PatientRoom pr where pr.retired=false and "
                + " pr.patientEncounter=:pe order by pr.admittedAt desc ";
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

    public List<BillFee> billFeeFromBillItemWithMatrix(BillItem billItem, PatientEncounter patientEncounter,Institution institution) {

        List<BillFee> billFeeList = new ArrayList<>();
        BillFee billFee;
        String sql;
        HashMap hm = new HashMap();
        sql = "Select f from ItemFee f where f.retired=false and f.item=:itm ";
        hm.put("itm", billItem.getItem());
        List<ItemFee> itemFee = getItemFeeFacade().findBySQL(sql, hm);

        for (Fee i : itemFee) {
            billFee = new BillFee();
            billFee.setFee(i);
            billFee.setFeeValue(i.getFee());
            billFee.setDepartment(billItem.getItem().getDepartment());
            billFee.setBillItem(billItem);

            billFee.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

            if (billItem.getItem().getDepartment() != null) {
                billFee.setDepartment(billItem.getItem().getDepartment());
            }

            if (billItem.getItem().getDepartment().getInstitution() != null) {
                billFee.setInstitution(billItem.getItem().getDepartment().getInstitution());
            }

            if (i.getStaff() != null) {
                billFee.setStaff(i.getStaff());
            } else {
                billFee.setStaff(null);
            }

            billFee.setSpeciality(i.getSpeciality());

            billFeeList.add(billFee);
        }

        BillFee bf = null;

        bf = getBillFeeMatrix(billItem,institution);
        double serviceValue = getHospitalFeeByItem(billItem.getItem());
        PatientRoom currentRoom = getCurrentPatientRoom(patientEncounter);
        bf.setFeeValue(calInwardMargin(billItem, serviceValue, currentRoom.getRoomFacilityCharge().getDepartment()));

        if (bf != null && bf.getFeeValue() != 0.0) {
            billFeeList.add(bf);
        }

        return billFeeList;
    }

//    public List<BillFee> billFeeFromBillItemWithMatrix(BillItem billItem) {
//
//        List<BillFee> billFeeList = new ArrayList<>();
//        BillFee billFee;
//        String sql;
//        sql = "Select f from ItemFee f where f.retired=false and f.item.id = " + billItem.getItem().getId();
//        List<ItemFee> itemFee = getItemFeeFacade().findBySQL(sql);
//
//        for (Fee i : itemFee) {
//            billFee = new BillFee();
//            billFee.setFee(i);
//            billFee.setFeeValue(i.getFee());
//            billFee.setBillItem(billItem);
//
//            billFee.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//
//            if (billItem.getItem().getDepartment() != null) {
//                billFee.setDepartment(billItem.getItem().getDepartment());
//            }
//
//            if (billItem.getItem().getDepartment().getInstitution() != null) {
//                billFee.setInstitution(billItem.getItem().getDepartment().getInstitution());
//            }
//
//            if (i.getStaff() != null) {
//                billFee.setStaff(i.getStaff());
//            } else {
//                billFee.setStaff(null);
//            }
//            billFee.setSpeciality(i.getSpeciality());
//
//            billFeeList.add(billFee);
//        }
//
//        BillFee bf = calInwardMargin(billItem);
//
//        if (bf.getFeeValue() != 0.0) {
//            billFeeList.add(bf);
//        }
//
//        return billFeeList;
//    }
//    private BillFee calInwardMargin2(BillItem billItem) {
//        BillFee billFee = new BillFee();
//        Fee matrix = createMatrixFee();
//        String sql;
//        HashMap hm = new HashMap();
//
//        billFee.setBillItem(billItem);
//        billFee.setFee(matrix);
//        billFee.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//
//        if (billItem.getItem() instanceof Investigation) {
//            if (((Investigation) billItem.getItem()).getInvestigationCategory() == null || billItem.getItem().getDepartment() == null) {
//                billFee.setFeeValue(0.0);
//                return billFee;
//            }
//            sql = "select a from InwardPriceAdjustment a where a.retired=false and a.category.id="
//                    + ((Investigation) billItem.getItem()).getInvestigationCategory().getId() + " and  a.department.id="
//                    + billItem.getItem().getDepartment().getId() + " and (a.fromPrice<" + billItem.getItem().getTotal() + " and a.toPrice >" + billItem.getItem().getTotal() + ")";
//        } else {
//            if (billItem.getItem().getCategory() == null || billItem.getItem().getDepartment() == null) {
//                billFee.setFeeValue(0.0);
//                return billFee;
//            }
//            sql = "select a from InwardPriceAdjustment a where a.retired=false and a.category.id="
//                    + billItem.getItem().getCategory().getId() + " and  a.department.id="
//                    + billItem.getItem().getDepartment().getId() + " and (a.fromPrice<" + billItem.getItem().getTotal() + " and a.toPrice >" + billItem.getItem().getTotal() + ")";
//        }
//
//        List<InwardPriceAdjustment> is = getInwardPriceAdjustmentFacade().findBySQL(sql);
//
//        if (is.size() <= 0) {
//            billFee.setFeeValue(0.0);
//            return billFee;
//        }
//
//        matrix.setFee((is.get(0).getMargin() * billItem.getItem().getTotal()) / 100);
//        billFee.setInstitution(billItem.getItem().getDepartment().getInstitution());
//        billFee.setFeeValue(matrix.getFee());
//
//        if (matrix.getId() == null) {
//            getFeeFacade().create(matrix);
//        } else {
//            getFeeFacade().edit(matrix);
//        }
//        return billFee;
//    }
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

        if (billItem.getItem() instanceof Investigation) {
            sql = "select a from InwardPriceAdjustment a where a.retired=false and a.category=:cat "
                    + " and  a.department=:dep and (a.fromPrice< :frPrice and a.toPrice >:tPrice)";

            hm.put("cat", ((Investigation) billItem.getItem()).getInvestigationCategory());

        } else {
            sql = "select a from InwardPriceAdjustment a where a.retired=false and a.category=:cat "
                    + " and  a.department=:dep and (a.fromPrice< :frPrice and a.toPrice >:tPrice)";

            hm.put("cat", billItem.getItem().getCategory());
        }

        hm.put("dep", department);
        hm.put("frPrice", serviceValue);
        hm.put("tPrice", serviceValue);

        InwardPriceAdjustment inwardPriceAdjustment = getInwardPriceAdjustmentFacade().findFirstBySQL(sql, hm);

        if (inwardPriceAdjustment == null) {
            return 0;
        }

        System.err.println(inwardPriceAdjustment);
        System.err.println(inwardPriceAdjustment.getMargin());
        System.err.println(serviceValue);
        return ((inwardPriceAdjustment.getMargin() * serviceValue) / 100);

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

    public double calTimedServiceCharge(PatientItem p, Date date) {
        TimedItemFee tmp = getTimedItemFee((TimedItem) p.getItem());
        long tempDur = tmp.getDurationHours();
        long tempOve = tmp.getOverShootHours();
        double tempFee = tmp.getFee();
        
        
        
        Date currentTime;

        if (date == null) {
            currentTime = Calendar.getInstance().getTime();
        } else {
            currentTime = date;
        }

        long tempServ = getCommonFunctions().calculateDurationMin(p.getFromTime(), currentTime);
        long count = 0l;

        if (tempServ != 0 && tempDur != 0) {
            count = tempServ / (tempDur * 60);
            if (((tempOve * 60) < tempServ % (tempDur * 60))) {
                count++;
            }

        }

        return (tempFee * count);
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

    public double calTotalLinen(List<PatientRoom> rmc) {
        long tmp, adm, dis = 0L;
        double totalLinen = 0.0;
        int i = 0;

        for (PatientRoom pr : rmc) {

            tmp = adm = pr.getAdmittedAt().getTime() / (1000 * 60 * 60 * 24);
            if (tmp != adm) {
                i = 0;
            }

            if (pr.getDischargedAt() != null) {
                dis = pr.getDischargedAt().getTime() / (1000 * 60 * 60 * 24);
            } else {
                dis = Calendar.getInstance().getTime().getTime() / (1000 * 60 * 60 * 24);
            }

            if (adm == dis) {
                if (i == 0) {
                    if (pr.getRoomFacilityCharge() != null && pr.getRoomFacilityCharge().getLinenCharge() != null) {
                        totalLinen += pr.getRoomFacilityCharge().getLinenCharge();
                    }
                } else {
                    totalLinen += pr.getAddedLinenCharge();
                }
                i++;
            }

            while (adm < dis) {
                totalLinen += pr.getRoomFacilityCharge().getLinenCharge();
                //System.out.println("adm : " + adm + " dis : " + dis);
                //System.out.println("Linen :" + totalLinen);
                adm++;
            }

        }

        return totalLinen;
    }

    public long calCountWithoutOverShoot(TimedItemFee tif, PatientRoom pr) {

        long tempDur = tif.getDurationHours();
        Long tempServ = 0L;

        tempServ = getCommonFunctions().calculateDurationMin(pr.getAdmittedAt(), pr.getDischargedAt());

        long count = tempServ / (tempDur * 60);

        if ((0 != tempServ % (tempDur * 60))) {
            count++;
        }

        return count;
    }

    public long calCount(TimedItemFee tif, Date admittedDate, Date dischargedDate) {

        long tempDur = tif.getDurationHours();
        long tempOve = tif.getOverShootHours();
        //  double tempFee = tif.getFee();
        Long tempServ = 0L;

        if (dischargedDate == null) {
            dischargedDate = new Date();
        }

        tempServ = getCommonFunctions().calculateDurationMin(admittedDate, dischargedDate);
        long count = 0;

        if (tempDur != 0) {
            count = tempServ / (tempDur * 60);
            if (((tempOve * 60) < tempServ % (tempDur * 60))) {
                count++;
            }
        }

        return count;
    }

    public ItemFeeFacade getItemFeeFacade() {
        return itemFeeFacade;
    }

    public void setItemFeeFacade(ItemFeeFacade itemFeeFacade) {
        this.itemFeeFacade = itemFeeFacade;
    }

    public InwardPriceAdjustmentFacade getInwardPriceAdjustmentFacade() {
        return inwardPriceAdjustmentFacade;
    }

    public void setInwardPriceAdjustmentFacade(InwardPriceAdjustmentFacade inwardPriceAdjustmentFacade) {
        this.inwardPriceAdjustmentFacade = inwardPriceAdjustmentFacade;
    }

    public FeeFacade getFeeFacade() {
        return feeFacade;
    }

    public void setFeeFacade(FeeFacade feeFacade) {
        this.feeFacade = feeFacade;
    }

    public AdmissionFacade getAdmissionFacade() {
        return admissionFacade;
    }

    public void setAdmissionFacade(AdmissionFacade admissionFacade) {
        this.admissionFacade = admissionFacade;
    }

    public PatientRoomFacade getPatientRoomFacade() {
        return patientRoomFacade;
    }

    public void setPatientRoomFacade(PatientRoomFacade patientRoomFacade) {
        this.patientRoomFacade = patientRoomFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }
}
