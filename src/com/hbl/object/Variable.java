package com.hbl.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Variable {
	private final List<Integer> valRange = new ArrayList<Integer>();
	private final String varName;
	static private Map<String,Variable> varMap = new HashMap<String,Variable>();
	
	private Variable(String varName){
		this.varName = varName;
	}
	
	private Variable(String varName,List<Integer> valRange){
		this.varName = varName;
		this.valRange.addAll(valRange);
	}
	
	static public Variable getVariable(String varName){
		//check if varName exit,exit return obj either create a new one
		if(varMap.containsKey(varName)){
			return varMap.get(varName);
		}
		else{
			Variable var = new Variable(varName);
			varMap.put(varName, var);
			return var;
		}
	}
	
	public List<Integer> setValRange(List<Integer> valRange){
		this.valRange.clear();
		this.valRange.addAll(valRange);
		return valRange;
	}
	
	public List<Integer> getValRange(){
		return valRange;
	}
	
	public String toString(){
		return varName;
	}
}
