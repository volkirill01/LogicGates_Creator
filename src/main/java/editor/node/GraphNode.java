package editor.node;

import editor.TestFieldsWindow;
import editor.utils.ImFonts;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GraphNode {

    private int nodeId;
    public List<GraphNodePin> inputPins = new ArrayList<>();
    public List<GraphNodePin> outputPins = new ArrayList<>();

    private Vector3f nodeColor = new Vector3f(43.0f, 45.0f, 52.0f);

    private ImVec2 position;

    private transient float inputHeight = 0.0f;
    private transient float outputHeight = 0.0f;
    private transient float contentWidth = 1.0f;

    private Map<String, List<Integer>> pinGroups = new HashMap<>();

    public void init(final int nodeId, ImVec2 position) {
        this.nodeId = nodeId;
        this.position = position;
    }

    public int initPins(int pinId) {
        for (GraphNodePin inputPin : this.inputPins)
            inputPin.init(pinId++);

        for (GraphNodePin outputPin : this.outputPins)
            outputPin.init(pinId++);

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
            ImGui.setCursorPosY(startCursorPos.y + outputHeight / 2.0f - inputHeight / 2.0f);
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
            float lineHeight = ImGui.getFontSize();
            if (this.inputHeight > this.outputHeight)
                ImGui.setCursorPosY(startCursorPos.y + this.inputHeight / 2.0f - lineHeight / 2.0f - 4.0f);
            else
                ImGui.setCursorPosY(startCursorPos.y + this.outputHeight / 2.0f - lineHeight / 2.0f - 4.0f);

            ImGui.pushFont(ImFonts.regular150);
            ImGui.text(getName());
            ImGui.popFont();
            ImGui.setCursorPos(ImGui.getItemRectMaxX() + spacing, startCursorPos.y);
        }

        if (inputHeight > outputHeight)
            ImGui.setCursorPosY(startCursorPos.y + inputHeight / 2.0f - outputHeight / 2.0f);
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

        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImGui.getStyle().getFramePaddingX(), ImGui.getStyle().getFramePaddingY());
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

        if (ImGui.isItemClicked())
            Gates_NodeEditor.setPinSelected(pin);

        if (Gates_NodeEditor.isPinSelected(pin)) {
            ImGui.sameLine();
            ImGui.getWindowDrawList().addCircleFilled(
                    ImGui.getCursorScreenPosX() - 16.0f, // X Pos
                    ImGui.getCursorScreenPosY() + 11.0f, // Y Pos
                    11.0f, // Circle size
                    ImGui.getColorU32(0.1f, 0.629f, 0.873f, 0.827f), // Color
                    12); // Circle segments
        }
        ImGui.sameLine();
        ImGui.getWindowDrawList().addCircleFilled(
                ImGui.getCursorScreenPosX() - 16.0f, // X Pos
                ImGui.getCursorScreenPosY() + 11.0f, // Y Pos
                9.0f, // Circle size
                color, // Color
                12); // Circle segments

//        ImGui.text((pin.isInput() ? "<-" : "->") + "/" + pin.getId() + "/" + pin.getValue());
//        if (pin.hasConnections())
//            for (int i = 0; i < pin.getConnectedPins().size(); i++)
//                ImGui.text("" + pin.getConnectedPin(i).getId() + " " + pin.getConnectedPin(i).getValue());

        String groupName = getGroupNameFromContainsPin(pin);

        int pinIndexState = -1;

        List<GraphNodePin> groupPins = getGroupPins(groupName);

        if (groupName != null)
            for (int i = 0; i < groupPins.size(); i++) {
                if (groupPins.get(i) == pin) {
                    if (i == 0)
                        pinIndexState = 0;
                    else if (i == groupPins.size() - 1)
                        pinIndexState = 2;
                    else
                        pinIndexState = 1;
                }
            }

        float xPos = ImGui.getCursorScreenPosX() + 5.0f;
        ImVec2 tmp = new ImVec2();

        if (groupName == null) {
            ImGui.calcTextSize(tmp, pin.getLabel());

            if (pin.isInput())
                xPos = ImGui.getCursorScreenPosX() - tmp.x - 35.0f;

            ImGui.getWindowDrawList().addText(
                    ImFonts.regular100, // Font
                    ImGui.getFontSize(), // Font size
                    xPos, // X POS
                    ImGui.getCursorScreenPosY() + 3.0f, // Y POS
                    ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f), // Color
                    pin.getLabel());    // Text
        } else {
            if (pinIndexState == 0 || pinIndexState == 2)
                ImGui.calcTextSize(tmp, "--");
            else
                ImGui.calcTextSize(tmp, "|");

            if (pin.isInput())
                xPos = ImGui.getCursorScreenPosX() - tmp.x - 35.0f;

            if (pinIndexState == 0) {
                ImGui.getWindowDrawList().addText(
                        ImFonts.regular100, // Font
                        ImGui.getFontSize(), // Font size
                        xPos, // X POS
                        ImGui.getCursorScreenPosY() - 4.0f, // Y POS
                        ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f), // Color
                        "--");    // Text
            } else if (pinIndexState == 2) {
                ImGui.getWindowDrawList().addText(
                        ImFonts.regular100, // Font
                        ImGui.getFontSize(), // Font size
                        xPos, // X POS
                        ImGui.getCursorScreenPosY() + 11.0f, // Y POS
                        ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f), // Color
                        "--");    // Text
            } else if (pinIndexState == 1) {
                if (pin.isInput())
                    xPos -= 5.0f;
                else
                    xPos += 5.0f;

                ImGui.getWindowDrawList().addText(
                        ImFonts.regular100, // Font
                        ImGui.getFontSize(), // Font size
                        xPos, // X POS
                        ImGui.getCursorScreenPosY() + 3.0f, // Y POS
                        ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f), // Color
                        "|");    // Text
            }
        }

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

    public Vector3f getNodeColor() { return this.nodeColor; }

    public void drawDescription() { ImGui.text(getDescription()); }

    public ImVec2 getPosition() { return this.position; }

    public void setPosition(ImVec2 position) { this.position.set(position); }

    public void addGroup(String groupName, List<GraphNodePin> pins) {
        List<Integer> pinsIds = new ArrayList<>();

        for (GraphNodePin pin : pins)
            pinsIds.add(pin.getId());

        this.pinGroups.put(groupName, pinsIds);
    }

    private List<String> getGroupNames() { return this.pinGroups.keySet().stream().toList(); }

    private String getGroupNameFromContainsPin(GraphNodePin pin) {
        for (String groupName : this.pinGroups.keySet())
            if (this.pinGroups.get(groupName).contains(pin.getId()))
                return groupName;

        return null;
    }

    public List<GraphNodePin> getGroupPins(String groupName) {
        List<GraphNodePin> pins = new ArrayList<>();

        if (this.pinGroups != null)
            if (this.pinGroups.get(groupName) != null)
                for (int pinId : this.pinGroups.get(groupName)) {
                    for (GraphNodePin pin : this.inputPins)
                        if (pin.getId() == pinId)
                            pins.add(pin);

                    for (GraphNodePin pin : this.outputPins)
                        if (pin.getId() == pinId)
                            pins.add(pin);
                }

        return pins;
    }
}
