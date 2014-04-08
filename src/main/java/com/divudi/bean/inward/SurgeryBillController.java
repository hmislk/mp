/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.inward;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.dataStructure.DepartmentBillItems;
import com.divudi.data.inward.SurgeryBillType;
import com.divudi.ejb.BillBean;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.InwardBean;
import com.divudi.ejb.InwardCalculation;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Item;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.PatientItem;
import com.divudi.entity.PreBill;
import com.divudi.entity.RefundBill;
import com.divudi.entity.inward.EncounterComponent;
import com.divudi.entity.inward.TimedItem;
import com.divudi.entity.inward.TimedItemFee;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.EncounterComponentFacade;
import com.divudi.facade.PatientEncounterFacade;
import com.divudi.facade.PatientItemFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class SurgeryBillController implements Serializable {

    private Bill batchBill;
    private Bill professionalBill;
    private Bill timedServiceBill;
    private EncounterComponent proEncounterComponent;
    private EncounterComponent timedEncounterComponent;
    //////////
    private List<EncounterComponent> proEncounterComponents;
    private List<EncounterComponent> timedEncounterComponents;
    private List<DepartmentBillItems> departmentBillItems;
    //////
    @EJB
    private PatientEncounterFacade patientEncounterFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private EncounterComponentFacade encounterComponentFacade;
    @EJB
    private PatientItemFacade patientItemFacade;
    @EJB
    private BillNumberBean billNumberBean;
    @EJB
    private InwardCalculation inwardCalculation;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private PharmacyBean pharmacyBean;
    //////
    @Inject
    private SessionController sessionController;
    @Inject
    InwardTimedItemController inwardTimedItemController;

    public InwardTimedItemController getInwardTimedItemController() {
        return inwardTimedItemController;
    }

    public void setInwardTimedItemController(InwardTimedItemController inwardTimedItemController) {
        this.inwardTimedItemController = inwardTimedItemController;
    }

    public void updateProFee(BillFee bf) {
        updateBillFee(bf);
    }

    public void updateTimedService(BillFee bf) {
        if (generalChecking()) {
            return;
        }

        double value = savePatientItem(bf.getPatientItem());
        bf.setFeeValue(value);
        bf.setFeeGrossValue(value);
        updateBillFee(bf);
    }

    public void updateService(BillFee bf) {
        if (generalChecking()) {
            return;
        }

        updateBillFee(bf);
    }

    @EJB
    private BillBean billBean;

    private void updateBillFee(BillFee bf) {
        getBillFeeFacade().edit(bf);
        updateBillItem(bf.getBillItem());
        updateBill(bf.getBill());
        getBillBean().updateBatchBill(getBatchBill());
    }

    public void removeProEncFromList(EncounterComponent encounterComponent) {
        removeEncounterComponentFromList(encounterComponent, getProEncounterComponents());
    }

    public void removeProEncFromDbase(EncounterComponent encounterComponent) {
        if (generalChecking()) {
            return;
        }

        if (encounterComponent.getBillFee().getPaidValue() != 0) {
            UtilityController.addErrorMessage("Staff Payment Already Paid U cant Remove");
            return;
        }

        retiredEncounterComponent(encounterComponent);
        retiredBillFee(encounterComponent.getBillFee());

        updateBillItem(encounterComponent.getBillItem());
        updateBill(encounterComponent.getBillItem().getBill());
        getBillBean().updateBatchBill(getBatchBill());

    }

    private void retiredEncounterComponent(EncounterComponent encounterComponent) {
        encounterComponent.setRetired(true);
        encounterComponent.setRetiredAt(new Date());
        encounterComponent.setRetirer(getSessionController().getLoggedUser());
        getEncounterComponentFacade().edit(encounterComponent);
    }

    private void retiredBillFee(BillFee removingFee) {

        if (removingFee != null) {
            removingFee.setRetired(true);
            removingFee.setRetiredAt(new Date());
            removingFee.setRetirer(getSessionController().getLoggedUser());
            getBillFeeFacade().edit(removingFee);

            PatientItem patientItem = removingFee.getPatientItem();
            if (patientItem != null) {
                patientItem.setRetirer(getSessionController().getLoggedUser());
                patientItem.setRetiredAt(new Date());
                patientItem.setRetired(true);
                getPatientItemFacade().edit(patientItem);
            }

        }

    }

    private void retiredBillItem(BillItem billItem) {

        if (billItem != null) {
            billItem.setRetired(true);
            billItem.setRetiredAt(new Date());
            billItem.setRetirer(getSessionController().getLoggedUser());
            getBillItemFacade().edit(billItem);
        }

    }

    public void removeTimedEncFromList(EncounterComponent encounterComponent) {
        removeEncounterComponentFromList(encounterComponent, getTimedEncounterComponents());
    }

    public void removeTimedEncFromDbase(EncounterComponent encounterComponent) {
        if (generalChecking()) {
            return;
        }

        retiredEncounterComponent(encounterComponent);
        retiredBillFee(encounterComponent.getBillFee());

        updateBillItem(encounterComponent.getBillItem());
        updateBill(encounterComponent.getBillItem().getBill());
        getBillBean().updateBatchBill(getBatchBill());
    }

    public void removeServiceEncFromDbase(EncounterComponent encounterComponent) {
        if (generalChecking()) {
            return;
        }

        retiredEncounterComponent(encounterComponent);
        retiredBillItem(encounterComponent.getBillItem());

        for (BillFee bf : getBillFees(encounterComponent.getBillItem())) {
            retiredBillFee(bf);
        }

        updateBillItem(encounterComponent.getBillItem());
        updateBill(encounterComponent.getBillItem().getBill());
        getBillBean().updateBatchBill(getBatchBill());

    }

    public List<BillFee> getBillFees(BillItem billItem) {
        String sql = "Select bf from BillFee bf where retired=false and "
                + " bf.billItem=:bItm ";
        HashMap hm = new HashMap();
        hm.put("bItm", billItem);
        List<BillFee> lst = getBillFeeFacade().findBySQL(sql, hm);

        if (lst.isEmpty()) {
            return new ArrayList<>();
        }

        return lst;
    }

//    public void removePharmacyIssueEncFromDbase(EncounterComponent encounterComponent) {
//        if (generalChecking()) {
//            return;
//        }
//
//        retiredEncounterComponent(encounterComponent);
//        retiredBillItem(encounterComponent.getBillItem());
//
//        for (BillFee bf : getBillFees(encounterComponent.getBillItem())) {
//            retiredBillFee(bf);
//        }
//
//        updateBillItem(encounterComponent.getBillItem());
//        updateBill(encounterComponent.getBillItem().getBill());
//        updateBatchBill();
//
//    }
    private void removeEncounterComponentFromList(EncounterComponent encounterComponent, List<EncounterComponent> list) {
        list.remove(encounterComponent.getOrderNo());

        int index = 0;
        for (EncounterComponent ec : list) {
            ec.setOrderNo(index++);
        }

    }

    public void makeNull() {
        batchBill = null;
        professionalBill = null;
        proEncounterComponent = null;
        proEncounterComponents = null;
        /////////////
        timedServiceBill = null;
        timedEncounterComponent = null;
        timedEncounterComponents = null;
        departmentBillItems = null;

    }

    public void saveProcedure() {

        PatientEncounter procedure = getBatchBill().getProcedure();

        if (procedure.getId() == null || procedure.getId() == 0) {
            procedure.setParentEncounter(getBatchBill().getPatientEncounter());
            procedure.setCreatedAt(new Date());
            procedure.setCreater(getSessionController().getLoggedUser());

            getPatientEncounterFacade().create(procedure);
        } else {
            getPatientEncounterFacade().edit(procedure);
        }

    }

    private void saveBatchBill() {
        if (getBatchBill().getId() == null) {
            getBatchBill().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), BillType.SurgeryBill, BillNumberSuffix.SURG));
            getBatchBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getLoggedUser().getInstitution(), getBatchBill(), BillType.SurgeryBill, BillNumberSuffix.SURG));

            getBatchBill().setInstitution(getSessionController().getInstitution());
            getBatchBill().setDepartment(getSessionController().getDepartment());
            getBatchBill().setCreatedAt(Calendar.getInstance().getTime());
            getBatchBill().setCreater(getSessionController().getLoggedUser());

            getBillFacade().create(getBatchBill());
        } else {
            getBillFacade().edit(getBatchBill());
        }
    }

    private void saveBill(Bill bill, BillNumberSuffix billNumberSuffix) {
        if (bill.getId() == null) {
            bill.setForwardReferenceBill(getBatchBill());
            bill.setCreatedAt(Calendar.getInstance().getTime());
            bill.setCreater(getSessionController().getLoggedUser());
            bill.setPatientEncounter(getBatchBill().getPatientEncounter());
            bill.setProcedure(getBatchBill().getProcedure());
            bill.setDepartment(getSessionController().getDepartment());
            bill.setInstitution(getSessionController().getInstitution());

            bill.setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), bill.getBillType(), billNumberSuffix));
            bill.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getLoggedUser().getInstitution(),
                    bill, bill.getBillType(), billNumberSuffix));

            getBillFacade().create(bill);
        } else {
            getBillFacade().edit(bill);
        }
    }

    private void saveBillItem(BillItem billItem, Bill bill) {
        if (billItem.getId() == null) {
            billItem.setBill(bill);
            billItem.setCreatedAt(new Date());
            billItem.setCreater(getSessionController().getLoggedUser());

            getBillItemFacade().create(billItem);
        } else {
            getBillItemFacade().edit(billItem);
        }

    }

    @EJB
    private InwardBean inwardBean;

    private void saveBillFee(BillFee bf, Bill bill, BillItem bIllItem, double value) {
        if (bf.getId() == null) {
            bf.setBill(bill);
            bf.setFee(getInwardBean().getStaffFeeForInward(getSessionController().getLoggedUser()));
            bf.setBillItem(bIllItem);
            bf.setCreatedAt(Calendar.getInstance().getTime());
            bf.setCreater(getSessionController().getLoggedUser());
            bf.setFeeAt(Calendar.getInstance().getTime());
            bf.setFeeValue(value);
            bf.setFeeGrossValue(value);
            bf.setDepartment(getSessionController().getDepartment());
            bf.setPatienEncounter(getBatchBill().getProcedure());
            bf.setPatient(getBatchBill().getPatientEncounter().getPatient());
            bf.setInstitution(getSessionController().getInstitution());

            getBillFeeFacade().create(bf);
        } else {
            getBillFeeFacade().edit(bf);
        }
    }

    private double savePatientItem(PatientItem patientItem) {
        TimedItemFee timedItemFee = getInwardCalculation().getTimedItemFee((TimedItem) patientItem.getItem());
        double count = getInwardCalculation().calCount(timedItemFee, patientItem.getFromTime(), patientItem.getToTime());

        patientItem.setServiceValue(count * timedItemFee.getFee());
        patientItem.setPatientEncounter(getBatchBill().getPatientEncounter());
        if (patientItem.getId() == null) {
            patientItem.setCreater(getSessionController().getLoggedUser());
            patientItem.setCreatedAt(Calendar.getInstance().getTime());
            getPatientItemFacade().create(patientItem);

        } else {
            getPatientItemFacade().edit(patientItem);
        }

        return patientItem.getServiceValue();
    }

    private void saveEncounterComponent(BillItem billItem, EncounterComponent ec) {
        if (ec.getId() == null) {
            ec.setBillItem(billItem);
            ec.setCreatedAt(Calendar.getInstance().getTime());
            ec.setCreater(getSessionController().getLoggedUser());
            ec.setPatientEncounter(getBatchBill().getProcedure());
            if (ec.getBillFee() != null) {
                ec.setStaff(ec.getBillFee().getStaff());
            }
            getEncounterComponentFacade().create(ec);
        } else {
            getEncounterComponentFacade().edit(ec);
        }
    }

    public BillItem getFirstBillItem(Bill b) {
        String sql = "Select b From BillItem b where "
                + " b.retired=false and b.bill=:bill";
        HashMap hm = new HashMap();
        hm.put("bill", b);

        return getBillItemFacade().findFirstBySQL(sql, hm);
    }

    public List<BillItem> getBillItems(Bill b) {
        String sql = "Select b From BillItem b where "
                + " b.retired=false and b.bill=:bill";
        HashMap hm = new HashMap();
        hm.put("bill", b);

        return getBillItemFacade().findBySQL(sql, hm);
    }

    private boolean saveProfessionalBill() {
        BillItem bItem;
        if (getProfessionalBill().getId() == null) {
            saveBill(getProfessionalBill(), BillNumberSuffix.INWPRO);
            bItem = new BillItem();
            saveBillItem(bItem, getProfessionalBill());
        } else {
            getBillFacade().edit(getProfessionalBill());
            bItem = getFirstBillItem(getProfessionalBill());
        }

        for (EncounterComponent ec : getProEncounterComponents()) {
            saveBillFee(ec.getBillFee(), getProfessionalBill(), bItem, ec.getBillFee().getFeeValue());
            saveEncounterComponent(bItem, ec);
        }

        updateBillItem(bItem);
        updateBill(getProfessionalBill());

        return false;
    }

    private boolean saveTimeServiceBill() {
        BillItem bItem;
        double netValue = 0;
        if (getTimedServiceBill().getId() == null) {
            saveBill(getTimedServiceBill(), BillNumberSuffix.TIME);
            bItem = new BillItem();
            saveBillItem(bItem, getTimedServiceBill());
        } else {
            getBillFacade().edit(getTimedServiceBill());
            bItem = getFirstBillItem(getTimedServiceBill());
        }

        for (EncounterComponent ec : getTimedEncounterComponents()) {
            netValue = savePatientItem(ec.getBillFee().getPatientItem());

            saveBillFee(ec.getBillFee(), getTimedServiceBill(), bItem, netValue);
            saveEncounterComponent(bItem, ec);
        }

        updateBillItem(bItem);
        updateBill(getTimedServiceBill());

        return false;
    }

    private double getTotalByBillFee(BillItem billItem) {
        String sql = "Select sum(bf.feeValue) from BillFee bf where "
                + " bf.retired=false and bf.billItem=:bItm";
        HashMap hm = new HashMap();
        hm.put("bItm", billItem);
        return getBillFeeFacade().findDoubleByJpql(sql, hm);
    }

    private double getTotalByBillItem(Bill bill) {
        String sql = "Select sum(bf.netValue) from BillItem bf where "
                + " bf.retired=false and bf.bill=:bill";
        HashMap hm = new HashMap();
        hm.put("bill", bill);
        return getBillItemFacade().findDoubleByJpql(sql, hm);
    }

    private void updateBillItem(BillItem billItem) {
        double value = getTotalByBillFee(billItem);
        billItem.setNetValue(value);
        getBillItemFacade().edit(billItem);
    }

    private void updateBill(Bill bill) {
        double value = getTotalByBillItem(bill);
        bill.setTotal(value);
        bill.setNetTotal(value);
        getBillFacade().edit(bill);
    }

    public void save() {
        if (generalChecking()) {
            return;
        }

        saveProcedure();
        saveBatchBill();

        if (!getProEncounterComponents().isEmpty()) {
            saveProfessionalBill();
        }

        if (!getTimedEncounterComponents().isEmpty()) {
            saveTimeServiceBill();
        }

        getBillBean().updateBatchBill(getBatchBill());

        UtilityController.addSuccessMessage("Surgery Detail Successfull Updated");

        makeNull();
    }

    /**
     * Creates a new instance of SurgeryBill
     */
    public SurgeryBillController() {
    }

    public Bill getBatchBill() {
        if (batchBill == null) {
            batchBill = new BilledBill();
            batchBill.setBillType(BillType.SurgeryBill);
            batchBill.setProcedure(new PatientEncounter());
        }
        return batchBill;
    }

    private List<EncounterComponent> getEncounterComponents(Bill bi) {
        String sql = "Select enc from EncounterComponent enc "
                + " where enc.billItem.bill=:bill";
        HashMap hm = new HashMap();
        hm.put("bill", bi);

        return getEncounterComponentFacade().findBySQL(sql, hm);

    }

    private List<Bill> getBillsByForwardRef(Bill b) {
        String sql = "Select bf from Bill bf where bf.cancelled=false and "
                + " bf.retired=false and bf.forwardReferenceBill=:bill";
        HashMap hm = new HashMap();
        hm.put("bill", getBatchBill());
        List<Bill> list = getBillFacade().findBySQL(sql, hm);

        if (list == null) {
            return new ArrayList<>();
        }

        return list;
    }

    public void setBatchBill(Bill batchBill) {
        makeNull();
        this.batchBill = batchBill;
        for (Bill b : getBillsByForwardRef(batchBill)) {
            if (b.getSurgeryBillType() == SurgeryBillType.ProfessionalFee) {
                // System.err.println(SurgeryBillType.ProfessionalFee);
                setProfessionalBill(b);
                List<EncounterComponent> enc = getEncounterComponents(b);
                setProEncounterComponents(enc);
            }

            if (b.getSurgeryBillType() == SurgeryBillType.TimedService) {
                // System.err.println(SurgeryBillType.TimedService);
                setTimedServiceBill(b);
                List<EncounterComponent> enc = getEncounterComponents(b);
                setTimedEncounterComponents(enc);
            }

            if (b.getSurgeryBillType() == SurgeryBillType.Service) {
                departmentBillItems = getInwardBean().createDepartmentBillItems(batchBill.getPatientEncounter(), getBatchBill());
            }

            if (b.getSurgeryBillType() == SurgeryBillType.PharmacyItem) {
                createIssueTable();
            }

        }
    }

    private List<Bill> pharmacyIssues;

    public void createIssueTable() {
        pharmacyIssues = new ArrayList<>();
        String sql;
        HashMap hm;
        sql = "SELECT  b FROM Bill b "
                + " WHERE b.retired=false "
                + " and b.forwardReferenceBill=:bil "
                + " and b.billType=:btp "
                + " and b.billedBill is null "
                + " and (type(b)=:class) ";
        hm = new HashMap();
        hm.put("bil", getBatchBill());
        hm.put("btp", BillType.PharmacyBhtPre);
        hm.put("class", PreBill.class);

        List<Bill> bills = getBillFacade().findBySQL(sql, hm);

        hm.clear();
        sql = "SELECT  b FROM Bill b "
                + " WHERE b.retired=false "
                + " and b.billType=:btp"
                + " and b.forwardReferenceBill=:bil "
                + " and type(b.billedBill)=:billedClass "
                + " and  b.patientEncounter=:pe"
                + " and (type(b)=:class) ";
        hm = new HashMap();
        hm.put("btp", BillType.PharmacyBhtPre);
        hm.put("class", RefundBill.class);
        hm.put("billedClass", PreBill.class);
        hm.put("bil", getBatchBill());

        List<Bill> bills2 = getBillFacade().findBySQL(sql, hm);

        pharmacyIssues.addAll(bills);
        pharmacyIssues.addAll(bills2);

    }

    public Bill getProfessionalBill() {
        if (professionalBill == null) {
            professionalBill = new BilledBill();
            professionalBill.setBillType(BillType.InwardProfessional);
            professionalBill.setSurgeryBillType(SurgeryBillType.ProfessionalFee);
        }

        return professionalBill;
    }

    public void setProfessionalBill(Bill professionalBill) {

        this.professionalBill = professionalBill;
    }

    public Bill getTimedServiceBill() {
        if (timedServiceBill == null) {
            timedServiceBill = new BilledBill();
            timedServiceBill.setBillType(BillType.InwardBill);
            timedServiceBill.setSurgeryBillType(SurgeryBillType.TimedService);
        }
        return timedServiceBill;
    }

    public void setTimedServiceBill(Bill timedServiceBill) {
        this.timedServiceBill = timedServiceBill;
    }

    public EncounterComponent getProEncounterComponent() {
        if (proEncounterComponent == null) {
            proEncounterComponent = new EncounterComponent();
            proEncounterComponent.setBillFee(new BillFee());
        }
        return proEncounterComponent;
    }

    public void setProEncounterComponent(EncounterComponent proEncounterComponent) {
        this.proEncounterComponent = proEncounterComponent;
    }

    private boolean generalChecking() {
        if (getBatchBill().getPatientEncounter() == null) {
            UtilityController.addErrorMessage("Admission ?");
            return true;
        }
        if (getBatchBill().getProcedure().getItem() == null) {
            UtilityController.addErrorMessage("Select Surgery");
            return true;
        }

        if (getBatchBill().getPatientEncounter().isPaymentFinalized()) {
            UtilityController.addErrorMessage("Final Payment is Finalized");
            return true;
        }

        return false;

    }

    public void addProfessionalFee() {
        if (generalChecking()) {
            return;
        }

        if (getProEncounterComponent().getBillFee().getStaff() == null) {
            UtilityController.addErrorMessage("Select Staff ");
            return;
        }

        proEncounterComponent.setPatientEncounter(getBatchBill().getPatientEncounter());
        proEncounterComponent.setChildEncounter(getBatchBill().getProcedure());
        proEncounterComponent.setOrderNo(getProEncounterComponents().size());
        getProEncounterComponents().add(proEncounterComponent);

        proEncounterComponent = null;
    }

    public void addTimedService() {
        if (generalChecking()) {
            return;
        }

        if (getTimedEncounterComponent().getBillFee().getPatientItem().getItem() == null) {
            UtilityController.addErrorMessage("Select Timed Service ");
            return;
        }

        timedEncounterComponent.setPatientEncounter(getBatchBill().getPatientEncounter());
        timedEncounterComponent.setChildEncounter(getBatchBill().getProcedure());
        timedEncounterComponent.setOrderNo(getProEncounterComponents().size());
        getTimedEncounterComponents().add(timedEncounterComponent);

        timedEncounterComponent = null;
    }

    public List<EncounterComponent> getProEncounterComponents() {
        if (proEncounterComponents == null) {
            proEncounterComponents = new ArrayList<>();
        }
        return proEncounterComponents;
    }

    public void setProEncounterComponents(List<EncounterComponent> proEncounterComponents) {
        this.proEncounterComponents = proEncounterComponents;
    }

    public PatientEncounterFacade getPatientEncounterFacade() {
        return patientEncounterFacade;
    }

    public void setPatientEncounterFacade(PatientEncounterFacade patientEncounterFacade) {
        this.patientEncounterFacade = patientEncounterFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public EncounterComponentFacade getEncounterComponentFacade() {
        return encounterComponentFacade;
    }

    public void setEncounterComponentFacade(EncounterComponentFacade encounterComponentFacade) {
        this.encounterComponentFacade = encounterComponentFacade;
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public EncounterComponent getTimedEncounterComponent() {
        if (timedEncounterComponent == null) {
            timedEncounterComponent = new EncounterComponent();
            BillFee bf = new BillFee();
            PatientItem pi = new PatientItem();
            pi.setItem(new Item());
            bf.setPatientItem(pi);
            timedEncounterComponent.setBillFee(bf);
        }
        return timedEncounterComponent;
    }

    public void setTimedEncounterComponent(EncounterComponent timedEncounterComponent) {
        this.timedEncounterComponent = timedEncounterComponent;
    }

    public List<EncounterComponent> getTimedEncounterComponents() {
        if (timedEncounterComponents == null) {
            timedEncounterComponents = new ArrayList<>();
        }
        return timedEncounterComponents;
    }

    public void setTimedEncounterComponents(List<EncounterComponent> timedEncounterComponents) {
        this.timedEncounterComponents = timedEncounterComponents;
    }

    public PatientItemFacade getPatientItemFacade() {
        return patientItemFacade;
    }

    public void setPatientItemFacade(PatientItemFacade patientItemFacade) {
        this.patientItemFacade = patientItemFacade;
    }

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;
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

    public InwardBean getInwardBean() {
        return inwardBean;
    }

    public void setInwardBean(InwardBean inwardBean) {
        this.inwardBean = inwardBean;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public List<DepartmentBillItems> getDepartmentBillItems() {
        if (departmentBillItems == null) {
            departmentBillItems = getInwardBean().createDepartmentBillItems(getBatchBill().getPatientEncounter(), getBatchBill());
        }
        return departmentBillItems;
    }

    public void setDepartmentBillItems(List<DepartmentBillItems> departmentBillItems) {
        this.departmentBillItems = departmentBillItems;
    }

    public List<Bill> getPharmacyIssues() {
        return pharmacyIssues;
    }

    public void setPharmacyIssues(List<Bill> pharmacyIssues) {
        this.pharmacyIssues = pharmacyIssues;
    }

}
