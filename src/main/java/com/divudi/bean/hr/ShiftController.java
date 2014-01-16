/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.hr;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.hr.DayType;
import com.divudi.entity.hr.DayShift;
import com.divudi.entity.hr.Roster;
import com.divudi.entity.hr.Shift;
import com.divudi.facade.DayShiftFacade;
import com.divudi.facade.RosterFacade;
import com.divudi.facade.ShiftFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class ShiftController implements Serializable {

    private Shift current;
    private DayShift currentDayShift;
    private Roster currentRoster;
    private List<DayShift> dayShifts;
    private List<Shift> shiftList;
    @EJB
    private ShiftFacade facade;
    @EJB
    private DayShiftFacade dayShiftFacade;
    @EJB
    private RosterFacade rosterFacade;
    @Inject
    private SessionController sessionController;

    public DayType[] getDayTypes() {
        return DayType.values();
    }

    public void checkShift() {
        for (DayShift ds : getCurrent().getDayShifts()) {
            if (ds.getDayType() == getCurrentDayShift().getDayType()) {
                UtilityController.addErrorMessage("There Redundant DayType not Allowed ");
                return;
            }
        }

        String sql = "Select s From Shift s Where s.retired=false and s.roster=:rs";
        HashMap hs = new HashMap();
        hs.put("rs", getCurrent().getRoster());
        List<Shift> tmp = getFacade().findBySQL(sql, hs, TemporalType.DATE);

        boolean changed = false;
        for (Shift s : tmp) {
            for (DayShift ds : s.getDayShifts()) {
                if (ds.getDayType() == getCurrentDayShift().getDayType()) {
                    getCurrentDayShift().setStartingTime(ds.getEndingTime());
                    changed = true;
                }
            }
        }

        if (!changed) {
            getCurrentDayShift().setStartingTime(getCurrent().getRoster().getStartingTime());
        }

    }

//    public boolean checkTimeLimit() {
//        String sql = "Select s From DayShift s Where s.shift.roster=:rs and s.dayType=:dt order by s.startingTime";
//        HashMap hs = new HashMap();
//        hs.put("rs", getCurrent().getRoster());
//        hs.put("dt", getCurrentDayShift().getDayType());
//        List<DayShift> tmp = getDayShiftFacade().findBySQL(sql, hs, TemporalType.DATE);
//
//        Date dt = tmp.get(0).getStartingTime();
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(dt);
//        int dt24 = cal.get(Calendar.HOUR);
//       
//        cal.setTime(getCurrentDayShift().getEndingTime());
//        int current=cal.get
//       
//
//        if (dt24 < getCurrentDayShift().getEndingTime()) {
//            // UtilityController.addErrorMessage("");
//            return true;
//        }
//
//        return false;
//    }
    private boolean errorCheck() {
        if (getCurrent().getRoster() == null) {
            UtilityController.addErrorMessage("Select Roster");
            return true;
        }

        if (getCurrent().getName().trim().isEmpty() && getCurrent().getName().equals("")) {
            UtilityController.addErrorMessage("Enter Name");
            return true;
        }

        if (getCurrentDayShift().getDayType() == null) {
            UtilityController.addErrorMessage("Select Day Type");
            return true;
        }

        if (getCurrentDayShift().getStartingTime() == null) {
            UtilityController.addErrorMessage("Set Start Time");
            return true;
        }

        if (getCurrentDayShift().getEndingTime() == null) {
            UtilityController.addErrorMessage("Set End Time");
            return true;
        }
        if (getCurrentDayShift().getCount() == 0) {
            UtilityController.addErrorMessage("Set Staff count correctly");
            return true;
        }

//        if(getCurrentDayShift().getRepeatedDay()!=0 && getCurrentDayShift().isDayOff()){
//            UtilityController.addErrorMessage("Repeated day & dayoff can't active at Same  time");
//            return true;
//        }
//        if (checkTimeLimit()) {
//            UtilityController.addErrorMessage("You Cant add more than 24h per Roster");
//            return true;
//        }
        return false;
    }

    public void addDayShift() {
        if (errorCheck()) {
            return;
        }
        getCurrentDayShift().setShift(getCurrent());
        // getCurrent().getDayShifts().add(getCurrentDayShift());

        if (getCurrent().getId() == null || getCurrent().getId() == 0) {
            getFacade().create(getCurrent());
        } else {
            getFacade().edit(getCurrent());
        }

        if (getCurrentDayShift().getId() == null) {
            getDayShiftFacade().create(getCurrentDayShift());
        } else {
            getDayShiftFacade().edit(getCurrentDayShift());
        }

        //     getCurrentRoster().getShiftList().add(getCurrent());
        //     getRosterFacade().edit(getCurrentRoster());
        currentDayShift = new DayShift();
//        current = null;
//        currentRoster = null;
    }

    public void removeDayShift() {

        getCurrent().getDayShifts().remove(getCurrentDayShift());
        getCurrentDayShift().setShift(null);
        getDayShiftFacade().edit(getCurrentDayShift());
        if (getCurrent().getId() == null) {
            getFacade().create(getCurrent());
        } else {
            getFacade().edit(getCurrent());
        }

        //   getFacade().
        currentDayShift = null;

    }

    public List<Shift> completeShift(String qry) {
        List<Shift> a = null;
        if (qry != null) {
            a = getFacade().findBySQL("select c from Shift c where c.retired=false and upper(c.name) like '%" + qry.toUpperCase() + "%' order by c.name");
        }
        if (a == null) {
            a = new ArrayList<>();
        }
        return a;
    }

    public void saveSelected() {

        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("savedOldSuccessfully");
        } else {
            current.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setCreater(sessionController.getLoggedUser());
            getFacade().create(current);
            UtilityController.addSuccessMessage("savedNewSuccessfully");
        }

        recreateModel();

        current = null;
    }

    public ShiftController() {
    }

    public void prepareAdd() {
        current = null;
        currentDayShift = null;
    }

    private void recreateModel() {
        currentRoster = null;
    }

    public void delete() {

        if (current != null) {
//            // removeAll();
//            current.setRetired(true);
//            current.setRetiredAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
//            current.setRetirer(sessionController.getLoggedUser());

            getFacade().remove(current);
            getCurrentRoster().getShiftList().remove(getCurrent());

            getRosterFacade().edit(getCurrentRoster());
            UtilityController.addSuccessMessage("DeleteSuccessfull");
        } else {
            UtilityController.addSuccessMessage("NothingToDelete");
        }
        //   recreateModel();

        current = null;

    }

    public Shift getCurrent() {
        if (current == null) {
            current = new Shift();
            current.setRoster(getCurrentRoster());
        }
        return current;
    }

    public void setCurrent(Shift current) {
        this.current = current;

    }

    public ShiftFacade getFacade() {
        return facade;
    }

    public void setFacade(ShiftFacade facade) {
        this.facade = facade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public DayShift getCurrentDayShift() {
        if (currentDayShift == null) {
            currentDayShift = new DayShift();
        }
        return currentDayShift;
    }

    public void setCurrentDayShift(DayShift currentDayShift) {
        this.currentDayShift = currentDayShift;
    }

    public DayShiftFacade getDayShiftFacade() {
        return dayShiftFacade;
    }

    public void setDayShiftFacade(DayShiftFacade dayShiftFacade) {
        this.dayShiftFacade = dayShiftFacade;
    }

    public Roster getCurrentRoster() {
        return currentRoster;
    }

    public void setCurrentRoster(Roster currentRoster) {
        current = null;
        this.currentRoster = currentRoster;
    }

    public RosterFacade getRosterFacade() {
        return rosterFacade;
    }

    public void setRosterFacade(RosterFacade rosterFacade) {
        this.rosterFacade = rosterFacade;
    }

    public List<DayShift> getDayShifts() {
        String sql = "Select s From DayShift s where s.shift=:cur";
        HashMap hm = new HashMap();
        hm.put("cur", getCurrent());
        dayShifts = getDayShiftFacade().findBySQL(sql, hm);
        return dayShifts;
    }

    public void setDayShifts(List<DayShift> dayShifts) {
        this.dayShifts = dayShifts;
    }

    public List<Shift> getShiftList() {
        String sql = "Select s From Shift s where s.retired=false and s.roster=:rs ";
        HashMap hm = new HashMap();
        hm.put("rs", getCurrentRoster());

        return getFacade().findBySQL(sql, hm);
    }

    public void setShiftList(List<Shift> shiftList) {
        this.shiftList = shiftList;
    }

    @FacesConverter(forClass = Shift.class)
    public static class ShiftConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ShiftController controller = (ShiftController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "shiftController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Shift) {
                Shift o = (Shift) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + ShiftController.class.getName());
            }
        }
    }

    @FacesConverter("shift")
    public static class ShiftControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ShiftController controller = (ShiftController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "shiftController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Shift) {
                Shift o = (Shift) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + ShiftController.class.getName());
            }
        }
    }

}
