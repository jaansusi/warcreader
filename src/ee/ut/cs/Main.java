package ee.ut.cs;

import ee.ut.cs.WarcReader;
import ee.ut.cs.DomainChecker;

import java.util.List;

import java.io.File;
import java.io.IOException;

import java.lang.InterruptedException;
import java.lang.Process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;

public class Main {

    private static final int THREADS = 12;

    public static void main(String[] args) {
	if (args.length != 1) {
	    System.out.println("Enter a warc manifest name as an argument");
	    System.exit(0);
	}
	ExecutorService executor = Executors.newFixedThreadPool(THREADS);

	File warcsFile = new File(args[0]);
	try {
	    DomainChecker dom = new DomainChecker();
	    List<String> warcs = FileUtils.readLines(warcsFile,"UTF-8");
	    for (String curWarc : warcs) {
		Runnable wr = new WarcReader(curWarc, dom);
		executor.execute(wr);
		FileUtils.writeStringToFile(new File("data/audited"), curWarc+"\n", "UTF-8", true);
		FileUtils.forceDelete(new File(curWarc));

	    }
	    executor.shutdown();
	    //Wait until all threads are done
	    while (!executor.isTerminated()) {}

	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(0);
	}
	//Read in
	//Warcs, parsed_warcs
	//
	//Write to
	//Parsed_warcs, parsed_warcs_time, 
    }


}
