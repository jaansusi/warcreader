package ee.ut.cs;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.List;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;

class Aggregator {
    
    private static String host = "jdbc:mariadb://localhost:3306/mydb";
    private static String user = "jaan";
    private static String pass = "passwd";
    private static Connection con;
    private static Statement query;
    private static List<String> criteria;
    private static String date;
    private static String table;

    public static void main(String[] args) {
	if (args.length != 2) {
	    System.out.println("Usage: java -jar aggr.jar table_name YYYY-MM-DD");
	    System.exit(0);
	}
	date = args[1];
	table = args[0];
	
	Connection con = null;
	try {
	    //Initiate variables
	    con = DriverManager.getConnection(host, user, pass);
	    query = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	    criteria = new LinkedList<String>(); 
	    
	    //Retrieve all columns names of table and
	    //insert all criteria into the criteria List
	    createCriteria(query.executeQuery("DESCRIBE " + table + ";"));

	    //Retrieve all distinct domains on given date
	    ResultSet rs = query.executeQuery("SELECT distinct(domain) from " + table + " where warcDate > \"" + date + "\";");
	    
	    //Loop through all domains and aggregate their data
	    while (rs.next()) {
		ResultSet pages = getPagesOfDomain(rs.getObject(1).toString());
		pages.beforeFirst();
		semiPer(rs.getObject(1).toString(), pages);
		pages.beforeFirst();
		permissive(rs.getObject(1).toString(), pages);
		pages.beforeFirst();
		restrictive(rs.getObject(1).toString(), pages);
	    }
	    
	    //Close connection
	    con.close();
	} catch (SQLException e) {
	    System.out.println(e.getMessage());
	}
    }
    //This funcion is called out once per domain
    private static void semiPer(String domain, ResultSet pages) throws SQLException {
	
	//Initialize scores variable and create as many elements as there are
	//columns for success criteria, all have a starting score of 0 (type Double)
	List<Double> scores = new LinkedList<Double>();
	for (int i = 0; i < 39; i++)
	    scores.add(0.0);
	
	//Counts how many pages are in one domain so it knows with what
	//to divide the final score in the end
	int scoreCounter = 0;
	
	//Get the number of columns in retrieved rows
	ResultSetMetaData meta = pages.getMetaData();
	int columnCount = meta.getColumnCount();
	
	//Cycles through the rows of one domain
	while (pages.next()) {
	    scoreCounter++;
	    //Offset for leaving the first place in list for domain
	    for (int column = 1; column <= columnCount; column++) {
		Object value = pages.getObject(column);
	        int len = value.toString().length();
		//System.out.println(len);
	        int score = 0;
		switch(len) {
		    case 4://PASS
			score = 10;
		        break;
		    case 7://WARNING
			score = 5;
		        break;
		    case 5://ERROR
			score = 0;
		        break;
		}
		//Add the score to previously stored value
	        Double prevVal = scores.get(column-1);
		scores.set(column-1, prevVal+score);
	    }
	}
	 
	
	//Mistake counters for domain grade
	int aMis = 0;
	int aaMis = 0;
	int aaaMis = 0;
	
	//Divide scores with number of pages to get average
	//Aggregate into level
	for (int i = 0; i < scores.size(); i++) {
	    Double score = scores.get(i)/scoreCounter;
	    scores.set(i, score);
	    Double limit = 8.0;
	    //System.out.println(score + "-" + limit);
	    if (i <= 10) {
		if (score < limit)
		    aMis++;
	    } else if (i <= 23) {
		if (score < limit)
		    aaMis++;
	    } else {
		if (score < limit)
		    aaaMis++;
	    }
	}
	
	//Add the final grade into ans
	String grade = "-";
	if (aMis <= 2) {
	    grade = "A";
	    if (aaMis <= 2) {
		grade = "AA";
		if (aaaMis <= 2) {
		    grade = "AAA";
		}
	    }
	}
	//System.out.println("" + aMis + aaMis + aaaMis);
	//Add domain name and date as first elements, follow up with criteria scores
	String ans = domain + ";" + scoreCounter + ";" + grade;// + ";" + aMis+aaMis+aaaMis;

	//for (Double db : scores)
	    //ans += ";" + db.toString().replace(".", ",");
	try {
	    FileUtils.write(new File("semi" + date + ".csv"), ans+"\n", true);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    //This function is called out once per domain
    private static void permissive(String domain, ResultSet pages) throws SQLException {
	int columnCount = pages.getMetaData().getColumnCount();
	List<String> results = new LinkedList<String>();
	for (int i = 0; i < 39; i++)
	    results.add("");
	int counter = 0;
	while (pages.next()) {
	    counter++;
	    for (int column = 1; column <= columnCount; column++) {
		String value = pages.getObject(column).toString();
		if (!results.get(column-1).equals("PASS"))
		    results.set(column-1, pages.getObject(column).toString());
	    }
	}
	
	//Mistake counters for domain grade
	int aMis = 0;
	int aaMis = 0;
	int aaaMis = 0;

	//Aggregate into level
	for (int i = 0; i < results.size(); i++) {
	    String score = results.get(i);
	    if (i <= 10) {
		if (!score.equals("PASS"))
		    aMis++;
	    } else if (i <= 23) {
		if (!score.equals("PASS"))
		    aaMis++;
	    } else {
		if (!score.equals("PASS"))
		    aaaMis++;
	    }
	}
	
	//Add the final grade into ans
	String grade = "-";
	if (aMis <= 2) {
	    grade = "A";
	    if (aaMis <= 2) {
		grade = "AA";
		if (aaaMis <= 2) {
		    grade = "AAA";
		}
	    }
	}
	
	String ans = domain + ";" + counter + ";" + grade;

	//for (String str : results)
	//    ans += ";" + str;
	try { FileUtils.write(new File("perm" + date + ".csv"), ans+"\n", true);}
	catch (IOException e) { e.printStackTrace();}
    }

    private static void restrictive(String domain, ResultSet pages) throws SQLException {
	int columnCount = pages.getMetaData().getColumnCount();
	List<String> results = new LinkedList<String>();
	for (int i = 0; i < 39; i++)
	    results.add("");
	int counter = 0;
	while (pages.next()) {
	    counter++;
	    for (int column = 1; column <= columnCount; column++) {
		String value = pages.getObject(column).toString();
		if (results.get(column-1).equals("ERROR")) {
		    //Do nothing
		} else if (results.get(column-1).equals("WARNING")) {
		    if (value.equals("ERROR"))
			results.set(column-1, value);
		} else {
		    results.set(column-1, value);
		}
	    }
	}
	
	//Mistake counters for domain grade
	int aMis = 0;
	int aaMis = 0;
	int aaaMis = 0;
	
	//Aggregate into level
	for (int i = 0; i < results.size(); i++) {
	    String score = results.get(i);
	    if (i <= 10) {
		if (!score.equals("PASS"))
		    aMis++;
	    } else if (i <= 23) {
		if (!score.equals("PASS"))
		    aaMis++;
	    } else {
		if (!score.equals("PASS"))
		    aaaMis++;
	    }
	}
	
	//Add the final grade into ans
	String grade = "-";
	if (aMis <= 2) {
	    grade = "A";
	    if (aaMis <= 2) {
		grade = "AA";
		if (aaaMis <= 2) {
		    grade = "AAA";
		}
	    }
	}
	
	String ans = domain + ";" + counter + ";" + grade;
	//for (String str : results)
	//    ans += ";" + str;
	try { FileUtils.write(new File("rest" + date + ".csv"), ans+"\n", true);}
	catch (IOException e) { e.printStackTrace();}
    }
    //This function is called out once per run, to get the criteria list for the SELECT SQL
    private static void createCriteria(ResultSet rs) throws SQLException {
	while (rs.next()) {
	    ResultSetMetaData meta = rs.getMetaData();
	    int columnCount = meta.getColumnCount();
	    for (int column = 1; column <= columnCount; column++) {
		Object value = rs.getObject(column);
	        if (value != null) {
		    if (value.toString().length() > 0) {
			if (value.toString().substring(0,1).contains("A"))
			    criteria.add(value.toString());
		    }
		}
	    }
	}
    }

    //This function is called out once per domain
    private static ResultSet getPagesOfDomain(String domain) throws SQLException {
	String criteriaSQL = "";
	System.out.println("Get pages of domain: " + domain);
	for (Object str : criteria.toArray()) {
	   criteriaSQL += "`" + str.toString() + "`, ";
	}
	criteriaSQL = criteriaSQL.substring(0,criteriaSQL.length()-2);
	criteriaSQL = "SELECT " + criteriaSQL + " FROM " + table + " WHERE domain =\"" + domain + "\" AND warcDate > \"" + date + "\";";
	
	return query.executeQuery(criteriaSQL);
    }
}
