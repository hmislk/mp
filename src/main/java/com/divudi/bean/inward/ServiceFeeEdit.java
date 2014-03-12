/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.inward;

import com.divudi.ejb.InwardCalculation;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import static com.divudi.entity.Payment_.bill;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;

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
    private InwardCalculation inwardCalculation;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFacade billFacade;

    /**
     * Creates a new instance of ServiceFeeEdit
     */
    public ServiceFeeEdit() {
    }

    private void calBillFees() {
        String sql = "SELECT b FROM BillFee b WHERE b.retired=false and b.billItem=:billItem ";
        HashMap hm = new HashMap();
        hm.put("billItem", billItem);
        billFees = getBillFeeFacade().findBySQL(sql, hm);
    }

    public void updateFee(BillFee billFee) {
        getBillFeeFacade().edit(billFee);

        BillFee marginFee = null;
        marginFee = getInwardCalculation().getBillFeeMatrix(billFee.getBillItem());
        double serviceValue = getInwardCalculation().getHospitalFeeByBillItem(billFee.getBillItem());
        marginFee.setFeeValue(getInwardCalculation().calInwardMargin(billFee.getBillItem(), billFee.getBill().getPatientEncounter(), serviceValue));
        getBillFeeFacade().edit(marginFee);

        calBillFees();
        calBillItemTotal();
        calBillTotal();
    }

    private void calBillItemTotal() {
        String sql = "SELECT sum(b.feeValue) FROM BillFee b WHERE b.retired=false and b.billItem=:billItem ";
        HashMap hm = new HashMap();
        hm.put("bill", billItem);
        double val = getBillFeeFacade().findDoubleByJpql(sql, hm);

        billItem.setNetValue(val);
        getBillItemFacade().edit(billItem);

    }

    private void calBillTotal() {
        String sql = "SELECT sum(b.feeValue) FROM BillFee b WHERE b.retired=false and b.bill=:bill ";
        HashMap hm = new HashMap();
        hm.put("bill", billItem.getBill());
        double val = getBillFeeFacade().findDoubleByJpql(sql, hm);

        billItem.getBill().setNetTotal(val);
        getBillFacade().edit(billItem.getBill());
    }

    public List<BillFee> getBillFees() {
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

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;
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

}
