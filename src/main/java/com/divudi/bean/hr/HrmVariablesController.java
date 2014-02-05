/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.hr;

import com.divudi.bean.*;
import com.divudi.facade.HrmVariablesFacade;
import com.divudi.entity.hr.HrmVariables;
import com.divudi.entity.hr.PayeeTaxRange;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.faces.view.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@ViewScoped
public class HrmVariablesController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private HrmVariablesFacade ejbFacade;
    List<HrmVariables> selectedItems;
    private HrmVariables current;
    private PayeeTaxRange currentPayeeTaxRange;
    private List<HrmVariables> items = null;
    String selectText = "";

    public List<HrmVariables> getSelectedItems() {
        selectedItems = getFacade().findBySQL("select c from HrmVariables c where c.retired=false and upper(c.name) like '%" + getSelectText().toUpperCase() + "%' order by c.name");
        return selectedItems;
    }

    public List<HrmVariables> completeHrmVariables(String qry) {
        List<HrmVariables> a = null;
        if (qry != null) {
            a = getFacade().findBySQL("select c from HrmVariables c where c.retired=false and upper(c.name) like '%" + qry.toUpperCase() + "%' order by c.name");
        }
        if (a == null) {
            a = new ArrayList<>();
        }
        return a;
    }

    public void prepareAdd() {
        current = new HrmVariables();
    }

    public void setSelectedItems(List<HrmVariables> selectedItems) {
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

    public HrmVariablesFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(HrmVariablesFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public HrmVariablesController() {
    }

    public HrmVariables getCurrent() {
        if (getItems() != null) {
            current = getItems().get(0);
        }
        
        if (current == null) {
            current = new HrmVariables();

            //   Date dt=new Date();
            current.setName("HrmVariable");
        }
        return current;
    }

    public void setCurrent(HrmVariables current) {
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

    private HrmVariablesFacade getFacade() {
        return ejbFacade;
    }

    public List<HrmVariables> getItems() {
        items = getFacade().findAll("name", true);
        return items;
    }

    private boolean errorCheck() {
        if (getCurrentPayeeTaxRange().getFromSalary() == 0) {
            UtilityController.addErrorMessage("Set From Salary");
            return true;
        }

        if (getCurrentPayeeTaxRange().getToSalary() == 0) {
            UtilityController.addErrorMessage("Set To Salary");
            return true;
        }

        if (getCurrentPayeeTaxRange().getTaxRate() == 0) {
            UtilityController.addErrorMessage("Set Tax Rate");
            return true;
        }

        return false;
    }

    public double getEpfRate() {
        if (getItems() != null && getItems().get(0) != null) {
            return getItems().get(0).getEpfRate();
        }

        return 8.0;
    }

    public double getEpfCompanyRate() {
        if (getItems() != null && getItems().get(0) != null) {
            return getItems().get(0).getEpfCompanyRate();
        }

        return 8.0;
    }

    public double getEtfRate() {
        if (getItems() != null && getItems().get(0) != null) {
            return getItems().get(0).getEtfRate();
        }

        return 3.0;
    }

    public double getEtfCompanyRate() {
        if (getItems() != null && getItems().get(0) != null) {
            return getItems().get(0).getEtfCompanyRate();
        }

        return 3.0;
    }

    public void addTaxRange() {
        if (errorCheck()) {
            return;
        }
        getCurrent().getTaxRanges().add(getCurrentPayeeTaxRange());
        currentPayeeTaxRange = null;
    }

    public PayeeTaxRange getCurrentPayeeTaxRange() {
        if (currentPayeeTaxRange == null) {
            currentPayeeTaxRange = new PayeeTaxRange();
        }
        return currentPayeeTaxRange;
    }

    public void setCurrentPayeeTaxRange(PayeeTaxRange currentPayeeTaxRange) {
        this.currentPayeeTaxRange = currentPayeeTaxRange;
    }

    /**
     *
     */
    @FacesConverter(forClass = HrmVariables.class)
    public static class HrmVariablesConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            HrmVariablesController controller = (HrmVariablesController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "hrmVariablesController");
            return controller.getEjbFacade().find(getKey(value));
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
            if (object instanceof HrmVariables) {
                HrmVariables o = (HrmVariables) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + HrmVariablesController.class.getName());
            }
        }
    }

    @FacesConverter("hrmVariablesCon")
    public static class HrmVariablesControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            HrmVariablesController controller = (HrmVariablesController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "hrmVariablesController");
            return controller.getEjbFacade().find(getKey(value));
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
            if (object instanceof HrmVariables) {
                HrmVariables o = (HrmVariables) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + HrmVariablesController.class.getName());
            }
        }
    }
}
