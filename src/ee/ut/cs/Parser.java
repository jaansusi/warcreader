package ee.ut.cs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.*;
import java.util.Iterator;
import java.util.HashSet;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ArrayIndexOutOfBoundsException;
import java.lang.NoSuchMethodError;
import java.util.regex.PatternSyntaxException;


public class Parser {
	public static HashMap<String, String> pa11y(
		String fileAddress, String url, String std) {

		/*
		 * Run process and read output
		 */
		Process process = null;
		String output = "";
		try {
			process = new ProcessBuilder("timeout", "30", "pa11y", "-s", "WCAG2" + std, "-r", "json", "file://" + fileAddress).start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				output += line;
			}
			System.out.println("received with lenght " + output.length());
		
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		/*
		 * Remove the first and last 2 symbols
		 * Then split it into different pieces at },{
		 * Then add {} around the pieces and BAM! We have a list of JSONObjects!
		 */
		if (output.length() == 0) {
			return null;
		}
		//Remove first and last symbols
		output = output.substring(2, output.length()-2);
		
		ArrayList<JSONObject> jsonArray = new ArrayList<JSONObject>();
		
		//Split and iterate over split elements
		for (String str : output.split("\\},\\{")) {
			//Try creating a new JSONObject
			try {
				jsonArray.add(new JSONObject("{" + str + "}"));
			} catch (JSONException e) {
				//If can't, print stacktrace but continue
				System.out.println("Couldn't create a JSONObject into ArrayList with: \n" + str);
				e.printStackTrace();
			}

		}
		
		//JSONObject keys
		//[selector, message, context, typeCode, code, type]
		int i = 0, j = 0;
		//To understand how many different elements there are
		HashSet<String> uniqueErr = new HashSet<String>(), uniqueWarn = new HashSet<String>();
		for (JSONObject obj : jsonArray) {
			if (Integer.parseInt(obj.get("typeCode").toString()) == 1) {
				//System.out.println(obj.get("code"));
				i++;
				uniqueErr.add(obj.get("code").toString());
			}
			if (Integer.parseInt(obj.get("typeCode").toString()) == 2) {
				j++;
				uniqueWarn.add(obj.get("code").toString());
			}
			
			//Add to CSV
			
			try {
				//System.out.print("Writing to data.csv");
				File csv = new File("../data.csv");
				FileWriter fw = new FileWriter(csv.getAbsolutePath(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				
				String warc = fileAddress.substring(fileAddress.lastIndexOf("/")+1, fileAddress.length());
				//System.out.println(warc);
				String data = "\"" + warc + "\", \"" + url + "\", ";
				for (String key : obj.keySet()) {
					data += "\"" + obj.get(key).toString().replace(System.getProperty("line.separator"), "") + "\", ";
				}
				data = data.substring(0, data.length()-2) + System.getProperty("line.separator");
				//System.out.print(data);
				bw.write(data);
				
				bw.close();
				fw.close();
				//System.out.println(" complete");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//System.out.println("Element count in answer set: " + jsonArray.size());
		//System.out.println("Errors : " + i);
		//System.out.println("Of those, unique count: " + uniqueErr.size());
		
		//Create returned object
		HashMap<String, String> returned = new HashMap<String, String>();
		
		
		
		//Iterate over unique errors
		Iterator it = uniqueErr.iterator();
		//System.out.println(" and they are: ");
		ArrayList<String> errors = new ArrayList<String>();
		while (it.hasNext()) {
			String next = it.next().toString();
			//System.out.println("Error: " + std + parse(next));
			returned.put(parse(next), "ERROR");
			errors.add(parse(next));
		}
		//System.out.println();
		
		//System.out.println("Warnings : " + j);
		//System.out.println("Of those, unique count: " + uniqueWarn.size());
		
		
		//Iterate over unique warnings
		it = uniqueWarn.iterator();
		//System.out.println(" and they are: ");
		ArrayList<String> warnings = new ArrayList<String>();
		while (it.hasNext()) {
			//Original output: WCAG2AA.Principle1.Guideline1_3.1_3_1.H48
			String next = it.next().toString();
			if (!returned.containsKey(next))
				returned.put(parse(next), "WARNING");
			
			//System.out.println("Warning: " + std + parse(next));
			warnings.add(parse(next));
		}
		
		
		
		//Return them
		if (returned.size() == 0)
			return null;
		return returned;
	}
	
	private static String parse (String entry) {
		Pattern p = Pattern.compile("WCAG2(\\S+)\\.Principle(\\d)\\.Guideline(\\d)_(\\d)\\..*");
		Matcher m = p.matcher(entry);
		
		if (m.find()) {
			//A, AA or AAA
			String WCAGlevel = m.group(1);
			//System.out.print(WCAGlevel + " - ");
			
			//Principle and guideline number, e.g "1.1.3"
			String guideline = m.group(2) + "." + m.group(3) + "." + m.group(4);
			//System.out.println(guideline);
			
			return guideline;
		}
		return null;
	}

	private JSONObject getTransformer() {

		/*
		 * Key to transforming values to database format for access_lint
		 */
		try {
			JSONObject jsonTranslate = new JSONObject("{"
        		+	"\"Elements with ARIA roles must use a valid, non-abstract ARIA role\" : \"ARIA-1\" ,"
        		+	"\"aria-labelledby attributes should refer to an element which exists in the DOM\" : \"ARIA-2\" ,"
        		+	"\"Elements with ARIA roles must have all required attributes for that role\" : \"ARIA-3\" ,"
        		+	"\"ARIA state and property values must be valid\" : \"ARIA-4\" ,"
        		+	"\"role=main should only appear on significant elements\" : \"ARIA-5\" ,"
        		+	"\"aria-owns should not be used if ownership is implicit in the DOM\" : \"ARIA-6\" ,"
        		+	"\"An element's ID must not be present in more that one aria-owns attribute at any time\" : \"ARIA-7\" ,"
        		+	"\"Elements with ARIA roles must ensure required owned elemenare present\" : \"ARIA-8\" ,"
        		+	"\"Elements with ARIA roles must be in the correct scope\" : \"ARIA-9\" ,"
        		+	"\"This element has an unsupported ARIA attribute\" : \"ARIA-10\" ,"
        		+	"\"This element has an invalid ARIA attribute\" : \"ARIA-11\" ,"
        		+	"\"This element does not support ARIA roles, states and properties\" : \"ARIA-12\" ,"
        		+	"\"Audio elements should have controls\" : \"AUDIO-1\" ,"
        		+	"\"Text elements should have a reasonable contrast ratio\" : \"COLOR-1\" ,"
        		+	"\"These elements are focusable but either invisible or obscured by another element\" : \"FOCUS-1\" ,"
        		+	"\"Elements with onclick handlers must be focusable\" : \"FOCUS-2\" ,"
        		+	"\"Avoid positive integer values for tabIndex\" : \"FOCUS-3\" ,"
        		+	"\"The web page should have the content's human language indicated in the markup\" : \"HTML-1\" ,"
        		+	"\"An element's ID must be unique in the DOM\" : \"HTML-2\" ,"
        		+	"\"Meaningful images should not be used in element backgrounds\" : \"IMAGE-1\" ,"
        		+	"\"Controls and media elements should have labels\" : \"TEXT-1\" ,"
        		+	"\"Images should have an alt attribute\" : \"TEXT-2\" ,"
        		+	"\"The purpose of each link should be clear from the link text\" : \"TEXT-4\" ,"
        		+	"\"The web page should have a title that describes topic or purpose\" : \"TITLE-1\" ,"
        		+	"\"Video elements should use <track> elements to provide captions\" : \"VIDEO-1\" "
        		+	"}");
			return jsonTranslate;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null; 
	}
}
