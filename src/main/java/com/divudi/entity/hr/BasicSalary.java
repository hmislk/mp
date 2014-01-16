/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.hr;

import com.divudi.data.hr.PaysheetComponentType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

/**
 *
 * @author Buddhika
 */
@Entity
@Inheritance
public class BasicSalary extends PaysheetComponent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public PaysheetComponentType getComponentType() {
        return PaysheetComponentType.BasicSalary;
    }

    @Override
    public void setComponentType(PaysheetComponentType componentType) {
        this.componentType = PaysheetComponentType.BasicSalary;
    }
}
