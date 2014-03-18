
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.bean.UtilityController;
import com.divudi.data.BillType;
import com.divudi.data.FeeType;
import com.divudi.data.PaymentMethod;
import com.divudi.entity.Bill;
import com.divudi.entity.BillComponent;
import com.divudi.entity.BillEntry;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BillSession;
import com.divudi.entity.Department;
import com.divudi.entity.Fee;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.ItemFee;
import com.divudi.entity.PackageFee;
import com.divudi.entity.Packege;
import com.divudi.entity.WebUser;
import com.divudi.entity.lab.Investigation;
import com.divudi.entity.lab.PatientInvestigation;
import com.divudi.facade.BillComponentFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.BillSessionFacade;
import com.divudi.facade.FeeFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.ItemFeeFacade;
import com.divudi.facade.PackageFeeFacade;
import com.divudi.facade.PackegeFacade;
import com.divudi.facade.PatientInvestigationFacade;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TemporalType;

/**
 *
 * @author Buddhika
 */
@Stateless
public class BillBean {

    @EJB
    private PatientInvestigationFacade patientInvestigationFacade;
    @EJB
    BillFacade billFacade;
    @EJB
    private ItemFacade itemFacade;
    @EJB
    private PackegeFacade packegeFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillComponentFacade billComponentFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private BillSessionFacade billSessionFacade;
    @EJB
    private ItemFeeFacade itemFeeFacade;
    @EJB
    private FeeFacade feeFacade;
    @EJB
    PackageFeeFacade packageFeeFacade;
    @EJB
    ServiceSessionBean serviceSessionBean;

    public ServiceSessionBean getServiceSessionBean() {
        return serviceSessionBean;
    }

    public void setServiceSessionBean(ServiceSessionBean serviceSessionBean) {
        this.serviceSessionBean = serviceSessionBean;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public PackageFeeFacade getPackageFeeFacade() {
        return packageFeeFacade;
    }

    public void setPackageFeeFacade(PackageFeeFacade packageFeeFacade) {
        this.packageFeeFacade = packageFeeFacade;
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public List<BillComponent> billComponentsFromBillEntries(List<BillEntry> billEntries) {
        List<BillComponent> bcs = new ArrayList<>();
        for (BillEntry be : billEntries) {
            for (BillComponent bc : be.getLstBillComponents()) {
                if (bc != null) {
                    bcs.add(bc);
                }
            }
        }
        return bcs;
    }

    public List<Bill> billsForTheDay(Date fromDate, Date toDate, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where b.createdAt is not null and b.billType = :billType and b.createdAt between :fromDate and :toDate and b.retired=false order by b.insId desc  ";

        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 100);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsForTheDay2(Date fromDate, Date toDate, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from PreBill b where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false order by b.id desc ";

        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsForTheDayNotPaid(Date fromDate, Date toDate, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from PreBill b where b.billType = :billType and b.referenceBill is null "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false order by b.id desc ";

        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsForTheDayNotPaid(BillType type, Department department) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from PreBill b where b.billType = :billType "
                + " and b.department=:dep and b.referenceBill is null and b.backwardReferenceBill is null "
                + " and b.forwardReferenceBill is null  "
                + " and b.retired=false and b.netTotal!=0 order by b.id desc ";

        temMap.put("billType", type);
        temMap.put("dep", department);

        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsRefundForTheDay(Date fromDate, Date toDate, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from RefundBill b where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false order by b.id desc ";

        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsForTheDay(Date fromDate, Date toDate, Institution ins, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where b.billType = :billType and b.institution.id=" + ins.getId() + " and b.createdAt between :fromDate and :toDate and b.retired=false order by b.id desc  ";

        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 100);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsForTheDayForLazy(Date fromDate, Date toDate, Institution ins, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where b.billType = :billType and b.institution.id=" + ins.getId() + " and b.createdAt between :fromDate and :toDate and b.retired=false order by b.id desc  ";

        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsFromSearch(String searchStr, Date fromDate, Date toDate, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType and b.retired=false"
                + " and  b.createdAt between :fromDate and :toDate"
                + " and (upper(b.patient.person.name) like '%" + searchStr.toUpperCase() + "%' "
                + " or upper(b.patient.person.phone) like '%" + searchStr.toUpperCase() + "%' "
                + " or upper(b.insId) like '%" + searchStr.toUpperCase() + "%') order by b.insId desc  ";
        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsFromSearch2(String searchStr, Date fromDate, Date toDate, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from PreBill b where"
                + " b.billType = :billType and b.retired=false"
                + " and  b.createdAt between :fromDate and :toDate"
                + " and (upper(b.patient.person.name) like '%" + searchStr.toUpperCase() + "%' "
                + " or upper(b.patient.person.phone) like '%" + searchStr.toUpperCase() + "%' "
                + " or upper(b.insId) like '%" + searchStr.toUpperCase() + "%') order by b.insId desc  ";
        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        //System.err.println("Search : " + sql);
        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsFromSearch(String searchStr, Date fromDate, Date toDate, Institution ins, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where b.billType = :billType and b.institution.id=" + ins.getId() + " and b.retired=false and  b.createdAt between :fromDate "
                + " and :toDate and (upper(b.patient.person.name) like '%" + searchStr.toUpperCase() + "%'  or upper(b.patient.person.phone) like '%" + searchStr.toUpperCase() + "%'  or upper(b.insId) like '%" + searchStr.toUpperCase() + "%') order by b.id desc  ";
        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsFromSearchForUser(String searchStr, Date fromDate, Date toDate, WebUser user, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        if (searchStr == null || searchStr.trim().equals("")) {
            sql = "select b from BilledBill b where b.billType = :billType and b.retired=false and  b.createdAt between :fromDate and :toDate and b.creater.id = " + user.getId() + " order by b.id desc  ";
        } else {
            sql = "select b from BilledBill b where b.billType = :billType and b.retired=false and  b.createdAt between :fromDate and :toDate and  b.creater.id = " + user.getId() + " and (upper(b.patient.person.name) like '%" + searchStr.toUpperCase() + "%'  or upper(b.patient.person.phone) like '%" + searchStr.toUpperCase() + "%'  or upper(b.insId) like '%" + searchStr.toUpperCase() + "%') order by b.id desc  ";
        }

        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        //System.out.println("sql ");
        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<Bill> billsFromSearchForUser(String searchStr, Date fromDate, Date toDate, WebUser user, Institution ins, BillType type) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        if (searchStr == null || searchStr.trim().equals("")) {
            sql = "select b from BilledBill b where b.billType = :billType and b.retired=false and b.institution.id=" + ins.getId() + " and b.createdAt between :fromDate and :toDate and b.creater.id = " + user.getId() + " order by b.id desc  ";
        } else {
            sql = "select b from BilledBill b where b.billType = :billType and b.retired=false and b.institution.id=" + ins.getId() + " and b.createdAt between :fromDate and :toDate and  b.creater.id = " + user.getId() + " and (upper(b.patient.person.name) like '%" + searchStr.toUpperCase() + "%'  or upper(b.patient.person.phone) like '%" + searchStr.toUpperCase() + "%'  or upper(b.insId) like '%" + searchStr.toUpperCase() + "%') order by b.id desc  ";
        }
        temMap.put("billType", type);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        //System.out.println("sql ");
        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        if (lstBills == null) {
            lstBills = new ArrayList<>();
        }
        return lstBills;
    }

    public List<BillFee> billFeesFromBillEntries(List<BillEntry> billEntries) {
        List<BillFee> bcs = new ArrayList<>();
        for (BillEntry be : billEntries) {
            for (BillFee bc : be.getLstBillFees()) {
                bcs.add(bc);
            }
        }
        return bcs;
    }

    public Double billTotalFromBillEntries(List<BillEntry> billEntries) {
        Double bcs = 0.0;
        for (BillEntry be : billEntries) {
            for (BillFee bc : be.getLstBillFees()) {
                bcs = bcs + bc.getFeeValue();
            }
        }
        return bcs;
    }

    public List<BillSession> billSessionsFromBillEntries(List<BillEntry> billEntries) {
        List<BillSession> bcs = new ArrayList<>();
        for (BillEntry be : billEntries) {
            for (BillSession bc : be.getLstBillSessions()) {
                bcs.add(bc);
            }
        }
        return bcs;
    }

    public List<BillItem> billItemsFromBillEntries(List<BillEntry> billEntries) {
        List<BillItem> bcs = new ArrayList<>();
        for (BillEntry be : billEntries) {
            BillItem bi = be.getBillItem();
            double ft = 0;
            for (BillFee bf : be.getLstBillFees()) {
                ft = +bf.getFeeValue();
            }
            bi.setRate(ft);
            bi.setGrossValue(ft);
            bi.setNetValue(ft);
            bcs.add(be.getBillItem());
        }
        return bcs;
    }

    public List<Item> itemFromPackage(Item packege) {

        String sql = "Select i from PackageItem p join p.item i where p.retired=false and p.packege.id = " + packege.getId();
        List<Item> packageItems = getItemFacade().findBySQL(sql);

        return packageItems;
    }

    public List<Item> itemFromMedicalPackage(Item packege) {

        String sql = "Select i from MedicalPackageItem p join p.item i where p.retired=false and p.packege.id = " + packege.getId();
        List<Item> packageItems = getItemFacade().findBySQL(sql);

        return packageItems;
    }

//    public List<Item> itemFromMedicalPackage(BillItem billItem) {
//
//        String sql = "Select i from MedicalPackageItem p join p.item i where p.retired=false and p.packege.id = " + billItem.getItem().getId();
//        List<Item> packageItems = getItemFacade().findBySQL(sql);
//
//        return packageItems;
//    }
//    public List<Item> itemFromMedicalPackage(BillItem billItem) {
//
//        String sql = "Select i from MedicalPackageItem p join p.item i where p.retired=false and p.packege.id = " + billItem.getItem().getId();
//        List<Item> packageItems = getItemFacade().findBySQL(sql);
//
//        return packageItems;
//    }
    public int checkDepartment(List<BillEntry> billEntrys) {
        int c = 0;
        Department tdep = new Department();
        tdep.setId(0L);
        for (BillEntry be : billEntrys) {
            if (be.getBillItem().getItem().getDepartment().getId() != tdep.getId()) {
                tdep = be.getBillItem().getItem().getDepartment();
                c++;
            }
        }
        return c;
    }

    public void saveBillItem(Bill b, BillEntry e, WebUser wu) {
        e.getBillItem().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        e.getBillItem().setCreater(wu);
        e.getBillItem().setBill(b);
        getBillItemFacade().create(e.getBillItem());

        saveBillComponent(e, b, wu);
        saveBillFee(e, b, wu);
    }

//    public void calculateBillItem(Bill bill, BillEntry e) {
//        double s = 0.0;
//        double i = 0.0;
//        double tot = 0.0;
//        double net = 0.0;
//        for (BillFee bf : e.getLstBillFees()) {
//            tot += bf.getFee().getFee();
//            net += bf.getFeeValue();
//            if (bf.getFee().getStaff() == null) {
//                i = i + bf.getFeeValue();
//            } else {
//                s = s + bf.getFeeValue();
//            }
//            if (bf.getId() == null || bf.getId() == 0) {
//                getBillFeeFacade().create(bf);
//            } else {
//                getBillFeeFacade().edit(bf);
//            }
//        }
//
//        bill.setStaffFee(s);
//        bill.setPerformInstitutionFee(i);
//        bill.setTotal(tot);
//        bill.setDiscount(tot - net);
//        bill.setDiscountPercent(((tot - net) / tot) * 100);
//        bill.setNetTotal(net);
//        getBillFacade().edit(bill);
//    }
    public void calculateBillItems(Bill bill, List<BillEntry> billEntrys) {
        double s = 0.0;
        double i = 0.0;
        double val = 0.0;

        for (BillEntry e : billEntrys) {
            for (BillFee bf : e.getLstBillFees()) {
                //   tot += bf.getFeeGrossValue();
                val += bf.getFeeValue();
                if (bf.getFee().getFeeType() != FeeType.Staff) {
                    i = i + bf.getFeeValue();
                } else {
                    s = s + bf.getFeeValue();
                }
                if (bf.getId() == null || bf.getId() == 0) {
                    getBillFeeFacade().create(bf);
                } else {
                    getBillFeeFacade().edit(bf);
                }
            }
        }
        bill.setStaffFee(s);
        bill.setPerformInstitutionFee(i);

        bill.setTotal(val);
        bill.setNetTotal(val);

        getBillFacade().edit(bill);
    }

//    public void calculateBillItems(Bill bill, List<BillEntry> billEntrys) {
//        double s = 0.0;
//        double i = 0.0;
//        double tot = 0.0;
//        double net = 0.0;
//        for (BillEntry e : billEntrys) {
//            for (BillFee bf : e.getLstBillFees()) {
//                tot += bf.getFee().getFee();
//                net += bf.getFeeValue();
//                if (bf.getFee().getStaff() == null) {
//                    i = i + bf.getFeeValue();
//                } else {
//                    s = s + bf.getFeeValue();
//                }
//                if (bf.getId() == null || bf.getId() == 0) {
//                    getBillFeeFacade().create(bf);
//                } else {
//                    getBillFeeFacade().edit(bf);
//                }
//            }
//        }
//        bill.setStaffFee(s);
//        bill.setPerformInstitutionFee(i);
//
//        if (tot > net) {
//            bill.setTotal(tot);
//            bill.setDiscount(tot - net);
//            bill.setDiscountPercent(((tot - net) / tot) * 100);
//            bill.setNetTotal(net);
//        } else {
//            bill.setTotal(net);
//            bill.setDiscount(0.0);
//            bill.setNetTotal(net);
//        }
//
//        getBillFacade().edit(bill);
//    }
    public void calculateBillItems(Bill bill, BillEntry e) {
        double s = 0.0;
        double i = 0.0;
        double tot = 0.0;
        double net = 0.0;

        for (BillFee bf : e.getLstBillFees()) {
            tot += bf.getFee().getFee();
            net += bf.getFeeValue();
            if (bf.getFee().getStaff() == null) {
                i = i + bf.getFeeValue();
            } else {
                s = s + bf.getFeeValue();
            }
            if (bf.getId() == null || bf.getId() == 0) {
                getBillFeeFacade().create(bf);
            } else {
                getBillFeeFacade().edit(bf);
            }
        }

        bill.setStaffFee(s);
        bill.setPerformInstitutionFee(i);

        if (tot > net) {
            bill.setTotal(tot);
            bill.setDiscount(tot - net);
            bill.setDiscountPercent(((tot - net) / tot) * 100);
            bill.setNetTotal(net);
        } else {
            bill.setTotal(net);
            bill.setDiscount(0.0);
            bill.setNetTotal(net);
        }

        getBillFacade().edit(bill);
    }

    public void saveBillItems(Bill b, List<BillEntry> billEntries, WebUser wu) {
        for (BillEntry e : billEntries) {
            // BillItem temBi = e.getBillItem();
            e.getBillItem().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            e.getBillItem().setCreater(wu);
            e.getBillItem().setBill(b);
            getBillItemFacade().create(e.getBillItem());
            ////System.out.println("Saving Bill Item : " + e.getBillItem().getItem().getName());

            saveBillComponent(e, b, wu);
            saveBillFee(e, b, wu);
//            if (b.getBillType() != BillType.InwardBill && e.getBillItem() != null) {
//                
//                e.getBillItem().setBillSession(getServiceSessionBean().saveBillSession(e.getBillItem()));
//            }
            getBillItemFacade().edit(e.getBillItem());
        }
    }

    public void saveBillItems(Bill b, BillEntry e, WebUser wu) {

        // BillItem temBi = e.getBillItem();
        e.getBillItem().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        e.getBillItem().setCreater(wu);
        e.getBillItem().setBill(b);
        getBillItemFacade().create(e.getBillItem());
        ////System.out.println("Saving Bill Item : " + e.getBillItem().getItem().getName());

        saveBillComponent(e, b, wu);
        saveBillFee(e, b, wu);
//            if (b.getBillType() != BillType.InwardBill && e.getBillItem() != null) {
//                
//                e.getBillItem().setBillSession(getServiceSessionBean().saveBillSession(e.getBillItem()));
//            }
        getBillItemFacade().edit(e.getBillItem());

    }

    private void saveBillFee(BillEntry e, Bill b, WebUser wu) {
        for (BillFee bf : e.getLstBillFees()) {
            bf.setCreatedAt(Calendar.getInstance().getTime());
            bf.setCreater(wu);

            if (bf.getPatienEncounter() != null) {
                bf.setPatienEncounter(b.getPatientEncounter());
            }
            bf.setPatient(b.getPatient());

            bf.setBill(b);
            getBillFeeFacade().create(bf);
        }
    }

    private void savePatientInvestigation(BillEntry e, BillComponent bc, WebUser wu) {
        PatientInvestigation ptIx = new PatientInvestigation();

        ptIx.setCreatedAt(Calendar.getInstance().getTime());
        ptIx.setCreater(wu);

        ptIx.setBillItem(e.getBillItem());
        ptIx.setBillComponent(bc);
        ptIx.setPackege(bc.getPackege());
        ptIx.setApproved(Boolean.FALSE);
        ptIx.setCancelled(Boolean.FALSE);
        ptIx.setCollected(Boolean.FALSE);
        ptIx.setDataEntered(Boolean.FALSE);
        ptIx.setInvestigation((Investigation) bc.getItem());
        ptIx.setOutsourced(Boolean.FALSE);
        ptIx.setPatient(e.getBillItem().getBill().getPatient());

        if (e.getBillItem().getBill().getPatientEncounter() != null) {
            ptIx.setEncounter(e.getBillItem().getBill().getPatientEncounter());
        }

        ptIx.setPerformed(Boolean.FALSE);
        ptIx.setPrinted(Boolean.FALSE);
        ptIx.setPrinted(Boolean.FALSE);
        ptIx.setReceived(Boolean.FALSE);

        ptIx.setReceiveDepartment(e.getBillItem().getItem().getDepartment());
        ptIx.setApproveDepartment(e.getBillItem().getItem().getDepartment());
        ptIx.setDataEntryDepartment(e.getBillItem().getItem().getDepartment());
        ptIx.setPrintingDepartment(e.getBillItem().getItem().getDepartment());
        ptIx.setPerformDepartment(e.getBillItem().getItem().getDepartment());

        if (e.getBillItem().getItem() == null) {
            UtilityController.addErrorMessage("No Bill Item Selected");
        } else if (e.getBillItem().getItem().getDepartment() == null) {
            UtilityController.addErrorMessage("Under administration, add a Department for this investigation " + e.getBillItem().getItem().getName());
        } else if (e.getBillItem().getItem().getDepartment().getInstitution() == null) {
            UtilityController.addErrorMessage("Under administration, add an Institution for the department " + e.getBillItem().getItem().getDepartment());
        } else if (e.getBillItem().getItem().getDepartment().getInstitution() != wu.getInstitution()) {
            ptIx.setOutsourcedInstitution(e.getBillItem().getItem().getInstitution());
        }

        ptIx.setRetired(false);
        getPatientInvestigationFacade().create(ptIx);

    }

    private void saveBillComponent(BillEntry e, Bill b, WebUser wu) {
        for (BillComponent bc : e.getLstBillComponents()) {

            bc.setCreatedAt(Calendar.getInstance().getTime());
            bc.setCreater(wu);

            bc.setDepartment(b.getDepartment());
            bc.setInstitution(b.getDepartment().getInstitution());

            bc.setBill(b);
            getBillComponentFacade().create(bc);

            if (bc.getItem() instanceof Investigation) {
                savePatientInvestigation(e, bc, wu);
            }

        }
    }

    public List<BillComponent> billComponentsFromBillItem(BillItem billItem) {
        String sql;
        List<BillComponent> t = new ArrayList<>();
        BillComponent b;
        if (billItem.getItem() instanceof Packege) {
            sql = "Select i from PackageItem p join p.item i where p.packege.id = " + billItem.getItem().getId();
            List<Item> packageItems = getItemFacade().findBySQL(sql);
            for (Item i : packageItems) {
                b = new BillComponent();
                BillItem bit = new BillItem();
                b.setBillItem(bit);
                b.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
                b.setItem(i);
                b.setName(i.getName());
                b.setPackege((Packege) billItem.getItem());
                b.setStaff(i.getStaff());
                b.setSpeciality(i.getSpeciality());
                t.add(b);
            }

        } else {
            b = new BillComponent();
            b.setBillItem(billItem);
            b.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            b.setItem(billItem.getItem());
            //System.out.println("Bill Item is " + billItem.getItem());
            b.setName(billItem.getItem().getName());
            b.setPackege(null);
            b.setStaff(billItem.getItem().getStaff());
            b.setSpeciality(billItem.getItem().getSpeciality());
            t.add(b);
        }

        return t;
    }

    public Double billItemRate(BillEntry billEntry) {
        Double temTot = 0.0;
        for (BillFee f : billEntry.getLstBillFees()) {
            temTot += f.getFeeValue();
        }
        return temTot;
    }

    public List<BillSession> billSessionsfromBillItem(BillItem billItem) {
        //TODO: Create Logic
        return null;
    }

   
    public List<BillFee> billFeefromBillItemPackage(BillItem billItem, Item packege) {
        List<BillFee> t = new ArrayList<>();
        BillFee f;
        String sql;
        sql = "Select f from PackageFee f where f.retired=false and f.packege.id=" + packege.getId()
                + " and f.item.id = " + billItem.getItem().getId();
        List<PackageFee> packFee = getPackageFeeFacade().findBySQL(sql);
        for (Fee i : packFee) {
            f = new BillFee();
            f.setFee(i);
            f.setFeeValue(i.getFee());
            //      //System.out.println("Fee Value is " + f.getFeeValue());
            // f.setBill(billItem.getBill());
            f.setBillItem(billItem);
            f.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            if (billItem.getItem().getDepartment() != null) {
                f.setDepartment(billItem.getItem().getDepartment());
            } else {
                //  f.setDepartment(billItem.getBill().getDepartment());
            }
            if (billItem.getItem().getInstitution() != null) {
                f.setInstitution(billItem.getItem().getInstitution());
            } else {
                //   f.setInstitution(billItem.getBill().getDepartment().getInstitution());
            }
            if (i.getStaff() != null) {
                f.setStaff(i.getStaff());
            } else {
                f.setStaff(null);
            }
            f.setSpeciality(i.getSpeciality());
            t.add(f);

        }
        return t;
    }

    public List<Fee> getMedicalPackageFee(Item packege, Item item) {
        String sql;
        sql = "Select f from MedicalPackageFee f where f.retired=false and f.packege.id=" + packege.getId() + " and f.item.id = " + item.getId();
        return getFeeFacade().findBySQL(sql);
    }

    public List<Fee> getPackageFee(Item packege, Item item) {
        String sql;
        sql = "Select f from PackageFee f where f.retired=false and f.packege.id=" + packege.getId() + " and f.item.id = " + item.getId();
        return getFeeFacade().findBySQL(sql);
    }

    public List<BillFee> billFeefromBillItemMedicalPackage(BillItem billItem, Item packege) {
        List<BillFee> t = new ArrayList<>();
        BillFee f;
        String sql;
        sql = "Select f from MedicalPackageFee f where f.retired=false and f.packege.id=" + packege.getId() + " and f.item.id = " + billItem.getItem().getId();
        List<PackageFee> packFee = getPackageFeeFacade().findBySQL(sql);
        for (Fee i : packFee) {
            f = new BillFee();
            f.setFee(i);
            f.setFeeValue(i.getFee());
            f.setBillItem(billItem);
            f.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            if (billItem.getItem().getDepartment() != null) {
                f.setDepartment(billItem.getItem().getDepartment());
            } else {
                //  f.setDepartment(billItem.getBill().getDepartment());
            }
            if (billItem.getItem().getInstitution() != null) {
                f.setInstitution(billItem.getItem().getInstitution());
            } else {
                //   f.setInstitution(billItem.getBill().getDepartment().getInstitution());
            }
            if (i.getStaff() != null) {
                f.setStaff(i.getStaff());
            } else {
                f.setStaff(null);
            }
            f.setSpeciality(i.getSpeciality());
            t.add(f);

        }
        return t;
    }

    public List<BillFee> billFeefromBillItem(BillItem billItem) {
        List<BillFee> t = new ArrayList<>();
        BillFee f;
        String sql;
        if (billItem.getItem() instanceof Packege) {
            sql = "Select i from PackageItem p join p.item i where p.retired=false and p.packege.id = " + billItem.getItem().getId();
            List<Item> packageItems = getItemFacade().findBySQL(sql);
            for (Item pi : packageItems) {
                sql = "Select f from PackageFee f where f.retired=false and f.packege.id = " + billItem.getItem().getId() + " and f.item.id = " + pi.getId();
                List<PackageFee> packFee = getPackageFeeFacade().findBySQL(sql);
                for (Fee i : packFee) {
                    f = new BillFee();
                    f.setFee(i);
                    f.setFeeValue(i.getFee());
                    //  f.setBill(billItem.getBill());
                    f.setBillItem(billItem);
                    f.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
                    if (pi.getDepartment() != null) {
                        f.setDepartment(pi.getDepartment());
                    } else {
                        // f.setDepartment(billItem.getBill().getDepartment());
                    }
                    if (pi.getInstitution() != null) {
                        f.setInstitution(pi.getInstitution());
                    } else {
                        // f.setInstitution(billItem.getBill().getDepartment().getInstitution());
                    }
                    if (i.getStaff() != null) {
                        f.setStaff(i.getStaff());
                    } else {
                        f.setStaff(null);
                    }
                    f.setSpeciality(i.getSpeciality());
                    f.setStaff(i.getStaff());
                    t.add(f);

                }
            }
        } else {
            sql = "Select f from ItemFee f where f.retired=false and f.item.id = " + billItem.getItem().getId();
            List<ItemFee> itemFee = getItemFeeFacade().findBySQL(sql);
            for (Fee i : itemFee) {
                f = new BillFee();
                f.setFee(i);
                f.setFeeValue(i.getFee());
                //System.out.println("Fee Value is " + f.getFeeValue());
                // f.setBill(billItem.getBill());
                f.setBillItem(billItem);
                f.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
                if (billItem.getItem().getDepartment() != null) {
                    f.setDepartment(billItem.getItem().getDepartment());
                } else {
                    //  f.setDepartment(billItem.getBill().getDepartment());
                }
                if (billItem.getItem().getInstitution() != null) {
                    f.setInstitution(billItem.getItem().getInstitution());
                } else {
                    //   f.setInstitution(billItem.getBill().getDepartment().getInstitution());
                }
                if (i.getStaff() != null) {
                    f.setStaff(i.getStaff());
                } else {
                    f.setStaff(null);
                }
                f.setSpeciality(i.getSpeciality());
                t.add(f);
            }
        }
        return t;
    }

    public double totalFeeforItem(Item item) {
        List<BillFee> t = new ArrayList<>();
        Double bf = 0.0;
        BillFee f;
        String sql;
        if (item instanceof Packege) {
            sql = "Select i from PackageItem p join p.item i where i.retired=false and p.packege.id = " + item.getId();
            List<Item> packageItems = getItemFacade().findBySQL(sql);
            for (Item pi : packageItems) {
                sql = "Select f from PackageFee f where f.retired=false and f.packege.id = " + item.getId() + " and f.item.id = " + pi.getId();
                List<PackageFee> packFee = getPackageFeeFacade().findBySQL(sql);
                for (Fee i : packFee) {
                    bf = +i.getFee();
                }
            }
        } else {
            sql = "Select f from ItemFee f where f.retired=false and f.item.id = " + item.getId();
            List<ItemFee> itemFee = getItemFeeFacade().findBySQL(sql);
            for (Fee i : itemFee) {
                bf = +i.getFee();
            }
        }
        return bf;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade aItemFacade) {
        itemFacade = aItemFacade;
    }

    public PackegeFacade getPackegeFacade() {
        return packegeFacade;
    }

    public void setPackegeFacade(PackegeFacade aPackegeFacade) {
        packegeFacade = aPackegeFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade aBillItemFacade) {
        billItemFacade = aBillItemFacade;
    }

    public BillComponentFacade getBillComponentFacade() {
        return billComponentFacade;
    }

    public void setBillComponentFacade(BillComponentFacade aBillComponentFacade) {
        billComponentFacade = aBillComponentFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade aBillFeeFacade) {
        billFeeFacade = aBillFeeFacade;
    }

    public BillSessionFacade getBillSessionFacade() {
        return billSessionFacade;
    }

    public void setBillSessionFacade(BillSessionFacade aBillSessionFacade) {
        billSessionFacade = aBillSessionFacade;
    }

    public ItemFeeFacade getItemFeeFacade() {
        return itemFeeFacade;
    }

    public void setItemFeeFacade(ItemFeeFacade aItemFeeFacade) {
        itemFeeFacade = aItemFeeFacade;
    }

    public FeeFacade getFeeFacade() {
        return feeFacade;
    }

    public void setFeeFacade(FeeFacade feeFacade) {
        this.feeFacade = feeFacade;
    }

    public PatientInvestigationFacade getPatientInvestigationFacade() {
        return patientInvestigationFacade;
    }

    public void setPatientInvestigationFacade(PatientInvestigationFacade patientInvestigationFacade) {
        this.patientInvestigationFacade = patientInvestigationFacade;
    }

    public String checkPaymentMethod(PaymentMethod paymentMethod, Institution institution, String string, Date date) {
        switch (paymentMethod) {
            case Cheque:
                if (institution == null || string == null || date == null) {
                    return "Please select Cheque Number,Bank and Cheque Date";
                }

            case Slip:
                if (institution == null || string == null || date == null) {
                    return "Please Fill Memo,Bank and Slip Date ";
                }

            case Card:
                if (institution == null || string == null) {
                    return "Please Fill Credit Card Number and Bank";
                }
                if (string.trim().length() < 16) {
                    return "Enter 16 Digit";
                }
            case Credit:
                if (institution == null) {
                    return "Please Select Credit Company";
                }

        }

        if (institution != null && paymentMethod != PaymentMethod.Credit) {
            return "Please Select Payment Scheme with Credit";
        }

        return "";

    }
}
