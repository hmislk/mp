/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.dataStructure.PharmacyItemData;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyCalculation;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Item;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.Vmpp;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
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
public class GoodsReturnController implements Serializable {

    private Bill bill;
    private Bill returnBill;
    private boolean printPreview;
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
        this.bill = bill;
        generateBillComponent();
    }

    public Bill getReturnBill() {
        if (returnBill == null) {
            returnBill = new BilledBill();
            returnBill.setBillType(BillType.PharmacyGrnReturn);

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

        if (tmp.getPharmaceuticalBillItem().getQtyInUnit() > getPharmacyRecieveBean().calQty(tmp.getReferanceBillItem().getReferanceBillItem().getPharmaceuticalBillItem())) {
            tmp.setTmpQty(0.0);
            UtilityController.addErrorMessage("You cant return over than ballanced Qty ");
        }

        calTotal();
        getPharmacyController().setPharmacyItem(tmp.getPharmaceuticalBillItem().getBillItem().getItem());
    }

    public void makeNull() {
        bill = null;
        returnBill = null;
        printPreview = false;
        billItems = null;
    }

    public String viewList() {
        return "pharmacy_grn_list_for_return";
    }

    private void saveReturnBill() {
        getReturnBill().setInvoiceDate(getBill().getInvoiceDate());
        getReturnBill().setReferenceBill(getBill());
        getReturnBill().setToInstitution(getBill().getFromInstitution());
        getReturnBill().setToDepartment(getBill().getFromDepartment());
        getReturnBill().setFromInstitution(getBill().getToInstitution());
        getReturnBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getReturnBill(), BillType.PharmacyGrnReturn, BillNumberSuffix.GRNRET));
        getReturnBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getReturnBill(), BillType.PharmacyGrnReturn, BillNumberSuffix.GRNRET));

        getReturnBill().setInstitution(getSessionController().getInstitution());
        getReturnBill().setDepartment(getSessionController().getDepartment());
        // getReturnBill().setReferenceBill(getBill());
        getReturnBill().setCreater(getSessionController().getLoggedUser());
        getReturnBill().setCreatedAt(Calendar.getInstance().getTime());

        getBillFacade().create(getReturnBill());

    }

    private void saveComponent() {
        for (BillItem i : getBillItems()) {

            if (i.getTmpQty() == 0.0) {
                continue;
            }

            i.setBill(getReturnBill());
            i.setNetValue(i.getPharmaceuticalBillItem().getQtyInUnit() * i.getPharmaceuticalBillItem().getPurchaseRateInUnit());
            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());

            PharmaceuticalBillItem tmpPh = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);
            getBillItemFacade().create(i);

            getPharmaceuticalBillItemFacade().create(tmpPh);

            i.setPharmaceuticalBillItem(tmpPh);
            getBillItemFacade().edit(i);

            getPharmacyBean().deductFromStock(i.getPharmaceuticalBillItem().getStock(), Math.abs(i.getPharmaceuticalBillItem().getQtyInUnit()), i.getPharmaceuticalBillItem(), getSessionController().getDepartment());

            getReturnBill().getBillItems().add(i);

        }

        getBillFacade().edit(getReturnBill());

    }

    private boolean checkStock(PharmaceuticalBillItem pharmaceuticalBillItem) {
        double stockQty = getPharmacyBean().getStockQty(pharmaceuticalBillItem.getItemBatch(), getBill().getDepartment());

        if (pharmaceuticalBillItem.getQtyInUnit() > stockQty) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkGrnItems() {
        for (BillItem bi : getBillItems()) {
            if (checkStock(bi.getPharmaceuticalBillItem())) {
                return true;
            }
        }

        return false;
    }

    public void settle() {
        if (checkGrnItems()) {
            UtilityController.addErrorMessage("ITems for this GRN Already issued so you can't cancel ");
            return;

        }
        saveReturnBill();
        saveComponent();

        getBillFacade().edit(getReturnBill());

        printPreview = true;
        UtilityController.addSuccessMessage("Successfully Returned");

        //   return "pharmacy_good_receive_note_list";
    }

    private void calTotal() {
        double grossTotal = 0.0;
        int serialNo = 0;
        for (BillItem p : getBillItems()) {
            grossTotal += p.getPharmaceuticalBillItem().getPurchaseRate() * p.getPharmaceuticalBillItem().getQty();
            p.setSearialNo(serialNo++);
        }

        getReturnBill().setTotal(grossTotal);
        getReturnBill().setNetTotal(grossTotal);

        //  return grossTotal;
    }

    private void generateBillComponent() {
        billItems = null;
        for (PharmaceuticalBillItem grnPh : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getBill())) {
            BillItem bi = new BillItem();
            bi.setItem(grnPh.getBillItem().getItem());
            //Grns BillItem
            bi.setReferanceBillItem(grnPh.getBillItem());
            bi.setSearialNo(getBillItems().size());
            PharmaceuticalBillItem retPh = new PharmaceuticalBillItem();
            retPh.copy(grnPh);
            retPh.setBillItem(bi);

            double rBilled = getPharmacyRecieveBean().getTotalQty(grnPh.getBillItem(), BillType.PharmacyGrnReturn, new BilledBill());
            double rCacnelled = getPharmacyRecieveBean().getTotalQty(grnPh.getBillItem(), BillType.PharmacyGrnReturn, new CancelledBill());

            double netQty = Math.abs(rBilled) - Math.abs(rCacnelled);

            //System.err.println("Billed " + rBilled);
            //System.err.println("Cancelled " + rCacnelled);
            //System.err.println("Net " + netQty);

            retPh.setQtyInUnit(grnPh.getQtyInUnit() - netQty);

            List<Item> suggessions = new ArrayList<>();
            Item item = bi.getItem();

            if (item instanceof Amp) {
                suggessions.add(item);
                suggessions.add(getPharmacyBean().getAmpp((Amp) item));
            } else if (item instanceof Ampp) {
                suggessions.add(((Ampp) item).getAmp());
                suggessions.add(item);
            }

            bi.setTmpSuggession(suggessions);
            bi.setPharmaceuticalBillItem(retPh);

            getBillItems().add(bi);

        }

    }

//    public void onEditItem(BillItem tmp) {
//        double pur = getPharmacyBean().getLastPurchaseRate(tmp.getPharmaceuticalBillItem().getBillItem().getItem(), tmp.getPharmaceuticalBillItem().getBillItem().getReferanceBillItem().getBill().getDepartment());
//        double ret = getPharmacyBean().getLastRetailRate(tmp.getPharmaceuticalBillItem().getBillItem().getItem(), tmp.getPharmaceuticalBillItem().getBillItem().getReferanceBillItem().getBill().getDepartment());
//
//        tmp.getPharmaceuticalBillItem().setPurchaseRateInUnit(pur);
//        tmp.getPharmaceuticalBillItem().setRetailRateInUnit(ret);
//        tmp.getPharmaceuticalBillItem().setLastPurchaseRateInUnit(pur);
//
//        // onEdit(tmp);
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
