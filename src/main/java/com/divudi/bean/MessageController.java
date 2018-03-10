/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import com.divudi.data.MessageType;
import com.divudi.data.Privileges;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Message;
import com.divudi.entity.WebUser;
import com.divudi.facade.MessageFacade;
import com.divudi.facade.WebUserFacade;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.poi.util.IOUtils;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author buddhika
 */
@Named(value = "messageController")
@ApplicationScoped
public class MessageController {

    private UploadedFile file;
    /**
     * EJBs
     */
    @EJB
    WebUserFacade webUserFacade;
    @EJB
    MessageFacade messageFacade;
    @EJB
    CommonFunctions commonFunctions;
    /**
     * Controllers
     */
    @Inject
    WebUserController webUserController;
    @Inject
    SessionController sessionController;
    @Inject
    DepartmentController departmentController;
    /**
     * Class Variables
     */
    Message selected;
    Date fromDate;
    Date toDate;
    Institution institution;
    Department department;
    List<Message> messages;
    String comments;
    /**
     * Creates a new instance of MessageController
     */
    public MessageController() {
    }

    /**
     * Methods
     * @return 
     */
    public String viewSelectedImage(){
        if(selected==null){
            UtilityController.addErrorMessage("Noting Selected");
            return "";
        }
        System.out.println("selected.getComments() = " + selected.getComments());
        return "/image/view_image";
    }
    
    
    
    public void listImages() {
        String j;
        Map m = new HashMap();

        j = "Select m "
                + " from Message m "
                + " where m.createdAt between :fd and :td "
                + " and m.type=:t ";
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("t", MessageType.ImageReadeRequest);
        if(department!=null){
            j+= " and m.department=:d ";
            m.put("d", department);
        }else if(institution!=null){
            j+= " and m.institution=:i ";
            m.put("i", institution);
        }
        
        messages = getMessageFacade().findBySQL(j, m);
    }

    public void uploadImage() {
        InputStream in;
        if (file == null || "".equals(file.getFileName())) {
            UtilityController.addErrorMessage("Please select an image");
            return;
        }
        if (file == null) {
            UtilityController.addErrorMessage("Please select an image");
            return;
        }
        

        try {
            in = getFile().getInputstream();
            Message ei = new Message();
            ei.setFileName(file.getFileName());
            ei.setFileType(file.getContentType());
            ei.setBaImage(IOUtils.toByteArray(in));
            ei.setDepartment(sessionController.getDepartment());
            ei.setInstitution(sessionController.getInstitution());
            ei.setType(MessageType.ImageReadeRequest);
            ei.setCreatedAt(new Date());
            ei.setCreater(getSessionController().getLoggedUser());
            ei.setComments(comments);
            messageFacade.create(ei);
            UtilityController.addSuccessMessage("Image Added");
            comments = "";
        } catch (IOException e) {
            System.out.println("Error " + e.getMessage());
        }

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

    public WebUserFacade getWebUserFacade() {
        return webUserFacade;
    }

    public MessageFacade getMessageFacade() {
        return messageFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public WebUserController getWebUserController() {
        return webUserController;
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

    /**
     * Getters & Setters
     */
    public DepartmentController getDepartmentController() {
        return departmentController;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Institution getInstitution() {
        if (institution == null) {
            institution = getSessionController().getInstitution();
        }
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public List<Department> getDepartments() {
        return departmentController.getInstitutionDepatrments(institution);
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public Message getSelected() {
        return selected;
    }

    public void setSelected(Message selected) {
        this.selected = selected;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
    
    
    
}
