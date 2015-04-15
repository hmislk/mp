/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean;

import com.divudi.data.InvestigationItemType;
import com.divudi.entity.Category;
import com.divudi.entity.Consultant;
import com.divudi.entity.Department;
import com.divudi.entity.Doctor;
import java.util.TimeZone;
import com.divudi.entity.Person;
import com.divudi.entity.Speciality;
import com.divudi.facade.StaffFacade;
import com.divudi.entity.Staff;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.TemporalType;
import org.apache.commons.io.IOUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class StaffController implements Serializable {

    StreamedContent scCircular;
    StreamedContent scCircularById;
    private UploadedFile file;
    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    ////
    @EJB
    private StaffFacade ejbFacade;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private DepartmentFacade departmentFacade;
    List<Staff> selectedItems;
    private List<Staff> selectedList;
    private List<Staff> filteredStaff;
    private Staff selectedStaff;
    private Staff current;
    List<Staff> staffWithCode;
    private List<Staff> items = null;
    String selectText = "";

    Category formCategory;
    public Category getFormCategory() {
        return formCategory;
    }

    public List<Staff> getStaffWithCode() {
        return staffWithCode;
    }

    public void setStaffWithCode(List<Staff> staffWithCode) {
        this.staffWithCode = staffWithCode;
    }

    public void createStaffListWithOutSpecility() {

        String sql = "select s from Staff s where "
                + " s.retired=false "
                + " and s.speciality is null "
                + " order by s.person.name ";

        staffWithCode = getEjbFacade().findBySQL(sql);
    }

    public void createStaffList() {

        String sql = "select s from Staff s where "
                + " s.retired=false "
                + " order by s.person.name ";

        staffWithCode = getEjbFacade().findBySQL(sql);
    }

    public void createStaffOnly() {

        String sql = "select s from Staff s where "
                + " s.retired=false "
                + " and (type(s)!=:class1"
                + " and type(s)!=:class2)"
                + " order by s.code ";
        HashMap hm = new HashMap();
        hm.put("class1", Doctor.class);
        hm.put("class2", Consultant.class);
        staffWithCode = getEjbFacade().findBySQL(sql, hm);
    }

    public void createStaffWithCode() {
        HashMap hm = new HashMap();
        hm.put("class", Consultant.class);
        String sql = "select p from Staff p "
                + " where p.retired=false "
                + " and type(p)!=:class"
                + " and LENGTH(p.code) > 0 "
                + " and LENGTH(p.person.name) > 0 "
                + " order by p.codeInterger ";

        //System.out.println(sql);
        staffWithCode = getEjbFacade().findBySQL(sql, hm);

    }


    Date fromDate;
    Date toDate;


    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public List<Staff> completeStaffCode(String query) {
        List<Staff> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select p from Staff p "
                    + " where p.retired=false "
                    + " and LENGTH(p.code) > 0 "
                    + " and LENGTH(p.person.name) > 0 "
                    + " and (upper(p.person.name) like '%" + query.toUpperCase() + "%' "
                    + " or upper(p.code) like '%" + query.toUpperCase() + "%' )"
                    + " order by p.person.name";

            //System.out.println(sql);
            suggestions = getEjbFacade().findBySQL(sql, 20);
        }
        return suggestions;
    }

    public void makeNull() {
        items = null;
        selectedStaff = null;
        filteredStaff = null;
        current = null;
        selectedItems = null;
        selectedList = null;
        selectedStaff = null;
        staffWithCode = null;

    }

    public List<Department> getInstitutionDepatrments() {
        List<Department> d;
        //System.out.println("gettin ins dep ");
        if (getCurrent().getInstitution() == null) {
            return new ArrayList<>();
        } else {
            String sql = "Select d From Department d where d.retired=false and"
                    + " d.institution=:ins";
            HashMap hm = new HashMap();
            hm.put("ins", getCurrent().getInstitution());
            d = getDepartmentFacade().findBySQL(sql, hm);
        }

        return d;
    }

    List<Staff> suggestions;

    public List<Staff> completeStaff(String query) {

        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select p from Staff p where p.retired=false  and"
                    + " (upper(p.person.name) like :q or  "
                    + " upper(p.code) like :q )"
                    + " order by p.person.name";
            //System.out.println(sql);
            HashMap hm = new HashMap();
            hm.put("q", "%" + query.toUpperCase() + "%");
            suggestions = getFacade().findBySQL(sql, hm, 20);
        }
        return suggestions;
    }

    public List<Staff> getSpecialityStaff(Speciality speciality) {
        List<Staff> ss;
        String sql;
        HashMap hm = new HashMap();
        sql = "select p from Staff p where  "
                + " p.speciality=:sp and "
                + " p.retired=false "
                + "order by p.person.name";
//            //System.out.println(sql);
        hm.put("sp", speciality);
        ss = getFacade().findBySQL(sql, hm);

        System.err.println("Staff List Size " + ss.size());

        return ss;
    }

    public List<Staff> completeStaffWithoutDoctors(String query) {
        List<Staff> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select p from Staff p where p.retired=false and "
                    + "(upper(p.person.name) like '%" + query.toUpperCase() + "%' or "
                    + " upper(p.code) like '%" + query.toUpperCase() + "%' ) and type(p) != Doctor"
                    + " order by p.person.name";
            //System.out.println(sql);
            suggestions = getFacade().findBySQL(sql, 20);
        }
        return suggestions;
    }

    public String saveSignature() {
        InputStream in;
        if (file == null || "".equals(file.getFileName())) {
            return "";
        }
        if (file == null) {
            UtilityController.addErrorMessage("Please select an image");
            return "";
        }
        if (getCurrent().getId() == null || getCurrent().getId() == 0) {
            UtilityController.addErrorMessage("Please select staff member");
            return "";
        }
        //System.out.println("file name is not null");
        //System.out.println(file.getFileName());
        try {
            in = getFile().getInputstream();
            getCurrent().setFileName(file.getFileName());
            getCurrent().setFileType(file.getContentType());
            getCurrent().setBaImage(IOUtils.toByteArray(in));
            getFacade().edit(getCurrent());
            return "";
        } catch (Exception e) {
            //System.out.println("Error " + e.getMessage());
            return "";
        }

    }

    public StreamedContent getSignatureById() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        } else {
            // So, browser is requesting the image. Get ID value from actual request param.
            String id = context.getExternalContext().getRequestParameterMap().get("id");
            Long l;
            try {
                l = Long.valueOf(id);
            } catch (NumberFormatException e) {
                l = 0l;
            }
            Staff temImg = getFacade().find(Long.valueOf(id));
            if (temImg != null) {
                return new DefaultStreamedContent(new ByteArrayInputStream(temImg.getBaImage()), temImg.getFileType());
            } else {
                return new DefaultStreamedContent();
            }
        }
    }

    public StreamedContent getSignature() {
//        FacesContext context = FacesContext.getCurrentInstance();
//        if (context.getRenderResponse()) {
//            //System.out.println("render response");
//            return new DefaultStreamedContent();
//        } else {
        //System.out.println("image resuest");

        if (current == null) {
            //System.out.println("staff null");
            return new DefaultStreamedContent();
        }
        //System.out.println("staf is " + current);
        if (current.getId() != null && current.getBaImage() != null) {
            //System.out.println(current.getFileType());
            //System.out.println(current.getFileName());
            return new DefaultStreamedContent(new ByteArrayInputStream(current.getBaImage()), current.getFileType(), current.getFileName());
        } else {
            //System.out.println("nulls");
            return new DefaultStreamedContent();
        }
//        }

    }

    public StreamedContent displaySignature(Long stfId) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            return new DefaultStreamedContent();
        }
        if (stfId == null) {
            return new DefaultStreamedContent();
        }

        Staff temStaff = getFacade().findFirstBySQL("select s from Staff s where s.baImage != null and s.id = " + stfId);

        //System.out.println("Printing");
        if (temStaff == null) {
            return new DefaultStreamedContent();
        } else {
            if (temStaff.getId() != null && temStaff.getBaImage() != null) {
                //System.out.println(temStaff.getFileType());
                //System.out.println(temStaff.getFileName());
                return new DefaultStreamedContent(new ByteArrayInputStream(temStaff.getBaImage()), temStaff.getFileType(), temStaff.getFileName());
            } else {
                return new DefaultStreamedContent();
            }
        }
    }

    public List<Staff> getSelectedItems() {

        /**
         *
         *
         *
         *
         * sql = "select ss from Staff ss " + " where ss.retired=false " + " and
         * type(ss)!=:class " + " and ss.codeInterger!=0 ";
         *
         *
         *
         */
        String sql = "";
        HashMap hm = new HashMap();
        if (selectText.trim().equals("")) {
            sql = "select c from Staff c "
                    + " where c.retired=false "
                    + " and type(c)!=:class"
                    + " order by c.person.name";
        } else {
            sql = "select c from Staff c"
                    + " where c.retired=false "
                    + " and type(c)!=:class"
                    + " and (upper(c.person.name) like :q or upper(c.code) like :p) "
                    + " order by c.person.name";
            hm.put("q", "%" + getSelectText().toUpperCase() + "%");
            hm.put("p", "%" + getSelectText().toUpperCase() + "%");
        }

        hm.put("class", Consultant.class);
        selectedItems = getFacade().findBySQL(sql, hm);

        return selectedItems;
    }

    public List<Staff> completeItems(String qry) {
        HashMap hm = new HashMap();
        String sql = "select c from Staff c "
                + " where c.retired=false "
                + " and upper(c.person.name) like :q "
                + " or upper(c.code) like :q "
                + " order by c.person.name";
        hm.put("q", "%" + qry.toUpperCase() + "%");
        List<Staff> s = getFacade().findBySQL(sql, hm, 20);
        return s;
    }

    public StreamedContent getScCircular() {
        return scCircular;
    }

    public void setScCircular(StreamedContent scCircular) {
        this.scCircular = scCircular;
    }

    public StreamedContent getScCircularById() {
        return scCircularById;
    }

    public void setScCircularById(StreamedContent scCircularById) {
        this.scCircularById = scCircularById;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public void prepareAdd() {
        current = new Staff();
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

    public void setSelectedItems(List<Staff> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    private void recreateModel() {
        items = null;
    }

    public void saveSelected() {
        if (current == null) {
            UtilityController.addErrorMessage("Nothing to save");
            return;
        }
        if (current.getPerson() == null) {
            UtilityController.addErrorMessage("Nothing to save");
            return;
        }
        if (current.getSpeciality() == null) {
            UtilityController.addErrorMessage("Plaese Select Speciality.");
            return;
        }

        System.out.println("current.getId() = " + current.getId());
        System.out.println("current.getPerson().getId() = " + current.getPerson().getId());

//        if (current.getPerson().getId() == null || current.getPerson().getId() == 0) {
//            getPersonFacade().create(current.getPerson());
//        } else {
//            getPersonFacade().edit(current.getPerson());
//        }

        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getPersonFacade().edit(current.getPerson());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Staff Details Updated");
        } else {
            current.getPerson().setCreatedAt(new Date());
            current.getPerson().setCreater(getSessionController().getLoggedUser());
            getPersonFacade().create(current.getPerson());

            current.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
            UtilityController.addSuccessMessage("New Staff Created");
        }


        recreateModel();
        getItems();
    }


    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public StaffFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(StaffFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public StaffController() {
    }

    public Staff getCurrent() {
        if (current == null) {
            Person p = new Person();
            current = new Staff();
            current.setPerson(p);
        }
        return current;
    }

    public void setCurrent(Staff current) {
        this.current = current;
        getSignature();
    }


    private StaffFacade getFacade() {
        return ejbFacade;
    }

    List<Staff> staffes;
    List<Staff> selectedStaffes;
    double resetStaffBalance;

    public double getResetStaffBalance() {
        return resetStaffBalance;
    }

    public void setResetStaffBalance(double resetStaffBalance) {
        this.resetStaffBalance = resetStaffBalance;
    }

    public List<Staff> getSelectedStaffes() {
        return selectedStaffes;
    }

    public void setSelectedStaffes(List<Staff> selectedStaffes) {
        this.selectedStaffes = selectedStaffes;
    }

    public List<Staff> getStaffes() {
        return staffes;
    }

    public void setStaffes(List<Staff> staffes) {
        this.staffes = staffes;
    }

    public String admin_staff_view_signature() {
        fillStaffes();
        return "/admin_staff_view_signature";
    }

    public String admin_edit_staff_balance() {
        fillStaffes();
        return "/admin_edit_staff_balance";
    }

    public void fillStaffes() {
        String temSql;
        temSql = "SELECT i FROM Staff i where i.retired=false and i.person is not null and i.person.name is not null order by i.person.name";
        staffes = getFacade().findBySQL(temSql);
    }

    public List<Staff> getItems() {
        String temSql;
        temSql = "SELECT i FROM Staff i where i.retired=false and i.person is not null and i.person.name is not null order by i.person.name";
        items = getFacade().findBySQL(temSql);
        return items;
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    public DepartmentFacade getDepartmentFacade() {
        return departmentFacade;
    }

    public void setDepartmentFacade(DepartmentFacade departmentFacade) {
        this.departmentFacade = departmentFacade;
    }

    public List<Staff> getFilteredStaff() {
        return filteredStaff;
    }

    public void setFilteredStaff(List<Staff> filteredStaff) {
        this.filteredStaff = filteredStaff;
    }

    public Staff getSelectedStaff() {
        return selectedStaff;
    }

    public void setSelectedStaff(Staff selectedStaff) {
        this.selectedStaff = selectedStaff;
    }


    public List<Staff> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(List<Staff> selectedList) {
        this.selectedList = selectedList;
    }

    /**
     *
     */
    @FacesConverter(forClass = Staff.class)
    public static class StaffControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            StaffController controller = (StaffController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "staffController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            if (value == null || value.trim().equals("null")) {
                value = "";
            }
            try {
                key = Long.valueOf(value);
            } catch (Exception e) {
                key = 0l;
            }
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
            if (object instanceof Staff) {
                Staff o = (Staff) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + StaffController.class.getName());
            }
        }
    }

    @FacesConverter("stfcon")
    public static class StaffConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            StaffController controller = (StaffController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "staffController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            if (value == null || value.trim().equals("null")) {
                value = "";
            }
            try {
                key = Long.valueOf(value);
            } catch (Exception e) {
                key = 0l;
            }
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
            if (object instanceof Staff) {
                Staff o = (Staff) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + StaffController.class.getName());
            }
        }
    }
}
