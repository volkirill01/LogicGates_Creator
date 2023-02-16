package editor.gates;

import editor.node.Gates_NodeEditor;
import editor.node.GraphNode;
import editor.node.GraphNodePin;
import editor.utils.ImFonts;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImString;

public class GraphNode_Output extends GraphNode {

    public GraphNode_Output() { this.inputPins.add(new GraphNodePin(true, "Out(1)")); }

    @Override
    public void update() { }

    @Override
    public void drawNode() {
        ImGui.pushFont(ImFonts.regular150);
        ImGui.setCursorPos(ImGui.getCursorPosX() + 3.0f, ImGui.getCursorPosY() + 2.0f);
        ImGui.text(getName());
        ImGui.sameLine();
        ImGui.setCursorPos(ImGui.getCursorPosX() + 2.0f, ImGui.getCursorPosY() + 1.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImGui.getStyle().getFramePaddingX() - 3.0f, ImGui.getStyle().getFramePaddingY() - 4.0f);
        if (ImGui.button("+")) {
            GraphNodePin newPin = new GraphNodePin(true, "Out(" + (this.inputPins.size() + 1) + ")");
            newPin.init(Gates_NodeEditor.getCurrentGraph().getNextPinId());
            this.inputPins.add(newPin);
        }
        ImGui.popStyleVar();
        ImGui.popFont();
    }

    @Override
    protected void drawPin(GraphNodePin pin) {
        if (Gates_NodeEditor.showPins) {
            ImGui.pushID(pin.getId());
            super.drawPin(pin);
            ImGui.sameLine();
            ImGui.setCursorPosY(ImGui.getCursorPosY() + Gates_NodeEditor.pinTouchExtraPadding);
            ImGui.setNextItemWidth(70.0f);
            ImString pinLabelTmp = new ImString(pin.getLabel(), 20);
            if (ImGui.inputText("##PinLabel", pinLabelTmp))
                pin.setLabel(pinLabelTmp.get());
            ImGui.setCursorPosY(ImGui.getCursorPosY() - Gates_NodeEditor.pinTouchExtraPadding);
            ImGui.popID();
        }
    }

    @Override
    public String getName() { return "Output"; }

    @Override
    public GraphNode copy() { return new GraphNode_Output(); }
}
