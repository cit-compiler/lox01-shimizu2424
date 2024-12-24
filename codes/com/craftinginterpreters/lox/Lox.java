package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64); 
        } else if (args.length == 1) {
            runFile(args[0]);
            //引数の名前のファイルを実行
        } else {
            runPrompt();
            //引数に何も入力しなかった場合入力待ちの画面を表示
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
        //エラーがあったときは中断
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader render = new BufferedReader(input);

        for (;;) {
            System.out.print(">");
            String line = render.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
            //ユーザーがミスをしても、セッション全体が終了してはいけません。
        }
        //ctrl cが入るとnullが入るためプログラムが終わる
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
    
        Parser parser = new Parser(tokens);
        //Expr expression = parser.parse();
        List<Stmt> statements = parser.parse();
    
        // Stop if there was a syntax error.
        if (hadError) return;
    
        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
            "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    //loxは初期化時の文字列によって型を決める
    //tokenに分けることを字句解析という
    //tokenを見つけて区別する必要がある
    //カッコやセミコロンなどの文字を見つけたことを記録

}//742から

