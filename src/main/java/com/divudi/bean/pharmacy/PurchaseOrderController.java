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
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.LazyBill;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
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
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class PurchaseOrderController implements Serializable {

    @Inject
    private SessionController sessionController;
    @EJB
    private BillFacade billFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private BillItemFacade billItemFacade;
    ///////////////
    private Bill requestedBill;
    private Bill aprovedBill;
    private double netTotal;
    private Date fromDate;
    Date toDate;
    private boolean printPreview;
    private String txtSearch;
    /////////////
//    private List<PharmaceuticalBillItem> pharmaceuticalBillItems;
    private List<PharmaceuticalBillItem> filteredValue;
    private List<Bill> billsToApprove;
    // private List<BillItem> billItems;
    // List<PharmaceuticalBillItem> pharmaceuticalBillItems;
    //////////
    @EJB
    private CommonFunctions commonFunctions;
    private LazyDataModel<Bill> searchBills;
    
     public void removeItem(BillItem bi) {
        getAprovedBill().getBillItems().remove(bi);
        getBillFacade().edit(getAprovedBill());
        
        bi.setBill(null);
        getBillItemFacade().edit(bi);
     
        getNetTotal();
    }


    public void createAll() {
        searchBills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where b.cancelledBill is null  "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        } else {
            sql = "Select b From BilledBill b where b.cancelledBill is null  "
                    + " and b.createdAt between :fromDate and :toDate and"
                    + " (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.referenceBill.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' or "
                    + " upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        }

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyOrder);
        List<Bill> lst = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        searchBills = new LazyBill(lst);

    }

    public void createNotApproved() {
        searchBills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where b.cancelledBill is null and b.referenceBill is null  "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        } else {
            sql = "Select b From BilledBill b where b.cancelledBill is null and b.referenceBill is null "
                    + " and b.createdAt between :fromDate and :toDate and"
                    + " (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.referenceBill.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' or "
                    + " upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        }

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyOrder);
        List<Bill> lst1 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where b.cancelledBill is null and  b.referenceBill.creater is null "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        } else {
            sql = "Select b From BilledBill b where b.cancelledBill is null and  b.referenceBill.creater is null "
                    + " and b.createdAt between :fromDate and :toDate and"
                    + " (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.referenceBill.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' or "
                    + " upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        }

        List<Bill> lst2 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where b.cancelledBill is null and  b.referenceBill.creater is not null and b.referenceBill.cancelled=true "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        } else {
            sql = "Select b From BilledBill b where b.cancelledBill is null and  b.referenceBill.creater is not null and b.referenceBill.cancelled=true "
                    + " and b.createdAt between :fromDate and :toDate and"
                    + " (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.referenceBill.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' or "
                    + " upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        }

        List<Bill> lst3 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        lst1.addAll(lst2);
        lst1.addAll(lst3);

        searchBills = new LazyBill(lst1);

    }

    public void createApproved() {
        searchBills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        if (txtSearch == null || txtSearch.trim().equals("")) {
            sql = "Select b From BilledBill b where b.cancelledBill is null and b.referenceBill.creater is not null and b.referenceBill.cancelled=false "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        } else {
            sql = "Select b From BilledBill b where b.cancelledBill is null and b.referenceBill.creater is not null and b.referenceBill.cancelled=false  "
                    + " and b.createdAt between :fromDate and :toDate and"
                    + " (upper(b.toInstitution.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' "
                    + "  or upper(b.referenceBill.creater.webUserPerson.name) like '%" + txtSearch.toUpperCase() + "%' or "
                    + " upper(b.referenceBill.deptId) like '%" + txtSearch.toUpperCase() + "%' "
                    + " or upper(b.netTotal) like '%" + txtSearch.toUpperCase() + "%' ) "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";
        }

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyOrder);
        List<Bill> lst = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        searchBills = new LazyBill(lst);

    }

    public void clearList() {
        filteredValue = null;
        billsToApprove = null;
        printPreview = false;
//        billItems = null;
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

    public String approve() {
        if (getAprovedBill().getPaymentMethod() == null) {
            UtilityController.addErrorMessage("Select Paymentmethod");
            return "";
        }

        for (BillItem bi : getAprovedBill().getBillItems()) {
            bi.setCreatedAt(Calendar.getInstance().getTime());
            bi.setCreater(getSessionController().getLoggedUser());
            bi.setNetValue(bi.getPharmaceuticalBillItem().getQty() * bi.getPharmaceuticalBillItem().getPurchaseRate());

            getBillItemFacade().edit(bi);

            //  getAprovedBill().getBillItems().add(bi);
        }

        getNetTotal();
        editBill();

        return viewRequestedList();
        //   printPreview = true;

    }

    public String viewRequestedList() {
        clearList();
        return "pharmacy_purhcase_order_list_to_approve";
    }

    public void editBillItem(PharmaceuticalBillItem i) {

    }

    private void editBill() {
        getAprovedBill().setTotal(getNetTotal());
        getAprovedBill().setNetTotal(getNetTotal());

        getAprovedBill().setDeptId(getBillNumberBean().institutionBillNumberGenerator(getRequestedBill().getDepartment(), getAprovedBill(), BillType.PharmacyOrder, BillNumberSuffix.PO));
        getAprovedBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getRequestedBill().getInstitution(), getAprovedBill(), BillType.PharmacyOrder, BillNumberSuffix.PO));

        getAprovedBill().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getAprovedBill().setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        getAprovedBill().setCreater(getSessionController().getLoggedUser());
        getAprovedBill().setCreatedAt(Calendar.getInstance().getTime());

        getBillFacade().edit(getAprovedBill());
    }

    @Inject
    private PharmacyController pharmacyController;

    public void onEdit(BillItem tmp) {
        tmp.setNetValue(tmp.getPharmaceuticalBillItem().getQty() * tmp.getPharmaceuticalBillItem().getPurchaseRate());

        getPharmaceuticalBillItemFacade().edit(tmp.getPharmaceuticalBillItem());
        getBillItemFacade().edit(tmp);
        //   UtilityController.addSuccessMessage("Item Updated " + tmp.getQty());
        //   getPharmacyController().setPharmacyItem(tmp.getBillItem().getItem());
    }

    public void onFocus(PharmaceuticalBillItem ph) {
        getPharmacyController().setPharmacyItem(ph.getBillItem().getItem());
    }

    public List<Bill> getBillToAprove() {
//        String sql = "Select b From BilledBill b where b.cancelledBill is null and b.createdAt between :fromDate and :toDate "
//                + "and b.retired=false and b.billType= :bTp and "
//                + "(b.id not in (Select bo.referenceBill.id from BilledBill bo  where bo.cancelledBill is null and bo.billType= :bTp2)"
//                    + "or b.id in(Select bo1.referenceBill.id from BilledBill bo1  where bo1.cancelledBill is null and bo1.createdAt is null and bo1.billType= :bTp2))";
//        
        if (billsToApprove == null) {
            String sql = "Select b From BilledBill b where b.cancelledBill is null  "
                    + " and b.createdAt between :fromDate and :toDate "
                    + "and b.retired=false and b.billType= :bTp order by b.id desc ";

            HashMap tmp = new HashMap();
            tmp.put("toDate", getToDate());
            tmp.put("fromDate", getFromDate());
            tmp.put("bTp", BillType.PharmacyOrder);
            //   tmp.put("bTp2", BillType.PharmacyOrderApprove);
            billsToApprove = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

            if (billsToApprove == null) {
                billsToApprove = new ArrayList<>();
            }

        }

        return billsToApprove;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Bill getRequestedBill() {
        if (requestedBill == null) {
            requestedBill = new BilledBill();
        }
        return requestedBill;
    }

    public void saveBill() {
        getAprovedBill().setBillType(BillType.PharmacyOrderApprove);
        getAprovedBill().setPaymentMethod(getRequestedBill().getPaymentMethod());
        getAprovedBill().setFromDepartment(getRequestedBill().getDepartment());
        getAprovedBill().setFromInstitution(getRequestedBill().getInstitution());
        getAprovedBill().setToInstitution(getRequestedBill().getToInstitution());
        getAprovedBill().setReferenceBill(getRequestedBill());

        getBillFacade().create(getAprovedBill());

    }

    public BillItem saveBillItem(PharmaceuticalBillItem i) {
        BillItem tmp = new BillItem();
        tmp.setBill(getAprovedBill());
        tmp.setItem(i.getBillItem().getItem());
        tmp.setNetValue(i.getBillItem().getNetValue());
        getBillItemFacade().create(tmp);

        return tmp;
    }

    public void savePharmacyBillItem(BillItem b, PharmaceuticalBillItem i) {
        PharmaceuticalBillItem tmp = new PharmaceuticalBillItem();
        tmp.setBillItem(b);
        tmp.setQty(i.getQty());
        tmp.setPurchaseRate(i.getPurchaseRate());
        tmp.setRetailRate(i.getRetailRate());
        getPharmaceuticalBillItemFacade().create(tmp);
    }

    public void saveBillComponent() {
        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.bill.id=" + getRequestedBill().getId();
        List<PharmaceuticalBillItem> tmp = getPharmaceuticalBillItemFacade().findBySQL(sql);

        for (PharmaceuticalBillItem i : tmp) {
            BillItem bi = new BillItem();
            bi.setBill(getAprovedBill());
            bi.setItem(i.getBillItem().getItem());
            bi.setNetValue(i.getBillItem().getNetValue());
            getBillItemFacade().create(bi);

            PharmaceuticalBillItem ph = new PharmaceuticalBillItem();
            ph.setBillItem(bi);
            ph.setQtyInUnit(i.getQtyInUnit());
            ph.setPurchaseRateInUnit(i.getPurchaseRateInUnit());
            ph.setRetailRateInUnit(i.getRetailRateInUnit());
            getPharmaceuticalBillItemFacade().create(ph);

            bi.setPharmaceuticalBillItem(ph);
            getBillItemFacade().edit(bi);

            getAprovedBill().getBillItems().add(bi);
        }

        getBillFacade().edit(getAprovedBill());
    }

    public void createOrder() {
        String sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp and b.cancelledBill is null and b.referenceBill.id=" + getRequestedBill().getId();
        HashMap tmp = new HashMap();
        tmp.put("bTp", BillType.PharmacyOrderApprove);
        List<Bill> bil = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        if (!bil.isEmpty()) {
            setAprovedBill(bil.get(0));
        } else {
            saveBill();
            saveBillComponent();
        }

        //Update Requested Bill Reference
        getRequestedBill().setReferenceBill(getAprovedBill());
        getBillFacade().edit(getRequestedBill());
    }

    public void setRequestedBill(Bill requestedBill) {
        this.requestedBill = requestedBill;
        aprovedBill = null;
        printPreview = false;
        createOrder();
    }

    public Bill getAprovedBill() {
        if (aprovedBill == null) {
            aprovedBill = new BilledBill();
        }
        return aprovedBill;
    }

    public void setAprovedBill(Bill aprovedBill) {
        this.aprovedBill = aprovedBill;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
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

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public double getNetTotal() {
        netTotal = 0.0;
//        if (getAprovedBill().getId() == null) {
//            return 0.0;
//        }
//
//        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.bill.id=" + getAprovedBill().getId();
//        List<PharmaceuticalBillItem> tmp = getPharmaceuticalBillItemFacade().findBySQL(sql);
//
//        for (PharmaceuticalBillItem ph : tmp) {
//            netTotal += ph.getQty() * ph.getPurchaseRate();
//        }

        netTotal = 0.0;
        for (BillItem bi : getAprovedBill().getBillItems()) {
            netTotal += bi.getPharmaceuticalBillItem().getQty() * bi.getPharmaceuticalBillItem().getPurchaseRate();
        }
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
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

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public List<PharmaceuticalBillItem> getFilteredValue() {
        return filteredValue;
    }

    public void setFilteredValue(List<PharmaceuticalBillItem> filteredValue) {
        this.filteredValue = filteredValue;
    }

    public List<Bill> getBillsToApprove() {
        return billsToApprove;
    }

    public void setBillsToApprove(List<Bill> billsToApprove) {
        this.billsToApprove = billsToApprove;
    }

    public PharmacyController getPharmacyController() {
        return pharmacyController;
    }

    public void setPharmacyController(PharmacyController pharmacyController) {
        this.pharmacyController = pharmacyController;
    }

    public boolean getPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

//    public List<BillItem> getBillItems() {
//        if (billItems == null) {
//            billItems = new ArrayList<>();
//        }
//        return billItems;
//    }
//
//    public void setBillItems(List<BillItem> billItems) {
//        this.billItems = billItems;
//    }
    public void makeListNull() {
//        pharmaceuticalBillItems = null;
        filteredValue = null;
        billsToApprove = null;
        searchBills = null;

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
}
