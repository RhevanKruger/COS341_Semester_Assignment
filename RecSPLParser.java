import java.util.List;
import java.util.Map;
import java.util.HashMap; 
import java.util.Arrays; 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

class RecSPLParser {
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
    private Map<String, List<List<String>>> grammar;
    private SyntaxTree syntaxTree;
    private int currentTokenIndex;
    private List<Token> tokens;

    public RecSPLParser(String xmlFilePath) {
        this.tokens = new ArrayList<>();;
        this.currentTokenIndex = 0;
        this.grammar = new HashMap<>();
        parseXMLFile(xmlFilePath);
        // Initialize grammar rules
        initializeGrammar();
    }
    private void parseXMLFile(String xmlFilePath) {
        try {
            File inputFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList tokenList = doc.getElementsByTagName("TOK");

            for (int i = 0; i < tokenList.getLength(); i++) {
                Element tokenElement = (Element) tokenList.item(i);

                int id = Integer.parseInt(tokenElement.getElementsByTagName("ID").item(0).getTextContent());
                String tokenClass = tokenElement.getElementsByTagName("CLASS").item(0).getTextContent();
                String word = tokenElement.getElementsByTagName("WORD").item(0).getTextContent();

                Token token = new Token(id, tokenClass, word);
                tokens.add(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeGrammar() {
        grammar.put("PROG", Arrays.asList(
            Arrays.asList("main", "GLOBVARS", "ALGO", "FUNCTIONS")
        ));
        
        grammar.put("GLOBVARS", Arrays.asList(
            Arrays.asList("VTYP", "VNAME", ",", "GLOBVARS"),
            Arrays.asList()// Nullable
        ));
        
        grammar.put("VTYP", Arrays.asList(
            Arrays.asList("num"),
            Arrays.asList("text")
        ));
        
        grammar.put("VNAME", Arrays.asList(
            Arrays.asList("V") // Assuming "V" is the token type for variable names from the lexer
        ));
        
        grammar.put("ALGO", Arrays.asList(
            Arrays.asList("begin", "INSTRUC", "end")
        ));
        
        grammar.put("INSTRUC", Arrays.asList(
            Arrays.asList("COMMAND", ";", "INSTRUC"),
            Arrays.asList() // Nullable
        ));
        
        grammar.put("COMMAND", Arrays.asList(
            Arrays.asList("skip"),
            Arrays.asList("halt"),
            Arrays.asList("print", "ATOMIC"),
            Arrays.asList("ASSIGN"),
            Arrays.asList("CALL"),
            Arrays.asList("BRANCH")
        ));
        
        grammar.put("ATOMIC", Arrays.asList(
            Arrays.asList("VNAME"),
            Arrays.asList("CONST")
        ));
        
        grammar.put("CONST", Arrays.asList(
            Arrays.asList("N"), // Assuming "N" is the token type for numbers
            Arrays.asList("T")  // Assuming "T" is the token type for text
        ));
        
        grammar.put("ASSIGN", Arrays.asList(
            Arrays.asList("VNAME", "<", "input"),
            Arrays.asList("VNAME", "=", "TERM")
        ));
        
        grammar.put("CALL", Arrays.asList(
            Arrays.asList("FNAME", "(", "ATOMIC", ",", "ATOMIC", ",", "ATOMIC", ")")
        ));
        
        grammar.put("BRANCH", Arrays.asList(
            Arrays.asList("if", "COND", "then", "ALGO", "else", "ALGO")
        ));
        
        grammar.put("TERM", Arrays.asList(
            Arrays.asList("ATOMIC"),
            Arrays.asList("CALL"),
            Arrays.asList("OP")
        ));
        
        grammar.put("OP", Arrays.asList(
            Arrays.asList("UNOP", "(", "ARG", ")"),
            Arrays.asList("BINOP", "(", "ARG", ",", "ARG", ")")
        ));
        
        grammar.put("ARG", Arrays.asList(
            Arrays.asList("ATOMIC"),
            Arrays.asList("OP")
        ));
        
        grammar.put("COND", Arrays.asList(
            Arrays.asList("SIMPLE"),
            Arrays.asList("COMPOSIT")
        ));
        
        grammar.put("SIMPLE", Arrays.asList(
            Arrays.asList("BINOP", "(", "ATOMIC", ",", "ATOMIC", ")")
        ));
        
        grammar.put("COMPOSIT", Arrays.asList(
            Arrays.asList("BINOP", "(", "SIMPLE", ",", "SIMPLE", ")"),
            Arrays.asList("UNOP", "(", "SIMPLE", ")")
        ));
        
        grammar.put("UNOP", Arrays.asList(
            Arrays.asList("not"),
            Arrays.asList("sqrt")
        ));
        
        grammar.put("BINOP", Arrays.asList(
            Arrays.asList("or"),
            Arrays.asList("and"),
            Arrays.asList("eq"),
            Arrays.asList("grt"),
            Arrays.asList("add"),
            Arrays.asList("sub"),
            Arrays.asList("mul"),
            Arrays.asList("div")
        ));
        
        grammar.put("FNAME", Arrays.asList(
            Arrays.asList("F") // Assuming "F" is the token type for function names from the lexer
        ));
        
        grammar.put("FUNCTIONS", Arrays.asList(
            Arrays.asList("DECL", "FUNCTIONS"),
            Arrays.asList() // Nullable
        ));
        
        grammar.put("DECL", Arrays.asList(
            Arrays.asList("HEADER", "BODY")
        ));
        
        grammar.put("HEADER", Arrays.asList(
            Arrays.asList("FTYP", "FNAME", "(", "VNAME", ",", "VNAME", ",", "VNAME", ")")
        ));
        
        grammar.put("FTYP", Arrays.asList(
            Arrays.asList("num"),
            Arrays.asList("void")
        ));
        
        grammar.put("BODY", Arrays.asList(
            Arrays.asList("PROLOG", "LOCVARS", "ALGO", "EPILOG", "SUBFUNCS", "end")
        ));
        
        grammar.put("PROLOG", Arrays.asList(
            Arrays.asList("{")
        ));
        
        grammar.put("EPILOG", Arrays.asList(
            Arrays.asList("}")
        ));
        
        grammar.put("LOCVARS", Arrays.asList(
            Arrays.asList("VTYP", "VNAME", ",", "VTYP", "VNAME", ",","VTYP", "VNAME", ",")
        ));

        grammar.put("SUBFUNCS", Arrays.asList( 
            Arrays.asList("FUNCTIONS")
        ));
    }

    public void parse() {
        Node root = new Node(1, "PROG", false); // Start symbol is "PROG"
        syntaxTree = new SyntaxTree(root); // Ensure syntaxTree is initialized
    
        while (tokens.size() > currentTokenIndex) {
            parseSymbol(root, "PROG", tokens.get(currentTokenIndex));
        }
    }
    
    private void parseSymbol(Node parentNode, String symbol, Token currentToken) {
        System.out.println("parseSymbol: " + symbol + " token " + currentToken.word);
        
        if (!grammar.containsKey(symbol)) {
            // Terminal symbol: it should match the current token
            if (reachable(symbol, currentToken)) {
                Node childNode = new Node(generateUNID(), currentToken.word, true);
                parentNode.addChild(childNode);
                syntaxTree.addLeafNode(childNode);
                currentTokenIndex++;
            } else {
                throw new RuntimeException("Syntax error: Unexpected token " + currentToken.word + " at position " + currentTokenIndex);
            }
        } else {
            // Handle nullable non-terminals
            if (!reachable(symbol, currentToken) && isNullable(symbol)) {
                System.out.println("Nullable non-terminal " + symbol + " matched as empty");
                Node emptyNode = new Node(generateUNID(), symbol + "_empty", false);
                parentNode.addChild(emptyNode);
                syntaxTree.addInnerNode(emptyNode);
                return;
            }
    
            boolean matched = false;
            for (List<String> production : grammar.get(symbol)) {
                System.out.println("currentIndex: " + currentTokenIndex + " " + production + " " + currentToken.word);
                int savedIndex = currentTokenIndex;
                List<Node> childNodes = new ArrayList<>();
                boolean productionMatches = true;
    
                for (String childSymbol : production) {
                    if (currentTokenIndex >= tokens.size()) {
                        productionMatches = false;
                        break;
                    }
    
                    Node tempNode = new Node(generateUNID(), childSymbol, false);
                    parseSymbol(tempNode, childSymbol, tokens.get(currentTokenIndex));
    
                    // If the child node has children, it means it was successfully parsed
                    if (!tempNode.getChildren().isEmpty() || reachable(childSymbol, tokens.get(currentTokenIndex))) {
                        childNodes.add(tempNode);
                    } else {
                        productionMatches = false;
                        currentTokenIndex = savedIndex;
                        break;
                    }
    
                    // Update the current token after parsing each symbol
                    if (currentTokenIndex < tokens.size()) {
                        currentToken = tokens.get(currentTokenIndex);
                    }
                }
    
                // If we matched the production, add the nodes to the syntax tree
                if (productionMatches) {
                    Node nonTerminalNode = new Node(generateUNID(), symbol, false);
                    for (Node childNode : childNodes) {
                        nonTerminalNode.addChild(childNode);
                    }
                    parentNode.addChild(nonTerminalNode);
                    syntaxTree.addInnerNode(nonTerminalNode);
                    matched = true;
                    break;
                } else {
                    currentTokenIndex = savedIndex;
                }
            }
    
            if (!matched) {
                throw new RuntimeException("Syntax error: Unexpected token " + currentToken.word + " at position " + currentTokenIndex);
            }
        }
    }
    
    
    private boolean isNullable(String symbol) {
        // Check if the non-terminal can produce an empty sequence
        for (List<String> production : grammar.get(symbol)) {
            if (production.isEmpty()) {
                return true; // Nullable if there's an empty production
            }
        }
        return false;
    }
    
    
    
    private boolean reachable(String symbol, Token token) {
        return reachableHelper(symbol, token, new HashSet<>());
    }
    
    private boolean reachableHelper(String symbol, Token token, Set<String> visitedSymbols) {
        // If symbol is a terminal, check if it matches the current token
        if (!grammar.containsKey(symbol)) {
            return grammarContainsValue(symbol);
        }
    
        // If this symbol is already being checked in the current recursion path, stop recursion
        if (visitedSymbols.contains(symbol)) {
            return false;
        }
    
        // Mark the symbol as visited
        visitedSymbols.add(symbol);
    
        for (List<String> production : grammar.get(symbol)) {
            for (String element : production) {
                // If the element matches the token directly
                if (element.equals(getTokenWord(token))) {
                    return true;
                }
    
                // Recursively check if the token is reachable from the non-terminal
                if (!element.equals(symbol) && grammar.containsKey(element) && reachableHelper(element, token, visitedSymbols)) {
                    return true;
                }
            }
        }
    
        // Remove the symbol from visitedSymbols to allow future checks in different contexts
        visitedSymbols.remove(symbol);
        return false;
    }
    
    
    private boolean grammarContainsValue(String symbol) {
        for (List<List<String>> outerList : grammar.values()) {
            for (List<String> innerList : outerList) {
                if (innerList.contains(symbol)) {
                    return true;
                }
            }
        }
        return false;
    }
    private int generateUNID() {
        return (int) (Math.random() * 1000); // Replace with a proper unique ID generation logic
    }

    public String generateSyntaxTreeXML() {
        return syntaxTree.toXML();
    }

    public String getTokenWord(Token token) {
        if(token.tokenClass.equals("V")) {
            return "VNAME";
        } else if(token.tokenClass.equals("F")) {
            return "FNAME";
        } else if(token.tokenClass.equals("N")) {
            return "CONST";
        } else if(token.tokenClass.equals("T")) {
            return "CONST";
        } else if(token.tokenClass.equals("reserved_keyword")) {
            return token.word;
        }
        else {
            return "ERROR";
        }
    }
    public void writeSyntaxTreeToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            // Get the XML representation of the syntax tree
            String xmlContent = syntaxTree.toXML();
            
            // Write the XML content to the file
            writer.write(xmlContent);
            writer.flush();
            
            System.out.println("Syntax tree successfully written to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing syntax tree to file: " + e.getMessage());
        }
    }
}