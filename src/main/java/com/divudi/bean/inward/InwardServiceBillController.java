/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.inward;

import com.divudi.bean.BillController;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.FeeType;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.BillEntry;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Fee;
import com.divudi.entity.InwardPriceAdjustment;
import com.divudi.entity.Item;
import com.divudi.entity.ItemFee;
import com.divudi.facade.BillEntryFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.BillSessionFacade;
import com.divudi.facade.FeeFacade;
import com.divudi.facade.InwardPriceAdjustmentFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.ItemFeeFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.AjaxBehaviorEvent;
import javax.persistence.TemporalType;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 Informatics)
 */
@Named
@ViewScoped
public class InwardServiceBillController implements Serializable {
//    UID

    private static final long serialVersionUID = 1L;
//    Managed Properties
    @Inject
    SessionController sessionController;
    //    EJBs
    @Inject
    private BillFacade ejbFacade;
    @EJB
    private BillItemFacade billItemFacade;
    AdmissionController admissionController;
    @EJB
    FeeFacade feeFacade;
    @EJB
    BillEntryFacade billEntryFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private BillSessionFacade billSessionFacade;
    @EJB
    ItemFacade itemFacade;
    //
    @EJB
    private BillBean billBean;
    @EJB
    BillNumberBean billNumberBean;
    @EJB
    private CommonFunctions commonFunction;
    //
    String patientTabId = "tabNewPt";
    // Bill Values
    private Bill current;
    private List<Bill> items = null;
    String selectText = "";
    //
    private BillItem currentBillItem;
    private double billTotal;
    //
    List<BillFee> lstBillFees;
    List<BillItem> lstBillItems;
    List<BillEntry> lstBillEntries;
    private Integer index;
    //
//    private BillEntry entry;
    boolean printPreview;
    BillEntry removeBillEntry;
    double cashPaid;
    double cashBalance;
    @EJB
    CommonFunctions commonFunctions;
    private String ageText;
    private Date newDob;
    boolean toClearBill = false;

    public InwardServiceBillController() {
    }

    public boolean isToClearBill() {
        return toClearBill;
    }

    public void setToClearBill(boolean toClearBill) {
        this.toClearBill = toClearBill;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public void updateFees(AjaxBehaviorEvent event) {
    }

    public double getCashPaid() {
        return cashPaid;
    }

    public void setCashPaid(double cashPaid) {
        this.cashPaid = cashPaid;
        cashBalance = cashPaid - current.getTotal();
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public BillEntry getRemoveBillEntry() {
        return removeBillEntry;
    }

    public void setRemoveBillEntry(BillEntry removeBillEntry) {
        this.removeBillEntry = removeBillEntry;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public AdmissionController getAdmissionController() {
        return admissionController;
    }

    public List<Item> completeItem(String qry) {
        List<Item> completeItems = getItemFacade().findBySQL("select c from Item c where c.retired=false and (type(c) = Service or type(c) = Packege ) and upper(c.name) like '%" + qry.toUpperCase() + "%' order by c.name");
        return completeItems;
    }

    public void setAdmissionController(AdmissionController admissionController) {
        this.admissionController = admissionController;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public BillEntryFacade getBillEntryFacade() {
        return billEntryFacade;
    }

    public void setBillEntryFacade(BillEntryFacade billEntryFacade) {
        this.billEntryFacade = billEntryFacade;
    }

    public List<BillEntry> getLstBillEntries() {
        if (lstBillEntries == null) {
            lstBillEntries = new ArrayList<>();
        }
        return lstBillEntries;
    }

    public void setLstBillEntries(List<BillEntry> lstBillEntries) {
        this.lstBillEntries = lstBillEntries;
    }

    public String prepareNewBill() {
        clearBillItemValues();
        clearBillValues();
        setPrintPreview(true);
        printPreview = false;
        return "";
    }

    public void clearForNewBill() {
        clearBillItemValues();
        clearBillValues();
        setPrintPreview(true);
        printPreview = false;
        toClearBill = true;
    }

    public List<BillFee> getLstBillFees() {
        if (lstBillFees == null) {
            lstBillFees = getBillBean().billFeesFromBillEntries(getLstBillEntries());
        }
        return lstBillFees;
    }

    public void setLstBillFees(List<BillFee> lstBillFees) {
        this.lstBillFees = lstBillFees;
    }

    public List<BillItem> getLstBillItems() {
        if (lstBillItems == null) {
            lstBillItems = getBillBean().billItemsFromBillEntries(getLstBillEntries());
        }
        return lstBillItems;
    }

    public void setLstBillItems(List<BillItem> lstBillItems) {
        this.lstBillItems = lstBillItems;
    }

    public String getPatientTabId() {
        return patientTabId;
    }

    public void setPatientTabId(String patientTabId) {
        this.patientTabId = patientTabId;
    }

    public FeeFacade getFeeFacade() {
        return feeFacade;
    }

    public void setFeeFacade(FeeFacade feeFacade) {
        this.feeFacade = feeFacade;
    }

    private void recreateBillItems() {
        //Only remove Total and BillComponenbts,Fee and Sessions. NOT bill Entries
        lstBillFees = null;
        lstBillItems = null;

        billTotal = 0.0;
    }
    @EJB
    private ItemFeeFacade itemFeeFacade;

    public List<BillFee> billFeeFromBillItemWithMatrix(BillItem billItem) {
        List<BillFee> t = new ArrayList<BillFee>();
        BillFee f;
        String sql;
        sql = "Select f from ItemFee f where f.retired=false and f.item.id = " + billItem.getItem().getId();
        List<ItemFee> itemFee = getItemFeeFacade().findBySQL(sql);


        for (Fee i : itemFee) {
            f = new BillFee();
            f.setFee(i);
            f.setFeeValue(i.getFee());
            ///   //System.out.println("Fee Value is " + f.getFeeValue());
            f.setBillItem(billItem);

            f.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

            if (billItem.getItem().getDepartment() != null) {
                f.setDepartment(billItem.getItem().getDepartment());
            } else {
                f.setDepartment(billItem.getBill().getDepartment());
            }
            if (billItem.getItem().getInstitution() != null) {
                f.setInstitution(billItem.getItem().getInstitution());
            } else {
                f.setInstitution(billItem.getBill().getDepartment().getInstitution());
            }
            if (i.getStaff() != null) {
                f.setStaff(i.getStaff());
            } else {
                f.setStaff(null);
            }
            f.setSpeciality(i.getSpeciality());

            t.add(f);
        }

        BillFee bf = calInwardMargin(billItem);

        if (bf.getFeeValue() != 0.0) {
            t.add(bf);
        }

        return t;
    }
    @EJB
    private InwardPriceAdjustmentFacade priceAdjustmentFacade;

    private BillFee calInwardMargin(BillItem i) {


        BillFee f = new BillFee();
        HashMap hm = new HashMap();
        String sql = "Select f from Fee f where f.retired=false and f.FeeType=:nm";
        hm.put("nm", FeeType.Matrix);
        List<Fee> fee = getFeeFacade().findBySQL(sql, hm, TemporalType.TIME);


        Fee matrix;

        if (fee.size() <= 0) {
            matrix = new Fee();
            //   getFeeFacade().create(tmp);
        } else {
            matrix = fee.get(0);
        }

        matrix.setName("Matrix");
        matrix.setFeeType(FeeType.Matrix);

        f.setBillItem(i);
        f.setFee(matrix);

        f.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

        if (i.getItem().getCategory() == null || i.getItem().getDepartment() == null || i.getItem().getDepartment().getInstitution() == null) {
            f.setFeeValue(0.0);
            return f;
        }

        hm.clear();

        sql = "select a from InwardPriceAdjustment a where a.retired=false and a.category=:cat"
                + " and a.institution=:ins and a.department=:dep"
                + " and (a.fromPrice<" + i.getItem().getTotal() + " and a.toPrice >" + i.getItem().getTotal() + ")";

        hm.put("cat", i.getItem().getCategory());
        hm.put("ins", i.getItem().getInstitution());
        hm.put("dep", i.getItem().getDepartment());

        List<InwardPriceAdjustment> is = getPriceAdjustmentFacade().findBySQL(sql, hm);
        if (is.size() <= 0) {
            f.setFeeValue(0.0);
            return f;

        }

        matrix.setFee(is.get(0).getMargin());

        ////System.out.println("Margin : " + is.get(0).getMargin());
        f.setInstitution(i.getItem().getDepartment().getInstitution());
        f.setFeeValue(matrix.getFee());
        ////System.out.println("Margin : " + is.get(0).getMargin());

        if (matrix.getId() == null) {
            getFeeFacade().create(matrix);
        } else {
            getFeeFacade().edit(matrix);
        }


        return f;
    }

    public void addToBill() {

        if (getCurrentBillItem() == null) {
            UtilityController.addErrorMessage("Nothing to add");
            return;
        }
        if (getCurrentBillItem().getItem() == null) {
            UtilityController.addErrorMessage("Please select an service");
            return;
        }

        getCurrent().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getCurrent().setInstitution(getSessionController().getLoggedUser().getInstitution());
        getCurrentBillItem().setBill(getCurrent());
        BillEntry addingEntry = new BillEntry();
        addingEntry.setBillItem(getCurrentBillItem());
        addingEntry.setLstBillComponents(getBillBean().billComponentsFromBillItem(getCurrentBillItem()));
        //addingEntry.setLstBillFees(getBillBean().billFeefromBillItem(getCurrentBillItem()));
        addingEntry.setLstBillFees(billFeeFromBillItemWithMatrix(getCurrentBillItem()));
        addingEntry.setLstBillSessions(getBillBean().billSessionsfromBillItem(getCurrentBillItem()));
        lstBillEntries.add(addingEntry);
        getCurrentBillItem().setRate(getBillBean().billItemRate(addingEntry));
        getCurrentBillItem().setQty(1.0);
        getCurrentBillItem().setNetValue(getCurrentBillItem().getRate() * getCurrentBillItem().getQty()); // Price == Rate as Qty is 1 here

        calTotals();
        if (getCurrentBillItem().getNetValue() == 0.0) {
            UtilityController.addErrorMessage("Please enter the rate");
            return;
        }
        clearBillItemValues();
        //UtilityController.addSuccessMessage("Item Added");
    }

    public void feeChanged() {
        lstBillItems = null;
        getLstBillItems();
        calTotals();
    }

    private void calTotals() {
        double tot = 0.0;
        double dis = 0.0;

        for (BillEntry be : getLstBillEntries()) {
            BillItem bi = be.getBillItem();
            bi.setDiscount(0.0);
            bi.setGrossValue(0.0);
            bi.setNetValue(0.0);

            for (BillFee bf : be.getLstBillFees()) {
                if (bf.getBillItem().getItem().isUserChangable() && bf.getBillItem().getItem().isDiscountAllowed() != true) {
                    //System.out.println("Total is " + tot);
                    //System.out.println("Bill Fee value is " + bf.getFeeValue());
                    tot += bf.getFeeValue();
                    //System.out.println("After addition is " + tot);
                    bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getFeeValue());
                    bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFeeValue());

                } else if (getCurrent().getCreditCompany() != null) {

                    if (bf.getBillItem().getItem().isDiscountAllowed() != null && bf.getBillItem().getItem().isDiscountAllowed() == true) {

                        bf.setFeeValue(bf.getFee().getFee() / 100 * (100 - getCurrent().getCreditCompany().getLabBillDiscount()));
                        dis += (bf.getFee().getFee() / 100 * (getCurrent().getCreditCompany().getLabBillDiscount()));
                        bf.getBillItem().setDiscount(bf.getBillItem().getDiscount() + bf.getFee().getFee() / 100 * (getCurrent().getCreditCompany().getLabBillDiscount()));
                        tot += bf.getFee().getFee();
                        bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFee().getFee());
                        bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getBillItem().getGrossValue() - bf.getBillItem().getDiscount());
                    } else {

                        tot = tot + bf.getFeeValue();
                        bf.setFeeValue(bf.getFee().getFee());
                        bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFee().getFee());
                        bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getFee().getFee());
                    }
                } else {
                    //System.out.println("12");
                    if (bf.getBillItem().getItem().isDiscountAllowed() != null && bf.getBillItem().getItem().isDiscountAllowed() == true) {

                        bf.setFeeValue(bf.getFee().getFee());
                        dis = 0.0;
                        bf.getBillItem().setDiscount(0.0);

                        tot += bf.getFee().getFee();
                        bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFee().getFee());

                        bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getBillItem().getGrossValue() - bf.getBillItem().getDiscount());
                    } else {
                        //System.out.println("13");
                        tot = tot + bf.getFeeValue();
                        bf.setFeeValue(bf.getFee().getFee());
                        bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFee().getFee());
                        bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getFee().getFee());
                    }
                }
            }
            getCurrent().setDiscount(dis);
            getCurrent().setTotal(tot);
            getCurrent().setNetTotal(tot - dis);

        }
    }

    public void clearBillItemValues() {
        setCurrentBillItem(null);
        recreateBillItems();
    }

    public void onTabChange(TabChangeEvent event) {
        setPatientTabId(event.getTab().getId());

    }

    private void saveBill() {


        getCurrent().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), BillType.InwardBill,BillNumberSuffix.INWSER));
        getCurrent().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getLoggedUser().getInstitution(),getCurrent(), BillType.InwardBill,BillNumberSuffix.INWSER));
        getCurrent().setBillType(BillType.InwardBill);


        getCurrent().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setPatient(getCurrent().getPatientEncounter().getPatient());


        getCurrent().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setCreater(getSessionController().getLoggedUser());

        getFacade().create(getCurrent());

    }

    private void calculateBillItems() {
        double s = 0.0;
        double i = 0.0;
        double g = 0.0;
        for (BillEntry e : getLstBillEntries()) {
            for (BillFee bf : e.getLstBillFees()) {
                g = g + bf.getFee().getFee();
                if (bf.getFee().getStaff() == null) {
                    i = i + bf.getFeeValue();
                } else {
                    s = s + bf.getFeeValue();
                }
                if (bf.getId() == null || bf.getId() == 0) {
                    getBillFeeFacade().create(bf);
                } else {
                    getBillFeeFacade().edit(bf);
                }
            }
        }
        getCurrent().setStaffFee(s);
        getCurrent().setPerformInstitutionFee(i);
        getCurrent().setTotal(g);
        getFacade().edit(getCurrent());
    }

    private boolean errorCheck() {
        if (getCurrent().getPatientEncounter() == null) {
            UtilityController.addErrorMessage("Selct Patient Encounter");
            return true;
        }

        return false;
    }

    public void save() {
        if (errorCheck()) {
            return;
        }
        saveBill();
        saveBillItems();
        calculateBillItems();
        UtilityController.addSuccessMessage("Bill Saved");
        current = null;
        currentBillItem = null;
        lstBillEntries = null;
        lstBillFees = null;
        lstBillItems = null;
    }

    private void saveBillItems() {
        for (BillEntry e : getLstBillEntries()) {
            BillItem temBi = e.getBillItem();
            temBi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            temBi.setCreater(getSessionController().getLoggedUser());
            temBi.setBill(getCurrent());
            getBillItemFacade().create(temBi);

            saveBillFee(e);
        }
    }

    private void saveBillFee(BillEntry e) {
        for (BillFee bf : e.getLstBillFees()) {
            bf.setBill(getCurrent());
            getBillFeeFacade().create(bf);
        }
    }

    private void clearBillValues() {
        current = null;
        getCurrent();

        toClearBill = false;


        currentBillItem = new BillItem();
        setLstBillEntries(null);
    }

    public String getSelectText() {
        return selectText;
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public BillFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(BillFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public Bill getCurrent() {
        if (current == null) {
            current = new BilledBill();
            //current.setBillType(BillType.OpdBill);
            current.setDepartment(getSessionController().getLoggedUser().getDepartment());
            current.setInstitution(getSessionController().getLoggedUser().getInstitution());
        }
        return current;
    }

    public void setCurrent(Bill current) {
        this.current = current;
    }

    private BillFacade getFacade() {
        return ejbFacade;
    }

    public List<Bill> getItems() {
        //items = getFacade().findAll("name", true);
        String sql = "SELECT i FROM Bill i where i.retired=false and i.billType =:btp ";
        HashMap hm=new HashMap();
        hm.put("btp", BillType.OpdBill);
        items = getEjbFacade().findBySQL(sql,hm);
        if (items == null) {
            items = new ArrayList<Bill>();
        }
        return items;

    }

    public BillItem getCurrentBillItem() {
        if (currentBillItem == null) {
            currentBillItem = new BillItem();
            currentBillItem.setNetValue(0.0);
        }
        return currentBillItem;
    }

    public void setCurrentBillItem(BillItem currentBillItem) {
        this.currentBillItem = currentBillItem;
    }

    public void removeBillItem() {
        //TODO: Need to add Logic
        //System.out.println(getIndex());
        if (getIndex() != null) {
            boolean remove;
            BillEntry temp = getLstBillEntries().get(getIndex());
            //System.out.println("Removed Item:" + temp.getBillItem().getNetValue());
            recreateList(temp);
            // remove = getLstBillEntries().remove(getIndex());

            //  getLstBillEntries().remove(index);
            ////System.out.println("Is Removed:" + remove);

            calTotals();
            //System.out.println(getCurrent().getNetTotal());
        }
    }

    public void recreateList(BillEntry r) {
        List<BillEntry> temp = new ArrayList<>();
        for (BillEntry b : getLstBillEntries()) {
            if (b.getBillItem().getItem() != r.getBillItem().getItem()) {
                temp.add(b);
                //System.out.println(b.getBillItem().getNetValue());
            }
        }
        lstBillEntries = temp;
        lstBillFees = getBillBean().billFeesFromBillEntries(lstBillEntries);
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public double getBillTotal() {
        if (billTotal == 0.0) {
            billTotal = getBillBean().billTotalFromBillEntries(getLstBillEntries());
        }
        return billTotal;
    }

    public void setBillTotal(double billTotal) {
        this.billTotal = billTotal;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public BillSessionFacade getBillSessionFacade() {
        return billSessionFacade;
    }

    public void setBillSessionFacade(BillSessionFacade billSessionFacade) {
        this.billSessionFacade = billSessionFacade;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public CommonFunctions getCommonFunction() {
        return commonFunction;
    }

    public void setCommonFunction(CommonFunctions commonFunction) {
        this.commonFunction = commonFunction;
    }

    public Date getNewDob() {
        return newDob;
    }

    public void setNewDob(Date newDob) {
        this.newDob = newDob;


    }

    public String getAgeText() {
        return ageText;
    }

    public void setAgeText(String ageText) {
        this.ageText = ageText;
    }

    public ItemFeeFacade getItemFeeFacade() {
        return itemFeeFacade;
    }

    public void setItemFeeFacade(ItemFeeFacade itemFeeFacade) {
        this.itemFeeFacade = itemFeeFacade;
    }

    public InwardPriceAdjustmentFacade getPriceAdjustmentFacade() {
        return priceAdjustmentFacade;
    }

    public void setPriceAdjustmentFacade(InwardPriceAdjustmentFacade priceAdjustmentFacade) {
        this.priceAdjustmentFacade = priceAdjustmentFacade;
    }

 
}
