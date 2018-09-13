/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import com.divudi.data.BillNumber;
import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.entity.Bill;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Logins;
import com.divudi.entity.WebUser;
import com.divudi.facade.BillFacade;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.TemporalType;

/**
 *
 * @author Buddhika
 */
@Named
@ApplicationScoped
public class ApplicationController {

    Date startTime;
    Date storesExpiery;
    @EJB
    BillFacade billFacade;
    private List<BillNumber> billNumbers;

    public String institutionBillNumberGenerator(Department dep, Bill bill, BillType billType, BillNumberSuffix billNumberSuffix) {
        BillNumber bn = null;
        for (BillNumber tbn : getBillNumbers()) {
            if (tbn.getBillClass() == null) {
                continue;
            }
            if (tbn.getDepartment() == null) {
                continue;
            }
            if (tbn.getBillType() == null) {
                continue;
            }
            if (!tbn.getBillClass().equals(bill.getClass())) {
                continue;
            }
            if (!tbn.getDepartment().equals(dep)) {
                continue;
            }
            if (!tbn.getBillType().equals(billType)) {
                continue;
            }
            if (tbn.getLastNumber() == null) {
                Long temNo = getInstitutionBillNumberFromDatabase(dep, bill, billType, billNumberSuffix);
                if (temNo == null) {
                    temNo = 0l;
                }
                tbn.setLastNumber(temNo + 1);
            } else {
                tbn.setLastNumber(tbn.getLastNumber() + 1);
            }
            bn = tbn;
        }
        if (bn == null) {
            bn = new BillNumber();
            bn.setBillClass(bill.getClass());
            bn.setBillType(billType);
            bn.setDepartment(dep);
            Long temNo = getInstitutionBillNumberFromDatabase(dep, bill, billType, billNumberSuffix);
            if (temNo == null) {
                temNo = 0l;
            }
            bn.setLastNumber(temNo + 1);
            billNumbers.add(bn);
        }

        Long i = bn.getLastNumber() ;

        String result = "";

        if (billNumberSuffix != BillNumberSuffix.NONE) {
            result = dep.getDepartmentCode() + billNumberSuffix + "/" + (i + 1);
        } else {
            result = dep.getDepartmentCode() + "/" + (i + 1);
        }

        return result;
    }

    public Long getInstitutionBillNumberFromDatabase(Department dep, Bill bill, BillType billType, BillNumberSuffix billNumberSuffix) {
        String sql = "SELECT count(b) FROM Bill b where type(b)=:type and b.retired=false AND "
                + " b.department=:dep and b.createdAt is not null AND b.billType=:btp ";
        String result = "";
        HashMap hm = new HashMap();
        hm.put("dep", dep);
        hm.put("btp", billType);
        hm.put("type", bill.getClass());

        Long i = getBillFacade().findAggregateLong(sql, hm, TemporalType.DATE);

        return i;
    }

    public Long getInstitutionBillNumberFromDatabase(Institution ins, Bill bill, BillType billType, BillNumberSuffix billNumberSuffix) {
        String sql = "SELECT count(b) FROM Bill b where type(b)=:type and b.retired=false AND "
                + " b.institution=:ins and b.createdAt is not null AND b.billType=:btp ";
        HashMap hm = new HashMap();
        hm.put("ins", ins);
        hm.put("btp", billType);
        hm.put("type", bill.getClass());
        Long i = getBillFacade().findAggregateLong(sql, hm, TemporalType.DATE);
        return i;
    }

    public String institutionBillNumberGenerator(Institution ins, Bill bill, BillType billType, BillNumberSuffix billNumberSuffix) {

        BillNumber bn = null;
        for (BillNumber tbn : getBillNumbers()) {
            if (tbn.getBillClass() == null) {
                continue;
            }
            if (tbn.getInstitution() == null) {
                continue;
            }
            if (tbn.getBillType() == null) {
                continue;
            }
            if (!tbn.getBillClass().equals(bill.getClass())) {
                continue;
            }
            if (!tbn.getInstitution().equals(ins)) {
                continue;
            }
            if (!tbn.getBillType().equals(billType)) {
                continue;
            }
            if (tbn.getLastNumber() == null) {
                Long temNo = getInstitutionBillNumberFromDatabase(ins, bill, billType, billNumberSuffix);
                if (temNo == null) {
                    temNo = 0l;
                }
                tbn.setLastNumber(temNo + 1);
            } else {
                tbn.setLastNumber(tbn.getLastNumber() + 1);
            }
            bn = tbn;
        }
        if (bn == null) {
            bn = new BillNumber();
            bn.setBillClass(bill.getClass());
            bn.setBillType(billType);
            bn.setInstitution(ins);
            Long temNo = getInstitutionBillNumberFromDatabase(ins, bill, billType, billNumberSuffix);
            if (temNo == null) {
                temNo = 0l;
            }
            bn.setLastNumber(temNo + 1);
            billNumbers.add(bn);
        }
        String result = "";
        Long i = bn.getLastNumber();

        if (billNumberSuffix != BillNumberSuffix.NONE) {
            result = ins.getInstitutionCode() + billNumberSuffix + "/" + (i + 1);
        } else {
            result = ins.getInstitutionCode() + "/" + (i + 1);
        }

        return result;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @PostConstruct
    public void recordStart() {
        startTime = Calendar.getInstance().getTime();
//        if (sessionControllers == null) {
//            sessionControllers = new ArrayList<>();
//        }
    }

    List<Logins> loggins;

    public List<Logins> getLoggins() {
        if (loggins == null) {
            loggins = new ArrayList<>();
        }
        return loggins;
    }

    public void setLoggins(List<Logins> loggins) {
        this.loggins = loggins;
    }

    public Logins isLogged(WebUser u) {
        Logins tl = null;
        for (Logins l : getLoggins()) {
            if (l.getWebUser().equals(u)) {
                tl = l;
            }
        }
        return tl;
    }

    public void addToLoggins(SessionController sc) {
        Logins login = sc.getThisLogin();
        loggins.add(login);
        try {
//            for (SessionController s : getSessionControllers()) {
//                if (s.getLoggedUser().equals(login.getWebUser())) {
//                    //////System.out.println("making log out");
//                    s.logout();
//                }
//            }
//            getSessionControllers().add(sc);
        } catch (Exception e) {
            //////System.out.println("Error in addToLogins of Application controller." + e.getMessage());
        }
    }

    public void removeLoggins(SessionController sc) {
        Logins login = sc.getThisLogin();
        //////System.out.println("sessions logged before removing is " + getLoggins().size());
        loggins.remove(login);
//        sessionControllers.remove(sc);
    }

    /**
     * Creates a new instance of ApplicationController
     */
    public ApplicationController() {
    }

    public Date getStoresExpiery() {
        if (storesExpiery == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, 2020);
            c.set(Calendar.MONTH, 0);
            c.set(Calendar.DATE, 1);
            storesExpiery = c.getTime();
        }
        return storesExpiery;
    }

    public void setStoresExpiery(Date storesExpiery) {
        this.storesExpiery = storesExpiery;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public List<BillNumber> getBillNumbers() {
        if (billNumbers == null) {
            billNumbers = new ArrayList<>();
        }
        return billNumbers;
    }

    public void setBillNumbers(List<BillNumber> billNumbers) {
        this.billNumbers = billNumbers;
    }

}
