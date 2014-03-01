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
import com.divudi.data.FeeType;
import com.divudi.data.inward.InwardChargeType;
import static com.divudi.data.inward.InwardChargeType.AdmissionFee;
import static com.divudi.data.inward.InwardChargeType.LinenCharges;
import static com.divudi.data.inward.InwardChargeType.MOCharges;
import static com.divudi.data.inward.InwardChargeType.MaintainCharges;
import static com.divudi.data.inward.InwardChargeType.Medicine;
import static com.divudi.data.inward.InwardChargeType.NursingCharges;
import static com.divudi.data.inward.InwardChargeType.RoomCharges;
import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.ChargeItemTotal;
import com.divudi.data.dataStructure.RoomChargeData;
import com.divudi.ejb.BillNumberBean;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.InwardCalculation;
import com.divudi.entity.inward.Admission;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.PatientItem;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.inward.TimedItemFee;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PatientEncounterFacade;
import com.divudi.facade.PatientItemFacade;
import com.divudi.facade.PatientRoomFacade;
import com.divudi.facade.ServiceFacade;
import com.divudi.facade.TimedItemFeeFacade;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 Informatics)
 */
@Named
@SessionScoped
public class BhtSummeryController implements Serializable {

    private static final long serialVersionUID = 1L;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private PatientRoomFacade patientRoomFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private ServiceFacade serviceFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private PatientItemFacade patientItemFacade;
    @EJB
    private TimedItemFeeFacade timedItemFeeFacade;
    @EJB
    private PatientEncounterFacade patientEncounterFacade;
    ////////////////////////////
    @EJB
    private InwardCalculation inwardCalculation;
    @EJB
    private BillNumberBean billNumberBean;
    //////////////////////////
    @Inject
    private SessionController sessionController;
    @Inject
    private InwardTimedItemController inwardTimedItemController;
    @Inject
    private DischargeController dischargeController;
    ////////////////////////
    private List<RoomChargeData> patientRoom;
    private List<BillItem> service;
    private List<BillFee> profesionallFee;
    private List<Bill> paymentBill;
    List<PatientItem> patientItems;
    private List<ChargeItemTotal> chargeItemTotals;
    //////////////////////////
    private double costOfServices;
    private double costOfTimed;
    private double costOfMadicine;
    private double totalRoomCharges;
    private double totalMaintanance;
    private double totalMOCharge;
    private double totalLinen;
    private double totalAdditional;
    private double totalNursing;
    private double professionalCharges = 0.0;
    private double additionalCharge;
    private double grantTotal = 0.0;
    private double discount;
    private double due;
    private double paid;
    private PatientItem tmpPI;
    private Admission patientEncounter;
    private Bill current;
    @Temporal(TemporalType.TIMESTAMP)
    private Date currentTime;
    private Date toTime;
    private boolean printPreview;

    public void checkDate() {
        if (getPatientEncounter() != null && getPatientEncounter().getDateOfAdmission().after(getPatientEncounter().getDateOfDischarge())) {
            UtilityController.addErrorMessage("Check Discharge Time should be after Admitted Time");
        }

        if (getPatientEncounter() != null && getPatientEncounter().getDateOfDischarge().after(new Date())) {
            UtilityController.addErrorMessage("Check Discharge Time can't be set after than now");
        }

        chargeItemTotals = null;
        costOfServices = 0.0;
        costOfTimed = 0.0;
        costOfMadicine = 0.0;
        totalRoomCharges = 0.0;
        totalMaintanance = 0.0;
        totalMOCharge = 0.0;
        totalLinen = 0.0;
        totalAdditional = 0.0;
        totalNursing = 0.0;
        professionalCharges = 0.0;
        additionalCharge = 0.0;
        grantTotal = 0.0;
        discount = 0.0;
        due = 0.0;
        paid = 0.0;
        patientRoom = null;
        profesionallFee = null;
        patientItems = null;
        paymentBill = null;
        service = null;
        printPreview = false;
        current = null;
        tmpPI = null;
        currentTime = null;
        toTime = null;
    }

    public List<BillItem> getBillItems() {
        HashMap hm = new HashMap();
        String sql = "Select b From BillItem b where b.retired=false and b.bill=:b";
        hm.put("b", getCurrent());
        return getBillItemFacade().findBySQL(sql, hm);
    }

    public void settle() {
        if (errorCheck()) {
            return;
        }
        // savePayment();
        if (getPatientEncounter().getPaymentMethod() == PaymentMethod.Credit) {
            updateCreditDetail();
        }
        saveBill();
        saveBillItem();
        UtilityController.addSuccessMessage("Bill Saved");

        if (!getPatientEncounter().isDischarged()) {
            getDischargeController().setCurrent((Admission) getPatientEncounter());
            getDischargeController().discharge();
        }
        printPreview = true;
    }

    private void updateCreditDetail() {
        if (getPatientEncounter().getCreditLimit() <= getNetCharge()) {
            getPatientEncounter().setCreditUsedAmount(getPatientEncounter().getCreditLimit());
        } else {
            getPatientEncounter().setCreditUsedAmount(getNetCharge());
        }

        getPatientEncounterFacade().edit(getPatientEncounter());
    }

    public void discharge() {
        if (patientEncounter.isDischarged()) {
            UtilityController.addErrorMessage("Patient Already Discharged");
            return;
        }

        for (PatientItem pi : patientItems) {
            if (pi.getFinalize() == false) {
                getInwardTimedItemController().setTmpPI(pi);
                getInwardTimedItemController().finalizeService();
            }
        }

        getDischargeController().setCurrent((Admission) patientEncounter);
        getDischargeController().discharge();
        UtilityController.addSuccessMessage("Patient  Discharged");
    }

    public String dischargeLink() {
        getPatientEncounter().setDateOfDischarge(new Date());
        return "inward_discharge";
    }

    private boolean errorCheck() {
        if (getCurrent().getPaymentScheme() != null && getCurrent().getPaymentScheme().getPaymentMethod() != null && getCurrent().getPaymentScheme().getPaymentMethod() == PaymentMethod.Cheque) {
            if (getCurrent().getBank().getId() == null || getCurrent().getChequeRefNo() == null) {
                UtilityController.addErrorMessage("Please select Cheque Number and Bank");
                return true;
            }
        }

        if (getCurrent().getPaymentScheme() != null && getCurrent().getPaymentScheme().getPaymentMethod() != null && getCurrent().getPaymentScheme().getPaymentMethod() == PaymentMethod.Slip) {
            if (getCurrent().getBank().getId() == null || getCurrent().getComments() == null) {
                UtilityController.addErrorMessage("Please Fill Memo and Bank");
                return true;
            }
        }

        if (getCurrent().getPaymentScheme() != null && getCurrent().getPaymentScheme().getPaymentMethod() != null && getCurrent().getPaymentScheme().getPaymentMethod() == PaymentMethod.Card) {
            if (getCurrent().getBank() == null || getCurrent().getCreditCardRefNo() == null) {
                UtilityController.addErrorMessage("Please Fill Credit Card Number and Bank");
                return true;
            }
            if (getCurrent().getCreditCardRefNo().trim().length() < 16) {
                UtilityController.addErrorMessage("Enter 16 Digit");
                return true;
            }
        }

        if (getCurrent().getPaymentScheme() != null && getCurrent().getPaymentScheme().getPaymentMethod() != null && getCurrent().getPaymentScheme().getPaymentMethod() == PaymentMethod.Credit) {
            if (getCurrent().getCreditCompany() == null) {
                UtilityController.addErrorMessage("Please Select Credit Company");
                return true;
            }
        }

        if (getCurrent().getPaymentScheme().getPaymentMethod() == PaymentMethod.Cash) {

            if (getCurrent().getCashPaid() < due) {
                UtilityController.addErrorMessage("Please select tendered amount correctly");
                return true;
            }
        }

        if (checkCatTotal()) {
            return true;
        }

        return false;

    }

    private boolean checkCatTotal() {
        double tot = 0.0;
        double tot2 = 0.0;
        for (ChargeItemTotal cit : chargeItemTotals) {
            tot += cit.getTotal();
            tot2 += cit.getAdjustedTotal();
        }

        if (tot != tot2) {
            UtilityController.addErrorMessage("Please Adjust category amount correctly");
            return true;
        }

        return false;
    }

    private void saveBill() {
        getCurrent().setDiscount(discount);
        getCurrent().setNetTotal(due);
        getCurrent().setGrantTotal(grantTotal);

        getCurrent().setInstitution(getSessionController().getInstitution());

        getCurrent().setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), BillType.InwardPaymentBill, BillNumberSuffix.INWPAY));
        getCurrent().setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(),getCurrent(), BillType.InwardPaymentBill, BillNumberSuffix.INWPAY));

        getCurrent().setBillType(BillType.InwardPaymentBill);
        getCurrent().setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setPatientEncounter(patientEncounter);
        getCurrent().setPatient(patientEncounter.getPatient());
        getCurrent().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        getCurrent().setCreater(getSessionController().getLoggedUser());
        getBillFacade().create(getCurrent());
    }

    private void saveBillItem() {
        for (ChargeItemTotal cit : chargeItemTotals) {
            BillItem temBi = new BillItem();
            temBi.setBill(getCurrent());
            temBi.setInwardChargeType(cit.getInwardChargeType());
            temBi.setGrossValue(cit.getTotal());
            temBi.setNetValue(cit.getAdjustedTotal());
            temBi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            temBi.setCreater(getSessionController().getLoggedUser());
            getBillItemFacade().create(temBi);
            //   saveBillFee(temBi);
        }

    }

//    private void savePayment() {
//        BilledBill payment = new BilledBill();
//
//        payment.setInstitution(getSessionController().getInstitution());
//
//        payment.setDeptId(getBillNumberBean().departmentBillNumberGenerator(getSessionController().getDepartment(), BillType.InwardPaymentBill, "inwP"));
//        payment.setInsId(getBillNumberBean().institutionBillNumberGenerator(getSessionController().getInstitution(), BillType.InwardPaymentBill, "inwP"));
//
//        payment.setBillType(BillType.InwardPaymentBill);
//        payment.setBillDate(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//        payment.setBillTime(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//        payment.setPatientEncounter(patientEncounter);
//        payment.setPatient(patientEncounter.getPatient());
//        payment.setPaymentScheme(getCurrent().getPaymentScheme());
//        payment.setNetTotal();
//        getCurrent().setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//        getCurrent().setCreater(getSessionController().getLoggedUser());
//        getBillFacade().create(getCurrent());
//
//
//
//        BillItem temBi = new BillItem();
//        temBi.setBill(getCurrent());
//        temBi.setGrossValue(getCurrent().getTotal());
//        temBi.setNetValue(getCurrent().getTotal());
//        temBi.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//        temBi.setCreater(getSessionController().getLoggedUser());
//        getBillItemFacade().create(temBi);
//
//    }
    public List<PatientItem> getPatientItems() {
        if (patientItems == null) {
            if (getPatientEncounter() == null) {
                return new ArrayList<>();
            }

            HashMap hm = new HashMap();
            String sql = "SELECT i FROM PatientItem i where Type(i.item)=TimedItem and i.retired=false and i.patientEncounter=:pe";
            hm.put("pe", getPatientEncounter());
            patientItems = getPatientItemFacade().findBySQL(sql, hm);

            if (patientItems == null) {
                patientItems = new ArrayList<>();
            }

            for (PatientItem pi : patientItems) {
                if (pi.getFinalize() == null) {
                    double serviceTot = getInwardCalculation().calTimedServiceCharge(pi, getPatientEncounter().getDateOfDischarge());
                    pi.setServiceValue(serviceTot);
                }
            }
        }

        return patientItems;
    }

    public void finalizeService() {
        if (getToTime() != null) {
            if (getToTime().before(getTmpPI().getFromTime())) {
                UtilityController.addErrorMessage("Service Not Finalize check Service Start Time & End Time");
                return;
            }
        }

        if (getToTime() == null) {
            getTmpPI().setToTime(Calendar.getInstance().getTime());
        } else {
            getTmpPI().setToTime(getToTime());
        }

        double serviceTot = getInwardCalculation().calTimedServiceCharge(getTmpPI(), getToTime());
        getTmpPI().setServiceValue(serviceTot);

        getTmpPI().setFinalize(Boolean.TRUE);
        getPatientItemFacade().edit(tmpPI);

        setToTime(null);
    }

    public void makeNull() {
        chargeItemTotals = null;
        costOfServices = 0.0;
        costOfTimed = 0.0;
        costOfMadicine = 0.0;
        totalRoomCharges = 0.0;
        totalMaintanance = 0.0;
        totalMOCharge = 0.0;
        totalLinen = 0.0;
        totalAdditional = 0.0;
        totalNursing = 0.0;
        professionalCharges = 0.0;
        additionalCharge = 0.0;
        grantTotal = 0.0;
        discount = 0.0;
        due = 0.0;
        paid = 0.0;
        patientEncounter = null;
        patientRoom = null;
        profesionallFee = null;
        patientItems = null;
        paymentBill = null;
        service = null;
        printPreview = false;
        current = null;
        tmpPI = null;
        currentTime = null;
        toTime = null;
    }

    public Admission getPatientEncounter() {
        return patientEncounter;
    }

    public void setPatientEncounter(Admission patientEncounter) {
        makeNull();
        this.patientEncounter = patientEncounter;
        getPatientRoom();
        getService();
        getPatientItems();
        getProfessionalCharges();
        getPaymentBill();
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
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

    public List<RoomChargeData> getPatientRoom() {
        if (patientRoom == null) {
            patientRoom = new ArrayList<>();

            if (getPatientEncounter() == null) {
                return new ArrayList<>();
            }

            HashMap hm = new HashMap();
            String sql = "SELECT pr FROM PatientRoom pr where pr.retired=false and pr.patientEncounter=:pe order by pr.createdAt";
            hm.put("pe", getPatientEncounter());
            List<PatientRoom> tmp = getPatientRoomFacade().findBySQL(sql, hm);

            if (tmp != null) {
                setRoomChargeData(tmp);
            //    totalLinen = getInwardCalculation().calTotalLinen(tmp);               
                return patientRoom;
            } else {
                return new ArrayList<>();
            }
        } else {
            return patientRoom;
        }
    }

    private void setRoomChargeData(List<PatientRoom> tmp) {

        for (PatientRoom p : tmp) {

            RoomChargeData rcd = new RoomChargeData();
            addRoomCharge(rcd, p);
            addLinenCharge(rcd,p);
            addMaintananceCharge(rcd, p);
            addNursingCharge(rcd, p);
            addMoCharge(rcd, p);

            patientRoom.add(rcd);
        }
    }

    private void addMoCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;

        if (p.getRoomFacilityCharge() == null || p.getCurrentMoCharge() == 0.0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double mo = p.getCurrentMoCharge();
        //long servicedPeriod;
        if (p.getDischargedAt() != null) {
            charge = mo * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = mo * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }
        rcd.setMoChargeTot(charge);
    }

    private void addNursingCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;

        if (p.getRoomFacilityCharge() == null || p.getCurrentNursingCharge() == 0.0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double nursing = p.getCurrentNursingCharge();
        //long servicedPeriod;
        if (p.getDischargedAt() != null) {
            charge = nursing * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = nursing * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }
        rcd.setNursingTot(charge);
    }

    private void addRoomCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;
        if (p.getRoomFacilityCharge() == null || p.getRoomFacilityCharge().getRoomCharge() == null) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double roomCharge = p.getRoomFacilityCharge().getRoomCharge();
     //  //System.out.println("ssssssssssssssssssssss " + roomCharge);
        rcd.setPatientRoom(p);

        if (p.getDischargedAt() != null) {
            charge = roomCharge * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = roomCharge * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }

        rcd.setChargeTot(charge);
    }
    
    private void addLinenCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;

        if (p.getRoomFacilityCharge() == null || p.getCurrentLinenCharge() == 0.0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double linen = p.getCurrentLinenCharge();
        //long servicedPeriod;
        if (p.getDischargedAt() != null) {
            charge = linen * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = linen * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }
        rcd.setLinenTot(charge+p.getAddedLinenCharge());
    }

    private void addMaintananceCharge(RoomChargeData rcd, PatientRoom p) {
        double charge;

        if (p.getRoomFacilityCharge() == null || p.getCurrentMaintananceCharge() == 0.0) {
            return;
        }

        TimedItemFee timedFee = p.getRoomFacilityCharge().getTimedItemFee();
        double maintanance = p.getCurrentMaintananceCharge();
        //long servicedPeriod;
        if (p.getDischargedAt() != null) {
            charge = maintanance * getInwardCalculation().calCountWithoutOverShoot(timedFee, p);
        } else {
            charge = maintanance * getInwardCalculation().calCount(timedFee, p.getAdmittedAt(), getPatientEncounter().getDateOfDischarge());
        }
        rcd.setMaintananceTot(charge);
    }

//    private void calTotalCharge(List<RoomChargeData> pr) {
//        double tmpC, tmpM, tmpN, tmpMo;
//        tmpN = tmpC = tmpM = tmpMo = 0.0;
//        for (RoomChargeData rcd : pr) {
//            tmpC += rcd.getChargeTot();
//            tmpM += rcd.getMaintananceTot();
//            tmpN += rcd.getNursingTot();
//            tmpMo += rcd.getMoChargeTot();
//        }
//        totalRoomCharges = tmpC;
//        totalMaintanance = tmpM;
//        totalNursing = tmpN;
//        totalMOCharge = tmpMo;
//
//
//    }
    public void setPatientRoom(List<RoomChargeData> patientRoom) {
        this.patientRoom = patientRoom;
    }

    public PatientRoomFacade getPatientRoomFacade() {
        return patientRoomFacade;
    }

    public void setPatientRoomFacade(PatientRoomFacade patientRoomFacade) {
        this.patientRoomFacade = patientRoomFacade;
    }

    public List<BillItem> getService() {
        if (service == null) {
            if (getPatientEncounter() == null) {
                return new ArrayList<>();
            }

            String sql = "SELECT  b FROM BillItem b WHERE b.retired=false  and b.bill.billType=:btp and"
                    + " Type(b.item)!=TimedItem and b.item is not null  and b.bill.patientEncounter=:pe and b.bill.cancelled=false";
            HashMap hm = new HashMap();
            hm.put("btp", BillType.InwardBill);
            hm.put("pe", getPatientEncounter());

            service = getBillItemFacade().findBySQL(sql, hm, TemporalType.TIME);

            if (service == null) {
                service = new ArrayList<>();
            }

            calServiceTot(service);
        }

        return service;
    }

    private void calServiceTot(List<BillItem> sl) {
        double temp = 0.0;
        for (BillItem s : sl) {
            temp += s.getNetValue();
        }
        costOfServices = temp;

    }

    public void setService(List<BillItem> service) {
        this.service = service;
    }

    public ServiceFacade getServiceFacade() {
        return serviceFacade;
    }

    public void setServiceFacade(ServiceFacade serviceFacade) {
        this.serviceFacade = serviceFacade;
    }

    public List<BillFee> getProfesionallFee() {
        if (profesionallFee == null) {
            if (getPatientEncounter() == null) {
                return new ArrayList<>();
            }
            HashMap hm = new HashMap();
            String sql = "SELECT bt FROM BillFee bt WHERE bt.retired=false and bt.fee is null and  bt.bill.id in "
                    + "(SELECT  b.id FROM Bill b WHERE b.retired=false  and b.billType=:btp and b.patientEncounter=:pe and b.cancelled=false )";
            hm.put("btp", BillType.InwardBill);
            hm.put("pe", getPatientEncounter());

            profesionallFee = getBillFeeFacade().findBySQL(sql, hm, TemporalType.TIME);
            ////System.out.println("Size : " + profesionallFee.size());

            if (profesionallFee == null) {
                profesionallFee = new ArrayList<>();
            }

            calProfessionalTot(profesionallFee);
        }

        return profesionallFee;
    }

    private void calProfessionalTot(List<BillFee> pr) {
        double temp = 0.0;
        professionalCharges = 0.0;
        for (BillFee b : pr) {
            temp += b.getFeeValue();
        }
        professionalCharges = temp;
    }

    public void setProfesionallFee(List<BillFee> profesionallFee) {
        this.profesionallFee = profesionallFee;
    }

    public List<Bill> getPaymentBill() {
        if (paymentBill == null) {
            if (getPatientEncounter() == null) {
                return new ArrayList<>();
            }

            HashMap hm = new HashMap();
            String sql = "SELECT  b FROM Bill b WHERE b.retired=false  and b.billType=:btp and b.patientEncounter=:pe and b.cancelled=false";
            hm.put("btp", BillType.InwardPaymentBill);
            hm.put("pe", getPatientEncounter());
            paymentBill = getBillFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

            if (paymentBill == null) {

                return new ArrayList<>();
            }

            calPaidTot(paymentBill);
        }
        return paymentBill;
    }

    private void calPaidTot(List<Bill> lb) {
        double temp = 0.0;
        paid = 0.0;
        for (Bill b : lb) {
            temp += b.getNetTotal();
        }

        paid = temp;
    }

    public void setPaymentBill(List<Bill> paymentBill) {
        this.paymentBill = paymentBill;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public double getCostOfServices() {
        getService();
        return costOfServices;
    }

    public void setCostOfServices(double costOfServices) {
        this.costOfServices = costOfServices;
    }

    public double getProfessionalCharges() {
        getProfesionallFee();
        return professionalCharges;
    }

    public void setProfessionalCharges(double professionalCharges) {
        this.professionalCharges = professionalCharges;
    }

    public double getPaid() {
        getPaymentBill();
        return paid;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public double getTotalRoomCharges() {
        if (totalRoomCharges == 0.0) {
            for (RoomChargeData rcd : getPatientRoom()) {
                totalRoomCharges += rcd.getChargeTot();
            }
        }
        return totalRoomCharges;
    }

    public void setTotalRoomCharges(double totalRoomCharges) {
        this.totalRoomCharges = totalRoomCharges;
    }

    public double getTotalMaintanance() {
        if (totalMaintanance == 0.0) {
            for (RoomChargeData rcd : patientRoom) {
                totalMaintanance += rcd.getMaintananceTot();
            }
        }
        return totalMaintanance;
    }

    public void setTotalMaintanance(double totalMaintanance) {
        this.totalMaintanance = totalMaintanance;
    }

    public double getCostOfMadicine() {
        return costOfMadicine;
    }

    public void setCostOfMadicine(double costOfMadicine) {
        this.costOfMadicine = costOfMadicine;
    }

    public double getTotalLinen() {
        if (totalLinen == 0.0) {
            for (RoomChargeData rcd : patientRoom) {
                totalLinen += rcd.getLinenTot();
            }
        }
        return totalLinen;
    }

    public void setTotalLinen(double totalLinen) {
        this.totalLinen = totalLinen;
    }

    public double getTotalAdditional() {
        return totalAdditional;
    }

    public void setTotalAdditional(double totalAdditional) {
        this.totalAdditional = totalAdditional;
    }

    public double getGrantTotal() {
        grantTotal = 0.0;
        if (patientEncounter == null) {
            return grantTotal;
        }

        for (ChargeItemTotal c : getChargeItemTotals()) {
            grantTotal += c.getTotal();
        }

        return grantTotal;
    }
    double netCharge = 0.0;

    public void setGrantTotal(double grantTotal) {
        this.grantTotal = grantTotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getDue() {
        if (patientEncounter == null) {
            return 0.0;
        }

        due = grantTotal - discount - paid;

        if (getPatientEncounter().getPaymentMethod() == PaymentMethod.Credit) {
            due -= getPatientEncounter().getCreditLimit();

        }

        return due;
    }

    public void setDue(double due) {
        this.due = due;
    }

    public double getNetCharge() {
        if (discount == 0.0) {
            return grantTotal;
        }
        return (grantTotal - discount);
    }

    public double getTotalNursing() {
        if (totalNursing == 0.0) {
            for (RoomChargeData rcd : patientRoom) {
                totalNursing += rcd.getNursingTot();
            }
        }

        //System.out.println("nursing : " + totalNursing);
        return totalNursing;
    }

    public void setTotalNursing(double totalNursing) {
        this.totalNursing = totalNursing;
    }

    public Date getCurrentTime() {
        currentTime = Calendar.getInstance().getTime();

        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public PatientItemFacade getPatientItemFacade() {
        return patientItemFacade;
    }

    public void setPatientItemFacade(PatientItemFacade patientItemFacade) {
        this.patientItemFacade = patientItemFacade;
    }

    public TimedItemFeeFacade getTimedItemFeeFacade() {
        return timedItemFeeFacade;
    }

    public void setTimedItemFeeFacade(TimedItemFeeFacade timedItemFeeFacade) {
        this.timedItemFeeFacade = timedItemFeeFacade;
    }

    public PatientItem getTmpPI() {
        return tmpPI;
    }

    public void setTmpPI(PatientItem tmpPI) {
        this.tmpPI = tmpPI;
    }

    public double getCostOfTimed() {
        if (getPatientItems().size() > 0) {
            for (PatientItem pi : getPatientItems()) {
                costOfTimed += pi.getServiceValue();
            }
        }

        return costOfTimed;
    }

    public void setCostOfTimed(double costOfTimed) {
        this.costOfTimed = costOfTimed;
    }

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;
    }

    public List<ChargeItemTotal> getChargeItemTotals() {
        if (chargeItemTotals == null) {
            chargeItemTotals = new ArrayList<>();

            for (InwardChargeType i : InwardChargeType.values()) {
                ChargeItemTotal cit = new ChargeItemTotal();
                cit.setInwardChargeType(i);

                chargeItemTotals.add(cit);
            }

            if (getPatientEncounter() != null) {
                setKnownChargeTot(chargeItemTotals);
                setServiceTotCategoryWise(chargeItemTotals);
                setTimedServiceTotCategoryWise(chargeItemTotals);

            }


            for (ChargeItemTotal cit : chargeItemTotals) {
                cit.setAdjustedTotal(cit.getTotal());
            }
        }
        return chargeItemTotals;
    }

    public void onEdit(RowEditEvent event) {
    }

//    private void setOtherCharge(List<ChargeItemTotal> tmp) {
//        double tmpTot = 0.0;
//
//        for (ChargeItemTotal cit : tmp) {
//            if (cit.getInwardChargeType() == InwardChargeType.AdmissionFee || cit.getInwardChargeType() == InwardChargeType.RoomCharges) {
//                continue;
//            }
//            tmpTot += cit.getTotal();
//        }
//
//        if (tmpTot != 0.0) {
//            String sql = "Select i From Bill i where i.retired=false and i.billType=:btp and i.patientEncounter.id=" + getPatientEncounter().getId();
//            HashMap m = new HashMap();
//            m.put("btp", BillType.InwardBill);
//            List<Bill> bill = getBillFacade().findBySQL(sql, m, TemporalType.DATE);
//
//            double dtot = 0.0;
//
//            for (Bill b : bill) {
//                dtot += b.getNetTotal();
//            }
//
//            if (tmpTot != dtot) {
//                for (ChargeItemTotal cit : tmp) {
//                    if (cit.getInwardChargeType() == InwardChargeType.OtherCharges) {
//                        cit.setTotal(dtot - tmpTot);
//                    }
//                }
//            }
//        }
//
//    }
    private List<Bill> getAdditionalChargeBill() {

        String sql = "Select i From Bill i where i.retired=false and i.billType=:btp "
                + "and i.patientEncounter=:pe and i.id in "
                + "(Select bf.bill.id from BillFee bf where bf.retired=false and bf.patienEncounter=:pe and bf.fee.feeType=:fn)";
        HashMap m = new HashMap();
        m.put("btp", BillType.InwardBill);
        m.put("pe", getPatientEncounter());
        m.put("fn", FeeType.Additional);
        List<Bill> bill = getBillFacade().findBySQL(sql, m, TemporalType.DATE);

        return bill;
    }

    private void calAdditionalTot() {
        double temp = 0.0;
        additionalCharge = 0.0;
        for (Bill b : getAdditionalChargeBill()) {
            temp += b.getNetTotal();
        }
        additionalCharge = temp;
    }

    private void setKnownChargeTot(List<ChargeItemTotal> tmp) {
        for (ChargeItemTotal i : tmp) {
            switch (i.getInwardChargeType()) {
                case AdmissionFee:
                    if (getPatientEncounter().getAdmissionType() != null && !getPatientEncounter().getAdmissionType().isInwardPackage()) {
                        i.setTotal(getPatientEncounter().getAdmissionType().getAdmissionFee());
                    }
                    break;              
                case RoomCharges:
                    i.setTotal(getTotalRoomCharges());
                    break;
                case MOCharges:
                    i.setTotal(getTotalMOCharge());
                    break;
                case NursingCharges:
                    i.setTotal(getTotalNursing());
                    break;
                case LinenCharges:
                    i.setTotal(getTotalLinen());
                    break;
                case MaintainCharges:
                    i.setTotal(getTotalMaintanance());
                    break;
                case Medicine:
                    i.setTotal(getCostOfMadicine());
                    break;
                case ProfessionalCharge:
                    i.setTotal(getProfessionalCharges());
                    break;
                case OtherCharges:
                    i.setTotal(getAdditionalCharge());

            }
        }
    }

    private void setServiceTotCategoryWise(List<ChargeItemTotal> tmp) {
        for (BillItem b : getService()) {
            for (ChargeItemTotal ch : tmp) {
                if (b.getItem().getInwardChargeType() != null) {
                    if (b.getItem().getInwardChargeType() == ch.getInwardChargeType()) {
                        ch.setTotal(ch.getTotal() + b.getNetValue());
                        break;
                    }
                }
            }
        }
    }

    private void setTimedServiceTotCategoryWise(List<ChargeItemTotal> tmp) {
        for (PatientItem b : getPatientItems()) {
            for (ChargeItemTotal ch : tmp) {
                if (b.getItem().getInwardChargeType() != null) {
                    if (b.getItem().getInwardChargeType() == ch.getInwardChargeType()) {
                        ch.setTotal(ch.getTotal() + b.getServiceValue());
                        break;
                    }
                }
            }
        }
    }

    public void setChargeItemTotals(List<ChargeItemTotal> chargeItemTotals) {
        this.chargeItemTotals = chargeItemTotals;
    }

    public double getTotalMOCharge() {
        if (totalMOCharge == 0.0) {
            for (RoomChargeData rcd : patientRoom) {
                totalMOCharge += rcd.getMoChargeTot();
            }
        }
        return totalMOCharge;
    }

    public void setTotalMOCharge(double totalMOCharge) {
        this.totalMOCharge = totalMOCharge;
    }

    public Bill getCurrent() {
        if (current == null) {
            current = new BilledBill();
        }
        return current;
    }

    public void setCurrent(Bill current) {
        this.current = current;
    }

    public String prepareNewBill() {
        makeNull();
        return "inward_bht_summery";
    }

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public DischargeController getDischargeController() {
        return dischargeController;
    }

    public void setDischargeController(DischargeController dischargeController) {
        this.dischargeController = dischargeController;
    }

    public boolean isPrintPreview() {
        return printPreview;
    }

    public void setPrintPreview(boolean printPreview) {
        this.printPreview = printPreview;
    }

    public InwardTimedItemController getInwardTimedItemController() {
        return inwardTimedItemController;
    }

    public void setInwardTimedItemController(InwardTimedItemController inwardTimedItemController) {
        this.inwardTimedItemController = inwardTimedItemController;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    public double getAdditionalCharge() {
        calAdditionalTot();
        return additionalCharge;
    }

    public void setAdditionalCharge(double additionalCharge) {
        this.additionalCharge = additionalCharge;
    }

    public PatientEncounterFacade getPatientEncounterFacade() {
        return patientEncounterFacade;
    }

    public void setPatientEncounterFacade(PatientEncounterFacade patientEncounterFacade) {
        this.patientEncounterFacade = patientEncounterFacade;
    }
}
