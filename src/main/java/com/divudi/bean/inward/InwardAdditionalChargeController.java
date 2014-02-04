/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.inward;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.InwardCalculation;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Fee;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.BilledBillFacade;
import com.divudi.facade.FeeFacade;
import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 Informatics)
 */
@Named
@ViewScoped
public class InwardAdditionalChargeController implements Serializable {

    private static final long serialVersionUID = 1L;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private InwardCalculation inwardCalculation;
    //////////////
    @EJB
    private FeeFacade feeFacade;
    @EJB
    private BilledBillFacade billedBillFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    //////////////
    @Inject
    private SessionController sessionController;
    //////////////
    private BilledBill current;

    private boolean errorCheck() {
        if (getCurrent().getPatientEncounter() == null) {
            UtilityController.addErrorMessage("Select BHT");
            return true;
        }

        if (getCurrent().getTotal() < 1) {
            UtilityController.addErrorMessage("Enter Added Charge Correctly");
            return true;
        }

        if (getCurrent().getComments().isEmpty()) {
            UtilityController.addErrorMessage("Enter Discription");
            return true;
        }

        return false;

    }

    public void addCharge() {
        if (errorCheck()) {
            return;
        }

        saveBill();
        saveBillItem();
        UtilityController.addSuccessMessage("Additional Charges Added");
        makeNull();
    }

    public void makeNull() {
        current = null;
    }

    private void saveBill() {
        getCurrent().setBillType(BillType.InwardBill);
        getCurrent().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), getSessionController().getDepartment(), BillType.InwardBill));
        getCurrent().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getCurrent(), getCurrent().getBillType(),BillNumberSuffix.INWSER));

        getCurrent().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setNetTotal(getCurrent().getTotal());
        getCurrent().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setCreater(getSessionController().getLoggedUser());
        getBilledBillFacade().create(getCurrent());
    }

    private void saveBillItem() {
        BillItem temBi = new BillItem();
        temBi.setBill(getCurrent());
        temBi.setGrossValue(getCurrent().getTotal());
        temBi.setNetValue(getCurrent().getTotal());
        temBi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        temBi.setCreater(getSessionController().getLoggedUser());
        getBillItemFacade().create(temBi);

        saveBillFee(temBi);

    }

    private void saveBillFee(BillItem bt) {
        BillFee bf = new BillFee();
        Fee additional = getInwardCalculation().createAdditionalFee();

        bf.setPatienEncounter(getCurrent().getPatientEncounter());
        bf.setBill(getCurrent());
        bf.setFee(additional);
        bf.setBillItem(bt);
        bf.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        bf.setCreater(getSessionController().getLoggedUser());
        bf.setFeeValue(getCurrent().getTotal());

        getBillFeeFacade().create(bf);
    }

    public BilledBillFacade getBilledBillFacade() {
        return billedBillFacade;
    }

    public void setBilledBillFacade(BilledBillFacade billedBillFacade) {
        this.billedBillFacade = billedBillFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public BilledBill getCurrent() {
        if (current == null) {
            current = new BilledBill();
            current.setBillType(BillType.InwardBill);
        }

        return current;
    }

    public void setCurrent(BilledBill current) {
        this.current = current;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public FeeFacade getFeeFacade() {
        return feeFacade;
    }

    public void setFeeFacade(FeeFacade feeFacade) {
        this.feeFacade = feeFacade;
    }

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;
    }
}
