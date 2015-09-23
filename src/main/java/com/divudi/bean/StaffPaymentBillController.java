package com.divudi.bean;

import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.dataStructure.SearchKeyword;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CashTransactionBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.BillComponent;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.Speciality;
import com.divudi.entity.Staff;
import com.divudi.entity.WebUser;
import com.divudi.facade.BillComponentFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.CancelledBillFacade;
import com.divudi.facade.RefundBillFacade;
import com.divudi.facade.StaffFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.TemporalType;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class StaffPaymentBillController implements Serializable {

    @EJB
    private RefundBillFacade refundBillFacade;
    private List<BillComponent> billComponents;
    @EJB
    private CancelledBillFacade cancelledBillFacade;
    @EJB
    private BillComponentFacade billComponentFacade;
    @EJB
    BillFeeFacade billFeeFacade;
    private List<BillItem> billItems;
    private static final long serialVersionUID = 1L;
    private Date fromDate;
    private Date toDate;
    @Inject
    SessionController sessionController;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    List<Bill> selectedItems;
    private Bill current;
    private List<Bill> items = null;
    String selectText = "";
    Staff currentStaff;
    private List<BillFee> dueBillFeeReport;
    List<BillFee> dueBillFees;
    List<BillFee> payingBillFees;
    double totalDue;
    double totalPaying;
    @EJB
    BillNumberBean billNumberBean;
    private Boolean printPreview = false;
    PaymentScheme paymentScheme;
    Speciality speciality;
    @EJB
    StaffFacade staffFacade;
    private SearchKeyword searchKeyword;

    public List<BillComponent> getBillComponents() {
        if (getCurrent() != null) {
            String sql = "SELECT b FROM BillComponent b WHERE b.retired=false and b.bill.id=" + getCurrent().getId();
            billComponents = getBillComponentFacade().findBySQL(sql);
            if (billComponents == null) {
                billComponents = new ArrayList<BillComponent>();
            }
        }
        return billComponents;
    }
    private List<BillFee> billFees;
    private List<BillFee> tblBillFees;

    public List<BillFee> getBillFees() {
        if (getCurrent() != null) {
            if (billFees == null) {
                String sql = "SELECT b FROM BillFee b WHERE b.retired=false and b.bill.id=" + getCurrent().getId();
                billFees = getBillFeeFacade().findBySQL(sql);
                if (billFees == null) {
                    billFees = new ArrayList<BillFee>();
                }
            }
        }

        return billFees;
    }

    public String newPayment() {
        recreateModel();
        return "payment_staff_bill";
    }

    private void recreateModel() {

        billFees = null;
        billItems = null;
        printPreview = false;
        billItems = null;
        selectedItems = null;
        items = null;
        dueBillFeeReport = null;
        dueBillFees = null;
        payingBillFees = null;
        billFees = null;
        /////////////////////    
        fromDate = null;
        toDate = null;
        current = null;
        selectText = "";
        currentStaff = null;
        totalDue = 0.0;
        totalPaying = 0.0;
        printPreview = false;
        paymentScheme = null;
        speciality = null;

    }

    public StaffFacade getStaffFacade() {
        return staffFacade;
    }

    public void setStaffFacade(StaffFacade staffFacade) {
        this.staffFacade = staffFacade;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
        currentStaff = null;
        dueBillFees = new ArrayList<BillFee>();
        payingBillFees = new ArrayList<BillFee>();
        totalPaying = 0.0;
        totalDue = 0.0;

    }

    public String prepareNewPayment() {
        currentStaff = null;
        dueBillFees = new ArrayList<>();
        payingBillFees = new ArrayList<>();
        totalPaying = 0.0;
        totalDue = 0.0;
        printPreview = false;
        return "payment_staff_bill";
    }

    public List<Staff> completeStaff(String query) {
        List<Staff> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            if (speciality != null) {
                sql = "select p from Staff p where p.retired=false and (upper(p.person.name) like '%" + query.toUpperCase() + "%'or  upper(p.code) like '%" + query.toUpperCase() + "%' ) and p.speciality.id = " + getSpeciality().getId() + " order by p.person.name";
            } else {
                sql = "select p from Staff p where p.retired=false and (upper(p.person.name) like '%" + query.toUpperCase() + "%'or  upper(p.code) like '%" + query.toUpperCase() + "%' ) order by p.person.name";
            }
            //////System.out.println(sql);
            suggestions = getStaffFacade().findBySQL(sql);
        }
        return suggestions;
    }

    public PaymentScheme getPaymentScheme() {
        return paymentScheme;
    }

    public void setPaymentScheme(PaymentScheme paymentScheme) {
        this.paymentScheme = paymentScheme;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public double getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(double totalDue) {
        this.totalDue = totalDue;
    }

    public double getTotalPaying() {
        return totalPaying;
    }

    public void setTotalPaying(double totalPaying) {
        this.totalPaying = totalPaying;
    }

    public void calculateDueFees() {
        if (currentStaff == null || currentStaff.getId() == null) {
            dueBillFees = new ArrayList<BillFee>();
        } else {
            String sql;
            HashMap h = new HashMap();
            sql = "select b from BillFee b where b.retired=false and (b.bill.billType!=:btp and b.bill.billType!=:btp2) and b.bill.cancelled=false and b.bill.refunded=false and (b.feeValue - b.paidValue) > 0 and b.staff.id = " + currentStaff.getId();
            h.put("btp", BillType.ChannelPaid);
            h.put("btp2", BillType.ChannelCredit);

            dueBillFees = getBillFeeFacade().findBySQL(sql, h, TemporalType.TIMESTAMP);

        }
    }

    public void calculateTotalDue() {
        totalDue = 0;
        for (BillFee f : dueBillFees) {
            totalDue = totalDue + f.getFeeValue() - f.getPaidValue();
        }
    }

    public void performCalculations() {
        calculateTotalDue();
        calculateTotalPay();
    }

    public void calculateTotalPay() {
        totalPaying = 0;

        for (BillFee f : payingBillFees) {
            //////System.out.println("totalPaying before " + totalPaying);
            //////System.out.println("fee val is " + f.getFeeValue());
            //////System.out.println("paid val is " + f.getPaidValue());
            totalPaying = totalPaying + (f.getFeeValue() - f.getPaidValue());
            //////System.out.println("totalPaying after " + totalPaying);
        }
        //////System.out.println("total pay is " + totalPaying);
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public List<BillFee> getDueBillFees() {
        return dueBillFees;
    }

    public void setDueBillFees(List<BillFee> dueBillFees) {
        this.dueBillFees = dueBillFees;
    }

    public List<BillFee> getPayingBillFees() {
        return payingBillFees;
    }

    public void setPayingBillFees(List<BillFee> payingBillFees) {
        //////System.out.println("setting paying bill fees " + payingBillFees.size());
        this.payingBillFees = payingBillFees;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Staff getCurrentStaff() {
        return currentStaff;

    }

    public void setCurrentStaff(Staff currentStaff) {

        this.currentStaff = currentStaff;

        dueBillFees = new ArrayList<BillFee>();
        payingBillFees = new ArrayList<BillFee>();
        totalPaying = 0.0;
        totalDue = 0.0;
        printPreview = false;

        calculateDueFees();
        performCalculations();

    }

    public List<Bill> getSelectedItems() {
        selectedItems = getFacade().findBySQL("select c from Bill c where c.retired=false and upper(c.name) like '%" + getSelectText().toUpperCase() + "%' order by c.name");
        return selectedItems;
    }

    public void prepareAdd() {
        current = new BilledBill();
    }

    public void setSelectedItems(List<Bill> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    private Bill createPaymentBill() {
        BilledBill tmp = new BilledBill();
        tmp.setBillDate(Calendar.getInstance().getTime());
        tmp.setBillTime(Calendar.getInstance().getTime());
        tmp.setBillType(BillType.PaymentBill);
        tmp.setCreatedAt(Calendar.getInstance().getTime());
        tmp.setCreater(getSessionController().getLoggedUser());
        tmp.setDepartment(getSessionController().getLoggedUser().getDepartment());

        tmp.setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), BillType.PaymentBill, BillNumberSuffix.PROPAY));
        tmp.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), tmp, BillType.PaymentBill, BillNumberSuffix.PROPAY));

        tmp.setDiscount(0.0);
        tmp.setDiscountPercent(0.0);

        tmp.setInstitution(getSessionController().getLoggedUser().getInstitution());
        tmp.setNetTotal(0 - totalPaying);
        tmp.setPaymentScheme(paymentScheme);
        tmp.setStaff(currentStaff);
        tmp.setToStaff(currentStaff);
        tmp.setTotal(0 - totalPaying);

        return tmp;
    }

    private boolean errorCheck() {
        if (currentStaff == null) {
            UtilityController.addErrorMessage("Please select a Staff Memeber");
            return true;
        }
        performCalculations();
        if (totalPaying == 0) {
            UtilityController.addErrorMessage("Please select payments to update");
            return true;
        }
        if (paymentScheme == null) {
            UtilityController.addErrorMessage("Please select a payment method");
            return true;
        }

        return false;
    }

    @EJB
    CashTransactionBean cashTransactionBean;

    public CashTransactionBean getCashTransactionBean() {
        return cashTransactionBean;
    }

    public void setCashTransactionBean(CashTransactionBean cashTransactionBean) {
        this.cashTransactionBean = cashTransactionBean;
    }

    public void settleBill() {
        if (errorCheck()) {
            return;
        }
        calculateTotalPay();
        Bill b = createPaymentBill();
        current = b;
        getBillFacade().create(b);
        saveBillCompo(b);
        printPreview = true;

        WebUser wb = getCashTransactionBean().saveBillCashOutTransaction(b, getSessionController().getLoggedUser());
        getSessionController().setLoggedUser(wb);
        UtilityController.addSuccessMessage("Successfully Paid");
        //////System.out.println("Paid");
    }

    private void saveBillCompo(Bill b) {
        for (BillFee bf : getPayingBillFees()) {
            saveBillItemForPaymentBill(b, bf);
//            saveBillFeeForPaymentBill(b,bf); No need to add fees for this bill
            bf.setPaidValue(bf.getFeeValue());
            getBillFeeFacade().edit(bf);
            //////System.out.println("marking as paid");
        }
    }

    private void saveBillItemForPaymentBill(Bill b, BillFee bf) {
        BillItem i = new BillItem();
        i.setReferanceBillItem(bf.getBillItem());
        i.setReferenceBill(bf.getBill());
        i.setPaidForBillFee(bf);
        i.setBill(b);
        i.setCreatedAt(Calendar.getInstance().getTime());
        i.setCreater(getSessionController().getLoggedUser());
        i.setDiscount(0.0);
        i.setGrossValue(bf.getFeeValue());
//        if (bf.getBillItem() != null && bf.getBillItem().getItem() != null) {
//            i.setItem(bf.getBillItem().getItem());
//        }
        i.setNetValue(bf.getFeeValue());
        i.setQty(1.0);
        i.setRate(bf.getFeeValue());
        getBillItemFacade().create(i);
        b.getBillItems().add(i);
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
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

    public StaffPaymentBillController() {
    }

    public Bill getCurrent() {
        if (current == null) {
            current = new BilledBill();
        }
        return current;
    }

    public void setCurrent(Bill current) {
        currentStaff = null;
        dueBillFees = new ArrayList<BillFee>();
        payingBillFees = new ArrayList<BillFee>();
        totalPaying = 0.0;
        totalDue = 0.0;
        recreateModel();
        this.current = current;
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("DeleteSuccessfull");
        } else {
            UtilityController.addSuccessMessage("NothingToDelete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    private BillFacade getFacade() {
        return billFacade;
    }

    public List<Bill> getItems() {
        items = getFacade().findAll("name", true);
        return items;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public Boolean getPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(Boolean printPreview) {
        this.printPreview = printPreview;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
        //  resetLists();
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
        //  resetLists();
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public List<BillFee> getDueBillFeeReport() {
        String sql;
        Map temMap = new HashMap();
        sql = "select b from BillFee b where b.retired=false and b.bill.cancelled=false and (b.feeValue - b.paidValue) > 0 and b.bill.institution.id=" + getSessionController().getInstitution().getId() + " and b.bill.billDate between :fromDate and :toDate order by b.staff.id  ";
        //////System.out.println("sql is " + sql);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        dueBillFeeReport = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        //////System.out.println(dueBillFeeReport.size());

        if (dueBillFeeReport == null) {
            dueBillFeeReport = new ArrayList<BillFee>();
        }

        return dueBillFeeReport;
    }

    private LazyDataModel<BillFee> dueBillFee;

    public List<BillFee> getDueBillFeeReportAll() {

        String sql;
        Map temMap = new HashMap();
        if (!getSelectText().equals("")) {
            sql = "select b from BillFee b where b.retired=false and (b.bill.billType!=:btp and b.bill.billType!=:btp2) and b.bill.cancelled=false and (b.feeValue - b.paidValue) > 0 and  b.bill.billDate between :fromDate and :toDate and upper(b.staff.person.name) like '%" + selectText.toUpperCase() + "%' order by b.staff.id  ";
        } else {
            sql = "select b from BillFee b where b.retired=false and (b.bill.billType!=:btp and b.bill.billType!=:btp2) and b.bill.cancelled=false and (b.feeValue - b.paidValue) > 0 and  b.bill.billDate between :fromDate and :toDate order by b.staff.id  ";
        }
        //////System.out.println("sql is " + sql);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.ChannelPaid);
        temMap.put("btp2", BillType.ChannelCredit);

        dueBillFeeReport = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        //////System.out.println(dueBillFeeReport.size());

        if (dueBillFeeReport == null) {
            dueBillFeeReport = new ArrayList<BillFee>();
        }

        return dueBillFeeReport;
    }

    public void setDueBillFeeReport(List<BillFee> dueBillFeeReport) {
        this.dueBillFeeReport = dueBillFeeReport;
    }

    public List<BillItem> getBillItems() {
        if (getCurrent() != null) {
            String sql = "SELECT b FROM BillItem b WHERE b.retired=false and b.bill.id=" + getCurrent().getId();
            billItems = getBillItemFacade().findBySQL(sql);
            if (billItems == null) {
                billItems = new ArrayList<BillItem>();
            }
        }

        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public CancelledBillFacade getCancelledBillFacade() {
        return cancelledBillFacade;
    }

    public void setCancelledBillFacade(CancelledBillFacade cancelledBillFacade) {
        this.cancelledBillFacade = cancelledBillFacade;
    }

    public BillComponentFacade getBillComponentFacade() {
        return billComponentFacade;
    }

    public void setBillComponentFacade(BillComponentFacade billComponentFacade) {
        this.billComponentFacade = billComponentFacade;
    }

    public RefundBillFacade getRefundBillFacade() {
        return refundBillFacade;
    }

    public void setRefundBillFacade(RefundBillFacade refundBillFacade) {
        this.refundBillFacade = refundBillFacade;
    }

    public void setBillComponents(List<BillComponent> billComponents) {
        this.billComponents = billComponents;
    }

    public void setBillFees(List<BillFee> billFees) {
        this.billFees = billFees;
    }

    public void setItems(List<Bill> items) {
        this.items = items;
    }

    public LazyDataModel<BillFee> getDueBillFee() {
        return dueBillFee;
    }

    public void setDueBillFee(LazyDataModel<BillFee> dueBillFee) {
        this.dueBillFee = dueBillFee;
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

    public List<BillFee> getTblBillFees() {
        return tblBillFees;
    }

    public void setTblBillFees(List<BillFee> tblBillFees) {
        this.tblBillFees = tblBillFees;
    }

    /**
     *
     */
    @FacesConverter(forClass = BilledBill.class)
    public static class StaffPaymentBillControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            StaffPaymentBillController controller = (StaffPaymentBillController) facesContext.getApplication().getELResolver().
                    //getValue(facesContext.getELContext(), null, "billController");
                    getValue(facesContext.getELContext(), null, "staffPaymentBillController");
            return controller.billFacade.find(getKey(value));
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
            if (object instanceof BilledBill) {
                BilledBill o = (BilledBill) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + StaffPaymentBillController.class.getName());
            }
        }
    }
}
