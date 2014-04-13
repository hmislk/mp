/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.dataStructure.YearMonthDay;
import com.divudi.data.inward.InwardChargeType;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CashTransactionBean;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CashTransaction;
import com.divudi.entity.Drawer;
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
import static org.joda.time.PeriodType.yearMonthDay;

/**
 *
 * @author Buddhika
 */
@Named
@SessionScoped
public class DrawerAdjustmentController implements Serializable {

    /**
     * Creates a new instance of PharmacySaleController
     */
    public DrawerAdjustmentController() {
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
    private PersonFacade personFacade;
    @EJB
    private PatientFacade patientFacade;
    @EJB
    BillNumberBean billNumberBean;
    /////////////////////////
//    Item selectedAlternative;
    private Bill adjustmentBill;
    Drawer drawer;
    CashTransaction cashTransaction;
    String comment;
    List<BillItem> billItems;
    private boolean printPreview;

    public CashTransaction getCashTransaction() {
        if (cashTransaction == null) {
            cashTransaction = new CashTransaction();
        }
        return cashTransaction;
    }

    public void setCashTransaction(CashTransaction cashTransaction) {
        this.cashTransaction = cashTransaction;
    }

    public Bill getAdjustmentBill() {
        if (adjustmentBill == null) {
            adjustmentBill = new BilledBill();
            adjustmentBill.setBillType(BillType.DrawerAdjustment);
        }
        return adjustmentBill;
    }

    public void setAdjustmentPreBill(Bill adjustmentPreBill) {
        this.adjustmentBill = adjustmentPreBill;
    }

    public Drawer getDrawer() {
        return drawer;
    }

    public void setDrawer(Drawer drawer) {
        this.drawer = drawer;
    }

    public void makeNull() {
        printPreview = false;
        drawer = null;
        cashTransaction = null;
        clearBill();
    }

    public String newSaleBill() {
        clearBill();
        return "";
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    private void saveAdjustmentBill() {
        getAdjustmentBill().setCreatedAt(Calendar.getInstance().getTime());
        getAdjustmentBill().setCreater(getSessionController().getLoggedUser());
        getAdjustmentBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getAdjustmentBill(), getAdjustmentBill().getBillType(), BillNumberSuffix.NONE));
        getAdjustmentBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getAdjustmentBill(), getAdjustmentBill().getBillType(), BillNumberSuffix.NONE));
        getAdjustmentBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getAdjustmentBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
        getAdjustmentBill().setToDepartment(null);
        getAdjustmentBill().setToInstitution(null);
        getAdjustmentBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        getAdjustmentBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());
        getAdjustmentBill().setComments(comment);
        if (getAdjustmentBill().getId() == null) {
            getBillFacade().create(getAdjustmentBill());
        } else {
            getBillFacade().edit(getAdjustmentBill());
        }
    }

    private boolean errorCheck() {
        if (getDrawer() == null) {
            return true;
        }

        return false;
    }

    @EJB
    CashTransactionBean cashTransactionBean;

    public CashTransactionBean getCashTransactionBean() {
        return cashTransactionBean;
    }

    public void setCashTransactionBean(CashTransactionBean cashTransactionBean) {
        this.cashTransactionBean = cashTransactionBean;
    }

    public void saveAdjustBill() {

        if (errorCheck()) {
            return;
        }

        saveAdjustmentBill();
        CashTransaction ct = getCashTransactionBean().saveCashAdjustmentTransaction(cashTransaction, adjustmentBill, drawer, getSessionController().getLoggedUser());
        getAdjustmentBill().setCashTransaction(ct);
        getBillFacade().edit(getAdjustmentBill());

        getCashTransactionBean().resetBallance(getDrawer(), ct);

        printPreview = true;
    }

    private void clearBill() {
        billItems = null;
        comment = "";
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

}
