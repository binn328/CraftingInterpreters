package com.binn328.lox;

import java.util.List;

import static com.binn328.lox.TokenType.*;

/**
 * 문법 규칙을 파싱하는 파서 클래스
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * expression 규칙
     * expression -> equality;
     * @return
     */
    private Expr expression() {
        return equality();
    }

    /**
     * equality 규칙
     * equality -> comparison ( ( "!=" | "==" ) comparison )* ;
     * @return
     */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * comparison 규칙
     * comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     * @return
     */
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * 덧셈과 뺄샘
     * @return
     */
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * 곱셈과 나눗셈
     * @return
     */
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * unary 규칙: 단항 연산자
     * unary -> ( "!" | "-" ) unary
     *          | primary;
     * @return
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    /**
     * primary 규칙
     * primary -> NUMBER | STRING | "true" | "false" | "nil"
     *            | "(" expression ")" ;
     * @return
     */
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false));
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
    }

    /**
     * ( ... )* 루프를 빠져나가기 위한 조건을 검사하는 메소드
     * 현재 토큰이 주어진 타입 중 하나라도 해당되는지 확인한다.
     * @param types
     * @return
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * 주어진 토큰을 검색한다.
     * @param type
     * @param message
     * @return
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    /**
     * 현재 토큰이 주어진 타입이면 true를 반환한다.
     * match와 달리 토큰을 소비하지 않는다.
     * @param type
     * @return
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    /**
     * 현재 토큰을 소비하고 반환한다.
     * @return
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * 파싱할 토큰이 남아있는지 확인한다.
     * @return
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * 토큰을 소비하지 않고 현재 토큰을 반환한다.
     * @return
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * 바로 직전에 소비된 토큰을 반환한다.
     * @return
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * 에러를 알린다.
     * @param token
     * @param message
     * @return
     */
    private ParseError report(Token token, String message) {
        Lox.error(token, message);
        return new ParserError();
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }


}
