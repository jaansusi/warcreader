package ee.ut.cs;

import ee.ut.cs.WarcReader;
import ee.ut.cs.DomainChecker;

import java.util.List;

import java.io.File;
import java.io.IOException;

import java.lang.InterruptedException;
import java.lang.Process;

import org.apache.commons.io.FileUtils;

public class Main {
    public static void main(String[] args) {
	if (args.length != 1) {
	    System.out.println("Enter a warc manifest name as an argument");
	    System.exit(0);
	}
	File warcsFile = new File(args[0]);
	try {
	    DomainChecker dom = new DomainChecker();
	    List<String> warcs = FileUtils.readLines(warcsFile,"UTF-8");
	    for (String curWarc : warcs) {
		Process process = new ProcessBuilder("scp", "jaan@deepweb.ut.ee:/mnt/" + curWarc, System.getProperty("user.dir")+"/.").start();
		process.waitFor();
		WarcReader.readWarc(System.getProperty("user.dir") + "/" + curWarc, dom);
		FileUtils.writeStringToFile(new File("data/audited"), curWarc+"\n", "UTF-8", true);
		FileUtils.forceDelete(new File(curWarc));

	    }
	} catch (IOException | InterruptedException e) {
	    e.printStackTrace();
	    System.exit(0);
	}
	//Read in
	//Warcs, parsed_warcs
	//
	//Write to
	//Parsed_warcs, parsed_warcs_time, 
    }


}
