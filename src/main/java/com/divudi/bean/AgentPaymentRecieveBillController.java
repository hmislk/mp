/*
 * To change this currentlate, choose Tools | currentlates
 * and open the currentlate in the editor.
 */
package com.divudi.bean;

import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.dataStructure.PaymentMethodData;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CashTransactionBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.WebUser;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.InstitutionFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class AgentPaymentRecieveBillController implements Serializable {

    private Bill current;
    private boolean printPreview = false;
    @EJB
    private BillNumberBean billNumberBean;
    @Inject
    private SessionController sessionController;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    private BillItem currentBillItem;
    private List<BillItem> billItems;
    private int index;
    private PaymentMethodData paymentMethodData;

    public void addToBill() {
        getCurrentBillItem().setNetValue(getCurrent().getNetTotal());
        getCurrentBillItem().setGrossValue(getCurrent().getNetTotal());
        getCurrentBillItem().setBillSession(null);
        getCurrentBillItem().setDiscount(0.0);
        getCurrentBillItem().setItem(null);
        getCurrentBillItem().setQty(1.0);
        getCurrentBillItem().setRate(getCurrent().getNetTotal());
        getBillItems().add(getCurrentBillItem());
        currentBillItem = null;
    }

    public AgentPaymentRecieveBillController() {
    }

    @Inject
    private PaymentSchemeController paymentSchemeController;

    private boolean errorCheck() {
        if (getCurrent().getFromInstitution() == null) {
            UtilityController.addErrorMessage("Select Agency");
            return true;
        }

        if (getCurrent().getPaymentScheme() == null) {
            return true;
        }

        if (getCurrent().getPaymentScheme().getPaymentMethod() == null) {
            return true;
        }

        if (getPaymentSchemeController().errorCheckPaymentScheme(getCurrent().getPaymentScheme().getPaymentMethod(), paymentMethodData)) {
            return true;
        }

        return false;
    }

    private void saveBill(BillType billType) {
        getCurrent().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getCurrent(), billType, BillNumberSuffix.AGNPAY));
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
        addToBill();
        if (errorCheck()) {
            return;
        }

        getBillBean().setPaymentMethodData(getCurrent(), getCurrent().getPaymentMethod(), getPaymentMethodData());

        getCurrent().setTotal(getCurrent().getNetTotal());

        saveBill(BillType.AgentPaymentReceiveBill);
        saveBillItem();

        //Add to Agent Ballance
        getCurrent().getFromInstitution().setBallance(getCurrent().getFromInstitution().getBallance() + getCurrent().getTotal());
        getInstitutionFacade().edit(getCurrent().getFromInstitution());

        ///////////////////
        WebUser wb = getCashTransactionBean().saveBillCashInTransaction(getCurrent(), getSessionController().getLoggedUser());
        getSessionController().setLoggedUser(wb);

        UtilityController.addSuccessMessage("Bill Saved");
        printPreview = true;

    }

    private void saveBillItem() {
        for (BillItem tmp : getBillItems()) {
            tmp.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            tmp.setCreater(getSessionController().getLoggedUser());
            tmp.setBill(getCurrent());
            tmp.setNetValue(tmp.getNetValue());
            getBillItemFacade().create(tmp);
        }
    }

    public void recreateModel() {
        current = null;
        printPreview = false;
        currentBillItem = null;
        paymentMethodData = null;
        billItems = null;

    }

    public String prepareNewBill() {
        recreateModel();
        return "";
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

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
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
            billItems = new ArrayList<BillItem>();
        }
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

    public void setInstitutionFacade(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
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
