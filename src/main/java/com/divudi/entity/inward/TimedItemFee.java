/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.inward;

import com.divudi.entity.Fee;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 *
 * @author buddhika
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class TimedItemFee extends Fee implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer durationHours = 0;
    private Integer overShootHours = 0;

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public Integer getOverShootHours() {
        return overShootHours;
    }

    public void setOverShootHours(Integer overShootHours) {
        this.overShootHours = overShootHours;
    }
}
