/*
 * To change this currentlate, choose Tools | currentlates
 * and open the currentlate in the editor.
 */
package com.divudi.bean;

import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.Title;
import com.divudi.ejb.BillNumberBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.Person;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PersonFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author safrin
 */
@Named(value = "pettyCashBillController")
@SessionScoped
public class PettyCashBillController implements Serializable {

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
    private Person newPerson;

    public PettyCashBillController() {
    }

    public Title[] getTitle() {
        return Title.values();
    }

    private boolean errorCheck() {
         if (getCurrent().getPaymentScheme() != null && getCurrent().getPaymentScheme().getPaymentMethod() != null && getCurrent().getPaymentScheme().getPaymentMethod() == PaymentMethod.Cheque) {
            if (getCurrent().getBank() == null || getCurrent().getChequeRefNo() == null || getCurrent().getChequeDate() == null) {
                UtilityController.addErrorMessage("Please select Cheque Number,Bank and Cheque Date");
                return true;
            }

        }
        
        
        if (getCurrent().getInvoiceNumber().isEmpty()) {
            UtilityController.addErrorMessage("Enter Invoice Number");
            return true;
        }

        if (getCurrent().getNetTotal() < 1) {
            UtilityController.addErrorMessage("Type Amount");
            return true;
        }

        if (checkInvoice()) {
            UtilityController.addErrorMessage("Invoice Number Already Exist");
            return true;
        }

        return false;
    }

    private boolean checkInvoice() {
        String sql = "Select b From BilledBill b where b.retired=false and b.cancelledBill is null and b.billType= :btp and upper(b.invoiceNumber)='" + getCurrent().getInvoiceNumber().trim().toUpperCase() + "'";
        HashMap h = new HashMap();
        h.put("btp", BillType.PettyCash);
        List<Bill> tmp = getBillFacade().findBySQL(sql, h, TemporalType.TIME);

     //   System.out.println("asdsads" + tmp.size());
        if (tmp.size() > 0) {
            return true;
        }

        return false;
    }

    private void saveBill() {

        getCurrent().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(),getCurrent(), BillType.PettyCash,BillNumberSuffix.PTYPAY));

        getCurrent().setBillType(BillType.PettyCash);

        getCurrent().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getCurrent().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getCurrent().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

        getCurrent().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setCreater(getSessionController().getLoggedUser());

        getCurrent().setTotal(-getCurrent().getNetTotal());
        getCurrent().setNetTotal(-getCurrent().getNetTotal());

        getBillFacade().create(getCurrent());
    }
    @EJB
    private PersonFacade personFacade;

    public void settleBill() {

        if (errorCheck()) {
            return;
        }

        if (getTabId().equals("tabNew")) {
            getPersonFacade().create(getNewPerson());
            getCurrent().setPerson(getNewPerson());
        }

        getCurrent().setTotal(getCurrent().getNetTotal());
        
        saveBill();
        saveBillItem();


        UtilityController.addSuccessMessage("Bill Saved");
        printPreview = true;

    }

    private void saveBillItem() {
        BillItem tmp = new BillItem();

        tmp.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        tmp.setCreater(getSessionController().getLoggedUser());
        tmp.setBill(getCurrent());
        tmp.setNetValue(-getCurrent().getNetTotal());
        getBillItemFacade().create(tmp);

    }
    private String tabId = "tabStaff";

    public void recreateModel() {
        current = null;
        printPreview = false;
        newPerson = null;

        tabId = "tabStaff";
    }

    public void onTabChange(TabChangeEvent event) {
        setTabId(event.getTab().getId());
    }

    public void prepareNewBill() {
        recreateModel();
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

    public Person getNewPerson() {
        if (newPerson == null) {
            newPerson = new Person();
        }
        return newPerson;
    }

    public void setNewPerson(Person newPerson) {
        this.newPerson = newPerson;
    }

    public String getTabId() {
        return tabId;
    }

    public void setTabId(String tabId) {
        this.tabId = tabId;
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }
}
