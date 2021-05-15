package kc;

/**
 * @author 18-1-037-0110
 * 問題番号 問題3.3
 * 提出日 2020年6月10日
 *
 * このクラスは整数,演算子,文字,予約語をトークンごとにきりとる
 *
 */
class LexicalAnalyzer {

	private FileScanner sourceFileScanner; // ソースファイルに対するスキャナ

	/**
	 * コンストラクタ
	 * 指定したファイル名の読み取りをする.
	 */
	LexicalAnalyzer(String sourceFileName) {
		this.sourceFileScanner = new FileScanner(sourceFileName);
	}

	/**
	 * 次に来るTokenのインスタンスを返す.
	 */
	Token nextToken() {

		Token token = null; //トークンの種類
		char currentChar; //現在読み込んでいる文字
		String word = ""; // 連結する文字列
		int value = 0; // 整数値を読み込む際に利用

		/*
		 * 空白文字,行末記号,水平タブの処理をする.
		 */
		do{
			currentChar = sourceFileScanner.nextChar();
		}while(currentChar == ' '||currentChar == '\n'||currentChar == '\t');

		/*
		 * ファイル末に到達した時に実行される
		 */
		if (currentChar == '\0')
			token = new Token(Symbol.EOF);

		/*
		 * 最初の文字が'+'の時に実行される
		 */
		else if (currentChar == '+') {
			/*
			 * 二文字目が'+'の時に実行される(++)
			 */
			if(sourceFileScanner.lookAhead() == '+') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.INC);
			}
			/*
			 * 二文字目が'='の時に実行される(+=)
			 */
			else if(sourceFileScanner.lookAhead()== '=') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.ASSIGNADD);
			}
			/*
			 * '+'のみの時に実行される
			 */
			else
				token = new Token(Symbol.ADD);
		}

		/*
		 * 最初の文字が'-'の時に実行される
		 */
		else if(currentChar == '-') {
			/*
			 * 二文字目が'-'の時に実行される(--)
			 */
			if(sourceFileScanner.lookAhead() == '-') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.DEC);
			}
			/*
			 * 二文字目が'='の時に実行される(-=)
			 */
			else if(sourceFileScanner.lookAhead() == '=') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.ASSIGNSUB);
			}
			/*
			 * '-'のみの時に実行される
			 */
			else
				token = new Token(Symbol.SUB);
		}

		/*
		 *  最初の文字が'*'の時に実行される
		 */
		else if(currentChar == '*') {
			/*
			 *  二文字目が'='の時に実行される(*=)
			 */
			if(sourceFileScanner.lookAhead() == '=') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.ASSIGNMUL);
			}
			/*
			 *  '*'のみの時に実行される
			 */
			else
				token = new Token(Symbol.MUL);
		}


		/*
		 *  最初の文字が'/'の時に実行される
		 */
		else if(currentChar == '/') {
			/*
			 *  二文字目が'='の時に実行される(/=)
			 */
			if(sourceFileScanner.lookAhead() == '=') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.ASSIGNDIV);
			}

			/*
			 * コメントの時を読みとばす (/* のversion)
			 */
			else if(sourceFileScanner.lookAhead() == '*') {
				currentChar = sourceFileScanner.nextChar();
				currentChar = sourceFileScanner.nextChar();

				/*
				 * 文字 *,/が連続してくるまで文字を読み飛ばす
				 */
				while(true) {
					if(currentChar == '*'&&sourceFileScanner.lookAhead()=='/')
						break;
					/*
					 * 無限ループを回避するために作った.
					 */
					if(currentChar == '\n')
						syntaxError();
					currentChar = sourceFileScanner.nextChar();
				}

				if(currentChar == '*') {
					currentChar = sourceFileScanner.nextChar(); /* *を読み飛ばす*/
					if(currentChar ==  '/') {
						token = nextToken(); /*再帰で次のトークンを読む*/
					}
					else syntaxError();
				}
				else syntaxError();
			}

			/*
			 * コメントを読み飛ばす (// のversion)
			 */
			else if(sourceFileScanner.lookAhead()== '/') {
				currentChar = sourceFileScanner.nextChar();
				while(currentChar != '\n') {
					currentChar = sourceFileScanner.nextChar();
				}
				if(currentChar == '\n');
				else syntaxError();
				token = nextToken();

			}

			/*
			 *  '/'のみの時に実行する
			 */
			else
				token = new Token(Symbol.DIV);
		}

		/*
		 *  最初の文字が'%'時に実行される
		 */
		else if(currentChar == '%') {

			/*
			 * 二文字目が'='の時に実行される(%=)
			 */
			if(sourceFileScanner.lookAhead() == '=') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.ASSIGNMOD);
			}
			/*
			 *  '%'のみの時に実行する
			 */
			else
				token = new Token(Symbol.MOD);
		}

		else if(currentChar == ':') {
			token = new Token(Symbol.COLON);
		}

		/*
		 * 最初の文字が'<'の時に実行される
		 */
		else if(currentChar == '<') {
			/*
			 * 二文字目が'='の時に実行される(<=)
			 */
			if(sourceFileScanner.lookAhead() == '=') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.LESSEQ);
			}
			/*
			 * '<'のみの時に実行される
			 */
			else
			token = new Token(Symbol.LESS);
		}

		/*
		 * 最初の文字が'>'の時に実行される
		 */
		else if(currentChar == '>') {
			/*
			 * 二文字目が'='の時に実行される(>=)
			 */
			if(sourceFileScanner.lookAhead() == '=') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.GREATEQ);
			}
			/*
			 * '>'のみの時に実行される
			 */
			else
			token = new Token(Symbol.GREAT);
		}

		/*
		 * 最初の文字が'&'の時に実行される
		 */
		else if(currentChar == '&') {
			/*
			 * 二文字目が'&'の時に実行される(&&)
			 */
			if(sourceFileScanner.lookAhead() == '&') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.AND);
			}
			else syntaxError();
		}

		/*
		 * 最初の文字が'|'の時に実行される
		 */
		else if(currentChar == '|') {
			/*
			 * 二文字目が'|'の時に実行される(||)
			 */
			if(sourceFileScanner.lookAhead() == '|') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.OR);
			}
			else syntaxError();
		}

		/*
		 * 最初の文字が';'の時に実行される
		 */
		else if(currentChar == ';') {
			token = new Token(Symbol.SEMICOLON);
		}

		/*
		 * 最初の文字が'('の時に実行される
		 */
		else if(currentChar == '(') {
			token = new Token(Symbol.LPAREN);
		}

		/*
		 * 最初の文字が')'の時に実行される
		 */
		else if(currentChar == ')') {
			token = new Token(Symbol.RPAREN);
		}

		/*
		 * 最初の文字が'{'の時に実行される
		 */
		else if(currentChar == '{') {
			token = new Token(Symbol.LBRACE);
		}

		/*
		 * 最初の文字が'}'の時に実行される
		 */
		else if(currentChar == '}') {
			token = new Token(Symbol.RBRACE);
		}

		/*
		 * 最初の文字が'['の時に実行される
		 */
		else if(currentChar == '[') {
			token = new Token(Symbol.LBRACKET);
		}

		/*
		 * 最初の文字が']'の時に実行される
		 */
		else if(currentChar == ']') {
			token = new Token(Symbol.RBRACKET);
		}

		/*
		 * 最初の文字が','の時に実行される
		 */
		else if(currentChar == ',') {
			token = new Token(Symbol.COMMA);
		}

		/*
		 * 最初の文字が'!'の時に実行される
		 */
		else if (currentChar == '!') {
			/*
			 * 二文字目が'='の時に実行される(!=)
			 */
			if (sourceFileScanner.lookAhead() == '=') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.NOTEQ);
			}

			/*
			 * '!'のみの時に実行される(!)
			 */
			else {
				token = new Token(Symbol.NOT);
			}
		}

		/*
		 * 最初の文字が'='の時に実行される
		 */
		else if (currentChar == '=') {
			/*
			 * 二文字目が'='の時に実行される(==)
			 */
			if (sourceFileScanner.lookAhead() == '=') {
				sourceFileScanner.nextChar();
				token = new Token(Symbol.EQUAL);
			}

			/*
			 *'='のみの時に実行される(=)
			 */
			else {
				token = new Token(Symbol.ASSIGN);
			}
		}

		/*
		 * 最初の文字がシングルクォートの時に実行される (文字型の処理)
		 */
		else if(currentChar == '\'') {
			currentChar = sourceFileScanner.nextChar(); /*二文字目をcurrentCharに読み込む*/
			/*
			 * 二文字目がバックスラッシュの時に実行される (特殊文字の処理) <- 発展課題
			 */
			if(currentChar == '\\') {
				// 後にここの部分に処理を記述する.
			}
			/*
			 * 特殊文字以外の場合
			 */
			else {
				/*
				 * 二文字目もシングルクォートの時にエラーが出るようにする
				 */
				if(currentChar == '\''&&sourceFileScanner.lookAhead()!='\'') syntaxError();
				else value = (int) currentChar; /* 2文字目の文字コードを記憶*/
			}
			currentChar = sourceFileScanner.nextChar(); /*3文字目を読み込む*/
			if(currentChar != '\'') syntaxError(); /* 3文字目がシングルクォートでなければエラーを出す*/
			token = new Token(Symbol.CHARACTER,value); /* 文字のトークン生成*/
		}

		/*
		 * 1文字目が英字の場合('_'も含む) 変数名or予約後の処理
		 */
		else if(Character.isLowerCase(currentChar)||Character.isUpperCase(currentChar)||currentChar == '_') {

			word += currentChar; // 最初の文字を連結させる
			/*
			 * 2文字目以降英字もしくは数字であるかぎり,ループ処理をする
			 */
			while(Character.isLowerCase(sourceFileScanner.lookAhead())||Character.isUpperCase(sourceFileScanner.lookAhead())||Character.isDigit(sourceFileScanner.lookAhead())||sourceFileScanner.lookAhead() == '_') {
				currentChar = sourceFileScanner.nextChar(); /*二文字目以降の文字もcurrentCharに格納する*/
				word += currentChar; //二文字目以降の文字をwordに連結させる
			}

			if(word.equals("main"))
				token = new Token(Symbol.MAIN); /*tokenにMAINを引数にしたインスタンスを生成*/
			else if(word.equals("null"))
				token = new Token(Symbol.NULL); /*tokenにNULLを引数にしたインスタンスを生成*/
			else if(word.equals("if"))
				token = new Token(Symbol.IF); /*tokenにIFを引数にしたインスタンスを生成*/
			else if(word.equals("while"))
				token = new Token(Symbol.WHILE); /*tokenにWHILEを引数にしたインスタンスを生成*/
			else if(word.equals("inputint"))
				token = new Token(Symbol.INPUTINT); /*tokenにINPUTINTを引数にしたインスタンスを生成*/
			else if(word.equals("inputchar"))
				token = new Token(Symbol.INPUTCHAR); /*tokenにINPUTCHARを引数にしたインスタンスを生成*/
			else if(word.equals("outputint"))
				token = new Token(Symbol.OUTPUTINT); /*tokenにOUTPUTINTを引数にしたインスタンスを生成*/
			else if(word.equals("outputchar"))
				token = new Token(Symbol.OUTPUTCHAR); /*tokenにOUTPUTCHARを引数にしたインスタンスを生成*/
			else if(word.equals("break"))
				token = new Token(Symbol.BREAK); /*tokenにBREAKを引数にしたインスタンスを生成*/
			else if(word.equals("continue"))
				token = new Token(Symbol.CONTINUE); /*tokenにCONTINUEを引数にしたインスタンスを生成*/
			else if(word.equals("int"))
				token = new Token(Symbol.INT); /*tokenにINTを引数にしたインスタンスを生成*/
			else if(word.equals("for"))
				token = new Token(Symbol.FOR); /*tokenにFORを引数にしたインスタンスを生成*/
			else if(word.equals("outputstr"))
				token = new Token(Symbol.OUTPUTSTR); /*tokenにOUTPUTSTRを引数にしたインスタンスを生成*/
			else if(word.equals("setstr"))
				token = new Token(Symbol.SETSTR); /*tokenにSETSTRを引数にしたインスタンスを生成*/
			else if(word.equals("else"))
				token = new Token(Symbol.ELSE); /*tokenにELSEを引数にしたインスタンスを生成*/
			else if(word.equals("do"))
				token = new Token(Symbol.DO); /*tokenにDOを引数にしたインスタンスを生成*/
			else if(word.equals("switch"))
				token = new Token(Symbol.SWITCH); /*tokenにSWITCHを引数にしたインスタンスを生成*/
			else if(word.equals("case"))
				token = new Token(Symbol.CASE); /*tokenにCASEを引数にしたインスタンスを生成*/
			else if(word.equals("continue"))
				token = new Token(Symbol.CONTINUE); /*tokenにCONTINUEを引数にしたインスタンスを生成*/
			else if(word.equals("char"))
				token = new Token(Symbol.CHAR); /*tokenにCHARを引数にしたインスタンスを生成*/
			else if(word.equals("boolean"))
				token = new Token(Symbol.BOOLEAN); /*tokenにBOOLEANを引数にしたインスタンスを生成*/
			else if(word.equals("true"))
				token = new Token(Symbol.TRUE); /*tokenにTRUEを引数にしたインスタンスを生成*/
			else if(word.equals("false"))
				token = new Token(Symbol.FALSE); /*tokenにFALSEを引数にしたインスタンスを生成*/
			else if(word.equals("default"))
				token = new Token(Symbol.DEFAULT); /*tokenにDEFAULTを引数にしたインスタンスを生成*/
			else token = new Token(Symbol.NAME,word); /*予約語以外の文字列を変数として処理する*/
		}


		/*
		 * 読み込んだ数字の最初が0の場合.
		 */
		else if(currentChar == '0') {
			/*
			 * 次の文字が'x'の場合(16進数の処理を行う)
			 */
			if(sourceFileScanner.lookAhead() == 'x') {
				sourceFileScanner.nextChar(); /*文字'x'を読み飛ばす*/
				if(Character.digit(sourceFileScanner.lookAhead(), 16)!=-1) {
					currentChar = sourceFileScanner.nextChar();
					value = Character.digit(currentChar, 16); /*valueに16進数の先頭を格納*/
					/*
					 * 次の文字が16進数の数に該当する限りループ処理をする.
					 */
					while(Character.digit(sourceFileScanner.lookAhead(), 16)!= -1) {
						currentChar = sourceFileScanner.nextChar();
						value *= 16; // 位をひとつあげる
						value += Character.digit(currentChar, 16);
					}
					token = new Token(Symbol.INTEGER,value);
				}
				else syntaxError();
			}

			else
			token = new Token(Symbol.INTEGER, 0); /*読み込んだ文字が0のみの場合*/
		}
		/*
		 * 読み込んだ数字の最初が1~9の場合 (整数型の処理)
		 */
		else if (Character.isDigit(currentChar)&&currentChar != '0') {
			//一文字目を読み込む
			value = Character.digit(currentChar, 10);
			/*
			 * 次に読み取る文字が数値の間ループを続ける.
			 */
			while (Character.isDigit(sourceFileScanner.lookAhead())) {
				currentChar = sourceFileScanner.nextChar();
				value *= 10;
				value += Character.digit(currentChar, 10);
			}
			// tokenに整数値のオブジェクトを作成(0を除く)
			token = new Token(Symbol.INTEGER, value);
		}
		/*
		 * 構文エラーの時に実行する
		 */
		else
			syntaxError();
		return token;
	}

	/**
	 * 現在, 読み込んでいるファイルを閉じる.
	 */
	void closeFile() {
		sourceFileScanner.closeFile();
	}

	/**
	 * 現在, ファイルのどの部分を解析しているかを文字列をして返す
	 */
	String analyzeAt() {
		return sourceFileScanner.scanAt();
	}

	/**
	 *  字句解析で構文エラーが出た時に利用されるメソッド
	 */
	@SuppressWarnings("unused")
	private void syntaxError() {
		System.out.print(sourceFileScanner.scanAt());
		//下記の文言は自動採点で使用するので変更しないでください。
		System.out.println("で字句解析プログラムが構文エラーを検出");
		closeFile();
		System.exit(1);
	}
}