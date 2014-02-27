/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.report;

import com.divudi.bean.SessionController;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.BillsTotals;
import com.divudi.data.table.String1Value1;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.RefundBill;
import com.divudi.entity.WebUser;
import com.divudi.facade.BillFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;

import javax.inject.Inject;
import javax.persistence.TemporalType;

/**
 *
 * @author www.divudi.com
 */


@Named
@RequestScoped
public class CommonReport implements Serializable {

    @Inject
    SessionController sessionController;
    ///////////////////
    @EJB
    private BillFacade billFacade;
    @EJB
    CommonFunctions commonFunctions;
    ////////////////////
    private Institution collectingIns;
    Institution institution;
    private Date fromDate;
    private Date toDate;
    private WebUser webUser;
    private Department department;
    private BillType billType;
    private Institution creditCompany;
    /////////////////////
    private BillsTotals billedBills;
    private BillsTotals cancellededBills;
    private BillsTotals refundedBills;
    private BillsTotals billedBillsPh;
    private BillsTotals billedBillsPh2;
    private BillsTotals cancellededBillsPh;
    private BillsTotals cancellededBillsPh2;
    private BillsTotals refundedBillsPh;
    private BillsTotals refundedBillsPh2;
    private BillsTotals paymentBills;
    private BillsTotals paymentCancelBills;
    private BillsTotals pettyPayments;
    private BillsTotals pettyPaymentsCancel;
    private BillsTotals cashRecieves;
    private BillsTotals cashRecieveCancel;
    private BillsTotals agentRecieves;
    private BillsTotals agentCancelBill;
    private BillsTotals inwardPayments;
    private BillsTotals inwardPaymentCancel;
    private BillsTotals grnBilled;
    private BillsTotals grnCancelled;
    private BillsTotals grnReturn;
    private BillsTotals grnReturnCancel;
    //////////////////    
    private List<String1Value1> dataTableData;

    /**
     * Creates a new instance of CommonReport
     */
    public CommonReport() {
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
         recreteModal();
        this.institution = institution;
      
    }

    public BillType[] getBillTypes() {
        return BillType.values();
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
        recreteModal();
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
        recreteModal();
    }

    public WebUser getWebUser() {

        return webUser;
    }

    public void setWebUser(WebUser webUser) {
        this.webUser = webUser;
        recreteModal();
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
        recreteModal();
    }

    public List<Bill> getBillsByReferingDoc() {

        Map temMap = new HashMap();
        List<Bill> tmp;

        String sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.billType = :bTp and b.createdAt between :fromDate and :toDate  order by b.referredBy.person.name";
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bTp", BillType.OpdBill);

        tmp = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (tmp == null) {
            tmp = new ArrayList<Bill>();
        }

        return tmp;

    }

    public List<Bill> getBillsByCollecting() {

        Map temMap = new HashMap();
        temMap.put("fromDate", fromDate);
        temMap.put("toDate", toDate);
        String sql;
        List<Bill> tmp;

        if (collectingIns == null) {
            //sql = "SELECT b FROM BilledBill b WHERE b.retired=false  and b.createdAt between :fromDate and :toDate  order by b.collectingCentre.name";
            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and  b.createdAt between :fromDate and :toDate  order by b.collectingCentre.name";
        } else {
            sql = "SELECT b FROM BilledBill b WHERE b.retired=false   and  b.collectingCentre=:col  and b.createdAt between :fromDate and :toDate  order by b.collectingCentre.name";
            temMap.put("col", getCollectingIns());
        }

        tmp = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (tmp == null) {
            tmp = new ArrayList<Bill>();
        }

        return tmp;

    }

    public BillsTotals getUserBills() {
        if (billedBills == null) {
            getBilledBills().setBills(userBills(new BilledBill(), BillType.OpdBill, getWebUser()));
            // calTot(getBilledBills());
        }

        return billedBills;
    }

    public BillsTotals getUserCancelledBills() {
        if (cancellededBills == null) {
            getCancellededBills().setBills(userBills(new CancelledBill(), BillType.OpdBill, getWebUser()));
            //   calTot(getCancellededBills());
        }
        return cancellededBills;
    }

    public BillsTotals getUserRefundedBills() {
        if (refundedBills == null) {
            getRefundedBills().setBills(userBills(new RefundBill(), BillType.OpdBill, getWebUser()));
            //  calTot(getRefundedBills());
        }
        return refundedBills;
    }

    public BillsTotals getUserPaymentBills() {
        if (paymentBills == null) {
            getPaymentBills().setBills(userBills(new BilledBill(), BillType.PaymentBill, getWebUser()));
            //  calTot(getPaymentBills());
        }
        return paymentBills;
    }

    public BillsTotals getUserPaymentCancelBills() {
        if (paymentCancelBills == null) {
            getPaymentCancelBills().setBills(userBills(new CancelledBill(), BillType.PaymentBill, getWebUser()));
            //   calTot(getPaymentCancelBills());
        }
        return paymentCancelBills;
    }

    public BillsTotals getInstitutionBilledBills() {
        if (billedBills == null) {
            getBilledBills().setBills(institutionBill(getInstitution(), new BilledBill(), BillType.OpdBill));
            //  calTot(getBilledBills());
        }
        return billedBills;
    }

    public BillsTotals getInstitutionCancelledBills() {
        if (cancellededBills == null) {
            getCancellededBills().setBills(institutionBill(getInstitution(), new CancelledBill(), BillType.OpdBill));
            //   calTot(getCancellededBills());
        }
        return cancellededBills;
    }

    public BillsTotals getInstitutionRefundedBills() {
        if (refundedBills == null) {
            getRefundedBills().setBills(institutionBill(getInstitution(), new RefundBill(), BillType.OpdBill));
            //  calTot(getRefundedBills());
        }
        return refundedBills;
    }

    public BillsTotals getInstitutionPaymentBills() {
        if (paymentBills == null) {
            getPaymentBills().setBills(institutionBill(getInstitution(), new BilledBill(), BillType.PaymentBill));
            // calTot(getPaymentBills());
        }
        return paymentBills;
    }

    public BillsTotals getInstitutionPaymentCancelBills() {
        if (paymentCancelBills == null) {
            getPaymentCancelBills().setBills(institutionBill(getInstitution(), new CancelledBill(), BillType.PaymentBill));
            // calTot(getPaymentCancelBills());
        }
        return paymentCancelBills;
    }

    public BillsTotals getUserPaymentBillsOwn() {

        return paymentBills;
    }

    public BillsTotals getUserInwardPaymentBillsOwn() {

        return inwardPayments;
    }

    public BillsTotals getInstitutionInwardPaymentBillsOwn() {
        if (inwardPayments == null) {
            getInwardPayments().setBills(billsOwn(new BilledBill(), BillType.InwardPaymentBill));
        }
        //  calTot(getInwardPayments());
        return inwardPayments;
    }

    public List<Bill> getBillsByReferingDocOwn() {

        Map temMap = new HashMap();
        List<Bill> tmp;
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bTp", BillType.OpdBill);

        String sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.toInstitution=:ins "
                + "and b.billType =:bTp and b.createdAt between :fromDate and :toDate  order by b.referredBy.person.name ";
        tmp = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (tmp == null) {
            tmp = new ArrayList<>();
        }

        return tmp;

    }

    public List<Bill> getBillsByCollectingOwn() {

        Map temMap = new HashMap();
        List<Bill> tmp;
        temMap.put("fromDate", fromDate);
        temMap.put("toDate", toDate);
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bType", BillType.LabBill);
        String sql;

        if (collectingIns == null) {
            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and  b.billType=:bType and  b.institution=:ins and b.createdAt between :fromDate and :toDate  order by b.collectingCentre.name";
        } else {
            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and  b.billType =:bType and b.institution=:ins and b.collectingCentre=:col and b.createdAt between :fromDate and :toDate  order by b.collectingCentre.name";
            temMap.put("col", getCollectingIns());
        }
        tmp = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (tmp == null) {
            tmp = new ArrayList<Bill>();
        }

        total = 0.0;

        for (Bill b : tmp) {
            total += b.getNetTotal();
        }

        return tmp;

    }

    public List<Bill> getBillsByLabCreditOwn() {

        Map temMap = new HashMap();
        List<Bill> tmp;
        temMap.put("fromDate", fromDate);
        temMap.put("toDate", toDate);
        //   temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bType", BillType.LabBill);
        temMap.put("col", getCreditCompany());
        String sql;

        sql = "SELECT b FROM BilledBill b WHERE b.retired=false and  b.billType =:bType "
                + "  and b.creditCompany=:col and b.createdAt between :fromDate and :toDate "
                + "order by b.creditCompany.name";

        tmp = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (tmp == null) {
            tmp = new ArrayList<>();
        }

        total = 0.0;

        for (Bill b : tmp) {
            total += b.getNetTotal();
        }

        return tmp;

    }

    private double total;

    public BillsTotals getUserRefundedBillsOwn() {

        return refundedBills;
    }

    public BillsTotals getUserRefundedBillsOwnPh() {

        return refundedBillsPh;
    }

    public BillsTotals getUserRefundedBillsPhOther() {
        if (refundedBillsPh2 == null) {
            getRefundedBillsPh2().setBills(userPharmacyBillsOther(new RefundBill(), BillType.PharmacySale, getWebUser()));
        }
        // calTot(getRefundedBills());
        return refundedBillsPh2;
    }

    public BillsTotals getUserCancelledBillsOwn() {

        return cancellededBills;
    }

    public BillsTotals getUserCancelledBillsOwnPh() {

        return cancellededBillsPh;
    }

    public BillsTotals getUserCancelledBillsPhOther() {

        return cancellededBillsPh2;
    }

    private List<Bill> userBillsOwn(Bill billClass, BillType billType, WebUser webUser) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType = :btp"
                + " and b.creater=:web and b.institution=:ins and b.createdAt between :fromDate and"
                + " :toDate order by b.insId ";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("web", webUser);
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Bill> grnBills(Bill billClass, BillType billType, Department dep) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and "
                + " b.billType = :btp "
                + " and b.department=:d "
                + " and b.createdAt between :fromDate and "
                + " :toDate order by b.deptId,b.fromInstitution.name ";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("d", dep);

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Bill> grnBills(Bill billClass, BillType billType, Department dep, Institution ins) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and "
                + " b.billType = :btp and (b.fromInstitution=:ins or b.toInstitution=:ins ) "
                + " and b.department=:d "
                + " and b.createdAt between :fromDate and "
                + " :toDate order by b.deptId,b.fromInstitution.name ";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("d", dep);
        temMap.put("ins", ins);

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Bill> userPharmacyBillsOwn(Bill billClass, BillType billType, WebUser webUser) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false "
                + " and b.billType = :btp"
                + " and b.creater=:web and b.referenceBill.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate order by b.deptId ";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("web", webUser);
        temMap.put("ins", getSessionController().getInstitution());

//        checkOtherInstiution
        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Bill> userPharmacyBillsOther(Bill billClass, BillType billType, WebUser webUser) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and "
                + "  b.billType = :btp"
                + " and b.creater=:web and b.referenceBill.institution!=:ins "
                + " and b.createdAt between :fromDate"
                + " and :toDate order by b.deptId ";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("web", webUser);
        temMap.put("ins", getSessionController().getInstitution());

        Bill b = getBillFacade().findFirstBySQL(sql, temMap, TemporalType.DATE);

        if (b != null && institution == null) {
            //System.err.println("SYS " + b.getInstitution().getName());
            institution = b.getInstitution();
        }

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Bill> institutionBill(Institution ins, Bill billClass, BillType billType) {
        String sql;
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);

        if (institution == null) {
            sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType = :btp"
                    + " and  b.createdAt between :fromDate and :toDate ";
        } else {
            sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType = :btp"
                    + " and b.institution=:ins and b.createdAt between :fromDate and :toDate";

            temMap.put("ins", ins);
        }

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private List<Bill> userBills(Bill billClass, BillType billType, WebUser webUser) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType = :btp"
                + " and b.creater=:web and b.createdAt between :fromDate and :toDate";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("bill", billClass.getClass());
        temMap.put("btp", billType);
        temMap.put("web", webUser);

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public BillsTotals getUserBillsOwn() {

        return billedBills;
    }

    public BillsTotals getUserBillsOwnPh() {

        return billedBillsPh;
    }

    public BillsTotals getUserBillsPhOther() {
        return billedBillsPh2;
    }

    public BillsTotals getInstitutionPaymentBillsOwn() {
        if (paymentBills == null) {
            getPaymentBills().setBills(billsOwn(new BilledBill(), BillType.PaymentBill));
            //   calTot(getPaymentBills());
        }
        return paymentBills;
    }

    public BillsTotals getInstitutionRefundedBillsOwn() {
        if (refundedBills == null) {
            getRefundedBills().setBills(billsOwn(new RefundBill(), BillType.OpdBill));
        }
        //  calTot(getRefundedBills());
        return refundedBills;
    }

    public BillsTotals getInstitutionRefundedBillsOwnPh() {
        if (refundedBillsPh == null) {
            getRefundedBillsPh().setBills(billsOwn(new RefundBill(), BillType.PharmacySale));
        }
        //  calTot(getRefundedBills());
        return refundedBillsPh;
    }

    public BillsTotals getInstitutionCancelledBillsOwn() {
        if (cancellededBills == null) {
            getCancellededBills().setBills(billsOwn(new CancelledBill(), BillType.OpdBill));
        }
        ///   calTot(getCancellededBills());
        return cancellededBills;
    }

    public BillsTotals getInstitutionCancelledBillsOwnPh() {
        if (cancellededBillsPh == null) {
            getCancellededBillsPh().setBills(billsOwn(new CancelledBill(), BillType.PharmacySale));
        }
        ///   calTot(getCancellededBills());
        return cancellededBillsPh;
    }

    public BillsTotals getUserAgentRecieveBills() {

        return agentRecieves;
    }

    public BillsTotals getUserCashRecieveBills() {

        return cashRecieves;
    }

    public BillsTotals getUserAgentRecieveBillCancel() {

        return agentCancelBill;
    }

    public BillsTotals getUserCashRecieveBillCancel() {

        return cashRecieveCancel;
    }

    public BillsTotals getUserPettyPaymentBills() {

        return pettyPayments;
    }

    public BillsTotals getUserPettyPaymentCancelBills() {

        return pettyPaymentsCancel;
    }

    public BillsTotals getUserPaymentCancelBillsOwn() {

        return paymentCancelBills;
    }

    public BillsTotals getUserInwardPaymentCancelBillsOwn() {

        return inwardPaymentCancel;
    }

    public BillsTotals getInstitutionInwardPaymentCancelBillsOwn() {
        if (inwardPaymentCancel == null) {
            List<Bill> tmp = billsOwn(new CancelledBill(), BillType.InwardPaymentBill);
            getInwardPaymentCancel().setBills(tmp);
        }
        //    calTot(getInwardPaymentCancel());
        return inwardPaymentCancel;
    }

    public BillsTotals getInstitutionPaymentCancelBillsOwn() {
        if (paymentCancelBills == null) {
            List<Bill> tmp = billsOwn(new CancelledBill(), BillType.PaymentBill);

            getPaymentCancelBills().setBills(tmp);
        }
        //   calTot(getPaymentCancelBills());
        return paymentCancelBills;
    }

    public BillsTotals getInstitutionPettyPaymentBillsOwn() {
        if (pettyPayments == null) {
            List<Bill> tmp = billsOwn(new BilledBill(), BillType.PettyCash);

            getPettyPayments().setBills(tmp);
            //   calTot(getPettyPayments());
        }
        return pettyPayments;
    }

//    public List<Bill> getInstitutionPettyPaymentBills() {
//        if (pettyPayments == null) {
//            String sql;
//            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.billType = :bTp and  b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.PettyCash);
//            pettyPayments = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (pettyPayments == null) {
//                pettyPayments = new ArrayList<Bill>();
//            }
//
//        }
//
//        calPettyTotal();
//        return pettyPayments;
//    }
    public BillsTotals getInstitutionPettyCancellBillsOwn() {
        if (pettyPaymentsCancel == null) {
            List<Bill> tmp = billsOwn(new CancelledBill(), BillType.PettyCash);
            getPettyPaymentsCancel().setBills(tmp);
//        calTot(getPettyPaymentsCancel());
        }
        return pettyPaymentsCancel;
    }

//    public List<Bill> getInstitutionPettyCancellBills() {
//        if (pettyPaymentsCancel == null) {
//            String sql;
//
//            sql = "SELECT b FROM CancelledBill b WHERE b.retired=false and b.billType = :bTp and b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.PettyCash);
//            pettyPaymentsCancel = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (pettyPaymentsCancel == null) {
//                pettyPaymentsCancel = new ArrayList<Bill>();
//            }
//
//        }
//        calPettyCancelTotal();
//        return pettyPaymentsCancel;
//    }
    public BillsTotals getInstitutionCashRecieveBillsOwn() {
        if (cashRecieves == null) {
            getCashRecieves().setBills(billsOwn(new BilledBill(), BillType.CashRecieveBill));
            //      calTot(getCashRecieves());
        }
        return cashRecieves;
    }

//    public List<Bill> getInstitutionCashRecieveBills() {
//        if (cashRecieves == null) {
//            String sql;
//
//            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.billType = :bTp and  b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.CashRecieveBill);
//            cashRecieves = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (cashRecieves == null) {
//                cashRecieves = new ArrayList<Bill>();
//            }
//
//        }
//        calCashRecieveTot();
//        return cashRecieves;
//    }
    public BillsTotals getInstitutionAgentBillsOwn() {
        if (agentRecieves == null) {
            getAgentRecieves().setBills(billsOwn(new BilledBill(), BillType.AgentPaymentReceiveBill));
        }
        //    calTot(getAgentRecieves());
        return agentRecieves;
    }

//    public List<Bill> getInstitutionAgentBills() {
//        if (agentRecieves == null) {
//            String sql;
//
//            sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.billType = :bTp and  b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.AgentPaymentReceiveBill);
//            agentRecieves = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (agentRecieves == null) {
//                agentRecieves = new ArrayList<Bill>();
//            }
//
//        }
//        calAgentTotal();
//        return agentRecieves;
//    }
    public BillsTotals getInstitutionCashRecieveCancellBillsOwn() {
        if (cashRecieveCancel == null) {
            getCashRecieveCancel().setBills(billsOwn(new CancelledBill(), BillType.CashRecieveBill));
            //    calTot(getCashRecieveCancel());
        }
        return cashRecieveCancel;
    }

//    public List<Bill> getInstitutionCashRecieveCancellBills() {
//        if (cashRecieveCancel == null) {
//            String sql;
//
//            sql = "SELECT b FROM CancelledBill b WHERE b.retired=false and b.billType = :bTp and b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.CashRecieveBill);
//            cashRecieveCancel = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (cashRecieveCancel == null) {
//                cashRecieveCancel = new ArrayList<Bill>();
//            }
//
//        }
//        calCashRecieveCancelTot();
//        return cashRecieveCancel;
//    }
    public BillsTotals getInstitutionAgentCancellBillsOwn() {
        if (agentCancelBill == null) {
            getAgentCancelBill().setBills(billsOwn(new CancelledBill(), BillType.AgentPaymentReceiveBill));
            //   calTot(getAgentCancelBill());
        }
        return agentCancelBill;
    }

//    public List<Bill> getInstitutionAgentCancellBills() {
//        if (agentCancelBill == null) {
//            String sql;
//
//            sql = "SELECT b FROM CancelledBill b WHERE b.retired=false and b.billType = :bTp  and b.createdAt between :fromDate and :toDate order by b.id";
//
//            Map temMap = new HashMap();
//            temMap.put("fromDate", getFromDate());
//            temMap.put("toDate", getToDate());
//            temMap.put("bTp", BillType.AgentPaymentReceiveBill);
//            agentCancelBill = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//
//            if (agentCancelBill == null) {
//                agentCancelBill = new ArrayList<Bill>();
//            }
//
//        }
//        calAgentCancelTot();
//        return agentCancelBill;
//    }
    private List<Bill> billsOwn(Bill billClass, BillType billType) {
        String sql = "SELECT b FROM Bill b WHERE type(b)=:bill and b.retired=false and b.billType=:btp and b.institution=:ins"
                + " and b.createdAt between :fromDate and :toDate";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", billType);
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bill", billClass.getClass());

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double calValue(Bill billClass, BillType billType, PaymentMethod paymentMethod) {
        String sql = "SELECT sum(b.netTotal) FROM Bill b WHERE"
                + " type(b)=:bill and b.retired=false and "
                + " b.billType=:btp "
                + " and (b.paymentMethod=:pm or b.paymentScheme.paymentMethod=:pm)"
                + "  and b.institution=:ins"
                + " and b.createdAt between :fromDate and :toDate";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", billType);
        temMap.put("pm", paymentMethod);
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bill", billClass.getClass());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double calValue(Bill billClass, BillType billType, PaymentMethod paymentMethod, WebUser wUser) {
        String sql = "SELECT sum(b.netTotal) FROM Bill b WHERE"
                + " type(b)=:bill and b.retired=false and "
                + " b.billType=:btp and b.creater=:w "
                + " and (b.paymentMethod=:pm or b.paymentScheme.paymentMethod=:pm)"
                + "  and b.institution=:ins"
                + " and b.createdAt between :fromDate and :toDate";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", billType);
        temMap.put("pm", paymentMethod);
        temMap.put("w", wUser);
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bill", billClass.getClass());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double calValue(Bill billClass, BillType billType, PaymentMethod paymentMethod, Department dep) {
        String sql = "SELECT sum(b.netTotal) FROM Bill b WHERE"
                + " type(b)=:bill and b.retired=false and "
                + " b.billType=:btp and b.department=:d "
                + " and b.paymentMethod=:pm "
                + "  and b.createdAt between :fromDate and :toDate";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", billType);
        temMap.put("pm", paymentMethod);
        temMap.put("d", dep);
        temMap.put("bill", billClass.getClass());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double calValue(Bill billClass, BillType billType, PaymentMethod paymentMethod, Department dep, Institution ins) {
        String sql = "SELECT sum(b.netTotal) FROM Bill b WHERE"
                + " type(b)=:bill and b.retired=false and "
                + " b.billType=:btp and b.department=:d "
                + " and b.paymentMethod=:pm and "
                + " (b.fromInstitution=:ins or b.toInstitution=:ins) "
                + "  and b.createdAt between :fromDate and :toDate";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", billType);
        temMap.put("pm", paymentMethod);
        temMap.put("d", dep);
        temMap.put("ins", ins);
        temMap.put("bill", billClass.getClass());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double calValueOther(Bill billClass, BillType billType, PaymentMethod paymentMethod, WebUser wUser) {
        String sql = "SELECT sum(b.netTotal) FROM Bill b WHERE"
                + " type(b)=:bill and b.retired=false and "
                + " b.billType=:btp and b.creater=:w "
                + " and (b.paymentMethod=:pm or b.paymentScheme.paymentMethod=:pm)"
                + "  and b.institution!=:ins"
                + " and b.createdAt between :fromDate and :toDate";
        Map temMap = new HashMap();
        temMap.put("fromDate", getFromDate());
        temMap.put("toDate", getToDate());
        temMap.put("btp", billType);
        temMap.put("pm", paymentMethod);
        temMap.put("w", wUser);
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("bill", billClass.getClass());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    public BillsTotals getInstitutionBilledBillsOwn() {
        if (billedBills == null) {
            List<Bill> tmp = billsOwn(new BilledBill(), BillType.OpdBill);

            getBilledBills().setBills(tmp);
        }
        //    calTot(getBilledBills());
        return billedBills;
    }

    public BillsTotals getInstitutionBilledBillsOwnPh() {
        if (billedBillsPh == null) {
            List<Bill> tmp = billsOwn(new BilledBill(), BillType.PharmacySale);

            getBilledBillsPh().setBills(tmp);
        }
        //    calTot(getBilledBills());
        return billedBillsPh;
    }

    public double getFinalCreditTotal(List<BillsTotals> list) {

        double tmp = 0.0;
        for (BillsTotals bt : list) {
            if (bt != null) {
                tmp += bt.getCredit();
            }
        }

        return tmp;
    }

    public double getFinalCreditCardTotal(List<BillsTotals> list) {

        double tmp = 0.0;
        for (BillsTotals bt : list) {
            if (bt != null) {
                tmp += bt.getCard();
            }
        }

        return tmp;
    }

    public void createTableByBillType() {
        billedBills = null;
        cancellededBills = null;
        refundedBills = null;
        List<Bill> list = null;
        getBilledBills().setBills(billsOwn(new BilledBill(), billType));
        getBilledBills().setCard(calValue(new BilledBill(), billType, PaymentMethod.Card));
        getBilledBills().setCash(calValue(new BilledBill(), billType, PaymentMethod.Cash));
        getBilledBills().setCheque(calValue(new BilledBill(), billType, PaymentMethod.Cheque));
        getBilledBills().setCredit(calValue(new BilledBill(), billType, PaymentMethod.Credit));
        getBilledBills().setSlip(calValue(new BilledBill(), billType, PaymentMethod.Slip));
        ////////////
        getCancellededBills().setBills(billsOwn(new CancelledBill(), billType));
        getCancellededBills().setCard(calValue(new CancelledBill(), billType, PaymentMethod.Card));
        getCancellededBills().setCash(calValue(new CancelledBill(), billType, PaymentMethod.Cash));
        getCancellededBills().setCheque(calValue(new CancelledBill(), billType, PaymentMethod.Cheque));
        getCancellededBills().setCredit(calValue(new CancelledBill(), billType, PaymentMethod.Credit));
        getCancellededBills().setSlip(calValue(new CancelledBill(), billType, PaymentMethod.Slip));
        /////////////
        getRefundedBills().setBills(billsOwn(new RefundBill(), billType));
        getRefundedBills().setCard(calValue(new RefundBill(), billType, PaymentMethod.Card));
        getRefundedBills().setCash(calValue(new RefundBill(), billType, PaymentMethod.Cash));
        getRefundedBills().setCheque(calValue(new RefundBill(), billType, PaymentMethod.Cheque));
        getRefundedBills().setCredit(calValue(new RefundBill(), billType, PaymentMethod.Credit));
        getRefundedBills().setSlip(calValue(new RefundBill(), billType, PaymentMethod.Slip));

    }

    public void createCashierTableByUser() {
        recreteModal();
        //Opd Billed Bills
        getBilledBills().setBills(userBillsOwn(new BilledBill(), BillType.OpdBill, getWebUser()));
        getBilledBills().setCard(calValue(new BilledBill(), BillType.OpdBill, PaymentMethod.Card, getWebUser()));
        getBilledBills().setCash(calValue(new BilledBill(), BillType.OpdBill, PaymentMethod.Cash, getWebUser()));
        getBilledBills().setCheque(calValue(new BilledBill(), BillType.OpdBill, PaymentMethod.Cheque, getWebUser()));
        getBilledBills().setCredit(calValue(new BilledBill(), BillType.OpdBill, PaymentMethod.Credit, getWebUser()));
        getBilledBills().setSlip(calValue(new BilledBill(), BillType.OpdBill, PaymentMethod.Slip, getWebUser()));

        //Opd Cancelled Bill
        getCancellededBills().setBills(userBillsOwn(new CancelledBill(), BillType.OpdBill, getWebUser()));
        getCancellededBills().setCard(calValue(new CancelledBill(), BillType.OpdBill, PaymentMethod.Card, getWebUser()));
        getCancellededBills().setCash(calValue(new CancelledBill(), BillType.OpdBill, PaymentMethod.Cash, getWebUser()));
        getCancellededBills().setCheque(calValue(new CancelledBill(), BillType.OpdBill, PaymentMethod.Cheque, getWebUser()));
        getCancellededBills().setCredit(calValue(new CancelledBill(), BillType.OpdBill, PaymentMethod.Credit, getWebUser()));
        getCancellededBills().setSlip(calValue(new CancelledBill(), BillType.OpdBill, PaymentMethod.Slip, getWebUser()));

        //Opd Refunded Bill
        getRefundedBills().setBills(userBillsOwn(new RefundBill(), BillType.OpdBill, getWebUser()));
        getRefundedBills().setCard(calValue(new RefundBill(), BillType.OpdBill, PaymentMethod.Card, getWebUser()));
        getRefundedBills().setCash(calValue(new RefundBill(), BillType.OpdBill, PaymentMethod.Cash, getWebUser()));
        getRefundedBills().setCheque(calValue(new RefundBill(), BillType.OpdBill, PaymentMethod.Cheque, getWebUser()));
        getRefundedBills().setCredit(calValue(new RefundBill(), BillType.OpdBill, PaymentMethod.Credit, getWebUser()));
        getRefundedBills().setSlip(calValue(new RefundBill(), BillType.OpdBill, PaymentMethod.Slip, getWebUser()));

        //Pharmacy Billed
        getBilledBillsPh().setBills(userPharmacyBillsOwn(new BilledBill(), BillType.PharmacySale, getWebUser()));
        getBilledBillsPh().setCard(calValue(new BilledBill(), BillType.PharmacySale, PaymentMethod.Card, getWebUser()));
        getBilledBillsPh().setCash(calValue(new BilledBill(), BillType.PharmacySale, PaymentMethod.Cash, getWebUser()));
        getBilledBillsPh().setCheque(calValue(new BilledBill(), BillType.PharmacySale, PaymentMethod.Cheque, getWebUser()));
        getBilledBillsPh().setCredit(calValue(new BilledBill(), BillType.PharmacySale, PaymentMethod.Credit, getWebUser()));
        getBilledBillsPh().setSlip(calValue(new BilledBill(), BillType.PharmacySale, PaymentMethod.Slip, getWebUser()));

        //Pharmacy Cancelled       
        getCancellededBillsPh().setBills(userPharmacyBillsOwn(new CancelledBill(), BillType.PharmacySale, getWebUser()));
        getCancellededBillsPh().setCard(calValue(new CancelledBill(), BillType.PharmacySale, PaymentMethod.Card, getWebUser()));
        getCancellededBillsPh().setCash(calValue(new CancelledBill(), BillType.PharmacySale, PaymentMethod.Cash, getWebUser()));
        getCancellededBillsPh().setCheque(calValue(new CancelledBill(), BillType.PharmacySale, PaymentMethod.Cheque, getWebUser()));
        getCancellededBillsPh().setCredit(calValue(new CancelledBill(), BillType.PharmacySale, PaymentMethod.Credit, getWebUser()));
        getCancellededBillsPh().setSlip(calValue(new CancelledBill(), BillType.PharmacySale, PaymentMethod.Slip, getWebUser()));

        //Pharmacy Refunded      
        getRefundedBillsPh().setBills(userPharmacyBillsOwn(new RefundBill(), BillType.PharmacySale, getWebUser()));
        getRefundedBillsPh().setCard(calValue(new RefundBill(), BillType.PharmacySale, PaymentMethod.Card, getWebUser()));
        getRefundedBillsPh().setCash(calValue(new RefundBill(), BillType.PharmacySale, PaymentMethod.Cash, getWebUser()));
        getRefundedBillsPh().setCheque(calValue(new RefundBill(), BillType.PharmacySale, PaymentMethod.Cheque, getWebUser()));
        getRefundedBillsPh().setCredit(calValue(new RefundBill(), BillType.PharmacySale, PaymentMethod.Credit, getWebUser()));
        getRefundedBillsPh().setSlip(calValue(new RefundBill(), BillType.PharmacySale, PaymentMethod.Slip, getWebUser()));

        //Payment Billed Bill
        getPaymentBills().setBills(userBillsOwn(new BilledBill(), BillType.PaymentBill, getWebUser()));
        getPaymentBills().setCard(calValue(new BilledBill(), BillType.PaymentBill, PaymentMethod.Card, getWebUser()));
        getPaymentBills().setCash(calValue(new BilledBill(), BillType.PaymentBill, PaymentMethod.Cash, getWebUser()));
        getPaymentBills().setCheque(calValue(new BilledBill(), BillType.PaymentBill, PaymentMethod.Cheque, getWebUser()));
        getPaymentBills().setCredit(calValue(new BilledBill(), BillType.PaymentBill, PaymentMethod.Credit, getWebUser()));
        getPaymentBills().setSlip(calValue(new BilledBill(), BillType.PaymentBill, PaymentMethod.Slip, getWebUser()));

        //Payment Cancelled Bill
        getPaymentCancelBills().setBills(userBillsOwn(new CancelledBill(), BillType.PaymentBill, getWebUser()));
        getPaymentCancelBills().setCard(calValue(new CancelledBill(), BillType.PaymentBill, PaymentMethod.Card, getWebUser()));
        getPaymentCancelBills().setCash(calValue(new CancelledBill(), BillType.PaymentBill, PaymentMethod.Cash, getWebUser()));
        getPaymentCancelBills().setCheque(calValue(new CancelledBill(), BillType.PaymentBill, PaymentMethod.Cheque, getWebUser()));
        getPaymentCancelBills().setCredit(calValue(new CancelledBill(), BillType.PaymentBill, PaymentMethod.Credit, getWebUser()));
        getPaymentCancelBills().setSlip(calValue(new CancelledBill(), BillType.PaymentBill, PaymentMethod.Slip, getWebUser()));

        //Petty Cash Paymennt       
        getPettyPayments().setBills(userBillsOwn(new BilledBill(), BillType.PettyCash, getWebUser()));
        getPettyPayments().setCard(calValue(new BilledBill(), BillType.PettyCash, PaymentMethod.Card, getWebUser()));
        getPettyPayments().setCash(calValue(new BilledBill(), BillType.PettyCash, PaymentMethod.Cash, getWebUser()));
        getPettyPayments().setCheque(calValue(new BilledBill(), BillType.PettyCash, PaymentMethod.Cheque, getWebUser()));
        getPettyPayments().setCredit(calValue(new BilledBill(), BillType.PettyCash, PaymentMethod.Credit, getWebUser()));
        getPettyPayments().setSlip(calValue(new BilledBill(), BillType.PettyCash, PaymentMethod.Slip, getWebUser()));

        //Petty Cash Paymennt Cancell       
        getPettyPaymentsCancel().setBills(userBillsOwn(new CancelledBill(), BillType.PettyCash, getWebUser()));
        getPettyPaymentsCancel().setCard(calValue(new CancelledBill(), BillType.PettyCash, PaymentMethod.Card, getWebUser()));
        getPettyPaymentsCancel().setCash(calValue(new CancelledBill(), BillType.PettyCash, PaymentMethod.Cash, getWebUser()));
        getPettyPaymentsCancel().setCheque(calValue(new CancelledBill(), BillType.PettyCash, PaymentMethod.Cheque, getWebUser()));
        getPettyPaymentsCancel().setCredit(calValue(new CancelledBill(), BillType.PettyCash, PaymentMethod.Credit, getWebUser()));
        getPettyPaymentsCancel().setSlip(calValue(new CancelledBill(), BillType.PettyCash, PaymentMethod.Slip, getWebUser()));

        //Cash Receive Bill       
        getCashRecieves().setBills(userBillsOwn(new BilledBill(), BillType.CashRecieveBill, getWebUser()));
        getCashRecieves().setCard(calValue(new BilledBill(), BillType.CashRecieveBill, PaymentMethod.Card, getWebUser()));
        getCashRecieves().setCash(calValue(new BilledBill(), BillType.CashRecieveBill, PaymentMethod.Cash, getWebUser()));
        getCashRecieves().setCheque(calValue(new BilledBill(), BillType.CashRecieveBill, PaymentMethod.Cheque, getWebUser()));
        getCashRecieves().setCredit(calValue(new BilledBill(), BillType.CashRecieveBill, PaymentMethod.Credit, getWebUser()));
        getCashRecieves().setSlip(calValue(new BilledBill(), BillType.CashRecieveBill, PaymentMethod.Slip, getWebUser()));

        //Cash Recieve Cancel      
        getCashRecieveCancel().setBills(userBillsOwn(new CancelledBill(), BillType.CashRecieveBill, getWebUser()));
        getCashRecieveCancel().setCard(calValue(new CancelledBill(), BillType.CashRecieveBill, PaymentMethod.Card, getWebUser()));
        getCashRecieveCancel().setCash(calValue(new CancelledBill(), BillType.CashRecieveBill, PaymentMethod.Cash, getWebUser()));
        getCashRecieveCancel().setCheque(calValue(new CancelledBill(), BillType.CashRecieveBill, PaymentMethod.Cheque, getWebUser()));
        getCashRecieveCancel().setCredit(calValue(new CancelledBill(), BillType.CashRecieveBill, PaymentMethod.Credit, getWebUser()));
        getCashRecieveCancel().setSlip(calValue(new CancelledBill(), BillType.CashRecieveBill, PaymentMethod.Slip, getWebUser()));

        //Agent Recieve
        getAgentRecieves().setBills(userBillsOwn(new BilledBill(), BillType.AgentPaymentReceiveBill, getWebUser()));
        getAgentRecieves().setCard(calValue(new BilledBill(), BillType.AgentPaymentReceiveBill, PaymentMethod.Card, getWebUser()));
        getAgentRecieves().setCash(calValue(new BilledBill(), BillType.AgentPaymentReceiveBill, PaymentMethod.Cash, getWebUser()));
        getAgentRecieves().setCheque(calValue(new BilledBill(), BillType.AgentPaymentReceiveBill, PaymentMethod.Cheque, getWebUser()));
        getAgentRecieves().setCredit(calValue(new BilledBill(), BillType.AgentPaymentReceiveBill, PaymentMethod.Credit, getWebUser()));
        getAgentRecieves().setSlip(calValue(new BilledBill(), BillType.AgentPaymentReceiveBill, PaymentMethod.Slip, getWebUser()));

        //Agent Receive Cancel
        getAgentCancelBill().setBills(userBillsOwn(new CancelledBill(), BillType.AgentPaymentReceiveBill, getWebUser()));
        getAgentCancelBill().setCard(calValue(new CancelledBill(), BillType.AgentPaymentReceiveBill, PaymentMethod.Card, getWebUser()));
        getAgentCancelBill().setCash(calValue(new CancelledBill(), BillType.AgentPaymentReceiveBill, PaymentMethod.Cash, getWebUser()));
        getAgentCancelBill().setCheque(calValue(new CancelledBill(), BillType.AgentPaymentReceiveBill, PaymentMethod.Cheque, getWebUser()));
        getAgentCancelBill().setCredit(calValue(new CancelledBill(), BillType.AgentPaymentReceiveBill, PaymentMethod.Credit, getWebUser()));
        getAgentCancelBill().setSlip(calValue(new CancelledBill(), BillType.AgentPaymentReceiveBill, PaymentMethod.Slip, getWebUser()));

        //Inward Payment
        getInwardPayments().setBills(userBillsOwn(new BilledBill(), BillType.InwardPaymentBill, getWebUser()));
        getInwardPayments().setCard(calValue(new BilledBill(), BillType.InwardPaymentBill, PaymentMethod.Card, getWebUser()));
        getInwardPayments().setCash(calValue(new BilledBill(), BillType.InwardPaymentBill, PaymentMethod.Cash, getWebUser()));
        getInwardPayments().setCheque(calValue(new BilledBill(), BillType.InwardPaymentBill, PaymentMethod.Cheque, getWebUser()));
        getInwardPayments().setCredit(calValue(new BilledBill(), BillType.InwardPaymentBill, PaymentMethod.Credit, getWebUser()));
        getInwardPayments().setSlip(calValue(new BilledBill(), BillType.InwardPaymentBill, PaymentMethod.Slip, getWebUser()));

        //Inward Payment Cancel
        getInwardPaymentCancel().setBills(userBillsOwn(new CancelledBill(), BillType.InwardPaymentBill, getWebUser()));
        getInwardPaymentCancel().setCard(calValue(new CancelledBill(), BillType.InwardPaymentBill, PaymentMethod.Card, getWebUser()));
        getInwardPaymentCancel().setCash(calValue(new CancelledBill(), BillType.InwardPaymentBill, PaymentMethod.Cash, getWebUser()));
        getInwardPaymentCancel().setCheque(calValue(new CancelledBill(), BillType.InwardPaymentBill, PaymentMethod.Cheque, getWebUser()));
        getInwardPaymentCancel().setCredit(calValue(new CancelledBill(), BillType.InwardPaymentBill, PaymentMethod.Credit, getWebUser()));
        getInwardPaymentCancel().setSlip(calValue(new CancelledBill(), BillType.InwardPaymentBill, PaymentMethod.Slip, getWebUser()));

        //Phrmacy Bill Other
        getBilledBillsPh2().setBills(userPharmacyBillsOther(new BilledBill(), BillType.PharmacySale, getWebUser()));
        getBilledBillsPh2().setCard(calValueOther(new BilledBill(), BillType.PharmacySale, PaymentMethod.Card, getWebUser()));
        getBilledBillsPh2().setCash(calValueOther(new BilledBill(), BillType.PharmacySale, PaymentMethod.Cash, getWebUser()));
        getBilledBillsPh2().setCheque(calValueOther(new BilledBill(), BillType.PharmacySale, PaymentMethod.Cheque, getWebUser()));
        getBilledBillsPh2().setCredit(calValueOther(new BilledBill(), BillType.PharmacySale, PaymentMethod.Credit, getWebUser()));
        getBilledBillsPh2().setSlip(calValueOther(new BilledBill(), BillType.PharmacySale, PaymentMethod.Slip, getWebUser()));

        //Pharmacy Cancell Other
        getCancellededBillsPh2().setBills(userPharmacyBillsOther(new CancelledBill(), BillType.PharmacySale, getWebUser()));
        getCancellededBillsPh2().setCard(calValueOther(new CancelledBill(), BillType.PharmacySale, PaymentMethod.Card, getWebUser()));
        getCancellededBillsPh2().setCash(calValueOther(new CancelledBill(), BillType.PharmacySale, PaymentMethod.Cash, getWebUser()));
        getCancellededBillsPh2().setCheque(calValueOther(new CancelledBill(), BillType.PharmacySale, PaymentMethod.Cheque, getWebUser()));
        getCancellededBillsPh2().setCredit(calValueOther(new CancelledBill(), BillType.PharmacySale, PaymentMethod.Credit, getWebUser()));
        getCancellededBillsPh2().setSlip(calValueOther(new CancelledBill(), BillType.PharmacySale, PaymentMethod.Slip, getWebUser()));

        //Pharmacy Refunded Other
        getRefundedBillsPh2().setBills(userPharmacyBillsOther(new RefundBill(), BillType.PharmacySale, getWebUser()));
        getRefundedBillsPh2().setCard(calValueOther(new RefundBill(), BillType.PharmacySale, PaymentMethod.Card, getWebUser()));
        getRefundedBillsPh2().setCash(calValueOther(new RefundBill(), BillType.PharmacySale, PaymentMethod.Cash, getWebUser()));
        getRefundedBillsPh2().setCheque(calValueOther(new RefundBill(), BillType.PharmacySale, PaymentMethod.Cheque, getWebUser()));
        getRefundedBillsPh2().setCredit(calValueOther(new RefundBill(), BillType.PharmacySale, PaymentMethod.Credit, getWebUser()));
        getRefundedBillsPh2().setSlip(calValueOther(new RefundBill(), BillType.PharmacySale, PaymentMethod.Slip, getWebUser()));

    }

    public void createGrnDetailTable() {
        recreteModal();

        grnBilled = new BillsTotals();
        grnCancelled = new BillsTotals();
        grnReturn = new BillsTotals();
        grnReturnCancel = new BillsTotals();

        if (getDepartment() == null) {
            return;
        }

        //GRN Billed Bills
        getGrnBilled().setBills(grnBills(new BilledBill(), BillType.PharmacyGrnBill, getDepartment()));
        getGrnBilled().setCash(calValue(new BilledBill(), BillType.PharmacyGrnBill, PaymentMethod.Cash, getDepartment()));
        getGrnBilled().setCredit(calValue(new BilledBill(), BillType.PharmacyGrnBill, PaymentMethod.Credit, getDepartment()));

        //GRN Cancelled Bill
        getGrnCancelled().setBills(grnBills(new CancelledBill(), BillType.PharmacyGrnBill, getDepartment()));
        getGrnCancelled().setCash(calValue(new CancelledBill(), BillType.PharmacyGrnBill, PaymentMethod.Cash, getDepartment()));
        getGrnCancelled().setCredit(calValue(new CancelledBill(), BillType.PharmacyGrnBill, PaymentMethod.Credit, getDepartment()));

        //GRN Refunded Bill
        getGrnReturn().setBills(grnBills(new BilledBill(), BillType.PharmacyGrnReturn, getDepartment()));
        getGrnReturn().setCash(calValue(new BilledBill(), BillType.PharmacyGrnReturn, PaymentMethod.Cash, getDepartment()));
        getGrnReturn().setCredit(calValue(new BilledBill(), BillType.PharmacyGrnReturn, PaymentMethod.Credit, getDepartment()));

        //GRN Refunded Bill Cancel
        getGrnReturnCancel().setBills(grnBills(new CancelledBill(), BillType.PharmacyGrnReturn, getDepartment()));
        getGrnReturnCancel().setCash(calValue(new CancelledBill(), BillType.PharmacyGrnReturn, PaymentMethod.Cash, getDepartment()));
        getGrnReturnCancel().setCredit(calValue(new CancelledBill(), BillType.PharmacyGrnReturn, PaymentMethod.Credit, getDepartment()));

    }

    public void createGrnDetailTableByDealor() {
        recreateList();

        grnBilled = new BillsTotals();
        grnCancelled = new BillsTotals();
        grnReturn = new BillsTotals();
        grnReturnCancel = new BillsTotals();

        if (getDepartment() == null || getInstitution() == null) {
            return;
        }

        //GRN Billed Bills
        getGrnBilled().setBills(grnBills(new BilledBill(), BillType.PharmacyGrnBill, getDepartment(), getInstitution()));
        getGrnBilled().setCash(calValue(new BilledBill(), BillType.PharmacyGrnBill, PaymentMethod.Cash, getDepartment(), getInstitution()));
        getGrnBilled().setCredit(calValue(new BilledBill(), BillType.PharmacyGrnBill, PaymentMethod.Credit, getDepartment(), getInstitution()));

        //GRN Cancelled Bill
        getGrnCancelled().setBills(grnBills(new CancelledBill(), BillType.PharmacyGrnBill, getDepartment(), getInstitution()));
        getGrnCancelled().setCash(calValue(new CancelledBill(), BillType.PharmacyGrnBill, PaymentMethod.Cash, getDepartment(), getInstitution()));
        getGrnCancelled().setCredit(calValue(new CancelledBill(), BillType.PharmacyGrnBill, PaymentMethod.Credit, getDepartment(), getInstitution()));

        //GRN Refunded Bill
        getGrnReturn().setBills(grnBills(new BilledBill(), BillType.PharmacyGrnReturn, getDepartment(), getInstitution()));
        getGrnReturn().setCash(calValue(new BilledBill(), BillType.PharmacyGrnReturn, PaymentMethod.Cash, getDepartment(), getInstitution()));
        getGrnReturn().setCredit(calValue(new BilledBill(), BillType.PharmacyGrnReturn, PaymentMethod.Credit, getDepartment(), getInstitution()));

        //GRN Refunded Bill Cancel
        getGrnReturnCancel().setBills(grnBills(new CancelledBill(), BillType.PharmacyGrnReturn, getDepartment(), getInstitution()));
        getGrnReturnCancel().setCash(calValue(new CancelledBill(), BillType.PharmacyGrnReturn, PaymentMethod.Cash, getDepartment(), getInstitution()));
        getGrnReturnCancel().setCredit(calValue(new CancelledBill(), BillType.PharmacyGrnReturn, PaymentMethod.Credit, getDepartment(), getInstitution()));

    }

    private void recreateList() {
        billedBills = null;
        cancellededBills = null;
        refundedBills = null;
        billedBillsPh = null;
        cancellededBillsPh = null;
        refundedBillsPh = null;
        billedBillsPh2 = null;
        cancellededBillsPh2 = null;
        refundedBillsPh2 = null;
        pettyPayments = null;
        pettyPaymentsCancel = null;
        agentCancelBill = null;
        agentRecieves = null;
        cashRecieves = null;
        cashRecieveCancel = null;
        paymentBills = null;
        paymentCancelBills = null;
        inwardPayments = null;
        inwardPaymentCancel = null;
        grnBilled = null;
        grnCancelled = null;
        grnReturn = null;
        grnReturnCancel = null;
    }

    public void recreteModal() {
        collectingIns = null;
        dataTableData = null;
        institution = null;
        //  department=null;
        recreateList();
    }

    public Institution getCollectingIns() {
        return collectingIns;
    }

    public void setCollectingIns(Institution collectingIns) {
        //recreteModal();
        this.collectingIns = collectingIns;
    }

    public double getFinalChequeTot(List<BillsTotals> list) {

        double tmp = 0.0;
        for (BillsTotals bt : list) {
            if (bt != null) {
                tmp += bt.getCheque();
            }
        }

        return tmp;
    }

    public double getFinalSlipTot(List<BillsTotals> list) {

        double tmp = 0.0;
        for (BillsTotals bt : list) {
            if (bt != null) {
                tmp += bt.getSlip();
            }
        }

        return tmp;
    }

    public double getFinalCashTotal(List<BillsTotals> list) {

        double tmp = 0.0;
        for (BillsTotals bt : list) {
            if (bt != null) {
                tmp += bt.getCash();
            }
        }

        return tmp;
    }

    public BillsTotals getBilledBills() {
        if (billedBills == null) {
            billedBills = new BillsTotals();
            //  billedBills.setBillType(BillType.OpdBill);
        }
        return billedBills;
    }

    public void setBilledBills(BillsTotals billedBills) {
        this.billedBills = billedBills;
    }

    public BillsTotals getCancellededBills() {
        if (cancellededBills == null) {
            cancellededBills = new BillsTotals();
            //   cancellededBills.setBillType(BillType.OpdBill);
        }
        return cancellededBills;
    }

    public void setCancellededBills(BillsTotals cancellededBills) {
        this.cancellededBills = cancellededBills;
    }

    public BillsTotals getRefundedBills() {
        if (refundedBills == null) {
            refundedBills = new BillsTotals();
            //    refundedBills.setBillType(BillType.OpdBill);
        }
        return refundedBills;
    }

    public void setRefundedBills(BillsTotals refundedBills) {
        this.refundedBills = refundedBills;
    }

    public BillsTotals getPaymentBills() {
        if (paymentBills == null) {
            paymentBills = new BillsTotals();
            //    paymentBills.setBillType(BillType.PaymentBill);
        }
        return paymentBills;
    }

    public void setPaymentBills(BillsTotals paymentBills) {
        this.paymentBills = paymentBills;
    }

    public BillsTotals getPaymentCancelBills() {
        if (paymentCancelBills == null) {
            paymentCancelBills = new BillsTotals();
            //    paymentCancelBills.setBillType(BillType.PaymentBill);
        }
        return paymentCancelBills;
    }

    public void setPaymentCancelBills(BillsTotals paymentCancelBills) {
        this.paymentCancelBills = paymentCancelBills;
    }

    public BillsTotals getPettyPayments() {
        if (pettyPayments == null) {
            pettyPayments = new BillsTotals();
            //    pettyPayments.setBillType(BillType.PettyCash);
        }
        return pettyPayments;
    }

    public void setPettyPayments(BillsTotals pettyPayments) {
        this.pettyPayments = pettyPayments;
    }

    public BillsTotals getPettyPaymentsCancel() {
        if (pettyPaymentsCancel == null) {
            pettyPaymentsCancel = new BillsTotals();
            //     pettyPaymentsCancel.setBillType(BillType.PettyCash);
        }
        return pettyPaymentsCancel;
    }

    public void setPettyPaymentsCancel(BillsTotals pettyPaymentsCancel) {
        this.pettyPaymentsCancel = pettyPaymentsCancel;
    }

    public BillsTotals getCashRecieves() {
        if (cashRecieves == null) {
            cashRecieves = new BillsTotals();
            //    cashRecieves.setBillType(BillType.CashRecieveBill);
        }
        return cashRecieves;
    }

    public void setCashRecieves(BillsTotals cashRecieves) {
        this.cashRecieves = cashRecieves;
    }

    public BillsTotals getCashRecieveCancel() {
        if (cashRecieveCancel == null) {
            cashRecieveCancel = new BillsTotals();
            //   cashRecieveCancel.setBillType(BillType.CashRecieveBill);
        }
        return cashRecieveCancel;
    }

    public void setCashRecieveCancel(BillsTotals cashRecieveCancel) {
        this.cashRecieveCancel = cashRecieveCancel;
    }

    public BillsTotals getAgentRecieves() {
        if (agentRecieves == null) {
            agentRecieves = new BillsTotals();
            //  agentRecieves.setBillType(BillType.AgentPaymentReceiveBill);
        }
        return agentRecieves;
    }

    public void setAgentRecieves(BillsTotals agentRecieves) {
        this.agentRecieves = agentRecieves;
    }

    public BillsTotals getAgentCancelBill() {
        if (agentCancelBill == null) {
            agentCancelBill = new BillsTotals();
            //    agentCancelBill.setBillType(BillType.AgentPaymentReceiveBill);
        }
        return agentCancelBill;
    }

    public void setAgentCancelBill(BillsTotals agentCancelBill) {
        this.agentCancelBill = agentCancelBill;
    }

    public BillsTotals getInwardPayments() {
        if (inwardPayments == null) {
            inwardPayments = new BillsTotals();
            //   inwardPayments.setBillType(BillType.InwardPaymentBill);
        }
        return inwardPayments;
    }

    public void setInwardPayments(BillsTotals inwardPayments) {
        this.inwardPayments = inwardPayments;
    }

    public BillsTotals getInwardPaymentCancel() {
        if (inwardPaymentCancel == null) {
            inwardPaymentCancel = new BillsTotals();
            //    inwardPaymentCancel.setBillType(BillType.InwardPaymentBill);
        }
        return inwardPaymentCancel;
    }

    public void setInwardPaymentCancel(BillsTotals inwardPaymentCancel) {
        this.inwardPaymentCancel = inwardPaymentCancel;
    }

    public List<String1Value1> getCreditSlipSum() {
        List<BillsTotals> list2 = new ArrayList<>();
        list2.add(billedBills);
        list2.add(cancellededBills);
        list2.add(refundedBills);
        list2.add(billedBillsPh);
        list2.add(cancellededBillsPh);
        list2.add(refundedBillsPh);
        list2.add(paymentBills);
        list2.add(paymentCancelBills);
        list2.add(pettyPayments);
        list2.add(pettyPaymentsCancel);
        list2.add(agentRecieves);
        list2.add(agentCancelBill);
        list2.add(inwardPayments);
        list2.add(inwardPaymentCancel);
        list2.add(cashRecieves);
        list2.add(cashRecieveCancel);

        List<String1Value1> list = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list2));
        list.add(tmp1);

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Slip Total");
        tmp2.setValue(getFinalSlipTot(list2));
        list.add(tmp2);

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Total");
        tmp3.setValue(tmp1.getValue() + tmp2.getValue());
        list.add(tmp3);

        return list;
    }

    public List<String1Value1> getCreditSlipSum2() {
        List<BillsTotals> list2 = new ArrayList<>();
        list2.add(billedBillsPh2);
        list2.add(cancellededBillsPh2);
        list2.add(refundedBillsPh2);

        List<String1Value1> list = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list2));
        list.add(tmp1);

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Slip Total");
        tmp2.setValue(getFinalSlipTot(list2));
        list.add(tmp2);

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Total");
        tmp3.setValue(tmp1.getValue() + tmp2.getValue());
        list.add(tmp3);

        return list;
    }

    public List<String1Value1> getDataTableData() {
        List<BillsTotals> list = new ArrayList<>();
        list.add(getBilledBills());
        list.add(getCancellededBills());
        list.add(getRefundedBills());
        list.add(getBilledBillsPh());
        list.add(getCancellededBillsPh());
        list.add(getRefundedBillsPh());
        list.add(getPaymentBills());
        list.add(getPaymentCancelBills());
        list.add(getPettyPayments());
        list.add(getPettyPaymentsCancel());
        list.add(getAgentRecieves());
        list.add(getAgentCancelBill());
        list.add(getInwardPayments());
        list.add(getInwardPaymentCancel());
        list.add(getCashRecieves());
        list.add(getCashRecieveCancel());

        dataTableData = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list));

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Credit Card Total");
        tmp2.setValue(getFinalCreditCardTotal(list));

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cheque Total");
        tmp3.setValue(getFinalChequeTot(list));

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Final Slip Total");
        tmp4.setValue(getFinalSlipTot(list));

        String1Value1 tmp5 = new String1Value1();
        tmp5.setString("Final Cash Total");
        tmp5.setValue(getFinalCashTotal(list));

        dataTableData.add(tmp1);
        dataTableData.add(tmp2);
        dataTableData.add(tmp3);
        dataTableData.add(tmp4);
        dataTableData.add(tmp5);

        return dataTableData;
    }

    public List<String1Value1> getGrnTotal() {
        List<BillsTotals> list = new ArrayList<>();
        list.add(getGrnBilled());
        list.add(getGrnCancelled());
        list.add(getGrnReturn());
        list.add(getGrnReturnCancel());

        dataTableData = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list));

        String1Value1 tmp5 = new String1Value1();
        tmp5.setString("Final Cash Total");
        tmp5.setValue(getFinalCashTotal(list));

        dataTableData.add(tmp1);
        dataTableData.add(tmp5);

        return dataTableData;
    }

    public List<String1Value1> getDataTableDataByType() {
        List<BillsTotals> list = new ArrayList<>();
        if (billType == BillType.OpdBill) {
            list.add(getBilledBills());
            list.add(getCancellededBills());
            list.add(getRefundedBills());
        }
        if (billType == BillType.PharmacySale) {
            list.add(getBilledBillsPh());
            list.add(getCancellededBillsPh());
            list.add(getRefundedBillsPh());
        }

        if (billType == BillType.PaymentBill) {
            list.add(getPaymentBills());
            list.add(getPaymentCancelBills());
        }
        if (billType == BillType.PettyCash) {
            list.add(getPettyPayments());
            list.add(getPettyPaymentsCancel());
        }
        if (billType == BillType.AgentPaymentReceiveBill) {
            list.add(getAgentRecieves());
            list.add(getAgentCancelBill());
        }
        if (billType == BillType.InwardPaymentBill) {
            list.add(getInwardPayments());
            list.add(getInwardPaymentCancel());
        }
        if (billType == BillType.CashRecieveBill) {
            list.add(getCashRecieves());
            list.add(getCashRecieveCancel());
        }

        List< String1Value1> data = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list));

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Credit Card Total");
        tmp2.setValue(getFinalCreditCardTotal(list));

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cheque Total");
        tmp3.setValue(getFinalChequeTot(list));

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Final Slip Total");
        tmp4.setValue(getFinalSlipTot(list));

        String1Value1 tmp5 = new String1Value1();
        tmp5.setString("Final Cash Total");
        tmp5.setValue(getFinalCashTotal(list));

        data.add(tmp1);
        data.add(tmp2);
        data.add(tmp3);
        data.add(tmp4);
        data.add(tmp5);

        return data;
    }

    public List<String1Value1> getDataTableDataByType2() {
        List<BillsTotals> list = new ArrayList<>();

        list.add(getBilledBills());
        list.add(getCancellededBills());
        list.add(getRefundedBills());

        List< String1Value1> data = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotal(list));

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Credit Card Total");
        tmp2.setValue(getFinalCreditCardTotal(list));

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cheque Total");
        tmp3.setValue(getFinalChequeTot(list));

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Final Slip Total");
        tmp4.setValue(getFinalSlipTot(list));

        String1Value1 tmp5 = new String1Value1();
        tmp5.setString("Final Cash Total");
        tmp5.setValue(getFinalCashTotal(list));
        
        String1Value1 tmp6 = new String1Value1();
        tmp6.setString("Grant Total");
        tmp6.setValue(getFinalCreditTotal(list)+getFinalCreditCardTotal(list)+getFinalChequeTot(list)+getFinalSlipTot(list)+getFinalCashTotal(list));

        data.add(tmp1);
        data.add(tmp2);
        data.add(tmp3);
        data.add(tmp4);
        data.add(tmp5);
        data.add(tmp6);

        return data;
    }

    public List<String1Value1> getCashChequeSum() {
        List<BillsTotals> list2 = new ArrayList<>();
        list2.add(billedBills);
        list2.add(cancellededBills);
        list2.add(refundedBills);
        list2.add(billedBillsPh);
        list2.add(cancellededBillsPh);
        list2.add(refundedBillsPh);
        list2.add(paymentBills);
        list2.add(paymentCancelBills);
        list2.add(pettyPayments);
        list2.add(pettyPaymentsCancel);
        list2.add(agentRecieves);
        list2.add(agentCancelBill);
        list2.add(inwardPayments);
        list2.add(inwardPaymentCancel);
        list2.add(cashRecieves);
        list2.add(cashRecieveCancel);

        List<String1Value1> list = new ArrayList<>();

        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Card Total");
        tmp1.setValue(getFinalCreditCardTotal(list2));

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Cheque Total");
        tmp2.setValue(getFinalChequeTot(list2));

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cash Total");
        tmp3.setValue(getFinalCashTotal(list2));

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Total");
        tmp4.setValue(tmp1.getValue() + tmp2.getValue() + tmp3.getValue());

        list.add(tmp1);
        list.add(tmp2);
        list.add(tmp3);
        list.add(tmp4);
        return list;
    }

    public List<String1Value1> getCashChequeSum2() {
        List<BillsTotals> list2 = new ArrayList<>();
        list2.add(billedBillsPh2);
        list2.add(cancellededBillsPh2);
        list2.add(refundedBillsPh2);

        List<String1Value1> list = new ArrayList<>();

        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Card Total");
        tmp1.setValue(getFinalCreditCardTotal(list2));

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Cheque Total");
        tmp2.setValue(getFinalChequeTot(list2));

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cash Total");
        tmp3.setValue(getFinalCashTotal(list2));

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Total");
        tmp4.setValue(tmp1.getValue() + tmp2.getValue() + tmp3.getValue());

        list.add(tmp1);
        list.add(tmp2);
        list.add(tmp3);
        list.add(tmp4);
        return list;
    }

    public void setDataTableData(List<String1Value1> dataTableData) {
        this.dataTableData = dataTableData;
    }

    public BillType getBillType() {
        return billType;
    }

    public void setBillType(BillType billType) {
        this.billType = billType;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Institution getCreditCompany() {
        return creditCompany;
    }

    public void setCreditCompany(Institution creditCompany) {
        this.creditCompany = creditCompany;
    }

    public BillsTotals getBilledBillsPh() {
        if (billedBillsPh == null) {
            billedBillsPh = new BillsTotals();
        }
        return billedBillsPh;
    }

    public void setBilledBillsPh(BillsTotals billedBillsPh) {
        this.billedBillsPh = billedBillsPh;
    }

    public BillsTotals getCancellededBillsPh() {
        if (cancellededBillsPh == null) {
            cancellededBillsPh = new BillsTotals();
        }
        return cancellededBillsPh;
    }

    public void setCancellededBillsPh(BillsTotals cancellededBillsPh) {
        this.cancellededBillsPh = cancellededBillsPh;
    }

    public BillsTotals getRefundedBillsPh() {
        if (refundedBillsPh == null) {
            refundedBillsPh = new BillsTotals();
        }
        return refundedBillsPh;
    }

    public void setRefundedBillsPh(BillsTotals refundedBillsPh) {
        this.refundedBillsPh = refundedBillsPh;
    }

    public BillsTotals getBilledBillsPh2() {
        if (billedBillsPh2 == null) {
            billedBillsPh2 = new BillsTotals();
        }
        return billedBillsPh2;
    }

    public void setBilledBillsPh2(BillsTotals billedBillsPh2) {
        this.billedBillsPh2 = billedBillsPh2;
    }

    public BillsTotals getCancellededBillsPh2() {
        if (cancellededBillsPh2 == null) {
            cancellededBillsPh2 = new BillsTotals();
        }
        return cancellededBillsPh2;
    }

    public void setCancellededBillsPh2(BillsTotals cancellededBillsPh2) {
        this.cancellededBillsPh2 = cancellededBillsPh2;
    }

    public BillsTotals getRefundedBillsPh2() {
        if (refundedBillsPh2 == null) {
            refundedBillsPh2 = new BillsTotals();
        }
        return refundedBillsPh2;
    }

    public void setRefundedBillsPh2(BillsTotals refundedBillsPh2) {
        this.refundedBillsPh2 = refundedBillsPh2;
    }

    public BillsTotals getGrnBilled() {
        return grnBilled;
    }

    public void setGrnBilled(BillsTotals grnBilled) {
        this.grnBilled = grnBilled;
    }

    public BillsTotals getGrnCancelled() {
        return grnCancelled;
    }

    public void setGrnCancelled(BillsTotals grnCancelled) {
        this.grnCancelled = grnCancelled;
    }

    public BillsTotals getGrnReturn() {
        return grnReturn;
    }

    public void setGrnReturn(BillsTotals grnReturn) {
        this.grnReturn = grnReturn;
    }

    public BillsTotals getGrnReturnCancel() {
        return grnReturnCancel;
    }

    public void setGrnReturnCancel(BillsTotals grnReturnCancel) {
        this.grnReturnCancel = grnReturnCancel;
    }
}
