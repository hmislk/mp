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
import com.divudi.data.inward.RoomFacility;
import com.divudi.entity.inward.RoomFacilityCharge;
import com.divudi.entity.inward.TimedItemFee;
import com.divudi.facade.RoomFacilityChargeFacade;
import com.divudi.facade.TimedItemFeeFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
public class RoomFacilityChargeController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private RoomFacilityChargeFacade ejbFacade;
    List<RoomFacilityCharge> selectedItems;
    private RoomFacilityCharge current;
    private List<RoomFacilityCharge> items = null;
    String selectText = "";
   

    public List<RoomFacilityCharge> completeRoom(String query) {
        List<RoomFacilityCharge> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<RoomFacilityCharge>();
        } else {
            sql = "select p from RoomFacilityCharge p where p.retired=false and upper(p.name) like '%" + query.toUpperCase() + "%' order by p.name";
            System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);           
        }
        return suggestions;
    }

    public List<RoomFacilityCharge> completeRoomChange(String query) {
        List<RoomFacilityCharge> suggestions;
        String sql;
        HashMap hm=new HashMap();
        if (getCurrent() == null || getCurrent().getRoom() == null || query == null) {
            suggestions = new ArrayList<RoomFacilityCharge>();
        } else {
            sql = "select p from RoomFacilityCharge p where p.retired=false and (p.room.filled=false or p.room=:rm) and upper(p.name) like '%" + query.toUpperCase() + "%' order by p.name";
            hm.put("rm", getCurrent().getRoom());
            System.out.println(sql);
            suggestions = getFacade().findBySQL(sql,hm);

        }
        return suggestions;
    }

    public List<RoomFacilityCharge> getSelectedItems() {
        selectedItems = getFacade().findBySQL("select c from RoomFacilityCharge c where c.retired=false  and upper(c.name) like '%" + getSelectText().toUpperCase() + "%' order by c.name");
        return selectedItems;
    }

    public void prepareAdd() {
        current = new RoomFacilityCharge();
        TimedItemFee tmp = new TimedItemFee();
        current.setTimedItemFee(tmp);
    }

    public void setSelectedItems(List<RoomFacilityCharge> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    private void recreateModel() {
        items = null;
    }
    @EJB
    private TimedItemFeeFacade timedItemFeeFacade;

    private TimedItemFee saveTimedItemFee() {
        TimedItemFee temp = getCurrent().getTimedItemFee();

        if (temp.getId() == null) {
            getTimedItemFeeFacade().create(temp);
        } else {
            getTimedItemFeeFacade().edit(temp);
        }

        return temp;
    }

    public void saveSelected() {
        saveTimedItemFee();

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
        getItems();
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public RoomFacilityChargeFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(RoomFacilityChargeFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public RoomFacilityChargeController() {
    }

    public RoomFacilityCharge getCurrent() {
        if (current == null) {
            current = new RoomFacilityCharge();

            TimedItemFee tmp = new TimedItemFee();
            current.setTimedItemFee(tmp);
        }
        return current;
    }

    public void setCurrent(RoomFacilityCharge current) {
        this.current = current;
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setRetirer(sessionController.getLoggedUser());
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

    private RoomFacilityChargeFacade getFacade() {
        return ejbFacade;
    }

    public List<RoomFacilityCharge> getItems() {
//         items = getFacade().findAll("name", true);
        String sql = "SELECT i FROM RoomFacilityCharge i where i.retired=false ";
        items = getEjbFacade().findBySQL(sql);
        if (items == null) {
            items = new ArrayList<RoomFacilityCharge>();
        }
        return items;
    }

    public RoomFacility[] getRoomFacilitys() {
        return RoomFacility.values();
    }

   
    public TimedItemFeeFacade getTimedItemFeeFacade() {
        return timedItemFeeFacade;
    }

    public void setTimedItemFeeFacade(TimedItemFeeFacade timedItemFeeFacade) {
        this.timedItemFeeFacade = timedItemFeeFacade;
    }

    /**
     *
     */
    @FacesConverter("fac")
    public static class RoomFacilityChargeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            RoomFacilityChargeController controller = (RoomFacilityChargeController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "roomFacilityChargeController");
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
            if (object instanceof RoomFacilityCharge) {
                RoomFacilityCharge o = (RoomFacilityCharge) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + RoomFacilityChargeController.class.getName());
            }
        }
    }
}
