/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.hr;

import com.divudi.bean.UtilityController;
import com.divudi.data.hr.LeaveType;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.HumanResourceBean;
import com.divudi.entity.hr.Grade;
import com.divudi.entity.hr.StaffLeave;
import com.divudi.facade.StaffLeaveFacade;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Named
@ViewScoped
public class StaffLeaveController implements Serializable {

    private StaffLeave current;
    @EJB
    private StaffLeaveFacade staffLeaveFacade;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private HumanResourceBean humanResourceBean;

    public List<StaffLeave> getItems() {

        return getHumanResourceBean().getStaffLeave(getCurrent().getStaff(),
                getCommonFunctions().getFirstDayOfYear(getCurrent().getLeaveDate()),
                getCommonFunctions().getLastDayOfYear(getCurrent().getLeaveDate()));
    }

    public LeaveType[] getLeaveType() {
        return LeaveType.values();
    }

    private boolean errorCheck() {
        if (getCurrent().getLeaveType() == null) {
            UtilityController.addErrorMessage("Please select Leave Type");
            return true;
        }

        return false;
    }

    public void save() {
        if (errorCheck()) {
            return;
        }
        
        getStaffLeaveFacade().create(getCurrent());
        current = null;
        UtilityController.addSuccessMessage("Staff Leave Added");
    }

    public void clear() {
        current = null;
    }

    /**
     * Creates a new instance of StaffLeaveController
     */
    public StaffLeaveController() {
    }

    public StaffLeave getCurrent() {
        if (current == null) {
            current = new StaffLeave();
            current.setLeaveDate(new Date());
        }
        return current;
    }

    public void setCurrent(StaffLeave current) {
        this.current = current;
    }

    public StaffLeaveFacade getStaffLeaveFacade() {
        return staffLeaveFacade;
    }

    public void setStaffLeaveFacade(StaffLeaveFacade staffLeaveFacade) {
        this.staffLeaveFacade = staffLeaveFacade;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public HumanResourceBean getHumanResourceBean() {
        return humanResourceBean;
    }

    public void setHumanResourceBean(HumanResourceBean humanResourceBean) {
        this.humanResourceBean = humanResourceBean;
    }

    @FacesConverter(forClass = StaffLeave.class)
    public static class LeaveConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            StaffLeaveController controller = (StaffLeaveController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "staffLeaveController");
            return controller.getStaffLeaveFacade().find(getKey(value));
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
            if (object instanceof Grade) {
                Grade o = (Grade) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + StaffLeaveController.class.getName());
            }
        }
    }

}
