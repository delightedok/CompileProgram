package com.hbl.object;

import java.util.ArrayList;
import java.util.List;

import com.hbl.object.TreeNode.nodeKind;
import com.tamgitsun.object.WordInfo;
import com.tamgitsun.words.Token;

//将扩展后的范式转化为树
//exp->exp2 {op exp2}
//exp2->~exp3|exp3
//exp3->(exp)|predicate(digit{,digit})|digit op_compare digit
public class Analysis2 {
	private Token token = Token.getInstance();
	private WordInfo wi;
	
	public TreeNode doAnalysis(String arg){
		token.setParagraph(arg);
		TreeNode root = exp();
		return root;
	}
	
	//遍历root,为每一个节点计算唯一标识码nodeHash
	public int walkTree(TreeNode rootTree){
		if(nodeKind.CONN == rootTree.getKind()){
			int x = walkTree(rootTree.child.get(0));
			int y = walkTree(rootTree.child.get(1));
			String temp = String.format("%d%d", x+y,x*y);
			String hash = rootTree.root.getWord() + temp;
			rootTree.nodeHash = hash.hashCode();
			//hashmap不含有该node，则添加list<TreeNode>,且将node加入list
			//否则直接将node加入list
			if(!TreeNode.map.containsKey(rootTree.nodeHash)){
				List<TreeNode> a = new ArrayList<TreeNode>();
				a.add(rootTree);
				TreeNode.map.put(rootTree.nodeHash, a);
			}
			else{
				TreeNode.map.get(rootTree.nodeHash).add(rootTree);
			}
		}
		else if(nodeKind.PREDICATE == rootTree.getKind()){
			//设置该node的唯一标识
			//将其添加入hashmap中
			int x = 0;
			int y = 1;
			int z = 0;
			for(int a :rootTree.paramList){
				z = z + 1;
				x = x + a;
				y = y * a;
			}
			String temp = String.format("%d%d%d", x,y,z);
			String hash = rootTree.root.getWord()+temp;
			rootTree.nodeHash = hash.hashCode();
			//hashmap不含有该node，则添加list<TreeNode>,且将node加入list
			//否则直接将node加入list
			if(!TreeNode.map.containsKey(rootTree.nodeHash)){
				List<TreeNode> a = new ArrayList<TreeNode>();
				a.add(rootTree);
				TreeNode.map.put(rootTree.nodeHash, a);
			}
			else{
				TreeNode.map.get(rootTree.nodeHash).add(rootTree);
			}
		}
		return rootTree.nodeHash;
	}
	
	//遍历tree,计算每一个节点的值(false=0,true=1,未知=-1)
	//如果是根节点则预先设置值为true
	public void computeValue(TreeNode tree,boolean isRoot){
		if(isRoot)
			tree.value = 1;
		switch(tree.root.getWord()){
		case "$":
			//若op=合取
			//值=1，其子节点值必须全部为1，若出现0则该范式条件不可满足
			if(tree.value == 1){
				int x = tree.child.get(0).value;
				int y = tree.child.get(1).value;
				if(x==0 || y==0){
					System.out.println("该范式为false");
					System.exit(0);
				}
				else{
					tree.child.get(0).value = 1;
					tree.child.get(1).value = 1;
				}
			}
			else if(tree.value == 0){
				int x = tree.child.get(0).value;
				int y = tree.child.get(1).value;
				if(x+y==2){
					System.out.println("该范式为false");
					System.exit(0);
				}
				else{
					if(x==1)
						tree.child.get(1).value = 0;
					else if(y==1)
						tree.child.get(0).value = 0;
				}
			}
			break;
		case "|":
			//析取
			//值=0,两个子节点必为0
			if(tree.value == 0){
				int x = tree.child.get(0).value;
				int y = tree.child.get(1).value;
				if(x==1 || y==1){
					System.out.println("该范式为false");
					System.exit(0);
				}
				else{
					tree.child.get(0).value = 0;
					tree.child.get(1).value = 0;
				}
			}
			else if(tree.value == 1){
				int x = tree.child.get(0).value;
				int y = tree.child.get(1).value;
				if(x+y==0){
					System.out.println("该范式为false");
					System.exit(0);
				}
				else{
					if(x==0)
						tree.child.get(1).value = 1;
					else if(y==0)
						tree.child.get(0).value = 1;
				}
			}
			break;
		case "->":
			//值=0,child[0]必=1,child[1]必=0
			if(tree.value == 0){
				int x = tree.child.get(0).value;
				int y = tree.child.get(1).value;
				if(x==0||y==1){
					System.out.println("该范式为false");
					System.exit(0);
				}
				else{
					tree.child.get(0).value = 0;
					tree.child.get(1).value = 1;
				}
			}
			else if(tree.value == 1){
				int x = tree.child.get(0).value;
				int y = tree.child.get(1).value;
				if(x==0 && y==1){
					System.out.println("该范式为false");
					System.exit(0);
				}
				else{
					if(x==1){
						tree.child.get(1).value = 1;
					}
				}
			}
		}
	}
	
	//根节点属性: null,谓词predicate,变量var或连接词(|,&,->)
	//exp->exp2 {op exp2}
	private TreeNode exp(){
		TreeNode p = exp2();
		while(match(WordInfo.OP_CONNECTIVES1)
				| match(WordInfo.OP_CONNECTIVES2)){
			TreeNode t = new TreeNode(wi);
			t.child.add(p);
			t.child.add(exp2());
			p = t;
		}
		return p;
	}
	
	//exp2->~exp3|exp3
	private TreeNode exp2(){
		if(match(WordInfo.OP_NEGATION)){
			TreeNode ret = exp3();
			ret.isNegation = true;
			return ret;
		}
		return exp3();
	}
	
	//exp3->(exp)|predicate(digit{,digit})|digit op_compare digit
	private TreeNode exp3(){
		if(match(WordInfo.OP_LEFTBACKET)){
			TreeNode p = exp();
			match(WordInfo.OP_RIGHTBACKET,true);
			return p;
		}
		else if(match(WordInfo.PREDICATE)){
			TreeNode p = new TreeNode(wi);
			match(WordInfo.OP_LEFTBACKET,true);
			while(match(WordInfo.DIGITS,true)){
				p.paramList.add(Integer.parseInt(wi.getWord()));
				if(match(WordInfo.OP_COMMAS) == false)
					break;
			}
			match(WordInfo.OP_RIGHTBACKET,true);
			return p;
		}
		else if(match(WordInfo.DIGITS)){
			TreeNode p = new TreeNode(wi);
			match(WordInfo.OP_COMPARE,true);
			TreeNode q = new TreeNode(wi);
			q.child.add(p);
			match(WordInfo.DIGITS,true);
			q.child.add(new TreeNode(wi));
			return q;
		}
		else{
			System.out.println("analysis exp3 error at"+wi.getWord());
			System.exit(0);
			return null;
		}
			
}
	//消耗一个token,比较,成功则返回true同时赋值给this.wi，否则false结束输出错误信息
	//exit mode
	private boolean match(int value,boolean exit){
		WordInfo word = token.getToken();
		if(word.getToken() == value){
			wi = word;
			return true;
		}
		else{
			token.returnLastToken(word);
			if(exit == true){
				System.out.println("error ,缺少"+word.getWord());
				System.exit(0);
			}
			return false;
		}
	}
	
	//no exit mode
	private boolean match(int value){
		WordInfo word = token.getToken();
		if(word.getToken() == value){
			wi = word;
			return true;
		}
		else{
			token.returnLastToken(word);
			return false;
		}
	}
}


