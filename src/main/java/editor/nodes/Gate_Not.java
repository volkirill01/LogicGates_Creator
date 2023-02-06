package editor.nodes;

import editor.GraphNode;
import editor.GraphNodePin;
import editor.TestFieldsWindow;
import org.joml.Vector3f;

public class Gate_Not extends GraphNode {

    public Gate_Not() {
        this.inputPins.add(new GraphNodePin(true, "In", "In"));
        this.outputPins.add(new GraphNodePin(false, "Out", "Out"));
    }

    @Override
    public void update() {
        this.outputPins.get(0).setValue(!this.inputPins.get(0).getValue());
    }

    @Override
    public Vector3f getNodeColor() { return new Vector3f(202.0f, 16.0f, 6.0f); }

    @Override
    public void drawNode() { }

    @Override
    public String getName() { return "Not"; }

    @Override
    public String getDescription() { return "Description"; }

    @Override
    public GraphNode copy() { return new Gate_Not(); }
}
