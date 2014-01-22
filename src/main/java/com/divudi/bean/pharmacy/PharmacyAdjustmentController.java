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
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.Title;
import com.divudi.data.dataStructure.YearMonthDay;
import com.divudi.data.inward.InwardChargeType;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.Patient;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.Person;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;

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
    Item selectedAlternative;
    private Bill deptAdjustmentPreBill;
    private Bill saleBill;
    Bill bill;
    BillItem billItem;
    BillItem removingBillItem;
    BillItem editingBillItem;
    Double qty;
    Double pr;
    Double rsr;
    Double wsr;
    Stock stock;

    private Patient newPatient;
    private Patient searchedPatient;
    private YearMonthDay yearMonthDay;
    private String patientTabId = "tabNewPt";
    private String strTenderedValue = "";
    boolean billPreview = false;
    /////////////////
    List<Stock> replaceableStocks;
    List<BillItem> billItems;
    List<Item> itemsWithoutStocks;
    /////////////////////////
    PaymentScheme paymentScheme;
    String creditCardRefNo;
    Institution creditBank;
    String chequeRefNo;
    Institution chequeBank;
    Date chequeDate;
    String comment;
    Institution slipBank;
    Date slipDate;
    Institution creditCompany;
    double cashPaid;
    double netTotal;
    double balance;
    Double editingQty;

    public Double getEditingQty() {
        return editingQty;
    }

    public void setEditingQty(Double editingQty) {
        this.editingQty = editingQty;
    }

    public void onTabChange(TabChangeEvent event) {
        setPatientTabId(event.getTab().getId());

    }

    public void onCellEdit(CellEditEvent event) {
//        Object oldValue = event.getOldValue();  
//        Object newValue = event.getNewValue();  
//          
//        if(newValue != null && !newValue.equals(oldValue)) {  
//            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cell Changed", "Old: " + oldValue + ", New:" + newValue);  
//            FacesContext.getCurrentInstance().addMessage(null, msg);  
//        }  

        for (BillItem b : billItems) {
            calculateBillItemForEditing(b);
        }
        calTotal();
    }

    public void editQty(BillItem bi) {
        System.out.println("editQty");
        System.out.println("bi = " + bi);
        System.out.println("editingQty = " + editingQty);
        if (bi == null) {
            System.out.println("No Bill Item to Edit Qty");
            return;
        }
        System.out.println("bi.getSearialNo() = " + bi.getSearialNo());
        if (editingQty == null) {
            System.out.println("Editing qty is null");
            return;
        }
        for (BillItem tbi : billItems) {
            System.out.println("tbi = " + tbi);
            if (tbi.getSearialNo() == bi.getSearialNo()) {
                tbi.setQty(editingQty);
                System.out.println("Matching tbi = " + tbi);
                System.out.println("bi.getSearialNo() = " + tbi.getSearialNo());
                calculateBillItemForEditing(tbi);

            } else {
                System.out.println("No Match");
            }
        }
        calTotal();
        editingQty = null;
    }

    public Title[] getTitle() {
        return Title.values();
    }

    public Sex[] getSex() {
        return Sex.values();
    }

    public List<Stock> getReplaceableStocks() {
        return replaceableStocks;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public void setReplaceableStocks(List<Stock> replaceableStocks) {
        this.replaceableStocks = replaceableStocks;
    }

    public Item getSelectedAlternative() {
        return selectedAlternative;
    }

    public void setSelectedAlternative(Item selectedAlternative) {
        this.selectedAlternative = selectedAlternative;
    }

    public void selectReplaceableStocks() {
        if (selectedAlternative == null || !(selectedAlternative instanceof Amp)) {
            replaceableStocks = new ArrayList<>();
            return;
        }
        String sql;
        Map m = new HashMap();
        double d = 0.0;
        Amp amp = (Amp) selectedAlternative;
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        m.put("s", d);
        m.put("vmp", amp.getVmp());
        sql = "select i from Stock i join treat(i.itemBatch.item as Amp) amp where i.stock >:s and i.department=:d and amp.vmp=:vmp order by i.itemBatch.item.name";
        replaceableStocks = getStockFacade().findBySQL(sql, m);
    }

    public List<Item> getItemsWithoutStocks() {
        return itemsWithoutStocks;
    }

    public void setItemsWithoutStocks(List<Item> itemsWithoutStocks) {
        this.itemsWithoutStocks = itemsWithoutStocks;
    }

    public String newSaleBill() {
        clearBill();
        clearBillItem();
        billPreview = false;
        return "pharmacy_retail_sale";
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

    public List<Stock> completeAvailableStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("s", d);
        m.put("n", "%" + qry.toUpperCase() + "%");
        sql = "select i from Stock i where i.stock >:s and i.department=:d and upper(i.itemBatch.item.name) like :n order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        items = getStockFacade().findBySQL(sql, m, 20);
        itemsWithoutStocks = completeRetailSaleItems(qry);
        System.out.println("selectedSaleitems = " + itemsWithoutStocks);
        return items;
    }

    
    public List<Stock> completeAllStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("n", "%" + qry.toUpperCase() + "%");
        sql = "select i from Stock i where i.department=:d and upper(i.itemBatch.item.name) like :n order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        items = getStockFacade().findBySQL(sql, m, 20);
        itemsWithoutStocks = completeRetailSaleItems(qry);
        System.out.println("selectedSaleitems = " + itemsWithoutStocks);
        return items;
    }

    
    public List<Stock> completeStaffStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        double d = 0.0;
        m.put("s", d);
        m.put("n", "%" + qry.toUpperCase() + "%");
        sql = "select i from Stock i where i.stock >:s and (upper(i.staff.code) like :n or upper(i.staff.person.name) like :n or upper(i.itemBatch.item.name) like :n ) order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        items = getStockFacade().findBySQL(sql, m, 20);
        itemsWithoutStocks = completeRetailSaleItems(qry);
        System.out.println("selectedSaleitems = " + itemsWithoutStocks);
        return items;
    }

    public BillItem getBillItem() {
        if (billItem == null) {
            billItem = new BillItem();
        }
        if (billItem.getPharmaceuticalBillItem() == null) {
            PharmaceuticalBillItem pbi = new PharmaceuticalBillItem();
            pbi.setBillItem(billItem);
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

    private boolean errorCheckForSaleBill() {
//        if (checkPaymentScheme(getSaleBill().getPaymentScheme())) {
//            return true;
//        }

        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Cheque) {
            if (getChequeBank() == null || getChequeRefNo() == null || getChequeDate() == null) {
                UtilityController.addErrorMessage("Please select Cheque Number,Bank and Cheque Date");
                return true;
            }

        }

        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Slip) {
            if (getSlipBank() == null || getComment() == null || getSlipDate() == null) {
                UtilityController.addErrorMessage("Please Fill Memo,Bank and Slip Date ");
                return true;
            }

        }

        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Card) {
            if (getCreditBank() == null || getCreditCardRefNo() == null) {
                UtilityController.addErrorMessage("Please Fill Credit Card Number and Bank");
                return true;
            }

//            if (getCreditCardRefNo().trim().length() < 16) {
//                UtilityController.addErrorMessage("Enter 16 Digit");
//                return true;
//            }
        }

        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Credit) {
            if (getCreditCompany() == null) {
                UtilityController.addErrorMessage("Please Select Credit Company");
                return true;
            }

        }

        if (getCreditCompany() != null && getPaymentScheme().getPaymentMethod() != PaymentMethod.Credit) {
            UtilityController.addErrorMessage("Please Select Payment Scheme with Credit");
            return true;
        }

        if (paymentScheme.getPaymentMethod() == PaymentMethod.Cash) {
            if (cashPaid == 0.0) {
                UtilityController.addErrorMessage("Please select tendered amount correctly");
                return true;
            }
            if (cashPaid < getNetTotal()) {
                UtilityController.addErrorMessage("Please select tendered amount correctly");
                return true;
            }
        }
        return false;
    }

    private void saveDeptAdjustmentBill() {
        getDeptAdjustmentPreBill().setBillDate(Calendar.getInstance().getTime());
        getDeptAdjustmentPreBill().setBillTime(Calendar.getInstance().getTime());
        getDeptAdjustmentPreBill().setCreatedAt(Calendar.getInstance().getTime());
        getDeptAdjustmentPreBill().setCreater(getSessionController().getLoggedUser());
        getDeptAdjustmentPreBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getDeptAdjustmentPreBill(), BillType.PharmacyAdjustment, BillNumberSuffix.NONE));
        getDeptAdjustmentPreBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getDeptAdjustmentPreBill(), BillType.PharmacyAdjustment, BillNumberSuffix.NONE));
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
        }
    }

    private void saveSaleBill(Patient tmpPatient) {

        getSaleBill().setBillType(BillType.PharmacySale);

        getSaleBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getSaleBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getSaleBill().setToDepartment(null);
        getSaleBill().setToInstitution(null);

        getSaleBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        getSaleBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getSaleBill().setCreditCompany(creditCompany);

        getSaleBill().setGrantTotal(getDeptAdjustmentPreBill().getGrantTotal());
        getSaleBill().setDiscount(getDeptAdjustmentPreBill().getDiscount());
        getSaleBill().setNetTotal(getDeptAdjustmentPreBill().getNetTotal());
        getSaleBill().setTotal(getDeptAdjustmentPreBill().getTotal());

        getSaleBill().setTmpRefBill(getDeptAdjustmentPreBill());

        if (paymentScheme.getPaymentMethod().equals(PaymentMethod.Cheque)) {
            getSaleBill().setBank(chequeBank);
            getSaleBill().setChequeRefNo(chequeRefNo);
            getSaleBill().setChequeDate(chequeDate);
        }

        if (paymentScheme.getPaymentMethod().equals(PaymentMethod.Slip)) {
            getSaleBill().setBank(slipBank);
            getSaleBill().setChequeDate(slipDate);
            getSaleBill().setComments(comment);
        }

        if (paymentScheme.getPaymentMethod().equals(PaymentMethod.Card)) {
            getSaleBill().setCreditCardRefNo(creditCardRefNo);
            getSaleBill().setBank(creditBank);
        }

        getSaleBill().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getSaleBill().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getSaleBill().setPatient(tmpPatient);
        getSaleBill().setPaymentScheme(getPaymentScheme());

        getSaleBill().setReferenceBill(getDeptAdjustmentPreBill());
        getSaleBill().setCreatedAt(Calendar.getInstance().getTime());
        getSaleBill().setCreater(getSessionController().getLoggedUser());

        getSaleBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), getSaleBill(), BillType.PharmacySale, BillNumberSuffix.PHSAL));
        getSaleBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(
                getSessionController().getDepartment(), getSaleBill(), BillType.PharmacySale, BillNumberSuffix.PHSAL));

        getBillFacade().create(getSaleBill());

        updatePreBill();

    }

    private void updatePreBill() {
        getDeptAdjustmentPreBill().setReferenceBill(getSaleBill());

        getBillFacade().edit(getDeptAdjustmentPreBill());

    }

    private PharmaceuticalBillItem clonePharmacyItem(BillItem newSaleBillItem, BillItem oldSaleBillItem) {
        PharmaceuticalBillItem pbi;
        boolean createNew = false;
        if (newSaleBillItem.getPharmaceuticalBillItem() == null) {
            pbi = new PharmaceuticalBillItem();
            pbi.setBillItem(newSaleBillItem);
            createNew = true;
        } else {
            pbi = newSaleBillItem.getPharmaceuticalBillItem();
        }
        pbi.setDoe(oldSaleBillItem.getPharmaceuticalBillItem().getDoe());
        pbi.setFreeQty(oldSaleBillItem.getPharmaceuticalBillItem().getFreeQty());
        pbi.setItemBatch(oldSaleBillItem.getPharmaceuticalBillItem().getItemBatch());
        pbi.setPurchaseRate(oldSaleBillItem.getPharmaceuticalBillItem().getPurchaseRate());
        pbi.setQty(oldSaleBillItem.getPharmaceuticalBillItem().getQty());
        pbi.setRetailRate(oldSaleBillItem.getPharmaceuticalBillItem().getRetailRate());
        pbi.setStock(oldSaleBillItem.getPharmaceuticalBillItem().getStock());
        pbi.setStockHistory(oldSaleBillItem.getPharmaceuticalBillItem().getStockHistory());
        pbi.setStringValue(oldSaleBillItem.getPharmaceuticalBillItem().getStringValue());
        pbi.setWholesaleRate(oldSaleBillItem.getPharmaceuticalBillItem().getWholesaleRate());
        if (createNew) {
            getPharmaceuticalBillItemFacade().create(pbi);
        } else {
            getPharmaceuticalBillItemFacade().edit(pbi);
        }
        return pbi;
    }

    

    private void saveDeptAdjustmentBillItems() {
        billItem = null;
        BillItem tbi = getBillItem();

        tbi.getPharmaceuticalBillItem().setStock(stock);

        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setQty(qty);

        //pharmaceutical Bill Item
        tbi.getPharmaceuticalBillItem().setDoe(getStock().getItemBatch().getDateOfExpire());
        tbi.getPharmaceuticalBillItem().setFreeQty(0.0);
        tbi.getPharmaceuticalBillItem().setItemBatch(getStock().getItemBatch());
        tbi.getPharmaceuticalBillItem().setQty(qty);

        //Rates
        //Values
        tbi.setGrossValue(getStock().getItemBatch().getRetailsaleRate() * qty);
        tbi.setNetValue(qty * tbi.getNetRate());
        tbi.setDiscount(tbi.getGrossValue() - tbi.getNetValue());
        tbi.setInwardChargeType(InwardChargeType.Medicine);
        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setBill(getDeptAdjustmentPreBill());
        tbi.setSearialNo(getDeptAdjustmentPreBill().getBillItems().size() + 1);
        getDeptAdjustmentPreBill().getBillItems().add(tbi);
        tbi.setCreatedAt(Calendar.getInstance().getTime());
        tbi.setCreater(getSessionController().getLoggedUser());
//        getBillItemFacade().edit(tbi);
//        getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());
        getBillFacade().edit(getDeptAdjustmentPreBill());
    }

    
    private void savePrAdjustmentBillItems() {
        billItem = null;
        BillItem tbi = getBillItem();
        tbi.getPharmaceuticalBillItem().setPurchaseRate(pr);
        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setRate(pr);
        //pharmaceutical Bill Item
        tbi.getPharmaceuticalBillItem().setStock(stock);
        //Rates
        //Values
        tbi.setGrossValue(getStock().getItemBatch().getRetailsaleRate() * getStock().getStock());
        tbi.setNetValue(getStock().getStock() * tbi.getNetRate());
        tbi.setDiscount(tbi.getGrossValue() - tbi.getNetValue());
        tbi.setInwardChargeType(InwardChargeType.Medicine);
        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setBill(getDeptAdjustmentPreBill());
        tbi.setSearialNo(getDeptAdjustmentPreBill().getBillItems().size() + 1);
        getDeptAdjustmentPreBill().getBillItems().add(tbi);
        tbi.setCreatedAt(Calendar.getInstance().getTime());
        tbi.setCreater(getSessionController().getLoggedUser());
//        getBillItemFacade().edit(tbi);
//        getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());
        getBillFacade().edit(getDeptAdjustmentPreBill());
    }

    private void saveRsrAdjustmentBillItems() {
        billItem = null;
        BillItem tbi = getBillItem();
        tbi.getPharmaceuticalBillItem().setPurchaseRate(rsr);
        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setRate(rsr);
        //pharmaceutical Bill Item
        tbi.getPharmaceuticalBillItem().setStock(stock);
        //Rates
        //Values
        tbi.setGrossValue(getStock().getItemBatch().getRetailsaleRate() * getStock().getStock());
        tbi.setNetValue(getStock().getStock() * tbi.getNetRate());
        tbi.setDiscount(tbi.getGrossValue() - tbi.getNetValue());
        tbi.setInwardChargeType(InwardChargeType.Medicine);
        tbi.setItem(getStock().getItemBatch().getItem());
        tbi.setBill(getDeptAdjustmentPreBill());
        tbi.setSearialNo(getDeptAdjustmentPreBill().getBillItems().size() + 1);
        getDeptAdjustmentPreBill().getBillItems().add(tbi);
        tbi.setCreatedAt(Calendar.getInstance().getTime());
        tbi.setCreater(getSessionController().getLoggedUser());
//        getBillItemFacade().edit(tbi);
//        getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());
        getBillFacade().edit(getDeptAdjustmentPreBill());
    }

    
    
    public void adjustDepartmentStock() {
        saveDeptAdjustmentBill();
        saveDeptAdjustmentBillItems();
        setBill(getBillFacade().find(getDeptAdjustmentPreBill().getId()));
        getPharmacyBean().resetStock(stock, qty);
        clearBill();
        clearBillItem();
        billPreview = true;
    }

    public void adjustPurchaseRate() {
        saveDeptAdjustmentBill();
        savePrAdjustmentBillItems();
        getStock().getItemBatch().setPurcahseRate(pr);
        getItemBatchFacade().edit(getStock().getItemBatch());
        clearBill();
        clearBillItem();
        billPreview = true;
    }
    
    public void adjustRetailRate() {
        saveDeptAdjustmentBill();
        saveRsrAdjustmentBillItems();
        getStock().getItemBatch().setRetailsaleRate(rsr);
        getItemBatchFacade().edit(getStock().getItemBatch());
        clearBill();
        clearBillItem();
        billPreview = true;
    }

    public void settleBillWithPay() {
        editingQty = null;
        if (errorCheckForSaleBill()) {
            return;
        }
        saveDeptAdjustmentBill();
        saveDeptAdjustmentBillItems();
        clearBill();
        clearBillItem();
        billPreview = true;
    }

  
    public String newPharmacyRetailSale() {
        clearBill();
        clearBillItem();
        billPreview = false;
        return "pharmacy_retail_sale";
    }

    public void addBillItem() {
        editingQty = null;
        if (billItem == null) {
            return;
        }
        if (billItem.getPharmaceuticalBillItem() == null) {
            return;
        }
        if (getStock() == null) {
            UtilityController.addErrorMessage("Item?");
            return;
        }
        if (getQty() == null) {
            UtilityController.addErrorMessage("Quentity?");
            return;
        }
        if (getQty() > getStock().getStock()) {
            UtilityController.addErrorMessage("No Sufficient Stocks?");
            return;
        }

        billItem.getPharmaceuticalBillItem().setStock(stock);
        calculateBillItem();

        billItem.setInwardChargeType(InwardChargeType.Medicine);

        billItem.setItem(getStock().getItemBatch().getItem());
        billItem.setBill(getDeptAdjustmentPreBill());

        billItem.setSearialNo(billItems.size() + 1);

        billItems.add(billItem);

        calTotal();

        clearBillItem();
    }

    private void calTotal() {
        getDeptAdjustmentPreBill().setTotal(0);
        double netTot = 0.0;
        double discount = 0.0;
        double grossTot = 0.0;
        for (BillItem b : billItems) {
            netTot = netTot + b.getNetValue();
            grossTot = grossTot + b.getGrossValue();
            discount = discount + b.getDiscount();
            getDeptAdjustmentPreBill().setTotal(getDeptAdjustmentPreBill().getTotal() + b.getNetValue());
        }
        getDeptAdjustmentPreBill().setNetTotal(grossTot);
        getDeptAdjustmentPreBill().setTotal(netTot);
        getDeptAdjustmentPreBill().setGrantTotal(grossTot);
        getDeptAdjustmentPreBill().setDiscount(discount);
        setNetTotal(getDeptAdjustmentPreBill().getNetTotal());
    }

    public void removeBillItem() {
        if (removingBillItem == null) {
            UtilityController.addErrorMessage("Nothing to remove");
            return;
        }
        List<BillItem> nbis = new ArrayList<>();
        int i = 1;
        for (BillItem tbi : billItems) {
            if (tbi.getSearialNo() == removingBillItem.getSearialNo()) {

            } else {
                tbi.setSearialNo(i);
                nbis.add(tbi);
                i++;
            }

        }
        billItems = nbis;
        calTotal();
    }

    public void calculateBillItem() {
        if (stock == null) {
            return;
        }
        if (getDeptAdjustmentPreBill() == null) {
            return;
        }
        if (billItem == null) {
            return;
        }
        if (billItem.getPharmaceuticalBillItem() == null) {
            return;
        }
        if (billItem.getPharmaceuticalBillItem().getStock() == null) {
            getBillItem().getPharmaceuticalBillItem().setStock(stock);
        }
        if (getQty() == null) {
            qty = 0.0;
        }

        //Bill Item
//        billItem.setInwardChargeType(InwardChargeType.Medicine);
        billItem.setItem(getStock().getItemBatch().getItem());
        billItem.setQty(qty);

        //pharmaceutical Bill Item
        billItem.getPharmaceuticalBillItem().setDoe(getStock().getItemBatch().getDateOfExpire());
        billItem.getPharmaceuticalBillItem().setFreeQty(0.0);
        billItem.getPharmaceuticalBillItem().setItemBatch(getStock().getItemBatch());
        billItem.getPharmaceuticalBillItem().setQty(qty);

        //Rates
        //Values
        billItem.setGrossValue(getStock().getItemBatch().getRetailsaleRate() * qty);
        billItem.setNetValue(qty * billItem.getNetRate());
        billItem.setDiscount(billItem.getGrossValue() - billItem.getNetValue());

    }

    public void calculateBillItemForEditing(BillItem bi) {
        System.out.println("calculateBillItemForEditing");
        System.out.println("bi = " + bi);
        if (getDeptAdjustmentPreBill() == null || bi == null || bi.getPharmaceuticalBillItem() == null || bi.getPharmaceuticalBillItem().getStock() == null) {
            System.out.println("calculateItemForEditingFailedBecause of null");
            return;
        }
        System.out.println("bi.getQty() = " + bi.getQty());
        System.out.println("bi.getRate() = " + bi.getRate());
        bi.setGrossValue(bi.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate() * bi.getQty());
        bi.setNetValue(bi.getQty() * bi.getNetRate());
        bi.setDiscount(bi.getGrossValue() - bi.getNetValue());
        System.out.println("bi.getNetValue() = " + bi.getNetValue());

    }

    public void handleSelect(SelectEvent event) {
        getBillItem().getPharmaceuticalBillItem().setStock(stock);
        calculateRates(billItem);
    }

    public void calculateAllRates() {
        System.out.println("calculating all rates");
        for (BillItem tbi : getBillItems()) {
            calculateRates(tbi);
            calculateBillItemForEditing(tbi);
        }
        calTotal();
    }

    public void calculateRates(BillItem bi) {
        System.out.println("calculating rates");
        if (bi.getPharmaceuticalBillItem().getStock() == null) {
            System.out.println("stock is null");
            return;
        }
        getBillItem();
        bi.setRate(bi.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate());
        bi.setDiscountRate(calculateBillItemDiscountRate(bi));
        bi.setNetRate(bi.getRate() - bi.getDiscountRate());
    }

    public double calculateBillItemDiscountRate(BillItem bi) {
        System.out.println("bill item discount rate");
        System.out.println("getPaymentScheme() = " + getPaymentScheme());
        if (getPaymentScheme() == null) {
            System.out.println("ps is null");
            return 0.0;
        }
        if (bi == null) {
            System.out.println("bi is null");
            return 0.0;
        }
        if (bi.getPharmaceuticalBillItem() == null) {
            System.out.println("pi is null");
            return 0.0;
        }
        if (bi.getPharmaceuticalBillItem().getStock() == null) {
            System.out.println("stock is null");
            return 0.0;
        }
        if (bi.getPharmaceuticalBillItem().getStock().getItemBatch() == null) {
            System.out.println("batch is null");
            return 0.0;
        }
        double tr = bi.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate();
        System.out.println("tr = " + tr);
        double tdp = getPaymentScheme().getDiscountPercentForPharmacy();
        System.out.println("tdp = " + tdp);
        double dr;
        dr = (tr * tdp) / 100;
        System.out.println("dr = " + dr);
        return dr;

    }

    private void clearBill() {
        deptAdjustmentPreBill = null;
        saleBill = null;
        newPatient = null;
        searchedPatient = null;
        billItems = null;
        patientTabId = "tabNewPt";
        cashPaid = 0;
        netTotal = 0;
        balance = 0;
        comment = "";
    }

    private void clearBillItem() {
        billItem = null;
        removingBillItem = null;
        editingBillItem = null;
        qty = null;
        pr=null;
        rsr=null;
        wsr=null;
        stock = null;
        editingQty = null;
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

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
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

    public Patient getNewPatient() {
        if (newPatient == null) {
            newPatient = new Patient();
            Person p = new Person();

            newPatient.setPerson(p);
        }
        return newPatient;
    }

    public void setNewPatient(Patient newPatient) {
        this.newPatient = newPatient;
    }

    public Patient getSearchedPatient() {
        return searchedPatient;
    }

    public void setSearchedPatient(Patient searchedPatient) {
        this.searchedPatient = searchedPatient;
    }

    public YearMonthDay getYearMonthDay() {
        if (yearMonthDay == null) {
            yearMonthDay = new YearMonthDay();
        }
        return yearMonthDay;
    }

    public void setYearMonthDay(YearMonthDay yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }

    public Bill getDeptAdjustmentPreBill() {
        if (deptAdjustmentPreBill == null) {
            deptAdjustmentPreBill = new PreBill();
            deptAdjustmentPreBill.setBillType(BillType.PharmacySale);
        }
        return deptAdjustmentPreBill;
    }

    public void setDeptAdjustmentPreBill(Bill deptAdjustmentPreBill) {
        this.deptAdjustmentPreBill = deptAdjustmentPreBill;
    }

    public Bill getSaleBill() {
        if (saleBill == null) {
            saleBill = new BilledBill();
            saleBill.setBillType(BillType.PharmacySale);
        }
        return saleBill;
    }

    public void setSaleBill(Bill saleBill) {
        this.saleBill = saleBill;
    }

    public String getPatientTabId() {
        return patientTabId;
    }

    public void setPatientTabId(String patientTabId) {
        this.patientTabId = patientTabId;
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

    public String getStrTenderedValue() {
        return strTenderedValue;
    }

    public void setStrTenderedValue(String strTenderedValue) {
        this.strTenderedValue = strTenderedValue;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public PaymentScheme getPaymentScheme() {
        return paymentScheme;
    }

    public void setPaymentScheme(PaymentScheme paymentScheme) {
        if (this.paymentScheme != paymentScheme) {
            this.paymentScheme = paymentScheme;
            calculateAllRates();
        } else {
            this.paymentScheme = paymentScheme;
        }
    }

    public String getCreditCardRefNo() {
        return creditCardRefNo;
    }

    public void setCreditCardRefNo(String creditCardRefNo) {
        this.creditCardRefNo = creditCardRefNo;
    }

    public Institution getCreditBank() {
        return creditBank;
    }

    public void setCreditBank(Institution creditBank) {
        this.creditBank = creditBank;
    }

    public String getChequeRefNo() {
        return chequeRefNo;
    }

    public void setChequeRefNo(String chequeRefNo) {
        this.chequeRefNo = chequeRefNo;
    }

    public Institution getChequeBank() {
        return chequeBank;
    }

    public void setChequeBank(Institution chequeBank) {
        this.chequeBank = chequeBank;
    }

    public Date getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(Date chequeDate) {
        this.chequeDate = chequeDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Institution getSlipBank() {
        return slipBank;
    }

    public void setSlipBank(Institution slipBank) {
        this.slipBank = slipBank;
    }

    public Date getSlipDate() {
        return slipDate;
    }

    public void setSlipDate(Date slipDate) {
        this.slipDate = slipDate;
    }

    public Institution getCreditCompany() {
        return creditCompany;
    }

    public void setCreditCompany(Institution creditCompany) {
        this.creditCompany = creditCompany;
    }

    public double getCashPaid() {
        return cashPaid;
    }

    public void setCashPaid(double cashPaid) {
        balance = cashPaid - netTotal;
        this.cashPaid = cashPaid;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        balance = cashPaid - netTotal;
        this.netTotal = netTotal;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isBillPreview() {
        return billPreview;
    }

    public void setBillPreview(boolean billPreview) {
        this.billPreview = billPreview;
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

    public void setPr(Double pr) {
        this.pr = pr;
    }

    public Double getRsr() {
        return rsr;
    }

    public void setRsr(Double rsr) {
        this.rsr = rsr;
    }

    public Double getWsr() {
        return wsr;
    }

    public void setWsr(Double wsr) {
        this.wsr = wsr;
    }

    
    
    
}
