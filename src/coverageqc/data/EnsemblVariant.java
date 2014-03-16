package coverageqc.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnsemblVariant {
    
    @XmlTransient
    @JsonProperty("hgvs")
    public Map<String, String> hgvsMap;
    @XmlElement
    @JsonProperty("transcripts")
    public EnsemblTranscript[] transcripts;
    
    @XmlElement
    public Hgvs getHgvs() {
        Hgvs hgvs = new Hgvs();
        for(String allele : hgvsMap.keySet()) {
            hgvs.allele = allele;
            hgvs.transcript = hgvsMap.get(allele);
        }
        return hgvs;
    }
    
    @XmlRootElement
    public static class Hgvs {
        @XmlAttribute
        public String allele;
        @XmlAttribute
        public String transcript;
    }
    
}
