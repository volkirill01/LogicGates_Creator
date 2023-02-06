package editor.nodes;

import editor.Gates_NodeEditor;
import editor.GraphNode;
import editor.GraphNodePin;
import editor.utils.ImFonts;
import imgui.ImGui;

public class GraphNode_Output extends GraphNode {

    public GraphNode_Output() {
        this.inputPins.add(new GraphNodePin(true, "In (1)", "In"));
    }

    @Override
    public void update() { }

    @Override
    public void drawNode() {
        ImGui.pushFont(ImFonts.regular150);
        ImGui.text(getName());
        ImGui.popFont();
        ImGui.sameLine();
        if (ImGui.button("+")) {
            GraphNodePin newPin = new GraphNodePin(true, "In (" + (this.inputPins.size() + 1) + ")", "");
            newPin.init(Gates_NodeEditor.getCurrentGraph().getNextPinId());
            this.inputPins.add(newPin);
        }
    }

    @Override
    public String getName() { return "Output"; }

    @Override
    public String getDescription() { return "Description"; }

    @Override
    public GraphNode copy() { return new GraphNode_Output(); }
}
