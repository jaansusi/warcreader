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

import ee.ut.cs.Decider;

public class HTMLReader {
 
	public void parsePage (ArchiveRecord ar) {
		ArchiveRecordHeader arh = ar.getHeader();
		System.out.println("-------------------------------------------------------\n");
		//System.out.println(arh.getHeaderFields());
		//Clean content-type
		String type = arh.getHeaderValue("Content-Type").toString().split(";")[0]; 
		//System.out.println(type);
		
		//If type is not what we wanted, we don't even bother reading it
		if (!type.equals("application/http")) {
			System.out.println("Wrong type: " + type);
			System.out.println("Skipping cycle...\n");
			return;
		}
		
		
		System.out.println("Type OK: " + type);
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
			System.out.println("Url is " + url);
			System.out.println("Matcher groups: '" + domainUrl + "'  and  '" + pageUrl + "'");
		} else {
			System.out.println("Matcher didn't find anything from " + url);
			//Something went wrong with the regex matching, let's cancel
			//this cycle, no point of uploading without the URL-s
			System.out.println("Skipping cycle...\n");
			return;
		}
			
		//Now that we know that the type is right and the domain url has
		//been found, check whether the domain is worth testing, is the 
		//domain a part of the list that we're grading
		//Seems that type was okay, read data into a string
		int a;
		String html = "";
		while ((a = ar.read()) != -1) {
			html += (char) a;
			//System.out.print((char)a);
		}
		//System.out.println(html);
		BufferedReader reader = new BufferedReader(new StringReader(html));
		String str;
				
		//Create a file to write to
		Integer rand;
	        int min = 1;
		int max = 10000;
             	do {
          		rand = new Random().nextInt((max - min) + 1) + min;
       		} while (new File("TempFile-" + rand + ".html").exists());
              		File tempFile = new File("TempFile-" + rand + ".html");

		//We only give lines after the Content-Type line for auditing
		//so we start writing to file only after write is true
		Boolean write = false;
		//Write to a TempFile
		FileWriter fw = new FileWriter(tempFile.getAbsolutePath());
		BufferedWriter bw = new BufferedWriter(fw);
		/*
		while ((str = reader.readLine()) != null) {
			if (write) {
				System.out.println(str);
				bw.write(str);
			//Yes, there can be this string later in the html but this does not
			//concern us anymore, we aren't cheking that by that time
			} else if (str.contains("text\\/html")) {
				//Write all the next lines to TempFile
				write = true;
			} else {
				System.out.println(write + str);
			}
		}
		*/
		String[] splitHtml = html.split(System.getProperty("line.separator"));
		for (String line : splitHtml) {
			//System.out.println("ASD" + line);
			if (write) {
				bw.write(line);
			} else if (line.contains("text")) {
				write = true;
				System.out.println("Found");
			} else {
				System.out.println(line);
			}
		}
		reader.close();
		bw.close();
		fw.close();
		//replaceAll removes everything before the file name
		String warcAddress = arh.getHeaderValue("reader-identifier").toString();
		String warcName = warcAddress.replaceAll(".*/","");
		//Call out Decider with the data
		System.out.println("Warc: " + warcName);
		Boolean result = new Decider().parse(tempFile.getAbsolutePath(), warcDate, domainUrl, pageUrl, warcName, warcAddress);
		tempFile.delete();
		System.out.println();
	}
}