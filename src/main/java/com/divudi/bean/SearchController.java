/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import com.divudi.data.BillType;
import com.divudi.data.dataStructure.SearchKeyword;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.PreBill;
import com.divudi.entity.lab.PatientInvestigation;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PatientInvestigationFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
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
public class SearchController implements Serializable {

    private SearchKeyword searchKeyword;
    Date fromDate;
    Date toDate;
    private int maxResult = 50;
    private BillType billType;
    ////////////
    private List<Bill> bills;
    private List<Bill> selectedBills;
    private List<BillFee> billFees;
    private List<BillItem> billItems;
    private List<PatientInvestigation> patientInvestigations;
    private List<PatientInvestigation> patientInvestigationsSigle;
    ////////////
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PatientInvestigationFacade patientInvestigationFacade;
    @EJB
    private BillBean billBean;
    @EJB
    private PharmacyBean pharmacyBean;
    //////////
    @Inject
    private SessionController sessionController;
    @Inject
    TransferController transferController;

    public void makeListNull() {
        maxResult = 50;
        bills = null;
        selectedBills = null;
        billFees = null;
        billItems = null;
        patientInvestigations = null;
    }

    public void createPreRefundTable() {

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from RefundBill b where b.billType = :billType "
                + " and b.institution=:ins and b.cancelled=false "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false and b.deptId is not null ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.PharmacyPre);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createReturnSaleBills() {

        Map m = new HashMap();
        m.put("bt", BillType.PharmacyPre);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from RefundBill b where  b.retired=false and b.institution=:ins and"
                + " b.createdAt between :fd and :td and b.billType=:bt ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.billedBill.deptId) like :rNo )";
            m.put("rNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyTable() {

        Map m = new HashMap();
        m.put("bt", BillType.PharmacyPre);
        m.put("rBt", BillType.PharmacySale);
        m
                .put("class", PreBill.class
                );
        m.put(
                "rClass", BilledBill.class
        );
        m.put(
                "fd", getFromDate());
        m.put(
                "td", getToDate());
        m.put(
                "ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from Bill b where b.retired=false and b.createdAt  "
                + " between :fd and :td and b.billType=:bt and b.institution=:ins and"
                + " b.referenceBill.billType=:rBt and type(b)=:class and type(b.referenceBill)=:rClass ";

        if (getSearchKeyword()
                .getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword()
                .getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword()
                .getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :dep )";
            m.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword()
                .getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword()
                .getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        //     //System.out.println("sql = " + sql);
        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createIssueTable() {
        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.PharmacyTransferIssue);
        sql = "Select b From BilledBill b where b.retired=false and "
                + " b.toDepartment=:dep and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :fDep )";
            tmp.put("fDep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setTmpRefBill(getRefBill(b));

        }

    }

    private Bill getRefBill(Bill b) {
        String sql = "Select b From Bill b where b.retired=false "
                + " and b.cancelled=false and b.billType=:btp and "
                + " b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyTransferReceive);
        return getBillFacade().findFirstBySQL(sql, hm);
    }

    public void makeNull() {
        searchKeyword = null;
        bills = null;

    }

    public void createTableByBillType() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where b.retired=false and "
                + " (type(b)=:class1 or type(b)=:class2) "
                + " and b.department=:dep and b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  (upper(b.toInstitution.name) like :toIns )";
            temMap.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.deptId) like :refId )";
            temMap.put("refId", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :inv )";
            temMap.put("inv", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " (upper(bItem.item.name) like :itm ))";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }
        
          if (getSearchKeyword().getCode()!= null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " (upper(bItem.item.code) like :cde ))";
            temMap.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("class1", BilledBill.class);
        temMap.put("class2", PreBill.class);
        temMap.put("billType", billType);
        temMap.put("dep", getSessionController().getDepartment());
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        //temMap.put("dep", getSessionController().getDepartment());
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, maxResult);
        //     //System.err.println("SIZE : " + lst.size());

    }

    public void createTableByBillTypeAllDepartment() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where b.retired=false and "
                + " (type(b)=:class1 or type(b)=:class2) "
                + " and  b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  (upper(b.toInstitution.name) like :toIns )";
            temMap.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.deptId) like :refId )";
            temMap.put("refId", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :inv )";
            temMap.put("inv", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " (upper(bItem.item.name) like :itm ))";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }
        
         if (getSearchKeyword().getCode()!= null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " (upper(bItem.item.code) like :cde ))";
            temMap.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }


        sql += " order by b.createdAt desc  ";

        temMap.put("class1", BilledBill.class);
        temMap.put("class2", PreBill.class);
        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        //temMap.put("dep", getSessionController().getDepartment());
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, maxResult);
        //     //System.err.println("SIZE : " + lst.size());

    }

    public void createRequestTable() {
        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toDep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.PharmacyTransferRequest);

        sql = "Select b From BilledBill b where "
                + " b.retired=false and  b.toDepartment=:toDep"
                + " and b.billType= :bTp and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :dep )";
            tmp.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyBillItemTable() {
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.PharmacyPre);
        m.put("rBType", BillType.PharmacySale);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);
        m.put("rClass", BilledBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class and type(bi.bill.referenceBill)=:rClass"
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType and "
                + " bi.bill.referenceBill.billType=:rBType "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.bill.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(bi.netValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }
        
        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  (upper(bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPoRequestedAndApproved() {
        bills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where "
                + " b.createdAt between :fromDate and :toDate "
                + "and b.retired=false and b.billType= :bTp  ";

        sql += createKeySql(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyOrder);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, maxResult);

    }

    public void createApproved() {
        bills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where"
                + " b.referenceBill.creater is not null and b.referenceBill.cancelled=false "
                + " and b.createdAt between :fromDate and :toDate "
                + "and b.retired=false and b.billType= :bTp  ";

        sql += createKeySql(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyOrder);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, maxResult);

    }

    public void createNotApproved() {
        bills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where  b.referenceBill is null  "
                + " and b.createdAt between :fromDate and :toDate "
                + "and b.retired=false and b.billType= :bTp ";

        sql += createKeySql(tmp);
        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyOrder);
        List<Bill> lst1 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, maxResult);

        sql = "Select b From BilledBill b where "
                + " b.referenceBill.creater is null "
                + " and b.createdAt between :fromDate and :toDate "
                + "and b.retired=false and b.billType= :bTp  ";

        sql += createKeySql(tmp);
        sql += " order by b.createdAt desc  ";

        List<Bill> lst2 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, maxResult);

        sql = "Select b From BilledBill b where "
                + "  b.referenceBill.creater is not null and b.referenceBill.cancelled=true "
                + " and b.createdAt between :fromDate and :toDate "
                + "and b.retired=false and b.billType= :bTp ";

        sql += createKeySql(tmp);
        sql += " order by b.createdAt desc  ";

        List<Bill> lst3 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, maxResult);

        lst1.addAll(lst2);
        lst1.addAll(lst3);

        bills = lst1;

    }

    private String createKeySql(HashMap tmp) {
        String sql = "";
        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  (upper(b.toInstitution.name) like :toIns )";
            tmp.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCreator() != null && !getSearchKeyword().getCreator().trim().equals("")) {
            sql += " and  (upper(b.creater.webUserPerson.name) like :crt )";
            tmp.put("crt", "%" + getSearchKeyword().getCreator().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :crt )";
            tmp.put("crt", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :reqTotal )";
            tmp.put("reqTotal", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.netTotal) like :appTotal )";
            tmp.put("appTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        return sql;

    }

    private String keysForGrnReturn(HashMap tmp) {
        String sql = "";
        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :invoice )";
            tmp.put("invoice", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.deptId) like :refNo )";
            tmp.put("refNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            tmp.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.netTotal) like :total )";
            tmp.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            tmp.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        return sql;
    }

    public void createGrnTable() {
        bills = null;
        String sql;
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp "
                + " and b.institution=:ins and"
                + " b.createdAt between :fromDate and :toDate ";

        sql += keysForGrnReturn(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.PharmacyGrnBill);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getReturnBill(b));
        }

    }

    public void createGrnTableAllIns() {
        bills = null;
        String sql;
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        sql += keysForGrnReturn(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        // tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.PharmacyGrnBill);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getReturnBill(b));
        }

    }

    private List<Bill> getReturnBill(Bill b) {
        String sql = "Select b From BilledBill b where b.retired=false and b.creater is not null"
                + " and  b.billType=:btp and "
                + " b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyGrnReturn);
        return getBillFacade().findBySQL(sql, hm);
    }

    public void createPoTable() {
        bills = null;
        String sql;
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp"
                + " and  b.referenceBill.institution=:ins and "
                + " b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  (upper(b.toInstitution.name) like :toIns )";
            tmp.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            tmp.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.PharmacyOrderApprove);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getGrns(b));
        }

    }

    private List<Bill> getGrns(Bill b) {
        String sql = "Select b From BilledBill b where b.retired=false and b.creater is not null"
                + " and b.billType=:btp and "
                + " b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyGrnBill);
        return getBillFacade().findBySQL(sql, hm);
    }

    public void createDueFeeTable() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where b.retired=false and "
                + " b.bill.billType=:btp "
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 and"
                + "  b.bill.billDate between :fromDate"
                + " and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.OpdBill);

        billFees = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createPaymentTable() {
        billItems = null;
        HashMap temMap = new HashMap();
        String sql = "Select b FROM BillItem b where b.retired=false and b.bill.billType=:bType "
                + "and b.referenceBill.billType=:refType and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.billItem.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.OpdBill);

        billItems = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createBillItemTableByKeyword() {

        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.OpdBill);
        m.put("ins", getSessionController().getInstitution());

        sql = "select bi from BillItem bi where bi.bill.institution=:ins "
                + " and bi.bill.billType=:bType "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (searchKeyword.getPatientName() != null && !searchKeyword.getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.bill.patient.person.name) like :patientName )";
            m.put("patientName", "%" + searchKeyword.getPatientName().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getPatientPhone() != null && !searchKeyword.getPatientPhone().trim().equals("")) {
            sql += " and  (upper(bi.bill.patient.person.phone) like :patientPhone )";
            m.put("patientPhone", "%" + searchKeyword.getPatientPhone().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getBillNo() != null && !searchKeyword.getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.insId) like :billNo )";
            m.put("billNo", "%" + searchKeyword.getBillNo().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getItemName() != null && !searchKeyword.getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itemName )";
            m.put("itemName", "%" + searchKeyword.getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";
        //System.err.println("Sql " + sql);

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

        //   searchBillItems = new LazyBillItem(tmp);
    }

    public void createPatientInvestigationsTable() {

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  ";

        Map temMap = new HashMap();

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.id desc  ";
//    

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        //System.err.println("Sql " + sql);
        patientInvestigations = getPatientInvestigationFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createPatientInvestigationsTableSingle() {
        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient p where "
                + " p=:pt ";
        Map temMap = new HashMap();
        sql += " order by pi.id desc  ";
        temMap.put("pt", getTransferController().getPatient());
//        //System.out.println("temMap = " + temMap);
//        //System.out.println("sql = " + sql);
        patientInvestigationsSigle = getPatientInvestigationFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);
//        //System.out.println("patientInvestigations.size() = " + patientInvestigations.size());
    }

    public void createPatientInvestigationsTableAll() {

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  ";

        Map temMap = new HashMap();

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(i.phone) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.id desc  ";
//    

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        //System.err.println("Sql " + sql);
        patientInvestigations = getPatientInvestigationFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createPreBillsForReturn() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b where b.billType = :billType and b.institution=:ins and"
                + " b.referenceBill.billType=:refBillType "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("billType", BillType.PharmacyPre);
        temMap.put("refBillType", BillType.PharmacySale);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public void createPreBillsNotPaid() {

        bills = getBillBean().billsForTheDayNotPaid(BillType.PharmacyPre, getSessionController().getDepartment());

    }

    public void addToStock() {
        for (Bill b : getSelectedBills()) {
            String msg = getPharmacyBean().reAddToStock(b, getSessionController().getLoggedUser(), getSessionController().getDepartment());
            if (!msg.isEmpty()) {
                UtilityController.addErrorMessage(msg);
            }
        }

    }

    public void createPreTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false and b.deptId is not null ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.PharmacyPre);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createGrnPaymentTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.GrnPayment);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createTableByKeyword() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.OpdBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createCollectingTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.LabBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
//        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createCollectingTableAll() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.LabBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
//        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createCreditTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstiution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBank() != null && !getSearchKeyword().getBank().trim().equals("")) {
            sql += " and  (upper(b.bank.name) like :bank )";
            temMap.put("bank", "%" + getSearchKeyword().getBank().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.chequeRefNo) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.CashRecieveBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public List<Bill> getChannelPaymentBills() {
        if (bills == null) {
            String sql;
            Map temMap = new HashMap();
            if (bills == null) {
                sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.id in"
                        + "(Select bt.bill.id From BillItem bt Where bt.referenceBill.billType=:btp"
                        + " or bt.referenceBill.billType=:btp2) and b.billType=:type and b.createdAt "
                        + "between :fromDate and :toDate order by b.id";

                temMap.put("toDate", getToDate());
                temMap.put("fromDate", getFromDate());
                temMap.put("type", BillType.PaymentBill);
                temMap.put("btp", BillType.ChannelPaid);
                temMap.put("btp2", BillType.ChannelCredit);
                bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 100);

                if (bills == null) {
                    bills = new ArrayList<>();
                }
            }
        }
        return bills;

    }

    public void createChannelDueBillFee() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where b.retired=false and (b.bill.billType=:btp or b.bill.billType=:btp2) "
                + " and b.bill.id in(Select bs.bill.id From BillSession bs where bs.retired=false ) "
                + "and b.bill.cancelled=false and (b.feeValue - b.paidValue) > 0 and  "
                + "b.bill.createdAt between :fromDate and :toDate order by b.staff.id  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.ChannelPaid);
        temMap.put("btp2", BillType.ChannelCredit);

        billFees = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createAgentPaymentTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType "
                + " and b.institution=:ins and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.institutionCode) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("billType", BillType.AgentPaymentReceiveBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createInwardServiceTable() {

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where b.createdAt is not null and b.billType = :billType and b.patientEncounter.discharged=false and "
                + " b.id in(Select bf.bill.id From BillFee bf where bf.retired=false and bf.createdAt between :fromDate and :toDate and bf.billItem is not null)"
                + " and b.createdAt between :fromDate and :toDate and b.retired=false order by b.insId desc  ";

        temMap.put("billType", BillType.InwardBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardServiceTableDischarged() {

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where b.createdAt is not null and b.billType = :billType and b.patientEncounter.discharged=true and "
                + " b.id in(Select bf.bill.id From BillFee bf where bf.retired=false and bf.createdAt between :fromDate and :toDate and bf.billItem is not null)"
                + " and b.createdAt between :fromDate and :toDate and b.retired=false order by b.insId desc  ";

        temMap.put("billType", BillType.InwardBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardPaymentBills() {

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where b.createdAt is not null and b.patientEncounter.discharged=false and"
                + " b.billType = :billType and b.createdAt between :fromDate and :toDate "
                + "and b.retired=false order by b.insId desc  ";

        temMap.put("billType", BillType.InwardPaymentBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardPaymentBillsDischarged() {

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where b.createdAt is not null and "
                + " b.patientEncounter.discharged=true and"
                + " b.billType = :billType and b.createdAt between :fromDate and :toDate "
                + "and b.retired=false order by b.insId desc  ";

        temMap.put("billType", BillType.InwardPaymentBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardProBills() {

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where b.createdAt is not null and b.billType = :billType and b.patientEncounter.discharged=false and "
                + " b.id in(Select bf.bill.id From BillFee bf where bf.retired=false and bf.createdAt between :fromDate and :toDate and bf.billItem is null)"
                + " and b.createdAt between :fromDate and :toDate and b.retired=false order by b.insId desc  ";

        temMap.put("billType", BillType.InwardBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardProBillsDischarged() {

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where b.createdAt is not null and b.billType = :billType and b.patientEncounter.discharged=true and "
                + " b.id in(Select bf.bill.id From BillFee bf where bf.retired=false and bf.createdAt between :fromDate and :toDate and bf.billItem is null)"
                + " and b.createdAt between :fromDate and :toDate and b.retired=false order by b.insId desc  ";

        temMap.put("billType", BillType.InwardBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createPettyTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :stf )";
            temMap.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  (upper(b.person.name) like :per )";
            temMap.put("per", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.PettyCash);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

//     public List<Bill> getInstitutionPaymentBills() {
//        if (bills == null) {
//            String sql;
//            Map temMap = new HashMap();
//            if (bills == null) {
//                if (txtSearch == null || txtSearch.trim().equals("")) {
//                    sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.billType=:type and b.createdAt between :fromDate and :toDate order by b.id";
//                    temMap.put("toDate", getToDate());
//                    temMap.put("fromDate", getFromDate());
//                    temMap.put("type", BillType.PaymentBill);
//                    bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 100);
//
//                } else {
//                    sql = "select b from BilledBill b where b.retired=false and b.billType=:type and b.createdAt between :fromDate and :toDate and (upper(b.staff.person.name) like '%" + txtSearch.toUpperCase() + "%'  or upper(b.staff.person.phone) like '%" + txtSearch.toUpperCase() + "%'  or upper(b.insId) like '%" + txtSearch.toUpperCase() + "%') order by b.createdAt desc  ";
//                    temMap.put("toDate", getToDate());
//                    temMap.put("fromDate", getFromDate());
//                    temMap.put("type", BillType.PaymentBill);
//                    bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 100);
//                }
//                if (bills == null) {
//                    bills = new ArrayList<Bill>();
//                }
//            }
//        }
//        return bills;
//
//    }
    public SearchController() {
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public SearchKeyword getSearchKeyword() {
        if (searchKeyword == null) {
            searchKeyword = new SearchKeyword();
        }
        return searchKeyword;
    }

    public void setSearchKeyword(SearchKeyword searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
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

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public List<BillFee> getBillFees() {
        return billFees;
    }

    public void setBillFees(List<BillFee> billFees) {
        this.billFees = billFees;
    }

    public List<PatientInvestigation> getPatientInvestigations() {
        return patientInvestigations;
    }

    public void setPatientInvestigations(List<PatientInvestigation> patientInvestigations) {
        this.patientInvestigations = patientInvestigations;
    }

    public PatientInvestigationFacade getPatientInvestigationFacade() {
        return patientInvestigationFacade;
    }

    public void setPatientInvestigationFacade(PatientInvestigationFacade patientInvestigationFacade) {
        this.patientInvestigationFacade = patientInvestigationFacade;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public int getMaxResult() {

        return maxResult;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    public List<Bill> getSelectedBills() {
        return selectedBills;
    }

    public void setSelectedBills(List<Bill> selectedBills) {
        this.selectedBills = selectedBills;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public BillType getBillType() {
        return billType;
    }

    public void setBillType(BillType billType) {
        this.billType = billType;
    }

    public TransferController getTransferController() {
        return transferController;
    }

    public void setTransferController(TransferController transferController) {
        this.transferController = transferController;
    }

    public List<PatientInvestigation> getPatientInvestigationsSigle() {
        if (patientInvestigationsSigle == null) {
            createPatientInvestigationsTableSingle();
        }
        return patientInvestigationsSigle;
    }

    public void setPatientInvestigationsSigle(List<PatientInvestigation> patientInvestigationsSigle) {
        this.patientInvestigationsSigle = patientInvestigationsSigle;
    }

}
