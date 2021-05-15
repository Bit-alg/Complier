package kc;

/**
 * @author 18-1-037-0110
 * 問題番号 問題 3.2
 * 提出日 2020年6月3日
 *
 * このクラスでは整数値や一部の算術演算子における字句解析を行う.
 */
class SLexicalAnalyzer {
    private FileScanner sourceFileScanner; // 入力ファイルのFileScannerへの参照

    /**
     * コンストラクタ
     * 引数のファイル名を利用してファイル読み取りをする
     */
    SLexicalAnalyzer(String sourceFileName) {
        this.sourceFileScanner = new FileScanner(sourceFileName);
    }

    /**
     * 次のトークンを参照する
     */
    Token nextToken() {
    		Token token = null; //トークンの種類
    		char currentChar; //現在読み込んでいる文字
    		String word = ""; // 連結する文字列
    		int value = 0; // 整数値を読み込む際に利用

    		/*
    		 * 空白文字を読み飛ばす
    		 */
    		do {
    			currentChar = sourceFileScanner.nextChar();
    		}while(currentChar == ' ');

    		/*
    		 * 行末記号を読み飛ばす
    		 */
    		while(currentChar == '\n'){
    			currentChar = sourceFileScanner.nextChar();
    		}

    		/*
    		 * 水平タブを読み飛ばす
    		 */
    		while(currentChar == '\t') {
    			currentChar = sourceFileScanner.nextChar();
    		}

    		/*
    		 * ファイル末に到達した時に実行される
    		 */
    		if(currentChar == '\0')
    			token = new Token(Symbol.EOF);


    		/*
    		 * 最初の文字が'+'の時に実行される
    		 */
    		else if(currentChar == '+') {
    			token = new Token(Symbol.ADD);
    		}

    		/*
    		 * 最初の文字が'!'の時に実行される
    		 */
    		else if(currentChar == '!') {
    			/*
    			 * 二文字目が'='の時に実行される
    			 */
    			if(sourceFileScanner.lookAhead()=='=') {
    				sourceFileScanner.nextChar();
    				token = new Token(Symbol.NOTEQ);
    			}

    			/*
    			 * '!'のみの時に実行される
    			 */
    			else {
    				token = new Token(Symbol.NOT);
    			}
    		}

    		/*
    		 * 最初の文字が'='の時に実行される
    		 */
    		else if(currentChar == '=') {
    			/*
    			 * 二文字目が'='の時に実行される
    			 */
    			if(sourceFileScanner.lookAhead()=='=') {
    				sourceFileScanner.nextChar();
    				token = new Token(Symbol.EQUAL);
    			}

    			/*
    			 *'='のみの時に実行される
    			 */
    			else {
    				token = new Token(Symbol.ASSIGN);
    			}
    		}

    		/*
    		 * 読み込んだ数字の最初が0の場合.
    		 */
    		else if(Character.digit(currentChar, 10) == 0) {
    			token = new Token(Symbol.INTEGER,0);
    		}
    		/*
    		 * 読み込んだ数字の最初が1~9の場合
    		 */
    		else if(Character.digit(currentChar, 10)!=0) {
    			//一文字目を読み込む
    			word = ""+currentChar;

    			/*
    			 * 次に読み取る文字が数値の間ループを続ける
    			 */
    			while(Character.isDigit(sourceFileScanner.lookAhead())) {
    				currentChar = sourceFileScanner.nextChar();
    				word += currentChar;
    			}
    			// String型からInteger型に変換して変数valueに格納している.
    			value = Integer.parseInt(word);
    			// tokenに整数値のオブジェクトを作成(0を除く)
    			token = new Token(Symbol.INTEGER, value);
    		}

    		/*
    		 * 構文エラーが起きた時に実行する
    		 */
    		else
    			syntaxError();

    		return token;

    }

    /**
     * ファイルを閉じる
     */
    void closeFile() {
    		sourceFileScanner.closeFile();
    }

    /**
     * 構文エラーが発生した際の処理をしている
     */
    private void syntaxError() {
        System.out.print (sourceFileScanner.scanAt());
        //下記の文言は自動採点で使用するので変更しないでください。
        System.out.println ("で字句解析プログラムが構文エラーを検出");
        closeFile();
        System.exit(1);
    }
}