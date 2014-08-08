/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coverageqc.functions;


import coverageqc.data.DoNotCall;
import coverageqc.data.Variant;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.PrintOrientation;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Tom
 */
public class MyExcelGenerator{
    
    public HashMap<Integer,Integer> tsvRearrangeConversion=null;
    public HashMap<String,Integer> originalHeadings=null;
    public String[] originalHeadingsArray=null;
    public XSSFWorkbook workbookcopy;
    String headingLine;
    ArrayList<String> sheetNames;
   
    public MyExcelGenerator ()
    {
        //sheetNames=givenSheetNames;
        workbookcopy = new XSSFWorkbook();
       // for(int i=0; i<givenSheetNames.size(); i++)
       // {
       //     workbookcopy.createSheet(sheetNames.get(i));
       // }        
        
    }
    
    public void excelRowCreator(int currentRowNum, String currentSheet, Variant currentVariant, String variantTsvDataLine, ArrayList<DoNotCall> donotcalls) {
      
        XSSFRow currentRow = this.workbookcopy.getSheet(currentSheet).createRow(currentRowNum);
        //list of possible cells to highlight
        Boolean filterCell=false;
        Boolean altVariantFreqCell = false;
        Boolean consequenceCell = false;
        Boolean alleleMinorCell = false;
        Boolean containsDoNotCall = false;
        //XSSFCellStyle cellStyle = currentRow.getRowStyle();
       // Cell cellInterp = currentRow.createCell(0);
       // XSSFCellStyle cellStyle = (XSSFCellStyle)cellInterp.getCellStyle();
      
        XSSFCellStyle cellStyle = getDefaultCellStyle(currentRow,Color.WHITE);
        
        String textOnInterp="";
          //cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
       // XSSFColor myColor;
         // Tom Addition
                    //if any of these the text on Interp is "Do Not Call"
                    if(currentVariant.consequence.contains("synonymous_variant") || 
                            currentVariant.altVariantFreq.floatValue()<=5 || currentVariant.typeOfDoNotCall.equals("Don't call, always"))
                    {
                        textOnInterp=textOnInterp+"Do Not Call -";
                       
                       // cellStyle.setFillForegroundColor(new XSSFColor(Color.GRAY));
                         cellStyle = getDefaultCellStyle(currentRow,Color.GRAY);
                         containsDoNotCall = true;
                        
                         if (currentVariant.typeOfDoNotCall.equals("Don't call, always")) {
                             
                             if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                             {
                            textOnInterp=textOnInterp+" on lab list of definative do-not-calls";
                              }else
                              {
                            textOnInterp=textOnInterp+", on lab list of definative do-not-calls";
                              }
                     
                        }
                         if(currentVariant.consequence.contains("synonymous_variant"))
                        {
                            consequenceCell = true;
                                //if the string size 
                                if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                {
                                    textOnInterp=textOnInterp+" synonymous variant";
                                }else
                                {
                                    textOnInterp=textOnInterp+", synonymous variant";
                                }
                        
                      
                        }     
                        if(currentVariant.altVariantFreq.floatValue()<=5)
                        {
                        altVariantFreqCell = true;
                             if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+" variant <5%";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", variant <5%";
                                 }
                       
                         }
                
                    }
                    //if any of these the text on Interp is "Warning"
                    if((currentVariant.onTheDoNotCallList &&(!currentVariant.typeOfDoNotCall.equals("Don't call, always"))) || currentVariant.alleleFreqGlobalMinor.floatValue()>1 || !(currentVariant.filters.equals("PASS")))
                    {
                        
                        //cellStyle.setFillForegroundColor(new XSSFColor(Color.GRAY));
                       // System.out.println(currentVariant.gene);
                        if (!containsDoNotCall)
                        {
                         cellStyle = getDefaultCellStyle(currentRow,Color.YELLOW);
                        }
                            if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+"WARNING -";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", WARNING -";
                                 }
                        
                        
                        
                        if((currentVariant.onTheDoNotCallList &&!(currentVariant.typeOfDoNotCall.equals("Don't call, always"))))
                        {
                     
                            if(currentVariant.typeOfDoNotCall.contains("In same location"))
                            {
                                if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+"in same location as lab list of do-not-calls";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", in same location as lab list of do-not-calls";
                                 }
                            }else
                            {
                                 if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+"on lab list of possible do-not-calls ";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", on lab list of possible do-not-calls - ";
                                 }
                            }
                            
                            
                            
                            }
                    
                        
                        
                       if(currentVariant.alleleFreqGlobalMinor>1)
                       {
                           alleleMinorCell = true;  
                           
                           if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+" MAF >1%";
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", MAF >1%";
                                 }
                           
                       }
                       
                       if (!(currentVariant.filters.equals("PASS")))
                       {
                           filterCell=true;
                                if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                                 {
                                        textOnInterp=textOnInterp+" Quality Filter = " + currentVariant.filters;
                                 }else
                                 {
                                        textOnInterp=textOnInterp+", Quality Filter = " + currentVariant.filters;
                                 }
                           
                           
                       }
                       
                      }//end if statement saying it is on Warning
                       
                    
                    
                    //now creating three columns of interps 
                    for(int x=0; x<=2; x++)
                    {
                        Cell cellInterp = currentRow.createCell(x);
                        cellInterp.setCellStyle(cellStyle);
                        cellInterp.setCellValue(textOnInterp);
                    }
                   
                    
                    //adding TSV dataline to output excel file
                       // row = sheet.createRow(rownum++);
                   // String[] headingsArray = tsvHeadingLine.split("\t");
                       
                      //  HashMap<String, Integer> headings = new HashMap<String, Integer>();
                      //    for(int x = 0; x < headingsArray.length; x++) {
                     //      headings.put(headingsArray[x].substring(0, headingsArray[x].indexOf("_")), x);
                     //       }
                          String[] dataArray = variantTsvDataLine.split("\t");
        
                        for(int x = 0; x < dataArray.length; x++) {
                               Cell cell = currentRow.createCell(x+3);
                               //System.out.println(x);
                               //System.out.println(this.tsvRearrangeConversion.get(x));
                               if(    (this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Filters_")&&filterCell) 
                                       || (this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Allele Freq Global Minor_")&&alleleMinorCell)  
                                       )
                               {
                                  // cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                                  // cellStyle.setFillForegroundColor(new XSSFColor(Color.GRAY));
                                   cellStyle = getDefaultCellStyle(currentRow,Color.YELLOW);
                               }else if( (this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Consequence_")&&consequenceCell) 
                                       || (altVariantFreqCell && (this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Alt Variant Freq_"))) )
                               {
                                    cellStyle = getDefaultCellStyle(currentRow,Color.GRAY);
                               }else
                               {
                                   cellStyle = getDefaultCellStyle(currentRow, Color.WHITE);
                                   //cellStyle.setFillForegroundColor(new XSSFColor(Color.white));
                               }
                                 cell.setCellStyle(cellStyle);
                              // XSSFCellStyle style = workbookcopy.createCellStyle();
                              // XSSFColor myColor = new XSSFColor(Color.RED);
                              // style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                              // style.setFillForegroundColor(myColor);
                              
                                cell.setCellValue(dataArray[this.tsvRearrangeConversion.get(x)]);
                                
                             }
                        
       
                    
                    
       // return currentRow;
    }
    
    public void excelHeadingCreator(String specifiedsheet, String headingLine)
    {
        XSSFSheet currentSheet = workbookcopy.createSheet(specifiedsheet);
        XSSFRow currentRow = currentSheet.createRow(0);
        
        XSSFCellStyle cellStyle;
      
       // String[] originalHeadingsArray = headingLine.split("\t");
        this.originalHeadingsArray=headingLine.split("\t");;
      
        HashMap<String, Integer> headings = new HashMap<String, Integer>();
        for(int x = 0; x < originalHeadingsArray.length; x++) {
            headings.put(originalHeadingsArray[x].substring(0, originalHeadingsArray[x].indexOf("_")), x);
        }
        this.setRearrangedHashMap(headings);
        this.originalHeadings=headings;
        
        //the headers for the first three columns
        for(int x = 0; x<3; x++)
        {
             Cell cell = currentRow.createCell(x);
           cellStyle = getDefaultCellStyle(currentRow,Color.WHITE);
            if(x==0 || x== 1)
           {
              
               cell.setCellStyle(cellStyle);
               if(x==0)
               {
                    cell.setCellValue("Tom's Interpretation");
                    //cell.setCellValue("Fellow1's Interpretation");
               }else
               {
               cell.setCellValue("Christina's Interpretation");
               //cell.setCellValue("Fellow2's Interpretation");
               }
               
           }else if(x==2)
           {
               cell.setCellStyle(cellStyle);
               cell.setCellValue("Attending Pathologist Interpretation");
           }
        }
        
        for(int x = 0; x < originalHeadingsArray.length; x++) {
           
            //plus three because have three header columns
           Cell cell = currentRow.createCell(x+3);
           cellStyle = getDefaultCellStyle(currentRow,Color.WHITE);
          // cell.setCellStyle(cellStyle);
               if(this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Gene_")
                       ||this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Variant_")
                       ||this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)].contains("Chr_"))
                     //  ||originalHeadingsArray[x].contains("Coordinate_")
                    //   ||originalHeadingsArray[x].contains("Type_"))
                     //  ||originalHeadingsArray[x-3].contains("Genotype_")
                      // ||originalHeadingsArray[x-3].contains("Coordinate_")
                     //  ||originalHeadingsArray[x-3].contains("Filters_"))
               {
                   cellStyle.setRotation((short)0);   
               }else
               {  
                cellStyle.setRotation((short)90);      
               }
            
              cell.setCellStyle(cellStyle);
              //cell.setCellValue(originalHeadingsArray[x-3]);
             // this.tsvRearrangeConversion.get(x)
              cell.setCellValue(this.originalHeadingsArray[this.tsvRearrangeConversion.get(x)]);
               //cell.setCellValue(headinsArray[headingsConversion.get(x-3)]);
           
           
           
        }//end for loop
        
        
        //return currentRow;
    }
    
    
    public void excelFormator(String sheetName, File variantTsvFile) throws IOException
    {
       // String[] headingsArray = tsvHeadingLine.split("\t");
       // HashMap<String, Integer> headings = new HashMap<String, Integer>();
       // for(int x = 0; x < headingsArray.length; x++) {
       //     headings.put(headingsArray[x].substring(0, headingsArray[x].indexOf("_")), x);
       // }
        XSSFSheet currentSheet = this.workbookcopy.getSheet(sheetName);
       
        XSSFPrintSetup printSetup = (XSSFPrintSetup)currentSheet.getPrintSetup();
        
             File xslxTempFile = new File(variantTsvFile.getCanonicalPath() + ".coverage_qc.xlsx");
                        currentSheet.getHeader().setLeft(xslxTempFile.getName());
                        currentSheet.getHeader().setRight("DO NOT DISCARD!!!  Keep with patient folder.");
                        //in Dr. Carter's VBA was set at points 18 which is .25 inches
                        currentSheet.setMargin(Sheet.RightMargin, .25);
                        currentSheet.setMargin(Sheet.LeftMargin, .25);
                      
                        printSetup.setOrientation(PrintOrientation.LANDSCAPE);
                        
                        
                        //NOTE: setFitWidth doesn't work for columns, ie can't setFitToPageColumns, this 
                        //is the best workaround I can do, it will only looked cramped for those with a lot of calls
                        printSetup.setFitWidth((short)1);
                        printSetup.setFitHeight((short)3);
                       currentSheet.setRepeatingRows(CellRangeAddress.valueOf("1"));
                        currentSheet.setFitToPage(true);
                        //making it by default not print the fellow's interp
                        currentSheet.getWorkbook().setPrintArea(0, 2, 20, 0, currentSheet.getLastRowNum());
                       
                        
                       for(int x=0; x<currentSheet.getRow(0).getPhysicalNumberOfCells();x++)
                       {
                           currentSheet.autoSizeColumn(x);
                           if (x>33)
                           {    
                           currentSheet.setColumnHidden(x, true);
                           }
                       }
                        currentSheet.setColumnWidth(0, 10000);
                        currentSheet.setColumnWidth(1, 10000);
                        currentSheet.setColumnWidth(2, 10000);
                     
                      
                               
//                 currentSheet.setColumnWidth(this.tsvRearrangeConversion.get(this.originalHeadings.get("Consequence"))+3, 3500);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Gene"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Variant"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Chr"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Coordinate"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Type"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Genotype"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Exonic"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Filters"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Quality"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("GQX"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Alt Variant Freq"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Read Depth"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Alt Read Depth"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Consequence"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Sift"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("PolyPhen"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Allele Freq Global Minor"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Classification"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Inherited From"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Allelic Depths"))+3, true); 
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Custom Annotation"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Custom Gene Annotation"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Num Transcripts"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Transcript"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("cDNA Position"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("CDS Position"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Protein Position"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Amino Acids"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Codons"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("HGNC"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Transcript HGNC"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Canonical"))+3, true);
//                 //currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Sift"))+3, false);
//                 //currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("PolyPhen"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ENSP"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("HGVSc"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("HGVSp"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("dbSNP ID"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Ancestral Allele"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Global Minor Allele"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Allele Freq Amr"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Allele Freq Asn"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Allele Freq Af"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Allele Freq Eur"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Allele Freq Evs"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("EVS Coverage"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("EVS Samples"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Conserved Sequence"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("COSMIC ID"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("COSMIC Wildtype"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("COSMIC Allele"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("COSMIC Gene"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("COSMIC Primary Site"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("COSMIC Histology"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar Accession"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar Ref"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar Alleles"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar Allele Type"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar Significance"))+3, false);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Alternate Alleles"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("Google Scholar"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("PubMed"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("UCSC Browser"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar RS"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar Disease Name"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar MedGen"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar OMIM"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar Orphanet"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar GeneReviews"))+3, true);
//                 currentSheet.setColumnHidden(this.tsvRearrangeConversion.get(this.originalHeadings.get("ClinVar SnoMedCt ID"))+3, true);
                
                 
            
                   
    }
    
    //If Color.White is specified there is no do not calls or warnings
    //If Color.Yellow is specified there is no do not calls but there are warnings
    //If Color.Gray is specifified there are do not calls and there may or may not be warnings
    private  XSSFCellStyle getDefaultCellStyle(XSSFRow currentRow, Color specifiedColor)
    {
       
        XSSFCellStyle cellStyle = currentRow.getSheet().getWorkbook().createCellStyle();
        
        cellStyle.setWrapText(true);
        cellStyle.setBorderBottom(cellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(cellStyle.BORDER_THIN);
        cellStyle.setBorderRight(cellStyle.BORDER_THIN);
        cellStyle.setBorderTop(cellStyle.BORDER_THIN);
        XSSFFont myFont = currentRow.getSheet().getWorkbook().createFont();
        myFont.setFontHeight(9);
        cellStyle.setFont(myFont);
        cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(new XSSFColor(specifiedColor));
       return cellStyle;
    }
    
    
    
    public void setRearrangedHashMap(HashMap<String, Integer> originalHeadings)
    {
        //illumina TSV columns end in BQ (26 *2) + 17; hence the hashmap should be 69 in length, TSV ends in BQ
        //HashMap<Integer,Integer> tsvRearrangeConversion;
        tsvRearrangeConversion = new HashMap<Integer,Integer>();
        int x=0;
        tsvRearrangeConversion.put(x++,originalHeadings.get("Gene"));//A
        tsvRearrangeConversion.put(x++,originalHeadings.get("Variant"));//B
        tsvRearrangeConversion.put(x++,originalHeadings.get("Chr"));//C
        tsvRearrangeConversion.put(x++,originalHeadings.get("cDNA Position"));//AC
        tsvRearrangeConversion.put(x++,originalHeadings.get("CDS Position"));//AD
        tsvRearrangeConversion.put(x++,originalHeadings.get("Protein Position"));//AE
        tsvRearrangeConversion.put(x++,originalHeadings.get("Coordinate"));//D
        tsvRearrangeConversion.put(x++,originalHeadings.get("Type"));//E
        tsvRearrangeConversion.put(x++,originalHeadings.get("Genotype"));//F
        tsvRearrangeConversion.put(x++,originalHeadings.get("Exonic"));//G
        tsvRearrangeConversion.put(x++,originalHeadings.get("Filters"));//H
        tsvRearrangeConversion.put(x++,originalHeadings.get("Quality"));//I
        tsvRearrangeConversion.put(x++,originalHeadings.get("GQX"));//J
        tsvRearrangeConversion.put(x++,originalHeadings.get("Alt Variant Freq"));//k
        tsvRearrangeConversion.put(x++,originalHeadings.get("Read Depth"));//L
        tsvRearrangeConversion.put(x++,originalHeadings.get("Alt Read Depth"));//M
        tsvRearrangeConversion.put(x++,originalHeadings.get("Consequence"));//N
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Global Minor"));//O
        tsvRearrangeConversion.put(x++,originalHeadings.get("Sift"));//P
        tsvRearrangeConversion.put(x++,originalHeadings.get("PolyPhen"));//Q
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC ID"));//R
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC Primary Site"));//S
        tsvRearrangeConversion.put(x++,originalHeadings.get("ENSP"));//T
        tsvRearrangeConversion.put(x++,originalHeadings.get("HGVSc"));//U
        tsvRearrangeConversion.put(x++,originalHeadings.get("HGVSp"));//V
        tsvRearrangeConversion.put(x++,originalHeadings.get("dbSNP ID"));//W
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Accession"));//X
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Ref"));//Y
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Alleles"));//Z
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Allele Type"));//AA
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Significance"));//AB
        tsvRearrangeConversion.put(x++,originalHeadings.get("Classification"));//AF
        tsvRearrangeConversion.put(x++,originalHeadings.get("Inherited From"));//AG
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allelic Depths"));//AH
        tsvRearrangeConversion.put(x++,originalHeadings.get("Custom Annotation"));//AI
        tsvRearrangeConversion.put(x++,originalHeadings.get("Custom Gene Annotation"));//AJ
        tsvRearrangeConversion.put(x++,originalHeadings.get("Num Transcripts"));//AK
        tsvRearrangeConversion.put(x++,originalHeadings.get("Transcript"));//AL
        tsvRearrangeConversion.put(x++,originalHeadings.get("Amino Acids"));//AM
        tsvRearrangeConversion.put(x++,originalHeadings.get("Codons"));//AN
        tsvRearrangeConversion.put(x++,originalHeadings.get("HGNC"));//AO
        tsvRearrangeConversion.put(x++,originalHeadings.get("Transcript HGNC"));//AP
        tsvRearrangeConversion.put(x++,originalHeadings.get("Canonical"));//AQ
        tsvRearrangeConversion.put(x++,originalHeadings.get("Ancestral Allele"));//AR
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq"));//AS
        tsvRearrangeConversion.put(x++,originalHeadings.get("Global Minor Allele"));//AT
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Amr"));//AU
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Asn"));//AV
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Af"));//AW
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Eur"));//AX
        tsvRearrangeConversion.put(x++,originalHeadings.get("Allele Freq Evs"));//AY
        tsvRearrangeConversion.put(x++,originalHeadings.get("EVS Coverage"));//AZ
        tsvRearrangeConversion.put(x++,originalHeadings.get("EVS Samples"));//BA
        tsvRearrangeConversion.put(x++,originalHeadings.get("Conserved Sequence"));//BB
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC Wildtype"));//BC
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC Allele"));//BD
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC Gene"));//BE
        tsvRearrangeConversion.put(x++,originalHeadings.get("COSMIC Histology"));//BF
        tsvRearrangeConversion.put(x++,originalHeadings.get("Alternate Alleles"));//BG
        tsvRearrangeConversion.put(x++,originalHeadings.get("Google Scholar"));//BH
        tsvRearrangeConversion.put(x++,originalHeadings.get("PubMed"));//BI
        tsvRearrangeConversion.put(x++,originalHeadings.get("UCSC Browser"));//BJ
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar RS"));//BK
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Disease Name"));//BL
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar MedGen"));//BM
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar OMIM"));//BN
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar Orphanet"));//BO
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar GeneReviews"));//BP
        tsvRearrangeConversion.put(x++,originalHeadings.get("ClinVar SnoMedCt ID"));//BQ
        
        
    }
    
    public XSSFWorkbook getWorkbook()
    {
        return this.workbookcopy;
    }
    
}
