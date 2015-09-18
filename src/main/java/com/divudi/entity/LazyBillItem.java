/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity;

import java.util.List;
import org.primefaces.model.LazyDataModel;

public class LazyBillItem extends LazyDataModel<BillItem> {

    private final List<BillItem> datasource;

    public LazyBillItem(List<BillItem> datasource) {
        this.datasource = datasource;
    }

    @Override
    public BillItem getRowData(String rowKey) {
        for (BillItem billItem : datasource) {
            if (billItem.getId().equals(rowKey)) {
                return billItem;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(BillItem billItem) {
        return billItem.getId();
    }

}
