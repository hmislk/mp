/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.data.FeeType;
import com.divudi.data.inward.AdmissionTypeEnum;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.Fee;
import com.divudi.entity.InwardPriceAdjustment;
import com.divudi.entity.ItemFee;
import com.divudi.entity.PatientItem;
import com.divudi.entity.inward.AdmissionType;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.inward.TimedItem;
import com.divudi.entity.inward.TimedItemFee;
import com.divudi.entity.lab.Investigation;
import com.divudi.facade.AdmissionFacade;
import com.divudi.facade.FeeFacade;
import com.divudi.facade.InwardPriceAdjustmentFacade;
import com.divudi.facade.ItemFeeFacade;
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

    public List<BillFee> billFeeFromBillItemWithMatrix(BillItem billItem) {

        List<BillFee> billFeeList = new ArrayList<>();
        BillFee billFee;
        String sql;
        sql = "Select f from ItemFee f where f.retired=false and f.item.id = " + billItem.getItem().getId();
        List<ItemFee> itemFee = getItemFeeFacade().findBySQL(sql);

        for (Fee i : itemFee) {
            billFee = new BillFee();
            billFee.setFee(i);
            billFee.setFeeValue(i.getFee());
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

        BillFee bf = calInwardMargin(billItem);

        if (bf.getFeeValue() != 0.0) {
            billFeeList.add(bf);
        }

        return billFeeList;
    }

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
    private BillFee calInwardMargin(BillItem billItem) {
        BillFee billFee = new BillFee();
        Fee matrix = createMatrixFee();
        String sql;
        HashMap hm = new HashMap();

        billFee.setBillItem(billItem);
        billFee.setFee(matrix);
        billFee.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

        if (billItem.getItem() instanceof Investigation) {
            sql = "select a from InwardPriceAdjustment a where a.retired=false and a.category=:cat "
                    + " and  a.department=:dep and (a.fromPrice< :frPrice and a.toPrice >:tPrice)";

            hm.put("cat", ((Investigation) billItem.getItem()).getInvestigationCategory());

        } else {
            sql = "select a from InwardPriceAdjustment a where a.retired=false and a.category=:cat "
                    + " and  a.department=:dep and (a.fromPrice< :frPrice and a.toPrice >:tPrice)";

            hm.put("cat", billItem.getItem().getCategory());
        }

        hm.put("dep", billItem.getItem().getDepartment());
        hm.put("frPrice", billItem.getItem().getTotal());
        hm.put("tPrice", billItem.getItem().getTotal());

        List<InwardPriceAdjustment> is = getInwardPriceAdjustmentFacade().findBySQL(sql, hm);

        if (is.size() <= 0) {
            billFee.setFeeValue(0.0);
            return billFee;
        }

        matrix.setFee((is.get(0).getMargin() * billItem.getItem().getTotal()) / 100);
        billFee.setInstitution(billItem.getItem().getDepartment().getInstitution());
        billFee.setFeeValue(matrix.getFee());

        if (matrix.getId() == null) {
            getFeeFacade().create(matrix);
        } else {
            getFeeFacade().edit(matrix);
        }
        return billFee;
    }

    private Fee createMatrixFee() {
        String sql = "Select f from Fee f where f.retired=false and f.feeType=:nm";
        HashMap hm = new HashMap();
        hm.put("nm", FeeType.Matrix);
        List<Fee> fee = getFeeFacade().findBySQL(sql, hm, TemporalType.TIME);
        Fee matrix;

        if (fee.size() <= 0) {
            matrix = new Fee();
            matrix.setName("Matrix");
            matrix.setFeeType(FeeType.Matrix);
            matrix.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            return matrix;
        } else {
            return fee.get(0);
        }
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
}
