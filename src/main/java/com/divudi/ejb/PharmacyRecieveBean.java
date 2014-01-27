/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.data.PaymentMethod;
import com.divudi.entity.Bill;
import com.divudi.entity.BillItem;
import com.divudi.entity.Category;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.ItemBatch;
import com.divudi.entity.pharmacy.ItemsDistributors;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.PharmaceuticalItem;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.Vmpp;
import com.divudi.facade.AmpFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.CategoryFacade;
import com.divudi.facade.ItemBatchFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.ItemsDistributorsFacade;
import com.divudi.facade.PharmaceuticalBillItemFacade;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TemporalType;

/**
 *
 * @author safrin
 */
@Stateless
public class PharmacyRecieveBean {

    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private AmpFacade ampFacade;
    @EJB
    private ItemFacade itemFacade;
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
    private BillNumberBean billNumberBean;

//    public void editBill(Bill bill, Bill ref, SessionController sc) {
//
//       
//    }
    public List<Item> getItemsForDealor(Institution i) {
        String temSql;
        List<Item> tmp;
        if (i != null) {
            temSql = "SELECT i.item FROM ItemsDistributors i where i.retired=false and i.institution.id = " + i.getId();
            tmp = getItemFacade().findBySQL(temSql);
        } else {
            tmp = null;
        }

        if (tmp == null) {
            tmp = new ArrayList<>();
        }
        return tmp;
    }

    public boolean checkItem(Institution ins, Item i) {
        String sql = "Select i from ItemsDistributors i where i.retired=false and i.institution.id= " + ins.getId() + " and i.item.id=" + i.getId();
        ItemsDistributors tmp = getItemsDistributorsFacade().findFirstBySQL(sql);
        if (tmp != null) {
            return true;
        }

        return false;
    }

    public Double calRetailRate(PharmaceuticalBillItem ph) {

        PharmaceuticalItem i = (PharmaceuticalItem) ph.getBillItem().getItem();
        double margin = 0.0;
        String sql;
        Category cat;

        if (i instanceof Amp) {
            sql = "Select p.category from Vmp p where p.retired=false and p.id in (Select a.vmp.id from Amp a where a.retired=false and a.id=" + i.getId() + ")";
        } else if (i instanceof Ampp) {
            sql = "Select p.category from Vmp p where p.retired=false and p.id in (Select a.amp.vmp.id from Ampp a where a.retired=false and a.id=" + i.getId() + ")";

        } else {
            return 0.0;
        }

        cat = getCategoryFacade().findFirstBySQL(sql);

        if (cat != null) {
            margin = cat.getSaleMargin();
        }

        Double retailPrice = ph.getPurchaseRate() + (ph.getPurchaseRate() * (margin / 100));

        return retailPrice;
    }
  

    public boolean checkPurchasePrice(PharmaceuticalBillItem i) {
        double oldPrice, newPrice = 0.0;

        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.id=" + i.getBillItem().getReferanceBillItem().getId();
        PharmaceuticalBillItem tmp = getPharmaceuticalBillItemFacade().findFirstBySQL(sql);

        oldPrice = tmp.getPurchaseRate();
        newPrice = i.getPurchaseRate();

        double max = oldPrice + (oldPrice * (getPharmacyBean().getMaximumPurchasePriceChange() / 100.0));
        System.err.println("Old Pur Price : " + oldPrice);
        System.err.println("New Pur Price : " + newPrice);
        System.err.println("MAX Price : " + max);

        if (max < newPrice) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkRetailPrice(PharmaceuticalBillItem i) {

        double max = i.getPurchaseRate() + (i.getPurchaseRate() * (getPharmacyBean().getMaximumRetailPriceChange() / 100));

        System.err.println("Purchase Price : " + i.getPurchaseRate());
        System.err.println("Retail Price : " + i.getRetailRate());
        System.err.println("MAX Price : " + max);

        if (max < i.getRetailRate()) {
            return true;
        } else {
            return false;
        }
    }

    public ItemBatch saveItemBatch(BillItem tmp) {
        //   System.err.println("Save Item Batch");
        ItemBatch itemBatch = new ItemBatch();
        Item itm = tmp.getItem();

        if (itm instanceof Ampp) {
            itm = ((Ampp) itm).getAmp();
        }

        double purchase = tmp.getPharmaceuticalBillItem().getPurchaseRateInUnit();
        double retail = tmp.getPharmaceuticalBillItem().getRetailRateInUnit();

        System.err.println("Puchase :  " + purchase);
        System.err.println("Puchase :  " + retail);

        itemBatch.setDateOfExpire(tmp.getPharmaceuticalBillItem().getDoe());
        itemBatch.setBatchNo(tmp.getPharmaceuticalBillItem().getStringValue());
        itemBatch.setPurcahseRate(purchase);
        itemBatch.setRetailsaleRate(retail);

        HashMap hash = new HashMap();
        String sql;

        itemBatch.setItem(itm);
        sql = "Select p from ItemBatch p where  p.item.id=" + itemBatch.getItem().getId()
                + " and p.dateOfExpire= :doe and p.retailsaleRate=" + itemBatch.getRetailsaleRate() + " and p.purcahseRate=" + itemBatch.getPurcahseRate();

        hash.put("doe", itemBatch.getDateOfExpire());

        List<ItemBatch> i = getItemBatchFacade().findBySQL(sql, hash, TemporalType.TIMESTAMP);
        System.err.println("Size " + i.size());
        if (i.size() > 0) {
//            System.err.println("Edit");
//            i.get(0).setBatchNo(i.get(0).getBatchNo());
//            i.get(0).setDateOfExpire(i.get(0).getDateOfExpire());
            return i.get(0);
        } else {
            System.err.println("Create");
            getItemBatchFacade().create(itemBatch);
        }

        System.err.println("ItemBatc Id " + itemBatch.getId());
        return itemBatch;
    }

    public List<Item> findItem(Amp tmp, List<Item> items) {

        String sql;

        sql = "SELECT i from Amp i where i.retired=false and "
                + "i.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getVmp().getId() + ")";
        items = getItemFacade().findBySQL(sql);

        sql = "SELECT i from Ampp i where i.retired=false and "
                + "i.amp.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getVmp().getId() + ")";
        List<Item> amppList = getItemFacade().findBySQL(sql);
        for (Item i : amppList) {
            items.add(i);
        }

        return items;
    }

    public List<Item> findPack(Amp amp) {

        String sql;
        HashMap hm = new HashMap();
        sql = "SELECT i from Ampp i where i.retired=false and "
                + " i.amp=:am";

        hm.put("am", amp);

        return getItemFacade().findBySQL(sql, hm);

    }

   
    public List<Item> findItem(Ampp tmp, List<Item> items) {

        String sql;
        sql = "SELECT i from Amp i where i.retired=false and "
                + "i.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getAmp().getVmp().getId() + ")";
        items = getItemFacade().findBySQL(sql);

        sql = "SELECT i from Ampp i where i.retired=false and "
                + "i.amp.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getAmp().getVmp().getId() + ")";
        List<Item> amppList = getItemFacade().findBySQL(sql);
        for (Item i : amppList) {
            items.add(i);
        }
        return items;
    }

    public List<Item> findItem(Vmp tmp, List<Item> items) {

        String sql;
        sql = "SELECT i from Amp i where i.retired=false and "
                + "i.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getId() + ")";
        items = getItemFacade().findBySQL(sql);

        sql = "SELECT i from Ampp i where i.retired=false and "
                + "i.amp.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getId() + ")";
        List<Item> amppList = getItemFacade().findBySQL(sql);
        for (Item i : amppList) {
            items.add(i);
        }
        return items;
    }

    public List<Item> findItem(Vmpp tmp, List<Item> items) {

        String sql;
        sql = "SELECT i from Amp i where i.retired=false and "
                + "i.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getVmp().getId() + ")";
        items = getItemFacade().findBySQL(sql);

        sql = "SELECT i from Ampp i where i.retired=false and "
                + "i.amp.vmp.id in (select vv.vmp.id from VtmsVmps vv where vv.vmp.id=" + tmp.getVmp().getId() + ")";
        List<Item> amppList = getItemFacade().findBySQL(sql);
        for (Item i : amppList) {
            items.add(i);
        }
        return items;
    }



    public String errorCheck(Bill b) {
        String msg = "";

        if (b.getInvoiceNumber() == null || "".equals(b.getInvoiceNumber().trim())) {
            msg = "Please Fill invoice number";
        }

        if (b.getPaymentMethod() != null && b.getPaymentMethod() == PaymentMethod.Cheque) {
            if (b.getBank().getId() == null || b.getChequeRefNo() == null) {
                msg = "Please select Cheque Number and Bank";
            }
        }

        if (b.getPaymentMethod() != null && b.getPaymentMethod() == PaymentMethod.Slip) {
            if (b.getBank().getId() == null || b.getComments() == null) {
                msg = "Please Fill Memo and Bank";
            }
        }

        if (b.getBillItems().isEmpty()) {
            msg = "There is no Item to receive";
        }

        if (checkItemBatch(b.getBillItems())) {
            msg = "Please Fill Batch deatail and Sale Price to All Item";
        }

        return msg;
    }

    public void calSaleFreeValue(Bill b,List<BillItem> billItems) {
        double sale = 0.0;
        double free = 0.0;

        for (BillItem i : billItems) {
            sale += i.getPharmaceuticalBillItem().getQty() * i.getPharmaceuticalBillItem().getPurchaseRate();
            free += i.getPharmaceuticalBillItem().getFreeQty() * i.getPharmaceuticalBillItem().getPurchaseRate();
        }

        b.setSaleValue(sale);
        b.setFreeValue(free);
    }

    public boolean checkItemBatch(List<BillItem> list) {

        for (BillItem i : list) {
            if (i.getPharmaceuticalBillItem().getQty() != 0.0) {
                if (i.getPharmaceuticalBillItem().getDoe() == null || i.getPharmaceuticalBillItem().getStringValue().trim().equals("")) {
                    return true;
                }
                if (i.getPharmaceuticalBillItem().getPurchaseRate() > i.getPharmaceuticalBillItem().getRetailRate()) {
                    return true;
                }

            }

        }
        return false;
    }

//    public void preCalForAddToStock(PharmacyItemData ph, ItemBatch itb, Department d) {
//
//        Item item = ph.getPharmaceuticalBillItem().getBillItem().getItem();
//        Double qty = 0.0;
//
//        if (item instanceof Amp) {
//            qty = ph.getPharmaceuticalBillItem().getQty() + ph.getPharmaceuticalBillItem().getFreeQty();
//
//        } else {
//            qty = (ph.getPharmaceuticalBillItem().getQty() + ph.getPharmaceuticalBillItem().getFreeQty()) * item.getDblValue();
//            //      System.out.println("sssssss " + qty);
//        }
//
//        if (itb.getId() != null) {
//           
//        }
//    }
    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public AmpFacade getAmpFacade() {
        return ampFacade;
    }

    public void setAmpFacade(AmpFacade ampFacade) {
        this.ampFacade = ampFacade;
    }

    public ItemFacade getItemFacade() {
        return itemFacade;
    }

    public void setItemFacade(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
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

    public BillNumberBean getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberBean billNumberBean) {
        this.billNumberBean = billNumberBean;
    }
}
