package coverageqc.data;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

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
    @XmlElement
    public ArrayList<EnsemblVariant> ensemblVariants = new ArrayList<EnsemblVariant>();
    
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
    public static Base populate(String vcfLine, TreeMap<String, Base> bases) throws InterruptedException {
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
        EnsemblVariantData ensemblVariantData = null;
        if(fields[9].substring(0, 3).equals("0/1") || fields[9].substring(0, 3).equals("1/1")) {
            variant = fields[3] + ">" + fields[4];
            ensemblVariantData = findEnsemblVariant(chr.substring(3) + ":" + pos + "-" + (pos + fields[3].length() - 1), fields[4]);
            Thread.sleep(200); // Ensembl web service throttling 6 req/sec
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
            if(ensemblVariantData != null && ensemblVariantData.variants != null) {
                for(EnsemblVariant ensemblVariant : ensemblVariantData.variants) {
                    base.ensemblVariants.add(ensemblVariant);
                }
            }
        }
        return base;
    }

    public static EnsemblVariantData findEnsemblVariant(String region, String allele) {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJsonProvider.class);
        Client client = Client.create(config);
        WebResource webResource = client.resource(UriBuilder.fromUri("http://beta.rest.ensembl.org/vep/human/" + region + "/" + allele + "/consequences").build());
        ClientResponse response = webResource
            .accept(javax.ws.rs.core.MediaType.APPLICATION_JSON)
            .get(ClientResponse.class);
        EnsemblVariantData variantData = response.getEntity(new GenericType<EnsemblVariantData>(EnsemblVariantData.class));
        return variantData;
    }

}
