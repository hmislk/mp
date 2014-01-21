/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.divudi.ejb;

import com.divudi.entity.BillItem;
import com.divudi.entity.Item;
import com.divudi.facade.BillItemFacade;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author ruhunu
 */
@Stateless
public class ItemMovementReportEjb {
    @EJB
    BillItemFacade billItemFacade;
    
    
    public List<BillItem> allBillItems(Item item, Date fromDate, Date toDate){
        String sql;
        Map m = new HashMap();
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("i", item);
        sql = "select bi from BillItem bi where bi.bill.billTime between :fd and :td and b.item=:i";
        return getBillItemFacade().findBySQL(sql, m);
        
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }
    
    
    
}
