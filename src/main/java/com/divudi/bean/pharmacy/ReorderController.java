package com.divudi.bean.pharmacy;

import com.divudi.bean.UtilityController;
import com.divudi.data.BillType;
import com.divudi.entity.BillItem;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Person;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.PharmaceuticalItem;
import com.divudi.entity.pharmacy.Reorder;
import com.divudi.facade.ReorderFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import org.apache.tools.ant.util.facade.FacadeTaskHelper;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.primefaces.event.RowEditEvent;

@Named
@SessionScoped
public class ReorderController implements Serializable {

    private Reorder current;
    private List<Reorder> items = null;
    @EJB
    ReorderFacade ejbFacade;

    Department department;
    Institution institution;
    Person person;

    @Inject
    AmpController ampController;

    List<Reorder> departmentReorders;

    public void onEdit(RowEditEvent event) {

        Reorder tmp = (Reorder) event.getObject();
        getEjbFacade().edit(tmp);
        UtilityController.addSuccessMessage("Reorder Level Updted");
    }

    public void fillDepartmentReorders() {
        if (department == null) {
            return;
        }
        items = new ArrayList<>();
        List<Amp> amps = getAmpController().getItems();

        for (Amp a : amps) {
            Reorder r;
            Map m = new HashMap();
            m.put("d", department);
            m.put("i", a);
            String sql = "Select r from Reorder r where r.item=:i and r.department=:d";
            r = getEjbFacade().findFirstBySQL(sql, m);
            if (r == null) {
                r = new Reorder();
                r.setDepartment(department);
                r.setItem(a);
                r.setMonthsConsideredForShortTermAnalysis(12);
                r.setYearsConsideredForLognTermAnalysis(5);

                r.setPurchaseCycleDurationInDays(calculateOrderingCycleDurationInDays(r));
                r.setDemandInUnitsPerDay(calculateDailyDemandInUnits(r));
                r.setLeadTimeInDays(calculateLeadTime(r));
                
                r.setRoq(calculateRoq(r));
                

                getEjbFacade().create(r);

            }
            items.add(r);
        }
    }

    public double calculateRoq(Reorder reorder){
        int numberOfDaysToOrder;
        if(reorder.getPurchaseCycleDurationInDays() < reorder.getLeadTimeInDays()){
           numberOfDaysToOrder = reorder.getLeadTimeInDays();
        }else{
            numberOfDaysToOrder = reorder.getPurchaseCycleDurationInDays();
        }
        return numberOfDaysToOrder * reorder.getDemandInUnitsPerDay();
    }
    
    public int calculateLeadTime(Reorder reorder) {
        String jpql;
        Map m = new HashMap();
        DateTime dt = new DateTime();
        DateTime tfd = dt.minusMonths(reorder.getMonthsConsideredForShortTermAnalysis());
        Date fd = tfd.toDate();
        Date td = new Date();

        BillItem bi = new BillItem();
        bi.getReferanceBillItem();

        jpql = "Select avg(b.createdAt - rb.createdAt) "
                + " from BillItem bi "
                + " join bi.bill b "
                + " joib bi.referanceBillItem rbi "
                + " join rbi.bill rb "
                + " where b.billType in :bts "
                + " and rb.billType in :rbts "
                + " and bi.item=:amp "
                + " and b.createdAt between :fd and :td "
                + " ";

        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacyOrderApprove);

        List<BillType> rbts = new ArrayList<>();
        bts.add(BillType.PharmacyGrnBill);

        m.put("bts", bts);
        m.put("rbts", rbts);
        m.put("amp", reorder.getItem());
        m.put("fd", fd);
        m.put("td", td);
        Object[] obj = ejbFacade.findSingleAggregate(jpql, m);

        System.err.println("obj = " + obj);
        
        if (obj == null) {
            return 7;
        }
        int avgLeadTimeInDays;
        
        try {
            Long avgLeadTimeInMs;
            avgLeadTimeInMs = (Long) obj[0];
            avgLeadTimeInDays = (int) ( avgLeadTimeInMs / (1000*60*60*24));
        } catch (Exception e) {
            avgLeadTimeInDays = 7;
        }
        return avgLeadTimeInDays;
    }

    public double calculateDailyDemandInUnits(Reorder reorder) {
        String jpql;
        Map m = new HashMap();
        DateTime dt = new DateTime();
        DateTime tfd = dt.minusMonths(reorder.getMonthsConsideredForShortTermAnalysis());
        Date fd = tfd.toDate();
        Date td = new Date();

//        BillItem bi = new BillItem();
//        bi.getQty();
        jpql = "Select max(b.createdAt),min(b.createdAt),sum(bi.qty) "
                + " from BillItem bi "
                + " join bi.bill b "
                + " where b.billType in :bts"
                + " and bi.item=:amp "
                + " and b.createdAt between :fd and :td "
                + " ";

        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacyAdjustment);
        bts.add(BillType.PharmacyPre);
        bts.add(BillType.PharmacyBhtPre);
        bts.add(BillType.PharmacyIssue);
        m.put("bts", bts);
        m.put("amp", reorder.getItem());
        m.put("fd", fd);
        m.put("td", td);
        Object[] obj = ejbFacade.findSingleAggregate(jpql, m);
        if (obj == null) {
            return 14;
        }
        Date minDate;
        Date maxDate;
        Double totalQty;

        try {
            minDate = (Date) obj[0];
        } catch (Exception e) {
            minDate = new Date();
        }
        try {
            maxDate = (Date) obj[1];
        } catch (Exception e) {
            maxDate = new Date();
        }
        try {
            totalQty = (Double) obj[2];
        } catch (Exception e) {
            totalQty = 0.0;
        }

        DateTime mind = new DateTime(minDate);
        DateTime maxd = new DateTime(maxDate);
        Days daysDiff = Days.daysBetween(mind, maxd);

        int ds = daysDiff.getDays();
        return (totalQty / ds);
    }

    public int calculateOrderingCycleDurationInDays(Reorder reorder) {
        String jpql;
        Map m = new HashMap();

        DateTime dt = new DateTime();
        DateTime tfd = dt.minusMonths(reorder.getMonthsConsideredForShortTermAnalysis());

        Date fd = tfd.toDate();
        Date td = new Date();

        jpql = "Select max(b.createdAt),min(b.createdAt),count(b) "
                + " from BillItem bi "
                + " join bi.bill b "
                + " where b.billType in :bts"
                + " and bi.item=:amp "
                + " and b.createdAt between :fd and :td "
                + " ";

        List<BillType> bts = new ArrayList<>();
        bts.add(BillType.PharmacyPurchaseBill);
        bts.add(BillType.PharmacyGrnBill);
        m.put("bts", bts);
        m.put("amp", reorder.getItem());
        m.put("fd", fd);
        m.put("td", td);

        Object[] obj = ejbFacade.findSingleAggregate(jpql, m);
        if (obj == null) {
            return 14;
        }
        Date minDate;
        Date maxDate;
        int count;

        try {
            minDate = (Date) obj[0];
        } catch (Exception e) {
            minDate = new Date();
        }
        try {
            maxDate = (Date) obj[1];
        } catch (Exception e) {
            maxDate = new Date();
        }
        try {
            count = (int) obj[2];
        } catch (Exception e) {
            count = 1;
        }

        if (count == 0) {
            count = 1;
        }
        DateTime mind = new DateTime(minDate);
        DateTime maxd = new DateTime(maxDate);

        Days daysDiff = Days.daysBetween(mind, maxd);

        int ds = daysDiff.getDays();

        return (int) (ds / count);

    }

    public AmpController getAmpController() {
        return ampController;
    }

    public void setAmpController(AmpController ampController) {
        this.ampController = ampController;
    }

    public List<Reorder> getDepartmentReorders() {
        return departmentReorders;
    }

    public void setDepartmentReorders(List<Reorder> departmentReorders) {
        this.departmentReorders = departmentReorders;
    }

    public Reorder getCurrent() {
        return current;
    }

    public void setCurrent(Reorder current) {
        this.current = current;
    }

    public List<Reorder> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<Reorder> items) {
        this.items = items;
    }

    public ReorderFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(ReorderFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public ReorderController() {
    }

    public Reorder getReorder(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Reorder.class)
    public static class ReorderControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ReorderController controller = (ReorderController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "reorderController");
            return controller.getReorder(getKey(value));
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
            if (object instanceof Reorder) {
                Reorder o = (Reorder) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Reorder.class.getName());
            }
        }

    }

}
