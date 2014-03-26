/*
 * To change this currentlate, choose Tools | currentlates
 * and open the currentlate in the editor.
 */
package com.divudi.bean;

import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.PaymentMethodData;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Institution;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Inject;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class CashRecieveBillController implements Serializable {

    private Bill current;
    private boolean printPreview = false;
    @EJB
    private BillNumberBean billNumberBean;
    @Inject
    private SessionController sessionController;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    private String tabId = "tabOpd";
    private BillItem currentBillItem;
    private BillItem removingItem;
    private List<BillItem> billItems;
    private int index;
    private PaymentMethodData paymentMethodData;

    public void remove() {
        billItems.remove(index);
        calTotal();
    }

    private boolean errorCheckForAdding() {
        if (getCurrentBillItem().getReferenceBill().getCreditCompany() == null) {
            UtilityController.addErrorMessage("U cant add without credit company name");
            return true;
        }

        for (BillItem b : getBillItems()) {
            if (b.getReferenceBill() != null && b.getReferenceBill().getCreditCompany() != null) {
                if (getCurrentBillItem().getReferenceBill().getCreditCompany().getId() != b.getReferenceBill().getCreditCompany().getId()) {
                    UtilityController.addErrorMessage("U can add only one type Credit companies at Once");
                    return true;
                }
            }
        }

        return false;
    }

    private boolean errorCheckForBht() {
        if (getCurrentBillItem().getPatientEncounter() == null) {
            UtilityController.addErrorMessage("Select Bht");
            return true;
        }

        if (getCurrentBillItem().getPatientEncounter().getCreditUsedAmount() == 0.0) {
            UtilityController.addErrorMessage("No Due to Add");
            return true;
        }

        if (getCurrentBillItem().getPatientEncounter().getCreditCompany() != null) {
            UtilityController.addErrorMessage("U cant add Without credit company");
            return true;
        }

        for (BillItem b : getBillItems()) {
            if (getCurrentBillItem().getPatientEncounter().getCreditCompany().getId() != b.getPatientEncounter().getCreditCompany().getId()) {
                UtilityController.addErrorMessage("U can add only one type Credit companies at Once");
                return true;
            }
        }

        return false;
    }

    public void addToBill() {
        if (errorCheckForAdding()) {
            return;
        }

        getCurrent().setFromInstitution(getCurrentBillItem().getReferenceBill().getCreditCompany());
        //     getCurrentBillItem().getBill().setNetTotal(getCurrentBillItem().getNetValue());
        //     getCurrentBillItem().getBill().setTotal(getCurrent().getNetTotal());

        getBillItems().add(getCurrentBillItem());

        currentBillItem = null;
        calTotal();

    }

    private List<Bill> creditBills;

    public void addAllBill() {
        for (Bill b : creditBills) {
            currentBillItem = null;
            getCurrentBillItem().setReferenceBill(b);
            addToBill();
        }
    }

    public void addBhtToBill() {
        if (errorCheckForBht()) {
            return;
        }

        getCurrent().setFromInstitution(getCurrentBillItem().getPatientEncounter().getCreditCompany());
        getCurrentBillItem().setNetValue(getCurrentBillItem().getPatientEncounter().getCreditUsedAmount());
        getBillItems().add(getCurrentBillItem());

        currentBillItem = null;
        calTotal();
    }

    private void calTotal() {
        double n = 0.0;
        for (BillItem b : billItems) {
            n += b.getNetValue();
        }
        getCurrent().setNetTotal(n);
        //System.out.println("AAA : " + n);
    }

    public void onTabChange(TabChangeEvent event) {
        setTabId(event.getTab().getId());

    }

//    public double getDue() {
//        if (getPatientEncounter() == null) {
//            return 0.0;
//        }
//
//        String sql = "SELECT  sum(b.netTotal) FROM BilledBill b WHERE b.retired=false  and b.billType=com.divudi.data.BillType.InwardBill and b.cancelledBill is null and b.patientEncounter.id=" + getPatientEncounter().getId();
//        Double tmp = getBillFacade().findAggregateDbl(sql);
//
//        sql = "SELECT  sum(b.netTotal) FROM BilledBill b WHERE b.retired=false  and b.billType=com.divudi.data.BillType.InwardPaymentBill and b.cancelledBill is null and b.patientEncounter.id=" + getPatientEncounter().getId();
//        tmp = tmp - getBillFacade().findAggregateDbl(sql);
//
//        return tmp;
//    }
    public CashRecieveBillController() {
    }

    @Inject
    private PaymentSchemeController paymentSchemeController;

    private boolean errorCheck() {
        if (getBillItems().isEmpty()) {
            UtilityController.addErrorMessage("No Bill Item ");
            return true;
        }

        if (getCurrent().getFromInstitution() == null) {
            UtilityController.addErrorMessage("Select Credit Company");
            return true;
        }

        if (getTabId().equals("tabOpd")) {
            if (getBillItems().get(0).getReferenceBill().getCreditCompany().getId()
                    != getCurrent().getFromInstitution().getId()) {
                UtilityController.addErrorMessage("Select same credit company as BillItem ");
                return true;
            }
        }

        if (getCurrent().getPaymentScheme() == null) {
            return true;
        }

        if (getCurrent().getPaymentScheme().getPaymentMethod() == null) {
            return true;
        }

        if (getPaymentSchemeController().errorCheckPaymentScheme(getCurrent().getPaymentScheme().getPaymentMethod(), getPaymentMethodData())) {
            return true;
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

    @EJB
    private BillBean billBean;

    public void settleBill() {

        if (errorCheck()) {
            return;
        }

        getBillBean().setPaymentMethodData(getCurrent(), getCurrent().getPaymentScheme().getPaymentMethod(), getPaymentMethodData());

        getCurrent().setTotal(getCurrent().getNetTotal());

        saveBill(BillType.CashRecieveBill);
        saveBillItem();

        if (getTabId().equals("tabBht")) {
            savePayments();
        }

        UtilityController.addSuccessMessage("Bill Saved");
        printPreview = true;

    }

    private void savePayments() {
        for (BillItem b : getBillItems()) {
            Bill bil = saveBhtPaymentBill(b);
            saveBhtBillItem(bil);
        }
    }

    private Bill saveBhtPaymentBill(BillItem b) {
        Bill tmp = new BilledBill();
        tmp.setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), getSessionController().getDepartment(), BillType.InwardPaymentBill));
        tmp.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), tmp, tmp.getBillType(), BillNumberSuffix.INWPAY));

        tmp.setBillType(BillType.InwardPaymentBill);
        tmp.setPatientEncounter(b.getPatientEncounter());
        tmp.setPatient(b.getPatientEncounter().getPatient());
        tmp.setPaymentScheme(getCurrent().getPaymentScheme());
        tmp.setNetTotal(b.getNetValue());
        tmp.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        tmp.setCreater(getSessionController().getLoggedUser());
        getBillFacade().create(tmp);

        return tmp;
    }

    private void saveBhtBillItem(Bill b) {
        BillItem temBi = new BillItem();
        temBi.setBill(b);
        temBi.setNetValue(b.getNetTotal());
        temBi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        temBi.setCreater(getSessionController().getLoggedUser());
        getBillItemFacade().create(temBi);
    }

    private void saveBillItem() {
        for (BillItem tmp : getBillItems()) {
            tmp.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            tmp.setCreater(getSessionController().getLoggedUser());
            tmp.setBill(getCurrent());
            tmp.setNetValue(tmp.getNetValue());
            getBillItemFacade().create(tmp);

            updateReferenceBill(tmp);

        }

    }

    private void updateReferenceBill(BillItem tmp) {
        double ballance, refBallance = 0;

        //System.err.println("Paid Amount " + tmp.getReferenceBill().getPaidAmount());
        //System.err.println("Net Total " + tmp.getReferenceBill().getNetTotal());
        //System.err.println("Net Value " + tmp.getNetValue());
        refBallance = tmp.getReferenceBill().getNetTotal() - tmp.getReferenceBill().getPaidAmount();

        //System.err.println("refBallance " + refBallance);
        //   ballance=refBallance-tmp.getNetValue();
        if (refBallance > tmp.getNetValue()) {
            tmp.getReferenceBill().setPaidAmount(tmp.getReferenceBill().getPaidAmount() + tmp.getNetValue());
        } else {
            tmp.getReferenceBill().setPaidAmount(refBallance - tmp.getNetValue());
        }
        //System.err.println("Updated " + tmp.getReferenceBill().getPaidAmount());

//        if (tmp.getReferenceBill().getPaidAmount() != 0.0) {
//            tmp.getReferenceBill().setPaidAmount(tmp.getReferenceBill().getPaidAmount() + tmp.getNetValue());
//        } else {
//            tmp.getReferenceBill().setPaidAmount(tmp.getNetValue());
//        }
        getBillFacade().edit(tmp.getReferenceBill());

    }

    public void recreateModel() {
        current = null;
        printPreview = false;
        currentBillItem = null;
        paymentMethodData = null;
        billItems = null;
        tabId = "tabOpd";

    }

    public String prepareNewBill() {
        recreateModel();
        return "";
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

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
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

    public String getTabId() {
        return tabId;
    }

    public void setTabId(String tabId) {
        this.tabId = tabId;
    }

    public BillItem getCurrentBillItem() {
        if (currentBillItem == null) {
            currentBillItem = new BillItem();
            //  currentBillItem.setBill(new );
        }
        return currentBillItem;
    }

    public void setCurrentBillItem(BillItem currentBillItem) {
        this.currentBillItem = currentBillItem;
    }

    public List<BillItem> getBillItems() {
        if (billItems == null) {
            billItems = new ArrayList<BillItem>();
        }
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public BillItem getRemovingItem() {
        return removingItem;
    }

    public void setRemovingItem(BillItem removingItem) {
        this.removingItem = removingItem;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Bill> getCreditBills() {
        return creditBills;
    }

    public void setCreditBills(List<Bill> creditBills) {
        recreateModel();
        this.creditBills = creditBills;
        addAllBill();
    }

    public PaymentMethodData getPaymentMethodData() {
        if (paymentMethodData == null) {
            paymentMethodData = new PaymentMethodData();
        }
        return paymentMethodData;
    }

    public void setPaymentMethodData(PaymentMethodData paymentMethodData) {
        this.paymentMethodData = paymentMethodData;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public PaymentSchemeController getPaymentSchemeController() {
        return paymentSchemeController;
    }

    public void setPaymentSchemeController(PaymentSchemeController paymentSchemeController) {
        this.paymentSchemeController = paymentSchemeController;
    }
}
