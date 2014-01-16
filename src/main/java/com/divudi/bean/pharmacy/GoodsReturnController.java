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
import com.divudi.ejb.PharmacyRecieveBean;
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
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
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
public class GoodsReturnController implements Serializable {

    private Bill bill;
    private Bill returnBill;
    private boolean printPreview;
    ////////

    private List<PharmacyItemData> pharmacyItemDatas;
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
    private PharmacyRecieveBean pharmacyRecieveBean;

    public void onEdit(PharmacyItemData tmp) {
        //    PharmaceuticalBillItem tmp = (PharmaceuticalBillItem) event.getObject();

        if (tmp.getBillItem().getQty() > getPharmacyRecieveBean().calQty(tmp.getPoBillItem().getPharmaceuticalBillItem())) {
            tmp.getBillItem().setQty(0.0);
            UtilityController.addErrorMessage("You cant return over than ballanced Qty ");
        }

        //  getBillItemFacade().edit(tmp.getBillItem());
        //  getPharmaceuticalBillItemFacade().edit(tmp);
        calTotal();
        getPharmacyController().setPharmacyItem(tmp.getPharmaceuticalBillItem().getBillItem().getItem());
    }

    public void makeNull() {
        bill = null;
        returnBill = null;
        printPreview = false;
        pharmacyItemDatas = null;

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
        for (PharmacyItemData i : getPharmacyItemData()) {
            i.getPharmaceuticalBillItem().setQtyInUnit(0-i.getBillItem().getQty());

            if (i.getPharmaceuticalBillItem().getQtyInUnit()== 0.0) {
                continue;
            }

            i.getBillItem().setNetValue(i.getPharmaceuticalBillItem().getQtyInUnit()* i.getPharmaceuticalBillItem().getPurchaseRateInUnit());
            i.getBillItem().setCreatedAt(Calendar.getInstance().getTime());
            i.getBillItem().setCreater(getSessionController().getLoggedUser());

            getBillItemFacade().create(i.getBillItem());

            i.getPharmaceuticalBillItem().setBillItem(i.getBillItem());
            getPharmaceuticalBillItemFacade().create(i.getPharmaceuticalBillItem());

            i.getBillItem().setPharmaceuticalBillItem(i.getPharmaceuticalBillItem());
            getBillItemFacade().edit(i.getBillItem());

            //   getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
            getPharmacyBean().updateStock(i.getPharmaceuticalBillItem().getStock(), i.getPharmaceuticalBillItem().getQtyInUnit());

            //   i.getBillItem().getTmpReferenceBillItem().getPharmaceuticalBillItem().setRemainingQty(i.getRemainingQty() - i.getQty());
            //   getPharmaceuticalBillItemFacade().edit(i.getBillItem().getTmpReferenceBillItem().getPharmaceuticalBillItem());
            //      updateRemainingQty(i);
            getReturnBill().getBillItems().add(i.getBillItem());
        }

    }

//    private void updateRemainingQty(PharmacyItemData pharmacyItemData) {
//        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.id=" + pharmacyItemData.getPoBillItem().getId();
//        PharmaceuticalBillItem po = getPharmaceuticalBillItemFacade().findFirstBySQL(sql);
//        po.setRemainingQty(po.getRemainingQty() + pharmacyItemData.getPharmaceuticalBillItem().getQty());
//
//        System.err.println("Added Remaini Qty " + pharmacyItemData.getPharmaceuticalBillItem().getQty());
//        System.err.println("Final Remaini Qty " + po.getRemainingQty());
//        getPharmaceuticalBillItemFacade().edit(po);
//
//    }
     private boolean checkStock(PharmaceuticalBillItem pharmaceuticalBillItem) {
        double stockQty = getPharmacyBean().getStockQty(pharmaceuticalBillItem.getItemBatch(), getBill().getDepartment());

        if (pharmaceuticalBillItem.getQtyInUnit() > stockQty) {
            return true;
        } else {
            return false;
        }
    }
    
     private boolean checkGrnItems() {
        for (PharmacyItemData bi :getPharmacyItemData()) {
              bi.getPharmaceuticalBillItem().setQty(bi.getBillItem().getQty());
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

        for (PharmacyItemData p : getPharmacyItemData()) {
            grossTotal += p.getPharmaceuticalBillItem().getPurchaseRate() * p.getBillItem().getQty();

        }

        getReturnBill().setTotal(grossTotal);
        getReturnBill().setNetTotal(grossTotal);

        //  return grossTotal;
    }

    public List<PharmacyItemData> getPharmacyItemData() {
        if (pharmacyItemDatas == null) {
            pharmacyItemDatas = new ArrayList<>();
            String sql = "Select p from PharmaceuticalBillItem p where p.billItem.bill.id=" + getBill().getId();
            List<PharmaceuticalBillItem> tmp2 = getPharmaceuticalBillItemFacade().findBySQL(sql);

            for (PharmaceuticalBillItem i : tmp2) {
                PharmacyItemData pid = new PharmacyItemData();
                BillItem bi = new BillItem();
                bi.setBill(getReturnBill());
                bi.setItem(i.getBillItem().getItem());
                bi.setReferanceBillItem(i.getBillItem());
                //   bi.setTmpReferenceBillItem(i.getBillItem());

                pid.setBillItem(bi);

                PharmaceuticalBillItem tmp = new PharmaceuticalBillItem();
                tmp.copy(i);
                tmp.setBillItem(bi);
//                tmp.setPurchaseRate(i.getPurchaseRate());
//                tmp.setDoe(i.getDoe());
//                tmp.setStringValue(i.getStringValue());
//                tmp.setItemBatch(i.getItemBatch());
//                tmp.setRetailRate(i.getRetailRate());

                // getPharmaceuticalBillItemFacade().create(tmp);
                pid.setGrnBillItem(i.getBillItem());
                pid.setPoBillItem(i.getBillItem().getReferanceBillItem());

                //tmp.setQty(getPharmacyRecieveBean().calQty(pid.getPoBillItem().getPharmaceuticalBillItem()));
                double rBilled = getPharmacyRecieveBean().getTotalQty(i.getBillItem(), BillType.PharmacyGrnReturn, new BilledBill());
                double rCacnelled = getPharmacyRecieveBean().getTotalQty(i.getBillItem(), BillType.PharmacyGrnReturn, new CancelledBill());

                double netQty=Math.abs(rBilled)-Math.abs(rCacnelled);
                        
                System.err.println("Billed " + rBilled);
                System.err.println("Cancelled " + rCacnelled);
                System.err.println("Net " + netQty);

                tmp.setQty(i.getQty() - netQty);

                pid.setPharmaceuticalBillItem(tmp);

                List<Item> suggessions = new ArrayList<>();
                Item item = i.getBillItem().getItem();

                if (item instanceof Amp) {
                    suggessions.add(item);
                    suggessions.add(getPharmacyBean().getAmpp((Amp) item));
                } else if (item instanceof Ampp) {
                    suggessions.add(((Ampp) item).getAmp());
                    suggessions.add(item);
                }

                pid.setSuggession(suggessions);

                pharmacyItemDatas.add(pid);
            }
        }
        return pharmacyItemDatas;
    }

    public void onEditItem(PharmacyItemData tmp) {
        double pur = getPharmacyBean().getLastPurchaseRate(tmp.getPharmaceuticalBillItem().getBillItem().getItem(), tmp.getPharmaceuticalBillItem().getBillItem().getReferanceBillItem().getBill().getDepartment());
        double ret = getPharmacyBean().getLastRetailRate(tmp.getPharmaceuticalBillItem().getBillItem().getItem(), tmp.getPharmaceuticalBillItem().getBillItem().getReferanceBillItem().getBill().getDepartment());

        tmp.getPharmaceuticalBillItem().setPurchaseRateInUnit(pur);
        tmp.getPharmaceuticalBillItem().setRetailRateInUnit(ret);
        tmp.getPharmaceuticalBillItem().setLastPurchaseRateInUnit(pur);

        // onEdit(tmp);
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

    public PharmacyRecieveBean getPharmacyRecieveBean() {
        return pharmacyRecieveBean;
    }

    public void setPharmacyRecieveBean(PharmacyRecieveBean pharmacyRecieveBean) {
        this.pharmacyRecieveBean = pharmacyRecieveBean;
    }

    public List<PharmacyItemData> getPharmacyItemDatas() {
        return pharmacyItemDatas;
    }

    public void setPharmacyItemDatas(List<PharmacyItemData> pharmacyItemDatas) {
        this.pharmacyItemDatas = pharmacyItemDatas;
    }

}
