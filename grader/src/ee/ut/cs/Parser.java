package ee.ut.cs;

import org.apache.commons.lang.StringUtils;

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
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (java.lang.NullPointerException e) {
			System.out.println("Caught NullPointerException in Parser");
			System.out.println("Pa11y output: \n" + output);
		}
		/*
		 * Remove the first and last 2 symbols
		 * Then split it into different pieces at },{
		 * Then add {} around the pieces and BAM! We have a list of JSONObjects!
		 */
		if (output.length() == 0) {
			System.out.println("Output null, return null");
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
				return null;
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
				//Cut off the last comma from data
				data = data.substring(0, data.length()-2) + System.getProperty("line.separator");
				bw.write(data);
				
				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Create returned object
		HashMap<String, String> returned = new HashMap<String, String>();
		
		Iterator it = uniqueErr.iterator();
		ArrayList<String> errors = new ArrayList<String>();
		while (it.hasNext()) {
			String next = it.next().toString();
			//System.out.println("Error: " + std + parse(next));
			returned.put(parse(next), "ERROR");
			errors.add(parse(next));
		}
		
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
		Pattern p = Pattern.compile("WCAG2(\\S+)\\.Principle(\\d)\\.Guideline(\\d)_(\\d)\\.(\\d_\\d_\\d).*");
		Matcher m = p.matcher(entry);
		
		if (m.find()) {
			//A, AA or AAA
			String WCAGlevel = m.group(1);
			//System.out.print(WCAGlevel + " - ");
			
			//Principle and guideline number, e.g "1.1.3"
			String guideline = WCAGlevel + "." + StringUtils.join(m.group(5).split("_"), ".");
			//System.out.println(guideline);
			
			return guideline;
		}
		System.out.println("Did not find correct SC values");
		return null;
	}
}
