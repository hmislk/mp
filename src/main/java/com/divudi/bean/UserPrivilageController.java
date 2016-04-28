/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean;

import com.divudi.data.dataStructure.PrivilageNode;
import com.divudi.data.Privileges;
import com.divudi.entity.WebUser;
import java.util.TimeZone;
import com.divudi.facade.WebUserPrivilegeFacade;
import com.divudi.entity.WebUserPrivilege;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.faces.component.UIComponent;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
//package org.primefaces.examples.view;  

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.TemporalType;

import org.primefaces.model.TreeNode;

//import org.primefaces.examples.domain.Document;  
//import org.primefaces.model;
/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@SessionScoped
public class UserPrivilageController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    SessionController sessionController;
    @EJB
    private WebUserPrivilegeFacade ejbFacade;
    private List<WebUserPrivilege> selectedItems;
    private WebUserPrivilege current;
    private WebUser currentWebUser;
    private List<WebUserPrivilege> items = null;
    //private Privileges currentPrivileges;
    private TreeNode root;
    private TreeNode[] selectedNodes;
    private List<Privileges> privilegeList;

    private TreeNode createTreeNode() {
        TreeNode tmproot = new PrivilageNode("Root", null);

        TreeNode node0 = new PrivilageNode("OPD", tmproot);
        TreeNode node00 = new PrivilageNode("Billing Menu", node0, Privileges.Opd);
        TreeNode node01 = new PrivilageNode("Bill", node0, Privileges.OpdBilling);
        TreeNode node02 = new PrivilageNode("Bill Search", node0, Privileges.OpdBillSearch);
        TreeNode node03 = new PrivilageNode("Bill Item Search", node0, Privileges.OpdBillItemSearch);
        TreeNode node04 = new PrivilageNode("Reprint", node0, Privileges.OpdReprint);
        TreeNode node05 = new PrivilageNode("Cancel", node0, Privileges.OpdCancel);
        TreeNode node06 = new PrivilageNode("Return", node0, Privileges.OpdReturn);
        TreeNode node07 = new PrivilageNode("Reactivate", node0, Privileges.OpdReactivate);

        TreeNode node1 = new PrivilageNode("Inward", tmproot);
        TreeNode node10 = new PrivilageNode("Inward Menu", node1, Privileges.Inward);
        TreeNode node11 = new PrivilageNode("Billing", node1, Privileges.InwardBilling);
        TreeNode node12 = new PrivilageNode("Search Bills", node1, Privileges.InwardBillSearch);
        TreeNode node13 = new PrivilageNode("Search Bill Items", node1, Privileges.InwardBillItemSearch);
        TreeNode node14 = new PrivilageNode("Reprint", node1, Privileges.InwardBillReprint);
        TreeNode node15 = new PrivilageNode("Cancel", node1, Privileges.InwardCancel);
        TreeNode node16 = new PrivilageNode("Return", node1, Privileges.InwardReturn);
        TreeNode node17 = new PrivilageNode("Reactivate", node1, Privileges.InwardReactivate);
        TreeNode node18 = new PrivilageNode("Show Inward Fee", node1, Privileges.ShowInwardFee);

        TreeNode node2 = new PrivilageNode("Lab", tmproot);
        TreeNode node200 = new PrivilageNode("Lab Menu", node2, Privileges.Lab);
        TreeNode node201 = new PrivilageNode("Billing", node2, Privileges.LabBilling);
        TreeNode node202 = new PrivilageNode("Search Bills", node2, Privileges.LabBillSearch);
        TreeNode node203 = new PrivilageNode("Search Bills Items", node2, Privileges.LabBillItemSearch);
        TreeNode node204 = new PrivilageNode("Patient Edit", node2, Privileges.LabEditPatient);
        TreeNode node205 = new PrivilageNode("Reprint", node2, Privileges.LabBillReprint);
        TreeNode node206 = new PrivilageNode("Return", node2, Privileges.LabBillReturning);
        TreeNode node207 = new PrivilageNode("Cancel", node2, Privileges.LabBillCancelling);
        TreeNode node208 = new PrivilageNode("Reactivate", node2, Privileges.LabBillReactivating);
        TreeNode node209 = new PrivilageNode("Sample Collection", node2, Privileges.LabSampleCollecting);
        TreeNode node210 = new PrivilageNode("Sample Receive", node2, Privileges.LabSampleReceiving);
        TreeNode node211 = new PrivilageNode("DataEntry", node2, Privileges.LabDataentry);
        TreeNode node212 = new PrivilageNode("Autherize", node2, Privileges.LabAutherizing);
        TreeNode node213 = new PrivilageNode("De-Autherize", node2, Privileges.LabDeAutherizing);
        TreeNode node214 = new PrivilageNode("Report Print", node2, Privileges.LabPrinting);
        TreeNode node215 = new PrivilageNode("Lab Report Formats Editing", node2, Privileges.LabReportFormatEditing);
        TreeNode node216 = new PrivilageNode("Lab Summeries", node2, Privileges.LabSummeriesLevel1);

        TreeNode node3 = new PrivilageNode("Pharmacy", tmproot);
        TreeNode node300 = new PrivilageNode("Pharmacy Menu", node3, Privileges.Pharmacy);
        TreeNode node301 = new PrivilageNode("Pharmacy Administration", node3, Privileges.PharmacyAdministration);
        TreeNode node306 = new PrivilageNode("Pharmacy Stock Adjustment", node3, Privileges.PharmacyStockAdjustment);
        TreeNode node307 = new PrivilageNode("Pharmacy Re Add To Stock", node3, Privileges.PharmacyReAddToStock);

        ///////////////////////
        TreeNode node302 = new PrivilageNode("GRN", node3);
        TreeNode node3021 = new PrivilageNode("GRN", node302, Privileges.PharmacyGoodReceive);
        TreeNode node3022 = new PrivilageNode("GRN Cancelling", node302, Privileges.PharmacyGoodReceiveCancel);
        TreeNode node3023 = new PrivilageNode("GRN Return", node302, Privileges.PharmacyGoodReceiveReturn);
        TreeNode node3024 = new PrivilageNode("GRN Edit", node302, Privileges.PharmacyGoodReceiveEdit);
        ///////////////////////
        TreeNode node303 = new PrivilageNode("Order", node3);
        TreeNode node3031 = new PrivilageNode("Order Creation", node303, Privileges.PharmacyOrderCreation);
        TreeNode node3032 = new PrivilageNode("Order Aproval", node303, Privileges.PharmacyOrderApproval);
        TreeNode node3033 = new PrivilageNode("Order Cancellation", node303, Privileges.PharmacyOrderCancellation);
        //////////////////
        TreeNode node304 = new PrivilageNode("Sale", node3);
        TreeNode node3041 = new PrivilageNode("Pharmacy Sale", node304, Privileges.PharmacySale);
        TreeNode node3042 = new PrivilageNode("Pharmacy Sale Cancel", node304, Privileges.PharmacySaleCancel);
        TreeNode node3043 = new PrivilageNode("Pharmacy Sale Return", node304, Privileges.PharmacySaleReturn);
        //////////////////
        TreeNode node305 = new PrivilageNode("Purchase", node3);
        TreeNode node3051 = new PrivilageNode("Purchase", node305, Privileges.PharmacyPurchase);
        TreeNode node3052 = new PrivilageNode("Purchase Cancel", node305, Privileges.PharmacyPurchaseCancellation);
        TreeNode node3053 = new PrivilageNode("Purchase Return", node305, Privileges.PharmacyPurchaseReturn);
        ///////////////////
        TreeNode node308 = new PrivilageNode("Pharmacy Dealor Payment", node3, Privileges.PharmacyDealorPayment);
        TreeNode node309 = new PrivilageNode("Pharmacy Search", node3, Privileges.PharmacySearch);
        TreeNode node310 = new PrivilageNode("Pharmacy Reports", node3, Privileges.PharmacyReports);
        TreeNode node311 = new PrivilageNode("Pharmacy Summery", node3, Privileges.PharmacySummery);
        TreeNode node312 = new PrivilageNode("Pharmacy Transfer", node3, Privileges.PharmacyTransfer);
        TreeNode node313 = new PrivilageNode("Pharmacy Set Reorder Level", node3, Privileges.PharmacySetReorderLevel);

        ///////////////////
        TreeNode node4 = new PrivilageNode("Payment", tmproot);
        TreeNode node400 = new PrivilageNode("Payment Menu", node4, Privileges.Payment);
        TreeNode node401 = new PrivilageNode("Staff Payment", node4, Privileges.PaymentBilling);
        TreeNode node402 = new PrivilageNode("Payment Search", node4, Privileges.PaymentBillSearch);
        TreeNode node403 = new PrivilageNode("Payment Reprints", node4, Privileges.PaymentBillReprint);
        TreeNode node404 = new PrivilageNode("Payment Cancel", node4, Privileges.PaymentBillCancel);
        TreeNode node405 = new PrivilageNode("Payment Refund", node4, Privileges.PaymentBillRefund);
        TreeNode node406 = new PrivilageNode("Payment Reactivation", node4, Privileges.PaymentBillReactivation);

        TreeNode node5 = new PrivilageNode("Reports", tmproot);
        TreeNode node53 = new PrivilageNode("Reports Menu", node5, Privileges.Reports);
        TreeNode node51 = new PrivilageNode("For Own Institution", node5);
        TreeNode node52 = new PrivilageNode("For All Institution", node5);

        TreeNode node510 = new PrivilageNode("Cash/Card Bill Reports", node51, Privileges.ReportsSearchCashCardOwn);
        TreeNode node511 = new PrivilageNode("Credit Bill Reports", node51, Privileges.ReportsSearchCreditOwn);
        TreeNode node512 = new PrivilageNode("Item Reports", node51, Privileges.ReportsItemOwn);

        TreeNode node520 = new PrivilageNode("Cash/Card Bill Reports", node52, Privileges.ReportsSearchCashCardOther);
        TreeNode node521 = new PrivilageNode("Credit Bill Reports", node52, Privileges.ReportSearchCreditOther);
        TreeNode node522 = new PrivilageNode("Item Reports", node52, Privileges.ReportsItemOwn);

        TreeNode node7 = new PrivilageNode("Clinicals", tmproot);
        TreeNode node700 = new PrivilageNode("Clinical Data", node7, Privileges.Clinical);
        TreeNode node701 = new PrivilageNode("Patient Summery", node7, Privileges.ClinicalPatientSummery);
        TreeNode node702 = new PrivilageNode("Patient Details", node7, Privileges.ClinicalPatientDetails);
        TreeNode node703 = new PrivilageNode("Patient Photo", node7, Privileges.ClinicalPatientPhoto);
        TreeNode node704 = new PrivilageNode("Visit Details", node7, Privileges.ClinicalVisitDetail);
        TreeNode node705 = new PrivilageNode("Visit Summery", node7, Privileges.ClinicalVisitSummery);
        TreeNode node706 = new PrivilageNode("History", node7, Privileges.ClinicalHistory);
        TreeNode node707 = new PrivilageNode("Administration", node7, Privileges.ClinicalAdministration);

        TreeNode node6 = new PrivilageNode("Administration", tmproot);
        TreeNode node60 = new PrivilageNode("Admin Menu", node6, Privileges.Admin);
        TreeNode node61 = new PrivilageNode("Manage Users", node6, Privileges.AdminManagingUsers);
        TreeNode node62 = new PrivilageNode("Manage Institutions", node6, Privileges.AdminInstitutions);
        TreeNode node63 = new PrivilageNode("Manage Staff", node6, Privileges.AdminStaff);
        TreeNode node64 = new PrivilageNode("Manage Items/Services", node6, Privileges.AdminItems);
        TreeNode node65 = new PrivilageNode("Manage Fees/Prices/Packages", node6, Privileges.AdminPrices);
        TreeNode node66 = new PrivilageNode("Remove Stock", node6, Privileges.DevelopersOnly);

        TreeNode node9 = new PrivilageNode("Human Resource", tmproot);
        TreeNode node91 = new PrivilageNode("HR Menu", node9, Privileges.Hr);

        TreeNode node8 = new PrivilageNode("Higheist Accountability", tmproot);
        TreeNode node81 = new PrivilageNode("Change Professional Fee", node8, Privileges.ChangeProfessionalFee);
        TreeNode node82 = new PrivilageNode("Change Professional Fee", node8, Privileges.ChangeCollectingCentre);

        TreeNode node20 = new PrivilageNode("Store", tmproot);
        TreeNode node2000 = new PrivilageNode("Store Menu", node20, Privileges.Store);
        TreeNode node2001 = new PrivilageNode("Issue", node20);
        TreeNode node20010 = new PrivilageNode("Issue Menu", node2001, Privileges.StoreIssue);
        TreeNode node20011 = new PrivilageNode("Inward Billing", node2001, Privileges.StoreIssueInwardBilling);
        TreeNode node20012 = new PrivilageNode("Search Issue Bill", node2001, Privileges.StoreIssueSearchBill);
        TreeNode node20013 = new PrivilageNode("Search Issue Bill Items", node2001, Privileges.StoreIssueBillItems);
        TreeNode node2002 = new PrivilageNode("Purchase", node20);
        TreeNode node20020 = new PrivilageNode("Purchase Menu", node2002, Privileges.StorePurchase);
        TreeNode node20021 = new PrivilageNode("Purchase Order", node2002, Privileges.StorePurchaseOrder);
        TreeNode node20022 = new PrivilageNode("PO Approve", node2002, Privileges.StorePurchaseOrderApprove);
        TreeNode node20023 = new PrivilageNode("GRN Recive", node2002, Privileges.StorePurchaseGRNRecive);
        TreeNode node20024 = new PrivilageNode("GRN Return", node2002, Privileges.StorePurchaseGRNReturn);
        TreeNode node20025 = new PrivilageNode("Purchase", node2002, Privileges.StorePurchasePurchase);
        TreeNode node2003 = new PrivilageNode("Transfer", node20);
        TreeNode node20030 = new PrivilageNode("Transfer Menu", node2003, Privileges.StoreTransfer);
        TreeNode node20031 = new PrivilageNode("Request", node2003, Privileges.StoreTransferRequest);
        TreeNode node20032 = new PrivilageNode("Issue", node2003, Privileges.StoreTransferIssue);
        TreeNode node20033 = new PrivilageNode("Recive", node2003, Privileges.StoreTransferRecive);
        TreeNode node20034 = new PrivilageNode("Report", node2003, Privileges.StoreTransferReport);
        TreeNode node2004 = new PrivilageNode("Ajustment", node20);
        TreeNode node20040 = new PrivilageNode("Adjustment Menu", node2004, Privileges.StoreAdjustment);
        TreeNode node20041 = new PrivilageNode("Department Stock(Qty)", node2004, Privileges.StoreAdjustmentDepartmentStock);
        TreeNode node20042 = new PrivilageNode("Staff Stock Adjustment", node2004, Privileges.StoreAdjustmentStaffStock);
        TreeNode node20043 = new PrivilageNode("Purchase Rate", node2004, Privileges.StoreAdjustmentPurchaseRate);
        TreeNode node20044 = new PrivilageNode("Sale Rate", node2004, Privileges.StoreAdjustmentSaleRate);
        TreeNode node2005 = new PrivilageNode("Delor Payment", node20);
        TreeNode node20050 = new PrivilageNode("Delor Payment Menu", node2005, Privileges.StoreDealorPayment);
        TreeNode node20051 = new PrivilageNode("Delor Due Search", node2005, Privileges.StoreDealorPaymentDueSearch);
        TreeNode node20052 = new PrivilageNode("Delor Due By Age", node2005, Privileges.StoreDealorPaymentDueByAge);
        TreeNode node20053 = new PrivilageNode("Payment", node2005);
        TreeNode node200530 = new PrivilageNode("Payment Menu", node20053, Privileges.StoreDealorPaymentPayment);
        TreeNode node200531 = new PrivilageNode("GRN Payment", node20053, Privileges.StoreDealorPaymentPaymentGRN);
        TreeNode node200532 = new PrivilageNode("GRN Payment(Select)", node20053, Privileges.StoreDealorPaymentPaymentGRNSelect);
        TreeNode node20054 = new PrivilageNode("GRN Payment Due Search", node2005, Privileges.StoreDealorPaymentGRNDoneSearch);
        TreeNode node2006 = new PrivilageNode("Search", node20);
        TreeNode node20060 = new PrivilageNode("Search Menu", node2006, Privileges.StoreSearch);
        TreeNode node2007 = new PrivilageNode("Report", node20);
        TreeNode node20070 = new PrivilageNode("Report Menu", node2007, Privileges.StoreReports);
        TreeNode node2008 = new PrivilageNode("Summery", node20);
        TreeNode node20080 = new PrivilageNode("Summery Menu", node2008, Privileges.StoreSummery);
        TreeNode node2009 = new PrivilageNode("Administration", node20);
        TreeNode node20090 = new PrivilageNode("Administration Menu", node2009, Privileges.StoreAdministration);

        TreeNode node21 = new PrivilageNode("Search", tmproot);
        TreeNode node2100 = new PrivilageNode("Search Menu", node21, Privileges.Search);
        TreeNode node2101 = new PrivilageNode("Grand Search", node21, Privileges.SearchGrand);

        TreeNode node22 =new PrivilageNode("Cash Transaction", tmproot);
        TreeNode node2200=new PrivilageNode("Cash Transaction Menu", node22, Privileges.CashTransaction);
        TreeNode node2201=new PrivilageNode("Cash In", node22, Privileges.CashTransactionCashIn);
        TreeNode node2202=new PrivilageNode("Cash Out", node22, Privileges.CashTransactionCashOut);
        TreeNode node2203=new PrivilageNode("List To Cash Recieve", node22, Privileges.CashTransactionListToCashRecieve);
        
        TreeNode node23=new PrivilageNode("Channelling", tmproot);
        TreeNode node2300=new PrivilageNode("Channelling Menu", node23, Privileges.Channelling);
        TreeNode node2301=new PrivilageNode("Channel Booking", node23, Privileges.ChannellingChannelBooking);
        TreeNode node2302=new PrivilageNode("Past Booking", node23, Privileges.ChannellingPastBooking);
        TreeNode node2303=new PrivilageNode("Booked List", node23, Privileges.ChannellingBookedList);
        TreeNode node2304=new PrivilageNode("Doctor Leave", node23, Privileges.ChannellingDoctorLeave);
        TreeNode node2305=new PrivilageNode("Channel Sheduling", node23, Privileges.ChannellingChannelSheduling);
        TreeNode node2306=new PrivilageNode("Channel Agent Fee", node23, Privileges.ChannellingChannelAgentFee);
        TreeNode node2307=new PrivilageNode("Payment", node23);
        TreeNode node23070=new PrivilageNode("Payment Menu", node2307, Privileges.ChannellingPayment);
        TreeNode node23071=new PrivilageNode("Pay Doctor", node2307, Privileges.ChannellingPaymentPayDoctor);
        TreeNode node23072=new PrivilageNode("Payment Due Search", node2307, Privileges.ChannellingPaymentDueSearch);
        TreeNode node23073=new PrivilageNode("Payment Done Search", node2307, Privileges.ChannellingPaymentDoneSearch);

        return tmproot;
    }

    public UserPrivilageController() {
        root = createTreeNode();
    }

    public TreeNode getRoot2() {
        return root;
    }

    public void setRoot2(TreeNode root2) {
        this.root = root2;
    }

    public TreeNode[] getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(TreeNode[] selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    private void removeAllPrivilages() {
        String sql = "SELECT i FROM WebUserPrivilege i where i.webUser.id= " + getCurrentWebUser().getId();
        List<WebUserPrivilege> tmp = getEjbFacade().findBySQL(sql);

        for (WebUserPrivilege wb : tmp) {
            wb.setRetired(true);
            getEjbFacade().edit(wb);
        }

    }

    public void savePrivileges() {
        if (currentWebUser == null) {
            UtilityController.addErrorMessage("Please select a user");
            return;
        }
        if (selectedNodes != null && selectedNodes.length > 0) {
            removeAllPrivilages();
            for (TreeNode node : selectedNodes) {
                Privileges p;
                p = ((PrivilageNode) node).getP();
                addSinglePrivilege(p);
            }
        }
        getItems();
    }

    public void addSinglePrivilege(Privileges p) {
        if (p == null) {
            return;
        }
        WebUserPrivilege wup;
        Map m = new HashMap();
        m.put("wup", p);
        String sql = "SELECT i FROM WebUserPrivilege i where i.retired=false and i.webUser.id= " + getCurrentWebUser().getId() + " and i.privilege=:wup ";
        List<WebUserPrivilege> tmp = getEjbFacade().findBySQL(sql, m, TemporalType.DATE);

        if (tmp == null || tmp.isEmpty()) {
            wup = new WebUserPrivilege();
            wup.setCreater(getSessionController().getLoggedUser());
            wup.setCreatedAt(Calendar.getInstance().getTime());
            wup.setPrivilege(p);
            wup.setWebUser(getCurrentWebUser());
            getFacade().create(wup);
        }

//        for (WebUserPrivilege wu : tmpNode) {
//            wu.setRetired(false);
//        }
    }

    public void remove() {
        if (getCurrent() == null) {
            UtilityController.addErrorMessage("Select Privilage");
            return;
        }

        getCurrent().setRetired(true);

        getFacade().edit(getCurrent());
    }

    private void recreateModel() {
        items = null;
    }

    public WebUserPrivilegeFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(WebUserPrivilegeFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public WebUserPrivilege getCurrent() {
        if (current == null) {
            current = new WebUserPrivilege();

        }
        return current;
    }

    public void setCurrent(WebUserPrivilege current) {
        this.current = current;
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(Calendar.getInstance(TimeZone.getTimeZone("IST")).getTime());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("DeleteSuccessfull");
        } else {
            UtilityController.addSuccessMessage("NothingToDelete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    private WebUserPrivilegeFacade getFacade() {
        return ejbFacade;
    }
    private TreeNode tmpNode;

//    public List<WebUserPrivilege> getItems2() {
//        // items = getFacade().findAll("name", true);
//        if (getCurrentWebUser() == null) {
//            return new ArrayList<WebUserPrivilege>();
//        }
//
//        String sql = "SELECT i FROM WebUserPrivilege i where i.retired=false and i.webUser.id= " + getCurrentWebUser().getId() + " order by i.webUser.webUserPerson.name";
//        items = getEjbFacade().findBySQL(sql);
//        if (items == null) {
//            items = new ArrayList<WebUserPrivilege>();
//        }
//        for (TreeNode n : root.getChildren()) {
//            n.setSelected(false);
//        }
//        for (TreeNode n : root.getChildren()) {
//            //////System.out.println("n is " + n);
//            for (TreeNode n1 : n.getChildren()) {
//                Privileges p;
//                //////System.out.println("n1 is " + n1);
//                //
//                try {
//                    if (n1 instanceof PrivilageNode) {
//                        p = ((PrivilageNode) n1).getP();
//                        markTreeNode(p, n1);
//                    } else {
//                        //////System.out.println("type of p is ");
//                    }
//                } catch (Exception e) {
//                    //////System.out.println("exception e is " + e.getMessage());
//                }
//            }
//        }
//        return items;
//    }
    private void unselectNode() {
        for (TreeNode n : root.getChildren()) {
            n.setSelected(false);
            for (TreeNode n1 : n.getChildren()) {
                n1.setSelected(false);
                for (TreeNode n2 : n1.getChildren()) {
                    n2.setSelected(false);
                }
            }
        }

        tmpNode = root;
    }

    public List<WebUserPrivilege> getItems() {
        if (getCurrentWebUser() == null) {
            root = createTreeNode();
            tmpNode = root;
            return new ArrayList<WebUserPrivilege>();

        }

        String sql = "SELECT i FROM WebUserPrivilege i where i.retired=false and i.webUser.id= " + getCurrentWebUser().getId() + " order by i.webUser.webUserPerson.name";
        items = getEjbFacade().findBySQL(sql);
        if (items == null) {
            items = new ArrayList<>();
            root = createTreeNode();
            tmpNode = root;
            return items;
        }

        root = createTreeNode();
        for (WebUserPrivilege wup : items) {
            for (TreeNode n : root.getChildren()) {
                if (wup.getPrivilege() == ((PrivilageNode) n).getP()) {
                    n.setSelected(true);
                }
                for (TreeNode n1 : n.getChildren()) {
                    if (wup.getPrivilege() == ((PrivilageNode) n1).getP()) {
                        n1.setSelected(true);
                    }
                    for (TreeNode n2 : n1.getChildren()) {
                        if (wup.getPrivilege() == ((PrivilageNode) n2).getP()) {
                            n2.setSelected(true);
                        }
                    }
                }
            }
        }
        tmpNode = root;
        return items;
    }

//    public void markTreeNode(Privileges p, TreeNode n) {
//        if (p == null) {
//            return;
//        }
//        n.setSelected(false);
//        Map m = new HashMap();
//        m.put("wup", p);
//        String sql = "SELECT i FROM WebUserPrivilege i where i.webUser.id= " + getCurrentWebUser().getId() + " and i.privilege=:wup ";
//        List<WebUserPrivilege> tmp = getEjbFacade().findBySQL(sql, m, TemporalType.DATE);
//        if (tmp == null || tmp.isEmpty()) {
//            for (WebUserPrivilege wu : tmp) {
//                if (!wu.isRetired()) {
//                    n.setSelected(true);
//                }
//            }
//        }
//    }
    public WebUser getCurrentWebUser() {
        return currentWebUser;
    }

    public void setCurrentWebUser(WebUser currentWebUser) {
        this.currentWebUser = currentWebUser;
        tmpNode = null;
    }

    public List<Privileges> getPrivilegeList() {
        return privilegeList;
    }

    public void setPrivilegeList(List<Privileges> privilegeList) {
        this.privilegeList = privilegeList;

    }

    public List<WebUserPrivilege> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<WebUserPrivilege> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public TreeNode getTmp() {
        getItems();
        return tmpNode;
    }

    public void setTmp(TreeNode tmp) {
        this.tmpNode = tmp;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    /**
     *
     */
    @FacesConverter(forClass = WebUserPrivilege.class)
    public static class WebUserPrivilegeControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserPrivilageController controller = (UserPrivilageController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "userPrivilegeController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof WebUserPrivilege) {
                WebUserPrivilege o = (WebUserPrivilege) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + UserPrivilageController.class.getName());
            }
        }
    }
}
