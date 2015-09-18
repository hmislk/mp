/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.UtilityController;
import com.divudi.data.BillType;
import com.divudi.data.dataStructure.PharmacyStockRow;
import com.divudi.data.dataStructure.StockReportRecord;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Category;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.PreBill;
import com.divudi.entity.RefundBill;
import com.divudi.entity.Staff;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.ItemBatch;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.entity.pharmacy.StockHistory;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.facade.AmpFacade;
import com.divudi.facade.ItemBatchFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import com.divudi.facade.StockFacade;
import com.divudi.facade.StockHistoryFacade;
import com.divudi.facade.VmpFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;

/**
 *
 * @author Buddhika
 */
@Named(value = "reportsStock")
@SessionScoped
public class ReportsStock implements Serializable {

    /**
     * Bean Variables
     */
    Department department;
    Staff staff;
    Institution institution;
    private Category category;
    List<Stock> stocks;
    List<PharmacyStockRow> pharmacyStockRows;
    List<PharmacyStockRow> pharmacyStockRowsOne;
    double stockSaleValue;
    double stockPurchaseValue;
    double stockQty;
    List<StockReportRecord> records;
    Date fromDate;
    Date toDate;
    Date fromDateE;
    Date toDateE;
    Vmp vmp;

    public double getStockQty() {
        return stockQty;
    }

    public void setStockQty(double stockQty) {
        this.stockQty = stockQty;
    }

    public Vmp getVmp() {
        return vmp;
    }

    public void setVmp(Vmp vmp) {
        this.vmp = vmp;
    }

    /**
     * Managed Beans
     */
    @Inject
    DealerController dealerController;

    /**
     * EJBs
     */
    @EJB
    StockFacade stockFacade;
    @EJB
    AmpFacade ampFacade;
    @EJB
    VmpFacade vmpFacade;

    /**
     * Methods
     */
    public void fillDepartmentStocks() {
        if (department == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }
        Map m = new HashMap();
        String sql;
        sql = "select s from Stock s where s.department=:d order by s.itemBatch.item.name";
        m.put("d", department);
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;
        for (Stock ts : stocks) {
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
        }
    }

    public void fillDepartmentNonEmptyStocks() {
        Map m = new HashMap();
        String sql;
        if (department == null) {
            sql = "select s from Stock s where s.stock>:z and s.department=:d order by s.itemBatch.item.name";
            m.put("d", department);
            m.put("z", 0.0);
        } else {
            sql = "select s from Stock s where s.stock>:z order by s.itemBatch.item.name";
            m.put("z", 0.0);
        }
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;
        for (Stock ts : stocks) {
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
        }
    }

    Item item;

    public List<PharmacyStockRow> getPharmacyStockRowsOne() {
        return pharmacyStockRowsOne;
    }

    public void setPharmacyStockRowsOne(List<PharmacyStockRow> pharmacyStockRowsOne) {
        this.pharmacyStockRowsOne = pharmacyStockRowsOne;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
    
    
    
    public String fillAllDepartmentStocksByAmp() {
        Map m = new HashMap();
        String sql;
        sql = "select new com.divudi.data.dataStructure.PharmacyStockRow"
                + "(s.department, s.itemBatch.item.code, "
                + "s.itemBatch.item.name, "
                + "sum(s.stock), "
                + "sum(s.itemBatch.purcahseRate * s.stock), "
                + "sum(s.itemBatch.retailsaleRate * s.stock))  "
                + "from Stock s where s.stock>:z "
                + "and s.itemBatch.item=:item "
                + "group by s.department, s.itemBatch.item.name, s.itemBatch.item.code "
                + "order by s.department, s.itemBatch.item.name";
        m.put("z", 0.0);
        m.put("item", item);
        List<PharmacyStockRow> lsts = (List) getStockFacade().findObjects(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue += 0.0;

        Stock s;
        
        for (PharmacyStockRow r : lsts) {
            stockPurchaseValue += r.getPurchaseValue();
            stockSaleValue += r.getSaleValue();
        }
        pharmacyStockRows = lsts;

        return "pharmacy_report_all_department_stock_by_item";
    }

    public String fillDepartmentNonEmptyStocksByVmp() {

        Map m = new HashMap();
        String sql;

        if (department == null) {
            sql = "select s from Stock s join TREAT(s.itemBatch.item as Amp) amp "
                    + "where s.stock>:z and amp.vmp=:vmp "
                    + "order by s.itemBatch.item.name";
            m.put("z", 0.0);
            m.put("vmp", vmp);
        } else {
            sql = "select s from Stock s join TREAT(s.itemBatch.item as Amp) amp "
                    + "where s.stock>:z and s.department=:d and amp.vmp=:vmp "
                    + "order by s.itemBatch.item.name";
            m.put("d", department);
            m.put("z", 0.0);
            m.put("vmp", vmp);
        }
        //System.err.println("");
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;
        for (Stock ts : stocks) {
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
        }
        return "pharmacy_report_department_stock_by_single_product";
    }

    Item ampOrVmp;

    public Item getAmpOrVmp() {
        return ampOrVmp;
    }

    public void setAmpOrVmp(Item ampOrVmp) {
        this.ampOrVmp = ampOrVmp;
    }

    public String fillAllDepartmentNonEmptyStocksByVmpOrAmp() {
        Map m = new HashMap();
        String sql;
        if (ampOrVmp instanceof Vmp) {
            sql = "select s from Stock s join TREAT(s.itemBatch.item as Amp) amp "
                    + "where s.stock>:z and amp.vmp=:vmp "
                    + " and s.department is not null "
                    + "order by s.itemBatch.item.name";
            m.put("z", 0.0);
            m.put("vmp", ampOrVmp);
        } else if (ampOrVmp instanceof Amp) {
            sql = "select s from Stock s join TREAT(s.itemBatch.item as Amp) amp "
                    + "where s.stock>:z and amp=:amp "
                    + " and s.department is not null "
                    + "order by s.itemBatch.item.name";
            m.put("z", 0.0);
            m.put("amp", ampOrVmp);
        } else {
            return "";
        }
        //System.err.println("");
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;
        stockQty = 0.0;
        for (Stock ts : stocks) {
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
            stockQty = stockQty + ts.getStock();
        }
        return "/pharmacy/report_all_department_stock_by_single_product_or_item";
    }

    public void fillAllDepartmentEmptyStocks() {
        Map m = new HashMap();
        String sql;
        sql = "select s from Stock s "
                + " where s.stock>:z "
                + " and s.department is null "
                + " order by s.itemBatch.item.name ";
        m.put("z", 0.0);

        //System.err.println("");
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;
        stockQty = 0.0;
        for (Stock ts : stocks) {
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
            stockQty = stockQty + ts.getStock();
        }
    }

    public void fillDepartmentNonEmptyItemStocks() {
        if (department == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }
        Map m = new HashMap();
        String sql;
        sql = "select new com.divudi.data.dataStructure.PharmacyStockRow"
                + "(s.itemBatch.item.code, "
                + "s.itemBatch.item.name, "
                + "sum(s.stock), "
                + "sum(s.itemBatch.purcahseRate * s.stock), "
                + "sum(s.itemBatch.retailsaleRate * s.stock))  "
                + "from Stock s where s.stock>:z and s.department=:d "
                + "group by s.itemBatch.item.name, s.itemBatch.item.code "
                + "order by s.itemBatch.item.name";
        m.put("d", department);
        m.put("z", 0.0);
        List<PharmacyStockRow> lsts = (List) getStockFacade().findObjects(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;

        for (PharmacyStockRow r : lsts) {
            stockPurchaseValue += r.getPurchaseValue();
            stockSaleValue += r.getSaleValue();
        }
        pharmacyStockRows = lsts;
    }

    public void fillDepartmentNonEmptyProductStocks() {

        Map m = new HashMap();
        String sql;
        if (department == null) {
            sql = "select new com.divudi.data.dataStructure.PharmacyStockRow(vmp, sum(s.stock), "
                    + "sum(s.itemBatch.purcahseRate * s.stock), sum(s.itemBatch.retailsaleRate * s.stock))  "
                    + "from Stock s join s.itemBatch.item as amp join amp.vmp as vmp "
                    + "where s.stock>:z  "
                    + "group by vmp, vmp.name "
                    + "order by vmp.name";
            m.put("z", 0.0);
        } else {
            sql = "select new com.divudi.data.dataStructure.PharmacyStockRow(vmp, sum(s.stock), "
                    + "sum(s.itemBatch.purcahseRate * s.stock), sum(s.itemBatch.retailsaleRate * s.stock))  "
                    + "from Stock s join s.itemBatch.item as amp join amp.vmp as vmp "
                    + "where s.stock>:z and s.department=:d "
                    + "group by vmp, vmp.name "
                    + "order by vmp.name";
            m.put("d", department);
            m.put("z", 0.0);
        }
//        //System.out.println("sql = " + sql);
//        //System.out.println("m = " + m);
//        //System.out.println("getStockFacade().findObjects(sql, m) = " + getStockFacade().findObjects(sql, m));
        List<PharmacyStockRow> lsts = (List) getStockFacade().findObjects(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;
        for (PharmacyStockRow r : lsts) {
            stockPurchaseValue += r.getPurchaseValue();
            stockSaleValue += r.getSaleValue();
        }
        pharmacyStockRows = lsts;
    }

    public void fillDepartmentStocksMinus() {
        if (department == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }
        Map m = new HashMap();
        String sql;
        sql = "select s from Stock s where s.stock<0 and s.department=:d order by s.itemBatch.item.name";
        m.put("d", department);
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;
        for (Stock ts : stocks) {
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
        }
    }

    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private StockHistoryFacade stockHistoryFacade;

    private PharmaceuticalBillItem getPreviousPharmacuticalBillByBatch(ItemBatch itemBatch, Department department, Date date) {
        String sql = "Select sh from PharmaceuticalBillItem sh where "
                + " sh.itemBatch=:itmB and sh.billItem.bill.department=:dep "
                + " and (sh.billItem.bill.billType=:btp1 or sh.billItem.bill.billType=:btp2 )"
                + "  and sh.billItem.createdAt between :fd and :td "
                + " order by sh.billItem.createdAt desc";
        HashMap hm = new HashMap();
        hm.put("itmB", itemBatch);
        Calendar cl = Calendar.getInstance();
        cl.set(Calendar.MONTH, 1);
        cl.set(Calendar.DAY_OF_MONTH, 26);
        hm.put("td", date);
        hm.put("fd", cl.getTime());
        hm.put("dep", department);
        hm.put("btp1", BillType.PharmacyGrnBill);
        hm.put("btp2", BillType.PharmacyPurchaseBill);
        return getPharmaceuticalBillItemFacade().findFirstBySQL(sql, hm, TemporalType.TIMESTAMP);
    }

    private StockHistory getPreviousStockHistoryByBatch(ItemBatch itemBatch, Department department, Date date) {
        String sql = "Select sh from StockHistory sh where sh.retired=false and"
                + " sh.itemBatch=:itmB and sh.department=:dep and sh.pbItem.billItem.createdAt<:dt "
                + " order by sh.pbItem.billItem.createdAt desc";
        HashMap hm = new HashMap();
        hm.put("itmB", itemBatch);
        hm.put("dt", date);
        hm.put("dep", department);
        return getStockHistoryFacade().findFirstBySQL(sql, hm, TemporalType.TIMESTAMP);
    }

    public void fillDepartmentStocksError() {
        Set<Stock> stockSet = new HashSet<>();
        String sql;
        Map temMap = new HashMap();

        sql = "select p from PharmaceuticalBillItem p where "
                + " p.billItem.bill.department=:dep "
                + " and p.billItem.createdAt>:date and "
                + "  p.stockHistory is not null order by p.stockHistory.id ";

        temMap.put("dep", department);
        temMap.put("date", date);

        List<PharmaceuticalBillItem> list = getPharmaceuticalBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        for (PharmaceuticalBillItem b : list) {
            //System.err.println("Item Name " + b.getBillItem().getItem().getName());
            //System.err.println("History Id " + b.getStockHistory().getId());
            //System.err.println("Stock History " + b.getStockHistory().getStockQty());
            //System.err.println("Department " + b.getBillItem().getBill().getDepartment().getName());
            StockHistory sh = getPreviousStockHistoryByBatch(b.getItemBatch(), b.getBillItem().getBill().getDepartment(), b.getBillItem().getCreatedAt());
            PharmaceuticalBillItem phi = getPreviousPharmacuticalBillByBatch(b.getStock().getItemBatch(), b.getBillItem().getBill().getDepartment(), b.getBillItem().getCreatedAt());

            double calculatedStk = 0;
            boolean flg = false;
            if (sh != null) {
                //System.out.println("Previuos Stock " + sh.getStockQty());
                calculatedStk = (sh.getStockQty() + sh.getPbItem().getQtyInUnit() + sh.getPbItem().getFreeQtyInUnit());
                flg = true;
            } else if (phi != null) {
                calculatedStk = phi.getQtyInUnit() + phi.getFreeQtyInUnit();
                flg = true;
            }

            //System.out.println("calculated History Qty " + calculatedStk);
            if (flg == true && b.getStockHistory().getStockQty() != calculatedStk) {
                stockSet.add(b.getStock());
                //System.out.println("TRUE");
            }

            //System.out.println("#########");
        }

        stocks = new ArrayList<>();
        for (Stock s : stockSet) {
            stocks.add(s);
        }

    }

    public void fillDepartmentStocksError2() {
        if (department == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }
        Map m = new HashMap();
        String sql;
        sql = "select s from Stock s where s.department=:d order by s.itemBatch.item.name";
        m.put("d", department);
        stocks = getStockFacade().findBySQL(sql, m);
        Set<Stock> tmpStockList = new HashSet<>();

        for (Stock st : stocks) {
            sql = "Select ph from PharmaceuticalBillItem ph where ph.stock=:st "
                    + " and ph.billItem.createdAt>:date  "
                    + " and ph.stockHistory is not null  "
                    + " order by ph.stockHistory.id ";

            m.clear();
            m.put("st", st);
            m.put("date", date);

            List<PharmaceuticalBillItem> phList = getPharmaceuticalBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

            PharmaceuticalBillItem previousPh = null;
            double calculatedStock = 0;

            for (PharmaceuticalBillItem ph : phList) {
                if (previousPh == null) {
                    previousPh = ph;
                    calculatedStock = ph.getStockHistory().getStockQty();
                    continue;
                }
                double preHistoryQty = 0;
                double curHistory = 0;

                if (previousPh.getStockHistory() != null) {
                    preHistoryQty = previousPh.getStockHistory().getStockQty();
                }

                if (ph.getStockHistory() != null) {
                    curHistory = ph.getStockHistory().getStockQty();
                }

                double calcualtedQty = preHistoryQty + previousPh.getQtyInUnit() + previousPh.getFreeQtyInUnit();

                switch (ph.getBillItem().getBill().getBillType()) {
                    case PharmacyGrnBill:
                    case PharmacyPurchaseBill:
                    case PharmacyTransferReceive:
                        if (ph.getBillItem().getBill() instanceof BilledBill) {
                            calculatedStock += Math.abs(ph.getQtyInUnit());
                            calculatedStock += Math.abs(ph.getFreeQtyInUnit());
                        } else if (ph.getBillItem().getBill() instanceof CancelledBill || ph.getBillItem().getBill() instanceof RefundBill) {
                            calculatedStock -= Math.abs(ph.getQtyInUnit());
                            calculatedStock -= Math.abs(ph.getFreeQtyInUnit());
                        }
                        break;
                    case PharmacyGrnReturn:
                    case PurchaseReturn:
                    case PharmacyTransferIssue:
                        if (ph.getBillItem().getBill() instanceof BilledBill) {
                            calculatedStock -= Math.abs(ph.getQtyInUnit());
                            calculatedStock -= Math.abs(ph.getFreeQtyInUnit());
                        } else if (ph.getBillItem().getBill() instanceof CancelledBill || ph.getBillItem().getBill() instanceof RefundBill) {
                            calculatedStock += Math.abs(ph.getQtyInUnit());
                            calculatedStock += Math.abs(ph.getFreeQtyInUnit());
                        }
                        break;
                    case PharmacyPre:
                        if (ph.getBillItem().getBill() instanceof PreBill) {
                            if (ph.getBillItem().getBill().getReferenceBill() == null) {
                                break;
                            }
                            calculatedStock -= Math.abs(ph.getQtyInUnit());

                        } else if (ph.getBillItem().getBill() instanceof CancelledBill || ph.getBillItem().getBill() instanceof RefundBill) {
                            calculatedStock += Math.abs(ph.getQtyInUnit());
                        }
                        break;
                    default:

                }

                if (calcualtedQty != curHistory) {
                    //System.err.println("Itm " + ph.getBillItem().getItem().getName());
                    //System.err.println("Prv History Qty " + preHistoryQty);
                    //System.err.println("Prv Qty " + previousPh.getQtyInUnit());
                    //System.err.println("Prv Free Qty " + previousPh.getFreeQtyInUnit());
                    //System.err.println("History " + curHistory);
                    //System.err.println("######");
                    st.setCalculated(calculatedStock);
                    tmpStockList.add(st);
                } else {
                    //System.out.println("Itm " + ph.getBillItem().getItem().getName());
                    //System.out.println("Prv History Qty " + preHistoryQty);
                    //System.out.println("Prv Qty " + previousPh.getQtyInUnit());
                    //System.out.println("Prv Free Qty " + previousPh.getFreeQtyInUnit());
                    //System.out.println("History " + curHistory);
                    //System.out.println("######");
                }

                previousPh = ph;
            }

        }

        List<Stock> stk = new ArrayList<>();
        for (Stock st : tmpStockList) {
            stk.add(st);
        }

        stocks = stk;

    }

    private Date date;

    public void fillDepartmentExpiaryStocks() {
        if (department == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }
        Map m = new HashMap();
        String sql;
        sql = "select s from Stock s where s.department=:d and s.itemBatch.dateOfExpire between :fd and :td order by s.itemBatch.dateOfExpire";
        m.put("d", department);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;
        for (Stock ts : stocks) {
//            ts.getItemBatch().getDateOfExpire()
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
        }
    }

    public void fillDepartmentNonmovingStocks() {
        if (department == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }
        Map m = new HashMap();
        String sql;
        sql = "select s from Stock s where s.department=:d and s.stock > 0 and s.itemBatch.item not in (select bi.item FROM BillItem bi where  bi.bill.department=:d and (bi.bill.billType=:t1 or bi.bill.billType=:t2) and bi.bill.billDate between :fd and :td group by bi.item having SUM(bi.qty) > 0 ) order by s.itemBatch.dateOfExpire";
        m.put("d", department);
        m.put("t1", BillType.PharmacyTransferIssue);
        m.put("t2", BillType.PharmacyPre);
        m.put("fd", getFromDateE());
        m.put("td", getToDateE());
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;
        for (Stock ts : stocks) {
//            ts.getItemBatch().getDateOfExpire()
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
        }
    }

    public void fillStaffStocks() {
        if (staff == null) {
            UtilityController.addErrorMessage("Please select a staff member");
            return;
        }
        Map m = new HashMap();
        String sql;
        sql = "select s from Stock s where s.staff=:d order by s.itemBatch.item.name";
        m.put("d", staff);
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;
        for (Stock ts : stocks) {
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
        }
    }

    public void fillDistributorStocks() {
        if (department == null || institution == null) {
            UtilityController.addErrorMessage("Please select a department && Dealor");
            return;
        }
        Map m;
        String sql;
        records = new ArrayList<>();

        m = new HashMap();
        m.put("ins", institution);
        m.put("dep", department);
        sql = "select s from Stock s where s.department=:dep and s.itemBatch.item.id in "
                + "(select item.id from ItemsDistributors id join id.item as item where id.retired=false and id.institution=:ins)"
                + " order by s.itemBatch.item.name ";
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;

        for (Stock ts : stocks) {
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
        }

    }

    public void fillCategoryStocks() {
        if (department == null || category == null) {
            UtilityController.addErrorMessage("Please select a department && Category");
            return;
        }
        Map m;
        String sql;
        records = new ArrayList<>();

        m = new HashMap();
        m.put("cat", category);
        m.put("dep", department);
        sql = "select s from Stock s where s.department=:dep and s.itemBatch.item.category=:cat order by s.itemBatch.item.name";
        stocks = getStockFacade().findBySQL(sql, m);
        stockPurchaseValue = 0.0;
        stockSaleValue = 0.0;

        for (Stock ts : stocks) {
            stockPurchaseValue = stockPurchaseValue + (ts.getItemBatch().getPurcahseRate() * ts.getStock());
            stockSaleValue = stockSaleValue + (ts.getItemBatch().getRetailsaleRate() * ts.getStock());
        }

    }

    List<Stock> bulkAdjustmentStocks;

    public List<Stock> getBulkAdjustmentStocks() {
        return bulkAdjustmentStocks;
    }

    public void setBulkAdjustmentStocks(List<Stock> bulkAdjustmentStocks) {
        this.bulkAdjustmentStocks = bulkAdjustmentStocks;
    }

    public void updateItemBatch(ItemBatch s) {
        getItemBatchFacade().edit(s);
    }

    public void updateStock(Stock s) {
        getStockFacade().edit(s);
        getItemBatchFacade().edit(s.getItemBatch());
    }

    @EJB
    ItemBatchFacade itemBatchFacade;

    public ItemBatchFacade getItemBatchFacade() {
        return itemBatchFacade;
    }

    public void fillCategoryStocksForBulkAdjustment() {
        if (department == null || category == null) {
            UtilityController.addErrorMessage("Please select a department && Category");
            return;
        }
        Map m;
        String sql;
        m = new HashMap();
        m.put("cat", category);
        m.put("dep", department);
        sql = "select s from Stock s where s.department=:dep and s.itemBatch.item.category=:cat order by s.itemBatch.item.name";
        bulkAdjustmentStocks = getStockFacade().findBySQL(sql, m);
    }

    public void fillCategoryStocksForBulkAdjustmentForBatchesWithoutDept() {
        if (category == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }
        Map m;
        String sql;
        m = new HashMap();
        m.put("cat", category);
        sql = "select s from Stock s where (s.department is null and s.staff is null) and s.itemBatch.item.category=:cat order by s.itemBatch.item.name";
        bulkAdjustmentStocks = getStockFacade().findBySQL(sql, m);
    }
    
    public void fillAllDistributorStocks() {
        if (department == null) {
            UtilityController.addErrorMessage("Please select a department");
            return;
        }
        Map m;
        String sql;
        records = new ArrayList<>();
        List<Institution> dealers = getDealerController().getItems();
        stockSaleValue = 0.0;
        stockPurchaseValue = 0.0;
        for (Institution i : dealers) {
            ////System.out.println("i = " + i);
            m = new HashMap();
            m.put("ins", i);
            m.put("d", department);
            sql = "select sum(s.stock),sum(s.stock * s.itemBatch.purcahseRate),sum(s.stock * s.itemBatch.retailsaleRate)"
                    + " from Stock s where s.department=:d and s.itemBatch.item.id in (select item.id from ItemsDistributors id join id.item as item where id.retired=false and id.institution=:ins)";
            Object[] objs = getStockFacade().findSingleAggregate(sql, m);

            if (objs[0] != null && (Double) objs[0] > 0) {
                StockReportRecord r = new StockReportRecord();
                ////System.out.println("objs = " + objs);
                r.setInstitution(i);
                r.setQty((Double) objs[0]);
                r.setPurchaseValue((Double) objs[1]);
                r.setRetailsaleValue((Double) objs[2]);
                records.add(r);
                stockPurchaseValue = stockPurchaseValue + r.getPurchaseValue();
                stockSaleValue = stockSaleValue + r.getRetailsaleValue();
            }
        }

    }

    /**
     * Getters & Setters
     *
     * @return
     */
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

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    /**
     * Constructor
     */
    public ReportsStock() {
    }

    public double getStockSaleValue() {
        return stockSaleValue;
    }

    public void setStockSaleValue(double stockSaleValue) {
        this.stockSaleValue = stockSaleValue;
    }

    public double getStockPurchaseValue() {
        return stockPurchaseValue;
    }

    public void setStockPurchaseValue(double stockPurchaseValue) {
        this.stockPurchaseValue = stockPurchaseValue;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public List<StockReportRecord> getRecords() {
        return records;
    }

    public void setRecords(List<StockReportRecord> records) {
        this.records = records;
    }

    public DealerController getDealerController() {
        return dealerController;
    }

    public void setDealerController(DealerController dealerController) {
        this.dealerController = dealerController;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = Calendar.getInstance().getTime();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 3);
            toDate = c.getTime();
        }
        return toDate;
    }

    public void fillThreeMonthsExpiary() {
        fromDate = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 3);
        toDate = c.getTime();
        fillDepartmentExpiaryStocks();;
    }

    public void fillSixMonthsExpiary() {
        fromDate = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 6);
        toDate = c.getTime();
        fillDepartmentExpiaryStocks();;
    }

    public void fillOneYearExpiary() {
        fromDate = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 1);
        toDate = c.getTime();
        fillDepartmentExpiaryStocks();;
    }

    public void fillThreeMonthsNonmoving() {
        toDateE = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) - 3);
        fromDateE = c.getTime();
        fillDepartmentNonmovingStocks();
    }

    public void fillSixMonthsNonmoving() {
        toDateE = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) - 6);
        fromDateE = c.getTime();
        fillDepartmentNonmovingStocks();
    }

    public void fillOneYearNonmoving() {
        toDateE = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
        fromDateE = c.getTime();
        fillDepartmentNonmovingStocks();
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getFromDateE() {
        if (fromDateE == null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MONTH, c.get(Calendar.MONTH) - 3);
            fromDateE = c.getTime();
        }
        return fromDateE;
    }

    public void setFromDateE(Date fromDateE) {
        this.fromDateE = fromDateE;
    }

    public Date getToDateE() {
        if (toDateE == null) {
            toDateE = Calendar.getInstance().getTime();
        }
        return toDateE;
    }

    public void setToDateE(Date toDateE) {
        this.toDateE = toDateE;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public StockHistoryFacade getStockHistoryFacade() {
        return stockHistoryFacade;
    }

    public void setStockHistoryFacade(StockHistoryFacade stockHistoryFacade) {
        this.stockHistoryFacade = stockHistoryFacade;
    }

    public List<PharmacyStockRow> getPharmacyStockRows() {
        return pharmacyStockRows;
    }

    public void setPharmacyStockRows(List<PharmacyStockRow> pharmacyStockRows) {
        this.pharmacyStockRows = pharmacyStockRows;
    }

    public AmpFacade getAmpFacade() {
        return ampFacade;
    }

    public void setAmpFacade(AmpFacade ampFacade) {
        this.ampFacade = ampFacade;
    }

    public VmpFacade getVmpFacade() {
        return vmpFacade;
    }

    public void setVmpFacade(VmpFacade vmpFacade) {
        this.vmpFacade = vmpFacade;
    }

}
