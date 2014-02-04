/*
 * MSc(Biomedical Informatics) Project
 *
 * Development and Implementation of a Web-based Combined Data Repository of
 Genealogical, Clinical, Laboratory and Genetic Data
 * and
 * a Set of Related Tools
 */
package com.divudi.bean.hr;

import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.hr.EmployeeStatus;
import com.divudi.data.hr.SalaryPaymentFrequency;
import com.divudi.data.hr.SalaryPaymentMethod;
import com.divudi.entity.Department;
import java.util.TimeZone;
import com.divudi.entity.Person;
import com.divudi.entity.Speciality;
import com.divudi.facade.StaffFacade;
import com.divudi.entity.Staff;
import com.divudi.entity.hr.StaffDesignation;
import com.divudi.entity.hr.StaffEmployeeStatus;
import com.divudi.entity.hr.StaffEmployment;
import com.divudi.entity.hr.StaffGrade;
import com.divudi.entity.hr.StaffStaffCategory;
import com.divudi.entity.hr.StaffWorkingDepartment;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.StaffEmploymentFacade;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Named;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.faces.view.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.apache.commons.io.IOUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Named
@RequestScoped
public class StaffImageController implements Serializable {

    StreamedContent scCircular;
    StreamedContent scCircularById;
    private UploadedFile file;
    @EJB
    StaffFacade staffFacade;

    private static final long serialVersionUID = 1L;

    public StaffFacade getStaffFacade() {
        return staffFacade;
    }

    public void setStaffFacade(StaffFacade staffFacade) {
        this.staffFacade = staffFacade;
    }

    @Inject
    StaffController staffController;

    public StaffController getStaffController() {
        return staffController;
    }

    public void setStaffController(StaffController staffController) {
        this.staffController = staffController;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String saveSignature() {
        InputStream in;
        if (file == null || "".equals(file.getFileName())) {
            return "";
        }
        if (file == null) {
            UtilityController.addErrorMessage("Please select an image");
            return "";
        }
        if (getStaffController().getCurrent().getId() == null || getStaffController().getCurrent().getId() == 0) {
            UtilityController.addErrorMessage("Please select staff member");
            return "";
        }
        System.out.println("file name is not null");
        System.out.println(file.getFileName());
        try {
            in = getFile().getInputstream();
            File f = new File(getStaffController().getCurrent().toString() + getStaffController().getCurrent().getFileType());
            FileOutputStream out = new FileOutputStream(f);

            //            OutputStream out = new FileOutputStream(new File(fileName));
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            in.close();
            out.flush();
            out.close();

            getStaffController().getCurrent().setRetireComments(f.getAbsolutePath());
            getStaffController().getCurrent().setFileName(file.getFileName());
            getStaffController().getCurrent().setFileType(file.getContentType());
            in = file.getInputstream();
            getStaffController().getCurrent().setBaImage(IOUtils.toByteArray(in));
            getStaffFacade().edit(getStaffController().getCurrent());
            return "";
        } catch (IOException e) {
            System.out.println("Error " + e.getMessage());
            return "";
        }

    }

    public StreamedContent getSignatureById() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        } else {
            // So, browser is requesting the image. Get ID value from actual request param.
            String id = context.getExternalContext().getRequestParameterMap().get("id");
            Long l;
            try {
                l = Long.valueOf(id);
            } catch (NumberFormatException e) {
                l = 0l;
            }
            Staff temImg = getStaffFacade().find(l);
            if (temImg != null) {
                return new DefaultStreamedContent(new ByteArrayInputStream(temImg.getBaImage()), temImg.getFileType());
            } else {
                return new DefaultStreamedContent();
            }
        }
    }

    public StreamedContent displaySignature(Long stfId) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            return new DefaultStreamedContent();
        }
        if (stfId == null) {
            return new DefaultStreamedContent();
        }

        Staff temStaff = getStaffFacade().findFirstBySQL("select s from Staff s where s.baImage != null and s.id = " + stfId);

        System.out.println("Printing");

        if (temStaff == null) {
            return new DefaultStreamedContent();
        } else {
            if (temStaff.getId() != null && temStaff.getBaImage() != null) {
                System.out.println(temStaff.getFileType());
                System.out.println(temStaff.getFileName());
                return new DefaultStreamedContent(new ByteArrayInputStream(temStaff.getBaImage()), temStaff.getFileType(), temStaff.getFileName());
            } else {
                return new DefaultStreamedContent();
            }
        }
    }

}
