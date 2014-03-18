/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data.dataStructure;

import com.divudi.data.inward.InwardChargeType;
import com.divudi.entity.Item;
import java.util.List;

/**
 *
 * @author safrin
 */
public class ChargeItemTotal {

    private InwardChargeType inwardChargeType;
    private List<Item> list;
    private double total = 0.0;
    private double discount = 0;
    private double netTotal = 0;
    private double adjustedTotal = 0.0;

    public List<Item> getList() {
        return list;
    }

    public void setList(List<Item> list) {
        this.list = list;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getAdjustedTotal() {
        return adjustedTotal;
    }

    public void setAdjustedTotal(Double adjustedTotal) {
        this.adjustedTotal = adjustedTotal;
    }

    public InwardChargeType getInwardChargeType() {
        return inwardChargeType;
    }

    public void setInwardChargeType(InwardChargeType inwardChargeType) {
        this.inwardChargeType = inwardChargeType;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

}
