package coverageqc.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author geoffrey.hughes.smith@gmail.com
 */
@XmlRootElement
public class Bin {
    
    @XmlAttribute
    public long startCount;
    @XmlAttribute
    public long endCount;
    @XmlAttribute
    public String name;
    @XmlAttribute
    public long count; // count of reads in this bin
    @XmlAttribute
    public long pct; // percentage of reads in this bin

    /*
     * 
     */
    public Bin() {        
    }
    
    /**
     * 
     * @param startCount
     * @param endCount
     * @param name 
     */
    public Bin(long startCount, long endCount, String name) {
        this.startCount = startCount;
        this.endCount = endCount;
        this.name = name;
    }
    
}
