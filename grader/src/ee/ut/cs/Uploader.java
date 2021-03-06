package ee.ut.cs;


import java.sql.*;
import org.json.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.lang.String;
import java.lang.Error;
import java.lang.management.ManagementFactory;

public class Uploader {

	private static String host = "jdbc:mariadb://localhost:3306/mydb";
	private static String user = "jaan";
	private static String pass = "passwd";

	public Boolean postGradesAccess(JSONObject json, String domain, String url) {
		//If uploaded successfully, return true, otherwise false
		
		String fields = "", values = "";			
		
		//Iterate over elements in json keyset
		for (String el : json.keySet()) {
			fields += "`" + el + "`, ";

			//json.get(el) retrieves for example ["PASS"]
			//Brackets and quotation marks are removed, and the value itself is added to SQL with upper commas
			values += "'" + json.get(el).toString().substring(2, json.get(el).toString().length() - 2) + "', ";
		}
		
		String extraFields = "`Time`, `Domain`, `Url`";	
		
		//Date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm:ss");
		String extraValues = "'" + sdf.format(new Date()).toString() + "'";
		
		//Add page address to sql
		extraValues += ", '" + domain + "', '" + url + "'";
		
		//Combine sql
		String sql = "INSERT INTO `access_lint` (" + fields + extraFields + ") VALUES (" + values + extraValues + ");";
		
		//Try creating the connection and uploading the values
		Connection con = null;
		try {
			con = DriverManager.getConnection(host, user, pass);
			Statement query = con.createStatement();
			
			query.execute(sql);
			con.close();
			//Return true if upload didn't throw errors
			return true;
		} catch (SQLException e) {
		    System.out.println(e);
		    System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		    //If error is thrown, report that upload failed
			return false;
		}
		
		
		
	}
	public Boolean postGradesCodeSniffer(HashMap<String, String> input) {
		//Returns true on successful upload, otherwise false
		
		
		String columns = "", values = "";
		
		//If not null, add to sql
		if (input != null) {
			for (String str : input.keySet()) {
				columns += "`" + str + "`, ";
				values += "'" + input.get(str) + "', ";
			}
			columns = columns.substring(0, columns.length()-2);
			values = values.substring(0, values.length()-2);
		}
		
		//Start generating the sql
		
		//Date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm:ss");
		String time = "'" + sdf.format(new Date()).toString() + "', ";
		
		//Combine everything
		String sql = "INSERT INTO `withLevels` (`time`, " + columns + ") VALUES " + "(" + time + values + ");";
		sql.replace("'", "\'");
		try {
			Connection con = DriverManager.getConnection(host, user, pass);
			Statement query = con.createStatement();
			query.execute(sql);
			sql = sql.replace("'", "\\'");
			con.close();
			//If upload successful, return true
			return true;
		} catch (SQLException e) {
			
			System.out.println("Exception: " + e.getCause());
			//If error is thrown, return that upload failed
			return false;
		}
	}
}
