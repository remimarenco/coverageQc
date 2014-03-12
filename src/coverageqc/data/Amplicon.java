package coverageqc.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author geoffrey.hughes.smith@gmail.com
 */
@XmlRootElement
public class Amplicon implements Comparable<Object>{

    @XmlAttribute
    public String chr;
    @XmlAttribute
    public long startPos;
    @XmlAttribute
    public long endPos;
    @XmlAttribute
    public String name;

    @java.lang.Override
    public int compareTo(Object o) {
        if(this.chr.compareTo(((Amplicon)o).chr) != 0) {
            return this.chr.compareTo(((Amplicon)o).chr);
        }
        else {
            return (new Long(this.startPos)).compareTo(new Long(((Amplicon)o).startPos));
        }
    }

    /**
     * 
     * @param bedLine
     * @return 
     */
    public static Amplicon populate(String bedLine) {
        String[] fields = bedLine.split("\t");
        Amplicon amplicon = new Amplicon();
        amplicon.chr = fields[0];
        amplicon.startPos = Long.parseLong(fields[1]) + 1;
        amplicon.endPos = Long.parseLong(fields[2]) + 0;
        amplicon.name = fields[3];
        return amplicon;
    }
    
}
