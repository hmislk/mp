
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data;

/**
 *
 * @author Buddhika
 */
public enum BillType {

    LabBill,
    PaymentBill,
    OpdBill,
    InwardPaymentBill,
    InwardBill,
    AdmissionBill,
    CashRecieveBill,
    PettyCash,
    AgentPaymentReceiveBill,
    @Deprecated
    PharmacyBill, //Cash In
    PharmacySale,
    @Deprecated
    SandryGrn,
    PharmacyIssue,
    PharmacyPre,
    PharmacyOrder,
    PharmacyOrderApprove,
    PharmacyGrnBill,//Cash out
    PharmacyGrnReturn,
    GrnPayment,
    PharmacyPurchaseBill, //Cash out
    PurchaseReturn,// Purchase Return
    PharmacyTransferRequest,
    PharmacyTransferIssue,
    PharmacyTransferReceive,
    PharmacyAdjustment,
    PharmacyMajorAdjustment,
    ChannelPaid,
    ChannelCredit,
    ChannelProPayment,
    gpBooking,
    gpSettling,;

    public String getLabel() {
        switch (this) {
            case OpdBill:
                return "OPD ";
            case PaymentBill:
                return "Professional Pay ";
            case PettyCash:
                return "Petty Cash ";
            case CashRecieveBill:
                return "Credit Company Payment ";
            case AgentPaymentReceiveBill:
                return "Agent Payment";
            case InwardPaymentBill:
                return "Inward Payment";
            case PharmacyOrder:
                return "Purchase Order Request";
            case PharmacyOrderApprove:
                return "Purchase Order Aproved";
            case PharmacyGrnBill:
                return "Good Receive Note";
            case PharmacyGrnReturn:
                return "Good Receive Note Return";
            case PharmacyPurchaseBill:
                return "Pharmacy Purchase";
            case PurchaseReturn:
                return "Pharmacy Purchase Return";
            case PharmacySale:
                return "Pharmacy Sale Bill";
            case PharmacyPre:
                return "Pharmacy Pre Bill";
            case PharmacyAdjustment:
                return "Pharmacy Adjustment Bill";
            case GrnPayment:
                return "Grn Payment";

        }

        return "";
    }
}
