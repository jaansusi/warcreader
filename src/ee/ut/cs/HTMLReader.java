package ee.ut.cs;

import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.apache.commons.io.FileUtils;


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

import ee.ut.cs.Decider;
import ee.ut.cs.DomainChecker;

public class HTMLReader {
    public static void parsePage (ArchiveRecord ar, DomainChecker domains) throws IOException {
    	ArchiveRecordHeader arh = ar.getHeader();
	Boolean debug = false;
	if (debug) System.out.println("-------------------------------------------------------\n");
	//System.out.println(arh.getHeaderFields());
	//Clean content-type
	String type = arh.getHeaderValue("Content-Type").toString().split(";")[0]; 
	//System.out.println(type);
	//If type is not what we wanted, we don't even bother reading it
	if (!type.equals("application/http")) {
		if (debug) System.out.println("Wrong type: " + type);
		if (debug) System.out.println("Skipping cycle...\n");
		return;
	}
	
	
	if (debug) System.out.println("Type OK: " + type);
	String warcDate = arh.getDate();
	warcDate = warcDate.substring(0,10);
	String url = arh.getUrl();
	
	//Regex should look something like this
	//Group 1 is domain and group 2 is page url
	//(?:https?:\\/\\/)?((?:[a-zA-Z0-9]+\\.)+[a-zA-Z0-9]+)(?:\\/(.*))?
	Pattern pat = Pattern.compile("(https?:\\/\\/)?([^\\/]*)(\\/.*)?");
	Matcher mat = pat.matcher(url);
	String domainUrl = null;
	String pageUrl = null;
	if (mat.find()) {
		domainUrl = mat.group(2);
		pageUrl = mat.group(3);
		if (debug) System.out.println("Url is " + url);
		if (debug) System.out.println("Matcher groups: '" + domainUrl + "'  and  '" + pageUrl + "'");
		
		//If domain is NOT what we want, skip this page
		if (!domains.check(domainUrl)) {
		    //System.out.println(domainUrl);
		    return;
		}
		if (debug) System.out.println("Good URL: " + domainUrl);
	} else {
		System.out.println("Matcher didn't find anything from " + url);
		//Something went wrong with the regex matching, let's cancel
		//this cycle, no point of uploading without the URL-s
		if (debug) System.out.println("Skipping cycle...\n");
		return;
	}
		
	//Now that we know that the type is right and the domain url has
	//been found, check whether the domain is worth testing, is the 
	//domain a part of the list that we're grading
	
	//TO-DO Add domain testing
	
	//Seems that type was okay, read data into a string
	int a;
	String html = "";
	String line = "";
	Boolean write = false;
	//We only give lines after the Content-Type line for auditing
	//so we start writing to file only after write is true
	//Write to a TempFile
		while ((a = ar.read()) != -1) {
	    line += (char) a;
	    if ((char) a == '\n') {
		if (write) {
		    html += line;
		//Yes, there can be this string later in the html but this does not
		//concern us anymore, we aren't cheking that by that time
		} else if (line.contains("Content-Type:")) {
		    if (line.contains("text/html")) {
			//JACKPOT! This is the content we want!
			//Write all the next lines in this record to TempFile
			write = true;
			line = "";
		    //If there is a Content-Type: but it isn't text/html
		    //it is not the type we are looking for so skip
		    } else return;
		}
		//Empty variable so next line can be read in
		line = "";
	    }
	}
	//System.out.println(html);
			
	//Create a file to write to
	Integer rand;
        int min = 1;
	int max = 10000;
    	do {
	    rand = new Random().nextInt((max - min) + 1) + min;
       	} while (new File("TempFile-" + rand + ".html").exists());
	    File tempFile = new File("TempFile-" + rand + ".html");
	
	//Write to the created file
	FileUtils.write(tempFile, html, "UTF-8");
	//replaceAll removes everything before the file name
	String warcAddress = arh.getHeaderValue("reader-identifier").toString();
	String warcName = warcAddress.replaceAll(".*/","");
	//Call out Decider with the data
	if (debug) System.out.println("Warc: " + warcName);
	Boolean result = new Decider().parse(tempFile.getAbsolutePath(), warcDate, domainUrl, pageUrl, warcName, warcAddress);
	tempFile.delete();
	if (debug) System.out.println();
    }
}
