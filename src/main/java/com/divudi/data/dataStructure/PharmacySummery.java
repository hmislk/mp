/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.divudi.data.dataStructure;

import com.divudi.data.table.String1Value1;
import java.util.List;

/**
 *
 * @author safrin
 */
public class PharmacySummery {
    private List<String1Value1> bills;
    private double summeryTotal;

    public List<String1Value1> getBills() {
        return bills;
    }

    public void setBills(List<String1Value1> bills) {
        this.bills = bills;
    }

    public double getSummeryTotal() {
        return summeryTotal;
    }

    public void setSummeryTotal(double summeryTotal) {
        this.summeryTotal = summeryTotal;
    }
    
}
