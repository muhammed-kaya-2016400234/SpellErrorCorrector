import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Corrector {

	String corpusFile;
	String errorFile;
	HashMap<String,Integer> dictionary=new HashMap<String,Integer>();
	HashMap<String,HashMap<String,Integer>> spellErrors=new HashMap<String,HashMap<String,Integer>> ();
	String alphabet="'abcdefghijklmnopqrstuvwxyz";		//alphabet that is used by substitution and transposition
	String alphabet2="#'abcdefghijklmnopqrstuvwxyz";	//alphabet that is used by insertion and deletion
	
	//confusion matrices
	int[][] ins=new int[alphabet2.length()][alphabet2.length()];	
	int[][] del=new int[alphabet2.length()][alphabet2.length()];
	int[][] rep=new int[alphabet.length()][alphabet.length()];
	int[][] tr=new int[alphabet.length()][alphabet.length()];
	
	//number of all(not distinct) words in the dictionary
	int numWords=0;
	boolean laplaceMode=false;
	
	public Corrector(String corpusF,String errF){
		this.corpusFile=corpusF;
		this.errorFile=errF;
		try {
			dictionary=getDictionaryFromFile(corpusF);
			numWords=getNumberOfWordsInDict(dictionary);
			spellErrors=parseSpellErrors(errF);
			produceConfusionMatrices();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//parse given file(corpus.txt) and create a dictionary as hashmap where keys are the words and values are the number of occurrences of the word.
	public HashMap<String,Integer> getDictionaryFromFile(String filepath) throws IOException{
		 
		  File file = new File(filepath); 
		  HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
		  Vector<String> lines=Main.readFile(filepath);
		  for(String line:lines) {
			line=line.toLowerCase();		//convert all words to lowercase
			
			//replace any character in corpus,which is not apostrophe or not in alphabet, with a whitespace.
			String b=line.replaceAll("[^a-zA-Z']", " ");	
			String[] words=b.split("\\s+");		// get all words in corpus
			
			//put words in hashmap and calculate their occurrences
			for (String word:words) {
				
				if(dictionary.containsKey(word)) {
					dictionary.replace(word, dictionary.get(word)+1);
				}else {
					dictionary.put(word,1);
				}
			}
			
		  }
		  return dictionary;
	}
	
	//get number of all words(not distinct) in the dictionary
	public static int getNumberOfWordsInDict(HashMap<String,Integer> dictionary) {
		int count=0;
		for(Integer i:dictionary.values()) {
			count+=i;
		}
		return count;
	}
	
	
	//parse spell-errors.txt file and create a hashmap where keys are the correct words.The values are also hashmaps where keys are 
	//misspelled versions of a word and values are the number of times they were misspelled.
	public static HashMap<String,HashMap<String,Integer>> parseSpellErrors(String filepath) throws IOException{
		  
		  HashMap<String,HashMap<String,Integer>> errors=new HashMap<String,HashMap<String,Integer>>();
		  File file = new File(filepath); 		  
		  Vector<String> lines=Main.readFile(filepath);
		  for(String line:lines) {
			  
			  	line=line.toLowerCase();				
				String b=line.replaceAll("\\s+","");		
				String[] words=b.split(":");
				if(words.length>1) {
					String errs=words[1];
					String[] errlist=errs.split(",");
					
					HashMap<String,Integer> errmap=new HashMap<String,Integer>();
					for(String err:errlist) {
						String[]spl=err.split("\\*");
						if(spl.length>1) {
							Integer a=Integer.parseInt(spl[1]);
							errmap.put(spl[0],a);
						
						}else {
							errmap.put(spl[0],1);
						}				
					}
					errors.put(words[0],errmap);				
				}			
		  }
		  	
		 
		return errors;	
	}
	
	//generate all words which have an edit distance of 1 with the String s
	public static Set<String> generateWords(String s){
		String alphabet="abcdefghijklmnopqrstuvwxyz";
		StringBuilder sb=new StringBuilder(s);
		Set<String> vec = new HashSet<String>(); 
		for(int i=0;i<=s.length();i++) {
				if(i!=s.length()) {
					//deletion
					sb=new StringBuilder(s);
					String del=sb.deleteCharAt(i).toString();
					vec.add(del);
					
					//transposition
					if(i!=s.length()-1) {
						sb=new StringBuilder(s);
						String s1=Character.toString(s.charAt(i));
						String s2=Character.toString(s.charAt(i+1));
						if(!s1.equals(s2)) {
							String replace=s2+s1;
							sb.replace(i, i+2, replace);
							vec.add(sb.toString());
						}
					}
				}

				for(int j=0;j<alphabet.length();j++) {
					//insertion
					sb=new StringBuilder(s);
					char c=alphabet.charAt(j);
					String ins=sb.insert(i,c).toString();
					vec.add(ins);
					
					//substitution
					sb=new StringBuilder(s);
					if(i<s.length()) {
						if(c!=s.charAt(i)) {
							String subs=sb.replace(i,i+1,Character.toString(c)).toString();
							vec.add(subs);
						}
						
					}
				}
				
			
			
		}
		
		return vec;
		
	}
	
	//Find words in dictionary which have an edit distance of 1 with a given string "errorWord"
	public static Vector<String> findCandidatesInCorpus(HashMap<String,Integer> dictionary,String errorWord) {
		Vector<String> vec=new Vector<String>();
		for(String s:generateWords(errorWord)) {
			if(dictionary.containsKey(s)) {
				vec.add(s);
			}
		}
		return vec;
	}
	
	
	public void produceConfusionMatrices(){
		
		for(String s:spellErrors.keySet()) {
			HashMap<String,Integer> map=spellErrors.get(s);
			for(String k:map.keySet()) {
				int count=map.get(k);
				Matrix_calc m=new Matrix_calc(k,s);
				Vector<Operation> ops=m.calculateEdits();
				for(Operation op:ops) {
					String opname=op.opname;
					String oper1=op.op1.toLowerCase();
					String oper2=op.op2.toLowerCase();
					int i1=alphabet.indexOf(oper1);
					int i2=alphabet.indexOf(oper2);
					int j1=alphabet2.indexOf(oper1);
					int j2=alphabet2.indexOf(oper2);
					if(opname.equals("ins")&&j1!=-1&&j2!=-1) {
						
						ins[j2][j1]+=count;
					}else if(opname.equals("del")&&j1!=-1&&j2!=-1) {
						
						del[j1][j2]+=count;
					}else if(opname.equals("rep")&&i1!=-1&&i2!=-1) {
						
						rep[i2][i1]+=count;
					}else if(opname.equals("tr")&&i1!=-1&&i2!=-1) {
					
						tr[i1][i2]+=count;
					}
				}
				
				
			}
		}
		
		
	}
	
	//returns the correectWord for a given misspelled word using dictionary and confusion matrices
	public String correctWord(String word) {
		double maxProb=0;
		String bestCorrection="";
		
		//calculate probability p(w)(px|w) for all candidates and return the one with the highest probability
		for(String s:findCandidatesInCorpus(dictionary,word)) {
			Vector<Operation> ops=new Matrix_calc(word,s).calculateEdits();
			double prob=0;
			for(Operation op:ops) {
				if(!op.opname.equals("copy")) {
					prob=calculateProbability(s,op.opname,op.op1,op.op2);
					break;
				}
				
			}
			if(prob>maxProb) {
				maxProb=prob;
				bestCorrection=s;
			}
		}
		return bestCorrection;
	}
	
	
	//calculate probability p(w)(px|w)
	public double calculateProbability(String candidate,String opname,String oper1,String oper2) {
		return calculatePXW(opname,oper1,oper2)*1.0*dictionary.get(candidate)/numWords;	
	}
	
	
	//Calculate probability P(X|W)
	public double calculatePXW(String opname,String oper1,String oper2) {
		int i1=alphabet.indexOf(oper1);
		int i2=alphabet.indexOf(oper2);
		int j1=alphabet2.indexOf(oper1);
		int j2=alphabet2.indexOf(oper2);
		int d1=0;
		int d2=1;
		if(oper1.equals("#")) {
			oper1="";
		}
		if(oper2.equals("#")) {
			oper2="";
		}
		if(opname.equals("ins")&&j1!=-1&&j2!=-1) {
			d1=ins[j2][j1];
			d2=getNumberOfOccurrences(oper1);
			if(laplaceMode) {
				d2+=alphabet2.length();
			}
		}else if(opname.equals("del")&&j1!=-1&&j2!=-1) {
			
			d2=getNumberOfOccurrences(oper1+oper2);
			d1=del[j1][j2];
			if(laplaceMode) {
				d2+=alphabet2.length();
			}
			
		}else if(opname.equals("rep")&&i1!=-1&&i2!=-1) {
			d2=getNumberOfOccurrences(oper2);
			d1=rep[i2][i1];
			if(laplaceMode) {
				d2+=alphabet.length();
			}
		}else if(opname.equals("tr")&&i1!=-1&&i2!=-1) {
			d2=getNumberOfOccurrences(oper1+oper2);
			d1=tr[i1][i2];
			if(laplaceMode) {
				d2+=alphabet.length();
			}
		}
		
		if(laplaceMode) {
			d1++;
		}
		
		return d1*1.0/d2;
		
	}
	
	//gets the number of occurrences of a string c as a substring in the dictionary
	public int getNumberOfOccurrences(String c) {
		int count=0;
		for(String s:dictionary.keySet()) {
			count+=s.split(c).length-1;
		}
		return count;
	}
}
