/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.divudi.data;

import java.util.Date;

/**
 *
 * @author buddhika
 */
public class DailySummeryRow {
    Date summeryDate;
    double freeAmounts;
    double profit;
    double discounts;

    //com.divudi.data.DailySummeryRow(Date summeryDate, double freeAmounts, double profit, double discounts)
    
    public DailySummeryRow(Date summeryDate, double freeAmounts, double profit, double discounts) {
        this.summeryDate = summeryDate;
        this.freeAmounts = freeAmounts;
        this.profit = profit;
        this.discounts = discounts;
    }
    
    
    
    public DailySummeryRow() {
    }

    public Date getSummeryDate() {
        return summeryDate;
    }

    public void setSummeryDate(Date summeryDate) {
        this.summeryDate = summeryDate;
    }

    public double getFreeAmounts() {
        return freeAmounts;
    }

    public void setFreeAmounts(double freeAmounts) {
        this.freeAmounts = freeAmounts;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public double getDiscounts() {
        return discounts;
    }

    public void setDiscounts(double discounts) {
        this.discounts = discounts;
    }

    
    
}
