/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.data.BillType;
import com.divudi.data.InstitutionType;
import com.divudi.data.PaymentMethod;
import com.divudi.entity.Bill;
import com.divudi.entity.Institution;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.InstitutionFacade;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Stateless
public class CreditBean {

    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private BillFacade billFacade;

    public List<Bill> getCreditBills(Institution ins, BillType billType, Date fromDate, Date toDate, boolean lessThan) {
        String sql = "Select b From BilledBill b"
                + " where b.retired=false "
                + " and b.createdAt  between :frm and :to ";

        if (lessThan) {
            sql += " and (abs(b.netTotal)-abs(b.paidAmount))>:val ";
        } else {
            sql += " and (abs(b.netTotal)-abs(b.paidAmount))<:val ";
        }

        sql += " and b.cancelledBill is null  "
                + " and b.refundedBill is null"
                + " and b.creditCompany=:cc "
                + " and b.paymentMethod= :pm "
                + " and b.billType=:tp";

        HashMap hm = new HashMap();
        hm.put("frm", fromDate);
        hm.put("to", toDate);
        hm.put("cc", ins);
        hm.put("pm", PaymentMethod.Credit);
        hm.put("tp", billType);
        hm.put("val", 0.1);
        List<Bill> bills = getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

        return bills;
    }

    public List<Institution> getCreditInstitution(BillType billType, Date fromDate, Date toDate, boolean lessThan) {
        String sql;
        HashMap hm;
        sql = "Select distinct(b.creditCompany) From BilledBill b "
                + " where b.retired=false ";

        if (lessThan) {
            sql += " and (abs(b.netTotal)-abs(b.paidAmount))>:val ";
        } else {
            sql += " and (abs(b.netTotal)-abs(b.paidAmount))<:val ";
        }

        sql += " and b.cancelled=false "
                + " and b.refundedBill is null"
                + " and b.createdAt between :frm and :to "
                + " and b.paymentMethod= :pm "
                + " and b.billType=:tp"
                + " order by b.creditCompany.name  ";

        hm = new HashMap();
        hm.put("frm", fromDate);
        hm.put("to", toDate);
        hm.put("pm", PaymentMethod.Credit);
        hm.put("tp", billType);
        hm.put("val", 0.1);
        List<Institution> setIns = getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

        return setIns;
    }


    public List<Institution> getCreditCompanyFromBht(boolean lessThan,PaymentMethod paymentMethod) {
        String sql;
        HashMap hm;
        sql = "Select distinct(b.creditCompany) "
                + " From PatientEncounter b "
                + " where b.retired=false "
                + " and b.paymentFinalized=true "
                + " and b.paymentMethod=:pm ";

        if (lessThan) {
            sql += " and abs(b.creditUsedAmount)-abs(b.creditPaidAmount)> :val ";
        } else {
            sql += " and abs(b.creditUsedAmount)-abs(b.creditPaidAmount)< :val ";
        }

        hm = new HashMap();
        hm.put("val", 0.1);
        hm.put("pm", paymentMethod);
        return getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);
    }

    public List<Institution> getCreditInstitutionByPatientEncounter(Date fromDate, Date toDate, PaymentMethod paymentMethod, boolean lessThan) {
        String sql;
        HashMap hm;
        sql = "Select distinct(b.creditCompany)"
                + " From PatientEncounter b "
                + " where b.retired=false ";
        if (lessThan) {
            sql += " and abs(b.creditUsedAmount)-abs(b.creditPaidAmount) >:val ";
        } else {
            sql += " and abs(b.creditUsedAmount)-abs(b.creditPaidAmount) <:val ";
        }
        sql += " and b.dateOfDischarge between :frm and :to "
                + " and b.paymentFinalized = true "
                + " and b.paymentMethod = :pm "
                + " order by b.creditCompany.name  ";

        hm = new HashMap();
        hm.put("frm", fromDate);
        hm.put("to", toDate);
        hm.put("pm", paymentMethod);
        hm.put("val", 0.1);
        List<Institution> setIns = getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

        return setIns;
    }

    public double getPaidAmount(Bill b, BillType billType) {
        String sql = "Select sum(b.netValue) From BillItem b "
                + " where b.retired=false "
                + " and b.referenceBill=:rB "
                + " and b.bill.billType=:btp ";

        HashMap hm = new HashMap();
        hm.put("rB", b);
        hm.put("btp", billType);

        return getBillItemFacade().findDoubleByJpql(sql, hm);

    }


    public List<Institution> getDealorFromReturnBills(Date frmDate, Date toDate, InstitutionType institutionType) {
        String sql;
        HashMap hm;
        sql = "Select distinct(b.toInstitution) From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and b.paymentMethod=:pm "
                + " and b.toInstitution.institutionType=:insTp"
                + " and b.createdAt between :frm and :to "
                + " and (b.billType=:tp1 or b.billType=:tp2)"
                + " order by b.toInstitution.name  ";
        hm = new HashMap();
        hm.put("frm", frmDate);
        hm.put("to", toDate);
        hm.put("pm", PaymentMethod.Credit);
        hm.put("insTp", institutionType);
        hm.put("tp1", BillType.PharmacyGrnReturn);
        hm.put("tp2", BillType.PurchaseReturn);
        return getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public List<Bill> getBills(Institution institution, Date frmDate, Date toDate) {
        String sql;
        HashMap hm = new HashMap();
        sql = "Select b From BilledBill b"
                + " where  b.retired=false "
                + " and b.cancelled=false "
                + " and b.paymentMethod=:pm "
                + " and b.createdAt  between :frm and :to"
                + " and ((abs(b.netTotal)-abs(b.paidAmount))> :val) "
                + " and (b.fromInstitution=:ins ) "
                + " and (b.billType=:tp1 or b.billType=:tp2)";
        hm.put("frm", frmDate);
        hm.put("to", toDate);
        hm.put("val", 0.1);
        hm.put("ins", institution);
        hm.put("pm", PaymentMethod.Credit);
        hm.put("tp1", BillType.PharmacyGrnBill);
        hm.put("tp2", BillType.PharmacyPurchaseBill);
        return getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public double getGrnReturnValue(Bill refBill) {
        String sql = "select sum(b.netTotal) from"
                + " Bill b where "
                + " b.retired=false "
                + " and b.paymentMethod=:pm "
                + " and b.referenceBill=:refBill "
                + " and (b.billType=:bType1 or b.billType=:bType2 )";

        HashMap hm = new HashMap();
        hm.put("refBill", refBill);
        hm.put("pm", PaymentMethod.Credit);
        hm.put("bType1", BillType.PharmacyGrnReturn);
        hm.put("bType2", BillType.PurchaseReturn);
        return getBillFacade().findDoubleByJpql(sql, hm, TemporalType.DATE);
    }

    public List<Institution> getDealorFromBills(InstitutionType institutionType) {
        String sql;
        HashMap hm;
        sql = "Select distinct(b.fromInstitution)"
                + " From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and b.paymentMethod=:pm "
                + " and b.fromInstitution.institutionType=:insTp "
                + " and ((abs(b.netTotal)-abs(b.paidAmount))> :val) "
                + " and (b.billType=:tp1 or b.billType=:tp2) "
                + " order by b.fromInstitution.name ";
        hm = new HashMap();
        hm.put("val", 0.1);
        hm.put("pm", PaymentMethod.Credit);
        hm.put("insTp", institutionType);
        hm.put("tp1", BillType.PharmacyPurchaseBill);
        hm.put("tp2", BillType.PharmacyGrnBill);
        return getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public List<Institution> getCreditCompanyFromBills(boolean lessThan) {
        String sql;
        HashMap hm;
        sql = "Select distinct(b.creditCompany) "
                + " From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and b.paymentMethod=:pm "
                + " and b.billType=:tp1 ";

        if (lessThan) {
            sql += " and ((abs(b.netTotal)-abs(b.paidAmount))> :val) ";
        } else {
            sql += " and ((abs(b.netTotal)-abs(b.paidAmount))< :val) ";
        }

        hm = new HashMap();
        hm.put("val", 0.1);
        hm.put("pm", PaymentMethod.Credit);
        hm.put("tp1", BillType.OpdBill);
        return getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);
    }

    public List<Institution> getDealorFromReturnBills() {
        String sql;
        HashMap hm;
        sql = "Select distinct(b.toInstitution) "
                + " From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and b.paymentMethod=:pm "
                + " and (b.billType=:tp1 or b.billType=:tp2)"
                + " order by b.toInstitution.name  ";
        hm = new HashMap();
        hm.put("pm", PaymentMethod.Credit);
        hm.put("tp1", BillType.PharmacyGrnReturn);
        hm.put("tp2", BillType.PurchaseReturn);
        return getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public List<Institution> getDealorFromReturnBills(InstitutionType institutionType) {
        String sql;
        HashMap hm;
        sql = "Select distinct(b.toInstitution) "
                + " From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and b.paymentMethod=:pm "
                + " and b.toInstitution.institutionType=:insTp "
                + " and (b.billType=:tp1 or b.billType=:tp2)"
                + " order by b.toInstitution.name  ";
        hm = new HashMap();
        hm.put("pm", PaymentMethod.Credit);
        hm.put("insTp", institutionType);
        hm.put("tp1", BillType.PharmacyGrnReturn);
        hm.put("tp2", BillType.PurchaseReturn);
        return getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public List<Bill> getBills(Institution institution) {
        String sql;
        HashMap hm = new HashMap();
        sql = "Select b From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false  "
                + " and b.paymentMethod=:pm "
                + " and b.createdAt is not null "
                + " and ((abs(b.netTotal)-abs(b.paidAmount))> :val) "
                + " and (b.fromInstitution=:ins ) "
                + " and (b.billType=:tp1 or b.billType=:tp2)";

        hm.put("val", 0.1);
        hm.put("ins", institution);
        hm.put("pm", PaymentMethod.Credit);
        hm.put("tp1", BillType.PharmacyGrnBill);
        hm.put("tp2", BillType.PharmacyPurchaseBill);
        return getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public List<Bill> getCreditBills(Institution institution, boolean lessThan) {
        String sql;
        HashMap hm = new HashMap();
        sql = "Select b From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and b.paymentMethod=:pm "
                + " and b.createdAt is not null "
                + " and(b.creditCompany=:ins ) "
                + " and b.billType=:tp1";

        if (lessThan) {
            sql += " and ((abs(b.netTotal)-abs(b.paidAmount))> :val) ";
        } else {
            sql += " and ((abs(b.netTotal)-abs(b.paidAmount))< :val) ";
        }

        hm.put("val", 0.1);
        hm.put("ins", institution);
        hm.put("pm", PaymentMethod.Credit);
        hm.put("tp1", BillType.OpdBill);
        return getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }


    public List<Institution> getDealorFromBills(Date frmDate, Date toDate, InstitutionType institutionType) {
        String sql;
        HashMap hm;
        sql = "Select distinct(b.fromInstitution) "
                + " From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and b.paymentMethod=:pm "
                + " and b.fromInstitution.institutionType=:insTp"
                + " and ((abs(b.netTotal)-abs(b.paidAmount))> :val) "
                + " and b.createdAt between :frm and :to "
                + " and (b.billType=:tp1 or b.billType=:tp2) "
                + " order by b.fromInstitution.name ";
        hm = new HashMap();
        hm.put("frm", frmDate);
        hm.put("to", toDate);
        hm.put("val", 0.1);
        hm.put("pm", PaymentMethod.Credit);
        hm.put("insTp", institutionType);
        hm.put("tp1", BillType.PharmacyPurchaseBill);
        hm.put("tp2", BillType.PharmacyGrnBill);
        return getInstitutionFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
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


}
