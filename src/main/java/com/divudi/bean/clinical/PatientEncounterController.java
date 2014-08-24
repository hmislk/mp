/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.clinical;

import com.divudi.bean.*;
import com.divudi.bean.pharmacy.PharmacySaleController;
import com.divudi.data.inward.PatientEncounterType;
import com.divudi.entity.Department;
import com.divudi.entity.Doctor;
import com.divudi.entity.Institution;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.clinical.ClinicalFindingItem;
import com.divudi.entity.clinical.ClinicalFindingValue;
import com.divudi.entity.lab.Investigation;
import com.divudi.facade.ClinicalFindingItemFacade;
import com.divudi.facade.PatientEncounterFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class PatientEncounterController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private PatientEncounterFacade ejbFacade;
    @EJB
    ClinicalFindingItemFacade clinicalFindingItemFacade;
    List<PatientEncounter> selectedItems;
    private PatientEncounter current;
    private List<PatientEncounter> items = null;
    List<PatientEncounter> currentPatientEncounters;
    String selectText = "";

    ClinicalFindingItem diagnosis;
    String diagnosisComments;
    Investigation investigation;

    ClinicalFindingValue removingCfv;
    @Inject
    PharmacySaleController pharmacySaleController;

    Date fromDate;
    Date toDate;
    Institution institution;
    Department department;
    Doctor doctor;

    public String listAllEncounters() {
        String jpql;
        Map m = new HashMap();
        jpql = "select pe from PatientEncounter pe where pe.retired=false and pe.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);
        if (institution != null) {
            jpql = jpql + " and pe.department.institution=:ins ";
            m.put("ins", institution);

        } else if (department != null) {
            jpql = jpql + " and pe.department=:dep ";
            m.put("dep", department);
        }
        if (doctor != null) {
            jpql = jpql + " and pe.opdDoctor=:doc ";
            m.put("doc", doctor);
        }
        //System.out.println("m = " + m);
        //System.out.println("sql = " + jpql);
        items = getFacade().findBySQL(jpql, m);
        return "/clinical/clinical_reports_all_opd_visits";
    }

    public void addDx() {
        if (diagnosis == null) {
            UtilityController.addErrorMessage("Please select a diagnosis");
            return;
        }
        if (current == null) {
            UtilityController.addErrorMessage("Please select a visit");
            return;
        }
        ClinicalFindingValue dx = new ClinicalFindingValue();
        dx.setItemValue(diagnosis);
        dx.setClinicalFindingItem(diagnosis);
        dx.setEncounter(current);
        dx.setPerson(current.getPatient().getPerson());
        dx.setStringValue(diagnosis.getName());
        dx.setLobValue(diagnosisComments);
        current.getClinicalFindingValues().add(dx);
        getFacade().edit(current);
        diagnosis = new ClinicalFindingItem();
        diagnosisComments = "";
        UtilityController.addSuccessMessage("Diagnosis added");
        current = getFacade().find(current.getId());
    }

    public List<PatientEncounter> getCurrentPatientEncounters() {
        return currentPatientEncounters;
    }

    public void setCurrentPatientEncounters(List<PatientEncounter> currentPatientEncounters) {
        this.currentPatientEncounters = currentPatientEncounters;
    }

    public void fillCurrentPatientEncounters() {
        Map m = new HashMap();
        m.put("p", current.getPatient());
        m.put("pe", current);
        String sql;
        sql = "Select e from PatientEncounter e where e.patient=:p and e!=:pe order by e.id desc";
        currentPatientEncounters = getFacade().findBySQL(sql, m);
    }

    public void removeCfv() {
        if (current == null) {
            UtilityController.addErrorMessage("No Patient Encounter");
            return;
        }
        if (removingCfv == null) {
            UtilityController.addErrorMessage("No Finding selected to remove");
            return;
        }
        current.getClinicalFindingValues().remove(removingCfv);
        saveSelected();
        UtilityController.addSuccessMessage("Removed");
    }

    public ClinicalFindingItem getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(ClinicalFindingItem diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Investigation getInvestigation() {
        return investigation;
    }

    public void setInvestigation(Investigation investigation) {
        this.investigation = investigation;
    }

    public List<PatientEncounter> getSelectedItems() {
        selectedItems = getFacade().findBySQL("select c from PatientEncounter c where c.retired=false and i.institutionType = com.divudi.data.PatientEncounterType.Agency and upper(c.name) like '%" + getSelectText().toUpperCase() + "%' order by c.name");
        return selectedItems;
    }

    public void prepareAdd() {
        current = new PatientEncounter();
    }

    public void setSelectedItems(List<PatientEncounter> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    private void recreateModel() {
        items = null;
    }

    public void saveSelected() {
        current.setDateTime(new Date());
        current.setDepartment(sessionController.getDepartment());
        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("savedOldSuccessfully");
        } else {

            current.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
            UtilityController.addSuccessMessage("savedNewSuccessfully");
        }
        recreateModel();
        getItems();
    }

    public String issueItems() {
        if (current == null) {
            return "";
        }
        getPharmacySaleController().setSearchedPatient(current.getPatient());
//        getPharmacySaleController().getBill().setPatientEncounter(current);
//        getPharmacySaleController().getBill().setPatient(current.getPatient());
        return "/clinical/clinical_pharmacy_sale";
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public PatientEncounterFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(PatientEncounterFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public PatientEncounterController() {
    }

    public PatientEncounter getCurrent() {
        return current;
    }

    public void setCurrent(PatientEncounter current) {
        this.current = current;
        fillCurrentPatientEncounters();
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("DeleteSuccessfull");
        } else {
            UtilityController.addSuccessMessage("NothingToDelete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    private PatientEncounterFacade getFacade() {
        return ejbFacade;
    }

    public List<PatientEncounter> getItems() {
        Map m = new HashMap();
        m.put("pet", PatientEncounterType.OpdVisit);
        String sql = "SELECT i FROM PatientEncounter i where i.retired=false order by i.id desc";
        items = getEjbFacade().findBySQL(sql);
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public String getDiagnosisComments() {
        return diagnosisComments;
    }

    public void setDiagnosisComments(String diagnosisComments) {
        this.diagnosisComments = diagnosisComments;
    }

    public ClinicalFindingValue getRemovingCfv() {
        return removingCfv;
    }

    public void setRemovingCfv(ClinicalFindingValue removingCfv) {
        this.removingCfv = removingCfv;
    }

    public PharmacySaleController getPharmacySaleController() {
        return pharmacySaleController;
    }

    public void setPharmacySaleController(PharmacySaleController pharmacySaleController) {
        this.pharmacySaleController = pharmacySaleController;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = new Date();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = new Date();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public ClinicalFindingItemFacade getClinicalFindingItemFacade() {
        return clinicalFindingItemFacade;
    }

    public void setClinicalFindingItemFacade(ClinicalFindingItemFacade clinicalFindingItemFacade) {
        this.clinicalFindingItemFacade = clinicalFindingItemFacade;
    }



}
