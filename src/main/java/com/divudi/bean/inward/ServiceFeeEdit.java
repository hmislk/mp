/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.inward;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.FeeType;
import com.divudi.ejb.InwardBean;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class ServiceFeeEdit implements Serializable {

    private BillItem billItem;
    private List<BillFee> billFees;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private InwardBean inwardBean;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFacade billFacade;
    @Inject
    private SessionController sessionController;

    public InwardBean getInwardBean() {
        return inwardBean;
    }

    public void setInwardBean(InwardBean inwardBean) {
        this.inwardBean = inwardBean;
    }

    
    
    /**
     * Creates a new instance of ServiceFeeEdit
     */
    public ServiceFeeEdit() {
    }

    private void calBillFees() {
        //System.err.println("Calculating BillFee 1 " + billItem);
        String sql = "SELECT b FROM BillFee b WHERE b.retired=false and b.billItem=:billItem ";
        HashMap hm = new HashMap();
        hm.put("billItem", billItem);
        billFees = getBillFeeFacade().findBySQL(sql, hm);
        //System.err.println("Calculating BillFee 2 " + billFees);
    }

    public void updateFee(RowEditEvent event) {
        BillFee billFee = (BillFee) event.getObject();

        if (billFee.getFee() != null && billFee.getFee().getFeeType() == FeeType.Staff) {
            if (billFee.getPaidValue() != 0) {
                UtilityController.addErrorMessage("Staff Fee Allready Paid");
                return;
            }
        }

        billFee.setEditor(getSessionController().getLoggedUser());
        billFee.setEditedAt(new Date());

        getBillFeeFacade().edit(billFee);
        double serviceValue = 0;
        BillFee marginFee = null;
        marginFee = getInwardBean().getBillFeeMatrix(billFee.getBillItem(), billFee.getBill().getInstitution());
        serviceValue = getInwardBean().getHospitalFeeByBillItem(billFee.getBillItem());

        double matrixValue = getInwardBean().calInwardMargin(billFee.getBillItem(), serviceValue, billFee.getBill().getFromDepartment());
        marginFee.setBill(billFee.getBill());
        marginFee.setFeeValue(matrixValue);

        if (marginFee.getId() != null) {
            marginFee.setEditedAt(new Date());
            marginFee.setEditor(getSessionController().getLoggedUser());
            getBillFeeFacade().edit(marginFee);
        }

        if (marginFee.getId() == null && marginFee.getFeeValue() != 0) {
            getBillFeeFacade().create(marginFee);
        }

        calBillFees();
        calBillItemTotal();
        calBillTotal();
    }

    private void calBillItemTotal() {
        String sql = "SELECT sum(b.feeValue) FROM BillFee b WHERE b.retired=false and b.billItem=:billItem ";
        HashMap hm = new HashMap();
        hm.put("billItem", billItem);
        double val = getBillFeeFacade().findDoubleByJpql(sql, hm);

        billItem.setNetValue(val);
        billItem.setEditedAt(new Date());
        billItem.setEditor(getSessionController().getLoggedUser());
        getBillItemFacade().edit(billItem);

    }

    private void calBillTotal() {
        String sql = "SELECT sum(b.feeValue) FROM BillFee b WHERE b.retired=false and b.bill=:bill ";
        HashMap hm = new HashMap();
        hm.put("bill", billItem.getBill());
        double val = getBillFeeFacade().findDoubleByJpql(sql, hm);

        billItem.getBill().setNetTotal(val);
        billItem.getBill().setEditedAt(new Date());
        billItem.getBill().setEditor(getSessionController().getLoggedUser());
        getBillFacade().edit(billItem.getBill());
    }

    public List<BillFee> getBillFees() {
        if (billFees == null) {
            billFees = new ArrayList<>();
        }
        return billFees;
    }

    public void setBillFees(List<BillFee> billFees) {
        this.billFees = billFees;

    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

   

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public BillItem getBillItem() {
        return billItem;
    }

    public void setBillItem(BillItem billItem) {
        this.billItem = billItem;

        calBillFees();
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

}
