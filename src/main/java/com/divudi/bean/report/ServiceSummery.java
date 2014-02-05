/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.report;

import com.divudi.bean.SessionController;
import com.divudi.data.BillType;
import com.divudi.data.FeeType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.BillItemWithFee;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Category;
import com.divudi.entity.Item;
import com.divudi.entity.RefundBill;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Named
@ViewScoped
public class ServiceSummery implements Serializable {

    @Inject
    private SessionController sessionController;
    // private List<DailyCash> dailyCashs;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fromDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date toDate;
    private Item service;
    private Category category;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFeeFacade billFeeFacade;

    /**
     * Creates a new instance of ServiceSummery
     */
    public ServiceSummery() {
    }

    public double getServiceTot() {
        String sql;
        Map temMap = new HashMap();

        sql = "select sum(bi.feeValue) FROM BillFee bi where  bi.bill.institution=:ins and "
                + " bi.bill.billType= :bTp and bi.fee.feeType=:ftp "
                + " and  bi.bill.createdAt between :fromDate and :toDate and bi.billItem.item=:itm"
                + " and ( bi.bill.paymentScheme.paymentMethod = :pm1 or  bi.bill.paymentScheme.paymentMethod = :pm2 "
                + " or  bi.bill.paymentScheme.paymentMethod = :pm3 or  bi.bill.paymentScheme.paymentMethod = :pm4)";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bTp", BillType.OpdBill);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        temMap.put("ftp", FeeType.OwnInstitution);
        temMap.put("itm", getService());
        //     List<BillItem> tmp = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        return getBillFeeFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private long getCount(Bill bill) {
        String sql;
        Map temMap = new HashMap();
        sql = "select count(bi) FROM BillItem bi where bi.bill.billType=:bType and bi.item=:itm "
                + " and type(bi.bill)=:billClass and bi.bill.toInstitution=:ins and "
                + " ( bi.bill.paymentScheme.paymentMethod = :pm1 or  bi.bill.paymentScheme.paymentMethod = :pm2 "
                + " or  bi.bill.paymentScheme.paymentMethod = :pm3 or  bi.bill.paymentScheme.paymentMethod = :pm4) "
                + " and bi.bill.createdAt between :fromDate and :toDate order by bi.item.name";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("itm", getService());
        temMap.put("billClass", bill.getClass());
        temMap.put("bType", BillType.OpdBill);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        temMap.put("ins", getSessionController().getInstitution());
        return getBillItemFacade().countBySql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public long getCountTotal() {
        long countTotal = 0;

        long billed = getCount(new BilledBill());
        long cancelled = getCount(new CancelledBill());
        long refunded = getCount(new RefundBill());

        countTotal = billed - (refunded + cancelled);

        //     System.err.println("Billed : " + billed);
        //   System.err.println("Cancelled : " + cancelled);
        //    System.err.println("Refunded : " + refunded);
        //     System.err.println("Gross Tot : " + countTotal);
        return countTotal;
    }

    private List<BillItem> getBillItem() {
        String sql;
        Map temMap = new HashMap();

        sql = "select bi FROM BillItem bi where  bi.bill.institution=:ins and  bi.bill.billType= :bTp  "
                + " and  bi.bill.createdAt between :fromDate and :toDate and bi.item=:itm"
                + " and ( bi.bill.paymentScheme.paymentMethod = :pm1 or  bi.bill.paymentScheme.paymentMethod = :pm2 "
                + " or  bi.bill.paymentScheme.paymentMethod = :pm3 or  bi.bill.paymentScheme.paymentMethod = :pm4)";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bTp", BillType.OpdBill);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        temMap.put("itm", getService());
        List<BillItem> tmp = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        return tmp;

    }

    public List<BillItemWithFee> getServiceSummery() {
        List<BillItemWithFee> tmp = new ArrayList<>();
        for (BillItem i : getBillItem()) {
            BillItemWithFee bi = new BillItemWithFee();
            bi.setBillItem(i);
            bi.setHospitalFee(calHospitalFee(i));
            tmp.add(bi);
        }

        return tmp;
    }

    private double calHospitalFee(BillItem bi) {
        HashMap hm = new HashMap();
        String sql = "Select sum(f.feeValue) from BillFee f where f.retired=false and f.billItem=:b and "
                + " f.fee.feeType=:ftp";
        hm.put("b", bi);
        hm.put("ftp", FeeType.OwnInstitution);

        return getBillFeeFacade().findDoubleByJpql(sql, hm, TemporalType.DATE);

    }

    public List<BillItemWithFee> getServiceCategorySummery() {

        List<BillItemWithFee> tmp = new ArrayList<>();

        for (BillItem i : getBillItemByCategory()) {
            BillItemWithFee bi = new BillItemWithFee();
            bi.setBillItem(i);
            bi.setHospitalFee(calHospitalFee(i));
            tmp.add(bi);
        }

        return tmp;
    }

    private List<BillItem> getBillItemByCategory() {
        String sql;
        Map temMap = new HashMap();

        sql = "select bi FROM BillItem bi where  bi.bill.institution=:ins and  bi.bill.billType= :bTp  "
                + " and  bi.bill.createdAt between :fromDate and :toDate and bi.item.category=:cat"
                + " and ( bi.bill.paymentScheme.paymentMethod = :pm1 or  bi.bill.paymentScheme.paymentMethod = :pm2 "
                + " or  bi.bill.paymentScheme.paymentMethod = :pm3 or  bi.bill.paymentScheme.paymentMethod = :pm4) "
                + " order by bi.item.name";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bTp", BillType.OpdBill);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        temMap.put("cat", getCategory());
        List<BillItem> tmp = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        return tmp;

    }

    public long getCountTotal2() {
        long countTotal = 0;

        long billed = getCount2(new BilledBill());
        long cancelled = getCount2(new CancelledBill());
        long refunded = getCount2(new RefundBill());

        countTotal = billed - (refunded + cancelled);

        //     System.err.println("Billed : " + billed);
        //   System.err.println("Cancelled : " + cancelled);
        //    System.err.println("Refunded : " + refunded);
        //     System.err.println("Gross Tot : " + countTotal);
        return countTotal;
    }

    private long getCount2(Bill bill) {
        String sql;
        Map temMap = new HashMap();
        sql = "select count(bi) FROM BillItem bi where bi.bill.billType=:bType and bi.item.category=:cat "
                + " and type(bi.bill)=:billClass and bi.bill.toInstitution=:ins and "
                + " ( bi.bill.paymentScheme.paymentMethod = :pm1 or  bi.bill.paymentScheme.paymentMethod = :pm2 "
                + " or  bi.bill.paymentScheme.paymentMethod = :pm3 or  bi.bill.paymentScheme.paymentMethod = :pm4) "
                + " and bi.bill.createdAt between :fromDate and :toDate order by bi.item.name";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("cat", getCategory());
        temMap.put("billClass", bill.getClass());
        temMap.put("bType", BillType.OpdBill);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        temMap.put("ins", getSessionController().getInstitution());
        return getBillItemFacade().countBySql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public double getServiceTot2() {
        String sql;
        Map temMap = new HashMap();

        sql = "select sum(bi.feeValue) FROM BillFee bi where  bi.bill.institution=:ins"
                + " and  bi.bill.billType= :bTp and bi.fee.feeType=:ftp "
                + " and  bi.bill.createdAt between :fromDate and :toDate and bi.billItem.item.category=:cat"
                + " and ( bi.bill.paymentScheme.paymentMethod = :pm1 or  bi.bill.paymentScheme.paymentMethod = :pm2 "
                + " or  bi.bill.paymentScheme.paymentMethod = :pm3 or  bi.bill.paymentScheme.paymentMethod = :pm4)";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bTp", BillType.OpdBill);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        temMap.put("ftp", FeeType.OwnInstitution);
        temMap.put("cat", getCategory());
        //     List<BillItem> tmp = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        return getBillFeeFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Item getService() {
        return service;
    }

    public void setService(Item service) {
        this.service = service;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

}
