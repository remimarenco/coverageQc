package coverageqc;
import coverageqc.data.Amplicon;
import coverageqc.data.Base;
import coverageqc.data.Bin;
import coverageqc.data.GeneExon;
import coverageqc.data.Variant;
import coverageqc.data.Vcf;
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

//Tom Addition to edit excel files
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
	public static String[][] doNotCallList;

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
             System.out.println("If BED and file names are not specified, the system will attempt to use the\n\"exons_ensembl.bed\" and \"amplicons.bed\" files located in the same directory\nas this JAR (or exe) file.  If excel file name is not specified will look in parent of exe directory then under \\Clinical Specimens.  If not found will look under exe file directory");
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
                        //very specific assumption, that the do Not Call List is in Clinical Specimens folder in the parent directory of the exe file, it will test this assumption and if the file exists it will use that file
                         String[] jarFileDirParts = jarFileDir.split("\\\\");
                        int jarFileLength = jarFileDirParts.length;
                        jarFileDirParts[jarFileLength-1]="CLINICAL SPECIMENS";
                        StringBuilder possibleExcelFileDirBuild = new StringBuilder();
                        
                        for (int i=0, il=jarFileDirParts.length; i< il; i++)
                        {
                            possibleExcelFileDirBuild.append(jarFileDirParts[i]);
                            possibleExcelFileDirBuild.append("\\");
                        }
                         possibleExcelFileDirBuild.append("Do not call list.xlsx");   
                        String possibleExcelFileDir = possibleExcelFileDirBuild.toString();
                        
                         File possibleDoNotCallFile = new File(possibleExcelFileDir);
                        if (possibleDoNotCallFile.exists())
                        {
                             doNotCallFile = new File(possibleExcelFileDir);
                        } else
                        {
                        File[] doNotCallFiles = (new File(jarFileDir))
					.listFiles(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							return (pathname.getName().endsWith("list"));
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
                       
                            
                        }///end Tom Addition
            
            
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
        
           // TOM ADDITION
		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
		DoNotCallConverter(doNotCallFile);
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
        if(variantTsvFile != null) {
            String variantTsvDataLine;
            String variantTsvHeadingLine = variantTsvBufferedReader.readLine();
            while((variantTsvDataLine = variantTsvBufferedReader.readLine()) != null) {
                // Tom addition adding in variable doNotCallList
                Variant variant = Variant.populate(variantTsvHeadingLine, variantTsvDataLine, doNotCallList);
                //end Tom addition
                boolean foundGeneExon = false;
                for(GeneExon geneExon : vcf.findGeneExonsForChrPos("chr" + String.valueOf(variant.chr), variant.coordinate)) {
                    foundGeneExon = true;
                    geneExon.variants.add(variant);
                    // Tom Addition
                    if (variant.onTheDoNotCallList) {
			geneExon.containsDoNotCall = true;
			geneExon.typeOfDoNotCall = variant.typeOfDoNotCall;
                        }
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
    
    // Tom Addition//////////////////////////////////////////////
	private static void DoNotCallConverter(File providedDoNotCallFile)
			throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		// return null;
		// var a2 = new Array(3);
            if (providedDoNotCallFile==null)
            {
                //since do not call is optional it will return if there is no Do Not Call File
                doNotCallList = null;
                return;
            }
            try
            {
		InputStream inp = new FileInputStream(providedDoNotCallFile);

		// Get the workbook instance for XLS file
		XSSFWorkbook workbook = new XSSFWorkbook(inp);

		// Get first sheet from the workbook
		XSSFSheet sheet = workbook.getSheetAt(0);

		Iterator<Row> rowIterator = sheet.iterator();
		// rowIterator.
		// sheet.getRow(1).getLastCellNum()
		int maxRows = sheet.getPhysicalNumberOfRows();
	
		doNotCallList = new String[maxRows][2];
		// int if 1 then always a do not call, if 2 then a do not call if
		// percentage is low, if 3 then undetermined significance
		int typeOfCall = 1;
		int rowIndex = 0;
		int columnNumber;
		int cellIndex;
		String[] headerArray;
		HashMap<String, Integer> headings = new HashMap<String, Integer>();

		while (rowIterator.hasNext()) {
			
			Row row = rowIterator.next();
			rowIndex = row.getRowNum();
			// columnNumber = row.getLastCellNum();
			// For each row, iterate through each columns
			Iterator<Cell> cellIterator = row.cellIterator();
			// Assumption, in DoNotCallList, when the first or second column is
			// empty then it is just a comment is when it is just a comment
			if (rowIndex == 0) {
				// this means we are at the header row and want to skip this row
				// should try and get the heading line
				// String[] headingsArray = tsvHeadingLine.split("\t");
				columnNumber = row.getLastCellNum();
				headerArray = new String[columnNumber];
				// XSSFRow headerRow = sheet.getRow(0);
				// headerRow.toString();

				// System.out.println(row.toString());
				// outputing to see if I can split and make into a string array
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					cellIndex = cell.getColumnIndex();
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_BOOLEAN:
						headerArray[cellIndex] = Boolean.toString(cell
								.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						headerArray[cellIndex] = Double.toString(cell
								.getNumericCellValue());
						break;
					case Cell.CELL_TYPE_STRING:
						headerArray[cellIndex] = cell.getStringCellValue();
						break;
					default:
						headerArray[cellIndex] = "";
					}

				}

				for (int x = 0; x < headerArray.length; x++) {
					headings.put(
							headerArray[x].substring(0,
									headerArray[x].indexOf("_")), x);
				}

				// typeOfCall++;
				continue;

			} else if (row.getCell(0).getCellType() == 3) {
				// if the cell type is 3 that means it is a blank field
				// a blank space, ignore,there will be blank spaces before a
				// change in type
				continue;
			} else if (row.getLastCellNum() == 1) {
				typeOfCall++;
				System.out.println(rowIndex);
				continue;

			} else if (row.getCell(1).getCellType() == 3) {
				// this means a comment row and DoNotCalls below this are a
				// separate type
				typeOfCall++;
				continue;
			}
			System.out.println(rowIndex);
			System.out.println(row.getCell(headings.get("HGVSc").intValue()));
			

			if (row.getCell(headings.get("HGVSc").intValue()) != null
					&& row.getCell(headings.get("HGVSc").intValue())
							.getCellType() == 1) {
				doNotCallList[rowIndex - 1][0] = row.getCell(
						headings.get("HGVSc").intValue()).getStringCellValue();
				// doNotCallListStringLinearName.add(row.getCell(headings.get("HGVSc").intValue()).getStringCellValue());
			} else if (row.getCell(headings.get("ENSP").intValue())
					.getCellType() == 1) {
				doNotCallList[rowIndex - 1][0] = row.getCell(
						headings.get("ENSP").intValue()).getStringCellValue();
				

			} else {
				throw new IllegalArgumentException(
						"Both HSVSc and ENSP is blank, can't characterize this doNotCall");
			}

			
			doNotCallList[rowIndex - 1][1] = Integer.toString(typeOfCall);

		}

		inp.close();
                
            }catch(Exception e)
            {
                //Doing this in case someone messes with Do Not Call .xslx file in an unexpected way, if that happens an html file will still be made, just variants won't be searched whether or not they are on the list
                 System.out.println("There is something work with the do Not Call list.xslx.  Variants will not be searched whether or not they are on this list");
                 doNotCallList = null;
            }
                    

		

	}
	// End Tom Addition////////////////////////////////////////////////////////////
    
    
}
