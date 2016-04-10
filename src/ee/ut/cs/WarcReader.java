package ee.ut.cs;

import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.ArchiveRecord;
//import org.archive.io.ArchiveReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Iterator;

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
				System.out.println(archIt.next().read());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
