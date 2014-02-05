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
import com.divudi.data.BillType;
import com.divudi.entity.Category;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.InwardPriceAdjustment;
import com.divudi.entity.PaymentScheme;
import com.divudi.facade.InwardPriceAdjustmentFacade;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import javax.inject.Inject;
import javax.inject.Named; import javax.ejb.EJB;
import javax.inject.Inject;
import javax.faces.view.ViewScoped;
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
@ViewScoped
public  class InwardPriceAdjustmntController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private InwardPriceAdjustmentFacade ejbFacade;
    private InwardPriceAdjustment current;
    private List<InwardPriceAdjustment> items = null;
    BillType billType;
    PaymentScheme paymentScheme;
    Category category;
    Institution institution;
    Department department;
    double fromPrice;
    double toPrice;
    double margin;

    private void recreateModel() {
        fromPrice = toPrice + 1;
        toPrice = 0.0;
        margin = 0;
        items = null;
    }

    public void saveSelected() {

        if (fromPrice == toPrice) {
            UtilityController.addErrorMessage("Check prices");
            return;
        }
        if (toPrice == 0) {
            UtilityController.addErrorMessage("Check prices");
            return;
        }
      
        if (department == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }
        
        if (category == null) {
            UtilityController.addErrorMessage("Please select a category");
            return;
        }

        InwardPriceAdjustment a = new InwardPriceAdjustment();
       
        a.setCategory(category);
        a.setDepartment(department);
        a.setFromPrice(fromPrice);
        a.setToPrice(toPrice);
        a.setInstitution(department.getInstitution());
        a.setMargin(margin);
        a.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        a.setCreater(getSessionController().getLoggedUser());
        getFacade().create(a);
        UtilityController.addSuccessMessage("savedNewSuccessfully");
        recreateModel();
        getItems();
    }

    public InwardPriceAdjustmentFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(InwardPriceAdjustmentFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public InwardPriceAdjustmntController() {
    }

    public InwardPriceAdjustment getCurrent() {
        return current;
    }

    public void setCurrent(InwardPriceAdjustment current) {
       
        this.current = current;
    }

    public BillType getBillType() {
        return billType;
    }

    public void setBillType(BillType billType) {
        this.billType = billType;
    }

    public PaymentScheme getPaymentScheme() {
        return paymentScheme;
    }

    public void setPaymentScheme(PaymentScheme paymentScheme) {
        this.paymentScheme = paymentScheme;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public double getFromPrice() {
        return fromPrice;
    }

    public void setFromPrice(double fromPrice) {
        this.fromPrice = fromPrice;
    }

    public double getToPrice() {
        return toPrice;
    }

    public void setToPrice(double toPrice) {
        this.toPrice = toPrice;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
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
    //    recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    private InwardPriceAdjustmentFacade getFacade() {
        return ejbFacade;
    }

    public List<InwardPriceAdjustment> getItems() {
        String sql;
        sql = "select a from InwardPriceAdjustment a where a.retired=false" ;
        items = getFacade().findBySQL(sql);
        return items;
    }

    /**
     *
     */
    @FacesConverter(forClass = InwardPriceAdjustment.class)
    public static class InwardPriceAdjustmentControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            InwardPriceAdjustmntController controller = (InwardPriceAdjustmntController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "inwardPriceAdjustmentController");
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
            if (object instanceof InwardPriceAdjustment) {
                InwardPriceAdjustment o = (InwardPriceAdjustment) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + InwardPriceAdjustmntController.class.getName());
            }
        }
    }
}
