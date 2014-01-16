/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.Barcode39;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Buddhika
 */
@Named(value = "barcodeController")
@RequestScoped
public class BarcodeController {

    /**
     * Creates a new instance of BarcodeController
     */
    public BarcodeController() {
    }

    public StreamedContent getBarcodeBy() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        } else {
            // So, browser is requesting the image. Get ID value from actual request param.
            String code = context.getExternalContext().getRequestParameterMap().get("code");
            return new DefaultStreamedContent(new ByteArrayInputStream(getBarcodeBytes(code)), "jpg");
        }
    }

    public byte[] getBarcodeBytes(String code) {
        Barcode39 code39 = new Barcode39();
        code39.setCode(code);
        code39.setFont(null);
        code39.setExtended(true);
        Image image = null;
        try {
            image = Image.getInstance(code39.createAwtImage(Color.BLACK, Color.WHITE), null);
        } catch (BadElementException | IOException ex) {
            Logger.getLogger(BarcodeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image.getRawData();
    }

}
