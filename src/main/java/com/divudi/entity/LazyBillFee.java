/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyBillFee extends LazyDataModel<BillFee> {

    private final List<BillFee> datasource;

    public LazyBillFee(List<BillFee> datasource) {
        this.datasource = datasource;
    }

    @Override
    public BillFee getRowData(String rowKey) {
        for (BillFee billFee : datasource) {
            if (billFee.getId().equals(rowKey)) {
                return billFee;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(BillFee billFee) {
        return billFee.getId();
    }

  
}
