package ee.ut.cs;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.apache.commons.io.FileUtils;

public class DomainChecker {
    private List<String> domains;
    public DomainChecker () throws IOException {
	File f = new File("data/domains.csv");
	this.domains = FileUtils.readLines(f, "UTF-8");
    }
    
    public List<String> getAsList() {
	return this.domains;
    }

    public boolean check(String domain) {
	for (String str : this.domains) {
	    if (str.contains(domain))
		return true;
	}
	return false;
    }
}
