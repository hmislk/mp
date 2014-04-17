/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.ejb;

import com.divudi.data.PaymentMethod;
import com.divudi.data.inward.InwardChargeType;
import com.divudi.entity.BillItem;
import com.divudi.entity.Category;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.PriceMatrix;
import com.divudi.entity.WebUser;
import com.divudi.entity.inward.InwardPriceAdjustment;
import com.divudi.entity.memberShip.MembershipScheme;
import com.divudi.facade.PriceMatrixFacade;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author safrin
 */
@Stateless
public class PriceMatrixBean {

    @EJB
    PriceMatrixFacade priceMatrixFacade;

    public InwardPriceAdjustment getInwardPriceAdjustment(BillItem billItem) {

        String sql = "select a from InwardPriceAdjustment a "
                + " where a.retired=false "
                + " and a.category=:cat"
                + " and a.institution=:ins"
                + " and a.department=:dep"
                + " and (a.fromPrice<:tot and a.toPrice >:tot)";

        HashMap hm = new HashMap();
        hm.put("cat", billItem.getItem().getCategory());
        hm.put("ins", billItem.getItem().getInstitution());
        hm.put("dep", billItem.getItem().getDepartment());
        hm.put("tot", billItem.getItem().getTotal());

        return (InwardPriceAdjustment) getPriceMatrixFacade().findFirstBySQL(sql, hm);

    }

    public InwardPriceAdjustment getInwardPriceAdjustment(Department department, double dbl, Category category) {
        String sql = "select a from InwardPriceAdjustment a "
                + " where a.retired=false"
                + " and a.category=:cat "
                + " and  a.department=:dep"
                + " and (a.fromPrice< :frPrice and a.toPrice >:tPrice)";
        HashMap hm = new HashMap();
        hm.put("dep", department);
        hm.put("frPrice", dbl);
        hm.put("tPrice", dbl);
        hm.put("cat", category);

        return (InwardPriceAdjustment) getPriceMatrixFacade().findFirstBySQL(sql, hm);
    }

    public PriceMatrix getMemberDisCount(PaymentMethod paymentMethod, MembershipScheme membershipScheme, Institution ins, InwardChargeType inwardChargeType) {
        String sql;
        HashMap hm = new HashMap();

        if (membershipScheme != null) {
            if (ins != null) {
                sql = "Select i from InwardMemberShipDiscount i"
                        + "  where i.retired=false "
                        + " and i.membershipScheme=:m "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw"
                        + " and i.institution=:ins ";
                hm.put("m", membershipScheme);
                hm.put("ins", ins);
            } else {
                sql = "Select i from InwardMemberShipDiscount i "
                        + " where i.retired=false "
                        + " and  i.membershipScheme=:m "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw "
                        + " and i.institution is null ";
                hm.put("m", membershipScheme);
            }
        } else {
            if (ins != null) {
                sql = "Select i from InwardMemberShipDiscount i "
                        + " where i.retired=false "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw "
                        + " and i.membershipScheme is null"
                        + " and i.institution=:ins ";
                hm.put("ins", ins);
            } else {
                sql = "Select i from InwardMemberShipDiscount i "
                        + " where i.retired=false "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw "
                        + " and i.membershipScheme is null"
                        + " and i.institution is null ";
            }
        }

        hm.put("p", paymentMethod);
        hm.put("inw", inwardChargeType);

        return getPriceMatrixFacade().findFirstBySQL(sql, hm);
    }

    public List<PriceMatrix> getInwardMemberShipDiscounts(PaymentMethod paymentMethod) {
        String sql = "select ipa from InwardMemberShipDiscount ipa "
                + " where ipa.retired=false"
                + " and ipa.paymentMethod=:pm"
                + " and ipa.membershipScheme is null"
                + " and ipa.institution is null"
                + " order by ipa.paymentMethod ";

        HashMap hm = new HashMap();
        hm.put("pm", paymentMethod);

        return getPriceMatrixFacade().findBySQL(sql, hm);
    }

    public List<PriceMatrix> getInwardMemberShipDiscounts(Institution ins, PaymentMethod pay) {
        String sql = "select ipa from InwardMemberShipDiscount ipa "
                + " where ipa.retired=false"
                + " and ipa.paymentMethod=:pm"
                + " and ipa.institution=:ins"
                + " and ipa.membershipScheme is null";

        HashMap hm = new HashMap();
        hm.put("pm", pay);
        hm.put("ins", ins);

        return getPriceMatrixFacade().findBySQL(sql, hm);
    }

    public List<PriceMatrix> getInwardMemberShipDiscounts(MembershipScheme mem, PaymentMethod pay) {
        String sql = "select ipa from InwardMemberShipDiscount ipa "
                + " where ipa.retired=false"
                + " and ipa.paymentMethod=:pm"
                + " and ipa.membershipScheme=:mem"
                + " and ipa.institution is null";

        HashMap hm = new HashMap();
        hm.put("pm", pay);
        hm.put("mem", mem);

        return getPriceMatrixFacade().findBySQL(sql, hm);
    }

    public List<PriceMatrix> getInwardMemberShipDiscounts(Institution ins, MembershipScheme mem, PaymentMethod pay) {
        String sql = "select ipa from InwardMemberShipDiscount ipa "
                + " where ipa.retired=false"
                + " and ipa.paymentMethod=:pm"
                + " and ipa.membershipScheme=:mem"
                + " and ipa.institution=:ins";

        HashMap hm = new HashMap();
        hm.put("pm", pay);
        hm.put("mem", mem);
        hm.put("ins", ins);

        return getPriceMatrixFacade().findBySQL(sql, hm);
    }

    public PriceMatrix getInwardMemberShipDiscount(MembershipScheme membershipScheme, Institution institution, PaymentMethod paymentMethod, InwardChargeType inwardChargeType, WebUser webUser) {
        PriceMatrix object = getMemberDisCount(paymentMethod, membershipScheme, institution, inwardChargeType);

        if (object == null) {
            object = new PriceMatrix();
            object.setCreatedAt(new Date());
            object.setCreater(webUser);
            object.setInwardChargeType(inwardChargeType);
            object.setMembershipScheme(membershipScheme);
            object.setPaymentMethod(paymentMethod);
            object.setInstitution(institution);
            getPriceMatrixFacade().create(object);
        }

        return object;

    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public PriceMatrixFacade getPriceMatrixFacade() {
        return priceMatrixFacade;
    }

    public void setPriceMatrixFacade(PriceMatrixFacade priceMatrixFacade) {
        this.priceMatrixFacade = priceMatrixFacade;
    }
}
