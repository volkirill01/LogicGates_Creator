package editor.nodes;

import editor.GraphNode;
import editor.GraphNodePin;
import imgui.internal.ImGui;
import imgui.type.ImBoolean;

public class Gate_Toggle extends GraphNode {

    public Gate_Toggle() {
        this.outputPins.add(new GraphNodePin(false, "Out", "Out"));
    }

    @Override
    public void update() { }

    @Override
    public void drawNode() {
        if (ImGui.checkbox("##Value", new ImBoolean(this.outputPins.get(0).getValue())))
            this.outputPins.get(0).setValue(!this.outputPins.get(0).getValue());
    }

    @Override
    public String getName() { return "Toggle"; }

    @Override
    public String getDescription() { return "Description"; }

    @Override
    public GraphNode copy() { return new Gate_Toggle(); }
}
