package coverageqc.data;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author geoffrey.hughes.smith@gmail.com
 */
@XmlRootElement
public class Variant implements Comparable<Object> {

    @XmlAttribute
    public String pipeline;
    @XmlAttribute
    public String gene;
    @XmlAttribute
    public String variant;
    @XmlAttribute
    public Integer chr;
    @XmlAttribute
    public Long coordinate;
    @XmlAttribute
    public String type;
    @XmlAttribute
    public String genotype;
    @XmlAttribute
    public Float altVariantFreq;
    @XmlAttribute
    public String altVariantFreqFormatted;
    @XmlAttribute
    public Long readDepth;
    @XmlAttribute
    public Long altReadDepth;
    @XmlAttribute
    public String consequence;
    @XmlAttribute
    public String cosmicId;
    @XmlAttribute
    public String hgvsc;
    public HashMap<String, String> hgvscMap;
    @XmlAttribute
    public String hgvsp;
    public HashMap<String, String> hgvspMap;
    @XmlAttribute
    public String dbSnpIdPrefix;
    @XmlAttribute
    public String dbSnpIdSuffix;
    @XmlAttribute
    public String filters;
    @XmlAttribute
    public Float alleleFreqGlobalMinor;
    @XmlAttribute
    public String alleleFreqGlobalMinorFormatted;
    
    @java.lang.Override
    public int compareTo(Object o) {
        if(this.coordinate.compareTo(((Variant)o).coordinate) != 0) {
            return this.coordinate.compareTo(((Variant)o).coordinate);
        }
        else if(this.pipeline.compareTo(((Variant)o).pipeline) != 0) {
            return this.pipeline.compareTo(((Variant)o).pipeline);
        }
        else
            return(System.identityHashCode(this) > System.identityHashCode(o) ? 1 : -1);
    }
    
    public static Variant populate(String tsvHeadingLine, String tsvDataLine) {
        Variant variant = new Variant();
        variant.pipeline = "Illumina";
        String[] headingsArray = tsvHeadingLine.split("\t");
        HashMap<String, Integer> headings = new HashMap<String, Integer>();
        for(int x = 0; x < headingsArray.length; x++) {
            headings.put(headingsArray[x].substring(0, headingsArray[x].indexOf("_")), x);
        }
        String[] dataArray = tsvDataLine.split("\t");
        variant.gene = dataArray[headings.get("Gene").intValue()];
        variant.variant = dataArray[headings.get("Variant").intValue()];
        variant.chr = Integer.valueOf(dataArray[headings.get("Chr").intValue()] != null && !dataArray[headings.get("Chr").intValue()].isEmpty() ? dataArray[headings.get("Chr").intValue()] : null);
        // note: subtracting zero (0)
        variant.coordinate = Long.valueOf(dataArray[headings.get("Coordinate").intValue()] != null && !dataArray[headings.get("Coordinate").intValue()].isEmpty() ? dataArray[headings.get("Coordinate").intValue()] : null) - 0;
        variant.type = dataArray[headings.get("Type").intValue()];
        variant.genotype = dataArray[headings.get("Genotype").intValue()];
        variant.altVariantFreq = Float.valueOf(dataArray[headings.get("Alt Variant Freq").intValue()] != null && !dataArray[headings.get("Alt Variant Freq").intValue()].isEmpty() ? dataArray[headings.get("Alt Variant Freq").intValue()] : null);
        variant.readDepth = Long.valueOf(dataArray[headings.get("Read Depth").intValue()] != null && !dataArray[headings.get("Read Depth").intValue()].isEmpty() ? dataArray[headings.get("Read Depth").intValue()] : null);
        variant.altReadDepth = Long.valueOf(dataArray[headings.get("Alt Read Depth").intValue()] != null && !dataArray[headings.get("Alt Read Depth").intValue()].isEmpty() ? dataArray[headings.get("Alt Read Depth").intValue()] : null);
        variant.consequence = dataArray[headings.get("Consequence").intValue()];
        variant.cosmicId = dataArray[headings.get("COSMIC ID").intValue()];
        variant.filters = dataArray[headings.get("Filters").intValue()];
        // note: parsing out RefSeq IDs
        if(dataArray[headings.get("HGVSc")] != null) {
            Pattern pattern = Pattern.compile(".*:(.*)");
            Matcher matcher = pattern.matcher(dataArray[headings.get("HGVSc")]);
            if(matcher.find()) {
                variant.hgvsc = matcher.group(1);
            }
            else {
                variant.hgvsc = dataArray[headings.get("HGVSc")];
            }
        }
        // note: parsing out RefSeq IDs
        if(dataArray[headings.get("HGVSp")] != null) {
            Pattern pattern = Pattern.compile(".*:(.*)");
            Matcher matcher = pattern.matcher(dataArray[headings.get("HGVSp")]);
            if(matcher.find()) {
                variant.hgvsp = matcher.group(1);
            }
            else {
                variant.hgvsp    = dataArray[headings.get("HGVSp")];
            }
        }
        if(dataArray[headings.get("dbSNP ID")] != null) {
            Pattern pattern = Pattern.compile("([A-Za-z]*)([0-9]*)");
            Matcher matcher = pattern.matcher(dataArray[headings.get("dbSNP ID")]);
            if(matcher.find()) {
                variant.dbSnpIdPrefix = matcher.group(1);
                variant.dbSnpIdSuffix = matcher.group(2);
            }
            else {
                variant.dbSnpIdPrefix = dataArray[headings.get("dbSNP ID")];
            }
        }
        variant.alleleFreqGlobalMinor = Float.valueOf(dataArray[headings.get("Allele Freq Global Minor").intValue()] != null && !dataArray[headings.get("Allele Freq Global Minor").intValue()].isEmpty() ? dataArray[headings.get("Allele Freq Global Minor").intValue()] : null);
        if(variant.altVariantFreq != null) {
            variant.altVariantFreqFormatted = String.format("%.2f", variant.altVariantFreq);
        }
        if(variant.alleleFreqGlobalMinor != null) {
            variant.alleleFreqGlobalMinorFormatted = String.format("%.2f", variant.alleleFreqGlobalMinor);
        }
        return variant;
    }
    
    public static Variant populateNewman(String tsvHeadingLine, String tsvDataLine, String pipeline) {
        Variant variant = new Variant();
        variant.pipeline = pipeline;
        String[] headingsArray = tsvHeadingLine.split("\t");
        HashMap<String, Integer> headings = new HashMap<String, Integer>();
        for(int x = 0; x < headingsArray.length; x++) {
            headings.put(headingsArray[x], x);
        }
        String[] dataArray = tsvDataLine.split("\t");
        variant.gene = dataArray[headings.get("Gene:RefGene").intValue()];
        //variant.variant = dataArray[headings.get("Variant").intValue()];
        variant.chr = Integer.valueOf(dataArray[headings.get("Chromosome").intValue()] != null && !dataArray[headings.get("Chromosome").intValue()].isEmpty() ? dataArray[headings.get("Chromosome").intValue()].substring(3) : null);
        // note: subtracting zero (0)
        variant.coordinate = Long.valueOf(dataArray[headings.get("Start Position").intValue()] != null && !dataArray[headings.get("Start Position").intValue()].isEmpty() ? dataArray[headings.get("Start Position").intValue()] : null) - 0;
        //variant.type = dataArray[headings.get("Type").intValue()];
        variant.genotype = dataArray[headings.get("Genotype").intValue()];
        variant.altVariantFreq = Float.valueOf(dataArray[headings.get("Variant allele frequency").intValue()] != null && !dataArray[headings.get("Variant allele frequency").intValue()].isEmpty() ? dataArray[headings.get("Variant allele frequency").intValue()] : null);
        if(variant.altVariantFreq != null) {
            variant.altVariantFreq = variant.altVariantFreq.floatValue() * 100;
        }
        //variant.readDepth = Long.valueOf(dataArray[headings.get("Read Depth").intValue()] != null && !dataArray[headings.get("Read Depth").intValue()].isEmpty() ? dataArray[headings.get("Read Depth").intValue()] : null);
        //variant.altReadDepth = Long.valueOf(dataArray[headings.get("Alt Read Depth").intValue()] != null && !dataArray[headings.get("Alt Read Depth").intValue()].isEmpty() ? dataArray[headings.get("Alt Read Depth").intValue()] : null);
        variant.consequence = dataArray[headings.get("Amino Acid Change:RefGene").intValue()];
        //variant.cosmicId = dataArray[headings.get("COSMIC ID").intValue()];
        //variant.filters = dataArray[headings.get("Filters").intValue()];
        // note: parsing out RefSeq IDs
        {
            String columnHeading = " Occurrences in Exome Variant Server 6500 samples (http://evs.gs.washington.edu/EVS/)";
            if(dataArray[headings.get(columnHeading)] != null) {
                variant.hgvscMap = new HashMap<String, String>();
                Pattern pattern = Pattern.compile(".*:(.*):.*:(.*):.*");
                String[] values = dataArray[headings.get(columnHeading)].split(",");
                for(int x = 0; x < values.length; x++) {
                    Matcher matcher = pattern.matcher(values[x]);
                    if(matcher.find()) {
                        variant.hgvscMap.put(matcher.group(1), matcher.group(2));
                    }
                }
            }
        }
        // note: parsing out RefSeq IDs
        {
            String columnHeading = " Occurrences in Exome Variant Server 6500 samples (http://evs.gs.washington.edu/EVS/)";
            if(dataArray[headings.get(columnHeading)] != null) {
                variant.hgvspMap = new HashMap<String, String>();
                Pattern pattern = Pattern.compile(".*:(.*):.*:.*:(.*)");
                String[] values = dataArray[headings.get(columnHeading)].split(",");
                for(int x = 0; x < values.length; x++) {
                    Matcher matcher = pattern.matcher(values[x]);
                    if(matcher.find()) {
                        variant.hgvspMap.put(matcher.group(1), matcher.group(2));
                    }
                }
            }
        }
        if(dataArray[headings.get("SIFT")] != null) {
            Pattern pattern = Pattern.compile("([A-Za-z]*)([0-9]*)");
            Matcher matcher = pattern.matcher(dataArray[headings.get("SIFT")]);
            if(matcher.find()) {
                variant.dbSnpIdPrefix = matcher.group(1);
                variant.dbSnpIdSuffix = matcher.group(2);
            }
            else {
                variant.dbSnpIdPrefix = dataArray[headings.get("SIFT")];
            }
        }
        //variant.alleleFreqGlobalMinor = Float.valueOf(dataArray[headings.get("Allele Freq Global Minor").intValue()] != null && !dataArray[headings.get("Allele Freq Global Minor").intValue()].isEmpty() ? dataArray[headings.get("Allele Freq Global Minor").intValue()] : null);
        if(variant.altVariantFreq != null) {
            variant.altVariantFreqFormatted = String.format("%.2f", variant.altVariantFreq);
        }
        if(variant.alleleFreqGlobalMinor != null) {
            variant.alleleFreqGlobalMinorFormatted = String.format("%.2f", variant.alleleFreqGlobalMinor);
        }
        return variant;
    }

}
