package ee.ut.cs;

import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
//import org.archive.io.ArchiveReader;

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.Iterator;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import ee.ut.cs.HTMLReader;
import ee.ut.cs.DomainChecker;

public class WarcReader {
    public static void readWarc (String warcAddress, DomainChecker dom) {
	try {
	    Iterator<ArchiveRecord> archIt = WARCReaderFactory.get(new File(warcAddress)).iterator();
		while (archIt.hasNext()) {
		    ArchiveRecord ar = archIt.next();
		    HTMLReader.parsePage(ar, dom);
		}
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
