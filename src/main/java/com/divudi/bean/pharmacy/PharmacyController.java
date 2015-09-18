/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.DepartmentController;
import com.divudi.bean.ItemController;
import com.divudi.bean.SessionController;
import com.divudi.data.BillType;
import com.divudi.data.InstitutionType;
import com.divudi.data.dataStructure.DepartmentSale;
import com.divudi.data.dataStructure.DepartmentStock;
import com.divudi.data.dataStructure.InstitutionSale;
import com.divudi.data.dataStructure.InstitutionStock;
import com.divudi.data.dataStructure.StockAverage;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.facade.AmpFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.InstitutionFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import com.divudi.facade.StockFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import javax.persistence.TemporalType;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class PharmacyController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    private SessionController sessionController;
    //////////
    @EJB
    private DepartmentFacade departmentFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private StockFacade stockFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private AmpFacade ampFacade;
    ///////////
    private Item pharmacyItem;
    // private double grantStock;
    private Date fromDate;
    private Date toDate;
    ////////
    //List<DepartmentStock> departmentStocks;
    private List<DepartmentSale> departmentSale;
    private List<BillItem> grns;
    private List<BillItem> pos;
    private List<BillItem> directPurchase;
    Double persentage;
    int orderForInDays;

    public int getOrderForInDays() {
        return orderForInDays;
    }

    public void setOrderForInDays(int orderForInDays) {
        this.orderForInDays = orderForInDays;
    }

    public void makeNull() {
        departmentSale = null;
        pos = null;
        grns = null;
        institutionSales = null;
        institutionStocks = null;
        institutionTransferIssue = null;
        directPurchase = null;
    }

    public List<Stock> completeAllStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("n", "%" + qry.toUpperCase() + "%");
        sql = "select i from Stock i where i.department=:d and "
                + " (upper(i.itemBatch.item.name) like :n  or "
                + " upper(i.itemBatch.item.code) like :n  or  "
                + " upper(i.itemBatch.item.barcode) like :n ) ";
        items = getStockFacade().findBySQL(sql, m, 20);

        return items;
    }

    public List<Stock> completeExistingStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", getSessionController().getLoggedUser().getDepartment());
        double d = 0.0;
        m.put("n", "%" + qry.toUpperCase() + "%");
        m.put("s", d);
        sql = "select i from Stock i where i.department=:d and "
                + " i.stock > :s and "
                + " (upper(i.itemBatch.item.name) like :n  or "
                + " upper(i.itemBatch.item.code) like :n  or  "
                + " upper(i.itemBatch.item.barcode) like :n ) ";
        items = getStockFacade().findBySQL(sql, m, 40);

        return items;
    }

    public List<Stock> completeStaffStocks(String qry) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        double d = 0.0;
        m.put("s", d);
        m.put("n", "%" + qry.toUpperCase() + "%");
        sql = "select i from Stock i where i.stock >:s and (upper(i.staff.code) like :n or upper(i.staff.person.name) like :n or upper(i.itemBatch.item.name) like :n ) order by i.itemBatch.item.name, i.itemBatch.dateOfExpire";
        items = getStockFacade().findBySQL(sql, m, 20);

        return items;
    }

    public List<Department> getInstitutionDepatrments(Institution ins) {
        List<Department> d;
        HashMap hm = new HashMap();
        String sql = "Select d From Department d where d.retired=false and d.institution=:ins";
        hm.put("ins", ins);
        d = getDepartmentFacade().findBySQL(sql, hm);

        return d;
    }

    private double grantStock;

    public double getGrantStock() {
        return grantStock;

    }

    public double getTransferIssueValueByInstitution(Institution toIns, Item i) {
        //   List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("toIns", toIns);
        m.put("frmIns", getSessionController().getInstitution());
        m.put("i", i);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferIssue);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.stock.itemBatch.purcahseRate*i.pharmaceuticalBillItem.qty) "
                + "from BillItem i where i.bill.toInstitution=:toIns and i.bill.fromInstitution=:frmIns "
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public double getTransferIssueValueByDepartmet(Department toDep, Item i) {
        //   List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("tDep", toDep);
        m.put("fDep", getSessionController().getDepartment());
        m.put("i", i);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferIssue);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.stock.itemBatch.purcahseRate*i.pharmaceuticalBillItem.qty) "
                + " from BillItem i where i.bill.toDepartment=:tDep and i.bill.fromDepartment=:fDep "
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public double getTransferIssueQtyByDepartmet(Department tDep, Item i) {
        //   List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("tDep", tDep);
        m.put("fDep", getSessionController().getDepartment());
        m.put("i", i);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferIssue);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.qty) from BillItem i where i.bill.toDepartment=:tDep"
                + " and  i.bill.fromDepartment=:fDep"
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public double getTransferIssueQtyByInstitution(Institution toIns, Item i) {
        //   List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("tIns", toIns);
        m.put("fIns", getSessionController().getInstitution());
        m.put("i", i);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferIssue);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.qty) from BillItem i where "
                + " i.bill.toInstitution=:tIns and i.bill.fromInstitution=:fIns "
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private double grantSaleQty;
    private double grantSaleValue;

    private double grantTransferIssueQty;
    private double grantTransferIssueValue;

    @EJB
    private InstitutionFacade institutionFacade;

    private List<Institution> getCompany() {
        String sql;
        HashMap hm = new HashMap();
        hm.put("type", InstitutionType.Company);
        sql = "select c from Institution c where c.retired=false and c.institutionType=:type order by c.name";

        return getInstitutionFacade().findBySQL(sql, hm);
    }

    private List<InstitutionStock> institutionStocks;

    public List<Object[]> calDepartmentStock(Institution institution) {
        //   ////System.err.println("Cal Department Stock");
        Item item;

        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }

        String sql;
        Map m = new HashMap();
        m.put("ins", institution);
        m.put("i", item);
        sql = "select i.department,sum(i.stock) from Stock i where "
                + " i.department.institution=:ins and i.itemBatch.item=:i"
                + " group by i.department"
                + " having sum(i.stock) > 0 ";

        return getBillItemFacade().findAggregates(sql, m);

    }

    public List<Object[]> calDepartmentStock(Institution institution, Item itm) {
        //   ////System.err.println("Cal Department Stock");

        if (itm instanceof Ampp) {
            itm = ((Ampp) itm).getAmp();
        }

        String sql;
        Map m = new HashMap();
        m.put("ins", institution);
        m.put("i", itm);
        sql = "select i.department,sum(i.stock),i.itemBatch.dateOfExpire,i.itemBatch.purcahseRate,i.itemBatch.retailsaleRate from Stock i where "
                + " i.department.institution=:ins and i.itemBatch.item=:i"
                + " group by i.department";
//                + " having sum(i.stock) >= 0 ";

        return getBillItemFacade().findAggregates(sql, m);

    }

    @Inject
    DepartmentController departmentController;

    public List<Object[]> calAllDepartmentStock(Institution institution, Item itm) {
        //    System.out.println("calAllDepartmentStock");
        List<Object[]> objs = new ArrayList<>();
        if (itm instanceof Ampp) {
            itm = ((Ampp) itm).getAmp();
        }

        List<Department> deps = departmentController.getInstitutionDepatrments(getSessionController().getInstitution());
        //    System.out.println("deps = " + deps);
        String sql;
        Map m;
        for (Department dep : deps) {

            m = new HashMap();
            m.put("dep", dep);
            m.put("i", itm);
            sql = "select sum(i.stock),i.itemBatch.dateOfExpire,i.itemBatch.purcahseRate,i.itemBatch.retailsaleRate "
                    + " from Stock i "
                    + " where  i.department=:dep "
                    + " and i.itemBatch.item=:i";
            List<Object> lobjs = new ArrayList<>();
            lobjs.add(dep);
            Object[] tobjs = getBillItemFacade().findSingleAggregate(sql, m);
            //    System.out.println("tobjs = " + Arrays.toString(tobjs));
            if (tobjs != null && tobjs[0] != null) {
                lobjs.addAll(Arrays.asList(tobjs));
            } else {
                lobjs.add(0.0);
                lobjs.add(null);
                lobjs.add(0.0);
                lobjs.add(0.0);
            }
            Object[] obj = lobjs.toArray();
            //    System.out.println("obj = " + Arrays.toString(obj));
            objs.add(obj);
        }
        return objs;
    }

    public List<Object[]> calDepartmentTransferIssue(Institution institution) {
        Item item;

        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }

        String sql;
        Map m = new HashMap();
        m.put("curr", getSessionController().getDepartment());
        m.put("i", item);
        m.put("ins", institution);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferIssue);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select i.bill.toDepartment,sum(i.pharmaceuticalBillItem.stock.itemBatch.purcahseRate*i.pharmaceuticalBillItem.qty),"
                + " sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i where i.bill.toDepartment.institution=:ins and i.bill.department=:curr "
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to group by i.bill.toDepartment";

        return getBillItemFacade().findAggregates(sql, m);

    }

    public List<Object[]> calDepartmentTransferReceive(Institution institution) {
        Item item;

        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }

        String sql;
        Map m = new HashMap();
        m.put("dep", getSessionController().getDepartment());
        m.put("i", item);
        m.put("ins", institution);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyTransferReceive);
        //   m.put("refType", BillType.PharmacySale);
        sql = "select i.bill.fromDepartment,sum(i.pharmaceuticalBillItem.stock.itemBatch.purcahseRate*i.pharmaceuticalBillItem.qty),"
                + " sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i where i.bill.fromDepartment.institution=:ins and i.bill.department=:dep "
                + " and i.item=:i and i.bill.billType=:btp and i.createdAt between :frm and :to group by i.bill.fromDepartment";

        return getBillItemFacade().findAggregates(sql, m);

    }

    public List<Object[]> calDepartmentSale(Institution institution) {
        Item item;

        if (pharmacyItem instanceof Ampp) {
            item = ((Ampp) pharmacyItem).getAmp();
        } else {
            item = pharmacyItem;
        }

        String sql;
        Map m = new HashMap();
        m.put("itm", item);
        m.put("ins", institution);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyPre);
        m.put("refType", BillType.PharmacySale);
        sql = "select i.bill.department,sum(i.netValue),sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i where i.bill.department.institution=:ins"
                + " and i.bill.referenceBill.billType=:refType "
                + " and i.item=:itm and i.bill.billType=:btp and i.createdAt between :frm and :to  "
                + " group by i.bill.department";

        return getBillItemFacade().findAggregates(sql, m);

    }

    public double calDepartmentSaleQty(Department department, Item itm) {

        if (itm instanceof Ampp) {
            itm = ((Ampp) pharmacyItem).getAmp();
        }

        String sql;
        Map m = new HashMap();
        m.put("itm", itm);
        m.put("dep", department);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyPre);
        m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i where i.bill.department=:dep"
                + " and i.bill.referenceBill.billType=:refType "
                + " and i.item=:itm and i.bill.billType=:btp and "
                + " i.createdAt between :frm and :to  ";

        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    private Institution institution;
    private List<StockAverage> stockAverages;

    @Inject
    private ItemController itemController;

    public void averageByDate() {
        Calendar frm = Calendar.getInstance();
        frm.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);

        long lValue = to.getTimeInMillis() - frm.getTimeInMillis();
        double dayCount = 0;
        if (lValue != 0) {
            dayCount = lValue / (1000 * 60 * 60 * 24);
        }

        //System.err.println("Day Count " + dayCount);
        List<Item> items = getItemController().getDealorItem();
        createStockAverage(dayCount, items);

    }

    public void averageByMonth() {
        Calendar frm = Calendar.getInstance();
        frm.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);

        long lValue = to.getTimeInMillis() - frm.getTimeInMillis();
        double monthCount = 0;
        if (lValue != 0) {
            monthCount = lValue / (1000 * 60 * 60 * 24 * 30);
        }

        //System.err.println("Month Count " + monthCount);
        List<Item> items = getItemController().getDealorItem();
        createStockAverage(Math.abs(monthCount), items);

    }

    public void averageByMonth(Item item) {
        Calendar frm = Calendar.getInstance();
        frm.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);
        List<Item> items = new ArrayList<>();

        long lValue = to.getTimeInMillis() - frm.getTimeInMillis();
        double monthCount = 0;
        if (lValue != 0) {
            monthCount = lValue / (1000 * 60 * 60 * 24 * 30);
        }

        if (item == null) {
            return;
        }
        items.add(item);
        //System.err.println("Month Count " + monthCount);
        createStockAverage(Math.abs(monthCount), items);

    }

    public void createStockAverage(double dayCount, List<Item> itms) {

        stockAverages = new ArrayList<>();

        List<Item> items = itms;
        //    System.out.println("items = " + items.size());
        List<Institution> insList = getCompany();
        for (Item i : items) {

            System.err.println("i.getName() = " + i.getName());

            double itemStockTotal = 0;
            double itemAverageTotal = 0;
            StockAverage stockAverage = new StockAverage();
            stockAverage.setItem(i);
            stockAverage.setInstitutionStocks(new ArrayList<InstitutionStock>());

            for (Institution ins : insList) {
                double insStockTotal = 0;
                double insAverageTotal = 0;
                InstitutionStock newTable = new InstitutionStock();
                newTable.setInstitution(ins);
                newTable.setDepatmentStocks(new ArrayList<DepartmentStock>());
                List<Object[]> objs = calDepartmentStock(ins, i);

                for (Object[] obj : objs) {
//                    //System.err.println("Inside ");
                    DepartmentStock r = new DepartmentStock();
                    r.setDepartment((Department) obj[0]);
                    r.setStock((Double) obj[1]);

                    try {
                        double qty = calDepartmentSaleQty(r.getDepartment(), i);
                        qty = 0 - qty;
                        if (qty != 0 && dayCount != 0) {
                            double avg = qty / dayCount;
                            r.setAverage(avg);
                        }

                        insStockTotal += r.getStock();
                        insAverageTotal += r.getAverage();
                        newTable.getDepatmentStocks().add(r);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                newTable.setInstitutionTotal(insStockTotal);
                newTable.setInstitutionAverage(insAverageTotal);

                if (insStockTotal != 0 || insAverageTotal != 0) {
                    stockAverage.getInstitutionStocks().add(newTable);
                    itemStockTotal += insStockTotal;
                    itemAverageTotal += insAverageTotal;
                }
            }

            stockAverage.setItemAverageTotal(itemAverageTotal);
            stockAverage.setItemStockTotal(itemStockTotal);
            stockAverages.add(stockAverage);
        }

    }

    public void createInstitutionStock() {
        //   ////System.err.println("Institution Stock");
        List<Institution> insList = getCompany();

        institutionStocks = new ArrayList<>();
        grantStock = 0;

        for (Institution ins : insList) {
            InstitutionStock newTable = new InstitutionStock();
            List<DepartmentStock> list = new ArrayList<>();
            double totalStock = 0;
            List<Object[]> objs = calDepartmentStock(ins);

            for (Object[] obj : objs) {
                DepartmentStock r = new DepartmentStock();
                r.setDepartment((Department) obj[0]);
                r.setStock((Double) obj[1]);
                list.add(r);

                //Total Institution Stock
                totalStock += r.getStock();
                grantStock += r.getStock();

            }

            if (totalStock != 0) {
                newTable.setDepatmentStocks(list);
                newTable.setInstitution(ins);
                newTable.setInstitutionTotal(totalStock);

                institutionStocks.add(newTable);
            }
        }

    }

    public void createInstitutionSale() {
        List<Institution> insList = getCompany();

        institutionSales = new ArrayList<>();
        grantSaleQty = 0;
        grantSaleValue = 0;

        for (Institution ins : insList) {
            InstitutionSale newTable = new InstitutionSale();
            List<DepartmentSale> list = new ArrayList<>();
            double totalValue = 0;
            double totalQty = 0;
            List<Object[]> objs = calDepartmentSale(ins);

            for (Object[] obj : objs) {
                DepartmentSale r = new DepartmentSale();
                r.setDepartment((Department) obj[0]);
                r.setSaleValue((Double) obj[1]);
                r.setSaleQty((Double) obj[2]);
                list.add(r);
                //Total Institution Stock
                totalValue += r.getSaleValue();
                totalQty += r.getSaleQty();
                grantSaleValue += r.getSaleValue();
                grantSaleQty += r.getSaleQty();

            }

            if (totalQty != 0 || totalValue != 0) {
                newTable.setDepartmentSales(list);
                newTable.setInstitution(ins);
                newTable.setInstitutionQty(totalQty);
                newTable.setInstitutionValue(totalValue);

                institutionSales.add(newTable);

            }
        }

    }

    public List<InstitutionStock> getInstitutionStock() {
        return institutionStocks;
    }

    private List<InstitutionSale> institutionSales;

    private List<InstitutionSale> institutionTransferIssue;

    private List<InstitutionSale> institutionTransferReceive;
    private double grantTransferReceiveQty = 0;
    private double grantTransferReceiveValue = 0;

    public void createInstitutionTransferIssue() {
        List<Institution> insList = getCompany();

        institutionTransferIssue = new ArrayList<>();
        grantTransferIssueQty = 0;
        grantTransferIssueValue = 0;

        for (Institution ins : insList) {
            InstitutionSale newTable = new InstitutionSale();
            List<DepartmentSale> list = new ArrayList<>();
            double totalValue = 0;
            double totalQty = 0;
            List<Object[]> objs = calDepartmentTransferIssue(ins);

            for (Object[] obj : objs) {
                DepartmentSale r = new DepartmentSale();
                r.setDepartment((Department) obj[0]);
                r.setSaleValue((Double) obj[1]);
                r.setSaleQty((Double) obj[2]);
                list.add(r);
                //Total Institution Stock
                totalValue += r.getSaleValue();
                totalQty += r.getSaleQty();
                grantTransferIssueValue += r.getSaleValue();
                grantTransferIssueQty += r.getSaleQty();

            }

            if (totalQty != 0 || totalValue != 0) {
                newTable.setDepartmentSales(list);
                newTable.setInstitution(ins);
                newTable.setInstitutionQty(totalQty);
                newTable.setInstitutionValue(totalValue);

                institutionTransferIssue.add(newTable);

            }
        }

    }

    public void createInstitutionTransferReceive() {
        List<Institution> insList = getCompany();

        institutionTransferReceive = new ArrayList<>();
        grantTransferReceiveQty = 0;
        grantTransferReceiveValue = 0;

        for (Institution ins : insList) {
            InstitutionSale newTable = new InstitutionSale();
            List<DepartmentSale> list = new ArrayList<>();
            double totalValue = 0;
            double totalQty = 0;
            List<Object[]> objs = calDepartmentTransferReceive(ins);

            for (Object[] obj : objs) {
                DepartmentSale r = new DepartmentSale();
                r.setDepartment((Department) obj[0]);
                r.setSaleValue((Double) obj[1]);
                r.setSaleQty((Double) obj[2]);
                list.add(r);
                //Total Institution Stock
                totalValue += r.getSaleValue();
                totalQty += r.getSaleQty();
                grantTransferReceiveValue += r.getSaleValue();
                grantTransferReceiveQty += r.getSaleQty();

            }

            if (totalQty != 0 || totalValue != 0) {
                newTable.setDepartmentSales(list);
                newTable.setInstitution(ins);
                newTable.setInstitutionQty(totalQty);
                newTable.setInstitutionValue(totalValue);

                institutionTransferReceive.add(newTable);

            }
        }

    }

    public List<BillItem> getGrns() {
        return grns;
    }

    public void createTable() {
        createGrnTable();
        createPoTable();
        createDirectPurchaseTable();

    }

    public void createGrnTable() {

        // ////System.err.println("Getting GRNS : ");
        String sql = "Select b From BillItem b where type(b.bill)=:class and b.bill.creater is not null "
                + " and b.bill.cancelled=false and b.retired=false and b.item=:i "
                + " and b.bill.billType=:btp and b.createdAt between :frm and :to order by b.id desc ";
        HashMap hm = new HashMap();
        hm.put("i", pharmacyItem);
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
        hm.put("class", BilledBill.class);
        hm.put("btp", BillType.PharmacyGrnBill);

        grns = getBillItemFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public void createDirectPurchaseTable() {

        // ////System.err.println("Getting GRNS : ");
        String sql = "Select b From BillItem b where type(b.bill)=:class and b.bill.creater is not null "
                + " and b.bill.cancelled=false and b.retired=false and b.item=:i "
                + " and b.bill.billType=:btp and b.createdAt between :frm and :to order by b.id desc ";
        HashMap hm = new HashMap();
        hm.put("i", pharmacyItem);
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
        hm.put("btp", BillType.PharmacyPurchaseBill);
        hm.put("class", BilledBill.class);
        directPurchase = getBillItemFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public List<BillItem> getPos() {
        return pos;
    }

    public void createPoTable() {

        // ////System.err.println("Getting POS : ");
        String sql = "Select b From BillItem b where type(b.bill)=:class and b.bill.creater is not null "
                + " and b.bill.cancelled=false and b.retired=false and b.item=:i "
                + " and b.bill.billType=:btp and b.createdAt between :frm and :to order by b.id desc";
        HashMap hm = new HashMap();
        hm.put("i", pharmacyItem);
        hm.put("btp", BillType.PharmacyOrderApprove);
        hm.put("frm", getFromDate());
        hm.put("to", getToDate());
        hm.put("class", BilledBill.class);
        pos = getBillItemFacade().findBySQL(sql, hm, TemporalType.TIMESTAMP);

        for (BillItem t : pos) {
            //   t.setPharmaceuticalBillItem(getPoQty(t));
            t.setTotalGrnQty(getGrnQty(t));
        }

    }

//    private PharmaceuticalBillItem getPoQty(BillItem b) {
//        String sql = "Select b From PharmaceuticalBillItem b where b.billItem=:bt";
//
//        HashMap hm = new HashMap();
//        hm.put("bt", b);
//
//        return getPharmaceuticalBillItemFacade().findFirstBySQL(sql, hm);
//    }
    private double getGrnQty(BillItem b) {
        String sql = "Select sum(b.pharmaceuticalBillItem.qty) From BillItem b where b.retired=false and b.creater is not null"
                + " and b.bill.cancelled=false and b.bill.billType=:btp and "
                + " b.referanceBillItem=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyGrnBill);
        double value = getBillFacade().findDoubleByJpql(sql, hm);

//        if (pharmacyItem instanceof Ampp) {
//            value = value / pharmacyItem.getDblValue();
//        }
        return value;
    }

    double totalStock = 0.0;
    double totalPurchase = 0.0;
    double totalRetailSale = 0.0;

    public void createStockDealorAllDepartments() {

        stockAverages = new ArrayList<>();
        List<Item> items = getItemController().getDealorItem();
        List<Institution> insList = getCompany();
        for (Item i : items) {
            double itemStockTotal = 0;
            double itemPurchaaseTotal = 0;
            double itemRetailSaleTotal = 0;
            StockAverage stockAverage = new StockAverage();
            stockAverage.setItem(i);
            stockAverage.setInstitutionStocks(new ArrayList<InstitutionStock>());

            for (Institution ins : insList) {
                double insStockTotal = 0;
                double insAverageTotal = 0;
                double insPurchaseTotal = 0.0;
                double insRetailSaleTotal = 0.0;
                InstitutionStock newTable = new InstitutionStock();
                newTable.setInstitution(ins);
                newTable.setDepatmentStocks(new ArrayList<DepartmentStock>());
                List<Object[]> objs = calDepartmentStock(ins, i);

                for (Object[] obj : objs) {
//                    //System.err.println("Inside ");
                    DepartmentStock r = new DepartmentStock();
                    r.setDepartment((Department) obj[0]);
                    r.setStock((Double) obj[1]);
                    r.setExpiryDate((Date) obj[2]);
                    r.setPurchaseRate((Double) obj[3]);
                    r.setRetailSale((Double) obj[4]);

                    insStockTotal += r.getStock();
                    insPurchaseTotal += r.getPurchaseRate() * r.getStock();
                    insRetailSaleTotal += r.getRetailSale() * r.getStock();
                    newTable.getDepatmentStocks().add(r);

                }

                newTable.setInstitutionTotal(insStockTotal);
                newTable.setInstitutionPurchaseTotal(insPurchaseTotal);
                newTable.setInstitutionRetailSaleTotal(insRetailSaleTotal);

                if (insStockTotal != 0 || insAverageTotal != 0) {
                    stockAverage.getInstitutionStocks().add(newTable);
                    itemStockTotal += insStockTotal;
                    itemPurchaaseTotal += insPurchaseTotal;
                    itemRetailSaleTotal += insRetailSaleTotal;
                }
            }

            if (itemStockTotal != 0) {
                stockAverage.setItemStockTotal(itemStockTotal);
                stockAverage.setItemPurchaseTotal(itemPurchaaseTotal);
                stockAverage.setItemRetailSalekTotal(itemRetailSaleTotal);
                stockAverages.add(stockAverage);
            }
        }
        totalStock = 0.0;
        totalPurchase = 0.0;
        totalRetailSale = 0.0;
        for (StockAverage sa : stockAverages) {
            totalStock += sa.getItemStockTotal();
            totalPurchase += sa.getItemPurchaseTotal();
            totalRetailSale += sa.getItemRetailSalekTotal();
        }
        //System.out.println("totalStock = " + totalStock);
        //System.out.println("totalPurchase = " + totalPurchase);
        //System.out.println("totalRetailSale = " + totalRetailSale);
    }

    public void averageByDatePercentage() {
        //    System.out.println("averageByDatePercentage = ");
        Calendar frm = Calendar.getInstance();
        frm.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);

        long lValue = to.getTimeInMillis() - frm.getTimeInMillis();
        double dayCount = 0;
        if (lValue != 0) {
            dayCount = lValue / (1000 * 60 * 60 * 24);
        }

        //System.err.println("Day Count " + dayCount);
        createStockAverageByPer(dayCount);

    }

    public void createStockAverageByPer(double dayCount) {
        //    System.out.println("dayCount = " + dayCount);
        //    System.out.println("createStockAverageByPer");
        stockAverages = new ArrayList<>();
        List<Item> items = getItemController().getDealorItem();
        List<Institution> insList; // = getCompany();
        insList = new ArrayList<>();
        insList.add(getSessionController().getInstitution());
        //    System.out.println("items = " + items);
        for (Item i : items) {
            //    System.out.println("i = " + i);
            double itemStockTotal = 0;
            double itemAverageTotal = 0;
            StockAverage stockAverage = new StockAverage();
            stockAverage.setItem(i);
            stockAverage.setInstitutionStocks(new ArrayList<InstitutionStock>());

            for (Institution ins : insList) {
                //    System.out.println("ins = " + ins);
                double insStockTotal = 0;
                double insAverageTotal = 0;
                double insStock = 0.0;
                InstitutionStock newTable = new InstitutionStock();
                newTable.setInstitution(ins);
                newTable.setDepatmentStocks(new ArrayList<DepartmentStock>());
                //    System.out.println("calAllDepartmentStock");
                List<Object[]> objs = calAllDepartmentStock(ins, i);
                double calPerStock = 0.0;
                //    System.out.println("objs = " + objs);
                for (Object[] obj : objs) {
                    //    System.out.println("obj = " + Arrays.toString(obj));
//                    //System.err.println("Inside ");
                    DepartmentStock r = new DepartmentStock();
                    r.setDepartment((Department) obj[0]);
                    r.setStock((Double) obj[1]);

                    double qty = calDepartmentSaleQtyByPer(r.getDepartment(), i);
                    qty = 0 - qty;
//                    if (qty != 0 && dayCount != 0) {
                    double avg = qty / dayCount;
                    calPerStock = (avg * persentage) / 100;
                    insStock = r.getStock();
                    r.setAverage(avg);
//                    }

//                    //System.out.println("calPerStock = " + calPerStock);
//                    //System.out.println("insStockTotal = " + insStockTotal);
//                    //System.out.println("insAverageTotal = " + insAverageTotal);
                    // if ((insStock < calPerStock)) {
                    //System.out.println("*insStock = " + insStock);
                    //System.out.println("*calPerStock = " + calPerStock);
                    insStockTotal += r.getStock();
                    insAverageTotal += r.getAverage();
                    newTable.getDepatmentStocks().add(r);
                    //}

                }

//                //System.out.println("calPerStock = " + calPerStock);
//                //System.out.println("insStockTotal = " + insStockTotal);
//                //System.out.println("insAverageTotal = " + insAverageTotal);
                newTable.setInstitutionTotal(insStockTotal);
                newTable.setInstitutionAverage(insAverageTotal);

//                if ((insStockTotal != 0 || insAverageTotal != 0) && insStock < calPerStock) {
                stockAverage.getInstitutionStocks().add(newTable);
                itemStockTotal += insStockTotal;
                itemAverageTotal += insAverageTotal;
//                }
            }

//            if (itemAverageTotal != 0 || itemStockTotal != 0) {
            stockAverage.setItemAverageTotal(itemAverageTotal);
            stockAverage.setItemStockTotal(itemStockTotal);
            stockAverages.add(stockAverage);
//            }

        }

    }

    public double findPharmacyMovement(Department department, Item itm, BillType[] bts, Date fd, Date td) {
        try {
            if (itm instanceof Ampp) {
                itm = ((Ampp) pharmacyItem).getAmp();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sql;
        Map m = new HashMap();
        m.put("itm", itm);
        m.put("dep", department);
        m.put("frm", fd);
        m.put("to", td);
        List<BillType> bts1 = Arrays.asList(bts);
        m.put("bts", bts1);
        sql = "select sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i "
                + " where i.bill.department=:dep"
                + " and i.item=:itm "
                + " and i.bill.billType in :bts "
                + " and i.createdAt between :frm and :to  ";
        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);
    }

    public Date findFirstPharmacyMovementDate(Department department, Item itm, BillType[] bts, Date fd, Date td) {
        try {
            if (itm instanceof Ampp) {
                itm = ((Ampp) pharmacyItem).getAmp();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String sql;
        Map m = new HashMap();
        m.put("itm", itm);
        m.put("dep", department);
        m.put("frm", fd);
        m.put("to", td);
        List<BillType> bts1 = Arrays.asList(bts);
        m.put("bts", bts1);
        sql = "select i "
                + " from BillItem i "
                + " where i.bill.department=:dep"
                + " and i.item=:itm "
                + " and i.bill.billType in :bts "
                + " and i.bill.createdAt between :frm and :to  "
                + " order by i.id";
        BillItem d = getBillItemFacade().findFirstBySQL(sql, m, TemporalType.TIMESTAMP);
        if (d == null) {
            return fd;
        } else if (d.getBill() != null && d.getBill().getCreatedAt() != null) {
            return d.getBill().getCreatedAt();
        } else if (d.getCreatedAt() != null) {
            return d.getCreatedAt();
        } else {
            return fd;
        }
    }

    public double calDepartmentSaleQtyByPer(Department department, Item itm) {

        if (itm instanceof Ampp) {
            itm = ((Ampp) pharmacyItem).getAmp();
        }

        String sql;
        Map m = new HashMap();
        m.put("itm", itm);
        m.put("dep", department);
        m.put("frm", getFromDate());
        m.put("to", getToDate());
        m.put("btp", BillType.PharmacyPre);
        m.put("refType", BillType.PharmacySale);
        sql = "select sum(i.pharmaceuticalBillItem.qty) "
                + " from BillItem i where i.bill.department=:dep"
                + " and i.bill.referenceBill.billType=:refType "
                + " and i.item=:itm and i.bill.billType=:btp and "
                + " i.createdAt between :frm and :to  ";

        return getBillItemFacade().findDoubleByJpql(sql, m, TemporalType.TIMESTAMP);

    }

    public void averageByMonthByPercentage() {
        Calendar frm = Calendar.getInstance();
        frm.setTime(fromDate);
        Calendar to = Calendar.getInstance();
        to.setTime(toDate);

        long lValue = to.getTimeInMillis() - frm.getTimeInMillis();
        double monthCount = 0;
        if (lValue != 0) {
            monthCount = lValue / (1000 * 60 * 60 * 24 * 30);
        }

        //System.err.println("Month Count " + monthCount);
        createStockAverageByPer(Math.abs(monthCount));

    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public DepartmentFacade getDepartmentFacade() {
        return departmentFacade;
    }

    public void setDepartmentFacade(DepartmentFacade departmentFacade) {
        this.departmentFacade = departmentFacade;
    }

    public Item getPharmacyItem() {

        return pharmacyItem;
    }

    public void setPharmacyItem(Item pharmacyItem) {

        makeNull();

        this.pharmacyItem = pharmacyItem;
        createInstitutionSale();
        createInstitutionStock();
        createInstitutionTransferIssue();
        createInstitutionTransferReceive();
        averageByMonth(pharmacyItem);
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public Date getFromDate() {
        if (fromDate == null) {
//            Date date = ;
            fromDate = getCommonFunctions().getAddedDate(new Date(), -30);
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        makeNull();
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = new Date();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        makeNull();
        this.toDate = toDate;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public AmpFacade getAmpFacade() {
        return ampFacade;
    }

    public void setAmpFacade(AmpFacade ampFacade) {
        this.ampFacade = ampFacade;
    }

    public List<DepartmentSale> getDepartmentSale() {
        return departmentSale;
    }

    public void setDepartmentSale(List<DepartmentSale> departmentSale) {
        this.departmentSale = departmentSale;
    }

    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

    public void setInstitutionFacade(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
    }

    public List<BillItem> getDirectPurchase() {
        return directPurchase;
    }

    public void setDirectPurchase(List<BillItem> directPurchase) {
        this.directPurchase = directPurchase;
    }

    public List<InstitutionStock> getInstitutionStocks() {
        return institutionStocks;
    }

    public void setInstitutionStocks(List<InstitutionStock> institutionStocks) {
        this.institutionStocks = institutionStocks;
    }

    public List<InstitutionSale> getInstitutionSales() {
        return institutionSales;
    }

    public void setInstitutionSales(List<InstitutionSale> institutionSales) {
        this.institutionSales = institutionSales;
    }

    public List<InstitutionSale> getInstitutionTransferIssue() {
        return institutionTransferIssue;
    }

    public void setInstitutionTransferIssue(List<InstitutionSale> institutionTransferIssue) {
        this.institutionTransferIssue = institutionTransferIssue;
    }

    public void setGrns(List<BillItem> grns) {
        this.grns = grns;
    }

    public void setPos(List<BillItem> pos) {
        this.pos = pos;
    }

    public void setGrantStock(double grantStock) {
        this.grantStock = grantStock;
    }

    public double getGrantSaleQty() {
        return grantSaleQty;
    }

    public void setGrantSaleQty(double grantSaleQty) {
        this.grantSaleQty = grantSaleQty;
    }

    public double getGrantSaleValue() {
        return grantSaleValue;
    }

    public void setGrantSaleValue(double grantSaleValue) {
        this.grantSaleValue = grantSaleValue;
    }

    public double getGrantTransferIssueQty() {
        return grantTransferIssueQty;
    }

    public void setGrantTransferIssueQty(double grantTransferIssueQty) {
        this.grantTransferIssueQty = grantTransferIssueQty;
    }

    public double getGrantTransferIssueValue() {
        return grantTransferIssueValue;
    }

    public void setGrantTransferIssueValue(double grantTransferIssueValue) {
        this.grantTransferIssueValue = grantTransferIssueValue;
    }

    public List<InstitutionSale> getInstitutionTransferReceive() {
        return institutionTransferReceive;
    }

    public void setInstitutionTransferReceive(List<InstitutionSale> institutionTransferReceive) {
        this.institutionTransferReceive = institutionTransferReceive;
    }

    public double getGrantTransferReceiveQty() {
        return grantTransferReceiveQty;
    }

    public void setGrantTransferReceiveQty(double grantTransferReceiveQty) {
        this.grantTransferReceiveQty = grantTransferReceiveQty;
    }

    public double getGrantTransferReceiveValue() {
        return grantTransferReceiveValue;
    }

    public void setGrantTransferReceiveValue(double grantTransferReceiveValue) {
        this.grantTransferReceiveValue = grantTransferReceiveValue;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public List<StockAverage> getStockAverages() {
        return stockAverages;
    }

    public void setStockAverages(List<StockAverage> stockAverages) {
        this.stockAverages = stockAverages;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public void setItemController(ItemController itemController) {
        this.itemController = itemController;
    }

    public Double getPersentage() {
        if (persentage == null) {
            persentage = 100.0;
        }
        return persentage;
    }

    public void setPersentage(Double persentage) {
        this.persentage = persentage;
    }

    public double getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(double totalStock) {
        this.totalStock = totalStock;
    }

    public double getTotalPurchase() {
        return totalPurchase;
    }

    public void setTotalPurchase(double totalPurchase) {
        this.totalPurchase = totalPurchase;
    }

    public double getTotalRetailSale() {
        return totalRetailSale;
    }

    public void setTotalRetailSale(double totalRetailSale) {
        this.totalRetailSale = totalRetailSale;
    }

}
