package org.biojava.ontology.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

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

import org.biojava.bio.program.ssbind.AnnotationFactory;
import org.biojava.ontology.io.AffymetrixModule.PersonalNamespaceContext;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileManager;
import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

public class AppendBaseOntology {
final static int size=2048;	
	
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
	/*
		class PersonalNamespaceContextOwl implements NamespaceContext {

		    public String getNamespaceURI(String prefix) {
		        if (prefix == null) throw new NullPointerException("Null prefix");
		        else if ("pre".equals(prefix)) return "http://www.w3.org/XML/1998/namespace";
		        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
		        else if ("o".equals(prefix)) return "http://www.w3.org/2002/07/owl#";
		        return XMLConstants.NULL_NS_URI;
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
		*/
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
		        	long sourceSize = uCon.getContentLength();
		        	long destSize = -999; //some value we know cannot be the same as sourceSize.
			        
			        if(destFile.exists()) {
			        	sourceSize = uCon.getContentLength();
			        	destSize = destFile.length();
			        	System.out.println("Destination file " + localDirectory + " already exists: source size " + sourceSize + ", dest size " + destSize);
			        }
			        
			        if(sourceSize != destSize) {
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
				public String fileDownload(String geoTarUrl, String path)
				{
					int slashIndex =geoTarUrl .lastIndexOf('/');
					int periodIndex =geoTarUrl .lastIndexOf('.');

					String fileName=geoTarUrl .substring(slashIndex + 1);
			    		
					if (periodIndex >=1 &&  slashIndex >= 0 && slashIndex < geoTarUrl .length()-1)
					{
						System.err.println("About to call fileUrl on " + geoTarUrl + " amd " + path + "/" + fileName);
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
					 * Code to parse the xml file and retrieve sources	
					 * Code adapted from "http://www.ibm.com/developerworks/library/x-javaxpathapi/index.html"
					 * @throws Exception 
					 */
						public TreeMap<String,TreeMap<String,String>> parseXML(String inputXMLFile, String inputBaseOWLFile) throws Exception{
							
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
										System.out.println(attrNode.getNodeValue() + "	" + desc + "|");
										String GSMIDs = attrNode.getNodeValue().toString();
										String TitleText = desc.toString();
										
										OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
										OWLDataFactory OWLfactory = manager.getOWLDataFactory();
										
										File file = new File(inputBaseOWLFile);
										OWLOntology localFile = manager.loadOntologyFromOntologyDocument(file);
										System.out.println("Loaded ontology: " + localFile);
										
										// We can always obtain the location where an ontology was loaded from
									    //IRI documentIRI = manager.getOntologyDocumentIRI(localFile);
									    //System.out.println("    from: " + documentIRI);
	
									    //Set<OWLClass> AllClasses = localFile.getClassesInSignature();
									    OWLOntologyID ontologyID = localFile.getOntologyID();
									    IRI MainIRI = ontologyID.getOntologyIRI();
									    System.out.println("Ontology IRI: " + MainIRI);
									    
										OWLClass sampleCls = OWLfactory.getOWLClass(IRI.create(localFile.getOntologyID().getOntologyIRI().toString()
								                + "#sample"));
										
									    System.err.println("Class Chosen: " + sampleCls);
									    
									    OWLNamedIndividual GSMID = OWLfactory.getOWLNamedIndividual(IRI.create(MainIRI + "#"+GSMIDs));
									    //OWLIndividual john = OWLfactory.getOWLNamedIndividual(IRI.create(MainIRI + "#John"));
									    System.err.println("Individual Created: " + GSMID);
									   
									    OWLClassAssertionAxiom classAssertion = OWLfactory.getOWLClassAssertionAxiom(
									    		sampleCls, GSMID);
									    System.err.println("Individual asserted to class: " + classAssertion);
									    
									    //OWLOntology ontology = manager.createOntology(IRI.create(base));
									   manager.addAxiom(localFile, classAssertion);
									   
									   OWLAnnotation commentAnno = OWLfactory.getOWLAnnotation(OWLfactory.getRDFSComment(),
											   OWLfactory.getOWLLiteral(TitleText, "en"));
									   OWLAxiom ax = OWLfactory.getOWLAnnotationAssertionAxiom(GSMID.getIRI(), commentAnno);
									   
									   manager.applyChange(new AddAxiom(localFile, ax));
									   //File file2 = File.createTempFile("owlapiexamples", "saving");
									   manager.saveOntology(localFile, IRI.create(file.toURI()));
									   OWLOntologyFormat format = manager.getOntologyFormat(localFile);
									   OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
									   if (format.isPrefixOWLOntologyFormat()) {
								            owlxmlFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
								        } 
									   manager.saveOntology(localFile, owlxmlFormat, IRI.create(file.toURI()));
									   //manager.saveOntology(localFile, new OWLXMLOntologyFormat(), new StreamDocumentTarget(System.out));
									 
									  
														}//if loop
													}//[k] for loop
							    				}//if loop
											}//[i] for loop
							return returnMap;									
					}
						
						/**
						 * 
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
					
						public static void main(String[] args) throws Exception
						{
							if(args.length==3)
							{
								AppendBaseOntology urld = new AppendBaseOntology();
						
								String ConfigFile = args[0];
								String outputDirectory = null;
								String[] GEOMetaURLs = null;
								String owlFileName = null;							

								TreeMap <String, String> ConfigMap = urld.ReadConfigFile(ConfigFile);
								//Retriveing parameters from configuration TreeMap
								
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
								
								if (ConfigMap.containsKey("MAIN_OUTPUT_FOLDER") && ConfigMap.get("MAIN_OUTPUT_FOLDER") !=null){
									outputDirectory = ConfigMap.get("MAIN_OUTPUT_FOLDER");
								}
								else{
									System.err.println("Location of output folder is not given -> (Missing MAIN_OUTPUT_FOLDER value)");
									System.exit(1);
								}

						 
						    	//Input and Output directories are the same path
						    	String inputDirectory = outputDirectory;
						    	String XMLfileName = null;
						    	String XMLOutput = outputDirectory; 
						    	
						    	for(int i=0;i<GEOMetaURLs.length;i++){
							    	XMLfileName = urld.fileDownload(GEOMetaURLs[i], outputDirectory);
							    	XMLOutput = urld.GUnzip(XMLfileName,inputDirectory,outputDirectory);
							    	String XMLFileToParse = urld.UntarXML(XMLOutput, outputDirectory);
							    	urld.parseXML(XMLFileToParse, owlFileName);
								
								
							}
							
							}
											
						}
					}
						