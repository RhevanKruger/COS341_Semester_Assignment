import java.util.ArrayList;
import java.util.List;

class SyntaxTree {
    private Node root;
    private List<Node> innerNodes;
    private List<Node> leafNodes;

    public SyntaxTree(Node root) {
        this.root = root;
        this.innerNodes = new ArrayList<>();
        this.leafNodes = new ArrayList<>();
    }

    public void addInnerNode(Node node) {
        innerNodes.add(node);
    }

    public void addLeafNode(Node node) {
        leafNodes.add(node);
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<SYNTREE>\n");
        xml.append("<ROOT>\n");
        xml.append("\t<UNID>").append(root.getUNID()).append("</UNID>\n");
        xml.append("\t<SYMB>").append(root.getSymbol()).append("</SYMB>\n");
        xml.append("\t<CHILDREN>\n");
        for (Node child : root.getChildren()) {
            xml.append("\t\t<ID>").append(child.getUNID()).append("</ID>\n");
        }
        xml.append("\t</CHILDREN>\n");
        xml.append("</ROOT>\n");

        xml.append("<INNERNODES>\n");
        for (Node innerNode : innerNodes) {
            xml.append(innerNode.toXML());
        }
        xml.append("</INNERNODES>\n");

        xml.append("<LEAFNODES>\n");
        for (Node leafNode : leafNodes) {
            xml.append(leafNode.toXML());
        }
        xml.append("</LEAFNODES>\n");

        xml.append("</SYNTREE>");
        return xml.toString();
    }
}
