package editor.nodes;

import editor.GraphNode;

public class Gate_Not extends GraphNode {

    @Override
    public void drawNode() {

    }

    @Override
    public String getName() { return "Not"; }

    @Override
    public GraphNode copy() { return new Gate_Not(); }
}
