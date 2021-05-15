package kc;

/**
 * @author 18-1-037-0110 花光大輔
 * 問題番号 問題 2.10
 * 提出日 2020年5月27日
 *
 * このクラスは変数の型や名前,変数を格納しているアドレスを用意する.
 */
class Var {

	private Type type; /*変数の型を決める*/
	private String name; /*変数名*/
	private int address; /*Dseg上のアドレス*/
	private int size; /*配列の場合, サイズとして用いる*/
	//private boolean assigned; /*代入されたか*/
	//private boolean reffered; /*参照されたか*/


    /**
     * コンストラクタ
     * 各フィールドに対して引数を用いて初期化をする.
     */
    Var(Type type, String name, int address, int size) {
    		this.type = type;
    		this.name = name;
    		this.address = address;
    		this.size = size;
    }

    /**
     * typeのゲッター
     */
    Type getType() {
     return type;
    }

    /**
     *  nameのゲッター
     */
    String getName() {
       return name;
    }

    /**
     * addressのゲッター
     */
    int getAddress() {
        return address;
    }

    /**
     * sizeのゲッター
     */
    int getSize() {
       return size;
    }
}