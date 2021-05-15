package kc;

/**
 *
 * @author 18-1-037-0110
 * 問題番号 問題 3.1
 * 提出日 2020年6月3日
 *
 * このクラスはファイルで読み取ったトークンの種類に関して処理する.
 */
class Token {

	private Symbol symbol; // トークンの種別を表す.
	private int intValue; // トークンの種別が整数or文字の時に,その整数値or文字コードを保持する.
	private String strValue; // トークンの種別が名前or文字列である時, それを表す文字列を保持する.

    /**
     * コンストラクタ
     * 整数, 文字, 名前以外のトークンを生成するための, トークンの種別のみを引数にしている
     */
    Token(Symbol symbol) {
       this.symbol = symbol;
    }

    /**
     * コンストラクタ
     * 整数, 文字のトークンを生成するための, トークンの種別と値を引数にしている
     */
    Token(Symbol symbol, int intValue) {
       this.symbol = symbol;
       this.intValue = intValue;
    }


    /**
     * コンストラクタ
     * 名前, 文字列のトークンを生成するための, トークンの種別と文字列を引数とする
     */
    Token(Symbol symbol, String strValue) {
       this.symbol = symbol;
       this.strValue = strValue;
    }


    /**
     * フィールドにあるsymbolが, 引数のsymbolと一致しているかを調べる
     */
    boolean checkSymbol(Symbol symbol) {
    		boolean tempBool = false;

    		if(this.symbol == symbol)
    			tempBool = true;

    		return tempBool;

    }

    /**
     * フィールドのsymbolを返す
     */
    Symbol getSymbol() {
    		return symbol;
    }


    /**
     * フィールドのintValueを返す
     */
    int getIntValue() {
    		return intValue;
    }

    /**
     * フィールドのstrValueを返す
     */
    String getStrValue() {
    		return strValue;
    }
}