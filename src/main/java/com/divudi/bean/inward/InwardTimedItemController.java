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
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.InwardCalculation;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.PatientItem;
import com.divudi.facade.PatientItemFacade;
import com.divudi.facade.TimedItemFeeFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 Informatics)
 */
@Named
@SessionScoped
public class InwardTimedItemController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    private SessionController sessionController;
    //////////////////////
    private List<PatientItem> items;
    private PatientItem current;
    private PatientItem tmpPI;
    private Date toTime;
    /////////
    @EJB
    private PatientItemFacade patientItemFacade;
    @EJB
    private TimedItemFeeFacade timedItemFeeFacade;
    /////////////////
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private InwardCalculation inwardCalculation;

    public void makeNull() {
        items = null;
        current = null;
        tmpPI = null;
        toTime = null;
    }

    private boolean errorCheck() {
        if (getCurrent().getPatientEncounter() == null) {
            UtilityController.addErrorMessage("Please Select BHT");
            return true;
        }

        return false;
    }

    public void save() {
        if (errorCheck()) {
            return;
        }

        double serviceTot = getInwardCalculation().calTimedServiceCharge(getCurrent(),getToTime());
        getCurrent().setServiceValue(serviceTot);
        getCurrent().setCreater(getSessionController().getLoggedUser());
        getCurrent().setCreatedAt(Calendar.getInstance().getTime());
        getPatientItemFacade().create(getCurrent());
        PatientEncounter tmp = getCurrent().getPatientEncounter();
        current = new PatientItem();
        current.setPatientEncounter(tmp);
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

        double serviceTot = getInwardCalculation().calTimedServiceCharge(getTmpPI(),getToTime());
        getTmpPI().setServiceValue(serviceTot);

        getTmpPI().setFinalize(Boolean.TRUE);
        getPatientItemFacade().edit(tmpPI);

        setToTime(null);
    }

    public List<PatientItem> getItems() {
        if (getCurrent().getPatientEncounter() == null) {
            return new ArrayList<PatientItem>();
        }

        String sql = "SELECT i FROM PatientItem i where type(i.item)=TimedItem and i.retired=false and i.patientEncounter=:pe" ;
        HashMap hm=new HashMap();
        hm.put("pe", getCurrent().getPatientEncounter());
        items = getPatientItemFacade().findBySQL(sql,hm);

        if (items == null) {
            items = new ArrayList<PatientItem>();
        }

        for (PatientItem pi : items) {
            if (pi.getFinalize() == null) {
                double serviceTot = getInwardCalculation().calTimedServiceCharge(pi,getToTime());
                pi.setServiceValue(serviceTot);
            }
        }

        return items;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public PatientItemFacade getPatientItemFacade() {
        return patientItemFacade;
    }

    public void setPatientItemFacade(PatientItemFacade patientItemFacade) {
        this.patientItemFacade = patientItemFacade;
    }

    public PatientItem getCurrent() {
        if (current == null) {
            current = new PatientItem();
            current.setFromTime(Calendar.getInstance().getTime());
        }

        return current;
    }

    public void setCurrent(PatientItem current) {
        this.current = current;
    }

    public TimedItemFeeFacade getTimedItemFeeFacade() {
        return timedItemFeeFacade;
    }

    public void setTimedItemFeeFacade(TimedItemFeeFacade timedItemFeeFacade) {
        this.timedItemFeeFacade = timedItemFeeFacade;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public PatientItem getTmpPI() {
        return tmpPI;
    }

    public void setTmpPI(PatientItem tmpPI) {
        this.tmpPI = tmpPI;
    }

    public InwardCalculation getInwardCalculation() {
        return inwardCalculation;
    }

    public void setInwardCalculation(InwardCalculation inwardCalculation) {
        this.inwardCalculation = inwardCalculation;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }
}
