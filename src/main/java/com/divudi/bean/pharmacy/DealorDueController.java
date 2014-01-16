/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.table.String1Value5;
import com.divudi.data.dataStructure.InstitutionBills;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.Institution;
import com.divudi.facade.BillFacade;
import com.divudi.facade.InstitutionFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
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
@SessionScoped
public class DealorDueController implements Serializable {

    private Date fromDate;
    private Date toDate;
    private List<InstitutionBills> items;
    private List<String1Value5> dealorCreditAge;
    private List<String1Value5> filteredList;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private CommonFunctions commonFunctions;

    public void makeNull() {
        fromDate = null;
        toDate = null;
        items = null;
        dealorCreditAge = null;
        filteredList = null;
    }

    public DealorDueController() {
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfMonth(new Date());
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

    private List<Institution> getGrnDealors() {
        String sql;
        HashMap hm;
        sql = "Select b.fromInstitution From Bill b where b.retired=false "
                + " and b.paidAmount!=(0-b.netTotal) and b.createdAt between :frm and :to "
                + " and b.billType=:tp order by b.fromInstitution.name  ";
        hm = new HashMap();
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
    //    hm.put("ins", getS)
        //  hm.put("pm", PaymentMethod.Credit);
        hm.put("tp", BillType.PharmacyGrnBill);
        return getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    private List<Institution> getGrnReturnDealors() {
        String sql;
        HashMap hm;
        sql = "Select b.toInstitution From Bill b where b.retired=false "
                + " and b.paidAmount!=(0-b.netTotal) and b.createdAt between :frm and :to "
                + " and b.billType=:tp order by b.toInstitution.name  ";
        hm = new HashMap();
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
        //  hm.put("pm", PaymentMethod.Credit);
        hm.put("tp", BillType.PharmacyGrnReturn);
        return getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    private List<Bill> getBills(BillType billType1, BillType billType2, Institution institution) {
        String sql;
        HashMap hm = new HashMap();
        sql = "Select b From Bill b where b.retired=false and b.createdAt "
                + "  between :frm and :to and b.paidAmount!=(0-b.netTotal) and "
                + " (b.fromInstitution=:ins or b.toInstitution=:ins) "
                + " and (b.billType=:tp1 or b.billType=:tp2)";
        hm = new HashMap();
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
        hm.put("ins", institution);
        hm.put("tp1", billType1);
        hm.put("tp2", billType2);
        return getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    private void setValues(Institution inst, String1Value5 dataTable5Value) {
        String sql;
        HashMap hm = new HashMap();
        sql = "Select b From Bill b where b.retired=false and b.createdAt is not null and "
                + " b.paidAmount!=(0-b.netTotal) and "
                + " (b.fromInstitution=:ins or b.toInstitution=:ins) "
                + " and (b.billType=:tp1 or b.billType=:tp2)";
        hm = new HashMap();
        hm.put("ins", inst);
        hm.put("tp1", BillType.PharmacyGrnBill);
        hm.put("tp2", BillType.PharmacyGrnReturn);
        List<Bill> lst = getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

        for (Bill b : lst) {
            Long dayCount = getCommonFunctions().getDayCountTillNow(b.getInvoiceDate());

            if (dayCount < 30) {
                dataTable5Value.setValue1(dataTable5Value.getValue1() + (b.getNetTotal() + b.getPaidAmount()));
            } else if (dayCount < 60) {
                dataTable5Value.setValue2(dataTable5Value.getValue2() + (b.getNetTotal() + b.getPaidAmount()));
            } else if (dayCount < 90) {
                dataTable5Value.setValue3(dataTable5Value.getValue3() + (b.getNetTotal() + b.getPaidAmount()));
            } else {
                dataTable5Value.setValue4(dataTable5Value.getValue4() + (b.getNetTotal() + b.getPaidAmount()));
            }

        }

    }

    public List<InstitutionBills> getItems() {
        System.err.println("GET ITEMS");
        Set<Institution> setIns = new HashSet<>();

        for (Institution ins : getGrnDealors()) {
            System.err.println("Ins Nme " + ins.getName());
            setIns.add(ins);
        }

        for (Institution ins : getGrnReturnDealors()) {
            System.err.println("Ins Nme " + ins.getName());
            setIns.add(ins);
        }

        items = new ArrayList<>();
        for (Institution ins : setIns) {
            InstitutionBills newIns = new InstitutionBills();
            newIns.setInstitution(ins);
            List<Bill> lst = getBills(BillType.PharmacyGrnBill, BillType.PharmacyGrnReturn, ins);

            newIns.setBills(lst);

            for (Bill b : lst) {
                System.err.println("Bill " + b.getId());
                newIns.setTotal(newIns.getTotal() + b.getNetTotal());
                newIns.setPaidTotal(newIns.getPaidTotal() + b.getPaidAmount());
            }

            items.add(newIns);
        }

        return items;
    }

    public void createAgeTable() {
        makeNull();
        Set<Institution> setIns = new HashSet<>();

        for (Institution ins : getGrnDealors()) {
            //   System.err.println("Ins Nme " + ins.getName());
            setIns.add(ins);
        }

        for (Institution ins : getGrnReturnDealors()) {
            // System.err.println("Ins Nme " + ins.getName());
            setIns.add(ins);
        }

        dealorCreditAge = new ArrayList<>();
        for (Institution ins : setIns) {
            String1Value5 newRow = new String1Value5();
            newRow.setString(ins.getName());
            setValues(ins, newRow);
            dealorCreditAge.add(newRow);
        }

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

    public List<String1Value5> getDealorCreditAge() {
        return dealorCreditAge;
    }

    public void setDealorCreditAge(List<String1Value5> dealorCreditAge) {
        this.dealorCreditAge = dealorCreditAge;
    }

    public List<String1Value5> getFilteredList() {
        return filteredList;
    }

    public void setFilteredList(List<String1Value5> filteredList) {
        this.filteredList = filteredList;
    }
}
