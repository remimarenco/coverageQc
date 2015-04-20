package coverageqc;

import coverageqc.data.Amplicon;
import coverageqc.data.Base;
import coverageqc.data.Bin;
import coverageqc.data.DoNotCall;
import coverageqc.functions.MyExcelEditor;
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
import org.apache.poi.ss.usermodel.PrintOrientation;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author geoffrey.hughes.smith@gmail.com
 */
public class CoverageQc {
    // TODO : Check the type of the args => first should be a vcf,
    //          second should be a exon file, third a amplicon and last a doNotCall (xlsx)

    private final static Logger LOGGER = Logger.getLogger(CoverageQc.class.getName());

    public static ArrayList<DoNotCall> donotcalls = new ArrayList<DoNotCall>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws OpenXML4JException,
            InvalidFormatException, UnsupportedEncodingException, FileNotFoundException, IOException, JAXBException, TransformerConfigurationException, TransformerException {
        
        // If no parameters given
        if (args.length == 0) {
            System.out.println("");
            
            System.out.println("USAGE: java -jar coverageQc.jar VCF-file-name exon-BED-file amplicon-BED-file doNotCall-xlsx-file(optional)");
            System.out.println("");
            
            System.out.println("If BED and file names are not specified, the system will attempt to use the\n\"exons_ensembl.bed\" and \"amplicons.bed\" files located in the same directory\nas this JAR (or exe) file.  If excel file name is not specified will look under exe file directory");
            return;
        }
        
        // We retrieve the jarFile through its location
        File jarFile = new File(CoverageQc.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String jarFileDir = URLDecoder.decode(jarFile.getParent(), "UTF-8");
        
        // The vcfFile is the first argument
        final File vcfFile = new File(args[0]);
        
        // Init of the others files needed
        File exonBedFile;
        File ampliconBedFile;
        File doNotCallFile = null;
        
        // If only the vcf file is provided
        if (args.length == 1) {
            // Look for the BED files in the JAR file directory with names of
            // The form:
            //      xxx.YYYYMMDD.exons.bed
            //      xxx.YYYYMMDD.amplicons.bed
            //
            // Ultimately use the ones that sort alphabetically highest
            
            // We use the jarFile Directory to list files inside (or in the parent for amplicons)
            // We then create a filter to get the alphabetically highest file
            // which finished by exons.bed and amplicons.bed
            File[] exonFiles = (new File(jarFileDir)).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return (pathname.getName().endsWith("exons.bed"));
                }
            });
            File[] ampliconFiles = (new File(URLDecoder.decode(jarFile.getParent(), "UTF-8"))).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return (pathname.getName().endsWith("amplicons.bed"));
                }
            });
            
            // If we can't find exons or amplicons, then error
            if (exonFiles.length == 0 || ampliconFiles.length == 0) {
                System.out.println("ERROR: Could not find exons.bed and/or amplicons.bed file(s) in " + URLDecoder.decode(jarFile.getParent(), "UTF-8"));
                return;
            }
            
            // We take the last one of the exonFiles
            Arrays.sort(exonFiles, Collections.reverseOrder());
            exonBedFile = exonFiles[0];
            
            // We take the last one of the ampliconFiles
            Arrays.sort(ampliconFiles, Collections.reverseOrder());
            ampliconBedFile = ampliconFiles[0];

            // Assuming it is always in the jarfiledirectory
            // We try to suppose the location of the doNotCallFiles
            File[] doNotCallFiles = (new File(jarFileDir))
                    .listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return (pathname.getName().endsWith("list.xlsx"));
                        }
                    });
            
            // If the file is found,
            // Take the last one
            if (doNotCallFiles.length != 0) {
                Arrays.sort(doNotCallFiles, Collections.reverseOrder());
                doNotCallFile = doNotCallFiles[0];
            } else {
                // There is no file in jarfile directory so null will be used
            }

        } 
        // If there are more than the vcf file as arguments
        else {
            exonBedFile = new File(args[1]);
            ampliconBedFile = new File(args[2]);
            
            // If doNotCallFile exists in the args
            if (args.length >= 4) {
                doNotCallFile = new File(args[3]);
            }
        }

        // Creating xslx file
        // Will populate doNotCall, reading XSLX file
        if (doNotCallFile != null) {

            InputStream inp = new FileInputStream(doNotCallFile);
            // Get the workbook instance for XLS file
            XSSFWorkbook workbook = new XSSFWorkbook(inp);

            Iterator<XSSFSheet> sheetIterator = workbook.iterator();
            int typeOfCall = 1;
            int rowIndex;
            
            // Loop to parse the xlsx doNotCall
            while (sheetIterator.hasNext()) {
                XSSFSheet sheet = sheetIterator.next();
                Iterator<Row> rowIterator = sheet.iterator();
                Row headerRow = null;
                // Loop to get all the content of the row, in a DoNotCall Object
                while (rowIterator.hasNext()) {

                    Row row = rowIterator.next();

                    rowIndex = row.getRowNum();
                    
                    // We fetch the headerRow
                    // TODO : Get the headerRow out of the loop for optimization purpose
                    if (rowIndex == 0) {
                        headerRow = row;
                        continue;
                    } else if (row.getCell(0) == null) {
                        // If it is null then nothing is there
                        continue;
                    } else if (row.getCell(0).getCellType() == 3) {
                        // If the cell type is 3 that means it is a blank field
                        continue;
                    }
                    // We populate the doNotCall object with the headerRow, the actual row, 
                    // and the actual xlsx tabulation
                    DoNotCall donotcall = DoNotCall.populate(headerRow, row, typeOfCall);
                    donotcalls.add(donotcall);
                }
                typeOfCall++;
            }
        }
        
        // Creation of the 3 first arguments as files
        Reader vcfFileReader = new FileReader(vcfFile);
        BufferedReader vcfBufferedReader = new BufferedReader(vcfFileReader);

        Reader exonBedFileReader = new FileReader(exonBedFile);
        BufferedReader exonBedBufferedReader = new BufferedReader(exonBedFileReader);

        Reader ampliconBedFileReader = new FileReader(ampliconBedFile);
        BufferedReader ampliconBedBufferedReader = new BufferedReader(ampliconBedFileReader);

        // Is there a variant file to fold into the report?
        File variantTsvFile = null;
        Integer variantTsvFileLineCount = null;
        Reader variantTsvFileReader = null;
        BufferedReader variantTsvBufferedReader = null;
        
        // TODO: We assume the .tsv file is in the same folder as the vcf file,
        // Maybe we should pass it as an other argument
        {
            File[] files = (new File(vcfFile.getCanonicalFile().getParent())).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return ((pathname.getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase() + ".")
                            && pathname.getName().toLowerCase().endsWith(".tsv")));
                }
            });
            // If you found a tsv file
            if (files.length == 1) {
                variantTsvFile = files[0];
                variantTsvFileReader = new FileReader(variantTsvFile);
                
                // Get the number of line
                LineNumberReader lnr = new LineNumberReader(variantTsvFileReader);
                while (lnr.skip(Long.MAX_VALUE) > 0) {
                }
                variantTsvFileLineCount = lnr.getLineNumber();
                variantTsvFileReader.close();
                
                variantTsvFileReader = new FileReader(variantTsvFile);
                variantTsvBufferedReader = new BufferedReader(variantTsvFileReader);
            }
        }
        
        // Init of the VCF
        // TODO : Move it into the constructor of the VCF Class
        Vcf vcf = new Vcf();
        vcf.runDate = new Date();
        vcf.fileName = vcfFile.getCanonicalPath();
        vcf.exonBedFileName = exonBedFile.getCanonicalPath();
        // So the call file will be displayed in the report
        if (doNotCallFile != null) {
            vcf.doNotCallFileName = doNotCallFile.getCanonicalPath();
        } else {
            vcf.doNotCallFileName = "NO DO NOT CALL FILE USED!";
        }
        vcf.ampliconBedFileName = ampliconBedFile.getCanonicalPath();
        vcf.variantTsvFileName = (variantTsvFile != null ? variantTsvFile.getCanonicalPath() : null);
        vcf.variantTsvFileLineCount = variantTsvFileLineCount;

        // Attempt to deduce the amplicon BED, patient BAM, and patient VCF
        // file names for this gVCF file, the assumption is that they are in
        // the same directory as the gVCF file
        {
            // TODO: Separate the search of the different files
            File[] files = (new File(vcfFile.getCanonicalFile().getParent())).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    // We compare the name of the files in the folder where the vcfFile is located
                    // TODO: Stop using these assumptions inside the code, ask instead
                    return ((pathname.getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase() + ".")
                            && (pathname.getName().toLowerCase().endsWith(".bam") || (pathname.getName().toLowerCase().endsWith(".vcf")))
                            && (pathname.getName().indexOf("genome") < 0))
                            || (pathname.getName().toLowerCase().startsWith(vcfFile.getName().substring(0, vcfFile.getName().indexOf(".")).toLowerCase() + "_")
                            && (pathname.getName().toLowerCase().endsWith(".bam") || (pathname.getName().toLowerCase().endsWith(".vcf")))
                            && (pathname.getName().indexOf("genome") < 0)));
                }
            });
            // We add all the bam files found
            for (File file : files) {
                vcf.bedBamVcfFileUrls.add(file.toURI().toURL());
            }
            // We add the amplicon file to the vcf object
            vcf.bedBamVcfFileUrls.add(ampliconBedFile.toURI().toURL());
        }

        // Read exon BED file
        String exonBedLine;
        // We only read the lines which begins with "chr"
        while ((exonBedLine = exonBedBufferedReader.readLine()) != null) {
            if (!exonBedLine.startsWith("chr")) {
                continue;
            }
            // We add the gene exon properties into the vcf
            vcf.geneExons.add(GeneExon.populate(exonBedLine));
        }
        LOGGER.info(vcf.geneExons.size() + " regions read from exon BED file");
        exonBedFileReader.close();

        // Read amplicon BED file
        String ampliconBedLine;
        while ((ampliconBedLine = ampliconBedBufferedReader.readLine()) != null) {
            // We are only interested in "chr" beginning lines
            if (!ampliconBedLine.startsWith("chr")) {
                continue;
            }
            // We create the amplicon object with the line
            Amplicon amplicon = Amplicon.populate(ampliconBedLine);
            boolean foundGeneExon = false;
            // If the amplicon is on the gene exon
            // We add it into the geneExon object
            // And add the codingRegion param if amplicon name is finishing with "_coding"
            for (GeneExon geneExon : vcf.findGeneExonsForChrRange(amplicon.chr, amplicon.startPos, amplicon.endPos)) {
                foundGeneExon = true;
                geneExon.amplicons.add(amplicon);
                if (amplicon.name.endsWith("_coding")) {
                    geneExon.codingRegion = amplicon;
                }
            }
            if (!foundGeneExon) {
                LOGGER.info("the following amplicon does not correspond to an exon region: " + ampliconBedLine);
            }
        }
        LOGGER.info(vcf.getAmpliconCount() + " regions read from amplicon BED file");
        ampliconBedFileReader.close();

        // Read gVCF file
        String vcfLine;
        while ((vcfLine = vcfBufferedReader.readLine()) != null) {
            if (vcfLine.startsWith("#")) {
                continue;
            }
            Base base = Base.populate(vcfLine, vcf.bases);
            boolean foundGeneExon = false;
            for (GeneExon geneExon : vcf.findGeneExonsForChrPos(base.chr, base.pos)) {
                foundGeneExon = true;
                geneExon.bases.put(new Long(base.pos), base);
            }
            if (!foundGeneExon) {
                LOGGER.info("the following base does not correspond to an exon region: " + vcfLine);
            }
        }
        LOGGER.info(vcf.getBaseCount() + " bases read from VCF file");
        LOGGER.info(vcf.getReadDepthCount() + " read depths read from VCF file");
        vcfFileReader.close();

        for (GeneExon geneExon : vcf.geneExons) {
            // If a position is absent, create it with read depth 0
            for (long pos = geneExon.startPos; pos <= geneExon.endPos; pos++) {
                if (vcf.bases.get(geneExon.chr + "|" + Long.toString(pos)) == null) {
                    Base base = new Base();
                    base.pos = pos;
                    base.readDepths.add(new Long(0));
                    vcf.bases.put(geneExon.chr + "|" + Long.toString(pos), base);
                }
                if (geneExon.bases.get(new Long(pos)) == null) {
                    geneExon.bases.put(new Long(pos), vcf.bases.get(geneExon.chr + "|" + Long.toString(pos)));
                }
            }
            // Perform binning operation
            for (Base base : geneExon.bases.values()) {
                // Don't count a base if it is outside of the coding region
                if ((base.pos < geneExon.codingRegion.startPos) || (base.pos > geneExon.codingRegion.endPos)) {
                    continue;
                }
                for (Bin bin : geneExon.bins) {
                    if (base.getTotalReadDepth() >= bin.startCount && base.getTotalReadDepth() <= bin.endCount) {
                        // TODO: These operations (count and pct processes) would need to be owned by Bin,
                        // not by Main CoverageQC in the javascript version
                        bin.count++;
                        bin.pct = Math.round((100d * bin.count) / (Math.min(geneExon.endPos, geneExon.codingRegion.endPos) - Math.max(geneExon.startPos, geneExon.codingRegion.startPos) + 1));
                        break;
                    }
                }
            }

            // Assign QC value
            if (geneExon.bins.get(0).count > 0 || geneExon.bins.get(1).count > 0) {
                geneExon.qc = "fail";
            } else if (geneExon.bins.get(2).count > 0) {
                geneExon.qc = "warn";
            } else if (geneExon.bins.get(3).count > 0) {
                geneExon.qc = "pass";
            }
        }

        // Read variant file
        XSSFWorkbook workbookcopy = new XSSFWorkbook();
        if (variantTsvFile != null) {
            String variantTsvDataLine;
            String variantTsvHeadingLine = variantTsvBufferedReader.readLine();
            // Making excel copy of tsv
            
            XSSFSheet sheet = workbookcopy.createSheet("TSV copy");
            XSSFRow row = sheet.createRow(0);
            MyExcelEditor.excelHeadingCreator(row, variantTsvHeadingLine);

            int rownum = 1;

            while ((variantTsvDataLine = variantTsvBufferedReader.readLine()) != null) {
                Variant variant = Variant.populate(variantTsvHeadingLine, variantTsvDataLine, donotcalls);

                row = sheet.createRow(rownum++);
                MyExcelEditor.excelRowCreator(row, variant, variantTsvHeadingLine, variantTsvDataLine, donotcalls);

                boolean foundGeneExon = false;
                for (GeneExon geneExon : vcf.findGeneExonsForChrPos("chr" + String.valueOf(variant.chr), variant.coordinate)) {
                    foundGeneExon = true;
                    geneExon.variants.add(variant);

                    if (variant.onTheDoNotCallList) {

                        if (variant.typeOfDoNotCall.equals("Don't call, always")) {
                            geneExon.containsDoNotCallAlways = true;
                            geneExon.donotcallVariantsAlways.add(variant);
                        }
                    }

                }
                if (!foundGeneExon) {
                    System.out.println("ERROR: The following variant does not correspond to an exon region:\n" + variantTsvDataLine);
                    return;
                }
            }

            // Adding page setup parameters per Dr. Carter, and column hiding options
            MyExcelEditor.excelFormator(sheet, variantTsvFile, variantTsvHeadingLine);

            variantTsvFileReader.close();
        }

        // Write to XML
        File xmlTempFile = new File(vcfFile.getCanonicalPath() + ".coverage_qc.xml");
        OutputStream xmlOutputStream = new FileOutputStream(xmlTempFile);
        JAXBContext jc = JAXBContext.newInstance("coverageqc.data");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
        m.marshal(vcf, xmlOutputStream);
        xmlOutputStream.close();
        LOGGER.info(xmlTempFile.getCanonicalPath() + " created");

        // Write to xlsx
        File xslxTempFile = new File(variantTsvFile.getCanonicalPath() + ".coverage_qc.xlsx");
        OutputStream xslxOutputStream = new FileOutputStream(xslxTempFile);
        workbookcopy.write(xslxOutputStream);
        xslxOutputStream.close();
        LOGGER.info(xslxTempFile.getCanonicalPath() + " created");

        // Transform XML to HTML via XSLT
        Source xmlSource = new StreamSource(new FileInputStream(xmlTempFile.getCanonicalPath()));
        Source xslSource;
        if ((new File(jarFileDir + "/coverageQc.xsl")).exists()) {
            xslSource = new StreamSource(new FileInputStream(jarFileDir + "/coverageQc.xsl"));
        } else {
            xslSource = new StreamSource(ClassLoader.getSystemResourceAsStream("coverageQc.xsl"));
        }
        Transformer trans = TransformerFactory.newInstance().newTransformer(xslSource);
        trans.transform(xmlSource, new StreamResult(vcfFile.getCanonicalPath() + ".coverage_qc.html"));
        LOGGER.info(vcfFile.getCanonicalPath() + ".coverage_qc.html created");

        // Show HTML file in default browser
        File htmlFile = new File(vcfFile.getPath() + ".coverage_qc.html");
        Desktop.getDesktop().browse(htmlFile.toURI());

    }

}
