package coverageqc.data;

import coverageqc.data.EnsemblAllele;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnsemblTranscript {
    @XmlAttribute
    @JsonProperty("transcript_id")
    public String transcriptId;
    @XmlAttribute
    @JsonProperty("name")
    public String name;
    @XmlElement
    @JsonProperty("alleles")
    public EnsemblAllele[] alleles;
}
