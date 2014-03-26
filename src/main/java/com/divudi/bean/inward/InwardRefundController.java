/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.inward;

import com.divudi.bean.PaymentSchemeController;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.PaymentMethodData;
import com.divudi.ejb.BillBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.RefundBill;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class InwardRefundController implements Serializable {

    private static final long serialVersionUID = 1L;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @Inject
    private SessionController sessionController;
    private double paidAmount;
    private Bill current;
    private PaymentMethodData paymentMethodData;

    public void makeNull() {
        current = null;
        paidAmount = 0.0;
    }

    public PaymentMethod[] getPaymentMethods() {
        return PaymentMethod.values();
    }

    @Inject
    private PaymentSchemeController paymentSchemeController;

    private boolean errorCheck() {
        if (getCurrent().getPatientEncounter() == null) {
            UtilityController.addErrorMessage("Select BHT");
            return true;
        }
        if (getCurrent().getPaymentMethod() == null) {
            UtilityController.addErrorMessage("Select Payment Method");
            return true;
        }

        if (getPaymentSchemeController().errorCheckPaymentScheme(getCurrent().getPaymentMethod(), paymentMethodData)) {
            return true;
        }

        if (getPaidAmount() < getCurrent().getTotal()) {
            UtilityController.addErrorMessage("Check Refuning Amount");
            return true;
        }

        return false;
    }

    public void pay() {
        if (errorCheck()) {
            return;
        }

        saveBill();
        saveBillItem();
        makeNull();
        UtilityController.addSuccessMessage("Payment Bill Saved");
    }

    @EJB
    private BillBean billBean;

    private void saveBill() {
        getBillBean().setPaymentMethodData(getCurrent(), getCurrent().getPaymentMethod(), getPaymentMethodData());
        getCurrent().setBillType(BillType.InwardPaymentBill);
        getCurrent().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setTotal(0 - getCurrent().getTotal());
        getCurrent().setNetTotal(getCurrent().getTotal());
        getCurrent().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setCreater(getSessionController().getLoggedUser());
        getBillFacade().create(getCurrent());
    }

    private void saveBillItem() {
        BillItem temBi = new BillItem();
        temBi.setBill(getCurrent());
        temBi.setGrossValue(0 - getCurrent().getTotal());
        temBi.setNetValue(0 - getCurrent().getTotal());
        temBi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        temBi.setCreater(getSessionController().getLoggedUser());
        getBillItemFacade().create(temBi);

        saveBillFee(temBi);

    }

    private void saveBillFee(BillItem bt) {
        BillFee bf = new BillFee();
        bf.setBill(getCurrent());
        bf.setBillItem(bt);
        bf.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        bf.setCreater(getSessionController().getLoggedUser());
        bf.setFeeValue(-getCurrent().getTotal());

        getBillFeeFacade().create(bf);
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

    public Bill getCurrent() {
        if (current == null) {
            current = new RefundBill();
            current.setBillType(BillType.InwardPaymentBill);
        }

        return current;
    }

    public void setCurrent(RefundBill current) {
        this.current = current;
    }

    public void calculatePaidAmount() {

        HashMap map = new HashMap();
        String sql = "SELECT sum(bb.netTotal) FROM Bill bb where bb.retired=false "
                + " and bb.billType=:bType and bb.patientEncounter=:pe";
        map.put("bType", BillType.InwardPaymentBill);
        map.put("pe", getCurrent().getPatientEncounter());
        paidAmount = getBillFacade().findDoubleByJpql(sql, map);

    }

    public double getPaidAmount() {

        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
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
}
