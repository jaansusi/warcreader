package ee.ut.cs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.io.BufferedReader;
import java.io.FileReader;

import java.net.URL;

import org.json.JSONObject;
import org.json.JSONException;

import ee.ut.cs.Parser;
import ee.ut.cs.Uploader;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;

import java.lang.management.ManagementFactory;

public class WarcParser {


  public Boolean parse (String TempFile, String warcDate, String domainUrl, String pageUrl, String warcPath) {
	try {
		
		//Warc creation date
		String date = warcName.substring(4,12);
		System.out.println(warcName);
		date = date.substring(0,4) + "-" + date.substring(4,6) + "-" + date.substring(6,7);
		System.out.println(date);

		//Make a decision based on the conf file located in root folder
		Properties prop = new Properties();
		InputStream is = new FileInputStream("plugin-conf.cfg");
		prop.load(is);
		String grader = prop.getProperty("GRADER");
		is.close();
		
		/*
		 * Audit
		 */
		
		Parser p = new Parser();
		Uploader sql = new Uploader();
		JSONObject j = new JSONObject();
		
		/*if (grader.equals("AL")) {
			try {
				j = p.accessLint(f.getAbsolutePath());
			} catch (JSONException e) {
				e.printStackTrace();
			}
				if(sql.postGradesAccess(j, domUrl.getHost(), domUrl.getFile()) == true)
				System.out.println("upload successful");
			else
				System.out.println("upload failed");;
		} else */if (grader.equals("HTML")) {
			String[] standards = {"A", "AA", "AAA"};
			for (String std : standards) {
				HashMap<String, String> array = p.pa11y(warcPath, domainUrl+pageUrl, std);
				array.put("warcDate", date);
				if (array != null)
					sql.postGradesCodeSniffer(array, domainUrl, pageUrl);
						
			}
				//if (sql.postGradesCodeSniffer(array, domUrl.getHost(), domUrl.getFile()) == true)
				//	System.out.println("upload successful");
				//else
				//	System.out.println("upload failed");
		}
		return true;
//		if (f.delete())
//			System.out.println("\t and file deleted.");
//		else System.out.println("\t but file not deleted.");
	} catch (JSONException | IOException e) {
		System.out.println(e.getMessage());
	}
	
	//System.out.print(".");
    return false;
  }
}
