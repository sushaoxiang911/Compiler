package cs132.proj1;

import java.util.Scanner;

enum Type {
	NUM, INCROP, BINOP, LSIGN, LEFTPAR, RIGHTPAR, COMMENT, EOF
}


class Token {
	private String value;
	private Type type;
	public Token () {
		this.type = Type.EOF;
	}
	public Token (String value) throws Exception {
		this.value = value;
		if (value.equals("0") ||
				value.equals("1") ||
				value.equals("2") ||
				value.equals("3") ||
				value.equals("4") ||
				value.equals("5") ||
				value.equals("6") ||
				value.equals("7") ||
				value.equals("8") ||
				value.equals("9"))
			type = Type.NUM;
		else if (value.equals("++") || value.equals("--"))
			type = Type.INCROP;
		else if (value.equals("+") || value.equals("-"))
			type = Type.BINOP;
		else if (value.equals("$"))
			type = Type.LSIGN;
		else if (value.equals("("))
			type = Type.LEFTPAR;
		else if (value.equals(")"))
			type = Type.RIGHTPAR;
		else if (value.equals("#"))
			type = Type.COMMENT;
		else
			throw new Exception();
	}
	
	public Type getType() {
		return type;
	}
	
	public String getValue() {
		return value;
	}	
}

class TokenScanner {
	private int lineNum;
	private String currentLine;
	private int cursor;
	private Scanner scan;
	
	private void ChangeToNewLine() {
		lineNum ++;
		currentLine = scan.nextLine();
		cursor = 0;
		MoveToNonWhiteSpace();
	}
	private void MoveToNonWhiteSpace() {
		while (cursor < currentLine.length() && Character.isWhitespace(currentLine.charAt(cursor)))
			cursor++;
	}
	public TokenScanner() {
		lineNum = 0;
		currentLine = "";
		cursor = 0;
		scan = new Scanner(System.in);
	}
	
	
	public Token getNextToken() throws Exception {
		MoveToNonWhiteSpace();
		while (currentLine.isEmpty() || cursor >= currentLine.length() || currentLine.charAt(cursor) == '#') {
			if (!scan.hasNextLine())
				return new Token();
			ChangeToNewLine();
		}
		
		char currentChar = currentLine.charAt(cursor);
		cursor++;
		if (currentChar == '+' || currentChar == '-') {
			while (currentLine.isEmpty() || cursor >= currentLine.length() || currentLine.charAt(cursor) == '#') {
				if (!scan.hasNextLine()) {
					String result = "";
					result += currentChar;
					return new Token(result);
				}
				ChangeToNewLine();
			}
			if (currentLine.charAt(cursor) == currentChar) {
				cursor++;
				String result = "";
				result += currentChar;
				result += currentChar;
				return new Token(result);
			} else {
				String result = "";
				result += currentChar;
				return new Token(result);
			}
		} else {
			String result = "";
			result += currentChar;
			return new Token(result);
		}
	}
	public int getLineNumber() {
		return lineNum;
	}
}

class TokenParser {
	Token currentToken;
	
	String goal(TokenScanner ts) throws Exception {
		currentToken = ts.getNextToken();
    	String result = expr(ts);
        if (currentToken.getType() != Type.EOF)
            throw new Exception();
		return new String(" " + result);
	}
	
	String expr(TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.LEFTPAR || 
				currentType == Type.INCROP ||
				currentType == Type.NUM ||
				currentType == Type.LSIGN) {
			String part1 = expr1(ts);
			String part2 = exprPrime(ts);
			if (!part2.equals(""))
				return new String(part1 + " " + part2);
			else
				return part1;
		} else 
			throw new Exception();
	}
	
	String exprPrime(TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.LEFTPAR ||
				currentType == Type.NUM ||
				currentType == Type.LSIGN) {
			String part1 = conexpr1(ts);
			String part2 = exprPrime(ts);
			if (!part2.equals("")) 
				return new String (part1 + " " + "_" + " " + part2);
			else 
				return new String (part1 + " " + "_");
		} else if (currentType == Type.RIGHTPAR ||
					currentType == Type.EOF) {
			return "";
		} else 
			throw new Exception();
	}
	
	String conexpr1 (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.LEFTPAR ||
				currentType == Type.NUM ||
				currentType == Type.LSIGN) {
			String part1 = expr3(ts);
			String part2 = expr1Prime(ts);
			if (!part2.equals(""))
				return new String (part1 + " " + part2);
			else
				return part1;
		} else 
			throw new Exception();
	}
	
	String expr1 (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.LEFTPAR ||
				currentType == Type.INCROP ||
				currentType == Type.NUM ||
				currentType == Type.LSIGN) {
			String part1 = expr2(ts);
			String part2 = expr1Prime(ts);
			if (!part2.equals(""))
				return new String (part1 + " " + part2);
			else
				return part1;
		} else 
			throw new Exception();
	}
	
	String expr1Prime (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.BINOP) {
			String tokenValue = currentToken.getValue();
			currentToken = ts.getNextToken();
			String part1 = expr2(ts);
			String part2 = expr1Prime(ts);
			if (!part2.equals(""))
				return new String (part1 + " " + tokenValue + " " + part2);
			else
				return new String (part1 + " " + tokenValue);
		} else if (currentType == Type.LEFTPAR ||
				currentType == Type.RIGHTPAR ||
				currentType == Type.NUM ||
				currentType == Type.LSIGN ||
				currentType == Type.EOF)
			return "";
		else 
			throw new Exception();
	}
	
	String expr2 (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.INCROP) {
			String tokenValue = currentToken.getValue();
			currentToken = ts.getNextToken();
			String result = expr2(ts);
			// prefix
			return new String (result + " " + tokenValue + "_");
		} else if (currentType == Type.LEFTPAR ||
				currentType == Type.NUM ||
				currentType == Type.LSIGN) {
			String result = expr3(ts);
			return result;
		} else
			throw new Exception();
	}
	
	String expr2Prime (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.INCROP) {
			String tokenValue = currentToken.getValue();
			currentToken = ts.getNextToken();
			String result = expr2(ts);
			return new String (result + " " + tokenValue + "_");
		} else 
			throw new Exception();
	}
	
	String expr3 (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.LEFTPAR ||
				currentType == Type.NUM ||
				currentType == Type.LSIGN) {
			String part1 = expr4(ts);
			String part2 = expr3Prime(ts);
			if (!part2.equals("")) 
				return new String (part1 + " " + part2);
			else 
				return part1;
		} else
			throw new Exception();
	}
	
	String expr3Prime (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		// postfix
		if (currentType == Type.INCROP) {
			String tokenValue = currentToken.getValue();
			currentToken = ts.getNextToken();
			String result = expr3Prime(ts);
			if (!result.equals("")) 
				return new String (result + " " + "_" + tokenValue);
			else
				return new String ("_" + tokenValue);
		} else
			return "";
	}
	
	String expr4 (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.NUM) {
			String tokenValue = currentToken.getValue();
			currentToken = ts.getNextToken();
			return tokenValue;
		} else if (currentType == Type.LSIGN) {
			String result = lvalue(ts);
			return result;
		} else if (currentType == Type.LEFTPAR) {
			String result = expr5(ts);
			return result;
		} else 
			throw new Exception();
	}
	
	String expr5 (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.LEFTPAR) {
			currentToken = ts.getNextToken();
			String result = expr(ts);
			if (currentToken.getType() == Type.RIGHTPAR) {
				currentToken = ts.getNextToken();
				return result;
			} else 
				throw new Exception();
		} else
			throw new Exception();
	}
	
	String expr6 (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.NUM ||
				currentType == Type.LSIGN ||
				currentType == Type.LEFTPAR) {
			String result = expr4 (ts);
			return result;
		} else if (currentType == Type.INCROP) {
			String result = expr2Prime(ts);
			return result;
		} else
			throw new Exception();
	}
	
	String lvalue (TokenScanner ts) throws Exception {
		Type currentType = currentToken.getType();
		if (currentType == Type.LSIGN) {
			String tokenValue = currentToken.getValue();
			currentToken = ts.getNextToken();
			String result = expr6(ts);
			return new String (result + " " + tokenValue);
		} else
			throw new Exception();
	}
}

public class Parser {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TokenScanner ts = new TokenScanner();
		TokenParser tp = new TokenParser();
			try {
				String result = tp.goal(ts);
				System.out.println(result);
				System.out.println("Expression parsed successfully");
			} catch (Exception e) {
				System.out.println("Parse error in line " + ts.getLineNumber());
			}
	}

}