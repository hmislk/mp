/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity;


import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.UserStock;
import com.divudi.entity.pharmacy.Vmpp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;

/**
 *
 * @author buddhika
 */
@Entity
public class BillItem implements Serializable {

    @OneToOne(mappedBy = "billItem", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    BillSession billSession;

    @OneToOne(mappedBy = "billItem", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    PharmaceuticalBillItem pharmaceuticalBillItem;
    static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    Double qty = 0.0;
    Double qtyPacks = 0.0;
    @Lob
    String descreption;
    double remainingQty;
    double Rate;
    double ratePacks;
    double discountRate;
    double netRate;
    double netRatePack;
    double expectedRate;
    double grossValue;
    double discount;
    double netValue;
    private double adjustedValue;
//    private double dblValue;
    @ManyToOne
    Item item;
    @ManyToOne
    Item itemAmpp;
    @ManyToOne
    Bill bill;
    Boolean refunded;
    //Created Properties
    @ManyToOne
    WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date createdAt;
    //Edited Properties
    @ManyToOne
    private WebUser editor;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date editedAt;
    //Retairing properties
    boolean retired;
    @ManyToOne
    WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date retiredAt;
    String retireComments;
    String insId;
    String deptId;
    String catId;
    String sessionId;
    String itemId;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date fromTime;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date toTime;
    @OneToOne
    BillItem referanceBillItem;
    @OneToOne
    BillFee paidForBillFee;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    Date sessionDate;
    @ManyToOne
    Bill referenceBill;
    
    String agentRefNo;

//    @Transient
    int searialNo;
    @Transient
    double totalGrnQty;
    @Transient
    private List<Item> tmpSuggession;
    @Transient
    private double tmpQty;
    @Transient
    private UserStock transUserStock;
    @Transient
    private BillItem transBillItem;
    @OneToMany(mappedBy = "billItem")
    private List<BillFee> billFees = new ArrayList<>();
    @OneToMany(mappedBy = "referenceBillItem", fetch = FetchType.LAZY)
    private List<BillFee> proFees = new ArrayList<>();

    public void copy(BillItem billItem) {
        item = billItem.getItem();
        sessionDate = billItem.getSessionDate();
       
        agentRefNo = billItem.getAgentRefNo();
        item = billItem.getItem();
        itemAmpp = billItem.getItemAmpp();
        qty = billItem.getQty();
        qtyPacks = billItem.getQtyPacks();
        grossValue = billItem.getGrossValue();
        netValue = billItem.getNetValue();
        discount = billItem.getDiscount();
        adjustedValue = billItem.getAdjustedValue();
        discountRate = billItem.getDiscountRate();
        Rate = billItem.getRate();
        ratePacks = billItem.getRatePacks();
        expectedRate = billItem.getExpectedRate();
        netRate = billItem.getNetRate();
        searialNo = billItem.getSearialNo();
        tmpQty = billItem.tmpQty;
        referenceBill = billItem.getReferenceBill();
        retireComments=billItem.getRetireComments();
        //  referanceBillItem=billItem.getReferanceBillItem();
    }

    public Item getItemAmpp() {
        return itemAmpp;
    }

    public void setItemAmpp(Item itemAmpp) {
        this.itemAmpp = itemAmpp;
    }
    
    

    public BillItem() {
    }

    public Double getQtyPacks() {
        return qtyPacks;
    }

    public void setQtyPacks(Double qtyPacks) {
        this.qtyPacks = qtyPacks;
    }

    
    
    public void invertValue(BillItem billItem) {
        if (billItem.getQty() != null) {
            qty = 0 - billItem.getQty();
        }
        Rate = 0 - billItem.getRate();
        discount = 0 - billItem.getDiscount();
        netRate = 0 - billItem.getNetRate();
        expectedRate = 0 - billItem.getExpectedRate();
        grossValue = 0 - billItem.getGrossValue();
        netValue = 0 - billItem.getNetValue();
        adjustedValue = 0 - billItem.getAdjustedValue();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof BillItem)) {
            return false;
        }
        BillItem other = (BillItem) object;
        if (this.id==null && other.id==null) {
            if (this.searialNo==other.searialNo) {
                return true;
            }else{
                return false;
            }
        }
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.BillItem[ id=" + id + " ]";
    }

    public PharmaceuticalBillItem getPharmaceuticalBillItem() {
        if (pharmaceuticalBillItem == null) {
            pharmaceuticalBillItem = new PharmaceuticalBillItem();
            pharmaceuticalBillItem.setBillItem(this);
        }
        return pharmaceuticalBillItem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getRate() {
        return Rate;
    }

    public double getExpectedRate() {
        return expectedRate;
    }

    public void setExpectedRate(double expectedRate) {
        this.expectedRate = expectedRate;
    }

    
    
    public void setRate(double Rate) {
        this.Rate = Rate;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getNetRate() {
        return netRate;
    }

    public void setNetRate(double netRate) {
        this.netRate = netRate;
    }

    public double getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(double grossValue) {
        this.grossValue = grossValue;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getNetValue() {
        return netValue;
    }

    public void setNetValue(double netValue) {
        this.netValue = netValue;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public Boolean isRefunded() {
        return refunded;
    }

    public Boolean getRefunded() {
        return refunded;
    }

    public void setRefunded(Boolean refunded) {
        this.refunded = refunded;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public String getInsId() {
        return insId;
    }

    public void setInsId(String insId) {
        this.insId = insId;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    public BillItem getReferanceBillItem() {
        return referanceBillItem;
    }

    public void setReferanceBillItem(BillItem referanceBillItem) {
        this.referanceBillItem = referanceBillItem;
    }

    public BillFee getPaidForBillFee() {
        return paidForBillFee;
    }

    public void setPaidForBillFee(BillFee paidForBillFee) {
        this.paidForBillFee = paidForBillFee;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    public int getSearialNo() {
        return searialNo;
    }

    public void setSearialNo(int searialNo) {
        this.searialNo = searialNo;
    }

    public BillSession getBillSession() {
        return billSession;
    }

    public void setBillSession(BillSession billSession) {
        this.billSession = billSession;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double Qty) {
        this.qty = Qty;

    }

    public double getRemainingQty() {
        return remainingQty;
    }

    public void setRemainingQty(double remainingQty) {
        this.remainingQty = remainingQty;
    }

  

    public Bill getReferenceBill() {
        return referenceBill;
    }

    public void setReferenceBill(Bill referenceBill) {
        this.referenceBill = referenceBill;
    }


    public String getAgentRefNo() {
        return agentRefNo;
    }

    public void setAgentRefNo(String agentRefNo) {
        this.agentRefNo = agentRefNo;
    }

    public double getTotalGrnQty() {
        return totalGrnQty;
    }

    public void setTotalGrnQty(double totalGrnQty) {
        this.totalGrnQty = totalGrnQty;
    }

    public void setPharmaceuticalBillItem(PharmaceuticalBillItem pharmaceuticalBillItem) {
        this.pharmaceuticalBillItem = pharmaceuticalBillItem;
    }

//    public double getDblValue() {
//        return dblValue;
//    }
//
//    public void setDblValue(double dblValue) {
//        this.dblValue = dblValue;
//    }
    public List<Item> getTmpSuggession() {
        return tmpSuggession;
    }

    public void setTmpSuggession(List<Item> tmpSuggession) {
        this.tmpSuggession = tmpSuggession;
    }

    public double getTmpQty() {
        if (getItem() instanceof Ampp || getItem() instanceof Vmpp) {
            return tmpQty / getItem().getDblValue();
        } else {
            return tmpQty;
        }
    }

    public void setTmpQty(double tmpQty) {
        qty = tmpQty;
        if (getItem() instanceof Ampp || getItem() instanceof Vmpp) {
            this.tmpQty = tmpQty * getItem().getDblValue();
        } else {
            this.tmpQty = tmpQty;
        }

        if (getPharmaceuticalBillItem() != null) {
            getPharmaceuticalBillItem().setQty((double) this.tmpQty);
        }
    }

    public UserStock getTransUserStock() {
        return transUserStock;
    }

    public void setTransUserStock(UserStock transUserStock) {
        this.transUserStock = transUserStock;
    }

    public List<BillFee> getBillFees() {
        return billFees;
    }

    public void setBillFees(List<BillFee> billFees) {
        this.billFees = billFees;
    }

    public double getAdjustedValue() {
        return adjustedValue;
    }

    public void setAdjustedValue(double adjustedValue) {
        this.adjustedValue = adjustedValue;
    }

    public BillItem getTransBillItem() {
        return transBillItem;
    }

    public void setTransBillItem(BillItem transBillItem) {
        this.transBillItem = transBillItem;
    }

    public WebUser getEditor() {
        return editor;
    }

    public void setEditor(WebUser editor) {
        this.editor = editor;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    public List<BillFee> getProFees() {
        return proFees;
    }

    public void setProFees(List<BillFee> proFees) {
        this.proFees = proFees;
    }

    public String getDescreption() {
        return descreption;
    }

    public void setDescreption(String descreption) {
        this.descreption = descreption;
    }

    public double getRatePacks() {
        return ratePacks;
    }

    public void setRatePacks(double ratePacks) {
        this.ratePacks = ratePacks;
    }

    public double getNetRatePack() {
        return netRatePack;
    }

    public void setNetRatePack(double netRatePack) {
        this.netRatePack = netRatePack;
    }

    
    
    
    
}
