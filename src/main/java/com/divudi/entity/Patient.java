/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Years;

/**
 *
 * @author buddhika
 */
@Entity
public class Patient implements Serializable {

    static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //Main Properties
    Long id;
    @ManyToOne
    Person person;
    //personaI dentification Number
    Integer pinNo;
    //healthdentification Number
    Integer hinNo;
    //Created Properties
    @ManyToOne
    WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date createdAt;
    //Retairing properties
    boolean retired;
    @ManyToOne
    WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date retiredAt;
    String retireComments;
    @Transient
    String age;
    @Transient
    Long ageInDays;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    byte[] baImage;
    String fileName;
    String fileType;
    String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getAgeInDays() {
        if (person == null) {
            System.out.println("patient is null");
            age = "";
            return 0l;
        }
        if (person.getDob() == null) {
            System.out.println("dob is null");
            age = "";
            return 0l;
        }
        LocalDate birthdate = new LocalDate(getPerson().getDob());
        LocalDate now = new LocalDate();
        Years ageInYears;
        Days ageDays;
        ageDays = Days.daysBetween(birthdate, now);
        System.err.println("Days : " + (long) ageDays.getDays());
        return (long) ageDays.getDays();
    }

    public void setAgeInDays(Long ageInDays) {
        this.ageInDays = ageInDays;
    }

    public String getAge() {
        LocalDate birthdate = new LocalDate(getPerson().getDob());
        LocalDate now = new LocalDate();
        Years ageInYears;
        ageInYears = Years.yearsBetween(birthdate, now);
        if (0 < ageInYears.getYears()) {
            return ageInYears.getYears() + " Years";
        } else {
            Months ageInMonths = Months.monthsBetween(birthdate, now);
            if (ageInMonths.getMonths() > 0) {
                return ageInMonths.getMonths() + " Months";
            } else {
                Days ageDays = Days.daysBetween(birthdate, now);
                return ageDays.getDays() + " Days";
            }
        }
    }

    public void setAge(String age) {
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Patient)) {
            return false;
        }
        Patient other = (Patient) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.Patient[ id=" + id + " ]";
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Integer getPinNo() {
        return pinNo;
    }

    public void setPinNo(Integer pinNo) {
        this.pinNo = pinNo;
    }

    public Integer getHinNo() {
        return hinNo;
    }

    public void setHinNo(Integer hinNo) {
        this.hinNo = hinNo;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public byte[] getBaImage() {
        return baImage;
    }

    public void setBaImage(byte[] baImage) {
        this.baImage = baImage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
