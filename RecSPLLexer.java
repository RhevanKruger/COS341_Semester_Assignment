import java.io.*;
import java.util.regex.*;

public class RecSPLLexer {

    // Token class to store the information of each token
    static class Token {
        int id;
        String tokenClass;
        String word;

        public Token(int id, String tokenClass, String word) {
            this.id = id;
            this.tokenClass = tokenClass;
            this.word = word;
        }

        // Convert the token to XML format
        public String toXML() {
            return "<TOK>\n" + "\t<ID>" + id + "</ID>\n" + "\t<CLASS>" + tokenClass + "</CLASS>\n" + "\t<WORD>" + word + "</WORD>\n" + "</TOK>";
        }
    }

    private static int tokenId = 0;

    // Regular expressions for different token classes
    private static final String VARIABLE_REGEX = "V_[a-z]([a-z]|[0-9])*";
    private static final String FUNCTION_REGEX = "F_[a-z]([a-z]|[0-9])*";
    private static final String TEXT_REGEX = "\"[A-Z][a-z]{1,7}\"";
    private static final String NUMBER_REGEX = "-?[0-9]+(\\.[0-9]+)?";
    private static final String RESERVED_KEYWORDS_REGEX = "(main|begin|end|if|then|else|halt|print|skip|input|output|num|text|void|call|add|sub|mul|div|eq|grt|and|or|not|sqrt|=|<|\\(|\\)|\\{|\\}|,|;)";

    // Error handling for lexical errors
    private static void throwLexicalError(String message) throws Exception {
        throw new Exception("Lexical Error: " + message);
    }

    // Main lexer function to tokenize input and store it as XML
    public static void lex(String inputFileName, String outputFileName) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
        
        writer.write("<TOKENSTREAM>\n");

        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            for (String token : tokens) {
                Token tok = identifyToken(token);
                writer.write(tok.toXML() + "\n");
            }
        }

        writer.write("</TOKENSTREAM>");
        writer.close();
        reader.close();
    }

    // Identify the type of token based on the regular expressions
    private static Token identifyToken(String word) throws Exception {
        tokenId++;

        if (word.matches(VARIABLE_REGEX)) {
            return new Token(tokenId, "V", word);
        } else if (word.matches(FUNCTION_REGEX)) {
            return new Token(tokenId, "F", word);
        } else if (word.matches(TEXT_REGEX)) {
            return new Token(tokenId, "T", word);
        } else if (word.matches(NUMBER_REGEX)) {
            return new Token(tokenId, "N", word);
        } else if (word.matches(RESERVED_KEYWORDS_REGEX)) {
            return new Token(tokenId, "reserved_keyword", word);
        } else {
            throwLexicalError("Unrecognized token: " + word);
            return null; // Won't be reached due to exception
        }
    }

    public static void main(String[] args) {
        try {
            lex("input.txt", "output.xml");
            System.out.println("Lexing completed. XML output generated.");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
