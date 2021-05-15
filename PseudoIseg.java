package kc;

import java.io.*;   // ファイル入出力用
import java.util.*; // ArrayList用

/**
 * 各命令(Instractions.class のインスタンス) を格納するテーブルをArrayListで作る．
 * ここで作った命令列をファイルに出力し,  VSM の Iseg に改めて格納するので, 
 * PseudoIseg(仮のIseg) と名付けた．
 * ArrayListによる命令表と, 命令表を操作するいくつかのメソッドを定義している.
 */
class PseudoIseg {
    ArrayList<Instruction> pIseg; //表本体
    int pIsegPtr; //命令数カウンタ

    PseudoIseg() {
        pIseg = new ArrayList<Instruction>(); //表本体を作成
        pIsegPtr = 0;         //命令カウンタは 0        
    }
    
    /**
     * 一つの命令を作って, 表に格納するメソッド．
     * しかし実際には, このあとで定義する appendCode や appendCode 
     * からのみ利用される
     * @param opCode オペレータ
     * @param flag フラグメントレジスタ
     * @param addr オペランド
     * @return 追加した命令の表内での位置
     */
    int setI (Operator opCode, int flag, int addr) {
         /* オペレータ opCode, アドレス修飾値 flag, オペランド addr
         から, 一命令を作る．*/
         Instruction inst = new Instruction(opCode,flag,addr);

         //作った命令を表に追加し, カウンタをインクレメントする．
         pIseg.add(inst);
         ++pIsegPtr; 

         //返り値は, 追加した命令の表内での位置．
         return pIsegPtr-1;
      }

    /** 
     * オペランドを必要とするオペレータを追加するメソッド．
     * @param opCode オペレータ
     * @param addr オペランド
     * @return 追加した命令の表内での位置
     */
    int appendCode(Operator opCode, int addr) {
        return setI(opCode, 0, addr);
    }

    /** 
     * オペランドを必要としないオペレータを追加するメソッド．
     * @param opCode オペレータ
     * @return 追加した命令の表内での位置
     */
    int appendCode(Operator opCode) {
        return setI(opCode, 0, 0);
    }

    /**
     * @return 表内に格納された最後の命令の位置
     */
    int getLastCodeAddress () {
        return pIsegPtr-1;
    }

    /** 
     * 表内の全ての命令を表示するメソッド．
     */
    void dump() {
        for (int i = 0; i < pIsegPtr; i++) {
            System.out.print(i + ": ");
            System.out.println(pIseg.get(i).printInstruction());
        }
    }

    /** 
     * 表内の全ての命令をファイルに出力するメソッド．
     * 引数なしで呼ばれた場合,ファイル名は "OpCode.asm" になる．
     */
    void dump2file() {
        PrintWriter outputFile = null;
        try {
            outputFile = new PrintWriter(
                             new BufferedWriter(
                                 new FileWriter("OpCode.asm")));
            for (int i = 0; i < pIsegPtr; i++)
                outputFile.println((pIseg.get(i)).printInstruction());
       } catch(IOException exception) {
           System.out.println(exception);
       } finally {
            outputFile.close();
            System.exit(1);
       }
    }

    /**
     * 表内の全ての命令をファイルに出力するメソッド．
     * 引数ありで呼ばれた場合,ファイル名は その引数 になる．
     * @param outputFileName 出力ファイル名
     */
    void dump2file (String outputFileName) {
        PrintWriter outputFile = null;
        try {
            outputFile = new PrintWriter(
                             new BufferedWriter(
                                 new FileWriter(outputFileName)));
            for (int i = 0; i < pIsegPtr; i++)
                outputFile.println((pIseg.get(i)).printInstruction());
        } catch(IOException exception) {
            System.out.println(exception);
        } finally {
            outputFile.close();
            System.exit(1);
        }
    }

    /**
     * ptr 番目の命令の オペレータ を opCode に変更するメソッド
     * @param ptr 変更する位置
     * @param opCode オペレータ
     */
    void replaceCode (int ptr, Operator opCode) {
        int oldReg = (pIseg.get(ptr)).getReg();
        int oldAddr = (pIseg.get(ptr)).getAddr();
        Instruction inst = new Instruction (opCode, oldReg, oldAddr);
        pIseg.remove (ptr);
        pIseg.add (ptr,inst);
    }

    /**
     * ptr 番目の命令の オペランド を addrs に変更するメソッド
     * @param ptr 変更する位置
     * @param addrs オペランド
     */
    void replaceCode (int ptr, int addrs) {
        int oldReg = pIseg.get(ptr).getReg();
        Operator oldOp = pIseg.get(ptr).getOperator();
        Instruction inst = new Instruction (oldOp, oldReg, addrs);
        pIseg.remove (ptr);
        pIseg.add (ptr, inst);
    }
    
    /**
     * ISeg の指定した番地の命令を返す
     * @param ptr 返す命令の位置
     * @return 指定した番地の命令
     */
    Instruction getInstruction (int ptr) {
       return pIseg.get (ptr);
    }

    /**
     * ISeg の指定した番地のオペレータを返す
     * @param ptr 返すオペレータの位置
     */
    Operator getOperator (int ptr) {
        return pIseg.get(ptr).getOperator();
    }

    /**
     * ISeg の指定した番地のオペランドを返す
     * @param ptr 返すオペランドの位置
     */
    int getOperand (int ptr) {
        return pIseg.get(ptr).getAddr();
    }

    /**
     * ISeg の指定した番地の命令を削除する
     * @param ptr 削除する位置
     */
    void removeCode (int ptr) {
       pIseg.remove (ptr); 
       --pIsegPtr;           // 命令カウンタを1減らす
    }

    /**
     * ISeg の末尾の番地の命令を削除する
     */
    void removeLastCode() {
        pIseg.remove (pIsegPtr-1);
        --pIsegPtr;           // 命令カウンタを1減らす
    }

    /**
     * 指定した番地の命令が引数で与えられた命令と一致するか判定するメソッド
     * @paran prt 番地
     * @param opCode 命令
     * @return 命令が一致すればtrueを返す
     */
    boolean checkOperator (int ptr, Operator opCode) {
        return pIseg.get(ptr).equals (opCode);
    }

    /**
     * 指定した番地の命令が引数で与えられた命令と一致するか判定するメソッド
     * @paran prt 番地
     * @param op 文字列形式の命令
     * @return 命令が一致すればtrueを返す
     */
    boolean checkOperator (int ptr, String op) {
        return pIseg.get(ptr).equals (op);
    }    
}