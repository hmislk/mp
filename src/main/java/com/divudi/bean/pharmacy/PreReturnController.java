/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyCalculation;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.RefundBill;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class PreReturnController implements Serializable {

    private Bill bill;
    private Bill returnBill;
    private boolean printPreview;
    ////////

    private List<BillItem> billItems;
    ///////
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @Inject
    private PharmaceuticalItemController pharmaceuticalItemController;
    @Inject
    private PharmacyController pharmacyController;
    @Inject
    private SessionController sessionController;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private BillFacade billFacade;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private BillItemFacade billItemFacade;

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        makeNull();

        if (bill.getDepartment() == null) {
            return;
        }

        if (getSessionController().getDepartment().getId() != bill.getDepartment().getId()) {
            UtilityController.addErrorMessage("U can't return another department's Issue.please log to specific department");
            return;
        }

        this.bill = bill;
        generateBillComponent(BillType.PharmacyPre);
    }

    public Bill getReturnBill() {
        if (returnBill == null) {
            returnBill = new RefundBill();
            returnBill.setBillType(BillType.PharmacyPre);

        }

        return returnBill;
    }

    public void setReturnBill(Bill returnBill) {
        this.returnBill = returnBill;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    @EJB
    private PharmacyCalculation pharmacyRecieveBean;

    public void onEdit(BillItem tmp) {
        //    PharmaceuticalBillItem tmp = (PharmaceuticalBillItem) event.getObject();

        if (tmp.getQty() > getPharmacyRecieveBean().calQty3(tmp.getReferanceBillItem())) {
            tmp.setQty(0.0);
            UtilityController.addErrorMessage("You cant return over than ballanced Qty ");
        }

        calTotal();
        //   getPharmacyController().setPharmacyItem(tmp.getPharmaceuticalBillItem().getBillItem().getItem());
    }

    public void makeNull() {
        bill = null;
        returnBill = null;
        printPreview = false;
        billItems = null;

    }

    private void saveReturnBill() {
        getReturnBill().setBilledBill(getBill());

        getReturnBill().copy(getBill());

        double dbl = 0 - getReturnBill().getTotal();

        getReturnBill().setTotal(dbl);
        getReturnBill().setNetTotal(dbl);

        getReturnBill().setCreater(getSessionController().getLoggedUser());
        getReturnBill().setCreatedAt(Calendar.getInstance().getTime());

        getReturnBill().setDepartment(getSessionController().getDepartment());
        getReturnBill().setInstitution(getSessionController().getInstitution());

        getReturnBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(
                getSessionController().getInstitution(), new RefundBill(), BillType.PharmacyPre, BillNumberSuffix.PHRET));

        getReturnBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(
                getSessionController().getDepartment(), new RefundBill(), BillType.PharmacyPre, BillNumberSuffix.PHRET));

        //   getReturnBill().setInsId(getBill().getInsId());
        getBillFacade().create(getReturnBill());

    }

    private void saveComponent() {
        for (BillItem i : getBillItems()) {
            i.getPharmaceuticalBillItem().setQtyInUnit((double) (double) i.getQty());

            if (i.getPharmaceuticalBillItem().getQty() == 0.0) {
                continue;
            }

            i.setBill(getReturnBill());
            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());
            i.setQty((double) i.getPharmaceuticalBillItem().getQty());

            double value = i.getNetRate() * i.getQty();
            i.setGrossValue(0 - value);
            i.setNetValue(0 - value);

            PharmaceuticalBillItem tmpPh = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);
            getBillItemFacade().create(i);

            getPharmaceuticalBillItemFacade().create(tmpPh);

            i.setPharmaceuticalBillItem(tmpPh);
            getBillItemFacade().edit(i);

            //   getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
            ////System.err.println("STOCK " + i.getPharmaceuticalBillItem().getStock());
            getPharmacyBean().addToStock(i.getPharmaceuticalBillItem().getStock(), Math.abs(i.getPharmaceuticalBillItem().getQtyInUnit()), i.getPharmaceuticalBillItem(), getSessionController().getDepartment());

            //   i.getBillItem().getTmpReferenceBillItem().getPharmaceuticalBillItem().setRemainingQty(i.getRemainingQty() - i.getQty());
            //   getPharmaceuticalBillItemFacade().edit(i.getBillItem().getTmpReferenceBillItem().getPharmaceuticalBillItem());
            //      updateRemainingQty(i);
            getReturnBill().getBillItems().add(i);
        }

    }

    public void settle() {

        saveReturnBill();
        saveComponent();

        getBillFacade().edit(getReturnBill());

        getBill().getReturnPreBills().add(getReturnBill());
        getBillFacade().edit(getBill());

        /// setOnlyReturnValue();
        printPreview = true;
        UtilityController.addSuccessMessage("Successfully Returned");

        //   return "pharmacy_good_receive_note_list";
    }

    private void calTotal() {
        double grossTotal = 0.0;

        for (BillItem p : getBillItems()) {
            grossTotal += p.getNetRate() * p.getQty();

        }

        getReturnBill().setTotal(grossTotal);
        getReturnBill().setNetTotal(grossTotal);

        //  return grossTotal;
    }

    public void generateBillComponent(BillType billType) {
        //System.err.println("Generate");
        for (PharmaceuticalBillItem i : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getBill())) {
            BillItem bi = new BillItem();
            bi.setBill(getReturnBill());
            bi.setReferenceBill(getBill());
            bi.setReferanceBillItem(i.getBillItem());
            bi.copy(i.getBillItem());
            bi.setQty(0.0);

            PharmaceuticalBillItem tmp = new PharmaceuticalBillItem();
            tmp.setBillItem(bi);
            tmp.copy(i);

            double rFund = getPharmacyRecieveBean().getTotalQty(i.getBillItem(), billType);

            //System.err.println("Refund " + rFund);
            double tmpQty = (Math.abs(i.getQtyInUnit())) - Math.abs(rFund);

            //System.err.println("TMP " + tmpQty);
            if (tmpQty <= 0) {
                continue;
            }

            tmp.setQtyInUnit((double) tmpQty);

            bi.setPharmaceuticalBillItem(tmp);

            getBillItems().add(bi);
        }

    }

//    private double calRemainingQty(PharmaceuticalBillItem i) {
//        if (i.getRemainingQty() == 0.0) {
////            if (i.getBillItem().getItem() instanceof Ampp) {
////                return (i.getQty()) * i.getBillItem().getItem().getDblValue();
////            } else {
////                return i.getQty();
////            }
//            return i.getQty();
//        } else {
//            return i.getRemainingQty();
//        }
//
//    }
    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public PharmaceuticalItemController getPharmaceuticalItemController() {
        return pharmaceuticalItemController;
    }

    public void setPharmaceuticalItemController(PharmaceuticalItemController pharmaceuticalItemController) {
        this.pharmaceuticalItemController = pharmaceuticalItemController;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public void setPharmacyController(PharmacyController pharmacyController) {
        this.pharmacyController = pharmacyController;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public PharmacyCalculation getPharmacyRecieveBean() {
        return pharmacyRecieveBean;
    }

    public void setPharmacyRecieveBean(PharmacyCalculation pharmacyRecieveBean) {
        this.pharmacyRecieveBean = pharmacyRecieveBean;
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

}
