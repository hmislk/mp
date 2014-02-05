/*
 * To change this currentlate, choose Tools | currentlates
 * and open the currentlate in the editor.
 */
package com.divudi.bean;

import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.ejb.BillNumberBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Institution;
import com.divudi.entity.PatientEncounter;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.InstitutionFacade;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author safrin
 */
@Named
@ViewScoped
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
    private PatientEncounter patientEncounter;
    private BillItem currentBillItem;
    private List<BillItem> billItems;
    private int index;
    private Institution chequeBank;
    private Institution slipBank;
    private Date slipDate;
    private Date chequeDate;

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

    private boolean errorCheck() {
        if (getCurrent().getFromInstitution() == null) {
            UtilityController.addErrorMessage("Select Agency");
            return true;
        }

        if (getCurrent().getPaymentScheme() != null && getCurrent().getPaymentScheme().getPaymentMethod() != null && getCurrent().getPaymentScheme().getPaymentMethod() == PaymentMethod.Cheque) {
            if (getChequeBank() == null || getCurrent().getChequeRefNo() == null || getChequeDate() == null) {
                UtilityController.addErrorMessage("Please select Cheque Number,Bank and Cheque Date");
                return true;
            }

        }


        if (getCurrent().getPaymentScheme() != null && getCurrent().getPaymentScheme().getPaymentMethod() != null && getCurrent().getPaymentScheme().getPaymentMethod() == PaymentMethod.Slip) {
            if (getSlipBank() == null || getCurrent().getComments() == null || getSlipDate() == null) {
                UtilityController.addErrorMessage("Please Fill Memo,Bank and Slip Date");
                return true;
            }

        }



        return false;
    }

    private void saveBill(BillType billType) {
        getCurrent().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(),getCurrent(), billType,BillNumberSuffix.AGNPAY));
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

    public void settleBill() {
        addToBill();
        if (errorCheck()) {
            return;
        }

        if (getCurrent().getPaymentScheme().getPaymentMethod() == PaymentMethod.Cheque) {
            getCurrent().setBank(chequeBank);
            getCurrent().setChequeDate(chequeDate);
        } else if (getCurrent().getPaymentScheme().getPaymentMethod() == PaymentMethod.Slip) {
            getCurrent().setBank(slipBank);
            getCurrent().setChequeDate(slipDate);
        }


        getCurrent().setTotal(getCurrent().getNetTotal());

        saveBill(BillType.AgentPaymentReceiveBill);
        saveBillItem();

        //Add to Agent Ballance
        getCurrent().getFromInstitution().setBallance(getCurrent().getFromInstitution().getBallance() + getCurrent().getTotal());
        getInstitutionFacade().edit(getCurrent().getFromInstitution());
        
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
        patientEncounter = null;
        slipBank = null;
        chequeBank = null;
        slipDate = null;
        chequeDate = null;
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

    public PatientEncounter getPatientEncounter() {
        return patientEncounter;
    }

    public void setPatientEncounter(PatientEncounter patientEncounter) {
        this.patientEncounter = patientEncounter;
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

    public Institution getChequeBank() {
        return chequeBank;
    }

    public void setChequeBank(Institution chequeBank) {
        this.chequeBank = chequeBank;
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

    public Date getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(Date chequeDate) {
        this.chequeDate = chequeDate;
    }

    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

    public void setInstitutionFacade(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
    }
}
