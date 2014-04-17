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
import com.divudi.ejb.PriceMatrixBean;
import com.divudi.entity.Institution;
import com.divudi.entity.PriceMatrix;
import com.divudi.entity.memberShip.MembershipScheme;
import com.divudi.facade.PriceMatrixFacade;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
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
    private List<PriceMatrix> items;
    @Inject
    private EnumController enumController;
    @Inject
    private SessionController sessionController;
    @EJB
    private PriceMatrixFacade priceMatrixFacade;
    @EJB
    PriceMatrixBean priceMatrixBean;

    public PriceMatrixBean getPriceMatrixBean() {
        return priceMatrixBean;
    }

    public void setPriceMatrixBean(PriceMatrixBean priceMatrixBean) {
        this.priceMatrixBean = priceMatrixBean;
    }

    public void edit(PriceMatrix inwardPriceAdjustment) {
        getPriceMatrixFacade().edit(inwardPriceAdjustment);
    }

    public void makeNull() {
        currentMembershipScheme = null;
        currentPaymentMethod = null;
        items = null;
    }

    public void createItems() {

        if (getCurrentPaymentMethod() == null) {
            return;
        }

        items = new ArrayList<>();
        for (InwardChargeType ict : getEnumController().getInwardChargeTypes()) {
            items.add(getPriceMatrixBean().getInwardMemberShipDiscount(getCurrentMembershipScheme(), getInstitution(), getCurrentPaymentMethod(), ict, getSessionController().getLoggedUser()));
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

    public List<PriceMatrix> getItems() {
        return items;
    }

    public void setItems(List<PriceMatrix> items) {
        this.items = items;
    }

    public EnumController getEnumController() {
        return enumController;
    }

    public void setEnumController(EnumController enumController) {
        this.enumController = enumController;
    }

    public PriceMatrixFacade getPriceMatrixFacade() {
        return priceMatrixFacade;
    }

    public void setPriceMatrixFacade(PriceMatrixFacade priceMatrixFacade) {
        this.priceMatrixFacade = priceMatrixFacade;
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
