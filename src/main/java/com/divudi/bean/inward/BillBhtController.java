/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.inward;

import com.divudi.bean.BillSearch;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.inward.SurgeryBillType;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.InwardCalculation;
import com.divudi.entity.BatchBill;
import com.divudi.entity.Bill;
import com.divudi.entity.BillComponent;
import com.divudi.entity.BillEntry;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.inward.EncounterComponent;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.lab.Investigation;
import com.divudi.facade.BatchBillFacade;
import com.divudi.facade.BillComponentFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.EncounterComponentFacade;
import com.divudi.facade.FeeFacade;
import com.divudi.facade.InwardPriceAdjustmentFacade;
import com.divudi.facade.ItemFeeFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PatientInvestigationFacade;
import com.divudi.facade.PersonFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.Transient;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class BillBhtController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    /////////////////
    @EJB
    private ItemFeeFacade itemFeeFacade;
    @EJB
    private InwardPriceAdjustmentFacade priceAdjustmentFacade;
    @EJB
    private FeeFacade feeFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PatientInvestigationFacade patientInvestigationFacade;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private PatientFacade patientFacade;
    @EJB
    private BillComponentFacade billComponentFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    ///////////////////
    @EJB
    private InwardCalculation inwardCalculation;
    @EJB
    private BillBean billBean;
    @EJB
    CommonFunctions commonFunctions;
    @EJB
    private BillNumberBean billNumberBean;
    ///////////////////
    private double total;
    private double discount;
    private double netTotal;
    private double cashPaid;
    private double cashBalance;
    private String creditCardRefNo;
    private String chequeRefNo;
    private Institution chequeBank;
    private BillItem currentBillItem;
    private Integer index;
    private PatientEncounter patientEncounter;
    private PaymentScheme paymentScheme;
    private Bill batchBill;
    /////////////////////
    private List<BillComponent> lstBillComponents;
    private List<BillFee> lstBillFees;
    private List<BillItem> lstBillItems;
    private List<BillEntry> lstBillEntries;
    private boolean printPreview;
    private List<Bill> bills;
    Date date;

    public Date getDate() {
        if(date==null)date=new Date();
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void selectSurgeryBillListener() {
        patientEncounter = getBatchBill().getPatientEncounter();
    }

    public void makeNull() {
        date=null;
        total = 0.0;
        discount = 0.0;
        netTotal = 0.0;
        cashPaid = 0.0;
        cashBalance = 0.0;
        creditCardRefNo = "";
        chequeRefNo = "";
        chequeBank = null;
        currentBillItem = null;
        index = 0;
        patientEncounter = null;
        paymentScheme = null;
        lstBillComponents = null;
        lstBillFees = null;
        lstBillItems = null;
        lstBillEntries = null;
        printPreview = false;
        batchBill = null;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    @EJB
    private BatchBillFacade batchBillFacade;
    @Inject
    private BillSearch billSearch;

    private void saveBatchBill() {
        Bill tmp = new BilledBill();
        tmp.setCreatedAt(new Date());
        tmp.setCreater(getSessionController().getLoggedUser());
        getBillFacade().create(tmp);

        for (Bill b : bills) {
            b.setBackwardReferenceBill(tmp);
            getBillFacade().edit(b);
        }

        for (Bill b : bills) {
            tmp.getForwardReferenceBills().add(b);
        }

        getBillFacade().edit(tmp);

    }

    public void cancellAll() {
        for (Bill b : bills) {
            getBillSearch().setBill((BilledBill) b);
            getBillSearch().setPaymentScheme(b.getPaymentScheme());
            getBillSearch().setComment("Batch Cancell");
            //System.out.println("ggg : " + getBillSearch().getComment());
            getBillSearch().cancelBill();
        }

    }

    public void putToBills() {

        Set<Department> billDepts = new HashSet<>();
        for (BillEntry e : lstBillEntries) {
            billDepts.add(e.getBillItem().getItem().getDepartment());
        }
        for (Department d : billDepts) {
            BilledBill myBill = new BilledBill();
            saveBill(d, myBill);
            List<BillEntry> tmp = new ArrayList<>();
            for (BillEntry e : lstBillEntries) {
                if (e.getBillItem().getItem().getDepartment().getId() == d.getId()) {
                    getBillBean().saveBillItem(myBill, e, getSessionController().getLoggedUser());
                    //getBillBean().calculateBillItem(myBill, e);
                    tmp.add(e);
                }
            }
            getBillBean().calculateBillItems(myBill, tmp);
            bills.add(myBill);
        }

    }

    public void settleBill() {
        bills = new ArrayList<>();
        if (errorCheck()) {
            return;
        }

        if (getBillBean().checkDepartment(getLstBillEntries()) == 1) {
            BilledBill temp = new BilledBill();
            Bill b = saveBill(lstBillEntries.get(0).getBillItem().getItem().getDepartment(), temp);
            getBillBean().saveBillItems(b, getLstBillEntries(), getSessionController().getLoggedUser());
            getBillBean().calculateBillItems(b, getLstBillEntries());
            bills.add(b);
        } else {
            putToBills();
        }

        printPreview = true;
        saveBatchBill();

        UtilityController.addSuccessMessage("Bill Saved");

    }

    public void settleBillSurgery() {
        if (getBatchBill() == null) {
            return;
        }

        if (getBatchBill().getProcedure() == null) {
            return;
        }

        settleBill();
        getBillBean().saveEncounterComponents(bills, batchBill, getSessionController().getLoggedUser());
        getBillBean().updateBatchBill(getBatchBill());

    }

    @EJB
    private EncounterComponentFacade encounterComponentFacade;

    private Bill saveBill(Department bt, BilledBill temp) {

        temp.setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), bt, BillType.InwardBill));
        temp.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), temp, BillType.InwardBill, BillNumberSuffix.INWSER));
        //getCurrent().setCashBalance(cashBalance);
        //getCurrent().setCashPaid(cashPaid);
        temp.setBillType(BillType.InwardBill);

        getBillBean().setSurgeryData(temp, getBatchBill(), SurgeryBillType.Service);

        temp.setDepartment(getSessionController().getLoggedUser().getDepartment());
        temp.setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        temp.setFromDepartment(getInwardCalculation().getCurrentPatientRoom(patientEncounter).getRoomFacilityCharge().getDepartment());
        temp.setFromInstitution(getInwardCalculation().getCurrentPatientRoom(patientEncounter).getRoomFacilityCharge().getDepartment().getInstitution());

        temp.setToDepartment(bt);
        temp.setToInstitution(bt.getInstitution());

        temp.setBillDate(date);
        temp.setBillTime(date);
        temp.setPatientEncounter(patientEncounter);
        temp.setPaymentScheme(getPaymentScheme());

        temp.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        temp.setCreater(getSessionController().getLoggedUser());
        getFacade().create(temp);
        return temp;

    }

    private boolean errorCheck() {
        if (getLstBillEntries().isEmpty()) {

            UtilityController.addErrorMessage("No investigations are added to the bill to settle");
            return true;
        }

        if (getPatientEncounter() == null) {
            UtilityController.addErrorMessage("Please select Bht Number");
            return true;
        }

        return false;
    }

    public void addToBill() {

        if (getCurrentBillItem() == null) {
            UtilityController.addErrorMessage("Nothing to add");
            return;
        }
        if (getCurrentBillItem().getItem() == null) {
            UtilityController.addErrorMessage("Please select an investigation");
            return;
        }

        if (getPatientEncounter() == null) {
            UtilityController.addErrorMessage("Please Select Bht");
            return;
        }

        PatientRoom patientRoom = getInwardCalculation().getCurrentPatientRoom(patientEncounter);

        if (patientRoom == null) {
            UtilityController.addErrorMessage("Please Set Room or Bed For This Patient");
            return;
        }

        if (patientRoom.getRoomFacilityCharge().getDepartment() == null) {
            UtilityController.addErrorMessage("Under administration, add a Department for this Room " + patientRoom.getRoom().getName());
            return;
        }

        if (getCurrentBillItem().getItem().getDepartment() == null) {
            UtilityController.addErrorMessage("Under administration, add a Department for this item " + getCurrentBillItem().getItem().getName());
            return;
        } else if (getCurrentBillItem().getItem().getDepartment().getInstitution() == null) {
            UtilityController.addErrorMessage("Under administration, add an Institution for the department " + getCurrentBillItem().getItem().getDepartment());
            return;
        } else if (getCurrentBillItem().getItem().getCategory() == null) {
            if (!(getCurrentBillItem().getItem() instanceof Investigation)) {
                UtilityController.addErrorMessage("Under administration, add Category For Item : " + getCurrentBillItem().getItem().getName());
                return;
            } else {
                if (((Investigation) getCurrentBillItem().getItem()).getInvestigationCategory() == null) {
                    UtilityController.addErrorMessage("Under administration, add Category For Investigation " + getCurrentBillItem().getItem().getName());
                    return;
                }
            }

        }

        if (getCurrentBillItem().getItem().isRequestForQuentity()) {

        } else {
            getCurrentBillItem().setQty(1.0);
        }

        for (int i = 0; i < getCurrentBillItem().getQty(); i++) {
            BillEntry addingEntry = new BillEntry();
            BillItem billItem = new BillItem();

            billItem.copy(currentBillItem);
            billItem.setQty(1.0);
            addingEntry.setBillItem(billItem);
            addingEntry.setLstBillComponents(getBillBean().billComponentsFromBillItem(billItem));
            System.err.println("Add To Bill");
            addingEntry.setLstBillFees(getInwardCalculation().billFeeFromBillItemWithMatrix(billItem, getPatientEncounter(), billItem.getItem().getInstitution()));
            addingEntry.setLstBillSessions(getBillBean().billSessionsfromBillItem(billItem));
            lstBillEntries.add(addingEntry);

            billItem.setRate(getBillBean().billItemRate(addingEntry));

            calTotals();
            if (billItem.getNetValue() == 0.0) {
                UtilityController.addErrorMessage("Please enter the rate");
                return;
            }
        }

        clearBillItemValues();
        //UtilityController.addSuccessMessage("Item Added");
    }

    public void clearBillItemValues() {
        setCurrentBillItem(null);
        recreateBillItems();
    }

    private void recreateBillItems() {
        //Only remove Total and BillComponenbts,Fee and Sessions. NOT bill Entries
        lstBillComponents = null;
        lstBillFees = null;
        lstBillItems = null;

        //billTotal = 0.0;
    }

    public void calTotals() {
        double tot = 0.0;
        double dis = 0.0;

        for (BillEntry be : getLstBillEntries()) {
            BillItem bi = be.getBillItem();
            bi.setDiscount(0.0);
            bi.setGrossValue(0.0);
            bi.setNetValue(0.0);

            for (BillFee bf : be.getLstBillFees()) {
                tot += bf.getFeeValue();
                bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getFeeValue());
                //    bf.getBillItem().setNetValue(bf.getBillItem().getNetValue());
                bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFeeValue());

            }
        }
        setDiscount(dis);
        setTotal(tot);
        setNetTotal(tot - dis);
    }

    public void feeChanged() {
        lstBillItems = null;
        getLstBillItems();
        calTotals();
    }

    public void prepareNewBill() {
        clearBillItemValues();
        makeNull();
        printPreview = false;

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

        }

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
        lstBillComponents = getBillBean().billComponentsFromBillEntries(lstBillEntries);
        lstBillFees = getBillBean().billFeesFromBillEntries(lstBillEntries);
    }

    public BillFacade getEjbFacade() {
        return billFacade;
    }

    public void setEjbFacade(BillFacade ejbFacade) {
        this.billFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillBhtController() {
    }

    private BillFacade getFacade() {
        return billFacade;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public PaymentScheme getPaymentScheme() {
        return paymentScheme;
    }

    public void setPaymentScheme(PaymentScheme paymentScheme) {
        this.paymentScheme = paymentScheme;
    }

    public List<BillComponent> getLstBillComponents() {
        if (lstBillComponents == null) {
            lstBillComponents = getBillBean().billComponentsFromBillEntries(getLstBillEntries());
        }

        return lstBillComponents;
    }

    public void setLstBillComponents(List<BillComponent> lstBillComponents) {
        this.lstBillComponents = lstBillComponents;
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
            lstBillItems = new ArrayList<BillItem>();
        }
        return lstBillItems;
    }

    public void setLstBillItems(List<BillItem> lstBillItems) {
        this.lstBillItems = lstBillItems;
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public double getCashPaid() {
        return cashPaid;
    }

    public void setCashPaid(double cashPaid) {
        this.cashPaid = cashPaid;
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
    }

    public String getCreditCardRefNo() {
        return creditCardRefNo;
    }

    public void setCreditCardRefNo(String creditCardRefNo) {
        this.creditCardRefNo = creditCardRefNo;
    }

    public String getChequeRefNo() {
        return chequeRefNo;
    }

    public void setChequeRefNo(String chequeRefNo) {
        this.chequeRefNo = chequeRefNo;
    }

    public Institution getChequeBank() {
        if (chequeBank == null) {
            chequeBank = new Institution();
        }

        return chequeBank;
    }

    public void setChequeBank(Institution chequeBank) {
        this.chequeBank = chequeBank;
    }

    public BillItem getCurrentBillItem() {
        if (currentBillItem == null) {
            currentBillItem = new BillItem();
        }

        return currentBillItem;
    }

    public void setCurrentBillItem(BillItem currentBillItem) {
        this.currentBillItem = currentBillItem;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;

    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    public PatientFacade getPatientFacade() {
        return patientFacade;
    }

    public void setPatientFacade(PatientFacade patientFacade) {
        this.patientFacade = patientFacade;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;

    }

    public BillComponentFacade getBillComponentFacade() {
        return billComponentFacade;
    }

    public void setBillComponentFacade(BillComponentFacade billComponentFacade) {
        this.billComponentFacade = billComponentFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public PatientInvestigationFacade getPatientInvestigationFacade() {
        return patientInvestigationFacade;
    }

    public void setPatientInvestigationFacade(PatientInvestigationFacade patientInvestigationFacade) {
        this.patientInvestigationFacade = patientInvestigationFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;

    }

    public PatientEncounter getPatientEncounter() {
        return patientEncounter;
    }

    public void setPatientEncounter(PatientEncounter patientEncounter) {
        this.patientEncounter = patientEncounter;

    }

    public InwardPriceAdjustmentFacade getPriceAdjustmentFacade() {
        return priceAdjustmentFacade;
    }

    public void setPriceAdjustmentFacade(InwardPriceAdjustmentFacade priceAdjustmentFacade) {
        this.priceAdjustmentFacade = priceAdjustmentFacade;
    }

    public FeeFacade getFeeFacade() {
        return feeFacade;
    }

    public void setFeeFacade(FeeFacade feeFacade) {
        this.feeFacade = feeFacade;
    }

    public ItemFeeFacade getItemFeeFacade() {
        return itemFeeFacade;
    }

    public void setItemFeeFacade(ItemFeeFacade itemFeeFacade) {
        this.itemFeeFacade = itemFeeFacade;
    }

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;

    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public BatchBillFacade getBatchBillFacade() {
        return batchBillFacade;
    }

    public void setBatchBillFacade(BatchBillFacade batchBillFacade) {
        this.batchBillFacade = batchBillFacade;
    }

    public BillSearch getBillSearch() {
        return billSearch;
    }

    public void setBillSearch(BillSearch billSearch) {
        this.billSearch = billSearch;
    }

    public Bill getBatchBill() {
        return batchBill;
    }

    public void setBatchBill(Bill batchBill) {
        this.batchBill = batchBill;
    }

    public EncounterComponentFacade getEncounterComponentFacade() {
        return encounterComponentFacade;
    }

    public void setEncounterComponentFacade(EncounterComponentFacade encounterComponentFacade) {
        this.encounterComponentFacade = encounterComponentFacade;
    }

    /**
     *
     */
    @FacesConverter(forClass = Bill.class)
    public static class BillControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BillBhtController controller = (BillBhtController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "billBhtController");
            return controller.getBillFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Bill) {
                Bill o = (Bill) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + BillBhtController.class.getName());
            }
        }
    }
}
