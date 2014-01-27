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
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.LazyBill;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.ItemBatch;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.Vmpp;
import com.divudi.facade.AmpFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.CategoryFacade;
import com.divudi.facade.ItemBatchFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class GrnController implements Serializable {

    @Inject
    private SessionController sessionController;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private BillFacade billFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private ItemFacade itemFacade;
    @EJB
    private CategoryFacade categoryFacade;
    @EJB
    private ItemBatchFacade itemBatchFacade;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private AmpFacade ampFacade;
    @EJB
    private PharmacyRecieveBean pharmacyBillBean;
    @EJB
    private CommonFunctions commonFunctions;
    /////////////////
    private Institution dealor;
    private Bill approveBill;
    private Bill grnBill;
    //   private Double cashPaid;
    private Date fromDate;
    private Date toDate;
    private boolean printPreview;
    //////////////
    //private List<PharmacyItemData> pharmacyItems;
    private List<Bill> pos;
    List<Bill> grns;
    private List<Bill> filteredValue;
    private List<BillItem> billItems;
    private int serial;

    public void removeItem(BillItem bi) {
        getBillItems().remove(bi.getSearialNo());

        calGrossTotal();
        //  getNetTotal();
    }

    public void clearList() {
        pos = null;
        filteredValue = null;
        grns = null;
        billItems = null;
    }

    public void setBatch(BillItem pid) {
        if (pid.getPharmaceuticalBillItem().getDoe() != null) {
            if (pid.getPharmaceuticalBillItem().getDoe().getTime() < Calendar.getInstance().getTimeInMillis()) {
                pid.getPharmaceuticalBillItem().setStringValue(null);
                return;
            }
        }

        Date date = pid.getPharmaceuticalBillItem().getDoe();
        DateFormat df = new SimpleDateFormat("ddMMyyyy");
        String reportDate = df.format(date);
        pid.getPharmaceuticalBillItem().setStringValue(reportDate);

        onEdit(pid);
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return toDate;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return fromDate;
    }

    public void saveBill() {
        getGrnBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), getGrnBill(), BillType.PharmacyGrnBill, BillNumberSuffix.GRN));
        getGrnBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), getGrnBill(), BillType.PharmacyGrnBill, BillNumberSuffix.GRN));

        getGrnBill().setToInstitution(getApproveBill().getFromInstitution());
        getGrnBill().setToDepartment(getApproveBill().getFromDepartment());

        getGrnBill().setInstitution(getSessionController().getInstitution());
        getGrnBill().setDepartment(getSessionController().getDepartment());

        getGrnBill().setCreater(getSessionController().getLoggedUser());
        getGrnBill().setCreatedAt(Calendar.getInstance().getTime());

        calGrossTotal();
        getBillFacade().create(getGrnBill());

    }

    public void settle() {
        String msg = getPharmacyBillBean().errorCheck(getGrnBill());
        if (!msg.isEmpty()) {
            UtilityController.addErrorMessage(msg);
            return;
        }

        getPharmacyBillBean().calSaleFreeValue(getGrnBill(), getBillItems());
        if (getGrnBill().getInvoiceDate() == null) {
            getGrnBill().setInvoiceDate(getApproveBill().getCreatedAt());
        }

        saveBill();

        for (BillItem i : getBillItems()) {
            i.setBill(getGrnBill());
            i.setCreater(getSessionController().getLoggedUser());
            i.setCreatedAt(new Date());

            PharmaceuticalBillItem tmpPh = i.getPharmaceuticalBillItem();
            i.setPharmaceuticalBillItem(null);
            getBillItemFacade().create(i);

            tmpPh.setBillItem(i);
            getPharmaceuticalBillItemFacade().create(tmpPh);

            i.setPharmaceuticalBillItem(tmpPh);
            getBillItemFacade().edit(i);

            ItemBatch itemBatch = getPharmacyBillBean().saveItemBatch(i);

            double addingQty = i.getPharmaceuticalBillItem().getQtyInUnit() + i.getPharmaceuticalBillItem().getFreeQtyInUnit();

            getPharmacyBean().setPurchaseRate(itemBatch, getSessionController().getDepartment());
            getPharmacyBean().setRetailRate(itemBatch, getSessionController().getDepartment());
            i.getPharmaceuticalBillItem().setItemBatch(itemBatch);

            Stock stock = getPharmacyBean().addToStock(i.getPharmaceuticalBillItem(), Math.abs(addingQty), getSessionController().getDepartment());

            i.getPharmaceuticalBillItem().setStock(stock);

            i.setNetValue(0 - (i.getPharmaceuticalBillItem().getQty() * i.getPharmaceuticalBillItem().getPurchaseRate()));

            double consumed = getPharmacyBean().calQty(i.getReferanceBillItem().getPharmaceuticalBillItem());
            i.setRemainingQty(i.getReferanceBillItem().getPharmaceuticalBillItem().getQty() - consumed);
        }

        //  getPharmacyBillBean().editBill(, , getSessionController());
        printPreview = true;

    }

    public String viewPoList() {
        clearList();
        return "pharmacy_purchase_order_list_for_recieve";
    }

    public GrnController() {
    }

    public Institution getDealor() {
        return dealor;
    }

    public void setDealor(Institution dealor) {
        this.dealor = dealor;
    }

    private LazyDataModel<Bill> searchBills;
    private String txtSearch;

    public void makeListNull() {
        searchBills = null;
//        pharmacyItems = null;
        pos = null;
        grns = null;
        filteredValue = null;
    }

    public void createPoTable() {
        searchBills = null;
        String sql;
        HashMap tmp = new HashMap();
        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp"
                    + " and b.cancelled=false and b.referenceBill.institution=:ins and "
                    + " b.createdAt between :fromDate and :toDate order by b.id desc  ";
        } else {
            sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp"
                    + " and b.cancelled=false  and b.referenceBill.institution=:ins "
                    + " and  (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + " b.createdAt between :fromDate and :toDate order by b.id desc ";
            // tmp.put("del", getDealor());
        }
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.PharmacyOrderApprove);
        pos = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        for (Bill b : pos) {
            b.setListOfBill(getGrns(b));
        }

        searchBills = new LazyBill(pos);

    }

    public void createGrnTable() {
        searchBills = null;
        String sql;
        HashMap tmp = new HashMap();
        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp "
                    + " and b.cancelled=false and b.institution=:ins and"
                    + " b.createdAt between :fromDate and :toDate order by b.id desc  ";
        } else {
            sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp and"
                    + " b.cancelled=false and b.institution=:ins "
                    + " and (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.referenceBill.netTotal) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + " and b.createdAt between :fromDate and :toDate order by b.id desc ";
            // tmp.put("del", getDealor());
        }
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.PharmacyGrnBill);
        grns = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        for (Bill b : grns) {
            b.setListOfBill(getReturnBill(b));
        }

        searchBills = new LazyBill(grns);

    }

    public List<Bill> getPo() {
        if (pos == null) {
            String sql;
            HashMap tmp = new HashMap();
            if (getDealor() == null) {
                sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp and b.cancelled=false and "
                        + "b.createdAt is not null and b.createdAt between :fromDate and :toDate order by b.id desc  ";
            } else {
                sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp and b.cancelled=false and b.toInstitution=:del and "
                        + "b.createdAt is not null and b.createdAt between :fromDate and :toDate order by b.id desc ";
                tmp.put("del", getDealor());
            }
            tmp.put("toDate", getToDate());
            tmp.put("fromDate", getFromDate());
            tmp.put("bTp", BillType.PharmacyOrderApprove);
            pos = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);
            if (pos == null) {
                return new ArrayList<>();
            }
            for (Bill b : pos) {
                b.setListOfBill(getGrns(b));
            }
        }

        return pos;
    }

    private List<Bill> getGrns(Bill b) {
        String sql = "Select b From BilledBill b where b.retired=false and b.creater is not null"
                + " and b.billType=:btp and "
                + " b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyGrnBill);
        return getBillFacade().findBySQL(sql, hm);
    }

    private List<Bill> getReturnBill(Bill b) {
        String sql = "Select b From BilledBill b where b.retired=false and b.creater is not null"
                + " and b.cancelled=false and b.billType=:btp and "
                + " b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyGrnReturn);
        return getBillFacade().findBySQL(sql, hm);
    }

//    public List<Bill> getGrns() {
//        String sql = "Select b From Bill b where b.retired=false and b.createdAt between :frmDate and :toDate"
//                + " and b.cancelled=false and b.billType=:btp ";
//        HashMap hm = new HashMap();
//         hm.put("ref", b);
//        hm.put("frmDate", getFromDate());
//        hm.put("toDate", getToDate());
//        hm.put("btp", BillType.PharmacyGrnBill);
//        
//        
//        
//        return getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);
//    }
//    
    public List<Bill> getGrns() {
        if (grns == null) {
            String sql;
            HashMap tmp = new HashMap();
            if (getDealor() == null) {
                sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp and b.cancelled=false and "
                        + "b.createdAt is not null and b.createdAt between :fromDate and :toDate order by b.deptId desc  ";
            } else {
                sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp and b.cancelled=false and b.toInstitution=:del and "
                        + "b.createdAt is not null and b.createdAt between :fromDate and :toDate  order by b.deptId desc ";
                tmp.put("del", getDealor());
            }
            tmp.put("toDate", getToDate());
            tmp.put("fromDate", getFromDate());
            tmp.put("bTp", BillType.PharmacyGrnBill);
            grns = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);
            if (grns == null) {
                return new ArrayList<>();
            }
            for (Bill b : grns) {
                b.setListOfBill(getReturnBill(b));
            }
        }

        return grns;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Bill getApproveBill() {
        if (approveBill == null) {
            serial = 1;
            approveBill = new BilledBill();
        }
        return approveBill;
    }

    public void generateBillComponent() {

        for (PharmaceuticalBillItem i : getPharmaceuticalBillItemFacade().getPharmaceuticalBillItems(getApproveBill())) {

            double remains = getPharmacyBean().calQtyInTwoSql(i);
            System.err.println("Tot GRN Qty : " + remains);
            System.err.println("QTY : " + i.getQtyInUnit());
            if (i.getQtyInUnit() >= remains && (i.getQtyInUnit() - remains) != 0) {
                BillItem bi = new BillItem();
                bi.setItem(i.getBillItem().getItem());
                bi.setSearialNo(serial++);
                bi.setReferanceBillItem(i.getBillItem());
                bi.setTmpQty(i.getQtyInUnit() - remains);

                //Set Suggession
                bi.setTmpSuggession(getSuggession(bi.getItem()));

                PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
                ph.setBillItem(bi);
                ph.setQtyInUnit(bi.getQty());
                ph.setPurchaseRate(i.getPurchaseRate());
                ph.setRetailRate(i.getRetailRate());
                ph.setLastPurchaseRate(getPharmacyBean().getLastPurchaseRate(bi.getItem(), getSessionController().getDepartment()));

                bi.setPharmaceuticalBillItem(ph);

                getBillItems().add(bi);
            }

        }
    }

    public void createGrn() {
        generateBillComponent();
        calGrossTotal();

    }

    public void findLastEdited() {
        String sql = "Select b From BilledBill b where b.creater is null and  b.retired=false and b.billType= :bTp "
                + " and b.referenceBill.id=" + getApproveBill().getId();
        HashMap tmp = new HashMap();
        tmp.put("bTp", BillType.PharmacyGrnBill);
        List<Bill> bil = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        if (!bil.isEmpty()) {
            setGrnBill(bil.get(0));
        }

    }

    private double getRetailPrice(BillItem billItem) {
        String sql = "select (p.retailRate) from PharmaceuticalBillItem p where p.billItem=:b";
        HashMap hm = new HashMap();
        hm.put("b", billItem.getReferanceBillItem());
        return getPharmaceuticalBillItemFacade().findDoubleByJpql(sql, hm);
    }

    public void onEditItem(BillItem tmp) {
        double pur = getPharmacyBean().getLastPurchaseRate(tmp.getItem(), tmp.getReferanceBillItem().getBill().getDepartment());
        double ret = getPharmacyBean().getLastRetailRate(tmp.getItem(), tmp.getReferanceBillItem().getBill().getDepartment());

        tmp.getPharmaceuticalBillItem().setPurchaseRateInUnit(pur);
        tmp.getPharmaceuticalBillItem().setRetailRateInUnit(ret);
        tmp.getPharmaceuticalBillItem().setLastPurchaseRateInUnit(pur);

        // onEdit(tmp);
    }

    public void onEdit(BillItem tmp) {

        double remains = getPharmacyBean().calQtyInTwoSql(tmp.getPharmaceuticalBillItem());
        if (remains < tmp.getQty()) {
            tmp.getPharmaceuticalBillItem().setQtyInUnit(remains);
            UtilityController.addErrorMessage("You cant Change Qty than Remaining qty");
        }

        if (tmp.getPharmaceuticalBillItem().getPurchaseRate() > tmp.getPharmaceuticalBillItem().getRetailRate()) {
            tmp.getPharmaceuticalBillItem().setRetailRate(getRetailPrice(tmp.getPharmaceuticalBillItem().getBillItem()));
            UtilityController.addErrorMessage("You cant set retail price below purchase rate");
        }

        if (tmp.getPharmaceuticalBillItem().getDoe() != null) {
            if (tmp.getPharmaceuticalBillItem().getDoe().getTime() < Calendar.getInstance().getTimeInMillis()) {
                tmp.getPharmaceuticalBillItem().setDoe(null);
                UtilityController.addErrorMessage("Check Date of Expiry");
                //    return;
            }
        }

        calGrossTotal();
    }

    public void onEditPurchaseRate(BillItem tmp) {

        double retail = tmp.getPharmaceuticalBillItem().getPurchaseRate() + (tmp.getPharmaceuticalBillItem().getPurchaseRate() * (getPharmacyBean().getMaximumRetailPriceChange() / 100));
        tmp.getPharmaceuticalBillItem().setRetailRate(retail);

        onEdit(tmp);
    }

    private List<Item> getSuggession(Item item) {
        List<Item> suggessions = new ArrayList<>();

        if (item instanceof Amp) {
            suggessions = getPharmacyBillBean().findItem((Amp) item, suggessions);
        } else if (item instanceof Ampp) {
            suggessions = getPharmacyBillBean().findItem((Ampp) item, suggessions);
        } else if (item instanceof Vmp) {
            suggessions = getPharmacyBillBean().findItem((Vmp) item, suggessions);
        } else if (item instanceof Vmpp) {
            suggessions = getPharmacyBillBean().findItem((Vmpp) item, suggessions);
        }

        return suggessions;
    }

    private void calGrossTotal() {
        double tmp = 0.0;

        for (BillItem p : getBillItems()) {
            tmp += p.getPharmaceuticalBillItem().getPurchaseRate() * p.getPharmaceuticalBillItem().getQty();
        }

        getGrnBill().setTotal(0 - tmp);

    }

    public double getNetTotal() {

        double tmp = getGrnBill().getTotal() + getGrnBill().getTax() - getGrnBill().getDiscount();
        getGrnBill().setNetTotal(0 - tmp);

        return tmp;
    }

    private void recreate() {
        grnBill = null;
        dealor = null;
        pos = null;
        printPreview = false;
        billItems = null;

    }

    public void setApproveBill(Bill approveBill) {
        this.approveBill = approveBill;
        recreate();
        createGrn();
    }

    public Bill getGrnBill() {
        if (grnBill == null) {
            grnBill = new BilledBill();
            grnBill.setBillType(BillType.PharmacyGrnBill);
        }
        return grnBill;
    }

    public void setGrnBill(Bill grnBill) {
        this.grnBill = grnBill;
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

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public CategoryFacade getCategoryFacade() {
        return categoryFacade;
    }

    public void setCategoryFacade(CategoryFacade categoryFacade) {
        this.categoryFacade = categoryFacade;
    }

    public ItemBatchFacade getItemBatchFacade() {
        return itemBatchFacade;
    }

    public void setItemBatchFacade(ItemBatchFacade itemBatchFacade) {
        this.itemBatchFacade = itemBatchFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public AmpFacade getAmpFacade() {
        return ampFacade;
    }

    public void setAmpFacade(AmpFacade ampFacade) {
        this.ampFacade = ampFacade;
    }

    public PharmacyRecieveBean getPharmacyBillBean() {
        return pharmacyBillBean;
    }

    public void setPharmacyBillBean(PharmacyRecieveBean pharmacyBillBean) {
        this.pharmacyBillBean = pharmacyBillBean;
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

    public List<Bill> getFilteredValue() {
        return filteredValue;
    }

    public void setFilteredValue(List<Bill> filteredValue) {
        this.filteredValue = filteredValue;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public List<Bill> getPos() {
        return pos;
    }

    public void setPos(List<Bill> pos) {
        this.pos = pos;
    }

    public LazyDataModel<Bill> getSearchBills() {
        return searchBills;
    }

    public void setSearchBills(LazyDataModel<Bill> searchBills) {
        this.searchBills = searchBills;
    }

    public String getTxtSearch() {
        return txtSearch;
    }

    public void setTxtSearch(String txtSearch) {
        this.txtSearch = txtSearch;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }
}
