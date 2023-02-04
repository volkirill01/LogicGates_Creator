package editor.nodes;

import editor.Gates_NodeEditor;
import editor.GraphNode;
import editor.GraphNodePin;
import editor.TestFieldsWindow;
import editor.utils.ImFonts;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;

public class GraphNode_Input extends GraphNode {

    public GraphNode_Input() {
        this.outputPins.add(new GraphNodePin(false, "Out (1)", "Out"));
    }

    @Override
    public void update() { }

    @Override
    public void drawNode() {
        if (ImGui.button("+")) {
            GraphNodePin newPin = new GraphNodePin(false, "Out (" + (this.outputPins.size() + 1) + ")", "Out");
            newPin.init(Gates_NodeEditor.getCurrentGraph().getNextPinId());
            this.outputPins.add(newPin);
        }
        ImGui.sameLine();
        ImGui.setCursorPosY(ImGui.getCursorPosY() - 4.0f);
        ImGui.pushFont(ImFonts.regular150);
        ImGui.text(getName());
        ImGui.popFont();
    }

    @Override
    protected void drawPin(GraphNodePin pin) {
        if (pin.getValue()) {
            ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.743f, 0.912f, 0.051f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0.843f, 9.8f, 0.11f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.FrameBgActive, 0.943f, 1.0f, 0.13f, 1.0f);
        } else {
            ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.835f, 0.146f, 0.046f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0.935f, 0.186f, 0.086f, 1.0f);
            ImGui.pushStyleColor(ImGuiCol.FrameBgActive, 1.0f, 0.260f, 0.106f, 1.0f);
        }
        ImGui.pushStyleColor(ImGuiCol.CheckMark, 0.0f, 0.0f, 0.0f, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 7.0f);
        if (ImGui.checkbox("##isActive" + pin.getId(), pin.getValue()))
            pin.setValue(!pin.getValue());
        ImGui.popStyleVar();
        ImGui.popStyleColor(4);
        ImGui.sameLine();
        super.drawPin(pin);
    }

    @Override
    public String getName() { return "Input"; }

    @Override
    public String getDescription() { return "Description"; }

    @Override
    public GraphNode copy() { return new GraphNode_Input(); }
}
