package com.tamgitsun.analysis;

import java.util.ArrayList;
import java.util.List;

import com.hbl.object.TreeNode;
import com.tamgitsun.object.Result;
import com.tamgitsun.object.WordInfo;
import com.tamgitsun.words.Token;

public class AnalysisTree {
	
	private AnalysisTree(){}
	
	public static AnalysisTree analysis=null;
	private static Object obj=new Object();
	private List<String> sentences=new ArrayList<String>();
	//private int rows=0;
	private Result result=new Result();
	private Token token=null;
	
	public static AnalysisTree getInstance(){
		if(analysis==null){
			synchronized(obj){
				if(analysis==null){
					analysis=new AnalysisTree();
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
	public Result doAnalysis(){
		result.result=Result.RESULT_TRUE;
		//rows=0;
		for(String s:sentences){
			Result result=new Result();
			token=Token.getInstance();
			token.setParagraph(s);
			WordInfo wi=token.getToken();
			boolean tempResult=true;
			if(wi.getToken()!=WordInfo.END){
				switch(wi.getToken()){
				case WordInfo.VARIABLE:
					result=term(wi);
					if(result.result==Result.RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.PREDICATE:
					result=form3(wi);
					if(result.result==Result.RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.DIGITS:
					result=form3(wi);
					if(result.result==Result.RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.OP_LEFTBACKET:
					result=form3(wi);
					if(result.result==Result.RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.OP_NEGATION:
					result=form3(wi);
					if(result.result==Result.RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.OP_QUANTIFER:
					result=form3(wi);
					if(result.result==Result.RESULT_FALSE){
						tempResult=false;
					}
					break;
				case WordInfo.WRONG:
					result.result=Result.RESULT_FALSE;
					tempResult=false;
					break;
				}
				
				wi=token.getToken();
				if(wi.getToken()!=WordInfo.END){ 
					result.result=Result.RESULT_FALSE;
				}
			}                                                                                                                                        
			if((result.result==Result.RESULT_FALSE)||(tempResult==false)){
				this.result=result;//将错误的result返回
				break;
			}else{
				this.result.tree.child.add(result.tree);//将该句的树添加到要返回的result
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
	private Result term(WordInfo wi){
		Result result=new Result();
		result.tree.root=wi;
		if(match(wi.getToken(),WordInfo.VARIABLE)==true){
			wi=token.getToken();
			while(wi.getToken()==WordInfo.OP_MATH){
				result.tree.add(wi);
				wi=token.getToken();
				if(match(wi.getToken(),WordInfo.VARIABLE)==true){
					result.tree.add(wi);
					wi=token.getToken();
				}else{
					token.returnLastToken(wi);
					break;
				}
			}
			token.returnLastToken(wi);
			return result;
		}
		result.result=Result.RESULT_FALSE;
		return result;
	}

	/*form0 -> predicate(term{,term})|digit op_compare digit -> predicate(term{,term})|digit{ op_compare digit}*/
	private Result form0(WordInfo wi){
		Result result=new Result();
		switch(wi.getToken()){
			case WordInfo.PREDICATE:
				result.tree.root=wi;
				wi=token.getToken();
				if(match(wi.getToken(),WordInfo.OP_LEFTBACKET)==true){
					result.tree.add(wi);
					wi=token.getToken();
					Result re=term(wi);
					if(re.result==Result.RESULT_TRUE){
						result.tree.child.add(re.tree);
						wi=token.getToken();
						while(wi.getToken()!=WordInfo.OP_RIGHTBACKET){
							if(match(wi.getToken(),WordInfo.OP_COMMAS)==true){
								result.tree.add(wi);
								wi=token.getToken();
								Result re1=term(wi);
								if(re1.result==Result.RESULT_TRUE){
									result.tree.child.add(re1.tree);
									wi=token.getToken();
								}else{
									result.result=Result.RESULT_FALSE;
									return result;
								}
							}else{
								result.result=Result.RESULT_FALSE;
								return result;
							}
						}
						result.tree.add(wi);
						//token.returnLastToken(wi);
						return result;
					}else{
						result.result=Result.RESULT_FALSE;
						return result;
					}
				}else{
					result.result=Result.RESULT_FALSE;
					return result;
				}
			case WordInfo.DIGITS:
				result.tree.root=wi;
				wi=token.getToken();
				while(wi.getToken()==WordInfo.OP_COMPARE){
					result.tree.add(wi);
					wi=token.getToken();
					if(match(wi.getToken(),WordInfo.DIGITS)==true){
						result.tree.add(wi);
						wi=token.getToken();
					}else{
						result.result=Result.RESULT_FALSE;
						return result;
					}
				}
				token.returnLastToken(wi);
				return result;
		}
		result.result=Result.RESULT_FALSE;
		return result;
	}

	/*form1 -> op_quantifer variable {op_quantifer variable} form3|(form0)|~from0|form0*/
	private Result form1(WordInfo wi){
		Result result=new Result();
		switch(wi.getToken()){
		case WordInfo.OP_NEGATION:
			result.tree.root=wi;
			wi=token.getToken();
			Result re=form0(wi);
			if(re.result==Result.RESULT_TRUE){
				result.tree.child.add(re.tree);
				return result;
			}
			break;
		case WordInfo.OP_QUANTIFER:
			result.tree.root=wi;
			wi=token.getToken();
			if(match(wi.getToken(),WordInfo.VARIABLE)==true){
				result.tree.add(wi);
				wi=token.getToken();
				while(match(wi.getToken(),WordInfo.OP_QUANTIFER)==true){
					result.tree.add(wi);
					wi=token.getToken();
					if(match(wi.getToken(),WordInfo.VARIABLE)==true){
						result.tree.add(wi);
						wi=token.getToken();
					}else{
						token.returnLastToken(wi);
						result.result=Result.RESULT_FALSE;
						return result;
					}
				}
				if(WordInfo.OP_LEFTBACKET == wi.getToken()){
					wi = token.getToken();
				}
				Result re1=form3(wi);
				token.getToken();
				if(re1.result==Result.RESULT_TRUE){
					result.tree.child.add(re1.tree);
					return result;
				}
			}
			break;
		case WordInfo.OP_LEFTBACKET:
			result.tree.root=wi;
			wi=token.getToken();
			Result re1=form0(wi);
			if(re1.result==Result.RESULT_TRUE){
				result.tree.child.add(re1.tree);
				wi=token.getToken();
				if(match(wi.getToken(),WordInfo.OP_RIGHTBACKET)==true){
					result.tree.add(wi);
					return result;
				}
			}
			break;
		case WordInfo.PREDICATE:
			return form0(wi);
		case WordInfo.DIGITS:
			return form0(wi);
		}
		result.result=Result.RESULT_FALSE;
		return result;
	}

	/*form2 -> form2 op_connectives1 form2|form1 -> form1{ op_connectives1 form2}*/
	private Result form2(WordInfo wi){
		Result result=new Result();
		Result re=form1(wi);
		if(re.result==Result.RESULT_TRUE){
			result.tree.child.add(re.tree);
			wi=token.getToken();
			while(wi.getToken()==WordInfo.OP_CONNECTIVES1){
				result.tree.add(wi);
				wi=token.getToken();
				Result re1=form2(wi);
				if(re1.result==Result.RESULT_TRUE){
					result.tree.child.add(re1.tree);
					wi=token.getToken();
				}else{
					break;
				}
			}
			token.returnLastToken(wi);
			return result;
		}
		result.result=Result.RESULT_FALSE;
		return result;
	}

	/*form3 -> from3 op_connectives2 form3|form2 -> form2{ op_connectives2 form3}*/
	private Result form3(WordInfo wi){
		Result result=new Result();
		Result re=form2(wi);
		if(re.result==Result.RESULT_TRUE){
			result.tree.child.add(re.tree);
			wi=token.getToken();
			while(wi.getToken()==WordInfo.OP_CONNECTIVES2){
				result.tree.add(wi);
				wi=token.getToken();
				Result re1=form3(wi);
				if(re1.result==Result.RESULT_TRUE){
					result.tree.child.add(re1.tree);
					wi=token.getToken();
				}else{
					break;
				}
			}
			token.returnLastToken(wi);
			return result;
		}
		result.result=Result.RESULT_FALSE;
		return result;
	}
	
	public List<String> getSentences(){
		if(sentences.size()>0){
			return sentences;
		}else{
			return null;
		}
	}
	
	public int getResult(){
		return this.result.result;
	}
	
	public TreeNode getTree(){
		return this.result.tree;
	}

}
