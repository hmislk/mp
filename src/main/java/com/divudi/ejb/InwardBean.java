/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.data.BillType;
import com.divudi.data.FeeType;
import com.divudi.entity.Bill;
import com.divudi.entity.Fee;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.WebUser;
import com.divudi.entity.inward.InwardFee;
import com.divudi.entity.inward.PatientRoom;
import com.divudi.entity.inward.RoomFacilityCharge;
import com.divudi.facade.BillFacade;
import com.divudi.facade.FeeFacade;
import com.divudi.facade.PatientRoomFacade;
import com.divudi.facade.RoomFacade;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Stateless
public class InwardBean {

    @EJB
    private PatientRoomFacade patientRoomFacade;
    @EJB
    private RoomFacade roomFacade;
    @EJB
    private InwardCalculation inwardCalculation;
    @EJB
    private BillFacade billFacade;
    @EJB
    private FeeFacade feeFacade;

    public Fee getStaffFeeForInward(WebUser webUser) {
        String sql = "Select f From InwardFee f "
                + " where f.retired=false "
                + " and f.feeType=:st ";

        HashMap hm = new HashMap();
        hm.put("st", FeeType.Staff);

        Fee fee = getFeeFacade().findFirstBySQL(sql, hm);
        if (fee == null) {
            fee = new InwardFee();
            fee.setCreatedAt(new Date());
            fee.setCreater(webUser);
            fee.setFeeType(FeeType.Staff);
            getFeeFacade().create(fee);
        }

        return fee;

    }

    public void updateFinalFill(PatientEncounter patientEncounter) {
        String sql = "Select b From BilledBill b where b.retired=false and b.cancelled=false "
                + " and b.billType=:btp and b.patientEncounter=:pe";
        HashMap hm = new HashMap();
        hm.put("btp", BillType.InwardFinalBill);
        hm.put("pe", patientEncounter);

        Bill b = getBillFacade().findFirstBySQL(sql, hm);

        double paid = getPaidValue(patientEncounter);
//        System.err.println("NET " + b.getNetTotal());
//        System.err.println("PAID " + paid);

        b.setPaidAmount(0-paid);
        getBillFacade().edit(b);

    }

    public double getPaidValue(PatientEncounter patientEncounter) {

        HashMap hm = new HashMap();
        String sql = "SELECT  sum(b.netTotal) FROM Bill b WHERE b.retired=false  and b.billType=:btp "
                + " and b.patientEncounter=:pe ";
        hm.put("btp", BillType.InwardPaymentBill);
        hm.put("pe", patientEncounter);
        double dbl = getBillFacade().findDoubleByJpql(sql, hm, TemporalType.TIMESTAMP);

        return dbl;

    }

    public PatientRoom savePatientRoom(RoomFacilityCharge newRoomFacilityCharge, double addLinenCharge, Date addmittedAt, PatientEncounter patientEncounter, WebUser webUser) {
        PatientRoom pr = new PatientRoom();

        //  pr.setCurrentLinenCharge(newRoomFacilityCharge.getLinenCharge());
        pr.setCurrentMaintananceCharge(newRoomFacilityCharge.getMaintananceCharge());
        pr.setCurrentMoCharge(newRoomFacilityCharge.getMoCharge());
        pr.setCurrentNursingCharge(newRoomFacilityCharge.getNursingCharge());
        pr.setCurrentRoomCharge(newRoomFacilityCharge.getRoomCharge());

        pr.setAddedLinenCharge(addLinenCharge);
        pr.setAdmittedAt(addmittedAt);

        PatientRoom currentPatientRoom = getInwardCalculation().getCurrentPatientRoom(patientEncounter);

        pr.setPreviousRoom(currentPatientRoom);
        pr.setCreatedAt(Calendar.getInstance().getTime());
        pr.setCreater(webUser);
        pr.setAddmittedBy(webUser);
        pr.setPatientEncounter(patientEncounter);
        pr.setRoomFacilityCharge(newRoomFacilityCharge);
        pr.setRoom(newRoomFacilityCharge.getRoom());

        getPatientRoomFacade().create(pr);

        if (patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            makeRoomFilled(pr);
        }

        return pr;
    }

    public PatientRoom savePatientRoom(PatientRoom patientRoom, PatientEncounter patientEncounter, Date admittedAt, WebUser webUser) {
        //     patientRoom.setCurrentLinenCharge(patientRoom.getRoomFacilityCharge().getLinenCharge());
        patientRoom.setCurrentMaintananceCharge(patientRoom.getRoomFacilityCharge().getMaintananceCharge());
        patientRoom.setCurrentMoCharge(patientRoom.getRoomFacilityCharge().getMoCharge());
        patientRoom.setCurrentNursingCharge(patientRoom.getRoomFacilityCharge().getNursingCharge());
        patientRoom.setCurrentRoomCharge(patientRoom.getRoomFacilityCharge().getRoomCharge());

        patientRoom.setAddmittedBy(webUser);
        patientRoom.setAdmittedAt(admittedAt);
        patientRoom.setCreatedAt(Calendar.getInstance().getTime());
        patientRoom.setCreater(webUser);
        patientRoom.setPatientEncounter(patientEncounter);
        patientRoom.setRoom(patientRoom.getRoomFacilityCharge().getRoom());

        if (patientRoom.getId() == null || patientRoom.getId() == 0) {
            getPatientRoomFacade().create(patientRoom);
        } else {
            getPatientRoomFacade().edit(patientRoom);
        }

        if (patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            makeRoomFilled(patientRoom);
        }

        return patientRoom;
    }

    public void makeRoomFilled(PatientRoom pr) {

        pr.getRoom().setFilled(true);
        getRoomFacade().edit(pr.getRoom());

    }

    public void makeRoomVacant(PatientEncounter patientEncounter) {
        if (!patientEncounter.getAdmissionType().isRoomChargesAllowed()) {
            return;
        }

        PatientRoom pr = getInwardCalculation().getCurrentPatientRoom(patientEncounter);
        pr.getRoom().setFilled(false);
        getRoomFacade().edit(pr.getRoom());
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public PatientRoomFacade getPatientRoomFacade() {
        return patientRoomFacade;
    }

    public void setPatientRoomFacade(PatientRoomFacade patientRoomFacade) {
        this.patientRoomFacade = patientRoomFacade;
    }

    public RoomFacade getRoomFacade() {
        return roomFacade;
    }

    public void setRoomFacade(RoomFacade roomFacade) {
        this.roomFacade = roomFacade;
    }

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public FeeFacade getFeeFacade() {
        return feeFacade;
    }

    public void setFeeFacade(FeeFacade feeFacade) {
        this.feeFacade = feeFacade;
    }
}
