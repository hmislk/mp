/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data;

/**
 *
 * @author www.divudi.com
 */
public enum Privileges {
    
    //Main Menu Privileges
    
    Opd,
    Inward,
    Lab,
    Pharmacy,
    Payment,
    Hr,
    Reports,
    User,
    Admin,
    Channelling,
    Clinical,
    
    //Submenu Privileges
    OpdBilling,
    OpdBillSearch,
    OpdBillItemSearch,
    OpdReprint,
    OpdCancel,
    OpdReturn,
    OpdReactivate,
    
    
    InwardBilling,
    InwardBillSearch,
    InwardBillItemSearch,
    InwardBillReprint,
    InwardCancel,
    InwardReturn,
    InwardReactivate,
    
    LabBilling,
    LabBillSearch,
    LabBillItemSearch,
    LabBillCancelling,
    LabBillReturning,
    LabBillReprint,
    LabBillRefunding,
    LabBillReactivating,
    LabSampleCollecting,
    LabSampleReceiving,
    LabReportFormatEditing,
    LabDataentry,
    LabAutherizing,
    LabDeAutherizing,
    LabPrinting,
    LabReprinting,
    LabSummeriesLevel1,
    LabSummeriesLevel2,
    LabSummeriesLevel3,
    LabReportSearchOwn,
    LabReportSearchAll,    
    LabReceive,        
    LabEditPatient,    
    
    
    PaymentBilling,
    PaymentBillSearch,
    PaymentBillReprint,
    PaymentBillCancel,
    PaymentBillRefund,
    PaymentBillReactivation,
    
    ReportsSearchCashCardOwn,
    ReportsSearchCreditOwn,    
    ReportsItemOwn,
    ReportsSearchCashCardOther,    
    ReportSearchCreditOther,
    ReportsItemOther,
    
    
    PharmacyOrderCreation,
    PharmacyOrderApproval,
    PharmacyOrderCancellation,
    
    PharmacySale,
    PharmacySaleReprint,
    PharmacySaleCancel,
    PharmacySaleReturn,
    
    PharmacyInwardBilling,
    PharmacyInwardBillingCancel,
    PharmacyInwardBillingReturn,
    
    PharmacyGoodReceive,
    PharmacyGoodReceiveCancel,
    PharmacyGoodReceiveReturn,
    
    PharmacyPurchase,
    PharmacyPurchaseReprint,
    PharmacyPurchaseCancellation,
    PharmacyPurchaseReturn,
    PharmacyAdministration,
    PharmacyStockAdjustment,
    PharmacyReAddToStock,
    
    ClinicalPatientSummery,
    ClinicalPatientDetails,
    ClinicalPatientPhoto,
    ClinicalVisitDetail,
    ClinicalVisitSummery,
    ClinicalHistory,
    ClinicalAdministration,
    
    
    
    
    
    ChannelAdd,
    ChannelCancel,
    ChannelRefund,
    ChannelReturn,
    ChannelView,
    ChannelDoctorPayments,
    ChannelDoctorPaymentCancel,
    ChannelViewHistory,
    ChannelCreateSessions,
    ChannelManageSessions,
    ChannelAdministration,
    ChannelAgencyReports,
    
    
    
    
    
    AdminManagingUsers,
    AdminInstitutions,
    AdminStaff,
    AdminItems,
    AdminPrices,
    ChangeProfessionalFee,
    ChangeCollectingCentre,
}
