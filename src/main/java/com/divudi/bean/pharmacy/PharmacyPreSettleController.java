/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.PaymentSchemeController;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.Title;
import com.divudi.data.dataStructure.PaymentMethodData;
import com.divudi.data.dataStructure.YearMonthDay;
import com.divudi.data.inward.InwardChargeType;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.Patient;
import com.divudi.entity.Person;
import com.divudi.entity.PreBill;
import com.divudi.entity.RefundBill;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import com.divudi.facade.StockFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author Buddhika
 */
@Named
@SessionScoped
public class PharmacyPreSettleController implements Serializable {

    /**
     * Creates a new instance of PharmacySaleController
     */
    public PharmacyPreSettleController() {
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
/////////////////////////
    Item selectedAlternative;
    Bill saleReturnBill;

    private Bill preBill;
    private Bill saleBill;
    Bill bill;
    BillItem billItem;
    BillItem removingBillItem;
    BillItem editingBillItem;
    Double qty;
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
    //   PaymentScheme paymentScheme;
    private PaymentMethodData paymentMethodData;
    double cashPaid;
    double netTotal;
    double balance;
    Double editingQty;

    public void makeNull() {
        selectedAlternative = null;
        preBill = null;
        saleBill = null;
        saleReturnBill = null;
        bill = null;
        billItem = null;
        removingBillItem = null;
        editingBillItem = null;
        qty = 0.0;
        stock = null;
        newPatient = null;
        searchedPatient = null;
        yearMonthDay = null;
        patientTabId = "tabNewPt";
        strTenderedValue = "";
        billPreview = false;
        replaceableStocks = null;
        billItems = null;
        itemsWithoutStocks = null;
        paymentMethodData = null;
        cashPaid = 0;
        netTotal = 0;
        balance = 0;
        editingQty = null;

    }

    public Double getEditingQty() {
        return editingQty;
    }

    public void setEditingQty(Double editingQty) {
        this.editingQty = editingQty;
    }

    public void onTabChange(TabChangeEvent event) {
        setPatientTabId(event.getTab().getId());

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

    public List<Item> getItemsWithoutStocks() {
        return itemsWithoutStocks;
    }

    public void setItemsWithoutStocks(List<Item> itemsWithoutStocks) {
        this.itemsWithoutStocks = itemsWithoutStocks;
    }

    public String searchBill() {
        clearBill();
        clearBillItem();
        billPreview = false;
        return "pharmacy_search_pre_bill";
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

    @Inject
    private PaymentSchemeController paymentSchemeController;

    private boolean errorCheckForSaleBill() {
//        if (checkPaymentScheme(getSaleBill().getPaymentScheme())) {
//            return true;
//        }

        if (getPreBill().getPaymentScheme() == null) {
            return true;
        }

        if (getPaymentSchemeController().errorCheckPaymentScheme(getPreBill().getPaymentScheme().getPaymentMethod(), paymentMethodData));

        if (getPreBill().getPaymentScheme().getPaymentMethod() == PaymentMethod.Cash) {
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

    @EJB
    private BillBean billBean;

    private void saveSaleBill(Patient tmpPatient) {

        getSaleBill().setBillType(BillType.PharmacySale);

        getSaleBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getSaleBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getSaleBill().setToDepartment(null);
        getSaleBill().setToInstitution(null);

        getSaleBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        getSaleBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        //System.err.println(getPreBill());
        //System.err.println(getPreBill().getGrantTotal());
        //System.err.println(getPreBill().getDiscount());
        //System.err.println(getPreBill().getNetTotal());
        //System.err.println(getPreBill().getTotal());
        getSaleBill().setGrantTotal(getPreBill().getGrantTotal());
        getSaleBill().setDiscount(getPreBill().getDiscount());
        getSaleBill().setNetTotal(getPreBill().getNetTotal());
        getSaleBill().setTotal(getPreBill().getTotal());

        //   getSaleBill().setRefBill(getPreBill());
        getSaleBill().setPaymentScheme(getPreBill().getPaymentScheme());

        getBillBean().setPaymentMethodData(getSaleBill(), getSaleBill().getPaymentScheme().getPaymentMethod(), paymentMethodData);

        getSaleBill().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getSaleBill().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getSaleBill().setPatient(tmpPatient);

        getSaleBill().setReferenceBill(getPreBill());
        getSaleBill().setCreatedAt(Calendar.getInstance().getTime());
        getSaleBill().setCreater(getSessionController().getLoggedUser());

        //       getSaleBill().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), null, BillType.PharmacySale));
//        getSaleBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(
//                getSessionController().getInstitution(), getSaleBill(), BillType.PharmacySale, BillNumberSuffix.PHSAL));
        getSaleBill().setInsId(getPreBill().getInsId());
        getSaleBill().setDeptId(getPreBill().getDeptId());
        getBillFacade().create(getSaleBill());

        updatePreBill();

    }

    private void saveSaleReturnBill(Patient tmpPatient) {

        getSaleReturnBill().setPaymentScheme(getPreBill().getPaymentScheme());
        getSaleReturnBill().setBillType(BillType.PharmacySale);
        getSaleReturnBill().setReferenceBill(getPreBill());

        getSaleReturnBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getSaleReturnBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getSaleReturnBill().setToDepartment(null);
        getSaleReturnBill().setToInstitution(null);

        getSaleReturnBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        getSaleReturnBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getSaleReturnBill().setGrantTotal(getPreBill().getGrantTotal());
        getSaleReturnBill().setDiscount(getPreBill().getDiscount());
        getSaleReturnBill().setNetTotal(getPreBill().getNetTotal());
        getSaleReturnBill().setTotal(getPreBill().getTotal());


        getBillBean().setPaymentMethodData(getSaleReturnBill(), getSaleReturnBill().getPaymentScheme().getPaymentMethod(), paymentMethodData);

        getSaleReturnBill().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getSaleReturnBill().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getSaleReturnBill().setPatient(tmpPatient);

        getSaleReturnBill().setReferenceBill(getPreBill());
        getSaleReturnBill().setCreatedAt(Calendar.getInstance().getTime());
        getSaleReturnBill().setCreater(getSessionController().getLoggedUser());

        //       getSaleReturnBill().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), null, BillType.PharmacySale));
//        getSaleReturnBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(
//                getSessionController().getInstitution(), getSaleReturnBill(), BillType.PharmacySale, BillNumberSuffix.PHSAL));
        getSaleReturnBill().setInsId(getPreBill().getInsId());
        getSaleReturnBill().setDeptId(getPreBill().getDeptId());

        getBillFacade().create(getSaleReturnBill());

        //   updateSaleReturnPreBill();
    }

//     private void updateSaleReturnPreBill() {
//        getPreBill().setReferenceBill(getSaleReturnBill());
//        getBillFacade().edit(getPreBill());
//    }
//  
    private void updatePreBill() {
        getPreBill().setReferenceBill(getSaleBill());

        getBillFacade().edit(getPreBill());

    }

    private void saveSaleBillItems() {
        for (BillItem tbi : getPreBill().getBillItems()) {
            BillItem newBil = new BillItem();
            newBil.copy(tbi);
            newBil.setBill(getSaleBill());
            newBil.setInwardChargeType(InwardChargeType.Medicine);
            //      newBil.setBill(getSaleBill());
            newBil.setCreatedAt(Calendar.getInstance().getTime());
            newBil.setCreater(getSessionController().getLoggedUser());
            getBillItemFacade().create(newBil);

            PharmaceuticalBillItem newPhar = new PharmaceuticalBillItem();
            newPhar.copy(tbi.getPharmaceuticalBillItem());
            newPhar.setBillItem(newBil);
            getPharmaceuticalBillItemFacade().create(newPhar);

            newBil.setPharmaceuticalBillItem(newPhar);
            getBillItemFacade().edit(newBil);

            //   getPharmacyBean().deductFromStock(tbi.getItem(), tbi.getQty(), tbi.getBill().getDepartment());
            getSaleBill().getBillItems().add(newBil);
        }
        getBillFacade().edit(getSaleBill());

    }

    private void saveSaleReturnBillItems() {
        for (BillItem tbi : getPreBill().getBillItems()) {

            BillItem sbi = new BillItem();

            sbi.copy(tbi);
            // sbi.invertValue(tbi);

            sbi.setBill(getSaleReturnBill());
            sbi.setReferanceBillItem(tbi);
            sbi.setCreatedAt(Calendar.getInstance().getTime());
            sbi.setCreater(getSessionController().getLoggedUser());

            getBillItemFacade().create(sbi);

            PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
            ph.copy(tbi.getPharmaceuticalBillItem());

            ph.setBillItem(sbi);
            getPharmaceuticalBillItemFacade().create(ph);

            //        getPharmacyBean().deductFromStock(tbi.getItem(), tbi.getQty(), tbi.getBill().getDepartment());
            getSaleReturnBill().getBillItems().add(sbi);
        }
        getBillFacade().edit(getSaleReturnBill());
    }

    public void settleBillWithPay2() {
        editingQty = null;
        if (errorCheckForSaleBill()) {
            return;
        }

        saveSaleBill(getPreBill().getPatient());
        saveSaleBillItems();

        getPreBill().getCashBillsPre().add(getSaleBill());
        getBillFacade().edit(getPreBill());

        setBill(getBillFacade().find(getSaleBill().getId()));

        clearBill();
        clearBillItem();
        billPreview = true;

    }

    public void settleReturnBillWithPay() {
        editingQty = null;

        saveSaleReturnBill(getPreBill().getPatient());
        saveSaleReturnBillItems();

        getPreBill().getReturnCashBills().add(getSaleReturnBill());
        getBillFacade().edit(getPreBill());

        setBill(getBillFacade().find(getSaleReturnBill().getId()));

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

    private void clearBill() {
        preBill = null;
        saleBill = null;
        newPatient = null;
        searchedPatient = null;
        billItems = null;
        patientTabId = "tabNewPt";
        cashPaid = 0;
        netTotal = 0;
        balance = 0;
    }

    private void clearBillItem() {
        billItem = null;
        removingBillItem = null;
        editingBillItem = null;
        qty = null;
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

    public Bill getPreBill() {
        if (preBill == null) {
            preBill = new PreBill();
            preBill.setBillType(BillType.PharmacyPre);
        }
        return preBill;
    }

    public void setPreBill(Bill preBill) {
        makeNull();
        this.preBill = preBill;
        //System.err.println("Setting Bill " + preBill);
        billPreview = false;

    }

    public Bill getSaleBill() {
        if (saleBill == null) {
            saleBill = new BilledBill();
            saleBill.setBillType(BillType.PharmacySale);
        }
        return saleBill;
    }

    public Bill getSaleReturnBill() {
        if (saleReturnBill == null) {
            saleReturnBill = new RefundBill();
            saleReturnBill.setBillType(BillType.PharmacySale);
        }
        return saleReturnBill;
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

    public PaymentMethodData getPaymentMethodData() {
        if (paymentMethodData == null) {
            paymentMethodData = new PaymentMethodData();
        }
        return paymentMethodData;
    }

    public void setPaymentMethodData(PaymentMethodData paymentMethodData) {
        this.paymentMethodData = paymentMethodData;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public PaymentSchemeController getPaymentSchemeController() {
        return paymentSchemeController;
    }

    public void setPaymentSchemeController(PaymentSchemeController paymentSchemeController) {
        this.paymentSchemeController = paymentSchemeController;
    }

}
