package com.hbl.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.tamgitsun.object.WordInfo;

public final class TreeNode {
	//private final int MAXCHILD = 5;
	
	//public TreeNode[] child = new TreeNode[MAXCHILD];
	static public enum nodeKind {COMPARE,PREDICATE,VAR,DIGIT,CONN,NILL}
	static public HashMap<Integer,List<TreeNode>> map= new HashMap<Integer,List<TreeNode>>();
	public boolean isNegation = false; 
	private nodeKind treenodeKind = nodeKind.NILL;
	public WordInfo root = new WordInfo();
	
	//use for compute value
	//-1 for ?,0 for false,1 for true
	public int value = -1;
	//use for track all the TreeNode obj in the computing value step
	//唯一标识该node
	public int nodeHash = 0;
	//若treenodeKind = nodeKind.PREDICATE,记录该谓词下的所有常量
	public List<Integer> paramList = new ArrayList<Integer>();
	public List<TreeNode> child = new ArrayList<TreeNode>();
	
	public TreeNode(){
		
	}
	
	
	public TreeNode(WordInfo wi){
		root = wi;
		switch(wi.getToken()){
		case WordInfo.OP_CONNECTIVES1:
		case WordInfo.OP_CONNECTIVES2:
			treenodeKind = nodeKind.CONN;
			break;
		case WordInfo.PREDICATE:
			treenodeKind = nodeKind.PREDICATE;
			break;
		case WordInfo.VARIABLE:
			treenodeKind = nodeKind.VAR;
			break;
		case WordInfo.DIGITS:
			treenodeKind = nodeKind.DIGIT;
			break;
		case WordInfo.OP_COMPARE:
			treenodeKind = nodeKind.COMPARE;
			break;
		}
	}
	
	public void add(WordInfo wi){
		TreeNode tn = new TreeNode();
		tn.root = wi;
		child.add(tn);
	}
	
	public void displayTree(int blankNum){
		for(int i = 0; i < blankNum; i++){
			System.out.print(" ");
		}
		System.out.println("[" + root.getToken() + "," + root.getWord() + "]");
		for(TreeNode tn : child){
			tn.displayTree(blankNum + 1);
		}
	}
	
	public nodeKind getKind(){
		return this.treenodeKind;
	}
	
	public String toString(){
		String s = "";
		//deal with the node
		switch(root.getToken()){
		case 0:
			break;
		case WordInfo.OP_QUANTIFER:
			Stack<String> quanti = new Stack<String>();
			Stack<Variable> var = new Stack<Variable>();
			quanti.push(root.getWord());
			int childOrder = 0;
			var.push(Variable.getVariable(child.get(childOrder).root.getWord()));
			childOrder = childOrder + 1;
			TreeNode treeChild = child.get(childOrder);
			while(WordInfo.OP_QUANTIFER == treeChild.root.getToken()){
				quanti.push(treeChild.root.getWord());
				childOrder = childOrder + 1;
				treeChild = child.get(childOrder);
				var.push(Variable.getVariable(treeChild.root.getWord()));
				childOrder = childOrder + 1;
				treeChild = child.get(childOrder);
			}
			s = treeChild.toString();
			//每个量词遍历一次
			int size = quanti.size();
			for(int i = 1;i <= size;i++){
				String op;
				String tempS = "";
				Variable v = var.pop();
				//量词 $=任意，#=存在
				if("$".equals(quanti.pop()))
						op = "&";
				else
					op = "|";
				//遍历变量的值域
				int rangeOrder = 1;
				for(int q : v.getValRange()){
					String temp = "";
					temp = s.replace(v.toString(), String.valueOf(q));
					temp = "(".concat(temp).concat(")");
					if(1 != rangeOrder)
						temp = op.concat(temp);
					rangeOrder = rangeOrder + 1;
					tempS = tempS.concat(temp);
				}
				s = tempS;
			}
			return s;
		default:
			s = s.concat(root.getWord());
			break;
		}
		//deal with childs
		for(TreeNode child : this.child){
			s = s.concat(child.toString());
		}
		return s;
	}
	
}
