/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.data.dataStructure.DateRange;
import com.divudi.data.dataStructure.ExtraDutyCount;
import com.divudi.data.dataStructure.OtNormalSpecial;
import com.divudi.data.hr.DayType;
import com.divudi.data.dataStructure.StaffMonthDay;
import com.divudi.data.dataStructure.StartEndRecord;
import com.divudi.data.hr.ExtraDutyType;
import com.divudi.data.hr.FingerPrintRecordType;
import com.divudi.data.hr.PaysheetComponentType;
import com.divudi.data.hr.PhType;
import com.divudi.data.hr.Times;
import com.divudi.data.hr.WorkingType;
import com.divudi.entity.Staff;
import com.divudi.entity.WebUser;
import com.divudi.entity.hr.DayShift;
import com.divudi.entity.hr.FingerPrintRecord;
import com.divudi.entity.hr.PaysheetComponent;
import com.divudi.entity.hr.PhDate;
import com.divudi.entity.hr.StaffLeave;
import com.divudi.entity.hr.StaffPaysheetComponent;
import com.divudi.entity.hr.StaffSalary;
import com.divudi.entity.hr.StaffSalaryComponant;
import com.divudi.entity.hr.StaffShift;
import com.divudi.facade.FingerPrintRecordFacade;
import com.divudi.facade.PaysheetComponentFacade;
import com.divudi.facade.PhDateFacade;
import com.divudi.facade.StaffFacade;
import com.divudi.facade.StaffLeaveFacade;
import com.divudi.facade.StaffPaysheetComponentFacade;
import com.divudi.facade.StaffSalaryFacade;
import com.divudi.facade.StaffShiftFacade;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Stateless
public class HumanResourceBean {

    private LinkedList<Staff> staffLink;
    @EJB
    private StaffPaysheetComponentFacade staffPaysheetComponentFacade;
    @EJB
    private PaysheetComponentFacade paysheetComponentFacade;
    @EJB
    private StaffSalaryFacade staffSalaryFacade;
    @EJB
    private StaffFacade staffFacade;
    @EJB
    private StaffShiftFacade staffShiftFacade;
    @EJB
    private PhDateFacade phDateFacade;
    @EJB
    private StaffLeaveFacade staffLeaveFacade;
    @EJB
    private FingerPrintRecordFacade fingerPrintRecordFacade;
    ///////////////////
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private FinalVariables finalVariables;

    public List<StaffLeave> getStaffLeave(Staff staff, Date frmDate, Date toDate) {

        String sql = "Select s From StaffLeave s where s.retired=false and s.staff=:st and s.leaveDate between :frm and :to ";
        HashMap hm = new HashMap();
        hm.put("st", staff);
        hm.put("frm", frmDate);
        hm.put("to", toDate);

        return getStaffLeaveFacade().findBySQL(sql, hm, TemporalType.DATE);
    }

    public boolean isHoliday(Date d) {
        String sql = "Select d From PhDate d Where d.retired=false and d.phDate=:dtd";
        HashMap hm = new HashMap();
        hm.put("dtd", d);
        List<PhDate> tmp = getPhDateFacade().findBySQL(sql, hm, TemporalType.DATE);

        if (!tmp.isEmpty()) {
            return true;
        }

        return false;
    }

    public double calWorkedDuraion(StaffShift ss) {
        long endTime = 0l;
        long startTime = 0l;
        long result;

        if (ss.getEndRecord() != null && ss.getEndRecord().getRecordTimeStamp() != null) {
            endTime = ss.getEndRecord().getRecordTimeStamp().getTime();
        }

        if (ss.getStartRecord() != null && ss.getStartRecord().getRecordTimeStamp() != null) {
            startTime = ss.getStartRecord().getRecordTimeStamp().getTime();
        }

        result = endTime - startTime;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(result);

        return cal.get(Calendar.MINUTE);
    }

    public StaffMonthDay calculateDayType(Staff staff, Date frm, Date to) {

        StaffMonthDay tmp = new StaffMonthDay();
        tmp.setStaff(staff);
        for (StaffShift ds : getStaffShift(staff, frm, to)) {
            tmp.setRosteredTime(tmp.getRosteredTime() + (ds.getDayShift().getDurationHour() * 60));
            //  tmp.setWorkedTime(tmp.getWorkedTime() + calWorkedDuraion(ds));
            switch (getDayType(ds.getShiftDate())) {
                case Weekday:
                    tmp.setWeekdays(tmp.getWeekdays() + 1);
                    break;
                case Saturday:
                    tmp.setSaturday(tmp.getSaturday() + 1);
                    break;
                case Sunday:
                    tmp.setSunday(tmp.getSunday() + 1);
                    break;
                case Holiday:
                    tmp.setHoliday(tmp.getHoliday() + 1);
                    break;
            }
        }

        return tmp;

    }

    public boolean checkDateWithDayType(Date date, DayShift ds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayValue = cal.get(Calendar.DAY_OF_WEEK);

        if (isHoliday(date) && ds.getDayType() == DayType.Holiday) {
            return true;
        }

        if (!isHoliday(date) && (dayValue == 2 || dayValue == 3 || dayValue == 4 || dayValue == 5 || dayValue == 6) && ds.getDayType() == DayType.Weekday) {
            return true;
        }

        if (!isHoliday(date) && dayValue == 7 && ds.getDayType() == DayType.Saturday) {
            return true;
        }

        if (!isHoliday(date) && dayValue == 1 && ds.getDayType() == DayType.Sunday) {
            return true;
        }

        if (ds.getDayType() == DayType.All) {
            return true;
        }

        return false;
    }

    public DayType getDayType(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayValue = cal.get(Calendar.DAY_OF_WEEK);

        if (isHoliday(date)) {
            return DayType.Holiday;
        }

        if (!isHoliday(date) && (dayValue == 2 || dayValue == 3 || dayValue == 4 || dayValue == 5 || dayValue == 6)) {
            return DayType.Weekday;
        }

        if (!isHoliday(date) && dayValue == 7) {
            return DayType.Saturday;
        }

        if (!isHoliday(date) && dayValue == 1) {
            return DayType.Sunday;
        }

        return DayType.All;
    }

    public List<StaffShift> getStaffShift(Staff staff, Date frm, Date to) {
        String sql;
        HashMap hm = new HashMap();

        sql = "select st From StaffShift st where st.retired=false and st.shiftDate between :frmD and :toD and st.staff=:staf";
        hm.put("frmD", frm);
        hm.put("toD", to);
        hm.put("staf", staff);

        return getStaffShiftFacade().findBySQL(sql, hm, TemporalType.DATE);
    }

    public List<StaffShift> getStaffShiftFromRecord(Staff staff, Date frm, Date to) {
        String sql;
        HashMap hm = new HashMap();

        sql = "select st From StaffShift st where st.retired=false and  "
                + " st.finalized=false and "
                + " st.startRecord.recordTimeStamp between :frmD and :toD and st.staff=:staf";
        hm.put("frmD", frm);
        hm.put("toD", to);
        hm.put("staf", staff);

        return getStaffShiftFacade().findBySQL(sql, hm, TemporalType.DATE);
    }

    public List<Staff> getStaffForSalary() {
        String temSql;
        List<Staff> tmp;
        temSql = "SELECT i FROM Staff i where i.retired=false and i.person is not null and i.person.name is not null order by i.person.name";
        tmp = getStaffFacade().findBySQL(temSql);
        return tmp;
    }

    public void setEpf(StaffSalaryComponant ssc, double epf, double epfCompany) {
        if (ssc.getStaffPaysheetComponent().getPaysheetComponent().isIncludedForEpf()) {
            double tmp = ssc.getComponantValue() * (epf / 100.0);
            ssc.setEpfValue(tmp);

            tmp = ssc.getComponantValue() * (epfCompany / 100.0);
            ssc.setEpfCompanyValue(tmp);

        }
    }

    public void setEtf(StaffSalaryComponant ssc, double etf, double etfCompany) {
        if (ssc.getStaffPaysheetComponent().getPaysheetComponent().isIncludedForEpf()) {
            double tmp = ssc.getComponantValue() * (etf / 100.0);
            ssc.setEtfValue(tmp);

            tmp = ssc.getComponantValue() * (etfCompany / 100.0);
            ssc.setEtfCompanyValue(tmp);

        }
    }

    public boolean checkExistingSalary(Date date, Staff staff) {
        String sql = "Select s From StaffSalary s where s.retired=false and s.staff=:st and s.salaryCycle.fromDate>=:fd "
                + "and s.salaryCycle.toDate<=:td";

        HashMap hm = new HashMap<>();
        hm.put("fd", getCommonFunctions().getStartOfMonth(date));
        hm.put("td", getCommonFunctions().getEndOfMonth(date));
        hm.put("st", staff);

        List<StaffSalary> tmp = getStaffSalaryFacade().findBySQL(sql, hm, TemporalType.DATE);

        if (!tmp.isEmpty()) {
            updateItems(tmp, staff);
            return true;
        }

        return false;
    }

    private void updateItems(List<StaffSalary> items, Staff staff) {
        for (StaffSalary st : items) {
            if (st.getStaff().getId() == staff.getId()) {
                st.setExist(true);
            }
        }

    }

    public List<StaffPaysheetComponent> getStaffPaysheetComponentWithoutBasic(Staff staff, Date date) {
        String sql = "Select s From StaffPaysheetComponent s where s.retired=false and s.staff=:st"
                + " and (s.paysheetComponent.componentType!=:bs1 and s.paysheetComponent.componentType!=:bs2"
                + " and s.paysheetComponent.componentType!=:bs3 and s.paysheetComponent.componentType!=:bs4 )"
                + " and (s.toDate>=:dt or s.toDate is null)";
        HashMap hm = new HashMap();
        hm.put("st", staff);
        hm.put("dt", getCommonFunctions().getEndOfMonth(date));
        hm.put("bs1", PaysheetComponentType.BasicSalary);
        hm.put("bs2", PaysheetComponentType.OT);
        hm.put("bs3", PaysheetComponentType.ExtraDuty);
        hm.put("bs4", PaysheetComponentType.No_Pay_Deduction);
        return getStaffPaysheetComponentFacade().findBySQL(sql, hm, TemporalType.DATE);
    }

    public StaffPaysheetComponent getBasic(Staff staff) {
        ////System.err.println("Getting Basic " + staff.getStaffEmployment());

        String sql;
        HashMap hm;
        StaffPaysheetComponent tmp;

        sql = "Select s From StaffPaysheetComponent s where s.retired=false and "
                + " s.staff=:stf and s.paysheetComponent.componentType=:type "
                + "and s.fromDate<=:cu  and s.toDate>=:cu ";

        hm = new HashMap();
        hm.put("stf", staff);
        hm.put("type", PaysheetComponentType.BasicSalary);
        hm.put("cu", new Date());
        tmp = getStaffPaysheetComponentFacade().findFirstBySQL(sql, hm, TemporalType.DATE);

        if (tmp == null) {
            sql = "Select s From StaffPaysheetComponent s where s.retired=false and "
                    + " s.staff=:stf and s.paysheetComponent.componentType=:type"
                    + " and s.fromDate<=:cu  and s.toDate is null ";

            hm = new HashMap();
            hm.put("stf", staff);
            hm.put("type", PaysheetComponentType.BasicSalary);
            hm.put("cu", new Date());
            tmp = getStaffPaysheetComponentFacade().findFirstBySQL(sql, hm, TemporalType.DATE);

        }

        return tmp;

    }

    public StaffPaysheetComponent getComponent(Staff staff, WebUser user, PaysheetComponentType paysheetComponentType) {
        String sql;
        HashMap hm;
        StaffPaysheetComponent tmp;

        sql = "Select s From StaffPaysheetComponent s where s.retired=false and "
                + " s.staff=:stf and s.paysheetComponent.componentType=:type ";

        hm = new HashMap();
        hm.put("stf", staff);
        hm.put("type", paysheetComponentType);
        tmp = getStaffPaysheetComponentFacade().findFirstBySQL(sql, hm, TemporalType.DATE);

        if (tmp == null) {

            tmp = new StaffPaysheetComponent();
            tmp.setCreatedAt(new Date());
            tmp.setCreater(user);
            tmp.setPaysheetComponent(getComponentName(user, paysheetComponentType));
            tmp.setStaff(staff);
            getStaffPaysheetComponentFacade().create(tmp);
        }

        return tmp;

    }

//    public double calOt(Date date) {
//        DateRange dateRange = getCommonFunctions().getDateRangeForOT(date);
//        ////System.err.println("From : " + dateRange.getFromDate());
//        ////System.err.println("To : " + dateRange.getToDate());
//
//        return 0.0;
//    }
    private boolean checkExistingStaffShift(Date date) {
        String sql = "select s from StaffShift s where s.retired=false and s.finalized=false and"
                + " s.shiftStartTime=:d";

        HashMap hm = new HashMap();
        hm.put("d", date);

        StaffShift ss = getStaffShiftFacade().findFirstBySQL(sql, hm, TemporalType.TIMESTAMP);

        if (ss != null) {
            return true;
        }

        return false;
    }

    private void createNewStaffShift(StaffShift ss, Calendar date) {
        Date tmp = getCommonFunctions().getEndOfDay(date.getTime());
        Calendar time = Calendar.getInstance();
        time.setTime(tmp);
        time.add(Calendar.MILLISECOND, 1);
        ////System.err.println("Start Time : " + time.getTime());

        if (checkExistingStaffShift(time.getTime())) {
            return;
        }

        StaffShift stf = new StaffShift();
        stf.setContinuedTo(ss.getContinuedTo());
        stf.setCreatedAt(new Date());
        stf.setCreater(ss.getCreater());
        stf.setDayOff(ss.isDayOff());
        stf.setDayShift(ss.getDayShift());
        stf.setEndRecord(ss.getEndRecord());
        stf.setShiftDate(date.getTime());
        stf.setShiftEndTime(ss.getShiftEndTime());
        stf.setSleepingDay(ss.isSleepingDay());
        stf.setStaff(ss.getStaff());
        stf.setWorkingType(ss.getWorkingType());

        stf.setShiftStartTime(time.getTime());
        stf.setStartRecord(createFingerPrint(stf));

        getStaffShiftFacade().create(stf);
    }

    private FingerPrintRecord createFingerPrint(StaffShift ss) {
        FingerPrintRecord fpr = new FingerPrintRecord();
        fpr.setCreatedAt(new Date());
        fpr.setFingerPrintRecordType(FingerPrintRecordType.Varified);
        fpr.setStaff(ss.getStaff());
        fpr.setRecordTimeStamp(ss.getShiftStartTime());
        fpr.setStaffShift(ss);
        fpr.setTimes(Times.inTime);
        getFingerPrintRecordFacade().create(fpr);
        return fpr;
    }

    public List<StartEndRecord> getRecords(Staff staff, DateRange dateRange) {
        List<StaffShift> staffShifts = getStaffShiftFromRecord(staff, dateRange.getFromDate(), dateRange.getToDate());

        List<StartEndRecord> startEndRecords = new ArrayList<>();
        StartEndRecord addedRecord = new StartEndRecord();
        for (StaffShift ss : staffShifts) {

            if (addedRecord == null) {
                addedRecord = new StartEndRecord();
            }

            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            Calendar lastDate = Calendar.getInstance();

            start.setTime(ss.getStartRecord().getRecordTimeStamp());
            if (ss.getEndRecord() != null) {
                start.setTime(ss.getEndRecord().getRecordTimeStamp());
            }

            lastDate.setTime(dateRange.getToDate());

            if (ss.getStartRecord() != null && ss.getEndRecord() != null
                    && start.get(Calendar.DATE) == lastDate.get(Calendar.DATE)
                    && end.get(Calendar.DATE) != lastDate.get(Calendar.DATE)) {

                //create new Staffshft Record
                createNewStaffShift(ss, lastDate);
                //Update EndRecord Time
                ss.getEndRecord().setRecordTimeStamp(getCommonFunctions().getEndOfDay(dateRange.getToDate()));
                getFingerPrintRecordFacade().edit(ss.getEndRecord());
                //Update Staff shift
                ss.setShiftEndTime(getCommonFunctions().getEndOfDay(dateRange.getToDate()));
                ss.setFinalized(true);
                getStaffShiftFacade().edit(ss);

                addedRecord.setStartRecord(ss.getStartRecord());
                addedRecord.setStaffShift(ss);
                addedRecord.setEndRecord(ss.getEndRecord());
                startEndRecords.add(addedRecord);
                addedRecord = null;

            }

            if (ss.getStartRecord() != null && ss.getEndRecord() == null
                    && start.get(Calendar.DATE) == lastDate.get(Calendar.DATE)) {

                //create new Staffshft Record
                createNewStaffShift(ss, lastDate);
                //Update EndRecord Time
                ss.getEndRecord().setRecordTimeStamp(getCommonFunctions().getEndOfDay(dateRange.getToDate()));
                getFingerPrintRecordFacade().edit(ss.getEndRecord());
                //Update Staff shift
                ss.setShiftEndTime(getCommonFunctions().getEndOfDay(dateRange.getToDate()));
                ss.setFinalized(true);
                getStaffShiftFacade().edit(ss);

                addedRecord.setStartRecord(ss.getStartRecord());
                addedRecord.setEndRecord(ss.getEndRecord());
                addedRecord.setStaffShift(ss);

                startEndRecords.add(addedRecord);
                addedRecord = null;

            }

            if (ss.getStartRecord() != null && ss.getEndRecord() == null
                    && start.get(Calendar.DATE) != lastDate.get(Calendar.DATE)) {
                addedRecord.setStartRecord(ss.getStartRecord());
                addedRecord.setStaffShift(ss);
                if (addedRecord.getStartRecord() != null && addedRecord.getEndRecord() != null) {
                    startEndRecords.add(addedRecord);
                    addedRecord = null;
                }
                //     addedRecord = null;
            }

            if (ss.getStartRecord() == null && ss.getEndRecord() != null
                    && start.get(Calendar.DATE) != lastDate.get(Calendar.DATE)) {
                addedRecord.setEndRecord(ss.getEndRecord());
                addedRecord.setStaffShift(ss);
                if (addedRecord.getStartRecord() != null && addedRecord.getEndRecord() != null) {
                    startEndRecords.add(addedRecord);
                    addedRecord = null;
                }
                //     addedRecord = null;
            }

            if (ss.getStartRecord() != null && ss.getEndRecord() != null
                    && start.get(Calendar.DATE) != lastDate.get(Calendar.DATE)) {

                addedRecord.setStartRecord(ss.getStartRecord());
                addedRecord.setEndRecord(ss.getEndRecord());
                addedRecord.setStaffShift(ss);
                startEndRecords.add(addedRecord);
                addedRecord = null;
            }

        }

        return startEndRecords;
    }

    public OtNormalSpecial calOt(Date date, Staff staff) {
        OtNormalSpecial otNormalSpecial = new OtNormalSpecial();

        DateRange dateRange = getCommonFunctions().getDateRangeForOT(date);
        
        otNormalSpecial.setDateRange(dateRange);
        
        ////System.err.println("From : " + dateRange.getFromDate());
        ////System.err.println("To : " + dateRange.getToDate());

        List<StartEndRecord> startEndRecords = getRecords(staff, dateRange);

        double otMaxMinute = getFinalVariables().getOtTime() * 4 * 60;
        double workedMinute = 0.0;

        for (StartEndRecord ser : startEndRecords) {
            Calendar start = Calendar.getInstance();
            start.setTime(ser.getStartRecord().getRecordTimeStamp());
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(ser.getEndRecord().getRecordTimeStamp());

            int netTime = endTime.get(Calendar.MINUTE) - start.get(Calendar.MINUTE);
            workedMinute += netTime;

        }

        if (workedMinute > (staff.getWorkingHour() * 4 * 60)) {
            otNormalSpecial.setSpecialMin(otMaxMinute - (staff.getWorkingHour() * 4 * 60));
        }

        if (workedMinute > otMaxMinute) {
            otNormalSpecial.setNormalMin(workedMinute - otMaxMinute);
        }

        return otNormalSpecial;

    }

    public List<ExtraDutyCount> calExtraDuty(Date date, Staff staff) {

        DateRange dateRange = new DateRange();
        dateRange.setFromDate(getCommonFunctions().getStartOfMonth(date));
        dateRange.setToDate(getCommonFunctions().getEndOfMonth(date));

        ////System.err.println("From : " + dateRange.getFromDate());
        ////System.err.println("To : " + dateRange.getToDate());

        List<StaffShift> staffShifts = getStaffShift(staff, dateRange.getFromDate(), dateRange.getToDate());

        List<ExtraDutyCount> extraDutyCounts = new ArrayList<>();

        for (ExtraDutyType edt : ExtraDutyType.values()) {
            ExtraDutyCount newD = new ExtraDutyCount();
            newD.setExtraDutyType(edt);
            newD.setCount(calDutyCount(edt, staffShifts));
            extraDutyCounts.add(newD);
        }

        return extraDutyCounts;

    }

    private double calDutyCount(ExtraDutyType extraDutyType, List<StaffShift> staffShifts) {
        if (extraDutyType == ExtraDutyType.DayOff) {
            return getDayOffs(staffShifts);
        }

        if (extraDutyType == ExtraDutyType.SleepingDay) {
            return getSleepingDays(staffShifts);
        }

        if (extraDutyType == ExtraDutyType.MurchantileHoliday) {
            return getHoliDayCount(staffShifts, PhType.MurchantileHoliday);
        }

        if (extraDutyType == ExtraDutyType.Poya) {
            return getHoliDayCount(staffShifts, PhType.Poya);
        }

        if (extraDutyType == ExtraDutyType.PublicHoliday) {
            return getHoliDayCount(staffShifts, PhType.PublicHoliday);
        }

        return 0.0;
    }

    private double getDayOffs(List<StaffShift> list) {
        double tmp = 0.0;
        for (StaffShift sst : list) {
            if (sst.isDayOff()) {
                tmp++;
            }
        }

        return tmp;
    }

    private double getSleepingDays(List<StaffShift> list) {
        double tmp = 0.0;
        for (StaffShift sst : list) {
            if (sst.isSleepingDay()) {
                tmp++;
            }
        }

        return tmp;
    }

    private double getHoliDayCount(List<StaffShift> list, PhType phType) {
        double tmp = 0.0;
        for (StaffShift sst : list) {
            if (getHolidayType(sst.getShiftDate(), phType)) {
                tmp++;
            }
        }

        return tmp;
    }

    public boolean getHolidayType(Date d, PhType phType) {
        String sql = "Select d From PhDate d Where d.retired=false and d.phDate=:dtd and d.phType=:tp";
        HashMap hm = new HashMap();
        hm.put("dtd", d);
        hm.put("tp", phType);
        List<PhDate> tmp = getPhDateFacade().findBySQL(sql, hm, TemporalType.DATE);

        if (!tmp.isEmpty()) {
            return true;
        }

        return false;
    }

    private PaysheetComponent getComponentName(WebUser user, PaysheetComponentType paysheetComponentType) {
        String sql;
        HashMap hm;
        PaysheetComponent tmp;

        sql = "Select s From PaysheetComponent s where s.retired=false and  s.componentType=:type ";

        hm = new HashMap();
        hm.put("type", paysheetComponentType);
        tmp = getPaysheetComponentFacade().findFirstBySQL(sql, hm, TemporalType.DATE);

        if (tmp == null) {
            tmp = new PaysheetComponent();
            tmp.setCreatedAt(new Date());
            tmp.setComponentType(paysheetComponentType);
            tmp.setCreater(user);
            tmp.setName(paysheetComponentType.toString());

            getPaysheetComponentFacade().create(tmp);
        }

        return tmp;
    }

    public StaffSalary getStaffSalary(Staff s, Date date) {

        String sql = "Select s From StaffSalary s where s.retired=false and "
                + " s.staff=:s and s.salaryCycle.fromDate>=:fd "
                + "and s.salaryCycle.toDate<=:td";

        HashMap hm = new HashMap<>();
        hm.put("fd", getCommonFunctions().getStartOfMonth(date));
        hm.put("td", getCommonFunctions().getEndOfMonth(date));
        hm.put("s", s);

        StaffSalary tmp = getStaffSalaryFacade().findFirstBySQL(sql, hm, TemporalType.DATE);

        if (tmp == null) {
            tmp = new StaffSalary();
            tmp.setStaff(s);
        }

        return tmp;
    }

    public StaffPaysheetComponentFacade getStaffPaysheetComponentFacade() {
        return staffPaysheetComponentFacade;
    }

    public void setStaffPaysheetComponentFacade(StaffPaysheetComponentFacade staffPaysheetComponentFacade) {
        this.staffPaysheetComponentFacade = staffPaysheetComponentFacade;
    }

    public LinkedList<Staff> getStaffLink() {
        return staffLink;
    }

    public void setStaffLink(LinkedList<Staff> staffLink) {
        this.staffLink = staffLink;
    }

    public StaffSalaryFacade getStaffSalaryFacade() {
        return staffSalaryFacade;
    }

    public void setStaffSalaryFacade(StaffSalaryFacade staffSalaryFacade) {
        this.staffSalaryFacade = staffSalaryFacade;
    }

    public StaffFacade getStaffFacade() {
        return staffFacade;
    }

    public void setStaffFacade(StaffFacade staffFacade) {
        this.staffFacade = staffFacade;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public StaffShiftFacade getStaffShiftFacade() {
        return staffShiftFacade;
    }

    public void setStaffShiftFacade(StaffShiftFacade staffShiftFacade) {
        this.staffShiftFacade = staffShiftFacade;
    }

    public PhDateFacade getPhDateFacade() {
        return phDateFacade;
    }

    public void setPhDateFacade(PhDateFacade phDateFacade) {
        this.phDateFacade = phDateFacade;
    }

    public StaffLeaveFacade getStaffLeaveFacade() {
        return staffLeaveFacade;
    }

    public void setStaffLeaveFacade(StaffLeaveFacade staffLeaveFacade) {
        this.staffLeaveFacade = staffLeaveFacade;
    }

    public PaysheetComponentFacade getPaysheetComponentFacade() {
        return paysheetComponentFacade;
    }

    public void setPaysheetComponentFacade(PaysheetComponentFacade paysheetComponentFacade) {
        this.paysheetComponentFacade = paysheetComponentFacade;
    }

    public FingerPrintRecordFacade getFingerPrintRecordFacade() {
        return fingerPrintRecordFacade;
    }

    public void setFingerPrintRecordFacade(FingerPrintRecordFacade fingerPrintRecordFacade) {
        this.fingerPrintRecordFacade = fingerPrintRecordFacade;
    }

    public FinalVariables getFinalVariables() {
        return finalVariables;
    }

    public void setFinalVariables(FinalVariables finalVariables) {
        this.finalVariables = finalVariables;
    }
}
