/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

// <editor-fold defaultstate="collapsed" desc="Imports">
import com.divudi.bean.MessageController;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.StockQty;
import com.divudi.data.dataStructure.SearchKeyword;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyCalculation;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import org.primefaces.event.RowEditEvent;
// </editor-fold>

/**
 *
 * @author Buddhika
 */
@Named
@SessionScoped
public class ExpiaryTransferController implements Serializable {

    // <editor-fold defaultstate="collapsed" desc="EJBs">
    /**
     * EJBs
     */
    @EJB
    private BillFacade billFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private PharmacyCalculation pharmacyCalculation;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private PharmacyCalculation pharmacyRecieveBean;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Controllers">
    /**
     * Controllers
     */
    @Inject
    MessageController messageController;
    @Inject
    private SessionController sessionController;
    @Inject
    private ReportsStock reportsStock;
    @Inject
    private PharmacyController pharmacyController;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Class Variables">
    private Bill expiaryTransferBill;
    private boolean printPreview;
    private List<BillItem> billItems;
    private List<Stock> selectedStocks;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Navigation">
    public String toNewExpiaryTransferBill() {
        prepaireForNewBill();
        fillExpiary();
        return "/expiary/expiary_transfer";
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Functions">
    public void prepaireForNewBill() {
        printPreview=false;
        expiaryTransferBill = new BilledBill();
        expiaryTransferBill.setBillType(BillType.PharmacyTransferIssue);
        selectedStocks = null;
        billItems = new ArrayList<>();
    }

    public void fillExpiary() {
        reportsStock.setFromDate(null);
        reportsStock.setToDate(new Date());
        reportsStock.setDepartment(sessionController.getDepartment());
        reportsStock.fillDepartmentExpiaryStocks();

    }

    public void settle() {
        if (getExpiaryTransferBill().getToStaff() == null) {
            UtilityController.addErrorMessage("Please Select Staff");
            return;
        }
        if (getExpiaryTransferBill().getToDepartment() == null) {
            UtilityController.addErrorMessage("Please Select Department");
            return;
        }
        if (getSelectedStocks().isEmpty()) {
            UtilityController.addErrorMessage("Please Select Items to Transfer");
            return;
        }

        saveBill();
        addBillItems();

        for (BillItem i : getBillItems()) {

            i.getPharmaceuticalBillItem().setQtyInUnit(0 - i.getPharmaceuticalBillItem().getQtyInUnit());

            double requstedQty;
            double issuedQty;

            if (i.getQty() == 0.0 || i.getItem() instanceof Vmpp || i.getItem() instanceof Vmp) {
                continue;
            }

            i.setBill(getExpiaryTransferBill());
            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());
            i.setPharmaceuticalBillItem(i.getPharmaceuticalBillItem());

            PharmaceuticalBillItem tmpPh = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);
            getBillItemFacade().create(i);

            getPharmaceuticalBillItemFacade().create(tmpPh);

            i.setPharmaceuticalBillItem(tmpPh);
            getBillItemFacade().edit(i);
            //Remove Department Stock
            boolean returnFlag = getPharmacyBean().deductFromStock(i.getPharmaceuticalBillItem().getStock(),
                    Math.abs(i.getPharmaceuticalBillItem().getQtyInUnit()),
                    i.getPharmaceuticalBillItem(),
                    getSessionController().getDepartment());

            if (returnFlag) {

                //Addinng Staff
                Stock staffStock = getPharmacyBean().addToStock(i.getPharmaceuticalBillItem(),
                        Math.abs(i.getPharmaceuticalBillItem().getQtyInUnit()), getExpiaryTransferBill().getToStaff());

                i.getPharmaceuticalBillItem().setStaffStock(staffStock);

            } else {
                i.setTmpQty(0);
                getBillItemFacade().edit(i);
            }

            getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());

            getExpiaryTransferBill().getBillItems().add(i);
        }

        getExpiaryTransferBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getExpiaryTransferBill(), BillType.PharmacyTransferIssue, BillNumberSuffix.PHTI));
        getExpiaryTransferBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getExpiaryTransferBill(), BillType.PharmacyTransferIssue, BillNumberSuffix.PHTI));

        getExpiaryTransferBill().setInstitution(getSessionController().getInstitution());
        getExpiaryTransferBill().setDepartment(getSessionController().getDepartment());

        getExpiaryTransferBill().setToInstitution(getExpiaryTransferBill().getToDepartment().getInstitution());

        getExpiaryTransferBill().setCreater(getSessionController().getLoggedUser());
        getExpiaryTransferBill().setCreatedAt(Calendar.getInstance().getTime());

        getExpiaryTransferBill().setNetTotal(calTotal());

        getBillFacade().edit(getExpiaryTransferBill());

        messageController.addMessageToWebUsers(expiaryTransferBill);

        Bill b = getBillFacade().find(getExpiaryTransferBill().getId());
        expiaryTransferBill = null;
        expiaryTransferBill = b;

        printPreview = true;

    }

    private void saveBill() {
        getExpiaryTransferBill().setFromDepartment(sessionController.getDepartment());
        getExpiaryTransferBill().setFromInstitution(sessionController.getInstitution());
        getExpiaryTransferBill().setFromDepartment(sessionController.getDepartment());
        getExpiaryTransferBill().setFromInstitution(sessionController.getInstitution());

        getExpiaryTransferBill().setToInstitution(getExpiaryTransferBill().getToDepartment().getInstitution());
        getExpiaryTransferBill().setCreatedAt(new Date());
        getExpiaryTransferBill().setCreater(sessionController.getLoggedUser());

        if (getExpiaryTransferBill().getId() == null) {
            getBillFacade().create(getExpiaryTransferBill());
        } else {
            getBillFacade().edit(getExpiaryTransferBill());
        }

    }

    public void addBillItems() {
        billItems = new ArrayList<>();
        for (Stock s : selectedStocks) {

            BillItem bItem = new BillItem();
            bItem.setSearialNo(getBillItems().size());
            bItem.setItem(s.getItemBatch().getItem());

            bItem.setTmpQty(s.getStock());

            PharmaceuticalBillItem phItem = new PharmaceuticalBillItem();
            phItem.setBillItem(bItem);
            phItem.setQtyInUnit((double) s.getStock());
            phItem.setFreeQtyInUnit(0.0);
            phItem.setPurchaseRateInUnit((double) s.getItemBatch().getPurcahseRate());
            phItem.setRetailRateInUnit((double) s.getItemBatch().getRetailsaleRate());
            phItem.setStock(s);
            phItem.setDoe(s.getItemBatch().getDateOfExpire());
            phItem.setItemBatch(s.getItemBatch());
            bItem.setPharmaceuticalBillItem(phItem);

            getBillItems().add(bItem);

        }
    }

    private double calTotal() {
        double value = 0;
        int serialNo = 0;
        for (BillItem b : getExpiaryTransferBill().getBillItems()) {
            value += (b.getPharmaceuticalBillItem().getPurchaseRate() * b.getPharmaceuticalBillItem().getQty());
            b.setSearialNo(serialNo++);
        }

        return value;

    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Defaults">
    public ExpiaryTransferController() {
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Unclassified">
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public List<Stock> getSelectedStocks() {
        if (selectedStocks == null) {
            selectedStocks = new ArrayList<>();
        }
        return selectedStocks;
    }

    public void setSelectedStocks(List<Stock> selectedStocks) {
        this.selectedStocks = selectedStocks;
    }

    public Bill getExpiaryTransferBill() {
        if (expiaryTransferBill == null) {
            expiaryTransferBill = new BilledBill();
            expiaryTransferBill.setBillType(BillType.PharmacyTransferIssue);
        }
        return expiaryTransferBill;
    }

    public void setExpiaryTransferBill(Bill expiaryTransferBill) {
        this.expiaryTransferBill = expiaryTransferBill;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public ReportsStock getReportsStock() {
        return reportsStock;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
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
 
    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public PharmacyCalculation getPharmacyRecieveBean() {
        return pharmacyRecieveBean;
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

 
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sample Cord Fold">
    // </editor-fold>
}
