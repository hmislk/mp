/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.bean.SessionController;
import com.divudi.data.BillType;
import com.divudi.data.HistoryType;
import com.divudi.data.ItemBatchQty;
import com.divudi.data.StockQty;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.Staff;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.ItemBatch;
import com.divudi.entity.pharmacy.MeasurementUnit;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.PharmaceuticalItemCategory;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.entity.pharmacy.StockHistory;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.Vmpp;
import com.divudi.entity.pharmacy.Vtm;
import com.divudi.entity.pharmacy.VtmsVmps;
import com.divudi.facade.AmpFacade;
import com.divudi.facade.AmppFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.CategoryFacade;
import com.divudi.facade.ItemBatchFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.ItemsDistributorsFacade;
import com.divudi.facade.MeasurementUnitFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import com.divudi.facade.PharmaceuticalItemCategoryFacade;
import com.divudi.facade.StockFacade;
import com.divudi.facade.StockHistoryFacade;
import com.divudi.facade.VmpFacade;
import com.divudi.facade.VmppFacade;
import com.divudi.facade.VtmFacade;
import com.divudi.facade.VtmsVmpsFacade;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 *
 * @author Buddhika
 */
@Stateless
public class PharmacyBean {

    @EJB
    PharmaceuticalItemCategoryFacade PharmaceuticalItemCategoryFacade;
    @EJB
    VmppFacade vmppFacade;
    @EJB
    StockFacade stockFacade;
    @EJB
    private ItemFacade itemFacade;
    @EJB
    private AmpFacade ampFacade;
    @EJB
    private ItemBatchFacade itemBatchFacade;
    @EJB
    private PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade;
    @EJB
    private ItemsDistributorsFacade itemsDistributorsFacade;
    @EJB
    private CategoryFacade categoryFacade;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    StockHistoryFacade stockHistoryFacade;

    public PharmaceuticalItemCategoryFacade getPharmaceuticalItemCategoryFacade() {
        return PharmaceuticalItemCategoryFacade;
    }

    public List<Stock> availableStocks(Item item, Department dept) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", dept);
        double d = 0.0;
        m.put("s", d);
        m.put("item", item);
        sql = "select i from Stock i where i.stock >:s and i.department=:d and i.itemBatch.item=:item order by i.itemBatch.dateOfExpire ";
        items = getStockFacade().findBySQL(sql, m);
        return items;
    }

    public List<Stock> getStocksFromIemBatch(Item item, Department dept) {
        List<Stock> items;
        String sql;
        Map m = new HashMap();
        m.put("d", dept);
        double d = 0.0;
        m.put("s", d);
        m.put("item", item);
        sql = "select i from Stock i where i.stock >:s and i.department=:d and i.itemBatch.item=:item order by i.itemBatch.dateOfExpire ";
        items = getStockFacade().findBySQL(sql, m);
        return items;
    }

    public void setPharmaceuticalItemCategoryFacade(PharmaceuticalItemCategoryFacade PharmaceuticalItemCategoryFacade) {
        this.PharmaceuticalItemCategoryFacade = PharmaceuticalItemCategoryFacade;
    }

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public double getStockQty(ItemBatch batch, Department department) {
        String sql;
        HashMap hm = new HashMap();
        sql = "select sum(s.stock) from Stock s where s.itemBatch=:batch "
                + " and s.department=:dep";
        hm.put("batch", batch);
        hm.put("dep", department);
        return getStockFacade().findDoubleByJpql(sql, hm);
    }

    public double getStockQty(ItemBatch batch, Staff staff) {
        String sql;
        HashMap hm = new HashMap();
        sql = "select sum(s.stock) from Stock s where s.itemBatch=:batch "
                + " and s.staff=:stf";
        hm.put("batch", batch);
        hm.put("stf", staff);
        return getStockFacade().findAggregateDbl(sql);
    }

    public double getStockQty(ItemBatch batch, Institution institution) {
        String sql;
        sql = "select sum(s.stock) from Stock s where s.itemBatch.id = " + batch.getId() + " and s.department.institution.id = " + institution.getId();
        return getStockFacade().findAggregateDbl(sql);
    }

    public double getStockQty(Item item, Department department) {
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }
        String sql;
        Map m = new HashMap();
        m.put("d", department);
        m.put("i", item);
        sql = "select sum(s.stock) from Stock s where s.department=:d and s.itemBatch.item=:i";
        return getStockFacade().findDoubleByJpql(sql, m);

    }

    public double getStockQty(Item item, Institution institution) {
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }
        String sql;
        Map m = new HashMap();
        m.put("d", institution);
        m.put("i", item);
        sql = "select sum(s.stock) from Stock s where s.department.institution=:d and s.itemBatch.item=:i";
        return getStockFacade().findAggregateDbl(sql, m);
    }

    public double getStockByPurchaseValue(ItemBatch batch) {
        Map m = new HashMap<>();
        String sql;
        m.put("i", batch);
        sql = "Select sum(s.itemBatch.purcahseRate * s.stock) from Stock s where s.itemBatch=:i";
        return getItemBatchFacade().findDoubleByJpql(sql, m);
    }

    public double getStockByPurchaseValue(Item item) {
        Map m = new HashMap<>();
        String sql;
        m.put("i", item);
        sql = "Select sum(s.itemBatch.purcahseRate * s.stock) from Stock s where s.itemBatch.item=:i";
        return getItemBatchFacade().findDoubleByJpql(sql, m);
    }

    public double getStockByPurchaseValue(Item item, Department dept) {
        Map m = new HashMap<>();
        String sql;
        m.put("i", item);
        m.put("d", dept);
        sql = "Select sum(s.itemBatch.purcahseRate * s.stock) from Stock s where s.itemBatch.item=:i and s.department=:d";
        return getItemBatchFacade().findDoubleByJpql(sql, m);
    }

    public boolean resetStock(Stock stock, double qty) {
        stock.setStock(qty);
        getStockFacade().edit(stock);
        return true;
    }

    public Stock addToStock(ItemBatch batch, double qty, Staff staff) {
        System.err.println("Item Batch " + batch);
        System.err.println("Adding Staff QTY " + qty);
        String sql;
        sql = "Select s from Stock s where s.itemBatch.id = " + batch.getId()
                + " and s.staff.id = " + staff.getId();
        Stock s = getStockFacade().findFirstBySQL(sql);
        if (s == null) {
            s = new Stock();
            s.setStaff(staff);
            s.setItemBatch(batch);
        }
        s.setStock(s.getStock() + qty);
        if (s.getId() == null || s.getId() == 0) {
            getStockFacade().create(s);
        } else {
            getStockFacade().edit(s);
        }
        return s;
    }

    public Stock addToStock(ItemBatch batch, double qty, Department department) {
        System.err.println("Adding Stock : ");

        String sql;
        HashMap hm = new HashMap();
        sql = "Select s from Stock s where s.itemBatch=:bch and s.department=:dep";
        hm.put("bch", batch);
        hm.put("dep", department);
        Stock s = getStockFacade().findFirstBySQL(sql, hm);
//        System.err.println("ss" + s);
        if (s == null) {
            s = new Stock();
            s.setDepartment(department);
            s.setItemBatch(batch);
        }
        s.setStock(s.getStock() + qty);
//        System.err.println("Stock 1 : " + s.getStock());
//        System.err.println("Stock 2 : " + qty);
//        System.err.println("Stock 3 : " + s);
//        System.err.println("Stock 4 : " + s.getId());
        if (s.getId() == null || s.getId() == 0) {
            //  Stock ss = new Stock();
            getStockFacade().create(s);
        } else {
            getStockFacade().edit(s);
        }
        return s;
    }

    public boolean deductFromStock(ItemBatch batch, double qty, Department department) {
        String sql;
        HashMap hm = new HashMap();
        sql = "Select s from Stock s where s.itemBatch=:bch and"
                + " s.department=:dep";
        hm.put("bch", batch);
        hm.put("dep", department);
        Stock s = getStockFacade().findFirstBySQL(sql, hm);
        if (s == null) {
            s = new Stock();
            s.setDepartment(department);
            s.setItemBatch(batch);
        }
        if (s.getStock() < qty) {
            return false;
        }
        s.setStock(s.getStock() - qty);
        if (s.getId() == null || s.getId() == 0) {
            getStockFacade().create(s);
        } else {
            getStockFacade().edit(s);
        }
        return true;
    }

    public boolean deductFromStock(ItemBatch batch, double qty, Staff staff) {
        String sql;
        sql = "Select s from Stock s where s.itemBatch.id = " + batch.getId() + " and s.staff.id = " + staff.getId();
        Stock s = getStockFacade().findFirstBySQL(sql);
        if (s == null) {
            s = new Stock();
            s.setStaff(staff);
            s.setItemBatch(batch);
        }
        if (s.getStock() < qty) {
            return false;
        }
        s.setStock(s.getStock() - qty);
        if (s.getId() == null || s.getId() == 0) {
            getStockFacade().create(s);
        } else {
            getStockFacade().edit(s);
        }
        return true;
    }

    public boolean deductFromStock(ItemBatch batch, double qty, Department department, boolean minusAllowed) {
        if (!minusAllowed) {
            return deductFromStock(batch, qty, department);
        }
        String sql;
        sql = "Select s from Stock s where s.itemBatch.id = " + batch.getId() + " and s.department.id = " + department.getId();
        Stock s = getStockFacade().findFirstBySQL(sql);
        if (s == null) {
            s = new Stock();
            s.setDepartment(department);
            s.setItemBatch(batch);
        }
        s.setStock(s.getStock() - qty);
        if (s.getId() == null || s.getId() == 0) {
            getStockFacade().create(s);
        } else {
            getStockFacade().edit(s);
        }
        return true;
    }

    public List<ItemBatchQty> deductFromStock(Item item, double qty, Department department) {
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }

        if (qty <= 0) {
            return new ArrayList<>();
        }
        String sql;
        Map m = new HashMap();
        m.put("i", item);
        m.put("d", department);
        sql = "select s from Stock s where s.itemBatch.item=:i "
                + " and s.department=:d order by s.itemBatch.dateOfExpire asc";
        List<Stock> stocks = getStockFacade().findBySQL(sql, m);
        List<ItemBatchQty> dl = new ArrayList<>();
        double toAddQty = qty;
        System.err.println("QTY 1 : " + toAddQty);
        for (Stock s : stocks) {
            if (s.getStock() >= toAddQty) {
                deductFromStock(s.getItemBatch(), toAddQty, department);
                System.err.println("QTY 2 : " + s.getStock());
                dl.add(new ItemBatchQty(s.getItemBatch(), toAddQty));
                break;
            } else {
                toAddQty = toAddQty - s.getStock();
                dl.add(new ItemBatchQty(s.getItemBatch(), s.getStock()));
                deductFromStock(s.getItemBatch(), s.getStock(), department);
            }
        }
        return dl;
    }

    public List<StockQty> getStockByQty(Item item, double qty, Department department) {
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }

        if (qty <= 0) {
            return new ArrayList<>();
        }
        String sql;
        Map m = new HashMap();
        m.put("i", item);
        m.put("d", department);
        sql = "select s from Stock s where s.itemBatch.item=:i "
                + " and s.department=:d order by s.itemBatch.dateOfExpire asc";
        List<Stock> stocks = getStockFacade().findBySQL(sql, m);
        List<StockQty> list = new ArrayList<>();
        double toAddQty = qty;
        System.err.println("QTY 1 : " + toAddQty);
        for (Stock s : stocks) {
            if (s.getStock() >= toAddQty) {
                //   deductFromStock(s.getItemBatch(), toAddQty, department);
                System.err.println("QTY 2 : " + s.getStock());
                list.add(new StockQty(s, toAddQty));
                break;
            } else {
                toAddQty = toAddQty - s.getStock();
                list.add(new StockQty(s, s.getStock()));
                //      deductFromStock(s.getItemBatch(), s.getStock(), department);
            }
        }
        return list;
    }

    public void deductFromStock(Stock stock, double qty, PharmaceuticalBillItem pbi, Department d) {
        if (stock == null) {
            return;
        }
        addToStockHistory(pbi,d);
        System.err.println("Before Update " + stock.getStock());
        stock.setStock(stock.getStock() - qty);
        System.err.println("After  Update " + stock.getStock());
        getStockFacade().edit(stock);
    }
    


    public void updateStock(Stock stock, double qty, PharmaceuticalBillItem pbi, Department d) {
        if (stock == null) {
            return;
        }
        addToStockHistory(pbi,d);
        System.err.println("Before Update " + stock.getStock());
        stock.setStock(stock.getStock() + qty);
        System.err.println("After Update " + stock.getStock());

        getStockFacade().edit(stock);
    }
    
  

    public void addToStockHistory(PharmaceuticalBillItem pbi, Department d) {
        StockHistory sh;
        String sql;
        sql = "Select sh from StockHistory sh where sh.pbItem=:pbi";
        Map m = new HashMap();
        m.put("pbi", pbi);
        sh = getStockHistoryFacade().findFirstBySQL(sql, m);
        if (sh == null) {
            sh = new StockHistory();
        }
        
        sh.setFromDate(Calendar.getInstance().getTime());
        sh.setPbItem(pbi);
        sh.setHxDate(Calendar.getInstance().get(Calendar.DATE));
        sh.setHxMonth(Calendar.getInstance().get(Calendar.MONTH));
        sh.setHxWeek(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
        sh.setHxYear(Calendar.getInstance().get(Calendar.YEAR));
        
        sh.setStockAt(Calendar.getInstance().getTime());
        if (pbi != null && pbi.getBillItem() != null && pbi.getBillItem().getItem() != null) {
            sh.setDepartment(d);
            sh.setInstitution(d.getInstitution());
            sh.setStockQty(getStockQty(pbi.getBillItem().getItem(), d));
            sh.setItem(pbi.getBillItem().getItem());
            if (pbi.getStock() != null && pbi.getStock().getItemBatch() != null) {
                sh.setItemBatch(pbi.getStock().getItemBatch());
            } else {
                sh.setItemBatch(pbi.getItemBatch());
            }

        }
        getStockHistoryFacade().edit(sh);
    }

    public void addToStock(Stock stock, double qty, PharmaceuticalBillItem pbi, Department d) {
        if (stock == null) {
            return;
        }
        if (stock.getStock() == null) {
            stock.setStock(0.0);
        }
        addToStockHistory(pbi,d);
        System.err.println("Before Update" + stock.getStock());
        stock.setStock(stock.getStock() + qty);
        System.err.println("After Update " + stock.getStock());
        getStockFacade().edit(stock);
    }

    public List<ItemBatchQty> deductFromStock(Item item, double qty, Staff staff, PharmaceuticalBillItem pbi, Department d) {
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }
        addToStockHistory(pbi,d);
        String sql;
        sql = "select s from Stock s where s.itemBatch.item.id = " + item.getId() + " and s.staff.id = " + staff.getId() + " order by s.itemBatch.dateOfExpire desc";
        List<Stock> stocks = getStockFacade().findBySQL(sql);
        List<ItemBatchQty> dl = new ArrayList<>();
        double toAddQty = qty;
        for (Stock s : stocks) {
            if (toAddQty <= 0) {
                break;
            }
            if (s.getStock() >= toAddQty) {
                deductFromStock(s.getItemBatch(), toAddQty, staff);
                dl.add(new ItemBatchQty(s.getItemBatch(), toAddQty));
                break;
            } else {
                toAddQty = toAddQty - s.getStock();
                dl.add(new ItemBatchQty(s.getItemBatch(), s.getStock()));
                deductFromStock(s.getItemBatch(), s.getStock(), staff);
            }
        }
        return dl;
    }

    public void addToHistory() {
    }

//    public double getRetailRate(Item item, Department department) {
//
//        System.out.println("getting Retail rate");
//        double rate = getLastRetailRate(item, department);
//        if (item instanceof Ampp) {
//            return rate * item.getDblValue();
//        } else if (item instanceof Amp) {
//            return rate;
//        } else {
//            return 0.0;
//        }
//    }
    public double getWholesaleRate(Item item, Department department) {
        return 0.0;
    }

    public double getRetailRate(ItemBatch batch, Department department) {
        return 0.0;
    }

    public double getRetailRate(Stock stock, PaymentScheme paymentScheme) {
        if (stock == null) {
            return 0.0;
        }
        if (paymentScheme == null) {
            return stock.getItemBatch().getRetailsaleRate();
        } else {
            return stock.getItemBatch().getRetailsaleRate() * (1 + ((paymentScheme.getDiscountPercentForPharmacy()) / 100));
        }
    }

    public double getSaleRate(ItemBatch batch, Department department) {
        return 0.0;
    }

    public double getWholesaleRate(ItemBatch batch, Department department) {
        return 0.0;
    }

    public double getPurchaseRate(ItemBatch batch, Department department) {
        String sql;
        sql = "Select s from Stock s where s.itemBatch=:b and s.department=:d";
        Map m = new HashMap();
        m.put("b", batch);
        m.put("d", department);
        Stock s = getStockFacade().findFirstBySQL(sql, m);
        if (s == null) {
            return 10.0;
        } else {
            return s.getItemBatch().getPurcahseRate();
        }
    }

//    public double getPurchaseRate(Item item, Department department) {
//        System.out.println("getting purchase rate");
//        double rate = getLastPurchaseRate(item, department);
//        if (item instanceof Ampp) {
//            return rate * item.getDblValue();
//        } else if (item instanceof Amp) {
//            return rate;
//        } else {
//            return 0.0;
//        }
//
//    }
    public void reSetPurchaseRate(ItemBatch batch, Department department) {
    }

    public void reSetRetailRate(ItemBatch batch, Department department) {
    }

    public void setPurchaseRate(ItemBatch batch, Department department) {
    }

    public void setPurchaseRate(Item item, Department department) {
    }

    public void setRetailRate(Item item, Department department) {
    }

    public void setSaleRate(Item item, Department department) {
    }

    public void setWholesaleRate(Item item, Department department) {
    }

    public void setRetailRate(ItemBatch batch, Department department) {
    }

    public void setSaleRate(ItemBatch batch, Department department) {
    }

    public void setWholesaleRate(ItemBatch batch, Department department) {
    }

    public ItemBatch getItemBatch(Date doe, String batchNo, Item item) {
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }
        String sql;
        Map m = new HashMap();
        sql = "Select ib from ItemBatch ib where ib.item=:i and ib.dateOfExpire=:doe and ib.batchNo=:batchNo";
        m.put("i", item);
        m.put("batchNo", batchNo);
        m.put("doe", doe);
        ItemBatch ib = getItemBatchFacade().findFirstBySQL(sql, m);
        if (ib == null) {
            ib = new ItemBatch();
            ib.setDateOfExpire(doe);
            ib.setBatchNo(batchNo);
            ib.setItem(item);
            getItemBatchFacade().create(ib);
        }
        return ib;
    }

    public ItemBatch getItemBatch(Date doe, double salePrice, double purchasePrice, Item item) {
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }
        String sql;
        Map m = new HashMap();
        sql = "Select ib from ItemBatch ib where ib.item=:i and ib.dateOfExpire=:doe and ib.purcahseRate=:pr and ib.retailsaleRate=:rr";
        m.put("i", item);
        m.put("pr", purchasePrice);
        m.put("rr", salePrice);
        m.put("doe", doe);
        ItemBatch ib = getItemBatchFacade().findFirstBySQL(sql, m);
        if (ib == null) {
            ib = new ItemBatch();
            ib.setDateOfExpire(doe);
            ib.setPurcahseRate(purchasePrice);
            ib.setRetailsaleRate(salePrice);
            ib.setItem(item);
            getItemBatchFacade().create(ib);
        }
        return ib;
    }

    public VmppFacade getVmppFacade() {
        return vmppFacade;
    }

    public void setVmppFacade(VmppFacade vmppFacade) {
        this.vmppFacade = vmppFacade;
    }

    public Vmpp getVmpp(Ampp ampp, MeasurementUnit packUnit) {
        String sql;
        Map m = new HashMap();
        m.put("vmp", ampp.getAmp().getVmp());
        m.put("s", packUnit);
        m.put("d", ampp.getDblValue());
        sql = "select v from Vmpp v where v.retired=false and v.vmp =:vmp and v.dblValue =:d and v.packUnit=:s";
        Vmpp v = getVmppFacade().findFirstBySQL(sql, m);
        if (v == null) {
            v = new Vmpp();
            v.setVmp(ampp.getAmp().getVmp());
            v.setDblValue(ampp.getDblValue());
            v.setPackUnit(packUnit);
            try {
                v.setName(ampp.getAmp().getVmp().getName() + "(" + ampp.getDblValue() + " " + ampp.getVmpp().getPackUnit().getName() + ")");
            } catch (Exception e) {
                System.err.println("Error : " + e.getMessage());
            }
            getVmppFacade().create(v);
        }
        return v;
    }

    public double getOrderingQty(Item item, Department department) {
        double qty = 10;
        if (item instanceof Ampp) {
            qty /= item.getDblValue();
        }
        return qty;
    }

    public double getMaximumPurchasePriceChange() {
        return 50.0;
    }

    public double getMaximumRetailPriceChange() {
        return 15.0;
    }

    public void setMaximumGrnPriceChange() {
    }

    public void recordPrice() {
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public AmpFacade getAmpFacade() {
        return ampFacade;
    }

    public void setAmpFacade(AmpFacade ampFacade) {
        this.ampFacade = ampFacade;
    }

    public ItemBatchFacade getItemBatchFacade() {
        return itemBatchFacade;
    }

    public void setItemBatchFacade(ItemBatchFacade itemBatchFacade) {
        this.itemBatchFacade = itemBatchFacade;
    }

    public PharmaceuticalBillItemFacade getPharmaceuticalBillItemFacade() {
        return pharmaceuticalBillItemFacade;
    }

    public void setPharmaceuticalBillItemFacade(PharmaceuticalBillItemFacade pharmaceuticalBillItemFacade) {
        this.pharmaceuticalBillItemFacade = pharmaceuticalBillItemFacade;
    }

    public ItemsDistributorsFacade getItemsDistributorsFacade() {
        return itemsDistributorsFacade;
    }

    public void setItemsDistributorsFacade(ItemsDistributorsFacade itemsDistributorsFacade) {
        this.itemsDistributorsFacade = itemsDistributorsFacade;
    }

    public CategoryFacade getCategoryFacade() {
        return categoryFacade;
    }

    public void setCategoryFacade(CategoryFacade categoryFacade) {
        this.categoryFacade = categoryFacade;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public PharmaceuticalItemCategory getPharmaceuticalCategoryByName(String name, boolean createNew) {
        if (name == null || name.trim().equals("")) {
            return null;
        }
        name = name.trim();
        PharmaceuticalItemCategory cat;
        cat = getPharmaceuticalItemCategoryFacade().findFirstBySQL("SELECT c FROM PharmaceuticalItemCategory c Where upper(c.name) = '" + name.toUpperCase() + "' ");
        if (cat == null && createNew == true) {
            cat = new PharmaceuticalItemCategory();
            cat.setName(name);
            getPharmaceuticalItemCategoryFacade().create(cat);
        } else if (cat != null) {
            cat.setRetired(false);
            cat.setName(name);
            getPharmaceuticalItemCategoryFacade().edit(cat);
        }
        return cat;
    }

    public PharmaceuticalItemCategory getPharmaceuticalCategoryByName(String name) {
        return getPharmaceuticalCategoryByName(name, true);
    }

    public MeasurementUnit getUnitByName(String name, boolean createNew) {
        if (name == null || name.trim().equals("")) {
            return null;
        }
        MeasurementUnit m;
        name = name.trim();
        String sql;
        Map map = new HashMap();
        sql = "SELECT c FROM MeasurementUnit c Where upper(c.name) =:n ";
        map.put("n", name.toUpperCase());
        m = getMeasurementUnitFacade().findFirstBySQL(sql, map);
        if (m == null && createNew == true) {
            m = new MeasurementUnit();
            m.setName(name);
            getMeasurementUnitFacade().create(m);
        } else if (m != null) {
            m.setName(name);
            m.setRetired(false);
            getMeasurementUnitFacade().edit(m);
        }
        return m;
    }

    public MeasurementUnit getUnitByName(String name) {
        return getUnitByName(name, true);
    }

    @EJB
    VmpFacade vmpFacade;
    @EJB
    VtmsVmpsFacade vtmsVmpsFacade;
    
    public VtmsVmpsFacade getVtmsVmpsFacade() {
        return vtmsVmpsFacade;
    }

    public void setVtmsVmpsFacade(VtmsVmpsFacade vtmsVmpsFacade) {
        this.vtmsVmpsFacade = vtmsVmpsFacade;
    }

    public VmpFacade getVmpFacade() {
        return vmpFacade;
    }

    public void setVmpFacade(VmpFacade vmpFacade) {
        this.vmpFacade = vmpFacade;
    }

    @EJB
    AmppFacade amppFacade;

    public AmppFacade getAmppFacade() {
        return amppFacade;
    }

    public void setAmppFacade(AmppFacade amppFacade) {
        this.amppFacade = amppFacade;
    }

    public Vmpp getVmpp(Vmp vmp, double issueUnitsPerPack, MeasurementUnit packUnit) {
        if (vmp == null || packUnit == null || vmp.getCategory() == null) {
            System.out.println("vmp is " + vmp);
            System.out.println("pack unit is " + packUnit);
            System.out.println("vmp is " + vmp);
            if (vmp != null) {
                System.out.println("cat is " + vmp.getCategory());
            }
            return null;
        }
        String sql;
        Map m = new HashMap();
        m.put("v", vmp);
        m.put("u", packUnit);
        m.put("d", issueUnitsPerPack);
        sql = "select p from Vmpp p where p.vmp=:v and p.packUnit=:u and p.dblValue=:d";
        Vmpp vmpp = getVmppFacade().findFirstBySQL(sql, m);
        if (vmpp == null) {
            vmpp = new Vmpp();
            vmpp.setVmp(vmp);
            vmpp.setName(vmp.getName() + " " + vmp.getCategory().getName() + " (" + issueUnitsPerPack + " " + packUnit.getName() + ")");
            vmpp.setPackUnit(packUnit);
            vmpp.setCreatedAt(Calendar.getInstance().getTime());
            vmpp.setDblValue(issueUnitsPerPack);
            getVmppFacade().create(vmpp);
        } else {
            vmpp.setRetired(false);
            getVmppFacade().edit(vmpp);
        }
        return vmpp;
    }

    public Ampp getAmpp(Amp amp) {
        String sql = "select a from Ampp a where a.retired=false and a.amp.id=" + amp.getId();
        return getAmppFacade().findFirstBySQL(sql);
    }

    public Ampp getAmpp(Amp amp, double issueUnitsPerPack, MeasurementUnit unit) {
        Vmpp vmpp = getVmpp(amp.getVmp(), issueUnitsPerPack, unit);
        Ampp ampp;
        String sql;
        Map m = new HashMap();
        m.put("v", vmpp);
        m.put("a", amp);
        sql = "select p from Ampp p where p.vmpp=:v and p.amp=:a";
        ampp = getAmppFacade().findFirstBySQL(sql, m);
        if (ampp == null) {
            ampp = new Ampp();
            ampp.setAmp(amp);
            ampp.setName(amp.getName() + " " + issueUnitsPerPack + amp.getMeasurementUnit() + unit.getName());
            ampp.setDblValue(issueUnitsPerPack);
            ampp.setMeasurementUnit(unit);
            ampp.setVmpp(vmpp);
            getAmppFacade().create(ampp);
        } else {
            ampp.setRetired(false);
            getAmppFacade().edit(ampp);
        }
        return ampp;
    }

    public Vmp getVmp(Vtm vtm, double strength, MeasurementUnit strengthUnit, PharmaceuticalItemCategory cat) {
        String sql;
        if (strength == 0 || strengthUnit == null || cat == null) {
            return null;
        }
        Map m = new HashMap();
        m.put("vtm", vtm);
        m.put("s", strength);
        m.put("su", strengthUnit);
        m.put("c", cat);
        sql = "select v from VtmsVmps v where v.vtm=:vtm and v.strength=:s and v.strengthUnit=:su and v.pharmaceuticalItemCategory=:c";
        VtmsVmps v = getVtmsVmpsFacade().findFirstBySQL(sql, m);
        Vmp vmp;
        if (v == null) {
            vmp = new Vmp();

            vmp.setName(vtm.getName() + " " + strength + " " + strengthUnit.getName() + " " + cat.getName());

            vmp.setCreatedAt(Calendar.getInstance().getTime());
            getVmpFacade().create(vmp);

            v = new VtmsVmps();
            v.setCreatedAt(Calendar.getInstance().getTime());
            v.setStrength(strength);
            v.setStrengthUnit(strengthUnit);
            v.setVtm(vtm);
            v.setVmp(vmp);
            v.setPharmaceuticalItemCategory(cat);
            getVtmsVmpsFacade().create(v);
        }
        v.getVmp().setRetired(false);
        return v.getVmp();
    }

    public Vtm getVtmByName(String name, boolean createNew) {
        if (name == null || name.trim().equals("")) {
            return null;
        }
        name = name.trim();
        Vtm vtm = null;
        Map m = new HashMap();
        m.put("n", name.toUpperCase());
        vtm = getVtmFacade().findFirstBySQL("SELECT c FROM Vtm c Where upper(c.name) =:n ", m);
        if (vtm == null && createNew) {
            vtm = new Vtm();
            vtm.setName(name);
            getVtmFacade().create(vtm);
        } else if (vtm != null) {
            vtm.setName(name);
            vtm.setRetired(false);
            getVtmFacade().edit(vtm);
        }
        return vtm;
    }

    public Vtm getVtmByName(String name) {
        return getVtmByName(name, true);
    }

    @EJB
    private VtmFacade VtmFacade;
    @EJB
    private MeasurementUnitFacade measurementUnitFacade;

    public MeasurementUnitFacade getMeasurementUnitFacade() {
        return measurementUnitFacade;
    }

    public void setMeasurementUnitFacade(MeasurementUnitFacade measurementUnitFacade) {
        this.measurementUnitFacade = measurementUnitFacade;
    }

    public VtmFacade getVtmFacade() {
        return VtmFacade;
    }

    public void setVtmFacade(VtmFacade VtmFacade) {
        this.VtmFacade = VtmFacade;
    }

//    public double getLastPurchaseRate(Item item) {
//        Map m = new HashMap();
//        String sql;
//        sql = "Select bi.pharmaceuticalBillItem.purchaseRate from BillItem bi where bi.retired=false and bi.bill.cancelled=false and bi.pharmaceuticalBillItem.itemBatch.item=:i and bi.bill.billType=:t order by bi.id desc";
//        m.put("i", item);
//        m.put("t", BillType.PharmacyGrnBill);
//        return getBillItemFacade().findDoubleByJpql(sql, m);
//    }
    public double getLastPurchaseRate(Item item, Department dept) {
        System.out.println("getting last purchase rate");
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }

        Map m = new HashMap();
        String sql;
        sql = "Select bi.pharmaceuticalBillItem.itemBatch from BillItem bi where "
                + " bi.retired=false and bi.bill.cancelled=false "
                + " and bi.bill.department=:d "
                + " and bi.pharmaceuticalBillItem.itemBatch.item=:i "
                + " and (bi.bill.billType=:t or bi.bill.billType=:t1) "
                + " order by bi.id desc";
        m.put("i", item);
        m.put("d", dept);
        m.put("t", BillType.PharmacyGrnBill);
        m.put("t1", BillType.PharmacyPurchaseBill);
        ItemBatch ii = getItemBatchFacade().findFirstBySQL(sql, m);
        // System.err.println("d = " + ii.getPurcahseRate());
        if (ii != null) {
            return ii.getPurcahseRate();
        } else {
            return 0.0;
        }

    }

    public double getLastPurchaseRate(Item item, Institution ins) {
        System.out.println("getting last purchase rate");
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }

        Map m = new HashMap();
        String sql;
        sql = "Select bi.pharmaceuticalBillItem.itemBatch from BillItem bi where "
                + " bi.retired=false and bi.bill.cancelled=false "
                + " and bi.bill.department.institution=:ins "
                + " and bi.pharmaceuticalBillItem.itemBatch.item=:i "
                + " and (bi.bill.billType=:t or bi.bill.billType=:t1) "
                + " order by bi.id desc";
        m.put("i", item);
        m.put("ins", ins);
        m.put("t", BillType.PharmacyGrnBill);
        m.put("t1", BillType.PharmacyPurchaseBill);
        ItemBatch ii = getItemBatchFacade().findFirstBySQL(sql, m);
        // System.err.println("d = " + ii.getPurcahseRate());
        if (ii != null) {
            return ii.getPurcahseRate();
        } else {
            return 0.0;
        }

    }

    public double getLastPurchaseRate(Item item, Department dept, boolean anyValueFromHirachi) {
        double d = getLastPurchaseRate(item, dept);
        if (d == 0) {
            d = getLastPurchaseRate(item, dept.getInstitution());
        }
        if (d == 0) {
            d = getLastPurchaseRate(item);
        }
        return d;
    }

    public double getLastPurchaseRate(Item item) {
        System.out.println("getting last purchase rate");
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }

        Map m = new HashMap();
        String sql;
        sql = "Select bi.pharmaceuticalBillItem.itemBatch from BillItem bi where "
                + " bi.retired=false and bi.bill.cancelled=false "
                + " and bi.pharmaceuticalBillItem.itemBatch.item=:i "
                + " and (bi.bill.billType=:t or bi.bill.billType=:t1) "
                + " order by bi.id desc";
        m.put("i", item);
        m.put("t", BillType.PharmacyGrnBill);
        m.put("t1", BillType.PharmacyPurchaseBill);
        ItemBatch ii = getItemBatchFacade().findFirstBySQL(sql, m);
        // System.err.println("d = " + ii.getPurcahseRate());
        if (ii != null) {
            return ii.getPurcahseRate();
        } else {
            return 0.0;
        }

    }

    public double getLastRetailRate(Item item, Institution ins) {
        System.out.println("getting last purchase rate");
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }
        Map m = new HashMap();
        String sql;
        sql = "Select bi.pharmaceuticalBillItem.itemBatch from BillItem bi where "
                + " bi.retired=false and bi.bill.cancelled=false and bi.bill.department.institution=:ins "
                + " and bi.pharmaceuticalBillItem.itemBatch.item=:i"
                + " and (bi.bill.billType=:t or bi.bill.billType=:t1) "
                + " order by bi.id desc";
        m.put("i", item);
        m.put("ins", ins);
        m.put("t", BillType.PharmacyGrnBill);
        m.put("t1", BillType.PharmacyPurchaseBill);
        ItemBatch ii = getItemBatchFacade().findFirstBySQL(sql, m);
        // System.err.println("d = " + ii.getPurcahseRate());
        if (ii != null) {
            return ii.getRetailsaleRate();
        } else {
            return 0.0;
        }

    }

    public double getLastRetailRate(Item item) {
        System.out.println("getting last purchase rate");
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }
        Map m = new HashMap();
        String sql;
        sql = "Select bi.pharmaceuticalBillItem.itemBatch from BillItem bi where "
                + " bi.retired=false and bi.bill.cancelled=false "
                + " and bi.pharmaceuticalBillItem.itemBatch.item=:i"
                + " and (bi.bill.billType=:t or bi.bill.billType=:t1) "
                + " order by bi.id desc";
        m.put("i", item);
        m.put("t", BillType.PharmacyGrnBill);
        m.put("t1", BillType.PharmacyPurchaseBill);
        ItemBatch ii = getItemBatchFacade().findFirstBySQL(sql, m);
        // System.err.println("d = " + ii.getPurcahseRate());
        if (ii != null) {
            return ii.getRetailsaleRate();
        } else {
            return 0.0;
        }

    }

    public double getLastRetailRate(Item item, Department dept) {
        System.out.println("getting last purchase rate");
        if (item instanceof Ampp) {
            item = ((Ampp) item).getAmp();
        }
        Map m = new HashMap();
        String sql;
        sql = "Select bi.pharmaceuticalBillItem.itemBatch from BillItem bi where "
                + " bi.retired=false and bi.bill.cancelled=false and bi.bill.department=:d "
                + " and bi.pharmaceuticalBillItem.itemBatch.item=:i"
                + " and (bi.bill.billType=:t or bi.bill.billType=:t1) "
                + " order by bi.id desc";
        m.put("i", item);
        m.put("d", dept);
        m.put("t", BillType.PharmacyGrnBill);
        m.put("t1", BillType.PharmacyPurchaseBill);
        ItemBatch ii = getItemBatchFacade().findFirstBySQL(sql, m);
        // System.err.println("d = " + ii.getPurcahseRate());
        if (ii != null) {
            return ii.getRetailsaleRate();
        } else {
            return 0.0;
        }

    }

    public double getLastRetailRate(Item item, Department dept, boolean anyValueFromHighrachy) {
        double d = getLastRetailRate(item, dept);
        if (d == 0) {
            getLastRetailRate(item, dept.getInstitution());
        }
        if (d == 0) {
            getLastRetailRate(item);
        }
        return d;
    }

    public StockHistoryFacade getStockHistoryFacade() {
        return stockHistoryFacade;
    }

    public void setStockHistoryFacade(StockHistoryFacade stockHistoryFacade) {
        this.stockHistoryFacade = stockHistoryFacade;
    }

}
