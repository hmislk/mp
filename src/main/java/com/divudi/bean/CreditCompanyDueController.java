/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.InstitutionBills;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.Institution;
import com.divudi.facade.BillFacade;
import com.divudi.facade.InstitutionFacade;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Named
@ViewScoped
public class CreditCompanyDueController implements Serializable {

    private Date fromDate;
    private Date toDate;
    private List<InstitutionBills> items;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private CommonFunctions commonFunctions;

    public CreditCompanyDueController() {
    }

    public Date getFromDate() {
        if(fromDate==null){
            fromDate=getCommonFunctions().getStartOfMonth(new Date());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = new Date();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public List<InstitutionBills> getItems() {
        String sql;
        HashMap hm;
        sql = "Select b.creditCompany From Bill b where b.retired=false "
                + " and b.paidAmount!=b.netTotal and b.cancelledBill is null and "
                + " b.refundedBill is null and b.createdAt between :frm and :to "
                + " and b.paymentScheme.paymentMethod= :pm and b.billType=:tp order by b.creditCompany.name  ";
        hm = new HashMap();
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
        hm.put("pm", PaymentMethod.Credit);
        hm.put("tp", BillType.OpdBill);
        List<Institution> tmp = getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);
        Set<Institution> setIns = new HashSet<>();

        for (Institution ins : tmp) {
            setIns.add(ins);
        }

        items = new ArrayList<>();
        for (Institution ins : setIns) {

            sql = "Select b From Bill b where b.retired=false and b.createdAt "
                    + "  between :frm and :to and b.paidAmount!=b.netTotal and b.cancelledBill is null  "
                    + " and b.refundedBill is null and b.creditCompany=:cc "
                    + " and b.paymentScheme.paymentMethod= :pm and b.billType=:tp";
            hm = new HashMap();
            hm.put("frm", getFromDate());
            hm.put("to", getToDate());
            hm.put("cc", ins);
            hm.put("pm", PaymentMethod.Credit);
            hm.put("tp", BillType.OpdBill);
            List<Bill> bills = getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);
            InstitutionBills newIns = new InstitutionBills();
            newIns.setInstitution(ins);
            newIns.setBills(bills);

            for (Bill b : bills) {
                newIns.setTotal(newIns.getTotal() + b.getNetTotal());
                newIns.setPaidTotal(newIns.getPaidTotal() + b.getPaidAmount());
            }

            items.add(newIns);
        }

        return items;
    }

    private Institution institution;

    public List<Bill> getItems2() {
        String sql;
        HashMap hm;

        sql = "Select b From Bill b where b.retired=false and b.createdAt "
                + "  between :frm and :to and b.creditCompany=:cc "
                + " and b.paymentScheme.paymentMethod= :pm and b.billType=:tp";
        hm = new HashMap();
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
        hm.put("cc", getInstitution());
        hm.put("pm", PaymentMethod.Credit);
        hm.put("tp", BillType.OpdBill);
        return getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public double getCreditTotal() {
        String sql;
        HashMap hm;

        sql = "Select sum(b.netTotal) From Bill b where b.retired=false and b.createdAt "
                + "  between :frm and :to and b.creditCompany=:cc "
                + " and b.paymentScheme.paymentMethod= :pm and b.billType=:tp";
        hm = new HashMap();
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
        hm.put("cc", getInstitution());
        hm.put("pm", PaymentMethod.Credit);
        hm.put("tp", BillType.OpdBill);
        return getBillFacade().findDoubleByJpql(sql, hm, TemporalType.TIMESTAMP);

    }

    public void setItems(List<InstitutionBills> items) {
        this.items = items;
    }

    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

    public void setInstitutionFacade(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }
}
