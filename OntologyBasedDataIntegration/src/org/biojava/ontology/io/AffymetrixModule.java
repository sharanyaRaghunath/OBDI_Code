package org.biojava.ontology.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


import org.genepattern.client.GPClient;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;



public class AffymetrixModule {
final static int size=2048;

	// nested class for doing Namespace stuff for XPath
	
	class PersonalNamespaceContext implements NamespaceContext {

	    public String getNamespaceURI(String prefix) {
	        if (prefix == null) throw new NullPointerException("Null prefix");
	        else if ("pre".equals(prefix)) return "http://www.ncbi.nlm.nih.gov/geo/info/MINiML";
	        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
	        return XMLConstants.XML_NS_URI;
	    }

	    // This method isn't necessary for XPath processing.
	    public String getPrefix(String uri) {
	        throw new UnsupportedOperationException();
	    }

	    // This method isn't necessary for XPath processing either.
	    @SuppressWarnings("rawtypes")
		public Iterator getPrefixes(String uri) {
	        throw new UnsupportedOperationException();
	    }
	    
	}
	// end nested class for doing Namespace stuff for owlDoc 
	
	// nested class for doing Namespace stuff for owlDoc
	
		class PersonalNamespaceContextOwl implements NamespaceContext {

		    public String getNamespaceURI(String prefix) {
		        if (prefix == null) throw new NullPointerException("Null prefix");
		        else if ("pre".equals(prefix)) return "http://www.w3.org/XML/1998/namespace";
		        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
		        else if ("o".equals(prefix)) return "http://www.w3.org/2002/07/owl#";
		        return XMLConstants.XML_NS_URI;
		    }

		    // This method isn't necessary for XPath processing.
		    public String getPrefix(String uri) {
		        throw new UnsupportedOperationException();
		    }

		    // This method isn't necessary for XPath processing either.
		    @SuppressWarnings("rawtypes")
			public Iterator getPrefixes(String uri) {
		        throw new UnsupportedOperationException();
		    }
		    
		}
		// end nested class for doing Namespace stuff for XPath 

		
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
		    @SuppressWarnings("unused")
			int ByteRead,ByteWritten=0;
	        Url= new URL(geoTarUrl );
	        uCon = Url.openConnection();
	        is = uCon.getInputStream();
	        
	        //find out if the destination file already exists.
	        //if not, download the file.
	        //if it does, check to see if the source and destination files have
	        //the same size in bytes.
	        //if they do, don't download (assume they're the same)
	        //if they don't, download.
	        File destFile = new File(localDirectory);
        	//long sourceSize = uCon.getContentLength();
        	//long destSize = -999; //some value we know cannot be the same as sourceSize.
	        
	        if(destFile.exists()) {
	        	//sourceSize = uCon.getContentLength();
	        	//destSize = destFile.length();
	        	//System.out.println("Destination file " + localDirectory + " already exists: source size " + sourceSize + ", dest size " + destSize);
	        	System.err.println("Destination file " + localDirectory + " already exists: Not downloading file");
	        }
	        
	        //if(sourceSize != destSize) {
	        else
	        {
		        outStream = new BufferedOutputStream(new FileOutputStream(localDirectory));
		        
		        buf = new byte[size];
		        
		        while ((ByteRead = is.read(buf)) != -1)
		        {
		            outStream.write(buf, 0, ByteRead);
		            ByteWritten += ByteRead;
		        }
		        
		        //System.out.println("Downloaded Successfully.");
	        }   
	        /*
	        outStream = new BufferedOutputStream(new FileOutputStream(localDirectory));
		        
		        buf = new byte[size];
		        
		        while ((ByteRead = is.read(buf)) != -1)
		        {
		            outStream.write(buf, 0, ByteRead);
		            ByteWritten += ByteRead;
		        }
		        
		        //System.out.println("Downloaded Successfully.");
	        }
	        else {
	        	System.out.println("Destination file exists and is the same size as source; skipping download");
	        }
	        */
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
		        if(outStream != null) outStream.close(); //if file download skipped, outStream == null
		    }
		    catch (IOException e)
		    {
		    	e.printStackTrace();
		    }
		}
	}
	

/**
*fileDownload - Download the raw data and MINiMAL metadata file for each experiment 
* @param geoTarUrl
* @param path
* @return the name of the file that was downloaded, with no path
*/
	public String fileDownload(String geoTarUrl, String path, String outputFileName)
	{
		int slashIndex =geoTarUrl .lastIndexOf('/');
		int periodIndex =geoTarUrl .lastIndexOf('.');
		
		//Because of GEO reorganization different form are required for raw data versus metadata download
				String fileName=null;
				if (outputFileName == null){
					fileName = geoTarUrl .substring(slashIndex + 1);
				}
				else{
					fileName = outputFileName;
				}
		if (periodIndex >=1 &&  slashIndex >= 0 && slashIndex < geoTarUrl .length()-1)
		{
			System.err.println("About to call fileUrl on " + geoTarUrl + " and " + path + "/" + fileName);
		    fileUrl(geoTarUrl ,path +"/" + fileName);
		}
		else
		{
		    System.err.println("path or file name.");
		    return null;
		}
		
		return fileName;
	}

/**
 * UntarFile - Untar the experiment file from GEO	
 * @param localFileName
 * @param sourceFolder
 * @param destFolder
 * @return
 * @throws FileNotFoundException
 * @throws IOException
 */
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
			      //if (entry.getName().endsWith(".gz"))
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
	
	
/**
 * GUnzip - Method used to "ungzip" the "GSEXXX.family.xml.tgz" file from GEO
 * The file is downloded from GEO from the fileUrl and the fileDownload method	
 * @param localFileName
 * @param sourceFolder
 * @param destFolder
 * @return
 * @throws FileNotFoundException
 * @throws IOException
 */
	@SuppressWarnings("resource")
	public String GUnzip(String localFileName, String sourceFolder, String destFolder) throws FileNotFoundException, IOException{
		String tgzFile = sourceFolder + "/" + localFileName;
		
		System.err.println("About to open gzip input stream from " + tgzFile); 
		GZIPInputStream gzipInStream = new GZIPInputStream(new FileInputStream(tgzFile));
		
		String outputFile = tgzFile.replace(".tgz",   ".tar");
		if (outputFile.compareTo(tgzFile)== 0){
			System.err.println("tgz file does not have .tgz extension");
			throw new IOException();
		}
		System.err.println("outputFile is: " + outputFile);
		OutputStream out = new FileOutputStream(outputFile);
		
		byte[] buf = new byte[1024];
		int len;
		while ( ( len = gzipInStream.read(buf) ) > 0 ) {
			//System.err.println("- reading a buffer!");
			out.write(buf, 0, len);
		}
		gzipInStream.close();
		out.close();
		
		return outputFile;
				
	}
	
/**
 * UntarXMl- Method used to "untar" the "GSEXXX.family.xml.tar" files
 * The tar file has already been processed by the Java GZIP method
 * @param tarXMLFile
 * @param XMLDestDir
 * @return
 * @throws IOException
 */
	
	public String UntarXML(String tarXMLFile, String XMLDestDir) throws IOException{
		//String tarXMLFile = outputFile;
		String XMLOutput = null;
		//Create a TarInputStream
		System.err.println("About to open tar input stream from " + tarXMLFile);
		TarInputStream XMLtarInStream = new TarInputStream(new BufferedInputStream(new FileInputStream(tarXMLFile)));
		TarEntry XMLentry;
			while((XMLentry = XMLtarInStream.getNextEntry()) != null) {
				int count;
				byte data[] = new byte[2048];
				if (XMLentry.getName().endsWith(".xml"))
			    {
					  XMLOutput = XMLDestDir + "/" + XMLentry.getName();
					  System.err.println("XML output is " + XMLOutput);
					  FileOutputStream XMLfileOutStream = new FileOutputStream(XMLOutput);
				      BufferedOutputStream dest = new BufferedOutputStream(XMLfileOutStream);
	
				      while((count = XMLtarInStream.read(data)) != -1) {
				         dest.write(data, 0, count);
				      }
				      dest.flush();
				      dest.close();
				      		     
			      }
				else {
					System.err.println("Skipping tar entry " + XMLentry.getName());
					}
			   }
			XMLtarInStream.close();
			return XMLOutput;
	}
/**
 * ExtractGzip - Each tar file gzipped and function is used to extract the gzip file	
 * @param GzipCelSamples
 * @return
 * @throws IOException
 */
	public Vector<String> ExtractGzip(Vector<String> GzipCelSamples) throws IOException {
		byte[] buf = new byte[1024]; 
		Vector<String> celSamples = new Vector<String>();
		//The following is a regular expression that should match "GSM" followed by one or more
		//digits somewhere in a string - we're assuming this will only occur once in the string,
		//which might be dangerous.
		Pattern pattern = Pattern.compile("(GSM[0-9]+)");
		for (int a = 0; a < GzipCelSamples.size(); a++)
    	{
    		System.out.println("Extracting: " + GzipCelSamples.elementAt(a)); //Debug print file name
			File infile = new File(GzipCelSamples.elementAt(a));
			GZIPInputStream gzipInStream = new GZIPInputStream(new FileInputStream(infile));
			
			String theFilename = GzipCelSamples.elementAt(a);
			// Compile and use regular expression
			Matcher matcher = pattern.matcher(theFilename);
			boolean matchFound = matcher.find();

			String GSMString = null; 
			if (matchFound) {
			    // Get all groups for this match - WE WILL ASSUME THERE'S ONLY ONE and that
				// it's what we want.
			    GSMString = matcher.group(0);
			    System.err.println("Found a GSM match in " + theFilename + ": \"" + GSMString + "\"" );
			} else {
				System.err.println("ERROR: didn't find a GSM+digits substring in " + theFilename);
				//currently not doing anything about it, though.
			}
			
			//this is the original code which we will fix in a minute
			//original: File outFile = new File(GzipCelSamples.elementAt(a).replaceAll("\\.gz$",   ""));
			//new: replace everything after last slash with GSMString plus ".CEL"
			//unless there is no slash anywhere in it, in which case replace the whole string with that.
			String theOutputFilename = null;
			if(!theFilename.contains("/")) theOutputFilename = GSMString + ".CEL";
			//the regex below should mean: slash followed by 0 or more characters that are not 
			//slashes followed by end of line.
			else theOutputFilename = theFilename.replaceAll("/[^/]*$", "/" + GSMString + ".CEL");
			System.err.println("Output Filename: \"" + theOutputFilename + "\"");
			File outFile = new File(theOutputFilename);
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
	
	
/**
 * Code to parse the xml file and retrieve sources	
 * Code adapted from "http://www.ibm.com/developerworks/library/x-javaxpathapi/index.html"
 * @throws Exception 
 */
	public TreeMap<String,TreeMap<String,String>> parseXML(String inputXMLFile) throws Exception{
		
		//TreeMap<String,String> map = null;
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		System.out.println("About to parse XML document \"" + inputXMLFile + "\"");
		Document doc = (Document) builder.parse(inputXMLFile);
		
		//Start code for getting iid for Platform
		TreeMap<String, String> PlatformIIDtoTitleTextmap = new TreeMap<String,String>();
		XPathFactory Platformfactory = XPathFactory.newInstance();
		XPath PlatformXpath = Platformfactory.newXPath();
		PlatformXpath.setNamespaceContext(new PersonalNamespaceContext());
		XPathExpression expr2 = PlatformXpath.compile("//pre:Platform[@iid]");
		//XPathExpression expr = xpath.compile("//pre:Sample");
		
		Object PlatformResult = expr2.evaluate(doc, XPathConstants.NODESET);
		NodeList PlatformNodes = (NodeList) PlatformResult;
		
		XPath charXPath2=Platformfactory.newXPath();
		charXPath2.setNamespaceContext(new PersonalNamespaceContext());
			for (int j = 0; j < PlatformNodes.getLength(); j++) {	
				NamedNodeMap attributePlatformNodes = PlatformNodes.item(j).getAttributes();
		     
				if(attributePlatformNodes != null && attributePlatformNodes.getNamedItem("iid") != null) {
					Node attrPlatformNode = attributePlatformNodes.getNamedItem("iid");		
					System.out.println("Platform IID is: " + attrPlatformNode.getNodeValue());
					XPathExpression PlatformExpr = charXPath2.compile("//pre:Platform[@iid='"+attrPlatformNode.getNodeValue()+"']/pre:Title/text()");
					String PlatformDesc=(String)PlatformExpr.evaluate(doc, XPathConstants.STRING);
					System.err.println(PlatformDesc);
					PlatformIIDtoTitleTextmap.put(attrPlatformNode.getNodeValue(),PlatformDesc);
				}
			}
			
			
		//Start code for getting iid for Sample	
		TreeMap<String,TreeMap<String,String>> returnMap = new TreeMap<String,TreeMap<String, String>>();
		//returnMap = new TreeMap<String,TreeMap<String,String>>();			
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(new PersonalNamespaceContext());
		XPathExpression expr = xpath.compile("//pre:Sample[@iid]");
				
		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		
		XPath charXPath=factory.newXPath();
		charXPath.setNamespaceContext(new PersonalNamespaceContext());
		for (int i = 0; i < nodes.getLength(); i++) {
			//System.out.println(nodes.item(i));  
			//System.out.println(nodes.item(i).getNodeValue() + " = " + nodes.item(i).getLocalName());
			NamedNodeMap attributeNodes = nodes.item(i).getAttributes();
		    if(attributeNodes != null && attributeNodes.getNamedItem("iid") != null) {
		    		Node attrNode = attributeNodes.getNamedItem("iid");
					//System.out.println("-- IID is: " + attrNode.getNodeValue());
		    									
					String newXpath = "//pre:Sample[@iid='"+attrNode.getNodeValue()+"']/pre:Platform-Ref[@ref]";
		    		XPathExpression charExpr = charXPath.compile(newXpath);
					NodeList PlatRefnodes = (NodeList)charExpr.evaluate(doc, XPathConstants.NODESET);
					//HERE can assume there is only one node in the nodeset (though may want to
					//error trap against that by verifying that PlatRefnodes.getLength() == 1)
					//and so attributeNodes equivalent = PlatRefnodes.item(0).getAttributes is the
					//thing to traverse looking for getNamedItem("ref")
						for (int k = 0; k < PlatRefnodes.getLength(); k++) {	
							NamedNodeMap attributeNodes2 = PlatRefnodes.item(k).getAttributes();
							if(attributeNodes2 != null && attributeNodes2.getNamedItem("ref") != null) {
								Node attrNode2 = attributeNodes2.getNamedItem("ref");		
				    //from HERE do a separate XPath for //pre:Sample[@iid='"+attrNode.getNodeValue()+"']/pre:Title/text()
					//and so on.
					//String desc=charExpr.evaluate(doc, XPathConstants.NODE).toString();
					TreeMap<String, String> tempMap = new TreeMap<String,String>();
					String TitleXpath = "//pre:Sample[@iid='"+attrNode.getNodeValue()+"']/pre:Title/text()"; 
					XPathExpression charExpr2 = charXPath.compile(TitleXpath);
					String desc=(String)charExpr2.evaluate(doc, XPathConstants.STRING);
					//returnMap.put(attrNode.getNodeValue(), desc, attrNode2.getNodeValue());
					tempMap.put("Title", desc);
					tempMap.put("PlatformTitleText", PlatformIIDtoTitleTextmap.get(attrNode2.getNodeValue()));
					//tempMap.put("PlatformIID", PlatformIIDtoTitleTextmap.put(attrPlatformNode.getNodeValue(),PlatformDesc));
					returnMap.put(attrNode.getNodeValue(), tempMap);
					//System.out.println(attrNode.getNodeValue() + "|" + desc + "|" + PlatformIIDtoTitleTextmap.get(attrNode2.getNodeValue()));
					//System.out.println(attrNode.getNodeValue() + "|" + desc + "|" + PlatformIIDtoTitleTextmap.get(attrNode2.getNodeValue()) + "|");
					System.out.println(attrNode.getNodeValue() + "	" + desc + "|");
					//System.out.println(attrNode.getNodeValue() + "|" + tempMap);
									}//if loop
								}//[k] for loop
		    				}//if loop
						}//[i] for loop
		return returnMap;			
}

/**
 * Executes Hermit reasoner 
 * Uses OWL API 	
 * @param inputBaseOntology
 * @param outReasonedFile
 * @throws OWLOntologyCreationException  
 */
	public void executeHermitReasoner (String inputBaseOntology, String DirectoryStructure, String MasterIndexingFile, String GenePatternCLMFile) throws OWLOntologyCreationException, IOException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		String fileName = MasterIndexingFile;
		String fileName2 = GenePatternCLMFile;

		
		File file = new File(inputBaseOntology);
		OWLOntology localFile = manager.loadOntologyFromOntologyDocument(file);
		System.err.println("Loaded ontology: " + localFile);
		
		// Obtain the location of where an ontology was loaded from
	    //IRI documentIRI = manager.getOntologyDocumentIRI(localFile);
	    //System.out.println("    from: " + documentIRI);

	    OWLOntologyID ontologyID = localFile.getOntologyID();
	    IRI MainIRI = ontologyID.getOntologyIRI();
	    System.err.println("Ontology IRI: " + MainIRI);
	    
	    
	    OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
	    ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
	    OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
	    OWLReasoner reasoner = reasonerFactory.createReasoner(localFile, config);
	    reasoner.precomputeInferences();
	    boolean consistent = reasoner.isConsistent();
        System.err.println("Consistent: " + consistent);
        System.err.println("\n");
        org.semanticweb.owlapi.reasoner.Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
        // This node contains owl:Nothing and all the classes that are
        // equivalent to owl:Nothing - i.e. the unsatisfiable classes. We just
        // want to print out the unsatisfiable classes excluding owl:Nothing,
        // and we can used a convenience method on the node to get these
        java.util.Set<OWLClass> unsatisfiable = ((org.semanticweb.owlapi.reasoner.Node<OWLClass>) bottomNode).getEntitiesMinusBottom();
        
        if (!unsatisfiable.isEmpty()) {
            System.err.println("The following classes are unsatisfiable: ");
            for (OWLClass cls : unsatisfiable) {
                System.err.println("    " + cls);
            }
        } else {
            System.err.println("There are no unsatisfiable classes");
        }
        System.err.println("\n");
        
      
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        BufferedWriter out2 = new BufferedWriter(new FileWriter(fileName2));

       
        OWLDataFactory OWLfactory = manager.getOWLDataFactory();

//        OWLClass conditionClass = OWLfactory.getOWLClass(IRI.create(MainIRI+"#condition"));
//        System.out.println(conditionClass);
//        NodeSet<OWLClass> subClses = reasoner.getSubClasses(conditionClass, false);
//        java.util.Set<OWLClass> clses = subClses.getFlattened();

        
        OWLClass experimentClass = OWLfactory.getOWLClass(IRI.create(MainIRI+"#ML-Experiment"));
        String MainFolder = experimentClass.toString();
        String CleanMainFolder = MainFolder.replace(MainIRI + "#", "");
        int indexOfOpenBracketMF = CleanMainFolder.indexOf("<");
        int indexOfLastBracketMF = CleanMainFolder.lastIndexOf(">");
        String FinalMainFolder = CleanMainFolder.substring(indexOfOpenBracketMF+1, indexOfLastBracketMF);
        System.err.println("Name of the main folder created in user's local system : "+ FinalMainFolder);
        
        NodeSet<OWLClass> subClassExp = reasoner.getSubClasses(experimentClass, true);
        java.util.Set<OWLClass> clsesExp = subClassExp.getFlattened();
        //System.err.println("Subclasses of experiment: "+ clsesExp);
        
        for (OWLClass cls : clsesExp) {
            String ExpStr = cls.toString();
            String CleanExp = ExpStr.replace(MainIRI + "#", "");
            int indexOfOpenBracketExp = CleanExp.indexOf("<");
            int indexOfLastBracketExp = CleanExp.lastIndexOf(">");
            String FinalExp = CleanExp.substring(indexOfOpenBracketExp+1, indexOfLastBracketExp);
           
            NodeSet<OWLNamedIndividual> SampleindividualsNodeSet = reasoner.getInstances(cls, false);
           
            java.util.Set<OWLNamedIndividual> Sampleindividuals = SampleindividualsNodeSet.getFlattened();
            //System.err.println("Sample instances for "+cls2+ "are"+ind);
            for (OWLNamedIndividual ind : Sampleindividuals) {
        	   //System.err.println("Sample instances for "+cls+ " are "+ind);
        	   //System.err.println("Here are all the sample individuals " + ind);
        	   String SampleIndividualStr = ind.toString();
               String CleanSampleIndividual = SampleIndividualStr.replaceAll(MainIRI + "#", "");
               int indexOfOpenBracketInd = CleanSampleIndividual.indexOf("<");
               int indexOfLastBracketInd = CleanSampleIndividual.lastIndexOf(">");
               String FinalSamIndividual = CleanSampleIndividual.substring(indexOfOpenBracketInd+1, indexOfLastBracketInd);
               String SamIndividualFileExt = FinalSamIndividual.concat(".cel");
               
               NodeSet<OWLClass> getCondition = reasoner.getTypes(ind, true);
               java.util.Set<OWLClass> cond = getCondition.getFlattened();
             //System.err.println("TRYING TO GET CONDITIONS: " + getCondition);
               for (OWLClass condition : cond) {
                       String conditionStr = condition.toString();
                       String CleanCondition = conditionStr.replace(MainIRI + "#", "");
                       int indexOfOpenBracketCond = CleanCondition.indexOf("<");
                       int indexOfLastBracketCond = CleanCondition.lastIndexOf(">");
                       String FinalCondition = CleanCondition.substring(indexOfOpenBracketCond+1, indexOfLastBracketCond);
               	
               	OWLAnnotationProperty comment = OWLfactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
               	//System.out.println("Test 1: "+comment);
               	for (OWLAnnotation annotation : ind.getAnnotations(localFile, comment)) {                   	
               		if (annotation.getValue() instanceof OWLLiteral) {
               			OWLLiteral val = (OWLLiteral) annotation.getValue();
               			//System.err.println("Title Text for " +ind+ "is " +val.getLiteral());
              
               			//System.out.println(FinalSamIndividual+"|"+val.getLiteral()+"|"+FinalExp+"|"+FinalCondition);
               			String clmFile = SamIndividualFileExt+"	"+FinalCondition+"	"+val.getLiteral();
               			String mapFile = FinalSamIndividual+"|"+val.getLiteral()+"|"+FinalExp+"|"+FinalCondition;
               			System.out.println(mapFile);
               			
               			String manyDirs = DirectoryStructure + "/" + FinalMainFolder + "/" + FinalExp + "/" + FinalCondition;
						boolean DirsCreated = (new File(manyDirs)).mkdirs();
							if (DirsCreated){
								System.out.println("Directories:"+manyDirs+"created");
							}
               			
               			Scanner in = new Scanner(new String(mapFile));
               			
               			while (in.hasNextLine())
               		    {
               		       String line = in.nextLine();
               		       out.write(line);
               		       out.newLine();
               		    }
               			
               			Scanner in2 = new Scanner(new String(clmFile));
               			
               			while (in2.hasNextLine())
               		    {
               		       String line2 = in2.nextLine();
               		       out2.write(line2);
               		       out2.newLine();
               		    }

               		    }
                      }
                    }  
           		}
           }
        out.close();
        out2.close();
        }                       	   


/**
* Code to parse the owl file and retrieve sources	
* Code uses xpath since owl can be handled like a xml file
* @throws ParserConfigurationException 
* @throws IOException 
*/
//	public void createExperiments(String inputOWLFile, String DirectoryStructure) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
//		
//		DocumentBuilderFactory domOwlFactory = DocumentBuilderFactory.newInstance();
//		domOwlFactory.setNamespaceAware(true);
//		
//		DocumentBuilder owlBuilder = domOwlFactory.newDocumentBuilder();
//		System.out.println("About to parse OWL document \"" + inputOWLFile + "\"");
//		Document owlDoc = (Document) owlBuilder.parse(inputOWLFile);
//		
//		//Start code for getting iid for Platform
//		XPathFactory Experimentfactory = XPathFactory.newInstance();
//		XPath ExpXpath = Experimentfactory.newXPath();
//		ExpXpath.setNamespaceContext(new PersonalNamespaceContextOwl());
//		String subclassPath = "//o:Ontology/o:SubClassOf/o:Class[@IRI ='#ML-Experiment' and position()=2]/ancestor::o:SubClassOf/child::o:Class[@IRI!=\"#ML-Experiment\"]";
//		XPathExpression expr2 = ExpXpath.compile(subclassPath);
//		
//		Object ExpResult = expr2.evaluate(owlDoc, XPathConstants.NODESET);
//		NodeList ExpNodes = (NodeList) ExpResult;
//		
//		System.err.println("Nodes found by : " + subclassPath);
//	
//		for(int p=0;p<ExpNodes.getLength();p++) {
//			String experimentName = null;
//			NamedNodeMap attributeNodes = ExpNodes.item(p).getAttributes();
//			if(attributeNodes != null && attributeNodes.getNamedItem("IRI") != null) {
//				Node attrNode = attributeNodes.getNamedItem("IRI");		
//				experimentName = attrNode.getNodeValue();
//				System.out.println("All Experiments : " + experimentName);
//				String expNameClean = experimentName.replace("#", "");
//				System.err.println("All Experiments : " + expNameClean);
//				 
//				//TreeMap<String, String> conditionMap = new TreeMap<String,String>();
//				XPathFactory conditionfactory = XPathFactory.newInstance();
//				XPath conditionXpath = conditionfactory.newXPath();
//				conditionXpath.setNamespaceContext(new PersonalNamespaceContextOwl());		
//				String objPropXpath = "//o:Ontology/o:EquivalentClasses/o:Class[@IRI=\""+experimentName+"\"]/ancestor::o:EquivalentClasses/o:ObjectUnionOf/o:ObjectSomeValuesFrom/o:ObjectProperty[@IRI=\"#hasCondition\"]/ancestor::o:ObjectSomeValuesFrom/child::o:Class[@IRI]"; 
//				//String objPropXpath = "//o:Ontology/o:EquivalentClasses/o:Class[@IRI=\""+experimentName+"\"]/ancestor::o:EquivalentClasses/o:ObjectIntersectionOf/o:ObjectSomeValuesFrom/o:ObjectProperty[@IRI=\"#hasCondition\"]/ancestor::o:ObjectSomeValuesFrom/child::o:Class[@IRI]"; 
//				//String objPropXpath = "//o:Ontology/o:SubClassOf/o:Class[@IRI=\""+experimentName+"\"]/ancestor::o:SubClassOf/o:ObjectSomeValuesFrom/o:ObjectProperty[@IRI=\"#hasCondition\"]/ancestor::o:ObjectSomeValuesFrom/child::o:Class[@IRI]"; 
//				XPathExpression condExpr2 = conditionXpath.compile(objPropXpath);
//				
//				Object conditionResult = condExpr2.evaluate(owlDoc, XPathConstants.NODESET);
//				NodeList conditionNodes = (NodeList) conditionResult;
//				for(int q=0;q<conditionNodes.getLength();q++) {
//					String conditionName = null;
//					NamedNodeMap attributeNodes2 = conditionNodes.item(q).getAttributes();
//					if(attributeNodes2 != null && attributeNodes2.getNamedItem("IRI") != null) {
//						Node attrNode2 = attributeNodes2.getNamedItem("IRI");		
//						conditionName = attrNode2.getNodeValue();
//						System.err.println("All Conditions : " + conditionName);
//						String condNameClean = conditionName.replace("#", "");
//						System.err.println("All Conditions : " + condNameClean);
//						
//						//Can do this outside the source
//						//String MLExperiments = "MLFlexExperiments";
//						String manyDirs = DirectoryStructure + "/" + expNameClean + "/" + condNameClean;
//						boolean DirsCreated = (new File(manyDirs)).mkdirs();
//							if (DirsCreated){
//								System.out.println("Directories:"+manyDirs+"created");
//							}
//				}	
//				}	
//					
//			} else {
//				//error: couldn't find an experiment name
//				System.err.println("WARNING: couldn't find experiment name");
//			}
//		}
//	}

/**
 * GenerateMLFlexExperimentFile - Generates an experiment file that can be execute in ML-Flex	
 * @param inputOWLFile
 * @param MLFlexExperimentFile
 * @throws ParserConfigurationException
 * @throws SAXException
 * @throws IOException
 * @throws XPathExpressionException
 */

		public void GenerateMLFlexExperimentFile(String inputOWLFile, String MLFlexExperimentFile) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
			
			PrintStream out = new PrintStream(new FileOutputStream(MLFlexExperimentFile));
			System.setOut(out);
			
			DocumentBuilderFactory domOwlFactory = DocumentBuilderFactory.newInstance();
			domOwlFactory.setNamespaceAware(true);
			
			DocumentBuilder owlBuilder = domOwlFactory.newDocumentBuilder();
			System.err.println("About to parse OWL document \"" + inputOWLFile + "\"");
			Document owlDoc = (Document) owlBuilder.parse(inputOWLFile);
			
			XPathFactory Experimentfactory = XPathFactory.newInstance();
			XPath ExpXpath = Experimentfactory.newXPath();
			ExpXpath.setNamespaceContext(new PersonalNamespaceContextOwl());

			String subclassPath = "//o:Ontology/o:SubClassOf/o:Class[@IRI ='#ML-Experiment' and position()=2]/ancestor::o:SubClassOf/child::o:Class[@IRI!='#ML-Experiment']"; 
					///ancestor::o:SubClassOf/child::o:Class[@IRI!=\"#ML-Experiment\"]";
			XPathExpression expr2 = ExpXpath.compile(subclassPath);
			Object ExpResult = expr2.evaluate(owlDoc, XPathConstants.NODESET);
			NodeList ExpNodes = (NodeList) ExpResult;
			
			//Put all xpath compiling below
			
			System.err.println("Nodes found by : " + subclassPath);
		
			for(int p=0;p<ExpNodes.getLength();p++) {
				String experimentName = null;
				NamedNodeMap attributeNodes = ExpNodes.item(p).getAttributes();
				if(attributeNodes != null && attributeNodes.getNamedItem("IRI") != null) {
					Node attrNode = attributeNodes.getNamedItem("IRI");		
					experimentName = attrNode.getNodeValue();
					System.err.println("All Experiments : " + experimentName);
					//String expNameClean = experimentName.replace("#", "");
					//System.err.println("All Experiments : " + expNameClean);
					
					
					XPathFactory annotationfactory = XPathFactory.newInstance();
					XPath annotationXpath = annotationfactory.newXPath();
					annotationXpath.setNamespaceContext(new PersonalNamespaceContextOwl());
					
					//Get the Ml_Flex parameters but still have to retrieve the value for each parameter 
					String AnnoAssertXpath = "//o:Ontology/o:AnnotationAssertion[o:IRI=\""+experimentName+"\"]/o:AnnotationProperty/@IRI[starts-with(.,'http://bmi.utah.edu/ML-Flex#')]/ancestor::o:AnnotationAssertion";
					
					XPathExpression AnnoAssertExpr = annotationXpath.compile(AnnoAssertXpath);
					Object AssertNodeResult = AnnoAssertExpr.evaluate(owlDoc, XPathConstants.NODESET);
					NodeList AssertNodeList = (NodeList) AssertNodeResult;		
					
					XPath MLFlexParamNameXpath = annotationfactory.newXPath();
					MLFlexParamNameXpath.setNamespaceContext(new PersonalNamespaceContextOwl());
					String MLFlexParamXpath = "o:AnnotationProperty/@IRI";
					XPathExpression MLFlexParamExpr = MLFlexParamNameXpath.compile(MLFlexParamXpath);
					
					XPath MLFlexParamValueXpath = annotationfactory.newXPath();
					MLFlexParamValueXpath.setNamespaceContext(new PersonalNamespaceContextOwl());
					String MLFlexParValXpath = "o:Literal/text()";
					XPathExpression MLFlexParamValueExpr = MLFlexParamValueXpath.compile(MLFlexParValXpath);
					
					//System.err.println("Here are the number of nodes:" + AssertNodeList.getLength());
					
					
					for(int q=0;q<AssertNodeList.getLength();q++) {
						String MLFlexParamResult = (String) MLFlexParamExpr.evaluate(AssertNodeList.item(q), XPathConstants.STRING);
						System.err.println("Here are the Parameter Names:" + MLFlexParamResult);
						String MLFlexParamResultClean = MLFlexParamResult.replace("http://bmi.utah.edu/ML-Flex#", "");
						System.err.println("All Parameters : " + MLFlexParamResultClean);
						
						String MLFlexParamValueResult = (String) MLFlexParamValueExpr.evaluate(AssertNodeList.item(q), XPathConstants.STRING);
						System.err.println("Here are the Parameter Values:" + MLFlexParamValueResult);
						System.out.println(MLFlexParamResultClean + "=" + MLFlexParamValueResult);
			
					}
					}
					
					
				}
			}
		
	
/**
 * TreeMapReadMasterInfoFile - creates a mapping between the Master Index File and the directory structure 
 * @param mapfile
 * @return
 */
	public TreeMap<String,TreeMap<String,String>> TreeMapReadMasterInfoFile (String mapfile){
		
		TreeMap<String,TreeMap<String,String>> map = null;
	       
        try {
            @SuppressWarnings("resource")
			LineNumberReader lnr = new LineNumberReader(new FileReader(new File(mapfile)));
            String line = lnr.readLine();
            map = new TreeMap<String,TreeMap<String,String>>();
            while(line != null) {
            	//note the "3" in the next line is how many columns there are in the index file.
            	//be sure to maintain this if the format changes.
                String[] tokens = line.split("\\|",4); //<----- NOTE SPECIAL TREATMENT of pipe char
                //the format of the index file is as follows: <Title Text>|MLExperiment|ExperimentFolder|ConditionFolder
                TreeMap<String,String> entrymap = new TreeMap<String,String>();
                //System.out.println("Mapping description \"" + tokens[0]+ "\" to " + tokens[2] + "/" + tokens[3]);
                System.out.println("Mapping description \"" + tokens[0]+ "\" to " + tokens[1] + "/" + tokens[2]+ "/" + tokens[3]);
                //entrymap.put("MLExperiments", tokens[1]);
                entrymap.put("TitleText", tokens[1]);
                entrymap.put("ExperimentFolder", tokens[2]);
                entrymap.put("ConditionFolder", tokens[3]);
                //System.err.println("Keys are: " + entrymap);
                if(map.containsKey(tokens[0])) {
                	System.err.println("ERROR: master info file has duplicate sample GSM ID: '" + tokens[0] + "'");
                	System.exit(1);
                }
                map.put(tokens[0], entrymap);
                line = lnr.readLine();
            }
        } catch (Exception e) {
            System.err.println("Unable to open final experiment info file" + mapfile + ": dying");
            return null;
        } 
       
        return map;
	}

/**
 * ZipCelFiles - function to ZIP all raw CEL files form a specified experiment folder
 * write the ZIP file to the appropriate experiment folder 
 * @param celSamples
 * @return 
 */
	public String ZipCelFiles(String DirectoryStructure, String OutputZipFile) throws IOException {
		
		byte buf [] = new byte[2048];
        FileOutputStream OutZipFile = new FileOutputStream(OutputZipFile);
       	ZipOutputStream ZipOutStream = new ZipOutputStream(new BufferedOutputStream(OutZipFile));
       	ZipOutStream.setMethod(ZipOutputStream.DEFLATED);
		
       	File[] things = new File(DirectoryStructure).listFiles();
		
			for (File thing : things){
				if (thing.isFile()) {
					System.err.println("This is a File: " + thing.getName());
					//System.out.println("Directory Name: " + file.getName());
				}else{
					File[] subThings  = thing.listFiles();
					for (File subThing : subThings){
						System.out.println("Raw data files: " + (subThing));{
							if (subThing.isFile()){
								//for (int k=0; k < ((List<String>) child).size(); k++){
								FileInputStream fileInStream = new FileInputStream(subThing);
								BufferedInputStream BufInStream = new BufferedInputStream(fileInStream);
								ZipOutStream.putNextEntry(new ZipEntry(subThing.getName()));
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
							// Keep original CEL file so that they can be copied to their destination treatment folder
							//CelFileIn.delete();
								}
							}
						//this zipoutstream.close() is what made this work.
						//It wasn't there before, and it is possible that closing the
						//zip out stream performed some bit of necessary concluding stuff
						//that made the file readable.
						//ZipOutStream.flush(); - commented out b/c close() should entail it.
				    	}				    
					}
				}
			ZipOutStream.close();
			OutZipFile.close();
			return OutputZipFile;
			
			}

/**
 * NormalizeData - function to normalize all raw CEL files form previous function
 * Will output GCT file which will be stored at a given location 
 * @param inputZipFile
 * @param clmFile
 * @param ExperimentOutDir
 * @throws WebServiceException 
 * @throws org.genepattern.webservice.WebServiceException 
 * @throws IOException 
 */
	@SuppressWarnings("unused")
	public void NormalizeData (String username, String password, String OutputZipFile, String clmFile, String OutPutFilesinDir) throws WebServiceException, org.genepattern.webservice.WebServiceException, IOException {		
		System.err.println("Implementing Gene Pattern Server: Executing Expression File Creator Module");
		GPClient gpClient=new GPClient("http://genepattern.broadinstitute.org/gp/", username, password);
		
		JobResult preprocess = gpClient.runAnalysis("ExpressionFileCreator",
			new Parameter[]{new Parameter("input.file", OutputZipFile), 
			new Parameter("method", "RMA"), 
			new Parameter ("quantile.normalization", "yes"), 
			new Parameter("background.correct", "yes"), 
			new Parameter("compute.present.absent.calls", "no"), 
			new Parameter("normalization.method", "median scaling"), 
			new Parameter("value.to.scale.to", ""), 
			new Parameter("clm.file", clmFile), 
			new Parameter("annotate.probes", "yes"), 
			new Parameter("cdf.file", ""),
			new Parameter("output.file", "<input.file_basename>")});
		File[] outputFiles = preprocess.downloadFiles(OutPutFilesinDir);
		System.err.println("Expression File Creator Module Complete");
	}

	
/**
 * FeatureSelectionSet - function to index the feature list and create a TreeSet
 * Will output a TreeSet that can be used for feature selection on the gct file  
 * @param FeatureListFile
 */
	public TreeSet<String> FeatureSelectionSet (String FeatureListFile) throws FileNotFoundException{
		TreeSet <String> attributesToKeep = new TreeSet <String> ();
		Scanner in = new Scanner(new File(FeatureListFile));
		while (in.hasNextLine())
	    {
	       String line = in.nextLine();
	       attributesToKeep.add(line);
	       System.err.println("Keys are: " + attributesToKeep);
	    }
		return attributesToKeep;

	}
			
		
/**
 * ConvertGctToARF - Takes the GenePattern gct file format and converts it to an ARFF file that can be used in ML-Flex	
 * @param inputGctFile
 * @param outputARFFfile
 * @throws IOException
*/
	public void ConvertGctToARFF (String inputGctFile, TreeSet <String> attributesToKeep, String outputARFFfile) throws IOException{
		
		@SuppressWarnings("resource")
		LineNumberReader lnr = new LineNumberReader (new InputStreamReader (new FileInputStream (new File (inputGctFile)))); 
		Vector <String> attrNames = new Vector <String> ();
		Vector <String> classVariables = new Vector <String> ();
		Vector <Vector <Double>> instanceValues = new Vector <Vector <Double>> ();
		TreeSet <String> uniqueClassVar = new TreeSet <String> ();
		int numberOfInstances = 0;
		String theLine = "dummy";
		String [] columns = null;
		
		while (theLine != null){
			theLine = lnr.readLine();
			if (theLine == null) continue;
			//System.err.println("Line number is: " + lnr.getLineNumber());
			//In Gct file format, line 1 and line 2 can be omitted
			if (lnr.getLineNumber() > 2){
				columns = theLine.split("\\t");
				if(lnr.getLineNumber() ==3){
					System.err.println("Line 3, number of columns is " + columns.length);
					//The third line contains class variables applicable to ARFF file
					//From columns 3 through n, we have the class variables for instances 0 through n-2 
					for (int j = 2; j<columns.length; j++){
						System.err.println("Adding class " + columns[j]);
						classVariables.add(columns[j]);
						uniqueClassVar.add(columns[j]);
						//While we are here, we now know how many instances there are so we can initialize their instanceValues Vector
						instanceValues.add(new Vector<Double>());
					}
					numberOfInstances = columns.length - 2;
					System.err.println("NumberofInstances = " + numberOfInstances + ", classVariables.size()=" + classVariables.size());
				}
				else {
					if (columns.length > 1){
						
						if(attributesToKeep == null || attributesToKeep.contains(columns[0])){
						//Append line number to attribute name to be certain that they are unique	
						attrNames.add(columns[0]+"-"+lnr.getLineNumber());
						for (int j = 2; j<columns.length; j++){
							//Record the value for each attribute
							//Reading each row as a column and then splitting each row into a column 
							//Recording the attribute with the respective value into the ARFF file
							//instanceValues.get(j-2).add(Double.valueOf(columns[j]));
							double val = 0.0;
							try {
							    val = Double.valueOf(columns[j]);
							} catch (NumberFormatException nfe) {
							    if(columns[j].compareToIgnoreCase("NA") == 0) {
							        //got a not-a-number
							        val = Double.NaN;        //specially defined constant that means primitive-double not-a-number
							    } else {
							        System.err.println("Badly formatted number " + columns[j]);
							        val = 0.0;        //or whatever your value for badly-formatted numbers is
							    }
							}

							instanceValues.get(j-2).add(Double.valueOf(val));
							}
						}
					}
				}
			}
		}
		//System.out.print("Cannot write file " + outputARFFfile);
		PrintWriter pw = new PrintWriter(new File (outputARFFfile));
		pw.println("@relation arffFile");
		TreeMap <String, Integer> attrIndexMap = new TreeMap<String, Integer>();
		for (int j = 0; j<attrNames.size(); j++){
			pw.println("@attribute '"+ attrNames.get(j)+ "' numeric");
			//record the attribute index by name
			attrIndexMap.put(attrNames.get(j), Integer.valueOf(j)); 
		}
		pw.print("@attribute class {");
		Iterator <String> itr = uniqueClassVar.iterator();
		for (int j = 0; j<uniqueClassVar.size(); j++){
			pw.print(itr.next());
			if (j != uniqueClassVar.size()-1)
				pw.print(",");
		}
		pw.println("}");
		
		pw.println("@data");
		for(int i = 0; i < numberOfInstances; i++){
			for (int j = 0; j< attrNames.size(); j++){
				//Will replace NaN with a question mark 
				if (instanceValues.get(i).get(j) == Double.NaN) pw.println("?");
				else pw.print(instanceValues.get(i).get(j)+",");
			}
			pw.println(classVariables.get(i));
		}
		pw.println();
		pw.close();
		//return attrIndexMap;
	}
	
	/**
	 * Executes machine - learning analysis using ML-Flex
	 * @param MLFlexJarFile
	 * @param MLFlexExperimentFile
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void executeMLFlexExperiment (String MLFlexJarFile, String MLFlexExperimentFile) throws IOException, InterruptedException{
		String MLFlexCMD = "java -Xmx2g -jar " +MLFlexJarFile+ " EXPERIMENT_FILE="+MLFlexExperimentFile+" ACTION=Process EXPORT=true";
		System.err.println("Here is the command line arg " + MLFlexCMD);
		Process test = Runtime.getRuntime().exec(MLFlexCMD);
		/*
		test.waitFor();
		
		String line;

        BufferedReader error = new BufferedReader(new InputStreamReader(test.getErrorStream()));
        while((line = error.readLine()) != null){
            System.err.println(line);
        }
        error.close();

        BufferedReader input = new BufferedReader(new InputStreamReader(test.getInputStream()));
        while((line=input.readLine()) != null){
            System.err.println(line);
        }

        input.close();

        OutputStream outputStream = test.getOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        printStream.println();
        printStream.flush();
        printStream.close();
        */
		//System.err.println("ML-Flex Experiment is complete!" + test);
	}
	
	/**
	 * Reads and maps the different parameters executed in the config file.
	 * @param mapfile
	 * @return
	 */
	public TreeMap<String,String> ReadConfigFile (String mapfile){
		
		TreeMap<String, String> map = null;
	       
	    try {
	        @SuppressWarnings("resource")
			LineNumberReader lnr = new LineNumberReader(new FileReader(new File(mapfile)));
	        String line = lnr.readLine();
	        map = new TreeMap<String,String>();
	        while(line != null) {
	        	//note the "3" in the next line is how many columns there are in the index file.
	        	//be sure to maintain this if the format changes.
	            String[] tokens = line.split("\\s*\\=\\s*",2); //<----- NOTE SPECIAL TREATMENT of pipe char
	            //the format of the index file is as follows: <Title Text>|MLExperiment|ExperimentFolder|ConditionFolder
	            System.out.println("Mapping description \"" + tokens[0]+ "\" to " + tokens[1]);
	            if(map.containsKey(tokens[0])) {
	            	System.err.println("ERROR: config file has duplicate labels: '" + tokens[0] + "'");
	            	System.exit(1);
	            }
	            map.put(tokens[0], tokens[1]); 
	            line = lnr.readLine();
	        }
	    } catch (Exception e) {
	        System.err.println("Unable to open config file" + mapfile + ": dying");
	        return null;
	    } 
	   
	    return map;
	}


/**
* Starting main method to execute all the methods described in this class
* @param args
* @throws Exception
*/
public static void main(String[] args) throws Exception
	{
		if(args.length==3)
		{
			AffymetrixModule urld = new AffymetrixModule();
			String UserName = args[0];
			String Password = args[1];
			String ConfigFile = args[2];
			String outputDirectory = null;
			String[] GEOExpURLs = null;
			String[] GEOMetaURLs = null;
			String owlFileName = null;
			String MasterIndexingFile = null;
			String FoldersWithCelFiles = null;
			String OutPutZipFiles = null;
			String gctFile = null;
			String clmFile = null;
			String featureList = null;
			String outputARFF = null;
			String MLFlexJarFile = null;
			String MLFlexExperimentFile = null;

			TreeMap <String, String> ConfigMap = urld.ReadConfigFile(ConfigFile);
			//Retriveing parameters from configuration TreeMap
			if (ConfigMap.containsKey("GEO_DATA_LINKS") && ConfigMap.get("GEO_DATA_LINKS") !=null){
				String line = ConfigMap.get("GEO_DATA_LINKS");
				GEOExpURLs = line.split("\\s*\\,\\s*");
			}
			else{
				System.err.println("Location of experiment link from GEO is not given -> (Missing GEO_DATA_LINKS value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("GEO_XML_LINKS") && ConfigMap.get("GEO_XML_LINKS") !=null){
				String line = ConfigMap.get("GEO_XML_LINKS");
				GEOMetaURLs = line.split("\\s*\\,\\s*");
			}
			else{
				System.err.println("Location of metadata link from GEO is not given -> (Missing GEO_XML_LINKS value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("OWL_FILE") && ConfigMap.get("OWL_FILE") !=null){
				owlFileName = ConfigMap.get("OWL_FILE");
			}
			else{
				System.err.println("Location of ontology file is not given -> (Missing OWL_FILE value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("MASTER_INDEX_FILE") && ConfigMap.get("MASTER_INDEX_FILE") !=null){
				MasterIndexingFile = ConfigMap.get("MASTER_INDEX_FILE");
			}
			else{
				System.err.println("Location of ML-Flex Experiment file is not given -> (Missing MASTER_INDEX_FILE value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("ZIPPED_RAW_FILES") && ConfigMap.get("ZIPPED_RAW_FILES") !=null){
				OutPutZipFiles = ConfigMap.get("ZIPPED_RAW_FILES");
			}
			else{
				System.err.println("Location of zip file is not given -> (Missing ZIPPED_RAW_FILES value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("MAIN_OUTPUT_FOLDER") && ConfigMap.get("MAIN_OUTPUT_FOLDER") !=null){
				outputDirectory = ConfigMap.get("MAIN_OUTPUT_FOLDER");
			}
			else{
				System.err.println("Location of output folder is not given -> (Missing MAIN_OUTPUT_FOLDER value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("RAW_FILE_FOLDER") && ConfigMap.get("RAW_FILE_FOLDER") !=null){
				FoldersWithCelFiles = ConfigMap.get("RAW_FILE_FOLDER");
			}
			else{
				System.err.println("Location of top folder with cel files is not given -> (Missing RAW_FILE_FOLDER value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("GENE_PATTERN_GCT_FILE") && ConfigMap.get("GENE_PATTERN_GCT_FILE") !=null){
				gctFile = ConfigMap.get("GENE_PATTERN_GCT_FILE");
			}
			else{
				System.err.println("Location of Gene Pattern GCT file is not given -> (Missing GENE_PATTERN_GCT_FILE value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("GENE_PATTERN_CLM_FILE") && ConfigMap.get("GENE_PATTERN_CLM_FILE") !=null){
				clmFile = ConfigMap.get("GENE_PATTERN_CLM_FILE");
			}
			else{
				System.err.println("Location of Gene Pattern CLM file is not given -> (Missing GENE_PATTERN_CLM_FILE value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("OPTIONAL_FEATURE_LIST") && ConfigMap.get("OPTIONAL_FEATURE_LIST") !=null){
				featureList = ConfigMap.get("OPTIONAL_FEATURE_LIST");
			}
			else{
				System.err.println("Location of feature list file is not given -> (Missing OPTIONAL_FEATURE_LIST value)");
			}
			if (ConfigMap.containsKey("WEKA_ARFF_FILE") && ConfigMap.get("WEKA_ARFF_FILE") !=null){
				outputARFF = ConfigMap.get("WEKA_ARFF_FILE");
			}
			else{
				System.err.println("Location of ARFF file is not given -> (Missing WEKA_ARFF_FILE value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("MLFLEX_EXPERIMENT_FILE") && ConfigMap.get("MLFLEX_EXPERIMENT_FILE") !=null){
				MLFlexExperimentFile = ConfigMap.get("MLFLEX_EXPERIMENT_FILE");
			}
			else{
				System.err.println("Location of ML-Flex Experiment file is not given -> (Missing MLFLEX_EXPERIMENT_FILE value)");
				System.exit(1);
			}
			if (ConfigMap.containsKey("MLFLEX_JAR_FILE") && ConfigMap.get("MLFLEX_JAR_FILE") !=null){
				MLFlexJarFile = ConfigMap.get("MLFLEX_JAR_FILE");
			}
			else{
				System.err.println("Location of ML-Flex jar file is not given -> (Missing MLFLEX_JAR_FILE value)");
				System.exit(1);
			}

	    	String fileName = null;
	    	//Input and Output directories are the same path
	    	String inputDirectory = outputDirectory;
	    	String XMLfileName = null;
	    	String XMLOutput = outputDirectory;
	    	String outputFileName = null;
	    	Vector<String> GzipCelSamples = null;
	    	Vector<String> celSamples = null;		    	    	
			String ZipFile = null;		    	
			TreeSet <String> FeatureTreeSet = null;
			//String clmFile2 = "/Users/sharanya/Documents/clmFile.txt";
			//String MasterIndexFile = "/Users/sharanya/Documents/MasterIndexFile.txt";
			
			urld.executeHermitReasoner(owlFileName, MasterIndexingFile, clmFile, outputDirectory);

			
	    	//Currently the executeHermitReasoner is used to create the directory structure
			//urld.createExperiments(owlFileName, outputDirectory);
			
			
			for(int i=0;i<GEOExpURLs.length;i++){
	    		System.err.println("Experiment file: " + GEOExpURLs[i]);
		    	fileName = urld.fileDownload(GEOExpURLs[i], outputDirectory, outputFileName);
		    	GzipCelSamples = urld.UntarFile(fileName,inputDirectory,outputDirectory);
	    		celSamples = urld.ExtractGzip(GzipCelSamples);
		    	System.err.println("Here is the location of all the .cel files:");
		    	for (int k=0; k < celSamples.size(); k++){
					System.err.println(celSamples.elementAt(k));
		    	}
	    		System.err.println("Metadata file: " + GEOMetaURLs[i]);
		    	XMLfileName = urld.fileDownload(GEOMetaURLs[i], outputDirectory, null);
		    	XMLOutput = urld.GUnzip(XMLfileName,inputDirectory,outputDirectory);
		    	String XMLFileToParse = urld.UntarXML(XMLOutput, outputDirectory);
		    	
	    	
	    	//Construct Mapping from description String to Destination Directory
	    	//In future, will have to pass mapping file as a parameter
			TreeMap<String, TreeMap<String,String>> DirMapping = urld.TreeMapReadMasterInfoFile(MasterIndexingFile);
			//Will hold mapping from iid to Description as returned by parseXML
			TreeMap<String, TreeMap<String, String>> IDDescriptionMapping = null;

							
	    	try {
	    		IDDescriptionMapping = urld.parseXML(XMLFileToParse);
			} catch (XPathExpressionException e) {
				 //TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Now, step through each sample and using the description related to that sample, put the sample in the respective directory. 			
			Iterator<String> iterSample = IDDescriptionMapping.keySet().iterator();
			while (iterSample.hasNext()){
				String Sampleiid = iterSample.next();
				TreeMap<String, String> SampleDescription = IDDescriptionMapping.get(Sampleiid);

				if(DirMapping == null) {
				    System.err.println("DirMapping is null, dying");
				    System.exit(1);
				}
				if(SampleDescription == null) {
				    System.err.println("SampleDescription is null, dying");
				    System.exit(1);
				}
				//if(SampleDescription.get("Title") == null) {
				if(SampleDescription.get("Sampleiid") == null) {
				    System.err.println("** Sample Description Platform Title Text is null");
				    System.exit(1);
				}
				
				//System.err.println("Experiment Title equals |" + SampleDescription.get("Title") + "|");
				//System.err.println("Experiment Title equals |" + SampleDescription.get("Sampleiid") + "|");
				System.err.println("Experiment Title equals |" + Sampleiid + "|");
				TreeMap<String, String> MainExperiment = null;
				String SampleKey = SampleDescription.get("Sampleiid");
				if (SampleKey == null){
					System.err.println("SampleKey is null");
					MainExperiment = DirMapping.get(Sampleiid); 
				}
				else {
				//Sampleiid used to be SampleKey that was causing problems.
				MainExperiment = DirMapping.get(SampleKey);
				}
				//TreeMap<String, String> MainExperiment = DirMapping.get(SampleDescription.get("Title")); 
				if (MainExperiment == null) {
					System.err.println("WARNING: No mapping file entry found for: " + SampleDescription.get("Sampleiid"));
					//System.err.println("WARNING: No mapping file entry found for: " + SampleDescription.get("Title"));
					continue;
				}
			/*
				//System.err.println("Experiment Title equals |" + SampleDescription.get("Title") + "|");
				System.err.println("Experiment Title equals |" + SampleDescription.get("Sampleiid") + "|");
				TreeMap<String, String> MainExperiment = DirMapping.get(SampleDescription.get("Sampleiid")); 
				//TreeMap<String, String> MainExperiment = DirMapping.get(SampleDescription.get("Title")); 
				if (MainExperiment == null) {
					System.err.println("WARNING: No mapping file entry found for: " + SampleDescription.get("Sampleiid"));
					//System.err.println("WARNING: No mapping file entry found for: " + SampleDescription.get("Title"));
					continue;
				}
				*/
				
				//String TitleText = MainExperiment.get("TitleText");
				String SpecificExp = MainExperiment.get("ExperimentFolder");
				String TreatmentDir = MainExperiment.get("ConditionFolder");
				if (TreatmentDir == null){
					System.err.println("Couldn't find treatment directory for TreatmentDir=\"" +TreatmentDir + "\", sampleDescription = \"" + SampleDescription + "\" (Sample " + Sampleiid + ")" );
					}
				else{
					String SampleFileName = outputDirectory + "/" + Sampleiid + ".CEL";
					//Next line will need MainExpFolder
					String SampleDir = outputDirectory + "/" + "/" + SpecificExp + "/" + TreatmentDir;
					//String SampleDir = outputDirectory + "/" + MLFlexExpFolder + "/" + SpecificExp + "/" + TreatmentDir;
					System.out.println("Sample "+ SampleFileName + " will go in directory " + SampleDir + " (" + SampleDescription + ")");
					//Now we know full path and file name for the sample file and full path for the destination directory. 
					InputStream in = new FileInputStream(SampleFileName);
					OutputStream out = new FileOutputStream(SampleDir + "/" + Sampleiid + ".CEL");
					byte[] buf = new byte[2048];
	                int len;
	                while ((len = in.read(buf)) > 0) {
	                    out.write(buf, 0, len);
	                }
	                in.close();
	                out.close();
	                }		
														
				}
				 
		}
	    
			//Execute remaining methods
			ZipFile = urld.ZipCelFiles(FoldersWithCelFiles, OutPutZipFiles);
			urld.NormalizeData (UserName, Password, ZipFile, clmFile, outputDirectory);
			FeatureTreeSet = urld.FeatureSelectionSet(featureList);
			urld.GenerateMLFlexExperimentFile(owlFileName, MLFlexExperimentFile);
			urld.ConvertGctToARFF(gctFile, FeatureTreeSet, outputARFF);
			urld.executeMLFlexExperiment(MLFlexJarFile, MLFlexExperimentFile);
			
		}
		
		else
		{
			System.err.println("Usage: UrlDownload <OutputDir> <RawURL> <XMLURL>");
		}
						
	}
}
//}