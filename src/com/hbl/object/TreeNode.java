package com.hbl.object;

import java.util.ArrayList;
import java.util.List;

import com.tamgitsun.object.WordInfo;

public final class TreeNode {
	//private final int MAXCHILD = 5;
	
	//public TreeNode[] child = new TreeNode[MAXCHILD];
	
	public WordInfo root = new WordInfo();
	
	
	public List<TreeNode> child = new ArrayList<TreeNode>();
	
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
	
}
