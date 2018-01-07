/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.BillController;
import com.divudi.data.BillType;
import com.divudi.data.InstitutionType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.InstitutionBills;
import com.divudi.data.table.String1Value5;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.CreditBean;
import com.divudi.entity.Bill;
import com.divudi.entity.Institution;
import com.divudi.facade.BillFacade;
import com.divudi.facade.InstitutionFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class CustomerDueController implements Serializable {

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
    @EJB
    private CreditBean creditBean;

    double grantTotal;
    double billCount;
    double billedAmount;
    double returnedAmount;
    double paidAmount;

    double value1;
    double value2;
    double value3;
    double value4;
    double GrandDealorAge;

    public void makeNull() {
        fromDate = null;
        toDate = null;
        items = null;
        dealorCreditAge = null;
        filteredList = null;
    }

    public CustomerDueController() {
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

    private void setValues(Institution inst, String1Value5 dataTable5Value) {

        List<Bill> lst = getCreditBean().getBills(inst);
        //System.err.println("Institution Ins " + inst.getName());
        for (Bill b : lst) {
            double rt = getCreditBean().getGrnReturnValue(b);

            //   double dbl = Math.abs(b.getNetTotal()) - (Math.abs(b.getTmpReturnTotal()) + Math.abs(b.getPaidAmount()));
            b.setTmpReturnTotal(rt);

            Long dayCount = getCommonFunctions().getDayCountTillNow(b.getInvoiceDate());

            double finalValue = (b.getNetTotal() + b.getPaidAmount() + b.getTmpReturnTotal());

            //System.err.println("DayCount " + dayCount);
            //System.err.println("NetTotal " + b.getNetTotal());
            //System.err.println("Return  " + b.getTmpReturnTotal());
            //System.err.println("Paid " + b.getPaidAmount());
            //System.err.println("Final " + finalValue);
            if (dayCount < 30) {
                dataTable5Value.setValue1(dataTable5Value.getValue1() + finalValue);
            } else if (dayCount < 60) {
                dataTable5Value.setValue2(dataTable5Value.getValue2() + finalValue);
            } else if (dayCount < 90) {
                dataTable5Value.setValue3(dataTable5Value.getValue3() + finalValue);
            } else {
                dataTable5Value.setValue4(dataTable5Value.getValue4() + finalValue);
            }

        }

    }

    @Inject
    private BillController billController;
    @Inject
    private PharmacyDealorBill pharmacyDealorBill;

    List<Bill> bills;
    List<Bill> filteredBills;
    double billsNetTotal;
    double billsPaidTotal;
    double billsBalanceTotal;

    public void calculateFilteredBillTotals(List<Bill> billsToCal) {
        billsNetTotal = 0.0;
        billsPaidTotal = 0.0;
        billsBalanceTotal = 0.0;
        if (billsToCal == null) {
            return;
        }
        for (Bill b : billsToCal) {
            billsNetTotal += b.getNetTotal();
            billsPaidTotal += b.getPaidAmount();
        }
        billsBalanceTotal = Math.abs(billsNetTotal) - Math.abs(billsPaidTotal);
    }

    public String fillSingleDealerTransactions() {
        String jpql;
        Map m;
        m = new HashMap();
        BillType[] bts = {BillType.PharmacyWholeSale};
        m.put("bts", bts);
        if (institution == null) {
            jpql = "Select b from Bill b where b.retired=false and b.billType in :bts order by b.id";
            bills = getBillFacade().findBySQL(jpql,m);
        } else {
            jpql = "Select b from Bill b where b.retired=false  and b.billType in :bts and (b.fromInstitution=:ins or b.toInstitution=:ins ) order by b.id";
            m.put("ins", institution);
            bills = getBillFacade().findBySQL(jpql, m);
        }

        calculateFilteredBillTotals(bills);
        return "pharmacy_search_customer";
    }

    public double getBillsNetTotal() {
        return billsNetTotal;
    }

    public void setBillsNetTotal(double billsNetTotal) {
        this.billsNetTotal = billsNetTotal;
    }

    public double getBillsPaidTotal() {
        return billsPaidTotal;
    }

    public void setBillsPaidTotal(double billsPaidTotal) {
        this.billsPaidTotal = billsPaidTotal;
    }

    public double getBillsBalanceTotal() {
        return billsBalanceTotal;
    }

    public void setBillsBalanceTotal(double billsBalanceTotal) {
        this.billsBalanceTotal = billsBalanceTotal;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public List<Bill> getFilteredBills() {
        return filteredBills;
    }

    public void setFilteredBills(List<Bill> filteredBills) {
        this.filteredBills = filteredBills;
        calculateFilteredBillTotals(filteredBills);
    }

    public void fillItems() {
        //System.err.println("Fill Items");
//        Set<Institution> setIns = new HashSet<>();

        List<Institution> setIns = new ArrayList<>();

        List<Institution> list = getCreditBean().getDealorFromBills(getFromDate(), getToDate(), InstitutionType.Dealer);
        list.addAll(getCreditBean().getDealorFromReturnBills(getFromDate(), getToDate(), InstitutionType.Dealer));

        setIns.addAll(list);

        //System.err.println("size " + setIns.size());
        items = new ArrayList<>();
        billCount = 0.0;
        for (Institution ins : setIns) {
            //     //System.err.println("Ins " + ins.getName());
            InstitutionBills newIns = new InstitutionBills();
            newIns.setInstitution(ins);
            List<Bill> lst = getCreditBean().getBills(ins, getFromDate(), getToDate());

            double tmp = 0.0;
            tmp = (double) lst.size();
            //    //System.out.println("tmp = " + tmp);
            //    //System.out.println("lst.size() = " + lst.size());
            //    //System.out.println("billCount = " + billCount);
            billCount += tmp;
            //    //System.out.println("billCount = " + billCount);

            newIns.setBills(lst);

            for (Bill b : lst) {
                double rt = getCreditBean().getGrnReturnValue(b);
                b.setTmpReturnTotal(rt);

                double dbl = Math.abs(b.getNetTotal()) - (Math.abs(b.getTmpReturnTotal()) + Math.abs(b.getPaidAmount()));

                if (dbl > 0.1) {
                    b.setTransBoolean(true);
                    newIns.setReturned(newIns.getReturned() + b.getTmpReturnTotal());
                    newIns.setTotal(newIns.getTotal() + b.getNetTotal());
                    newIns.setPaidTotal(newIns.getPaidTotal() + b.getPaidAmount());

                    //    //System.err.println("Net Total " + b.getNetTotal());
                    //     //System.err.println("Paid " + b.getPaidAmount());
                    //System.err.println("Return " + b.getTmpReturnTotal());
                }
            }

            double finalValue = (newIns.getPaidTotal() + newIns.getTotal() + newIns.getReturned());
            //System.err.println("Final Value " + finalValue);
            if (finalValue != 0 && finalValue < 0.1) {
                items.add(newIns);
            }
        }
        billedAmount = 0.0;
        returnedAmount = 0.0;
        paidAmount = 0.0;
        grantTotal = 0.0;
        for (InstitutionBills its : items) {
            billedAmount += its.getTotal();
            returnedAmount += its.getReturned();
            paidAmount += its.getPaidTotal();
            grantTotal += its.getPaidTotal() + its.getReturned() + its.getTotal();

        }
        //    //System.out.println("grantTotal = " + grantTotal);
    }

    public void fillItemsStore() {
        //System.err.println("Fill Items");
        Set<Institution> setIns = new HashSet<>();

        List<Institution> list = getCreditBean().getDealorFromBills(getFromDate(), getToDate(), InstitutionType.StoreDealor);
        list.addAll(getCreditBean().getDealorFromReturnBills(getFromDate(), getToDate(), InstitutionType.StoreDealor));

        setIns.addAll(list);
        //System.err.println("size " + setIns.size());
        items = new ArrayList<>();
        for (Institution ins : setIns) {
            //     //System.err.println("Ins " + ins.getName());
            InstitutionBills newIns = new InstitutionBills();
            newIns.setInstitution(ins);
            List<Bill> lst = getCreditBean().getBills(ins, getFromDate(), getToDate());

            newIns.setBills(lst);

            for (Bill b : lst) {
                double rt = getCreditBean().getGrnReturnValue(b);
                b.setTmpReturnTotal(rt);

                double dbl = Math.abs(b.getNetTotal()) - (Math.abs(b.getTmpReturnTotal()) + Math.abs(b.getPaidAmount()));

                if (dbl > 0.1) {
                    b.setTransBoolean(true);
                    newIns.setReturned(newIns.getReturned() + b.getTmpReturnTotal());
                    newIns.setTotal(newIns.getTotal() + b.getNetTotal());
                    newIns.setPaidTotal(newIns.getPaidTotal() + b.getPaidAmount());

                    //    //System.err.println("Net Total " + b.getNetTotal());
                    //     //System.err.println("Paid " + b.getPaidAmount());
                    //System.err.println("Return " + b.getTmpReturnTotal());
                }
            }

            double finalValue = (newIns.getPaidTotal() + newIns.getTotal() + newIns.getReturned());
            //System.err.println("Final Value " + finalValue);
            if (finalValue != 0 && finalValue < 0.1) {
                items.add(newIns);
            }
        }
    }

    public List<InstitutionBills> getItems() {
        return items;
    }

    public void createAgeTable() {
        makeNull();
        //System.err.println("Fill Items");
//        Set<Institution> setIns = new HashSet<>();
        List<Institution> setIns = new ArrayList<>();

        List<Institution> list = getCreditBean().getDealorFromBills(InstitutionType.Dealer);
        list.addAll(getCreditBean().getDealorFromReturnBills(InstitutionType.Dealer));

        setIns.addAll(list);
        //System.err.println("size " + setIns.size());

        dealorCreditAge = new ArrayList<>();
        for (Institution ins : setIns) {
            if (ins == null) {
                continue;
            }

            String1Value5 newRow = new String1Value5();
            newRow.setString(ins.getName());
            setValues(ins, newRow);

            if (newRow.getValue1() != 0
                    || newRow.getValue2() != 0
                    || newRow.getValue3() != 0
                    || newRow.getValue4() != 0) {
                dealorCreditAge.add(newRow);
            }
        }

        value1 = 0.0;
        value2 = 0.0;
        value3 = 0.0;
        value4 = 0.0;
        GrandDealorAge = 0.0;

        for (String1Value5 stv : dealorCreditAge) {
            value1 += stv.getValue1();
            value2 += stv.getValue2();
            value3 += stv.getValue3();
            value4 += stv.getValue4();

        }
    }

    public void createAgeTableStore() {
        makeNull();
        //System.err.println("Fill Items");
        Set<Institution> setIns = new HashSet<>();

        List<Institution> list = getCreditBean().getDealorFromBills(InstitutionType.StoreDealor);
        list.addAll(getCreditBean().getDealorFromReturnBills(InstitutionType.StoreDealor));

        setIns.addAll(list);
        //System.err.println("size " + setIns.size());

        dealorCreditAge = new ArrayList<>();
        for (Institution ins : setIns) {
            if (ins == null) {
                continue;
            }

            String1Value5 newRow = new String1Value5();
            newRow.setString(ins.getName());
            setValues(ins, newRow);

            if (newRow.getValue1() != 0
                    || newRow.getValue2() != 0
                    || newRow.getValue3() != 0
                    || newRow.getValue4() != 0) {
                dealorCreditAge.add(newRow);
            }
        }

    }

    private Institution institution;

    public List<Bill> getItems2() {
        String sql;
        HashMap hm;

        sql = "Select b From Bill b where b.retired=false and b.createdAt "
                + "  between :frm and :to and b.creditCompany=:cc "
                + " and b.paymentMethod= :pm and b.billType=:tp";
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
                + " and b.paymentMethod= :pm and b.billType=:tp";
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

    public BillController getBillController() {
        return billController;
    }

    public void setBillController(BillController billController) {
        this.billController = billController;
    }

    public PharmacyDealorBill getPharmacyDealorBill() {
        return pharmacyDealorBill;
    }

    public void setPharmacyDealorBill(PharmacyDealorBill pharmacyDealorBill) {
        this.pharmacyDealorBill = pharmacyDealorBill;
    }

    public CreditBean getCreditBean() {
        return creditBean;
    }

    public void setCreditBean(CreditBean creditBean) {
        this.creditBean = creditBean;
    }

    public double getGrantTotal() {
        return grantTotal;
    }

    public void setGrantTotal(double grantTotal) {
        this.grantTotal = grantTotal;
    }

    public double getBillCount() {
        return billCount;
    }

    public void setBillCount(double billCount) {
        this.billCount = billCount;
    }

    public double getBilledAmount() {
        return billedAmount;
    }

    public void setBilledAmount(double billedAmount) {
        this.billedAmount = billedAmount;
    }

    public double getReturnedAmount() {
        return returnedAmount;
    }

    public void setReturnedAmount(double returnedAmount) {
        this.returnedAmount = returnedAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getValue1() {
        return value1;
    }

    public void setValue1(double value1) {
        this.value1 = value1;
    }

    public double getValue2() {
        return value2;
    }

    public void setValue2(double value2) {
        this.value2 = value2;
    }

    public double getValue3() {
        return value3;
    }

    public void setValue3(double value3) {
        this.value3 = value3;
    }

    public double getValue4() {
        return value4;
    }

    public void setValue4(double value4) {
        this.value4 = value4;
    }

    public double getGrandDealorAge() {
        return GrandDealorAge;
    }

    public void setGrandDealorAge(double GrandDealorAge) {
        this.GrandDealorAge = GrandDealorAge;
    }

}
