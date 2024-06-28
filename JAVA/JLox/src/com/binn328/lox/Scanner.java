package com.binn328.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.binn328.lox.TokenType.*;

public class Scanner {
    /**
     * 소스 코드
     */
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    /**
     * 스캔 중인 렉심의 첫 번째 문자의 위치
     */
    private int start = 0;
    /**
     * 현재 처리 중인 문자의 위치
     */
    private int current = 0;
    /**
     * current가 위치한 소스 줄 번호
     */
    private int line = 1;
    /**
     * 예약어를 저장해두는 맵
     */
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("ture", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    /**
     * 소스코드를 처음부터 끝가지 쭉 읽어들여 더이상 문자가 없을 때까지 토큰을 추가한다.
     * 제일 마지막에는 EOF 토큰을 붙인다.
     *
     * @return
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * 문자를 모두 소비했는지 체크하는 헬퍼 메소드
     *
     * @return
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * 소스파일의 다음 문자를 읽어 반환한다.
     *
     * @return
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * 현재 렉심의 텍스트를 가져와서 새 토큰을 만든다.
     *
     * @param type
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * 현재 렉심의 텍스트를 가져와서 새 토큰을 만든다.
     *
     * @param type
     * @param literal
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // 두 번째 /를 찾아도 토큰을 종료시키지 않고 줄 끝까지 문자를 소비한다.
                    // 주석은 //로 입력되기 때문에 이런 동작이 필요하다.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            // 필요없는 공백 문자들은 무시한다.
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;

            case '"':
                string();
                break;

            default:
                // 숫자 렉심을 처리한다.
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }

                break;
        }
    }

    /**
     * 식별자를 처리하는 메소드
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        // 식별자를 스캔한 다음, map에 포함된 예약어와 매칭되는게 하나라도 있는지 검사한다.
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        // 매칭되는 것이 있으면 TokenType을 이용, 아니면 사용자가 정의한 식별자로 처리한다.
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    /**
     * 숫자 리터럴을 모두 소비하는 메소드
     */
    private void number() {
        while (isDigit(peek())) advance();

        // 소수부분을 peek 한다.
        if (peek() == '.' && isDigit(peekNext())) {
            // .을 소비한다.
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /**
     * 문자열 리터럴을 처리하는 메소드
     */
    private void string() {
        // 맨 끝에 "가 나올때까지 문자를 소비한다.
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        // 문자열이 닫히기 전에 문자가 소진되면 에러를 출력한다.
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // 닫는 "를 처리
        advance();

        // 문자열 앞 뒤에 존재하는 ""를 제거하고 토큰을 추가한다.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * 문자 2개짜리 렉심을 확인하기 위해 사용하는 헬퍼 메소드
     *
     * @param expected
     * @return
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    /**
     * 문자 2개짜리 렉심을 확인하기 위해 사용하지만, 문자를 소비하지 않는다.
     *
     * @return
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * 소수점 뒤에 존재하는 소수부분을 살펴본다.
     *
     * @return
     */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    /**
     * 해당 문자가 알파벳이나 _인지 검사하는 메소드
     * @param c 검사할 문자
     * @return
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    /**
     * 해당 문자가 알파벳이나 _, 숫자인지 검사하는 메소드
     * @param c 검사할 문자
     * @return
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * 문자의 숫자 여부를 확인하는 메소드
     *
     * @param c 검사할 문자
     * @return
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
