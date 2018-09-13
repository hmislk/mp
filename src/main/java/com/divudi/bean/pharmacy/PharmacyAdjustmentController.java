/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.ApplicationController;
import com.divudi.bean.SessionController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.dataStructure.YearMonthDay;

import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.Department;
import com.divudi.entity.Item;
import com.divudi.entity.PreBill;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.ItemBatchFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import com.divudi.facade.StockFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Buddhika
 */
@Named
@SessionScoped
public class PharmacyAdjustmentController implements Serializable {

    /**
     * Creates a new instance of PharmacySaleController
     */
    public PharmacyAdjustmentController() {
    }

    @Inject
    SessionController sessionController;
    @Inject
    ApplicationController applicationController;
////////////////////////
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    ItemFacade itemFacade;
    @EJB
    StockFacade stockFacade;
    @EJB
    PharmacyBean pharmacyBean;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private PatientFacade patientFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    BillNumberBean billNumberBean;
    @EJB
    ItemBatchFacade itemBatchFacade;
/////////////////////////
//    Item selectedAlternative;
    private Bill deptAdjustmentPreBill;
    private Bill saleBill;
    Bill bill;
    BillItem billItem;
    BillItem removingBillItem;
    BillItem editingBillItem;

    Stock stock;

    List<Stock> stocks;

    Item item;

    String comment;

    private Double qty;
    private Double pr;
    private Double rsr;
    private Double wsr;
    Double wsff;
    Double wsfq;

    private YearMonthDay yearMonthDay;

    List<BillItem> billItems;
    private boolean printPreview;

    Department department;
    String departmentName;

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String toAdjustDeptStock() {
        printPreview = false;
        clearBill();
        clearBillItem();
        return "/pharmacy_adjustment_department";
    }

    public void makeNull() {
        printPreview = false;
        clearBill();
        clearBillItem();
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public String newSaleBill() {
        clearBill();
        clearBillItem();
        return "";
    }

    public List<Item> completeRetailSaleItems(String qry) {
        Map m = new HashMap<>();
        List<Item> items;
        String sql;
        sql = "select i from Item i where i.retired=false and upper(i.name) like :n and type(i)=:t and i.id not in(select ibs.id from Stock ibs where ibs.stock >:s and ibs.department=:d and upper(ibs.itemBatch.item.name) like :n ) order by i.name ";
        m.put("t", Amp.class);
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        m.put("n", "%" + qry + "%");
        double s = 0.0;
        m.put("s", s);
        items = getItemFacade().findBySQL(sql, m, 10);
        return items;
    }

    public void listStocks() {
        stocks = new ArrayList<>();
        try {
            String sql;
            Map m = new HashMap();
            m.put("d", getSessionController().getLoggedUser().getDepartment());
            double d = 0.0;
            m.put("s", d);
            m.put("item", item);
            sql = "select i from Stock i where i.stock >=:s and i.department=:d and i.itemBatch.item=:item order by i.stock desc ";
            stocks = getStockFacade().findBySQL(sql, m);
        } catch (Exception e) {
            System.out.println("ERROR = " + e.getMessage());
        }
    }

    public List<Stock> completeAvailableStocks(String qry) {
        List<Stock> items = new ArrayList<>();
        if (qry == null || qry.trim().equals("")) {
            return items;
        }
        try {
            String sql;
            Map m = new HashMap();
            m.put("d", getSessionController().getLoggedUser().getDepartment());
            double d = 0.0;
            m.put("s", d);
            m.put("n", "%" + qry.toUpperCase() + "%");
            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n or upper(i.itemBatch.item.barcode) like :n ) ";
            items = getStockFacade().findBySQL(sql, m, 30);
            return items;
        } catch (Exception e) {
            System.out.println("ERROR = " + e.getMessage());
            return items;
        }
    }

    public List<Stock> completeAllStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("n", "%" + qry.toUpperCase() + "%");
        sql = "select i from Stock i where i.department=:d and "
                + " (upper(i.itemBatch.item.name) like :n  or "
                + " upper(i.itemBatch.item.code) like :n  or  "
                + " upper(i.itemBatch.item.barcode) like :n ) ";
        items = getStockFacade().findBySQL(sql, m, 20);

        return items;
    }

    public List<Stock> completeStaffStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        double d = 0.0;
        m.put("s", d);
        m.put("n", "%" + qry.toUpperCase() + "%");
        sql = "select i from Stock i where i.stock >=:s and (upper(i.staff.code) like :n or upper(i.staff.person.name) like :n or upper(i.itemBatch.item.name) like :n ) order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        items = getStockFacade().findBySQL(sql, m, 20);

        return items;
    }

    public BillItem getBillItem() {
        if (billItem == null) {
            billItem = new BillItem();
        }
        if (billItem.getPharmaceuticalBillItem() == null) {
            PharmaceuticalBillItem pbi = new PharmaceuticalBillItem();
            pbi.setBillItem(billItem);
            billItem.setPharmaceuticalBillItem(pbi);
        }

        return billItem;
    }

    public void setBillItem(BillItem billItem) {
        this.billItem = billItem;
    }

    public List<BillItem> getBillItems() {
        if (billItems == null) {
            billItems = new ArrayList<>();
        }
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    private Bill saveDeptResetBill() {
        Bill b = new Bill();
        b.setBillDate(Calendar.getInstance().getTime());
        b.setBillTime(Calendar.getInstance().getTime());
        b.setCreatedAt(Calendar.getInstance().getTime());
        b.setCreater(getSessionController().getLoggedUser());
        b.setDeptId(getApplicationController().institutionBillNumberGenerator(department, b, BillType.PharmacyAdjustment, BillNumberSuffix.NONE));
        b.setInsId(getApplicationController().institutionBillNumberGenerator(department.getInstitution(), b, BillType.PharmacyAdjustment, BillNumberSuffix.NONE));
        b.setBillType(BillType.PharmacyAdjustment);
        b.setDepartment(department);
        b.setInstitution(department.getInstitution());
        b.setToDepartment(null);
        b.setToInstitution(null);
        b.setFromDepartment(department);
        b.setFromInstitution(department.getInstitution());
        b.setComments(comment);
        if (b.getId() == null) {
            getBillFacade().create(b);
        } else {
            getBillFacade().edit(b);
        }
        return b;
    }

    private PharmaceuticalBillItem saveDeptResetBillItem(Stock s, double q, Bill prebill) {
        billItem = null;
        BillItem tbi = getBillItem();

        PharmaceuticalBillItem ph = getBillItem().getPharmaceuticalBillItem();
        tbi.setPharmaceuticalBillItem(null);
        ph.setStock(s);
        tbi.setItem(s.getItemBatch().getItem());
        tbi.setQty(q);

        //pharmaceutical Bill Item
        ph.setDoe(s.getItemBatch().getDateOfExpire());
        ph.setFreeQty(0.0f);
        ph.setItemBatch(s.getItemBatch());

        Stock fetchedStock = getStockFacade().find(s.getId());
        double stockQty = fetchedStock.getStock();
        double changingQty;

        changingQty = q - stockQty;

        ph.setQty(changingQty);

        //Rates
        //Values
        tbi.setGrossValue(s.getItemBatch().getRetailsaleRate() * q);
        tbi.setNetValue(q * tbi.getNetRate());
        tbi.setDiscount(tbi.getGrossValue() - tbi.getNetValue());
        tbi.setItem(s.getItemBatch().getItem());
        tbi.setBill(prebill);
        tbi.setSearialNo(prebill.getBillItems().size() + 1);
        tbi.setCreatedAt(Calendar.getInstance().getTime());
        tbi.setCreater(getSessionController().getLoggedUser());

        ph.setBillItem(null);
        getPharmaceuticalBillItemFacade().create(ph);

        tbi.setPharmaceuticalBillItem(ph);

        getBillItemFacade().create(tbi);

        ph.setBillItem(tbi);
        getPharmaceuticalBillItemFacade().edit(ph);

        prebill.getBillItems().add(tbi);

        getBillFacade().edit(prebill);

        return ph;

    }

    private void saveDeptAdjustmentBill() {
        getDeptAdjustmentPreBill().setBillDate(Calendar.getInstance().getTime());
        getDeptAdjustmentPreBill().setBillTime(Calendar.getInstance().getTime());
        getDeptAdjustmentPreBill().setCreatedAt(Calendar.getInstance().getTime());
        getDeptAdjustmentPreBill().setCreater(getSessionController().getLoggedUser());
        getDeptAdjustmentPreBill().setDeptId(getApplicationController().institutionBillNumberGenerator(getSessionController().getDepartment(), getDeptAdjustmentPreBill(), BillType.PharmacyAdjustment, BillNumberSuffix.NONE));
        getDeptAdjustmentPreBill().setInsId(getApplicationController().institutionBillNumberGenerator(getSessionController().getInstitution(), getDeptAdjustmentPreBill(), BillType.PharmacyAdjustment, BillNumberSuffix.NONE));
        getDeptAdjustmentPreBill().setBillType(BillType.PharmacyAdjustment);
        getDeptAdjustmentPreBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getDeptAdjustmentPreBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
        getDeptAdjustmentPreBill().setToDepartment(null);
        getDeptAdjustmentPreBill().setToInstitution(null);
        getDeptAdjustmentPreBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        getDeptAdjustmentPreBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
        getDeptAdjustmentPreBill().setComments(comment);
        if (getDeptAdjustmentPreBill().getId() == null) {
            getBillFacade().create(getDeptAdjustmentPreBill());
        } else {
            getBillFacade().edit(getDeptAdjustmentPreBill());
        }
    }

    private PharmaceuticalBillItem saveDeptAdjustmentBillItems() {
        billItem = null;
        BillItem tbi = getBillItem();

        PharmaceuticalBillItem ph = getBillItem().getPharmaceuticalBillItem();

        tbi.setPharmaceuticalBillItem(null);
        ph.setStock(stock);

        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setQty((double) qty);

        //pharmaceutical Bill Item
        ph.setDoe(getStock().getItemBatch().getDateOfExpire());
        ph.setFreeQty(0.0f);
        ph.setItemBatch(getStock().getItemBatch());

        Stock fetchedStock = getStockFacade().find(stock.getId());
        double stockQty = fetchedStock.getStock();
        double changingQty;

        changingQty = qty - stockQty;

        ph.setQty(changingQty);

        //Rates
        //Values
        tbi.setGrossValue(getStock().getItemBatch().getRetailsaleRate() * qty);
        tbi.setNetValue(qty * tbi.getNetRate());
        tbi.setDiscount(tbi.getGrossValue() - tbi.getNetValue());
        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setBill(getDeptAdjustmentPreBill());
        tbi.setSearialNo(getDeptAdjustmentPreBill().getBillItems().size() + 1);
        tbi.setCreatedAt(Calendar.getInstance().getTime());
        tbi.setCreater(getSessionController().getLoggedUser());

        ph.setBillItem(null);
        getPharmaceuticalBillItemFacade().create(ph);

        tbi.setPharmaceuticalBillItem(ph);

        getBillItemFacade().create(tbi);

        ph.setBillItem(tbi);
        getPharmaceuticalBillItemFacade().edit(ph);

        getDeptAdjustmentPreBill().getBillItems().add(tbi);

        getBillFacade().edit(getDeptAdjustmentPreBill());

        return ph;

    }

    private void savePrAdjustmentBillItems() {
        billItem = null;
        BillItem tbi = getBillItem();
        PharmaceuticalBillItem ph = getBillItem().getPharmaceuticalBillItem();

        ph.setBillItem(null);
        ph.setPurchaseRate(pr);
        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setRate(pr);
        //pharmaceutical Bill Item
        ph.setStock(stock);
        //Rates
        //Values
        tbi.setGrossValue(getStock().getItemBatch().getRetailsaleRate() * getStock().getStock());
        tbi.setNetValue(getStock().getStock() * tbi.getNetRate());
        tbi.setDiscount(tbi.getGrossValue() - tbi.getNetValue());
        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setBill(getDeptAdjustmentPreBill());
        tbi.setSearialNo(getDeptAdjustmentPreBill().getBillItems().size() + 1);
        tbi.setCreatedAt(Calendar.getInstance().getTime());
        tbi.setCreater(getSessionController().getLoggedUser());
        getPharmaceuticalBillItemFacade().create(ph);
        tbi.setPharmaceuticalBillItem(ph);

        getBillItemFacade().create(tbi);

        ph.setBillItem(tbi);
        getPharmaceuticalBillItemFacade().edit(ph);
//        getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());
        getDeptAdjustmentPreBill().getBillItems().add(tbi);
        getBillFacade().edit(getDeptAdjustmentPreBill());
    }

    private void saveRsrAdjustmentBillItems() {
        billItem = null;
        BillItem tbi = getBillItem();
        PharmaceuticalBillItem ph = getBillItem().getPharmaceuticalBillItem();

        ph.setBillItem(null);
        ph.setPurchaseRate(rsr);
        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setRate(rsr);
        //pharmaceutical Bill Item
        ph.setStock(stock);
        //Rates
        //Values
        tbi.setGrossValue(getStock().getItemBatch().getRetailsaleRate() * getStock().getStock());
        tbi.setNetValue(getStock().getStock() * tbi.getNetRate());
        tbi.setDiscount(tbi.getGrossValue() - tbi.getNetValue());
        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setBill(getDeptAdjustmentPreBill());
        tbi.setSearialNo(getDeptAdjustmentPreBill().getBillItems().size() + 1);
        tbi.setCreatedAt(Calendar.getInstance().getTime());
        tbi.setCreater(getSessionController().getLoggedUser());
        getPharmaceuticalBillItemFacade().create(ph);
        tbi.setPharmaceuticalBillItem(ph);

        getBillItemFacade().create(tbi);

        ph.setBillItem(tbi);
        getPharmaceuticalBillItemFacade().edit(ph);
//        getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());
        getDeptAdjustmentPreBill().getBillItems().add(tbi);
        getBillFacade().edit(getDeptAdjustmentPreBill());
    }

    private boolean errorCheck() {
        if (getStock() == null) {
            return true;
        }

        if (getStock().getItemBatch() == null) {
            return true;
        }

        return false;
    }

    public String resetAllDepartmentStocks() {
        if (department == null) {
            JsfUtil.addErrorMessage("Dept?");
            return "";
        }
        if (!departmentName.equals(department.getName())) {
            JsfUtil.addErrorMessage("Dept wrong?");
            return "";
        }
        Bill b = saveDeptResetBill();
        String j = "select s from Stock s "
                + " where s.department=:dept "
                + " and s.stock > :stock";
        Map m = new HashMap();
        m.put("dept", department);
        m.put("stock", 0.0);
        List<Stock> ss = getStockFacade().findBySQL(j, m);

        for (Stock s : ss) {
            PharmaceuticalBillItem ph = saveDeptResetBillItem(s, 0, b);
            b.getBillItems().add(getBillItem());
            getBillFacade().edit(b);
            getPharmacyBean().resetStock(ph, s, 0, department);
        }
        JsfUtil.addSuccessMessage("Adjusted.");
        clearBill();
        clearBillItem();
        listStocks();
        return "";
    }

    public String adjustDepartmentStock() {
        if (errorCheck()) {
            return "";
        }
        saveDeptAdjustmentBill();
        PharmaceuticalBillItem ph = saveDeptAdjustmentBillItems();
        getDeptAdjustmentPreBill().getBillItems().add(getBillItem());
        getBillFacade().edit(getDeptAdjustmentPreBill());
        getPharmacyBean().resetStock(ph, stock, qty, getSessionController().getDepartment());
        JsfUtil.addSuccessMessage("Adjusted.");
        printPreview = false;
        clearBill();
        clearBillItem();
        listStocks();
        return "";
    }

    public void adjustPurchaseRate() {
        saveDeptAdjustmentBill();
        savePrAdjustmentBillItems();
        getStock().getItemBatch().setPurcahseRate(pr);
        getItemBatchFacade().edit(getStock().getItemBatch());
        clearBill();
        clearBillItem();
    }

    public void adjustRetailRate() {
        saveDeptAdjustmentBill();
        saveRsrAdjustmentBillItems();
        getStock().getItemBatch().setRetailsaleRate(rsr);
        getStock().getItemBatch().setWholesaleRate(wsr);
        getStock().getItemBatch().setWholesaleFreeFor(wsff);
        getStock().getItemBatch().setWholesaleFreeQty(wsfq);
        
        getItemBatchFacade().edit(getStock().getItemBatch());
        clearBill();
        clearBillItem();
    }

    private void clearBill() {
        stock = null;
        deptAdjustmentPreBill = null;
        billItems = null;
        qty = 0.0;
        comment = "";
    }

    private void clearBillItem() {
        billItem = null;
        removingBillItem = null;
        editingBillItem = null;
        qty = null;
        pr = null;
        rsr = null;
        wsr = null;
        wsff=null;
        wsfq=null;
        stock = null;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public BillItem getRemovingBillItem() {
        return removingBillItem;
    }

    public void setRemovingBillItem(BillItem removingBillItem) {
        this.removingBillItem = removingBillItem;
    }

    public BillItem getEditingBillItem() {
        return editingBillItem;
    }

    public void setEditingBillItem(BillItem editingBillItem) {
        this.editingBillItem = editingBillItem;
    }

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public Bill getDeptAdjustmentPreBill() {
        if (deptAdjustmentPreBill == null) {
            deptAdjustmentPreBill = new PreBill();
            deptAdjustmentPreBill.setBillType(BillType.PharmacyAdjustment);
        }
        return deptAdjustmentPreBill;
    }

    public void setDeptAdjustmentPreBill(Bill deptAdjustmentPreBill) {
        this.deptAdjustmentPreBill = deptAdjustmentPreBill;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public ItemBatchFacade getItemBatchFacade() {
        return itemBatchFacade;
    }

    public void setItemBatchFacade(ItemBatchFacade itemBatchFacade) {
        this.itemBatchFacade = itemBatchFacade;
    }

    public Double getPr() {
        return pr;
    }

    public Double getRsr() {
        return rsr;
    }

    public Double getWsr() {
        return wsr;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public void setPr(Double pr) {
        this.pr = pr;
    }

    public void setRsr(Double rsr) {
        this.rsr = rsr;
    }

    public void setWsr(Double wsr) {
        this.wsr = wsr;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    public PatientFacade getPatientFacade() {
        return patientFacade;
    }

    public void setPatientFacade(PatientFacade patientFacade) {
        this.patientFacade = patientFacade;
    }

    public Bill getSaleBill() {
        return saleBill;
    }

    public void setSaleBill(Bill saleBill) {
        this.saleBill = saleBill;
    }

    public YearMonthDay getYearMonthDay() {
        return yearMonthDay;
    }

    public void setYearMonthDay(YearMonthDay yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Double getWsff() {
        return wsff;
    }

    public void setWsff(Double wsff) {
        this.wsff = wsff;
    }

    public Double getWsfq() {
        return wsfq;
    }

    public void setWsfq(Double wsfq) {
        this.wsfq = wsfq;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }



}
