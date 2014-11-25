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
import com.divudi.data.dataStructure.SearchKeyword;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyCalculation;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Item;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Ampp;
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
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import org.primefaces.event.RowEditEvent;

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
    private PharmacyCalculation pharmacyCalculation;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private PharmacyCalculation pharmacyRecieveBean;
    private List<BillItem> billItems;

    public void remove(BillItem billItem) {
        getBillItems().remove(billItem.getSearialNo());
        int serialNo = 0;
        for (BillItem b : getBillItems()) {
            b.setSearialNo(serialNo++);
        }
    }

    public void makeNull() {
        requestedBill = null;
        issuedBill = null;
        printPreview = false;
        fromDate = null;
        toDate = null;
        billItems = null;
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

    private List<Bill> bills;
    private SearchKeyword searchKeyword;

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
        generateBillComponent();
    }

    public void generateBillComponent() {

        for (PharmaceuticalBillItem i : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getRequestedBill())) {

            double billedIssue = getPharmacyCalculation().getBilledIssuedByRequestedItem(i.getBillItem(), BillType.PharmacyTransferIssue);
            double cancelledIssue = getPharmacyCalculation().getCancelledIssuedByRequestedItem(i.getBillItem(), BillType.PharmacyTransferIssue);

            double issuableQty = i.getQtyInUnit() - (Math.abs(billedIssue) - Math.abs(cancelledIssue));

            //System.err.println("Issueable Qty " + issuableQty);

            List<StockQty> stockQtys = getPharmacyBean().getStockByQty(i.getBillItem().getItem(), issuableQty, getSessionController().getDepartment());

            for (StockQty sq : stockQtys) {
                if (sq.getQty() == 0) {
                    continue;
                }
                //System.err.println("Stock " + sq.getStock());
                //System.err.println("QTY " + sq.getQty());
                BillItem bItem = new BillItem();
                bItem.setSearialNo(getBillItems().size());
                bItem.setItem(i.getBillItem().getItem());
                bItem.setReferanceBillItem(i.getBillItem());
                bItem.setTmpQty(sq.getQty());
                //System.err.println("Bill Item QTY " + bItem.getQty());

//               s bItem.setTmpSuggession(getSuggession(i.getBillItem().getItem()));
                //     ////System.err.println("List "+bItem.getTmpSuggession());
                PharmaceuticalBillItem phItem = new PharmaceuticalBillItem();
                phItem.setBillItem(bItem);
                phItem.setQtyInUnit((double) sq.getQty());
                //System.err.println("Pharmac Item QTY " + phItem.getQtyInUnit());
                phItem.setFreeQtyInUnit(i.getFreeQtyInUnit());
                phItem.setPurchaseRateInUnit((double) sq.getStock().getItemBatch().getPurcahseRate());
                phItem.setRetailRateInUnit((double) sq.getStock().getItemBatch().getRetailsaleRate());
                phItem.setStock(sq.getStock());
                phItem.setDoe(sq.getStock().getItemBatch().getDateOfExpire());
                phItem.setItemBatch(sq.getStock().getItemBatch());
                bItem.setPharmaceuticalBillItem(phItem);

                getBillItems().add(bItem);

            }

        }

        Stock stock = new Stock();
        boolean flag = false;
        for (BillItem b : getBillItems()) {
            if (b.getPharmaceuticalBillItem().getStock().getId() == stock.getId()) {
                flag = true;
                break;
            }
            stock = b.getPharmaceuticalBillItem().getStock();
        }

        if (flag) {
            billItems = null;
            UtilityController.addErrorMessage("There is Some Item in request that are added Multiple Time in Transfer request!!! please check request you can't issue errornus transfer request");
        }

    }

    public void settle() {
        if (getIssuedBill().getToStaff() == null) {
            UtilityController.addErrorMessage("Please Select Staff");
            return;
        }

        saveBill();

        for (BillItem i : getBillItems()) {

            i.getPharmaceuticalBillItem().setQtyInUnit(0 - i.getPharmaceuticalBillItem().getQtyInUnit());

            if (i.getQty() == 0.0 || i.getItem() instanceof Vmpp || i.getItem() instanceof Vmp) {
                continue;
            }

            i.setBill(getIssuedBill());
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
                        Math.abs(i.getPharmaceuticalBillItem().getQtyInUnit()), getIssuedBill().getToStaff());

                i.getPharmaceuticalBillItem().setStaffStock(staffStock);

            } else {
                i.setTmpQty(0);
                getBillItemFacade().edit(i);
            }

            getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());

            getIssuedBill().getBillItems().add(i);
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
        getRequestedBill().getForwardReferenceBills().add(getIssuedBill());
        getBillFacade().edit(getRequestedBill());

        Bill b = getBillFacade().find(getIssuedBill().getId());
        issuedBill = null;
        issuedBill = b;

        printPreview = true;

    }

    private double calTotal() {
        double value = 0;
        int serialNo = 0;
        for (BillItem b : getIssuedBill().getBillItems()) {
            value += (b.getPharmaceuticalBillItem().getPurchaseRate() * b.getPharmaceuticalBillItem().getQty());
            b.setSearialNo(serialNo++);
        }

        return value;

    }

    @Inject
    private PharmacyController pharmacyController;

    public void onEdit(RowEditEvent event) {
        BillItem tmp = (BillItem) event.getObject();
//        ////System.err.println("1 " + tmp);
//        ////System.err.println("2 " + tmp.getPharmaceuticalBillItem());
//        ////System.err.println("3 " + tmp.getPharmaceuticalBillItem().getItemBatch());
//        ////System.err.println("4 " + getSessionController().getDepartment());
        double availableStock = getPharmacyBean().getStockQty(tmp.getPharmaceuticalBillItem().getItemBatch(), getSessionController().getDepartment());
//        double oldValue = (getPharmaceuticalBillItemFacade().find(tmp.getPharmaceuticalBillItem().getId())).getQty();
//        ////System.err.println("AvailableStock " + availableStock);
//        ////System.err.println("Old Value " + oldValue);
        if (availableStock < tmp.getPharmaceuticalBillItem().getQtyInUnit()) {
            tmp.setTmpQty(0.0);
            UtilityController.addErrorMessage("You cant issue over than Stock Qty setted Old Value");
        }

    }

    public void onFocus(BillItem tmp) {
        getPharmacyController().setPharmacyItem(tmp.getItem());
    }

    private void saveBill() {
        getIssuedBill().setReferenceBill(getRequestedBill());
        getIssuedBill().setToInstitution(getRequestedBill().getInstitution());
        getIssuedBill().setToDepartment(getRequestedBill().getDepartment());

        getBillFacade().create(getIssuedBill());
    }

    public Bill getIssuedBill() {
        if (issuedBill == null) {
            issuedBill = new BilledBill();
            issuedBill.setBillType(BillType.PharmacyTransferIssue);
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

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public SearchKeyword getSearchKeyword() {
        if (searchKeyword == null) {
            searchKeyword = new SearchKeyword();
        }
        return searchKeyword;
    }

    public void setSearchKeyword(SearchKeyword searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public PharmacyCalculation getPharmacyCalculation() {
        return pharmacyCalculation;
    }

    public void setPharmacyCalculation(PharmacyCalculation pharmacyCalculation) {
        this.pharmacyCalculation = pharmacyCalculation;
    }

}
