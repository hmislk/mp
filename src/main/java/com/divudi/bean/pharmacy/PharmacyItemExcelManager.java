/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.bean.pharmacy;

import com.divudi.bean.InstitutionController;
import com.divudi.bean.SessionController;
import com.divudi.bean.UtilityController;
import com.divudi.data.BillType;
import com.divudi.data.InstitutionType;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Institution;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.Atm;
import com.divudi.entity.pharmacy.ItemsDistributors;
import com.divudi.entity.pharmacy.MeasurementUnit;
import com.divudi.entity.pharmacy.PharmaceuticalItem;
import com.divudi.entity.pharmacy.PharmaceuticalItemCategory;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.Vmpp;
import com.divudi.entity.pharmacy.Vtm;
import com.divudi.entity.pharmacy.VtmsVmps;
import com.divudi.facade.AmpFacade;
import com.divudi.facade.AmppFacade;
import com.divudi.facade.AtmFacade;
import com.divudi.facade.BillFacade;
import com.divudi.facade.ItemsDistributorsFacade;
import com.divudi.facade.MeasurementUnitFacade;
import com.divudi.facade.PharmaceuticalItemCategoryFacade;
import com.divudi.facade.PharmaceuticalItemFacade;
import com.divudi.facade.VmpFacade;
import com.divudi.facade.VmppFacade;
import com.divudi.facade.VtmFacade;
import com.divudi.facade.VtmsVmpsFacade;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Buddhika
 */
@Named(value = "pharmacyItemExcelManager")
@SessionScoped
public class PharmacyItemExcelManager implements Serializable {

    /**
     *
     * EJBs
     *
     */
    @EJB
    AtmFacade atmFacade;
    @EJB
    VtmFacade vtmFacade;
    @EJB
    AmpFacade ampFacade;
    @EJB
    VmpFacade vmpFacade;
    @EJB
    AmppFacade amppFacade;
    @EJB
    VmppFacade vmppFacade;
    @EJB
    VtmsVmpsFacade vtmInAmpFacade;
    @EJB
    MeasurementUnitFacade muFacade;
    @EJB
    PharmaceuticalItemCategoryFacade pharmaceuticalItemCategoryFacade;
    @EJB
    private PharmacyBean pharmacyBean;

    /**
     *
     * Values of Excel Columns
     *
     */
//        Category      0
//        Item Name     1
//        Code          2
//        Trade Name    3
//        Generic Name  4
//        Generic Product  5            
//        Strength      6
//        Strength Unit 7
//        Pack Size     8
//        Issue Unit    9
//        Pack Unit     10	
//        Manufacturer  11
//        Importer      12
    /**
     * Values of Excel Columns
     */
    int number = 0;
    int catCol = 1;
    int ampCol = 2;
    int codeCol = 3;
    int barcodeCol = 4;
    int vtmCol = 5;
    int strengthOfIssueUnitCol = 6;
    int strengthUnitCol = 7;
    int issueUnitsPerPackCol = 8;
    int issueUnitCol = 9;
    int packUnitCol = 10;
    int distributorCol = 11;
    int manufacturerCol = 12;
    int importerCol = 13;
    int startRow = 1;
    /**
     * DataModals
     *
     */
    List<Vtm> vtms;
    List<Amp> amps;
    List<Ampp> ampps;
    /**
     *
     * Uploading File
     *
     */
    private UploadedFile file;

    /**
     * Creates a new instance of DemographyExcelManager
     */
    public PharmacyItemExcelManager() {
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getDistributorCol() {
        return distributorCol;
    }

    public void setDistributorCol(int distributorCol) {
        this.distributorCol = distributorCol;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    @Inject
    private InstitutionController institutionController;

    @Inject
    SessionController sessionController;

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public void removeDuplicateAmpps() {
        List<Ampp> temAmpps = getAmppFacade().findAll(true);
        for (Ampp ampp : temAmpps) {
            for (Ampp dup : temAmpps) {
                if (ampp.getName().equals(dup.getName())) {
                    if (ampp.isRetired() == false && dup.isRetired() == false) {
                        dup.setRetired(true);
                        getAmppFacade().edit(dup);

                    }
                }
            }
        }
    }

    @EJB
    private BillFacade billFacade;

    public void resetGrnValue() {
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where (type(b)=:class) "
                + " and b.billType = :billType ";

        temMap.put("class", BilledBill.class);
        temMap.put("billType", BillType.PharmacyGrnBill);
        //temMap.put("dep", getSessionController().getDepartment());
        List<Bill> bills = getBillFacade().findBySQL(sql, temMap);

        for (Bill b : bills) {
            if (b.getNetTotal() > 0) {
                b.setNetTotal(0 - b.getNetTotal());
                b.setTotal(0 - b.getTotal());
                getBillFacade().edit(b);
            }
        }

        sql = "select b from Bill b where (type(b)=:class) "
                + " and b.billType = :billType ";

        temMap.put("class", CancelledBill.class);
        temMap.put("billType", BillType.PharmacyGrnBill);
        //temMap.put("dep", getSessionController().getDepartment());
        bills = getBillFacade().findBySQL(sql, temMap);

        for (Bill b : bills) {
            if (b.getNetTotal() < 0) {
                b.setNetTotal(0 - b.getNetTotal());
                b.setTotal(0 - b.getTotal());
                getBillFacade().edit(b);
            }
        }

    }

    public String importToExcel() {
        //System.out.println("importing to excel");
        String strCat;
        String strAmp;
        String strCode;
        String strBarcode;
        String strGenericName;
        String strStrength;
        String strStrengthUnit;
        String strPackSize;
        String strIssueUnit;
        String strPackUnit;
        String strDistributor;
        String strManufacturer;
        String strImporter;

        PharmaceuticalItemCategory cat;
        Vtm vtm;
        Atm atm;
        Vmp vmp;
        Amp amp;
        Ampp ampp;
        Vmpp vmpp;
        VtmsVmps vtmsvmps;
        MeasurementUnit issueUnit;
        MeasurementUnit strengthUnit;
        MeasurementUnit packUnit;
        double strengthUnitsPerIssueUnit;
        double issueUnitsPerPack;
        Institution distributor;
        Institution Manufacturer;
        Institution Importer;

        File inputWorkbook;
        Workbook w;
        Cell cell;
        InputStream in;
        UtilityController.addSuccessMessage(file.getFileName());
        try {
            UtilityController.addSuccessMessage(file.getFileName());
            in = file.getInputstream();
            File f;
            f = new File(Calendar.getInstance().getTimeInMillis() + file.getFileName());
            FileOutputStream out = new FileOutputStream(f);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            in.close();
            out.flush();
            out.close();

            inputWorkbook = new File(f.getAbsolutePath());

            UtilityController.addSuccessMessage("Excel File Opened");
            w = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet = w.getSheet(0);

            for (int i = startRow; i < sheet.getRows(); i++) {

                Map m = new HashMap();

                //Category
                cell = sheet.getCell(catCol, i);
                strCat = cell.getContents();
                //System.out.println("strCat is " + strCat);
                cat = getPharmacyBean().getPharmaceuticalCategoryByName(strCat);
                if (cat == null) {
                    continue;
                }
                //System.out.println("cat = " + cat.getName());

                //Strength Unit
                cell = sheet.getCell(strengthUnitCol, i);
                strStrengthUnit = cell.getContents();
                //System.out.println("strStrengthUnit is " + strengthUnitCol);
                strengthUnit = getPharmacyBean().getUnitByName(strStrengthUnit);
                if (strengthUnit == null) {
                    continue;
                }
                //System.out.println("strengthUnit = " + strengthUnit.getName());
                //Pack Unit
                cell = sheet.getCell(packUnitCol, i);
                strPackUnit = cell.getContents();
                //System.out.println("strPackUnit = " + strPackUnit);
                packUnit = getPharmacyBean().getUnitByName(strPackUnit);
                if (packUnit == null) {
                    continue;
                }
                //System.out.println("packUnit = " + packUnit.getName());
                //Issue Unit
                cell = sheet.getCell(issueUnitCol, i);
                strIssueUnit = cell.getContents();
                //System.out.println("strIssueUnit is " + strIssueUnit);
                issueUnit = getPharmacyBean().getUnitByName(strIssueUnit);
                if (issueUnit == null) {
                    continue;
                }
                //StrengthOfAnMeasurementUnit
                cell = sheet.getCell(strengthOfIssueUnitCol, i);
                strStrength = cell.getContents();
                //System.out.println("strStrength = " + strStrength);
                if (!strStrength.equals("")) {
                    try {
                        strengthUnitsPerIssueUnit = Double.parseDouble(strStrength);
                    } catch (NumberFormatException e) {
                        strengthUnitsPerIssueUnit = 0.0;
                    }
                } else {
                    strengthUnitsPerIssueUnit = 0.0;
                }

                //Issue Units Per Pack
                cell = sheet.getCell(issueUnitsPerPackCol, i);
                strPackSize = cell.getContents();
                //System.out.println("strPackSize = " + strPackSize);
                if (!strPackSize.equals("")) {
                    try {
                        issueUnitsPerPack = Double.parseDouble(strPackSize);
                    } catch (NumberFormatException e) {
                        issueUnitsPerPack = 0.0;
                    }
                } else {
                    issueUnitsPerPack = 0.0;
                }

                //Vtm
                cell = sheet.getCell(vtmCol, i);
                strGenericName = cell.getContents();
                //System.out.println("strGenericName = " + strGenericName);
                if (!strGenericName.equals("")) {
                    vtm = getPharmacyBean().getVtmByName(strGenericName);
                } else {
                    //System.out.println("vtm is null");
                    vtm = null;
                }

                //Vmp
                vmp = getPharmacyBean().getVmp(vtm, strengthUnitsPerIssueUnit, strengthUnit, cat);
                if (vmp == null) {
                    //System.out.println("vmp is null");
                    continue;
                }
                //System.out.println("vmp = " + vmp.getName());
                //Amp
                cell = sheet.getCell(ampCol, i);
                strAmp = cell.getContents();
                //System.out.println("strAmp = " + strAmp);
                m = new HashMap();
                m.put("v", vmp);
                m.put("n", strAmp);
                if (!strCat.equals("")) {
                    amp = ampFacade.findFirstBySQL("SELECT c FROM Amp c Where upper(c.name)=:n AND c.vmp=:v", m);
                    if (amp == null) {
                        amp = new Amp();
                        amp.setName(strAmp);
                        amp.setMeasurementUnit(strengthUnit);
                        amp.setDblValue(strengthUnitsPerIssueUnit);
                        amp.setCategory(cat);
                        amp.setVmp(vmp);
                        getAmpFacade().create(amp);
                    } else {
                        amp.setRetired(false);
                        getAmpFacade().edit(amp);
                    }
                } else {
                    amp = null;
                    //System.out.println("amp is null");
                }
                if (amp == null) {
                    continue;
                }
                //System.out.println("amp = " + amp.getName());
                //Ampp
                ampp = getPharmacyBean().getAmpp(amp, issueUnitsPerPack, packUnit);

                //Code
                cell = sheet.getCell(codeCol, i);
                strCode = cell.getContents();
                //System.out.println("strCode = " + strCode);
                amp.setCode(strCode);
                getAmpFacade().edit(amp);
                //Code
                cell = sheet.getCell(barcodeCol, i);
                strBarcode = cell.getContents();
                //System.out.println("strBarCode = " + strBarcode);
                amp.setCode(strBarcode);
                getAmpFacade().edit(amp);
                //Distributor
                cell = sheet.getCell(distributorCol, i);
                strDistributor = cell.getContents();
                distributor = getInstitutionController().getInstitutionByName(strDistributor, InstitutionType.Dealer);
                if (distributor != null) {
                    //System.out.println("distributor = " + distributor.getName());
                    ItemsDistributors id = new ItemsDistributors();
                    id.setInstitution(distributor);
                    id.setItem(amp);
                    id.setOrderNo(0);
                    getItemsDistributorsFacade().create(id);
                } else {
                    //System.out.println("distributor is null");
                }
            }

            UtilityController.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
            return "";
        } catch (IOException ex) {
            UtilityController.addErrorMessage(ex.getMessage());
            return "";
        } catch (BiffException e) {
            UtilityController.addErrorMessage(e.getMessage());
            return "";
        }
    }

    public String importToExcelBarcode() {
        //System.out.println("importing to excel");

        String strAmp;
        String strBarcode;

        PharmaceuticalItemCategory cat;

        Amp amp;
        File inputWorkbook;
        Workbook w;
        Cell cell;
        InputStream in;
        UtilityController.addSuccessMessage(file.getFileName());
        try {
            UtilityController.addSuccessMessage(file.getFileName());
            in = file.getInputstream();
            File f;
            f = new File(Calendar.getInstance().getTimeInMillis() + file.getFileName());
            FileOutputStream out = new FileOutputStream(f);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            in.close();
            out.flush();
            out.close();

            inputWorkbook = new File(f.getAbsolutePath());

            UtilityController.addSuccessMessage("Excel File Opened");
            w = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet = w.getSheet(0);

            for (int i = startRow; i < sheet.getRows(); i++) {

                Map m = new HashMap();

                //Amp
                cell = sheet.getCell(ampCol, i);
                strAmp = cell.getContents();
                //System.out.println("strAmp = " + strAmp);
                m = new HashMap();
                m.put("n", strAmp);

                amp = ampFacade.findFirstBySQL("SELECT c FROM Amp c Where upper(c.name)=:n", m);
                if (amp == null) {

                } else {
                    amp.setRetired(false);
                    getAmpFacade().edit(amp);
                }

                if (amp == null) {
                    continue;
                }

                //Code
                cell = sheet.getCell(codeCol, i);
                strBarcode = cell.getContents();
                amp.setCode(strBarcode);

                //Barcode
                cell = sheet.getCell(barcodeCol, i);
                strBarcode = cell.getContents();
                amp.setBarcode(strBarcode);

                getAmpFacade().edit(amp);

            }

            UtilityController.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
            return "";
        } catch (IOException ex) {
            UtilityController.addErrorMessage(ex.getMessage());
            return "";
        } catch (BiffException e) {
            UtilityController.addErrorMessage(e.getMessage());
            return "";
        }
    }

    public String importToExcelCategoriOnly() {
        //System.out.println("importing to excel");
        String strCat;
        String strAmp = null;

        PharmaceuticalItemCategory cat;
        Amp amp;

        File inputWorkbook;
        Workbook w;
        Cell cell;
        InputStream in;
        UtilityController.addSuccessMessage(file.getFileName());
        try {
            UtilityController.addSuccessMessage(file.getFileName());
            in = file.getInputstream();
            File f;
            f = new File(Calendar.getInstance().getTimeInMillis() + file.getFileName());
            FileOutputStream out = new FileOutputStream(f);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            in.close();
            out.flush();
            out.close();

            inputWorkbook = new File(f.getAbsolutePath());

            UtilityController.addSuccessMessage("Excel File Opened");
            w = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet = w.getSheet(0);

            for (int i = startRow; i < sheet.getRows(); i++) {

                Map m = new HashMap();

                //Category
                cell = sheet.getCell(catCol, i);
                strCat = cell.getContents();
                //System.out.println("strCat is " + strCat);
                cat = getPharmacyBean().getPharmaceuticalCategoryByName(strCat);
                if (cat == null) {
                    continue;
                }
                //System.out.println("cat = " + cat.getName());
                if (!strCat.equals("")) {
                    amp = ampFacade.findFirstBySQL("SELECT c FROM Amp c Where upper(c.name)=:n AND c.vmp=:v", m);
                    if (amp == null) {
                        amp = new Amp();
                        amp.setName(strAmp);
                        amp.setCategory(cat);
                        getAmpFacade().create(amp);
                    } else {
                        amp.setRetired(false);
                        amp.setCategory(cat);
                        getAmpFacade().edit(amp);
                    }
                } else {
                    amp = null;
                    //System.out.println("amp is null");
                }
                if (amp == null) {
                    continue;
                }
            }

            UtilityController.addSuccessMessage("Succesful. All the data in Excel File Impoted to the database");
            return "";
        } catch (IOException ex) {
            UtilityController.addErrorMessage(ex.getMessage());
            return "";
        } catch (BiffException e) {
            UtilityController.addErrorMessage(e.getMessage());
            return "";
        }
    }

    @EJB
    ItemsDistributorsFacade itemsDistributorsFacade;

    @EJB
    PharmaceuticalItemFacade pharmaceuticalItemFacade;

    public PharmaceuticalItemFacade getPharmaceuticalItemFacade() {
        return pharmaceuticalItemFacade;
    }

    public void setPharmaceuticalItemFacade(PharmaceuticalItemFacade pharmaceuticalItemFacade) {
        this.pharmaceuticalItemFacade = pharmaceuticalItemFacade;
    }

    public void removeAllPharmaceuticalItems() {
        String sql;
        sql = "select p from PharmaceuticalItem p";
        List<PharmaceuticalItem> pis = getPharmaceuticalItemFacade().findBySQL(sql);
        for (PharmaceuticalItem p : pis) {
            getPharmaceuticalItemFacade().remove(p);
        }
    }

    public ItemsDistributorsFacade getItemsDistributorsFacade() {
        return itemsDistributorsFacade;
    }

    public void setItemsDistributorsFacade(ItemsDistributorsFacade itemsDistributorsFacade) {
        this.itemsDistributorsFacade = itemsDistributorsFacade;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getAmpCol() {
        return ampCol;
    }

    public void setAmpCol(int ampCol) {
        this.ampCol = ampCol;
    }

    public AmpFacade getAmpFacade() {
        return ampFacade;
    }

    public void setAmpFacade(AmpFacade ampFacade) {
        this.ampFacade = ampFacade;
    }

    public AmppFacade getAmppFacade() {
        return amppFacade;
    }

    public void setAmppFacade(AmppFacade amppFacade) {
        this.amppFacade = amppFacade;
    }

    public AtmFacade getAtmFacade() {
        return atmFacade;
    }

    public void setAtmFacade(AtmFacade atmFacade) {
        this.atmFacade = atmFacade;
    }

    public int getMeasurementUnitCol() {
        return issueUnitCol;
    }

    public void setMeasurementUnitCol(int issueUnitCol) {
        this.issueUnitCol = issueUnitCol;
    }

    public int getMeasurementUnitsPerPackCol() {
        return issueUnitsPerPackCol;
    }

    public void setMeasurementUnitsPerPackCol(int issueUnitsPerPackCol) {
        this.issueUnitsPerPackCol = issueUnitsPerPackCol;
    }

    public int getCatCol() {
        return catCol;
    }

    public void setCatCol(int catCol) {
        this.catCol = catCol;
    }

    public VtmsVmpsFacade getVtmInAmpFacade() {
        return vtmInAmpFacade;
    }

    public void setVtmInAmpFacade(VtmsVmpsFacade vtmInAmpFacade) {
        this.vtmInAmpFacade = vtmInAmpFacade;
    }

    public MeasurementUnitFacade getMuFacade() {
        return muFacade;
    }

    public void setMuFacade(MeasurementUnitFacade muFacade) {
        this.muFacade = muFacade;
    }

    public int getIssueUnitCol() {
        return issueUnitCol;
    }

    public void setIssueUnitCol(int issueUnitCol) {
        this.issueUnitCol = issueUnitCol;
    }

    public int getStrengthUnitCol() {
        return strengthUnitCol;
    }

    public void setStrengthUnitCol(int strengthUnitCol) {
        this.strengthUnitCol = strengthUnitCol;
    }

    public int getIssueUnitsPerPackCol() {
        return issueUnitsPerPackCol;
    }

    public void setIssueUnitsPerPackCol(int issueUnitsPerPackCol) {
        this.issueUnitsPerPackCol = issueUnitsPerPackCol;
    }

    public int getPackUnitCol() {
        return packUnitCol;
    }

    public void setPackUnitCol(int packUnitCol) {
        this.packUnitCol = packUnitCol;
    }

    public PharmaceuticalItemCategoryFacade getPharmaceuticalItemCategoryFacade() {
        return pharmaceuticalItemCategoryFacade;
    }

    public void setPharmaceuticalItemCategoryFacade(PharmaceuticalItemCategoryFacade pharmaceuticalItemCategoryFacade) {
        this.pharmaceuticalItemCategoryFacade = pharmaceuticalItemCategoryFacade;
    }

    public int getStrengthOfIssueUnitCol() {
        return strengthOfIssueUnitCol;
    }

    public void setStrengthOfIssueUnitCol(int strengthOfIssueUnitCol) {
        this.strengthOfIssueUnitCol = strengthOfIssueUnitCol;
    }

    public int getMeasurmentUnitCol() {
        return strengthUnitCol;
    }

    public void setMeasurmentUnitCol(int strengthUnitCol) {
        this.strengthUnitCol = strengthUnitCol;
    }

    public VmpFacade getVmpFacade() {
        return vmpFacade;
    }

    public void setVmpFacade(VmpFacade vmpFacade) {
        this.vmpFacade = vmpFacade;
    }

    public VmppFacade getVmppFacade() {
        return vmppFacade;
    }

    public void setVmppFacade(VmppFacade vmppFacade) {
        this.vmppFacade = vmppFacade;
    }

    public int getVtmCol() {
        return vtmCol;
    }

    public void setVtmCol(int vtmCol) {
        this.vtmCol = vtmCol;
    }

    public VtmFacade getVtmFacade() {
        return vtmFacade;
    }

    public void setVtmFacade(VtmFacade vtmFacade) {
        this.vtmFacade = vtmFacade;
    }

    public VtmsVmpsFacade getVtmsVmpsFacade() {
        return vtmInAmpFacade;
    }

    public void setVtmsVmpsFacade(VtmsVmpsFacade vtmInAmpFacade) {
        this.vtmInAmpFacade = vtmInAmpFacade;
    }

    public List<Ampp> getAmpps() {
        return getAmppFacade().findAll();
    }

    public void setAmpps(List<Ampp> ampps) {
        this.ampps = ampps;
    }

    public List<Amp> getAmps() {
        return getAmpFacade().findAll();
    }

    public void setAmps(List<Amp> amps) {
        this.amps = amps;
    }

    public List<Vtm> getVtms() {
        return getVtmFacade().findAll();
    }

    public void setVtms(List<Vtm> vtms) {
        this.vtms = vtms;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public int getCodeCol() {
        return codeCol;
    }

    public void setCodeCol(int codeCol) {
        this.codeCol = codeCol;
    }

    public int getManufacturerCol() {
        return manufacturerCol;
    }

    public void setManufacturerCol(int manufacturerCol) {
        this.manufacturerCol = manufacturerCol;
    }

    public int getImporterCol() {
        return importerCol;
    }

    public void setImporterCol(int importerCol) {
        this.importerCol = importerCol;
    }

    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public void setInstitutionController(InstitutionController institutionController) {
        this.institutionController = institutionController;
    }

    public int getBarcodeCol() {
        return barcodeCol;
    }

    public void setBarcodeCol(int barcodeCol) {
        this.barcodeCol = barcodeCol;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

}
