/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author buddhika
 */
public class DailySummeryRow {

    Date summeryDate;
    Double freeAmounts;
    Double profit;
    Double discounts;
    Double grossTotal;
    
    

    //com.divudi.data.DailySummeryRow(Date summeryDate, Double freeAmounts, Double profit, Double discounts)
    public DailySummeryRow(Date summeryDate, Double freeAmounts, Double profit, Double discounts, Double grossTotal) {
        this.summeryDate = summeryDate;
        this.freeAmounts = freeAmounts;
        this.profit = profit;
        this.discounts = discounts;
        this.grossTotal = grossTotal;
    }

    public DailySummeryRow(Object summeryDateOfMonth, Double freeAmounts, Double profit, Double discounts, Double grossTotal) {
        try {
            this.summeryDate = (Date) summeryDateOfMonth;
        } catch (Exception e) {
        //    System.out.println("e = " + e);
        }
        this.freeAmounts = freeAmounts;
        this.profit = profit;
        this.discounts = discounts;
        this.grossTotal = grossTotal;
    }

    public DailySummeryRow() {
    }

    public Double getGrossTotal() {
        return grossTotal;
    }

    public void setGrossTotal(Double grossTotal) {
        this.grossTotal = grossTotal;
    }

    
    
    public Date getSummeryDate() {
        return summeryDate;
    }

    public void setSummeryDate(Date summeryDate) {
        this.summeryDate = summeryDate;
    }

    public Double getFreeAmounts() {
        return freeAmounts;
    }

    public void setFreeAmounts(Double freeAmounts) {
        this.freeAmounts = freeAmounts;
    }

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    public Double getDiscounts() {
        return discounts;
    }

    public void setDiscounts(Double discounts) {
        this.discounts = discounts;
    }

}
