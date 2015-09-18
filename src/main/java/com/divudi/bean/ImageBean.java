/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Buddhika
 */
@Named
@RequestScoped
public class ImageBean {

    private StreamedContent image;

    @Produces
    @Named
    public StreamedContent getImage() {
        return new DefaultStreamedContent();
    }

    public ImageBean() {
    }
    
}
