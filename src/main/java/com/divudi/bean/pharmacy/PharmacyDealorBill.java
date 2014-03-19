/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.BillController;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.ejb.BillNumberBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Institution;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Inject;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import java.util.ArrayList;
import java.util.HashMap;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class PharmacyDealorBill implements Serializable {

    //Atribtes
    private boolean printPreview;
    private Bill current;
    private Institution chequeBank;
    private Institution slipBank;
    private Date chequeDate;
    private Date slipDate;
    private BillItem currentBillItem;
    private Institution institution;
    private int index;
    //List
    private List<BillItem> billItems;
    //EJB
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    //Inject
    @Inject
    private SessionController sessionController;

    public void makeNull() {
        printPreview = false;
        current = null;
        chequeBank = null;
        slipBank = null;
        currentBillItem = null;
        institution = null;
        index = 0;
        chequeDate = null;
        slipDate = null;
    }

    private boolean errorCheckForAdding() {
        if (getCurrentBillItem().getReferenceBill().getFromInstitution() == null) {
            UtilityController.addErrorMessage("U cant add without credit company name");
            return true;
        }

        if (!checkPaidAmount(getCurrentBillItem())) {
            UtilityController.addSuccessMessage("U cant add more than ballance");
            return true;
        }

        for (BillItem b : getBillItems()) {
            if (b.getReferenceBill() != null && b.getReferenceBill().getFromInstitution() != null) {
                if (getCurrentBillItem().getReferenceBill().getFromInstitution().getId() != b.getReferenceBill().getFromInstitution().getId()) {
                    UtilityController.addErrorMessage("U can add only one type Credit companies at Once");
                    return true;
                }
            }
        }

        return false;
    }

    public double getGrnReturnValue(Bill refBill) {
        String sql = "select sum(b.netTotal) from"
                + " Bill b where b.retired=false and "
                + " b.referenceBill=:refBill "
                + " and (b.billType=:bType1 or b.billType=:bType2 )";

        HashMap hm = new HashMap();
        hm.put("refBill", refBill);
        hm.put("bType1", BillType.PharmacyGrnReturn);
        hm.put("bType2", BillType.PurchaseReturn);
        return getBillFacade().findDoubleByJpql(sql, hm, TemporalType.DATE);
    }

    public void selectListener() {
        double grnReturnTotal = getGrnReturnValue(getCurrentBillItem().getReferenceBill());
        getCurrentBillItem().getReferenceBill().setTmpReturnTotal(grnReturnTotal);

        double ballanceAmt = getCurrentBillItem().getReferenceBill().getNetTotal()
                + grnReturnTotal
                + getCurrentBillItem().getReferenceBill().getPaidAmount();

        System.err.println("Ballance Amount " + ballanceAmt);
        if (ballanceAmt < 0.01) {
            getCurrentBillItem().setNetValue(0 - ballanceAmt);
        }

    }

    @Inject
    private BillController billController;

    public void selectInstitutionListener() {
        Institution ins = institution;
        makeNull();

        List<Bill> list = getBillController().getDealorBills(ins);
        for (Bill b : list) {
            getCurrentBillItem().setReferenceBill(b);
            selectListener();
            addToBill();
        }
    }

    public void addToBill() {

        if (errorCheckForAdding()) {
            return;
        }

        getCurrent().setToInstitution(getCurrentBillItem().getReferenceBill().getFromInstitution());
        //     getCurrentBillItem().getBill().setNetTotal(getCurrentBillItem().getNetValue());
        //     getCurrentBillItem().getBill().setTotal(getCurrent().getNetTotal());

        if (getCurrentBillItem().getNetValue() != 0) {
            System.err.println("11 " + getCurrentBillItem().getReferenceBill().getDeptId());
            System.err.println("aa " + getCurrentBillItem().getNetValue());
            getBillItems().add(getCurrentBillItem());
        }

        currentBillItem = null;
        calTotal();
    }

    public void changeNetValueListener(BillItem billItem) {

        if (!checkPaidAmount(billItem)) {
            billItem.setNetValue(0);
//            UtilityController.addSuccessMessage("U cant add more than ballance");
//            return;
        }

        calTotal();
    }

    public void calTotal() {
        double n = 0.0;
        for (BillItem b : billItems) {
            n += b.getNetValue();
        }
        getCurrent().setNetTotal(0 - n);
        // //System.out.println("AAA : " + n);
    }

    public void remove() {
        getBillItems().remove(index);
        calTotal();
    }

    private boolean errorCheck() {
        if (getBillItems().isEmpty()) {
            UtilityController.addErrorMessage("No Bill Item ");
            return true;
        }

        if (getCurrent().getToInstitution() == null) {
            UtilityController.addErrorMessage("Select Cant settle without Dealor");
            return true;
        }

        if (getCurrent().getPaymentMethod() != null && getCurrent().getPaymentMethod() == PaymentMethod.Cheque) {
            if (getChequeBank() == null || getCurrent().getChequeRefNo() == null || getChequeDate() == null) {
                UtilityController.addErrorMessage("Please select Cheque Number,Bank and Cheque Date");
                return true;
            }

        }

        if (getCurrent().getPaymentMethod() != null && getCurrent().getPaymentMethod() == PaymentMethod.Slip) {
            if (getSlipBank() == null || getCurrent().getComments() == null || getSlipDate() == null) {
                UtilityController.addErrorMessage("Please Fill Memo,Bank and Slip Date");
                return true;
            }

        }

        return false;
    }

    private void saveBill(BillType billType) {

        getCurrent().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getCurrent(), billType, BillNumberSuffix.CRDPAY));

        getCurrent().setBillType(billType);

        getCurrent().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getCurrent().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getCurrent().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

        getCurrent().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setCreater(getSessionController().getLoggedUser());

        getCurrent().setNetTotal(getCurrent().getNetTotal());

        if (getCurrent().getId() == null) {
            getBillFacade().create(getCurrent());
        } else {
            getBillFacade().edit(getCurrent());
        }

    }

    public void settleBill() {
        if (errorCheck()) {
            return;
        }

        if (getCurrent().getPaymentMethod() == PaymentMethod.Cheque) {
            getCurrent().setBank(getChequeBank());
            getCurrent().setChequeDate(getChequeDate());
        } else if (getCurrent().getPaymentMethod() == PaymentMethod.Slip) {
            getCurrent().setBank(getSlipBank());
            getCurrent().setChequeDate(getSlipDate());
        }

        getCurrent().setTotal(getCurrent().getNetTotal());

        saveBill(BillType.GrnPayment);
        saveBillItem();

        UtilityController.addSuccessMessage("Bill Saved");
        printPreview = true;
    }

    private void saveBillItem() {
        for (BillItem tmp : getBillItems()) {
            tmp.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            tmp.setCreater(getSessionController().getLoggedUser());
            tmp.setBill(getCurrent());
            tmp.setNetValue(0 - tmp.getNetValue());
            getBillItemFacade().create(tmp);

            updateReferenceBill(tmp);

        }

    }

    private boolean checkPaidAmount(BillItem tmp) {
        double ballance, refBallance = 0;

        //System.err.println("Paid Amount " + tmp.getReferenceBill().getPaidAmount());
        //System.err.println("GRN Value " + tmp.getReferenceBill().getNetTotal());
        //System.err.println("GRN Return Value " + tmp.getReferenceBill().getTmpReturnTotal());
        //System.err.println("Entered Amount " + tmp.getNetValue());
        refBallance = tmp.getReferenceBill().getTmpReturnTotal() + tmp.getReferenceBill().getNetTotal() + tmp.getReferenceBill().getPaidAmount();

        //System.err.println("refBallance " + refBallance);
        //   ballance=refBallance-tmp.getNetValue();
        if (refBallance <= (0 - tmp.getNetValue())) {
            return true;
        }

        return false;
    }

    private void updateReferenceBill(BillItem tmp) {
        tmp.getReferenceBill().setPaidAmount(0 - tmp.getNetValue());
        getBillFacade().edit(tmp.getReferenceBill());

    }

    /**
     * Creates a new instance of pharmacyDealorBill
     */
    public PharmacyDealorBill() {
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public Bill getCurrent() {
        if (current == null) {
            current = new BilledBill();
        }
        return current;
    }

    public void setCurrent(Bill current) {
        this.current = current;
    }

    public Institution getChequeBank() {
        return chequeBank;
    }

    public void setChequeBank(Institution chequeBank) {
        this.chequeBank = chequeBank;
    }

    public Date getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(Date chequeDate) {
        this.chequeDate = chequeDate;
    }

    public Date getSlipDate() {
        return slipDate;
    }

    public void setSlipDate(Date slipDate) {
        this.slipDate = slipDate;
    }

    public BillItem getCurrentBillItem() {
        if (currentBillItem == null) {
            currentBillItem = new BillItem();
        }
        return currentBillItem;
    }

    public void setCurrentBillItem(BillItem currentBillItem) {
        this.currentBillItem = currentBillItem;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<BillItem> getBillItems() {
        if (billItems == null) {
            billItems = new ArrayList<>();
        }
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public Institution getSlipBank() {
        return slipBank;
    }

    public void setSlipBank(Institution slipBank) {
        this.slipBank = slipBank;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public BillController getBillController() {
        return billController;
    }

    public void setBillController(BillController billController) {
        this.billController = billController;
    }

}
