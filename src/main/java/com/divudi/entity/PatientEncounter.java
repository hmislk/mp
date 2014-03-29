/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity;

//import ch.lambdaj.Lambda;
import com.divudi.data.inward.PatientEncounterType;
import com.divudi.data.PaymentMethod;
import com.divudi.entity.clinical.ClinicalFindingValue;
import com.divudi.entity.inward.AdmissionType;
import com.divudi.entity.inward.EncounterComponent;
import com.divudi.entity.inward.PatientRoom;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.Transient;
//import org.hamcrest.Matchers;

/**
 *
 * @author buddhika
 */
@Entity
public class PatientEncounter implements Serializable {
//    @OneToMany(mappedBy = "patientEncounter",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
//     List<PatientRoom> patientRooms;

    @OneToMany(mappedBy = "patientEncounter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<EncounterComponent> encounterComponents;

    static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //Main Properties   
    Long id;
    String bhtNo;
    long bhtLong;
    @ManyToOne
    Patient patient;
    @ManyToOne
    Person guardian;
    @ManyToOne
    private PatientRoom currentPatientRoom;
    @ManyToOne
    private Item item;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fromTime;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date toTime;
    //Created Properties
    @ManyToOne
    WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date createdAt;
    //Retairing properties
    boolean retired;
    @ManyToOne
    WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date retiredAt;
    String retireComments;
    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;
    @ManyToOne
    Institution creditCompany;
    @ManyToOne
    Doctor referringDoctor;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date dateOfAdmission;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date timeOfAdmission;
    @ManyToOne
    AdmissionType admissionType;
    Boolean discharged = false;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date timeOfDischarge;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date dateOfDischarge;
    double creditLimit;
    double creditUsedAmount;
    @Enumerated(EnumType.STRING)
    PatientEncounterType patientEncounterType;
    @OneToMany(mappedBy = "parentEncounter")
    List<PatientEncounter> childEncounters;
    @OneToMany(mappedBy = "encounter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<ClinicalFindingValue> clinicalFindingValues;
    String name;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date dateTime;
    @ManyToOne
    PatientEncounter parentEncounter;
    @ManyToOne
    BillSession billSession;
    private boolean paymentFinalized;
    String referanceNo;
    String policyNo;

//    @OneToOne
//    PatientRoom lastPatientRoom;
    @Transient
    List<ClinicalFindingValue> diagnosis;

    @Transient
    List<ClinicalFindingValue> investigations;

    @Transient
    List<ClinicalFindingValue> symptoms;

    @Transient
    List<ClinicalFindingValue> signs;

    @Transient
    List<ClinicalFindingValue> procedures;

    @Transient
    List<ClinicalFindingValue> plans;

    public List<ClinicalFindingValue> getDiagnosis() {
        if (diagnosis == null) {
//            diagnosis = Lambda.filter(Lambda.having(Lambda.on(ClinicalFindingValue.class).getClinicalFindingItem().getSymanticType(), Matchers.equalTo(SymanticType.Disease_or_Syndrome)), getClinicalFindingValues());
        }
        return diagnosis;
    }

    public void setDiagnosis(List<ClinicalFindingValue> diagnosis) {
        this.diagnosis = diagnosis;
    }

    public List<ClinicalFindingValue> getInvestigations() {
        if (investigations == null) {
//            investigations = Lambda.filter(Lambda.having(Lambda.on(ClinicalFindingValue.class).getClinicalFindingItem().getSymanticType(), Matchers.equalTo(SymanticType.Laboratory_Procedure)), getClinicalFindingValues());
        }
        return investigations;
    }

    public void setInvestigations(List<ClinicalFindingValue> investigations) {
        this.investigations = investigations;
    }

    public List<ClinicalFindingValue> getSymptoms() {
        if (symptoms == null) {
//            symptoms = Lambda.filter(Lambda.having(Lambda.on(ClinicalFindingValue.class).getClinicalFindingItem().getSymanticType(), Matchers.equalTo(SymanticType.Symptom)), getClinicalFindingValues());
        }
        return symptoms;
    }

    public void setSymptoms(List<ClinicalFindingValue> symptoms) {
        this.symptoms = symptoms;
    }

    public List<ClinicalFindingValue> getSigns() {
        if (signs == null) {
//            signs = Lambda.filter(Lambda.having(Lambda.on(ClinicalFindingValue.class).getClinicalFindingItem().getSymanticType(), Matchers.equalTo(SymanticType.Sign)), getClinicalFindingValues());
        }
        return signs;
    }

    public void setSigns(List<ClinicalFindingValue> signs) {
        this.signs = signs;
    }

    public List<ClinicalFindingValue> getProcedures() {
        if (procedures == null) {
//            procedures = Lambda.filter(Lambda.having(Lambda.on(ClinicalFindingValue.class).getClinicalFindingItem().getSymanticType(), Matchers.equalTo(SymanticType.Therapeutic_Procedure)), getClinicalFindingValues());
        }
        return procedures;
    }

    public void setProcedures(List<ClinicalFindingValue> procedures) {
        this.procedures = procedures;
    }

    public List<ClinicalFindingValue> getPlans() {
        if (plans == null) {
//            plans = Lambda.filter(Lambda.having(Lambda.on(ClinicalFindingValue.class).getClinicalFindingItem().getSymanticType(), Matchers.equalTo(SymanticType.Preventive_Procedure)), getClinicalFindingValues());
        }
        return plans;
    }

    public void setPlans(List<ClinicalFindingValue> plans) {
        this.plans = plans;
    }

    public BillSession getBillSession() {
        return billSession;
    }

    public void setBillSession(BillSession billSession) {
        this.billSession = billSession;
    }

    public List<PatientEncounter> getChildEncounters() {
        return childEncounters;
    }

    public void setChildEncounters(List<PatientEncounter> childEncounters) {
        this.childEncounters = childEncounters;
    }

    public PatientEncounter getParentEncounter() {
        return parentEncounter;
    }

    public void setParentEncounter(PatientEncounter parentEncounter) {
        this.parentEncounter = parentEncounter;
    }

    public List<ClinicalFindingValue> getClinicalFindingValues() {
        return clinicalFindingValues;
    }

    public void setClinicalFindingValues(List<ClinicalFindingValue> clinicalFindingValues) {
        this.clinicalFindingValues = clinicalFindingValues;
    }

    public PatientEncounterType getPatientEncounterType() {
        return patientEncounterType;
    }

    public void setPatientEncounterType(PatientEncounterType patientEncounterType) {
        this.patientEncounterType = patientEncounterType;
    }

    public Institution getCreditCompany() {
        return creditCompany;
    }

    public void setCreditCompany(Institution creditCompany) {
        this.creditCompany = creditCompany;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof PatientEncounter)) {
            return false;
        }
        PatientEncounter other = (PatientEncounter) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.PatientEncounter[ id=" + id + " ]";
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getReferringDoctor() {
        return referringDoctor;
    }

    public void setReferringDoctor(Doctor referringDoctor) {
        this.referringDoctor = referringDoctor;
    }

    public long getBhtLong() {
        return bhtLong;
    }

    public void setBhtLong(long bhtLong) {
        this.bhtLong = bhtLong;
    }

    public String getBhtNo() {
        return bhtNo;
    }

    public void setBhtNo(String bhtNo) {
        this.bhtNo = bhtNo;
    }

    public Person getGuardian() {
        if (guardian == null) {
            guardian = new Person();
        }
        return guardian;
    }

    public void setGuardian(Person guardian) {
        this.guardian = guardian;
    }

    public Date getDateOfAdmission() {
        return dateOfAdmission;
    }

    public void setDateOfAdmission(Date dateOfAdmission) {
        this.dateOfAdmission = dateOfAdmission;
    }

    public Date getTimeOfAdmission() {
        return timeOfAdmission;
    }

    public void setTimeOfAdmission(Date timeOfAdmission) {
        this.timeOfAdmission = timeOfAdmission;
    }

    public AdmissionType getAdmissionType() {
        return admissionType;
    }

    public void setAdmissionType(AdmissionType admissionType) {
        this.admissionType = admissionType;
    }

    public Boolean isDischarged() {
        return discharged;
    }

    public Boolean getDischarged() {
        return discharged;
    }

    public void setDischarged(Boolean discharged) {
        this.discharged = discharged;
    }

    public Date getTimeOfDischarge() {
        return timeOfDischarge;
    }

    public void setTimeOfDischarge(Date timeOfDischarge) {
        this.timeOfDischarge = timeOfDischarge;
    }

    public Date getDateOfDischarge() {
        return dateOfDischarge;
    }

    public void setDateOfDischarge(Date dateOfDischarge) {
        this.dateOfDischarge = dateOfDischarge;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public double getCreditUsedAmount() {
        return creditUsedAmount;
    }

    public void setCreditUsedAmount(double creditUsedAmount) {
        this.creditUsedAmount = creditUsedAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public List<EncounterComponent> getEncounterComponents() {
        return encounterComponents;
    }

    public void setEncounterComponents(List<EncounterComponent> encounterComponents) {
        this.encounterComponents = encounterComponents;
    }

//    public List<PatientRoom> getPatientRooms() {
//        return patientRooms;
//    }
//
//    public void setPatientRooms(List<PatientRoom> patientRooms) {
//        this.patientRooms = patientRooms;
//    }
//    
//    public PatientRoom getLastPateintRoom(){
//        return getPatientRooms().get(getPatientRooms().size()-1);
//    }
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    public boolean isPaymentFinalized() {
        return paymentFinalized;
    }

    public void setPaymentFinalized(boolean paymentFinalized) {
        this.paymentFinalized = paymentFinalized;
    }

    public PatientRoom getCurrentPatientRoom() {
        return currentPatientRoom;
    }

    public void setCurrentPatientRoom(PatientRoom currentPatientRoom) {
        this.currentPatientRoom = currentPatientRoom;
    }

    public String getReferanceNo() {
        return referanceNo;
    }

    public void setReferanceNo(String referanceNo) {
        this.referanceNo = referanceNo;
    }

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }



}
