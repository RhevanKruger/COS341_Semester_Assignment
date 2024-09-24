import java.util.ArrayList;
import java.util.List;
class Node {
    int parentId;
    int unid;
    String symbol; // Non-terminal or terminal symbol
    List<Node> children;
    boolean isLeaf;

    public Node(int unid, int p, String symbol, boolean isLeaf) {
        this.parentId = p;
        this.unid = unid;
        this.symbol = symbol;
        this.children = new ArrayList<>();
        this.isLeaf = isLeaf;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public int getUNID() {
        return unid;
    }

    public String getSymbol() {
        return symbol;
    }

    public List<Node> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder();
        if (isLeaf) {
            xml.append("<LEAF>\n");
            xml.append("\t<PARENT>").append(this.parentId).append("</PARENT>\n");
            xml.append("\t<UNID>").append(this.unid).append("</UNID>\n");
            xml.append("\t<TERMINAL>\n").append(this.symbol).append("\n\t</TERMINAL>\n");
            xml.append("</LEAF>\n");
        } else {
            xml.append("<IN>\n");
            xml.append("\t<UNID>").append(this.unid).append("</UNID>\n");
            xml.append("\t<SYMB>").append(this.symbol).append("</SYMB>\n");
            xml.append("\t<CHILDREN>\n");
            for (Node child : children) {
                xml.append("\t\t<ID>").append(child.getUNID()).append("</ID>\n");
            }
            xml.append("\t</CHILDREN>\n");
            xml.append("</IN>\n");
        }
        return xml.toString();
    }

    public void removeChild(Node childNode) {
        children.remove(childNode);
    }
    public String toString() {
        //return node representation with its children
        String temp = "";
        temp += "Node{" +
                "unid=" + unid +
                ", symbol='" + symbol + '\'' +
                ", children=[";
        for (Node child : children) {
            temp += child.getSymbol() + ", ";
        }
        temp += "]}";
        temp += "isLeaf=" + isLeaf;
        return temp;
    }
}
