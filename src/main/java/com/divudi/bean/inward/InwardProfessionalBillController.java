/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.inward;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.BillEntry;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Item;
import com.divudi.entity.Speciality;
import com.divudi.entity.Staff;
import com.divudi.facade.BillEntryFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.BillSessionFacade;
import com.divudi.facade.FeeFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.StaffFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.faces.event.AjaxBehaviorEvent;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class InwardProfessionalBillController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @Inject
    AdmissionController admissionController;
    ////////////////////
    @EJB
    private BillFacade ejbFacade;
    @EJB
    private BillItemFacade billItemFacade;
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
    @EJB
    private StaffFacade staffFacade;
    ////////////////////
    @EJB
    private BillBean billBean;
    @EJB
    BillNumberBean billNumberBean;
    @EJB
    CommonFunctions commonFunctions;
    //////////////////    
    private List<Bill> items = null;
    List<BillFee> lstBillFees;
    List<BillItem> lstBillItems;
    List<BillEntry> lstBillEntries;
    /////////////////
    String patientTabId = "tabNewPt";
    String selectText = "";
    private String ageText;
    private Speciality speciality;
    private Staff staff;
    private Bill current;
    BillEntry removeBillEntry;
    private BillFee currentBillFee;
    private double billTotal;
    double cashPaid;
    double cashBalance;
    private Integer index;
    private Date newDob;
    boolean toClearBill = false;
    boolean printPreview;

    public List<Staff> completeStaff(String query) {
        List<Staff> suggestions;
        String sql;
        HashMap hm = new HashMap();

        if (getCurrentBillFee() != null && getCurrentBillFee().getSpeciality() != null) {
            sql = " select p from Staff p where p.retired=false and "
                    + " (upper(p.person.name) like :q "
                    + " or  upper(p.code) like :q  ) "
                    + " and p.speciality=:spe order by p.person.name";
            hm.put("spe", getCurrentBillFee().getSpeciality());
        } else {
            sql = " select p from Staff p where p.retired=false and "
                    + " (upper(p.person.name) "
                    + " like :q or  upper(p.code) like :q "
                    + " ) order by p.person.name";
        }
        hm.put("q", "%" + query.toUpperCase() + "%");
        suggestions = getStaffFacade().findBySQL(sql, hm, 20);

        return suggestions;
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
            lstBillEntries = new ArrayList<BillEntry>();
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
            // lstBillFees = getBillBean().billFeesFromBillEntries(getLstBillEntries());
            lstBillFees = new ArrayList<BillFee>();
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

    public void addToBill() {
        if (getCurrent().getPatientEncounter() == null) {
            UtilityController.addErrorMessage("Please Select Patient Encounter");
            return;
        }

        if (currentBillFee == null) {
            UtilityController.addErrorMessage("Nothing to add");
            return;
        }

        getCurrent().setDepartment(getSessionController().getLoggedUser().getDepartment());
        getCurrent().setInstitution(getSessionController().getLoggedUser().getInstitution());
        currentBillFee.setPatienEncounter(getCurrent().getPatientEncounter());
        currentBillFee.setTransSerial(lstBillFees.size());
        lstBillFees.add(getCurrentBillFee());
        calTotals();
        //    clearBillItemValues();

        currentBillFee = null;
        //   UtilityController.addSuccessMessage("Fee Added");
    }

    public void feeChanged() {
        lstBillItems = null;
        getLstBillItems();
        calTotals();
    }

    private void calTotals() {
        double tot = 0.0;
        double dis = 0.0;
        int index = 0;
        for (BillFee bf : getLstBillFees()) {
            bf.setTransSerial(++index);
            tot += bf.getFeeValue();
        }

        getCurrent().setDiscount(dis);
        getCurrent().setTotal(tot);
        getCurrent().setNetTotal(tot - dis);

    }

    public void clearBillItemValues() {
        //setCurrentBillItem(null);
        // setCurrentBillFee(null);
        recreateBillItems();
    }

    public void onTabChange(TabChangeEvent event) {
        setPatientTabId(event.getTab().getId());

    }

    private Bill saveBill(BillFee bi) {
        Bill bill = new BilledBill();
        bill.setBillType(BillType.InwardBill);
        bill.setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), BillType.InwardBill, BillNumberSuffix.INWPRO));
        bill.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getLoggedUser().getInstitution(), bill, BillType.InwardBill, BillNumberSuffix.INWPRO));

        /////////
        bill.setPatientEncounter(getCurrent().getPatientEncounter());
        bill.setReferredBy(getCurrent().getReferredBy());
        bill.setCollectingCentre(getCurrent().getCollectingCentre());
        bill.setStaff(getCurrent().getStaff());
        bill.setTotal(bi.getFeeValue());
        bill.setNetTotal(bi.getFeeValue());
        ////////////////

        bill.setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        bill.setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        bill.setPatient(getCurrent().getPatientEncounter().getPatient());

        bill.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        bill.setCreater(getSessionController().getLoggedUser());

        getFacade().create(bill);
        return bill;
    }

    private boolean errorCheck() {
        if (getCurrent().getPatientEncounter() == null) {
            UtilityController.addErrorMessage("Selct Patient Encounter");
            return true;
        }

        if (lstBillFees.size() <= 0) {
            UtilityController.addErrorMessage("Professional Fee Should Not Empty");
            return true;
        }

        return false;
    }

    public void save() {
        if (errorCheck()) {
            return;
        }

        for (BillFee bf : getLstBillFees()) {
            Bill b = saveBill(bf);
            saveBillFee(b, bf);
        }
        UtilityController.addSuccessMessage("Bill Saved");
        current = null;
        currentBillFee = null;
        //currentBillItem=null;
        lstBillEntries = null;
        lstBillFees = null;
        lstBillItems = null;
    }

    private void saveBillFee(Bill b, BillFee bf) {

        bf.setBill(b);
        bf.setPatienEncounter(getCurrent().getPatientEncounter());
        bf.setPatient(getCurrent().getPatientEncounter().getPatient());
        bf.setCreater(getSessionController().getLoggedUser());
        bf.setCreatedAt(Calendar.getInstance().getTime());
        getBillFeeFacade().create(bf);

    }

    private void clearBillValues() {
        current = null;
        getCurrent();

        toClearBill = false;

        currentBillFee = new BillFee();
        //currentBillItem = new BillItem();
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

    public InwardProfessionalBillController() {
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
        String sql = "SELECT i FROM Bill i where i.retired=false and i.billType=:btp ";
        HashMap hm = new HashMap();
        hm.put("btp", BillType.OpdBill);
        items = getEjbFacade().findBySQL(sql);
        if (items == null) {
            items = new ArrayList<Bill>();
        }
        return items;

    }

    public void removeBillItem() {
        //TODO: Need to add Logic
        //System.out.println(getIndex());
        if (getIndex() != null) {
            //   boolean remove;
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

    public void remove(BillFee bf) {
        getLstBillFees().remove(bf.getTransSerial());
        calTotals();
    }

    public void recreateList(BillEntry r) {
        List<BillEntry> temp = new ArrayList<BillEntry>();
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

    public Date getNewDob() {
        return newDob;
    }

    public void setNewDob(Date newDob) {
        this.newDob = newDob;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    public String getAgeText() {
        return ageText;
    }

    public void setAgeText(String ageText) {
        this.ageText = ageText;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public BillFee getCurrentBillFee() {
        if (currentBillFee == null) {
            currentBillFee = new BillFee();

        }

        return currentBillFee;
    }

    public void setCurrentBillFee(BillFee currentBillFee) {
        this.currentBillFee = currentBillFee;

    }

    public StaffFacade getStaffFacade() {
        return staffFacade;
    }

    public void setStaffFacade(StaffFacade staffFacade) {
        this.staffFacade = staffFacade;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

}
