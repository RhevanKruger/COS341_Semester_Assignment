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
    public String toString() {
        StringBuilder tree = new StringBuilder();
        tree.append("Root: ").append(root.getSymbol()).append(" (ID: ").append(root.getUNID()).append(")\n");
        tree.append("Inner Nodes: \n");
        for (Node innerNode : innerNodes) {
            tree.append("Node ID: ").append(innerNode.getUNID())
                .append(", Parent ID: ").append(innerNode.parentId)
                .append(", Symbol: ").append(innerNode.getSymbol())
                .append(", Children: [");
            for (Node child : innerNode.getChildren()) {
                tree.append(child.getUNID()).append(", ");
            }
            tree.append("]\n");
        }
        tree.append("Leaf Nodes: \n");
        for (Node leafNode : leafNodes) {
            tree.append("Node ID: ").append(leafNode.getUNID())
                .append(", Parent ID: ").append(leafNode.parentId)
                .append(", Symbol: ").append(leafNode.getSymbol())
                .append("\n");
        }
        return tree.toString();
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
 // Remove node and its children from the tree
 public void removeNode(Node node) {
    // Remove the node from its parent's children list
    if (node.parentId != -1) { // Assuming -1 indicates no parent (root node)
        Node parent = findNodeById(node.parentId);
        if (parent != null) {
            parent.removeChild(node);
        }
    }

    // Recursively remove all children of the node
    for (Node child : new ArrayList<>(node.getChildren())) {
        removeNode(child);
    }

    // Remove the node from the list of inner nodes or leaf nodes
    if (node.isLeaf()) {
        leafNodes.remove(node);
    } else {
        innerNodes.remove(node);
    }
}

// Helper method to find a node by its ID
private Node findNodeById(int id) {
    if (root.getUNID() == id) {
        return root;
    }
    for (Node node : innerNodes) {
        if (node.getUNID() == id) {
            return node;
        }
    }
    for (Node node : leafNodes) {
        if (node.getUNID() == id) {
            return node;
        }
    }
    return null;
}

}
