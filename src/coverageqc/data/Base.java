package coverageqc.data;

import java.util.HashSet;
import java.util.TreeMap;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author geoffrey.hughes.smith@gmail.com
 */
public class Base implements Comparable<Object> {
    
    @XmlAttribute
    public String chr;
    @XmlAttribute
    public long pos;
    @XmlTransient
    public HashSet<Long> readDepths = new HashSet<Long>();
    @XmlAttribute
    public String variant; // e.g., "A>G"
    @XmlAttribute
    public String variantText; // e.g., "A>G (804>34 reads)"
    @XmlAttribute
    public String quality;
    @XmlAttribute
    public String filter;
    
    @java.lang.Override
    public int compareTo(Object o) {
        return (new Long(this.pos)).compareTo(new Long(((Base)o).pos));
    }

    /**
     * @todo This is currently a "max" operation; this relates to how (I think)
     * indels are represented in the gVCF file, with multiple entries for the
     * same position.
     * @return The read depth that will be used for this position.
     */
    @XmlAttribute
    public long getTotalReadDepth() {
        long totalReadDepth = 0;
        for(Long readDepth : readDepths) {
            if(readDepth.longValue() > totalReadDepth) {
                totalReadDepth = readDepth.longValue();
            }
        }
        return totalReadDepth;
    }

    /**
     * 
     * @param vcfLine
     * @param bases
     * @return 
     */
    public static Base populate(String vcfLine, TreeMap<String, Base> bases) {
        String[] fields = vcfLine.split("\t");
        String chr = fields[0];
        long pos = Long.parseLong(fields[1]) - 0; // VCF is base 1, BED is base 0, I am using base 1
        // special handling for read depth:
        // [1] truncate "DP=" prefix
        // [2] maintaining set of read depths in Base class, since the same
        //     position can appear multiple time in the genomic VCF file;
        //     at this point I am taking the unique read depths for each
        //     position and maxing it - this might be risky
        long readDepth = Long.parseLong(fields[7].substring(3));
        String variant = null;
        if(fields[9].substring(0, 3).equals("0/1") || fields[9].substring(0, 3).equals("1/1")) {
            variant = fields[3] + ">" + fields[4];
        }
        Base base = bases.get(chr + "|" + Long.toString(pos));
        if(base == null) {
            base = new Base();
            base.chr = chr;
            base.pos = pos;
            bases.put(chr + "|" + Long.toString(pos), base);
        }
        base.readDepths.add(new Long(readDepth));
        if(variant != null) {
            base.variant = (base.variant == null ? "" : base.variant + ", ") + variant;
            int quality =  Math.round(Float.parseFloat(fields[5]));
            String filter = fields[6];
            int refReads = Integer.parseInt(fields[9].split(":")[2].split(",")[0]);
            int altReads = Integer.parseInt(fields[9].split(":")[2].split(",")[1]);
            base.variantText =
                (base.variantText == null ? pos + ": " : base.variantText + ", ")
                + ""
                + variant
                + " ("
                + "reads: " + refReads + ">" + altReads
                + ", filter: " + filter
                + ", qual: " + quality
                + ")";
        }
        return base;
    }
    
}
