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
import com.divudi.ejb.PharmacyRecieveBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.ItemsDistributorsFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author safrin
 */
@Named(value = "transferRequestController")
@SessionScoped
public class TransferRequestController implements Serializable {

    @Inject
    private SessionController sessionController;
    @EJB
    private ItemFacade itemFacade;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private ItemsDistributorsFacade itemsDistributorsFacade;
    private Bill bill;
    private Institution dealor;
    private Item currentItem;
    private List<PharmaceuticalBillItem> pharmaceuticalBillItems;
    //   private boolean printPreview;
    //   private double netTotal;
    @EJB
    private PharmacyRecieveBean pharmacyBillBean;

    public void recreate() {
        bill = null;
        pharmaceuticalBillItems = null;
        currentItem = null;
        dealor = null;
        //      printPreview = false;
    }

    public void addItem() {
        if (getBill().getToDepartment() == null) {
            UtilityController.addErrorMessage("Select Department");
            return;
        }

        saveBill();

        PharmaceuticalBillItem ptd = new PharmaceuticalBillItem();
        //    ptd.setPurchaseRate(getPharmacyBean().getPurchaseRate(getCurrentItem(), getSessionController().getDepartment()));

        BillItem b = getPharmacyBillBean().saveBillItem(getCurrentItem(), getBill(), getSessionController());

        PharmaceuticalBillItem p = getPharmacyBillBean().savePharmacyBillItem(b, getSessionController().getDepartment());

        //  p.setPurchaseRate(getPharmacyBean().getPurchaseRate(getCurrentItem(), getSessionController().getDepartment()));
        p.setRetailRateInUnit(getPharmacyBean().getLastRetailRate(getCurrentItem(), getSessionController().getDepartment()));

        getPharmacyBillItem().add(p);
//        getNetTotal();

        currentItem = null;
    }

    @Inject
    private PharmacyController pharmacyController;

    public void onEdit(PharmaceuticalBillItem tmp) {

        //  PharmaceuticalBillItem tmp = (PharmaceuticalBillItem) event.getObject();
        getBillItemFacade().edit(tmp.getBillItem());
        getPharmaceuticalBillItemFacade().edit(tmp);
        getPharmacyController().setPharmacyItem(tmp.getBillItem().getItem());
//        getNetTotal();
    }

    public void saveBill() {
        if (getBill().getId() == null) {
            getBill().setBillType(BillType.PharmacyTransferRequest);

            getBill().setInstitution(getSessionController().getInstitution());
            getBill().setDepartment(getSessionController().getDepartment());

            getBill().setToInstitution(getBill().getToDepartment().getInstitution());

            getBillFacade().create(getBill());
        }

    }

    public List<PharmaceuticalBillItem> getPharmacyBillItem() {
        if (pharmaceuticalBillItems == null) {
            pharmaceuticalBillItems = new ArrayList<>();
        }
        return pharmaceuticalBillItems;
    }

    public PharmaceuticalBillItem savePharmacyBillItem(BillItem b) {
        PharmaceuticalBillItem tmp = new PharmaceuticalBillItem();
        tmp.setBillItem(b);
        tmp.setQty(getPharmacyBean().getOrderingQty(b.getItem(), getSessionController().getDepartment()));
        tmp.setPurchaseRate(getPharmacyBean().getPurchaseRate(b.getPharmaceuticalBillItem().getItemBatch(), getSessionController().getDepartment()));

        getPharmaceuticalBillItemFacade().create(tmp);
        return tmp;
    }

    public void request() {
        if (getBill().getToDepartment() == null) {
            UtilityController.addErrorMessage("Select Requested Department");
            return;
        }

        for (PharmaceuticalBillItem ph : getPharmaceuticalBillItems()) {
            if (ph.getQty() == 0.0) {
                UtilityController.addErrorMessage("Check Items Qty");
                return;
            }
        }

        getBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getBill(), BillType.PharmacyTransferRequest, BillNumberSuffix.PHTRQ));
        getBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getBill(), BillType.PharmacyTransferRequest, BillNumberSuffix.PHTRQ));

        getPharmacyBillBean().editBill(getBill(), getSessionController().getLoggedUser());

        UtilityController.addSuccessMessage("Transfer Request Succesfully Created");

        //   printPreview = true;
        recreate();
    }

    public TransferRequestController() {
    }

    public Institution getDealor() {

        return dealor;
    }

    public void setDealor(Institution dealor) {
        this.dealor = dealor;
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

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public Bill getBill() {
        if (bill == null) {
            bill = new BilledBill();
        }
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public Item getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(Item currentItem) {
        this.currentItem = currentItem;
    }

    public ItemsDistributorsFacade getItemsDistributorsFacade() {
        return itemsDistributorsFacade;
    }

    public void setItemsDistributorsFacade(ItemsDistributorsFacade itemsDistributorsFacade) {
        this.itemsDistributorsFacade = itemsDistributorsFacade;
    }

    public List<PharmaceuticalBillItem> getPharmaceuticalBillItems() {
        return pharmaceuticalBillItems;
    }

    public void setPharmaceuticalBillItems(List<PharmaceuticalBillItem> pharmaceuticalBillItems) {
        this.pharmaceuticalBillItems = pharmaceuticalBillItems;
    }

    public PharmacyRecieveBean getPharmacyBillBean() {
        return pharmacyBillBean;
    }

    public void setPharmacyBillBean(PharmacyRecieveBean pharmacyBillBean) {
        this.pharmacyBillBean = pharmacyBillBean;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public void setPharmacyController(PharmacyController pharmacyController) {
        this.pharmacyController = pharmacyController;
    }

//    public boolean isPrintPreview() {
//        return printPreview;
//    }
//
//    public void setPrintPreview(boolean printPreview) {
//        this.printPreview = printPreview;
//    }
}
