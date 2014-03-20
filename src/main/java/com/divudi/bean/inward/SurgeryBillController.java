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
import com.divudi.data.inward.SurgeryBillType;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.InwardCalculation;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Item;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.PatientItem;
import com.divudi.entity.inward.EncounterComponent;
import com.divudi.facade.BatchBillFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.EncounterComponentFacade;
import com.divudi.facade.PatientEncounterFacade;
import com.divudi.facade.PatientItemFacade;
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
    private Bill serviceBill;
    private Bill timedServiceBill;
    private EncounterComponent proEncounterComponent;
    private EncounterComponent timedEncounterComponent;
    //////////
    private List<EncounterComponent> proEncounterComponents;
    private List<EncounterComponent> timedEncounterComponents;
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
    //////
    @Inject
    private SessionController sessionController;

    public void updateBillFee(BillFee bf) {
        getBillFeeFacade().edit(bf);
        updateBillItem(bf.getBill(), bf.getBillItem());
        updateBill(bf.getBill());
        updateBatchBill();
    }

    public void makeNull() {
        batchBill = null;
        professionalBill = null;
        serviceBill = null;
        timedServiceBill = null;
        proEncounterComponent = null;
        proEncounterComponents = null;
        timedEncounterComponent = null;
        timedEncounterComponents = null;
    }

    public boolean saveProcedure() {
        if (generalChecking()) {
            return true;
        }

        PatientEncounter procedure = getBatchBill().getProcedure();

        procedure.setParentEncounter(getBatchBill().getPatientEncounter());
        procedure.setCreatedAt(new Date());
        procedure.setCreater(getSessionController().getLoggedUser());

        if (procedure.getId() == null || procedure.getId() == 0) {
            getPatientEncounterFacade().create(procedure);
        } else {
            getPatientEncounterFacade().edit(procedure);
        }

        return false;

    }

    private void saveBatchBill() {
        getBatchBill().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getLoggedUser().getDepartment(), BillType.SurgeryBill, BillNumberSuffix.SURG));
        getBatchBill().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getLoggedUser().getInstitution(), getBatchBill(), BillType.SurgeryBill, BillNumberSuffix.SURG));

        getBatchBill().setInstitution(getSessionController().getInstitution());
        getBatchBill().setDepartment(getSessionController().getDepartment());
        getBatchBill().setCreatedAt(Calendar.getInstance().getTime());
        getBatchBill().setCreater(getSessionController().getLoggedUser());

        if (getBatchBill().getId() == null) {
            getBillFacade().create(getBatchBill());
        } else {
            getBillFacade().edit(getBatchBill());
        }
    }

    private void saveBill(Bill bill, BillNumberSuffix billNumberSuffix) {
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

        if (bill.getId() == null) {
            getBillFacade().create(bill);
        }
    }

    private void saveBillItem(BillItem billItem) {
        billItem.setBill(getProfessionalBill());
        billItem.setCreatedAt(new Date());
        billItem.setCreater(getSessionController().getLoggedUser());

        getBillItemFacade().create(billItem);

    }

    private void saveBillFee(BillFee bf,Bill bill) {
        if (bf.getId() == null) {
            bf.setBill(bill);
            bf.setCreatedAt(Calendar.getInstance().getTime());
            bf.setCreater(getSessionController().getLoggedUser());
            bf.setFeeAt(Calendar.getInstance().getTime());
            bf.setFeeGrossValue(bf.getFeeValue());
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
        double serviceTot = getInwardCalculation().calTimedServiceCharge(patientItem, patientItem.getToTime());
        patientItem.setServiceValue(serviceTot);

        if (patientItem.getId() == null) {
            patientItem.setCreater(getSessionController().getLoggedUser());
            patientItem.setCreatedAt(Calendar.getInstance().getTime());
            getPatientItemFacade().create(patientItem);

        } else {
            getPatientItemFacade().edit(patientItem);
        }

        return serviceTot;
    }

    private void saveEncounterComponent(BillItem billItem, EncounterComponent ec) {
        if (ec.getId() == null) {
            ec.setBillItem(billItem);
            ec.setCreatedAt(Calendar.getInstance().getTime());
            ec.setCreater(getSessionController().getLoggedUser());
            ec.setPatientEncounter(getBatchBill().getProcedure());
            ec.setStaff(ec.getBillFee().getStaff());
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

    private boolean saveProfessionalBill() {
        BillItem bItem;
        if (getProfessionalBill().getId() == null) {
            saveBill(getProfessionalBill(), BillNumberSuffix.INWPRO);
            bItem = new BillItem();
            saveBillItem(bItem);
        } else {
            getBillFacade().edit(getProfessionalBill());
            bItem = getFirstBillItem(getProfessionalBill());
        }

        for (EncounterComponent ec : getProEncounterComponents()) {
            saveBillFee(ec.getBillFee(),getProfessionalBill());
            saveEncounterComponent(bItem, ec);
        }

        updateBillItem(getProfessionalBill(), bItem);
        updateBill(getProfessionalBill());

        return false;
    }

    private boolean saveTimeServiceBill() {
        BillItem bItem;
        double netValue = 0;
        if (getTimedServiceBill().getId() == null) {
            saveBill(getTimedServiceBill(), BillNumberSuffix.TIME);
            bItem = new BillItem();
            saveBillItem(bItem);
        } else {
            getBillFacade().edit(getTimedServiceBill());
            bItem = getFirstBillItem(getTimedServiceBill());
        }

        for (EncounterComponent ec : getTimedEncounterComponents()) {
            netValue = savePatientItem(ec.getBillFee().getPatientItem());

            //Set Patient Item Service Value to Bill FeeValue
            ec.getBillFee().setFeeValue(netValue);

            saveBillFee(ec.getBillFee(),getTimedServiceBill());
            saveEncounterComponent(bItem, ec);
        }

        updateBillItem(getTimedServiceBill(), bItem);
        updateBill(getTimedServiceBill());

        return false;
    }

    private double getTotalByBillFee(Bill bill) {
        String sql = "Select sum(bf.feeValue) from BillFee bf where "
                + " bf.retired=false and bf.bill=:bill";
        HashMap hm = new HashMap();
        hm.put("bill", bill);
        return getBillFeeFacade().findDoubleByJpql(sql, hm);
    }

    private double getTotalByBillItem(Bill bill) {
        String sql = "Select sum(bf.netValue) from BillItem bf where "
                + " bf.retired=false and bf.bill=:bill";
        HashMap hm = new HashMap();
        hm.put("bill", bill);
        return getBillItemFacade().findDoubleByJpql(sql, hm);
    }

    private double getTotalByBill() {
        String sql = "Select sum(bf.netTotal) from Bill bf where "
                + " bf.retired=false and bf.forwardReferenceBill=:bill";
        HashMap hm = new HashMap();
        hm.put("bill", getBatchBill());
        return getBillFacade().findDoubleByJpql(sql, hm);
    }

    private void updateBillItem(Bill bill, BillItem billItem) {
        double value = getTotalByBillFee(bill);
        billItem.setNetValue(value);
        getBillItemFacade().edit(billItem);
    }

    private void updateBill(Bill bill) {
        double value = getTotalByBillItem(bill);
        bill.setTotal(value);
        bill.setNetTotal(value);
        getBillFacade().edit(bill);
    }

    private void updateBatchBill() {
        double value = getTotalByBill();
        getBatchBill().setTotal(value);

        getBillFacade().edit(getBatchBill());
    }

    public void save() {
        if (saveProcedure()) {
            return;
        }

        saveBatchBill();
        saveProfessionalBill();
        saveTimeServiceBill();

        updateBatchBill();

        UtilityController.addSuccessMessage("Surgery Detail Successfull Added");

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
        String sql = "Select bf from Bill bf where "
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
                setProfessionalBill(b);
                List<EncounterComponent> enc = getEncounterComponents(b);
                setProEncounterComponents(enc);
            }

            if (b.getSurgeryBillType() == SurgeryBillType.TimedService) {
                setTimedServiceBill(b);
                List<EncounterComponent> enc = getEncounterComponents(b);
                setTimedEncounterComponents(enc);
            }
        }
    }

    public Bill getProfessionalBill() {
        if (professionalBill == null) {
            professionalBill = new BilledBill();
            professionalBill.setBillType(BillType.InwardBill);
            professionalBill.setSurgeryBillType(SurgeryBillType.ProfessionalFee);
        }

        return professionalBill;
    }

    public void setProfessionalBill(Bill professionalBill) {

        this.professionalBill = professionalBill;
    }

    public Bill getServiceBill() {
        if (serviceBill == null) {
            serviceBill = new BilledBill();
            serviceBill.setBillType(BillType.InwardBill);
            serviceBill.setSurgeryBillType(SurgeryBillType.Service);
        }
        return serviceBill;
    }

    public void setServiceBill(Bill serviceBill) {
        this.serviceBill = serviceBill;
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

    public void addProfessionalFeeAfterSaving() {
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

        saveProfessionalBill();

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

    public void addTimedServiceFeeAfterSaving() {
        if (generalChecking()) {
            return;
        }

        if (getTimedEncounterComponent().getBillFee().getStaff() == null) {
            UtilityController.addErrorMessage("Select Staff ");
            return;
        }

        timedEncounterComponent.setPatientEncounter(getBatchBill().getPatientEncounter());
        timedEncounterComponent.setChildEncounter(getBatchBill().getProcedure());
        timedEncounterComponent.setOrderNo(getProEncounterComponents().size());

        saveTimeServiceBill();

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

}
