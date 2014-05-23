package org.biojava.ontology.io;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.ws.WebServiceException;

import org.genepattern.client.GPClient;
import org.genepattern.webservice.JobResult;
import org.genepattern.webservice.Parameter;

public class ImplementGenepattern {
	
	
	/**
	 * ZipCelFiles - function to ZIP all raw CEL files form a specified experiment folder
	 * write the ZIP file to the appropriate experiment folder 
	 * @param celSamples
	 * @return 
	 */
	public String ZipCelFiles(String TopExpFolder, String OutputZipFile) throws IOException {
		
		byte buf [] = new byte[2048];
        FileOutputStream OutZipFile = new FileOutputStream(OutputZipFile);
       	ZipOutputStream ZipOutStream = new ZipOutputStream(new BufferedOutputStream(OutZipFile));
       	ZipOutStream.setMethod(ZipOutputStream.DEFLATED);
		
       	File[] things = new File(TopExpFolder).listFiles();
		
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
		
		GPClient gpClient=new GPClient("http://genepattern.broadinstitute.org/gp/", username, password);
		
		//JobResult[] results = new JobResult[2]; results[0] = gpClient.runAnalysis("ExpressionFileCreator",
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
	}
		

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
					//In the case you have a treeset<String> keepAttrs parameter for attributes to keep 
					
					//which == null if you want to keep them all, do this:
					//if(keepAttrs == null || keepAttrs.contains(columns[0])DONE
					//{
					// (do all the stuff below starting at attrNames.add(columns[0]) through the end of the block)
					//}
					//System.err.println("On line #" + lnr.getLineNumber() + " number of columns is " + columns.length);
					//After row 3, we have all the attribute names in column 0
					
					if(attributesToKeep == null || attributesToKeep.contains(columns[0])){
					attrNames.add(columns[0]);
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
						        System.err.println("Hey! Badly formatted num	ber " + columns[j]);
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

public void executeMLFlexExperiment (String ExperimentFile) throws IOException{
	Runtime.getRuntime().exec("java -cp -Xmx2g -jar mlflex.jar EXPERIMENT_FILE="+ExperimentFile+"ACTION=Process EXPORT=true"); 
}



	public static void main(String[] args) throws IOException, javax.xml.ws.WebServiceException, org.genepattern.webservice.WebServiceException{
		ImplementGenepattern gpImplement = new ImplementGenepattern();
		
		String FoldersWithCelFiles = "/Users/sharanya/Documents/ThesisWorkV2/Java/MLFlexExperiments/TCellClassificationAffyU133A";
		String OutPutZipFiles = "/Users/sharanya/Documents/ThesisWorkV2/Java/MLFlexExperiments/TCellClassificationAffyU133A/TCellsFS.zip";
		String ZipFile = null;
		String UserName = "sharan7d";
		String Password = "satrangire";
		String clmFile = "/Users/sharanya/Documents/ThesisWorkV2/Java/SupportFiles/TCellGPClassFileForFS.txt";
		String OutPutFilesinDir = "/Users/sharanya/Documents/ThesisWorkV2/Java/MLFlexExperiments/TCellClassificationAffyU133A";
		String gctFile = "/Users/sharanya/Documents/ThesisWorkV2/ThesisData/IlluminaMelanomaData/melanomaIFNa2a.gct";
		String outputARFF = "/Users/sharanya/Documents/ThesisWorkV2/ThesisData/IlluminaMelanomaData/melanomaIFNa2a.arff";
		String featureList = "/Users/sharanya/Documents/ThesisWorkV2/ThesisData/IlluminaMelanomaData/FeatureList.txt";
		TreeSet <String> FeatureTreeSet = null;
		
		
		//ZipFile = gpImplement.ZipCelFiles(FoldersWithCelFiles, OutPutZipFiles);
		//gpImplement.NormalizeData (UserName, Password, ZipFile, clmFile, OutPutFilesinDir);
		FeatureTreeSet = gpImplement.FeatureSelectionSet(featureList);
		gpImplement.ConvertGctToARFF(gctFile, FeatureTreeSet, outputARFF);
		//gpImplement.executeMLFlexExperiment(MLExperimentFile);
		
	}

}
