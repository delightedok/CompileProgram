package com.tamgitsun.analysis;

import java.util.ArrayList;
import java.util.List;

import com.tamgitsun.object.WordInfo;
import com.tamgitsun.words.Token;

public class Analysis {
	
	private Analysis(){}
	
	public static Analysis analysis=null;
	private static Object obj=new Object();
	private List<String> sentences=new ArrayList<String>();
	//private int rows=0;
	final public static int RESULT_TRUE=0;
	final public static int RESULT_FALSE=1;
	private int result=RESULT_TRUE;
	private Token token=null;
	
	public static Analysis getInstance(){
		if(analysis==null){
			synchronized(obj){
				if(analysis==null){
					analysis=new Analysis();
				}
			}
		}
		return analysis;
	}
	
	public void setSentences(String sentences){
		String temp="";
		int pos=0;
		//rows=0;
		this.sentences.clear();
		while(pos<sentences.length()){
			if(sentences.charAt(pos)=='\r'){
				pos++;
				if(sentences.charAt(pos)=='\n'){
					this.sentences.add(temp);
					temp="";
				}else{
					pos--;
					temp+=sentences.charAt(pos);
				}
			}else{
				temp+=sentences.charAt(pos);
			}
			pos++;
		}
		if(sentences.charAt(pos-1)!='\n'){
			this.sentences.add(temp);
			temp="";
		}
	}
	
	public void setSentences(String []sentences){
		//rows=0;
		this.sentences.clear();
		for(String s:sentences){
			this.sentences.add(s);
		}
	}
	
	/**
	 * WordInfo:
	 * public final static int VARIABLE=1;
	 * public final static int PREDICATE=2;
	 * public final static int DIGITS=3;
	 * public final static int OP_LEFTBACKET=4;
	 * public final static int OP_RIGHTBACKET=5;
	 * public final static int OP_NEGATION=6;
	 * public final static int OP_COMPARE=7;
	 * public final static int OP_MATH=8;
	 * public final static int OP_QUANTIFER=9;
	 * public final static int OP_CONNECTIVES1=10;
	 * public final static int OP_CONNECTIVES2=11;
	 * public final static int OP_COMMAS=12;
	 * public final static int SPACE=13;
	 * public final static int WRONG=14;
	 * public final static int END=15;
	 * 
	 * syntax:
	 * term  -> variable op_math variable|variable -> variable{ op_math variable}
	 * form0 -> predicate(term{,term})|digit op_compare digit -> predicate(term{,term})|digit{ op_compare digit}
	 * form1 ->(op_quantifer variable)+ form3|(form0)|~from0|form0 -> op_quantifer variable {op_quantifer variable} form3|(form0)|~from0|form0
	 * form2 -> form2 op_connectives1 form2|form1 -> form1{ op_connectives1 form2}
	 * form3 -> from3 op_connectives2 form3|form2 -> form2{ op_connectives2 form3}
	 */
	public int doAnalysis(){
		result=RESULT_TRUE;
		//rows=0;
		for(String s:sentences){
			token=Token.getInstance();
			token.setParagraph(s);
			WordInfo wi=token.getToken();
			boolean tempResult=true;
			if(wi.getToken()!=WordInfo.END){
				switch(wi.getToken()){
				case WordInfo.VARIABLE:
					result=term(wi);
					if(result==RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.PREDICATE:
					result=form3(wi);
					if(result==RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.DIGITS:
					result=form3(wi);
					if(result==RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.OP_LEFTBACKET:
					result=form3(wi);
					if(result==RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.OP_NEGATION:
					result=form3(wi);
					if(result==RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.OP_QUANTIFER:
					result=form3(wi);
					if(result==RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.WRONG:
					result=RESULT_FALSE;
					tempResult=false;
					break;
				}
				
				wi=token.getToken();
				if(wi.getToken()!=WordInfo.END){
					result=RESULT_FALSE;
				}
			}
			if((result==RESULT_FALSE)||(tempResult==false)){
				break;
			}
		}
		return result;
	}
	
	private boolean match(int wiToken,int synToken){
		if(wiToken==synToken){
			return true;
		}else{
			return false;
		}
	}
	
	/*term  -> variable op_math variable|variable -> variable{ op_math variable}*/
	private int term(WordInfo wi){
		if(match(wi.getToken(),WordInfo.VARIABLE)==true){
			wi=token.getToken();
			while(wi.getToken()==WordInfo.OP_MATH){
				wi=token.getToken();
				if(match(wi.getToken(),WordInfo.VARIABLE)==true){
					wi=token.getToken();
				}else{
					token.returnLastToken(wi);
					break;
				}
			}
			token.returnLastToken(wi);
			return RESULT_TRUE;
		}
		return RESULT_FALSE;
	}

	/*form0 -> predicate(term{,term})|digit op_compare digit -> predicate(term{,term})|digit op_compare digit*/
	private int form0(WordInfo wi){
		switch(wi.getToken()){
			case WordInfo.PREDICATE:
				wi=token.getToken();
				if(match(wi.getToken(),WordInfo.OP_LEFTBACKET)==true){
					wi=token.getToken();
					if(term(wi)==RESULT_TRUE){
						wi=token.getToken();
						while(wi.getToken()!=WordInfo.OP_RIGHTBACKET){
							if(match(wi.getToken(),WordInfo.OP_COMMAS)==true){
								wi=token.getToken();
								if(term(wi)==RESULT_TRUE){
									wi=token.getToken();
								}else{
									return RESULT_FALSE;
								}
							}else{
								return RESULT_FALSE;
							}
						}
						//token.returnLastToken(wi);
						return RESULT_TRUE;
					}else{
						return RESULT_FALSE;
					}
				}else{
					return RESULT_FALSE;
				}
			case WordInfo.DIGITS:
				wi=token.getToken();
				if(wi.getToken()==WordInfo.OP_COMPARE){
					wi=token.getToken();
					if(match(wi.getToken(),WordInfo.DIGITS)==true){
						wi=token.getToken();
					}else{
						return RESULT_FALSE;
					}
				}
				token.returnLastToken(wi);
				return RESULT_TRUE;
		}
		return RESULT_FALSE;
	}

	/*form1 -> op_quantifer variable {op_quantifer variable} (form3)|(form0)|~from0|form0*/
	private int form1(WordInfo wi){
		switch(wi.getToken()){
		case WordInfo.OP_NEGATION:
			wi=token.getToken();
			if(form0(wi)==RESULT_TRUE){
				return RESULT_TRUE;
			}
			break;
		case WordInfo.OP_QUANTIFER:
			wi=token.getToken();
			if(match(wi.getToken(),WordInfo.VARIABLE)==true){
				wi=token.getToken();
				while(match(wi.getToken(),WordInfo.OP_QUANTIFER)==true){
					wi=token.getToken();
					if(match(wi.getToken(),WordInfo.VARIABLE)==true){
						wi=token.getToken();
					}else{
						token.returnLastToken(wi);
						return RESULT_FALSE;
					}
				}
				if(form3(wi)==RESULT_TRUE){
					return RESULT_TRUE;
				}
			}
			break;
		case WordInfo.OP_LEFTBACKET:
			wi=token.getToken();
			if(form0(wi)==RESULT_TRUE){
				wi=token.getToken();
				if(match(wi.getToken(),WordInfo.OP_RIGHTBACKET)==true){
					return RESULT_TRUE;
				}
			}
			break;
		case WordInfo.PREDICATE:
			return form0(wi);
		case WordInfo.DIGITS:
			return form0(wi);
		}
		return RESULT_FALSE;
	}

	/*form2 -> form2 op_connectives1 form2|form1 -> form1{ op_connectives1 form2}*/
	private int form2(WordInfo wi){
		if(form1(wi)==RESULT_TRUE){
			wi=token.getToken();
			while(wi.getToken()==WordInfo.OP_CONNECTIVES1){
				wi=token.getToken();
				if(form2(wi)==RESULT_TRUE){
					wi=token.getToken();
				}else{
					break;
				}
			}
			token.returnLastToken(wi);
			return RESULT_TRUE;
		}
		return RESULT_FALSE;
	}

	/*form3 -> from3 op_connectives2 form3|form2 -> form2{ op_connectives2 form3}*/
	private int form3(WordInfo wi){
		if(form2(wi)==RESULT_TRUE){
			wi=token.getToken();
			while(wi.getToken()==WordInfo.OP_CONNECTIVES2){
				wi=token.getToken();
				if(form3(wi)==RESULT_TRUE){
					wi=token.getToken();
				}else{
					break;
				}
			}
			token.returnLastToken(wi);
			return RESULT_TRUE;
		}
		return RESULT_FALSE;
	}
	
	public List<String> getSentences(){
		if(sentences.size()>0){
			return sentences;
		}else{
			return null;
		}
	}
	
	public int getResult(){
		return this.result;
	}

}
