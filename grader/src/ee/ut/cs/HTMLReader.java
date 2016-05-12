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

import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import ee.ut.cs.Decider;

public class HTMLReader {

    private static final Boolean debug = false;

	public static void parsePage (ArchiveRecord ar, List<String> domains) throws IOException {
    	ArchiveRecordHeader arh = ar.getHeader();
	
	if (debug) System.out.println("-------------------------------------------------------\n");
	
	//Clean content-type
	String type = arh.getHeaderValue("Content-Type").toString().split(";")[0]; 
	
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
	
	Pattern pat = Pattern.compile("(https?:\\/\\/)?([^\\/]*)(\\/.*)?");
	Matcher mat = pat.matcher(url);
	String domainUrl = null;
	String pageUrl = null;
	if (mat.find()) {
		domainUrl = mat.group(2);
		pageUrl = mat.group(3);
		if (debug) System.out.println("Url is " + url);
		if (debug) System.out.println("Matcher groups: '" + domainUrl + "'  and  '" + pageUrl + "'");
		
		try {
		    Boolean domainCheck = false;
		    //Check if domain is what we want
		    for (String str : domains) {
			if (str.contains(domainUrl))
			    domainCheck = true;
		    }

		    //If domain is NOT what we want, skip this page
		    if (!domainCheck) {
		        return;
		    }
		} catch (java.lang.NullPointerException e) {
		    e.printStackTrace();
		    System.out.println("Domains: " + domains);
		    System.out.println("Something went wrong with domain check, skipping cycle...\n");
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
	
	//Seems that type was okay, read data into a string
	int a;
	String html = "";
	String line = "";
	Boolean find = false;
	Boolean write = false;
	//We only give lines after the Content-Type line for auditing
	//so we start writing to file only after write is true
	//Write to a TempFile
	while ((a = ar.read()) != -1) {
	    line += (char) a;
	    //If we found content type, it fit and and now we find a <
	    //assume it is the start of html
	    if ((char) a == '<' && find)
		write = true;
	    if ((char) a == '\n') {
		if (write) {
		    html += line;		    
		//Yes, there can be this string later in the html but this does not
		//concern us anymore, we aren't cheking that by that time
		} else if (line.contains("Content-Type:")) {
		    if (line.contains("text/html")) {
			//JACKPOT! This is the content we want!
			//Now find a "<" which we will take as a start of html
			find = true;
			line = "";
		    //If there is a Content-Type: but it isn't text/html
		    //it is not the type we are looking for so skip
		    } else return;
		}
		//Empty variable so next line can be read in
		line = "";
	    }
	}
			
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
	try {
	    Boolean result = new Decider().parse(tempFile.getAbsolutePath(), warcDate, domainUrl, pageUrl, warcName, warcAddress);
	    tempFile.delete();
	} catch (Exception e) {
	    
	}
	if (debug) System.out.println();
    }
}
