package ee.ut.cs;

import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
//import org.archive.io.ArchiveReader;

import org.apache.commons.io.FileUtils;

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.lang.InterruptedException;
import java.lang.Process;

import java.util.Iterator;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import ee.ut.cs.HTMLReader;
import ee.ut.cs.DomainChecker;

public class WarcReader implements Runnable {
    private Thread t;
    private String threadName;
    private String curWarc;
    private DomainChecker domains;
    
    WarcReader (String curWarc, DomainChecker domains) {
	this.curWarc = curWarc;
	this.domains = domains;
    }

    public void run() {
	readWarc();
    }
    private void readWarc () {
	try {
	    System.out.println("Started parsing " + curWarc);
	    Process process = new ProcessBuilder("scp", "jaan@deepweb.ut.ee:/mnt/" + curWarc, ".").start();
	    process.waitFor();
	    Iterator<ArchiveRecord> archIt = WARCReaderFactory.get(new File("./" + curWarc)).iterator();
		while (archIt.hasNext()) {
			ArchiveRecord ar = archIt.next();
		    HTMLReader.parsePage(ar, domains);
		}
	    FileUtils.writeStringToFile(new File("data/audited"), curWarc+"\n", "UTF-8", true);
	    FileUtils.forceDelete(new File(curWarc));
	} catch (IOException | InterruptedException e) {
	    e.printStackTrace();
	}
    }
}
