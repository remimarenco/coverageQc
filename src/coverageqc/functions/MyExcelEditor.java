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

/**
 *
 * @author Tom
 */
public class MyExcelEditor{
    
    
    public static void excelRowCreator(XSSFRow currentRow, Variant currentVariant, String tsvHeadingLine, String variantTsvDataLine, ArrayList<DoNotCall> donotcalls) {
      
        
        //list of possible cells to highlight
        Boolean filterCell=false;
        Boolean altVariantFreqCell = false;
        Boolean consequenceCell = false;
        Boolean alleleMinorCell = false;
        //XSSFCellStyle cellStyle = currentRow.getRowStyle();
        Cell cellInterp = currentRow.createCell(0);
       // XSSFCellStyle cellStyle = (XSSFCellStyle)cellInterp.getCellStyle();
      
        XSSFCellStyle cellStyle = getDefaultCellStyle(currentRow);
        
        String textOnInterp="";
          //cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
       // XSSFColor myColor;
         // Tom Addition
                    //if any of these the text on Interp is "Do Not Call"
                    if(currentVariant.consequence.equals("synonymous_variant") || 
                            currentVariant.altVariantFreq.floatValue()<=5 || currentVariant.typeOfDoNotCall.equals("Don't call, always"))
                    {
                        textOnInterp=textOnInterp+"Do Not Call -";
                       
                       // cellStyle.setFillForegroundColor(new XSSFColor(Color.GRAY));
                         cellStyle = getGrayCellStyle(currentRow);
                        
                         if (currentVariant.typeOfDoNotCall.equals("Don't call, always")) {
                             
                             if(textOnInterp.lastIndexOf("-") == textOnInterp.length()-1)
                             {
                            textOnInterp=textOnInterp+" on lab list of definative do-not-calls";
                              }else
                              {
                            textOnInterp=textOnInterp+", on lab list of definative do-not-calls";
                              }
                     
                        }
                         if(currentVariant.consequence.equals("synonymous_variant"))
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
                         cellStyle = getGrayCellStyle(currentRow);
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
                    
                        
                        
                       if(currentVariant.alleleFreqGlobalMinor.floatValue()>1)
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
                       
                    
                    
                    //now creating both of the columns
                    cellInterp.setCellStyle(cellStyle);
                    cellInterp.setCellValue(textOnInterp);
                    Cell cellInterp2 = currentRow.createCell(1);
                    cellInterp2.setCellStyle(cellStyle);
                    cellInterp2.setCellValue(textOnInterp);
                    
                    
                    //adding TSV dataline to output excel file
                       // row = sheet.createRow(rownum++);
                    String[] headingsArray = tsvHeadingLine.split("\t");
                       
                        HashMap<String, Integer> headings = new HashMap<String, Integer>();
                          for(int x = 0; x < headingsArray.length; x++) {
                           headings.put(headingsArray[x].substring(0, headingsArray[x].indexOf("_")), x);
                            }
                          String[] dataArray = variantTsvDataLine.split("\t");
        
                        for(int x = 0; x < dataArray.length; x++) {
                               Cell cell = currentRow.createCell(x+2);
                               
                               if(    (headingsArray[x].contains("Filters_")&&filterCell) 
                                       || (altVariantFreqCell && (headingsArray[x].contains("Alt Variant Freq_"))) 
                                       || (headingsArray[x].contains("Consequence_")&&consequenceCell)
                                       || (headingsArray[x].contains("Allele Freq Global Minor_")&&alleleMinorCell)  
                                       )
                               {
                                  // cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                                  // cellStyle.setFillForegroundColor(new XSSFColor(Color.GRAY));
                                   cellStyle = getGrayCellStyle(currentRow);
                               }else
                               {
                                   cellStyle = getDefaultCellStyle(currentRow);
                                   //cellStyle.setFillForegroundColor(new XSSFColor(Color.white));
                               }
                                 cell.setCellStyle(cellStyle);
                              // XSSFCellStyle style = workbookcopy.createCellStyle();
                              // XSSFColor myColor = new XSSFColor(Color.RED);
                              // style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                              // style.setFillForegroundColor(myColor);
                              
                                cell.setCellValue(dataArray[x]);
                                
                             }
                        
       
                    
                    
       // return currentRow;
    }
    
    public static void excelHeadingCreator(XSSFRow currentRow, String variantTsvHeadingLine)
    {
        
        XSSFCellStyle cellStyle = getDefaultCellStyle(currentRow);
      
        String[] headingsArray = variantTsvHeadingLine.split("\t");
        
        for(int x = 0; x < headingsArray.length+2; x++) {
           
           Cell cell = currentRow.createCell(x);
           
          // cell.setCellStyle(cellStyle);
           if(x==0)
           {
              
               cell.setCellStyle(cellStyle);
               cell.setCellValue("Fellow's Interpretation");
               
           }else if(x==1)
           {
               cell.setCellStyle(cellStyle);
               cell.setCellValue("Attending Pathologist Interpretation");
           }else 
           {
               if(headingsArray[x-2].contains("Gene_")
                       ||headingsArray[x-2].contains("Variant_")
                       ||headingsArray[x-2].contains("Chr_")
                       ||headingsArray[x-2].contains("Coordinate_")
                       ||headingsArray[x-2].contains("Type_")
                       ||headingsArray[x-2].contains("Genotype_")
                       ||headingsArray[x-2].contains("Coordinate_")
                       ||headingsArray[x-2].contains("Filters_"))
               {
                   cellStyle = getDefaultCellStyle(currentRow);
                   cellStyle.setRotation((short)0);   
               }else
               {
                cellStyle = getDefaultCellStyle(currentRow);   
                cellStyle.setRotation((short)90);      
               }
            
               cell.setCellStyle(cellStyle);
               cell.setCellValue(headingsArray[x-2]);
           }
           
           
        }
        
        
        //return currentRow;
    }
    
    
    public static void excelFormator(XSSFSheet currentSheet, File variantTsvFile, String tsvHeadingLine) throws IOException
    {
        String[] headingsArray = tsvHeadingLine.split("\t");
        HashMap<String, Integer> headings = new HashMap<String, Integer>();
        for(int x = 0; x < headingsArray.length; x++) {
            headings.put(headingsArray[x].substring(0, headingsArray[x].indexOf("_")), x);
        }
        
       
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
                        currentSheet.getWorkbook().setPrintArea(0, 1, currentSheet.getRow(0).getPhysicalNumberOfCells(), 0, currentSheet.getLastRowNum());
                   
                       for(int x=0; x<currentSheet.getRow(0).getPhysicalNumberOfCells();x++)
                       {
                           currentSheet.autoSizeColumn(x);
                       }
                        currentSheet.setColumnWidth(0, 10000);
                        currentSheet.setColumnWidth(1, 10000);
                       
                 currentSheet.setColumnWidth(headings.get("Consequence").intValue()+2, 3500);
                 currentSheet.setColumnHidden(headings.get("Classification").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Inherited From").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Allelic Depths").intValue()+2, true); 
                 currentSheet.setColumnHidden(headings.get("Custom Annotation").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Custom Gene Annotation").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Num Transcripts").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Transcript").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("cDNA Position").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("CDS Position").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Protein Position").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Amino Acids").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Codons").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("HGNC").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Transcript HGNC").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Canonical").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Sift").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("PolyPhen").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("ENSP").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("HGVSc").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("HGVSp").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("dbSNP ID").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Ancestral Allele").intValue()+2, true);
                 currentSheet.setColumnHidden(headings.get("Allele Freq").intValue()+2, true);
                 //everything beyond this is hidden
                 for (int x = headings.get("Global Minor Allele").intValue()+2;x <currentSheet.getRow(0).getPhysicalNumberOfCells();x++)
                 {
                     currentSheet.setColumnHidden(x, true);     
                 }
                 
            
                   
    }
    
    private static XSSFCellStyle getDefaultCellStyle(XSSFRow currentRow)
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
        cellStyle.setFillForegroundColor(new XSSFColor(Color.white));
       return cellStyle;
    }
    private static XSSFCellStyle getGrayCellStyle(XSSFRow currentRow)
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
        cellStyle.setFillForegroundColor(new XSSFColor(Color.gray));
       return cellStyle;
    }
}
