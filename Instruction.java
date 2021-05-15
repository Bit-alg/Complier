package kc;

/**
 * PseudoIseg の要素．つまり１つの命令 を格納する要素の定義
 */
class Instruction {
    private Operator operator;  //オペレータ
    private int reg ;           //アドレス修飾用
    private int addr;           //オペランド

    /**
        Operatorクラスの要素から命令オブジェクトを作るためのコンストラクタ
        @param opcode 命令
        @param flag その命令のアドレス修飾子
        @param address オペランド
    */
    Instruction (Operator opcode, int flag, int address) {
        operator = opcode;
        reg = flag;
        addr = address;
    }

    /**
        文字列から命令オブジェクトを作るためのコンストラクタ
        @param opcode 文字列形式の命令
        @param flag その命令のアドレス修飾子
        @param address オペランド
    */
    Instruction (String opcode, int flag, int address) {
        operator = str2Opeartor (opcode);
        reg = flag;
        addr = address;
    }

    /**
     * 命令を出力するためのメソッド
     */
    String printInstruction() {
        /* 命令を出力する際, 命令のop部がop_oprndOuts
           内のものかどうかを区別する必要がある．*/
        String op_oprndCodeList =
            "PUSH PUSHI POP POPI BLT BLE BEQ BNE BGE BGT JUMP";

        //命令のop部がop_oprndCodeList 内のものでなければ, opのみを出力
        switch (operator) {
            case PUSH:
            case PUSHI:
            case POP:
            case BLT:
            case BLE:
            case BEQ:
            case BNE:
            case BGE:
            case BGT:
            case JUMP:
                // オペレータとオペランドを出力
                return  operator.name() + "\t" + addr;
            default:
                // オペレータのみを出力
                return  operator.name();
        }
    }

    /**
     * operator フィールドの getter
     * @return operator
     */
    Operator getOperator() {
        return operator;
    }

    /**
     * addr フィールドの getter
     * @return addr
     */
    int getAddr() {
        return addr;
    }

    /**
     * reg フィールドの getter
     * @return reg
     */
    int getReg() {
        return reg;
    }

    /**
     * 命令が引数で指定した命令と一致するか判定するメソッド
     * @param opcode 命令
     * @return 命令が引数の命令と一致すればtrue
     */
    boolean equals (Operator opcode) {
        return (operator.equals (opcode));
    }

    /**
     * 命令が引数で指定した命令と一致するか判定するメソッド
     * @param opcode 文字列形式の命令
     * @return 命令が引数の命令と一致すればtrue
     */
    boolean equals (String op) {
        Operator opcode = str2Opeartor(op);
        return (operator.equals (opcode));
    }

    /**
     * StringをOperatorに変換する
     * @param str 文字列形式の命令
     * @return Operator型の命令
     */
    Operator str2Opeartor (String op) {
        if (op.equals ("NOP"))           return Operator.NOP;
        else if (op.equals ("ASSGN"))    return Operator.ASSGN;
        else if (op.equals ("ADD"))      return Operator.ADD;
        else if (op.equals ("SUB"))      return Operator.SUB;
        else if (op.equals ("MUL"))      return Operator.MUL;
        else if (op.equals ("DIV"))      return Operator.DIV;
        else if (op.equals ("MOD"))      return Operator.MOD;
        else if (op.equals ("CSIGN"))    return Operator.CSIGN;
        else if (op.equals ("AND"))      return Operator.AND;
        else if (op.equals ("OR"))       return Operator.OR;
        else if (op.equals ("NOT"))      return Operator.NOT;
        else if (op.equals ("COMP"))     return Operator.COMP;
        else if (op.equals ("COPY"))     return Operator.COPY;
        else if (op.equals ("PUSH"))     return Operator.PUSH;
        else if (op.equals ("PUSHI"))    return Operator.PUSHI;
        else if (op.equals ("REMOVE"))   return Operator.REMOVE;
        else if (op.equals ("POP"))      return Operator.POP;
        else if (op.equals ("INC"))      return Operator.INC;
        else if (op.equals ("DEC"))      return Operator.DEC;
        else if (op.equals ("JUMP"))     return Operator.JUMP;
        else if (op.equals ("BLT"))      return Operator.BLT;
        else if (op.equals ("BLE"))      return Operator.BLE;
        else if (op.equals ("BEQ"))      return Operator.BEQ;
        else if (op.equals ("BNE"))      return Operator.BNE;
        else if (op.equals ("BGE"))      return Operator.BGE;
        else if (op.equals ("BGT"))      return Operator.BGT;
        else if (op.equals ("HALT"))     return Operator.HALT;
        else if (op.equals ("INPUT"))    return Operator.INPUT;
        else if (op.equals ("INPUTC"))   return Operator.INPUTC;
        else if (op.equals ("OUTPUT"))   return Operator.OUTPUT;
        else if (op.equals ("OUTPUTC"))  return Operator.OUTPUTC;
        else if (op.equals ("OUTPUTLN")) return Operator.OUTPUTLN;
        else if (op.equals ("LOAD"))     return Operator.LOAD;
        else                             return Operator.ERR;
    }
}
