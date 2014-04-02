/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data.inward;

/**
 *
 * @author Buddhika
 */
public enum InwardChargeType {

    AdmissionFee,
    // @Deprecated
    // PackageFee,
    RoomCharges,
    Medicine,
    MOCharges,
    MaintainCharges,
    @Deprecated
    Investigations,
    BloodTransfusioncharges,
    Immunization,
    LinenCharges,
    NursingCharges,
    MealCharges,
    OperationTheatreCharges,
    LarbourRoomCharges,
    ETUCharges,
    MedicalCareICU,
    TreatmentCharges,
    IntensiveCareManagement,
    AmbulanceCharges,
    HomeVisiting,
    GeneralIssuing,
    WardProcedures,
    ReimbursementCharges,
    OtherCharges,
    ProfessionalCharge,
    DressingCharges,
    OxygenCharges,
    physiotherapy,
    Laboratory,
    X_Ray,
    CT,
    Scanning,
    ECG_EEG,
    MedicalServices,;

    public String getLabel() {
        switch (this) {
            case AdmissionFee:
                return "Admission Fee";
            case RoomCharges:
                return "Room Charges";
            case MOCharges:
                return "MO Charges";
            case MaintainCharges:
                return "Maintain Charges";
            case BloodTransfusioncharges:
                return "Blood Transfusion Charges";
            case LinenCharges:
                return "Linen Charges";
            case NursingCharges:
                return "Nursing Charges";
            case MealCharges:
                return "Meal Charges";
            case OperationTheatreCharges:
                return "Operation Theatre Charges";
            case LarbourRoomCharges:
                return "Larbour Room Charges";
            case ETUCharges:
                return "ETU Charges";
            case MedicalCareICU:
                return "Medical Care ICU";
            case TreatmentCharges:
                return "Treatment Charges";
            case IntensiveCareManagement:
                return "Intensive Care Management";
            case AmbulanceCharges:
                return "Ambulance Charges";
            case HomeVisiting:
                return "Home Visiting";
            case GeneralIssuing:
                return "General Issuing";
            case WardProcedures:
                return "Ward Procedures";
            case ReimbursementCharges:
                return "Reimbursement Charges";
            case OtherCharges:
                return "Other Charges";
            case ProfessionalCharge:
                return "Professional Charge";
            case DressingCharges:
                return "Dressing Charges";
            case OxygenCharges:
                return "Oxygen Charges";
            case Laboratory:
                return "Laboratory";
            case X_Ray:
                return "X-Ray";
            case CT:
                return "CT";
            case Scanning:
                return "Scanning";
            case ECG_EEG:
                return "ECG-EEG";
            case MedicalServices:
                return "Medical Services";

            default:
                return this.toString();
        }

    }
}
