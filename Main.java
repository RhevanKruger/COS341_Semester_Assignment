public class Main {
    public static void main(String[] args) {
        try {
            RecSPLLexer.lex("input.txt", "output.xml");
            System.out.println("Lexing completed. XML output generated.");
            Thread.sleep(1000); // Delay for 1 second

            RecSPLParser parser = new RecSPLParser("output.xml");
            Thread.sleep(1000); // Delay for 1 second

            parser.parse();
            Thread.sleep(1000); // Delay for 1 second

            parser.writeSyntaxTreeToFile("syntaxtree.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
