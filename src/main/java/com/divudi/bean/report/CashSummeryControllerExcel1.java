/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.report;

import com.divudi.bean.CategoryController;
import com.divudi.bean.inward.AdmissionTypeController;
import com.divudi.data.BillType;
import com.divudi.data.FeeType;
import com.divudi.data.dataStructure.BillsItems;
import com.divudi.data.dataStructure.CategoryWithItem;
import com.divudi.data.dataStructure.DailyCash;
import com.divudi.data.dataStructure.DepartmentPayment;
import com.divudi.data.dataStructure.ItemWithFee;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.AdmissionTypeBills;
import com.divudi.data.table.String1Value2;
import com.divudi.data.table.String1Value3;
import com.divudi.data.table.String1Value1;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.BillBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Category;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.PreBill;
import com.divudi.entity.RefundBill;
import com.divudi.entity.inward.AdmissionType;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.CategoryFacade;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.ItemFacade;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Buddhika
 */
@Named
@RequestScoped
public class CashSummeryControllerExcel1 implements Serializable {

    private Institution institution;
    // private List<DailyCash> dailyCashs;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fromDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date toDate;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private DepartmentFacade departmentFacade;
    @EJB
    private CategoryFacade categoryFacade;
    @EJB
    private ItemFacade itemFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    private List<Bill> inwardPayments;
    private List<ItemWithFee> itemWithFees;
    List<String1Value3> otherInstitution;
    @EJB
    private BillBean billBean;
    private Item service;
    private double doctorPaymentTot = 0.0;
    private List<DepartmentPayment> departmentPayments;
    private double agentCollectionTot;
    private List<Bill> agentCollections;
    private List<BillsItems> creditCompanyCollections;
    private double creditCompanyTotal = 0.0;
    private double grantTotal;
    private double cardTot;
    private double chequeTot;
    private double slipTot;
    private double inwardTot;
    private double inwardProfTot;
    private double otherProfessionalTotal;
    private double otherHospitalTotal;
    private List<Bill> cardBill;
    private List<Bill> slipBill;
    private List<Bill> chequeBill;
    private List<DailyCash> pharmacySales;
    private double pharmacyTotal;
    @Inject
    private AdmissionTypeController admissionTypeController;
    private List<String1Value1> finalSumery;
    private List<String1Value2> string1Value2s;
    private List<String1Value1> collections2Hos;
    private List<String1Value1> inwardProfessions;
    private List<AdmissionTypeBills> admissionTypeBillses;
    @Inject
    private CategoryController categoryController;

    public long getCountTotal() {
        long countTotal = 0;

        long billed = getCount(new BilledBill());
        long cancelled = getCount(new CancelledBill());
        long refunded = getCount(new RefundBill());

        countTotal = billed - (refunded + cancelled);
        return countTotal;
    }

    private long getCount(Bill bill) {
        String sql;
        Map temMap = new HashMap();
        sql = "select count(bi) FROM BillItem bi where bi.bill.billType=:bType and bi.item=:itm "
                + " and type(bi.bill)=:billClass and bi.bill.toInstitution=:ins "
                + " and bi.bill.createdAt between :fromDate and :toDate order by bi.item.name";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("itm", getService());
        temMap.put("billClass", bill.getClass());
        temMap.put("bType", BillType.OpdBill);
        temMap.put("ins", getInstitution());
        return getBillItemFacade().countBySql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void makeNull() {

    }

    private void createFinalSummery() {
        System.err.println("createFinalSummery");
        finalSumery = new ArrayList<>();
        String1Value1 dd;
        dd = new String1Value1();
        dd.setString("Net Cash");
        dd.setValue(getNetCash());
        finalSumery.add(dd);

        dd = new String1Value1();
        dd.setString("Lab Handover Total");

        finalSumery.add(dd);

        dd = new String1Value1();
        dd.setString("Final Cash");

        finalSumery.add(dd);

    }

    public List<String1Value1> getBankingData() {
        List<String1Value1> tmp = new ArrayList<>();
        String1Value1 dd;
        dd = new String1Value1();
        dd.setString("People Bank");
        tmp.add(dd);

        dd = new String1Value1();
        dd.setString("HNB 1");
        tmp.add(dd);

        dd = new String1Value1();
        dd.setString("HNB 2");
        tmp.add(dd);

        dd = new String1Value1();
        dd.setString("BOC");
        tmp.add(dd);

        dd = new String1Value1();
        dd.setString("NSB");
        tmp.add(dd);

        dd = new String1Value1();
        dd.setString("B/F Cash Ballance");
        tmp.add(dd);

        return tmp;
    }

    private void createCollections2Hos() {
        System.err.println("createCollections2Hos");
        collections2Hos = new ArrayList<>();
        String1Value1 dd;
        dd = new String1Value1();
        dd.setString("Collection For the Day");
        dd.setValue(getGrantTotal2Hos());
        collections2Hos.add(dd);

        dd = new String1Value1();
        dd.setString("Petty cash Payments");
        dd.setValue(getPettyTot());
        collections2Hos.add(dd);

    }

    private void createInwardProfessions() {
        System.err.println("createInwardProfessions");
        inwardProfTot = 0.0;
        inwardProfessions = new ArrayList<>();

        for (AdmissionType at : getAdmissionTypeController().getItems()) {
            AdmissionTypeBills admB = new AdmissionTypeBills();
            admB.setAdmissionType(at);
            admB.setTotal(getInwardProfTot(at));
            inwardProfTot += admB.getTotal();

            if (admB.getTotal() != 0) {
                String1Value1 dd;
                dd = new String1Value1();
                dd.setString(admB.getAdmissionType().getName());
                dd.setValue(admB.getTotal());
                inwardProfessions.add(dd);
            }
        }

    }

    private List<Department> getDepartmentOfInstitution() {
        String sql = "select d from Department d where d.retired=false and d.institution=:ins";
        HashMap hm = new HashMap();
        hm.put("ins", getInstitution());

        return getDepartmentFacade().findBySQL(sql, hm);
    }

    private double getDepartmentSale(Department d) {
        String sql = "Select sum(b.netTotal) from Bill b "
                + " where b.retired=false"
                + " and  b.billType=:bType"
                + " and b.referenceBill.department=:dep "
                + " and b.createdAt between :fromDate and :toDate "
                + " and (b.paymentMethod = :pm1 "
                + " or  b.paymentMethod = :pm2 "
                + " or  b.paymentMethod = :pm3 "
                + " or  b.paymentMethod = :pm4)";
        HashMap hm = new HashMap();
        hm.put("bType", BillType.PharmacySale);
        hm.put("dep", d);
        hm.put("fromDate", getFromDate());
        hm.put("toDate", getToDate());
        hm.put("pm1", PaymentMethod.Cash);
        hm.put("pm2", PaymentMethod.Card);
        hm.put("pm3", PaymentMethod.Cheque);
        hm.put("pm4", PaymentMethod.Slip);
        double netTotal = getBillFacade().findDoubleByJpql(sql, hm, TemporalType.TIMESTAMP);

        return netTotal;
    }

    private void createPharmacySale() {
        System.err.println("createPharmacySale");
        pharmacySales = new ArrayList<>();
        pharmacyTotal = 0;
        for (Department d : getDepartmentOfInstitution()) {
            //System.err.println("DEP " + d.getName());
            double netTotal = getDepartmentSale(d);
            if (netTotal != 0) {
                //System.err.println("NET " + netTotal);
                pharmacyTotal += netTotal;
                DailyCash dl = new DailyCash();
                dl.setDepartment(d);
                dl.setDepartmentTotal(netTotal);
                pharmacySales.add(dl);
            }

        }

    }

    private void createOtherInstituion() {
        System.err.println("createOtherInstituion");
        otherInstitution = new ArrayList<>();
        otherHospitalTotal = getOtherInstitutionFees(FeeType.OwnInstitution);
        otherProfessionalTotal = getOtherInstitutionFees(FeeType.Staff);
        if ((otherHospitalTotal + otherProfessionalTotal) != 0) {
            String1Value3 tmp = new String1Value3();
            tmp.setString("Outer Institution");
            tmp.setValue1(otherHospitalTotal);
            tmp.setValue2(otherProfessionalTotal);
            tmp.setValue3(otherHospitalTotal + otherProfessionalTotal);

            otherInstitution.add(tmp);
        }
    }

    public List<String1Value3> getOtherInstitution() {
        return otherInstitution;
    }

    public void setOtherInstitution(List<String1Value3> otherInstitution) {
        this.otherInstitution = otherInstitution;
    }

    public List<ItemWithFee> getItemWithFees() {
        return itemWithFees;
    }

    public void setItemWithFees(List<ItemWithFee> itemWithFees) {
        this.itemWithFees = itemWithFees;
    }

    public double getNetCash() {
        double tmp = grantTotal - doctorPaymentTot + getPettyTot() - cardTot - slipTot - chequeTot - inwardProfTot;

        return tmp;
    }

    private void createCardBill() {
        System.err.println("createCardBill");
        cardTot = getBillTotal(PaymentMethod.Card);
        cardBill = getBills(PaymentMethod.Card);

    }

    private List<Bill> getBills(PaymentMethod paymentMethod) {
        List<Bill> lstBills;
        String sql;
        Map temMap = new HashMap();
        sql = "select b from Bill b where type(b)!=:type "
                + " and b.institution=:ins "
                + " and b.paymentMethod = :bTp "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false order by b.id desc  ";

        temMap.put("bTp", paymentMethod);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        temMap.put("type", PreBill.class);
        temMap.put("ins", getInstitution());
        lstBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        return lstBills;

    }

    private double getBillTotal(PaymentMethod paymentMethod) {

        String sql;
        Map temMap = new HashMap();
        sql = "select sum(b.netTotal) from Bill b where type(b)!=:type "
                + " and b.institution=:ins "
                + " and b.paymentMethod = :bTp "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false order by b.id desc  ";

        temMap.put("bTp", paymentMethod);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        temMap.put("type", PreBill.class);
        temMap.put("ins", getInstitution());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private void createSlipBill() {
        System.err.println("createSlipBill");
        slipTot = getBillTotal(PaymentMethod.Slip);
        slipBill = getBills(PaymentMethod.Slip);

    }

    private void createChequeBill() {
        System.err.println("createChequeBill");
        chequeTot = getBillTotal(PaymentMethod.Cheque);
        chequeBill = getBills(PaymentMethod.Cheque);

    }

    public double getOtherInstitutionFees(FeeType feeType) {
        String sql = "SELECT sum(bf.feeValue) FROM BillFee bf"
                + " WHERE bf.billItem.bill.institution=:ins "
                + " and bf.billItem.item.institution!=:ins "
                + " and bf.fee.feeType=:ftp "
                + " and  bf.createdAt between :fromDate and :toDate "
                + " and ( bf.billItem.bill.paymentMethod = :pm1 "
                + " or  bf.billItem.bill.paymentMethod = :pm2 "
                + " or  bf.billItem.bill.paymentMethod = :pm3"
                + "  or  bf.billItem.bill.paymentMethod = :pm4)";

        HashMap temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getInstitution());
        temMap.put("ftp", feeType);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        double val = getBillFeeFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

        return val;
    }

    private List<Bill> getBills(BillType billType) {
        String sql;
        sql = "SELECT b FROM Bill b WHERE b.retired=false and b.billType = :bTp and "
                + "b.institution=:ins and b.createdAt between :fromDate and :toDate order by b.id";

        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bTp", billType);
        temMap.put("ins", getInstitution());

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
    }

    private double getBillTotal(BillType billType) {
        String sql;
        sql = "SELECT sum(b.netTotal) FROM Bill b WHERE b.retired=false and b.billType = :bTp and "
                + "b.institution=:ins and b.createdAt between :fromDate and :toDate order by b.id";

        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bTp", billType);
        temMap.put("ins", getInstitution());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    private List<BillItem> getBillItems(Bill b) {
        String sql;
        HashMap temMap = new HashMap();

        sql = "SELECT b FROM BillItem b WHERE "
                + "b.bill.institution=:ins "
                + " and b.retired=false "
                + " and b.bill=:bl "
                + " and b.createdAt between :fromDate and :toDate "
                + " order by b.id";

        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bl", b);
        temMap.put("ins", getInstitution());
        return getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
    }

    private void createCreditCompanyCollection() {
        System.err.println("createCreditCompanyCollection");
        creditCompanyTotal = 0.0;
        creditCompanyCollections = new ArrayList<>();
        List<Bill> tmp = getBills(BillType.CashRecieveBill);
        creditCompanyTotal = getBillTotal(BillType.CashRecieveBill);
        for (Bill b : tmp) {

            BillsItems newB = new BillsItems();
            newB.setBill(b);
            newB.setBillItems(getBillItems(b));

            creditCompanyCollections.add(newB);
        }

    }

    private void createAgentCollection() {
        System.err.println("createAgentCollection");
        agentCollectionTot = 0.0;

        agentCollections = getBills(BillType.AgentPaymentReceiveBill);

        agentCollectionTot = getBillTotal(BillType.AgentPaymentReceiveBill);

    }

    public void createInwardCollection() {
        System.err.println("createInwardCollection");
        inwardTot = 0.0;
        admissionTypeBillses = new ArrayList<>();
        for (AdmissionType at : getAdmissionTypeController().getItems()) {
            AdmissionTypeBills admB = new AdmissionTypeBills();
            admB.setAdmissionType(at);
            admB.setBills(getInwardBills(at));
            admB.setTotal(getInwardBillTotal(at));
            inwardTot += admB.getTotal();
            admissionTypeBillses.add(admB);
        }
    }

    public List<AdmissionTypeBills> getInwardCollection() {

        return admissionTypeBillses;
    }

//    public List<String2Value1> getInwardCollection(){
//        for(AdmissionTypeBills adm:)
//    
//    }
    private List<Bill> getInwardBills(AdmissionType admissionType) {
        String sql;
        sql = "SELECT b FROM Bill b WHERE "
                + " (type(b)=:class1 or type(b)=:class2 or type(b)=:class3) and"
                + " b.retired=false and b.billType = :bTp "
                + "and b.patientEncounter.admissionType=:adm  and b.institution=:ins"
                + " and b.createdAt between :fromDate and :toDate order by b.id";
        Map temMap = new HashMap();
        temMap.put("class1", BilledBill.class);
        temMap.put("class2", CancelledBill.class);
        temMap.put("class3", RefundBill.class);
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bTp", BillType.InwardPaymentBill);
        temMap.put("adm", admissionType);
        temMap.put("ins", getInstitution());
        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
    }

    private double getInwardBillTotal(AdmissionType admissionType) {
        String sql;
        sql = "SELECT sum(b.netTotal) FROM Bill b WHERE "
                + " (type(b)=:class1 or type(b)=:class2 or type(b)=:class3) and"
                + " b.retired=false and b.billType = :bTp "
                + "and b.patientEncounter.admissionType=:adm  and b.institution=:ins"
                + " and b.createdAt between :fromDate and :toDate order by b.id";
        Map temMap = new HashMap();
        temMap.put("class1", BilledBill.class);
        temMap.put("class2", CancelledBill.class);
        temMap.put("class3", RefundBill.class);
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bTp", BillType.InwardPaymentBill);
        temMap.put("adm", admissionType);
        temMap.put("ins", getInstitution());
        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public double getInwardProfTot(AdmissionType adt) {

        HashMap temMap = new HashMap();

        String sql = "SELECT sum(b.netValue) FROM BillItem b WHERE "
                + " b.bill.billType=:btp"
                + " and b.referenceBill.billType=:refBtp1 "
                + " and b.referenceBill.billType=:refBtp2 "
                + " and b.referenceBill.patientEncounter.admissionType=:admis"
                + " and b.retired=false "
                + " and b.bill.createdAt between :fromDate and :toDate";

        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", BillType.PaymentBill);
        temMap.put("refBtp1", BillType.InwardBill);
        temMap.put("refBtp2", BillType.InwardProfessional);
        temMap.put("admis", adt);
        return getBillItemFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Department> getDepartmentList() {
        String sql = "SELECT distinct(b.referanceBillItem.item.department)"
                + " FROM BillItem b WHERE "
                + " b.bill.institution=:ins"
                + "  and b.retired=false "
                + " and b.bill.billType= :btp "
                + " and b.createdAt between :fromDate and :toDate";
        HashMap temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", BillType.PaymentBill);
        temMap.put("ins", getInstitution());

        return getDepartmentFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
    }

    private double getDepartmentPaymentTotal(Department department) {
        String sql = "SELECT sum(b.netValue)"
                + " FROM BillItem b WHERE "
                + " b.bill.institution=:ins"
                + "  and b.retired=false "
                + "  and b.referanceBillItem.item.department=:dep "
                + " and b.bill.billType= :btp "
                + " and b.createdAt between :fromDate and :toDate";
        HashMap temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("dep", department);
        temMap.put("btp", BillType.PaymentBill);
        temMap.put("ins", getInstitution());

        return getDepartmentFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    private double getDepartmentPaymentTotal() {
        String sql = "SELECT sum(b.netValue)"
                + " FROM BillItem b WHERE "
                + " b.bill.institution=:ins"
                + "  and b.retired=false "
                + " and b.bill.billType= :btp "
                + " and b.createdAt between :fromDate and :toDate";
        HashMap temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", BillType.PaymentBill);
        temMap.put("ins", getInstitution());

        return getDepartmentFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    public void createDepartmentPayment() {
        System.err.println("createDepartmentPayment");
        doctorPaymentTot = 0.0;

        List<Department> depList = getDepartmentList();

        departmentPayments = new ArrayList<>();

        for (Department dep : depList) {
            DepartmentPayment dp = new DepartmentPayment();
            dp.setDepartment(dep);

            double tot = 0.0;

            dp.setTotalPayment(getDepartmentPaymentTotal(dep));
            departmentPayments.add(dp);
        }

        doctorPaymentTot = getDepartmentPaymentTotal();
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return toDate;
    }

    private List<Item> findItem(Category d) {
        String sql;
        Map temMap = new HashMap();
        if (d == null) {
            return new ArrayList<>();
        }
        sql = "select distinct(bi.item) FROM BillItem bi where "
                + "  bi.bill.institution=:ins "
                + " and  bi.bill.billType= :bTp  "
                + " and bi.item.category.id=" + d.getId() + ""
                + " and  bi.bill.createdAt between :fromDate and :toDate "
                + " and ( bi.bill.paymentMethod = :pm1 or  bi.bill.paymentMethod = :pm2 "
                + " or  bi.bill.paymentMethod = :pm3 or  bi.bill.paymentMethod = :pm4)";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getInstitution());
        temMap.put("bTp", BillType.OpdBill);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        List<Item> tmp = getItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        return tmp;

    }

    private long getCount(Item i) {
        long billed, cancelled, refunded;
        billed = cancelled = refunded = 0l;

        billed = billItemForCount(new BilledBill(), i);
        cancelled = billItemForCount(new CancelledBill(), i);
        refunded = billItemForCount(new RefundBill(), i);

        return billed - (cancelled + refunded);

    }

    private long billItemForCount(Bill bill, Item i) {

        Map temMap = new HashMap();
        String sql;

        sql = "select count(bi) FROM BillItem bi where"
                + "  bi.bill.institution=:ins "
                + " and bi.item=:itm"
                + " and (bi.bill.paymentMethod = :pm1 "
                + " or bi.bill.paymentMethod = :pm2"
                + "  or bi.bill.paymentMethod = :pm3"
                + "  or bi.bill.paymentMethod = :pm4) "
                + "and bi.bill.billType=:btp "
                + " and type(bi.bill)=:billClass "
                + "and bi.bill.createdAt between :fromDate and :toDate "
                + " order by bi.item.name";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getInstitution());
        temMap.put("itm", i);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        temMap.put("billClass", bill.getClass());
        temMap.put("btp", BillType.OpdBill);

        return getBillItemFacade().countBySql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;

    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public DepartmentFacade getDepartmentFacade() {
        return departmentFacade;
    }

    public void setDepartmentFacade(DepartmentFacade departmentFacade) {
        this.departmentFacade = departmentFacade;
    }

    private double getFee(Item i, FeeType feeType) {
        String sql = "SELECT sum(bf.feeValue) FROM BillFee bf WHERE "
                + " bf.bill.billType=:bTp and bf.fee.feeType=:ftp "
                + " and bf.bill.institution=:ins and bf.bill.createdAt between :fromDate and :toDate "
                + "  and bf.billItem.item=:itm"
                + " and ( bf.bill.paymentMethod = :pm1 or  bf.bill.paymentMethod = :pm2"
                + " or  bf.bill.paymentMethod = :pm3 or  bf.bill.paymentMethod = :pm4)";

        HashMap temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getInstitution());
        temMap.put("itm", i);
        temMap.put("bTp", BillType.OpdBill);
        temMap.put("ftp", feeType);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        return getBillFeeFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double getFee(Category cat, FeeType feeType) {
        String sql = "SELECT sum(bf.feeValue) FROM BillFee bf WHERE "
                + " bf.bill.billType=:bTp and bf.fee.feeType=:ftp "
                + " and bf.bill.institution=:ins and bf.bill.createdAt between :fromDate and :toDate "
                + "  and bf.billItem.item.category=:cat "
                + " and ( bf.bill.paymentMethod = :pm1 or  bf.bill.paymentMethod = :pm2"
                + " or  bf.bill.paymentMethod = :pm3 or  bf.bill.paymentMethod = :pm4)";

        HashMap temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getInstitution());
        temMap.put("cat", cat);
        temMap.put("bTp", BillType.OpdBill);
        temMap.put("ftp", feeType);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        return getBillFeeFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double getFee(FeeType feeType) {
        String sql = "SELECT sum(bf.feeValue) FROM BillFee bf WHERE "
                + " bf.bill.billType=:bTp and bf.fee.feeType=:ftp "
                + " and bf.bill.institution=:ins and bf.bill.createdAt between :fromDate and :toDate "
                + " and ( bf.bill.paymentMethod = :pm1 or  bf.bill.paymentMethod = :pm2"
                + " or  bf.bill.paymentMethod = :pm3 or  bf.bill.paymentMethod = :pm4)";

        HashMap temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getInstitution());
        temMap.put("bTp", BillType.OpdBill);
        temMap.put("ftp", feeType);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        return getBillFeeFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public double getPettyTot() {

        double tmp = getBillTotal(BillType.PettyCash);

        return tmp;
    }

    public void createCashCategoryWithoutPro() {
        createOPdCategoryTable();
        createOtherInstituion();
        createPharmacySale();
        createInwardCollection();
        createAgentCollection();
        createCreditCompanyCollection();
        createCollections2Hos();
        createDepartmentPayment();
        createInwardProfessions();
        createCardBill();
        createChequeBill();
        createSlipBill();
        createFinalSummery();
    }

    public void createOPdCategoryTable() {
        System.err.println("createOPdCategoryTable");
        string1Value2s = new ArrayList<>();
        for (Category cat : getCategoryController().getServiceCategory()) {          
            for (Item i : findItem(cat)) {              
                double count = getCount(i);
                double hos = getFee(i, FeeType.OwnInstitution);
                //     double pro = getFee(i, FeeType.Staff);

                if (count != 0) {
                    String1Value2 newD = new String1Value2();
                    newD.setString(i.getName());
                    newD.setValue1(count);
                    newD.setValue2(hos);
                    newD.setSummery(false);
                    string1Value2s.add(newD);
                }
            }

            String1Value2 newD = new String1Value2();
            newD.setString(cat.getName() + " Total : ");
            newD.setValue2(getFee(cat, FeeType.OwnInstitution));
            newD.setSummery(true);
            string1Value2s.add(newD);

        }

        calNonCashTot();

    }

    public List<String1Value2> getDailyCashExcel() {
        return string1Value2s;
    }

    private double getSumByBill(PaymentMethod paymentMethod) {
        String sql = "SELECT sum(b.netTotal) FROM Bill b WHERE"
                + "  b.institution=:ins "
                + " and  b.createdAt between :fromDate and :toDate"
                + " and  b.paymentMethod = :pm "
                + " and b.billType=:btp1 "
                + " and b.billType=:btp2"
                + " and b.billType=:btp3"
                + " and b.billType=:btp4"
                + " and b.billType=:btp5";

        HashMap temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getInstitution());
        temMap.put("pm", paymentMethod);
        temMap.put("btp1", BillType.OpdBill);
        temMap.put("btp2", BillType.PaymentBill);
        temMap.put("btp3", BillType.AgentPaymentReceiveBill);
        temMap.put("btp4", BillType.CashRecieveBill);
        temMap.put("btp5", BillType.PettyCash);
        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);
    }

    private void calNonCashTot() {
        cardTot = getSumByBill(PaymentMethod.Card);
        chequeTot = getSumByBill(PaymentMethod.Cheque);
        slipTot = getSumByBill(PaymentMethod.Slip);

    }

    public CategoryFacade getCategoryFacade() {
        return categoryFacade;
    }

    public void setCategoryFacade(CategoryFacade categoryFacade) {
        this.categoryFacade = categoryFacade;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public void setInwardPayments(List<Bill> inwardPayments) {
        this.inwardPayments = inwardPayments;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public double getGrantTotal2Hos() {
        grantTotal = 0.0;

        grantTotal = getOtherProfessionalTotal()
                + getOtherHospitalTotal()
                + getOpdHospitalTotal()
                + getCreditCompanyTotal()
                + getAgentCollectionTot()
                + getInwardTot()
                + getPharmacyTotal();

        return grantTotal;
    }

    public double getOpdHospitalTotal() {
        double tmp = getFee(FeeType.OwnInstitution);

        return tmp;
    }

    public void setGrantTotal(double grantTotal) {
        this.grantTotal = grantTotal;
    }

    public double getCreditCompanyTotal() {
        return creditCompanyTotal;
    }

    public void setCreditCompanyTotal(double creditCompanyTotal) {
        this.creditCompanyTotal = creditCompanyTotal;
    }

    public double getCardTot() {
        return cardTot;
    }

    public void setCardTot(double cardTot) {
        this.cardTot = cardTot;
    }

    public double getChequeTot() {
        return chequeTot;
    }

    public void setChequeTot(double chequeTot) {
        this.chequeTot = chequeTot;
    }

    public double getSlipTot() {
        return slipTot;
    }

    public void setSlipTot(double slipTot) {
        this.slipTot = slipTot;
    }

    public double getAgentCollectionTot() {
        return agentCollectionTot;
    }

    public void setAgentCollectionTot(double agentCollectionTot) {
        this.agentCollectionTot = agentCollectionTot;
    }

    public double getDoctorPaymentTot() {
        return doctorPaymentTot;
    }

    public void setDoctorPaymentTot(double doctorPaymentTot) {
        this.doctorPaymentTot = doctorPaymentTot;
    }

    public AdmissionTypeController getAdmissionTypeController() {
        return admissionTypeController;
    }

    public void setAdmissionTypeController(AdmissionTypeController admissionTypeController) {
        this.admissionTypeController = admissionTypeController;
    }

    public double getInwardTot() {
        return inwardTot;
    }

    public void setInwardTot(double inwardTot) {
        this.inwardTot = inwardTot;
    }

    public double getInwardProfTot() {
        return inwardProfTot;
    }

    public void setInwardProfTot(double inwardProfTot) {
        this.inwardProfTot = inwardProfTot;
    }

    public Item getService() {
        return service;
    }

    public void setService(Item service) {
        this.service = service;
    }

    public double getPharmacyTotal() {
        return pharmacyTotal;
    }

    public void setPharmacyTotal(double pharmacyTotal) {
        this.pharmacyTotal = pharmacyTotal;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public List<String1Value2> getString1Value2s() {
        return string1Value2s;
    }

    public void setString1Value2s(List<String1Value2> string1Value2s) {
        this.string1Value2s = string1Value2s;
    }

    public List<AdmissionTypeBills> getAdmissionTypeBillses() {
        return admissionTypeBillses;
    }

    public void setAdmissionTypeBillses(List<AdmissionTypeBills> admissionTypeBillses) {
        this.admissionTypeBillses = admissionTypeBillses;
    }

    public List<DailyCash> getPharmacySales() {
        return pharmacySales;
    }

    public void setPharmacySales(List<DailyCash> pharmacySales) {
        this.pharmacySales = pharmacySales;
    }

    public List<BillsItems> getCreditCompanyCollections() {
        return creditCompanyCollections;
    }

    public void setCreditCompanyCollections(List<BillsItems> creditCompanyCollections) {
        this.creditCompanyCollections = creditCompanyCollections;
    }

    public List<Bill> getAgentCollections() {
        return agentCollections;
    }

    public void setAgentCollections(List<Bill> agentCollections) {
        this.agentCollections = agentCollections;
    }

    public List<String1Value1> getCollections2Hos() {
        return collections2Hos;
    }

    public void setCollections2Hos(List<String1Value1> collections2Hos) {
        this.collections2Hos = collections2Hos;
    }

    public List<DepartmentPayment> getDepartmentPayments() {
        return departmentPayments;
    }

    public void setDepartmentPayments(List<DepartmentPayment> departmentPayments) {
        this.departmentPayments = departmentPayments;
    }

    public List<String1Value1> getInwardProfessions() {
        return inwardProfessions;
    }

    public void setInwardProfessions(List<String1Value1> inwardProfessions) {
        this.inwardProfessions = inwardProfessions;
    }

    public List<String1Value1> getFinalSumery() {
        return finalSumery;
    }

    public void setFinalSumery(List<String1Value1> finalSumery) {
        this.finalSumery = finalSumery;
    }

    public List<Bill> getCardBill() {
        return cardBill;
    }

    public void setCardBill(List<Bill> cardBill) {
        this.cardBill = cardBill;
    }

    public List<Bill> getSlipBill() {
        return slipBill;
    }

    public void setSlipBill(List<Bill> slipBill) {
        this.slipBill = slipBill;
    }

    public List<Bill> getChequeBill() {
        return chequeBill;
    }

    public void setChequeBill(List<Bill> chequeBill) {
        this.chequeBill = chequeBill;
    }

    public List<Bill> getInwardPayments() {
        return inwardPayments;
    }

    public double getGrantTotal() {
        return grantTotal;
    }

    public double getOtherProfessionalTotal() {
        return otherProfessionalTotal;
    }

    public void setOtherProfessionalTotal(double otherProfessionalTotal) {
        this.otherProfessionalTotal = otherProfessionalTotal;
    }

    public double getOtherHospitalTotal() {
        return otherHospitalTotal;
    }

    public void setOtherHospitalTotal(double otherHospitalTotal) {
        this.otherHospitalTotal = otherHospitalTotal;
    }

    public CategoryController getCategoryController() {
        return categoryController;
    }

    public void setCategoryController(CategoryController categoryController) {
        this.categoryController = categoryController;
    }

}
