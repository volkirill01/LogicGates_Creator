package editor.node;

import editor.gates.Gate_7SegmentDisplay;
import editor.utils.ImFonts;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
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

    protected transient float inputHeight = 0.0f;
    protected transient float outputHeight = 0.0f;
    protected transient float contentWidth = 1.0f;
    protected transient float contentHeight = 0.0f;

    protected Map<String, List<Integer>> pinGroups = new HashMap<>();

    private transient Gate_7SegmentDisplay display = null;

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

        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, ImGui.getStyle().getItemSpacingX(), 0.0f);
        float spacing = 5.0f;

        ImVec2 startCursorPos = ImGui.getCursorPos();

        if (Gates_NodeEditor.showPins) {
            if (this.contentHeight > this.inputHeight)
                ImGui.setCursorPosY(startCursorPos.y + this.contentHeight / 2.0f - inputHeight / 2.0f - 4.0f);
            else if (outputHeight > inputHeight)
                ImGui.setCursorPosY(startCursorPos.y + outputHeight / 2.0f - inputHeight / 2.0f);
        }

        ImGui.beginGroup();
        for (GraphNodePin pin : this.inputPins)
            drawPin(pin);
        ImGui.endGroup();
        ImGui.setCursorPos(ImGui.getItemRectMaxX() + spacing, startCursorPos.y);

        if (Gates_NodeEditor.showPins)
            inputHeight = ImGui.getItemRectSizeY();
        else
            inputHeight = 22.0f;
//        } else
//            inputHeight = 22.0f;

        if (!hasDisplay()) {
            if (this.contentWidth > 0) {
                ImGui.beginGroup();
                drawNode();
                ImGui.endGroup();
                ImGui.setCursorPos(ImGui.getItemRectMaxX() + spacing, startCursorPos.y);
                this.contentWidth = ImGui.getItemRectSizeX();
                this.contentHeight = ImGui.getItemRectSizeY();
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
        } else {
            ImGui.beginGroup();
            drawDisplay();
            ImGui.endGroup();
            this.contentHeight = ImGui.getItemRectSizeY();
            ImGui.setCursorPos(ImGui.getItemRectMaxX() + spacing, startCursorPos.y);
        }

        if (Gates_NodeEditor.showPins) {
            if (this.contentHeight > this.outputHeight)
                ImGui.setCursorPosY(startCursorPos.y + this.contentHeight / 2.0f - this.outputHeight / 2.0f);
            else if (this.inputHeight > this.outputHeight)
                ImGui.setCursorPosY(startCursorPos.y + this.inputHeight / 2.0f - this.outputHeight / 2.0f);
        }
        ImGui.beginGroup();
        for (GraphNodePin pin : this.outputPins)
            drawPin(pin);
        ImGui.endGroup();
        outputHeight = ImGui.getItemRectSizeY();
//        } else {
//            outputHeight = 0.0f;
//        }
        ImGui.popStyleVar();
    }

    protected void drawPin(GraphNodePin pin) {
        if (Gates_NodeEditor.showPins) {
            if (pin.isInput())
                NodeEditor.beginPin(pin.getId(), NodeEditorPinKind.Input);
            else
                NodeEditor.beginPin(pin.getId(), NodeEditorPinKind.Output);

            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImGui.getStyle().getFramePaddingX() + Gates_NodeEditor.pinTouchExtraPadding, ImGui.getStyle().getFramePaddingY() + Gates_NodeEditor.pinTouchExtraPadding);
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
                        ImGui.getCursorScreenPosX() - 16.0f - Gates_NodeEditor.pinTouchExtraPadding, // X Pos
                        ImGui.getCursorScreenPosY() + 11.0f + Gates_NodeEditor.pinTouchExtraPadding, // Y Pos
                        11.0f, // Circle size
                        ImGui.getColorU32(0.1f, 0.629f, 0.873f, 0.827f), // Color
                        20); // Circle segments
            }
            ImGui.sameLine();
            ImGui.getWindowDrawList().addCircleFilled(
                    ImGui.getCursorScreenPosX() - 16.0f - Gates_NodeEditor.pinTouchExtraPadding, // X Pos
                    ImGui.getCursorScreenPosY() + 11.0f + Gates_NodeEditor.pinTouchExtraPadding, // Y Pos
                    9.0f, // Circle size
                    color, // Color
                    20); // Circle segments

//        ImGui.text("" + pin.getId());
//        if (pin.hasConnections())
//            for (int i = 0; i < pin.getConnectedPins().size(); i++)
//                ImGui.text("  Con-" + pin.getConnectedPin(i).getId());

            if (!Gates_NodeEditor.showPinTitles) {
                NodeEditor.endPin();
                return;
            }

            String groupName = getGroupNameFromContainsPin(pin);

            int pinIndexState = -1;

            List<GraphNodePin> groupPins = getGroupPins(groupName);

            if (groupName != null)
                for (int i = 0; i < groupPins.size(); i++) {
                    if (groupPins.get(i) == pin) {
                        if (groupPins.size() == 1)
                            pinIndexState = 3;
                        else if (i == 0)
                            pinIndexState = 0;
                        else if (i == groupPins.size() - 1)
                            pinIndexState = 2;
                        else
                            pinIndexState = 1;
                    }
                }

            float xPos = ImGui.getCursorScreenPosX() + 5.0f;
            ImVec2 tmp = new ImVec2();

            ImVec4 textColor = ImGui.getStyle().getColor(ImGuiCol.Text);

            if (groupName == null) {
                ImGui.calcTextSize(tmp, pin.getLabel());

                if (pin.isInput())
                    xPos = ImGui.getCursorScreenPosX() - tmp.x - 40.0f - (Gates_NodeEditor.pinTouchExtraPadding * 2.0f);

                ImGui.getWindowDrawList().addText(
                        ImFonts.regular100, // Font
                        ImGui.getFontSize(), // Font size
                        xPos + 1.0f, // X POS
                        ImGui.getCursorScreenPosY() + 3.0f + Gates_NodeEditor.pinTouchExtraPadding, // Y POS
                        ImGui.getColorU32(textColor.x, textColor.y, textColor.z, textColor.w), // Color
                        pin.getLabel());    // Text
            } else {
                //<editor-fold desc="Draw Group Lines">
                if (pin.isInput())
                    xPos = ImGui.getCursorScreenPosX() - 47.0f - (Gates_NodeEditor.pinTouchExtraPadding * 2.0f);
                else
                    xPos = ImGui.getCursorScreenPosX() + 4.0f;

                if (pinIndexState == 0) {
                    ImGui.getWindowDrawList().addLine(
                            xPos, ImGui.getCursorScreenPosY() + 1.0f,
                            xPos + 10.0f, ImGui.getCursorScreenPosY() + 1.0f,
                            ImGui.getColorU32(textColor.x, textColor.y, textColor.z, textColor.w));

                    if (!pin.isInput())
                        xPos += 10.0f;

                    ImGui.getWindowDrawList().addLine(
                            xPos, ImGui.getCursorScreenPosY() + 1.0f,
                            xPos, ImGui.getCursorScreenPosY() + 20.0f + (Gates_NodeEditor.pinTouchExtraPadding * 2.0f),
                            ImGui.getColorU32(textColor.x, textColor.y, textColor.z, textColor.w));
                } else if (pinIndexState == 2) {
                    ImGui.getWindowDrawList().addLine(
                            xPos, ImGui.getCursorScreenPosY() + 20.0f + (Gates_NodeEditor.pinTouchExtraPadding * 2.0f),
                            xPos + 10.0f, ImGui.getCursorScreenPosY() + 20.0f + (Gates_NodeEditor.pinTouchExtraPadding * 2.0f),
                            ImGui.getColorU32(textColor.x, textColor.y, textColor.z, textColor.w));

                    if (!pin.isInput())
                        xPos += 10.0f;

                    ImGui.getWindowDrawList().addLine(
                            xPos, ImGui.getCursorScreenPosY() - 2.0f,
                            xPos, ImGui.getCursorScreenPosY() + 20.0f + (Gates_NodeEditor.pinTouchExtraPadding * 2.0f),
                            ImGui.getColorU32(textColor.x, textColor.y, textColor.z, textColor.w));

                } else if (pinIndexState == 1) {
                    if (!pin.isInput())
                        xPos += 10.0f;

                    ImGui.getWindowDrawList().addLine(
                            xPos, ImGui.getCursorScreenPosY() - 2.0f,
                            xPos, ImGui.getCursorScreenPosY() + 20.0f + (Gates_NodeEditor.pinTouchExtraPadding * 2.0f),
                            ImGui.getColorU32(textColor.x, textColor.y, textColor.z, textColor.w));
                } else if (pinIndexState == 3) {
                    ImGui.getWindowDrawList().addLine(
                            xPos, ImGui.getCursorScreenPosY() + 20.0f + (Gates_NodeEditor.pinTouchExtraPadding * 2.0f),
                            xPos + 10.0f, ImGui.getCursorScreenPosY() + 20.0f + (Gates_NodeEditor.pinTouchExtraPadding * 2.0f),
                            ImGui.getColorU32(textColor.x, textColor.y, textColor.z, textColor.w));
                    ImGui.getWindowDrawList().addLine(
                            xPos, ImGui.getCursorScreenPosY() + 1.0f,
                            xPos + 10.0f, ImGui.getCursorScreenPosY() + 1.0f,
                            ImGui.getColorU32(textColor.x, textColor.y, textColor.z, textColor.w));

                    if (!pin.isInput())
                        xPos += 10.0f;

                    ImGui.getWindowDrawList().addLine(
                            xPos, ImGui.getCursorScreenPosY() + 1.0f,
                            xPos, ImGui.getCursorScreenPosY() + 20.0f + (Gates_NodeEditor.pinTouchExtraPadding * 2.0f),
                            ImGui.getColorU32(textColor.x, textColor.y, textColor.z, textColor.w));
                }
                //</editor-fold>
            }

            NodeEditor.endPin();
        }
    }

    public abstract GraphNode copy();

    public abstract void update();

    public abstract void drawNode();

    public abstract String getName();

    public int getId() { return this.nodeId; }

    public int getInputPinId(int index) { return inputPins.get(index).getId(); }

    public int getOutputPinId(int index) { return outputPins.get(index).getId(); }

    public Vector3f getNodeColor() { return this.nodeColor; }

    public ImVec2 getPosition() { return this.position; }

    public void setPosition(ImVec2 position) { this.position.set(position); }

    public void setPosition(float x, float y) { this.position.set(x, y); }

    public void addGroup(String groupName, List<GraphNodePin> pins) {
        List<Integer> pinsIds = new ArrayList<>();

        for (GraphNodePin pin : pins)
            pinsIds.add(pin.getId());

        this.pinGroups.put(groupName, pinsIds.stream().sorted().toList());
    }

    public void addGroupWithIds(String groupName, List<Integer> pinsIds) { this.pinGroups.put(groupName, pinsIds); }

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

    public Map<String, List<Integer>> getGroups() { return this.pinGroups; }

    public boolean hasDisplay() { return this.display != null; }

    public void setDisplay(Gate_7SegmentDisplay display) { this.display = display; }

    private void drawDisplay() {
        ImGui.dummy(Gates_NodeEditor.showPins ? 108.0f : 114.0f, 196.0f);

        float size = 74.0f;
        float thickness = 13.0f;
        float spacing = 2.0f;
        ImVec2 offset = new ImVec2(15.0f, -186.0f);
        if (!Gates_NodeEditor.showPins)
            offset.x += 7.0f;

        int[] segmentColors = new int[7];
        for (int i = 0; i < segmentColors.length; i++) {
            if (display.inputPins.get(i).getValue())
                segmentColors[i] = ImGui.getColorU32(0.831f, 0.163f, 0.088f, 1.0f); // On
            else
                segmentColors[i] = ImGui.getColorU32(0.070f, 0.072f, 0.076f, 1.0f); // Off
        }

        ImGui.getWindowDrawList().addConvexPolyFilled(new ImVec2[]{
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x, ImGui.getCursorScreenPosY() + thickness + offset.y - (spacing * 2.0f)),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + thickness, ImGui.getCursorScreenPosY() + offset.y - (spacing * 2.0f)),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + size - thickness, ImGui.getCursorScreenPosY() + offset.y - (spacing * 2.0f)),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + size, ImGui.getCursorScreenPosY() + thickness + offset.y - (spacing * 2.0f)),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + size - thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + offset.y - (spacing * 2.0f)),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + offset.y - (spacing * 2.0f)),
        }, 6, segmentColors[0]);
        ImGui.getWindowDrawList().addConvexPolyFilled(new ImVec2[]{
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x, ImGui.getCursorScreenPosY() + thickness + size + offset.y),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + thickness, ImGui.getCursorScreenPosY() + size + offset.y),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + size - thickness, ImGui.getCursorScreenPosY() + size + offset.y),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + size, ImGui.getCursorScreenPosY() + thickness + size + offset.y),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + size - thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + size + offset.y),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + size + offset.y),
        }, 6, segmentColors[6]);
        ImGui.getWindowDrawList().addConvexPolyFilled(new ImVec2[]{
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x, ImGui.getCursorScreenPosY() + thickness + (size * 2.0f) + offset.y + (spacing * 2.0f)),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + thickness, ImGui.getCursorScreenPosY() + (size * 2.0f) + offset.y + (spacing * 2.0f)),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + size - thickness, ImGui.getCursorScreenPosY() + (size * 2.0f) + offset.y + (spacing * 2.0f)),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + size, ImGui.getCursorScreenPosY() + thickness + (size * 2.0f) + offset.y + (spacing * 2.0f)),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + size - thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + (size * 2.0f) + offset.y + (spacing * 2.0f)),
                new ImVec2(ImGui.getCursorScreenPosX() + offset.x + thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + (size * 2.0f) + offset.y + (spacing * 2.0f)),
        }, 6, segmentColors[3]);

        ImGui.getWindowDrawList().addConvexPolyFilled(new ImVec2[]{
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x, ImGui.getCursorScreenPosY() + thickness + offset.y - spacing),
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x + thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + offset.y - spacing),
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x + thickness, ImGui.getCursorScreenPosY() + size + offset.y - spacing),
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x, ImGui.getCursorScreenPosY() + size + thickness + offset.y - spacing),
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x - thickness, ImGui.getCursorScreenPosY() + size + offset.y - spacing),
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x - thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + offset.y - spacing),
        }, 6, segmentColors[5]);
        ImGui.getWindowDrawList().addConvexPolyFilled(new ImVec2[]{
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size, ImGui.getCursorScreenPosY() + thickness + offset.y - spacing),
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size + thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + offset.y - spacing),
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size + thickness, ImGui.getCursorScreenPosY() + size + offset.y - spacing),
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size, ImGui.getCursorScreenPosY() + size + thickness + offset.y - spacing),
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size - thickness, ImGui.getCursorScreenPosY() + size + offset.y - spacing),
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size - thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + offset.y - spacing),
        }, 6, segmentColors[1]);


        ImGui.getWindowDrawList().addConvexPolyFilled(new ImVec2[]{
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x, ImGui.getCursorScreenPosY() + thickness + size + offset.y + spacing),
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x + thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + size + offset.y + spacing),
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x + thickness, ImGui.getCursorScreenPosY() + (size * 2.0f) + offset.y + spacing),
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x, ImGui.getCursorScreenPosY() + thickness + (size * 2.0f) + offset.y + spacing),
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x - thickness, ImGui.getCursorScreenPosY() + (size * 2.0f) + offset.y + spacing),
                new ImVec2(ImGui.getCursorScreenPosX() - spacing + offset.x - thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + size + offset.y + spacing),
        }, 6, segmentColors[4]);
        ImGui.getWindowDrawList().addConvexPolyFilled(new ImVec2[]{
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size, ImGui.getCursorScreenPosY() + thickness + size + offset.y + spacing),
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size + thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + size + offset.y + spacing),
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size + thickness, ImGui.getCursorScreenPosY() + (size * 2.0f) + offset.y + spacing),
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size, ImGui.getCursorScreenPosY() + thickness + (size * 2.0f) + offset.y + spacing),
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size - thickness, ImGui.getCursorScreenPosY() + (size * 2.0f) + offset.y + spacing),
                new ImVec2(ImGui.getCursorScreenPosX() + spacing + offset.x + size - thickness, ImGui.getCursorScreenPosY() + (thickness * 2.0f) + size + offset.y + spacing),
        }, 6, segmentColors[2]);
    }
}
