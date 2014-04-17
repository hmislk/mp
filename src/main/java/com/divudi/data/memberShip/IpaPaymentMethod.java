/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data.memberShip;

import com.divudi.data.PaymentMethod;
import com.divudi.entity.InwardPriceAdjustment;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author safrin
 */
public class IpaPaymentMethod {

    PaymentMethod paymentMethod;
    List<InwardPriceAdjustment> inwardPriceAdjustments;

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<InwardPriceAdjustment> getInwardPriceAdjustments() {
        if (inwardPriceAdjustments == null) {
            inwardPriceAdjustments = new ArrayList<>();
        }
        return inwardPriceAdjustments;
    }

    public void setInwardPriceAdjustments(List<InwardPriceAdjustment> inwardPriceAdjustments) {
        this.inwardPriceAdjustments = inwardPriceAdjustments;
    }

}
