package editor.gates;

import editor.node.Gates_NodeEditor;
import editor.node.GraphNode;
import editor.node.GraphNodePin;
import editor.utils.ImFonts;
import imgui.ImGui;
import imgui.type.ImString;

public class GraphNode_Output extends GraphNode {

    public GraphNode_Output() { this.inputPins.add(new GraphNodePin(true, "Out (1)")); }

    @Override
    public void update() { }

    @Override
    public void drawNode() {
        ImGui.pushFont(ImFonts.regular150);
        ImGui.text(getName());
        ImGui.popFont();
        ImGui.sameLine();
        if (ImGui.button("+")) {
            GraphNodePin newPin = new GraphNodePin(true, "Out (" + (this.inputPins.size() + 1) + ")");
            newPin.init(Gates_NodeEditor.getCurrentGraph().getNextPinId());
            this.inputPins.add(newPin);
        }
    }

    @Override
    protected void drawPin(GraphNodePin pin) {
        ImGui.pushID(pin.getId());
        super.drawPin(pin);
        ImGui.sameLine();
        if (Gates_NodeEditor.showPinTitles) {
            ImGui.setNextItemWidth(70.0f);
            ImString pinLabelTmp = new ImString(pin.getLabel(), 14);
            if (ImGui.inputText("##PinLabel", pinLabelTmp))
                pin.setLabel(pinLabelTmp.get());
        }
        ImGui.popID();
    }

    @Override
    public String getName() { return "Output"; }

    @Override
    public GraphNode copy() { return new GraphNode_Output(); }
}
