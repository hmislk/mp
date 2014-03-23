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
import com.divudi.data.inward.InwardChargeType;
import com.divudi.data.inward.SurgeryBillType;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.InwardCalculation;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Department;
import com.divudi.entity.Item;
import com.divudi.entity.Patient;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.PatientItem;
import com.divudi.entity.PreBill;
import com.divudi.entity.inward.EncounterComponent;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.entity.pharmacy.UserStock;
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
import java.util.TimeZone;
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
    private Bill pharmacyItemBill;
    private Bill storeItemBill;
    private EncounterComponent proEncounterComponent;
    private EncounterComponent timedEncounterComponent;
    private EncounterComponent serviceEncounterComponent;
    private EncounterComponent pharmacyItemEncounterComponent;
    //////////
    private List<EncounterComponent> proEncounterComponents;
    private List<EncounterComponent> timedEncounterComponents;
    private List<EncounterComponent> serviceEncounterComponents;
    private List<EncounterComponent> pharmacyItemEncounterComponents;
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

    private void updateBillFee(BillFee bf) {
        getBillFeeFacade().edit(bf);
        updateBillItem(bf.getBillItem());
        updateBill(bf.getBill());
        updateBatchBill();
    }

    public void removeProEncFromList(EncounterComponent encounterComponent) {
        removeEncounterComponentFromList(encounterComponent, getProEncounterComponents());
    }

    public void removePharmacyEncFromList(EncounterComponent encounterComponent) {
        removeEncounterComponentFromList(encounterComponent, getPharmacyItemEncounterComponents());
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
        updateBatchBill();

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

    public void removeServiceEncFromList(EncounterComponent encounterComponent) {
        removeEncounterComponentFromList(encounterComponent, getServiceEncounterComponents());
    }

    public void removeTimedEncFromDbase(EncounterComponent encounterComponent) {
        if (generalChecking()) {
            return;
        }

        retiredEncounterComponent(encounterComponent);
        retiredBillFee(encounterComponent.getBillFee());

        updateBillItem(encounterComponent.getBillItem());
        updateBill(encounterComponent.getBillItem().getBill());
        updateBatchBill();
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
        updateBatchBill();

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
        ///////////
        serviceBill = null;
        serviceEncounterComponent = null;
        serviceEncounterComponents = null;
        /////////////
        timedServiceBill = null;
        timedEncounterComponent = null;
        timedEncounterComponents = null;
        /////////////
        pharmacyItemBill = null;
        pharmacyItemEncounterComponent = null;
        pharmacyItemEncounterComponents = null;
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

    private void saveBillFee(BillFee bf, Bill bill, BillItem bIllItem, double value) {
        if (bf.getId() == null) {
            bf.setBill(bill);
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
        double serviceTot = getInwardCalculation().calTimedServiceCharge(patientItem, patientItem.getToTime());
        patientItem.setServiceValue(serviceTot);
        patientItem.setPatientEncounter(getBatchBill().getPatientEncounter());
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

    private boolean saveServiceBill() {

        saveBill(getServiceBill(), BillNumberSuffix.INWSER);

        for (EncounterComponent ec : getServiceEncounterComponents()) {
            List<BillFee> billFees = ec.getBillItem().getBillFees();
            ec.getBillItem().setBillFees(null);

            saveBillItem(ec.getBillItem(), getServiceBill());
            saveEncounterComponent(ec.getBillItem(), ec);

            for (BillFee bf : billFees) {
                saveBillFee(bf, getServiceBill(), ec.getBillItem(), bf.getFeeValue());
            }

            ec.getBillItem().setBillFees(billFees);

            updateBillItem(ec.getBillItem());
        }

        updateBill(getServiceBill());

        return false;
    }

    private void savePreBillFinally(Bill bill, Department currentBhtDepartment,
            BillType billType, BillNumberSuffix billNumberSuffix) {
        if (bill.getId() == null) {
            bill.setBillType(billType);
            bill.setForwardReferenceBill(getBatchBill());

            bill.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), bill, billType, billNumberSuffix));
            bill.setDeptId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getDepartment(), bill, billType, billNumberSuffix));

            bill.setDepartment(getSessionController().getLoggedUser().getDepartment());
            bill.setInstitution(getSessionController().getLoggedUser().getDepartment().getInstitution());

            bill.setCreatedAt(Calendar.getInstance().getTime());
            bill.setCreater(getSessionController().getLoggedUser());

            bill.setPatientEncounter(getBatchBill().getPatientEncounter());

            bill.setBillDate(new Date());
            bill.setBillTime(new Date());

            bill.setFromDepartment(currentBhtDepartment);

            getBillFacade().create(bill);
        } else {
            getBillFacade().edit(bill);
        }

    }

    private void savePreBillItemsFinally(List<EncounterComponent> list, Bill bill) {
        for (EncounterComponent tbi : list) {

            BillItem billItem = tbi.getBillItem();
            PharmaceuticalBillItem pharmaceuticalBillItem = billItem.getPharmaceuticalBillItem();

            System.err.println("BBB 1 " + billItem.getNetValue());
            System.err.println("BBB 2 " + billItem.getRate());
            System.err.println("BBB 3 " + pharmaceuticalBillItem.getQtyInUnit());

            if (billItem.getId() == null) {
                billItem.setBill(bill);
                billItem.setCreatedAt(Calendar.getInstance().getTime());
                billItem.setCreater(getSessionController().getLoggedUser());
                billItem.setPharmaceuticalBillItem(null);

                getBillItemFacade().create(billItem);

                pharmaceuticalBillItem.setBillItem(billItem);
                getPharmaceuticalBillItemFacade().create(pharmaceuticalBillItem);

                double qtyL = pharmaceuticalBillItem.getQtyInUnit() + pharmaceuticalBillItem.getFreeQtyInUnit();

                //Deduct Stock
                boolean returnFlag = getPharmacyBean().deductFromStock(pharmaceuticalBillItem.getStock(),
                        Math.abs(qtyL), pharmaceuticalBillItem, bill.getDepartment());

                if (!returnFlag) {
                    billItem.setTmpQty(0);
                    getPharmaceuticalBillItemFacade().edit(pharmaceuticalBillItem);
                    getBillItemFacade().edit(billItem);
                }

            }

            billItem.setPharmaceuticalBillItem(pharmaceuticalBillItem);
            getBillItemFacade().edit(billItem);

        }

        getBillFacade().edit(bill);
    }

    public void updateFee(Bill bill) {
        double total = 0;
        double netTotal = 0;
        for (EncounterComponent ec : getPharmacyItemEncounterComponents()) {
            System.err.println("Sett 1 " + ec.getBillItem());
            System.err.println("Sett 2 " + ec.getBillItem().getNetValue());
            double value = ec.getBillItem().getNetValue();
            BillFee marginFee, issueFee = null;

            /////////////
            issueFee = getInwardCalculation().getIssueBillFee(ec.getBillItem(), bill.getInstitution());
            issueFee.setBill(bill);
            issueFee.setBillItem(ec.getBillItem());
            issueFee.setFeeValue(value);

            if (issueFee.getId() != null) {
                getBillFeeFacade().edit(issueFee);
            }

            if (issueFee.getId() == null && issueFee.getFeeValue() != 0) {
                getBillFeeFacade().create(issueFee);
            }

            System.err.println("Sett 3 " + issueFee);
            System.err.println("Sett 4 " + issueFee.getFeeValue());

            /////////////
            marginFee = getInwardCalculation().getBillFeeMatrix(ec.getBillItem(), bill.getInstitution());
            double matrixValue = getInwardCalculation().calInwardMargin(ec.getBillItem(), value, bill.getFromDepartment());
            marginFee.setBill(bill);
            marginFee.setBillItem(ec.getBillItem());
            marginFee.setFeeValue(matrixValue);

            if (marginFee.getId() != null) {
                getBillFeeFacade().edit(marginFee);
            }

            if (marginFee.getId() == null && marginFee.getFeeValue() != 0) {
                getBillFeeFacade().create(marginFee);
            }

            System.err.println("Sett 5 " + marginFee);
            System.err.println("Sett 6 " + marginFee.getFeeValue());

            ec.getBillItem().setAdjustedValue(issueFee.getFeeValue() + marginFee.getFeeValue());
            getBillItemFacade().edit(ec.getBillItem());

            total += ec.getBillItem().getNetValue();
            netTotal += ec.getBillItem().getAdjustedValue();
        }

        bill.setTotal(total);
        bill.setNetTotal(netTotal);
        getBillFacade().edit(bill);

    }

    private boolean savePharmacyBill() {
        PatientRoom currentPatientRoom = getInwardCalculation().getCurrentPatientRoom(getBatchBill().getPatientEncounter());

        if (currentPatientRoom.getRoomFacilityCharge() == null) {
            return true;
        }

        if (currentPatientRoom.getRoomFacilityCharge().getDepartment() == null) {
            return true;
        }

        savePreBillFinally(getPharmacyItemBill(), currentPatientRoom.getRoomFacilityCharge().getDepartment(), BillType.PharmacyBhtPre, BillNumberSuffix.PHISSUE);
        savePreBillItemsFinally(getPharmacyItemEncounterComponents(), getPharmacyItemBill());

        // Calculation Margin and Create Billfee 
        updateFee(getPharmacyItemBill());

        for (EncounterComponent ec : getPharmacyItemEncounterComponents()) {
            saveEncounterComponent(ec.getBillItem(), ec);
            updateBillItem(ec.getBillItem());
        }

        updateBill(getPharmacyItemBill());

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

    private double getTotalByBill() {
        String sql = "Select sum(bf.netTotal) from Bill bf where "
                + " bf.retired=false and bf.forwardReferenceBill=:bill";
        HashMap hm = new HashMap();
        hm.put("bill", getBatchBill());
        return getBillFacade().findDoubleByJpql(sql, hm);
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

    private void updateBatchBill() {
        double value = getTotalByBill();
        getBatchBill().setTotal(value);

        getBillFacade().edit(getBatchBill());
    }

    public void save() {
        if (generalChecking()) {
            return;
        }

        saveProcedure();

        if (!getProEncounterComponents().isEmpty()
                || !getTimedEncounterComponents().isEmpty()
                || !getServiceEncounterComponents().isEmpty()
                || !getPharmacyItemEncounterComponents().isEmpty()) {

            saveBatchBill();

            if (!getProEncounterComponents().isEmpty()) {
                saveProfessionalBill();
            }

            if (!getTimedEncounterComponents().isEmpty()) {
                saveTimeServiceBill();
            }

            if (!getServiceEncounterComponents().isEmpty()) {
                saveServiceBill();
            }

            if (!getPharmacyItemEncounterComponents().isEmpty()) {
                savePharmacyBill();
            }

            updateBatchBill();
        }

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
                System.err.println(SurgeryBillType.ProfessionalFee);
                setProfessionalBill(b);
                List<EncounterComponent> enc = getEncounterComponents(b);
                setProEncounterComponents(enc);
            }

            if (b.getSurgeryBillType() == SurgeryBillType.TimedService) {
                System.err.println(SurgeryBillType.TimedService);
                setTimedServiceBill(b);
                List<EncounterComponent> enc = getEncounterComponents(b);
                setTimedEncounterComponents(enc);
            }

            if (b.getSurgeryBillType() == SurgeryBillType.Service) {
                System.err.println(SurgeryBillType.Service);
                setServiceBill(b);
                List<EncounterComponent> enc = getEncounterComponents(b);
                setServiceEncounterComponents(enc);
            }

            if (b.getSurgeryBillType() == SurgeryBillType.PharmacyItem) {
                System.err.println(SurgeryBillType.PharmacyItem);
                setPharmacyItemBill(b);
                List<EncounterComponent> enc = getEncounterComponents(b);
                setPharmacyItemEncounterComponents(enc);
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

    private boolean checkForService() {
        if (getServiceEncounterComponent().getBillItem().getItem() == null) {
            UtilityController.addErrorMessage("Select Service ");
            return true;
        }

        PatientRoom patientRoom = getInwardCalculation().getCurrentPatientRoom(getBatchBill().getPatientEncounter());

        if (patientRoom == null) {
            UtilityController.addErrorMessage("Please Set Room or Bed For This Patient");
            return true;
        }

        Item item = getServiceEncounterComponent().getBillItem().getItem();

        if (patientRoom.getRoomFacilityCharge().getDepartment() == null) {
            UtilityController.addErrorMessage("Under administration, add a Department for this Room " + patientRoom.getRoom().getName());
            return true;
        }

        if (item.getDepartment() == null) {
            UtilityController.addErrorMessage("Under administration, "
                    + " add a Department for this item " + item.getName());
            return true;
        } else if (item.getDepartment().getInstitution() == null) {
            UtilityController.addErrorMessage("Under administration, add an Institution for "
                    + " the department " + item.getDepartment());
            return true;
        } else if (item.getCategory() == null) {
            UtilityController.addErrorMessage("Under administration, add Category "
                    + "For Item : " + item.getName());
            return true;
        }

        return false;
    }

    public void addService() {
        if (generalChecking()) {
            return;
        }

        if (checkForService()) {
            return;
        }

        List<BillFee> billFees = new ArrayList<>();
        billFees = getInwardCalculation().billFeeFromBillItemWithMatrix(getServiceEncounterComponent().getBillItem(),
                getBatchBill().getPatientEncounter(),
                getServiceEncounterComponent().getBillItem().getItem().getInstitution());

        getServiceEncounterComponent().getBillItem().setBillFees(billFees);
        getServiceEncounterComponent().setPatientEncounter(getBatchBill().getPatientEncounter());
        getServiceEncounterComponent().setChildEncounter(getBatchBill().getProcedure());
        getServiceEncounterComponents().add(getServiceEncounterComponent());

        serviceEncounterComponent = null;
    }

    public void addServiceAfterSaving() {
        if (generalChecking()) {
            return;
        }

        if (checkForService()) {
            return;
        }

        List<BillFee> billFees = new ArrayList<>();
        billFees = getInwardCalculation().billFeeFromBillItemWithMatrix(getServiceEncounterComponent().getBillItem(),
                getBatchBill().getPatientEncounter(),
                getServiceEncounterComponent().getBillItem().getItem().getInstitution());

        getServiceEncounterComponent().getBillItem().setBillFees(billFees);
        getServiceEncounterComponent().setPatientEncounter(getBatchBill().getPatientEncounter());
        getServiceEncounterComponent().setChildEncounter(getBatchBill().getProcedure());
        getServiceEncounterComponents().add(getServiceEncounterComponent());

        saveServiceBill();

        serviceEncounterComponent = null;
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

    public EncounterComponent getServiceEncounterComponent() {
        if (serviceEncounterComponent == null) {
            serviceEncounterComponent = new EncounterComponent();
            serviceEncounterComponent.setBillItem(new BillItem());
        }
        return serviceEncounterComponent;
    }

    public void setServiceEncounterComponent(EncounterComponent serviceEncounterComponent) {
        this.serviceEncounterComponent = serviceEncounterComponent;
    }

    public List<EncounterComponent> getServiceEncounterComponents() {
        if (serviceEncounterComponents == null) {
            serviceEncounterComponents = new ArrayList<>();
        }
        return serviceEncounterComponents;
    }

    public void setServiceEncounterComponents(List<EncounterComponent> serviceEncounterComponents) {
        this.serviceEncounterComponents = serviceEncounterComponents;
    }

    public Bill getPharmacyItemBill() {
        if (pharmacyItemBill == null) {
            pharmacyItemBill = new PreBill();
            pharmacyItemBill.setBillType(BillType.PharmacyBhtPre);
            pharmacyItemBill.setSurgeryBillType(SurgeryBillType.PharmacyItem);
        }

        return pharmacyItemBill;
    }

    public void setPharmacyItemBill(Bill pharmacyBill) {
        this.pharmacyItemBill = pharmacyBill;
    }

    public Bill getStoreItemBill() {
        return storeItemBill;
    }

    public void setStoreItemBill(Bill storeItemBill) {
        this.storeItemBill = storeItemBill;
    }

    public EncounterComponent getPharmacyItemEncounterComponent() {
        if (pharmacyItemEncounterComponent == null) {
            pharmacyItemEncounterComponent = new EncounterComponent();
            BillItem billItem = new BillItem();
            billItem.setInwardChargeType(InwardChargeType.Medicine);
            billItem.setPharmaceuticalBillItem(new PharmaceuticalBillItem());
            pharmacyItemEncounterComponent.setBillItem(billItem);
        }
        return pharmacyItemEncounterComponent;
    }

    private boolean checkItemBatch() {
        for (EncounterComponent ec : getPharmacyItemEncounterComponents()) {
            if (ec.getBillItem().getPharmaceuticalBillItem().getStock().getId()
                    == ec.getBillItem().getPharmaceuticalBillItem().getStock().getId()) {
                return true;
            }
        }

        return false;
    }

    public void addPharmacyBillItem() {

        if (generalChecking()) {
            return;
        }

        if (getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem().getStock() == null) {
            UtilityController.addErrorMessage("Item?");
            return;
        }

        if (getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem().getStock().getItemBatch() == null) {
            return;
        }

        if (getPharmacyItemEncounterComponent().getBillItem().getQty() == null) {
            UtilityController.addErrorMessage("Quentity?");
            return;
        }

        double rate = getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate();
        double qty = getPharmacyItemEncounterComponent().getBillItem().getQty();
        Stock stock = getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem().getStock();
        Item item = getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem().getStock().getItemBatch().getItem();
        BillItem billItem = getPharmacyItemEncounterComponent().getBillItem();
        PharmaceuticalBillItem pharmaceuticalBillItem = getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem();

        if (qty > stock.getStock()) {
            UtilityController.addErrorMessage("No Sufficient Stocks?");
            return;
        }

        if (checkItemBatch()) {
            UtilityController.addErrorMessage("Already added this item batch");
            return;
        }

        billItem.setItem(item);
        billItem.setRate(rate);

        pharmaceuticalBillItem.setQtyInUnit(qty);
        pharmaceuticalBillItem.setDoe(stock.getItemBatch().getDateOfExpire());
        pharmaceuticalBillItem.setFreeQty(0.0f);
        pharmaceuticalBillItem.setItemBatch(stock.getItemBatch());
        pharmaceuticalBillItem.setQtyInUnit((double) (0 - qty));

        //Rates
        //Values
        billItem.setGrossValue(pharmaceuticalBillItem.getStock().getItemBatch().getRetailsaleRate() * qty);
        billItem.setNetValue(qty * billItem.getRate());
        billItem.setInwardChargeType(InwardChargeType.Medicine);
        billItem.setSearialNo(getPharmacyItemEncounterComponents().size());

        System.err.println("1 " + qty);
        System.err.println("2 " + rate);
        System.err.println("3 " + billItem.getRate());
        System.err.println("4 " + billItem.getNetValue());

        getPharmacyItemEncounterComponent().setPatientEncounter(getBatchBill().getPatientEncounter());
        getPharmacyItemEncounterComponent().setChildEncounter(getBatchBill().getProcedure());
        getPharmacyItemEncounterComponents().add(getPharmacyItemEncounterComponent());

        pharmacyItemEncounterComponent = null;

    }

    public void addPharmacyBillItemAfterSaving() {

        if (getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem().getStock() == null) {
            UtilityController.addErrorMessage("Item?");
            return;
        }

        if (getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem().getStock().getItemBatch() == null) {
            return;
        }

        if (getPharmacyItemEncounterComponent().getBillItem().getQty() == null) {
            UtilityController.addErrorMessage("Quentity?");
            return;
        }

        if (getPharmacyItemBill().getDepartment().getId() != getSessionController().getDepartment().getId()) {

            return;
        }

        double rate = getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate();
        double qty = getPharmacyItemEncounterComponent().getBillItem().getQty();
        Stock stock = getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem().getStock();
        Item item = getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem().getStock().getItemBatch().getItem();
        BillItem billItem = getPharmacyItemEncounterComponent().getBillItem();
        PharmaceuticalBillItem pharmaceuticalBillItem = getPharmacyItemEncounterComponent().getBillItem().getPharmaceuticalBillItem();

        if (qty > stock.getStock()) {
            UtilityController.addErrorMessage("No Sufficient Stocks?");
            return;
        }

        if (checkItemBatch()) {
            UtilityController.addErrorMessage("Already added this item batch");
            return;
        }

        billItem.setItem(item);
        billItem.setRate(rate);
        billItem.getPharmaceuticalBillItem().setQtyInUnit(qty);

        pharmaceuticalBillItem.setDoe(pharmaceuticalBillItem.getStock().getItemBatch().getDateOfExpire());
        pharmaceuticalBillItem.setFreeQty(0.0f);
        pharmaceuticalBillItem.setItemBatch(pharmaceuticalBillItem.getStock().getItemBatch());
        pharmaceuticalBillItem.setQtyInUnit((double) (0 - qty));

        //Rates
        //Values
        billItem.setGrossValue(pharmaceuticalBillItem.getStock().getItemBatch().getRetailsaleRate() * qty);
        billItem.setNetValue(qty * billItem.getNetRate());

        billItem.setInwardChargeType(InwardChargeType.Medicine);

        billItem.setSearialNo(getPharmacyItemEncounterComponents().size());

        getPharmacyItemEncounterComponents().add(getPharmacyItemEncounterComponent());

        savePharmacyBill();

    }

    public void setPharmacyItemEncounterComponent(EncounterComponent pharmacyItemEncounterComponent) {
        this.pharmacyItemEncounterComponent = pharmacyItemEncounterComponent;
    }

    public List<EncounterComponent> getPharmacyItemEncounterComponents() {
        if (pharmacyItemEncounterComponents == null) {
            pharmacyItemEncounterComponents = new ArrayList<>();
        }
        return pharmacyItemEncounterComponents;
    }

    public void setPharmacyItemEncounterComponents(List<EncounterComponent> pharmacyItemEncounterComponents) {
        this.pharmacyItemEncounterComponents = pharmacyItemEncounterComponents;
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

}
