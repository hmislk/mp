/*
 * Milk Payment System for Lucky Lanka Milk Processing Company
 *
 * Development and Implementation of Web-based System by ww.divudi.com
 Development and Implementation of Web-based System by ww.divudi.com
 * and
 * a Set of Related Tools
 */
package com.divudi.bean;

import com.divudi.data.DepartmentType;
import com.divudi.data.FeeType;
import com.divudi.entity.Institution;
import com.divudi.facade.ItemFacade;
import com.divudi.entity.Item;

import com.divudi.entity.Service;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.Vmpp;
import java.util.TimeZone;

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
import com.divudi.entity.Packege;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class ItemController implements Serializable {

    private static final long serialVersionUID = 1L;
    @EJB
    private ItemFacade ejbFacade;
    @Inject
    SessionController sessionController;
    private Item current;
    private List<Item> items = null;
    private Institution instituion;

    public List<Item> completeDealorItem(String query) {
        List<Item> suggestions;
        String sql;
        HashMap hm = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c.item from ItemsDistributors c where c.retired=false "
                    + " and c.institution=:ins and (upper(c.item.name) like :q or "
                    + " upper(c.item.barcode) like :q or upper(c.item.code) like :q )order by c.item.name";
            hm.put("ins", getInstituion());
            hm.put("q", "%" + query + "%");
            //////System.out.println(sql);
            suggestions = getFacade().findBySQL(sql, hm, 20);
        }
        return suggestions;

    }

    public List<Item> getDealorItem() {
        List<Item> suggestions;
        String sql;
        HashMap hm = new HashMap();

        sql = "select c.item from ItemsDistributors c where c.retired=false "
                + " and c.institution=:ins "
                + " and c.item is not null "
                + " and c.item.retired=false "
                + " order by c.item.name";
        hm.put("ins", getInstituion());

        //////System.out.println(sql);
        suggestions = getFacade().findBySQL(sql, hm);

        return suggestions;

    }

    public List<Item> completeItem(String query) {
        List<Item> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Item c where c.retired=false and (upper(c.name) like '%" + query.toUpperCase() + "%' or upper(c.barcode) like '%" + query.toUpperCase() + "%' order or upper(c.code) like '%" + query.toUpperCase() + "%' order )order by c.name";
            //////System.out.println(sql);
            suggestions = getFacade().findBySQL(sql, 20);
        }
        return suggestions;

    }

    public List<Item> completePharmacyItem(String query) {
        List<Item> suggestions;
        String sql;
        HashMap tmpMap = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {

            sql = "select c from Item c where c.retired=false and "
                    + " ( c.departmentType is null or c.departmentType!=:dep ) and"
                    + "(type(c)= :amp or type(c)= :ampp or type(c)= :vmp or"
                    + " type(c)= :vmpp) and (upper(c.name) "
                    + "like :q or upper(c.code) "
                    + "like :q or upper(c.barcode) like :q  ) order by c.name";
            //////System.out.println(sql);
            tmpMap.put("amp", Amp.class);
            tmpMap.put("ampp", Ampp.class);
            tmpMap.put("vmp", Vmp.class);
            tmpMap.put("vmpp", Vmpp.class);
            tmpMap.put("dep", DepartmentType.Store);
            tmpMap.put("q", "%" + query.toUpperCase() + "%");
            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 10);
        }
        return suggestions;

    }

    public List<Item> completeAmps(String query) {
        List<Item> suggestions;
        String sql;
        HashMap tmpMap = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Item c where c.retired=false and type(c)= :amp and "
                    + " ( c.departmentType is null or c.departmentType!=:dep ) "
                    + "(upper(c.name) like :q  or "
                    + " upper(c.code) like :q or "
                    + " upper(c.barcode) like :q ) order by c.name";
            //////System.out.println(sql);
            tmpMap.put("amp", Amp.class);
            tmpMap.put("dep", DepartmentType.Store);
            tmpMap.put("q", "%" + query.toUpperCase() + "%");
            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 10);
        }
        return suggestions;

    }

    public List<Item> completeAmpsAndVmps(String query) {
        List<Item> suggestions;
        String sql;
        HashMap tmpMap = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            if (query.length() > 5) {
                sql = "select c from Item c "
                        + " where c.retired=false "
                        + " and (type(c)= :amp or type(c)= :vmp) "
                        + " and (c.departmentType is null or c.departmentType!=:dep ) "
                        + " and (upper(c.name) like :q  or upper(c.code) like :q or upper(c.barcode) like :q ) "
                        + " order by c.name";
            } else {
                sql = "select c from Item c "
                        + " where c.retired=false "
                        + " and (type(c)= :amp or type(c)= :vmp) "
                        + " and (c.departmentType is null or c.departmentType!=:dep ) "
                        + " and (upper(c.name) like :q  or upper(c.code) like :q) "
                        + " order by c.name";
            }
            tmpMap.put("amp", Amp.class);
            tmpMap.put("vmp", Vmp.class);
            tmpMap.put("dep", DepartmentType.Store);
            tmpMap.put("q", "%" + query.toUpperCase() + "%");
            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 10);
        }
        return suggestions;

    }

    public List<Item> completeAmpItem(String query) {
        List<Item> suggestions;
        String sql;
        HashMap tmpMap = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {

            sql = "select c from Item c where c.retired=false "
                    + " and (type(c)= :amp) and "
                    + " ( c.departmentType is null or c.departmentType!=:dep ) "
                    + " and (upper(c.name) like :str or upper(c.code) like :str or"
                    + " upper(c.barcode) like :str ) order by c.name";
            //////System.out.println(sql);
            tmpMap.put("dep", DepartmentType.Store);
            tmpMap.put("amp", Amp.class);
            tmpMap.put("str", "%" + query.toUpperCase() + "%");
            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 30);
        }
        return suggestions;

    }

    public List<Item> completeStoreItem(String query) {
        List<Item> suggestions;
        String sql;
        HashMap tmpMap = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {

            sql = "select c from Item c where c.retired=false and "
                    + " (type(c)= :amp) and c.departmentType=:dep "
                    + " and (upper(c.name) like :str or"
                    + "  upper(c.code) like :str or"
                    + " upper(c.barcode) like :str ) order by c.name";
            //////System.out.println(sql);
            tmpMap.put("amp", Amp.class);
            tmpMap.put("dep", DepartmentType.Store);
            tmpMap.put("str", "%" + query.toUpperCase() + "%");
            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 30);
        }
        return suggestions;

    }

    public List<Item> completeAmpAndAmppItem(String query) {
        List<Item> suggestions;
        String sql;
        HashMap tmpMap = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            if (query.length() > 4) {
                sql = "select c from Item c where c.retired=false and (type(c)= :amp or type(c)=:ampp ) and (upper(c.name) like '%" + query.toUpperCase() + "%' or upper(c.code) like '%" + query.toUpperCase() + "%' or upper(c.barcode) like '%" + query.toUpperCase() + "%') order by c.name";
            } else {
                sql = "select c from Item c where c.retired=false and (type(c)= :amp or type(c)=:ampp ) and (upper(c.name) like '%" + query.toUpperCase() + "%' or upper(c.code) like '%" + query.toUpperCase() + "%') order by c.name";
            }

//////System.out.println(sql);
            tmpMap.put("amp", Amp.class);
            tmpMap.put("ampp", Ampp.class);
            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 30);
        }
        return suggestions;

    }

    public List<Item> completePackage(String query) {
        List<Item> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Item c where c.retired=false and type(c)=Packege and upper(c.name) like '%" + query.toUpperCase() + "%' order by c.name";
            //////System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        return suggestions;

    }

    public List<Item> completeService(String query) {
        List<Item> suggestions;
        String sql;
        HashMap hm = new HashMap();

        sql = "select c from Item c where c.retired=false and type(c)=:cls"
                + " and upper(c.name) like :q order by c.name";

        hm.put("cls", Service.class);
        hm.put("q", "%" + query.toUpperCase() + "%");
        suggestions = getFacade().findBySQL(sql, hm, 20);

        return suggestions;

    }

    public List<Item> completeServiceWithoutProfessional(String query) {
        List<Item> suggestions;
        String sql;
        HashMap hm = new HashMap();

        sql = "select c from Item c where c.retired=false and type(c)=:cls"
                + " and upper(c.name) like :q "
                + " c.id in (Select f.item.id From Itemfee f where f.retired=false "
                + " and f.feeType!=:ftp ) order by c.name ";

        hm.put("ftp", FeeType.Staff);
        hm.put("cls", Service.class);
        hm.put("q", "%" + query.toUpperCase() + "%");
        suggestions = getFacade().findBySQL(sql, hm, 20);

        return suggestions;

    }

    public List<Item> completeMedicalPackage(String query) {
        List<Item> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Item c where c.retired=false and type(c)=MedicalPackage and upper(c.name) like '%" + query.toUpperCase() + "%' order by c.name";
            //////System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        return suggestions;

    }

    public List<Item> completeInwardItems(String query) {
        List<Item> suggestions;
        HashMap m = new HashMap();
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Item c "
                    + " where c.retired=false "
                    + " and type(c)!=:pac "
                    + " and (type(c)=:ser "
                    + " or type(c)=:inv)  "
                    + " and upper(c.name) like :q"
                    + " order by c.name";
            m.put("pac", Packege.class);
            m.put("ser", Service.class);
            m.put("inv", Service.class);
            m.put("q", "%" + query.toUpperCase() + "%");
            //    //////System.out.println(sql);
            suggestions = getFacade().findBySQL(sql, m, 20);
        }
        return suggestions;
    }

    public List<Item> completeOpdItems(String query) {
        List<Item> suggestions;
        HashMap m = new HashMap();
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Item c "
                    + " where c.retired=false "
                    + " and type(c)!=:pac "
                    + " and type(c)!=:inw "
                    + " and (type(c)=:ser "
                    + " or type(c)=:inv)  "
                    + " and upper(c.name) like :q"
                    + " order by c.name";
            m.put("pac", Packege.class);
            m.put("inw", Service.class);
            m.put("ser", Service.class);
            m.put("inv", Service.class);
            m.put("q", "%" + query.toUpperCase() + "%");
            //    //////System.out.println(sql);
            suggestions = getFacade().findBySQL(sql, m, 20);
        }
        return suggestions;
    }

    public List<Item> completeItemWithoutPackOwn(String query) {
        List<Item> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<Item>();
        } else {
            sql = "select c from Item c where c.institution.id = " + getSessionController().getInstitution().getId() + " and c.retired=false and type(c)!=Packege and type(c)!=TimedItem and upper(c.name) like '%" + query.toUpperCase() + "%' order by c.name";
            //////System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        return suggestions;
    }

    /**
     *
     */
    public ItemController() {
    }

    /**
     * Prepare to add new Collecting Centre
     *
     * @return
     */
    public void prepareAdd() {
        current = new Item();
    }

    /**
     *
     * @return
     */
    public ItemFacade getEjbFacade() {
        return ejbFacade;
    }

    /**
     *
     * @param ejbFacade
     */
    public void setEjbFacade(ItemFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    /**
     *
     * @return
     */
    public SessionController getSessionController() {
        return sessionController;
    }

    /**
     *
     * @param sessionController
     */
    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    /**
     * Return the current item
     *
     * @return
     */
    public Item getCurrent() {
        if (current == null) {
            current = new Item();
        }
        return current;
    }

    /**
     * Set the current item
     *
     * @param current
     */
    public void setCurrent(Item current) {
        this.current = current;
    }

    private ItemFacade getFacade() {
        return ejbFacade;
    }

    /**
     *
     * @return
     */
    public List<Item> getItems() {
        String temSql;
        HashMap h = new HashMap();
        temSql = "SELECT i FROM Item i where (type(i)=:t1 or type(i)=:t2 ) and i.retired=false order by i.department.name";
        h.put("t1", Service.class);
        h.put("t2", Service.class);
        items = getFacade().findBySQL(temSql, h, TemporalType.TIME);
        return items;
    }

    /**
     *
     * Set all Items to null
     *
     */
    private void recreateModel() {
        items = null;
    }

    /**
     *
     */
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

    /**
     *
     * Delete the current Item
     *
     */
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

    public Institution getInstituion() {
        return instituion;
    }

    public void setInstituion(Institution instituion) {
        this.instituion = instituion;
    }

    @FacesConverter(forClass = Item.class)
    public static class ItemControllerConverter implements Converter {

        /**
         *
         * @param facesContext
         * @param component
         * @param value
         * @return
         */
        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ItemController controller = (ItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "itemController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key = 0l;
            try {
                key = Long.valueOf(value);
            } catch (Exception e) {

            }

            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        /**
         *
         * @param facesContext
         * @param component
         * @param object
         * @return
         */
        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Item) {
                Item o = (Item) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + ItemController.class.getName());
            }
        }
    }

    /**
     *
     */
    @FacesConverter("itemcon")
    public static class ItemConverter implements Converter {

        /**
         *
         * @param facesContext
         * @param component
         * @param value
         * @return
         */
        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ItemController controller = (ItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "itemController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key = 0l;
            try {
                key = Long.valueOf(value);
            } catch (Exception e) {

            }

            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        /**
         *
         * @param facesContext
         * @param component
         * @param object
         * @return
         */
        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Item) {
                Item o = (Item) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + ItemController.class.getName());
            }
        }
    }
}
