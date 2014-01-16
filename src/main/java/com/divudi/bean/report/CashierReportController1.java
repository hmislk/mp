/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.report;

import com.divudi.bean.SessionController;
import com.divudi.data.BillType;
import com.divudi.data.dataStructure.CashierSummeryData;
import com.divudi.data.PaymentMethod;
import com.divudi.data.table.String1Value5;
import com.divudi.data.table.String1Value1;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.RefundBill;
import com.divudi.entity.WebUser;
import com.divudi.facade.BillFacade;
import com.divudi.facade.WebUserFacade;
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
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Buddhika
 */
@Named
@SessionScoped
public class CashierReportController1 implements Serializable {

    @Inject
    private SessionController sessionController;
    @EJB
    private CommonFunctions commonFunction;
    @EJB
    private WebUserFacade webUserFacade;
    @EJB
    private BillFacade billFacade;
    private WebUser currentCashier;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fromDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date toDate;
    private List<WebUser> cashiers;
    private List<CashierSummeryData> cashierDatas;
    private double finalCashTot;
    private double finalCreditTot;
    private double finalCreditCardTot;
    private CashierSummeryData current;
    double finalChequeTot;
    private List<String1Value1> dataTableDatas;

    /**
     * Creates a new instance of CashierReportController
     */
    public void recreteModal() {
        finalCashTot = 0.0;
        finalChequeTot = 0.0;
        finalCreditCardTot = 0.0;
        finalCreditTot = 0.0;
        current = null;
        cashierDatas = null;
        cashiers = null;
        currentCashier = null;
        dataTableDatas = null;
    }

    public CashierReportController1() {
    }

    public List<CashierSummeryData> getCashierDatasOwn() {
        cashierDatas = new ArrayList<>();
        for (WebUser w : getCashiers()) {
            CashierSummeryData temp = new CashierSummeryData();
            temp.setCasheir(w);
            findSummeryOwn(temp, w);
            setDataTable(temp);
            cashierDatas.add(temp);

        }

        return cashierDatas;
    }

    private void setDataTable(CashierSummeryData c) {
        List<String1Value5> dataTable5Values = new ArrayList<>();

        if (c.getBilledCash() != 0 || c.getBilledCheque() != 0 || c.getBilledCredit() != 0
                || c.getBilledCreditCard() != 0 || c.getBilledSlip() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Billed");
            tmp.setValue1(c.getBilledCash());
            tmp.setValue2(c.getBilledCredit());
            tmp.setValue3(c.getBilledCreditCard());
            tmp.setValue4(c.getBilledCheque());
            tmp.setValue5(c.getBilledSlip());
            dataTable5Values.add(tmp);
        }

        if (c.getCancelledCash() != 0 || c.getCancelledCheque() != 0 || c.getCancelledCredit() != 0
                || c.getCancelledCreditCard() != 0 || c.getCancelledSlip() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Cancelled");
            tmp.setValue1(c.getCancelledCash());
            tmp.setValue2(c.getCancelledCredit());
            tmp.setValue3(c.getCancelledCreditCard());
            tmp.setValue4(c.getCancelledCheque());
            tmp.setValue5(c.getCancelledSlip());
            dataTable5Values.add(tmp);
        }

        if (c.getRefundCheque() != 0 || c.getRefundSlip() != 0 || c.getRefundedCash() != 0
                || c.getRefundedCredit() != 0 || c.getRefundedCreditCard() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Refunded");
            tmp.setValue1(c.getRefundedCash());
            tmp.setValue2(c.getRefundedCredit());
            tmp.setValue3(c.getRefundedCreditCard());
            tmp.setValue4(c.getRefundCheque());
            tmp.setValue5(c.getRefundSlip());
            dataTable5Values.add(tmp);
        }

        if (c.getPaymentCash() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Payment");
            tmp.setValue1(c.getPaymentCash());
            dataTable5Values.add(tmp);
        }

        if (c.getPaymentCashCancel() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Payment Cancel");
            tmp.setValue1(c.getPaymentCashCancel());
            dataTable5Values.add(tmp);
        }

        if (c.getPettyCash() != 0 || c.getPettyCheque() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Petty Cash");
            tmp.setValue1(c.getPettyCash());
            tmp.setValue4(c.getPettyCheque());
            dataTable5Values.add(tmp);
        }

        if (c.getPettyCancelCash() != 0 || c.getPettyCancelCheque() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Petty Cash Cancel");
            tmp.setValue1(c.getPettyCancelCash());
            tmp.setValue4(c.getPettyCancelCheque());
            dataTable5Values.add(tmp);
        }

        if (c.getCompanyCash() != 0 || c.getCompanyCheque() != 0 || c.getCompanySlip() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Credit Company Payment");
            tmp.setValue1(c.getCompanyCash());
            tmp.setValue4(c.getCompanyCheque());
            tmp.setValue5(c.getCompanySlip());
            dataTable5Values.add(tmp);
        }

        if (c.getCompanyCancelCash() != 0 || c.getCompanyCancelCheque() != 0 || c.getCompanyCancelSlip() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Credit Company Payment Cancel");
            tmp.setValue1(c.getCompanyCancelCash());
            tmp.setValue4(c.getCompanyCancelCheque());
            tmp.setValue5(c.getCompanyCancelSlip());
            dataTable5Values.add(tmp);
        }

        if (c.getAgentCash() != 0 || c.getAgentCheque() != 0 || c.getAgentSlip() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Agent Payment");
            tmp.setValue1(c.getAgentCash());
            tmp.setValue4(c.getAgentCheque());
            tmp.setValue5(c.getAgentSlip());
            dataTable5Values.add(tmp);
        }

        if (c.getAgentCancelCash() != 0 || c.getAgentCancelCheque() != 0 || c.getAgentCancelSlip() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Agent Payment Cancel");
            tmp.setValue1(c.getAgentCancelCash());
            tmp.setValue4(c.getAgentCancelCheque());
            tmp.setValue5(c.getAgentCancelSlip());
            dataTable5Values.add(tmp);
        }

        if (c.getInwardPaymentCash() != 0 || c.getInwardPaymentCheque() != 0 || c.getInwardPaymentSlip() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Inward Payment");
            tmp.setValue1(c.getInwardPaymentCash());
            tmp.setValue4(c.getInwardPaymentCheque());
            tmp.setValue5(c.getInwardPaymentSlip());
            dataTable5Values.add(tmp);
        }

        if (c.getInwardCancelCash() != 0 || c.getInwardCancelCheque() != 0 || c.getInwardCancelSlip() != 0) {
            String1Value5 tmp = new String1Value5();
            tmp.setString("Inward Payment Cancel");
            tmp.setValue1(c.getInwardCancelCash());
            tmp.setValue4(c.getInwardCancelCheque());
            tmp.setValue5(c.getInwardCancelSlip());
            dataTable5Values.add(tmp);
        }

        String1Value5 tmp = new String1Value5();
        tmp.setString("Net Total");
        tmp.setValue1(c.getNetCash());
        tmp.setValue2(c.getNetCredit());
        tmp.setValue3(c.getNetCreditCard());
        tmp.setValue4(c.getNetCheque());
        tmp.setValue5(c.getNetSlip());
        dataTable5Values.add(tmp);


        c.setDataTable5Value(dataTable5Values);
    }
    private double finalSlipTot = 0.0;

    public double getFinalChequeTot() {
        finalChequeTot = 0.0;
        for (CashierSummeryData s : getCashierDatas()) {
            finalChequeTot += s.getNetCheque();
        }
        return finalChequeTot;
    }

    public double getFinalChequeTotOwn() {
        finalChequeTot = 0.0;
        for (CashierSummeryData s : getCashierDatasOwn()) {
            finalChequeTot += s.getNetCheque();
        }
        return finalChequeTot;
    }

    void findSummeryOwn(CashierSummeryData c, WebUser w) {
        c.setBilledCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setBilledCredit(calTotOwn(w, new BilledBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setBilledCreditCard(calTotOwn(w, new BilledBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setBilledCheque(calTotOwn(w, new BilledBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setBilledSlip(calTotOwn(w, new BilledBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setCancelledCash(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setCancelledCredit(calTotOwn(w, new CancelledBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setCancelledCreditCard(calTotOwn(w, new CancelledBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setCancelledCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setCancelledSlip(calTotOwn(w, new CancelledBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setRefundedCash(calTotOwn(w, new RefundBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setRefundedCredit(calTotOwn(w, new RefundBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setRefundedCreditCard(calTotOwn(w, new RefundBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setRefundCheque(calTotOwn(w, new RefundBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setRefundSlip(calTotOwn(w, new RefundBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setPaymentCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.PaymentBill));
        c.setPaymentCashCancel(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.PaymentBill));

        c.setPettyCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.PettyCash));
        c.setPettyCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.PettyCash));

        c.setPettyCancelCash(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.PettyCash));
        c.setPettyCancelCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.PettyCash));

        c.setCompanyCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.CashRecieveBill));
        c.setCompanyCheque(calTotOwn(w, new BilledBill(), PaymentMethod.Cheque, BillType.CashRecieveBill));
        c.setCompanySlip(calTotOwn(w, new BilledBill(), PaymentMethod.Slip, BillType.CashRecieveBill));

        c.setCompanyCancelCash(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.CashRecieveBill));
        c.setCompanyCancelCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.CashRecieveBill));
        c.setCompanyCancelSlip(calTotOwn(w, new CancelledBill(), PaymentMethod.Slip, BillType.CashRecieveBill));

        c.setAgentCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.AgentPaymentReceiveBill));
        c.setAgentCheque(calTotOwn(w, new BilledBill(), PaymentMethod.Cheque, BillType.AgentPaymentReceiveBill));
        c.setAgentSlip(calTotOwn(w, new BilledBill(), PaymentMethod.Slip, BillType.AgentPaymentReceiveBill));

        c.setAgentCancelCash(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.AgentPaymentReceiveBill));
        c.setAgentCancelCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.AgentPaymentReceiveBill));
        c.setAgentCancelSlip(calTotOwn(w, new CancelledBill(), PaymentMethod.Slip, BillType.AgentPaymentReceiveBill));

        c.setInwardPaymentCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.InwardPaymentBill));
        c.setInwardPaymentCheque(calTotOwn(w, new BilledBill(), PaymentMethod.Cheque, BillType.InwardPaymentBill));
        c.setInwardPaymentSlip(calTotOwn(w, new BilledBill(), PaymentMethod.Slip, BillType.InwardPaymentBill));

        c.setInwardCancelCash(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.InwardPaymentBill));
        c.setInwardCancelCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.InwardPaymentBill));
        c.setInwardCancelSlip(calTotOwn(w, new CancelledBill(), PaymentMethod.Slip, BillType.InwardPaymentBill));
    }

    double calTotOwn(WebUser w, Bill billClass, PaymentMethod pM, BillType billType) {
        String sql;
        Map temMap = new HashMap();

        if (getSessionController().getInstitution() == null) {
            return 0.0;
        }

        if (billType == BillType.InwardPaymentBill) {
            sql = "select b from Bill b where type(b)=:bill and b.creater=:cret and "
                    + " b.paymentMethod=:payMethod  and b.institution=:ins"
                    + " and b.billType= :billTp and b.createdAt between :fromDate and :toDate ";
        } else {
            sql = "select b from Bill b where type(b)=:bill and b.creater=:cret and "
                    + " b.paymentScheme.paymentMethod= :payMethod  and b.institution=:ins"
                    + " and b.billType= :billTp and b.createdAt between :fromDate and :toDate ";
        }

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("payMethod", pM);
        temMap.put("bill", billClass.getClass());
        temMap.put("cret", w);
        temMap.put("ins", getSessionController().getInstitution());

        List<Bill> bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        double tot = 0;
        for (Bill b : bills) {
            tot += b.getNetTotal();
        }
        return tot;

    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunction().getStartOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
        recreteModal();

    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunction().getEndOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
        recreteModal();
    }

    public CommonFunctions getCommonFunction() {
        return commonFunction;
    }

    public void setCommonFunction(CommonFunctions commonFunction) {
        this.commonFunction = commonFunction;
    }

    public WebUserFacade getWebUserFacade() {
        return webUserFacade;
    }

    public void setWebUserFacade(WebUserFacade webUserFacade) {
        this.webUserFacade = webUserFacade;
    }

    public List<WebUser> getCashiers() {
        String sql;
        Map temMap = new HashMap();
        sql = "select us from WebUser us where us.id in (select b.creater.id from Bill b where b.createdAt between :fromDate and :toDate)";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        cashiers = getWebUserFacade().findBySQL(sql, temMap,TemporalType.TIMESTAMP);
        if (cashiers == null) {
            cashiers = new ArrayList<>();
        }

        return cashiers;
    }

    public void setCashiers(List<WebUser> cashiers) {
        this.cashiers = cashiers;

    }

    public List<CashierSummeryData> getCashierDatas() {
        cashierDatas = new ArrayList<>();
        for (WebUser w : getCashiers()) {
            CashierSummeryData temp = new CashierSummeryData();
            temp.setCasheir(w);
            findSummery(temp, w);
            setDataTable(temp);
            cashierDatas.add(temp);

        }

        return cashierDatas;
    }

    void findSummery(CashierSummeryData c, WebUser w) {
        c.setBilledCash(calTot(w, new BilledBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setBilledCredit(calTot(w, new BilledBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setBilledCreditCard(calTot(w, new BilledBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setBilledCheque(calTot(w, new BilledBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setBilledSlip(calTot(w, new BilledBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setCancelledCash(calTot(w, new CancelledBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setCancelledCredit(calTot(w, new CancelledBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setCancelledCreditCard(calTot(w, new CancelledBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setCancelledCheque(calTot(w, new CancelledBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setCancelledSlip(calTot(w, new CancelledBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setRefundedCash(calTot(w, new RefundBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setRefundedCredit(calTot(w, new RefundBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setRefundedCreditCard(calTot(w, new RefundBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setRefundCheque(calTot(w, new RefundBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setRefundSlip(calTot(w, new RefundBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setPaymentCash(calTot(w, new BilledBill(), PaymentMethod.Cash, BillType.PaymentBill));
        c.setPaymentCashCancel(calTot(w, new CancelledBill(), PaymentMethod.Cash, BillType.PaymentBill));

        c.setPettyCash(calTot(w, new BilledBill(), PaymentMethod.Cash, BillType.PettyCash));
        c.setPettyCheque(calTot(w, new BilledBill(), PaymentMethod.Cheque, BillType.PettyCash));

        c.setPettyCancelCash(calTot(w, new CancelledBill(), PaymentMethod.Cash, BillType.PettyCash));
        c.setPettyCancelCheque(calTot(w, new CancelledBill(), PaymentMethod.Cheque, BillType.PettyCash));

        c.setCompanyCash(calTot(w, new BilledBill(), PaymentMethod.Cash, BillType.CashRecieveBill));
        c.setCompanyCheque(calTot(w, new BilledBill(), PaymentMethod.Cheque, BillType.CashRecieveBill));
        c.setCompanySlip(calTot(w, new BilledBill(), PaymentMethod.Slip, BillType.CashRecieveBill));

        c.setCompanyCancelCash(calTot(w, new CancelledBill(), PaymentMethod.Cash, BillType.CashRecieveBill));
        c.setCompanyCancelCheque(calTot(w, new CancelledBill(), PaymentMethod.Cheque, BillType.CashRecieveBill));
        c.setCompanyCancelSlip(calTot(w, new CancelledBill(), PaymentMethod.Slip, BillType.CashRecieveBill));

        c.setAgentCash(calTot(w, new BilledBill(), PaymentMethod.Cash, BillType.AgentPaymentReceiveBill));
        c.setAgentCheque(calTot(w, new BilledBill(), PaymentMethod.Cheque, BillType.AgentPaymentReceiveBill));
        c.setAgentSlip(calTot(w, new BilledBill(), PaymentMethod.Slip, BillType.AgentPaymentReceiveBill));

        c.setAgentCancelCash(calTot(w, new CancelledBill(), PaymentMethod.Cash, BillType.AgentPaymentReceiveBill));
        c.setAgentCancelCheque(calTot(w, new CancelledBill(), PaymentMethod.Cheque, BillType.AgentPaymentReceiveBill));
        c.setAgentCancelSlip(calTot(w, new CancelledBill(), PaymentMethod.Slip, BillType.AgentPaymentReceiveBill));

    }

    double calTot(WebUser w, Bill bill, PaymentMethod paymentMethod, BillType billType) {
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b  where type(b)=:bill and b.creater=:web and "
                + "b.paymentScheme.paymentMethod= :pm "
                + " and b.billType= :billTp and b.createdAt between :fromDate and :toDate";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("pm", paymentMethod);
        temMap.put("bill", bill.getClass());
        temMap.put("web", w);
        List<Bill> bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        double tot = 0;
        for (Bill b : bills) {
            tot += b.getNetTotal();
        }
        return tot;
    }

    public void setCashierDatas(List<CashierSummeryData> cashierDatas) {
        this.cashierDatas = cashierDatas;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public double getFinalCashTot() {
        finalCashTot = 0.0;
        for (CashierSummeryData s : getCashierDatas()) {
            finalCashTot += s.getNetCash();
        }
        return finalCashTot;
    }

    public double getFinalCashTotOwn() {
        finalCashTot = 0.0;
        for (CashierSummeryData s : getCashierDatasOwn()) {
            finalCashTot += s.getNetCash();
        }
        return finalCashTot;
    }

    public void setFinalCashTot(double finalCashTot) {
        this.finalCashTot = finalCashTot;
    }

    public double getFinalCreditTot() {
        finalCreditTot = 0.0;
        for (CashierSummeryData s : getCashierDatas()) {
            finalCreditTot += s.getNetCredit();
        }
        return finalCreditTot;
    }

    public double getFinalCreditTotOwn() {
        finalCreditTot = 0.0;
        for (CashierSummeryData s : getCashierDatasOwn()) {
            finalCreditTot += s.getNetCredit();
        }
        return finalCreditTot;
    }

    public void setFinalCreditTot(double finalCreditTot) {
        this.finalCreditTot = finalCreditTot;
    }

    public double getFinalCreditCardTot() {
        finalCreditCardTot = 0.0;
        for (CashierSummeryData s : getCashierDatas()) {
            finalCreditCardTot += s.getNetCreditCard();
        }

        return finalCreditCardTot;
    }

    public double getFinalCreditCardTotOwn() {
        finalCreditCardTot = 0.0;
        for (CashierSummeryData s : getCashierDatasOwn()) {
            finalCreditCardTot += s.getNetCreditCard();
        }

        return finalCreditCardTot;
    }

    public void setFinalCreditCardTot(double finalCreditCardTot) {
        this.finalCreditCardTot = finalCreditCardTot;
    }

    public CashierSummeryData getCurrent() {
        if (current == null) {
            current = new CashierSummeryData();
        }

        return current;
    }

    public void setCurrent(CashierSummeryData current) {
        this.current = current;

    }

    public WebUser getCurrentCashier() {
        if (currentCashier == null) {
            currentCashier = new WebUser();
        }

        return currentCashier;

    }

    private void setCashierData() {
        current = null;

        getCurrent().setCasheir(getCurrentCashier());
        findSummery(getCurrent(), getCurrentCashier());

    }

    public void setCurrentCashier(WebUser currentCashier) {
        this.currentCashier = currentCashier;
        setCashierData();
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public double getFinalSlipTot() {
        for (CashierSummeryData s : getCashierDatas()) {
            finalSlipTot += s.getNetSlip();
        }
        return finalSlipTot;
    }

    public double getFinalSlipTotOwn() {
        finalSlipTot = 0.0;
        for (CashierSummeryData s : getCashierDatasOwn()) {
            finalSlipTot += s.getNetSlip();
        }
        return finalSlipTot;
    }

    public void setFinalSlipTot(double finalSlipTot) {
        this.finalSlipTot = finalSlipTot;
    }

    public List<String1Value1> getDataTableDatas() {
        dataTableDatas = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotOwn());

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Credit Card Total");
        tmp2.setValue(getFinalCreditCardTotOwn());

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cheque Total");
        tmp3.setValue(getFinalChequeTotOwn());

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Final Slip Total");
        tmp4.setValue(getFinalSlipTotOwn());

        String1Value1 tmp5 = new String1Value1();
        tmp5.setString("Final Cash Total");
        tmp5.setValue(getFinalCashTotOwn());

        dataTableDatas.add(tmp1);
        dataTableDatas.add(tmp2);
        dataTableDatas.add(tmp3);
        dataTableDatas.add(tmp4);
        dataTableDatas.add(tmp5);

        return dataTableDatas;
    }

    public void setDataTableDatas(List<String1Value1> dataTableDatas) {
        this.dataTableDatas = dataTableDatas;
    }
}
