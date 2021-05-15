package kc;

import java.util.ArrayList;

/**
 * @author 18-1-037-0110 花光大輔
 * 問題番号 問題 2.11
 * 提出日 2020年5月27日
 *
 * このクラスは変数を保存したり,新しく変数を作成,その変数に対するアドレスや変数型を調べたりする.
 */
class VarTable {

	private ArrayList<Var> varList; /*変数が入った配列*/
	private int nextAddress;  /*次に登録されるアドレス*/

    /**
     * コンストラクタ
     * varListを初期化して, 次に登録されるアドレス nextAddressも0に初期化する.
     */
    VarTable() {
        this.varList = new ArrayList<>();
        this.nextAddress = 0;
    }


    /**
     * 指定した変数の情報を返す.
     */
    @SuppressWarnings("unused") // ローカルメソッドを利用する際のおまじない.
	private Var getVar(String name) {
    		Var tempVar = null;

    		for(Var var:varList) {
    			/*
    			 * varList内にnameの変数がある場合実行される.
    			 */
    			if(name.equals(var.getName()))
    				tempVar = var;
    		}
    		return tempVar;
    }

    /**
     * 指定した変数が存在するかを真偽値を利用して返す.
     */
    boolean exist(String name) {
    	 Var var = getVar (name);
         return (var != null);
    }

    /**
     * 変数型, 変数名, 配列の場合サイズを指定して新しい変数を作成する.
     */
    boolean registerNewVariable(Type type, String name, int size) {
    		boolean temp = false;

    		/*
    		 * すでにnameと同じ名前の変数が存在しないかを確認して, なければ実行される.
    		 */
    		if(!exist(name)) {
    			temp = true;
    			varList.add(new Var(type,name,nextAddress,size));
    			nextAddress+=size;
    		}

    		return temp;

    }

    /**
     * 指定した変数のアドレス番地を返す.
     */
    int getAddress(String name) {
    		Var var = getVar(name);

    		if(var != null) return var.getAddress();

    		else return -1;
    }

    /**
     * 指定した変数の型を返す.
     */
    Type getType(String name) {

    	Var var = getVar (name);
        if (var != null) return var.getType();
        else return Type.NULL;
    }

    /**
     * 指定した変数の型が引数で利用されている型と一致するかを調べる.
     */
    boolean checkType(String name, Type type) {
    		Var var = getVar(name);
    		return (type.equals(var.getType()));

    }

    /**
     * 指定した変数のサイズを返す.
     */
    int getSize(String name) {
    		Var var = getVar(name);
    		if(var != null) return var.getSize();
    		else return -1;

    }

    /**
     * 動作確認用のメインメソッド
     * int型変数およびint型配列を表に登録し、その後登録された変数を表示する
     */
    public static void main(String[] args) {
    		VarTable varTable = new  VarTable(); /*VarTableオブジェクトの生成*/
    		String[] tempVar = {"var0","var1","var2","var3","var4"}; // 予め必要な変数をリスト内に作っておく

    		/*
    		 * for文を利用して4つ分のINT型の変数を作成する.
    		 */
    		for(int i=0;i<4;i++)
    			varTable.registerNewVariable(Type.INT, tempVar[i], 1);

    		/*
    		 * 配列型整数の変数を作成する
    		 */
    		varTable.registerNewVariable(Type.ARRAYOFINT, tempVar[4], 10);

    		/*
    		 * これまで登録した変数の型を調べてそれに応じたわかりやすい表示する.
    		 */
    		for(int i = 0;i <= varTable.getAddress(tempVar[4]);i++) {
    			if(varTable.checkType(tempVar[i],Type.INT))
    				System.out.printf("整数型のスカラーを格納している変数\n(変数型): %s, (アドレス番地): %d\n",varTable.getType(tempVar[i]).name(),varTable.getAddress(tempVar[i]));
    			else if(varTable.checkType(tempVar[i], Type.ARRAYOFINT))
    				System.out.printf("整数型配列を格納している変数\n(変数型): %s, (アドレス番地): %d,(サイズ): %d\n", varTable.getType(tempVar[i]).name(),varTable.getAddress(tempVar[i]),varTable.getSize(tempVar[i]));
    		}


    }
}