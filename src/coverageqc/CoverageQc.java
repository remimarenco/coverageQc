package coverageqc;
import coverageqc.data.Amplicon;
import coverageqc.data.Base;
import coverageqc.data.Bin;
import coverageqc.data.DoNotCall;
import coverageqc.data.GeneExon;
import coverageqc.data.Variant;
import coverageqc.data.Vcf;
import java.awt.Color;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//end Tom addition

/**
 *
 * @author geoffrey.hughes.smith@gmail.com
 */
public class CoverageQc {

    private final static Logger LOGGER = Logger.getLogger(CoverageQc.class.getName()); 
    
    // Tom Addition
	//public String[][] doNotCallList = null;
         public static ArrayList<DoNotCall> donotcalls = new ArrayList<DoNotCall>();

	// End Tom Addition
        
    /**
     * @param args the command line arguments//Tom Addition is the
	 *            OpenXML4JException and InvalidFormatException
     */
    public static void main(String[] args) throws OpenXML4JException,
			InvalidFormatException, UnsupportedEncodingException, FileNotFoundException, IOException, JAXBException, TransformerConfigurationException, TransformerException {

        if(args.length == 0) {
            System.out.println("");
            //Tom Addition adding in argument doNotCall List
            System.out.println("USAGE: java -jar coverageQc.jar VCF-file-name exon-BED-file amplicon-BED-file doNotCall-xlsx-file(optional)");
            System.out.println("");
            //Tom Addition extended comment
             System.out.println("If BED and file names are not specified, the system will attempt to use the\n\"exons_ensembl.bed\" and \"amplicons.bed\" files located in the same directory\nas this JAR (or exe) file.  If excel file name is not specified will look under exe file directory");
            return;
        }
        
        File jarFile = new File(CoverageQc.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String jarFileDir = URLDecoder.decode(jarFile.getParent(), "UTF-8");

        final File vcfFile = new File(args[0]);

        File exonBedFile;
        File ampliconBedFile;
        //Tom addition
        File doNotCallFile = null;
                 ///
        if(args.length == 1) {
            // look for the BED files in the JAR file directory with names of
            // the form:
            //      xxx.YYYYMMDD.exons.bed
            //      xxx.YYYYMMDD.amplicons.bed
            //
            // ultimately use the ones that sort alphabetically highest
            File[] exonFiles = (new File(jarFileDir)).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) { return(pathname.getName().endsWith("exons.bed")); }
            });
            File[] ampliconFiles = (new File(URLDecoder.decode(jarFile.getParent(), "UTF-8"))).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) { return(pathname.getName().endsWith("amplicons.bed")); }
            });
            if(exonFiles.length == 0 || ampliconFiles.length == 0) {
                System.out.println("ERROR: Could not find exons.bed and/or amplicons.bed file(s) in " + URLDecoder.decode(jarFile.getParent(), "UTF-8"));
                return;
            }
            Arrays.sort(exonFiles, Collections.reverseOrder());
            exonBedFile = exonFiles[0];
            Arrays.sort(ampliconFiles, Collections.reverseOrder());
            ampliconBedFile = ampliconFiles[0];
            
             //Tom Addition
                        //assuming it is always in the jarfiledirectory
                       
                        File[] doNotCallFiles = (new File(jarFileDir))
					.listFiles(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							return (pathname.getName().endsWith("list.xlsx"));
						}
					});
                          
                        if (doNotCallFiles.length != 0)
                        {
                        Arrays.sort(doNotCallFiles, Collections.reverseOrder());
			doNotCallFile = doNotCallFiles[0];
                        }else
                        {
                            //there is no file in jarfile directory so null will be used
                        }
                       
                            
                        ///end Tom Addition
            
            
        }
        else {
            exonBedFile = new File(args[1]);
            ampliconBedFile = new File(args[2]);
             //Tom addition
                        if (args.length>=4)
                        {
                        doNotCallFile = new File(args[3]);
                        }
             ////
        }
        
        ///Tom Addition creating xslx file
       // XSSFWorkbook newWorkbookcopy = new XSSFWorkbook();
        
        
        ///
         
        
           // TOM ADDITION
		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Will populate doNotCall, reading XSLX file	
        //DoNotCallConverter(doNotCallFile);
        if (doNotCallFile!=null)
        {
            
        InputStream inp = new FileInputStream(doNotCallFile);
        // Get the workbook instance for XLS file
	XSSFWorkbook workbook = new XSSFWorkbook(inp);
        
         Iterator<XSSFSheet> sheetIterator = workbook.iterator();
         int typeOfCall = 1;
	int rowIndex;
        while (sheetIterator.hasNext())
                {
                    XSSFSheet sheet = sheetIterator.next();
                    Iterator<Row> rowIterator = sheet.iterator();
                    Row headerRow = null;
                    while (rowIterator.hasNext()) {
			
			Row row = rowIterator.next();
                        
			rowIndex = row.getRowNum();
                       // System.out.println("The row index is " +rowIndex);
			if (rowIndex == 0) {	
                            headerRow=row;
				continue;
			}else if(row.getCell(0)==null)
                        {
                            // if it is null then nothing is there
                            continue;
                        }
                        else if (row.getCell(0).getCellType() == 3) {
				// if the cell type is 3 that means it is a blank field
				continue;
			} 
			DoNotCall donotcall = DoNotCall.populate(headerRow, row, typeOfCall);
                        donotcalls.add(donotcall);
		}//end while rowiterator           
                    typeOfCall++; 
                    //system.out.println()
                }//end while sheetiterator     
        }//end if doNotCallFile is null
        
        
       
		
                
            // END TOM ADDITION//////////////////////////////////////////////////////////////////////////
                
        
        
        Reader vcfFileReader = new FileReader(vcfFile);
        BufferedReader vcfBufferedReader = new BufferedReader(vcfFileReader);

        Reader exonBedFileReader = new FileReader(exonBedFile);
        BufferedReader exonBedBufferedReader = new BufferedReader(exonBedFileReader);

        Reader ampliconBedFileReader = new FileReader(ampliconBedFile);
        BufferedReader ampliconBedBufferedReader = new BufferedReader(ampliconBedFileReader);

        // is there a variant file to fold into the report?
        File variantTsvFile = null;
        Integer variantTsvFileLineCount = null;
        Reader variantTsvFileReader = null;
        BufferedReader variantTsvBufferedReader = null;
        {
            File[] files = (new File(vcfFile.getCanonicalFile().getParent())).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return(
                        (
                            pathname.getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase() + ".")
                            && pathname.getName().toLowerCase().endsWith(".tsv")
                        )
                    );
                }
            });
            if(files.length == 1) {
                variantTsvFile = files[0];
                variantTsvFileReader = new FileReader(variantTsvFile);
                LineNumberReader lnr = new LineNumberReader(variantTsvFileReader);
                while(lnr.skip(Long.MAX_VALUE) > 0) {}
                variantTsvFileLineCount = lnr.getLineNumber();
                variantTsvFileReader.close();
                variantTsvFileReader = new FileReader(variantTsvFile);
                variantTsvBufferedReader = new BufferedReader(variantTsvFileReader);
            }
        }
        
        Vcf vcf = new Vcf();
        vcf.runDate = new Date();
        vcf.fileName = vcfFile.getCanonicalPath();
        vcf.exonBedFileName = exonBedFile.getCanonicalPath();
        //Tom addition
        //so the call file will be displayed in the report
        if (doNotCallFile!=null)
        {
            vcf.doNotCallFileName = doNotCallFile.getCanonicalPath();
        }else
        {
         vcf.doNotCallFileName = "NO DO NOT CALL FILE USED!";   
        }
        vcf.ampliconBedFileName = ampliconBedFile.getCanonicalPath();
        vcf.variantTsvFileName = (variantTsvFile != null ? variantTsvFile.getCanonicalPath() : null);
        vcf.variantTsvFileLineCount = variantTsvFileLineCount;
        
        // attempt to deduce the amplicon BED, patient BAM, and patient VCF
        // file names for this gVCF file, the assumption is that they are in
        // the same directory as the gVCF file
        {
            File[] files = (new File(vcfFile.getCanonicalFile().getParent())).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return(
                        (
                            pathname.getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase() + ".")
                            && (pathname.getName().toLowerCase().endsWith(".bam") || (pathname.getName().toLowerCase().endsWith(".vcf")))
                            && (pathname.getName().indexOf("genome") < 0)
                        )
                        ||
                        (
                            pathname.getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase() + "_")
                            && (pathname.getName().toLowerCase().endsWith(".bam") || (pathname.getName().toLowerCase().endsWith(".vcf")))
                            && (pathname.getName().indexOf("genome") < 0)
                        )
                    );
                }
            });
            for(File file : files) {
                vcf.bedBamVcfFileUrls.add(file.toURI().toURL());
            }
            vcf.bedBamVcfFileUrls.add(ampliconBedFile.toURI().toURL());
        }

        // read exon BED file
        String exonBedLine;
        while((exonBedLine = exonBedBufferedReader.readLine()) != null) {
            if(!exonBedLine.startsWith("chr")) {
                continue;
            }
            vcf.geneExons.add(GeneExon.populate(exonBedLine));
        }
        LOGGER.info(vcf.geneExons.size() + " regions read from exon BED file");
        exonBedFileReader.close();

        // read amplicon BED file
        String ampliconBedLine;
        while((ampliconBedLine = ampliconBedBufferedReader.readLine()) != null) {
            if(!ampliconBedLine.startsWith("chr")) {
                continue;
            }
            Amplicon amplicon = Amplicon.populate(ampliconBedLine);
            boolean foundGeneExon = false;
            for(GeneExon geneExon : vcf.findGeneExonsForChrRange(amplicon.chr, amplicon.startPos, amplicon.endPos)) {
                foundGeneExon = true;
                geneExon.amplicons.add(amplicon);
                if(amplicon.name.endsWith("_coding")) {
                    geneExon.codingRegion = amplicon;
                }
            }
            if(!foundGeneExon) {
                LOGGER.info("the following amplicon does not correspond to an exon region: " + ampliconBedLine);
            }
        }
        LOGGER.info(vcf.getAmpliconCount() + " regions read from amplicon BED file");
        ampliconBedFileReader.close();

        // read gVCF file
        String vcfLine;
        while((vcfLine = vcfBufferedReader.readLine()) != null) {
            if(vcfLine.startsWith("#")) {
                continue;
            }
            Base base = Base.populate(vcfLine, vcf.bases);
            boolean foundGeneExon = false;
            for(GeneExon geneExon : vcf.findGeneExonsForChrPos(base.chr, base.pos)) {
                foundGeneExon = true;
                geneExon.bases.put(new Long(base.pos), base);
            }
            if(!foundGeneExon) {
                LOGGER.info("the following base does not correspond to an exon region: " + vcfLine);
            }
        }
        LOGGER.info(vcf.getBaseCount() + " bases read from VCF file");
        LOGGER.info(vcf.getReadDepthCount() + " read depths read from VCF file");
        vcfFileReader.close();

        for(GeneExon geneExon : vcf.geneExons) {
            // if a position is absent, create it with read depth 0
            for(long pos = geneExon.startPos; pos <= geneExon.endPos; pos++) {
                if(vcf.bases.get(geneExon.chr + "|" + Long.toString(pos)) == null) {
                    Base base = new Base();
                    base.pos = pos;
                    base.readDepths.add(new Long(0));
                    vcf.bases.put(geneExon.chr + "|" + Long.toString(pos), base);
                }
                if(geneExon.bases.get(new Long(pos)) == null) {
                    geneExon.bases.put(new Long(pos), vcf.bases.get(geneExon.chr + "|" + Long.toString(pos)));
                }
            }
            // perform binning operation
            for(Base base : geneExon.bases.values()) {
                // don't count a base if it is outside of the coding region
                if((base.pos < geneExon.codingRegion.startPos) || (base.pos > geneExon.codingRegion.endPos)) {
                    continue;
                }
                for(Bin bin : geneExon.bins) {
                    if(base.getTotalReadDepth() >= bin.startCount && base.getTotalReadDepth() <= bin.endCount) {
                        bin.count++;
                        bin.pct = Math.round((100d * bin.count) / (Math.min(geneExon.endPos, geneExon.codingRegion.endPos) - Math.max(geneExon.startPos, geneExon.codingRegion.startPos) + 1));
                        break;
                    }
                }
            }
            
            // assign QC value
            if(geneExon.bins.get(0).count > 0 || geneExon.bins.get(1).count > 0) {
                geneExon.qc = "fail";
            }
            else if(geneExon.bins.get(2).count > 0) {
                geneExon.qc = "warn";
            }
            else if(geneExon.bins.get(3).count > 0) {
                geneExon.qc = "pass";
            }
        }

        // read variant file
          XSSFWorkbook workbookcopy = new XSSFWorkbook();
        if(variantTsvFile != null) {
            String variantTsvDataLine;
            String variantTsvHeadingLine = variantTsvBufferedReader.readLine();
            //Tom addition 
        //making excel copy of tsv
        //XSSFWorkbook workbookcopy = new XSSFWorkbook();
        XSSFSheet sheet = workbookcopy.createSheet("TSV copy");
        Row row = sheet.createRow(0);
        int rownum =1;
        String[] headingsArray = variantTsvHeadingLine.split("\t");
        for(int x = 0; x < headingsArray.length; x++) {
           Cell cell = row.createCell(x);
           cell.setCellValue(headingsArray[x]);
        }
        XSSFCellStyle cellStyle = (XSSFCellStyle)workbookcopy.createCellStyle();
        cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
       // XSSFColor myColor;
        //end Tom addition
            
            while((variantTsvDataLine = variantTsvBufferedReader.readLine()) != null) {
                // Tom addition adding in variable doNotCallList
                Variant variant = Variant.populate(variantTsvHeadingLine, variantTsvDataLine, donotcalls);
                //end Tom addition
                
                
                boolean foundGeneExon = false;
                for(GeneExon geneExon : vcf.findGeneExonsForChrPos("chr" + String.valueOf(variant.chr), variant.coordinate)) {
                    foundGeneExon = true;
                    geneExon.variants.add(variant);
                    // Tom Addition
                    if (variant.onTheDoNotCallList) {
			//myColor = new XSSFColor(Color.YELLOW);
                        cellStyle = workbookcopy.createCellStyle();
                        cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                        cellStyle.setFillForegroundColor(new XSSFColor(Color.YELLOW));
                        if(variant.typeOfDoNotCall.equals("Don't call, always"))
                        {
                        geneExon.containsDoNotCallAlways = true;
                        geneExon.donotcallVariantsAlways.add(variant);
                        }
			//geneExon.typeOfDoNotCall = variant.typeOfDoNotCall;
                        }else if(variant.consequence.equals("synonymous_variant"))
                    {
                        cellStyle = workbookcopy.createCellStyle();
                        cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                        cellStyle.setFillForegroundColor(new XSSFColor(Color.ORANGE));
                    }else
                        {
                         cellStyle = workbookcopy.createCellStyle();
                          cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                          cellStyle.setFillForegroundColor(new XSSFColor(Color.WHITE));  
                         // cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                         
                        }
                    //  geneExon.checkIfContainsOnlyDoNotCall();
                //adding TSV dataline to output excel file
                        row = sheet.createRow(rownum++);
                        String[] dataArray = variantTsvDataLine.split("\t");
                        for(int x = 0; x < dataArray.length; x++) {
                               Cell cell = row.createCell(x);
                              // XSSFCellStyle style = workbookcopy.createCellStyle();
                              // XSSFColor myColor = new XSSFColor(Color.RED);
                              // style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
                              // style.setFillForegroundColor(myColor);
                               
                            cell.setCellStyle(cellStyle);
                           cell.setCellValue(dataArray[x]);
                             }
                //end Tom addition
                    
                    
		     // end Tom Addition
                }
                if(!foundGeneExon) {
                    //LOGGER.info("the following variant does not correspond to an exon region: " + variantTsvDataLine);
                    System.out.println("ERROR: The following variant does not correspond to an exon region:\n" + variantTsvDataLine);
                    return;
                }
            }
            //LOGGER.info(vcf.getFilteredAnnotatedVariantCount() + " variants read from TSV file");
            variantTsvFileReader.close();
        }

        // write to XML
        //File xmlTempFile = File.createTempFile("tmp", ".xml");
        File xmlTempFile = new File(vcfFile.getCanonicalPath() + ".coverage_qc.xml");
        OutputStream xmlOutputStream = new FileOutputStream(xmlTempFile);
        JAXBContext jc = JAXBContext.newInstance("coverageqc.data");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        m.marshal(vcf, xmlOutputStream);        
        xmlOutputStream.close();
        LOGGER.info(xmlTempFile.getCanonicalPath() + " created");
        
        //Tom addition
        //write to xlsx
         File xslxTempFile = new File(variantTsvFile.getCanonicalPath() + ".coverage_qc.xlsx");
        OutputStream xslxOutputStream = new FileOutputStream(xslxTempFile);
         workbookcopy.write(xslxOutputStream);
    xslxOutputStream.close();
     LOGGER.info(xslxTempFile.getCanonicalPath() + " created");
        //end Tom addition

        // transform XML to HTML via XSLT
        Source xmlSource = new StreamSource(new FileInputStream(xmlTempFile.getCanonicalPath()));
        Source xslSource;
        if((new File(jarFileDir + "/coverageQc.xsl")).exists()) {
            xslSource = new StreamSource(new FileInputStream(jarFileDir + "/coverageQc.xsl"));
        }
        else {
            xslSource = new StreamSource(ClassLoader.getSystemResourceAsStream("coverageQc.xsl"));
        }
        Transformer trans = TransformerFactory.newInstance().newTransformer(xslSource);
        trans.transform(xmlSource, new StreamResult(vcfFile.getCanonicalPath() + ".coverage_qc.html"));
        LOGGER.info(vcfFile.getCanonicalPath() + ".coverage_qc.html created");
        
        // show HTML file in default browser
        File htmlFile = new File(vcfFile.getPath() + ".coverage_qc.html");
        Desktop.getDesktop().browse(htmlFile.toURI());
        
    }
   
   
}
