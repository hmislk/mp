/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.PaymentSchemeController;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.Title;
import com.divudi.data.dataStructure.PaymentMethodData;
import com.divudi.data.dataStructure.YearMonthDay;

import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CashTransactionBean;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Item;
import com.divudi.entity.Patient;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.Person;
import com.divudi.entity.PreBill;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.entity.pharmacy.UserStock;
import com.divudi.entity.pharmacy.UserStockContainer;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import com.divudi.facade.StockFacade;
import com.divudi.facade.StockHistoryFacade;
import com.divudi.facade.UserStockContainerFacade;
import com.divudi.facade.UserStockFacade;
import com.divudi.facade.VmpFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.faces.event.AjaxBehaviorEvent;
import javax.enterprise.context.SessionScoped;
;
import javax.inject.Inject;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author Buddhika
 */


@Named
@SessionScoped
public class PharmacyWholesaleController implements Serializable {

    /**
     * Creates a new instance of PharmacySaleController
     */
    public PharmacyWholesaleController() {
    }

    @Inject
    PaymentSchemeController PaymentSchemeController;

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
    VmpFacade vmpFacade;
/////////////////////////
    Item selectedAvailableAmp;
    private PreBill preBill;
    private Bill saleBill;
    Bill printBill;
    Bill bill;
    BillItem billItem;
    //BillItem removingBillItem;
    BillItem editingBillItem;
    Double qty;
    Double freeQty;
    Stock stock;
    Stock replacableStock;

    PaymentScheme paymentScheme;

    int activeIndex;

    private Patient newPatient;
    private Patient searchedPatient;
    private YearMonthDay yearMonthDay;
    private String patientTabId = "tabNewPt";
    private String strTenderedValue = "";
    boolean billPreview = false;
    Vmp selectedGeneric;
    /////////////////
    List<Stock> replaceableStocks;
    List<Item> itemsWithoutStocks;
    List<Stock> stocksWithGeneric;
    List<Vmp> selectedGenerics;
    /////////////////////////   
    double cashPaid;
    double netTotal;
    double balance;
    Double editingQty;
    String cashPaidStr;
    ///////////////////
    private UserStockContainer userStockContainer;
    PaymentMethodData paymentMethodData;

    String errorMessage;

    public List<Stock> getStocksWithGeneric() {
        return stocksWithGeneric;
    }

    public void setStocksWithGeneric(List<Stock> stocksWithGeneric) {
        this.stocksWithGeneric = stocksWithGeneric;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public void makeNull() {
        selectedAvailableAmp = null;
        preBill = null;
        saleBill = null;
        printBill = null;
        bill = null;
        billItem = null;
        editingBillItem = null;
        qty = null;
        freeQty=null;
        stock = null;
        paymentScheme = null;
        activeIndex = 0;
        newPatient = null;
        searchedPatient = null;
        yearMonthDay = null;
        patientTabId = "tabNewPt";
        strTenderedValue = "";
        billPreview = false;
        replaceableStocks = null;
        itemsWithoutStocks = null;
        paymentMethodData = null;
        cashPaid = 0;
        netTotal = 0;
        balance = 0;
        editingQty = null;
        cashPaidStr = null;
    }

    public String getCashPaidStr() {
        if (cashPaid == 0.0) {
            cashPaidStr = "";
        } else {
            cashPaidStr = String.format("%1$,.2f", cashPaid);
        }
        return cashPaidStr;
    }

    public void setCashPaidStr(String cashPaidStr) {
        try {
            setCashPaid(Double.valueOf(cashPaidStr));
        } catch (Exception e) {
            setCashPaid(0);
        }
        this.cashPaidStr = cashPaidStr;
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

    public double getOldQty(BillItem bItem) {
        String sql = "Select b.qty From BillItem b where b.retired=false and b.bill=:b and b=:itm";
        HashMap hm = new HashMap();
        hm.put("b", getPreBill());
        hm.put("itm", bItem);
        return getBillItemFacade().findDoubleByJpql(sql, hm);
    }

    public void onEdit(RowEditEvent event) {
        BillItem tmp = (BillItem) event.getObject();
        onEdit(tmp);
    }

    private void setZeroToQty(BillItem tmp) {
        tmp.setQty(0.0);
        tmp.getPharmaceuticalBillItem().setQtyInUnit(0.0f);

        tmp.getTransUserStock().setUpdationQty(0);
        getUserStockFacade().edit(tmp.getTransUserStock());
    }

    //Check when edititng Qty
    //
    public boolean onEdit(BillItem tmp) {
        errorMessage = null;
        //Cheking Minus Value && Null
        if (tmp.getQty() <= 0 || tmp.getQty() == null) {
            setZeroToQty(tmp);
            onEditCalculation(tmp);
            errorMessage = "Can not enter a minus value.";
//            UtilityController.addErrorMessage("Can not enter a minus value");
            return true;
        }

        if (tmp.getQty() + tmp.getPharmaceuticalBillItem().getFreeQty() > tmp.getPharmaceuticalBillItem().getStock().getStock()) {
            setZeroToQty(tmp);
            onEditCalculation(tmp);
            errorMessage = "No sufficient stocks.";
//            UtilityController.addErrorMessage("No Sufficient Stocks?");
            return true;
        }

        //Check Is There Any Other User using same Stock
        if (!getPharmacyBean().isStockAvailable(tmp.getPharmaceuticalBillItem().getStock(), tmp.getQty() + tmp.getPharmaceuticalBillItem().getFreeQty(), getSessionController().getLoggedUser())) {

            setZeroToQty(tmp);
            onEditCalculation(tmp);

            errorMessage = "Another user also billing the same stock. Quentity is resetted.";

//            UtilityController.addErrorMessage("Another User On Change Bill Item "
//                    + " Qty value is resetted");
            return true;
        }

        tmp.getTransUserStock().setUpdationQty(tmp.getQty()+tmp.getPharmaceuticalBillItem().getFreeQty());
        getUserStockFacade().edit(tmp.getTransUserStock());

        onEditCalculation(tmp);

        return false;
    }

    private void onEditCalculation(BillItem tmp) {
        tmp.setGrossValue(tmp.getQty() * tmp.getRate());
        tmp.getPharmaceuticalBillItem().setQtyInUnit((double) (0 - tmp.getQty()));
        tmp.getPharmaceuticalBillItem().setFreeQtyInUnit((double) (0 - tmp.getPharmaceuticalBillItem().getFreeQty()));
        
        
        calculateBillItemForEditing(tmp);

        calTotal();

    }

    public void editQty(BillItem bi) {
        if (bi == null) {
            //////System.out.println("No Bill Item to Edit Qty");
            return;
        }
        if (editingQty == null) {
            //////System.out.println("Editing qty is null");
            return;
        }

        bi.setQty(editingQty);
        bi.getPharmaceuticalBillItem().setQtyInUnit((double) (0 - editingQty));
        calculateBillItemForEditing(bi);

        calTotal();
        editingQty = null;
    }

    private Patient savePatient() {
        switch (getPatientTabId()) {
            case "tabNewPt":
                if (!getNewPatient().getPerson().getName().trim().equals("")) {
                    getNewPatient().setCreater(getSessionController().getLoggedUser());
                    getNewPatient().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
                    getNewPatient().getPerson().setCreater(getSessionController().getLoggedUser());
                    getNewPatient().getPerson().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
                    getPersonFacade().create(getNewPatient().getPerson());
                    getPatientFacade().create(getNewPatient());
                    return getNewPatient();
                } else {
                    return null;
                }
            case "tabSearchPt":
                return getSearchedPatient();
        }
        return null;
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
        errorMessage = null;
        if (qty != null && qty <= 0) {
//            UtilityController.addErrorMessage("Can not enter a minus value");
            errorMessage = "Can not enter a minus value";
            return;
        }
        this.qty = qty;
    }

    public Double getFreeQty() {
        return freeQty;
    }

    public void setFreeQty(Double freeQty) {
        this.freeQty = freeQty;
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

    public Item getSelectedAvailableAmp() {
        return selectedAvailableAmp;
    }

    public void setSelectedAvailableAmp(Item selectedAvailableAmp) {
        this.selectedAvailableAmp = selectedAvailableAmp;
    }

    public void makeStockAsBillItemStock() {
        ////System.out.println("replacableStock = " + replacableStock);
        setStock(replacableStock);
        ////System.out.println("getStock() = " + getStock());
    }

    public void fillReplaceableStocksForAmp(Amp ampIn) {
        String sql;
        Map m = new HashMap();
        double d = 0.0;
        Amp amp = (Amp) ampIn;
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        m.put("s", d);
        m.put("vmp", amp.getVmp());
        m.put("a", amp);
        sql = "select i from Stock i join treat(i.itemBatch.item as Amp) amp "
                + "where i.stock >:s and "
                + "i.department=:d and "
                + "amp.vmp=:vmp "
                + "and amp<>:a "
                + "order by i.itemBatch.item.name";
        replaceableStocks = getStockFacade().findBySQL(sql, m);
    }

    public void selectReplaceableStocks() {
        if (selectedAvailableAmp == null || !(selectedAvailableAmp instanceof Amp)) {
            replaceableStocks = new ArrayList<>();
            return;
        }
        fillReplaceableStocksForAmp((Amp) selectedAvailableAmp);
    }

//    public void selectStocksFromGeneric() {
//        if (selectedGeneric == null) {
//            stocksWithGeneric = new ArrayList<>();
//            return;
//        }
//        String sql;
//        Map m = new HashMap();
//        double d = 0.0;
//        m.put("d", getSessionController().getLoggedUser().getDepartment());
//        m.put("s", d);
//        m.put("vmp", selectedGeneric);
//        sql = "select i from Stock i join treat(i.itemBatch.item as Amp) amp "
//                + "where i.stock >:s and "
//                + "i.department=:d and "
//                + "amp.vmp=:vmp "
//                + "order by i.itemBatch.item.name";
//        selectedGenerics = getStockFacade().findBySQL(sql, m, 10);
//    }
    public List<Item> getItemsWithoutStocks() {
        return itemsWithoutStocks;
    }

    public void setItemsWithoutStocks(List<Item> itemsWithoutStocks) {
        this.itemsWithoutStocks = itemsWithoutStocks;
    }

    public String newSaleBillWithoutReduceStock() {

        clearBill();
        clearBillItem();
        billPreview = false;
        return "pharmacy_wholesale";
    }

    public String newSaleBillWithoutReduceStockForCashier() {

        clearBill();
        clearBillItem();
        billPreview = false;
        return "pharmacy_whoolesale_for_cashier";
    }

    public String newSaleBill() {
        getPharmacyBean().retiredAllUserStockContainer(getSessionController().getLoggedUser());
        clearBill();
        clearBillItem();
        billPreview = false;
        return "pharmacy_wholesale";
    }

    public String newSaleBillForCashier() {
//        reAddToStock();
        getPharmacyBean().retiredAllUserStockContainer(getSessionController().getLoggedUser());

        clearBill();
        clearBillItem();
        billPreview = false;
        return "pharmacy_wholesale_for_cashier";
    }

    public List<Item> completeRetailSaleItemsWithoutStocks(String qry) {
        Map m = new HashMap<>();
        List<Item> items;
        String sql;

        if (qry.length() > 4) {
            sql = "select i from Amp i "
                    + "where i.retired=false and "
                    + "(upper(i.name) like :n or upper(i.code) like :n  or upper(i.barcode) like :n  or upper(i.vmp.name) like :n) and "
                    + "i.id not in(select ibs.itemBatch.item.id from Stock ibs where ibs.stock >:s and ibs.department=:d and (upper(ibs.itemBatch.item.name) like :n or upper(ibs.itemBatch.item.code) like :n  or upper(ibs.itemBatch.item.barcode) like :n  or upper(ibs.itemBatch.item.vmp.name) like :n )  ) "
                    + "order by i.name ";

        } else {

            sql = "select i from Amp i "
                    + "where i.retired=false and "
                    + "(upper(i.name) like :n or upper(i.code) like :n or upper(i.vmp.name) like :n) and "
                    + "i.id not in(select ibs.itemBatch.item.id from Stock ibs where ibs.stock >:s and ibs.department=:d and (upper(ibs.itemBatch.item.name) like :n or upper(ibs.itemBatch.item.code) like :n or upper(ibs.itemBatch.item.vmp.name) like :n )  ) "
                    + "order by i.name ";

        }

//        if (qry.length() > 4) {
//            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n or upper(i.itemBatch.item.barcode) like :n or upper(i.itemBatch.item.vmp.name) like :n) order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
//        } else {
//            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n or upper(i.itemBatch.item.vmp.name) like :n)  order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
//        }
//        
//        sql = "select i from Amp i "
//                + "where i.retired=false and "
//                + "upper(i.name) like :n and "
//                + "i.id not in(select ibs.itemBatch.item.id from Stock ibs where ibs.stock >:s and ibs.department=:d and ibs.itemBatch.item.name like :n) "
//                + "order by i.name ";
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        m.put("n", "%" + qry + "%");
        double s = 0.0;
        m.put("s", s);
        items = getItemFacade().findBySQL(sql, m, 10);
        return items;
    }

    public List<Stock> completeRetailSaleItemsFromGeneric(String qry) {
        Map m = new HashMap<>();
        List<Stock> stocks;
        String sql;
        sql = "select i from Stock i join treat(i.itemBatch.item as Amp) amp "
                + "where i.stock >:s and "
                + "i.department=:d and "
                + "upper(amp.vmp.name) like :n "
                + "order by i.itemBatch.item.name";
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        m.put("n", "%" + qry + "%");
        double s = 0.0;
        m.put("s", s);
        stocks = getStockFacade().findBySQL(sql, m, 10);
        return stocks;
    }

//    public void completeGenerics(String qry) {
//        Map m = new HashMap<>();
//        String sql;
//        sql = "select i from Vmp "
//                + "where upper(i.name) like :n "
//                + "order by i.name";
//        m.put("n", "%" + qry + "%");
//        selectedGenerics = getVmpFacade().findBySQL(sql, m);
//    }
    public List<Stock> completeAvailableStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("s", d);
        qry = qry.replaceAll("\n", "");
        qry = qry.replaceAll("\r", "");
        m.put("n", "%" + qry.toUpperCase().trim() + "%");

        ////System.out.println("qry = " + qry);
        if (qry.length() > 4) {
            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n or upper(i.itemBatch.item.barcode) like :n )  order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        } else {
            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n)  order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        }

        items = getStockFacade().findBySQL(sql, m, 20);

        if (qry.length() > 5 && items.size() == 1) {
            stock = items.get(0);
            handleSelectAction();
        } else if (!qry.trim().equals("") && qry.length() > 4) {
            itemsWithoutStocks = completeRetailSaleItemsWithoutStocks(qry);
//            completeGenerics(qry);
//            stocksWithGeneric = completeRetailSaleItemsFromGeneric(qry);
        }
        return items;
    }

    public List<Stock> completeAvailableStocksFromNameOrGeneric(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("s", d);
        qry = qry.replaceAll("\n", "");
        qry = qry.replaceAll("\r", "");
        m.put("n", "%" + qry.toUpperCase().trim() + "%");

        ////System.out.println("qry = " + qry);
        if (qry.length() > 4) {
            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n or upper(i.itemBatch.item.barcode) like :n or upper(i.itemBatch.item.vmp.name) like :n) order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        } else {
            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n or upper(i.itemBatch.item.vmp.name) like :n)  order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        }

        items = getStockFacade().findBySQL(sql, m, 20);

        if (qry.length() > 5 && items.size() == 1) {
            stock = items.get(0);
            handleSelectAction();
        } else if (!qry.trim().equals("") && qry.length() > 4) {
            itemsWithoutStocks = completeRetailSaleItemsWithoutStocks(qry);
        }
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

    private boolean errorCheckForPreBill() {
        errorMessage = null;
        if (getPreBill().getBillItems().isEmpty()) {
//            UtilityController.addErrorMessage("No Items added to bill to sale");
            errorMessage = "No items in the bill for sale. NOT settled.";
            return true;
        }
        return false;
    }

//    private boolean checkPaymentScheme(PaymentScheme paymentScheme) {
//        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Cheque) {
//            if (getSaleBill().getBank() == null || getSaleBill().getChequeRefNo() == null || getSaleBill().getChequeDate() == null) {
//                UtilityController.addErrorMessage("Please select Cheque Number,Bank and Cheque Date");
//                return true;
//            }
//
//        }
//
//        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Slip) {
//            if (getSaleBill().getBank() == null || getSaleBill().getComments() == null || getSaleBill().getChequeDate() == null) {
//                UtilityController.addErrorMessage("Please Fill Memo,Bank and Slip Date ");
//                return true;
//            }
//
//        }
//
//        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Card) {
//            if (getSaleBill().getBank() == null || getSaleBill().getCreditCardRefNo() == null) {
//                UtilityController.addErrorMessage("Please Fill Credit Card Number and Bank");
//                return true;
//            }
//
////            if (getCreditCardRefNo().trim().length() < 16) {
////                UtilityController.addErrorMessage("Enter 16 Digit");
////                return true;
////            }
//        }
//
//        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Credit) {
//            if (getSaleBill().getCreditCompany() == null) {
//                UtilityController.addErrorMessage("Please Select Credit Company");
//                return true;
//            }
//
//        }
//
//        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Cash) {
//            if (getPreBill().getCashPaid() == 0.0) {
//                UtilityController.addErrorMessage("Please select tendered amount correctly");
//                return true;
//            }
//            if (getPreBill().getCashPaid() < getPreBill().getNetTotal()) {
//                UtilityController.addErrorMessage("Please select tendered amount correctly");
//                return true;
//            }
//        }
//
//        return false;
//
//    }
    @Inject
    PaymentSchemeController paymentSchemeController;

    private boolean errorCheckForSaleBill() {
//        if (checkPaymentScheme(getSaleBill().getPaymentScheme())) {
//            return true;
//        }
        errorMessage = null;
        getPaymentSchemeController().errorCheckPaymentScheme(getPaymentScheme().getPaymentMethod(), paymentMethodData);

        if (getPreBill().getPaymentScheme().getPaymentMethod() == PaymentMethod.Cash) {
            if (cashPaid == 0.0) {
//                UtilityController.addErrorMessage("Please select tendered amount correctly");
                errorMessage = "Please enter tendered value correctly.";
                return true;
            }
            if (cashPaid < getNetTotal()) {
                errorMessage = "Tendered value is less than the bill value.";
//                UtilityController.addErrorMessage("Please select tendered amount correctly");
                return true;
            }
        }
        return false;
    }

    private void savePreBillFinally(Patient pt) {
        getPreBill().setInsId(getBillNumberBean().institutionBillNumberGeneratorByPayment(getSessionController().getInstitution(), getPreBill(), BillType.PharmacyWholesalePre, BillNumberSuffix.SALE));
        getPreBill().setDeptId(getBillNumberBean().institutionBillNumberGeneratorByPayment(getSessionController().getDepartment(), getPreBill(), BillType.PharmacyWholesalePre, BillNumberSuffix.SALE));

        getPreBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getPreBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getPreBill().setCreatedAt(Calendar.getInstance().getTime());
        getPreBill().setCreater(getSessionController().getLoggedUser());

        getPreBill().setPatient(pt);

        getPreBill().setToDepartment(null);
        getPreBill().setToInstitution(null);
        getPreBill().setBillDate(new Date());
        getPreBill().setBillTime(new Date());
        getPreBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        getPreBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
        getPreBill().setPaymentMethod(paymentScheme.getPaymentMethod());
        getPreBill().setPaymentScheme(paymentScheme);

        getBillBean().setPaymentMethodData(getPreBill(), getPaymentScheme().getPaymentMethod(), getPaymentMethodData());

        getBillFacade().create(getPreBill());

    }

    @EJB
    private UserStockContainerFacade userStockContainerFacade;

    private void saveUserStockContainer() {
        getPharmacyBean().retiredAllUserStockContainer(getSessionController().getLoggedUser());

        getUserStockContainer().setCreater(getSessionController().getLoggedUser());
        getUserStockContainer().setCreatedAt(new Date());

        getUserStockContainerFacade().create(getUserStockContainer());

    }

    @EJB
    private BillBean billBean;

    private void saveSaleBill(Patient tmpPatient) {
        calculateAllRates();

        getSaleBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getSaleBill().setInstitution(getSessionController().getLoggedUser().getInstitution());

        getSaleBill().setToDepartment(null);
        getSaleBill().setToInstitution(null);

        getSaleBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        getSaleBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getSaleBill().setGrantTotal(getPreBill().getGrantTotal());
        getSaleBill().setDiscount(getPreBill().getDiscount());
        getSaleBill().setNetTotal(getPreBill().getNetTotal());
        getSaleBill().setTotal(getPreBill().getTotal());
//        getSaleBill().setRefBill(getPreBill());

        getSaleBill().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getSaleBill().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getSaleBill().setPatient(tmpPatient);
        getSaleBill().setPaymentScheme(getPreBill().getPaymentScheme());
        getSaleBill().setReferenceBill(getPreBill());
        getSaleBill().setCreatedAt(Calendar.getInstance().getTime());
        getSaleBill().setCreater(getSessionController().getLoggedUser());
        //       getSaleBill().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), null, BillType.PharmacySale));
//        getSaleBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(
//                getSessionController().getInstitution(), new BilledBill(), BillType.PharmacySale, BillNumberSuffix.PHSAL));
        getSaleBill().setInsId(getPreBill().getInsId());
        getSaleBill().setDeptId(getPreBill().getDeptId());

        getSaleBill().setReferredBy(getPreBill().getReferredBy());
        getBillBean().setPaymentMethodData(getSaleBill(), getPaymentScheme().getPaymentMethod(), getPaymentMethodData());

        getBillFacade().create(getSaleBill());

        updatePreBill();
    }
//

    private void updatePreBill() {
        getPreBill().setReferenceBill(getSaleBill());
        getBillFacade().edit(getPreBill());
    }

    private void savePreBillItemsFinally(List<BillItem> list) {
        for (BillItem tbi : list) {
            if (onEdit(tbi)) {//If any issue in Stock Bill Item will not save & not include for total
                continue;
            }

            tbi.setBill(getPreBill());

            tbi.setCreatedAt(Calendar.getInstance().getTime());
            tbi.setCreater(getSessionController().getLoggedUser());

            PharmaceuticalBillItem tmpPh = tbi.getPharmaceuticalBillItem();
            tbi.setPharmaceuticalBillItem(null);

            getBillItemFacade().create(tbi);
            getPharmaceuticalBillItemFacade().create(tmpPh);

            tbi.setPharmaceuticalBillItem(tmpPh);
            getBillItemFacade().edit(tbi);

            double qtyL = tbi.getPharmaceuticalBillItem().getQtyInUnit() + tbi.getPharmaceuticalBillItem().getFreeQtyInUnit();

            //Deduct Stock
            boolean returnFlag = getPharmacyBean().deductFromStock(tbi.getPharmaceuticalBillItem().getStock(),
                    Math.abs(qtyL), tbi.getPharmaceuticalBillItem(), getPreBill().getDepartment());

            if (!returnFlag) {
                tbi.setTmpQty(0);
                getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());
                getBillItemFacade().edit(tbi);
            }

            getPreBill().getBillItems().add(tbi);
        }

        getPharmacyBean().retiredAllUserStockContainer(getSessionController().getLoggedUser());

        calculateAllRates();

        getBillFacade().edit(getPreBill());
    }

    private void saveSaleBillItems() {
        for (BillItem tbi : getPreBill().getBillItems()) {

            BillItem newBil = new BillItem();

            newBil.copy(tbi);
            newBil.setReferanceBillItem(tbi);
            newBil.setBill(getSaleBill());
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

            tbi.setReferanceBillItem(newBil);
            getBillItemFacade().edit(tbi);
        }
        getBillFacade().edit(getSaleBill());
    }

    @EJB
    private UserStockFacade userStockFacade;

    private UserStock saveUserStock(BillItem tbi) {
        UserStock us = new UserStock();
        us.setStock(tbi.getPharmaceuticalBillItem().getStock());
        us.setUpdationQty(tbi.getQty());
        us.setCreater(getSessionController().getLoggedUser());
        us.setCreatedAt(new Date());
        us.setUserStockContainer(getUserStockContainer());
        getUserStockFacade().create(us);

        getUserStockContainer().getUserStocks().add(us);

        return us;
    }

    private boolean checkAllBillItem() {
        for (BillItem b : getPreBill().getBillItems()) {

            if (onEdit(b)) {
                return true;
            }
        }

        return false;

    }

    public void settlePreBill() {
        editingQty = null;

        if (checkAllBillItem()) {// Before Settle Bill Current Bills Item Check Agian There is any otheruser change his qty
            return;
        }

        if (errorCheckForPreBill()) {
            return;
        }
        Patient pt = savePatient();

        List<BillItem> tmpBillItems = getPreBill().getBillItems();
        getPreBill().setBillItems(null);

        savePreBillFinally(pt);

        savePreBillItemsFinally(tmpBillItems);

        setPrintBill(getBillFacade().find(getPreBill().getId()));

        clearBill();
        clearBillItem();

        billPreview = true;
    }

    @EJB
    private CashTransactionBean cashTransactionBean;

    public void settleBillWithPay() {
        editingQty = null;

        if (checkAllBillItem()) {
            return;
        }

        if (errorCheckForSaleBill()) {
            return;
        }

        Patient pt = savePatient();
        getPreBill().setPaidAmount(getPreBill().getTotal());

        List<BillItem> tmpBillItems = getPreBill().getBillItems();
        getPreBill().setBillItems(null);

        savePreBillFinally(pt);
        savePreBillItemsFinally(tmpBillItems);

        saveSaleBill(pt);
        saveSaleBillItems();

        getCashTransactionBean().saveBillCashInTransaction(getSaleBill(), getSessionController().getLoggedUser());

        setPrintBill(getBillFacade().find(getSaleBill().getId()));

        clearBill();
        clearBillItem();
        billPreview = true;

    }

    public String newPharmacyRetailSale() {
        clearBill();
        clearBillItem();
        billPreview = false;
        return "pharmacy_wholesale";
    }

    private boolean checkItemBatch() {
        for (BillItem bItem : getPreBill().getBillItems()) {
            if (Objects.equals(bItem.getPharmaceuticalBillItem().getStock().getId(), getBillItem().getPharmaceuticalBillItem().getStock().getId())) {
                return true;
            }
        }

        return false;
    }

    public void addBillItem() {
        editingQty = null;
        errorMessage = null;

        if (billItem == null) {
            return;
        }
        if (billItem.getPharmaceuticalBillItem() == null) {
            return;
        }
        if (getStock() == null) {
            errorMessage = "Item?";
//            UtilityController.addErrorMessage("Item?");
            return;
        }
        if (getQty() == null) {
            errorMessage = "Quentity?";
//            UtilityController.addErrorMessage("Quentity?");
            return;
        }

        if (getQty() > getStock().getStock()) {
            errorMessage = "No sufficient stocks.";
//            UtilityController.addErrorMessage("No Sufficient Stocks?");
            return;
        }

        if (checkItemBatch()) {
            errorMessage = "This batch is already there in the bill.";
//            UtilityController.addErrorMessage("Already added this item batch");
            return;
        }
        //Checking User Stock Entity
        if (!getPharmacyBean().isStockAvailable(getStock(), getQty(), getSessionController().getLoggedUser())) {
            UtilityController.addErrorMessage("Sorry Already Other User Try to Billing This Stock You Cant Add");
            return;
        }

        billItem.getPharmaceuticalBillItem().setFreeQty(freeQty);
        
        billItem.getPharmaceuticalBillItem().setQtyInUnit((double) (0 - qty));
        billItem.getPharmaceuticalBillItem().setFreeQtyInUnit((double) (0 - freeQty));
        
        billItem.getPharmaceuticalBillItem().setStock(stock);
        
        billItem.getPharmaceuticalBillItem().setItemBatch(getStock().getItemBatch());
        
        calculateBillItem();


        billItem.setItem(getStock().getItemBatch().getItem());
        billItem.setBill(getPreBill());

        billItem.setSearialNo(getPreBill().getBillItems().size() + 1);
        getPreBill().getBillItems().add(billItem);

        if (getUserStockContainer().getId() == null) {
            saveUserStockContainer();
        }

        UserStock us = saveUserStock(billItem);
        billItem.setTransUserStock(us);

        calculateAllRates();

        calTotal();

        clearBillItem();
        setActiveIndex(1);
    }

    public void calTotal() {
        getPreBill().setTotal(0);
        double netTot = 0.0;
        double discount = 0.0;
        double grossTot = 0.0;
        int index = 0;
        for (BillItem b : getPreBill().getBillItems()) {
            if (b.isRetired()) {
                continue;
            }
            b.setSearialNo(index++);

            netTot = netTot + b.getNetValue();
            grossTot = grossTot + b.getGrossValue();
            discount = discount + b.getDiscount();
            getPreBill().setTotal(getPreBill().getTotal() + b.getNetValue());
        }

        //   netTot = netTot + getPreBill().getServiceCharge();
        getPreBill().setNetTotal(netTot);
        getPreBill().setTotal(grossTot);
        getPreBill().setGrantTotal(grossTot);
        getPreBill().setDiscount(discount);
        setNetTotal(getPreBill().getNetTotal());

    }

    @EJB
    private StockHistoryFacade stockHistoryFacade;

    public void removeBillItem(BillItem b) {
        if (b.getTransUserStock().isRetired()) {
            UtilityController.addErrorMessage("This Item Already removed");
            return;
        }

        b.getTransUserStock().setRetired(true);
        b.getTransUserStock().setRetiredAt(new Date());
        b.getTransUserStock().setRetireComments("Remove From Bill ");
        b.getTransUserStock().setRetirer(getSessionController().getLoggedUser());
        getUserStockFacade().edit(b.getTransUserStock());

        getPreBill().getBillItems().remove(b.getSearialNo());

        calTotal();
    }

    public void calculateBillItemListner(AjaxBehaviorEvent event) {
        calculateBillItem();
    }

    public void calculateBillItem() {
        if (stock == null) {
            return;
        }
        if (getPreBill() == null) {
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
        if(getFreeQty()==null){
            freeQty=0.0;
        }
    

        //Bill Item
//        billItem.setInwardChargeType(InwardChargeType.Medicine);
        billItem.setItem(getStock().getItemBatch().getItem());
        billItem.setQty(qty);

        //pharmaceutical Bill Item
        billItem.getPharmaceuticalBillItem().setDoe(getStock().getItemBatch().getDateOfExpire());
        billItem.getPharmaceuticalBillItem().setFreeQty(freeQty);
        billItem.getPharmaceuticalBillItem().setItemBatch(getStock().getItemBatch());
        billItem.getPharmaceuticalBillItem().setQtyInUnit((double) (0 - qty));
        billItem.getPharmaceuticalBillItem().setFreeQtyInUnit((double) (0 - freeQty));

        //Rates
        //Values
        billItem.setGrossValue(getStock().getItemBatch().getWholesaleRate() * qty);
        billItem.setNetValue(qty * billItem.getNetRate());
        billItem.setDiscount(billItem.getGrossValue() - billItem.getNetValue());

    }

    public void calculateBillItemForEditing(BillItem bi) {
        //////System.out.println("calculateBillItemForEditing");
        //////System.out.println("bi = " + bi);
        if (getPreBill() == null || bi == null || bi.getPharmaceuticalBillItem() == null || bi.getPharmaceuticalBillItem().getStock() == null) {
            //////System.out.println("calculateItemForEditingFailedBecause of null");
            return;
        }
        //////System.out.println("bi.getQty() = " + bi.getQty());
        //////System.out.println("bi.getRate() = " + bi.getRate());
        bi.setGrossValue(bi.getPharmaceuticalBillItem().getStock().getItemBatch().getWholesaleRate() * bi.getQty());
        bi.setNetValue(bi.getQty() * bi.getNetRate());
        bi.setDiscount(bi.getGrossValue() - bi.getNetValue());
        //////System.out.println("bi.getNetValue() = " + bi.getNetValue());

    }

    public void handleSelect(SelectEvent event) {
        handleSelectAction();
    }

    public void handleSelectAction() {
        if (stock == null) {
            ////System.out.println("Stock NOT selected.");
        }
        if (getBillItem() == null || getBillItem().getPharmaceuticalBillItem() == null) {
            ////System.out.println("Internal Error at PharmacySaleController.java > handleSelectAction");
        }

        getBillItem().getPharmaceuticalBillItem().setStock(stock);
        calculateRates(billItem);
        if (stock != null && stock.getItemBatch() != null) {
            fillReplaceableStocksForAmp((Amp) stock.getItemBatch().getItem());
        }
    }

    public void paymentSchemeChanged(AjaxBehaviorEvent ajaxBehavior) {
        calculateAllRates();
    }

    public void calculateAllRates() {
        //////System.out.println("calculating all rates");
        for (BillItem tbi : getPreBill().getBillItems()) {
            calculateRates(tbi);
            calculateBillItemForEditing(tbi);
        }
        calTotal();
    }

    public void calculateRateListner(AjaxBehaviorEvent event) {

    }

    public void calculateRates(BillItem bi) {
        //////System.out.println("calculating rates");
        if (bi.getPharmaceuticalBillItem().getStock() == null) {
            //////System.out.println("stock is null");
            return;
        }
        getBillItem();
        bi.setRate(bi.getPharmaceuticalBillItem().getStock().getItemBatch().getWholesaleRate());
        //   ////System.err.println("Rate "+bi.getRate());
        bi.setDiscount(calculateBillItemDiscountRate(bi));
        //  ////System.err.println("Discount "+bi.getDiscount());
        bi.setNetRate(bi.getRate() - bi.getDiscount());
        //  ////System.err.println("Net "+bi.getNetRate());
    }

    public double calculateBillItemDiscountRate(BillItem bi) {
        //////System.out.println("bill item discount rate");
        //////System.out.println("getPaymentScheme() = " + getPaymentScheme());
        if (bi == null) {
            //////System.out.println("bi is null");
            return 0.0;
        }
        if (bi.getPharmaceuticalBillItem() == null) {
            //////System.out.println("pi is null");
            return 0.0;
        }
        if (bi.getPharmaceuticalBillItem().getStock() == null) {
            //////System.out.println("stock is null");
            return 0.0;
        }
        if (bi.getPharmaceuticalBillItem().getStock().getItemBatch() == null) {
            //////System.out.println("batch is null");
            return 0.0;
        }
        bi.setItem(bi.getPharmaceuticalBillItem().getStock().getItemBatch().getItem());
        double tr = bi.getPharmaceuticalBillItem().getStock().getItemBatch().getWholesaleRate();
        //  ////System.err.println("tr = " + tr);
        double tdp = getPaymentScheme().getDiscountPercentForPharmacy();
        //    ////System.err.println("tdp = " + tdp);
        double dr;
        dr = (tr * tdp) / 100;
        //     ////System.err.println("dr = " + dr);

        if (bi.getItem().isDiscountAllowed()) {
            return dr;
        } else {
            return 0;
        }
    }

    private void clearBill() {
        preBill = null;
        saleBill = null;
        newPatient = null;
        searchedPatient = null;
//        billItems = null;
        patientTabId = "tabNewPt";
        cashPaid = 0;
        netTotal = 0;
        balance = 0;
        paymentScheme = null;
        userStockContainer = null;
    }

    private void clearBillItem() {
        billItem = null;
//        removingBillItem = null;
        editingBillItem = null;
        qty = null;
        freeQty=null;
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

    public PreBill getPreBill() {
        if (preBill == null) {
            preBill = new PreBill();
            preBill.setBillType(BillType.PharmacyWholesalePre);
            preBill.setPaymentScheme(getPaymentSchemeController().getItems().get(0));
        }
        return preBill;
    }

    public void setPreBill(PreBill preBill) {
        this.preBill = preBill;
    }

    public Bill getSaleBill() {
        if (saleBill == null) {
            saleBill = new BilledBill();
            saleBill.setBillType(BillType.PharmacyWholeSale);
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

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public Bill getPrintBill() {
        return printBill;
    }

    public void setPrintBill(Bill printBill) {
        this.printBill = printBill;
    }

    public PaymentSchemeController getPaymentSchemeController() {
        return PaymentSchemeController;
    }

    public void setPaymentSchemeController(PaymentSchemeController PaymentSchemeController) {
        this.PaymentSchemeController = PaymentSchemeController;
    }

    public PaymentScheme getPaymentScheme() {
        //  ////System.err.println("GEtting Paymen");
        return paymentScheme;
    }

    public void setPaymentScheme(PaymentScheme paymentScheme) {
        //     ////System.err.println("Setting Pay");
        this.paymentScheme = paymentScheme;
    }

    public StockHistoryFacade getStockHistoryFacade() {
        return stockHistoryFacade;
    }

    public void setStockHistoryFacade(StockHistoryFacade stockHistoryFacade) {
        this.stockHistoryFacade = stockHistoryFacade;
    }

    public UserStockContainer getUserStockContainer() {
        if (userStockContainer == null) {
            userStockContainer = new UserStockContainer();
        }
        return userStockContainer;
    }

    public void setUserStockContainer(UserStockContainer userStockContainer) {
        this.userStockContainer = userStockContainer;
    }

    public UserStockContainerFacade getUserStockContainerFacade() {
        return userStockContainerFacade;
    }

    public void setUserStockContainerFacade(UserStockContainerFacade userStockContainerFacade) {
        this.userStockContainerFacade = userStockContainerFacade;
    }

    public UserStockFacade getUserStockFacade() {
        return userStockFacade;
    }

    public void setUserStockFacade(UserStockFacade userStockFacade) {
        this.userStockFacade = userStockFacade;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public CashTransactionBean getCashTransactionBean() {
        return cashTransactionBean;
    }

    public void setCashTransactionBean(CashTransactionBean cashTransactionBean) {
        this.cashTransactionBean = cashTransactionBean;
    }

    public List<Vmp> getSelectedGenerics() {
        return selectedGenerics;
    }

    public void setSelectedGenerics(List<Vmp> selectedGenerics) {
        this.selectedGenerics = selectedGenerics;
    }

    public VmpFacade getVmpFacade() {
        return vmpFacade;
    }

    public void setVmpFacade(VmpFacade vmpFacade) {
        this.vmpFacade = vmpFacade;
    }

    public Vmp getSelectedGeneric() {
        return selectedGeneric;
    }

    public void setSelectedGeneric(Vmp selectedGeneric) {
        this.selectedGeneric = selectedGeneric;
    }

    public Stock getReplacableStock() {
        return replacableStock;
    }

    public void setReplacableStock(Stock replacableStock) {
        this.replacableStock = replacableStock;
    }

}
