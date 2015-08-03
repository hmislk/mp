/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.facade.BillComponentFacade;
import com.divudi.facade.BillEntryFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.InstitutionFacade;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.util.JsfUtil;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author Administrator
 */
@Named(value = "dataAdministrationController")
@ApplicationScoped
public class DataAdministrationController {

    @EJB
    BillComponentFacade billComponentFacade;
    @EJB
    BillFeeFacade billFeeFacade;
    @EJB
    BillEntryFacade billEntryFacade;
    @EJB
    BillItemFacade billItemFacade;
    @EJB
    BillFacade billFacade;
    @EJB
    ItemFacade itemFacade;
    @EJB
    InstitutionFacade institutionFacade;
    @EJB
    DepartmentFacade departmentFacade;
    @Inject
    SessionController sessionController;

    public void removeAllItemsWithoutName() {
        List<Item> iss = itemFacade.findAll();
        Date d = new Date();
        for (Item i : iss) {
            if (i == null) {
                return;
            }
            if (i.getName() == null) {
                return;
            }
            if (i.getName().trim().equals("")) {
                i.setRetired(true);
                i.setRetiredAt(d);
                i.setRetirer(sessionController.getLoggedUser());
                itemFacade.edit(i);
                System.out.println("i = " + i);
            }
        }
        JsfUtil.addSuccessMessage("Updated");
    }

    public void removeAllInstitutionsWithoutName() {
        List<Institution> iss = institutionFacade.findAll();
        Date d = new Date();
        for (Institution i : iss) {
            if (i == null) {
                return;
            }
            if (i.getName() == null) {
                return;
            }
            if (i.getName().trim().equals("")) {
                i.setRetired(true);
                i.setRetiredAt(d);
                i.setRetirer(sessionController.getLoggedUser());
                institutionFacade.edit(i);
                System.out.println("i = " + i);
            }
        }
        JsfUtil.addSuccessMessage("Updated");
    }

    public void removeAllDepartmentsWithoutName() {
        List<Department> iss = departmentFacade.findAll();
        Date d = new Date();
        for (Department i : iss) {
            if (i == null) {
                return;
            }
            if (i.getName() == null) {
                return;
            }
            if (i.getName().trim().equals("")) {
                i.setRetired(true);
                i.setRetiredAt(d);
                i.setRetirer(sessionController.getLoggedUser());
                departmentFacade.edit(i);
                System.out.println("i = " + i);
            }
        }
        JsfUtil.addSuccessMessage("Updated");
    }

    /**
     * Creates a new instance of DataAdministrationController
     */
    public DataAdministrationController() {
    }

    public BillComponentFacade getBillComponentFacade() {
        return billComponentFacade;
    }

    public void setBillComponentFacade(BillComponentFacade billComponentFacade) {
        this.billComponentFacade = billComponentFacade;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public BillEntryFacade getBillEntryFacade() {
        return billEntryFacade;
    }

    public void setBillEntryFacade(BillEntryFacade billEntryFacade) {
        this.billEntryFacade = billEntryFacade;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

}
