/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.memberShip;

import com.divudi.bean.EnumController;
import com.divudi.bean.SessionController;
import com.divudi.data.PaymentMethod;
import com.divudi.data.inward.InwardChargeType;
import com.divudi.entity.Institution;
import com.divudi.entity.InwardPriceAdjustment;
import com.divudi.entity.MembershipScheme;
import com.divudi.facade.InwardPriceAdjustmentFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class InwardMemberShipDiscount implements Serializable {

    private MembershipScheme currentMembershipScheme;
    private PaymentMethod currentPaymentMethod;
    private Institution institution;
    private List<InwardPriceAdjustment> items;
    @Inject
    private EnumController enumController;
    @Inject
    private SessionController sessionController;
    @EJB
    private InwardPriceAdjustmentFacade inwardPriceAdjustmentFacade;

    public void edit(InwardPriceAdjustment inwardPriceAdjustment) {
        getInwardPriceAdjustmentFacade().edit(inwardPriceAdjustment);
    }

    public void makeNull() {
        currentMembershipScheme = null;
        currentPaymentMethod = null;
        items = null;
    }

    public InwardPriceAdjustment getMemberDisCount(PaymentMethod paymentMethod, MembershipScheme membershipScheme, Institution ins, InwardChargeType inwardChargeType) {
        String sql;
        HashMap hm = new HashMap();

        if (membershipScheme != null) {
            if (ins != null) {
                sql = "Select i from InwardPriceAdjustment i"
                        + "  where i.retired=false "
                        + " and i.membershipScheme=:m "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw"
                        + " and i.institution=:ins ";
                hm.put("m", membershipScheme);
                hm.put("ins", ins);
            } else {
                sql = "Select i from InwardPriceAdjustment i "
                        + " where i.retired=false "
                        + " and  i.membershipScheme=:m "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw "
                        + " and i.institution is null ";
                hm.put("m", membershipScheme);
            }
        } else {
            if (ins != null) {
                sql = "Select i from InwardPriceAdjustment i "
                        + " where i.retired=false "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw "
                        + " and i.membershipScheme is null"
                        + " and i.institution=:ins ";
                hm.put("ins", ins);
            } else {
                sql = "Select i from InwardPriceAdjustment i "
                        + " where i.retired=false "
                        + " and i.paymentMethod=:p "
                        + " and i.inwardChargeType=:inw "
                        + " and i.membershipScheme is null"
                        + " and i.institution is null ";
            }
        }

        hm.put("p", paymentMethod);
        hm.put("inw", inwardChargeType);

        return getInwardPriceAdjustmentFacade().findFirstBySQL(sql, hm);
    }

    public List<InwardPriceAdjustment> getInwardPriceAdjustments(PaymentMethod paymentMethod) {
        String sql = "select ipa from InwardPriceAdjustment ipa "
                + " where ipa.retired=false"
                + " and ipa.paymentMethod=:pm"
                + " and ipa.membershipScheme is null"
                + " and ipa.institution is null"
                + " order by ipa.paymentMethod ";

        HashMap hm = new HashMap();
        hm.put("pm", paymentMethod);

        return getInwardPriceAdjustmentFacade().findBySQL(sql, hm);
    }

    public List<InwardPriceAdjustment> getInwardPriceAdjustments(Institution ins, PaymentMethod pay) {
        String sql = "select ipa from InwardPriceAdjustment ipa "
                + " where ipa.retired=false"
                + " and ipa.paymentMethod=:pm"
                + " and ipa.institution=:ins"
                + " and ipa.membershipScheme is null";

        HashMap hm = new HashMap();
        hm.put("pm", pay);
        hm.put("ins", ins);

        return getInwardPriceAdjustmentFacade().findBySQL(sql, hm);
    }

    public List<InwardPriceAdjustment> getInwardPriceAdjustments(MembershipScheme mem, PaymentMethod pay) {
        String sql = "select ipa from InwardPriceAdjustment ipa "
                + " where ipa.retired=false"
                + " and ipa.paymentMethod=:pm"
                + " and ipa.membershipScheme=:mem"
                + " and ipa.institution is null";

        HashMap hm = new HashMap();
        hm.put("pm", pay);
        hm.put("mem", mem);

        return getInwardPriceAdjustmentFacade().findBySQL(sql, hm);
    }

    private InwardPriceAdjustment getInwardPriceAdjustment(InwardChargeType inwardChargeType) {
        InwardPriceAdjustment object = getMemberDisCount(getCurrentPaymentMethod(), getCurrentMembershipScheme(), institution, inwardChargeType);

        if (object == null) {
            object = new InwardPriceAdjustment();
            object.setCreatedAt(new Date());
            object.setCreater(getSessionController().getLoggedUser());
            object.setInwardChargeType(inwardChargeType);
            object.setMembershipScheme(getCurrentMembershipScheme());
            object.setPaymentMethod(getCurrentPaymentMethod());
            object.setInstitution(institution);
            getInwardPriceAdjustmentFacade().create(object);
        }

        return object;

    }

    public void createItems() {

        if (getCurrentPaymentMethod() == null) {
            return;
        }

        items = new ArrayList<>();
        for (InwardChargeType ict : getEnumController().getInwardChargeTypes()) {
            items.add(getInwardPriceAdjustment(ict));
        }

    }

    /**
     * Creates a new instance of InwardMemberShipDiscount
     */
    public InwardMemberShipDiscount() {
    }

    public MembershipScheme getCurrentMembershipScheme() {
        return currentMembershipScheme;
    }

    public void setCurrentMembershipScheme(MembershipScheme currentMembershipScheme) {
        this.currentMembershipScheme = currentMembershipScheme;
    }

    public PaymentMethod getCurrentPaymentMethod() {
        return currentPaymentMethod;
    }

    public void setCurrentPaymentMethod(PaymentMethod currentPaymentMethod) {
        this.currentPaymentMethod = currentPaymentMethod;
    }

    public List<InwardPriceAdjustment> getItems() {
        return items;
    }

    public void setItems(List<InwardPriceAdjustment> items) {
        this.items = items;
    }

    public EnumController getEnumController() {
        return enumController;
    }

    public void setEnumController(EnumController enumController) {
        this.enumController = enumController;
    }

    public InwardPriceAdjustmentFacade getInwardPriceAdjustmentFacade() {
        return inwardPriceAdjustmentFacade;
    }

    public void setInwardPriceAdjustmentFacade(InwardPriceAdjustmentFacade inwardPriceAdjustmentFacade) {
        this.inwardPriceAdjustmentFacade = inwardPriceAdjustmentFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

}
