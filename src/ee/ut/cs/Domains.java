import java.io.File;
import java.io.IOException;

import java.util.List;

import org.apache.commons.io.FileUtils;



class Domains {
    private List<String> domains;
    public Domains () {
	File f = new File("domains.csv");
	try {
	    this.domains = FileUtils.readLines(f, "UTF-8");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public List<String> getDomains() {
	return this.domains;
    }
}
