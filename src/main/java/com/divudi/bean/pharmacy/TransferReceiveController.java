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
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.PharmacyBean;
import com.divudi.ejb.PharmacyRecieveBean;
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
import com.divudi.facade.StockFacade;
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
public class TransferReceiveController implements Serializable {

    private Bill issuedBill;
    private Bill receivedBill;
    private boolean printPreview;
    private Date fromDate;
    private Date toDate;
    ///////
    @Inject
    private SessionController sessionController;
    @Inject
    private PharmacyController pharmacyController;
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
    @EJB
    private PharmacyRecieveBean pharmacyRecieveBean;

    public void onFocus(BillItem tmp) {
        getPharmacyController().setPharmacyItem(tmp.getItem());
    }

    public void makeNull() {
        issuedBill = null;
        receivedBill = null;
        printPreview = false;
        fromDate = null;
        toDate = null;
    }

    public List<Bill> getIssued() {
        String sql;
        sql = "Select b From BilledBill b where b.retired=false and b.cancelled=false and "
                + " b.toDepartment=:dep and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.PharmacyTransferIssue);
        //   tmp.put("bTp2", BillType.PharmacyTransferReceive);
        List<Bill> bil = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        for (Bill b : bil) {
            b.setTmpRefBill(getRefBill(b));

        }

        if (bil == null) {
            return new ArrayList<>();
        }

        return bil;
    }

    private Bill getRefBill(Bill b) {
        String sql = "Select b From Bill b where b.retired=false "
                + " and b.cancelled=false and b.billType=:btp and "
                + " b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyTransferReceive);
        return getBillFacade().findFirstBySQL(sql, hm);
    }

    public TransferReceiveController() {
    }

    public Bill getIssuedBill() {
        if (issuedBill == null) {
            issuedBill = new BilledBill();
        }
        return issuedBill;
    }

    public void setIssuedBill(Bill issuedBill) {
        makeNull();
        this.issuedBill = issuedBill;
        receivedBill = null;
        create();
    }

    private List<Item> getSuggession(Item item) {
        List<Item> suggessions = new ArrayList<>();

        if (item instanceof Amp) {
            suggessions = getPharmacyRecieveBean().findPack((Amp) item);
            suggessions.add(item);
        } else if (item instanceof Ampp) {
            Amp amp = ((Ampp) item).getAmp();
            suggessions = getPharmacyRecieveBean().findPack(amp);
            suggessions.add(amp);
        }

        System.err.println("Sugg" + suggessions);

        return suggessions;
    }

    public void saveBillComponent() {
        HashMap hm = new HashMap();
        String sql = "Select p from PharmaceuticalBillItem p where "
                + " p.billItem.bill=:bill order by p.billItem.searialNo ";
        hm.put("bill", getIssuedBill());
        List<PharmaceuticalBillItem> tmp = getPharmaceuticalBillItemFacade().findBySQL(sql, hm);

        for (PharmaceuticalBillItem i : tmp) {

            BillItem bItem = new BillItem();
            bItem.setBill(getReceivedBill());
            bItem.setReferanceBillItem(i.getBillItem());
            bItem.copy(i.getBillItem());
            bItem.setQty(i.getQtyInUnit());
            bItem.setSearialNo(getReceivedBill().getBillItems().size() + 1);
            getBillItemFacade().create(bItem);

            bItem.setTmpSuggession(getSuggession(i.getBillItem().getItem()));

            PharmaceuticalBillItem phItem = new PharmaceuticalBillItem();
            phItem.setBillItem(bItem);
            phItem.copy(i);
            phItem.setStock(null);
            phItem.setItemBatch(null);
            phItem.invertValue(i);
            getPharmaceuticalBillItemFacade().create(phItem);

            bItem.setPharmaceuticalBillItem(phItem);
            getBillItemFacade().edit(bItem);

            getReceivedBill().getBillItems().add(bItem);

        }

        getBillFacade().edit(getReceivedBill());
    }

    @EJB
    private StockFacade stockFacade;

//    public Stock addToStock(ItemBatch batch, double qty, Department department) {
//        System.err.println("Adding Stock : ");
//
//        String sql;
//        HashMap hm = new HashMap();
//        sql = "Select s from Stock s where s.itemBatch=:bch and s.department=:dep";
//        hm.put("bch", batch);
//        hm.put("dep", department);
//        Stock s = getStockFacade().findFirstBySQL(sql, hm);
//        System.err.println("ss" + s);
//        if (s == null) {
//            s = new Stock();
//            s.setDepartment(department);
//            s.setItemBatch(batch);
//        }
//        s.setStock(s.getStock() + qty);
//        System.err.println("Stock 1 : " + s.getStock());
//        System.err.println("Stock 2 : " + qty);
//        System.err.println("Stock 3 : " + s);
//        System.err.println("Stock 4 : " + s.getId());
//        if (s.getId() == null || s.getId() == 0) {
//            //  Stock ss = new Stock();
//            getStockFacade().create(s);
//        } else {
//            getStockFacade().edit(s);
//        }
//        return s;
//    }
    public void settle() {

        for (BillItem i : getReceivedBill().getBillItems()) {

            i.getPharmaceuticalBillItem().setQtyInUnit(i.getQty());

            if (i.getPharmaceuticalBillItem().getQtyInUnit() == 0.0 || i.getItem() instanceof Vmpp || i.getItem() instanceof Vmp) {
                getPharmaceuticalBillItemFacade().remove(i.getPharmaceuticalBillItem());
                getBillItemFacade().remove(i);
                continue;
            }

            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());
            i.setPharmaceuticalBillItem(i.getPharmaceuticalBillItem());
            getBillItemFacade().edit(i);

            Stock staffStock = i.getPharmaceuticalBillItem().getStaffStock();
            double qty = Math.abs(i.getPharmaceuticalBillItem().getQtyInUnit());
            //Deduc Staff Stock

            getPharmacyBean().deductFromStock(staffStock, Math.abs(qty), i.getPharmaceuticalBillItem(), getSessionController().getDepartment());

            //Add To Stock
//            System.err.println("Stock " + stock);
//            System.err.println("item Batch " + stock.getItemBatch());
            Stock addedStock = getPharmacyBean().addToStock(staffStock.getItemBatch(), Math.abs(qty), getSessionController().getDepartment());

            i.getPharmaceuticalBillItem().setItemBatch(addedStock.getItemBatch());
            i.getPharmaceuticalBillItem().setStock(addedStock);
            i.getPharmaceuticalBillItem().setStaffStock(staffStock);

            getPharmaceuticalBillItemFacade().edit(i.getPharmaceuticalBillItem());

//            getReceivedBill().getBillItems().add(i);
        }

        getReceivedBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getReceivedBill(), BillType.PharmacyTransferReceive, BillNumberSuffix.PHTI));
        getReceivedBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getReceivedBill(), BillType.PharmacyTransferReceive, BillNumberSuffix.PHTI));

        getReceivedBill().setInstitution(getSessionController().getInstitution());
        getReceivedBill().setDepartment(getSessionController().getDepartment());

        getReceivedBill().setCreater(getSessionController().getLoggedUser());
        getReceivedBill().setCreatedAt(Calendar.getInstance().getTime());

        getReceivedBill().setNetTotal(calTotal());

        getBillFacade().edit(getReceivedBill());

        //Update Issue Bills Reference Bill
        getIssuedBill().setForwardReferenceBill(getReceivedBill());
        getBillFacade().edit(getIssuedBill());

        printPreview = true;

    }

    private double calTotal() {
        double value = 0;
        for (BillItem b : getReceivedBill().getBillItems()) {
            value += (b.getPharmaceuticalBillItem().getPurchaseRate() * b.getPharmaceuticalBillItem().getQty());

        }

        return value;

    }

    public void onEdit(BillItem tmp) {
        double availableStock = getPharmacyBean().getStockQty(tmp.getPharmaceuticalBillItem().getItemBatch(), getReceivedBill().getFromStaff());
        double oldValue = getPharmaceuticalBillItemFacade().find(tmp.getPharmaceuticalBillItem().getId()).getQtyInUnit();
        if (availableStock < tmp.getQty()) {
            tmp.setQty(oldValue);
            UtilityController.addErrorMessage("You cant recieved over than Issued Qty setted Old Value");
        }

     //   getPharmacyController().setPharmacyItem(tmp.getItem());
    }

    public void saveBill() {
        getReceivedBill().setBillType(BillType.PharmacyTransferReceive);
        getReceivedBill().setBackwardReferenceBill(getIssuedBill());
        getReceivedBill().setFromStaff(getIssuedBill().getToStaff());
        getReceivedBill().setFromInstitution(getIssuedBill().getInstitution());
        getReceivedBill().setFromDepartment(getIssuedBill().getDepartment());

        getBillFacade().create(getReceivedBill());
    }

    public void create() {
        saveBill();
        saveBillComponent();

        //Update Requested Bill Reference   
        getIssuedBill().setReferenceBill(getReceivedBill());
        getBillFacade().edit(getIssuedBill());
    }

    public Bill getReceivedBill() {
        if (receivedBill == null) {
            receivedBill = new BilledBill();
        }
        return receivedBill;
    }

    public void setReceivedBill(Bill receivedBill) {
        this.receivedBill = receivedBill;
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

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    public PharmacyRecieveBean getPharmacyRecieveBean() {
        return pharmacyRecieveBean;
    }

    public void setPharmacyRecieveBean(PharmacyRecieveBean pharmacyRecieveBean) {
        this.pharmacyRecieveBean = pharmacyRecieveBean;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public void setPharmacyController(PharmacyController pharmacyController) {
        this.pharmacyController = pharmacyController;
    }

}
