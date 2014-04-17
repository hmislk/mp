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
import com.divudi.ejb.InwardBean;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.PatientItem;
import com.divudi.entity.inward.TimedItem;
import com.divudi.entity.inward.TimedItemFee;
import com.divudi.facade.PatientItemFacade;
import com.divudi.facade.TimedItemFeeFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
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
    /////////
    @EJB
    private PatientItemFacade patientItemFacade;
    @EJB
    private TimedItemFeeFacade timedItemFeeFacade;
    /////////////////
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private InwardBean inwardBean;

    public void makeNull() {
        items = null;
        current = null;
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
        TimedItemFee timedItemFee = getInwardBean().getTimedItemFee((TimedItem) getCurrent().getItem());
        double count = getInwardBean().calCount(timedItemFee, getCurrent().getFromTime(), getCurrent().getToTime());

        getCurrent().setServiceValue(count * timedItemFee.getFee());
        getCurrent().setCreater(getSessionController().getLoggedUser());
        getCurrent().setCreatedAt(Calendar.getInstance().getTime());
        getPatientItemFacade().create(getCurrent());

        PatientEncounter tmp = getCurrent().getPatientEncounter();
        current = new PatientItem();
        current.setPatientEncounter(tmp);

        createPatientItems();
    }

    public void finalizeService(PatientItem tmpPI) {
        if (tmpPI.getToTime() != null) {
            if (tmpPI.getToTime().before(tmpPI.getFromTime())) {
                UtilityController.addErrorMessage("Service Not Finalize check Service Start Time & End Time");
                return;
            }
        }

        if (tmpPI.getToTime() == null) {
            tmpPI.setToTime(Calendar.getInstance().getTime());
        }

        TimedItemFee timedItemFee = getInwardBean().getTimedItemFee((TimedItem) tmpPI.getItem());
        double count = getInwardBean().calCount(timedItemFee, tmpPI.getFromTime(), tmpPI.getToTime());

        tmpPI.setServiceValue(count * timedItemFee.getFee());

        getPatientItemFacade().edit(tmpPI);

        createPatientItems();

    }

    public void createPatientItems() {
        String sql = "SELECT i FROM PatientItem i where type(i.item)=TimedItem "
                + " and i.retired=false and i.patientEncounter=:pe";
        HashMap hm = new HashMap();
        hm.put("pe", getCurrent().getPatientEncounter());
        items = getPatientItemFacade().findBySQL(sql, hm);

        if (items == null) {
            items = new ArrayList<>();
        }

        for (PatientItem pi : items) {
            TimedItemFee timedItemFee = getInwardBean().getTimedItemFee((TimedItem) pi.getItem());
            double count = getInwardBean().calCount(timedItemFee, pi.getFromTime(), pi.getToTime());
            pi.setServiceValue(count * timedItemFee.getFee());
        }
    }

    public List<PatientItem> getItems() {
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

    public InwardBean getInwardBean() {
        return inwardBean;
    }

    public void setInwardBean(InwardBean inwardBean) {
        this.inwardBean = inwardBean;
    }

}
