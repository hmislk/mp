/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.report;

import com.divudi.bean.SessionController;
import com.divudi.data.BillType;
import com.divudi.data.DailySummeryRow;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.BillsTotals;
import com.divudi.data.table.String1Value1;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.RefundBill;
import com.divudi.entity.WebUser;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.util.JsfUtil;
import com.divudi.util.CommonDateFunctions;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.TemporalType;

/**
 *
 * @author www.divudi.com
 */
@Named
@SessionScoped
public class CommonReportSession implements Serializable {

    @Inject
    SessionController sessionController;
    ///////////////////
    @EJB
    private BillFacade billFacade;
    @EJB
    CommonFunctions commonFunctions;
    ////////////////////
    private Institution collectingIns;
    Institution institution;
    private Date fromDate;
    private Date toDate;
    Date billDate;
    private WebUser webUser;
    private Department department;
    private BillType billType;
    private Institution creditCompany;
    /////////////////////
    private BillsTotals billedBills;
    private BillsTotals cancellededBills;
    private BillsTotals refundedBills;
    private BillsTotals billedBillsPh;
    private BillsTotals billedBillsPh2;
    private BillsTotals cancellededBillsPh;
    private BillsTotals cancellededBillsPh2;
    private BillsTotals refundedBillsPh;
    private BillsTotals refundedBillsPh2;
    private BillsTotals paymentBills;
    private BillsTotals paymentCancelBills;
    private BillsTotals pettyPayments;
    private BillsTotals pettyPaymentsCancel;
    private BillsTotals cashRecieves;
    private BillsTotals cashRecieveCancel;
    private BillsTotals agentRecieves;
    private BillsTotals agentCancelBill;
    private BillsTotals inwardPayments;
    private BillsTotals inwardPaymentCancel;
    //////////////////    
    private List<String1Value1> dataTableData;

    List<Bill> profitBills;
    List<Bill> purchaseBills;
    List<DailySummeryRow> dailySummeryRows;
    Institution dealer;

    Double profitTotal = 0.0;
    double grossTotal = 0.0;
    double netTotal = 0.0;
    double discountTotal = 0.0;
    double freeTotal = 0.0;

    
    BillsTotals GrnPaymentBill;
    BillsTotals GrnPaymentCancell;
    BillsTotals GrnPaymentReturn;
    BillsTotals GrnPaymentCancellReturn;
    private Institution supplier;
    @EJB
    BillItemFacade billItemFacade;
    
    public void createGrnPaymentBySupplierTable() {
        recreteModal();

        GrnPaymentBill = new BillsTotals();
        GrnPaymentCancell = new BillsTotals();
        GrnPaymentReturn = new BillsTotals();
        GrnPaymentCancellReturn = new BillsTotals();

        if (getDepartment() == null) {
            JsfUtil.addErrorMessage("Select Department to proceed");
            return;
        }

        //GRN Payment Billed Bills
        getGrnPaymentBill().setBillItems(grnBillItems(new BilledBill(), BillType.GrnPayment, getDepartment(), getSupplier()));
        getGrnPaymentBill().setCash(calValueUsingBillItem(new BilledBill(), BillType.GrnPayment, PaymentMethod.Cash, getDepartment(), getSupplier()));
        getGrnPaymentBill().setCredit(calValueUsingBillItem(new BilledBill(), BillType.GrnPayment, PaymentMethod.Credit, getDepartment(), getSupplier()));

        //GRN Payment Cancelled Bill
        getGrnPaymentCancell().setBillItems(grnBillItems(new CancelledBill(), BillType.GrnPayment, getDepartment(), getSupplier()));
        getGrnPaymentCancell().setCash(calValueUsingBillItem(new CancelledBill(), BillType.GrnPayment, PaymentMethod.Cash, getDepartment(), getSupplier()));
        getGrnPaymentCancell().setCredit(calValueUsingBillItem(new CancelledBill(), BillType.GrnPayment, PaymentMethod.Credit, getDepartment(), getSupplier()));

        //GRN Payment Refunded Bill
        getGrnPaymentReturn().setBillItems(grnBillItems(new BilledBill(), BillType.GrnPayment, getDepartment(), getSupplier()));
        getGrnPaymentReturn().setCash(calValueUsingBillItem(new BilledBill(), BillType.GrnPayment, PaymentMethod.Cash, getDepartment(), getSupplier()));
        getGrnPaymentReturn().setCredit(calValueUsingBillItem(new BilledBill(), BillType.GrnPayment, PaymentMethod.Credit, getDepartment(), getSupplier()));

        //GRN Payment Refunded Bill Cancel
        getGrnPaymentCancellReturn().setBillItems(grnBillItems(new CancelledBill(), BillType.GrnPayment, getDepartment(), getSupplier()));
        getGrnPaymentCancellReturn().setCash(calValueUsingBillItem(new CancelledBill(), BillType.GrnPayment, PaymentMethod.Cash, getDepartment(), getSupplier()));
        getGrnPaymentCancellReturn().setCredit(calValueUsingBillItem(new CancelledBill(), BillType.GrnPayment, PaymentMethod.Credit, getDepartment(), getSupplier()));

    }
    
    private List<BillItem> grnBillItems(Bill billClass, BillType billType, Department dep, Institution ins) {

        Map temMap = new HashMap();

        String sql = "SELECT b FROM BillItem b WHERE type(b.bill)=:bill "
                + " and b.bill.retired=false "
                + " and b.bill.billType = :btp "
                + " and b.bill.department=:d "
                + " and b.bill.createdAt between :fromDate and :toDate ";

        if (ins != null) {
            sql += " and (b.bill.fromInstitution=:ins or b.bill.toInstitution=:ins ) ";
            temMap.put("ins", ins);
        }
        sql += " order by b.bill.deptId";

        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("d", dep);

        return getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double calValueUsingBillItem(Bill billClass, BillType billType, PaymentMethod paymentMethod, Department dep, Institution ins) {
        
         Map temMap = new HashMap();
        
        String sql = "SELECT sum(b.referenceBill.netTotal) FROM BillItem b WHERE type(b.bill)=:bill "
                + " and b.bill.retired=false "
                + " and b.bill.billType=:btp "
                + " and b.bill.department=:d "
                + " and b.bill.paymentMethod=:pm "
                + " and b.bill.createdAt between :fromDate and :toDate";
        
        if(ins != null){
           sql += " and (b.bill.fromInstitution=:ins or b.bill.toInstitution=:ins) ";
           temMap.put("ins", ins);
        }
        
       
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", billType);
        temMap.put("pm", paymentMethod);
        temMap.put("d", dep);
        temMap.put("bill", billClass.getClass());

        return getBillItemFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }
    
    public List<String1Value1> getGRNPaymentTotal() {
        List<BillsTotals> list = new ArrayList<>();
        list.add(getGrnPaymentBill());
        list.add(getGrnPaymentCancell());
        list.add(getGrnPaymentReturn());
        list.add(getGrnPaymentCancellReturn());

        dataTableData = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list));

        String1Value1 tmp5 = new String1Value1();
        tmp5.setString("Final Cash Total");
        tmp5.setValue(getFinalCashTotal(list));

        String1Value1 tmp6 = new String1Value1();
        tmp6.setString("Final Credit & Cash Total");
        tmp6.setValue(getFinalCashTotal(list) + getFinalCreditTotal(list));

        dataTableData.add(tmp1);
        dataTableData.add(tmp5);
        dataTableData.add(tmp6);

        return dataTableData;
    }
    
    
    public List<Bill> getPurchaseBills() {
        return purchaseBills;
    }

    public void setPurchaseBills(List<Bill> purchaseBills) {
        this.purchaseBills = purchaseBills;
    }

    
    
    public String listPurchaseAndGrnBills() {
    //    System.out.println("list profit bills");
        String sql = "SELECT b FROM Bill b "
                + " WHERE (type(b)=:bc1 or type(b)=:bc2 or type(b)=:bc3 ) "
                + " and b.retired=false "
                + " and (b.billType=:bt1 or b.billType=:bt2) "
                + " and b.createdAt between :fromDate and :toDate ";

        Map temMap = new HashMap();

        if (department != null) {
            sql += " and b.department=:d ";
            temMap.put("d", department);
        }

        if (dealer != null) {
            sql += " and b.fromInstitution=:fi ";
            temMap.put("fi", dealer);
        }
        
        sql += " order by b.deptId  ";

        temMap.put("bc1", BilledBill.class);
        temMap.put("bc2", RefundBill.class);
        temMap.put("bc3", CancelledBill.class);

        temMap.put("bt1", BillType.PharmacyPurchaseBill);
        temMap.put("bt2", BillType.PharmacyGrnBill);

        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());

        purchaseBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        profitTotal = 0.0;
        grossTotal = 0.0;
        netTotal = 0.0;
        discountTotal = 0.0;
        freeTotal = 0.0;

        for (Bill b : purchaseBills) {
            grossTotal += b.getTotal();
            netTotal += b.getNetTotal();
            profitTotal += b.getNetTotal();
            discountTotal += b.getDiscount();
            if (discountTotal != 0.00) {
            //    System.out.println("b = " + b);
            //    System.out.println("b.getDiscount() = " + b.getDiscount());
            //    System.out.println("b.getId() = " + b.getId());
            }
            freeTotal += b.getFreeValue();
        }
        return "pharmacy_report_purchase_bill_list";
    }

    public String listPurchaseAndGrnBillsDailySummery() {
    //    System.out.println("list profit bills");
        String jpql;
        jpql = "SELECT new com.divudi.data.DailySummeryRow(FUNC('DATE',b.createdAt), sum(b.freeValue), sum(b.netTotal), sum(b.discount),  sum(b.total)) "
                + " FROM Bill b "
                + " WHERE (type(b)=:bc1 or type(b)=:bc2 or type(b)=:bc3 ) "
                + " and b.retired=false "
                + " and (b.billType=:bt1 or b.billType=:bt2) "
                + " and FUNC('DATE',b.createdAt) between :fromDate and :toDate ";

        Map temMap = new HashMap();

        if (department != null) {
            jpql += " and b.department=:d ";
            temMap.put("d", department);
        }

        if (dealer != null) {
            jpql += " and b.fromInstitution=:fi ";
            temMap.put("fi", dealer);
        }
        
        jpql += " group by FUNC('DATE',b.createdAt) "
                + "order by FUNC('DATE',b.createdAt)  ";

        temMap.put("bc1", BilledBill.class);
        temMap.put("bc2", RefundBill.class);
        temMap.put("bc3", CancelledBill.class);

        temMap.put("bt1", BillType.PharmacyPurchaseBill);
        temMap.put("bt2", BillType.PharmacyGrnBill);


        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());

        List<Object[]> dsso = getBillFacade().findAggregates(jpql, temMap, TemporalType.DATE);
        grossTotal = 0.0;
        profitTotal = 0.0;
        discountTotal = 0.0;
        freeTotal = 0.0;
        dailySummeryRows = new ArrayList<>();
        if (dsso == null) {
            dsso = new ArrayList<>();
        //    System.out.println("new list as null");
        }
        for (Object b : dsso) {
            DailySummeryRow dsr = (DailySummeryRow) b;
            grossTotal += dsr.getGrossTotal();
            profitTotal += dsr.getProfit();
            discountTotal += dsr.getDiscounts();
            freeTotal += dsr.getFreeAmounts();
            dailySummeryRows.add(dsr);
        }
        return "pharmacy_report_purchase_bill_list_ds";
    }

    public String listProfitBills() {
    //    System.out.println("list profit bills");
        String sql = "SELECT b FROM Bill b "
                + " WHERE (type(b)=:bc1 or type(b)=:bc2 or type(b)=:bc3 ) "
                + " and b.retired=false "
                + " and (b.billType=:bt1 or b.billType=:bt2 or b.billType=:bt3) "
                + " and b.createdAt between :fromDate and :toDate ";

        Map temMap = new HashMap();

        if (department != null) {
            sql += " and b.department=:d ";
            temMap.put("d", department);
        }

//        if (dealer != null) {
//            sql += " and b.fromInstitution=:fi ";
//            temMap.put("fi", dealer);
//        }
        sql += " order by b.deptId  ";

        temMap.put("bc1", BilledBill.class);
        temMap.put("bc2", RefundBill.class);
        temMap.put("bc3", CancelledBill.class);

        temMap.put("bt1", BillType.PharmacyPurchaseBill);
        temMap.put("bt2", BillType.PharmacyGrnBill);
        temMap.put("bt3", BillType.PharmacySale);

        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());

        profitBills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        profitTotal = 0.0;
        grossTotal = 0.0;
        netTotal = 0.0;
        discountTotal = 0.0;
        freeTotal = 0.0;

//    //    System.out.println("temMap = " + temMap);
//    //    System.out.println("sql = " + sql);
        for (Bill b : profitBills) {
            grossTotal += b.getTotal();
            netTotal += b.getNetTotal();
            profitTotal += (b.getNetTotal()+ b.getFreeValue());
            discountTotal += b.getDiscount();
//            if (discountTotal != 0.00) {
//            //    System.out.println("b = " + b);
//            //    System.out.println("b.getDiscount() = " + b.getDiscount());
//            //    System.out.println("b.getId() = " + b.getId());
//            }
            freeTotal += b.getFreeValue();
        }
        return "pharmacy_report_gross_profit_by_bills";
    }

    public String listProfitBillsDailySummery() {
    //    System.out.println("list profit bills");
        String jpql;
        jpql = "SELECT new com.divudi.data.DailySummeryRow(FUNC('DATE',b.createdAt), sum(b.freeValue), sum(b.netTotal+b.freeValue), sum(b.discount),  sum(b.total)) "
                + " FROM Bill b "
                + " WHERE (type(b)=:bc1 or type(b)=:bc2 or type(b)=:bc3 ) "
                + " and b.retired=false "
                + " and (b.billType=:bt1 or b.billType=:bt2 or b.billType=:bt3) "
                + " and FUNC('DATE',b.createdAt) between :fromDate and :toDate ";

        Map temMap = new HashMap();

        if (department != null) {
            jpql += " and b.department=:d ";
            temMap.put("d", department);
        }

//        if (dealer != null) {
//            jpql += " and b.fromInstitution=:fi ";
//            temMap.put("fi", dealer);
//        }
        jpql += " group by FUNC('DATE',b.createdAt) "
                + "order by FUNC('DATE',b.createdAt)  ";

        temMap.put("bc1", BilledBill.class);
        temMap.put("bc2", RefundBill.class);
        temMap.put("bc3", CancelledBill.class);

        temMap.put("bt1", BillType.PharmacyPurchaseBill);
        temMap.put("bt2", BillType.PharmacyGrnBill);
        temMap.put("bt3", BillType.PharmacySale);

        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());

        List<Object[]> dsso = getBillFacade().findAggregates(jpql, temMap, TemporalType.DATE);
        grossTotal = 0.0;
        profitTotal = 0.0;
        discountTotal = 0.0;
        freeTotal = 0.0;
        dailySummeryRows = new ArrayList<>();
        if (dsso == null) {
            dsso = new ArrayList<>();
        //    System.out.println("new list as null");
        }
        for (Object b : dsso) {
            DailySummeryRow dsr = (DailySummeryRow) b;
            grossTotal += dsr.getGrossTotal();
            profitTotal += dsr.getProfit();
            discountTotal += dsr.getDiscounts();
            freeTotal += dsr.getFreeAmounts();
            dailySummeryRows.add(dsr);
        }
        return "pharmacy_report_gross_profit_by_bills_ds";
    }

    public List<Bill> getProfitBills() {
        return profitBills;
    }

    public void setProfitBills(List<Bill> profitBills) {
        this.profitBills = profitBills;
    }

    public Double getProfitTotal() {
        return profitTotal;
    }

    public void setProfitTotal(Double profitTotal) {
        this.profitTotal = profitTotal;
    }

    public double getGrossTotal() {
        return grossTotal;
    }

    public void setGrossTotal(double grossTotal) {
        this.grossTotal = grossTotal;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public double getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(double discountTotal) {
        this.discountTotal = discountTotal;
    }

    public double getFreeTotal() {
        return freeTotal;
    }

    public void setFreeTotal(double freeTotal) {
        this.freeTotal = freeTotal;
    }

    /**
     * Creates a new instance of CommonReport
     */
    public CommonReportSession() {
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
        recreteModal();
    }

    public BillType[] getBillTypes() {
        return BillType.values();
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
        recreteModal();
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
        recreteModal();
    }

    public WebUser getWebUser() {

        return webUser;
    }

    public void setWebUser(WebUser webUser) {
        this.webUser = webUser;
        recreteModal();
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
        recreteModal();
    }

    public List<Bill> getBillsByReferingDoc() {

        Map temMap = new HashMap();
        List<Bill> tmp;

        String sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.billType = :bTp and b.createdAt between :fromDate and :toDate  order by b.referredBy.person.name";
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bTp", BillType.OpdBill);

        tmp = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (tmp == null) {
            tmp = new ArrayList<Bill>();
        }

        return tmp;

    }

    public List<Bill> getBillsByCollecting() {

        Map temMap = new HashMap();
        temMap.put("fromDate", fromDate);
        temMap.put("toDate", toDate);
        String sql;
        List<Bill> tmp;

        if (collectingIns == null) {
            //sql = "SELECT b FROM BilledBill b WHERE b.retired=false  and b.createdAt between :fromDate and :toDate  order by b.collectingCentre.name";
            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and  b.createdAt between :fromDate and :toDate  order by b.collectingCentre.name";
        } else {
            sql = "SELECT b FROM BilledBill b WHERE b.retired=false   and  b.collectingCentre=:col  and b.createdAt between :fromDate and :toDate  order by b.collectingCentre.name";
            temMap.put("col", getCollectingIns());
        }

        tmp = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (tmp == null) {
            tmp = new ArrayList<Bill>();
        }

        return tmp;

    }

    public BillsTotals getUserBills() {
        if (billedBills == null) {
            getBilledBills().setBills(userBills(new BilledBill(), BillType.OpdBill, getWebUser()));
            // calTot(getBilledBills());
        }

        return billedBills;
    }

    public BillsTotals getUserCancelledBills() {
        if (cancellededBills == null) {
            getCancellededBills().setBills(userBills(new CancelledBill(), BillType.OpdBill, getWebUser()));
            //   calTot(getCancellededBills());
        }
        return cancellededBills;
    }

    public BillsTotals getUserRefundedBills() {
        if (refundedBills == null) {
            getRefundedBills().setBills(userBills(new RefundBill(), BillType.OpdBill, getWebUser()));
            //  calTot(getRefundedBills());
        }
        return refundedBills;
    }

    public BillsTotals getUserPaymentBills() {
        if (paymentBills == null) {
            getPaymentBills().setBills(userBills(new BilledBill(), BillType.PaymentBill, getWebUser()));
            //  calTot(getPaymentBills());
        }
        return paymentBills;
    }

    public BillsTotals getUserPaymentCancelBills() {
        if (paymentCancelBills == null) {
            getPaymentCancelBills().setBills(userBills(new CancelledBill(), BillType.PaymentBill, getWebUser()));
            //   calTot(getPaymentCancelBills());
        }
        return paymentCancelBills;
    }

    public BillsTotals getInstitutionBilledBills() {
        if (billedBills == null) {
            getBilledBills().setBills(institutionBill(getInstitution(), new BilledBill(), BillType.OpdBill));
            //  calTot(getBilledBills());
        }
        return billedBills;
    }

    public BillsTotals getInstitutionCancelledBills() {
        if (cancellededBills == null) {
            getCancellededBills().setBills(institutionBill(getInstitution(), new CancelledBill(), BillType.OpdBill));
            //   calTot(getCancellededBills());
        }
        return cancellededBills;
    }

    public BillsTotals getInstitutionRefundedBills() {
        if (refundedBills == null) {
            getRefundedBills().setBills(institutionBill(getInstitution(), new RefundBill(), BillType.OpdBill));
            //  calTot(getRefundedBills());
        }
        return refundedBills;
    }

    public BillsTotals getInstitutionPaymentBills() {
        if (paymentBills == null) {
            getPaymentBills().setBills(institutionBill(getInstitution(), new BilledBill(), BillType.PaymentBill));
            // calTot(getPaymentBills());
        }
        return paymentBills;
    }

    public BillsTotals getInstitutionPaymentCancelBills() {
        if (paymentCancelBills == null) {
            getPaymentCancelBills().setBills(institutionBill(getInstitution(), new CancelledBill(), BillType.PaymentBill));
            // calTot(getPaymentCancelBills());
        }
        return paymentCancelBills;
    }

    public BillsTotals getUserPaymentBillsOwn() {
        if (paymentBills == null) {
            getPaymentBills().setBills(userBillsOwn(new BilledBill(), BillType.PaymentBill, getWebUser()));
        }
        //   calTot(getPaymentBills());
        return paymentBills;
    }

    public BillsTotals getUserInwardPaymentBillsOwn() {
        if (inwardPayments == null) {
            getInwardPayments().setBills(userBillsOwn(new BilledBill(), BillType.InwardPaymentBill, getWebUser()));
        }
        //  calTot(getInwardPayments());
        return inwardPayments;
    }

    public BillsTotals getInstitutionInwardPaymentBillsOwn() {
        if (inwardPayments == null) {
            getInwardPayments().setBills(billsOwn(new BilledBill(), BillType.InwardPaymentBill));
        }
        //  calTot(getInwardPayments());
        return inwardPayments;
    }

    public List<Bill> getBillsByReferingDocOwn() {

        Map temMap = new HashMap();
        List<Bill> tmp;
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bTp", BillType.OpdBill);

        String sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.toInstitution=:ins "
                + "and b.billType =:bTp and b.createdAt between :fromDate and :toDate  order by b.referredBy.person.name ";
        tmp = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (tmp == null) {
            tmp = new ArrayList<>();
        }

        return tmp;

    }

    public List<Bill> getBillsByCollectingOwn() {

        Map temMap = new HashMap();
        List<Bill> tmp;
        temMap.put("fromDate", fromDate);
        temMap.put("toDate", toDate);
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bType", BillType.LabBill);
        String sql;

        if (collectingIns == null) {
            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and  b.billType=:bType and  b.institution=:ins and b.createdAt between :fromDate and :toDate  order by b.collectingCentre.name";
        } else {
            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and  b.billType =:bType and b.institution=:ins and b.collectingCentre=:col and b.createdAt between :fromDate and :toDate  order by b.collectingCentre.name";
            temMap.put("col", getCollectingIns());
        }
        tmp = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (tmp == null) {
            tmp = new ArrayList<Bill>();
        }

        total = 0.0;

        for (Bill b : tmp) {
            total += b.getNetTotal();
        }

        return tmp;

    }

    public List<Bill> getBillsByLabCreditOwn() {

        Map temMap = new HashMap();
        List<Bill> tmp;
        temMap.put("fromDate", fromDate);
        temMap.put("toDate", toDate);
        //   temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bType", BillType.LabBill);
        temMap.put("col", getCreditCompany());
        String sql;

        sql = "SELECT b FROM BilledBill b WHERE b.retired=false and  b.billType =:bType "
                + "  and b.creditCompany=:col and b.createdAt between :fromDate and :toDate "
                + "order by b.creditCompany.name";

        tmp = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (tmp == null) {
            tmp = new ArrayList<>();
        }

        total = 0.0;

        for (Bill b : tmp) {
            total += b.getNetTotal();
        }

        return tmp;

    }

    private double total;

    public BillsTotals getUserRefundedBillsOwn() {
        if (refundedBills == null) {
            getRefundedBills().setBills(userBillsOwn(new RefundBill(), BillType.OpdBill, getWebUser()));
        }
        // calTot(getRefundedBills());
        return refundedBills;
    }

    public BillsTotals getUserRefundedBillsOwnPh() {
        if (refundedBillsPh == null) {
            getRefundedBillsPh().setBills(userPharmacyBillsOwn(new RefundBill(), BillType.PharmacySale, getWebUser()));
        }

        if (refundedBillsPh2 == null) {
            getRefundedBillsPh2().setBills(userPharmacyBillsOther(new RefundBill(), BillType.PharmacySale, getWebUser()));
        }
        // calTot(getRefundedBills());
        return refundedBillsPh;
    }

    public BillsTotals getUserRefundedBillsPhOther() {
        if (refundedBillsPh2 == null) {
            getRefundedBillsPh2().setBills(userPharmacyBillsOther(new RefundBill(), BillType.PharmacySale, getWebUser()));
        }
        // calTot(getRefundedBills());
        return refundedBillsPh2;
    }

    public BillsTotals getUserCancelledBillsOwn() {
        if (cancellededBills == null) {
            getCancellededBills().setBills(userBillsOwn(new CancelledBill(), BillType.OpdBill, getWebUser()));
        }
        //   calTot(getCancellededBills());
        return cancellededBills;
    }

    public BillsTotals getUserCancelledBillsOwnPh() {
        if (cancellededBillsPh == null) {
            getCancellededBillsPh().setBills(userPharmacyBillsOwn(new CancelledBill(), BillType.PharmacySale, getWebUser()));
        }

        if (cancellededBillsPh2 == null) {
            getCancellededBillsPh2().setBills(userPharmacyBillsOther(new CancelledBill(), BillType.PharmacySale, getWebUser()));
        }
        //   calTot(getCancellededBills());
        return cancellededBillsPh;
    }

    public BillsTotals getUserCancelledBillsPhOther() {
        if (cancellededBillsPh2 == null) {
            getCancellededBillsPh2().setBills(userPharmacyBillsOther(new CancelledBill(), BillType.PharmacySale, getWebUser()));
        }
        //   calTot(getCancellededBills());
        return cancellededBillsPh2;
    }

    private List<Bill> userBillsOwn(Bill billClass, BillType billType, WebUser webUser) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType = :btp"
                + " and b.creater=:web and b.institution=:ins and b.createdAt between :fromDate and :toDate ";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("web", webUser);
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Bill> userPharmacyBillsOwn(Bill billClass, BillType billType, WebUser webUser) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType = :btp"
                + " and b.creater=:web and b.referenceBill.institution=:ins and b.createdAt between :fromDate and :toDate ";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("web", webUser);
        temMap.put("ins", getSessionController().getInstitution());

//        checkOtherInstiution
        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Bill> userPharmacyBillsOther(Bill billClass, BillType billType, WebUser webUser) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType = :btp"
                + " and b.creater=:web and b.referenceBill.institution!=:ins and b.createdAt between :fromDate and :toDate ";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("web", webUser);
        temMap.put("ins", getSessionController().getInstitution());

        Bill b = getBillFacade().findFirstBySQL(sql, temMap, TemporalType.DATE);

        if (b != null && institution == null) {
            ////System.err.println("SYS "+b.getInstitution().getName());
            institution = b.getInstitution();
        }

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Bill> institutionBill(Institution ins, Bill billClass, BillType billType) {
        String sql;
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);

        if (institution == null) {
            sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType = :btp"
                    + " and  b.createdAt between :fromDate and :toDate ";
        } else {
            sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType = :btp"
                    + " and b.institution=:ins and b.createdAt between :fromDate and :toDate";

            temMap.put("ins", ins);
        }

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Bill> userBills(Bill billClass, BillType billType, WebUser webUser) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType = :btp"
                + " and b.creater=:web and b.createdAt between :fromDate and :toDate";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("web", webUser);

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public BillsTotals getUserBillsOwn() {
        if (billedBills == null) {
            getBilledBills().setBills(userBillsOwn(new BilledBill(), BillType.OpdBill, getWebUser()));
            //   calTot(getBilledBills());
        }
        return billedBills;
    }

    public BillsTotals getUserBillsOwnPh() {
        if (billedBillsPh == null) {
            getBilledBillsPh().setBills(userPharmacyBillsOwn(new BilledBill(), BillType.PharmacySale, getWebUser()));
            //   calTot(getBilledBills());
        }

        if (billedBillsPh2 == null) {
            getBilledBillsPh2().setBills(userPharmacyBillsOther(new BilledBill(), BillType.PharmacySale, getWebUser()));
            //   calTot(getBilledBills());
        }

        return billedBillsPh;
    }

    public BillsTotals getUserBillsPhOther() {
        if (billedBillsPh2 == null) {
            getBilledBillsPh2().setBills(userPharmacyBillsOther(new BilledBill(), BillType.PharmacySale, getWebUser()));
            //   calTot(getBilledBills());
        }
        return billedBillsPh2;
    }

    public BillsTotals getInstitutionPaymentBillsOwn() {
        if (paymentBills == null) {
            getPaymentBills().setBills(billsOwn(new BilledBill(), BillType.PaymentBill));
            //   calTot(getPaymentBills());
        }
        return paymentBills;
    }

    public BillsTotals getInstitutionRefundedBillsOwn() {
        if (refundedBills == null) {
            getRefundedBills().setBills(billsOwn(new RefundBill(), BillType.OpdBill));
        }
        //  calTot(getRefundedBills());
        return refundedBills;
    }

    public BillsTotals getInstitutionRefundedBillsOwnPh() {
        if (refundedBillsPh == null) {
            getRefundedBillsPh().setBills(billsOwn(new RefundBill(), BillType.PharmacySale));
        }
        //  calTot(getRefundedBills());
        return refundedBillsPh;
    }

    public BillsTotals getInstitutionCancelledBillsOwn() {
        if (cancellededBills == null) {
            getCancellededBills().setBills(billsOwn(new CancelledBill(), BillType.OpdBill));
        }
        ///   calTot(getCancellededBills());
        return cancellededBills;
    }

    public BillsTotals getInstitutionCancelledBillsOwnPh() {
        if (cancellededBillsPh == null) {
            getCancellededBillsPh().setBills(billsOwn(new CancelledBill(), BillType.PharmacySale));
        }
        ///   calTot(getCancellededBills());
        return cancellededBillsPh;
    }

    public BillsTotals getUserAgentRecieveBills() {
        if (agentRecieves == null) {
            getAgentRecieves().setBills(userBillsOwn(new BilledBill(), BillType.AgentPaymentReceiveBill, getWebUser()));
            //  calTot(getAgentRecieves());
        }
        return agentRecieves;
    }

    public BillsTotals getUserCashRecieveBills() {
        if (cashRecieves == null) {
            getCashRecieves().setBills(userBillsOwn(new BilledBill(), BillType.CashRecieveBill, getWebUser()));
        }
        // calTot(getCashRecieves());
        return cashRecieves;
    }

    public BillsTotals getUserAgentRecieveBillCancel() {
        if (agentCancelBill == null) {
            getAgentCancelBill().setBills(userBillsOwn(new CancelledBill(), BillType.AgentPaymentReceiveBill, getWebUser()));
            //  calTot(getAgentCancelBill());
        }
        return agentCancelBill;
    }

    public BillsTotals getUserCashRecieveBillCancel() {
        if (cashRecieveCancel == null) {
            getCashRecieveCancel().setBills(userBillsOwn(new CancelledBill(), BillType.CashRecieveBill, getWebUser()));
            // calTot(getCashRecieveCancel());
        }
        return cashRecieveCancel;
    }

    public BillsTotals getUserPettyPaymentBills() {
        if (pettyPayments == null) {
            getPettyPayments().setBills(userBillsOwn(new BilledBill(), BillType.PettyCash, getWebUser()));
            // calTot(getPettyPayments());
        }
        return pettyPayments;
    }

    public BillsTotals getUserPettyPaymentCancelBills() {
        if (pettyPaymentsCancel == null) {
            List<Bill> tmp = userBillsOwn(new CancelledBill(), BillType.PettyCash, getWebUser());
            getPettyPaymentsCancel().setBills(tmp);
            //  calTot(getPettyPaymentsCancel());
        }
        return pettyPaymentsCancel;
    }

    public BillsTotals getUserPaymentCancelBillsOwn() {
        if (paymentCancelBills == null) {
            List<Bill> tmp = userBillsOwn(new CancelledBill(), BillType.PaymentBill, getWebUser());
            getPaymentCancelBills().setBills(tmp);
        }
        //   calTot(getPaymentCancelBills());
        return paymentCancelBills;
    }

    public BillsTotals getUserInwardPaymentCancelBillsOwn() {
        if (inwardPaymentCancel == null) {
            List<Bill> tmp = userBillsOwn(new CancelledBill(), BillType.InwardPaymentBill, getWebUser());
            getInwardPaymentCancel().setBills(tmp);
        }
        //      calTot(getInwardPaymentCancel());
        return inwardPaymentCancel;
    }

    public BillsTotals getInstitutionInwardPaymentCancelBillsOwn() {
        if (inwardPaymentCancel == null) {
            List<Bill> tmp = billsOwn(new CancelledBill(), BillType.InwardPaymentBill);
            getInwardPaymentCancel().setBills(tmp);
        }
        //    calTot(getInwardPaymentCancel());
        return inwardPaymentCancel;
    }

    public BillsTotals getInstitutionPaymentCancelBillsOwn() {
        if (paymentCancelBills == null) {
            List<Bill> tmp = billsOwn(new CancelledBill(), BillType.PaymentBill);

            getPaymentCancelBills().setBills(tmp);
        }
        //   calTot(getPaymentCancelBills());
        return paymentCancelBills;
    }

    public BillsTotals getInstitutionPettyPaymentBillsOwn() {
        if (pettyPayments == null) {
            List<Bill> tmp = billsOwn(new BilledBill(), BillType.PettyCash);

            getPettyPayments().setBills(tmp);
            //   calTot(getPettyPayments());
        }
        return pettyPayments;
    }

//    public List<Bill> getInstitutionPettyPaymentBills() {
//        if (pettyPayments == null) {
//            String sql;
//            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.billType = :bTp and  b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.PettyCash);
//            pettyPayments = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (pettyPayments == null) {
//                pettyPayments = new ArrayList<Bill>();
//            }
//
//        }
//
//        calPettyTotal();
//        return pettyPayments;
//    }
    public BillsTotals getInstitutionPettyCancellBillsOwn() {
        if (pettyPaymentsCancel == null) {
            List<Bill> tmp = billsOwn(new CancelledBill(), BillType.PettyCash);
            getPettyPaymentsCancel().setBills(tmp);
//        calTot(getPettyPaymentsCancel());
        }
        return pettyPaymentsCancel;
    }

//    public List<Bill> getInstitutionPettyCancellBills() {
//        if (pettyPaymentsCancel == null) {
//            String sql;
//
//            sql = "SELECT b FROM CancelledBill b WHERE b.retired=false and b.billType = :bTp and b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.PettyCash);
//            pettyPaymentsCancel = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (pettyPaymentsCancel == null) {
//                pettyPaymentsCancel = new ArrayList<Bill>();
//            }
//
//        }
//        calPettyCancelTotal();
//        return pettyPaymentsCancel;
//    }
    public BillsTotals getInstitutionCashRecieveBillsOwn() {
        if (cashRecieves == null) {
            getCashRecieves().setBills(billsOwn(new BilledBill(), BillType.CashRecieveBill));
            //      calTot(getCashRecieves());
        }
        return cashRecieves;
    }

//    public List<Bill> getInstitutionCashRecieveBills() {
//        if (cashRecieves == null) {
//            String sql;
//
//            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.billType = :bTp and  b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.CashRecieveBill);
//            cashRecieves = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (cashRecieves == null) {
//                cashRecieves = new ArrayList<Bill>();
//            }
//
//        }
//        calCashRecieveTot();
//        return cashRecieves;
//    }
    public BillsTotals getInstitutionAgentBillsOwn() {
        if (agentRecieves == null) {
            getAgentRecieves().setBills(billsOwn(new BilledBill(), BillType.AgentPaymentReceiveBill));
        }
        //    calTot(getAgentRecieves());
        return agentRecieves;
    }

//    public List<Bill> getInstitutionAgentBills() {
//        if (agentRecieves == null) {
//            String sql;
//
//            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.billType = :bTp and  b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.AgentPaymentReceiveBill);
//            agentRecieves = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (agentRecieves == null) {
//                agentRecieves = new ArrayList<Bill>();
//            }
//
//        }
//        calAgentTotal();
//        return agentRecieves;
//    }
    public BillsTotals getInstitutionCashRecieveCancellBillsOwn() {
        if (cashRecieveCancel == null) {
            getCashRecieveCancel().setBills(billsOwn(new CancelledBill(), BillType.CashRecieveBill));
            //    calTot(getCashRecieveCancel());
        }
        return cashRecieveCancel;
    }

//    public List<Bill> getInstitutionCashRecieveCancellBills() {
//        if (cashRecieveCancel == null) {
//            String sql;
//
//            sql = "SELECT b FROM CancelledBill b WHERE b.retired=false and b.billType = :bTp and b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.CashRecieveBill);
//            cashRecieveCancel = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (cashRecieveCancel == null) {
//                cashRecieveCancel = new ArrayList<Bill>();
//            }
//
//        }
//        calCashRecieveCancelTot();
//        return cashRecieveCancel;
//    }
    public BillsTotals getInstitutionAgentCancellBillsOwn() {
        if (agentCancelBill == null) {
            getAgentCancelBill().setBills(billsOwn(new CancelledBill(), BillType.AgentPaymentReceiveBill));
            //   calTot(getAgentCancelBill());
        }
        return agentCancelBill;
    }

//    public List<Bill> getInstitutionAgentCancellBills() {
//        if (agentCancelBill == null) {
//            String sql;
//
//            sql = "SELECT b FROM CancelledBill b WHERE b.retired=false and b.billType = :bTp  and b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.AgentPaymentReceiveBill);
//            agentCancelBill = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (agentCancelBill == null) {
//                agentCancelBill = new ArrayList<Bill>();
//            }
//
//        }
//        calAgentCancelTot();
//        return agentCancelBill;
//    }
    private List<Bill> billsOwn(Bill billClass, BillType billType) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType=:btp and b.institution=:ins"
                + " and b.createdAt between :fromDate and :toDate";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", billType);
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bill", billClass.getClass());

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public BillsTotals getInstitutionBilledBillsOwn() {
        if (billedBills == null) {
            List<Bill> tmp = billsOwn(new BilledBill(), BillType.OpdBill);

            getBilledBills().setBills(tmp);
        }
        //    calTot(getBilledBills());
        return billedBills;
    }

    public BillsTotals getInstitutionBilledBillsOwnPh() {
        if (billedBillsPh == null) {
            List<Bill> tmp = billsOwn(new BilledBill(), BillType.PharmacySale);

            getBilledBillsPh().setBills(tmp);
        }
        //    calTot(getBilledBills());
        return billedBillsPh;
    }

    public double getFinalCreditTotal(List<BillsTotals> list) {

        double tmp = 0.0;
        for (BillsTotals bt : list) {
            if (bt != null) {
                tmp += bt.getCredit();
            }
        }

        return tmp;
    }

    public double getFinalCreditCardTotal(List<BillsTotals> list) {

        double tmp = 0.0;
        for (BillsTotals bt : list) {
            if (bt != null) {
                tmp += bt.getCard();
            }
        }

        return tmp;
    }

    public void recreteModal() {
        collectingIns = null;
        billedBills = null;
        cancellededBills = null;
        refundedBills = null;
        billedBillsPh = null;
        cancellededBillsPh = null;
        refundedBillsPh = null;
        billedBillsPh2 = null;
        cancellededBillsPh2 = null;
        refundedBillsPh2 = null;
        pettyPayments = null;
        pettyPaymentsCancel = null;
        agentCancelBill = null;
        agentRecieves = null;
        cashRecieves = null;
        cashRecieveCancel = null;
        paymentBills = null;
        paymentCancelBills = null;
        inwardPayments = null;
        inwardPaymentCancel = null;
        dataTableData = null;
        institution = null;
        
    }

    public Institution getCollectingIns() {
        return collectingIns;
    }

    public void setCollectingIns(Institution collectingIns) {
        //recreteModal();
        this.collectingIns = collectingIns;
    }

    public double getFinalChequeTot(List<BillsTotals> list) {

        double tmp = 0.0;
        for (BillsTotals bt : list) {
            if (bt != null) {
                tmp += bt.getCheque();
            }
        }

        return tmp;
    }

    public double getFinalSlipTot(List<BillsTotals> list) {

        double tmp = 0.0;
        for (BillsTotals bt : list) {
            if (bt != null) {
                tmp += bt.getSlip();
            }
        }

        return tmp;
    }

    public double getFinalCashTotal(List<BillsTotals> list) {

        double tmp = 0.0;
        for (BillsTotals bt : list) {
            if (bt != null) {
                tmp += bt.getCash();
            }
        }

        return tmp;
    }

    public BillsTotals getBilledBills() {
        if (billedBills == null) {
            billedBills = new BillsTotals();
            //  billedBills.setBillType(BillType.OpdBill);
        }
        return billedBills;
    }

    public void setBilledBills(BillsTotals billedBills) {
        this.billedBills = billedBills;
    }

    public BillsTotals getCancellededBills() {
        if (cancellededBills == null) {
            cancellededBills = new BillsTotals();
            //   cancellededBills.setBillType(BillType.OpdBill);
        }
        return cancellededBills;
    }

    public void setCancellededBills(BillsTotals cancellededBills) {
        this.cancellededBills = cancellededBills;
    }

    public BillsTotals getRefundedBills() {
        if (refundedBills == null) {
            refundedBills = new BillsTotals();
            //    refundedBills.setBillType(BillType.OpdBill);
        }
        return refundedBills;
    }

    public void setRefundedBills(BillsTotals refundedBills) {
        this.refundedBills = refundedBills;
    }

    public BillsTotals getPaymentBills() {
        if (paymentBills == null) {
            paymentBills = new BillsTotals();
            //    paymentBills.setBillType(BillType.PaymentBill);
        }
        return paymentBills;
    }

    public void setPaymentBills(BillsTotals paymentBills) {
        this.paymentBills = paymentBills;
    }

    public BillsTotals getPaymentCancelBills() {
        if (paymentCancelBills == null) {
            paymentCancelBills = new BillsTotals();
            //    paymentCancelBills.setBillType(BillType.PaymentBill);
        }
        return paymentCancelBills;
    }

    public void setPaymentCancelBills(BillsTotals paymentCancelBills) {
        this.paymentCancelBills = paymentCancelBills;
    }

    public BillsTotals getPettyPayments() {
        if (pettyPayments == null) {
            pettyPayments = new BillsTotals();
            //    pettyPayments.setBillType(BillType.PettyCash);
        }
        return pettyPayments;
    }

    public void setPettyPayments(BillsTotals pettyPayments) {
        this.pettyPayments = pettyPayments;
    }

    public BillsTotals getPettyPaymentsCancel() {
        if (pettyPaymentsCancel == null) {
            pettyPaymentsCancel = new BillsTotals();
            //     pettyPaymentsCancel.setBillType(BillType.PettyCash);
        }
        return pettyPaymentsCancel;
    }

    public void setPettyPaymentsCancel(BillsTotals pettyPaymentsCancel) {
        this.pettyPaymentsCancel = pettyPaymentsCancel;
    }

    public BillsTotals getCashRecieves() {
        if (cashRecieves == null) {
            cashRecieves = new BillsTotals();
            //    cashRecieves.setBillType(BillType.CashRecieveBill);
        }
        return cashRecieves;
    }

    public void setCashRecieves(BillsTotals cashRecieves) {
        this.cashRecieves = cashRecieves;
    }

    public BillsTotals getCashRecieveCancel() {
        if (cashRecieveCancel == null) {
            cashRecieveCancel = new BillsTotals();
            //   cashRecieveCancel.setBillType(BillType.CashRecieveBill);
        }
        return cashRecieveCancel;
    }

    public void setCashRecieveCancel(BillsTotals cashRecieveCancel) {
        this.cashRecieveCancel = cashRecieveCancel;
    }

    public BillsTotals getAgentRecieves() {
        if (agentRecieves == null) {
            agentRecieves = new BillsTotals();
            //  agentRecieves.setBillType(BillType.AgentPaymentReceiveBill);
        }
        return agentRecieves;
    }

    public void setAgentRecieves(BillsTotals agentRecieves) {
        this.agentRecieves = agentRecieves;
    }

    public BillsTotals getAgentCancelBill() {
        if (agentCancelBill == null) {
            agentCancelBill = new BillsTotals();
            //    agentCancelBill.setBillType(BillType.AgentPaymentReceiveBill);
        }
        return agentCancelBill;
    }

    public void setAgentCancelBill(BillsTotals agentCancelBill) {
        this.agentCancelBill = agentCancelBill;
    }

    public BillsTotals getInwardPayments() {
        if (inwardPayments == null) {
            inwardPayments = new BillsTotals();
            //   inwardPayments.setBillType(BillType.InwardPaymentBill);
        }
        return inwardPayments;
    }

    public void setInwardPayments(BillsTotals inwardPayments) {
        this.inwardPayments = inwardPayments;
    }

    public BillsTotals getInwardPaymentCancel() {
        if (inwardPaymentCancel == null) {
            inwardPaymentCancel = new BillsTotals();
            //    inwardPaymentCancel.setBillType(BillType.InwardPaymentBill);
        }
        return inwardPaymentCancel;
    }

    public void setInwardPaymentCancel(BillsTotals inwardPaymentCancel) {
        this.inwardPaymentCancel = inwardPaymentCancel;
    }

    public List<String1Value1> getCreditSlipSum() {
        List<BillsTotals> list2 = new ArrayList<>();
        list2.add(billedBills);
        list2.add(cancellededBills);
        list2.add(refundedBills);
        list2.add(billedBillsPh);
        list2.add(cancellededBillsPh);
        list2.add(refundedBillsPh);
        list2.add(paymentBills);
        list2.add(paymentCancelBills);
        list2.add(pettyPayments);
        list2.add(pettyPaymentsCancel);
        list2.add(agentRecieves);
        list2.add(agentCancelBill);
        list2.add(inwardPayments);
        list2.add(inwardPaymentCancel);
        list2.add(cashRecieves);
        list2.add(cashRecieveCancel);

        List<String1Value1> list = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list2));
        list.add(tmp1);

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Slip Total");
        tmp2.setValue(getFinalSlipTot(list2));
        list.add(tmp2);

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Total");
        tmp3.setValue(tmp1.getValue() + tmp2.getValue());
        list.add(tmp3);

        return list;
    }

    public List<String1Value1> getCreditSlipSum2() {
        List<BillsTotals> list2 = new ArrayList<>();
        list2.add(billedBillsPh2);
        list2.add(cancellededBillsPh2);
        list2.add(refundedBillsPh2);

        List<String1Value1> list = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list2));
        list.add(tmp1);

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Slip Total");
        tmp2.setValue(getFinalSlipTot(list2));
        list.add(tmp2);

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Total");
        tmp3.setValue(tmp1.getValue() + tmp2.getValue());
        list.add(tmp3);

        return list;
    }

    public List<String1Value1> getDataTableData() {
        List<BillsTotals> list = new ArrayList<>();
        list.add(getBilledBills());
        list.add(getCancellededBills());
        list.add(getRefundedBills());
        list.add(getBilledBillsPh());
        list.add(getCancellededBillsPh());
        list.add(getRefundedBillsPh());
        list.add(getPaymentBills());
        list.add(getPaymentCancelBills());
        list.add(getPettyPayments());
        list.add(getPettyPaymentsCancel());
        list.add(getAgentRecieves());
        list.add(getAgentCancelBill());
        list.add(getInwardPayments());
        list.add(getInwardPaymentCancel());
        list.add(getCashRecieves());
        list.add(getCashRecieveCancel());

        dataTableData = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list));

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Credit Card Total");
        tmp2.setValue(getFinalCreditCardTotal(list));

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cheque Total");
        tmp3.setValue(getFinalChequeTot(list));

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Final Slip Total");
        tmp4.setValue(getFinalSlipTot(list));

        String1Value1 tmp5 = new String1Value1();
        tmp5.setString("Final Cash Total");
        tmp5.setValue(getFinalCashTotal(list));

        dataTableData.add(tmp1);
        dataTableData.add(tmp2);
        dataTableData.add(tmp3);
        dataTableData.add(tmp4);
        dataTableData.add(tmp5);

        return dataTableData;
    }

    public List<String1Value1> getDataTableDataByType() {
        List<BillsTotals> list = new ArrayList<>();
        if (billType == BillType.OpdBill) {
            list.add(getBilledBills());
            list.add(getCancellededBills());
            list.add(getRefundedBills());
        }
        if (billType == BillType.PharmacySale) {
            list.add(getBilledBillsPh());
            list.add(getCancellededBillsPh());
            list.add(getRefundedBillsPh());
        }

        if (billType == BillType.PaymentBill) {
            list.add(getPaymentBills());
            list.add(getPaymentCancelBills());
        }
        if (billType == BillType.PettyCash) {
            list.add(getPettyPayments());
            list.add(getPettyPaymentsCancel());
        }
        if (billType == BillType.AgentPaymentReceiveBill) {
            list.add(getAgentRecieves());
            list.add(getAgentCancelBill());
        }
        if (billType == BillType.InwardPaymentBill) {
            list.add(getInwardPayments());
            list.add(getInwardPaymentCancel());
        }
        if (billType == BillType.CashRecieveBill) {
            list.add(getCashRecieves());
            list.add(getCashRecieveCancel());
        }

        List< String1Value1> data = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list));

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Credit Card Total");
        tmp2.setValue(getFinalCreditCardTotal(list));

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cheque Total");
        tmp3.setValue(getFinalChequeTot(list));

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Final Slip Total");
        tmp4.setValue(getFinalSlipTot(list));

        String1Value1 tmp5 = new String1Value1();
        tmp5.setString("Final Cash Total");
        tmp5.setValue(getFinalCashTotal(list));

        data.add(tmp1);
        data.add(tmp2);
        data.add(tmp3);
        data.add(tmp4);
        data.add(tmp5);

        return data;
    }

    public List<String1Value1> getCashChequeSum() {
        List<BillsTotals> list2 = new ArrayList<>();
        list2.add(billedBills);
        list2.add(cancellededBills);
        list2.add(refundedBills);
        list2.add(billedBillsPh);
        list2.add(cancellededBillsPh);
        list2.add(refundedBillsPh);
        list2.add(paymentBills);
        list2.add(paymentCancelBills);
        list2.add(pettyPayments);
        list2.add(pettyPaymentsCancel);
        list2.add(agentRecieves);
        list2.add(agentCancelBill);
        list2.add(inwardPayments);
        list2.add(inwardPaymentCancel);
        list2.add(cashRecieves);
        list2.add(cashRecieveCancel);

        List<String1Value1> list = new ArrayList<>();

        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Card Total");
        tmp1.setValue(getFinalCreditCardTotal(list2));

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Cheque Total");
        tmp2.setValue(getFinalChequeTot(list2));

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cash Total");
        tmp3.setValue(getFinalCashTotal(list2));

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Total");
        tmp4.setValue(tmp1.getValue() + tmp2.getValue() + tmp3.getValue());

        list.add(tmp1);
        list.add(tmp2);
        list.add(tmp3);
        list.add(tmp4);
        return list;
    }

    public List<String1Value1> getCashChequeSum2() {
        List<BillsTotals> list2 = new ArrayList<>();
        list2.add(billedBillsPh2);
        list2.add(cancellededBillsPh2);
        list2.add(refundedBillsPh2);

        List<String1Value1> list = new ArrayList<>();

        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Card Total");
        tmp1.setValue(getFinalCreditCardTotal(list2));

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Cheque Total");
        tmp2.setValue(getFinalChequeTot(list2));

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cash Total");
        tmp3.setValue(getFinalCashTotal(list2));

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Total");
        tmp4.setValue(tmp1.getValue() + tmp2.getValue() + tmp3.getValue());

        list.add(tmp1);
        list.add(tmp2);
        list.add(tmp3);
        list.add(tmp4);
        return list;
    }

    public void setDataTableData(List<String1Value1> dataTableData) {
        this.dataTableData = dataTableData;
    }

    public BillType getBillType() {
        return billType;
    }

    public void setBillType(BillType billType) {
        this.billType = billType;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Institution getCreditCompany() {
        return creditCompany;
    }

    public void setCreditCompany(Institution creditCompany) {
        this.creditCompany = creditCompany;
    }

    public BillsTotals getBilledBillsPh() {
        if (billedBillsPh == null) {
            billedBillsPh = new BillsTotals();
        }
        return billedBillsPh;
    }

    public void setBilledBillsPh(BillsTotals billedBillsPh) {
        this.billedBillsPh = billedBillsPh;
    }

    public BillsTotals getCancellededBillsPh() {
        if (cancellededBillsPh == null) {
            cancellededBillsPh = new BillsTotals();
        }
        return cancellededBillsPh;
    }

    public void setCancellededBillsPh(BillsTotals cancellededBillsPh) {
        this.cancellededBillsPh = cancellededBillsPh;
    }

    public BillsTotals getRefundedBillsPh() {
        if (refundedBillsPh == null) {
            refundedBillsPh = new BillsTotals();
        }
        return refundedBillsPh;
    }

    public void setRefundedBillsPh(BillsTotals refundedBillsPh) {
        this.refundedBillsPh = refundedBillsPh;
    }

    public BillsTotals getBilledBillsPh2() {
        if (billedBillsPh2 == null) {
            billedBillsPh2 = new BillsTotals();
        }
        return billedBillsPh2;
    }

    public void setBilledBillsPh2(BillsTotals billedBillsPh2) {
        this.billedBillsPh2 = billedBillsPh2;
    }

    public BillsTotals getCancellededBillsPh2() {
        if (cancellededBillsPh2 == null) {
            cancellededBillsPh2 = new BillsTotals();
        }
        return cancellededBillsPh2;
    }

    public void setCancellededBillsPh2(BillsTotals cancellededBillsPh2) {
        this.cancellededBillsPh2 = cancellededBillsPh2;
    }

    public BillsTotals getRefundedBillsPh2() {
        if (refundedBillsPh2 == null) {
            refundedBillsPh2 = new BillsTotals();
        }
        return refundedBillsPh2;
    }

    public void setRefundedBillsPh2(BillsTotals refundedBillsPh2) {
        this.refundedBillsPh2 = refundedBillsPh2;
    }

    public List<DailySummeryRow> getDailySummeryRows() {
        return dailySummeryRows;
    }

    public void setDailySummeryRows(List<DailySummeryRow> dailySummeryRows) {
        this.dailySummeryRows = dailySummeryRows;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
        fromDate = CommonDateFunctions.startOfDate(billDate);
        toDate = CommonDateFunctions.endOfDate(billDate);
    }

    public Institution getDealer() {
        return dealer;
    }

    public void setDealer(Institution dealer) {
        this.dealer = dealer;
    }

    public BillsTotals getGrnPaymentBill() {
        return GrnPaymentBill;
    }

    public void setGrnPaymentBill(BillsTotals GrnPaymentBill) {
        this.GrnPaymentBill = GrnPaymentBill;
    }

    public BillsTotals getGrnPaymentCancell() {
        return GrnPaymentCancell;
    }

    public void setGrnPaymentCancell(BillsTotals GrnPaymentCancell) {
        this.GrnPaymentCancell = GrnPaymentCancell;
    }

    public BillsTotals getGrnPaymentReturn() {
        return GrnPaymentReturn;
    }

    public void setGrnPaymentReturn(BillsTotals GrnPaymentReturn) {
        this.GrnPaymentReturn = GrnPaymentReturn;
    }

    public BillsTotals getGrnPaymentCancellReturn() {
        return GrnPaymentCancellReturn;
    }

    public void setGrnPaymentCancellReturn(BillsTotals GrnPaymentCancellReturn) {
        this.GrnPaymentCancellReturn = GrnPaymentCancellReturn;
    }

    public Institution getSupplier() {
        return supplier;
    }

    public void setSupplier(Institution supplier) {
        this.supplier = supplier;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

}
