/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.inward;

import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.facade.BillFeeFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class ServiceFeeEdit implements Serializable {

    private Bill bill;
    private List<BillFee> billFees;
    @EJB
    private BillFeeFacade billFeeFacade;

    /**
     * Creates a new instance of ServiceFeeEdit
     */
    public ServiceFeeEdit() {
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
        calBillFees();
    }

    private void calBillFees() {
        String sql = "SELECT b FROM BillFee b WHERE b.retired=false and b.bill.id=" + getBill().getId();
        billFees = getBillFeeFacade().findBySQL(sql);
    }
    
    public void updateFee(BillFee billFee){
    
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

}
