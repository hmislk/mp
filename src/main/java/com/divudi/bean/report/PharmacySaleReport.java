/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.report;

import com.divudi.bean.SessionController;
import com.divudi.data.BillType;
import com.divudi.data.DepartmentType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.DatedBills;
import com.divudi.data.dataStructure.PharmacyDetail;
import com.divudi.data.dataStructure.PharmacyPaymetMethodSummery;
import com.divudi.data.dataStructure.PharmacySummery;
import com.divudi.data.table.String1Value3;
import com.divudi.data.table.String2Value4;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.PreBill;
import com.divudi.entity.RefundBill;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.ItemsDistributors;
import com.divudi.facade.AmpFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.InstitutionFacade;
import com.divudi.facade.ItemsDistributorsFacade;
import javax.inject.Named;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Named
@RequestScoped
public class PharmacySaleReport implements Serializable {

    private Date fromDate;
    private Date toDate;
    private Department department;
    private Institution institution;
    private double grantNetTotal;
    private double grantTotal;
    private double grantProfessional;
    private double grantDiscount;
    private double grantCashTotal;
    private double grantCreditTotal;
    private double grantCardTotal;
    ///////
    private PharmacySummery billedSummery;
    private PharmacyDetail billedDetail;
    private PharmacyDetail cancelledDetail;
    private PharmacyDetail refundedDetail;
    private PharmacyPaymetMethodSummery billedPaymentSummery;
  //  private List<DatedBills> billDetail;

    List<DistributerWithDestributorItem> distributerWithDestributorItems;
    List<Amp> amps;
    List<Item> items;

    /////
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    AmpFacade ampFacade;
    @EJB
    InstitutionFacade institutionFacade;
    @EJB
    ItemsDistributorsFacade itemsDistributorsFacade;

    public void makeNull() {
        fromDate = null;
        toDate = null;
        department = null;
        grantNetTotal = 0;
        grantDiscount = 0;
        grantCardTotal = 0;
        grantCashTotal = 0;
        grantCreditTotal = 0;
        billedSummery = null;
        billedPaymentSummery = null;

    }

//    private double getSaleValueByDepartment(Date date) {
//        //   List<Stock> billedSummery;
//        Date fd = getCommonFunctions().getStartOfDay(date);
//        Date td = getCommonFunctions().getEndOfDay(date);
//        String sql;
//        Map m = new HashMap();
//        m.put("d", getDepartment());
//        m.put("fd", fd);
//        m.put("td", td);
//        m.put("btp", BillType.PharmacyPre);
//        m.put("refType", BillType.PharmacySale);
//        sql = "select sum(i.netTotal) from Bill i where i.department=:d and i.referenceBill.billType=:refType "
//                + " and i.billType=:btp and i.createdAt between :fd and :td ";
//        double saleValue = getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);
//        ////System.err.println("from " + fromDate);
//        ////System.err.println("Sale Value " + saleValue);
//        return saleValue;
//
//    }
    private double getSaleValueByDepartment(Date date, Bill bill) {

        Date fd = getCommonFunctions().getStartOfDay(date);
        Date td = getCommonFunctions().getEndOfDay(date);
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("fd", fd);
        m.put("td", td);
        m.put("cl", bill.getClass());
        m.put("btp", BillType.PharmacySale);
        sql = "select sum(i.netTotal) from Bill i where i.referenceBill.department=:d "
                + " and i.billType=:btp and type(i)=:cl and i.createdAt between :fd and :td order by i.deptId ";
        double saleValue = getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

        return saleValue;

    }

    @Inject
    private SessionController sessionController;

    private double getHandOverGrossValue(Date date) {

        String sql;

        sql = "select sum(f.total)"
                + " from Bill f "
                + " where f.retired=false "
                + " and f.billType = :billType "
                + " and f.createdAt between :fd and :td "
                + " and( f.paymentMethod=:pm1"
                + " or f.paymentMethod=:pm2"
                + " or f.paymentMethod=:pm3"
                + " or f.paymentMethod=:pm4 )"
                + " and f.toInstitution=:ins "
                + " and f.institution=:billedIns ";

        Date fd = getCommonFunctions().getStartOfDay(date);
        Date td = getCommonFunctions().getEndOfDay(date);

        //System.err.println("From " + fd);
        //System.err.println("To " + td);
        Map m = new HashMap();
        m.put("fd", fd);
        m.put("td", td);
        m.put("pm1", PaymentMethod.Cash);
        m.put("pm2", PaymentMethod.Card);
        m.put("pm3", PaymentMethod.Cheque);
        m.put("pm4", PaymentMethod.Slip);
        m.put("billType", BillType.OpdBill);
        m.put("ins", institution);
        m.put("billedIns", getSessionController().getInstitution());
        double saleValue = getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

        return saleValue;

    }

    private double getHandOverDiscountValue(Date date) {

        String sql;

        sql = "select sum(f.discount)"
                + " from Bill f "
                + " where f.retired=false "
                + " and f.billType = :billType "
                + " and f.createdAt between :fd and :td "
                + " and( f.paymentMethod=:pm1"
                + " or f.paymentMethod=:pm2"
                + " or f.paymentMethod=:pm3"
                + " or f.paymentMethod=:pm4 )"
                + " and f.toInstitution=:ins "
                + " and f.institution=:billedIns ";

        Date fd = getCommonFunctions().getStartOfDay(date);
        Date td = getCommonFunctions().getEndOfDay(date);

        Map m = new HashMap();
        m.put("fd", fd);
        m.put("td", td);
        m.put("pm1", PaymentMethod.Cash);
        m.put("pm2", PaymentMethod.Card);
        m.put("pm3", PaymentMethod.Cheque);
        m.put("pm4", PaymentMethod.Slip);
        m.put("billType", BillType.OpdBill);
        m.put("ins", institution);
        m.put("billedIns", getSessionController().getInstitution());

        double saleValue = getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

        return saleValue;

    }

    private double getHandOverProfValue(Date date) {

        String sql;

        sql = "select sum(f.staffFee)"
                + " from Bill f "
                + " where f.retired=false "
                + " and f.billType = :billType "
                + " and f.createdAt between :fd and :td "
                + " and( f.paymentMethod=:pm1"
                + " or f.paymentMethod=:pm2"
                + " or f.paymentMethod=:pm3"
                + " or f.paymentMethod=:pm4 )"
                + " and f.toInstitution=:ins "
                + " and f.institution=:billedIns ";

        Date fd = getCommonFunctions().getStartOfDay(date);
        Date td = getCommonFunctions().getEndOfDay(date);

        Map m = new HashMap();
        m.put("fd", fd);
        m.put("td", td);
        m.put("pm1", PaymentMethod.Cash);
        m.put("pm2", PaymentMethod.Card);
        m.put("pm3", PaymentMethod.Cheque);
        m.put("pm4", PaymentMethod.Slip);
        m.put("billType", BillType.OpdBill);
        m.put("ins", institution);
        m.put("billedIns", getSessionController().getInstitution());

        double saleValue = getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

        return saleValue;

    }

    private double getSaleValueByDepartment(Date date, PaymentMethod paymentMethod, Bill bill) {
        //   List<Stock> billedSummery;
        Date fd = getCommonFunctions().getStartOfDay(date);
        Date td = getCommonFunctions().getEndOfDay(date);
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("fd", fd);
        m.put("td", td);
        m.put("pm", paymentMethod);
        m.put("class", bill.getClass());
        //  m.put("btp", BillType.PharmacyPre);
        m.put("btp", BillType.PharmacySale);
        sql = "select sum(i.netTotal) from Bill i where i.paymentMethod=:pm and "
                + " i.referenceBill.department=:d and type(i)=:class "
                + " and i.billType=:btp and i.createdAt between :fd and :td order by i.deptId ";
        double saleValue = getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);
        //   ////System.err.println("from " + fromDate);
        //  ////System.err.println("Sale Value " + saleValue);
        return saleValue;

    }

    private double getSaleValuePaymentmethod(Date date, PaymentMethod paymentMethod, Bill bill) {
        //   List<Stock> billedSummery;
        Date fd = getCommonFunctions().getStartOfDay(date);
        Date td = getCommonFunctions().getEndOfDay(date);
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("fd", fd);
        m.put("td", td);
        m.put("pm", paymentMethod);
        m.put("class", bill.getClass());
        m.put("btp", BillType.PharmacySale);
        sql = "select sum(i.netTotal) from Bill i where type(i)=:class and i.paymentMethod=:pm and "
                + " i.referenceBill.department=:d and i.billType=:btp and i.createdAt between :fd and :td ";
        double saleValue = getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

        return saleValue;

    }
//
//    private double getDiscountValueByDepartment(Date date) {
//        //   List<Stock> billedSummery;
//        Date fd = getCommonFunctions().getStartOfDay(date);
//        Date td = getCommonFunctions().getEndOfDay(date);
//        String sql;
//        Map m = new HashMap();
//        m.put("d", getDepartment());
//        m.put("fd", fd);
//        m.put("td", td);
//        m.put("btp", BillType.PharmacyPre);
//        m.put("refType", BillType.PharmacySale);
//        sql = "select sum(i.discount) from Bill i where i.department=:d and i.referenceBill.billType=:refType "
//                + " and i.billType=:btp and i.createdAt between :fd and :td ";
//        double saleValue = getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);
//        ////System.err.println("from " + fromDate);
//        ////System.err.println("Sale Value " + saleValue);
//        return saleValue;
//
//    }

    private double getDiscountValueByDepartment(Date date, Bill bill) {

        Date fd = getCommonFunctions().getStartOfDay(date);
        Date td = getCommonFunctions().getEndOfDay(date);
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("fd", fd);
        m.put("td", td);
        m.put("cl", bill.getClass());
        m.put("btp", BillType.PharmacySale);
        sql = "select sum(i.discount) from Bill i where i.referenceBill.department=:d "
                + " and i.billType=:btp and type(i)=:cl and i.createdAt between :fd and :td ";
        double saleValue = getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

        return saleValue;

    }

    private List<Bill> getSaleBillByDepartment(Date date, Bill bill) {
        //   List<Stock> billedSummery;
        Date fd = getCommonFunctions().getStartOfDay(date);
        Date td = getCommonFunctions().getEndOfDay(date);
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("fd", fd);
        m.put("td", td);
        // m.put("btp", BillType.PharmacyPre);
        m.put("class", bill.getClass());
        m.put("btp", BillType.PharmacySale);
        sql = "select i from Bill i where i.referenceBill.department=:d  "
                + " and i.billType=:btp and type(i)=:class and"
                + " i.createdAt between :fd and :td order by i.deptId ";
        return getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantNetTotalByDepartment() {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("cl", PreBill.class);
        m.put("btp", BillType.PharmacySale);

        sql = "select sum(i.netTotal) from Bill i where i.referenceBill.department=:d "
                + " and i.billType=:btp and type(i)!=:cl and i.createdAt between :fd and :td ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantNetTotalByDepartment(Bill bill) {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("class", bill.getClass());
        // m.put("btp", BillType.PharmacyPre);
        m.put("btp", BillType.PharmacySale);
        sql = "select sum(i.netTotal) from Bill i where i.referenceBill.department=:d and"
                + " i.billType=:btp and type(i)=:class "
                + " and i.createdAt between :fromDate and :toDate ";
        return getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantHandOverNetotal(Bill bill) {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("ins", getInstitution());
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("class", bill.getClass());
        m.put("pm1", PaymentMethod.Cash);
        m.put("pm2", PaymentMethod.Card);
        m.put("pm3", PaymentMethod.Cheque);
        m.put("pm4", PaymentMethod.Slip);
        m.put("btp", BillType.OpdBill);
        sql = "select sum(abs(i.total) - (abs(i.staffFee) + abs(i.discount))) from "
                + " Bill i where i.toInstitution=:ins "
                + " and i.billType=:btp"
                + " and type(i)=:class "
                + " and( i.paymentMethod=:pm1 "
                + " or i.paymentMethod=:pm2 "
                + " or i.paymentMethod=:pm3 "
                + " or i.paymentMethod=:pm4 )"
                + " and i.createdAt between :fromDate and :toDate ";
        return getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantHandOverNetotal() {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("ins", getInstitution());
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("pm1", PaymentMethod.Cash);
        m.put("pm2", PaymentMethod.Card);
        m.put("pm3", PaymentMethod.Cheque);
        m.put("pm4", PaymentMethod.Slip);
        m.put("btp", BillType.OpdBill);
        sql = "select sum(abs(i.netTotal) - abs(i.staffFee))  "
                + " from Bill i where "
                + " i.toInstitution=:ins "
                + " and i.billType=:btp "
                + " and (i.paymentMethod=:pm1 "
                + " or i.paymentMethod=:pm2 "
                + " or i.paymentMethod=:pm3 "
                + " or i.paymentMethod=:pm4 )"
                + " and i.createdAt between :fromDate and :toDate ";
        return getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantHandOverTotal() {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("ins", getInstitution());
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("pm1", PaymentMethod.Cash);
        m.put("pm2", PaymentMethod.Card);
        m.put("pm3", PaymentMethod.Cheque);
        m.put("pm4", PaymentMethod.Slip);
        m.put("btp", BillType.OpdBill);
        m.put("billedIns", getSessionController().getInstitution());
        sql = "select sum(i.total)  "
                + " from Bill i where "
                + " i.toInstitution=:ins "
                + " and i.institution=:billedIns "
                + " and i.billType=:btp "
                + " and (i.paymentMethod=:pm1 "
                + " or i.paymentMethod=:pm2 "
                + " or i.paymentMethod=:pm3 "
                + " or i.paymentMethod=:pm4 )"
                + " and i.createdAt between :fromDate and :toDate ";
        return getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantHandOverProf() {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("ins", getInstitution());
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("pm1", PaymentMethod.Cash);
        m.put("pm2", PaymentMethod.Card);
        m.put("pm3", PaymentMethod.Cheque);
        m.put("pm4", PaymentMethod.Slip);
        m.put("btp", BillType.OpdBill);
        m.put("billedIns", getSessionController().getInstitution());
        sql = "select sum(i.staffFee)  "
                + " from Bill i where "
                + " i.toInstitution=:ins "
                + " and i.institution=:billedIns "
                + " and i.billType=:btp "
                + " and (i.paymentMethod=:pm1 "
                + " or i.paymentMethod=:pm2 "
                + " or i.paymentMethod=:pm3 "
                + " or i.paymentMethod=:pm4 )"
                + " and i.createdAt between :fromDate and :toDate ";
        return getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantHandOverDiscount() {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("ins", getInstitution());
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("pm1", PaymentMethod.Cash);
        m.put("pm2", PaymentMethod.Card);
        m.put("pm3", PaymentMethod.Cheque);
        m.put("pm4", PaymentMethod.Slip);
        m.put("btp", BillType.OpdBill);
        m.put("billedIns", getSessionController().getInstitution());
        sql = "select sum(i.discount)  "
                + " from Bill i where "
                + " i.toInstitution=:ins "
                + " and i.institution=:billedIns "
                + " and i.billType=:btp "
                + " and (i.paymentMethod=:pm1 "
                + " or i.paymentMethod=:pm2 "
                + " or i.paymentMethod=:pm3 "
                + " or i.paymentMethod=:pm4 )"
                + " and i.createdAt between :fromDate and :toDate ";
        return getBillFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantTotalByPaymentMethod(PaymentMethod paymentMethod, Bill bill) {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("pm", paymentMethod);
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("class", bill.getClass());
        m.put("btp", BillType.PharmacySale);
        sql = "select sum(i.netTotal) from Bill i where type(i)=:class and i.paymentMethod=:pm and "
                + " i.referenceBill.department=:d and i.billType=:btp and i.createdAt between :fromDate and :toDate ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantTotalByPaymentMethod(PaymentMethod paymentMethod) {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("pm", paymentMethod);
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("class", PreBill.class);
        m.put("btp", BillType.PharmacySale);
        sql = "select sum(i.netTotal) from Bill i where type(i)!=:class and i.paymentMethod=:pm and "
                + " i.referenceBill.department=:d and i.billType=:btp and i.createdAt between :fromDate and :toDate ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantDiscountByDepartment(Bill bill) {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("btp", BillType.PharmacySale);
        m.put("class", bill.getClass());
        sql = "select sum(i.discount) from Bill i where type(i)=:class and"
                + " i.referenceBill.department=:d and  "
                + " i.billType=:btp and i.createdAt between :fromDate and :toDate ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double calGrantDiscountByDepartment() {
        //   List<Stock> billedSummery;
        String sql;
        Map m = new HashMap();
        m.put("d", getDepartment());
        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("btp", BillType.PharmacySale);
        m.put("class", PreBill.class);
        sql = "select sum(i.discount) from Bill i where type(i)!=:class and"
                + " i.referenceBill.department=:d and "
                + " i.billType=:btp and i.createdAt between :fromDate and :toDate ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public void createSaleReportByDate() {
        billedSummery = new PharmacySummery();

        billedSummery.setBills(new ArrayList<String1Value3>());

        Date nowDate = getFromDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(nowDate);

        while (nowDate.before(getToDate())) {

            DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
            String formattedDate = df.format(nowDate);

            String1Value3 newRow = new String1Value3();
            newRow.setString(formattedDate);
            newRow.setValue1(getSaleValueByDepartment(nowDate, new BilledBill()));
            newRow.setValue2(getSaleValueByDepartment(nowDate, new CancelledBill()));
            newRow.setValue3(getSaleValueByDepartment(nowDate, new RefundBill()));

            billedSummery.getBills().add(newRow);

            Calendar nc = Calendar.getInstance();
            nc.setTime(nowDate);
            nc.add(Calendar.DATE, 1);
            nowDate = nc.getTime();

        }

        billedSummery.setBilledTotal(calGrantNetTotalByDepartment(new BilledBill()));
        billedSummery.setCancelledTotal(calGrantNetTotalByDepartment(new CancelledBill()));
        billedSummery.setRefundedTotal(calGrantNetTotalByDepartment(new RefundBill()));

        grantNetTotal = calGrantNetTotalByDepartment();

    }

    public void createLabHadnOverReportByDate() {
        billedSummery = new PharmacySummery();

        billedSummery.setBills(new ArrayList<String1Value3>());

        Date nowDate = getFromDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(nowDate);

        while (nowDate.before(getToDate())) {

            DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
            String formattedDate = df.format(nowDate);

            String1Value3 newRow = new String1Value3();
            newRow.setString(formattedDate);
            newRow.setValue1(getHandOverGrossValue(nowDate));
            newRow.setValue2(getHandOverDiscountValue(nowDate));
            newRow.setValue3(getHandOverProfValue(nowDate));

            billedSummery.getBills().add(newRow);

            Calendar nc = Calendar.getInstance();
            nc.setTime(nowDate);
            nc.add(Calendar.DATE, 1);
            nowDate = nc.getTime();

        }

        billedSummery.setBilledTotal(calGrantHandOverTotal());
        billedSummery.setCancelledTotal(calGrantHandOverDiscount());
        billedSummery.setRefundedTotal(calGrantHandOverProf());

    }

    public void createSalePaymentMethod() {
        billedPaymentSummery = new PharmacyPaymetMethodSummery();
        billedPaymentSummery.setBills(new ArrayList<String2Value4>());

        Date nowDate = getFromDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(nowDate);

        while (nowDate.before(getToDate())) {

            String2Value4 newRow = new String2Value4();

            DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
            String formattedDate = df.format(nowDate);

            newRow.setString(formattedDate);

            double cash = 0;
            double credit = 0;
            double card = 0;

            ////////
            cash = getSaleValuePaymentmethod(nowDate, PaymentMethod.Cash, new BilledBill());
            cash += getSaleValuePaymentmethod(nowDate, PaymentMethod.Cash, new CancelledBill());
            cash += getSaleValuePaymentmethod(nowDate, PaymentMethod.Cash, new RefundBill());
            /////////////
            credit = getSaleValuePaymentmethod(nowDate, PaymentMethod.Credit, new BilledBill());
            credit += getSaleValuePaymentmethod(nowDate, PaymentMethod.Credit, new CancelledBill());
            credit += getSaleValuePaymentmethod(nowDate, PaymentMethod.Credit, new RefundBill());

            //////////////
            card = getSaleValuePaymentmethod(nowDate, PaymentMethod.Card, new BilledBill());
            card += getSaleValuePaymentmethod(nowDate, PaymentMethod.Card, new CancelledBill());
            card += getSaleValuePaymentmethod(nowDate, PaymentMethod.Card, new RefundBill());

            newRow.setValue1(cash);
            newRow.setValue2(credit);
            newRow.setValue3(card);
            newRow.setValue4(cash + credit + card);

            billedPaymentSummery.getBills().add(newRow);

            Calendar nc = Calendar.getInstance();
            nc.setTime(nowDate);
            nc.add(Calendar.DATE, 1);
            nowDate = nc.getTime();

        }

        billedPaymentSummery.setCashTotal(
                calGrantTotalByPaymentMethod(PaymentMethod.Cash, new BilledBill())
                + calGrantTotalByPaymentMethod(PaymentMethod.Cash, new CancelledBill())
                + calGrantTotalByPaymentMethod(PaymentMethod.Cash, new RefundBill()));

        ////////////
        billedPaymentSummery.setCreditTotal(
                calGrantTotalByPaymentMethod(PaymentMethod.Credit, new BilledBill())
                + calGrantTotalByPaymentMethod(PaymentMethod.Credit, new CancelledBill())
                + calGrantTotalByPaymentMethod(PaymentMethod.Credit, new RefundBill()));

        ////////////////
        billedPaymentSummery.setCardTotal(
                calGrantTotalByPaymentMethod(PaymentMethod.Card, new BilledBill())
                + calGrantTotalByPaymentMethod(PaymentMethod.Card, new CancelledBill())
                + calGrantTotalByPaymentMethod(PaymentMethod.Card, new RefundBill()));

        grantCardTotal = calGrantTotalByPaymentMethod(PaymentMethod.Card);
        grantCashTotal = calGrantTotalByPaymentMethod(PaymentMethod.Cash);
        grantCreditTotal = calGrantTotalByPaymentMethod(PaymentMethod.Credit);
    }

    public void createSaleReportByDateDetail() {
        billedDetail = new PharmacyDetail();
        cancelledDetail = new PharmacyDetail();
        refundedDetail = new PharmacyDetail();

        billedDetail.setDatedBills(new ArrayList<DatedBills>());
        cancelledDetail.setDatedBills(new ArrayList<DatedBills>());
        refundedDetail.setDatedBills(new ArrayList<DatedBills>());

        Date nowDate = getFromDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(nowDate);
        while (nowDate.before(getToDate())) {

            double sumNetToal = getSaleValueByDepartment(nowDate, new BilledBill());
            double sumDiscount = getDiscountValueByDepartment(nowDate, new BilledBill());
            DatedBills newRow = new DatedBills();
            newRow.setDate(nowDate);
            newRow.setSumNetTotal(sumNetToal);
            newRow.setSumDiscount(sumDiscount);
            newRow.setBills(getSaleBillByDepartment(nowDate, new BilledBill()));

            if (!newRow.getBills().isEmpty()) {
                billedDetail.getDatedBills().add(newRow);
            }

            sumNetToal = getSaleValueByDepartment(nowDate, new CancelledBill());
            sumDiscount = getDiscountValueByDepartment(nowDate, new CancelledBill());
            newRow = new DatedBills();
            newRow.setDate(nowDate);
            newRow.setSumNetTotal(sumNetToal);
            newRow.setSumDiscount(sumDiscount);
            newRow.setBills(getSaleBillByDepartment(nowDate, new CancelledBill()));

            if (!newRow.getBills().isEmpty()) {
                cancelledDetail.getDatedBills().add(newRow);
            }

            sumNetToal = getSaleValueByDepartment(nowDate, new RefundBill());
            sumDiscount = getDiscountValueByDepartment(nowDate, new RefundBill());
            newRow = new DatedBills();
            newRow.setDate(nowDate);
            newRow.setSumNetTotal(sumNetToal);
            newRow.setSumDiscount(sumDiscount);
            newRow.setBills(getSaleBillByDepartment(nowDate, new RefundBill()));

            if (!newRow.getBills().isEmpty()) {
                refundedDetail.getDatedBills().add(newRow);
            }

            Calendar nc = Calendar.getInstance();
            nc.setTime(nowDate);
            nc.add(Calendar.DATE, 1);
            nowDate = nc.getTime();

        }

        billedDetail.setNetTotal(calGrantNetTotalByDepartment(new BilledBill()));
        billedDetail.setDiscount(calGrantDiscountByDepartment(new BilledBill()));

        cancelledDetail.setNetTotal(calGrantNetTotalByDepartment(new CancelledBill()));
        cancelledDetail.setDiscount(calGrantDiscountByDepartment(new CancelledBill()));

        refundedDetail.setNetTotal(calGrantNetTotalByDepartment(new RefundBill()));
        refundedDetail.setDiscount(calGrantDiscountByDepartment(new RefundBill()));

        grantNetTotal = calGrantNetTotalByDepartment();
        grantDiscount = calGrantDiscountByDepartment();

    }

    public void createSalePaymentMethodDetail() {
        billedDetail = new PharmacyDetail();
        cancelledDetail = new PharmacyDetail();
        refundedDetail = new PharmacyDetail();

        billedDetail.setDatedBills(new ArrayList<DatedBills>());
        cancelledDetail.setDatedBills(new ArrayList<DatedBills>());
        refundedDetail.setDatedBills(new ArrayList<DatedBills>());

        Date nowDate = getFromDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(nowDate);
        while (nowDate.before(getToDate())) {

            double sumCash = getSaleValueByDepartment(nowDate, PaymentMethod.Cash, new BilledBill());
            double sumCredit = getSaleValueByDepartment(nowDate, PaymentMethod.Credit, new BilledBill());
            double sumCard = getSaleValueByDepartment(nowDate, PaymentMethod.Card, new BilledBill());
            double sumDiscount = getDiscountValueByDepartment(nowDate, new BilledBill());
            DatedBills newRow = new DatedBills();
            newRow.setDate(nowDate);
            newRow.setSumCashTotal(sumCash);
            newRow.setSumCreditTotal(sumCredit);
            newRow.setSumCardTotal(sumCard);
            newRow.setSumDiscount(sumDiscount);
            newRow.setBills(getSaleBillByDepartment(nowDate, new BilledBill()));

            if (!newRow.getBills().isEmpty()) {
                billedDetail.getDatedBills().add(newRow);
            }

            ///
            sumCash = getSaleValueByDepartment(nowDate, PaymentMethod.Cash, new CancelledBill());
            sumCredit = getSaleValueByDepartment(nowDate, PaymentMethod.Credit, new CancelledBill());
            sumCard = getSaleValueByDepartment(nowDate, PaymentMethod.Card, new CancelledBill());
            sumDiscount = getDiscountValueByDepartment(nowDate, new CancelledBill());
            newRow = new DatedBills();
            newRow.setDate(nowDate);
            newRow.setSumCashTotal(sumCash);
            newRow.setSumCreditTotal(sumCredit);
            newRow.setSumCardTotal(sumCard);
            newRow.setSumDiscount(sumDiscount);
            newRow.setBills(getSaleBillByDepartment(nowDate, new CancelledBill()));

            if (!newRow.getBills().isEmpty()) {
                cancelledDetail.getDatedBills().add(newRow);
            }

            ///
            sumCash = getSaleValueByDepartment(nowDate, PaymentMethod.Cash, new RefundBill());
            sumCredit = getSaleValueByDepartment(nowDate, PaymentMethod.Credit, new RefundBill());
            sumCard = getSaleValueByDepartment(nowDate, PaymentMethod.Card, new RefundBill());
            sumDiscount = getDiscountValueByDepartment(nowDate, new RefundBill());
            newRow = new DatedBills();
            newRow.setDate(nowDate);
            newRow.setSumCashTotal(sumCash);
            newRow.setSumCreditTotal(sumCredit);
            newRow.setSumCardTotal(sumCard);
            newRow.setSumDiscount(sumDiscount);
            newRow.setBills(getSaleBillByDepartment(nowDate, new RefundBill()));

            if (!newRow.getBills().isEmpty()) {
                refundedDetail.getDatedBills().add(newRow);
            }

            Calendar nc = Calendar.getInstance();
            nc.setTime(nowDate);
            nc.add(Calendar.DATE, 1);
            nowDate = nc.getTime();

        }

        billedDetail.setDiscount(calGrantDiscountByDepartment(new BilledBill()));
        billedDetail.setCashTotal(calGrantTotalByPaymentMethod(PaymentMethod.Cash, new BilledBill()));
        billedDetail.setCreditTotal(calGrantTotalByPaymentMethod(PaymentMethod.Credit, new BilledBill()));
        billedDetail.setCardTotal(calGrantTotalByPaymentMethod(PaymentMethod.Card, new BilledBill()));

        cancelledDetail.setDiscount(calGrantDiscountByDepartment(new CancelledBill()));
        cancelledDetail.setCashTotal(calGrantTotalByPaymentMethod(PaymentMethod.Cash, new CancelledBill()));
        cancelledDetail.setCreditTotal(calGrantTotalByPaymentMethod(PaymentMethod.Credit, new CancelledBill()));
        cancelledDetail.setCardTotal(calGrantTotalByPaymentMethod(PaymentMethod.Card, new CancelledBill()));

        refundedDetail.setDiscount(calGrantDiscountByDepartment(new RefundBill()));
        refundedDetail.setCashTotal(calGrantTotalByPaymentMethod(PaymentMethod.Cash, new RefundBill()));
        refundedDetail.setCreditTotal(calGrantTotalByPaymentMethod(PaymentMethod.Credit, new RefundBill()));
        refundedDetail.setCardTotal(calGrantTotalByPaymentMethod(PaymentMethod.Card, new RefundBill()));

        grantCardTotal = calGrantTotalByPaymentMethod(PaymentMethod.Card);
        grantCashTotal = calGrantTotalByPaymentMethod(PaymentMethod.Cash);
        grantCreditTotal = calGrantTotalByPaymentMethod(PaymentMethod.Credit);
        grantDiscount = calGrantDiscountByDepartment();

    }

    public void createItemListWithOutItemDistributer() {
        List<Amp> allAmps = getAllPharmacyItems();
        //System.out.println("allAmps = " + allAmps.size());
        List<Amp> ampsWithDealor = getAllDealorItems();
        //System.out.println("ampsWithOutDealor = " + ampsWithDealor.size());
        allAmps.removeAll(ampsWithDealor);
        //System.out.println("After remove allAmps = " + allAmps.size());
        amps = new ArrayList<>();
        amps.addAll(allAmps);
        //System.out.println("amps = " + amps.size());
    }

    public void createItemsDistributersWithDistributer() {
        distributerWithDestributorItems = new ArrayList<>();
        List<Institution> distributors = getAllDealors();
        System.out.println("distributors.size() = " + distributors.size());
        for (Institution distributor : distributors) {
            DistributerWithDestributorItem dwdi = new DistributerWithDestributorItem();
            System.out.println("distributor = " + distributor.getName());
            List<ItemsDistributors> list = getAllDealorItems(distributor);
            System.out.println("list.size() = " + list.size());
            if (list.size() > 0) {
                dwdi.setDistributor(distributor);
                dwdi.setItemsDistributors(list);
                distributerWithDestributorItems.add(dwdi);
            }
        }
        System.out.println("distributerWithDestributorItems.size() = " + distributerWithDestributorItems.size());
    }

    public void createItemListOneItemHasGreterThanOneDistributor() {
        List<Object[]> objs = getAllDealorItemsWithCount();
        //System.out.println("objs = " + objs);
        //System.out.println("objs = " + objs.size());
        amps = new ArrayList<>();
        for (Object[] obj : objs) {
            //System.out.println("obj = " + obj);
            if (obj != null) {
                Amp item = (Amp) obj[0];
                //System.out.println("item = " + item.getName());
                long count = (long) obj[1];
                //System.out.println("count = " + count);
                if (count > 1) {
                    //System.out.println("****Add****");
                    amps.add(item);
                }
            }
        }
        //System.out.println("items = " + amps.size());

    }

    public void createItemListOneItemHasGreterThanOneDistributorOther() {
        List<Amp> ampsWithDealor = getAllDealorItems();
        //System.out.println("ampsWithDealor = " + ampsWithDealor.size());

        items = new ArrayList<>();
        for (Item i : ampsWithDealor) {
            System.err.println("in");
            //System.out.println("item = " + i.getName());
            List<Amp> allAmps = getAmpItems(i);
            //System.out.println("amps = " + allAmps.size());
            int count = 0;
            if (allAmps != null) {
                count = allAmps.size();
            }
            //System.out.println("count = " + count);
            if (count > 1) {
                //System.out.println("****Add****");
                items.add(i);
            }
            System.err.println("out");
        }
        //System.out.println("items = " + items.size());

    }

    public List<Amp> getAllPharmacyItems() {
        String sql;
        sql = "select c from Amp c "
                + " where c.retired=false order by c.name ";

        return ampFacade.findBySQL(sql);
    }

    public List<Amp> getAllDealorItems() {
        String sql;

        sql = "SELECT distinct(i.item) FROM ItemsDistributors i "
                + " where i.retired=false "
                + " and i.item.retired=false "
                + " order by i.item.name ";

        return ampFacade.findBySQL(sql);
    }

    public List<Object[]> getAllDealorItemsWithCount() {
        String sql;

        sql = "SELECT distinct(i.item),count(i.item) FROM ItemsDistributors i "
                + " where i.retired=false "
                + " and i.item.retired=false "
                + " order by i.item.name ";

        return ampFacade.findAggregates(sql);
    }

    public List<Amp> getAmpItems(Item a) {
        String sql;
        Map m = new HashMap();

        sql = "SELECT i.item FROM ItemsDistributors i "
                + " where i.retired=false "
                + " and i.item.retired=false "
                + " and i.item=:a ";

        m.put("a", a);
        return ampFacade.findBySQL(sql, m);
    }

    public List<Institution> getAllDealors() {
        String sql;

        sql = "SELECT distinct(i.institution) FROM ItemsDistributors i "
                + " where i.retired=false "
                + " order by i.institution.name ";

        return institutionFacade.findBySQL(sql);
    }

    public List<ItemsDistributors> getAllDealorItems(Institution ins) {
        String sql;
        Map m = new HashMap();

        sql = "SELECT i FROM ItemsDistributors i "
                + " where i.retired=false "
                + " and i.institution=:ins "
                + " order by i.item.name ";

        m.put("ins", ins);

        return itemsDistributorsFacade.findBySQL(sql, m);
    }

    /**
     * Creates a new instance of PharmacySaleReport
     */
    public PharmacySaleReport() {
    }

    public class DistributerWithDestributorItem {

        Institution distributor;
        List<ItemsDistributors> itemsDistributors;

        public Institution getDistributor() {
            return distributor;
        }

        public void setDistributor(Institution distributor) {
            this.distributor = distributor;
        }

        public List<ItemsDistributors> getItemsDistributors() {
            return itemsDistributors;
        }

        public void setItemsDistributors(List<ItemsDistributors> itemsDistributors) {
            this.itemsDistributors = itemsDistributors;
        }

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
            toDate = getCommonFunctions().getEndOfMonth(new Date());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public double getGrantNetTotal() {
        return grantNetTotal;
    }

    public void setGrantNetTotal(double grantNetTotal) {
        this.grantNetTotal = grantNetTotal;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public double getGrantDiscount() {
        return grantDiscount;
    }

    public void setGrantDiscount(double grantDiscount) {
        this.grantDiscount = grantDiscount;
    }

    public PharmacySummery getBilledSummery() {
        return billedSummery;
    }

    public void setBilledSummery(PharmacySummery billedSummery) {
        this.billedSummery = billedSummery;
    }

    public PharmacyPaymetMethodSummery getBilledPaymentSummery() {
        return billedPaymentSummery;
    }

    public void setBilledPaymentSummery(PharmacyPaymetMethodSummery billedPaymentSummery) {
        this.billedPaymentSummery = billedPaymentSummery;
    }

    public double getGrantCashTotal() {
        return grantCashTotal;
    }

    public void setGrantCashTotal(double grantCashTotal) {
        this.grantCashTotal = grantCashTotal;
    }

    public double getGrantCreditTotal() {
        return grantCreditTotal;
    }

    public void setGrantCreditTotal(double grantCreditTotal) {
        this.grantCreditTotal = grantCreditTotal;
    }

    public double getGrantCardTotal() {
        return grantCardTotal;
    }

    public void setGrantCardTotal(double grantCardTotal) {
        this.grantCardTotal = grantCardTotal;
    }

    public PharmacyDetail getBilledDetail() {
        return billedDetail;
    }

    public void setBilledDetail(PharmacyDetail billedDetail) {
        this.billedDetail = billedDetail;
    }

    public PharmacyDetail getCancelledDetail() {
        return cancelledDetail;
    }

    public void setCancelledDetail(PharmacyDetail cancelledDetail) {
        this.cancelledDetail = cancelledDetail;
    }

    public PharmacyDetail getRefundedDetail() {
        return refundedDetail;
    }

    public void setRefundedDetail(PharmacyDetail refundedDetail) {
        this.refundedDetail = refundedDetail;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public double getGrantTotal() {
        return grantTotal;
    }

    public void setGrantTotal(double grantTotal) {
        this.grantTotal = grantTotal;
    }

    public double getGrantProfessional() {
        return grantProfessional;
    }

    public void setGrantProfessional(double grantProfessional) {
        this.grantProfessional = grantProfessional;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public List<DistributerWithDestributorItem> getDistributerWithDestributorItems() {
        return distributerWithDestributorItems;
    }

    public void setDistributerWithDestributorItems(List<DistributerWithDestributorItem> distributerWithDestributorItems) {
        this.distributerWithDestributorItems = distributerWithDestributorItems;
    }

    public List<Amp> getAmps() {
        return amps;
    }

    public void setAmps(List<Amp> amps) {
        this.amps = amps;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
    
}
