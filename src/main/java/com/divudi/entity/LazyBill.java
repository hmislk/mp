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

public class LazyBill extends LazyDataModel<Bill> {

    private final List<Bill> datasource;

    public LazyBill(List<Bill> datasource) {
        this.datasource = datasource;
    }

    @Override
    public Bill getRowData(String rowKey) {
        for (Bill bill : datasource) {
            if (bill.getId().equals(rowKey)) {
                ////System.err.println("Gett Bills"+bill);
                return bill;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Bill bill) {
        ////System.err.println("GEt Row Key"+bill);
        return bill.getId();
    }

   
}
