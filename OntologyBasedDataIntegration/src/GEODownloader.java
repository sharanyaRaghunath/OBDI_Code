import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class GEODownloader
{
	final static int size=1024;
	private static String localFileName;
	
	public static void main(String[] args)
	{
		String geoTarUrl = args[0];
		String localFileName = GetTarFileFromGeo(geoTarUrl);
	}
	
	private static String GetTarFileFromGeo(String geoTarUrl)
	{
		OutputStream outStream = null;
		URLConnection  uCon = null;
		InputStream is = null;

		try
		{
			URL Url;
		    byte[] buf;
		    int ByteRead,ByteWritten=0;
	        Url= new URL(geoTarUrl);
	        outStream = new BufferedOutputStream(new
	        FileOutputStream(localFileName));
	        uCon = Url.openConnection();
	        is = uCon.getInputStream();
	        buf = new byte[size];
	        
	        while ((ByteRead = is.read(buf)) != -1)
	        {
	            outStream.write(buf, 0, ByteRead);
	            ByteWritten += ByteRead;
	        }
	        
	        System.out.println("Downloaded Successfully.");
	        System.out.println("File name:\""+localFileName+ "\"\nNo of bytes :" + ByteWritten);
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
		return localFileName;
	}
		
}
