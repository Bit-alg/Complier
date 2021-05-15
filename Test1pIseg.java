package kc;

/** 一つの命令はInstruction クラスのインスタンスに格納され, そのインスタンスが
命令表(PseudoIseg クラスのインスタンス) に格納される．
このプログラムでは, 命令を作って表に格納したり, 命令のオペレータやオペランドを
書き換えたり, 全命令を表示したり, ファイルに出力する方法を例示する*/
public class Test1pIseg {
	public static void main(String[] args) {
		PseudoIseg iseg = new PseudoIseg(); //命令を格納するiseg を作る
       /* PseudoIseg のメソッドappendCode を用いて、iseg に各命令を格納していく．
		  appendCode の返り値はint だが, 例示には必要ないのでとりあえず返り値は
		  無視して作っている*/
		iseg.appendCode(Operator.PUSHI,1);
		iseg.appendCode(Operator.INPUT);
		iseg.appendCode(Operator.ASSGN);
		iseg.appendCode(Operator.REMOVE);
		iseg.appendCode(Operator.PUSHI, 2);
		iseg.appendCode(Operator.INPUTC);
		iseg.appendCode(Operator.ASSGN);
		iseg.appendCode(Operator.REMOVE);
		iseg.appendCode(Operator.PUSH, 1);
		iseg.appendCode(Operator.OUTPUT);
		iseg.appendCode(Operator.PUSH, 1);
		iseg.appendCode(Operator.OUTPUTC);
		iseg.appendCode(Operator.HALT);
		

        //iseg 内の全命令を表示
		System.out.println("全命令の表示");
		iseg.dump();

        //4 番地の命令のOperator をPUSH に変更
		iseg.replaceCode(4, Operator.PUSH);
		System.out.println("\n 4 番地の命令のOperator をPUSH に変更"
						   +"\n した後の全命令の表示");
		iseg.dump();

        //10 番地の命令のOperand を5 に変更
		iseg.replaceCode(10,5);
		System.out.println("\n 10 番地の命令のOperand を5 に変更"
						   +"\n した後の全命令の表示");
		iseg.dump();

        //OpCode.asm というファイルにiseg 内の全命令を出力
		iseg.dump2file();
		System.out.println("OpCode.asm ファイルに命令を出力しました.");

        //test.asm というファイルにiseg 内の全命令を出力
		String outputFileName = "test.asm";
		iseg.dump2file(outputFileName);
		System.out.println(outputFileName + "ファイルに命令を出力しました.");
	}
}