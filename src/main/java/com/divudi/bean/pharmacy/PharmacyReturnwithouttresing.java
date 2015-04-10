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
import com.divudi.data.dataStructure.PaymentMethodData;
import com.divudi.data.dataStructure.YearMonthDay;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.Patient;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.PreBill;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.entity.pharmacy.UserStockContainer;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import com.divudi.facade.StockFacade;
import com.divudi.facade.VmpFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.RowEditEvent;


@Named
@SessionScoped




public class PharmacyReturnwithouttresing implements Serializable{
    
    
    
    String errorMassage = null ;
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
    Institution toInstitution;
    //BillItem removingBillItem;
    BillItem editingBillItem;
    Double qty;
    Stock stock;
    Stock replacableStock;

    PaymentScheme paymentScheme;

    int activeIndex;

    private Patient newPatient;
    private Patient searchedPatient;
    private YearMonthDay yearMonthDay;
  
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

      public void settleBill() {

        editingQty = null;
        //   System.out.println("editingQty = " + editingQty);
        errorMessage = null;
        //   System.out.println("errorMessage = " + errorMessage);
        if (checkAllBillItem()) {
            //   System.out.println("Check all bill Ietems");
            return;
        }

       

        getPreBill().setPaidAmount(getPreBill().getTotal());
        //   System.out.println("getPreBill().getPaidAmount() = " + getPreBill().getPaidAmount());
        List<BillItem> tmpBillItems = getPreBill().getBillItems();
        getPreBill().setBillItems(null);

        savePreBillFinally();
        savePreBillItemsFinally(tmpBillItems);

        setPrintBill(getBillFacade().find(getPreBill().getId()));

        clearBill();
        clearBillItem();

        billPreview = true;

    }

    private boolean checkItemBatch() {
        for (BillItem bItem : getPreBill().getBillItems()) {
            if (Objects.equals(bItem.getPharmaceuticalBillItem().getStock().getId(), getBillItem().getPharmaceuticalBillItem().getStock().getId())) {
                return true;
            }
        }

        return false;
    }

    @EJB
    

    public void addBillItem() {
        errorMessage = null;

        editingQty = null;

        if (billItem == null) {
            return;
        }
        if (billItem.getPharmaceuticalBillItem() == null) {
            return;
        }

        if (getToInstitution() == null) {
            UtilityController.addErrorMessage("Please Select To Institution");
            return;
        }

    
    }
    public Institution getToInstitution() {
        return toInstitution;
    }

    public void setToInstitution(Institution toInstitution) {
        this.toInstitution = toInstitution;
    }

    public String getErrorMassage() {
        return errorMassage;
    }

    public void setErrorMassage(String errorMassage) {
        this.errorMassage = errorMassage;
    }

    public PaymentSchemeController getPaymentSchemeController() {
        return PaymentSchemeController;
    }

    public void setPaymentSchemeController(PaymentSchemeController PaymentSchemeController) {
        this.PaymentSchemeController = PaymentSchemeController;
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

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public VmpFacade getVmpFacade() {
        return vmpFacade;
    }

    public void setVmpFacade(VmpFacade vmpFacade) {
        this.vmpFacade = vmpFacade;
    }

    public Item getSelectedAvailableAmp() {
        return selectedAvailableAmp;
    }

    public void setSelectedAvailableAmp(Item selectedAvailableAmp) {
        this.selectedAvailableAmp = selectedAvailableAmp;
    }

    public PreBill getPreBill() {
        if (preBill == null){
            preBill = new PreBill();
        }
        return preBill;
    }

    public void setPreBill(PreBill preBill) {
        this.preBill = preBill;
    }

    public Bill getSaleBill() {
        return saleBill;
    }

    public void setSaleBill(Bill saleBill) {
        this.saleBill = saleBill;
    }

    public Bill getPrintBill() {
        return printBill;
    }

    public void setPrintBill(Bill printBill) {
        this.printBill = printBill;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public BillItem getBillItem() {
        return billItem;
    }

    public void setBillItem(BillItem billItem) {
        this.billItem = billItem;
    }

    public BillItem getEditingBillItem() {
        return editingBillItem;
    }

    public void setEditingBillItem(BillItem editingBillItem) {
        this.editingBillItem = editingBillItem;
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

    public Stock getReplacableStock() {
        return replacableStock;
    }

    public void setReplacableStock(Stock replacableStock) {
        this.replacableStock = replacableStock;
    }

    public PaymentScheme getPaymentScheme() {
        return paymentScheme;
    }

    public void setPaymentScheme(PaymentScheme paymentScheme) {
        this.paymentScheme = paymentScheme;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public Patient getNewPatient() {
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
        return yearMonthDay;
    }

    public void setYearMonthDay(YearMonthDay yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }

    public boolean isBillPreview() {
        return billPreview;
    }

    public void setBillPreview(boolean billPreview) {
        this.billPreview = billPreview;
    }

    public Vmp getSelectedGeneric() {
        return selectedGeneric;
    }

    public void setSelectedGeneric(Vmp selectedGeneric) {
        this.selectedGeneric = selectedGeneric;
    }

    public List<Stock> getReplaceableStocks() {
        return replaceableStocks;
    }

    public void setReplaceableStocks(List<Stock> replaceableStocks) {
        this.replaceableStocks = replaceableStocks;
    }

    public List<Item> getItemsWithoutStocks() {
        return itemsWithoutStocks;
    }

    public void setItemsWithoutStocks(List<Item> itemsWithoutStocks) {
        this.itemsWithoutStocks = itemsWithoutStocks;
    }

    public List<Stock> getStocksWithGeneric() {
        return stocksWithGeneric;
    }

    public void setStocksWithGeneric(List<Stock> stocksWithGeneric) {
        this.stocksWithGeneric = stocksWithGeneric;
    }

    public List<Vmp> getSelectedGenerics() {
        return selectedGenerics;
    }

    public void setSelectedGenerics(List<Vmp> selectedGenerics) {
        this.selectedGenerics = selectedGenerics;
    }

    public double getCashPaid() {
        return cashPaid;
    }

    public void setCashPaid(double cashPaid) {
        this.cashPaid = cashPaid;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Double getEditingQty() {
        return editingQty;
    }

    public void setEditingQty(Double editingQty) {
        this.editingQty = editingQty;
    }

    public String getCashPaidStr() {
        return cashPaidStr;
    }

    public void setCashPaidStr(String cashPaidStr) {
        this.cashPaidStr = cashPaidStr;
    }

    public UserStockContainer getUserStockContainer() {
        return userStockContainer;
    }

    public void setUserStockContainer(UserStockContainer userStockContainer) {
        this.userStockContainer = userStockContainer;
    }

    public PaymentMethodData getPaymentMethodData() {
        return paymentMethodData;
    }

    public void setPaymentMethodData(PaymentMethodData paymentMethodData) {
        this.paymentMethodData = paymentMethodData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

      private boolean checkAllBillItem() {
        for (BillItem b : getPreBill().getBillItems()) {

            
        }

        return false;

    }

   

     private boolean errorCheckForSaleBill() {
        if (toInstitution == null) {
            errorMessage = "Please select a department to issue items. Bill can NOT be settled until you select the department";
            JsfUtil.addErrorMessage("Intitution");
            return true;
        }
        return false;
    }

     private void savePreBillFinally() {
        getPreBill().setInsId(getBillNumberBean().institutionBillNumberGeneratorByPayment(getSessionController().getInstitution(), getPreBill(), BillType.PharmacyIssue, BillNumberSuffix.DI));

        getPreBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getPreBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getPreBill().setCreatedAt(Calendar.getInstance().getTime());
        getPreBill().setCreater(getSessionController().getLoggedUser());

        getPreBill().setToInstitution(toInstitution);

        getPreBill().setDeptId(getBillNumberBean().institutionBillNumberGeneratorByPayment(getSessionController().getDepartment(), getPreBill(), BillType.PharmacyReturnWithoutTraising, BillNumberSuffix.PHDIRRET));

        getPreBill().setBillDate(new Date());
        getPreBill().setBillTime(new Date());
        getPreBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        getPreBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        if (getPreBill().getId() == null) {
            getBillFacade().create(getPreBill());
        }

    }


    private void clearBill() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void clearBillItem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void savePreBillItemsFinally(List<BillItem> tmpBillItems) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   public void onEdit(RowEditEvent event) {
        BillItem tmp = (BillItem) event.getObject();
        onEdit(tmp);
    }
    
    
   public void calculateAllRates() {
        //System.out.println("calculating all rates");
        for (BillItem tbi : getPreBill().getBillItems()) {
            calculateRates(tbi);
            calculateBillItemForEditing(tbi);
        }
        calTotal();
    }

    public void calculateRateListner(AjaxBehaviorEvent event) {

    }

    public void calculateRates(BillItem bi) {
        //   System.out.println("calculating rates");
        if (bi.getPharmaceuticalBillItem().getStock() == null) {
            System.out.println("stock is unavailable");
            return;
        }

}

    public void calculateBillItemForEditing(BillItem bi) {
        //System.out.println("calculateBillItemForEditing");
        //System.out.println("bi = " + bi);
        if (getPreBill() == null || bi == null || bi.getPharmaceuticalBillItem() == null || bi.getPharmaceuticalBillItem().getStock() == null) {
            //System.out.println("calculateItemForEditingFailedBecause of null");
            return;
        }

        bi.setGrossValue(bi.getQty() * bi.getRate());
        //bi.setMarginValue(bi.getQty() * bi.getMarginRate());
        bi.setNetValue(bi.getQty() * bi.getNetRate());

    }

       public void calTotal() {
        getPreBill().setTotal(0);
        double netTot = 0.0;
        double discount = 0.0;
        double grossTot = 0.0;
        //double margin = 0;
        int index = 0;
        for (BillItem b : getPreBill().getBillItems()) {
            if (b.isRetired()) {
                continue;
            }
            b.setSearialNo(index++);

            netTot = netTot + b.getNetValue();
            grossTot = grossTot + b.getGrossValue();
            discount = discount + b.getDiscount();
            //margin += b.getMarginValue();

        }

        netTot = netTot + getPreBill().getBalance();

        getPreBill().setNetTotal(netTot);
        getPreBill().setTotal(grossTot);
        //getPreBill().setMargin(margin);
        getPreBill().setDiscount(discount);
        setNetTotal(getPreBill().getNetTotal());

    }

    private void onEdit(BillItem tmp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}