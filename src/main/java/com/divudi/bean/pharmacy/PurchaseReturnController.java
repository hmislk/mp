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
import com.divudi.entity.Item;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
public class PurchaseReturnController implements Serializable {

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
        this.bill = bill;
        createBillItems();
    }

    public Bill getReturnBill() {
        if (returnBill == null) {
            returnBill = new BilledBill();
            returnBill.setBillType(BillType.PurchaseReturn);

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

    private double getRemainingQty(BillItem bilItem) {
        String sql = "Select sum(p.pharmaceuticalBillItem.qty) from BillItem p where"
                + "   p.creater is not null and"
                + " p.referanceBillItem=:bt and p.bill.billType=:btp";

        HashMap hm = new HashMap();
        hm.put("bt", bilItem);
        hm.put("btp", BillType.PurchaseReturn);
        ///    hm.put("class", bill.getClass());

        return bilItem.getQty() + getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);
    }

    public void onEdit(BillItem tmp) {
        //    PharmaceuticalBillItem tmp = (PharmaceuticalBillItem) event.getObject();

        double remain = getRemainingQty(tmp.getReferanceBillItem());
        if (tmp.getQty() > remain) {
            tmp.setQty(remain);
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

    private void saveReturnBill() {
        getReturnBill().setInvoiceDate(getBill().getInvoiceDate());
        getReturnBill().setReferenceBill(getBill());
        getReturnBill().setToInstitution(getBill().getFromInstitution());
        getReturnBill().setToDepartment(getBill().getFromDepartment());
        getReturnBill().setFromInstitution(getBill().getToInstitution());
        getReturnBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getReturnBill(), BillType.PurchaseReturn, BillNumberSuffix.PURRET));
        getReturnBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getReturnBill(), BillType.PurchaseReturn, BillNumberSuffix.PURRET));

        getReturnBill().setInstitution(getSessionController().getInstitution());
        getReturnBill().setDepartment(getSessionController().getDepartment());
        // getReturnBill().setReferenceBill(getBill());
        getReturnBill().setCreater(getSessionController().getLoggedUser());
        getReturnBill().setCreatedAt(Calendar.getInstance().getTime());

        getBillFacade().create(getReturnBill());

    }

    private void saveComponent() {
        for (BillItem i : getBillItems()) {
            i.getPharmaceuticalBillItem().setQtyInUnit((double)(double)(0 - i.getQty()));

            if (i.getPharmaceuticalBillItem().getQtyInUnit() == 0.0) {
                continue;
            }

            i.setNetValue(i.getPharmaceuticalBillItem().getQtyInUnit() * i.getPharmaceuticalBillItem().getPurchaseRateInUnit());
            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());

            PharmaceuticalBillItem tmpPharmaceuticalBillItem = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);

            getBillItemFacade().create(i);

            tmpPharmaceuticalBillItem.setBillItem(i);
            getPharmaceuticalBillItemFacade().create(tmpPharmaceuticalBillItem);

            i.setPharmaceuticalBillItem(tmpPharmaceuticalBillItem);
            getBillItemFacade().edit(i);

          boolean returnFlag=  getPharmacyBean().deductFromStock(i.getPharmaceuticalBillItem().getStock(), Math.abs(i.getPharmaceuticalBillItem().getQtyInUnit()), i.getPharmaceuticalBillItem(), getSessionController().getDepartment());

          if(!returnFlag){
              i.setTmpQty(0);
              getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
              getBillItemFacade().edit(i);
          }
        
          
          getReturnBill().getBillItems().add(i);
        }

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
            bi.getPharmaceuticalBillItem().setQty((double)(double)bi.getQty());
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

        for (BillItem p : getBillItems()) {
            grossTotal += p.getPharmaceuticalBillItem().getPurchaseRate() * p.getQty();

        }

        getReturnBill().setTotal(grossTotal);
        getReturnBill().setNetTotal(grossTotal);

        //  return grossTotal;
    }

    public void createBillItems() {
        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.bill.id=" + getBill().getId();
        List<PharmaceuticalBillItem> tmp2 = getPharmaceuticalBillItemFacade().findBySQL(sql);

        for (PharmaceuticalBillItem i : tmp2) {
            BillItem bi = new BillItem();
            bi.copy(i.getBillItem());
            bi.setQty(0.0);
            bi.setBill(getReturnBill());
            bi.setItem(i.getBillItem().getItem());
            bi.setReferanceBillItem(i.getBillItem());

            PharmaceuticalBillItem tmp = new PharmaceuticalBillItem();
            tmp.copy(i);
            tmp.setBillItem(bi);

            double rBilled = getPharmacyRecieveBean().getTotalQty(i.getBillItem(), BillType.PurchaseReturn, new BilledBill());

            ////System.err.println("Billed Qty " + i.getQty());
            ////System.err.println("Return Qty " + rBilled);
            tmp.setQty((double)(i.getQty() - Math.abs(rBilled)));

            bi.setPharmaceuticalBillItem(tmp);

            List<Item> suggessions = new ArrayList<>();
            Item item = i.getBillItem().getItem();

            if (item instanceof Amp) {
                suggessions.add(item);
                suggessions.add(getPharmacyBean().getAmpp((Amp) item));
            } else if (item instanceof Ampp) {
                suggessions.add(((Ampp) item).getAmp());
                suggessions.add(item);
            }

            getBillItems().add(bi);
        }

    }

    public void onEditItem(PharmacyItemData tmp) {
        double pur = (double)getPharmacyBean().getLastPurchaseRate(tmp.getPharmaceuticalBillItem().getBillItem().getItem(), tmp.getPharmaceuticalBillItem().getBillItem().getReferanceBillItem().getBill().getDepartment());
        double ret = (double)getPharmacyBean().getLastRetailRate(tmp.getPharmaceuticalBillItem().getBillItem().getItem(), tmp.getPharmaceuticalBillItem().getBillItem().getReferanceBillItem().getBill().getDepartment());

        tmp.getPharmaceuticalBillItem().setPurchaseRateInUnit(pur);
        tmp.getPharmaceuticalBillItem().setRetailRateInUnit(ret);
        tmp.getPharmaceuticalBillItem().setLastPurchaseRateInUnit(pur);

        // onEdit(tmp);
    }

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
