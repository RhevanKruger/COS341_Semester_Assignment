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
import java.sql.Array;
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
    private Map<String, Set<String>> firstSets;

    public RecSPLParser(String xmlFilePath) {
        this.tokens = new ArrayList<>();;
        this.currentTokenIndex = 0;
        this.grammar = new HashMap<>();
        parseXMLFile(xmlFilePath);
        // Initialize grammar rules
        initializeGrammar();
        // Compute first sets(we will use this to prune the parse tree)
        firstSets = computeFirstSets(grammar);
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
    private Map<String, Set<String>> computeFirstSets(Map<String, List<List<String>>> grammar) {
        Map<String, Set<String>> firstSets = new HashMap<>();
    
        for (String nonTerminal : grammar.keySet()) {
            firstSets.put(nonTerminal, new HashSet<>());
        }
    
        boolean changed;
        do {
            changed = false;
            for (String nonTerminal : grammar.keySet()) {
                Set<String> firstSet = firstSets.get(nonTerminal);
                for (List<String> production : grammar.get(nonTerminal)) {
                    for (String symbol : production) {
                        if (!grammar.containsKey(symbol)) { // Terminal
                            if (firstSet.add(symbol)) {
                                changed = true;
                            }
                            break;
                        } else { // Non-terminal
                            Set<String> symbolFirstSet = firstSets.get(symbol);
                            int prevSize = firstSet.size();
                            firstSet.addAll(symbolFirstSet);
                            if (firstSet.size() > prevSize) {
                                changed = true;
                            }
                            if (!symbolFirstSet.contains("")) {
                                break;
                            }
                        }
                    }
                }
            }
        } while (changed);
    
        return firstSets;
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
            Arrays.asList("BRANCH"),
            Arrays.asList("return", "ATOMIC")
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
        parseSymbol(root, "PROG", tokens.get(currentTokenIndex));
    }
    private boolean parseSymbol(Node parentNode, String symbol, Token currentToken) {
        System.out.println("parseSymbol: " + symbol + " token " + currentToken.word + " currentTokenIndex: " + currentTokenIndex);
    
        if (!grammar.containsKey(symbol)) {
            if (parentNode.getSymbol().equals(",") && symbol.equals(",")) {
                if (currentToken.word.equals(",")) {
                    Node commaNode = new Node(generateUNID(), ",", true);
                    parentNode.addChild(commaNode);
                    syntaxTree.addLeafNode(commaNode);
                    currentTokenIndex++;
                    return true;
                } else {
                    return false;
                }
            }
            else if (parentNode.getSymbol().equals(";") && symbol.equals(";")) {
                if (currentToken.word.equals(";")) {
                    Node commaNode = new Node(generateUNID(), ";", true);
                    parentNode.addChild(commaNode);
                    syntaxTree.addLeafNode(commaNode);
                    currentTokenIndex++;
                    return true;
                } else {
                    return false;
                }
            }
            if (reachable(symbol, currentToken)) {
                System.out.println("Reached terminal symbol: " + symbol + " token " + currentToken.word + " currentTokenIndex: " + currentTokenIndex);
                Node childNode = new Node(generateUNID(), currentToken.word, true);
                parentNode.addChild(childNode);
                syntaxTree.addLeafNode(childNode);
                currentTokenIndex++;
                return true;
            } else {
                return false;
            }
        } else {
            boolean matched = false;
            for (List<String> production : grammar.get(symbol)) {
                List<Node> childNodes = new ArrayList<>();
                ArrayList<Boolean> productionMatches =new ArrayList<Boolean>();
                            
                //handle terminal rules will one element in their list
                System.out.println("PRODUCATION: " + production+ " TOKEN: " + currentToken.word + " currentTokenIndex: " + currentTokenIndex);
                if(production.size() == 1 && !grammar.containsKey(production.get(0))) {
                    Node tempNode = new Node(generateUNID(), production.get(0), false);
                    if (parseSymbol(tempNode, production.get(0), currentToken)) {
                        parentNode.addChild(tempNode);
                        matched = true;
                        break;
                    } else {
                        continue;//check other rules
                    }
                }
                //handle non terminal symbols with keyword 
                if(!grammar.containsKey(production.get(0)) && !production.get(0).equals(currentToken.word)) {
                    continue;//check other rules
                }
                //prune based on first sets
                if (grammar.containsKey(production.get(0))&&!firstSets.get(production.get(0)).contains(getTokenWord(currentToken))) {
                    continue;//check other rules
                }

                //handle non terminal symbols with multiple children
                for (String childSymbol : production) {
                    System.out.println("CHILD SYMBOL: " + childSymbol);
                    if (currentTokenIndex >= tokens.size()) {
                        productionMatches.add(false);
                        break;
                    }
                    if(childSymbol.equals(symbol) ) {
                        //handle nullable symbols
                        productionMatches.add(true);
                        if(!allProductionMatchesTrue(productionMatches))
                            break;//check other rules
                    }
                    Node tempNode = new Node(generateUNID(), childSymbol, false);
                    boolean temp = parseSymbol(tempNode, childSymbol, tokens.get(currentTokenIndex));
                    if (temp ) {
                        childNodes.add(tempNode);
                        productionMatches.add(true);
                    } else {
                        productionMatches.add(false);
                        //continue checking other child symbols
                    }
                    if (currentTokenIndex < tokens.size()) {
                        currentToken = tokens.get(currentTokenIndex);
                    }
                }
    
                if (productionMatches.contains(true)) {
                    System.out.println("Matched Non terminal production: " + production + " token " + currentToken.word + " currentTokenIndex: " + currentTokenIndex);
                    Node nonTerminalNode = new Node(generateUNID(), symbol, false);
                    for (Node childNode : childNodes) {
                        nonTerminalNode.addChild(childNode);
                    }
                    parentNode.addChild(nonTerminalNode);
                    syntaxTree.addInnerNode(nonTerminalNode);
                    matched = true;
                    break;
                } else {
                    //print tokenindex and savedtokenindex
                    //System.out.println("TokenIndex: " + currentTokenIndex + " SavedTokenIndex: " + savedTokenIndex);
                    break;
                }
            }
    
            if (!matched) {
                throw new RuntimeException("Syntax error: Unexpected token " + currentToken.word + " at position " + currentTokenIndex);
            }
        }
        return true;
    }
    
    private boolean allProductionMatchesTrue(ArrayList<Boolean> productionMatches) {
        for (Boolean match : productionMatches) {
            if (!match) {
                return false;
            }
        }
        return true;
    }
    private boolean isNullable(String symbol) {
        return grammar.get(symbol)==null;
    }
    
    private boolean reachable(String symbol, Token token) {
        if (!grammar.containsKey(symbol)) {
            // Check if the token is a direct match with the terminal symbol
            if (symbol.equals(getTokenWord(token))) {
                return true; // Found a direct match with the token type
            }
            return false;
        }
    
        for (List<String> production : grammar.get(symbol)) {
            for (String element : production) {
                if (element.equals(getTokenWord(token))) {
                    return true; // Found a direct match with the token type
                }
    
                // Recursively check if the token is reachable from the non-terminal
                if (grammar.containsKey(element) && reachable(element, token)) {
                    return true;
                }
            }
        }
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
            return "V";
        } else if(token.tokenClass.equals("F")) {
            return "F";
        } else if(token.tokenClass.equals("N")) {
            return "N";
        } else if(token.tokenClass.equals("T")) {
            return "T";
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