/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.facade;

import com.divudi.entity.Bill;
import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author safrin
 */
@Stateless
public class PharmaceuticalBillItemFacade extends AbstractFacade<PharmaceuticalBillItem> {
    @PersistenceContext(unitName = "pu")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PharmaceuticalBillItemFacade() {
        super(PharmaceuticalBillItem.class);
    }
    
     public List<PharmaceuticalBillItem> getPharmaceuticalBillItems(Bill bill) {
        String sql = "Select p from PharmaceuticalBillItem p where p.billItem.bill=:b ";
        HashMap hm = new HashMap();
        hm.put("b", bill);
        List<PharmaceuticalBillItem> btm= findBySQL(sql, hm);
        ////System.err.println("Getting Bills Item"+btm.size());
        return btm;
    }

    
}
