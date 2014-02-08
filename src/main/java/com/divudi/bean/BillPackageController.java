/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean;

import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.Title;
import com.divudi.data.dataStructure.YearMonthDay;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.BillComponent;
import com.divudi.entity.BillEntry;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Department;
import com.divudi.entity.Doctor;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.Packege;
import com.divudi.entity.Patient;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.Person;
import com.divudi.entity.Staff;
import com.divudi.facade.BillComponentFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PatientInvestigationFacade;
import com.divudi.facade.PersonFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 Informatics)
 */
@Named
@SessionScoped
public class BillPackageController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    private boolean printPreview;
    private String patientTabId = "tabNewPt";
    //Interface Data
    private PaymentScheme paymentScheme;
    private Patient newPatient;
    private Patient searchedPatient;
    private Doctor referredBy;
    private Institution creditCompany;
    private Staff staff;
    private double total;
    private double discount;
    private double netTotal;
    private double cashPaid;
    private double cashBalance;
    private String creditCardRefNo;
    private String chequeRefNo;
    private Institution chequeBank;
    private BillItem currentBillItem;
    //Bill Items
    private List<BillComponent> lstBillComponents;
    private List<BillFee> lstBillFees;
    private List<BillItem> lstBillItems;
    private List<BillEntry> lstBillEntries;
    private Integer index;
    @EJB
    private PatientInvestigationFacade patientInvestigationFacade;
    @EJB
    private BillBean billBean;
    @EJB
    CommonFunctions commonFunctions;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private PatientFacade patientFacade;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private BillComponentFacade billComponentFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    //Temprory Variable
    private Patient tmpPatient;
    List<Bill> bills;
    private Institution slipBank;
    private String comment;
    private Institution creditBank;
    private Date chequeDate;
    private Date slipDate;
    @Inject
    private BillSearch billSearch;
    private YearMonthDay yearMonthDay;

    public void cancellAll() {
        for (Bill b : bills) {
            getBillSearch().setBill((BilledBill) b);
            getBillSearch().setPaymentScheme(b.getPaymentScheme());
            getBillSearch().setComment("Batch Cancell");
            //  //System.out.println("ggg : " + getBillSearch().getComment());
            getBillSearch().cancelBill();
        }
    }

    public Title[] getTitle() {

        return Title.values();
    }

    public void dateChangeListen() {
        getNewPatient().getPerson().setDob(getCommonFunctions().guessDob(yearMonthDay));

    }

    public Sex[] getSex() {
        return Sex.values();
    }

    public List<Bill> getBills() {
        if (bills == null) {
            bills = new ArrayList<Bill>();
        }
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    private void savePatient() {
        if (getPatientTabId().equals("tabNewPt")) {
            getNewPatient().setCreater(getSessionController().getLoggedUser());
            getNewPatient().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

            getNewPatient().getPerson().setCreater(getSessionController().getLoggedUser());
            getNewPatient().getPerson().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());

            getPersonFacade().create(getNewPatient().getPerson());
            getPatientFacade().create(getNewPatient());
            tmpPatient = getNewPatient();

        } else if (getPatientTabId().equals("tabSearchPt")) {
            tmpPatient = getSearchedPatient();
        }
    }

    public void putToBills() {
        bills = new ArrayList<Bill>();
        Set<Department> billDepts = new HashSet<Department>();
        for (BillEntry e : lstBillEntries) {
            billDepts.add(e.getBillItem().getItem().getDepartment());

        }
        for (Department d : billDepts) {
            BilledBill myBill = new BilledBill();

            saveBill(d, myBill);

            List<BillEntry> tmp = new ArrayList<BillEntry>();
            for (BillEntry e : lstBillEntries) {

                if (e.getBillItem().getItem().getDepartment().getId() == d.getId()) {
                    getBillBean().saveBillItem(myBill, e, getSessionController().getLoggedUser());
                    // getBillBean().calculateBillItem(myBill, e);                
                    tmp.add(e);
                }
            }
            //System.out.println("555");
            getBillBean().calculateBillItems(myBill, tmp);
            bills.add(myBill);
        }
    }

    public void settleBill() {

        if (errorCheck()) {
            return;
        }
        savePatient();
        if (getBillBean().checkDepartment(getLstBillEntries()) == 1) {
            BilledBill temp = new BilledBill();
            Bill b = saveBill(lstBillEntries.get(0).getBillItem().getItem().getDepartment(), temp);
            getBillBean().saveBillItems(b, getLstBillEntries(), getSessionController().getLoggedUser());
            getBillBean().calculateBillItems(b, getLstBillEntries());
            getBills().add(b);

        } else {
            //    //System.out.println("11");
            putToBills();
            //   //System.out.println("22");
        }

        clearBillItemValues();
        //System.out.println("33");
        UtilityController.addSuccessMessage("Bill Saved");
        printPreview = true;
    }

    private Bill saveBill(Department bt, BilledBill temp) {

        temp.setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), bt, BillType.OpdBill));
        temp.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(),bt,new BilledBill(),BillType.OpdBill,BillNumberSuffix.PACK));
        //getCurrent().setCashBalance(cashBalance); 
        //getCurrent().setCashPaid(cashPaid);
        //  temp.setBillType(bt);
        temp.setBillType(BillType.OpdBill);

        temp.setBillPackege((Packege) currentBillItem.getItem());

        temp.setDepartment(getSessionController().getLoggedUser().getDepartment());
        temp.setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        temp.setToDepartment(bt);
        temp.setToInstitution(bt.getInstitution());

        temp.setFromDepartment(getSessionController().getLoggedUser().getDepartment());
        temp.setFromInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

        temp.setStaff(staff);
        temp.setReferredBy(referredBy);
        temp.setCreditCompany(creditCompany);

        if (paymentScheme.getPaymentMethod().equals(PaymentMethod.Cheque)) {
            temp.setBank(chequeBank);
            temp.setChequeRefNo(chequeRefNo);
            temp.setChequeDate(chequeDate);
        }

        if (paymentScheme.getPaymentMethod().equals(PaymentMethod.Slip)) {
            temp.setBank(slipBank);
            temp.setChequeDate(slipDate);
            temp.setComments(comment);
        }

        if (paymentScheme.getPaymentMethod().equals(PaymentMethod.Card)) {
            temp.setCreditCardRefNo(creditCardRefNo);
            temp.setBank(creditBank);
        }

        temp.setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        temp.setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        temp.setPatient(tmpPatient);
//        temp.setPatientEncounter(patientEncounter);
        temp.setPaymentScheme(getPaymentScheme());

        temp.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        temp.setCreater(getSessionController().getLoggedUser());
        getFacade().create(temp);
        return temp;

    }

    private boolean errorCheck() {
        if (getPatientTabId().toString().equals("tabNewPt")) {

            if (getNewPatient().getPerson().getName() == null || getNewPatient().getPerson().getName().trim().equals("") || getNewPatient().getPerson().getSex() == null || getNewPatient().getPerson().getDob() == null) {
                UtilityController.addErrorMessage("Can not bill without Patient Name, Age or Sex.");
                return true;
            }

            if (!getCommonFunctions().checkAgeSex(getNewPatient().getPerson().getDob(), getNewPatient().getPerson().getSex(), getNewPatient().getPerson().getTitle())) {
                UtilityController.addErrorMessage("Check Title,Age,Sex");
                return true;
            }

            if (getNewPatient().getPerson().getPhone().length() < 1) {
                UtilityController.addErrorMessage("Phone Number is Required it should be fill");
                return true;
            }

        }
        if (getLstBillEntries().isEmpty()) {

            UtilityController.addErrorMessage("No investigations are added to the bill to settle");
            return true;
        }

        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod().toString().equals("Cheque")) {
            if (getChequeBank() == null || getChequeRefNo() == null || getChequeDate() == null) {
                UtilityController.addErrorMessage("Please select Cheque Number and Bank");
                return true;
            }
        }

        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Slip) {
            if (getSlipBank() == null || getComment() == null || getSlipDate() == null) {
                UtilityController.addErrorMessage("Please Fill Memo,Bank and Slip Date ");
                return true;
            }

        }

        if (paymentScheme != null && paymentScheme.getPaymentMethod() != null && paymentScheme.getPaymentMethod() == PaymentMethod.Card) {
            if (getCreditBank() == null || getCreditCardRefNo() == null) {
                UtilityController.addErrorMessage("Please Fill Credit Card Number,Bank and Cheque Date");
                return true;
            }

            if (getCreditCardRefNo().trim().length() < 16) {
                UtilityController.addErrorMessage("Enter 16 Digit");
                return true;
            }

        }

        if (paymentScheme.getPaymentMethod() == PaymentMethod.Cash) {
            if (cashPaid == 0.0) {
                UtilityController.addErrorMessage("Please select tendered amount correctly");
                return true;
            }
            if (cashPaid < getNetTotal()) {
                UtilityController.addErrorMessage("Please select tendered amount correctly");
                return true;
            }
        }

        return false;
    }

    private void addEntry(BillItem bi) {
        if (bi == null) {
            UtilityController.addErrorMessage("Nothing to add");
            return;
        }
        if (bi.getItem() == null) {
            UtilityController.addErrorMessage("Please select an investigation");
            return;
        }

        BillEntry addingEntry = new BillEntry();
        addingEntry.setBillItem(bi);
        addingEntry.setLstBillComponents(getBillBean().billComponentsFromBillItem(bi));
        addingEntry.setLstBillFees(getBillBean().billFeefromBillItemPackage(bi, currentBillItem.getItem()));
        addingEntry.setLstBillSessions(getBillBean().billSessionsfromBillItem(bi));
        getLstBillEntries().add(addingEntry);
        bi.setRate(getBillBean().billItemRate(addingEntry));
        bi.setQty(1.0);
        bi.setNetValue(bi.getRate() * bi.getQty()); // Price == Rate as Qty is 1 here

        calTotals();
        if (bi.getNetValue() == 0.0) {
            UtilityController.addErrorMessage("Please enter the rate");
            return;
        }
        //      clearBillItemValues();
        recreateBillItems();
    }

    public void addToBill() {
        if (getLstBillEntries().size() > 0) {
            UtilityController.addErrorMessage("You can not add more than on package at a time create new bill");
            return;
        }

        List<Item> itemList = getBillBean().itemFromPackage(currentBillItem.getItem());
        for (Item i : itemList) {
            if (i.getDepartment() == null) {
                UtilityController.addErrorMessage("Under administration, add a Department for item " + i.getName());
                return;
            }

            BillItem tmp = new BillItem();
            tmp.setItem(i);
            addEntry(tmp);
        }

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
//                if (bf.getBillItem().getItem().isUserChangable() && bf.getBillItem().getItem().getDiscountAllowed() != true) {
                //System.out.println("Total is " + tot);
                //    //System.out.println("Bill Fee value is " + bf.getFeeValue());
                tot += bf.getFeeValue();
                //System.out.println("After addition is " + tot);
                bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getFeeValue());
                bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFeeValue());

//                } else if (getCreditCompany() != null) {
//
//                    if (bf.getBillItem().getItem().getDiscountAllowed() != null && bf.getBillItem().getItem().getDiscountAllowed() == true) {
//
//                        bf.setFeeValue(bf.getFee().getFee() / 100 * (100 - getCreditCompany().getLabBillDiscount()));
//                        dis += (bf.getFee().getFee() / 100 * (getCreditCompany().getLabBillDiscount()));
//                        bf.getBillItem().setDiscount(bf.getBillItem().getDiscount() + bf.getFee().getFee() / 100 * (getCreditCompany().getLabBillDiscount()));
//                        tot += bf.getFee().getFee();
//                        bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFee().getFee());
//                        bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getBillItem().getGrossValue() - bf.getBillItem().getDiscount());
//                    } else {
//
//                        tot = tot + bf.getFeeValue();
//                        bf.setFeeValue(bf.getFee().getFee());
//                        bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFee().getFee());
//                        bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getFee().getFee());
//                    }
//                } else {
//                    //System.out.println("12");
//                    if (bf.getBillItem().getItem().getDiscountAllowed() != null && bf.getBillItem().getItem().getDiscountAllowed() == true) {
//                        if (getPaymentScheme() == null) {
//                            bf.setFeeValue(bf.getFee().getFee());
//                            dis = 0.0;
//                            bf.getBillItem().setDiscount(0.0);
//                        } else {
//                            bf.setFeeValue(bf.getFee().getFee() / 100 * (100 - getPaymentScheme().getDiscountPercentForLabBill()));
//                            dis += (bf.getFee().getFee() / 100 * (getPaymentScheme().getDiscountPercentForLabBill()));
//                            bf.getBillItem().setDiscount(bf.getBillItem().getDiscount() + bf.getFee().getFee() / 100 * (getPaymentScheme().getDiscountPercentForLabBill()));
//                        }
//                        tot += bf.getFee().getFee();
//                        bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFee().getFee());
//
//                        bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getBillItem().getGrossValue() - bf.getBillItem().getDiscount());
//                    } else {
//                        //System.out.println("13");
//                        tot = tot + bf.getFeeValue();
//                        bf.setFeeValue(bf.getFee().getFee());
//                        bf.getBillItem().setGrossValue(bf.getBillItem().getGrossValue() + bf.getFee().getFee());
//                        bf.getBillItem().setNetValue(bf.getBillItem().getNetValue() + bf.getFee().getFee());
//                    }
//                }
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

    public void clearBillValues() {
        setNewPatient(null);
        setSearchedPatient(null);
        setReferredBy(null);
        setCreditCompany(null);
        setYearMonthDay(null);
        setBills(null);
        setComment("");
        setChequeBank(null);
        setSlipBank(null);
        setChequeRefNo("");
        setCreditCardRefNo("");
        setChequeDate(null);
        setSlipDate(null);

        setCurrentBillItem(null);
        setLstBillComponents(null);
        setLstBillEntries(null);
        setLstBillFees(null);
        setStaff(null);
        setPatientTabId("tabNewPt");
        lstBillEntries = new ArrayList<BillEntry>();
        //   setForeigner(false);
        calTotals();

        setCashPaid(0.0);
        setDiscount(0.0);
        setCashBalance(0.0);
        printPreview = false;
    }

    public String prepareNewBill() {
        clearBillItemValues();
        clearBillValues();
        setPrintPreview(true);
        printPreview = false;
        return "opd_bill_package";
    }

    public void removeBillItem() {
        currentBillItem = null;
        lstBillComponents = null;
        lstBillFees = null;
        lstBillItems = null;
        lstBillEntries = null;
        setCashPaid(0.0);
        setDiscount(0.0);
        setCashBalance(0.0);
        setTotal(0.0);
        setNetTotal(0.0);
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

    public void onTabChange(TabChangeEvent event) {
        setPatientTabId(event.getTab().getId());

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

    public BillPackageController() {
    }

    private BillFacade getFacade() {
        return billFacade;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
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
        calTotals();
    }

    public String getPatientTabId() {
        return patientTabId;
    }

    public void setPatientTabId(String patientTabId) {
        this.patientTabId = patientTabId;
    }

    public Patient getNewPatient() {
        if (newPatient == null) {
            newPatient = new Patient();
            Person p = new Person();

            newPatient.setPerson(p);
        }
        return newPatient;
    }

    public void setNewPatient(Patient newPatient) {
        this.newPatient = newPatient;
    }

    public Patient getSearchedPatient() {
        return searchedPatient;
    }

    public void setSearchedPatient(Patient searchedPatient) {
        this.searchedPatient = searchedPatient;
    }

    public Doctor getReferredBy() {

        return referredBy;
    }

    public void setReferredBy(Doctor referredBy) {
        this.referredBy = referredBy;
    }

    public Institution getCreditCompany() {

        return creditCompany;
    }

    public void setCreditCompany(Institution creditCompany) {
        this.creditCompany = creditCompany;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
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
        cashBalance = cashPaid - getNetTotal();
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

    private Patient getTmpPatient() {
        return tmpPatient;
    }

    public void setTmpPatient(Patient tmpPatient) {
        this.tmpPatient = tmpPatient;
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

    public Institution getSlipBank() {
        return slipBank;
    }

    public void setSlipBank(Institution slipBank) {
        this.slipBank = slipBank;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Institution getCreditBank() {
        return creditBank;
    }

    public void setCreditBank(Institution creditBank) {
        this.creditBank = creditBank;
    }

    public Date getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(Date chequeDate) {
        this.chequeDate = chequeDate;
    }

    public Date getSlipDate() {
        return slipDate;
    }

    public void setSlipDate(Date slipDate) {
        this.slipDate = slipDate;
    }

    public BillSearch getBillSearch() {
        return billSearch;
    }

    public void setBillSearch(BillSearch billSearch) {
        this.billSearch = billSearch;
    }

    public YearMonthDay getYearMonthDay() {
        if (yearMonthDay == null) {
            yearMonthDay = new YearMonthDay();
        }
        return yearMonthDay;
    }

    public void setYearMonthDay(YearMonthDay yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
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
            BillPackageController controller = (BillPackageController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "billPackageController");
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
                        + object.getClass().getName() + "; expected type: " + BillPackageController.class.getName());
            }
        }
    }
}
