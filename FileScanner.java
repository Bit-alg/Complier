package kc;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * このクラスは, 指定されたファイルを読み込んだり,ファイル行を取り出し文字を出力する.
 * また,これは字句解析に利用される.
 * @author 18-1-037-0110 花光大輔
 * 問題番号 問題2.7
 * 提出日: 2020年5月20日
 *
 */
class FileScanner {

	private BufferedReader sourceFile; //入力ファイルの参照
	private String line; //行バッファ
	private int lineNumber; //行カウンタ
	private int columnNumber; //列カウンタ
	private char currentCharacter; //読み取り文字
	private char nextCharacter; //先読み文字

	/**
	 * FileScannerのコンストラクタ
	 * パスを利用して指定のファイルを参照する
	 * もし,ファイルの名前が不正な場合は例外処理をする
	 * @param sourceFileName ファイル名
	 */
	FileScanner(String sourceFileName) {
		Path path = Paths.get(sourceFileName);
		// ファイルのオープン
		try {
			sourceFile = Files.newBufferedReader(path);
		}
		// 指定されたファイルの形式が合わない場合
		catch (IOException err_mes) {
			System.out.printf("指定されたファイルは不正です.ファイル名:%s\n", sourceFileName);
			System.exit(1);
		}

		lineNumber = 0; //行カウンタを0に初期化している
		columnNumber = -1; //列カウンタを-1に初期化している
		nextCharacter = '\n'; // nextCharcterを'\n'に初期化している
		nextChar(); // nextCharacterに最初の文字を読み込むようしている.

	}

	/**
	 * 指定したファイルを閉じる
	 */
	void closeFile() {
		try {
			sourceFile.close();
		} catch (IOException err_mes) {
			System.out.println(err_mes);
			System.exit(1);
		}
	}

	/**
	 * コンストラクタ上で指定したファイルを開く.
	 * このファイルに未読の行があるかないかで処理を分けている
	 * また,ファイルを行ごとに読み込む際に改行を挿入しておく
	 */
	void readNextLine() {
		try {
			// ファイルにまだ読み込まれていない行があるかを判断する
			if (sourceFile.ready()) {
				/*
				 *ファイルの次の行を読み取る.
				 *また,その際に読み込んだ文字列は改行を含んでいないので'\n'を文字列に連結する.
				 */
				line = sourceFile.readLine();
				line += '\n'; // 行末に'\n'を連結する.

			} else {
				line = null;
			}
		} catch (IOException err_mes) { // 例外は Exception でキャッチしてもいい
			// ファイルの読み出しエラーが発生したときの処理
			System.out.println("入力したファイルは不正です.");
			closeFile();
			System.exit(1);
		}
	}

	/**
	 * nextCharacerの文字を返す
	 */
	char lookAhead() {
		return nextCharacter;
	}

	/**
	 * フィールド lineNumberの値に応じた行の文字列を返す
	 */
	String getLine() {
		return line;
	}

	/**
	 * 次の文字を参照して,現在の文字のフィールドに格納する.
	 * また, ファイル末に達した場合と行末に達した場合, 行の途中の場合に応じて処理を変える
	 */
	char nextChar() {

		currentCharacter = nextCharacter;

		// ファイル末に達した場合
		if(nextCharacter == '\0') {
		}

		// 行末に達した場合
		else if(nextCharacter == '\n') {
			readNextLine();  /*次のファイルの行を読み取る*/
			/*
			 * 次の行がnullでない場合
			 */
			if(getLine() != null) {

				nextCharacter = getLine().charAt(0); /*行の先頭の文字を取得する*/
				lineNumber++; //行数カウンタを+1する
				columnNumber = 0; // コラムカウンタを0にリセットする
			}
			/*
			 * 次の行がnull(何もない, 最終行)の場合
			 */
			else {
				nextCharacter = '\0';
			}
		}
		// 行の途中の場合
		else {
			columnNumber++; // 列カウンタをインクリメントする.
			nextCharacter = getLine().charAt(columnNumber); /*指定した列カウンタの文字を取得する*/
		}

		return currentCharacter;
	 }

	/**
	 * 指定したファイルの現在どこをスキャンしているかをわかりやすく表記する.
	 */
	String scanAt() {
		String message = lineNumber + "行目\n" + line;
        for (int i = 0; i < columnNumber - 1; ++i)
            message += " ";
        message += "*\n";
        return message;

	 }

	public static void main(String args[]) {
		FileScanner scanner = new FileScanner(args[0]); /*指定したファイルを読み取る*/
		/*
		 * ファイル末にいくまで出力を行う.
		 */
		while(scanner.getLine()!= null) {

			//scanner.readNextLine();
			//System.out.print(Scanner.getLine());
			System.out.print(scanner.nextChar()); // 文字出力をする.
			//System.out.print(scanner.scanAt());

		}


		scanner.closeFile(); /*ファイルを閉じる*/


	}
}