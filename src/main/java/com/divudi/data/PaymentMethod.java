/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data;

/**
 *
 * @author buddhika
 */
public enum PaymentMethod {

    Cash,
    Credit,
    Agent,
    Card,
    Cheque,
    Slip;

    public String getLabel() {
        switch (this) {
            case Cash:
                return "Cash";
            case Credit:
                return "Credit";
            case Agent:
                return "Agent";
            case Cheque:
                return "Cheque";
            case Slip:
                return "Slip";
        }
        return "";
    }

}
