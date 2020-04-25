import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Main {

	public static void main(String[] args) throws IOException{
		
		  if(args.length>0) {
			  
			  	  //get the directory path of the input file
			  	  File tmpDir = new File(args[0]);
				  String directory=args[0];
				  
				  if(tmpDir.exists()) {
					  
					  
					  final String dir = System.getProperty("user.dir");
					  
					  /*Corrector c is an object, which parses corpus.txt and spell-errors.txt files to 
					   * create a dictionary and confusion matrices.Then, it can correct a given misspelled word.
					    
					  */
					  Corrector c=new Corrector(dir+"\\corpus.txt",dir+"\\spell-errors.txt");
					  
					  //get the words from input file
					  Vector<String> lines=readFile(directory+"\\words.txt");
					  
					  //Run the corrector 2 times.With and without Laplace smoothing.
					  for(int j=0;j<2;j++) {
						  String name="";
						  if(j==1) {
							 c.laplaceMode=true;
							 name="Laplace";
						  }
						  
						  
						//correct the words and print them to results.txt and resultsLaplace.txt
						  Vector<String> results=new Vector<String>();
						  for(String line:lines) {
							  results.add(c.correctWord(line));
							  
						  }
						  writeToFile(directory+"\\results"+name+".txt",results);
					  
					  }
					  System.out.println("Completed!");
					  
				  }else {
					  System.out.println("Directory does not exist!");
				  }
			 
		  }else {
			  System.out.println("Please enter directory path!");
		  }
 
		  //Uncomment this line,if you want to print confusion matrices to files in the input directory
		  //printConfusionMatrices(directory,c);
		  
		   
		  
		  
		  
		  
		  
		  
		  	
	}
	
	public static void printConfusionMatrices(String directory,Corrector c) throws IOException {
		File file=new File(directory+"\\confusionMatrices");
	    boolean check=file.mkdirs();
	    if(check) {
		int[][][] matrices= {c.ins,c.del,c.rep,c.tr};
		String [] names= {"Insertion","Deletion","Substitution","Transposition"};
		for(int q=0;q<matrices.length;q++) {
			Vector<String> vec=new Vector<String>();
			int [][]m=matrices[q];
			String alphabet2=c.alphabet;
			if(q==0||q==1) {
				alphabet2=c.alphabet2;
			}
		    for(int i=-1;i<m.length;i++) {
		    	String line="";
			  if(i!=-1) {
				  line+=alphabet2.charAt(i)+" ";
				  
			  }
			  //System.out.println("length:" +c.ins[0].length);
			  for(int j=0;j<m[0].length;j++) {
				  String s="";
				  if(i==-1) {
					  s="  "+alphabet2.charAt(j);
				  }else {
					  s=m[i][j]+"";
					  
				  }
				  line+=s;
				  
				  for(int z=0;z<5-s.length();z++) {
					  line+=" ";
					 
				  }
			  }
			  vec.add(line);
			  
		    }
		   
		    writeToFile(directory+"\\confusionMatrices\\confusionMatrix"+names[q]+".txt",vec);
		}
	    }
		  
		  
	}
	
	public static Vector<String> readFile(String filepath) throws IOException{
		Vector<String> vec=new Vector<String>();
		File file = new File(filepath); 		  
		  BufferedReader br = new BufferedReader(new FileReader(file));
		  String line; 
		  while ((line = br.readLine()) != null) {
			  vec.add(line);
		  }
		
		return vec;
	}
	
	public static void writeToFile(String filepath,Vector<String> lines) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
	    
		for(String line:lines) {
			writer.write(line+"\n");
		}
		writer.close();
		
	}
	
	
	
	
}
