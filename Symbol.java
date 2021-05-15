package kc;
enum Symbol {
    NULL, //済み
    MAIN,       /* main */ //済み
    IF,         /* if */ //済み
    WHILE,      /* while */ //済み
	FOR,        /* for */ //済み
    INPUTINT,   /* inputint */ //済み
    INPUTCHAR,  /* inputchar */ //済み
    OUTPUTINT,  /* outputint */ //済み
    OUTPUTCHAR, /* outputchar */ //済み
    OUTPUTSTR,  /* outputstr(拡張用) */ //済み
    SETSTR,     /* setstr   (拡張用) */ //済み
    ELSE,       /* else     (拡張用) */ //済み
    DO,         /* do       (拡張用) */ //済み
    SWITCH,     /* switch   (拡張用) */ //済み
    CASE,       /* case     (拡張用) */ //済み
    DEFAULT,    /* default */
    BREAK,      /* break */ //済み
    CONTINUE,   /* continue (拡張用) */ //済み
    INT,        /* int */ //済み
    CHAR,       /* char     (拡張用) */ //済み
    BOOLEAN,    /* boolean  (拡張用) */ //済み
    TRUE,       /* true     (拡張用) */ //済み
    FALSE,      /* false    (拡張用) */ //済み
    EQUAL,      /* == */ // 済み
    NOTEQ,      /* != */ // 済み
    LESS,       /* < */ // 済み
    GREAT,      /* > */ // 済み
    LESSEQ,     /* <=       (拡張用) */ //済み
    GREATEQ,    /* >=       (拡張用) */ //済み
    AND,        /* && */ // 済み
    OR,         /* || */ // 済み
    NOT,        /* ! */ // 済み
    ADD,        /* + */ // 済み
    SUB,        /* - */ // 済み
    MUL,        /* * */ // 済み
    DIV,        /* / */ // 済み
    MOD,        /* % */ // 済み
    ASSIGN,     /* = */ // 済み
    ASSIGNADD,  /* += */ // 済み
    ASSIGNSUB,  /* -= */ // 済み
    ASSIGNMUL,  /* *= */ // 済み
    ASSIGNDIV,  /* /= */ // 済み
    ASSIGNMOD,  /* %=       (拡張用) */ //済み
    INC,        /* ++ */ // 済み
    DEC,        /* -- */ // 済み
    SEMICOLON,  /* ; */ // 済み
    COLON,      /* : */
    LPAREN,     /* ( */ // 済み
    RPAREN,     /* ) */ // 済み
    LBRACE,     /* { */ // 済み
    RBRACE,     /* } */ // 済み
    LBRACKET,   /* [ */ // 済み
    RBRACKET,   /* ] */ // 済み
    COMMA,      /* , */ // 済み
    INTEGER,    /* 整数 */ // 済み
    CHARACTER,  /* 文字 */ //済み
    NAME,       /* 変数名 */
    STRING,     /* 文字列   (拡張用) */
    ERR,        /* エラー */
    EOF         /* end of file */ // 済み
}