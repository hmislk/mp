/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.hr;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.hr.DayType;
import com.divudi.data.dataStructure.ShiftTable;
import com.divudi.data.dataStructure.StaffMonthDay;
import com.divudi.data.hr.FingerPrintRecordType;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.HumanResourceBean;
import com.divudi.entity.Staff;
import com.divudi.entity.hr.DayShift;
import com.divudi.entity.hr.FingerPrintRecord;
import com.divudi.entity.hr.PhDate;
import com.divudi.entity.hr.Roster;
import com.divudi.entity.hr.Shift;
import com.divudi.entity.hr.StaffShift;
import com.divudi.facade.DayShiftFacade;
import com.divudi.facade.FingerPrintRecordFacade;
import com.divudi.facade.PhDateFacade;
import com.divudi.facade.ShiftFacade;
import com.divudi.facade.StaffFacade;
import com.divudi.facade.StaffShiftFacade;
import com.divudi.facade.StaffWorkDayFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class ShiftTableController implements Serializable {

    private List<ShiftTable> shiftTables;
    private LinkedList<Staff> staffLink;
    private LinkedList<Staff> repeatedStaff;
    /////////////////////
    private Date date;
    private Staff currentStaff;
    private Roster roster;
    private boolean flag;
    /////////////////
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private ShiftFacade shiftFacade;
    @EJB
    private DayShiftFacade dayShiftFacade;
    @EJB
    private PhDateFacade phDateFacade;
    @EJB
    private StaffShiftFacade staffShiftFacade;
    @EJB
    private StaffWorkDayFacade staffWorkDayFacade;
    @EJB
    private FingerPrintRecordFacade fingerPrintRecordFacade;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private HumanResourceBean humanResourceBean;
    ////////////////
    @Inject
    private SessionController sessionController;

    private void createShiftTable() {
        shiftTables = new ArrayList<>();

        Calendar nc = Calendar.getInstance();
        nc.setTime(getDate());
        Date nowDate = nc.getTime();

        Calendar tc = Calendar.getInstance();
        tc.setTime(nowDate);
        tc.add(Calendar.DATE, 7);
        Date toDate = tc.getTime();

        while (toDate.after(nowDate)) {
            ShiftTable netT = new ShiftTable();
            netT.setDate(nowDate);

            List<StaffShift> ss = getExistingStaffShift(nowDate);
            if (ss.isEmpty()) {

                netT.setStaffShift(saveStaffShift(nowDate));
            } else {

                netT.setStaffShift(ss);
            }

            shiftTables.add(netT);

            Calendar c = Calendar.getInstance();
            c.setTime(nowDate);
            c.add(Calendar.DATE, 1);
            nowDate = c.getTime();

        }
    }

    private List<StaffShift> saveStaffShift(Date date) {
        List<StaffShift> tmp = new ArrayList<>();

        for (Shift st : getShift()) {
            for (DayShift ds : getDayShifts(st)) {

                if (!getHumanResourceBean().checkDateWithDayType(date, ds)) {
                    continue;
                }

                int count = 0;

                while (ds.getCount() > count) {

                    StaffShift newSt = new StaffShift();
                    newSt.setCreatedAt(new Date());
                    newSt.setCreater(getSessionController().getLoggedUser());
                    newSt.setDayShift(ds);
                    newSt.setShiftDate(date);
                    newSt.setStartRecord(createFingerPrint());
                    newSt.setEndRecord(createFingerPrint());
                    setRepeatedCount(newSt);

                    getFingerPrintRecordFacade().create(newSt.getStartRecord());
                    getFingerPrintRecordFacade().create(newSt.getEndRecord());
                    getStaffShiftFacade().create(newSt);
                    tmp.add(newSt);

                    count++;
                }

            }
        }

        return tmp;
    }

    private FingerPrintRecord createFingerPrint() {
        FingerPrintRecord fpr = new FingerPrintRecord();
        fpr.setCreatedAt(new Date());
        fpr.setCreater(getSessionController().getLoggedUser());
        fpr.setFingerPrintRecordType(FingerPrintRecordType.Varified);
        return fpr;
    }

//    private boolean checkStaffShift(StaffShift tmp) {
//        String sql = "Select s from StaffShift s where s.retired=false and s.staff=:st and s.shiftDate=:date and s.dayShift=:ds";
//        HashMap hm = new HashMap();
//        hm.put("st", tmp.getStaff());
//        hm.put("ds", tmp.getDayShift());
//        hm.put("date", tmp.getShiftDate());
//        List<StaffShift> st = getStaffShiftFacade().findBySQL(sql, hm, TemporalType.DATE);
//
//        if (st != null && !st.isEmpty()) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//   
    private StaffShift calPrevStaffShift(StaffShift tmp) {

        StaffShift preShift = getPrevStaffShift(tmp);
        StaffShift preDayStaffShift = getPrevDayStaffShift(tmp);

        if (preDayStaffShift != null) {
            preDayStaffShift.setContinuedTo(tmp);
            getStaffShiftFacade().edit(preDayStaffShift);
        }

        if (preShift != null) {
            UtilityController.addSuccessMessage("This Staff continue From : " + preShift.getDayShift().getShift().getName());
            return preShift;
        } else if (preDayStaffShift != null && tmp.getDayShift().getShift().getShiftOrder() == 1) {
            UtilityController.addSuccessMessage("This Staff continue From : " + preDayStaffShift.getDayShift().getShift().getName());
            return preDayStaffShift;
        } else {
            return null;
        }
    }

    public StaffShift getPrevStaffShift(StaffShift tmp) {
        String sql = "Select s from StaffShift s where s.retired=false and s.staff=:st "
                + " and (s.shiftDate=:date  and s.dayShift.shift.shiftOrder="
                + (tmp.getDayShift().getShift().getShiftOrder() - 1) + ")";
        HashMap hm = new HashMap();
        hm.put("st", tmp.getStaff());
        hm.put("date", tmp.getShiftDate());

        return getStaffShiftFacade().findFirstBySQL(sql, hm, TemporalType.DATE);

    }

    public StaffShift getPrevDayStaffShift(StaffShift ss) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ss.getShiftDate());
        cal.add(Calendar.DATE, -1);
        Date beforDate = cal.getTime();
        String sql = "select ss from  StaffShift ss "
                + "where ss.shiftDate=:bDate and ss.staff=:st and "
                + " ss.dayShift.shift.shiftOrder=" + getMaxShiftOrder(ss);
        HashMap hm = new HashMap();
        hm.put("bDate", beforDate);
        hm.put("st", ss.getStaff());

        StaffShift tmp = getStaffShiftFacade().findFirstBySQL(sql, hm);

        return tmp;
    }

    public double getMaxShiftOrder(StaffShift ss) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ss.getShiftDate());
        cal.add(Calendar.DATE, -1);
        Date beforDate = cal.getTime();
        String sql = "Select s from StaffShift s where s.shiftDate=:bDate and s.staff=:st";
        HashMap hm = new HashMap();
        hm.put("bDate", beforDate);
        hm.put("st", ss.getStaff());

        List<StaffShift> tmp = getStaffShiftFacade().findBySQL(sql, hm, TemporalType.DATE);
        double max = 0;
        for (StaffShift sst : tmp) {
            if (sst.getDayShift().getShift().getShiftOrder() > max) {
                max = sst.getDayShift().getShift().getShiftOrder();
            }
        }
        //System.err.println("max " + max);
        return max;

    }

    public StaffShift getFrvDayStaffShift(StaffShift ss) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ss.getShiftDate());
        cal.add(Calendar.DATE, 1);
        Date afterDate = cal.getTime();
        String sql = "select ss  from  StaffShift ss "
                + "where ss.shiftDate=:bDate and ss.staff=:st and "
                + " ss.dayShift.shift.shiftOrder=1";
        HashMap hm = new HashMap();
        hm.put("bDate", afterDate);
        hm.put("st", ss.getStaff());

        return getStaffShiftFacade().findFirstBySQL(sql, hm);

    }

    public StaffShift getFrwStaffShift(StaffShift tmp) {
        String sql = "Select s from StaffShift s where s.retired=false and s.staff=:st "
                + " and (s.shiftDate=:date  and s.dayShift.shift.shiftOrder="
                + (tmp.getDayShift().getShift().getShiftOrder() + 1) + ")";
        HashMap hm = new HashMap();
        hm.put("st", tmp.getStaff());
        hm.put("date", tmp.getShiftDate());
        return getStaffShiftFacade().findFirstBySQL(sql, hm, TemporalType.DATE);

    }

    public StaffShift calFrwStaffShift(StaffShift tmp) {
        if (tmp == null) {
            return null;
        }

        StaffShift frwStaffShift = getFrwStaffShift(tmp);
        StaffShift frwDayStaffShift = getFrvDayStaffShift(tmp);

        if (frwDayStaffShift != null) {
            frwDayStaffShift.setContinuedFrom(tmp);
            getStaffShiftFacade().edit(frwDayStaffShift);
        }

        if (frwStaffShift != null) {
            UtilityController.addSuccessMessage("This Staff continue to : " + frwStaffShift.getDayShift().getShift().getName());
            return frwStaffShift;
        } else if (frwDayStaffShift != null && tmp.getDayShift().getShift().getShiftOrder() == getMaxShiftOrder(tmp)) {
            UtilityController.addSuccessMessage("This Staff continue to : " + frwDayStaffShift.getDayShift().getShift().getName());
            return frwDayStaffShift;
        } else {
            return null;
        }
    }

    private void updateStaffShift(StaffShift tmp) {
        StaffShift prevStaffShift = calPrevStaffShift(tmp);
        if (prevStaffShift != null) {
            tmp.setContinuedFrom(prevStaffShift);
        } else {
            tmp.setContinuedFrom(null);
        }

        StaffShift frwStaffShift = calFrwStaffShift(tmp);
        if (frwStaffShift != null) {
            tmp.setContinuedTo(frwStaffShift);
        } else {
            tmp.setContinuedTo(null);
        }

        if (tmp.getStartRecord().getId() == null) {
            getFingerPrintRecordFacade().create(tmp.getStartRecord());
        } else {
            getFingerPrintRecordFacade().edit(tmp.getStartRecord());
        }

        if (tmp.getEndRecord().getId() == null) {
            getFingerPrintRecordFacade().create(tmp.getEndRecord());
        } else {
            getFingerPrintRecordFacade().edit(tmp.getEndRecord());
        }

        getStaffShiftFacade().edit(tmp);
    }

    public void onEdit(StaffShift tmp) {
        //   StaffShift tmp = (StaffShift) event.getObject();
        currentStaff = tmp.getStaff();

        updateStaffShift(tmp);

    }

    public void onEditBoolean(StaffShift tmp) {
        //   StaffShift tmp = (StaffShift) event.getObject();
        currentStaff = tmp.getStaff();

        getStaffShiftFacade().edit(tmp);

    }

    private List<StaffShift> getExistingStaffShift(Date d) {
        // List<StaffShift> tmp=new ArrayList<>();
        String sql;
        HashMap hm = new HashMap();

        sql = "select st From StaffShift st where st.retired=false and st.shiftDate=:dt and st.dayShift.shift.roster=:rs";
        hm.put("dt", d);
        hm.put("rs", getRoster());
        List<StaffShift> tmp = getStaffShiftFacade().findBySQL(sql, hm, TemporalType.DATE);
        for (StaffShift sst : tmp) {
            updateStaffShift(sst);
            sst.setFingerPrintRecordList(getMissedFingerFrintRecord(sst));
        }
        return tmp;
    }

    private List<FingerPrintRecord> getMissedFingerFrintRecord(StaffShift sst) {
        List<FingerPrintRecord> tmp;
        HashMap hm = new HashMap();
        Date startTime = sst.getShiftStartTime();
        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.add(Calendar.MINUTE, -(60 * 6));
        //System.err.println("min : " + c.getTime());
        Date startMinTime = c.getTime();
        c.add(Calendar.MINUTE, (12 * 60));
        //System.err.println("max : " + c.getTime());
        Date startMaxTime = c.getTime();
        /////////////////
        Date endTime = sst.getShiftEndTime();
        c = Calendar.getInstance();
        c.setTime(endTime);
        c.add(Calendar.MINUTE, -(60 * 6));
        //System.err.println("min : " + c.getTime());
        Date endMinTime = c.getTime();
        c.add(Calendar.MINUTE, (12 * 60));
        //System.err.println("max : " + c.getTime());
        Date endMaxTime = c.getTime();
        ////////////////
        String sql = "Select fpr from FingerPrintRecord fpr where fpr.fingerPrintRecordType=:tp "
                + " and  fpr.staff=:st and fpr.loggedRecord is not null";
        hm.put("tp", FingerPrintRecordType.Varified);
        hm.put("st", sst.getStaff());
        // hm.put("sMax", startMaxTime);
        //   hm.put("sMin", startMinTime);
        //  hm.put("eMax", endMaxTime);
        //   hm.put("eMin", endMinTime);
        tmp = getFingerPrintRecordFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);
        return tmp;
    }

    private List<StaffShift> getPreviousDayShift(StaffShift sst) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(sst.getShiftDate());
        cal.add(Calendar.DATE, -1);
        Date pre = cal.getTime();

        String sql = "Select ss From StaffShift ss where ss.shiftDate=:date and ss.dayShift.shift=:ds ";
        HashMap hm = new HashMap();
        hm.put("date", pre);
        hm.put("ds", sst.getDayShift().getShift());

        return getStaffShiftFacade().findBySQL(sql, hm, TemporalType.DATE);

    }

    private void setRepeatedCount(StaffShift sst) {

        List<StaffShift> tmp = getPreviousDayShift(sst);

        if (!tmp.isEmpty()) {
            StaffShift tmpSst = tmp.get(0);
            int i = tmpSst.getRepeatedCount();

            if (i > 0) {
                sst.setRepeatedCount(--i);
            } else {
                sst.setRepeatedCount(sst.getDayShift().getRepeatedDay());
            }

        } else {
            sst.setRepeatedCount(sst.getDayShift().getRepeatedDay());
        }

    }

    private LinkedList<Staff> createStaffLink() {
        LinkedList<Staff> newStack = new LinkedList<>();
        for (Staff st : getRoster().getStaffList()) {
            newStack.add(st);
        }

        return newStack;
    }

    private List<Shift> getShift() {
        String sql = "Select s From Shift s Where s.retired=false and s.roster=:rs order by s.shiftOrder  ";
        HashMap hm = new HashMap();
        hm.put("rs", getRoster());

        return getShiftFacade().findBySQL(sql, hm, TemporalType.DATE);
    }

    private List<DayShift> getDayShifts(Shift shift) {
        String sql = "Select s From DayShift s Where  s.shift=:sh  ";
        HashMap hm = new HashMap();
        hm.put("sh", shift);

        return getDayShiftFacade().findBySQL(sql, hm, TemporalType.DATE);

    }

    private boolean errorCheckForCreate() {
        if (getRoster() == null) {
            UtilityController.addErrorMessage("Select Roster");
            return true;
        }

        if (getRoster().getStaffList().size() == 0.0) {
            UtilityController.addErrorMessage("Empty Staff");
            return true;
        }

        return false;

    }

    public void makeNull() {
        shiftTables = null;
        staffLink = null;
        repeatedStaff = null;
        //   flag=true;

    }

    public void clear() {
        shiftTables = null;
        staffLink = null;
        repeatedStaff = null;
        /////////////////////
        date = null;
        currentStaff = null;
        roster = null;
        flag = false;
    }

    public void create() {
        makeNull();

        if (errorCheckForCreate()) {
            return;
        }

        getShiftTables();

    }

    public ShiftTableController() {
        //   createDynamicColumns();
    }

    public List<ShiftTable> getShiftTables() {
        if (shiftTables == null) {
            createShiftTable();
        }
        return shiftTables;
    }

    public void setShiftTables(List<ShiftTable> shiftTables) {
        this.shiftTables = shiftTables;
    }

    public LinkedList<Staff> getStaffLink() {
        if (staffLink == null) {
            staffLink = createStaffLink();
        } else if (staffLink.isEmpty()) {
            staffLink = createStaffLink();
        }

        return staffLink;
    }

    public void setStaffLink(LinkedList<Staff> staffLink) {
        this.staffLink = staffLink;
    }

    public StaffFacade getStaffFacade() {
        return staffFacade;
    }

    public void setStaffFacade(StaffFacade staffFacade) {
        this.staffFacade = staffFacade;
    }

    public Roster getRoster() {
        if (roster == null) {
            roster = new Roster();
        }
        return roster;
    }

    public void setRoster(Roster roster) {
        this.roster = roster;
    }

    public ShiftFacade getShiftFacade() {
        return shiftFacade;
    }

    public void setShiftFacade(ShiftFacade shiftFacade) {
        this.shiftFacade = shiftFacade;
    }

    public DayShiftFacade getDayShiftFacade() {
        return dayShiftFacade;
    }

    public void setDayShiftFacade(DayShiftFacade dayShiftFacade) {
        this.dayShiftFacade = dayShiftFacade;
    }

    public PhDateFacade getPhDateFacade() {
        return phDateFacade;
    }

    public void setPhDateFacade(PhDateFacade phDateFacade) {
        this.phDateFacade = phDateFacade;
    }

    public Staff getCurrentStaff() {
        return currentStaff;
    }

    public void setCurrentStaff(Staff currentStaff) {
        this.currentStaff = currentStaff;

    }

    public StaffShiftFacade getStaffShiftFacade() {
        return staffShiftFacade;
    }

    public void setStaffShiftFacade(StaffShiftFacade staffShiftFacade) {
        this.staffShiftFacade = staffShiftFacade;
    }

    public LinkedList<Staff> getRepeatedStaff() {
        if (repeatedStaff == null) {
            repeatedStaff = new LinkedList<>();
        }
        return repeatedStaff;
    }

    public void setRepeatedStaff(LinkedList<Staff> repeatedStaff) {
        this.repeatedStaff = repeatedStaff;
    }

    public Date getDate() {
        if (date == null) {
            date = new Date();
        }
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public StaffWorkDayFacade getStaffWorkDayFacade() {
        return staffWorkDayFacade;
    }

    public void setStaffWorkDayFacade(StaffWorkDayFacade staffWorkDayFacade) {
        this.staffWorkDayFacade = staffWorkDayFacade;
    }

    public List<StaffMonthDay> getStaffMonthDays() {
        List<StaffMonthDay> tmp = new ArrayList<>();
        for (Staff s : getRoster().getStaffList()) {
            StaffMonthDay sm = getHumanResourceBean().calculateDayType(s, getCommonFunctions().getStartOfMonth(date),
                    getCommonFunctions().getEndOfMonth(date));
            tmp.add(sm);
        }

        return tmp;
    }

    public List<StaffMonthDay> getStaffWeekDays() {
        List<StaffMonthDay> tmp = new ArrayList<>();
        for (Staff s : getRoster().getStaffList()) {
            StaffMonthDay sm = getHumanResourceBean().calculateDayType(s, getCommonFunctions().getFirstDayOfWeek(date),
                    getCommonFunctions().getLastDayOfWeek(date));
            tmp.add(sm);
        }

        return tmp;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public List<Staff> completeStaff(String query) {
        List<Staff> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            if (!getFlag()) {
                suggestions = getRoster().getStaffList();
            } else {
                sql = "select p from Staff p where p.retired=false and (upper(p.person.name) like '%" + query.toUpperCase() + "%'or  upper(p.code) like '%" + query.toUpperCase() + "%' ) order by p.person.name";
                suggestions = getStaffFacade().findBySQL(sql);
            }

        }
        return suggestions;
    }

    public void flagTrue() {
        flag = true;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public HumanResourceBean getHumanResourceBean() {
        return humanResourceBean;
    }

    public void setHumanResourceBean(HumanResourceBean humanResourceBean) {
        this.humanResourceBean = humanResourceBean;
    }

    public FingerPrintRecordFacade getFingerPrintRecordFacade() {
        return fingerPrintRecordFacade;
    }

    public void setFingerPrintRecordFacade(FingerPrintRecordFacade fingerPrintRecordFacade) {
        this.fingerPrintRecordFacade = fingerPrintRecordFacade;
    }

}
