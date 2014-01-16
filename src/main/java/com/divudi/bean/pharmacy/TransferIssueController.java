/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.StockQty;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.Stock;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class TransferIssueController implements Serializable {

    private Bill requestedBill;
    private Bill issuedBill;
    private boolean printPreview;
    private Date fromDate;
    private Date toDate;
    ///////
    @Inject
    private SessionController sessionController;
    ////
    @EJB
    private BillFacade billFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private BillItemFacade billItemFacade;
    ////
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private CommonFunctions commonFunctions;

    private BillItem rbillItem;

    public void remove() {
        rbillItem.setBill(null);

        getBillItemFacade().edit(rbillItem);

        System.err.println("RM " + rbillItem);
        System.err.println("Size 1" + getIssuedBill().getBillItems().size());
        getIssuedBill().getBillItems().remove(rbillItem);
        System.err.println("Size 2" + getIssuedBill().getBillItems().size());
        getBillFacade().edit(getIssuedBill());

    }

    public void makeNull() {
        requestedBill = null;
        issuedBill = null;
        printPreview = false;
        fromDate = null;
        toDate = null;
    }

    public List<Bill> getRequests() {
        String sql;

        sql = "Select b From BilledBill b where b.cancelledBill is null "
                + " and  b.retired=false and  b.toDepartment=:dep"
                + " and b.billType= :bTp and b.createdAt between :fromDate and :toDate ";

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.PharmacyTransferRequest);
        //   tmp.put("bTp2", BillType.PharmacyTransferIssue);
        List<Bill> bil = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        if (bil == null) {
            return new ArrayList<>();
        }

        return bil;
    }

    public TransferIssueController() {
    }

    public Bill getRequestedBill() {
        if (requestedBill == null) {
            requestedBill = new BilledBill();
        }
        return requestedBill;
    }

    public void setRequestedBill(Bill requestedBill) {
        makeNull();
        this.requestedBill = requestedBill;
        issuedBill = null;
        create();
    }

    public void saveBillComponent() {
        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.bill.id=" + getRequestedBill().getId();
        List<PharmaceuticalBillItem> tmp = getPharmaceuticalBillItemFacade().findBySQL(sql);

        for (PharmaceuticalBillItem i : tmp) {

            List<StockQty> stockQtys = getPharmacyBean().getStockByQty(i.getBillItem().getItem(), i.getQtyInUnit(), getSessionController().getDepartment());

            for (StockQty sq : stockQtys) {
                if (sq.getQty() == 0) {
                    continue;
                }
                System.err.println("Stock " + sq.getStock());
                System.err.println("QTY " + sq.getQty());
                BillItem bItem = new BillItem();
                bItem.setBill(getIssuedBill());
                bItem.setItem(i.getBillItem().getItem());
                bItem.setReferanceBillItem(i.getBillItem());
                bItem.setQty(sq.getQty());
                System.err.println("Bill Item QTY " + bItem.getQty());
                getBillItemFacade().create(bItem);

                PharmaceuticalBillItem phItem = new PharmaceuticalBillItem();
                phItem.setBillItem(bItem);
                phItem.setQtyInUnit(0 - sq.getQty());
                System.err.println("Pharmac Item QTY " + phItem.getQtyInUnit());
                phItem.setFreeQtyInUnit(i.getFreeQtyInUnit());
                phItem.setPurchaseRate(i.getPurchaseRate());
                phItem.setRetailRate(i.getRetailRate());
                phItem.setStock(sq.getStock());
                phItem.setDoe(sq.getStock().getItemBatch().getDateOfExpire());
                phItem.setItemBatch(sq.getStock().getItemBatch());
                getPharmaceuticalBillItemFacade().create(phItem);

                bItem.setPharmaceuticalBillItem(phItem);
                getBillItemFacade().edit(bItem);

                getIssuedBill().getBillItems().add(bItem);

            }

        }

        getBillFacade().edit(getIssuedBill());
    }

    public void settle() {
        if (getIssuedBill().getToStaff() == null) {
            UtilityController.addErrorMessage("Please Select Staff");
            return;
        }

        for (BillItem i : getIssuedBill().getBillItems()) {

            i.getPharmaceuticalBillItem().setQtyInUnit(0 - i.getQty());

            if (i.getQty() == 0.0 || i.getItem() instanceof Vmpp || i.getItem() instanceof Vmp) {
                i.setBill(null);
                ///getPharmaceuticalBillItemFacade().remove(i.getPharmaceuticalBillItem());
//                getIssuedBill().getBillItems().remove(i);
                getBillItemFacade().edit(i);
                continue;
            }

            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());
            i.setPharmaceuticalBillItem(i.getPharmaceuticalBillItem());
            getBillItemFacade().edit(i);

            //Remove Department Stock
            getPharmacyBean().updateStock(i.getPharmaceuticalBillItem().getStock(), i.getPharmaceuticalBillItem().getQtyInUnit());

            //Addinng Staff
            Stock staffStock = getPharmacyBean().addToStock(i.getPharmaceuticalBillItem().getStock().getItemBatch(),
                    Math.abs(i.getPharmaceuticalBillItem().getQtyInUnit()), getIssuedBill().getToStaff());

            i.getPharmaceuticalBillItem().setStaffStock(staffStock);

            getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());
        }

        getIssuedBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getIssuedBill(), BillType.PharmacyTransferIssue, BillNumberSuffix.PHTI));
        getIssuedBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getIssuedBill(), BillType.PharmacyTransferIssue, BillNumberSuffix.PHTI));

        getIssuedBill().setInstitution(getSessionController().getInstitution());
        getIssuedBill().setDepartment(getSessionController().getDepartment());       
       

        getIssuedBill().setToInstitution(getIssuedBill().getToDepartment().getInstitution());
       

        getIssuedBill().setCreater(getSessionController().getLoggedUser());
        getIssuedBill().setCreatedAt(Calendar.getInstance().getTime());

        getIssuedBill().setNetTotal(calTotal());
        
        getIssuedBill().setBackwardReferenceBill(getRequestedBill());

        getBillFacade().edit(getIssuedBill());

        //Update ReferenceBill
   //     getRequestedBill().setReferenceBill(getIssuedBill());
        getRequestedBill().setForwardReferenceBill(getIssuedBill());
        getBillFacade().edit(getRequestedBill());

        Bill b = getBillFacade().find(getIssuedBill().getId());
        issuedBill=null;
        issuedBill=b;

        printPreview = true;

    }

    private double calTotal() {
        double value = 0;
        for (BillItem b : getIssuedBill().getBillItems()) {
            value += (b.getPharmaceuticalBillItem().getStock().getItemBatch().getPurcahseRate() * b.getPharmaceuticalBillItem().getQty());

        }

        return value;

    }

    @Inject
    private PharmacyController pharmacyController;

    public void onEdit(BillItem tmp) {
//        System.err.println("1 " + tmp);
//        System.err.println("2 " + tmp.getPharmaceuticalBillItem());
//        System.err.println("3 " + tmp.getPharmaceuticalBillItem().getItemBatch());
//        System.err.println("4 " + getSessionController().getDepartment());
        double availableStock = getPharmacyBean().getStockQty(tmp.getPharmaceuticalBillItem().getItemBatch(), getSessionController().getDepartment());
        double oldValue = (getBillItemFacade().find(tmp.getId())).getQty();
//        System.err.println("AvailableStock " + availableStock);
//        System.err.println("Old Value " + oldValue);
        if (availableStock < tmp.getQty()) {
            tmp.setQty(oldValue);
            UtilityController.addErrorMessage("You cant issue over than Stock Qty setted Old Value");
        }

    }

    public void onFocus(BillItem tmp) {
        getPharmacyController().setPharmacyItem(tmp.getItem());
    }

    public void saveBill() {
        getIssuedBill().setBillType(BillType.PharmacyTransferIssue);
        getIssuedBill().setReferenceBill(getRequestedBill());
        getIssuedBill().setToInstitution(getRequestedBill().getInstitution());
        getIssuedBill().setToDepartment(getRequestedBill().getDepartment());

        getBillFacade().create(getIssuedBill());
    }

    public void create() {
        saveBill();
        saveBillComponent();

        //Update Requested Bill Reference   
    }

    public Bill getIssuedBill() {
        if (issuedBill == null) {
            issuedBill = new BilledBill();
        }
        return issuedBill;
    }

    public void setIssuedBill(Bill issuedBill) {
        this.issuedBill = issuedBill;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
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

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
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

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return toDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public void setPharmacyController(PharmacyController pharmacyController) {
        this.pharmacyController = pharmacyController;
    }

    public BillItem getRbillItem() {
        return rbillItem;
    }

    public void setRbillItem(BillItem rbillItem) {
        this.rbillItem = rbillItem;
    }

}
