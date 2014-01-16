/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.ItemController;
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
import java.util.Calendar;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class PurchaseOrderRequestController implements Serializable {

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
    private Bill currentBill;
    private Institution dealor;
    private Item currentItem;
    private List<BillItem> selectedBillItems;
    //private List<PharmaceuticalBillItem> pharmaceuticalBillItems;
    private double netTotal;
    @EJB
    PharmacyRecieveBean pharmacyBillBean;
    
    public void removeSelected(){
        if(selectedBillItems==null){
            return;
        }
        
        for(BillItem b:selectedBillItems){
            removeItem(b);
        }
    
    }

    public PharmacyRecieveBean getPharmacyBillBean() {
        return pharmacyBillBean;
    }

    public void setPharmacyBillBean(PharmacyRecieveBean pharmacyBillBean) {
        this.pharmacyBillBean = pharmacyBillBean;
    }

    public void recreate() {
        currentBill = null;
        currentItem = null;
        dealor = null;
    }

    public void addItem() {
        if (getCurrentItem() == null) {
            UtilityController.addErrorMessage("Please select and item from the list");
            return;
        }
        if (getCurrentBill() == null || getCurrentBill().getId() == null || getCurrentBill().getId() == 0) {
            UtilityController.addErrorMessage("You have to create new Bill before adding items");
            return;
        }
//        if (getPharmacyBillBean().checkItem(getDealor(), getCurrentItem())) {
//            UtilityController.addErrorMessage("Item already added for this dealor");
//            return;
//        }

        //Save BillItem
        BillItem bItem = new BillItem();
        bItem.setBill(getCurrentBill());
        bItem.setItem(currentItem);
        getBillItemFacade().create(bItem);

        //Save Pharmaceutical Item
        PharmaceuticalBillItem phItem = new PharmaceuticalBillItem();
        phItem.setBillItem(bItem);
        phItem.setQty(getPharmacyBean().getOrderingQty(bItem.getItem(), getSessionController().getDepartment()));
        phItem.setPurchaseRateInUnit(getPharmacyBean().getLastPurchaseRate(bItem.getItem(), getSessionController().getDepartment()));
        phItem.setRetailRateInUnit(getPharmacyBean().getLastRetailRate(bItem.getItem(), getSessionController().getDepartment()));

        getPharmaceuticalBillItemFacade().create(phItem);

        bItem.setPharmaceuticalBillItem(phItem);
        getBillItemFacade().edit(bItem);
        
        getCurrentBill().getBillItems().add(bItem);
        getBillFacade().edit(getCurrentBill());

        currentItem = null;
        getNetTotal();
    }

    public void removeItem(BillItem bi) {
        getCurrentBill().getBillItems().remove(bi);
        getBillFacade().edit(getCurrentBill());
        
        bi.setBill(null);
        getBillItemFacade().edit(bi);

        currentItem = null;
        getNetTotal();
    }

    private void updateBillItemList() {
        Bill bill = getBillFacade().find(getCurrentBill().getId());

        setCurrentBill(bill);

    }

    @Inject
    private PharmacyController pharmacyController;

    public void onFocus(BillItem bi) {
        getPharmacyController().setPharmacyItem(bi.getItem());
    }

    public void onEdit(BillItem bi) {

        bi.setNetValue(bi.getPharmaceuticalBillItem().getQty() * bi.getPharmaceuticalBillItem().getPurchaseRate());

        getBillItemFacade().edit(bi);
        getPharmaceuticalBillItemFacade().edit(bi.getPharmaceuticalBillItem());

        getPharmacyController().setPharmacyItem(bi.getItem());
    }

    public void saveBill() {
        getCurrentBill().setBillType(BillType.PharmacyOrder);

//        getCurrentBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getCurrentBill(), BillType.PharmacyOrder, BillNumberSuffix.POR));
//        getCurrentBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getCurrentBill(), BillType.PharmacyOrder, BillNumberSuffix.POR));
        getCurrentBill().setToInstitution(getDealor());

        getCurrentBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getCurrentBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getCurrentBill().setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        getCurrentBill().setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getBillFacade().create(getCurrentBill());

    }

    private void editBill() {
        getCurrentBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getCurrentBill(), BillType.PharmacyOrder, BillNumberSuffix.POR));
        getCurrentBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getCurrentBill(), BillType.PharmacyOrder, BillNumberSuffix.POR));

        getCurrentBill().setTotal(getNetTotal());
        getCurrentBill().setNetTotal(getNetTotal());
//        getCurrentBill().set
        getCurrentBill().setCreater(getSessionController().getLoggedUser());
        getCurrentBill().setCreatedAt(Calendar.getInstance().getTime());

        getBillFacade().edit(getCurrentBill());
    }

    public void saveBillComponent() {
        for (Item i : getPharmacyBillBean().getItemsForDealor(dealor)) {
            BillItem bi = new BillItem();
            bi.setBill(getCurrentBill());
            bi.setItem(i);
            getBillItemFacade().create(bi);

            PharmaceuticalBillItem tmp = new PharmaceuticalBillItem();
            tmp.setBillItem(bi);
            tmp.setQty(getPharmacyBean().getOrderingQty(bi.getItem(), getSessionController().getDepartment()));
            tmp.setPurchaseRateInUnit(getPharmacyBean().getLastPurchaseRate(bi.getItem(), getSessionController().getDepartment()));
            tmp.setRetailRateInUnit(getPharmacyBean().getLastRetailRate(bi.getItem(), getSessionController().getDepartment()));

            getPharmaceuticalBillItemFacade().create(tmp);
           
            bi.setPharmaceuticalBillItem(tmp);
            getBillItemFacade().edit(bi);
            
            getCurrentBill().getBillItems().add(bi);

        }
        
        getBillFacade().edit(getCurrentBill());
    }

    public void createOrder() {
        if (getDealor() == null) {
            UtilityController.addErrorMessage("Please Select Dealor");

        }
        saveBill();
   //     saveBillComponent();
        getNetTotal();
    }
    
     public void createOrderWithItems() {
        if (getDealor() == null) {
            UtilityController.addErrorMessage("Please Select Dealor");

        }
        saveBill();
        saveBillComponent();
        getNetTotal();
    }

    private void editBillItem() {
        for (BillItem bi : getCurrentBill().getBillItems()) {
            bi.setQty(bi.getPharmaceuticalBillItem().getQtyInUnit());
            bi.setRate(bi.getPharmaceuticalBillItem().getPurchaseRateInUnit());
            bi.setNetValue(bi.getPharmaceuticalBillItem().getQtyInUnit() * bi.getPharmaceuticalBillItem().getPurchaseRateInUnit());
            getBillItemFacade().edit(bi);
            //   netTotal += p.getQty() * p.getPurchaseRate();
        }

    }

    private boolean checkItemPrice() {
        for (BillItem bi : getCurrentBill().getBillItems()) {
            if (bi.getPharmaceuticalBillItem().getPurchaseRateInUnit() == 0) {
                return true;
            }
        }

        return false;
    }

    public void request() {
        if (getCurrentBill().getPaymentMethod() == null) {
            UtilityController.addErrorMessage("Please Select Paymntmethod");
            return;
        }
//
//        if (checkItemPrice()) {
//            UtilityController.addErrorMessage("Please enter purchase price for all");
//            return;
//        }

        editBillItem();
        editBill();

        UtilityController.addSuccessMessage("Request Succesfully Created");

        dealor = null;
        currentItem = null;
        netTotal = 0.0;
        currentBill = null;

    }

    public PurchaseOrderRequestController() {
    }

    
    @Inject
    private ItemController itemController;
    public Institution getDealor() {
        return dealor;
    }

    public void setDealor(Institution dealor) {
        getItemController().setInstituion(dealor);
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

    public Bill getCurrentBill() {
        if (currentBill == null) {
            currentBill = new BilledBill();
        }
        return currentBill;
    }

    public void setCurrentBill(Bill currentBill) {
        this.currentBill = currentBill;
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

    public double getNetTotal() {

        netTotal = 0.0;
        for (BillItem bi : getCurrentBill().getBillItems()) {
            netTotal += bi.getPharmaceuticalBillItem().getQty() * bi.getPharmaceuticalBillItem().getPurchaseRate();
        }

        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public void setPharmacyController(PharmacyController pharmacyController) {
        this.pharmacyController = pharmacyController;
    }

    public List<BillItem> getSelectedBillItems() {
        return selectedBillItems;
    }

    public void setSelectedBillItems(List<BillItem> selectedBillItems) {
        this.selectedBillItems = selectedBillItems;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public void setItemController(ItemController itemController) {
        this.itemController = itemController;
    }
}
