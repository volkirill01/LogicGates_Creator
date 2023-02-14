package editor.gates;

import editor.node.Gates_NodeEditor;
import editor.node.GraphNode;
import editor.node.GraphNodePin;
import editor.utils.ImFonts;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImString;

public class GraphNode_Input extends GraphNode {

    public GraphNode_Input() { this.outputPins.add(new GraphNodePin(false, "In(1)")); }

    @Override
    public void update() { }

    @Override
    public void drawNode() {
        ImGui.pushFont(ImFonts.regular150);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 3.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImGui.getStyle().getFramePaddingX() - 3.0f, ImGui.getStyle().getFramePaddingY() - 4.0f);
        if (ImGui.button("+")) {
            GraphNodePin newPin = new GraphNodePin(false, "In(" + (this.outputPins.size() + 1) + ")");
            newPin.init(Gates_NodeEditor.getCurrentGraph().getNextPinId());
            this.outputPins.add(newPin);
        }
        ImGui.popStyleVar();
        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 2.0f);
        ImGui.text(getName());
        ImGui.popFont();
    }

    @Override
    protected void drawPin(GraphNodePin pin) {
        ImGui.pushID(pin.getId());
        ImGui.setCursorPosY(ImGui.getCursorPosY() + Gates_NodeEditor.pinTouchExtraPadding);
        if (Gates_NodeEditor.showPinTitles) {
            ImGui.setNextItemWidth(70.0f);
            ImString pinLabelTmp = new ImString(pin.getLabel(), 14);
            if (ImGui.inputText("##PinLabel", pinLabelTmp))
                pin.setLabel(pinLabelTmp.get());
            ImGui.sameLine();
        }

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
        ImGui.setCursorPosY(ImGui.getCursorPosY() - Gates_NodeEditor.pinTouchExtraPadding);
        super.drawPin(pin);
        ImGui.popID();
    }

    @Override
    public String getName() { return "Input"; }

    @Override
    public GraphNode copy() { return new GraphNode_Input(); }
}
