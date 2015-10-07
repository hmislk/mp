/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.BillController;
import com.divudi.bean.PaymentSchemeController;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.dataStructure.PaymentMethodData;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CashTransactionBean;
import com.divudi.ejb.CreditBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Institution;
import com.divudi.entity.WebUser;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Inject;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.util.JsfUtil;
import java.util.ArrayList;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class PharmacyDealorBill implements Serializable {

    //Atribtes
    private boolean printPreview;
    private Bill current;
    private PaymentMethodData paymentMethodData;
    private BillItem currentBillItem;
    private Institution institution;
    //List
    private List<BillItem> billItems;
    private List<BillItem> selectedBillItems;
    //EJB
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    //Inject
    @Inject
    private SessionController sessionController;
    double payingAmount;
    List<BillItem> payingBillItems;

    public void makeNull() {
        printPreview = false;
        current = null;
        currentBillItem = null;
        institution = null;
        paymentMethodData = null;
        selectedBillItems = null;
        billItems = null;
    }

    private boolean errorCheckForAdding() {
        if (getCurrentBillItem().getReferenceBill().getFromInstitution() == null) {
            UtilityController.addErrorMessage("U cant add without credit company name");
            return true;
        }

        if (!isPaidAmountOk(getCurrentBillItem())) {
            UtilityController.addSuccessMessage("U cant add more than ballance");
            return true;
        }

        for (BillItem b : getBillItems()) {
            if (b.getReferenceBill() != null && b.getReferenceBill().getFromInstitution() != null) {
                if (getCurrentBillItem().getReferenceBill().getFromInstitution().getId() != b.getReferenceBill().getFromInstitution().getId()) {
                    UtilityController.addErrorMessage("U can add only one type Credit companies at Once");
                    return true;
                }
            }
        }
        
        for (BillItem b : getBillItems()) {
            //System.out.println("getCurrentBillItem().getReferenceBill().getInsId() = " + getCurrentBillItem().getReferenceBill().getInsId());
            //System.out.println("b.getReferenceBill().getInsId() = " + b.getReferenceBill().getInsId());
            if (b.getReferenceBill().equals(getCurrentBillItem().getReferenceBill())) {
                    UtilityController.addErrorMessage("This GRN is Alredy Added");
                    return true;
                
            }
        }

        return false;
    }

    @EJB
    CreditBean creditBean;

    private double getReferenceBallance(BillItem billItem) {
        double refBallance = 0;
        double neTotal = Math.abs(billItem.getReferenceBill().getNetTotal());
        double returned = Math.abs(billItem.getReferenceBill().getTmpReturnTotal());
        double paidAmt = Math.abs(getCreditBean().getPaidAmount(billItem.getReferenceBill(), BillType.GrnPayment));

        refBallance = neTotal - (paidAmt + returned);

        return refBallance;
    }

    public void selectListener() {

        double ballanceAmt = getReferenceBallance(getCurrentBillItem());

        //System.err.println("Ballance Amount " + ballanceAmt);
        if (ballanceAmt > 0.1) {
            getCurrentBillItem().setNetValue(ballanceAmt);
        }

    }

    @Inject
    private BillController billController;

    public void selectInstitutionListener() {
        Institution ins = institution;
        makeNull();

        List<Bill> list = getBillController().getDealorBills(ins);
        for (Bill b : list) {
            getCurrentBillItem().setReferenceBill(b);
            double returned = Math.abs(getCreditBean().getGrnReturnValue(getCurrentBillItem().getReferenceBill()));
            getCurrentBillItem().getReferenceBill().setTmpReturnTotal(returned);

            selectListener();
            addToBill();
        }
    }

    public void addToBill() {

        if (errorCheckForAdding()) {
            return;
        }

        getCurrent().setToInstitution(getCurrentBillItem().getReferenceBill().getFromInstitution());
        //     getCurrentBillItem().getBill().setNetTotal(getCurrentBillItem().getNetValue());
        //     getCurrentBillItem().getBill().setTotal(getCurrent().getNetTotal());

        if (getCurrentBillItem().getNetValue() != 0) {
            //  //System.err.println("11 " + getCurrentBillItem().getReferenceBill().getDeptId());
            //   //System.err.println("aa " + getCurrentBillItem().getNetValue());
            getCurrentBillItem().setSearialNo(getBillItems().size());
            getBillItems().add(getCurrentBillItem());
        }

        currentBillItem = null;
        calTotal();
        payingAmount = 0.0;
        for (BillItem items : billItems) {

            payingAmount += items.getNetValue();
        }
    }

    public void changeNetValueListener(BillItem billItem) {

        if (!isPaidAmountOk(billItem)) {
            billItem.setNetValue(0);
//            UtilityController.addSuccessMessage("U cant add more than ballance");
//            return;
        }

        calTotal();
    }

    public void calTotal() {
        double n = 0.0;
        for (BillItem b : billItems) {
            n += b.getNetValue();
        }
        getCurrent().setNetTotal(0 - n);
        // //////System.out.println("AAA : " + n);
    }

    public void calTotalWithResetingIndex() {
        double n = 0.0;
        payingAmount=0.0;
        int index = 0;
        for (BillItem b : billItems) {
            b.setSearialNo(index++);
            n += b.getNetValue();
            payingAmount+= b.getNetValue();
        }
        getCurrent().setNetTotal(0 - n);
        // //////System.out.println("AAA : " + n);
    }

    public void removeAll() {
        for (BillItem b : selectedBillItems) {

            //System.err.println("Removing Index " + b.getSearialNo());
            //System.err.println("Grn No " + b.getReferenceBill().getDeptId());
            remove(b);
        }

        //   calTotalWithResetingIndex();
        selectedBillItems = null;
    }

    public void remove(BillItem billItem) {
        getBillItems().remove(billItem.getSearialNo());
        calTotalWithResetingIndex();
    }

    public void removeWithoutIndex(BillItem billItem) {
        getBillItems().remove(billItem.getSearialNo());

    }

    @Inject
    private PaymentSchemeController paymentSchemeController;

    private boolean errorCheck() {
        if (getBillItems().isEmpty()) {
            UtilityController.addErrorMessage("No Bill Item ");
            return true;
        }

        if (getCurrent().getToInstitution() == null) {
            UtilityController.addErrorMessage("Select Cant settle without Dealor");
            return true;
        }

        if (getCurrent().getPaymentMethod() == null) {
            return true;
        }

        if (getPaymentSchemeController().errorCheckPaymentScheme(getCurrent().getPaymentMethod(), getPaymentMethodData())) {
            return true;
        }

        return false;
    }
    
    public boolean checkBillAlreadyPaid() {
        boolean flag=false;
        List<BillItem> removeBillItems=new ArrayList<>();
        for (BillItem bi : getBillItems()) {
            Bill b=getBillFacade().find(bi.getReferenceBill().getId());
            if (Math.abs(b.getNetTotal() + b.getPaidAmount()) < 1) {
                removeBillItems.add(bi);
                flag=true;
            }
        }
        getBillItems().removeAll(removeBillItems);
        return flag;
    }

    private void saveBill(BillType billType) {

        getCurrent().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getCurrent(), billType, BillNumberSuffix.CRDPAY));

        getCurrent().setBillType(billType);

        getCurrent().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getCurrent().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getCurrent().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

        getCurrent().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setCreater(getSessionController().getLoggedUser());

        getCurrent().setNetTotal(getCurrent().getNetTotal());

        if (getCurrent().getId() == null) {
            getBillFacade().create(getCurrent());
        } else {
            getBillFacade().edit(getCurrent());
        }

    }

    @EJB
    private BillBean billBean;
    @EJB
    CashTransactionBean cashTransactionBean;

    public CashTransactionBean getCashTransactionBean() {
        return cashTransactionBean;
    }

    public void setCashTransactionBean(CashTransactionBean cashTransactionBean) {
        this.cashTransactionBean = cashTransactionBean;
    }

    public void settleBill() {
        if (errorCheck()) {
            return;
        }

        getBillBean().setPaymentMethodData(getCurrent(), getCurrent().getPaymentMethod(), getPaymentMethodData());

        getCurrent().setTotal(getCurrent().getNetTotal());

        if (checkBillAlreadyPaid()) {
            JsfUtil.addErrorMessage("This Bill Is All Ready Paid");
            return;
        }

        saveBill(BillType.GrnPayment);
        saveBillItem();

        WebUser wb = getCashTransactionBean().saveBillCashOutTransaction(getCurrent(), getSessionController().getLoggedUser());
        getSessionController().setLoggedUser(wb);

        UtilityController.addSuccessMessage("Bill Saved");
        printPreview = true;
    }

    private void saveBillItem() {
        for (BillItem tmp : getBillItems()) {
            tmp.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            tmp.setCreater(getSessionController().getLoggedUser());
            tmp.setBill(getCurrent());
            tmp.setNetValue(0 - tmp.getNetValue());
            getBillItemFacade().create(tmp);

            updateReferenceBill(tmp);

        }

    }

    private boolean isPaidAmountOk(BillItem tmp) {

        double refBallance = getReferenceBallance(tmp);
        double netValue = Math.abs(tmp.getNetValue());

        //System.err.println("RefBallance " + refBallance);
        //System.err.println("Net Value " + tmp.getNetValue());
        //   ballance=refBallance-tmp.getNetValue();
        if (refBallance >= netValue) {
            //System.err.println("1");
            return true;
        }

        if (netValue - refBallance < 0.1) {
            //System.err.println("2");
            return true;
        }

        return false;
    }

    private void updateReferenceBill(BillItem tmp) {
        double dbl = getCreditBean().getPaidAmount(tmp.getReferenceBill(), BillType.GrnPayment);
        //System.out.println("dbl = " + dbl);
        tmp.getReferenceBill().setPaidAmount(0 - dbl);
        getBillFacade().edit(tmp.getReferenceBill());
        //System.out.println("1 = " + tmp.getReferenceBill().getPaidAmount());
    }

    /**
     * Creates a new instance of pharmacyDealorBill
     */
    public PharmacyDealorBill() {
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public Bill getCurrent() {
        if (current == null) {
            current = new BilledBill();
        }
        return current;
    }

    public void setCurrent(Bill current) {
        this.current = current;
    }

    public BillItem getCurrentBillItem() {
        if (currentBillItem == null) {
            currentBillItem = new BillItem();
        }
        return currentBillItem;
    }

    public void setCurrentBillItem(BillItem currentBillItem) {
        this.currentBillItem = currentBillItem;
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

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
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

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public BillController getBillController() {
        return billController;
    }

    public void setBillController(BillController billController) {
        this.billController = billController;
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

    public PaymentSchemeController getPaymentSchemeController() {
        return paymentSchemeController;
    }

    public void setPaymentSchemeController(PaymentSchemeController paymentSchemeController) {
        this.paymentSchemeController = paymentSchemeController;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public List<BillItem> getSelectedBillItems() {
        return selectedBillItems;
    }

    public void setSelectedBillItems(List<BillItem> selectedBillItems) {
        this.selectedBillItems = selectedBillItems;
    }

    public CreditBean getCreditBean() {
        return creditBean;
    }

    public void setCreditBean(CreditBean creditBean) {
        this.creditBean = creditBean;
    }

    public double getPayingAmount() {
        return payingAmount;
    }

    public void setPayingAmount(double payingAmount) {
        this.payingAmount = payingAmount;
    }

    public List<BillItem> getPayingBillItems() {
        return payingBillItems;
    }

    public void setPayingBillItems(List<BillItem> payingBillItems) {
        this.payingBillItems = payingBillItems;
    }

}
