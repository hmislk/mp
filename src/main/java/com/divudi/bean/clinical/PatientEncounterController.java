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
import com.divudi.entity.clinical.ClinicalFindingItem;
import com.divudi.entity.clinical.ClinicalFindingValue;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.lab.Investigation;
import java.util.TimeZone;
import com.divudi.facade.PatientEncounterFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.ejb.EJB;

import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 Informatics)
 */
@Named
@SessionScoped
public class PatientEncounterController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private PatientEncounterFacade ejbFacade;
    List<PatientEncounter> selectedItems;
    private PatientEncounter current;
    private List<PatientEncounter> items = null;
    String selectText = "";

    ClinicalFindingItem diagnosis;
    String diagnosisComments;
    Investigation investigation;
    
    ClinicalFindingValue removingCfv;
    
    public void addDx(){
        if(diagnosis==null){
            UtilityController.addErrorMessage("Please select a diagnosis");
            return;
        }
        if(current==null){
            UtilityController.addErrorMessage("Please select a visit");
            return;
        }
        ClinicalFindingValue dx=new ClinicalFindingValue();
        dx.setItemValue(diagnosis);
        dx.setClinicalFindingItem(diagnosis);
        dx.setEncounter(current);
        dx.setPerson(current.getPatient().getPerson());
        dx.setStringValue(diagnosis.getName());
        dx.setLobValue(diagnosisComments);
        current.getClinicalFindingValues().add(dx);
        getFacade().edit(current);
        diagnosis=new ClinicalFindingItem();
        diagnosisComments = "";
        UtilityController.addSuccessMessage("Diagnosis added");
        current=getFacade().find(current.getId());
    }
    
    
    public List<PatientEncounter> completeAgency(String query) {
        List<PatientEncounter> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            Map m = new HashMap();
            sql = "select p from PatientEncounter p where p.retired=false and p.institutionType=:it and ((upper(p.name) like '%" + query.toUpperCase() + "%') or (upper(p.institutionCode) like '%" + query.toUpperCase() + "%') ) order by p.name";
            //System.out.println(sql);
            suggestions = getFacade().findBySQL(sql, m);
        }
        return suggestions;
    }
    
    public void removeCfv(){
        if(current==null){
            UtilityController.addErrorMessage("No Patient Encounter");
            return;
        }
        if(removingCfv==null){
            UtilityController.addErrorMessage("No Finding selected to remove");
            return;
        }
        current.getClinicalFindingValues().remove(removingCfv);
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
        // items = getFacade().findAll("name", true);
        String sql = "SELECT i FROM PatientEncounter i where i.retired=false and i.institutionType = com.divudi.data.PatientEncounterType.Agency order by i.name";
        items = getEjbFacade().findBySQL(sql);
        if (items == null) {
            items = new ArrayList<PatientEncounter>();
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
    
    
    
    
    
    /**
     *
     */
}
