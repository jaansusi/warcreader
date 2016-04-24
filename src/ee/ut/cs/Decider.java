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

public class Decider {


  public Boolean parse (String tempFile, String warcDate, String domainUrl, String pageUrl, String warcName, String warcAddress) {
	/*
	 * Audit
	 */
	//Parser p = new Parser();
	Uploader sql = new Uploader();
	JSONObject j = new JSONObject();
	
	String[] standards = {"A", "AA", "AAA"};
	for (String std : standards) {
		HashMap<String, String> map = Parser.pa11y(tempFile, domainUrl+pageUrl, std);
		//System.out.println(map.keySet());
		map.put("warcDate", warcDate);
		if (map != null)
			sql.postGradesCodeSniffer(map, domainUrl, pageUrl);
					
	}
	return true;
  }
}
