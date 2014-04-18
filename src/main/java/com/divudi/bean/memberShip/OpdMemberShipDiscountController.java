/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.memberShip;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.entity.Category;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.PriceMatrix;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.memberShip.MembershipScheme;
import com.divudi.entity.memberShip.OpdMemberShipDiscount;
import com.divudi.facade.PriceMatrixFacade;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class OpdMemberShipDiscountController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private PriceMatrixFacade ejbFacade;
    private PriceMatrix current;
    private List<PriceMatrix> items = null;
    BillType billType;
    PaymentScheme paymentScheme;
    PaymentMethod paymentMethod;
    MembershipScheme membershipScheme;
    Category category;
    Institution institution;
    Department department;
    double fromPrice;
    double toPrice;
    double margin;
    private Category roomLocation;

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public MembershipScheme getMembershipScheme() {
        return membershipScheme;
    }

    public void setMembershipScheme(MembershipScheme membershipScheme) {
        this.membershipScheme = membershipScheme;
    }

    public void recreateModel() {
        fromPrice = toPrice + 1;
        toPrice = 0.0;
        margin = 0;
        items = null;
        membershipScheme = null;
        paymentMethod = null;
    }

    public void saveSelectedDepartment() {

        if (membershipScheme == null) {
            UtilityController.addErrorMessage("Membership Scheme");
            return;
        }

        if (department == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }

        if (paymentMethod == null) {
            UtilityController.addErrorMessage("Please select Payment Method");
            return;
        }

        PriceMatrix a = new OpdMemberShipDiscount();

        a.setMembershipScheme(membershipScheme);
        a.setPaymentMethod(paymentMethod);
        a.setDepartment(department);       
        a.setInstitution(department.getInstitution());
        a.setDiscountPercent(margin);
        a.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        a.setCreater(getSessionController().getLoggedUser());
        getFacade().create(a);
        UtilityController.addSuccessMessage("savedNewSuccessfully");
        //    recreateModel();

        createItemsDepartments();
    }

    public void saveSelectedCategory() {

        if (membershipScheme == null) {
            UtilityController.addErrorMessage("Membership Scheme");
            return;
        }

        if (category == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }

        if (paymentMethod == null) {
            UtilityController.addErrorMessage("Please select Payment Method");
            return;
        }

        PriceMatrix a = new OpdMemberShipDiscount();

        a.setMembershipScheme(membershipScheme);
        a.setPaymentMethod(paymentMethod);
        a.setCategory(category);
        a.setInstitution(department.getInstitution());
        a.setDiscountPercent(margin);
        a.setCreatedAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
        a.setCreater(getSessionController().getLoggedUser());
        getFacade().create(a);
        UtilityController.addSuccessMessage("savedNewSuccessfully");
        //    recreateModel();

        createItemsCategory();
    }

    public PriceMatrixFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(PriceMatrixFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public OpdMemberShipDiscountController() {
    }

    public PriceMatrix getCurrent() {
        return current;
    }

    public void setCurrent(PriceMatrix current) {

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

    public void deleteDepartment() {
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

        current = null;
        getCurrent();
        filterItems = null;
        createItemsDepartments();
    }

    public void deleteCategory() {
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

        current = null;
        getCurrent();
        filterItems = null;
        createItemsCategory();
    }

    private PriceMatrixFacade getFacade() {
        return ejbFacade;
    }

    private List<PriceMatrix> filterItems;

    public List<PriceMatrix> getItems() {

        return items;
    }

    public void createItemsDepartments() {
        filterItems = null;
        String sql;
        sql = "select a from OpdMemberShipDiscount a "
                + " where a.retired=false "
                + " and a.category is null"
                + " order by a.membershipScheme.name,a.department.name,a.fromPrice";
        items = getFacade().findBySQL(sql);
    }

    public void createItemsCategory() {
        filterItems = null;
        String sql;
        sql = "select a from OpdMemberShipDiscount a "
                + " where a.retired=false "
                + " and a.department is null"
                + " order by a.membershipScheme.name,a.category.name,a.fromPrice";
        items = getFacade().findBySQL(sql);
    }

    public void onEdit(PriceMatrix tmp) {
        //Cheking Minus Value && Null
        getFacade().edit(tmp);

        //  createItems();
    }

    public Category getRoomLocation() {

        return roomLocation;
    }

    public void setRoomLocation(Category roomLocation) {
        this.roomLocation = roomLocation;
    }

    public List<PriceMatrix> getFilterItems() {
        return filterItems;
    }

    public void setFilterItems(List<PriceMatrix> filterItems) {
        this.filterItems = filterItems;
    }

}
