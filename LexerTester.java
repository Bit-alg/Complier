package kc;

/**
 * LexicalAnalyzer の動作試験のためのプログラム
 */
public class LexerTester {
	static LexicalAnalyzer lexer; // 試験対象となる字句解析器

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: java kc.LexerTester file");
			System.exit(1);
		}

		lexer = new LexicalAnalyzer(args[0]); // 試験対象の生成

		Token token = lexer.nextToken(); // トークン読み取り

		/*
		 * 読み取ったトークンがEOFになるまで その種別，値を表す文字列を表示する
		 */
		while (!(token.checkSymbol(Symbol.EOF))) {
			switch (token.getSymbol()) {
			case NULL:
				System.out.println("NULL");
				break;
			case MAIN:
				System.out.println("MAIN");
				break;
			case IF:
				System.out.println("IF");
				break;
			case WHILE:
				System.out.println("WHILE");
				break;
			case FOR:
				System.out.println("FOR");
				break;
			case INPUTINT:
				System.out.println("INPUTINT");
				break;
			case INPUTCHAR:
				System.out.println("INPUTCHAR");
				break;
			case OUTPUTINT:
				System.out.println("OUTPUTINT");
				break;
			case COLON:
				System.out.println("COLON");
				break;
			case DEFAULT:
				System.out.println("DEFAULT");
				break;
			case OUTPUTCHAR:
				System.out.println("OUTPUTCHAR");
				break;
			case OUTPUTSTR:
				System.out.println("OUTPUTSTR");
				break;
			case SETSTR:
				System.out.println("SETSTR");
				break;
			case ELSE:
				System.out.println("ELSE");
				break;
			case DO:
				System.out.println("DO");
				break;
			case SWITCH:
				System.out.println("SWITCH");
				break;
			case CASE:
				System.out.println("CASE");
				break;
			case BREAK:
				System.out.println("BREAK");
				break;
			case CONTINUE:
				System.out.println("CONTINUE");
				break;
			case INT:
				System.out.println("INT");
				break;
			case CHAR:
				System.out.println("CHAR");
				break;
			case BOOLEAN:
				System.out.println("BOOLEAN");
				break;
			case TRUE:
				System.out.println("TRUE");
				break;
			case FALSE:
				System.out.println("FALSE");
				break;
			case EQUAL:
				System.out.println("==");
				break;
			case NOTEQ:
				System.out.println("!=");
				break;
			case LESS:
				System.out.println("<");
				break;
			case GREAT:
				System.out.println(">");
				break;
			case LESSEQ:
				System.out.println("<=");
				break;
			case GREATEQ:
				System.out.println(">=");
				break;
			case AND:
				System.out.println("&&");
				break;
			case OR:
				System.out.println("||");
				break;
			case NOT:
				System.out.println("!");
				break;
			case ADD:
				System.out.println("+");
				break;
			case SUB:
				System.out.println("-");
				break;
			case MUL:
				System.out.println("*");
				break;
			case DIV:
				System.out.println("/");
				break;
			case MOD:
				System.out.println("%");
				break;
			case ASSIGN:
				System.out.println("=");
				break;
			case ASSIGNADD:
				System.out.println("+=");
				break;
			case ASSIGNSUB:
				System.out.println("-=");
				break;
			case ASSIGNMUL:
				System.out.println("*=");
				break;
			case ASSIGNDIV:
				System.out.println("/=");
				break;
			case ASSIGNMOD:
				System.out.println("%=");
				break;
			case INC:
				System.out.println("++");
				break;
			case DEC:
				System.out.println("--");
				break;
			case SEMICOLON:
				System.out.println(";");
				break;
			case LPAREN:
				System.out.println("(");
				break;
			case RPAREN:
				System.out.println(")");
				break;
			case LBRACE:
				System.out.println("{");
				break;
			case RBRACE:
				System.out.println("}");
				break;
			case LBRACKET:
				System.out.println("[");
				break;
			case RBRACKET:
				System.out.println("]");
				break;
			case COMMA:
				System.out.println(",");
				break;
			case INTEGER: // トークンが整数の場合はその値も表示
				System.out.println("Integer " + token.getIntValue());
				break;
			case CHARACTER: // トークンが文字の場合はその文字を'で囲んで表示
				System.out.println("Character '" + (char) token.getIntValue() + "'");
				break;
			case NAME: // トークンが名前の場合はその文字列を表示
				System.out.println("Name " + token.getStrValue());
				break;
			case STRING: // トークンが文字列の場合はその文字列を"で囲んで表示
				System.out.println("String \"" + token.getStrValue() + "\"");
				break;
			default: // いずれにも合致しない場合
				System.out.println("?????");
				break;
			}
			token = lexer.nextToken(); // 次のトークンの読み取り
		}
		lexer.closeFile(); // ファイルを閉じて終了
	}
}