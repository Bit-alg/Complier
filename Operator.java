package kc;

/**
 * 各VSMで利用可能な operator を列挙型で定義する． 
 */
enum Operator {
  NOP,
  ASSGN,
  ADD,
  SUB,
  MUL,
  DIV,
  MOD,
  CSIGN,
  AND,
  OR,
  NOT,
  COMP,
  COPY,
  PUSH,
  PUSHI,
  REMOVE,
  POP,
  INC,
  DEC,
  JUMP,
  BLT,
  BLE,
  BEQ,
  BNE,
  BGE,
  BGT,
  HALT,
  INPUT,
  INPUTC,
  OUTPUT,
  OUTPUTC,
  OUTPUTLN,
  LOAD,
  ERR   // エラー
}