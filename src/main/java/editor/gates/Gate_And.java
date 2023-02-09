package editor.gates;

import editor.node.GraphNode;
import editor.node.GraphNodePin;
import org.joml.Vector3f;

public class Gate_And extends GraphNode {

    public Gate_And() {
        this.inputPins.add(new GraphNodePin(true, "A"));
        this.inputPins.add(new GraphNodePin(true, "B"));
        this.outputPins.add(new GraphNodePin(false, "Out"));
    }

    @Override
    public void update() { this.outputPins.get(0).setValue(this.inputPins.get(0).getValue() && this.inputPins.get(1).getValue()); }

    @Override
    public Vector3f getNodeColor() { return new Vector3f(8.0f, 82.0f, 170.0f); }

    @Override
    public void drawNode() { }

    @Override
    public String getName() { return "And"; }

    @Override
    public GraphNode copy() { return new Gate_And(); }
}
