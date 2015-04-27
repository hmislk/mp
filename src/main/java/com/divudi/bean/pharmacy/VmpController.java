/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.ejb.BillBean;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.VtmsVmps;
import com.divudi.facade.AmpFacade;
import com.divudi.facade.SpecialityFacade;
import com.divudi.facade.VmpFacade;
import com.divudi.facade.VtmsVmpsFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Informatics)
 */
@Named
@SessionScoped
public class VmpController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private VmpFacade ejbFacade;
    @EJB
    private SpecialityFacade specialityFacade;
    @EJB
    private BillBean billBean;
    List<Vmp> selectedItems;
    private Vmp current;
    private List<Vmp> items = null;
    String selectText = "";
    String bulkText = "";
    boolean billedAs;
    boolean reportedAs;
    VtmsVmps addingVtmInVmp;
    VtmsVmps removingVtmInVmp;
    @EJB
    VtmsVmpsFacade vivFacade;
    List<VtmsVmps> vivs;

    public List<Vmp> completeVmp(String query) {
        List<Vmp> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<Vmp>();
        } else {
            sql = "select c from Vmp c where c.retired=false and upper(c.name) like '%" + query.toUpperCase() + "%' order by c.name";
            ////System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        return suggestions;
    }

    public List<VtmsVmps> getVivs() {
        if (getCurrent().getId() == null) {
            return new ArrayList<VtmsVmps>();
        } else {

            vivs = getVivFacade().findBySQL("select v from VtmsVmps v where v.vmp.id = " + getCurrent().getId());

            if (vivs == null) {
                return new ArrayList<VtmsVmps>();
            }

            return vivs;
        }
    }

    public void remove() {
        getVivFacade().remove(removingVtmInVmp);
    }

    public void setVivs(List<VtmsVmps> vivs) {
        this.vivs = vivs;
    }

    private boolean errorCheck() {
        if (addingVtmInVmp == null) {
            return true;
        }
        if (addingVtmInVmp.getVtm() == null) {
            UtilityController.addErrorMessage("Select Vtm");
            return true;
        }
//        TODO:Message
        if (current == null) {
            return true;
        }
        if (addingVtmInVmp.getStrength() == 0.0) {
            UtilityController.addErrorMessage("Type Strength");
            return true;
        }
        if (current.getCategory() == null) {
            UtilityController.addErrorMessage("Select Category");
            return true;
        }
        if (addingVtmInVmp.getStrengthUnit() == null) {
            UtilityController.addErrorMessage("Select Strenth Unit");
            return true;
        }

        return false;
    }

    public void addVtmInVmp() {
        if (errorCheck()) {
            return;
        }

        saveVmp();
        getAddingVtmInVmp().setVmp(current);
        getVivFacade().create(getAddingVtmInVmp());

        UtilityController.addSuccessMessage("Added");

        addingVtmInVmp = null;

    }

    private void saveVmp() {
        if (current.getName() == null || current.getName().equals("")) {
            current.setName(createVmpName());
        }

        if (current.getId() == null || current.getId() == 0) {
            getFacade().create(current);
        } else {
            getFacade().edit(current);
        }

    }

    public String createVmpName() {
        return addingVtmInVmp.getVtm().getName() + " " + addingVtmInVmp.getStrength() + " " + addingVtmInVmp.getStrengthUnit().getName() + " " + current.getCategory().getName();
    }

    public VtmsVmps getAddingVtmInVmp() {
        if (addingVtmInVmp == null) {
            addingVtmInVmp = new VtmsVmps();
        }
        return addingVtmInVmp;
    }

    public void setAddingVtmInVmp(VtmsVmps addingVtmInVmp) {
        this.addingVtmInVmp = addingVtmInVmp;
    }

    public VtmsVmps getRemovingVtmInVmp() {
        return removingVtmInVmp;
    }

    public void setRemovingVtmInVmp(VtmsVmps removingVtmInVmp) {
        this.removingVtmInVmp = removingVtmInVmp;
    }

    public VtmsVmpsFacade getVivFacade() {
        return vivFacade;
    }

    public void setVivFacade(VtmsVmpsFacade vivFacade) {
        this.vivFacade = vivFacade;
    }

    public List<Vmp> completeInvest(String query) {
        List<Vmp> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<Vmp>();
        } else {
            sql = "select c from Vmp c where c.retired=false and upper(c.name) like '%" + query.toUpperCase() + "%' order by c.name";
            ////System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        return suggestions;
    }

    public boolean isBilledAs() {
        return billedAs;
    }

    public void setBilledAs(boolean billedAs) {
        this.billedAs = billedAs;
    }

    public boolean isReportedAs() {
        return reportedAs;
    }

    public void setReportedAs(boolean reportedAs) {
        this.reportedAs = reportedAs;
    }

    public BillBean getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBean billBean) {
        this.billBean = billBean;
    }

    public String getBulkText() {

        return bulkText;
    }

    public void setBulkText(String bulkText) {
        this.bulkText = bulkText;
    }

    public List<Vmp> getSelectedItems() {

        if (selectText.trim().equals("")) {
            selectedItems = getFacade().findBySQL("select c from Vmp c where c.retired=false order by c.name");
        } else {
            String sql = "select c from Vmp c where c.retired=false and upper(c.name) like '%" + getSelectText().toUpperCase() + "%' order by c.name";
            selectedItems = getFacade().findBySQL(sql);

        }
        return selectedItems;
    }

    public void prepareAdd() {
        current = new Vmp();
        addingVtmInVmp = new VtmsVmps();
    }

    public void bulkUpload() {
        List<String> lstLines = Arrays.asList(getBulkText().split("\\r?\\n"));
        for (String s : lstLines) {
            List<String> w = Arrays.asList(s.split(","));
            try {
                String code = w.get(0);
                String ix = w.get(1);
                String ic = w.get(2);
                String f = w.get(4);
                ////System.out.println(code + " " + ix + " " + ic + " " + f);

                Vmp tix = new Vmp();
                tix.setCode(code);
                tix.setName(ix);
                tix.setDepartment(null);

            } catch (Exception e) {
            }

        }
    }

    public void setSelectedItems(List<Vmp> selectedItems) {
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
            getFacade().edit(getCurrent());
            UtilityController.addSuccessMessage("savedOldSuccessfully");
        }
        recreateModel();
        getItems();
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public VmpFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(VmpFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public VmpController() {
    }

    public Vmp getCurrent() {
        if (current == null) {
            current = new Vmp();
        }
        return current;
    }

    public void setCurrent(Vmp current) {
        this.current = current;
        if (current != null) {
            if (current.getBilledAs() == current) {
                billedAs = false;
            } else {
                billedAs = true;
            }
            if (current.getReportedAs() == current) {
                reportedAs = false;
            } else {
                reportedAs = true;
            }
        }
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

    private VmpFacade getFacade() {
        return ejbFacade;
    }

    public List<Vmp> getItems() {
        items = getFacade().findAll("name", true);
        return items;
    }

    public SpecialityFacade getSpecialityFacade() {
        return specialityFacade;
    }

    public void setSpecialityFacade(SpecialityFacade specialityFacade) {
        this.specialityFacade = specialityFacade;
    }

    @EJB
    AmpFacade ampFacade;

    public void fixVmps() {
        List<Vmp> vmps1;
        List<Vmp> vmps2;
        String jpql;
        Map m;
        jpql = "select v from Vmp v where v.retired=false";
        vmps1 = getFacade().findBySQL(jpql);

        for (Vmp v1 : vmps1) {
            if (!v1.getName().contains("Tablet")) {
                v1.setName(v1.getName().replace(" Tab", " Tablet"));
            }
            if (!v1.getName().contains("Capsule")) {
                v1.setName(v1.getName().replace(" Cap", " Capsule"));
            }
            getFacade().edit(v1);
            
        //    System.out.println("Considering v1 = " + v1.getName());
            jpql = "select v from Vmp v where v.retired=false and upper(v.name)=:name";
            m = new HashMap();
            m.put("name", v1.getName().toUpperCase());
            vmps2 = getFacade().findBySQL(jpql, m);
            Vmp v3 = getFacade().find(v1.getId());

            for (Vmp v2 : vmps2) {

                if (!v3.isRetired()) {

                    if (v1.getName().equalsIgnoreCase(v2.getName())) {
                        if (!v1.equals(v2)) {
                            v2.setRetired(true);
                            v2.setRetiredAt(new Date());
                            v2.setRetirer(getSessionController().getLoggedUser());
                            v2.setRetireComments("fixVmps");
                            getFacade().edit(v2);
                        //    System.out.println("v2 retired = " + v2.getName());
                            jpql = "select a from Amp a where a.retired=false and a.vmp=:vmp";
                            m = new HashMap();
                            m.put("vmp", v2);
                            List<Amp> amps = ampFacade.findBySQL(jpql, m);
                            for (Amp a : amps) {
                                a.setVmp(v1);
                                ampFacade.edit(a);
                            //    System.out.println("amp updates = " + a.getName());
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     *
     */
    @FacesConverter("vmp")
    public static class VmpControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            VmpController controller = (VmpController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "vmpController");
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
            if (object instanceof Vmp) {
                Vmp o = (Vmp) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + VmpController.class.getName());
            }
        }
    }
}
