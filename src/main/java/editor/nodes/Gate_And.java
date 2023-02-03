package editor.nodes;

import editor.GraphNode;
import editor.GraphNodePin;

public class Gate_And extends GraphNode {

    public Gate_And() {
        this.inputPins.add(new GraphNodePin(true, "In (A)", "A"));
        this.inputPins.add(new GraphNodePin(true, "In (B)", "B"));
        this.outputPins.add(new GraphNodePin(false, "Out", "Out"));
    }

    @Override
    public void update() { this.outputPins.get(0).setValue(this.inputPins.get(0).getValue() && this.inputPins.get(1).getValue()); }

    @Override
    public void drawNode() { }

    @Override
    public String getName() { return "And"; }

    @Override
    public String getDescription() { return "Description"; }

    @Override
    public GraphNode copy() { return new Gate_And(); }
}
