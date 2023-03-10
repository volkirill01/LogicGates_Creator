package editor.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import editor.TestFieldsWindow;
import editor.graph.Graph;
import editor.graph.GraphDeserializer;
import editor.gates.*;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.flag.NodeEditorStyleColor;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.flag.*;
import imgui.type.ImInt;
import imgui.type.ImLong;
import imgui.type.ImString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Gates_NodeEditor {

    private static final NodeEditorContext CONTEXT;
    private static Graph currentGraph = new Graph("projects/sample/graphs/sample.graph", "Demo");

    public static float pinTouchExtraPadding = 3.0f;

    private List<Graph> gates = new ArrayList<>();
    private boolean isStart;
    private boolean isUpdate;

    private String newGateName = "";

    public static boolean showPinTitles = true;
    public static boolean showPins = true;
    public static boolean showConnections = true;

    private static List<GraphNodePin> selectedPins = new ArrayList<>();

    private ImInt groupNumbersDisplayFormat = new ImInt(0);
    private final int groupNumbersDisplay_Decimal = 0;
    private final int groupNumbersDisplay_Binary = 1;
    private final int groupNumbersDisplay_DecimalBinary = 2;
    private final int groupNumbersDisplay_BinaryDecimal = 3;

    private final String[] numbersDisplayFormatsList = new String[]{
            "Decimal",
            "Binary",
            "Decimal | Binary",
            "Binary | Decimal"
    };

    private Map<Integer, Integer> links = new HashMap<>();

    public static boolean showGroupNameDialog = false;
    private boolean isInputNodeSelected = false;
    private boolean isNumberGroup = false;
    private boolean setFocusOnInput = true;
    private String groupName = "";

    private List<File> oldGates = new ArrayList<>();
    private List<Boolean> isGatesActiveList = new ArrayList<>();

    public static boolean showGatesGroupNameDialog = false;
    private String gateGroupName = "";
    private Map<String, List<Graph>> gateGroups = new HashMap<>();

    static {
        NodeEditorConfig config = new NodeEditorConfig();
        config.setSettingsFile("graph.ini");
        CONTEXT = new NodeEditorContext(config);
    }

    public Gates_NodeEditor() {
        this.isStart = true;
        currentGraph.createInputAndOutput();
    }

    public static Graph getCurrentGraph() { return currentGraph; }

    public static void setCurrentGraph(Graph graph) { currentGraph = graph; }

    public void imgui() {
        if (ImGui.begin("Editor")) {
            loadGates();

            drawMenuBar();

            NodeEditor.setCurrentEditor(CONTEXT);

            NodeEditor.begin("Node Editor");
            NodeEditor.pushStyleVar(NodeEditorStyleVar.NodePadding, 4.0f, 3.0f, 4.0f, 4.0f);
            NodeEditor.pushStyleVar(NodeEditorStyleVar.NodeRounding, 5.0f);
            NodeEditor.pushStyleVar(NodeEditorStyleVar.LinkStrength, 400.0f);
            NodeEditor.pushStyleColor(NodeEditorStyleColor.PinRect, 0.0f, 0.0f, 0.0f, 0.0f);
            NodeEditor.pushStyleColor(NodeEditorStyleColor.NodeBg, 0.206f, 0.214f, 0.225f, 1.0f);
            NodeEditor.pushStyleColor(NodeEditorStyleColor.HovNodeBorder, 1.0f, 1.0f, 1.0f, 0.5f);
            NodeEditor.pushStyleColor(NodeEditorStyleColor.SelNodeBorder, 0.0f, 0.794f, 1.0f, 1.0f);
            NodeEditor.pushStyleColor(NodeEditorStyleColor.NodeBorder, 0.343f, 0.343f, 0.343f, 1.0f);
            NodeEditor.pushStyleColor(NodeEditorStyleColor.Bg, 0.104f, 0.099f, 0.132f, 1.0f);
            NodeEditor.pushStyleColor(NodeEditorStyleColor.Grid, 1.0f, 1.0f, 1.0f, 0.039f);

            if (isStart) {
                isStart = false;
                currentGraph = currentGraph.load(currentGraph.getFilepath());
            }

            long[] selectedNodes = new long[NodeEditor.getSelectedObjectCount()];
            NodeEditor.getSelectedNodes(selectedNodes, NodeEditor.getSelectedObjectCount());
            long[] selectedLinks = new long[NodeEditor.getSelectedObjectCount()];
            NodeEditor.getSelectedLinks(selectedLinks, NodeEditor.getSelectedObjectCount());

            if (ImGui.isKeyPressed(GLFW_KEY_DELETE)) {
                for (long nodeId : selectedNodes) {
                    if (nodeId != 0)
                        if (nodeId != 1 && nodeId != 2)
                            currentGraph.deleteNodeById((int) nodeId);
                }

                for (long linkId : selectedLinks) {
                    if (linkId >= 5000) {
                        GraphNodePin inputPin = currentGraph.findInputPin(links.get((int) linkId));
                        inputPin.setValue(false);
                        if (inputPin.hasConnections()) {
                            inputPin.getConnectedPin(0).removeConnectedPin(inputPin);
//                            inputPin.getConnectedPin(0).getConnectedPinsIds().remove(inputPin.getId());
                            inputPin.removeConnectedPin(inputPin.getConnectedPin(0));
//                            inputPin.getConnectedPinsIds().remove(inputPin.getConnectedPin(0).getId());
                        }
                    }
                }

                NodeEditor.clearSelection();
            }

            for (GraphNode node : currentGraph.nodes.values()) {
                NodeEditor.pushStyleColor(NodeEditorStyleColor.NodeBg, node.getNodeColor().x / 255.0f, node.getNodeColor().y / 255.0f, node.getNodeColor().z / 255.0f, 1.0f);
                NodeEditor.beginNode(node.getId());
                ImGui.pushID(node.getId());

                node.updatePins();
                node.update();

                node.imgui();

                ImGui.popID();

                NodeEditor.endNode();
                NodeEditor.popStyleColor(1);

                if (showPinTitles && showPins) { // TODO MOVE THIS IN GraphNode CLASS
                    //<editor-fold desc="Draw Pin Group Names">
                    ImVec2 nodePosition = new ImVec2(NodeEditor.getNodePositionX(node.getId()), NodeEditor.getNodePositionY(node.getId()));

                    Map<String, List<Integer>> groups = node.getGroups();

                    for (String groupName : groups.keySet()) {
                        ImVec2 tmp = new ImVec2();
                        ImGui.calcTextSize(tmp, groupName);

                        if (groupName.startsWith("##Number_"))
                            drawGroup(node.inputPins, node.getGroupPins(groupName), groupName, nodePosition, new ImVec2(20.0f, 0.0f));
                        else
                            drawGroup(node.inputPins, node.getGroupPins(groupName), groupName, nodePosition, new ImVec2(tmp.x + 20.0f, 0.0f));

                        drawGroup(node.outputPins, node.getGroupPins(groupName), groupName, nodePosition, new ImVec2(-NodeEditor.getNodeSizeX(node.getId()) - 20.0f, 0.0f));
                    }
                    //</editor-fold>
                }
            }

            if (NodeEditor.beginCreate()) {
                final ImLong a = new ImLong();
                final ImLong b = new ImLong();
                if (NodeEditor.queryNewLink(a, b)) {
                    GraphNode sourceNode = currentGraph.findByOutput(a.get());
                    GraphNodePin sourcePin = currentGraph.findOutputPin(a.get());
                    GraphNode targetNode = currentGraph.findByInput(b.get());
                    GraphNodePin targetPin = currentGraph.findInputPin(b.get());
                    if (ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
                        if (sourceNode != null && sourcePin != null && targetNode != null && targetPin != null) {
                            if (sourceNode != targetNode) {
                                if (targetPin.hasConnections())
                                    for (GraphNodePin connectedPin : targetPin.getConnectedPins())
                                        connectedPin.removeConnectedPin(targetPin);

                                targetPin.setConnectedPin(sourcePin);
                                sourcePin.addConnectedPin(targetPin);
                            }
                        }
                    }
                }
            }
            NodeEditor.endCreate();

            if (showConnections) {
                links.clear();
                int uniqueLinkId = 5000;
                for (GraphNode node : currentGraph.nodes.values()) {
                    for (GraphNodePin inputPin : node.inputPins)
                        if (inputPin.hasConnections())
                            for (GraphNodePin connectedPin : inputPin.getConnectedPins()) {
                                GraphNode tmpNode = currentGraph.findByOutput(connectedPin.getId());
                                if (tmpNode != null && currentGraph.nodes.containsKey(tmpNode.getId())) {
                                    NodeEditor.link(uniqueLinkId++, connectedPin.getId(), inputPin.getId());
                                    links.put(uniqueLinkId - 1, inputPin.getId());
                                }
                            }
                }
            }

            NodeEditor.suspend();

            final long nodeWithContextMenu = NodeEditor.getNodeWithContextMenu();
            if (nodeWithContextMenu != -1) {
                ImGui.openPopup("node_context");
                ImGui.getStateStorage().setInt(ImGui.getID("delete_node_id"), (int) nodeWithContextMenu);
            }

            if (ImGui.isPopupOpen("node_context")) {
                final int targetNode = ImGui.getStateStorage().getInt(ImGui.getID("delete_node_id"));
                if (ImGui.beginPopup("node_context")) {
                    drawNodeContextPopup(targetNode);
                    ImGui.endPopup();
                }
            }
            if (ImGui.isKeyDown(GLFW_KEY_LEFT_CONTROL) && ImGui.isKeyPressed(GLFW_KEY_G) && !ImGui.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
                if (!showGroupNameDialog && !showGatesGroupNameDialog) {
                    showGroupNameDialog = true;
                    if (selectedPins.size() > 0)
                        isInputNodeSelected = !selectedPins.get(0).isInput();

                    isNumberGroup = false;
                }
            }
            if (ImGui.isKeyDown(GLFW_KEY_LEFT_CONTROL) && ImGui.isKeyDown(GLFW_KEY_LEFT_SHIFT) && ImGui.isKeyPressed(GLFW_KEY_G)) {
                if (!showGroupNameDialog && !showGatesGroupNameDialog) {
                    showGroupNameDialog = true;
                    if (selectedPins.size() > 0)
                        isInputNodeSelected = !selectedPins.get(0).isInput();

                    isNumberGroup = true;
                }
            }

//        if (NodeEditor.showBackgroundContextMenu())
//            ImGui.openPopup("node_editor_context");
//
//        if (ImGui.beginPopup("node_editor_context")) {
//            drawEditorContextPopup();
//            ImGui.endPopup();
//        }

            if (showGroupNameDialog)
                showGroupNameInputPopup();

            NodeEditor.resume();

            if (NodeEditor.isBackgroundClicked()) {
                selectedPins.clear();
//                Collections.fill(isGatesActiveList, false);
            }

            NodeEditor.end();

            isUpdate = true;
        }
        ImGui.end();
    }

    private void drawGroup(List<GraphNodePin> pins, List<GraphNodePin> group, String groupName, ImVec2 nodePosition, ImVec2 offset) {
        for (int i = 0; i < pins.size(); i++) {
            int groupIndex = 0;
            for (GraphNodePin grPin : group) {
                int j = grPin.getId();

                if (group.size() > 1) {
                    if ((float) group.size() % 2 == 0.0f) {
                        if (groupIndex == group.size() / 2)
                            if (pins.get(i).getId() == j)
                                drawGroupText(groupName, new ImVec2(nodePosition.x - offset.x, nodePosition.y + (i * (22.0f + pinTouchExtraPadding * 2.0f)) - 11.0f + 5.0f), group);
                    } else if (groupIndex == group.size() / 2) {
                        if (pins.get(i).getId() == j)
                            drawGroupText(groupName, new ImVec2(nodePosition.x - offset.x, nodePosition.y + (i * (22.0f + pinTouchExtraPadding * 2.0f)) + pinTouchExtraPadding + 5.0f), group);
                    }
                } else {
                    if (pins.get(i).getId() == j)
                        drawGroupText(groupName, new ImVec2(nodePosition.x - offset.x, nodePosition.y + (i * (22.0f + pinTouchExtraPadding * 2.0f)) + pinTouchExtraPadding + 5.0f), group);
                }
                groupIndex++;
            }
        }
    }

    private void drawGroupText(String groupName, ImVec2 position, List<GraphNodePin> groupPins) {
        ImGui.setCursorPos(position.x, position.y);
        if (groupName.startsWith("##Number_")) {
            StringBuilder binaryString = new StringBuilder();
            for (GraphNodePin groupPin : groupPins) {
                if (groupPin.getValue())
                    binaryString.append("1");
                else
                    binaryString.append("0");
            }
            int finalNumber = Integer.parseInt(binaryString.reverse().toString(),2);

            String finalText = "";

            switch (groupNumbersDisplayFormat.get()) {
                case groupNumbersDisplay_Decimal -> finalText = groupName.replace("##Number_", "") + ": " + finalNumber;
                case groupNumbersDisplay_Binary -> finalText = groupName.replace("##Number_", "") + ": " + binaryString;
                case groupNumbersDisplay_DecimalBinary -> finalText = groupName.replace("##Number_", "") + ": " + finalNumber + " | " + binaryString;
                case groupNumbersDisplay_BinaryDecimal -> finalText = groupName.replace("##Number_", "") + ": " + binaryString + " | " + finalNumber;
            }

            ImVec2 tmp = new ImVec2();
            ImGui.calcTextSize(tmp, finalText);
            if (groupPins.get(0).isInput())
                ImGui.setCursorPos(position.x - tmp.x, position.y);

            ImGui.text(finalText);
        } else {
            ImGui.text(groupName);
        }
    }

    private void loadGates() {
        if (isUpdate) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Graph.class, new GraphDeserializer())
                    .registerTypeAdapter(GraphNode.class, new GraphNodeDeserializer())
                    .create();

            File gatesFolder = new File("projects/sample/gates");

            List<File> gates = new ArrayList<>(List.of(Objects.requireNonNull(gatesFolder.listFiles())));
//            if (gates.equals(oldGates)) {
//                return;
//            }
            oldGates = gates;

            this.gates.clear();
//            this.isGatesActiveList.clear();

            for (File gate : gates) {
                String inFile = "";
                try {
                    inFile = new String(Files.readAllBytes(Paths.get(gate.getPath())));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!inFile.equals("")) {
                    Graph graph = gson.fromJson(inFile, Graph.class);

                    if (!isGateGrouped(graph))
                        if (this.gates.size() > 0) {
                            if (!containsGate(graph.getFilepath())) {
                                this.gates.add(graph);
//                                this.isGatesActiveList.add(false);
                            }
                        } else {
                            this.gates.add(graph);
//                            this.isGatesActiveList.add(false);
                        }
                }
            }
        }
    }

    private boolean containsGate(String filepath) {
        if (this.gates.size() > 0)
            for (Graph gate : this.gates)
                if (gate.getFilepath().equals(filepath))
                    return true;

        return false;
    }

    private void drawMenuBar() {
        if (ImGui.button("Navigate to content  ") || ImGui.isKeyPressed(GLFW_KEY_F))
            if (!showGroupNameDialog && !showGatesGroupNameDialog)
                NodeEditor.navigateToContent(1);

        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() - 20.0f);
        ImGui.textDisabled("F");

//        ImGui.sameLine();
//        if (ImGui.button("Save"))
//            currentGraph.save();

//        ImGui.sameLine();
//        if (ImGui.button("Open")) {
//            File graph = FileUtil.openFile(FileTypeFilter.gateAndGraphFilter, true);
//            if (graph != null)
//                currentGraph = currentGraph.load(graph.getPath());
//        }

//        ImGui.sameLine();
//        if (ImGui.button("Reload"))
//            currentGraph = currentGraph.load(currentGraph.getFilepath());

        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 9.0f);
        if (ImGui.button("Clear Graph\t   ") || (ImGui.isKeyDown(GLFW_KEY_LEFT_CONTROL) && ImGui.isKeyPressed(GLFW_KEY_L))) {
            if (!showGroupNameDialog && !showGatesGroupNameDialog) {
                selectedPins.clear();

                this.newGateName = "";
                currentGraph = new Graph(currentGraph.getFilepath(), currentGraph.getGateName());
                currentGraph.createInputAndOutput();
                currentGraph.getGateColor().set(43.0f, 45.0f, 52.0f);

                NodeEditor.setNodePosition(currentGraph.getInputNode().getId(), 450.0f, 350.0f);
                NodeEditor.setNodePosition(currentGraph.getOutputNode().getId(), 900.0f, 350.0f);

                NodeEditor.navigateToContent(1);
            }
        }
        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() - 54.0f);
        ImGui.textDisabled("Ctrl+L");

        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 11.0f);
        ImGui.text("Numbers display format");
        ImGui.sameLine();
        ImGui.setNextItemWidth(150.0f);
        ImVec4 buttonColor = ImGui.getStyle().getColor(ImGuiCol.Button);
        ImVec4 buttonHoveredColor = ImGui.getStyle().getColor(ImGuiCol.ButtonHovered);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, buttonColor.x, buttonColor.y, buttonColor.z, buttonColor.w);
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, buttonHoveredColor.x, buttonHoveredColor.y, buttonHoveredColor.z, buttonHoveredColor.w);
        ImGui.combo("##NumbersDisplayFormat", groupNumbersDisplayFormat, numbersDisplayFormatsList);
        ImGui.popStyleColor(2);

        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 3.0f);
        ImGui.text("Show Pin titles");
        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 10.0f);
        ImGui.textDisabled("Alt+H");
        ImGui.sameLine();
        if (ImGui.checkbox("##ShowPinTitles", showPinTitles) || (ImGui.isKeyDown(GLFW_KEY_LEFT_ALT) && ImGui.isKeyPressed(GLFW_KEY_H)))
            showPinTitles = !showPinTitles;

        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 3.0f);
        ImGui.text("Show Pins");
        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 10.0f);
        ImGui.textDisabled("Alt+P");
        ImGui.sameLine();
        if (ImGui.checkbox("##ShowPins", showPins) || (ImGui.isKeyDown(GLFW_KEY_LEFT_ALT) && ImGui.isKeyPressed(GLFW_KEY_P)))
            showPins = !showPins;

        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 3.0f);
        ImGui.text("Show Connections");
        ImGui.sameLine();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 10.0f);
        ImGui.textDisabled("Alt+L");
        ImGui.sameLine();
        if (ImGui.checkbox("##ShowConnections", showConnections) || (ImGui.isKeyDown(GLFW_KEY_LEFT_ALT) && ImGui.isKeyPressed(GLFW_KEY_L)))
            showConnections = !showConnections;

        ImGui.setCursorPosY(ImGui.getCursorPosY() + 3.0f);
        ImGui.text("Gate Name");
        ImGui.sameLine();
        ImGui.setCursorPosY(ImGui.getCursorPosY() - 3.0f);
        ImString gateNameTmp = new ImString(this.newGateName, 32);
        ImGui.setNextItemWidth(357.0f);
        if (ImGui.inputText("##GateName", gateNameTmp))
            this.newGateName = gateNameTmp.get();

        ImGui.sameLine();
        ImGui.text("Gate Color");
        ImGui.sameLine();
        float[] tmpColor = new float[]{
                currentGraph.getGateColor().x / 255.0f,
                currentGraph.getGateColor().y / 255.0f,
                currentGraph.getGateColor().z / 255.0f
        };
        if (ImGui.colorEdit3("##GateColor", tmpColor, ImGuiColorEditFlags.NoInputs))
            currentGraph.getGateColor().set(tmpColor[0] * 255.0f, tmpColor[1] * 255.0f, tmpColor[2] * 255.0f);

        ImGui.sameLine();
        if (ImGui.button("Create Gate")) {
            if (!newGateName.equals("")) {
                selectedPins.clear();

                this.gates.clear();
//                this.isGatesActiveList.clear();

                for (GraphNodePin pin : currentGraph.getInputNode().outputPins)
                    pin.setValue(false);
                for (GraphNodePin pin : currentGraph.getOutputNode().inputPins)
                    pin.setValue(false);

                Graph backup = new Graph(currentGraph.getFilepath(), currentGraph.getGateName());
                currentGraph.saveAsGate(newGateName);

                currentGraph = backup;
                currentGraph.createInputAndOutput();
                currentGraph.getGateColor().set(43.0f, 45.0f, 52.0f);
                this.newGateName = "";
            } else {
                System.out.println("Empty Gate Name!!!");
            }
        }

        ImGui.separator();
        if (ImGui.button("And"))
            positionNode(currentGraph.copyCreateGraphNode(new Gate_And(), new ImVec2()));
        ImGui.sameLine();
        if (ImGui.button("Not"))
            positionNode(currentGraph.copyCreateGraphNode(new Gate_Not(), new ImVec2()));
        ImGui.sameLine();
        if (ImGui.button("7-Segment Display"))
            positionNode(currentGraph.copyCreateGraphNode(new Gate_7SegmentDisplay(), new ImVec2()));

        ImGui.separator();
        for (int i = 0; i < this.gateGroups.size(); i++) {
            String groupName = (String) this.gateGroups.keySet().toArray()[i];
            ImVec2 buttonPos = ImGui.getCursorScreenPos();
            if (ImGui.button(groupName + "  "))
                ImGui.openPopup("GateGroup" + groupName);

            ImVec2 tmp = new ImVec2();
            ImGui.calcTextSize(tmp, groupName);

            ImVec4 textColor = ImGui.getStyle().getColor(ImGuiCol.Text);
            ImGui.getWindowDrawList().addTriangleFilled(
                    buttonPos.x + 13.0f + tmp.x, buttonPos.y + 8.0f,
                    buttonPos.x + 23.0f + tmp.x, buttonPos.y + 8.0f,
                    buttonPos.x + 18.0f + tmp.x, buttonPos.y + 16.0f,
                    ImGui.getColorU32(textColor.x, textColor.y, textColor.z, textColor.w));

            if (ImGui.isPopupOpen("GateGroup" + groupName))
                ImGui.setNextWindowPos(buttonPos.x, buttonPos.y + (ImGui.getFontSize() + ImGui.getStyle().getFramePaddingY() * 2.0f));
            if (ImGui.beginPopup("GateGroup" + groupName)) {
                ImGui.pushID("GateGroup" + groupName);

                for (int j = 0; j < this.gateGroups.get(groupName).size(); j++) {
                    Graph gate = this.gateGroups.get(groupName).get(j);

                    if (ImGui.menuItem(gate.getGateName())) {
                        if (!showGroupNameDialog && !showGatesGroupNameDialog)
                            positionNode(currentGraph.loadCreateGate(gate, new ImVec2()));
                        ImGui.closeCurrentPopup();
                    }
                }
                ImGui.popID();
                ImGui.endPopup();
            }

            ImVec2 lastButtonPos = new ImVec2();
            ImGui.getItemRectMax(lastButtonPos);
            float nextButtonX2 = lastButtonPos.x + (ImGui.getStyle().getItemSpacingX() * 2.0f) + (ImGui.getStyle().getFramePaddingX() * 2.0f) + tmp.x;

            if (i < this.gateGroups.size() - 1 && nextButtonX2 < ImGui.getContentRegionAvailX())
                ImGui.sameLine();
        }

        ImVec2 notSameLine = new ImVec2();
        for (int i = 0; i < this.gates.size(); i++) {
            if (isGateGrouped(this.gates.get(i))) {
                if (i == this.gates.size() - 1)
                    ImGui.setCursorPos(notSameLine.x, notSameLine.y);
                continue;
            }

            ImVec4 butColor = ImGui.getStyle().getColor(ImGuiCol.Button);
            ImVec4 butHoveredColor = ImGui.getStyle().getColor(ImGuiCol.ButtonHovered);
            ImVec4 butActiveColor = ImGui.getStyle().getColor(ImGuiCol.ButtonActive);
            if ((ImGui.isKeyDown(GLFW_KEY_LEFT_SHIFT) || ImGui.isKeyDown(GLFW_KEY_LEFT_CONTROL))) {
                if (!showGroupNameDialog && !showGatesGroupNameDialog) {
                    butColor = ImGui.getStyle().getColor(ImGuiCol.FrameBgHovered);
                    butHoveredColor = ImGui.getStyle().getColor(ImGuiCol.FrameBgHovered);
                    butActiveColor = ImGui.getStyle().getColor(ImGuiCol.FrameBgHovered);
                }
            }
            ImGui.pushStyleColor(ImGuiCol.Button, butColor.x, butColor.y, butColor.z, butColor.w);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, butHoveredColor.x, butHoveredColor.y, butHoveredColor.z, butHoveredColor.w);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, butActiveColor.x, butActiveColor.y, butActiveColor.z, butActiveColor.w);
            ImVec2 tmp = new ImVec2();
            ImGui.calcTextSize(tmp, this.gates.get(i).getGateName());
//            if (this.isGatesActiveList.get(i)) {
//                float buttonSelectionOutlineThickness = 2.0f;
//                ImGui.getWindowDrawList().addRectFilled(
//                        ImGui.getCursorScreenPosX() - buttonSelectionOutlineThickness, // X pos
//                        ImGui.getCursorScreenPosY() - buttonSelectionOutlineThickness, // Y pos
//                        ImGui.getCursorScreenPosX() + tmp.x + (ImGui.getStyle().getFramePaddingX() * 2.0f) + buttonSelectionOutlineThickness, // Size X
//                        ImGui.getCursorScreenPosY() + ImGui.getFontSize() + (ImGui.getStyle().getFramePaddingY() * 2.0f) + buttonSelectionOutlineThickness, // Size Y
//                        ImGui.getColorU32(0.1f, 0.629f, 0.873f, 0.827f), ImGui.getStyle().getFrameRounding() + 1.0f); // Color
//            }
            if (ImGui.button(this.gates.get(i).getGateName())) {
                if (!showGroupNameDialog && !showGatesGroupNameDialog) {
                    if (!ImGui.isKeyDown(GLFW_KEY_LEFT_SHIFT) && !ImGui.isKeyDown(GLFW_KEY_LEFT_CONTROL))
                        positionNode(currentGraph.loadCreateGate(this.gates.get(i), new ImVec2()));
                    else if (ImGui.isKeyDown(GLFW_KEY_LEFT_SHIFT))
                        this.isGatesActiveList.set(i, !this.isGatesActiveList.get(i));
                    else if (ImGui.isKeyDown(GLFW_KEY_LEFT_CONTROL))
                        this.isGatesActiveList.set(i, false);
                }
            }
            ImGui.openPopupOnItemClick("GroupButtons", ImGuiMouseButton.Right);

            ImGui.popStyleColor(3);

            ImVec2 lastButtonPos = new ImVec2();
            ImGui.getItemRectMax(lastButtonPos);
            float nextButtonX2 = lastButtonPos.x + ImGui.getStyle().getItemSpacingX() + (ImGui.getStyle().getFramePaddingX() * 2.0f) + tmp.x;

            notSameLine = ImGui.getCursorPos();
            if (i < this.gates.size() - 1 && nextButtonX2 < ImGui.getContentRegionAvailX())
                ImGui.sameLine();
        }

        if (ImGui.beginPopup("GroupButtons")) {
            if (ImGui.menuItem("Group Selected Gates"))
                showGatesGroupNameDialog = true;
            ImGui.endPopup();
        }

        if (showGatesGroupNameDialog)
            showGatesGroupNameInputPopup();
    }

    private void positionNode(GraphNode node) {
        float canvasX = NodeEditor.toCanvasX(ImGui.getWindowSizeX() / 2.0f + ImGui.getWindowPosX());
        float canvasY = NodeEditor.toCanvasY(ImGui.getWindowSizeY() / 2.0f + ImGui.getWindowPosY());
        node.setPosition(canvasX, canvasY);
        NodeEditor.setNodePosition(node.getId(), canvasX, canvasY);
    }

//    private void drawEditorContextPopup() {
//        if (startMousePos == null)
//            startMousePos = ImGui.getMousePos();
//
//        if (ImGui.beginMenu("Create Node")) {
//            for (String collectionName : this.createNodesList.keySet()) {
//                if (ImGui.beginMenu(collectionName)) {
//                    for (GraphNode node : this.createNodesList.get(collectionName)) {
//                        if (ImGui.menuItem(node.getName())) {
//                            GraphNode createdNode = this.currentGraph.copyCreateGraphNode(node, startMousePos);
//                            final float canvasX = NodeEditor.toCanvasX(startMousePos.x);
//                            final float canvasY = NodeEditor.toCanvasY(startMousePos.y);
//                            NodeEditor.setNodePosition(createdNode.getId(), canvasX, canvasY);
//                            startMousePos = null;
//                            ImGui.closeCurrentPopup();
//                        }
////                            ImGui.sameLine();
////                            if (EditorImGui.beginHelpMarker()) {
////                                node.drawTooltip();
////                                EditorImGui.endHelpMarker();
////                            }
//                    }
//                    ImGui.endMenu();
//                }
//            }
//            ImGui.endMenu();
//        }
//    }

    private void drawNodeContextPopup(int targetNode) {
        if (currentGraph.getInputNode().getId() == targetNode) {
            if (ImGui.menuItem("Group selected pins")) {
                showGroupNameDialog = true;
                isInputNodeSelected = true;
                isNumberGroup = false;
            }
            ImGui.sameLine();
            ImGui.textDisabled("Ctrl+G");
            if (ImGui.menuItem("Group selected pins to number")) {
                showGroupNameDialog = true;
                isInputNodeSelected = true;
                isNumberGroup = true;
            }
            ImGui.sameLine();
            ImGui.textDisabled("Ctrl+Shift+G");

        } else if (currentGraph.getOutputNode().getId() == targetNode) {
            if (ImGui.menuItem("Group selected pins")) {
                showGroupNameDialog = true;
                isInputNodeSelected = false;
                isNumberGroup = false;
            }
            ImGui.sameLine();
            ImGui.textDisabled("Ctrl+G");
            if (ImGui.menuItem("Group selected pins to number")) {
                showGroupNameDialog = true;
                isInputNodeSelected = false;
                isNumberGroup = true;
            }
            ImGui.sameLine();
            ImGui.textDisabled("Ctrl+Shift+G");

        } else if (ImGui.menuItem("Delete " + currentGraph.nodes.get(targetNode).getName())) {
            currentGraph.deleteNodeById(targetNode);
            ImGui.closeCurrentPopup();
        }
    }

    private void showGroupNameInputPopup() {
        if (selectedPins.size() > 0) {
            ImGui.setNextWindowSize(438.0f, 130.0f);
            ImGui.setNextWindowPos(ImGui.getWindowViewport().getWorkSizeX() / 2.0f - 438.0f / 2.0f + ImGui.getWindowViewport().getPosX(),
                    ImGui.getWindowViewport().getWorkSizeY() / 2.0f - 130.0f / 2.0f + ImGui.getWindowViewport().getPosY());
        } else {
            ImGui.setNextWindowSize(438.0f, 100.0f);
            ImGui.setNextWindowPos(ImGui.getWindowViewport().getWorkSizeX() / 2.0f - 438.0f / 2.0f + ImGui.getWindowViewport().getPosX(),
                    ImGui.getWindowViewport().getWorkSizeY() / 2.0f - 100.0f / 2.0f + ImGui.getWindowViewport().getPosY());
        }

        ImGui.openPopup("Enter group name");
        if (ImGui.beginPopupModal("Enter group name", ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove)) {
            ImVec2 tmp = new ImVec2();
            if (selectedPins.size() > 0) {
                ImGui.calcTextSize(tmp, "Group Name");
                ImGui.setCursorPos((ImGui.getContentRegionAvailX() / 2.0f) - (tmp.x / 2.0f), ImGui.getCursorPosY() + 5.0f);
                ImGui.text("Group Name");

                ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
                ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 10.0f, ImGui.getStyle().getFramePaddingY());
                ImGui.setCursorPosY(ImGui.getCursorPosY() + 5.0f);

                ImString tmpStr = new ImString(groupName, 14);
                if (ImGui.inputText("##GroupName", tmpStr))
                    groupName = tmpStr.get();

                if (setFocusOnInput) {
                    ImGui.setKeyboardFocusHere(-1);
                    setFocusOnInput = false;
                }
                ImGui.setWindowFocus();

                ImGui.popStyleVar();
            } else {
                ImGui.calcTextSize(tmp, "No pins selected");
                ImGui.setCursorPos((ImGui.getContentRegionAvailX() / 2.0f) - (tmp.x / 2.0f), ImGui.getCursorPosY() + 5.0f);
                ImGui.text("No pins selected");
            }
            ImGui.setCursorPos(ImGui.getContentRegionAvailX() / 6.0f, ImGui.getCursorPosY() + 10.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImGui.getContentRegionAvailX() / 6.0f, ImGui.getStyle().getFramePaddingY());
            if (ImGui.button("Close") || ImGui.isKeyPressed(GLFW_KEY_ESCAPE)) {
                groupName = "";
                showGroupNameDialog = false;
                setFocusOnInput = true;
            }

            ImGui.sameLine();
            if (ImGui.button("Ok") || ImGui.isKeyPressed(GLFW_KEY_ENTER)) {
                if (!groupName.equals("")) {
                    if (isNumberGroup)
                        groupName = "##Number_" + groupName;

                    groupSelectedPins();

                    groupName = "";
                    showGroupNameDialog = false;
                    setFocusOnInput = true;
                } else if (selectedPins.size() == 0) {
                    showGroupNameDialog = false;
                    setFocusOnInput = true;
                }
            }
            ImGui.popStyleVar();
            ImGui.endPopup();
        }
    }

    private void showGatesGroupNameInputPopup() {
//        if (isGatesActiveList.contains(true)) {
//            ImGui.setNextWindowSize(438.0f, 130.0f);
//            ImGui.setNextWindowPos(ImGui.getWindowViewport().getWorkSizeX() / 2.0f - 438.0f / 2.0f + ImGui.getWindowViewport().getPosX(),
//                    ImGui.getWindowViewport().getWorkSizeY() / 2.0f - 130.0f / 2.0f + ImGui.getWindowViewport().getPosY());
//        } else {
//            ImGui.setNextWindowSize(438.0f, 100.0f);
//            ImGui.setNextWindowPos(ImGui.getWindowViewport().getWorkSizeX() / 2.0f - 438.0f / 2.0f + ImGui.getWindowViewport().getPosX(),
//                    ImGui.getWindowViewport().getWorkSizeY() / 2.0f - 100.0f / 2.0f + ImGui.getWindowViewport().getPosY());
//        }

        ImGui.openPopup("Enter group name##Gates");
//        if (ImGui.beginPopupModal("Enter group name##Gates", ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove)) {
//            ImVec2 tmp = new ImVec2();
//            if (isGatesActiveList.contains(true)) {
//                ImGui.calcTextSize(tmp, "Group Name");
//                ImGui.setCursorPos((ImGui.getContentRegionAvailX() / 2.0f) - (tmp.x / 2.0f), ImGui.getCursorPosY() + 5.0f);
//                ImGui.text("Group Name");
//
//                ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
//                ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 10.0f, ImGui.getStyle().getFramePaddingY());
//                ImGui.setCursorPosY(ImGui.getCursorPosY() + 5.0f);
//
//                ImString tmpStr = new ImString(gateGroupName, 14);
//                if (ImGui.inputText("##GroupName", tmpStr))
//                    gateGroupName = tmpStr.get();
//
//                if (setFocusOnInput) {
//                    ImGui.setKeyboardFocusHere(-1);
//                    setFocusOnInput = false;
//                }
//                ImGui.setWindowFocus();
//
//                ImGui.popStyleVar();
//            } else {
//                ImGui.calcTextSize(tmp, "No gates selected");
//                ImGui.setCursorPos((ImGui.getContentRegionAvailX() / 2.0f) - (tmp.x / 2.0f), ImGui.getCursorPosY() + 5.0f);
//                ImGui.text("No gates selected");
//            }
//            ImGui.setCursorPos(ImGui.getContentRegionAvailX() / 6.0f, ImGui.getCursorPosY() + 10.0f);
//            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImGui.getContentRegionAvailX() / 6.0f, ImGui.getStyle().getFramePaddingY());
//            if (ImGui.button("Close") || ImGui.isKeyPressed(GLFW_KEY_ESCAPE)) {
//                gateGroupName = "";
//                showGatesGroupNameDialog = false;
//                setFocusOnInput = true;
//            }
//
//            ImGui.sameLine();
//            if (ImGui.button("Ok") || ImGui.isKeyPressed(GLFW_KEY_ENTER)) {
//                if (!gateGroupName.equals("")) {
//                    if (isNumberGroup)
//                        gateGroupName = "##Number_" + gateGroupName;
//
//                    groupSelectedGates();
//
//                    gateGroupName = "";
//                    showGatesGroupNameDialog = false;
//                    setFocusOnInput = true;
//                } else if (!isGatesActiveList.contains(true)) {
//                    showGatesGroupNameDialog = false;
//                    setFocusOnInput = true;
//                }
//            }
//            ImGui.popStyleVar();
//            ImGui.endPopup();
//        }
    }

    private void groupSelectedPins() {
        List<GraphNodePin> currentNodePins = new ArrayList<>(selectedPins);

        if (isInputNodeSelected) {
            GraphNode parent = currentGraph.getInputNode();
            currentNodePins.removeIf(pin -> !parent.outputPins.contains(pin));
            currentGraph.getInputNode().addGroup(groupName, currentNodePins);
        } else {
            GraphNode parent = currentGraph.getOutputNode();
            currentNodePins.removeIf(pin -> !parent.inputPins.contains(pin));
            currentGraph.getOutputNode().addGroup(groupName, currentNodePins);
        }
    }

    private void groupSelectedGates() {
//        List<Graph> groupGates = new ArrayList<>();
//
//        for (int i = 0; i < this.gates.size(); i++)
//            if (this.isGatesActiveList.get(i))
//                groupGates.add(this.gates.get(i));
//
//        gateGroups.put(gateGroupName, groupGates);
//
//        Collections.fill(this.isGatesActiveList, false);
    }

    private boolean isGateGrouped(Graph gate) {
        for (String groupName : this.gateGroups.keySet())
            for (Graph ga : this.gateGroups.get(groupName))
                if (ga.getFilepath().equals(gate.getFilepath()))
                    return true;

        return false;
    }

    public static void setPinSelected(GraphNodePin pin) {
        if (showGroupNameDialog && showGatesGroupNameDialog)
            return;

        if (!ImGui.isKeyDown(GLFW_KEY_LEFT_SHIFT) && !ImGui.isKeyDown(GLFW_KEY_LEFT_CONTROL))
            selectedPins.clear();

        if (ImGui.isKeyDown(GLFW_KEY_LEFT_CONTROL) || (selectedPins.contains(pin) && ImGui.isKeyDown(GLFW_KEY_LEFT_SHIFT)))
            selectedPins.remove(pin);
        else if (!selectedPins.contains(pin))
            selectedPins.add(pin);
    }

    public static boolean isPinSelected(GraphNodePin pin) { return selectedPins.contains(pin); }
}
