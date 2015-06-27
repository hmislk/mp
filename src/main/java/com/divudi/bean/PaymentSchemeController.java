/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean;

import com.divudi.data.PaymentMethod;
import com.divudi.data.dataStructure.PaymentMethodData;
import java.util.TimeZone;
import com.divudi.facade.PaymentSchemeFacade;
import com.divudi.entity.PaymentScheme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.TemporalType;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class PaymentSchemeController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private PaymentSchemeFacade ejbFacade;
    List<PaymentScheme> selectedItems;
    private PaymentScheme current;
    private List<PaymentScheme> items = null;
    String selectText = "";
    List<PaymentScheme> payingItems;
    List<PaymentScheme> rcItems;
    List<PaymentScheme> billingItems;
    List<PaymentScheme> list;
    private PaymentMethod[] paymentMethods;

    public List<PaymentScheme> getList() {
        if (list == null) {
            createList();
        }
        return list;
    }

    public void setList(List<PaymentScheme> list) {
        this.list = list;
    }

    public void createList() {
        String sql;
        HashMap hm = new HashMap();
        sql = "SELECT i FROM PaymentScheme i "
                + " where i.retired=false "
                + " order by i.orderNo, i.name";
        list = getFacade().findBySQL(sql, hm);
    }

    public boolean errorCheckPaymentScheme(PaymentMethod paymentMethod, PaymentMethodData paymentMethodData) {
        if (paymentMethod == PaymentMethod.Cheque) {
            if (paymentMethodData.getCheque().getInstitution() == null
                    || paymentMethodData.getCheque().getNo() == null
                    || paymentMethodData.getCheque().getDate() == null) {
                UtilityController.addErrorMessage("Please select Cheque Number,Bank and Cheque Date");
                return true;
            }

        }

        if (paymentMethod == PaymentMethod.Slip) {
            if (paymentMethodData.getSlip().getInstitution() == null
                    || paymentMethodData.getSlip().getComment() == null
                    || paymentMethodData.getSlip().getDate() == null) {
                UtilityController.addErrorMessage("Please Fill Memo,Bank and Slip Date ");
                return true;
            }

        }

        if (paymentMethod == PaymentMethod.Card) {
            if (paymentMethodData.getCreditCard().getInstitution() == null
                    || paymentMethodData.getCreditCard().getNo() == null) {
                UtilityController.addErrorMessage("Please Fill Credit Card Number and Bank");
                return true;
            }

        }

        return false;
    }

    public boolean checkPaid(PaymentMethod paymentMethod, double paid, double amount) {

        if (paymentMethod == PaymentMethod.Cash) {
            if (paid == 0.0) {
                UtilityController.addErrorMessage("Please select tendered amount correctly");
                return true;
            }
            if (paid < amount) {
                UtilityController.addErrorMessage("Please select tendered amount correctly");
                return true;
            }
        }

        return false;
    }

    public List<PaymentScheme> getPayingItems() {
        if (payingItems == null) {
            payingItems = getFacade().findBySQL("select c from PaymentScheme c where c.retired=false and c.validForPayments = true order by c.name");

        }
        return payingItems;
    }

    public void setItems(List<PaymentScheme> items) {
        this.items = items;
    }

    public void setRcItems(List<PaymentScheme> rcItems) {
        this.rcItems = rcItems;
    }

    public List<PaymentScheme> getRcItems() {
        ////System.out.println("getting rc items " + rcItems);
        if (rcItems == null) {
            ////System.out.println("rsItems null");
            rcItems = getFacade().findBySQL("select c from PaymentScheme c where c.retired=false and c.validForCrBills = true order by c.name");
            String temSql;
            temSql = "SELECT i FROM PaymentScheme i where i.retired=false order by i.name";
            items = getFacade().findBySQL(temSql);
            ////System.out.println("noew after getting rs items , it is " + rcItems);
        }
        return rcItems;
    }

    public List<PaymentScheme> getBillingItems() {
        if (billingItems == null) {
            billingItems = getFacade().findBySQL("select c from PaymentScheme c where c.retired=false and c.validForBilledBills = true order by c.name");
        }
        return billingItems;
    }

    public void setBillingItems(List<PaymentScheme> billingItems) {
        this.billingItems = billingItems;
    }

    public void setPayingItems(List<PaymentScheme> payingItems) {
        this.payingItems = payingItems;
    }

    public List<PaymentScheme> getSelectedItems() {
        selectedItems = getFacade().findBySQL("select c from PaymentScheme c where c.retired=false and upper(c.name) like '%" + getSelectText().toUpperCase() + "%' order by c.name");
        return selectedItems;
    }

    public void prepareAdd() {
        current = new PaymentScheme();
    }

    public void setSelectedItems(List<PaymentScheme> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    private void recreateModel() {
        items = null;
    }

    public void saveSelected() {

        if (getCurrent().getPaymentMethod() == null) {
            UtilityController.addErrorMessage("Payment Method?");
            return;
        }
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

    public PaymentSchemeFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(PaymentSchemeFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public PaymentSchemeController() {
    }

    public PaymentScheme getCurrent() {
        if (current == null) {
            current = new PaymentScheme();
        }
        return current;
    }

    public void setCurrent(PaymentScheme current) {
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

    private PaymentSchemeFacade getFacade() {
        return ejbFacade;
    }

    public List<PaymentScheme> getItems() {
        if (items == null) {
            String temSql;
            temSql = "SELECT i FROM PaymentScheme i where i.retired=false order by i.orderNo, i.name";
            items = getFacade().findBySQL(temSql);
        }
        return items;
    }

    public List<PaymentScheme> getItemsForReceive() {
        List<PaymentScheme> tmp = new ArrayList<PaymentScheme>();

        String temSql;
        HashMap tm = new HashMap();
        temSql = "SELECT i FROM PaymentScheme i where (i.paymentMethod= :m1 or i.paymentMethod= :m2 or i.paymentMethod= :m3) and "
                + " i.discountPercent=0 and i.retired=false order by i.name";
        tm.put("m1", PaymentMethod.Cash);
        tm.put("m2", PaymentMethod.Cheque);
        tm.put("m3", PaymentMethod.Slip);
        tmp = getFacade().findBySQL(temSql, tm, TemporalType.TIMESTAMP);

        return tmp;
    }

    public List<PaymentScheme> getSchemeForBooking() {
        List<PaymentScheme> tmp = new ArrayList<PaymentScheme>();

        String temSql;
        HashMap tm = new HashMap();
        temSql = "SELECT i FROM PaymentScheme i where (i.paymentMethod= :m1 or i.paymentMethod= :m2 or i.paymentMethod= :m3) and "
                + "  i.discountPercent=0 and i.retired=false order by i.name";
        tm.put("m1", PaymentMethod.Cash);
        tm.put("m2", PaymentMethod.Agent);
        tm.put("m3", PaymentMethod.Credit);
        tmp = getFacade().findBySQL(temSql, tm, TemporalType.TIMESTAMP);

        return tmp;
    }

    public List<PaymentScheme> getSchemeForBookingSettle() {
        List<PaymentScheme> tmp = new ArrayList<PaymentScheme>();

        String temSql;
        HashMap tm = new HashMap();
        temSql = "SELECT i FROM PaymentScheme i where (i.paymentMethod= :m1 or i.paymentMethod= :m2) and "
                + "  i.discountPercent=0 and i.retired=false order by i.name";
        tm.put("m1", PaymentMethod.Cash);
        tm.put("m2", PaymentMethod.Agent);
        tmp = getFacade().findBySQL(temSql, tm, TemporalType.TIMESTAMP);

        return tmp;
    }

    public List<PaymentScheme> getPettyPayment() {
        List<PaymentScheme> tmp = new ArrayList<PaymentScheme>();

        String temSql;
        HashMap tm = new HashMap();
        temSql = "SELECT i FROM PaymentScheme i where (i.paymentMethod= :m1 or i.paymentMethod= :m2) and "
                + " i.discountPercent=0 and i.retired=false order by i.name";
        tm.put("m1", PaymentMethod.Cash);
        tm.put("m2", PaymentMethod.Cheque);
        tmp = getFacade().findBySQL(temSql, tm, TemporalType.TIMESTAMP);

        return tmp;
    }

    public PaymentMethod[] getPaymentMethods() {

        return PaymentMethod.values();
    }

    public void setPaymentMethods(PaymentMethod[] paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    /**
     *
     */
    @FacesConverter(forClass = PaymentScheme.class)
    public static class PaymentSchemeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PaymentSchemeController controller = (PaymentSchemeController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "paymentSchemeController");
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
            if (object instanceof PaymentScheme) {
                PaymentScheme o = (PaymentScheme) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + PaymentSchemeController.class.getName());
            }
        }
    }
}
