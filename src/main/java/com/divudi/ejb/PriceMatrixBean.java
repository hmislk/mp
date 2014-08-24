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
import com.divudi.entity.ServiceCategory;
import com.divudi.entity.ServiceSubCategory;
import com.divudi.entity.WebUser;
import com.divudi.entity.inward.InwardPriceAdjustment;
import com.divudi.entity.lab.InvestigationCategory;
import com.divudi.entity.memberShip.InwardMemberShipDiscount;
import com.divudi.entity.memberShip.MembershipScheme;
import com.divudi.entity.memberShip.OpdMemberShipDiscount;
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

    public InwardMemberShipDiscount getInwardMemberDisCount(PaymentMethod paymentMethod, MembershipScheme membershipScheme, Institution ins, InwardChargeType inwardChargeType) {
        String sql;
        HashMap hm = new HashMap();
        hm.put("p", paymentMethod);
        hm.put("inw", inwardChargeType);
        if (membershipScheme != null) {
            hm.put("m", membershipScheme);
            if (ins != null) {
                sql = "Select i from InwardMemberShipDiscount i"
                        + "  where i.retired=false "
                        + " and i.membershipScheme=:m "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw"
                        + " and i.institution=:ins ";
                hm.put("ins", ins);
            } else {
                sql = "Select i from InwardMemberShipDiscount i "
                        + " where i.retired=false "
                        + " and  i.membershipScheme=:m "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw "
                        + " and i.institution is null ";
            }
        } else {
            if (ins != null) {
                sql = "Select i from InwardMemberShipDiscount i "
                        + " where i.retired=false "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw "
                        + " and i.institution=:ins"
                        + " and i.membershipScheme is null ";
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

        return (InwardMemberShipDiscount) getPriceMatrixFacade().findFirstBySQL(sql, hm);
    }

    public OpdMemberShipDiscount getOpdMemberDisCount(PaymentMethod paymentMethod, MembershipScheme membershipScheme, Department department, Category category) {
        OpdMemberShipDiscount opdMemberShipDiscount = null;

        //System.err.println(paymentMethod);
        //System.err.println(membershipScheme);
        //System.err.println(department);
        //System.err.println(category);

        //Get Discount From Category        
        opdMemberShipDiscount = fetchOpdMemberShipDiscount(membershipScheme, paymentMethod, category);

        //Get Discount From Parent Category
        if (opdMemberShipDiscount == null && category != null) {
            opdMemberShipDiscount = fetchOpdMemberShipDiscount(membershipScheme, paymentMethod, category.getParentCategory());

        }

        //Get Discount From Department
        if (opdMemberShipDiscount == null) {
            opdMemberShipDiscount = fetchOpdMemberShipDiscount(membershipScheme, paymentMethod, department);
        }

        //System.err.println("Discount 1 " + opdMemberShipDiscount);
        if (opdMemberShipDiscount != null) {
            //System.err.println("Discount 2 " + opdMemberShipDiscount.getDiscountPercent());
        }

        return opdMemberShipDiscount;
    }

    public OpdMemberShipDiscount fetchOpdMemberShipDiscount(MembershipScheme membershipScheme, PaymentMethod paymentMethod, Category category) {
        String sql;
        HashMap hm = new HashMap();
        hm.put("p", paymentMethod);
        hm.put("m", membershipScheme);
        hm.put("cat", category);
        sql = "Select i from OpdMemberShipDiscount i"
                + "  where i.retired=false "
                + " and i.membershipScheme=:m "
                + " and i.paymentMethod=:p"
                + " and i.category=:cat ";

        return (OpdMemberShipDiscount) getPriceMatrixFacade().findFirstBySQL(sql, hm);

    }

    public OpdMemberShipDiscount fetchOpdMemberShipDiscount(MembershipScheme membershipScheme, PaymentMethod paymentMethod, Department department) {
        String sql;
        HashMap hm = new HashMap();
        hm.put("p", paymentMethod);
        hm.put("m", membershipScheme);
        hm.put("dep", department);
        sql = "Select i from OpdMemberShipDiscount i"
                + "  where i.retired=false "
                + " and i.membershipScheme=:m "
                + " and i.paymentMethod=:p"
                + " and i.department=:dep ";

        return (OpdMemberShipDiscount) getPriceMatrixFacade().findFirstBySQL(sql, hm);

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

    public List<PriceMatrix> getOpdMemberShipDiscountsDepartment(MembershipScheme membershipScheme) {
        String sql = "select ipa from OpdMemberShipDiscount ipa "
                + " where ipa.retired=false"
                + " and ipa.membershipScheme=:pm"
                + " and ipa.category is null"
                + " order by ipa.department.name ";

        HashMap hm = new HashMap();
        hm.put("pm", membershipScheme);

        return getPriceMatrixFacade().findBySQL(sql, hm);
    }

    public List<PriceMatrix> getOpdMemberShipDiscountsCategory(MembershipScheme membershipScheme) {
        String sql = "select ipa from OpdMemberShipDiscount ipa "
                + " where ipa.retired=false"
                + " and ipa.membershipScheme=:pm"
                + " and ipa.department is null"
                + " order by ipa.category.name ";

        HashMap hm = new HashMap();
        hm.put("pm", membershipScheme);

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

    public InwardMemberShipDiscount getInwardMemberShipDiscount(MembershipScheme membershipScheme, Institution institution, PaymentMethod paymentMethod, InwardChargeType inwardChargeType, WebUser webUser) {
        PriceMatrix object = getInwardMemberDisCount(paymentMethod, membershipScheme, institution, inwardChargeType);

        if (object == null) {
            object = new InwardMemberShipDiscount();
            object.setCreatedAt(new Date());
            object.setCreater(webUser);
            object.setInwardChargeType(inwardChargeType);
            object.setMembershipScheme(membershipScheme);
            object.setPaymentMethod(paymentMethod);
            object.setInstitution(institution);
            getPriceMatrixFacade().create(object);
        }

        return (InwardMemberShipDiscount) object;

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
