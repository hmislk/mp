/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import com.divudi.data.MessageType;
import com.divudi.data.Privileges;
import com.divudi.entity.Bill;
import com.divudi.entity.Department;
import com.divudi.entity.Message;
import com.divudi.entity.WebUser;
import com.divudi.entity.WebUserDepartment;
import com.divudi.entity.WebUserPrivilege;
import com.divudi.facade.MessageFacade;
import com.divudi.facade.WebUserFacade;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author buddhika
 */
@Named(value = "messageController")
@ApplicationScoped
public class MessageController {

    /**
     * EJBs
     */
    @EJB
    WebUserFacade webUserFacade;
    @EJB
    MessageFacade messageFacade;
    @Inject
    SessionController sessionController;
    /**
     * Controllers
     */
    @Inject
    WebUserController webUserController;

    /**
     * Properties
     */
    /**
     * Creates a new instance of MessageController
     */
    public MessageController() {
    }

    public List<Message> userMessages(WebUser u) {
        String sql;
        Map m = new HashMap();
        m.put("wu", u);
        sql = "Select m "
                + " from Message m "
                + " where m.userIntended=:wu "
                + " and m.invalidated=false "
                + " and m.noted=false";
//        System.out.println("m = " + m);
//        System.out.println("sql = " + sql);
        return messageFacade.findBySQL(sql, m);
    }

//    public boolean hasUserMessages(WebUser u) {
//        if (u == null) {
//            return false;
//        }
//        try {
//            String sql;
//            Map m = new HashMap();
//            m.put("wu", u);
//            sql = "Select m "
//                    + " from Message m "
//                    + " where m.userIntended=:wu "
//                    + " and m.invalidated=false "
//                    + " and m.noted=false";
//            List<Message> ms = messageFacade.findBySQL(sql, m);
//            if (ms == null || ms.isEmpty()) {
//                return false;
//            }
//            return true;
//        } catch (Exception e) {
//            System.out.println("e = " + e.getMessage());
//            return false;
//        }
//    }

    
    public boolean getHasUserMessages() {
        WebUser u = sessionController.getLoggedUser();
        if (u == null) {
            return false;
        }
        try {
            String sql;
            Map m = new HashMap();
            m.put("wu", u);
            sql = "Select m "
                    + " from Message m "
                    + " where m.userIntended=:wu "
                    + " and m.invalidated=false "
                    + " and m.noted=false";
            List<Message> ms = messageFacade.findBySQL(sql, m);
            if (ms == null || ms.isEmpty()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            System.out.println("e = " + e.getMessage());
            return false;
        }
    }
    
    public void invalidateMessages(Bill originatingbill, Bill invalidatingBill) {
        System.out.println("invalidateMessages");
        if (originatingbill == null) {
            return;
        }
        String sql;
        Map hm = new HashMap();
        hm.put("b", originatingbill);
        sql = "Select m "
                + " from Message m "
                + " where m.originatingBill=:b";
//        System.out.println("m = " + hm);
//        System.out.println("sql = " + sql);
        List<Message> ms = messageFacade.findBySQL(sql, hm);
        for (Message m : ms) {
            m.setInvalidated(true);
            m.setInvalidatedAt(new Date());
            m.setInvalidateBill(invalidatingBill);
            m.setInvalidator(invalidatingBill.getCreater());
            messageFacade.edit(m);
        }
    }

    public void makeMessageAsNoted(Message m) {
        if (m == null) {
            return;
        }
        m.setNoted(true);
        m.setNotedAt(new Date());
        messageFacade.edit(m);
    }

    public void addMessageToWebUsers(Bill bill) {
        List<WebUser> us = getEligibleWebUsers(bill);
        String comments = "";
        String topic = "";
        MessageType mt = null;
        switch (bill.getBillType()) {
            case PharmacyTransferRequest:
                topic = "New Request to Issue";
                mt = MessageType.PharmacyTransferRequest;
                break;
            case PharmacyOrder:
                mt = MessageType.PharmacyApproval;
                topic = "New Order for Approval";
                break;
            case PharmacyTransferIssue:
                topic = "New Issue";
                mt = MessageType.PharmacyTransferIssue;
        }
        for (WebUser u : us) {
            Message m = new Message();

            m.setCreatedAt(new Date());
            m.setComments(comments);
            m.setCreater(bill.getCreater());
            m.setOriginatingBill(bill);
            m.setTopic(topic);
            m.setType(mt);
            m.setUserIntended(u);
            m.setInvalidated(false);
            m.setNoted(false);

            messageFacade.create(m);

        }
    }

    private List<WebUser> getEligibleWebUsers(Bill bill) {
        System.out.println("getEligibleWebUsers");
        System.out.println("bill = " + bill);
        Privileges p = null;
        Department dept = null;
        switch (bill.getBillType()) {
            case PharmacyTransferRequest:
                p = Privileges.PharmacyTransfer;
                dept = bill.getToDepartment();
                break;
            case PharmacyOrder:
                p = Privileges.PharmacyOrderApproval;
                dept = bill.getDepartment();
                break;
            case PharmacyTransferIssue:
                p = Privileges.PharmacyTransfer;
                dept = bill.getToDepartment();
                break;
        }
        Map m = new HashMap();
        m.put("dep", dept);
        String sql = "select wud.webUser "
                + " from WebUserDepartment wud "
                + " where wud.department=:dep";
//        System.out.println("sql = " + sql);
//        System.out.println("m = " + m);
        List<WebUser> us = webUserFacade.findBySQL(sql, m);
//        System.out.println("us.size() = " + us.size());
        List<WebUser> lst = new ArrayList<>();

        for (WebUser u : us) {
            System.out.println("u = " + u);
            if (webUserController.hasPrivilege(u, p)) {
                System.out.println("has privilege");
                lst.add(u);
            }
        }

        return lst;
    }
}
