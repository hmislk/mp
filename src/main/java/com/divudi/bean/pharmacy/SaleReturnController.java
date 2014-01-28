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
import com.divudi.entity.RefundBill;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
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
public class SaleReturnController implements Serializable {

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
            returnBill = new RefundBill();
            //     returnBill.setBillType(BillType.PharmacySale);

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

    public void onEdit(PharmacyItemData tmp) {
        //    PharmaceuticalBillItem tmp = (PharmaceuticalBillItem) event.getObject();

        if (tmp.getBillItem().getQty() > getPharmacyRecieveBean().calQty2(tmp.getBillItem().getReferanceBillItem())) {
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

    private void savePreReturnBill() {
        //   getReturnBill().setReferenceBill(getBill());

        getReturnBill().copy(getBill());

        getReturnBill().setBillType(BillType.PharmacyPre);

        getReturnBill().setTotal(0 - getReturnBill().getTotal());
        getReturnBill().setNetTotal(getReturnBill().getTotal());

        getReturnBill().setCreater(getSessionController().getLoggedUser());
        getReturnBill().setCreatedAt(Calendar.getInstance().getTime());

        getReturnBill().setInstitution(getSessionController().getInstitution());
        getReturnBill().setDepartment(getSessionController().getDepartment());

        getReturnBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(
                getSessionController().getInstitution(), new RefundBill(), BillType.PharmacyPre, BillNumberSuffix.PHRET));

        getReturnBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(
                getSessionController().getDepartment(), new RefundBill(), BillType.PharmacyPre, BillNumberSuffix.PHRET));

        getBillFacade().create(getReturnBill());

    }

    private Bill saveSaleReturnBill() {
        RefundBill refundBill = new RefundBill();
        refundBill.copy(getReturnBill());
        refundBill.setBillType(BillType.PharmacySale);
        refundBill.setReferenceBill(getReturnBill());
        refundBill.setPaymentScheme(getReturnBill().getPaymentScheme());

        refundBill.setTotal(getReturnBill().getTotal());
        refundBill.setNetTotal(getReturnBill().getTotal());

        refundBill.setCreater(getSessionController().getLoggedUser());
        refundBill.setCreatedAt(Calendar.getInstance().getTime());

        refundBill.setInstitution(getSessionController().getInstitution());
        refundBill.setDepartment(getSessionController().getDepartment());

//        refundBill.setInsId(getBillNumberBean().institutionBillNumberGenerator(
//                getSessionController().getInstitution(), new RefundBill(), BillType.PharmacySale, BillNumberSuffix.SALRET));
        refundBill.setInsId(getReturnBill().getInsId());
        getBillFacade().create(refundBill);

        updatePreReturnBill(refundBill);

        return refundBill;

    }

    public void updatePreReturnBill(Bill ref) {
        getReturnBill().setReferenceBill(ref);
        getBillFacade().edit(getReturnBill());
    }

    private void savePreComponent() {
        for (PharmacyItemData i : getPharmacyItemData()) {
            i.getPharmaceuticalBillItem().setQty(i.getBillItem().getQty());
            if (i.getPharmaceuticalBillItem().getQty() == 0.0) {
                continue;
            }

            i.getBillItem().setCreatedAt(Calendar.getInstance().getTime());
            i.getBillItem().setCreater(getSessionController().getLoggedUser());
            //   i.getBillItem().setQty(i.getPharmaceuticalBillItem().getQty());
            double value = i.getBillItem().getNetRate() * i.getBillItem().getQty();
            i.getBillItem().setGrossValue(0 - value);
            i.getBillItem().setNetValue(0 - value);

            getBillItemFacade().create(i.getBillItem());

            i.getPharmaceuticalBillItem().setBillItem(i.getBillItem());
            getPharmaceuticalBillItemFacade().create(i.getPharmaceuticalBillItem());

            i.getBillItem().setPharmaceuticalBillItem(i.getPharmaceuticalBillItem());
            getBillItemFacade().edit(i.getBillItem());

            //   getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
            getPharmacyBean().addToStock(i.getPharmaceuticalBillItem().getStock(), Math.abs(i.getPharmaceuticalBillItem().getQtyInUnit()), i.getPharmaceuticalBillItem(), getSessionController().getDepartment());

            //   i.getBillItem().getTmpReferenceBillItem().getPharmaceuticalBillItem().setRemainingQty(i.getRemainingQty() - i.getQty());
            //   getPharmaceuticalBillItemFacade().edit(i.getBillItem().getTmpReferenceBillItem().getPharmaceuticalBillItem());
            //      updateRemainingQty(i);
            getReturnBill().getBillItems().add(i.getBillItem());
        }

        updateReturnTotal();

    }

    private void saveSaleComponent(Bill bill) {
        for (BillItem i : getReturnBill().getBillItems()) {
            BillItem b = new BillItem();
            b.copy(i);
            b.setBill(bill);
            b.setCreatedAt(Calendar.getInstance().getTime());
            b.setCreater(getSessionController().getLoggedUser());

            getBillItemFacade().create(b);

            PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
            ph.copy(i.getPharmaceuticalBillItem());
            ph.setBillItem(b);
            getPharmaceuticalBillItemFacade().create(ph);

            b.setPharmaceuticalBillItem(ph);
            getBillItemFacade().edit(b);

            bill.getBillItems().add(b);
        }

        getBillFacade().edit(bill);

    }

    private void updateReturnTotal() {
        double tot = 0;
        for (BillItem b : getReturnBill().getBillItems()) {
            tot += b.getNetValue();
        }

        getReturnBill().setTotal(tot);
        getReturnBill().setNetTotal(tot);
        getBillFacade().edit(getReturnBill());
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
    public void settle() {
        savePreReturnBill();
        savePreComponent();

        Bill b = saveSaleReturnBill();
        saveSaleComponent(b);

        getBillFacade().edit(getReturnBill());

        printPreview = true;
        UtilityController.addSuccessMessage("Successfully Returned");

        //   return "pharmacy_good_receive_note_list";
    }

    private void calTotal() {
        double grossTotal = 0.0;

        for (PharmacyItemData p : getPharmacyItemData()) {
            grossTotal += p.getBillItem().getNetRate() * p.getBillItem().getQty();

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
                bi.setReferenceBill(getBill());
                bi.setReferanceBillItem(i.getBillItem());
                bi.copy(i.getBillItem());
                bi.setQty(0.0);
                //   bi.setTmpReferenceBillItem(i.getBillItem());
                pid.setBillItem(bi);

                PharmaceuticalBillItem tmp = new PharmaceuticalBillItem();
                tmp.setBillItem(bi);
                tmp.copy(i);

                // getPharmaceuticalBillItemFacade().create(tmp);
                pid.setGrnBillItem(i.getBillItem());
                pid.setPoBillItem(i.getBillItem().getReferanceBillItem());

                //tmp.setQty(getPharmacyRecieveBean().calQty(pid.getPoBillItem().getPharmaceuticalBillItem()));
                double rFund = getPharmacyRecieveBean().getTotalQty(i.getBillItem(), BillType.PharmacyPre, new RefundBill());
                //  double rCacnelled = getPharmacyRecieveBean().getTotalQty(i.getBillItem(), BillType.PharmacySale, new CancelledBill());

                System.err.println("Refund " + rFund);
//                System.err.println("Cancelled "+rCacnelled);
//                System.err.println("Net "+(rBilled-rCacnelled));

                tmp.setQty(i.getQty() - Math.abs(rFund));

                pid.setPharmaceuticalBillItem(tmp);

                pharmacyItemDatas.add(pid);
            }
        }
        return pharmacyItemDatas;
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

    public List<PharmacyItemData> getPharmacyItemDatas() {
        return pharmacyItemDatas;
    }

    public void setPharmacyItemDatas(List<PharmacyItemData> pharmacyItemDatas) {
        this.pharmacyItemDatas = pharmacyItemDatas;
    }

}
