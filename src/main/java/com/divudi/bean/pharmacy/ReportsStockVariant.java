/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.ejb.BillNumberBean;
import com.divudi.entity.Bill;
import com.divudi.entity.Category;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.PreBill;
import com.divudi.entity.Staff;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.entity.pharmacy.StockVarientBillItem;
import com.divudi.facade.BillFacade;
import com.divudi.facade.StockFacade;
import com.divudi.facade.StockVarientBillItemFacade;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author Buddhika
 */
@Named
@ViewScoped
public class ReportsStockVariant implements Serializable {

    /**
     * Bean Variables
     */
    Department department;
    private Category category;
    double systemStockValue;
    private double calCulatedStockValue;
    private double physicalStockValue;
    List<StockVarientBillItem> records;   
    private Bill recordedBill;

    /**
     * Managed Beans
     */
    @Inject
    DealerController dealerController;

    /**
     * EJBs
     */
    @EJB
    StockFacade stockFacade;

    /**
     * Methods
     */
    /**
     * Methods
     *
     * @return
     */
    public List<Object[]> calDepartmentStock() {

        String sql;
        Map m = new HashMap();
        m.put("dep", department);
        m.put("cat",category);
        sql = "select i.itemBatch.item,sum(i.stock),avg(i.itemBatch.purcahseRate) from Stock i where "
                + " i.department=:dep and i.itemBatch.item.category=:cat group by i.itemBatch.item order by i.itemBatch.item.name";

        return getStockFacade().findAggregates(sql, m);

    }

    @Inject
    private PharmacyErrorChecking pharmacyErrorChecking;

    public void fillCategoryStocks() {
        if (department == null || category == null) {
            UtilityController.addErrorMessage("Please select a department && Category");
            return;
        }

        records = new ArrayList<>();
        systemStockValue = 0.0;
        calCulatedStockValue = 0;

        for (Object[] obj : calDepartmentStock()) {
            StockVarientBillItem r = new StockVarientBillItem();
            r.setItem((Item) obj[0]);
            r.setSystemStock((Double) obj[1]);
            r.setPurchaseRate((Double)obj[2]);
            /////////
            getPharmacyErrorChecking().setItem(r.getItem());
            getPharmacyErrorChecking().setDepartment(department);
            getPharmacyErrorChecking().calculateTotals3();
            r.setCalCulatedStock(getPharmacyErrorChecking().getCalculatedStock());
            //////////////
            records.add(r);

            systemStockValue += (r.getSystemStock()*r.getPurchaseRate());
            calCulatedStockValue += (r.getCalCulatedStock()*r.getPurchaseRate());
        }

    }

    @Inject
    private SessionController sessionController;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private BillFacade billFacade;
    @EJB
    private StockVarientBillItemFacade stockVarientBillItemFacade;

    public void saveRecord() {

        getRecordedBill().setCreatedAt(new Date());
        getRecordedBill().setCreater(getSessionController().getLoggedUser());
        getRecordedBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(department, getRecordedBill(), BillType.PharmacyMajorAdjustment, BillNumberSuffix.MJADJ));
        getRecordedBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(department, getRecordedBill(), BillType.PharmacyMajorAdjustment, BillNumberSuffix.MJADJ));
        getBillFacade().create(getRecordedBill());

        for (StockVarientBillItem i : records) {
            i.setBill(getRecordedBill());
            getStockVarientBillItemFacade().create(i);
        }

        UtilityController.addSuccessMessage("Succesfully Saved");

        recreateModel();
    }

    public void recreateModel() {
        department = null;
        category = null;
        systemStockValue = 0;
        calCulatedStockValue = 0;
        physicalStockValue = 0;
        records = null;
        recordedBill = null;

    }

    /**
     * Getters & Setters
     *
     * @return
     */
    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    /**
     * Constructor
     */
    public ReportsStockVariant() {
    }

    public double getSystemStockValue() {
        return systemStockValue;
    }

    public void setSystemStockValue(double systemStockValue) {
        this.systemStockValue = systemStockValue;
    }

    public List<StockVarientBillItem> getRecords() {
        return records;
    }

    public void setRecords(List<StockVarientBillItem> records) {
        this.records = records;
    }

    public DealerController getDealerController() {
        return dealerController;
    }

    public void setDealerController(DealerController dealerController) {
        this.dealerController = dealerController;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

  

    public PharmacyErrorChecking getPharmacyErrorChecking() {
        return pharmacyErrorChecking;
    }

    public void setPharmacyErrorChecking(PharmacyErrorChecking pharmacyErrorChecking) {
        this.pharmacyErrorChecking = pharmacyErrorChecking;
    }

    public double getCalCulatedStockValue() {
        return calCulatedStockValue;
    }

    public void setCalCulatedStockValue(double calCulatedStockValue) {
        this.calCulatedStockValue = calCulatedStockValue;
    }

    public double getPhysicalStockValue() {
        return physicalStockValue;
    }

    public void setPhysicalStockValue(double physicalStockValue) {
        this.physicalStockValue = physicalStockValue;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public StockVarientBillItemFacade getStockVarientBillItemFacade() {
        return stockVarientBillItemFacade;
    }

    public void setStockVarientBillItemFacade(StockVarientBillItemFacade stockVarientBillItemFacade) {
        this.stockVarientBillItemFacade = stockVarientBillItemFacade;
    }

    public Bill getRecordedBill() {
        if (recordedBill == null) {
            recordedBill = new PreBill();
            recordedBill.setBillType(BillType.PharmacyMajorAdjustment);
        }
        return recordedBill;
    }

    public void setRecordedBill(Bill recordedBill) {
        this.recordedBill = recordedBill;
    }

}
