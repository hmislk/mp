/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.memberShip;

import com.divudi.bean.EnumController;
import com.divudi.bean.InstitutionController;
import com.divudi.data.memberShip.IpaCreditInstitution;
import com.divudi.data.PaymentMethod;
import com.divudi.data.memberShip.IpaMemberShip;
import com.divudi.data.memberShip.IpaPaymentMethod;
import com.divudi.entity.Institution;
import com.divudi.entity.MembershipScheme;
import com.divudi.facade.InwardPriceAdjustmentFacade;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

/**
 *
 * @author safrin
 */
@Named
@RequestScoped
public class ReportMemberShip {

    List<IpaPaymentMethod> ipaPaymentMethods;
    List<IpaCreditInstitution> ipaCreditInstitutions;
    private List<IpaMemberShip> ipaMemberShip;
    @Inject
    EnumController enumController;
    @Inject
    InstitutionController institutionController;
    @Inject
    InwardMemberShipDiscount inwardMemberShipDiscount;
    @Inject
    MembershipSchemeController membershipSchemeController;
    @EJB
    InwardPriceAdjustmentFacade inwardPriceAdjustmentFacade;

    public void createPaymentMethodInwardPriceAdjustments() {
        ipaPaymentMethods = new ArrayList<>();
        for (PaymentMethod pm : getEnumController().getPaymentMethods()) {
            IpaPaymentMethod subTable = new IpaPaymentMethod();
            subTable.setPaymentMethod(pm);
            subTable.setInwardPriceAdjustments(getInwardMemberShipDiscount().getInwardPriceAdjustments(pm));
            if (!subTable.getInwardPriceAdjustments().isEmpty()) {
                ipaPaymentMethods.add(subTable);
            }
        }
    }

    public void createCreditInstitutionInwardPriceAdjustments() {
        ipaCreditInstitutions = new ArrayList<>();
        for (Institution ins : getInstitutionController().getCreditCompany()) {
            IpaCreditInstitution subTable1 = new IpaCreditInstitution();
            subTable1.setInstitution(ins);
            for (PaymentMethod pm : getEnumController().getPaymentMethods()) {
                IpaPaymentMethod subTable2 = new IpaPaymentMethod();
                subTable2.setPaymentMethod(pm);
                subTable2.setInwardPriceAdjustments(getInwardMemberShipDiscount().getInwardPriceAdjustments(ins, pm));

                if (!subTable2.getInwardPriceAdjustments().isEmpty()) {
                    subTable1.getIpaPaymentMethods().add(subTable2);
                }
            }

            if (!subTable1.getIpaPaymentMethods().isEmpty()) {
                ipaCreditInstitutions.add(subTable1);
            }
        }
    }

    public void createMemberShipInwardPriceAdjustments() {
        ipaMemberShip = new ArrayList<>();
        for (MembershipScheme mem : getMembershipSchemeController().getItems()) {
            IpaMemberShip subTable1 = new IpaMemberShip();
            subTable1.setMembershipScheme(mem);
            for (PaymentMethod pm : getEnumController().getPaymentMethods()) {
                IpaPaymentMethod subTable2 = new IpaPaymentMethod();
                subTable2.setPaymentMethod(pm);
                subTable2.setInwardPriceAdjustments(getInwardMemberShipDiscount().getInwardPriceAdjustments(mem, pm));

                if (!subTable2.getInwardPriceAdjustments().isEmpty()) {
                    subTable1.getIpaPaymentMethods().add(subTable2);
                }
            }

            if (!subTable1.getIpaPaymentMethods().isEmpty()) {
                ipaMemberShip.add(subTable1);
            }
        }
    }

    public MembershipSchemeController getMembershipSchemeController() {
        return membershipSchemeController;
    }

    public void setMembershipSchemeController(MembershipSchemeController membershipSchemeController) {
        this.membershipSchemeController = membershipSchemeController;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public void setInstitutionController(InstitutionController institutionController) {
        this.institutionController = institutionController;
    }

    public InwardMemberShipDiscount getInwardMemberShipDiscount() {
        return inwardMemberShipDiscount;
    }

    public void setInwardMemberShipDiscount(InwardMemberShipDiscount inwardMemberShipDiscount) {
        this.inwardMemberShipDiscount = inwardMemberShipDiscount;
    }

    public List<IpaPaymentMethod> getIpaPaymentMethods() {
        return ipaPaymentMethods;
    }

    public void setIpaPaymentMethods(List<IpaPaymentMethod> ipaPaymentMethods) {
        this.ipaPaymentMethods = ipaPaymentMethods;
    }

    public List<IpaCreditInstitution> getIpaCreditInstitutions() {
        return ipaCreditInstitutions;
    }

    public void setIpaCreditInstitutions(List<IpaCreditInstitution> ipaCreditInstitutions) {
        this.ipaCreditInstitutions = ipaCreditInstitutions;
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

    /**
     * Creates a new instance of ReportMemberShip
     */
    public ReportMemberShip() {
    }

    public List<IpaMemberShip> getIpaMemberShip() {
        return ipaMemberShip;
    }

    public void setIpaMemberShip(List<IpaMemberShip> ipaMemberShip) {
        this.ipaMemberShip = ipaMemberShip;
    }

}
