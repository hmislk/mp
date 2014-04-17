/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.report;

import com.divudi.bean.ServiceSubCategoryController;
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
import com.divudi.entity.ServiceCategory;
import com.divudi.entity.ServiceSubCategory;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Named
@RequestScoped
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
    double count;
    double value;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFeeFacade billFeeFacade;

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

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
                + " and ( bi.bill.paymentMethod = :pm1 or  bi.bill.paymentMethod = :pm2 "
                + " or  bi.bill.paymentMethod = :pm3 or  bi.bill.paymentMethod = :pm4)";
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
                + " ( bi.bill.paymentMethod = :pm1 or  bi.bill.paymentMethod = :pm2 "
                + " or  bi.bill.paymentMethod = :pm3 or  bi.bill.paymentMethod = :pm4) "
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

        //     //System.err.println("Billed : " + billed);
        //   //System.err.println("Cancelled : " + cancelled);
        //    //System.err.println("Refunded : " + refunded);
        //     //System.err.println("Gross Tot : " + countTotal);
        return countTotal;
    }

    private List<BillItem> getBillItem() {
        String sql;
        Map temMap = new HashMap();

        sql = "select bi FROM BillItem bi where  bi.bill.institution=:ins and  bi.bill.billType= :bTp  "
                + " and  bi.bill.createdAt between :fromDate and :toDate and bi.item=:itm"
                + " and ( bi.bill.paymentMethod = :pm1 or  bi.bill.paymentMethod = :pm2 "
                + " or  bi.bill.paymentMethod = :pm3 or  bi.bill.paymentMethod = :pm4)";
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

    List<BillItemWithFee> billItemWithFees;

    public List<BillItemWithFee> getBillItemWithFees() {
        return billItemWithFees;
    }

    public void setBillItemWithFees(List<BillItemWithFee> billItemWithFees) {
        this.billItemWithFees = billItemWithFees;
    }

    public void createServiceCategorySummery() {
        if (getCategory() == null) {
            return;
        }
        if (getToDate() == null || getFromDate() == null) {
            return;
        }

        billItemWithFees = new ArrayList<>();

        List<BillItem> list = calBillItems();

        for (BillItem i : list) {
            BillItemWithFee bi = new BillItemWithFee();
            bi.setBillItem(i);
            bi.setHospitalFee(calHospitalFee(i));
            billItemWithFees.add(bi);
        }

        calCountTotal();
        calServiceTot();

    }

    @Inject
    ServiceSubCategoryController serviceSubCategoryController;

    public ServiceSubCategoryController getServiceSubCategoryController() {
        return serviceSubCategoryController;
    }

    public void setServiceSubCategoryController(ServiceSubCategoryController serviceSubCategoryController) {
        this.serviceSubCategoryController = serviceSubCategoryController;
    }

    private List<BillItem> calBillItems() {
        if (getCategory() instanceof ServiceSubCategory) {
            return getBillItemByCategory(category);
        }

        if (getCategory() instanceof ServiceCategory) {
            getServiceSubCategoryController().setParentCategory(getCategory());
            List<ServiceSubCategory> subCategorys = getServiceSubCategoryController().getItems();
            if (subCategorys.isEmpty()) {
                return getBillItemByCategory(getCategory());
            } else {
                Set<BillItem> setBillItem = new HashSet<>();
                for (ServiceSubCategory ssc : subCategorys) {
                    setBillItem.addAll(getBillItemByCategory(ssc));
                }

                List<BillItem> billItems = new ArrayList<>();
                billItems.addAll(setBillItem);
                return billItems;
            }
        }

        return null;
    }

    private List<BillItem> getBillItemByCategory(Category cat) {
        String sql;
        Map temMap = new HashMap();

        sql = "select bi FROM BillItem bi "
                + " where  bi.bill.institution=:ins"
                + " and  bi.bill.billType= :bTp  "
                + " and  bi.bill.createdAt between :fromDate and :toDate "
                + " and bi.item.category=:cat"
                + " and ( bi.bill.paymentMethod = :pm1 "
                + " or  bi.bill.paymentMethod = :pm2 "
                + " or  bi.bill.paymentMethod = :pm3"
                + " or  bi.bill.paymentMethod = :pm4) "
                + " order by bi.item.name";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bTp", BillType.OpdBill);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        temMap.put("cat", cat);
        List<BillItem> tmp = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        return tmp;

    }

    public void calCountTotal() {
        long countTotal = 0;
        long billed = 0l;
        long cancelled = 0l;
        long refunded = 0l;

        if (getCategory() instanceof ServiceSubCategory) {
            billed = getCount(new BilledBill(), getCategory());
            cancelled = getCount(new CancelledBill(), getCategory());
            refunded = getCount(new RefundBill(), getCategory());
        }

        if (getCategory() instanceof ServiceCategory) {
            getServiceSubCategoryController().setParentCategory(getCategory());
            List<ServiceSubCategory> subCategorys = getServiceSubCategoryController().getItems();
            if (subCategorys.isEmpty()) {
                billed = getCount(new BilledBill(), getCategory());
                cancelled = getCount(new CancelledBill(), getCategory());
                refunded = getCount(new RefundBill(), getCategory());
            } else {
                billed = 0l;
                cancelled = 0l;
                refunded = 0l;
                for (ServiceSubCategory ssc : subCategorys) {
                    billed += getCount(new BilledBill(), ssc);
                    cancelled += getCount(new CancelledBill(), ssc);
                    refunded += getCount(new RefundBill(), ssc);
                }
            }
        }

        countTotal = billed - (refunded + cancelled);

        count = countTotal;
    }

    private long getCount(Bill bill, Category cat) {
        String sql;
        Map temMap = new HashMap();
        sql = "select count(bi) FROM BillItem bi "
                + " where bi.bill.billType=:bType "
                + " and bi.item.category=:cat "
                + " and type(bi.bill)=:billClass "
                + " and bi.bill.toInstitution=:ins "
                + " and ( bi.bill.paymentMethod = :pm1 "
                + " or  bi.bill.paymentMethod = :pm2 "
                + " or  bi.bill.paymentMethod = :pm3"
                + " or  bi.bill.paymentMethod = :pm4) "
                + " and bi.bill.createdAt between :fromDate and :toDate"
                + " order by bi.item.name";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("cat", cat);
        temMap.put("billClass", bill.getClass());
        temMap.put("bType", BillType.OpdBill);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        temMap.put("ins", getSessionController().getInstitution());
        return getBillItemFacade().countBySql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double getServiceValue(Category cat) {
        String sql;
        Map temMap = new HashMap();

        sql = "select sum(bi.feeValue) FROM BillFee bi "
                + " where  bi.bill.institution=:ins"
                + " and  bi.bill.billType= :bTp "
                + " and bi.fee.feeType=:ftp "
                + " and  bi.bill.createdAt between :fromDate and :toDate"
                + " and bi.billItem.item.category=:cat"
                + " and ( bi.bill.paymentMethod = :pm1 "
                + " or  bi.bill.paymentMethod = :pm2 "
                + " or  bi.bill.paymentMethod = :pm3 "
                + " or  bi.bill.paymentMethod = :pm4)";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bTp", BillType.OpdBill);
        temMap.put("pm1", PaymentMethod.Cash);
        temMap.put("pm2", PaymentMethod.Card);
        temMap.put("pm3", PaymentMethod.Cheque);
        temMap.put("pm4", PaymentMethod.Slip);
        temMap.put("ftp", FeeType.OwnInstitution);
        temMap.put("cat", cat);
        //     List<BillItem> tmp = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        return getBillFeeFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private void calServiceTot() {

        if (getCategory() instanceof ServiceSubCategory) {
            value = getServiceValue(getCategory());
        }

        if (getCategory() instanceof ServiceCategory) {
            getServiceSubCategoryController().setParentCategory(getCategory());
            List<ServiceSubCategory> subCategorys = getServiceSubCategoryController().getItems();
            if (subCategorys.isEmpty()) {
                value = getServiceValue(getCategory());
            } else {
                value = 0;
                for (ServiceSubCategory ssc : subCategorys) {
                    value += getServiceValue(ssc);
                }
            }
        }

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
