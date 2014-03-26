/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.Title;
import com.divudi.data.inward.AdmissionTypeEnum;
import com.divudi.data.inward.InwardChargeType;
import com.divudi.data.inward.PatientEncounterComponentType;
import com.divudi.entity.PaymentScheme;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.inject.Inject;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class EnumController implements Serializable {

    private PaymentScheme paymentScheme;

    public Title[] getTitle() {
        return Title.values();
    }

    public Sex[] getSex() {
        return Sex.values();
    }

    public PaymentMethod[] getPaymentMethodForAdmission() {
        PaymentMethod[] tmp = {PaymentMethod.Cash, PaymentMethod.Credit};
        return tmp;
    }

    public InwardChargeType[] getInwardChargeTypes() {
        return InwardChargeType.values();
    }

    public PatientEncounterComponentType[] getPatientEncounterComponentTypes() {
        return PatientEncounterComponentType.values();
    }

    public BillType[] getCashFlowBillTypes() {
        BillType[] b = {
            BillType.OpdBill,
            BillType.PaymentBill,
            BillType.PettyCash,
            BillType.CashRecieveBill,
            BillType.AgentPaymentReceiveBill,
            BillType.InwardPaymentBill,
            BillType.PharmacySale,};

        return b;
    }

    public BillType[] getPharmacyBillTypes() {
        BillType[] b = {
            BillType.PharmacyGrnBill,
            BillType.PharmacyGrnReturn,
            BillType.PharmacyOrder,
            BillType.PharmacyOrderApprove,
            BillType.PharmacyPre,
            BillType.PharmacyPurchaseBill,
            BillType.PharmacySale,
            BillType.PharmacyAdjustment,
            BillType.PurchaseReturn,
            BillType.GrnPayment,
            BillType.PharmacyTransferRequest,
            BillType.PharmacyTransferIssue,};

        return b;
    }

    public PaymentMethod[] getPaymentMethods() {
        return PaymentMethod.values();
    }

    public PaymentMethod[] getPaymentMethodsForPo() {
        PaymentMethod[] p = {PaymentMethod.Cash, PaymentMethod.Credit};

        return p;
    }

    public boolean checkPaymentScheme(PaymentScheme scheme, String paymentMathod) {
        if (scheme != null && scheme.getPaymentMethod() != null) {
            //System.err.println("Payment Scheme : " + scheme.getPaymentMethod());
            //System.err.println("Payment Method : " + PaymentMethod.valueOf(paymentMathod));
            if (scheme.getPaymentMethod().equals(PaymentMethod.valueOf(paymentMathod))) {
                //System.err.println("Returning True");
                return true;
            } else {
                return false;
            }
        }

        return false;

    }

    public boolean checkPaymentScheme(String paymentMathod) {
        if (getPaymentScheme() != null && getPaymentScheme().getPaymentMethod() != null) {
            //System.err.println("Payment Scheme : " +getPaymentScheme().getPaymentMethod());
            //System.err.println("Payment Method : " + PaymentMethod.valueOf(paymentMathod));
            if (getPaymentScheme().getPaymentMethod().equals(PaymentMethod.valueOf(paymentMathod))) {
                //System.err.println("Returning True");
                return true;
            } else {
                return false;
            }
        }

        return false;

    }

    public boolean checkPaymentMethod(PaymentMethod paymentMethod, String paymentMathodStr) {
        if (paymentMethod != null) {
            //System.err.println("Payment method : " + paymentMethod);
            //System.err.println("Payment Method String : " + PaymentMethod.valueOf(paymentMathodStr));
            if (paymentMethod.equals(PaymentMethod.valueOf(paymentMathodStr))) {
                //System.err.println("Returning True");
                return true;
            } else {
                return false;
            }
        }

        return false;

    }

    public AdmissionTypeEnum[] getAdmissionTypeEnum() {
        return AdmissionTypeEnum.values();
    }

    /**
     * Creates a new instance of EnumController
     */
    public EnumController() {
    }

    public PaymentScheme getPaymentScheme() {
        return paymentScheme;
    }

    public void setPaymentScheme(PaymentScheme paymentScheme) {
        this.paymentScheme = paymentScheme;
    }

}
