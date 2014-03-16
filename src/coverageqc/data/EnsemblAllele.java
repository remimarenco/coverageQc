package coverageqc.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnsemblAllele {
    @XmlAttribute
    @JsonProperty("hgvs_transcript")
    public String hgvsTranscript;
    @XmlAttribute
    @JsonProperty("hgvs_protein")
    public String hgvsProtein;
    @XmlAttribute
    @JsonProperty("consequence_terms")
    public String[] consequenceTerms;
    
    @XmlAttribute
    public String getHgvsTranscriptParsed() {
        return hgvsTranscript != null ? hgvsTranscript.split(":")[1] : null;
    }
    
    @XmlAttribute
    public String getHgvsProteinParsed() {
        return hgvsProtein != null ? hgvsProtein.split(":")[1] : null;
    }

}
