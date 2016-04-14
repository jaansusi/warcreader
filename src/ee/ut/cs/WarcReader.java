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

public class WarcReader {
 
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Enter a warc name as argument");
			System.exit(0);
		}
		try {
			FileInputStream fis = new FileInputStream(args[0]);
			/*
			ArchiveReader aread = WARCReaderFactory.get(args[0], fis, true);
			for (ArchiveRecord rec : aread) {
				System.out.println(rec.getHeader());
			}
			*/
			Iterator<ArchiveRecord> archIt = WARCReaderFactory.get(new File(args[0])).iterator();
			while (archIt.hasNext()) {
				ArchiveRecord ar = archIt.next();
				ArchiveRecordHeader arh = ar.getHeader();
				System.out.println(arh.getHeaderFields());
				
				String warcDate = arh.getDate();
				String url = arh.getUrl();
				System.out.println(url + "---" + warcDate);
				
				//Clean content-type
				String type = arh.getHeaderValue("Content-Type").toString().split(";")[0]; 
				System.out.println();
				
				//If type is not what we wanted, we don't even bother reading it
				if (type != "application/http")
					continue;
				
				
				//Seems that type was okay, read data into a string
				int a;
				String html = "";
				while ((a = ar.read()) != -1) {
					html += (char) a;
					//System.out.print((char)a);
				}
				BufferedReader reader = new BufferedReader(new StringReader(html));
				String str;
				
				//Create a file to write to
				Integer rand;
			        int min = 1;
              			int max = 10000;
                		do {
                        		rand = new Random().nextInt((max - min) + 1) + min;
                		} while (new File("TempFile-" + rand + ".html").exists());
                		File f = new File("TempFile-" + rand + ".html");

				//We only give lines after the Content-Type line for auditing
				//so we start writing to file only after write is true
				Boolean write = false;
				//Write to a TempFile
				FileWriter fw = new FileWriter(f.getAbsolutePath());
				BufferedWriter bw = new BufferedWriter(fw);

				while ((str = reader.readLine()) != null) {
					if (write) {
						bw.write(str);
					} else if (str.contains("Content-Type: text/html")) {
						System.out.println(str);
						//Write all the next lines to TempFile
						write = true;
						break;
					}
				}
				reader.close();
				bw.close();
				fw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
