package com.binn328.lox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {
    /**
     * 에러가 난 코드를 더 이상 실행하지 않기 위해 사용하는 필드이다.
     */
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * 명령줄에서 jlox를 기동할 때, 파일 경로를 지정하여 스크립트 파일을 실행한다.
     * @param path 스크립트 파일의 경로
     * @throws IOException
     */
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // 종료코드로 에러를 식별한다.
        if (hadError) System.exit(65);
    }

    /**
     * 명령줄에서 jlox를 기동할 때, 대화형으로 실행하여 한 줄씩 실행할 수 있는 프롬프트가 표시된다.
     * @throws IOException
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;) {
            System.out.println("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            // 대화형에서는 발생한 오류 플래그를 초기화시켜주어야 한다.
            hadError = false;
        }
    }

    /**
     * 스캐너가 내놓을 토큰을 눈으로 확인할 수 있도록 화면에 출력한다.
     * @param source
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // 구문 에러가 발생하면 멈춘다.
        if (hadError) return;

        System.out.println(new AstPrinter().print(expression));
    }

    /**
     * 사용자에게 에러가 발생했음을 알리는 함수이다.
     * @param line 에러가 발생한 줄 번호이다.
     * @param message 에러가 발생한 원인에 대한 메시지이다.
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * error 함수의 helper 함수이다.
     * @param line 에러가 발생한 줄 번호이다..
     * @param where 에러가 발생한 부분을 나타낸다.
     * @param message 에러가 발생한 원인에 대한 메시지이다.
     */
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
}
