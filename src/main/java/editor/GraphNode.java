package editor;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;

import java.util.ArrayList;
import java.util.List;

public abstract class GraphNode {

    private int nodeId;
    public List<GraphNodePin> inputPins = new ArrayList<>();
    public List<GraphNodePin> outputPins = new ArrayList<>();

    private ImVec2 position;

    private float inputHeight = 0.0f;
    private float outputHeight = 0.0f;
    private float contentWidth = 1.0f;

    public void init(final int nodeId, ImVec2 position) {
        this.nodeId = nodeId;
        this.position = position;
    }

    public int initPins(int pinId) {
        for (GraphNodePin pin : this.inputPins)
            pin.init(pinId++);

        for (GraphNodePin pin : this.outputPins)
            pin.init(pinId++);

        return pinId;
    }

    public void updatePins() {
        for (GraphNodePin pin : this.inputPins)
            pin.update();
    }

    public void imgui() {
//        ImGui.text("Node Id " + this.nodeId);

        float spacing = 5.0f;

        ImVec2 startCursorPos = ImGui.getCursorPos();

        if (outputHeight > inputHeight)
            ImGui.setCursorPosY(startCursorPos.y + outputHeight / 4.0f + 1.0f);
        ImGui.beginGroup();
        for (GraphNodePin pin : this.inputPins)
            drawPin(pin);
        ImGui.endGroup();
        inputHeight = ImGui.getItemRectSizeY();

        ImGui.setCursorPos(ImGui.getItemRectMaxX() + spacing, startCursorPos.y);

        if (this.contentWidth > 0) {
            ImGui.beginGroup();
            drawNode();
            ImGui.endGroup();
            ImGui.setCursorPos(ImGui.getItemRectMaxX() + spacing, startCursorPos.y);
            this.contentWidth = ImGui.getItemRectSizeX();
        } else {
            ImGui.text(getName());
            ImGui.setCursorPos(ImGui.getItemRectMaxX() + spacing, startCursorPos.y);
        }

        if (inputHeight > outputHeight)
            ImGui.setCursorPosY(startCursorPos.y + inputHeight / 4.0f + 1.0f);
        ImGui.beginGroup();
        for (GraphNodePin pin : this.outputPins)
            drawPin(pin);
        ImGui.endGroup();
        outputHeight = ImGui.getItemRectSizeY();
    }

    protected void drawPin(GraphNodePin pin) {
        if (pin.isInput())
            NodeEditor.beginPin(pin.getId(), NodeEditorPinKind.Input);
        else
            NodeEditor.beginPin(pin.getId(), NodeEditorPinKind.Output);

        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImGui.getStyle().getFramePaddingX() + 1.0f, ImGui.getStyle().getFramePaddingX() - 2.0f);
        ImGui.pushStyleColor(ImGuiCol.Button, 0.0f, 0.0f, 0.0f, 0.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.0f, 0.0f, 0.0f, 0.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.0f, 0.0f, 0.0f, 0.0f);
        ImGui.button(" ");
        int color = pin.getValue() ? ImGui.getColorU32(0.936f, 0.401f, 0.069f, 1.0f) : ImGui.getColorU32(0.080f, 0.083f, 0.103f, 1.0f);
        if (ImGui.isItemHovered())
            color = pin.getValue() ? ImGui.getColorU32(0.946f, 0.546f, 0.190f, 1.0f) : ImGui.getColorU32(0.107f, 0.109f, 0.118f, 1.0f);
        ImGui.setItemAllowOverlap();
        ImGui.popStyleColor(3);
        ImGui.popStyleVar();

        ImGui.sameLine();
        ImGui.getWindowDrawList().addCircleFilled(
                ImGui.getCursorScreenPos().x - 17.0f, // X Pos
                ImGui.getCursorScreenPos().y + 9.0f, // Y Pos
                9.0f, // Circle size
                color, // Color
                12); // Circle segments

//        ImGui.text((pin.isInput() ? "<-" : "->") + "/" + pin.getId() + "/" + pin.getValue());
//        for (int connectedPinId : pin.getConnectedPinsIds())
//            ImGui.text("" + connectedPinId);

        NodeEditor.endPin();
    }

    public abstract GraphNode copy();

    public abstract void update();

    public abstract void drawNode();

    public abstract String getName();

    public abstract String getDescription();

    public int getId() { return this.nodeId; }

    public int getInputPinId(int index) { return inputPins.get(index).getId(); }

    public int getOutputPinId(int index) { return outputPins.get(index).getId(); }

    public void drawTooltip() { ImGui.text(getDescription()); }

    public ImVec2 getPosition() { return this.position; }

    public void setPosition(ImVec2 position) { this.position.set(position); }
}
