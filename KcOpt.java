package kc;
import java.util.ArrayList;

/**
 * 構文解析,基本的なコード生成に加えて以下の拡張をした.
 * 1. コメントの記述を可能とした
 * 2. 行末コメントの記述を可能とした
 * 3. for文において,初期化式と更新式を複数記述できるようにした
 * 4. 配列要素の後置 ++ の導入
 * 5. if文におけるelse節の導入をした
 * 6. do-while文の導入をした.
 * @author 18-1-037-0110
 * 問題番号 4.1 5.1 6.1
 * 提出日 2020/8/5
 *
 */
class KcOpt {

	private LexicalAnalyzer lexer; /*使用する字句解析器*/
	private Token token; /*字句解析から受け取ったトークン*/
	private VarTable variableTable; /*変数表を指し示す*/
	private PseudoIseg iseg; /*アセンブラコード表*/
	private boolean inLoop = false; /*ループ内部にいるかの判定*/
	private ArrayList<Integer> valueList; /*配列の要素を一時的に入れておく配列リスト*/
	private Symbol op = null; /*記号を格納しておくための変数*/
	private ArrayList<Integer> breakAddrList; /*break文のJUMP命令の番地を記憶する*/
	private boolean firstCopy = true;
	private int jumpAddrSwitch;
	private ArrayList<Integer> copyList;
	private int count;
	private int bneAddr;

	/**
	 * コンストラクタの生成
	 */
	KcOpt(String sourceFileName) {
		lexer = new LexicalAnalyzer(sourceFileName);
		token = lexer.nextToken();
		variableTable = new VarTable();
		iseg = new PseudoIseg();
		count = 0;
	}

	/**
	 * 非終端記号<Program>の構文解析をする
	 */
	void parseProgram() {
		/*
		 * トークンがmainの時にネスト内が実行される
		 */
		if (token.checkSymbol(Symbol.MAIN))
			parseMain_function(); /*parseMain_functionメソッドへ移動する*/
		else
			syntaxError("main が期待されます");


		/*
		 * ファイル末ならファイルを閉じる.
		 */
		if (token.checkSymbol(Symbol.EOF)) {
			iseg.appendCode(Operator.HALT); /*プログラム終了のコード生成*/
			closeFile();
		} else
			syntaxError("ファイル末ではありません"); /*ファイル末ではない場合*/
	}

	/**
	 * 非終端記号<Main_Function>の構文解析をする
	 */
	void parseMain_function() {
		/*
		 * トークンがmainだったため次のトークンを取り出す
		 */
		token = lexer.nextToken();
		/*
		 * トークンが'('なら次のトークンを取り出す
		 */
		if (token.checkSymbol(Symbol.LPAREN))
			token = lexer.nextToken();
		else
			syntaxError("( が期待されます");
		/*
		 * トークンが')'なら次のトークンを取り出す
		 */
		if (token.checkSymbol(Symbol.RPAREN))
			token = lexer.nextToken();
		else
			syntaxError(") が期待されます");
		/*
		 * トークンが { の時にネスト内を実行する
		 */
		if (token.checkSymbol(Symbol.LBRACE))
			parseBlock(); /*parseBlockメソッドへ移動する*/
		else
			syntaxError("{ が期待されます");
	}

	/**
	 * 非終端記号<Block>の構文解析をする
	 */
	void parseBlock() {
		/*
		 * 現在のトークンが { だったので次のトークンを取り出す
		 */
		token = lexer.nextToken();
		/*
		 * 現在のトークンが<Var_decl>のFirst集合{int}に属している間
		 * parseVar_declメソッドを実行する
		 */
		while (token.checkSymbol(Symbol.INT))
			parseVar_decl(); /*parseVar_declメソッドへ移動する*/

		/*
		 * 現在のトークンが<Statement>のFirst集合に属している間
		 * parseStatementメソッドを実行する
		 */
		while (isParseStatement(token))
			parseStatement();
		/*
		 * 現在のトークンが } なら次のトークンを取り出す.
		 */
		if (token.checkSymbol(Symbol.RBRACE))
			token = lexer.nextToken();
		else
			syntaxError("} が期待されます");
	}

	/**
	 * 非終端記号<Var_decl>の構文解析をする
	 */
	void parseVar_decl() {
		/*
		 * 現在のトークンが int なら次のトークンを取り出す.
		 */
		if (token.checkSymbol(Symbol.INT))
			token = lexer.nextToken();
		else
			syntaxError("int が期待されます");
		/*
		 * 現在のトークンがNAMEならparseName_listメソッドへ移動する
		 */
		if (token.checkSymbol(Symbol.NAME)) {
			parseName_list();
		}
		else
			syntaxError("変数名が期待されます");

		/*
		 * 現在のトークンが ; なら次のトークンを取り出す.
		 */
		if (token.checkSymbol(Symbol.SEMICOLON))
			token = lexer.nextToken();
		else
			syntaxError("';'が期待されます");
	}

	/**
	 * 非終端記号<Name_list>の構文解析をする
	 */
	void parseName_list() {

		if(token.checkSymbol(Symbol.NAME))
		parseName(); /*parseNameメソッドに移動する*/

		/*
		 * 現在のトークンが , ならネスト内をループする
		 */
		while (token.checkSymbol(Symbol.COMMA)) {
			/*
			 * トークン , の次のトークンを取り出す
			 */
			token = lexer.nextToken();
			/*
			 * 現在のトークンがFirst(<Name>)に属しているならparseNameへ移動する.
			 */
			if (token.checkSymbol(Symbol.NAME))
				parseName();
			else
				syntaxError("整数値or文字が期待されます");
		}
	}

	/**
	 * このメソッドは<Constant_list>のfirst集合が指定された記号に該当されるかどうかを返す.
	 * また<Constant>のFirst集合も同じ要素の集合である
	 * First(<Constant_list>) = {-, INT, CHAR} = First(<Constant>)
	 * @param symbol 指定された記号列
	 * @return 真偽値を返す
	 */
	boolean isParseConstant_list(Token token) {
		boolean temp = false; // 真偽値の初期化
		/*
		 * 引数の記号列が'-' or 整数値 or 文字の場合,真を返す.
		 */
		if (token.checkSymbol(Symbol.SUB) || token.checkSymbol(Symbol.INTEGER) || token.checkSymbol(Symbol.CHARACTER))
			temp = true;
		return temp;
	}

	/**
	 * 非終端記号<Name>の構文解析をする
	 */
	void parseName() {

		String name = token.getStrValue(); /*変数名の取得*/
		/*
		 * 取得した変数が既に存在する場合
		 */
		if(variableTable.exist(name))
			syntaxError("二重登録です");

		token = lexer.nextToken(); /*次のトークンを取得する*/
		/*
		 * トークンが '=',もしくは'['ならばネスト内を実行する
		 */
		if (token.checkSymbol(Symbol.ASSIGN) || token.checkSymbol(Symbol.LBRACKET)) {
			switch (token.getSymbol()) {
			/*トークンが'='の場合*/
			case ASSIGN:
				token = lexer.nextToken(); /*トークン'='を読み飛ばす*/
				int value = 0;
				/*
				 * 非終端記号の<Constant>のfirst集合に該当するならネスト内に実行する.
				 */
				if (isParseConstant_list(token))
					value = parseConstant();
				else
					syntaxError("整数値もしくは文字が期待されます");
				/*
				 * スカラ変数を変数表に登録する. (int型,要素1)
				 */
				variableTable.registerNewVariable(Type.INT, name, 1);
				int address = variableTable.getAddress(name); /*変数表を参照してnameの番地を得る*/
				iseg.appendCode(Operator.PUSHI,value); /*初期値に積む*/
				iseg.appendCode(Operator.POP,address); /*Dsegに代入*/
				break;

			/*トークンが'['の場合*/
			case LBRACKET:
				token = lexer.nextToken(); /*トークン'['を読み飛ばす*/
				/*
				 * トークンが整数値なら次のトークンを読む
				 */
				if (token.checkSymbol(Symbol.INTEGER)) {

					int size = token.getIntValue(); /*配列のサイズとしてINTEGERの値をsizeに格納する*/

					token = lexer.nextToken();
					/*
					 * トークンが']'なら次のトークンを読む
					 */
					if (token.checkSymbol(Symbol.RBRACKET))
						token = lexer.nextToken();
					else
						syntaxError("] が期待されます");


					/*
					 * 二次元配列の第二要素のはじめ.
					 */
					if(token.checkSymbol(Symbol.LBRACKET)) {
						token = lexer.nextToken(); /*トークン [ を読み飛ばす*/
						/*
						 * 添え字が整数値ならネスト内を通過
						 */
						if(token.checkSymbol(Symbol.INTEGER)) {
							token = lexer.nextToken(); /*トークン INTEGERを読み飛ばす*/
						}
						else syntaxError("整数値が期待されます.");
						/*
						 * トークンが ] ならネスト内を通過
						 */
						if(token.checkSymbol(Symbol.RBRACKET)) {
							token = lexer.nextToken(); /*トークン ] を読み飛ばす*/
						}
						else syntaxError(" ] が期待されます.");
					}


					/*
					 * サイズの決まった配列を変数表に格納する
					 */
					variableTable.registerNewVariable(Type.ARRAYOFINT, name, size);
				}
				/*
				 * トークンが']'の場合次のトークンを読む
				 */
				else if (token.checkSymbol(Symbol.RBRACKET)) {
					token = lexer.nextToken();
					/*
					 * トークンが'='なら次のトークンを読む
					 */
					if (token.checkSymbol(Symbol.ASSIGN))
						token = lexer.nextToken();
					else
						syntaxError("= が期待されます");
					/*
					 * トークンが'{'なら次のトークンを読む
					 */
					if (token.checkSymbol(Symbol.LBRACE))
						token = lexer.nextToken();
					else
						syntaxError("{ が期待されます");
					/*
					 * トークンが<Constant>のfirst集合に該当するならparseConstant_listメソッドへ移動する.
					 */
					if (isParseConstant_list(token))
						parseConstant_list();
					else
						syntaxError("整数値or文字が期待されます");
					/*
					 * トークンが'}'なら次のトークンを読む
					 */
					if (token.checkSymbol(Symbol.RBRACE))
						token = lexer.nextToken();
					else
						syntaxError("} が期待されます");

					int size = valueList.size(); // 初期値の個数を得る
					/*
					 * 配列のサイズが不明な配列を変数表に格納する
					 */
					variableTable.registerNewVariable(Type.ARRAYOFINT, name, size);

					address = variableTable.getAddress(name); //配列の先頭のアドレスを得る

					for(int i = 0;i<size;++i) {
						iseg.appendCode(Operator.PUSHI,valueList.get(i)); //i番目の初期値を積む
						iseg.appendCode(Operator.POP,address + i); // Dsegに格納
					}
				} else
					syntaxError("整数値もしくは']'が期待されます");
				break;

			default:
				syntaxError("'='もしくは'['が期待されます");
				break;
			}
		}
		/*
		 * 変数名単体の時に変数表に登録する.
		 */
		else
			variableTable.registerNewVariable(Type.INT, name, 1);
	}

	/**
	 * 非終端記号<Constant_list>の構文解析をする
	 */
	void parseConstant_list() {

		int value = 0;

		valueList = new ArrayList<>();

		/*
		 * トークンが - or 整数値 or 文字の間ネスト内をループする
		 */
		if (isParseConstant_list(token)) {
			value = parseConstant(); // parseConstanメソッドに移動
		}
		else
			syntaxError("整数値もしくは文字が期待されます");

		valueList.add(value); //配列の要素を格納する
		/*
		 * トークンが , の間ネスト内をループする
		 * 配列要素の数が複数存在するときにこのループ内を通る
		 */
		while (token.checkSymbol(Symbol.COMMA)) {

			/*
			 * トークンが , だったので次のトークンに移す
			 */
			token = lexer.nextToken();
			/*
			 * トークンが - or 整数値 or 文字の間ネスト内をループする
			 */
			if (isParseConstant_list(token))
				value = parseConstant();
			else
				syntaxError("整数値or文字が期待されます");

			valueList.add(value); //配列の要素を格納する.
		}
	}

	/**
	 * 非終端記号<Constant>の構文解析をする
	 */
	int parseConstant() {
		/*
		 * トークンが指定された記号列に応じて異なる処理をする(今回の場合は - or 整数値 or 文字に分岐する)
		 */
		switch (token.getSymbol()) {
		/*
		 * トークンが - の場合
		 */
		case SUB:
			int subValue = 0;
			/*
			 * トークンが - の場合,次のトークンに移す
			 */
			token = lexer.nextToken();
			/*
			 * トークンが整数値の場合,次のトークンに移す
			 */
			if (token.checkSymbol(Symbol.INTEGER)) {
				subValue = token.getIntValue();
				token = lexer.nextToken();
			}
			else
				syntaxError("整数値が期待されます");
			return -subValue; /*整数値にマイナスをつけた値を返す*/
		/*
		* トークンが整数値の場合
		*/
		case INTEGER:
			int value = token.getIntValue();
			token = lexer.nextToken();
			return value; /*整数値を返す*/
		/*
		 * トークンが文字の場合
		 */
		case CHARACTER:
			int charValue = token.getIntValue();
			token = lexer.nextToken();
			return charValue; /*文字コードを返す*/

		default:
			syntaxError("整数値or文字が期待されます");
			return -1;
		}
	}

	/**
	 * 非終端記号<Statement>のFirst集合を判別する
	 * First(<Statement>) = {if, while, for, outputchar, outputint, break, {, ;}に加えて
	 * First(<Exp_statement>)すなわちFirst(<Arithmetic_factor>)も追加したものである.
	 * @param token 指定したトークン
	 * @return 真偽値を返す.
	 */
	boolean isParseStatement(Token token) {
		boolean temp = false;

		if (token.checkSymbol(Symbol.IF) || token.checkSymbol(Symbol.WHILE) || token.checkSymbol(Symbol.FOR)
				|| token.checkSymbol(Symbol.OUTPUTCHAR)||token.checkSymbol(Symbol.CASE)||token.checkSymbol(Symbol.DEFAULT)
				|| token.checkSymbol(Symbol.OUTPUTINT) || token.checkSymbol(Symbol.BREAK)
				|| token.checkSymbol(Symbol.LBRACE)||token.checkSymbol(Symbol.SWITCH)
				|| token.checkSymbol(Symbol.SEMICOLON) ||token.checkSymbol(Symbol.DO)|| isParseArithmetic_factor(token))
			temp = true;

		return temp;
	}

	/**
	 * 非終端記号<Statement>の構文解析をする
	 */
	void parseStatement() {
		/*
		 * 現在のトークンがFirst(<Exp_statement>)に属しているならparseExp_statementメソッドに移動する
		 */
		if (isParseArithmetic_factor(token))
			parseExp_statement();
		/*現在のトークンがIFの場合*/
		else if(token.checkSymbol(Symbol.IF)){
			parseIf_statement();
		}
		/*現在のトークンがWHILEの場合*/
		else if(token.checkSymbol(Symbol.WHILE)) {
			parseWhile_statement();
		}
		/*現在のトークンがDOの場合(Do-while文の解析へ移動する)*/
		else if(token.checkSymbol(Symbol.DO)) {
			parseDoWhile_statement();
		}
		/*現在のトークンがSWITCHの場合*/
		else if(token.checkSymbol(Symbol.SWITCH)) {
			parseSwitch_statement();
		}
		/*現在のトークンがCASEの場合*/
		else if(token.checkSymbol(Symbol.CASE)) {
			parseCase_label();
		}
		/*現在のトークンがDEFAULTの場合*/
		else if(token.checkSymbol(Symbol.DEFAULT)) {
			parseDefault_label();
		}
		/*現在のトークンがFORの場合*/
		else if(token.checkSymbol(Symbol.FOR)) {
			parseFor_statement();
		}
		/*現在のトークンがOUTPUTCHARの場合*/
		else if(token.checkSymbol(Symbol.OUTPUTCHAR)) {
			parseOutputchar_statement();
		}
		/*現在のトークンがOUTPUTINTの場合*/
		else if(token.checkSymbol(Symbol.OUTPUTINT)) {
			parseOutputint_statement();
		}
		/*現在のトークンがBREAKの場合*/
		else if(token.checkSymbol(Symbol.BREAK)) {
				parseBreak_statement();
		}
		/*現在のトークンが { の場合*/
		else if(token.checkSymbol(Symbol.LBRACE)) {
			token = lexer.nextToken();
			/*
			 * 現在のトークンがFirst(<Statement>)に属していた場合,ParseStatementメソッドに移動する.
			 */
			while(isParseStatement(token))
				parseStatement();
			/*
			 * 現在のトークンが } なら次のトークンを取り出す.
			 */
			if(token.checkSymbol(Symbol.RBRACE)) token = lexer.nextToken();
			else syntaxError("} が期待されます");
		}
		/*現在のトークンが ; の場合*/
		else if(token.checkSymbol(Symbol.SEMICOLON)) {
			token = lexer.nextToken();
		}
		else
			syntaxError("予約語,整数値,文字,(,{,;,論理演算子,算術演算子のいずれかが期待されます");


	}

	/**
	 * 非終端記号<If_statement>の構文解析をする
	 */
	void parseIf_statement() {
		/*
		 * 現在のトークンがIFなら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.IF)) token = lexer.nextToken();
		else syntaxError("if が期待されます");

		/*
		 * 現在のトークンが ( なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.LPAREN)) token = lexer.nextToken();
		else syntaxError("( が期待されます");

		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			parseExpression();
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが ) なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.RPAREN)) token = lexer.nextToken();
		else syntaxError(") が期待されます");

		int beqAddr = iseg.appendCode(Operator.BEQ,-1); //飛び地未定

		/*
		 * 現在のトークンがFirst(<Statement>)に属しているならparseStatementメソッドに移動するs
		 */
		if(isParseStatement(token))
			parseStatement();
		else syntaxError("予約語,整数値,文字,(,{,;,論理演算子,算術演算子のいずれかが期待されます");
		int jumpAddr2 = iseg.appendCode(Operator.JUMP,-1);//

		int L1 = iseg.getLastCodeAddress();
		iseg.replaceCode(beqAddr, L1+1);

		/*
		 * if文のelse節が来た時の処理.
		 */
		if(token.checkSymbol(Symbol.ELSE)) {
			token = lexer.nextToken();
			if(isParseStatement(token))
				parseStatement();
			else syntaxError("予約語,整数値,文字,(,{,;,論理演算子,算術演算子のいずれかが期待されます");

		}
		int L2 = iseg.getLastCodeAddress();
		iseg.replaceCode(jumpAddr2, L2+1);

	}

	/**
	 * 非終端記号<While_statement>の構文解析をする
	 */
	void parseWhile_statement() {
		/*
		 * 現在のトークンがWHILEなら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.WHILE)) token = lexer.nextToken();
		else syntaxError("while が期待されます");

		/*
		 * 現在のトークンが ( なら次のトークンを取りだす
		 */
		if(token.checkSymbol(Symbol.LPAREN)) token = lexer.nextToken();
		else syntaxError("( が期待されます");

		int lastAddr = iseg.getLastCodeAddress(); /*条件式直前の番地を記憶*/

		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			parseExpression();
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが ) なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.RPAREN)) token = lexer.nextToken();
		else syntaxError(") が期待されます");

		int beqAddr = iseg.appendCode(Operator.BEQ,-1); /*飛び先未定*/

		boolean outerLoop = inLoop;
		ArrayList<Integer> outerList = breakAddrList;
		inLoop = true;
		breakAddrList = new ArrayList<Integer>();


		/*
		 * 現在のトークンがFirst(<Statement>)に属しているならparseStatementメソッドに移動するs
		 */
		if(isParseStatement(token)) {
			parseStatement(); /*この<Statement>はループ内部として処理される*/
		}
		else syntaxError("予約語,整数値,文字,(,{,;,論理演算子,算術演算子のいずれかが期待されます");

		int jumpAddr = iseg.appendCode(Operator.JUMP,lastAddr+1);
		iseg.replaceCode(beqAddr, jumpAddr+1);


		/*
		 * while文のbreakの処理.
		 */
		for(int i = 0; i<breakAddrList.size();i++) {
			int breakAddr = breakAddrList.get(i);
			iseg.replaceCode(breakAddr, jumpAddr+1); /*breakを実行するとループ内を抜けるようになっている.*/
		}
		inLoop = outerLoop; //ループ外へ
		breakAddrList = outerList; // breakリストの初期化


	}

	/**
	 * 非終端記号<Do-while_statement>の構文解析をする.
	 */
	void parseDoWhile_statement(){

		/*
		 * 現在のトークンがdoなら次のトークンを読む.
		 */
		if(token.checkSymbol(Symbol.DO)) token = lexer.nextToken();
		else syntaxError("doが期待されます");


		int LAddr = iseg.getLastCodeAddress();
		/*
		 * 現在のトークンがStatementのfirst集合ならがStatementの構文解析メソッドへ移動する
		 */
		if(isParseStatement(token)) parseStatement();
		else syntaxError("予約語,整数値,文字,(,{,;,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンがwhileなら次のトークンを読む.
		 */
		if(token.checkSymbol(Symbol.WHILE)) token = lexer.nextToken();
		else syntaxError("whileが期待されます");

		/*
		 * 現在のトークンが ( なら次のトークンを読む.
		 */
		if(token.checkSymbol(Symbol.LPAREN)) token = lexer.nextToken();
		else syntaxError(" ( が期待されます");

		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			parseExpression();
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		iseg.appendCode(Operator.BNE,LAddr+1); /*条件から異なる場合,ループの外を外れる*/

		/*
		 * 現在のトークンが ) なら次のトークンを読む.
		 */
		if(token.checkSymbol(Symbol.RPAREN)) token = lexer.nextToken();
		else syntaxError(" ) が期待されます");

		/*
		 * 現在のトークンが ; なら次のトークンを読む.
		 */
		if(token.checkSymbol(Symbol.SEMICOLON)) token = lexer.nextToken();
		else syntaxError(" ; が期待されます");
	}

	/**
	 * 非終端記号<Switch>の構文解析をする.
	 */
	void parseSwitch_statement() {
		/*
		 * 現在のトークンがswitchなら次のトークンを読む
		 */
		if(token.checkSymbol(Symbol.SWITCH)) token = lexer.nextToken();
		else syntaxError("switchが期待されます");

		/*
		 * 現在のトークンが ( なら次のトークンを読む
		 */
		if(token.checkSymbol(Symbol.LPAREN)) token = lexer.nextToken();
		else syntaxError(" ( が期待されます");

		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			parseExpression();
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが ) なら次のトークンを読む
		 */
		if(token.checkSymbol(Symbol.RPAREN)) token = lexer.nextToken();
		else syntaxError(" ) が期待されます");

		this.jumpAddrSwitch = iseg.appendCode(Operator.JUMP,-1);


		boolean outerLoop = inLoop;
		ArrayList<Integer> outerList = breakAddrList;
		ArrayList<Integer> tempList = copyList;
		inLoop = true; //ループ内へ
		breakAddrList = new ArrayList<Integer>(); //breakリストの初期化
		copyList = new ArrayList<Integer>();
		/*
		 * 現在のトークンがStatementのfirst集合ならネスト内を通る.
		 */
		if(isParseStatement(token)) {
			parseStatement();
		}

		int removeAddr = iseg.appendCode(Operator.REMOVE);

		for(int i = 0; i<breakAddrList.size();i++) {
			int breakAddr = breakAddrList.get(i);
			iseg.replaceCode(breakAddr, removeAddr+1);  /*for文のループ中にbreakを利用した場合にループからジャンプするようにする*/
		}

		inLoop = outerLoop;
		breakAddrList  = outerList;
		copyList = tempList;
	}

	/**
	 * 非終端記号<Case_label>の構文解析をする.
	 */
	void parseCase_label() {
		/*
		 * 現在のトークンがcaseなら次のトークンを読む
		 */
		if(token.checkSymbol(Symbol.CASE)) token = lexer.nextToken();
		else syntaxError("case が期待されます");

		int jupmAddr = iseg.appendCode(Operator.JUMP,-1);
		int copyAddr = iseg.appendCode(Operator.COPY);


		/*
		 * 1度目のCOPYの番地だけはParseSwitchメソッドのjumpAddrを入れ替える必要がある.
		 */
		if(this.firstCopy) {
			iseg.replaceCode(jumpAddrSwitch, copyAddr);
		}
		/*
		 * 現在のトークンがConstantのfirst集合ならネスト内をループする
		 */
		if(this.isParseConstant_list(token))
			parseConstant();

		iseg.appendCode(Operator.COMP);
		bneAddr = iseg.appendCode(Operator.BNE,-1); // 飛び地が確定したら
		copyList.add(bneAddr);

		if(!this.firstCopy)
		iseg.replaceCode(copyList.get(count++), copyAddr);

		iseg.replaceCode(jupmAddr, bneAddr+1);

		/*
		 * 現在のトークンが : なら次のトークンを読む
		 */
		if(token.checkSymbol(Symbol.COLON)) token = lexer.nextToken();
		else syntaxError(" : が期待されます");
		this.firstCopy = false;
	}

	void parseDefault_label() {
		if(token.checkSymbol(Symbol.DEFAULT)) token = lexer.nextToken();
		else syntaxError("defaultが期待されます");

		iseg.replaceCode(copyList.get(count), iseg.getLastCodeAddress());

		if(token.checkSymbol(Symbol.COLON)) token = lexer.nextToken();
		else syntaxError(" : が期待されます");

	}

	/**
	 * 非終端記号<Exp_statement>の構文解析をする
	 */
	void parseExp_statement() {
		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			parseExpression();
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが ; なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.SEMICOLON)) {
			token = lexer.nextToken();
			iseg.appendCode(Operator.REMOVE); /*評価値の削除*/
		}
		else syntaxError("; が期待されます");

	}

	/**
	 * 非終端記号<For_statement>の構文解析をする
	 */
	void parseFor_statement() {

		/*
		 * 現在のトークンがFORなら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.FOR)) token = lexer.nextToken();
		else syntaxError("forが期待されます");

		/*
		 * 現在のトークンが ( なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.LPAREN)) token = lexer.nextToken();
		else syntaxError("{ が期待されます");

		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			parseForExpressionList();

		/*
		 * 現在のトークンが ; なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.SEMICOLON)) token = lexer.nextToken();
		else syntaxError("; が期待されます");

		int removeAddr = iseg.appendCode(Operator.REMOVE);
											/*条件式直前の番地を記憶*/

		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token)) {
			parseExpression();
		}

		/*
		 * 現在のトークンが ; なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.SEMICOLON)) token = lexer.nextToken();
		else syntaxError("; が期待されます");




		int beqAddr = iseg.appendCode(Operator.BEQ,-1); /*飛び先未定*/
		int jumpAddr = iseg.appendCode(Operator.JUMP,-1); /*飛び先未定*/


		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			parseForExpressionList();



		/*
		 * 現在のトークンが ) なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.RPAREN)) token = lexer.nextToken();
		else syntaxError(") が期待されます");

		iseg.appendCode(Operator.REMOVE);
		int jumpAddr2 = iseg.appendCode(Operator.JUMP,removeAddr+1); /*removeAddrを利用してそのアドレス+1の番地へ飛ぶ*/
		iseg.replaceCode(jumpAddr,jumpAddr2+1); /*未確定だったjumpAddrの番地を決定する*/



		boolean outerLoop = inLoop;
		ArrayList<Integer> outerList = breakAddrList;
		inLoop = true; //ループ内へ
		breakAddrList = new ArrayList<Integer>(); //breakリストの初期化

		/*
		 * 現在のトークンがFirst(<Statement>)に属しているならparseStatementメソッドに移動するs
		 */
		if(isParseStatement(token))
			parseStatement();
		else syntaxError("予約語,整数値,文字,(,{,;,論理演算子,算術演算子のいずれかが期待されます");


		int jumpAddr3 = iseg.appendCode(Operator.JUMP,jumpAddr+1); /*jumpAddr+1の番地へ飛ぶ*/
		iseg.replaceCode(beqAddr, jumpAddr3+1); /*jumpAddr3+1を利用して未確定のbeqAddrに番地を与える*/



		for(int i = 0; i<breakAddrList.size();i++) {
			int breakAddr = breakAddrList.get(i);
			iseg.replaceCode(breakAddr, jumpAddr3+1);  /*for文のループ中にbreakを利用した場合にループからジャンプするようにする*/
		}
		inLoop = outerLoop;
		breakAddrList = outerList;

	}

	/**
	 * このメソッドはfor文の第一引数と第三引数を複数個指定できるように作った.
	 */
	void parseForExpressionList() {
		/*
		 * トークンが - or 整数値 or 文字の間ネスト内をループする
		 */
		if (isParseArithmetic_factor(token)) {
			parseExpression(); // parseConstanメソッドに移動
		}
		else
			syntaxError("整数値もしくは文字が期待されます");

		/*
		 * トークンが , の間ネスト内をループする
		 * 配列要素の数が複数存在するときにこのループ内を通る
		 */
		while (token.checkSymbol(Symbol.COMMA)) {

			/*
			 * トークンが , だったので次のトークンに移す
			 */
			token = lexer.nextToken();
			/*
			 * トークンが - or 整数値 or 文字の間ネスト内をループする
			 */
			if (isParseArithmetic_factor(token))
				parseExpression();
			else
				syntaxError("整数値or文字が期待されます");
		}
	}

	/**
	 * 非終端記号<Outputchar_statement>の構文解析をする
	 */
	void parseOutputchar_statement() {

		/*
		 * 現在のトークンがOUTPUTCHARなら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.OUTPUTCHAR)) token = lexer.nextToken();
		else syntaxError("outputcharが期待されます");

		/*
		 * 現在のトークンが ( なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.LPAREN)) token = lexer.nextToken();
		else syntaxError("{ が期待されます");

		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token)) {
			parseExpression();
			iseg.appendCode(Operator.OUTPUTC); /*整数の出力文コードの生成*/
			iseg.appendCode(Operator.OUTPUTLN);
		}
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが ) なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.RPAREN)) token = lexer.nextToken();
		else syntaxError(") が期待されます");

		/*
		 * 現在のトークンが ; なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.SEMICOLON)) {
			token = lexer.nextToken();
		}
		else syntaxError("; が期待されます");

	}

	/**
	 * 非終端記号<Outputint_statement>の構文解析をする
	 */
	void parseOutputint_statement() {
		/*
		 * 現在のトークンがOUTPUTINTなら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.OUTPUTINT)) token = lexer.nextToken();
		else syntaxError("outputint が期待されます");

		/*
		 * 現在のトークンが ( なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.LPAREN)) token = lexer.nextToken();
		else syntaxError("{ が期待されます");

		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token)) {
			parseExpression();
			iseg.appendCode(Operator.OUTPUT); /*文字の出力コードの生成*/
			iseg.appendCode(Operator.OUTPUTLN);
		}
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが ) なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.RPAREN)) token = lexer.nextToken();
		else syntaxError(") が期待されます");

		/*
		 * 現在のトークンが ; なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.SEMICOLON)) {
			token = lexer.nextToken();
		}
		else syntaxError("; が期待されます");
	}

	/**
	 * 非終端記号<Break_statement>の構文解析をする
	 */
	void parseBreak_statement() {
		/*
		 * 現在のトークンがBREAKなら次のトークンを取りだす
		 */
		if(token.checkSymbol(Symbol.BREAK)) token = lexer.nextToken();
		else syntaxError("break が期待されます");

		if(!(inLoop)) syntaxError("ループ内ではありません");

		int addr = iseg.appendCode(Operator.JUMP,-1);
		breakAddrList.add(addr);

		/*
		 * 現在のトークンが ; なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.SEMICOLON)) token = lexer.nextToken();
		else syntaxError("; が期待されます");

	}

	/**
	 * 非終端記号<Expression>の構文解析をする
	 */
	void parseExpression() {
		boolean hasLeftValue = false; //左辺値の有無の真偽値の初期化
		/*
		 * 現在のトークンがFirst(<Exp>)に属しているならparseExpメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			hasLeftValue = parseExp(); /*<Exp>の左辺値の有無をコピー*/
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが =, +=, -=, *=, /=のいずれかならネスト内を実行する
		 */
		if(token.checkSymbol(Symbol.ASSIGN)||token.checkSymbol(Symbol.ASSIGNADD)||token.checkSymbol(Symbol.ASSIGNSUB)
				||token.checkSymbol(Symbol.ASSIGNMUL)||token.checkSymbol(Symbol.ASSIGNDIV)) {
			/*
			 * 現在のトークンを取得する, ASSIGN以外にはCOPY,LOADを積んだのちexpressionメソッドを実行して各算術演算子のコードを積んでいる
			 */
			switch(token.getSymbol()) {
			// 現在のトークンが = の場合
			case ASSIGN:
				if(!hasLeftValue) /*左辺値がなければエラー*/
					syntaxError("左辺値がありません");
				token = lexer.nextToken();
				/*
				 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseExpression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");
				break;

			// 現在のトークンが += の場合
			case ASSIGNADD:
				if(!hasLeftValue) /*左辺値がなければエラー*/
					syntaxError("左辺値がありません");
				token = lexer.nextToken();
				iseg.appendCode(Operator.COPY);
				iseg.appendCode(Operator.LOAD);
				/*
				 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseExpression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

				iseg.appendCode(Operator.ADD);
				break;

			// 現在のトークンが -= の場合
			case ASSIGNSUB:
				if(!hasLeftValue) /*左辺値がなければエラー*/
					syntaxError("左辺値がありません");
				token = lexer.nextToken();
				iseg.appendCode(Operator.COPY);
				iseg.appendCode(Operator.LOAD);
				/*
				 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseExpression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

				iseg.appendCode(Operator.SUB);
				break;

			// 現在のトークンが *= の場合
			case ASSIGNMUL:
				if(!hasLeftValue) /*左辺値がなければエラー*/
					syntaxError("左辺値がありません");
				token = lexer.nextToken();
				iseg.appendCode(Operator.COPY);
				iseg.appendCode(Operator.LOAD);
				/*
				 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseExpression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

				iseg.appendCode(Operator.MUL);
				break;

			// 現在のトークンが /= の場合
			case ASSIGNDIV:
				if(!hasLeftValue) /*左辺値がなければエラー*/
					syntaxError("左辺値がありません");
				token = lexer.nextToken();
				iseg.appendCode(Operator.COPY);
				iseg.appendCode(Operator.LOAD);
				/*
				 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseExpression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

				iseg.appendCode(Operator.DIV);
				break;

			default:
				syntaxError("=, +=, -=, *=, /=のいずれかが期待されます");
			}

		iseg.appendCode(Operator.ASSGN); // = += -= *= /= で共通して積まれているコード
		}
	}

	/**
	 * 非終端記号<Exp>の構文解析をする
	 */
	boolean parseExp() {
		boolean hasLeftValue = false; //左辺値の有無の真偽値の初期化
		/*
		 * 現在のトークンがFirst(<Logical_term>)に属しているならparseLogical_termメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			hasLeftValue = parseLogical_term(); /*<Logical_term>の左辺値をコピー*/
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが || の間ネスト内をループする
		 */
		while(token.checkSymbol(Symbol.OR)) {
			token = lexer.nextToken(); /*現在のトークンから次のトークンへ移る*/
			hasLeftValue = false;

			/*
			 * 現在のトークンがFirst(<Logical_term>)に属しているならparseLogical_termメソッドに移動する
			 */
			if(isParseArithmetic_factor(token))
				parseLogical_term();
			else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

			iseg.appendCode(Operator.OR);
		}

		return hasLeftValue;

	}

	/**
	 * 非終端記号<Logical_term>の構文解析をする
	 */
	boolean parseLogical_term() {
		boolean hasLeftValue = false; //左辺値の有無の真偽値の初期化
		/*
		 * 現在のトークンがFirst(<Logical_factor>)に属しているならparseLogical_factorメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			hasLeftValue = parseLogical_factor(); /*<Logical_factor>の左辺値の有無をコピー*/
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが && の間ネスト内をループする
		 */
		while(token.checkSymbol(Symbol.AND)) {
			token = lexer.nextToken(); /*現在のトークンから次のトークンに移る*/
			hasLeftValue = false;

			/*
			 * 現在のトークンがFirst(<Logical_factor>)に属しているならparseLogical_factorメソッドに移動する
			 */
			if(isParseArithmetic_factor(token))
				parseLogical_factor();
			else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

			iseg.appendCode(Operator.AND);
		}

		return hasLeftValue;

	}

	/**
	 * 非終端記号<Logical_factor>の構文解析をする
	 */
	boolean parseLogical_factor() {
		boolean hasLeftValue = false; //左辺値の有無の真偽値の初期化
		/*
		 * 現在のトークンがFirst(<Arithmetic_expression>)に属しているならparthArithmetic_expressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			hasLeftValue = parseArithmetic_expression(); /*<Arithmetic_expression>の左辺値の有無をコピー*/
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが ==, !=, <, >のいずれかの時にネスト内を実行する
		 */
		if(token.checkSymbol(Symbol.EQUAL)||token.checkSymbol(Symbol.NOTEQ)||token.checkSymbol(Symbol.LESS)||token.checkSymbol(Symbol.GREAT)) {
			/*
			 * 現在のトークンを取得する
			 */
			switch(token.getSymbol()) {
			// == の場合
			case EQUAL:
				token = lexer.nextToken();
				hasLeftValue = false;
				/*
				 * 現在のトークンがFirst(<Arithmetic_expression>)に属しているならparseArithmetic_expressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseArithmetic_expression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");
				int compAddr = iseg.appendCode(Operator.COMP);  //スタックに積まれた最上位2つの値を比較してそれに応じた値を返す
				iseg.appendCode(Operator.BEQ,compAddr+4); // equalなら, 下のPUSHI 1へ飛ぶ.
				iseg.appendCode(Operator.PUSHI,0);
				iseg.appendCode(Operator.JUMP,compAddr+5); //2つしたの命令へ飛ぶ
				iseg.appendCode(Operator.PUSHI,1);
				break;

			// != の場合
			case NOTEQ:
				token = lexer.nextToken();
				hasLeftValue = false;
				/*
				 * 現在のトークンがFirst(<Arithmetic_expression>)に属しているならparseArithmetic_expressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseArithmetic_expression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");
				compAddr = iseg.appendCode(Operator.COMP);  //スタックに積まれた最上位2つの値を比較してそれに応じた値を返す
				iseg.appendCode(Operator.BNE,compAddr+4); // notEqualなら,下のPUSHI 1へ飛ぶ
				iseg.appendCode(Operator.PUSHI,0);
				iseg.appendCode(Operator.JUMP,compAddr+5); // 2つしたの命令へ飛ぶ
				iseg.appendCode(Operator.PUSHI,1);
				break;

			// < の場合
			case LESS:
				token = lexer.nextToken();
				hasLeftValue = false;
				/*
				 * 現在のトークンがFirst(<Arithmetic_expression>)に属しているならparseArithmetic_expressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseArithmetic_expression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");
				compAddr = iseg.appendCode(Operator.COMP);  //スタックに積まれた最上位2つの値を比較してそれに応じた値を返す
				iseg.appendCode(Operator.BLT,compAddr+4); // lessなら, 下のPUSHI 1へ飛ぶ
				iseg.appendCode(Operator.PUSHI,0);
				iseg.appendCode(Operator.JUMP,compAddr+5); // 2つしたの命令へ飛ぶ
				iseg.appendCode(Operator.PUSHI,1);
				break;

			// > の場合
			case GREAT:
				token = lexer.nextToken();
				hasLeftValue = false;
				/*
				 * 現在のトークンがFirst(<Arithmetic_expression>)に属しているならparseArithmetic_expressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseArithmetic_expression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");
				compAddr = iseg.appendCode(Operator.COMP); //スタックに積まれた最上位2つの値を比較してそれに応じた値を返す
				iseg.appendCode(Operator.BGT,compAddr+4); // greatなら, 下のPUSHI 1へ飛ぶ
				iseg.appendCode(Operator.PUSHI,0);
				iseg.appendCode(Operator.JUMP,compAddr+5); // 2つしたの命令へ飛ぶ
				iseg.appendCode(Operator.PUSHI,1);
				break;

			default:
				syntaxError("==, !=, <, > のいずれかが期待されます");
			}
		}
		return hasLeftValue;

	}

	/**
	 * 非終端記号<Arithmetic_expression>の構文解析をする
	 */
	boolean parseArithmetic_expression() {
		boolean hasLeftValue = false; //左辺値かどうかの真偽値の初期化
		/*
		 * 現在のトークンがFirst(<Arithmetic_term>)に属しているならparseArithmetic_termメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			hasLeftValue = parseArithmetic_term(); /*<Arithmetic_term>の左辺値の有無をコピー*/
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが +, - の間ネスト内をループする
		 */
		while(token.checkSymbol(Symbol.ADD)||token.checkSymbol(Symbol.SUB)) {
			Symbol op = null;
			/*
			 * 現在のトークンを取得する
			 */
			switch(token.getSymbol()) {
			// 現在のトークンが + の場合
			case ADD:
				op = token.getSymbol();
				token = lexer.nextToken();
				hasLeftValue = false;
				break;

			// 現在のトークンが - の場合
			case SUB:
				op = token.getSymbol();
				token = lexer.nextToken();
				hasLeftValue = false;
				break;

			default:
				hasLeftValue = false;
				syntaxError("+, - のいずれかが期待されます");
			}
			/*
			 * 現在のトークンがFirst(<Arithmetic_term>)に属しているならparseArithmetic_termメソッドに移動する
			 */
			if(isParseArithmetic_factor(token))
				parseArithmetic_term();
			else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

			if(op==Symbol.ADD) iseg.appendCode(Operator.ADD); /*加算演算に対するコード生成*/
			else iseg.appendCode(Operator.SUB); /*減算演算に対するコード生成*/
		}

		return hasLeftValue;
	}

	/**
	 * 非終端記号<Arithmetic_term>の構文解析をする
	 */
	boolean parseArithmetic_term() {
		boolean hasLeftValue = false; // 左辺値の判別をする変数の初期化
		/*
		 * 現在のトークンがFirst(<Arithmetic_factor>)に属しているならparseArithmetic_factorメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			 hasLeftValue = parseArithmetic_factor(); /*<Arithmetic_factor>の左辺値の有無をコピー*/
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが *, /, % のいずれかの間ネスト内をループする
		 */
		while(token.checkSymbol(Symbol.MUL)||token.checkSymbol(Symbol.DIV)||token.checkSymbol(Symbol.MOD)) {
			Symbol op = null;
			/*
			 * 現在のトークンを取得する
			 */
			switch(token.getSymbol()) {
			// 現在のトークンが * の場合
			case MUL:
				op = token.getSymbol();
				//++mul_n;
				token = lexer.nextToken();
				hasLeftValue = false; //掛け算があるので左辺値なしに
				break;

			// 現在のトークンが / の場合
			case DIV:
				op = token.getSymbol();
				//++div_n;
				token = lexer.nextToken();
				hasLeftValue = false; //割り算があるので左辺値なしに
				break;

			// 現在のトークンが % の場合
			case MOD:
				op = token.getSymbol();
				//++mod_n;
				token = lexer.nextToken();
				hasLeftValue = false; //剰余があるので左辺値になしに
				break;
			default:
				syntaxError("*, /, % のいずれかが期待されます");

			}

			/*
			 * 現在のトークンがFirst(<Arithmetic_factor>)に属しているならparseArithmetic_factorメソッドに移動する
			 */
			if(isParseArithmetic_factor(token))
				parseArithmetic_factor();
			else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

			if(op==Symbol.MUL) iseg.appendCode(Operator.MUL); /*乗用演算のコード生成*/
			else if(op == Symbol.DIV) iseg.appendCode(Operator.DIV); /*剰余演算のコード生成*/
			else if(op == Symbol.MOD) iseg.appendCode(Operator.MOD); /*余剰演算のコード生成*/

		}

		return hasLeftValue;
	}

	/**
	 * 非終端記号<Arithmetic_factor>のFirst集合を判別する
	 * First(<Arithmetic_factor>)は<Unsigned_factor>のFirst集合に -, !を追加したものである
	 * またこの集合は<Arithmetic_term>,<Arithmetic_expression>,<Logical_factor>,<Logical_term>,<Exp>,<Expression>,<Exp_statement>,<Expression_list>のFirst集合に等しい.
	 * @param token 指定したトークン
	 * @return 真偽値を返す
	 */
	boolean isParseArithmetic_factor(Token token) {
		boolean temp = false;

		if (isParseUnsigned_factor(token)||token.checkSymbol(Symbol.SUB)||token.checkSymbol(Symbol.NOT))
			temp = true;

		return temp;
	}

	/**
	 * 非終端記号<Arithmetic_factor>の構文解析をする
	 */
	boolean parseArithmetic_factor() {

		if(isParseUnsigned_factor(token)) {
			/*
			 * <Unsigned_factor> の左辺値の有無をコピー
			 */
			boolean hasLeftValue = parseUnsigned_factor();
			return hasLeftValue;
		}

		else if(token.checkSymbol(Symbol.SUB)||token.checkSymbol(Symbol.NOT)) {
			Symbol op = token.getSymbol(); /*トークンから記号を一時的に保存しておく*/
			token = lexer.nextToken();
			/*
			 * 現在のトークンがFirst(<Arithmetic_factor>)に属しているならparseArithmetic_factorメソッドに移動する
			 */
			if(isParseArithmetic_factor(token))
				parseArithmetic_factor();
			else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

			if(op == Symbol.SUB) iseg.appendCode(Operator.CSIGN); /*符号変換の生成コード*/
			else iseg.appendCode(Operator.NOT); /*否定の生成コード*/

			return false; /*左辺値なし*/
		}

		else {
			syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");
			return false;
		}


	}

	/**
	 * 非終端記号<Unsiged_factor>のFirst集合を判別する
	 * First(<Unsigned_factor>) = { NAME, ++, --, INT, CHAR, (, inputchar, inputint, +, *}
	 * @param token 指定したトークン
	 * @return 真偽値を返す
	 */
	boolean isParseUnsigned_factor(Token token) {
		boolean temp = false;

		if (token.checkSymbol(Symbol.NAME) || token.checkSymbol(Symbol.INC) || token.checkSymbol(Symbol.DEC)
				|| token.checkSymbol(Symbol.INTEGER)
				|| token.checkSymbol(Symbol.CHARACTER) || token.checkSymbol(Symbol.LPAREN)
				|| token.checkSymbol(Symbol.INPUTCHAR)
				|| token.checkSymbol(Symbol.INPUTINT) || token.checkSymbol(Symbol.ADD) || token.checkSymbol(Symbol.MUL))
			temp = true;

		return temp;
	}

	/**
	 * 非終端記号<Unsigned_factor>の構文解析をする
	 */
	boolean parseUnsigned_factor() {

		String name = ""; //変数名の初期化;
		/*
		 * 現在のトークンを取り出す
		 */
		switch(token.getSymbol()) {
		// 現在のトークンがNAMEの場合
		case NAME:
			name = token.getStrValue(); /*変数名を得る*/
			/*
			 * 変数がすでに登録されているかをチェックする
			 */
			if(!variableTable.exist(name))
				syntaxError("未定義です");
			int address = variableTable.getAddress(name); //変数名を参照してnameの番地を得る

			token = lexer.nextToken(); /*次のトークンを取り出す*/

			if(token.checkSymbol(Symbol.ASSIGN)||token.checkSymbol(Symbol.ASSIGNADD)||token.checkSymbol(Symbol.ASSIGNSUB)
					||token.checkSymbol(Symbol.ASSIGNMUL)||token.checkSymbol(Symbol.ASSIGNDIV)||variableTable.checkType(name, Type.ARRAYOFINT)) {
				iseg.appendCode(Operator.PUSHI,address); /*左辺値である場合PUSHIのコード生成をする*/
			}

			else iseg.appendCode(Operator.PUSH,address); /*スカラー変数で右辺値の場合*/

			/*
			 * 現在のトークンが ++, -- の場合ネスト内を実行する(スカラ変数が期待される)
			 */
			if(token.checkSymbol(Symbol.INC)||token.checkSymbol(Symbol.DEC)) {
				iseg.appendCode(Operator.COPY);

				/*
				 * ++, -- のトークンに応じてコード生成をする
				 */
				switch(token.getSymbol()) {
				case INC:
					iseg.appendCode(Operator.INC);
					break;
				case DEC:
					iseg.appendCode(Operator.DEC);
					break;
				default:
				}
				iseg.appendCode(Operator.POP,address);
				/*
				 * 登録された変数がINT型かどうか確かめる
				 */
				if(!variableTable.checkType(name, Type.INT))
					syntaxError("型が不一致です");

				token = lexer.nextToken();/*次のトークンを取りだす*/
				return false; /*Name(++ or --)は左辺値ではないのでfalseを返す*/
			}
			/*
			 * 現在のトークンが [ の場合ネスト内を実行する(配列変数が期待される)
			 */
			else if(token.checkSymbol(Symbol.LBRACKET)) {
				/*
				 * 登録された変数がARRAY型かどうかを確かめる
				 */
				if(!variableTable.checkType(name, Type.ARRAYOFINT))
					syntaxError("型が不一致です");

				//iseg.appendCode(Operator.PUSHI,address);

				token = lexer.nextToken(); /*次のトークンを取り出す*/

				/*
				 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseExpression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

				/*
				 * 現在のトークンが ] の場合次のトークンを取り出す
				 */
				if(token.checkSymbol(Symbol.RBRACKET)) token = lexer.nextToken();
				else syntaxError("] が期待されます");

				/*
				 * 二次元配列の場合の処理
				 */
				if(token.checkSymbol(Symbol.LBRACKET)) {
					token = lexer.nextToken(); /*トークン [を読み飛ばす*/
					/*
					 * トークンがExpressionのfirst集合ならネスト内を通過.
					 */
					if(isParseArithmetic_factor(token)) {
						parseExpression();
					}else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");
					/*
					 * トークンが ] ならネスト内を通過.
					 */
					if(token.checkSymbol(Symbol.RBRACKET)) token = lexer.nextToken();
					else syntaxError("] が期待されます");

				}

				iseg.appendCode(Operator.ADD);  /*配列の左辺値が含まれる*/

				/*
				 * 配列の後置インクリメント,デクリメントの処理をしている.
				 */
				if(token.checkSymbol(Symbol.INC)||token.checkSymbol(Symbol.DEC)) {
					Symbol op2 = token.getSymbol();
					token = lexer.nextToken();
					iseg.appendCode(Operator.COPY);
					iseg.appendCode(Operator.LOAD);
					if(op2==Symbol.INC)
						iseg.appendCode(Operator.INC);
					else if(op2==Symbol.DEC)
						iseg.appendCode(Operator.DEC);
					iseg.appendCode(Operator.ASSGN);
					if(op2==Symbol.INC)
						iseg.appendCode(Operator.DEC);
					else if(op2==Symbol.DEC)
						iseg.appendCode(Operator.INC);

					return false;
				}

				if(!(token.checkSymbol(Symbol.ASSIGN)||token.checkSymbol(Symbol.ASSIGNADD)||token.checkSymbol(Symbol.ASSIGNSUB)||
						token.checkSymbol(Symbol.ASSIGNMUL)||token.checkSymbol(Symbol.ASSIGNDIV))) /*左辺値かどうかでLOADを積むかを決める*/
					iseg.appendCode(Operator.LOAD);

				return true;
			}

			/*
			 * 登録された変数がINT型かどうかを確かめる.
			 */
			if(!variableTable.checkType(name, Type.INT))
				syntaxError("型が不一致です");
			return true; /*Name or Name[exp]は左辺値なのでtrueを返す*/

		// 現在のトークンが ++ の場合
		case INC:
			address = 0;
			token = lexer.nextToken(); /*次のトークンを取り出す*/
			/*
			 * 現在のトークンが NAMEの場合次のトークンを取り出す
			 */
			if(token.checkSymbol(Symbol.NAME)) {
				name = token.getStrValue(); /*変数名の取得*/
				address = variableTable.getAddress(name);
				/*
				 * 変数名が定義されているかを調べる
				 */
				if(!variableTable.exist(name))
					syntaxError("未定義です");
				token = lexer.nextToken();
			}
			else syntaxError("変数が期待されます");

			iseg.appendCode(Operator.PUSHI,address);
			/*
			 * 現在のトークンが [ の場合ネスト内を実行する (配列変数が期待される)
			 */
			if(token.checkSymbol(Symbol.LBRACKET)) {
				/*
				 * 変数がARRAY型かを確かめる
				 */
				if(!variableTable.checkType(name, Type.ARRAYOFINT))
					syntaxError("型が不一致です");

				token = lexer.nextToken();
				/*
				 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseExpression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

				/*
				 * 現在のトークンが ]の場合ネスト内を実行する
				 */
				if(token.checkSymbol(Symbol.RBRACKET)) token = lexer.nextToken();
				else syntaxError("] が期待されます");

				iseg.appendCode(Operator.ADD); /*配列の前置インクリメントに対するコード生成*/
				iseg.appendCode(Operator.COPY);
				iseg.appendCode(Operator.LOAD);
				iseg.appendCode(Operator.INC);
				iseg.appendCode(Operator.ASSGN);

				return false; /* ++Name[exp]は左辺値ではないのでfalseを返す*/
			}

			/*
			 * 変数がINT型かを確かめる.
			 */
			if(!variableTable.checkType(name, Type.INT))
				syntaxError("型が不一致です");

			iseg.appendCode(Operator.PUSH,address); /*スカラー変数の前置インクリメントに対するコード生成*/
			iseg.appendCode(Operator.INC);
			iseg.appendCode(Operator.ASSGN);

			return false; /* ++Nameは左辺値ではないのでfalseを返す*/

		// 現在のトークンが -- の場合
		case DEC:
			address = 0;
			token = lexer.nextToken(); /*次のトークンを取り出す*/
			/*
			 * 現在のトークンが NAMEの場合次のトークンを取り出す
			 */
			if(token.checkSymbol(Symbol.NAME)) {
				name = token.getStrValue(); /*変数名の取得*/
				address = variableTable.getAddress(name);
				/*
				 * 変数が存在しているかを調べる
				 */
				if(!variableTable.exist(name))
					syntaxError("未定義です");
				token = lexer.nextToken();
			}
			else syntaxError("変数が期待されます");

			iseg.appendCode(Operator.PUSHI,address);

			/*
			 * 現在のトークンが [ の場合ネスト内を実行する
			 */
			if(token.checkSymbol(Symbol.LBRACKET)) {
				/*
				 * 変数がARRAY型かを調べる
				 */
				if(!variableTable.checkType(name,Type.ARRAYOFINT))
					syntaxError("型が不一致です");

				token = lexer.nextToken();
				/*
				 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
				 */
				if(isParseArithmetic_factor(token))
					parseExpression();
				else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");
				/*
				 * 現在のトークンが ]の場合ネスト内を実行する
				 */
				if(token.checkSymbol(Symbol.RBRACKET)) token = lexer.nextToken();
				else syntaxError("] が期待されます");

				iseg.appendCode(Operator.ADD); /*配列の前置デクリメントに対するコード生成*/
				iseg.appendCode(Operator.COPY);
				iseg.appendCode(Operator.LOAD);
				iseg.appendCode(Operator.DEC);
				iseg.appendCode(Operator.ASSGN);

				return false; /* --Name[exp]は左辺値ではないのでfalseを返す*/
			}
			/*
			 * 変数がINT型かを調べる
			 */
			if(!variableTable.checkType(name, Type.INT))
				syntaxError("型が不一致です");

			iseg.appendCode(Operator.PUSH,address); /*スカラー変数の前置デクリメントに対するコード生成*/
			iseg.appendCode(Operator.DEC);
			iseg.appendCode(Operator.ASSGN);

			return false; /* --Nameは左辺値ではないのでfalseを返す*/

		// 現在のトークンが整数値の場合
		case INTEGER:
			int value = token.getIntValue(); /*tokenから整数値を得る*/
			token = lexer.nextToken(); /*次のトークンを取り出す*/
			iseg.appendCode(Operator.PUSHI,value); /*整数値を生成する*/
			return false; /* 整数値は左辺値ではないのでfalseを返す*/

		// 現在のトークンが文字の場合
		case CHARACTER:
			int charCode = token.getIntValue(); /*tokenから文字コードを得る*/
			token = lexer.nextToken(); /*次のトークンを取り出す*/
			iseg.appendCode(Operator.PUSHI,charCode); /*文字コードを生成する*/
			return false; /*文字は左辺値ではないのでfalseを返す*/

		// 現在のトークンが ( の場合
		case LPAREN:
			token = lexer.nextToken(); /*次のトークンを取り出す*/
			/*
			 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
			 */
			if(isParseArithmetic_factor(token))
				parseExpression();
			else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");
			/*
			 * 現在のトークンが ) の場合ネスト内を実行する
			 */
			if(token.checkSymbol(Symbol.RPAREN)) token = lexer.nextToken();
			else syntaxError(") が期待されます");
			return false; /* (exp) は左辺値ではないのでfalseを返す*/

		// 現在のトークンが INPUTCHARの場合
		case INPUTCHAR:
			token = lexer.nextToken(); /*次のトークンを取り出す*/
			iseg.appendCode(Operator.INPUTC); /*inputcharのコード生成*/
			return false; /*INPUTCHARは左辺値ではないのでfalseを返す*/

		// 現在のトークンが INPUTINTの場合
		case INPUTINT:
			token = lexer.nextToken(); /*次のトークンを取り出す*/
			iseg.appendCode(Operator.INPUT); /*inputintのコード生成*/
			return false; /*INPUTINTは左辺値ではないのでfalseを返す*/

		// 現在のトークンが + の場合
		case ADD:
			parseSum_function();
			return false; /* +は左辺値ではないのでfalseを返す*/

		// 現在のトークンが * の場合
		case MUL:
			parseProduct_function();
			return false; /* *は左辺値ではないのでfalseを返す*/

		default:
			syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");
			return false;
		}


	}

	/**
	 * 非終端記号<Sum_function>の構文解析をする
	 */
	void parseSum_function() {
		/*
		 * 現在のトークンが + なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.ADD)) {
			op = token.getSymbol();
			token = lexer.nextToken();
		}
		else syntaxError("+ が期待されます");

		/*
		 * 現在のトークンが ( なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.LPAREN)) token = lexer.nextToken();
		else syntaxError("( が期待されます");

		/*
		 * 現在のトークンがFirst(<Expression_list>)に属しているならparseExpression_listメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			parseExpression_list();
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが ) なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.RPAREN)) token = lexer.nextToken();
		else syntaxError(") が期待されます");


	}

	/**
	 * 非終端記号<Product_function>の構文解析をする
	 */
	void parseProduct_function() {
		/*
		 * 現在のトークンが * なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.MUL)) {
			op = token.getSymbol();
			token = lexer.nextToken();
		}
		else syntaxError("* が期待されます");

		/*
		 * 現在のトークンが ( なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.LPAREN)) token = lexer.nextToken();
		else syntaxError("( が期待されます");

		/*
		 * 現在のトークンがFirst(<Expression_list>)に属しているならparseExpression_listメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			parseExpression_list();
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが ) なら次のトークンを取り出す
		 */
		if(token.checkSymbol(Symbol.RPAREN)) token = lexer.nextToken();
		else syntaxError(") 期待されます");


	}

	/**
	 * 非終端記号<Expression_list>の構文解析をする
	 */
	void parseExpression_list() {
		Symbol temp = op; // 一時的に記号を格納しておく
		/*
		 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
		 */
		if(isParseArithmetic_factor(token))
			parseExpression();
		else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

		/*
		 * 現在のトークンが , の間ネスト内をループする
		 */
		while(token.checkSymbol(Symbol.COMMA)) {
			token = lexer.nextToken();

			/*
			 * 現在のトークンがFirst(<Expression>)に属しているならparseExpressionメソッドに移動する
			 */
			if(isParseArithmetic_factor(token))
				parseExpression();
			else syntaxError("整数値,文字,(,論理演算子,算術演算子のいずれかが期待されます");

			/*
			 * 記号が+である時にADDをコード生成する.
			 */
			if(temp == Symbol.ADD)
				iseg.appendCode(Operator.ADD);
			/*
			 * 記号が*である時にMULをコード生成する
			 */
			else if(temp == Symbol.MUL)
				iseg.appendCode(Operator.MUL);
		}
	}

	/**
	 * 現在読んでいるファイルを閉じる (lexerのcloseFile()に委譲)
	 */
	void closeFile() {
		lexer.closeFile();
	}

	/**
	 * アセンブラコードをファイルに出力する (isegのdump2file()に委譲)
	 */
	void dump2file() {
		iseg.dump2file();
	}

	/**
	 * アセンブラコードをファイルに出力する (isegのdump2file()に委譲)
	 *
	 * @param fileName 出力ファイル名
	 */
	void dump2file(String fileName) {
		iseg.dump2file(fileName);
	}

	/**
	 * エラーメッセージを出力しプログラムを終了する
	 *
	 * @param message 出力エラーメッセージ
	 */
	@SuppressWarnings("unused")
	private void syntaxError(String message) {
		System.out.print(lexer.analyzeAt());
		//下記の文言は自動採点で使用するので変更しないでください。
		System.out.println("で構文解析プログラムが構文エラーを検出");
		System.out.println(message);
		closeFile();
		System.exit(1);
	}

	/**
	 * 警告文を出してプログラムを続行する
	 * @param err_mes 警告文
	 */
	@SuppressWarnings("unused")
	private void warning(String err_mes) {
		System.out.println(lexer.analyzeAt()+"で警告");
		System.out.println(err_mes);
	}

	/**
	 * 引数で指定したK20言語ファイルを解析する 読み込んだファイルが文法上正しければアセンブラコードを出力する
	 */
	public static void main(String[] args) {
		KcOpt parser;

		if (args.length == 0) {
			System.out.println("Usage: java kc.Kc20 file [objectfile]");
			System.exit(0);
		}

		parser = new KcOpt(args[0]);

		parser.parseProgram();
		parser.closeFile();

		// 以下のコードは完全にコンパイラが完成してから, コメントアウトする.
		if (args.length == 1)
		parser.dump2file();
		else
		parser.dump2file(args[1]);
	}

}
