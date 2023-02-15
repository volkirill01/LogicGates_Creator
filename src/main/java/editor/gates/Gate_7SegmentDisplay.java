package editor.gates;

import editor.node.GraphNode;
import editor.node.GraphNodePin;

public class Gate_7SegmentDisplay extends GraphNode {

    public Gate_7SegmentDisplay() {
        this.inputPins.add(new GraphNodePin(true, "Up"));
        this.inputPins.add(new GraphNodePin(true, "Right-Up"));
        this.inputPins.add(new GraphNodePin(true, "Right-Down"));
        this.inputPins.add(new GraphNodePin(true, "Down"));
        this.inputPins.add(new GraphNodePin(true, "Left-Down"));
        this.inputPins.add(new GraphNodePin(true, "Left-Up"));
        this.inputPins.add(new GraphNodePin(true, "Middle"));

        this.setDisplay(this);
    }

    @Override
    public void update() { }

//    @Override
//    public void imgui() {
//        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, ImGui.getStyle().getItemSpacingX(), 0.0f);
//        float spacing = 5.0f;
//
//        ImVec2 startCursorPos = ImGui.getCursorPos();
//
//        if (super.outputHeight > super.inputHeight)
//            ImGui.setCursorPosY(startCursorPos.y + outputHeight / 2.0f - inputHeight / 2.0f);
//        ImGui.beginGroup();
//        for (GraphNodePin pin : this.inputPins)
//            drawPin(pin);
//        ImGui.endGroup();
//        inputHeight = ImGui.getItemRectSizeY();
//
//        ImGui.setCursorPos(ImGui.getItemRectMaxX() + spacing, startCursorPos.y);
//
//        if (super.contentWidth > 0) {
//            ImGui.beginGroup();
//            drawNode();
//            ImGui.endGroup();
//            ImGui.setCursorPos(ImGui.getItemRectMaxX() + spacing, startCursorPos.y);
//            this.contentWidth = ImGui.getItemRectSizeX();
//        } else {
//            float lineHeight = ImGui.getFontSize();
//            if (this.inputHeight > this.outputHeight)
//                ImGui.setCursorPosY(startCursorPos.y + this.inputHeight / 2.0f - lineHeight / 2.0f - 4.0f);
//            else
//                ImGui.setCursorPosY(startCursorPos.y + this.outputHeight / 2.0f - lineHeight / 2.0f - 4.0f);
//
//            ImGui.pushFont(ImFonts.regular150);
//            ImGui.text(getName());
//            ImGui.popFont();
//            ImGui.setCursorPos(ImGui.getItemRectMaxX() + spacing, startCursorPos.y);
//        }
//
//        if (inputHeight > outputHeight)
//            ImGui.setCursorPosY(startCursorPos.y + inputHeight / 2.0f - outputHeight / 2.0f);
//        ImGui.beginGroup();
//        for (GraphNodePin pin : this.outputPins)
//            drawPin(pin);
//        ImGui.endGroup();
//        outputHeight = ImGui.getItemRectSizeY();
//        ImGui.popStyleVar();
//    }

    @Override
    public void drawNode() { }

    @Override
    public String getName() { return "7-Segment Display"; }

    @Override
    public Gate_7SegmentDisplay copy() { return new Gate_7SegmentDisplay(); }
}
