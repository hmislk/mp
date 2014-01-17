/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data;

/**
 *
 * @author safrin
 */
public enum BillNumberSuffix {

    RF,//Refund
    CAN,//Cancel
    //Professional Payment
    PROPAY,//Professional Payment
    PROCAN,//Professional Cancel
    CHNPROPAY,//Channel Pro Pay
    //Pharmacy
    GRN,//Pharmacy GRN
    GRNCAN,//GRN Cancel
    GRNRET,//GRN Return
    GRNRETCAN,//GRN Return Cancel
    PO,//Purchase Order
    POCAN,//PO Cancel
    POR,//Purchase Order Request
    PHCAN,//Pharmacy Cancel    
    PHPUR,//Purchase  
    PURRET,//Purchase Return
    PURCAN,//Purchase Cancel
    PHTI,//Transfer Issue
    PHTICAN,//Trnsfer Cancel
    PHTR,//Transfer Recieve
    PHTRQ,//Transfer Request
    //PHPRE,//Pre Bill
    PHSAL,
    SALCAN,//Pharmacy Sale Cancel
    PHRET,
    RETCAN,//Sale Return Cancel
    //PHPRERET,//Pre BIll Return
    // PHSAL,//Sale Bill
    //  PRERET,//Pre Return
    // SALRET,//Sal Return
    //Inward
    INWPAY,//Payment Bill
    INWPRO,//Professional
    INWSER,//Service
    INWREF,//Refund
    INWCAN,//Cancell
    //Agent
    AGNPAY,//Payment 
    AGNCAN,//Payment Cancel
    //Credit Company
    CRDPAY,//Payment
    CRDCAN,//Cancel
    //Petty Cash
    PTYPAY,//Payment
    PTYCAN,//Cancell
    NONE,//NO Prefix
    PACK,//Package
}
