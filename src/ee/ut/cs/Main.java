package ee.ut.cs;

import ee.ut.cs.WarcReader;

public class Main {
    public static void main(String[] args) {
	if (args.length != 1) {
	    System.out.println("Enter a warc name as an argument");
	    System.exit(0);
	}
	WarcReader.readWarc(args[0]);
	//Read in
	//Warcs, parsed_warcs
	//
	//Write to
	//Parsed_warcs, parsed_warcs_time, 
    }


}
