/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean;

import com.divudi.facade.MembershipSchemeFacade;
import com.divudi.entity.MembershipScheme;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 Informatics)
 */
@Named
@SessionScoped
public class MembershipSchemeController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private MembershipSchemeFacade ejbFacade;
    List<MembershipScheme> selectedItems;
    private MembershipScheme current;
    private List<MembershipScheme> items = null;
    String selectText = "";

    public List<MembershipScheme> completeMembershipScheme(String qry) {
        List<MembershipScheme> c;
        c = getFacade().findBySQL("select c from MembershipScheme c where c.retired=false and upper(c.name) like '%" + qry.toUpperCase() + "%' order by c.name");
        if (c == null) {
            c = new ArrayList<>();
        }
        return c;
    }

    

  
    public List<MembershipScheme> getSelectedItems() {
        selectedItems = getFacade().findBySQL("select c from MembershipScheme c where c.retired=false and upper(c.name) like '%" + getSelectText().toUpperCase() + "%' order by c.name");
        return selectedItems;
    }

    public void prepareAdd() {
        current = new MembershipScheme();
    }

    public void setSelectedItems(List<MembershipScheme> selectedItems) {
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

    public MembershipSchemeFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(MembershipSchemeFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public MembershipSchemeController() {
    }

    public MembershipScheme getCurrent() {
        return current;
    }

    public void setCurrent(MembershipScheme current) {
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

    private MembershipSchemeFacade getFacade() {
        return ejbFacade;
    }

    public List<MembershipScheme> getItems() {
        items = getFacade().findAll("name", true);
        return items;
    }

    /**
     *
     */
    @FacesConverter(forClass = MembershipScheme.class)
    public static class MembershipSchemeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            MembershipSchemeController controller = (MembershipSchemeController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "membershipSchemeController");
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
            if (object instanceof MembershipScheme) {
                MembershipScheme o = (MembershipScheme) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + MembershipSchemeController.class.getName());
            }
        }
    }

    /**
     *
     */
    @FacesConverter("membershipSchemeConverter")
    public static class MembershipSchemeConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            MembershipSchemeController controller = (MembershipSchemeController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "membershipSchemeController");
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
            if (object instanceof MembershipScheme) {
                MembershipScheme o = (MembershipScheme) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + MembershipSchemeController.class.getName());
            }
        }
    }
}