import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
//import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

public class UrlDownload
{
	final static int size=1024;
	
	/**
	 * fileUrl - function to download a tar file from a given URL and
	 * write it to a specified directory 
	 * @param geoTarUrl
	 * @param localDirectory
	 */
	
	public void fileUrl(String geoTarUrl , String localDirectory)
	{
		OutputStream outStream = null;
		URLConnection  uCon = null;
		InputStream is = null;

		try
		{
			URL Url;
		    byte[] buf;
		    int ByteRead,ByteWritten=0;
	        Url= new URL(geoTarUrl );
	        outStream = new BufferedOutputStream(new FileOutputStream(localDirectory));
	        uCon = Url.openConnection();
	        is = uCon.getInputStream();
	        buf = new byte[size];
	        
	        while ((ByteRead = is.read(buf)) != -1)
	        {
	            outStream.write(buf, 0, ByteRead);
	            ByteWritten += ByteRead;
	        }
	        
	        //System.out.println("Downloaded Successfully.");
	        System.out.println(geoTarUrl);
	        System.out.println(localDirectory);
	    }

		catch (Exception e)
		{
		    e.printStackTrace();
		}
		finally
		{
		    try
		    {
		        is.close();
		        outStream.close();
		    }
		    catch (IOException e)
		    {
		    	e.printStackTrace();
		    }
		}
	}

	/**
	 * 
	 * @param geoTarUrl
	 * @param path
	 * @return the name of the file that was downloaded, with no path
	 */
	public String fileDownload(String geoTarUrl, String path)
	{
		int slashIndex =geoTarUrl .lastIndexOf('/');
		int periodIndex =geoTarUrl .lastIndexOf('.');

		String fileName=geoTarUrl .substring(slashIndex + 1);
		if (periodIndex >=1 &&  slashIndex >= 0 && slashIndex < geoTarUrl .length()-1)
		{
		    fileUrl(geoTarUrl ,path +"/" + fileName);
		}
		else
		{
		    System.err.println("path or file name.");
		    return null;
		}
		
		return fileName;
	}
		
	public Vector<String> UntarFile(String localFileName, String sourceFolder, String destFolder) throws FileNotFoundException, IOException {
			String tarFile = sourceFolder + "/" + localFileName;
			Vector<String> GzipCelSamples = new Vector<String>();

			   // Create a TarInputStream
			   TarInputStream tarInStream = new TarInputStream(new BufferedInputStream(new FileInputStream(tarFile)));
			   TarEntry entry;
			   while((entry =   tarInStream.getNextEntry()) != null) {
			      int count;
			      byte data[] = new byte[2048];

			      //process CEL files containing raw data
			      //assumes capitalization CEL.gz - if any files do it otherwise they will be skipped.
			      if (entry.getName().endsWith(".CEL.gz"))
			      {
			    	
				      FileOutputStream fileOutStream = new FileOutputStream(destFolder + "/" + entry.getName());
				      BufferedOutputStream dest = new BufferedOutputStream(fileOutStream);
	
				      while((count = tarInStream.read(data)) != -1) {
				         dest.write(data, 0, count);
				      }
				      
				      dest.flush();
				      dest.close();
				      
				      //also: record path to this entry
				      //which is destFolder + "/" + entry.getName()
				      GzipCelSamples.add(destFolder + "/" + entry.getName());
			      }
			   }
			   
			   tarInStream.close();
			   return GzipCelSamples;
	}
	public Vector<String> ExtractGzip(Vector<String> GzipCelSamples) throws IOException {
		byte[] buf = new byte[1024]; 
		Vector<String> celSamples = new Vector<String>();
		for (int a = 0; a < GzipCelSamples.size(); a++)
    	{
    		System.out.println("Extracting: " + GzipCelSamples.elementAt(a)); //Debug print file name
			File infile = new File(GzipCelSamples.elementAt(a));
			GZIPInputStream gzipInStream = new GZIPInputStream(new FileInputStream(infile));
			File outFile = new File(GzipCelSamples.elementAt(a).replaceAll("\\.gz$",   ""));
			FileOutputStream fileOutStream = new FileOutputStream(outFile);
			int len;
			while ( ( len = gzipInStream.read(buf) ) > 0 )
				fileOutStream.write(buf, 0, len);
			gzipInStream.close();
			fileOutStream.close();
			infile.delete(); 
			celSamples.add(GzipCelSamples.elementAt(a).replaceAll("\\.gz$",   ""));
		 }
		return celSamples;
		      	
	}
	
	public void ZipCelFiles(Vector<String> celSamples) throws IOException {
		byte buf [] = new byte[2048];
		//File f = new File(".");
        //String files[] = f.list();
        FileOutputStream OutZipFile = new FileOutputStream("C:/Users/sharan/Documents/Phd/BiomedicalInformatics/ThesisWork/Java/DC.zip");
       	ZipOutputStream ZipOutStream = new ZipOutputStream(new BufferedOutputStream(OutZipFile));
       	ZipOutStream.setMethod(ZipOutputStream.DEFLATED);
		for (int k=0; k < celSamples.size(); k++){
			FileInputStream fileInStream = new FileInputStream(celSamples.elementAt(k));
			BufferedInputStream BufInStream = new BufferedInputStream(fileInStream);
			File CelFileIn = new File(celSamples.elementAt(k));
			String entryName = CelFileIn.getName();
			System.out.println("Adding:" + celSamples.elementAt(k) + " (as " + entryName + ")");
			ZipOutStream.putNextEntry(new ZipEntry(entryName));
			int count, totalbytes;
			totalbytes = 0;
			while ( ( count = BufInStream.read(buf) ) > 0 )
			{
				ZipOutStream.write(buf, 0, count);
				totalbytes += count;
			}
			System.out.println("-> " + totalbytes);
			ZipOutStream.closeEntry();
			BufInStream.close();
			fileInStream.close();
			CelFileIn.delete();
			}
		//this zipoutstream.close() is what made this work.
		//It wasn't there before, and it is possible that closing the
		//zip out stream performed some bit of necessary concluding stuff
		//that made the file readable.
		//ZipOutStream.flush(); - commented out b/c close() should entail it.
		ZipOutStream.close();
		OutZipFile.close();
		
		 }
	 	
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		
		if(args.length>=2)
		{
			UrlDownload urld = new UrlDownload();
		    for (int i = 1; i < args.length; i++)
		    {
		    	String fileName = null;
		    	String inputDirectory = null;
		    	String outputDirectory = null;
		    	Vector<String> GzipCelSamples = null;
		    	Vector<String> celSamples = null;
		    			   	    	
		    	if(args[i].startsWith("file://localhost")) {
		    		//note: this doesn't support windows drive letters like
		    		//d: so if you're pulling data from a usb drive or dvd or
		    		//something this will break!
		    		System.out.println("This is a file from a local host");
		    		int slashIndex =args[i] .lastIndexOf('/');
		    		inputDirectory = args[i].substring(("file://localhost").length(),slashIndex);
		    		fileName=args[i] .substring(slashIndex + 1);
		    		outputDirectory = args[0];
		    	} else {
		    		System.out.println("Assuming ftp");
		    		fileName = urld.fileDownload(args[i], args[0]);
		    		inputDirectory = args[0];
		    		outputDirectory = args[0];
		    	}
	    		System.err.println("Input directory: " + inputDirectory);
	    		System.err.println("Output directory: " + outputDirectory);
	    		System.err.println("file name: " + fileName);

		    	GzipCelSamples = urld.UntarFile(fileName,inputDirectory,outputDirectory);
		    	celSamples = urld.ExtractGzip(GzipCelSamples);
		    	//for (int k=0; k < celSamples.size(); k++){
					//System.err.println(celSamples.elementAt(k));
		    	//}
		    	urld.ZipCelFiles(celSamples);
		    }
		    		   	    	
		}
		else 
		{
			System.err.println("Usage: UrlDownload <dir> <url> [<url>...]");
		}
		
		
	}
}