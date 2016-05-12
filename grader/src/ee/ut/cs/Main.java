package ee.ut.cs;

import ee.ut.cs.WarcReader;

import java.util.List;

import java.io.File;
import java.io.IOException;

import java.lang.InterruptedException;
import java.lang.Process;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;

public class Main {

    public static void main(String[] args) {
	if (args.length != 2) {
	    System.out.println("Usage: java -jar warcReader manifest thread_count");
	    System.exit(0);
	}
	ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(args[1]));

	File warcsFile = new File(args[0]);
	try {
	    List<String> domains = FileUtils.readLines(new File("data/domains.csv"), "UTF-8");
	    List<String> warcs = FileUtils.readLines(warcsFile,"UTF-8");
	    for (String curWarc : warcs) {
		Runnable wr = new WarcReader(curWarc, domains);
		executor.execute(wr);
		FileUtils.write(new File("data/audited"), curWarc + "\n", true);
	    }
	    executor.shutdown();
	    //Wait until all threads are done
	    while (!executor.isTerminated()) {}
	    
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(0);
	}
    }
}
