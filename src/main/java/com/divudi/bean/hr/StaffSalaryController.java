/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.hr;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.dataStructure.ExtraDutyCount;
import com.divudi.data.dataStructure.OtNormalSpecial;
import com.divudi.data.hr.PaysheetComponentType;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.HumanResourceBean;
import com.divudi.entity.Staff;
import com.divudi.entity.hr.StaffPaysheetComponent;
import com.divudi.entity.hr.StaffSalary;
import com.divudi.entity.hr.StaffSalaryComponant;
import com.divudi.facade.StaffEmploymentFacade;
import com.divudi.facade.StaffFacade;
import com.divudi.facade.StaffPaysheetComponentFacade;
import com.divudi.facade.StaffSalaryComponantFacade;
import com.divudi.facade.StaffSalaryFacade;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author safrin
 */
@Named
@ViewScoped
public class StaffSalaryController implements Serializable {

    private StaffSalary current;
    private Date date;
    //////////   
    List<StaffSalary> items;
    ///////
    @EJB
    private StaffSalaryFacade staffSalaryFacade;
    @EJB
    private StaffPaysheetComponentFacade staffPaysheetComponentFacade;
    @EJB
    private StaffSalaryComponantFacade staffSalaryComponantFacade;
    @EJB
    private StaffEmploymentFacade staffEmploymentFacade;
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private HumanResourceBean humanResourceBean;
    @EJB
    private CommonFunctions commonFunctions;
    /////////////
    @Inject
    private SessionController sessionController;
    @Inject
    private HrmVariablesController hrmVariablesController;
    @Inject
    private StaffController staffController;

    public void remove() {
        getCurrent().setRetired(true);
        getCurrent().setRetiredAt(new Date());
        getCurrent().setRetirer(getSessionController().getLoggedUser());

        getStaffSalaryFacade().edit(getCurrent());
        current = null;
    }

    private boolean errorCheck() {
        if (getDate() == null) {
            UtilityController.addErrorMessage("Please Select Date");
            return true;
        }

        if (getCurrent().getStaff() == null) {
            UtilityController.addErrorMessage("Please Select Staff");
            return true;
        }

        if (getHumanResourceBean().checkExistingSalary(getDate(), getCurrent().getStaff())) {
            UtilityController.addErrorMessage("There is Already defined Salary for this salary cycle please edit");
            return true;
        }

        return false;
    }

    public void save() {

        if (getCurrent().getId() != null) {
            getStaffSalaryFacade().edit(getCurrent());
            return;
        }

        if (errorCheck()) {
            return;
        }

        getCurrent().getSalaryCycle().setFromDate(getCommonFunctions().getStartOfMonth(getDate()));
        getCurrent().getSalaryCycle().setToDate(getCommonFunctions().getEndOfMonth(getDate()));
        getCurrent().setCreatedAt(new Date());
        getCurrent().setCreater(getSessionController().getLoggedUser());
        getStaffSalaryFacade().create(getCurrent());
        updateComponent();

        makeNull();
    }

    private void updateComponent() {
        for (StaffSalaryComponant ssc : getCurrent().getStaffSalaryComponants()) {
            ssc.setStaffSalary(getCurrent());
            getStaffSalaryComponantFacade().edit(ssc);
        }

    }

    public void onEdit(RowEditEvent event) {
        ////System.out.println("Runn");
        StaffSalaryComponant tmp = (StaffSalaryComponant) event.getObject();
        getHumanResourceBean().setEpf(tmp, getHrmVariablesController().getEpfRate(), getHrmVariablesController().getEpfCompanyRate());
        getHumanResourceBean().setEtf(tmp, getHrmVariablesController().getEtfRate(), getHrmVariablesController().getEtfCompanyRate());
        tmp.setLastEditedAt(new Date());
        tmp.setLastEditor(getSessionController().getLoggedUser());
        getStaffSalaryComponantFacade().edit(tmp);
    }

    public StaffSalaryController() {
    }

    public void makeNull() {
        current = null;
        items = null;
    }

    public void clear() {
        current = null;
        date = null;
        items = null;
    }

    public StaffSalary getCurrent() {
        if (current == null) {
            current = new StaffSalary();

        }
        return current;
    }

    public void setCurrent(StaffSalary current) {
        this.current = current;
        current.setTmpOtNormalSpecial(getHumanResourceBean().calOt(getDate(), getCurrent().getStaff()));
        current.setTmpExtraDutyCount(getHumanResourceBean().calExtraDuty(getDate(), getCurrent().getStaff()));
    }

    public StaffSalaryFacade getStaffSalaryFacade() {
        return staffSalaryFacade;
    }

    public void setStaffSalaryFacade(StaffSalaryFacade staffSalaryFacade) {
        this.staffSalaryFacade = staffSalaryFacade;
    }

//    private boolean checkBasic() {
//        for (StaffSalaryComponant ss : getStaffSalaryComponants()) {
//            if (ss.getStaffPaysheetComponent().getPaysheetComponent().getComponentType() == PaysheetComponentType.BasicSalary) {
//                return true;
//            }
//        }
//
//        return false;
//    }
    private void setBasic() {
        StaffSalaryComponant ss = new StaffSalaryComponant();
        ss.setCreatedAt(new Date());
        ss.setCreater(getSessionController().getLoggedUser());
        ss.setStaffPaysheetComponent(getHumanResourceBean().getBasic(getCurrent().getStaff()));
        if (ss.getStaffPaysheetComponent() != null) {
            ss.setComponantValue(ss.getStaffPaysheetComponent().getStaffPaySheetComponentValue());
        } else {
            return;
        }

        getHumanResourceBean().setEpf(ss, getHrmVariablesController().getEpfRate(), getHrmVariablesController().getEpfCompanyRate());
        getHumanResourceBean().setEtf(ss, getHrmVariablesController().getEtfRate(), getHrmVariablesController().getEtfCompanyRate());

        getCurrent().getStaffSalaryComponants().add(ss);

    }

    private void setOT() {
        StaffSalaryComponant ss = new StaffSalaryComponant();
        ss.setCreatedAt(new Date());
        ss.setCreater(getSessionController().getLoggedUser());
        ss.setStaffPaysheetComponent(getHumanResourceBean().getComponent(getCurrent().getStaff(), getSessionController().getLoggedUser(), PaysheetComponentType.OT));
        if (ss.getStaffPaysheetComponent() != null) {
            OtNormalSpecial otNormalSpecial = getHumanResourceBean().calOt(getDate(), getCurrent().getStaff());
            ss.setComponantValue(otNormalSpecial.getNormalValue() + otNormalSpecial.getSpecialValue());
        } else {
            return;
        }

        getHumanResourceBean().setEpf(ss, getHrmVariablesController().getEpfRate(), getHrmVariablesController().getEpfCompanyRate());
        getHumanResourceBean().setEtf(ss, getHrmVariablesController().getEtfRate(), getHrmVariablesController().getEtfCompanyRate());

        getCurrent().getStaffSalaryComponants().add(ss);

    }

    private void setExtraDuty() {
        StaffSalaryComponant ss = new StaffSalaryComponant();
        ss.setCreatedAt(new Date());
        ss.setCreater(getSessionController().getLoggedUser());
        ss.setStaffPaysheetComponent(getHumanResourceBean().getComponent(getCurrent().getStaff(), getSessionController().getLoggedUser(), PaysheetComponentType.ExtraDuty));
        if (ss.getStaffPaysheetComponent() != null) {
            List<ExtraDutyCount> extraDutyCounts = getHumanResourceBean().calExtraDuty(getDate(), getCurrent().getStaff());

            ss.setComponantValue(0);
        } else {
            return;
        }

        getHumanResourceBean().setEpf(ss, getHrmVariablesController().getEpfRate(), getHrmVariablesController().getEpfCompanyRate());
        getHumanResourceBean().setEtf(ss, getHrmVariablesController().getEtfRate(), getHrmVariablesController().getEtfCompanyRate());

        getCurrent().getStaffSalaryComponants().add(ss);

    }

    private void setNoPay() {
        StaffSalaryComponant ss = new StaffSalaryComponant();
        ss.setCreatedAt(new Date());
        ss.setCreater(getSessionController().getLoggedUser());
        ss.setStaffPaysheetComponent(getHumanResourceBean().getComponent(getCurrent().getStaff(), getSessionController().getLoggedUser(), PaysheetComponentType.No_Pay_Deduction));
        if (ss.getStaffPaysheetComponent() != null) {
            ss.setComponantValue(0);
        } else {
            return;
        }

        getHumanResourceBean().setEpf(ss, getHrmVariablesController().getEpfRate(), getHrmVariablesController().getEpfCompanyRate());
        getHumanResourceBean().setEtf(ss, getHrmVariablesController().getEtfRate(), getHrmVariablesController().getEtfCompanyRate());

        getCurrent().getStaffSalaryComponants().add(ss);

    }

    public void addSalaryComponent() {

        if (getDate() != null && getCurrent().getStaff() != null) {

            setBasic();

            for (StaffPaysheetComponent spc : getHumanResourceBean().getStaffPaysheetComponentWithoutBasic(getCurrent().getStaff(), getDate())) {
                StaffSalaryComponant ss = new StaffSalaryComponant();
                ss.setCreatedAt(new Date());
                ss.setCreater(getSessionController().getLoggedUser());
                ss.setComponantValue(spc.getStaffPaySheetComponentValue());
                ss.setStaffPaysheetComponent(spc);
                getHumanResourceBean().setEpf(ss, getHrmVariablesController().getEpfRate(), getHrmVariablesController().getEpfCompanyRate());
                getHumanResourceBean().setEtf(ss, getHrmVariablesController().getEtfRate(), getHrmVariablesController().getEtfCompanyRate());

                getCurrent().getStaffSalaryComponants().add(ss);
            }

            setOT();
            setExtraDuty();
            setNoPay();
        }

    }

    public void generate() {
        for (Staff s : getHumanResourceBean().getStaffForSalary()) {
            setCurrent(getHumanResourceBean().getStaffSalary(s, getDate()));
            addSalaryComponent();
            save();
            current = null;
        }
    }

    public List<StaffSalary> getItems() {
        if (items == null) {
            String sql = "Select s From StaffSalary s where s.retired=false and s.salaryCycle.fromDate>=:fd "
                    + "and s.salaryCycle.toDate<=:td";

            HashMap hm = new HashMap<>();
            hm.put("fd", getCommonFunctions().getStartOfMonth(getDate()));
            hm.put("td", getCommonFunctions().getEndOfMonth(getDate()));

            items = getStaffSalaryFacade().findBySQL(sql, hm, TemporalType.DATE);
        }
        return items;
    }

    public StaffPaysheetComponentFacade getStaffPaysheetComponentFacade() {
        return staffPaysheetComponentFacade;
    }

    public void setStaffPaysheetComponentFacade(StaffPaysheetComponentFacade staffPaysheetComponentFacade) {
        this.staffPaysheetComponentFacade = staffPaysheetComponentFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public HrmVariablesController getHrmVariablesController() {
        return hrmVariablesController;
    }

    public void setHrmVariablesController(HrmVariablesController hrmVariablesController) {
        this.hrmVariablesController = hrmVariablesController;
    }

    public StaffSalaryComponantFacade getStaffSalaryComponantFacade() {
        return staffSalaryComponantFacade;
    }

    public void setStaffSalaryComponantFacade(StaffSalaryComponantFacade staffSalaryComponantFacade) {
        this.staffSalaryComponantFacade = staffSalaryComponantFacade;
    }

    public StaffEmploymentFacade getStaffEmploymentFacade() {
        return staffEmploymentFacade;
    }

    public void setStaffEmploymentFacade(StaffEmploymentFacade staffEmploymentFacade) {
        this.staffEmploymentFacade = staffEmploymentFacade;
    }

    public StaffFacade getStaffFacade() {
        return staffFacade;
    }

    public void setStaffFacade(StaffFacade staffFacade) {
        this.staffFacade = staffFacade;
    }

    public StaffController getStaffController() {
        return staffController;
    }

    public void setStaffController(StaffController staffController) {
        this.staffController = staffController;
    }

    public HumanResourceBean getHumanResourceBean() {
        return humanResourceBean;
    }

    public void setHumanResourceBean(HumanResourceBean humanResourceBean) {
        this.humanResourceBean = humanResourceBean;
    }

    public Date getDate() {
        if (date == null) {
            date = new Date();
        }
        return date;
    }

    public void setDate(Date date) {
        items = null;
        this.date = date;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    @FacesConverter(forClass = StaffSalary.class)
    public static class StaffSalaryConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            StaffSalaryController controller = (StaffSalaryController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "staffSalaryController");
            return controller.getStaffSalaryFacade().find(getKey(value));
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
            if (object instanceof StaffSalary) {
                StaffSalary o = (StaffSalary) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + StaffSalaryController.class.getName());
            }
        }
    }

    @FacesConverter("staffSalaryCon")
    public static class StaffSalaryControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            StaffSalaryController controller = (StaffSalaryController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "staffSalaryController");
            return controller.getStaffSalaryFacade().find(getKey(value));
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
            if (object instanceof StaffSalary) {
                StaffSalary o = (StaffSalary) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + StaffSalaryController.class.getName());
            }
        }
    }
}
