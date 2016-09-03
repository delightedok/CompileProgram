package com.tamgitsun.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.hbl.object.*;
import com.tamgitsun.analysis.AnalysisTree;
import com.tamgitsun.object.Result;
import com.tamgitsun.object.WordInfo;

public class TreeTest {
//输入一个文件路径参数
	public static void main(String args[]){
		/***read file***/
		List<String> strs = new ArrayList<String>();
		try {
			strs = Files.readAllLines(Paths.get(args[0]));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(strs.size() == 0){
			System.out.println("Nothing in the file");
			System.exit(0);
		}
		/***read file end***/
		
		System.out.println("--------------test syntax tree start-----------");
		for(String str:strs){
			AnalysisTree at = AnalysisTree.getInstance();
			at.setSentences(str);
			Result result = at.doAnalysis();
			if(result.result == Result.RESULT_TRUE){
				System.out.println("correct");
			}else{
				System.out.println("incorrect");
			}
			TreeNode tree = result.tree;
			System.out.println("tree size:" + tree.child.size());
			for(TreeNode temp : tree.child){
				System.out.println(temp.root.getWord());
			}
			//显示语法树（没有展开的）
			tree.displayTree(0);
			//添加论域
			List<Integer> range = new ArrayList<Integer>();
			range.add(1);
			range.add(2);
			Variable.getVariable("x").setValRange(range);
			Variable.getVariable("y").setValRange(range);
			
			//置换变量为论域中的数，展开语法树，结果保存为String
			String res = tree.toString();
			System.out.println(res);
			
			//分析展开后的语法树，组成新的树，用于化简
			//新的树的例子：f(1) | f(2)
			//root:|,child[0]:f(1),child[1]:f(2)
			Analysis2 ana2 = new Analysis2();
			TreeNode node = ana2.doAnalysis(res);
			//遍历新树，为每一个节点附加唯一标识
			//如：节点f(1)|f(2)的唯一标识等于f(2)|f(1)
			//节点f(1,2,3)的唯一标识等于f(3,2,1)
			ana2.walkTree(node);
			//遍历新树，根据已有的节点值尝试为子节点赋值
			ana2.computeValue(node, true);
			node.displayTree(0);
		}
		System.out.println("--------------test syntax tree end-------------");
	}
	
}
