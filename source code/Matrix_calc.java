import java.util.Collections;
import java.util.Vector;


//class for calculating levenshtein distance matrix, edit distance and edit operations for 2 strings : w1 and w2
public class Matrix_calc {

	public String w1;	//correct
	public String w2;
	public int [][] m;
	public Vector<Operation> ops=new Vector<Operation>();
	
	public Matrix_calc(String c,String f) {
		this.w1=c;
		this.w2=f;
		this.m=produceMatrix();
	}
	
	
	//Compute Levenshtein distance matrix
	//The algorithm is changed to consider the transposition operation which is considered in original algorithm
	public int[][] produceMatrix(){
		int[][] matrix=new int[w2.length()+1][w1.length()+1];
		for(int i=1;i<=w1.length();i++) {
			matrix[0][i]=i;
		}
		for(int i=1;i<=w2.length();i++) {
			matrix[i][0]=i;
		}
		for(int i=1;i<=w1.length();i++) {
			for(int j=1;j<=w2.length();j++) {
				char c1=w1.charAt(i-1);
				char c2=w2.charAt(j-1);
				int val=0;
				if(c1==c2) {
					val=Math.min(Math.min(matrix[j][i-1]+1, matrix[j-1][i]+1), matrix[j-1][i-1]);
				}else {
					val=Math.min(Math.min(matrix[j][i-1]+1, matrix[j-1][i]+1), matrix[j-1][i-1]+1);
				}
				
				//If sub[x,y] operation occurs right after sub[y,x] operation, it is considered as transposition[y,x] operation
				//Therefore, the distance value will not be incremented again.
				if(j>1&&i>1) {
					char c3=w1.charAt(i-2);
					char c4=w2.charAt(j-2);
					//Math.min(Math.min(matrix[j-2][i-1],matrix[j-1][i-2]), matrix[j-2][i-2])
					if(c1==c4&&c2==c3&&c1!=c2&&matrix[j-1][i-1]==matrix[j-2][i-2]+1) {
						//System.out.println(c1 + " "+c2 + " "+ c3 + " "+ c4);
						val=Math.min(Math.min(matrix[j][i-1]+1, matrix[j-1][i]+1), matrix[j-1][i-1]	);
					}
				}
				
				matrix[j][i]=val;
			}
		}
		
		return matrix;
	}
	
	
	public int getEditDistance() {
		return this.m[w2.length()][w1.length()];
	}
	
	
	//Backtrack distance matrix to retrieve operations needed to convert w2 to w1
	public Vector<Operation> calculateEdits() {
		Vector<Operation> vec=new Vector<Operation>();
		int i=w1.length();
		int j=w2.length();
		while(i!=0||j!=0) {
			int curr=m[j][i];
			//int left=m[j][i-1];
			//int up=m[j-1][i];
			//int leftup=m[j-1][i-1];
			String c1;
			String c2;
			if(i!=0)
				c1=Character.toString(w1.charAt(i-1));
			else
				c1="#";			//# denotes the beginning of the word
			
			if(j!=0)
				c2=Character.toString(w2.charAt(j-1));
			else
				c2="#";
			String opname="";
			if(i==0) {
				opname="del";		//deletion
				j--;
			}else if(j==0) {
				opname="ins";		//insertion
				i--;
			}else if(c1.equals(c2)) {
				
				if(curr==m[j-1][i-1]) {
					j--;
					i--;
					opname="copy";
				}
				else if(curr==m[j-1][i]+1) {
					j--;
					opname="del";
				}else if(curr==m[j][i-1]+1) {
					i--;
					opname="ins";
				}
			}else {
				if(curr==m[j-1][i-1]+1) {
					j--;
					i--;
					opname="rep";		//substitution
				}
				else if(curr==m[j-1][i]+1) {
					j--;
					opname="del";
				}else if(curr==m[j][i-1]+1) {
					i--;
					opname="ins";
				}else if(curr==m[j-1][i-1]) {
					j--;
					i--;
					opname="rep";
				}
			}
			Operation op=new Operation(opname,c1,c2);
			vec.add(op);
			//System.out.println(opname+" "+c1+" "+c2);
		}
		Collections.reverse(vec);
		this.ops=vec;
		normalizeOperations(this.ops);  
		
		return vec;
	}
	
	
	//replace appropriate substitution operations with transpositions
	public void normalizeOperations(Vector<Operation> ops) {
		for(int i=1;i<ops.size();i++) {
			Operation op1=ops.get(i-1);
			Operation op2=ops.get(i);
			//if two adjacent substitution operations have same operands, replace them with a transposition operation
			if(op1.opname.equals("rep")&&op2.opname.equals("rep")&&op1.op1.equals(op2.op2)&&op1.op2.equals(op2.op1)) {
				ops.remove(i-1);
				ops.remove(i-1);//indexes are shifted
				Operation op=new Operation("tr",op1.op1,op1.op2);
				ops.insertElementAt(op, i-1);
				i--;
			}
		}
	}
	
}
