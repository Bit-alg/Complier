package kc;

/**
 *
 * SLexicalAnalyzer の動作試験のためのプログラム
 */
public class SLexerTester {
    static SLexicalAnalyzer lexer;  // 試験対象となる字句解析器

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java kc.SLexerTester file");
            System.exit(1);
        }

        lexer = new SLexicalAnalyzer(args[0]);   // 試験対象の生成

        Token token=lexer.nextToken();           // トークン読み取り

        /* 読み取ったトークンがEOFになるまで
         * その種別，値を表す文字列を表示する
         */
        while (!(token.checkSymbol(Symbol.EOF))) {
        	switch (token.getSymbol()) {
        	case EQUAL:
        		System.out.println("==");
        		break;
        	case NOTEQ:
        		System.out.println("!=");
        		break;
        	case NOT:
        		System.out.println("!");
        		break;
        	case ADD:
        		System.out.println("+");
        		break;
        	case ASSIGN:
        		System.out.println("=");
        		break;
        	case INTEGER: // トークンが整数の場合はその値も表示
        		System.out.println("Integer "+token.getIntValue());
        		break;
        	default:      // いずれにも合致しない場合
        		System.out.println("?????");
        		break;
        	}
        	token= lexer.nextToken(); // 次のトークンの読み取り
        }
        lexer.closeFile();        // ファイルを閉じて終了
    }
}