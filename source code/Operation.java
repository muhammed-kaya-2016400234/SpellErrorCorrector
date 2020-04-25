
public class Operation {

	public String opname;	//operation name [copy,ins(insertion),del(deletion),rep(substitution),tr(transposition)]
	public String op1;		//first operand(character)
	public String op2;		//second operand(character)
	
	public Operation() {}
	public Operation(String opname,String op1,String op2) {
		
		this.opname=opname;
		this.op1=op1;
		this.op2=op2;
		
	}
	
}
