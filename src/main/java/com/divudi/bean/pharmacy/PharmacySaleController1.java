///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.divudi.bean.pharmacy;
//
//import com.divudi.bean.PaymentSchemeController;
//import com.divudi.bean.SessionController;
//import com.divudi.bean.UtilityController;
//import com.divudi.data.BillNumberSuffix;
//import com.divudi.data.BillType;
//import com.divudi.data.PaymentMethod;
//import com.divudi.data.Sex;
//import com.divudi.data.Title;
//import com.divudi.data.dataStructure.YearMonthDay;
//import com.divudi.data.inward.InwardChargeType;
//import com.divudi.ejb.BillNumberBean;
//import com.divudi.ejb.PharmacyBean;
//import com.divudi.entity.Bill;
//import com.divudi.entity.BillItem;
//import com.divudi.entity.BilledBill;
//import com.divudi.entity.Institution;
//import com.divudi.entity.Item;
//import com.divudi.entity.Patient;
//import com.divudi.entity.PaymentScheme;
//import com.divudi.entity.Person;
//import com.divudi.entity.PreBill;
//import com.divudi.entity.pharmacy.Amp;
//import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
//import com.divudi.entity.pharmacy.Stock;
//import com.divudi.facade.BillFacade;
//import com.divudi.facade.BillItemFacade;
//import com.divudi.facade.ItemFacade;
//import com.divudi.facade.PatientFacade;
//import com.divudi.facade.PersonFacade;
//import com.divudi.facade.PharmaceuticalBillItemFacade;
//import com.divudi.facade.StockFacade;
//import com.divudi.facade.StockHistoryFacade;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.TimeZone;
//import javax.ejb.EJB;
//import javax.enterprise.context.SessionScoped;
//import javax.inject.Named;
//import javax.faces.event.AjaxBehaviorEvent;
//import javax.enterprise.context.SessionScoped;
//;
//import javax.inject.Inject;
//import org.primefaces.event.RowEditEvent;
//import org.primefaces.event.SelectEvent;
//import org.primefaces.event.TabChangeEvent;
//
///**
// *
// * @author Buddhika
// */
//
//
//@Named
//@SessionScoped
//public class PharmacySaleController1 implements Serializable {
//
//    /**
//     * Creates a new instance of PharmacySaleController
//     */
//    public PharmacySaleController1() {
//    }
//
//    @Inject
//    PaymentSchemeController PaymentSchemeController;
//
//    @Inject
//    SessionController sessionController;
//////////////////////////
//    @EJB
//    private BillFacade billFacade;
//    @EJB
//    private BillItemFacade billItemFacade;
//    @EJB
//    ItemFacade itemFacade;
//    @EJB
//    StockFacade stockFacade;
//    @EJB
//    PharmacyBean pharmacyBean;
//    @EJB
//    private PersonFacade personFacade;
//    @EJB
//    private PatientFacade patientFacade;
//    @EJB
//    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
//    @EJB
//    BillNumberBean billNumberBean;
///////////////////////////
//    Item selectedAlternative;
//    private PreBill preBill;
//    private Bill saleBill;
//    Bill printBill;
//    Bill bill;
//    BillItem billItem;
//    //BillItem removingBillItem;
//    BillItem editingBillItem;
//    Double qty;
//    Stock stock;
//
//    PaymentScheme paymentScheme;
//
//    int activeIndex;
//
//    private Patient newPatient;
//    private Patient searchedPatient;
//    private YearMonthDay yearMonthDay;
//    private String patientTabId = "tabNewPt";
//    private String strTenderedValue = "";
//    boolean billPreview = false;
//    /////////////////
//    List<Stock> replaceableStocks;
//    //List<BillItem> billItems;
//    List<Item> itemsWithoutStocks;
//    /////////////////////////
//
//    String creditCardRefNo;
//    Institution creditBank;
//    String chequeRefNo;
//    Institution chequeBank;
//    Date chequeDate;
//    String comment;
//    Institution slipBank;
//    Date slipDate;
//    Institution creditCompany;
//    double cashPaid;
//    double netTotal;
//    double balance;
//    Double editingQty;
//    String cashPaidStr;
//
//    public void makeNull() {
//        selectedAlternative = null;
//        preBill = null;
//        saleBill = null;
//        printBill = null;
//        bill = null;
//        billItem = null;
//        editingBillItem = null;
//        qty = null;
//        stock = null;
//        paymentScheme = null;
//        activeIndex = 0;
//        newPatient = null;
//        searchedPatient = null;
//        yearMonthDay = null;
//        patientTabId = "tabNewPt";
//        strTenderedValue = "";
//        billPreview = false;
//        replaceableStocks = null;
//        itemsWithoutStocks = null;
//        creditCardRefNo = null;
//        creditBank = null;
//        chequeRefNo = null;
//        chequeBank = null;
//        chequeDate = null;
//        comment = null;
//        slipBank = null;
//        slipDate = null;
//        creditCompany = null;
//        cashPaid = 0;
//        netTotal = 0;
//        balance = 0;
//        editingQty = null;
//        cashPaidStr = null;
//    }
//
//    public String getCashPaidStr() {
//        if (cashPaid == 0.0) {
//            cashPaidStr = "";
//        } else {
//            cashPaidStr = String.format("%1$,.2f", cashPaid);
//        }
//        return cashPaidStr;
//    }
//
//    public void setCashPaidStr(String cashPaidStr) {
//        try {
//            setCashPaid(Double.valueOf(cashPaidStr));
//        } catch (Exception e) {
//            setCashPaid(0);
//        }
//        this.cashPaidStr = cashPaidStr;
//    }
//
//    public Double getEditingQty() {
//        return editingQty;
//    }
//
//    public void setEditingQty(Double editingQty) {
//        this.editingQty = editingQty;
//    }
//
//    public void onTabChange(TabChangeEvent event) {
//        setPatientTabId(event.getTab().getId());
//
//    }
//
//    public double getOldQty(BillItem bItem) {
//        String sql = "Select b.qty From BillItem b where b.retired=false and b.bill=:b and b=:itm";
//        HashMap hm = new HashMap();
//        hm.put("b", getPreBill());
//        hm.put("itm", bItem);
//        return getBillItemFacade().findDoubleByJpql(sql, hm);
//    }
//
//    private double getStockByBillItem(BillItem bItem) {
//        String sql;
//        Map m = new HashMap();
//        m.put("dep", bItem.getBill().getDepartment());
//        m.put("batch", bItem.getPharmaceuticalBillItem().getItemBatch());
//        sql = "select i.stock from Stock i where "
//                + " i.department=:dep and i.itemBatch=:batch ";
//        return getStockFacade().findDoubleByJpql(sql, m);
//
//    }
//
//    private Stock getStockByStock(Stock stock) {
//        String sql;
//        Map m = new HashMap();
//        Stock st = getStockFacade().find(stock.getId());
//        return st;
//
//    }
//
//    public void onEdit(RowEditEvent event) {
//        BillItem tmp = (BillItem) event.getObject();
//        onEdit(tmp);
//    }
//
//    public void onEdit(BillItem tmp) {
//        if (tmp.getQty() == null) {
//            return;
//        }
//
//        //
//        double oldQty = getOldQty(tmp);
//        double newQty = tmp.getQty();
//
//        //
//        if (newQty <= 0) {
//            UtilityController.addErrorMessage("Can not enter a minus value");
//            return;
//        }
//
//        Stock currentStock = getStockByStock(tmp.getPharmaceuticalBillItem().getStock());
//
//        //System.err.println("old " + oldQty);
//        //System.err.println("new " + newQty);
//        double updationValue = newQty - oldQty;
//
//        //System.err.println("Updation Qty " + updationValue);
//        //System.err.println("Current Stock Qty " + currentStock);
//        if (updationValue > currentStock.getStock()) {
//            tmp.setQty(oldQty);
//            tmp.getPharmaceuticalBillItem().setQtyInUnit(0 - Math.abs(oldQty));
//            getBillItemFacade().edit(tmp);
//            UtilityController.addErrorMessage("No Sufficient Stocks Old Qty value is resetted");
//            return;
//        }
//
//        //   getPharmacyBean().updateStock(tmp.getPharmaceuticalBillItem().getStock(), updationValue);
////
//        if (oldQty == newQty) {
//            return;
//        } else if (oldQty > newQty) {
//            double max = oldQty - newQty;
//            //System.err.println("Max " + max);
//            getPharmacyBean().addToStock(tmp.getPharmaceuticalBillItem().getStock(), Math.abs(max), tmp.getPharmaceuticalBillItem(), getSessionController().getDepartment());
//        } else {
//            double min = newQty - oldQty;
//            //System.err.println("Min " + min);
//            getPharmacyBean().deductFromStock(tmp.getPharmaceuticalBillItem().getStock(), Math.abs(min), tmp.getPharmaceuticalBillItem(), getSessionController().getDepartment());
//        }
//
//        tmp.setGrossValue(tmp.getQty() * tmp.getRate());
//
//        getBillItemFacade().edit(tmp);
//
//        tmp.getPharmaceuticalBillItem().setQtyInUnit(0 - tmp.getQty());
//        getPharmaceuticalBillItemFacade().edit(tmp.getPharmaceuticalBillItem());
//
//        calculateBillItemForEditing(tmp);
//
//        calTotal();
//    }
//
//    public void editQty(BillItem bi) {
//        if (bi == null) {
//            //System.out.println("No Bill Item to Edit Qty");
//            return;
//        }
//        if (editingQty == null) {
//            //System.out.println("Editing qty is null");
//            return;
//        }
//
//        bi.setQty(editingQty);
//        bi.getPharmaceuticalBillItem().setQtyInUnit(0 - editingQty);
//        calculateBillItemForEditing(bi);
//
//        calTotal();
//        editingQty = null;
//    }
//
//    private Patient savePatient() {
//        switch (getPatientTabId()) {
//            case "tabNewPt":
//                if (!getNewPatient().getPerson().getName().trim().equals("")) {
//                    getNewPatient().setCreater(getSessionController().getLoggedUser());
//                    getNewPatient().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//                    getNewPatient().getPerson().setCreater(getSessionController().getLoggedUser());
//                    getNewPatient().getPerson().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//                    getPersonFacade().create(getNewPatient().getPerson());
//                    getPatientFacade().create(getNewPatient());
//                    return getNewPatient();
//                } else {
//                    return null;
//                }
//            case "tabSearchPt":
//                return getSearchedPatient();
//        }
//        return null;
//    }
//
//    public Title[] getTitle() {
//        return Title.values();
//    }
//
//    public Sex[] getSex() {
//        return Sex.values();
//    }
//
//    public List<Stock> getReplaceableStocks() {
//        return replaceableStocks;
//    }
//
//    public Double getQty() {
//        return qty;
//    }
//
//    public void setQty(Double qty) {
//        if (qty != null && qty <= 0) {
//            UtilityController.addErrorMessage("Can not enter a minus value");
//            return;
//        }
//        this.qty = qty;
//    }
//
//    public Stock getStock() {
//        return stock;
//    }
//
//    public void setStock(Stock stock) {
//        this.stock = stock;
//    }
//
//    public void setReplaceableStocks(List<Stock> replaceableStocks) {
//        this.replaceableStocks = replaceableStocks;
//    }
//
//    public Item getSelectedAlternative() {
//        return selectedAlternative;
//    }
//
//    public void setSelectedAlternative(Item selectedAlternative) {
//        this.selectedAlternative = selectedAlternative;
//    }
//
//    public void selectReplaceableStocks() {
//        if (selectedAlternative == null || !(selectedAlternative instanceof Amp)) {
//            replaceableStocks = new ArrayList<>();
//            return;
//        }
//        String sql;
//        Map m = new HashMap();
//        double d = 0.0;
//        Amp amp = (Amp) selectedAlternative;
//        m.put("d", getSessionController().getLoggedUser().getDepartment());
//        m.put("s", d);
//        m.put("vmp", amp.getVmp());
//        sql = "select i from Stock i join treat(i.itemBatch.item as Amp) amp where i.stock >:s and i.department=:d and amp.vmp=:vmp order by i.itemBatch.item.name";
//        replaceableStocks = getStockFacade().findBySQL(sql, m);
//    }
//
//    public List<Item> getItemsWithoutStocks() {
//        return itemsWithoutStocks;
//    }
//
//    public void setItemsWithoutStocks(List<Item> itemsWithoutStocks) {
//        this.itemsWithoutStocks = itemsWithoutStocks;
//    }
//
//    public void reAddToStock() {
//
//        String msg = getPharmacyBean().reAddToStock(getPreBill(), getSessionController().getLoggedUser(), getSessionController().getDepartment());
//        if (msg.trim() == "") {
//        } else {
//            UtilityController.addErrorMessage(msg);
//        }
//
//    }
//
//    public String newSaleBillWithoutReduceStock() {
//        //  reAddToStock();
//        clearBill();
//        clearBillItem();
//        billPreview = false;
//        return "pharmacy_retail_sale";
//    }
//
//    public String newSaleBillWithoutReduceStockForCashier() {
//        //  reAddToStock();
//        clearBill();
//        clearBillItem();
//        billPreview = false;
//        return "pharmacy_retail_sale_for_cashier";
//    }
//
//    public String newSaleBill() {
//        reAddToStock();
//        clearBill();
//        clearBillItem();
//        billPreview = false;
//        return "pharmacy_retail_sale";
//    }
//
//    public String newSaleBillForCashier() {
//        reAddToStock();
//        clearBill();
//        clearBillItem();
//        billPreview = false;
//        return "pharmacy_retail_sale_for_cashier";
//    }
//
//    public List<Item> completeRetailSaleItems(String qry) {
//        Map m = new HashMap<>();
//        List<Item> items;
//        String sql;
//        sql = "select i from Item i where i.retired=false and upper(i.name) like :n and type(i)=:t and i.id not in(select ibs.id from Stock ibs where ibs.stock >:s and ibs.department=:d and upper(ibs.itemBatch.item.name) like :n ) order by i.name ";
//        m
//                .put("t", Amp.class
//                );
//        m.put(
//                "d", getSessionController().getLoggedUser().getDepartment());
//        m.put(
//                "n", "%" + qry + "%");
//        double s = 0.0;
//
//        m.put(
//                "s", s);
//        items = getItemFacade().findBySQL(sql, m, 10);
//        return items;
//    }
//
//    public List<Stock> completeAvailableStocks(String qry) {
//        List<Stock> items;
//        String sql;
//        Map m = new HashMap();
//        m.put("d", getSessionController().getLoggedUser().getDepartment());
//        double d = 0.0;
//        m.put("s", d);
//        m.put("n", "%" + qry.toUpperCase() + "%");
//        if (qry.length() > 4) {
//            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n or upper(i.itemBatch.item.barcode) like :n )  order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
//        } else {
//            sql = "select i from Stock i where i.stock >:s and i.department=:d and (upper(i.itemBatch.item.name) like :n or upper(i.itemBatch.item.code) like :n)  order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
//        }
//        items = getStockFacade().findBySQL(sql, m, 20);
//        itemsWithoutStocks = completeRetailSaleItems(qry);
//        //System.out.println("selectedSaleitems = " + itemsWithoutStocks);
//        return items;
//    }
//
//    public BillItem getBillItem() {
//        if (billItem == null) {
//            billItem = new BillItem();
//        }
//        if (billItem.getPharmaceuticalBillItem() == null) {
//            PharmaceuticalBillItem pbi = new PharmaceuticalBillItem();
//            pbi.setBillItem(billItem);
//            billItem.setPharmaceuticalBillItem(pbi);
//        }
//        return billItem;
//    }
//
//    public void setBillItem(BillItem billItem) {
//        this.billItem = billItem;
//    }
//
//    private boolean errorCheckForPreBill() {
//        if (getPreBill().getBillItems().isEmpty()) {
//            UtilityController.addErrorMessage("No Items added to bill to sale");
//            return true;
//        }
//        return false;
//    }
//
////    private boolean checkPaymentScheme(PaymentScheme paymentScheme) {
////        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Cheque) {
////            if (getSaleBill().getBank() == null || getSaleBill().getChequeRefNo() == null || getSaleBill().getChequeDate() == null) {
////                UtilityController.addErrorMessage("Please select Cheque Number,Bank and Cheque Date");
////                return true;
////            }
////
////        }
////
////        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Slip) {
////            if (getSaleBill().getBank() == null || getSaleBill().getComments() == null || getSaleBill().getChequeDate() == null) {
////                UtilityController.addErrorMessage("Please Fill Memo,Bank and Slip Date ");
////                return true;
////            }
////
////        }
////
////        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Card) {
////            if (getSaleBill().getBank() == null || getSaleBill().getCreditCardRefNo() == null) {
////                UtilityController.addErrorMessage("Please Fill Credit Card Number and Bank");
////                return true;
////            }
////
//////            if (getCreditCardRefNo().trim().length() < 16) {
//////                UtilityController.addErrorMessage("Enter 16 Digit");
//////                return true;
//////            }
////        }
////
////        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Credit) {
////            if (getSaleBill().getCreditCompany() == null) {
////                UtilityController.addErrorMessage("Please Select Credit Company");
////                return true;
////            }
////
////        }
////
////        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Cash) {
////            if (getPreBill().getCashPaid() == 0.0) {
////                UtilityController.addErrorMessage("Please select tendered amount correctly");
////                return true;
////            }
////            if (getPreBill().getCashPaid() < getPreBill().getNetTotal()) {
////                UtilityController.addErrorMessage("Please select tendered amount correctly");
////                return true;
////            }
////        }
////
////        return false;
////
////    }
//    private boolean errorCheckForSaleBill() {
////        if (checkPaymentScheme(getSaleBill().getPaymentScheme())) {
////            return true;
////        }
//
//        if (getPreBill().getPaymentScheme() != null && getPreBill().getPaymentScheme().getPaymentMethod() != null && getPreBill().getPaymentScheme().getPaymentMethod() == PaymentMethod.Cheque) {
//            if (getChequeBank() == null || getChequeRefNo() == null || getChequeDate() == null) {
//                UtilityController.addErrorMessage("Please select Cheque Number,Bank and Cheque Date");
//                return true;
//            }
//
//        }
//        if (getPreBill().getPaymentScheme() != null && getPreBill().getPaymentScheme().getPaymentMethod() != null && getPreBill().getPaymentScheme().getPaymentMethod() == PaymentMethod.Card) {
//            if (getCreditBank() == null || getCreditCardRefNo() == null) {
//                UtilityController.addErrorMessage("Please Fill Credit Card Number and Bank");
//                return true;
//            }
//
//        }
//
//        if (getPreBill().getPaymentScheme().getPaymentMethod() == PaymentMethod.Cash) {
//            if (cashPaid == 0.0) {
//                UtilityController.addErrorMessage("Please select tendered amount correctly");
//                return true;
//            }
//            if (cashPaid < getNetTotal()) {
//                UtilityController.addErrorMessage("Please select tendered amount correctly");
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private void savePreBillFinally(Patient pt) {
//        getPreBill().setPatient(pt);
//
//        getPreBill().setDeptId(getBillNumberBean().institutionBillNumberGeneratorByPayment(getSessionController().getDepartment(), getPreBill(), BillType.PharmacyPre, BillNumberSuffix.SALE));
//
//        getPreBill().setToDepartment(null);
//        getPreBill().setToInstitution(null);
//        getPreBill().setBillDate(new Date());
//        getPreBill().setBillTime(new Date());
//        getPreBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
//        getPreBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
//        getPreBill().setPaymentMethod(paymentScheme.getPaymentMethod());
//        getPreBill().setPaymentScheme(paymentScheme);
//
//        savePaymentScheme(getPreBill());
//
//        getBillFacade().edit(getPreBill());
//    }
//
//    private void savePreBillInitially() {
//        calculateAllRates();
//        getPreBill().setInsId(getBillNumberBean().institutionBillNumberGeneratorByPayment(getSessionController().getInstitution(), getPreBill(), BillType.PharmacyPre, BillNumberSuffix.SALE));
//
//        getPreBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
//        getPreBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
//
//        getPreBill().setCreatedAt(Calendar.getInstance().getTime());
//        getPreBill().setCreater(getSessionController().getLoggedUser());
//
//        getBillFacade().create(getPreBill());
//
//    }
//
//    private void savePaymentScheme(Bill b) {
//        getPreBill().setCreditCompany(creditCompany);
//        if (b.getPaymentScheme().getPaymentMethod().equals(PaymentMethod.Cheque)) {
//            b.setBank(chequeBank);
//            b.setChequeRefNo(chequeRefNo);
//            b.setChequeDate(chequeDate);
//        }
//        if (b.getPaymentScheme().getPaymentMethod().equals(PaymentMethod.Slip)) {
//            b.setBank(slipBank);
//            b.setChequeDate(slipDate);
//            b.setComments(comment);
//        }
//        if (b.getPaymentScheme().getPaymentMethod().equals(PaymentMethod.Card)) {
//            b.setCreditCardRefNo(creditCardRefNo);
//            b.setBank(creditBank);
//        }
//
//        getBillFacade().edit(b);
//    }
//
//    private void saveSaleBill(Patient tmpPatient) {
//        calculateAllRates();
//
//        getSaleBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
//        getSaleBill().setInstitution(getSessionController().getLoggedUser().getInstitution());
//
//        getSaleBill().setToDepartment(null);
//        getSaleBill().setToInstitution(null);
//
//        getSaleBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
//        getSaleBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
//
//        getSaleBill().setGrantTotal(getPreBill().getGrantTotal());
//        getSaleBill().setDiscount(getPreBill().getDiscount());
//        getSaleBill().setNetTotal(getPreBill().getNetTotal());
//        getSaleBill().setTotal(getPreBill().getTotal());
////        getSaleBill().setRefBill(getPreBill());
//
//        getSaleBill().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//        getSaleBill().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//        getSaleBill().setPatient(tmpPatient);
//        getSaleBill().setPaymentScheme(getPreBill().getPaymentScheme());
//        getSaleBill().setReferenceBill(getPreBill());
//        getSaleBill().setCreatedAt(Calendar.getInstance().getTime());
//        getSaleBill().setCreater(getSessionController().getLoggedUser());
//        //       getSaleBill().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), null, BillType.PharmacySale));
////        getSaleBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(
////                getSessionController().getInstitution(), new BilledBill(), BillType.PharmacySale, BillNumberSuffix.PHSAL));
//        getSaleBill().setInsId(getPreBill().getInsId());
//        getSaleBill().setDeptId(getPreBill().getDeptId());
//        getBillFacade().create(getSaleBill());
//
//        savePaymentScheme(getSaleBill());
//        updatePreBill();
//    }
////
//
//    private void updatePreBill() {
//        getPreBill().setReferenceBill(getSaleBill());
//        getBillFacade().edit(getPreBill());
//    }
//    
//     private void savePreBillItemsFinally() {
//        for (BillItem tbi : getPreBill().getTransActiveBillItem()) {
//            getBillItemFacade().edit(tbi);
//            getPharmaceuticalBillItemFacade().edit(tbi.getPharmaceuticalBillItem());
//            
//            onEdit(tbi);
//          
//        }
//        getBillFacade().edit(getSaleBill());
//    }
//
//    private void saveSaleBillItems() {
//        for (BillItem tbi : getPreBill().getTransActiveBillItem()) {
//            onEdit(tbi);
//            
//            
//            BillItem newBil = new BillItem();
//
//            newBil.copy(tbi);
//            newBil.setReferanceBillItem(tbi);
//            newBil.setBill(getSaleBill());
//            newBil.setInwardChargeType(InwardChargeType.Medicine);
//            //      newBil.setBill(getSaleBill());
//            newBil.setCreatedAt(Calendar.getInstance().getTime());
//            newBil.setCreater(getSessionController().getLoggedUser());
//            getBillItemFacade().create(newBil);
//
//            PharmaceuticalBillItem newPhar = new PharmaceuticalBillItem();
//            newPhar.copy(tbi.getPharmaceuticalBillItem());
//            newPhar.setBillItem(newBil);
//            getPharmaceuticalBillItemFacade().create(newPhar);
//
//            newBil.setPharmaceuticalBillItem(newPhar);
//            getBillItemFacade().edit(newBil);
//
//            //   getPharmacyBean().deductFromStock(tbi.getItem(), tbi.getQty(), tbi.getBill().getDepartment());
//            getSaleBill().getBillItems().add(newBil);
//
//            tbi.setReferanceBillItem(newBil);
//            getBillItemFacade().edit(tbi);
//        }
//        getBillFacade().edit(getSaleBill());
//    }
//
//    private void savePreBillItems(BillItem tbi) {
//        tbi.setInwardChargeType(InwardChargeType.Medicine);
//        tbi.setBill(getPreBill());
//
//        //tbi.setDblValue(tbi.getQty());
//        tbi.setCreatedAt(Calendar.getInstance().getTime());
//        tbi.setCreater(getSessionController().getLoggedUser());
//
//        PharmaceuticalBillItem tmpPharmacyItem = tbi.getPharmaceuticalBillItem();
//
//        tbi.setPharmaceuticalBillItem(null);
//        getBillItemFacade().create(tbi);
//
//        getPharmaceuticalBillItemFacade().create(tmpPharmacyItem);
//        tbi.setPharmaceuticalBillItem(tmpPharmacyItem);
//
//        tbi.setSearialNo(getPreBill().getBillItems().size() + 1);
//
//        getBillItemFacade().edit(tbi);
//
//        getPharmacyBean().deductFromStock(tbi.getPharmaceuticalBillItem().getStock(), Math.abs(tbi.getPharmaceuticalBillItem().getQty()), tbi.getPharmaceuticalBillItem(), getSessionController().getDepartment());
//
//        getPreBill().getBillItems().add(tbi);
//
//        getBillFacade().edit(getPreBill());
//    }
//
//    public void settlePreBill() {
//        editingQty = null;
//        if (errorCheckForPreBill()) {
//            return;
//        }
//        Patient pt = savePatient();
//        savePreBillFinally(pt);
//        savePreBillItemsFinally();
//
//        setPrintBill(getBillFacade().find(getPreBill().getId()));
//
//        clearBill();
//        clearBillItem();
//
//        billPreview = true;
//    }
//
//    public void settleBillWithPay() {
//        editingQty = null;
//        if (errorCheckForSaleBill()) {
//            return;
//        }
//        Patient pt = savePatient();
//        getPreBill().setPaidAmount(getPreBill().getTotal());
//        savePreBillFinally(pt);
//        savePreBillItemsFinally();
//
//        saveSaleBill(pt);
//        saveSaleBillItems();
//
//        setPrintBill(getBillFacade().find(getSaleBill().getId()));
//
//        clearBill();
//        clearBillItem();
//        billPreview = true;
//
//    }
//
//    public String newPharmacyRetailSale() {
//        clearBill();
//        clearBillItem();
//        billPreview = false;
//        return "pharmacy_retail_sale";
//    }
//
//    private boolean checkItemBatch() {
//        for (BillItem bItem : getPreBill().getTransActiveBillItem()) {
//            if (bItem.getPharmaceuticalBillItem().getStock().getId() == getBillItem().getPharmaceuticalBillItem().getStock().getId()) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    public void addBillItem() {
//        editingQty = null;
//
//        if (billItem == null) {
//            return;
//        }
//        if (billItem.getPharmaceuticalBillItem() == null) {
//            return;
//        }
//        if (getStock() == null) {
//            UtilityController.addErrorMessage("Item?");
//            return;
//        }
//        if (getQty() == null) {
//            UtilityController.addErrorMessage("Quentity?");
//            return;
//        }
//        if (getQty() > getStock().getStock()) {
//            UtilityController.addErrorMessage("No Sufficient Stocks?");
//            return;
//        }
//
//        if (checkItemBatch()) {
//            UtilityController.addErrorMessage("Already added this item batch");
//            return;
//        }
//
//        billItem.getPharmaceuticalBillItem().setQtyInUnit(0 - qty);
//        billItem.getPharmaceuticalBillItem().setStock(stock);
//        billItem.getPharmaceuticalBillItem().setItemBatch(getStock().getItemBatch());
//        calculateBillItem();
//
//        billItem.setInwardChargeType(InwardChargeType.Medicine);
//
//        billItem.setItem(getStock().getItemBatch().getItem());
//        billItem.setBill(getPreBill());
//
//        if (getPreBill().getId() == null) {
//            savePreBillInitially();
//        }
//
//        savePreBillItems(billItem);
//
//        calculateAllRates();
//
//        calTotal();
//
//        clearBillItem();
//        setActiveIndex(1);
//    }
//
//    private void calTotal() {
//        getPreBill().setTotal(0);
//        double netTot = 0.0;
//        double discount = 0.0;
//        double grossTot = 0.0;
//        for (BillItem b : getPreBill().getTransActiveBillItem()) {
//            if (b.isRetired()) {
//                continue;
//            }
//            netTot = netTot + b.getNetValue();
//            grossTot = grossTot + b.getGrossValue();
//            discount = discount + b.getDiscount();
//            getPreBill().setTotal(getPreBill().getTotal() + b.getNetValue());
//        }
//        getPreBill().setNetTotal(netTot);
//        getPreBill().setTotal(grossTot);
//        getPreBill().setGrantTotal(grossTot);
//        getPreBill().setDiscount(discount);
//        setNetTotal(getPreBill().getNetTotal());
//
//        getBillFacade().edit(getPreBill());
//    }
//
//    @EJB
//    private StockHistoryFacade stockHistoryFacade;
//
//    public void removeBillItem(BillItem b) {
//        if (b.isRetired()) {
//            UtilityController.addErrorMessage("This Item Already removed");
//            return;
//        }
//
//        getPharmacyBean().addToStock(b.getPharmaceuticalBillItem().getStock(), Math.abs(b.getQty()), b.getPharmaceuticalBillItem(), getSessionController().getDepartment());
//
//        b.setRetired(true);
//        b.setRetiredAt(new Date());
//        b.setRetireComments("Remove From Bill ");
//        b.setRetirer(getSessionController().getLoggedUser());
//        getBillItemFacade().edit(b);
//
//        b.getPharmaceuticalBillItem().setQtyInUnit(0);
//        getPharmaceuticalBillItemFacade().edit(b.getPharmaceuticalBillItem());
//
//        b.getPharmaceuticalBillItem().getStockHistory().setRetired(true);
//        b.getPharmaceuticalBillItem().getStockHistory().setRetiredAt(new Date());
//        b.getPharmaceuticalBillItem().getStockHistory().setRetireComments("Remove From Bill");
//        getStockHistoryFacade().edit(b.getPharmaceuticalBillItem().getStockHistory());
//
//        calTotal();
//    }
//
//    public void calculateBillItemListner(AjaxBehaviorEvent event) {
//        calculateBillItem();
//    }
//
//    public void calculateBillItem() {
//        if (stock == null) {
//            return;
//        }
//        if (getPreBill() == null) {
//            return;
//        }
//        if (billItem == null) {
//            return;
//        }
//        if (billItem.getPharmaceuticalBillItem() == null) {
//            return;
//        }
//        if (billItem.getPharmaceuticalBillItem().getStock() == null) {
//            getBillItem().getPharmaceuticalBillItem().setStock(stock);
//        }
//        if (getQty() == null) {
//            qty = 0.0;
//        }
//
//        //Bill Item
////        billItem.setInwardChargeType(InwardChargeType.Medicine);
//        billItem.setItem(getStock().getItemBatch().getItem());
//        billItem.setQty(qty);
//
//        //pharmaceutical Bill Item
//        billItem.getPharmaceuticalBillItem().setDoe(getStock().getItemBatch().getDateOfExpire());
//        billItem.getPharmaceuticalBillItem().setFreeQty(0.0);
//        billItem.getPharmaceuticalBillItem().setItemBatch(getStock().getItemBatch());
//        billItem.getPharmaceuticalBillItem().setQtyInUnit(0 - qty);
//
//        //Rates
//        //Values
//        billItem.setGrossValue(getStock().getItemBatch().getRetailsaleRate() * qty);
//        billItem.setNetValue(qty * billItem.getNetRate());
//        billItem.setDiscount(billItem.getGrossValue() - billItem.getNetValue());
//
//    }
//
//    public void calculateBillItemForEditing(BillItem bi) {
//        //System.out.println("calculateBillItemForEditing");
//        //System.out.println("bi = " + bi);
//        if (getPreBill() == null || bi == null || bi.getPharmaceuticalBillItem() == null || bi.getPharmaceuticalBillItem().getStock() == null) {
//            //System.out.println("calculateItemForEditingFailedBecause of null");
//            return;
//        }
//        //System.out.println("bi.getQty() = " + bi.getQty());
//        //System.out.println("bi.getRate() = " + bi.getRate());
//        bi.setGrossValue(bi.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate() * bi.getQty());
//        bi.setNetValue(bi.getQty() * bi.getNetRate());
//        bi.setDiscount(bi.getGrossValue() - bi.getNetValue());
//        //System.out.println("bi.getNetValue() = " + bi.getNetValue());
//
//    }
//
//    public void handleSelect(SelectEvent event) {
//        getBillItem().getPharmaceuticalBillItem().setStock(stock);
//        calculateRates(billItem);
//    }
//
//    public void paymentSchemeChanged(AjaxBehaviorEvent ajaxBehavior) {
//        calculateAllRates();
//    }
//
//    public void calculateAllRates() {
//        //System.out.println("calculating all rates");
//        for (BillItem tbi : getPreBill().getTransActiveBillItem()) {
//            calculateRates(tbi);
//            calculateBillItemForEditing(tbi);
//        }
//        calTotal();
//    }
//
//    public void calculateRateListner(AjaxBehaviorEvent event) {
//
//    }
//
//    public void calculateRates(BillItem bi) {
//        //System.out.println("calculating rates");
//        if (bi.getPharmaceuticalBillItem().getStock() == null) {
//            //System.out.println("stock is null");
//            return;
//        }
//        getBillItem();
//        bi.setRate(bi.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate());
//        //   //System.err.println("Rate "+bi.getRate());
//        bi.setDiscount(calculateBillItemDiscountRate(bi));
//        //  //System.err.println("Discount "+bi.getDiscount());
//        bi.setNetRate(bi.getRate() - bi.getDiscount());
//        //  //System.err.println("Net "+bi.getNetRate());
//    }
//
//    public double calculateBillItemDiscountRate(BillItem bi) {
//        //System.out.println("bill item discount rate");
//        //System.out.println("getPaymentScheme() = " + getPaymentScheme());
//        if (bi == null) {
//            //System.out.println("bi is null");
//            return 0.0;
//        }
//        if (bi.getPharmaceuticalBillItem() == null) {
//            //System.out.println("pi is null");
//            return 0.0;
//        }
//        if (bi.getPharmaceuticalBillItem().getStock() == null) {
//            //System.out.println("stock is null");
//            return 0.0;
//        }
//        if (bi.getPharmaceuticalBillItem().getStock().getItemBatch() == null) {
//            //System.out.println("batch is null");
//            return 0.0;
//        }
//        bi.setItem(bi.getPharmaceuticalBillItem().getStock().getItemBatch().getItem());
//        double tr = bi.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate();
//        //  //System.err.println("tr = " + tr);
//        double tdp = getPaymentScheme().getDiscountPercentForPharmacy();
//        //    //System.err.println("tdp = " + tdp);
//        double dr;
//        dr = (tr * tdp) / 100;
//        //     //System.err.println("dr = " + dr);
//
//        if (bi.getItem().isDiscountAllowed()) {
//            return dr;
//        } else {
//            return 0;
//        }
//    }
//
//    private void clearBill() {
//        preBill = null;
//        saleBill = null;
//        newPatient = null;
//        searchedPatient = null;
////        billItems = null;
//        patientTabId = "tabNewPt";
//        cashPaid = 0;
//        netTotal = 0;
//        balance = 0;
//        paymentScheme = null;
//    }
//
//    private void clearBillItem() {
//        billItem = null;
////        removingBillItem = null;
//        editingBillItem = null;
//        qty = null;
//        stock = null;
//        editingQty = null;
//    }
//
//    public SessionController getSessionController() {
//        return sessionController;
//    }
//
//    public void setSessionController(SessionController sessionController) {
//        this.sessionController = sessionController;
//    }
//
//    public BillFacade getBillFacade() {
//        return billFacade;
//    }
//
//    public void setBillFacade(BillFacade billFacade) {
//        this.billFacade = billFacade;
//    }
//
//    public BillItemFacade getBillItemFacade() {
//        return billItemFacade;
//    }
//
//    public void setBillItemFacade(BillItemFacade billItemFacade) {
//        this.billItemFacade = billItemFacade;
//    }
//
//    public ItemFacade getItemFacade() {
//        return itemFacade;
//    }
//
//    public void setItemFacade(ItemFacade itemFacade) {
//        this.itemFacade = itemFacade;
//    }
//
//    public BillItem getEditingBillItem() {
//        return editingBillItem;
//    }
//
//    public void setEditingBillItem(BillItem editingBillItem) {
//        this.editingBillItem = editingBillItem;
//    }
//
//    public StockFacade getStockFacade() {
//        return stockFacade;
//    }
//
//    public void setStockFacade(StockFacade stockFacade) {
//        this.stockFacade = stockFacade;
//    }
//
//    public PharmacyBean getPharmacyBean() {
//        return pharmacyBean;
//    }
//
//    public void setPharmacyBean(PharmacyBean pharmacyBean) {
//        this.pharmacyBean = pharmacyBean;
//    }
//
//    public Patient getNewPatient() {
//        if (newPatient == null) {
//            newPatient = new Patient();
//            Person p = new Person();
//
//            newPatient.setPerson(p);
//        }
//        return newPatient;
//    }
//
//    public void setNewPatient(Patient newPatient) {
//        this.newPatient = newPatient;
//    }
//
//    public Patient getSearchedPatient() {
//        return searchedPatient;
//    }
//
//    public void setSearchedPatient(Patient searchedPatient) {
//        this.searchedPatient = searchedPatient;
//    }
//
//    public YearMonthDay getYearMonthDay() {
//        if (yearMonthDay == null) {
//            yearMonthDay = new YearMonthDay();
//        }
//        return yearMonthDay;
//    }
//
//    public void setYearMonthDay(YearMonthDay yearMonthDay) {
//        this.yearMonthDay = yearMonthDay;
//    }
//
//    public PreBill getPreBill() {
//        if (preBill == null) {
//            preBill = new PreBill();
//            preBill.setBillType(BillType.PharmacyPre);
//            preBill.setPaymentScheme(getPaymentSchemeController().getItems().get(0));
//        }
//        return preBill;
//    }
//
//    public void setPreBill(PreBill preBill) {
//        this.preBill = preBill;
//    }
//
//    public Bill getSaleBill() {
//        if (saleBill == null) {
//            saleBill = new BilledBill();
//            saleBill.setBillType(BillType.PharmacySale);
//        }
//        return saleBill;
//    }
//
//    public void setSaleBill(Bill saleBill) {
//        this.saleBill = saleBill;
//    }
//
//    public String getPatientTabId() {
//        return patientTabId;
//    }
//
//    public void setPatientTabId(String patientTabId) {
//        this.patientTabId = patientTabId;
//    }
//
//    public PersonFacade getPersonFacade() {
//        return personFacade;
//    }
//
//    public void setPersonFacade(PersonFacade personFacade) {
//        this.personFacade = personFacade;
//    }
//
//    public PatientFacade getPatientFacade() {
//        return patientFacade;
//    }
//
//    public void setPatientFacade(PatientFacade patientFacade) {
//        this.patientFacade = patientFacade;
//    }
//
//    public String getStrTenderedValue() {
//        return strTenderedValue;
//    }
//
//    public void setStrTenderedValue(String strTenderedValue) {
//        this.strTenderedValue = strTenderedValue;
//    }
//
//    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
//        return pharmaceuticalBillItemFacade;
//    }
//
//    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
//        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
//    }
//
//    public String getCreditCardRefNo() {
//        return creditCardRefNo;
//    }
//
//    public void setCreditCardRefNo(String creditCardRefNo) {
//        this.creditCardRefNo = creditCardRefNo;
//    }
//
//    public Institution getCreditBank() {
//        return creditBank;
//    }
//
//    public void setCreditBank(Institution creditBank) {
//        this.creditBank = creditBank;
//    }
//
//    public String getChequeRefNo() {
//        return chequeRefNo;
//    }
//
//    public void setChequeRefNo(String chequeRefNo) {
//        this.chequeRefNo = chequeRefNo;
//    }
//
//    public Institution getChequeBank() {
//        return chequeBank;
//    }
//
//    public void setChequeBank(Institution chequeBank) {
//        this.chequeBank = chequeBank;
//    }
//
//    public Date getChequeDate() {
//        return chequeDate;
//    }
//
//    public void setChequeDate(Date chequeDate) {
//        this.chequeDate = chequeDate;
//    }
//
//    public String getComment() {
//        return comment;
//    }
//
//    public void setComment(String comment) {
//        this.comment = comment;
//    }
//
//    public Institution getSlipBank() {
//        return slipBank;
//    }
//
//    public void setSlipBank(Institution slipBank) {
//        this.slipBank = slipBank;
//    }
//
//    public Date getSlipDate() {
//        return slipDate;
//    }
//
//    public void setSlipDate(Date slipDate) {
//        this.slipDate = slipDate;
//    }
//
//    public Institution getCreditCompany() {
//        return creditCompany;
//    }
//
//    public void setCreditCompany(Institution creditCompany) {
//        this.creditCompany = creditCompany;
//    }
//
//    public double getCashPaid() {
//        return cashPaid;
//    }
//
//    public void setCashPaid(double cashPaid) {
//        balance = cashPaid - netTotal;
//        this.cashPaid = cashPaid;
//    }
//
//    public double getNetTotal() {
//        return netTotal;
//    }
//
//    public void setNetTotal(double netTotal) {
//        balance = cashPaid - netTotal;
//        this.netTotal = netTotal;
//    }
//
//    public BillNumberBean getBillNumberBean() {
//        return billNumberBean;
//    }
//
//    public void setBillNumberBean(BillNumberBean billNumberBean) {
//        this.billNumberBean = billNumberBean;
//    }
//
//    public double getBalance() {
//        return balance;
//    }
//
//    public void setBalance(double balance) {
//        this.balance = balance;
//    }
//
//    public boolean isBillPreview() {
//        return billPreview;
//    }
//
//    public void setBillPreview(boolean billPreview) {
//        this.billPreview = billPreview;
//    }
//
//    public Bill getBill() {
//        return bill;
//    }
//
//    public void setBill(Bill bill) {
//        this.bill = bill;
//    }
//
//    public int getActiveIndex() {
//        return activeIndex;
//    }
//
//    public void setActiveIndex(int activeIndex) {
//        this.activeIndex = activeIndex;
//    }
//
//    public Bill getPrintBill() {
//        return printBill;
//    }
//
//    public void setPrintBill(Bill printBill) {
//        this.printBill = printBill;
//    }
//
//    public PaymentSchemeController getPaymentSchemeController() {
//        return PaymentSchemeController;
//    }
//
//    public void setPaymentSchemeController(PaymentSchemeController PaymentSchemeController) {
//        this.PaymentSchemeController = PaymentSchemeController;
//    }
//
//    public PaymentScheme getPaymentScheme() {
//        //  //System.err.println("GEtting Paymen");
//        return paymentScheme;
//    }
//
//    public void setPaymentScheme(PaymentScheme paymentScheme) {
//        //     //System.err.println("Setting Pay");
//        this.paymentScheme = paymentScheme;
//    }
//
//    public StockHistoryFacade getStockHistoryFacade() {
//        return stockHistoryFacade;
//    }
//
//    public void setStockHistoryFacade(StockHistoryFacade stockHistoryFacade) {
//        this.stockHistoryFacade = stockHistoryFacade;
//    }
//
//}