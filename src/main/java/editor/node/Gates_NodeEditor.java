package editor.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import editor.TestFieldsWindow;
import editor.graph.Graph;
import editor.graph.GraphDeserializer;
import editor.gates.*;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.flag.NodeEditorStyleColor;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImLong;
import imgui.type.ImString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class Gates_NodeEditor {

    private static final NodeEditorContext CONTEXT;
    private static Graph currentGraph = new Graph("projects/sample/graphs/sample.graph", "Demo");

    private ImVec2 startMousePos;

    private List<Graph> gates = new ArrayList<>();
    private boolean isStart;
    private boolean isUpdate;

    private String gateName = "";

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

//                List<GraphNodePin> groupInputPins = new ArrayList<>();
//                float yPos = 0.0f;
//
//                for (int i = 0; i < node.inputPins.size(); i++) {
//                    if (node.inputPins.get(i).getGroupName() != null)
//                        if (!node.inputPins.get(i).getGroupName().equals("")) {
//                            if (yPos == 0.0f)
//                                yPos = i;
//
//                            groupInputPins.add(node.inputPins.get(i));
//                        }
//                }
//
//                for (int i = 0; i < groupInputPins.size(); i++) {
//                    if (i == 0) {
//                        ImGui.setCursorScreenPos(NodeEditor.getNodePositionX(node.getId()) - 9.0f, NodeEditor.getNodePositionY(node.getId()) + (yPos * 26.0f));
//                        ImGui.text("-");
//                    }
//                    if (i == groupInputPins.size() - 1) {
//                        ImGui.setCursorScreenPos(NodeEditor.getNodePositionX(node.getId()) - 9.0f, NodeEditor.getNodePositionY(node.getId()) + (yPos * 26.0f) + TestFieldsWindow.getFloats[1]);
//                        ImGui.text("-");
//                    }
//                }
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

            int uniqueLinkId = 1;
            for (GraphNode node : currentGraph.nodes.values()) {
                for (GraphNodePin inputPin : node.inputPins)
                    if (inputPin.hasConnections())
                        for (GraphNodePin connectedPin : inputPin.getConnectedPins()) {
                            GraphNode tmpNode = currentGraph.findByOutput(connectedPin.getId());
                            if (tmpNode != null && currentGraph.nodes.containsKey(tmpNode.getId()))
                                NodeEditor.link(uniqueLinkId++, connectedPin.getId(), inputPin.getId());
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

//        if (NodeEditor.showBackgroundContextMenu())
//            ImGui.openPopup("node_editor_context");
//
//        if (ImGui.beginPopup("node_editor_context")) {
//            drawEditorContextPopup();
//            ImGui.endPopup();
//        }

            if (showGroupNameDialog)
                ImGui.openPopup("Enter group name");

            showGroutNameInputPopup();

            NodeEditor.resume();
            NodeEditor.end();

            isUpdate = true;
        }
        ImGui.end();
    }

    private void loadGates() {
        if (isUpdate) {
            this.gates.clear();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Graph.class, new GraphDeserializer())
                    .registerTypeAdapter(GraphNode.class, new GraphNodeDeserializer())
                    .create();

            File gatesFolder = new File("projects/sample/gates");

            List<File> gates = new ArrayList<>(List.of(Objects.requireNonNull(gatesFolder.listFiles())));

            for (File gate : gates) {
                String inFile = "";
                try {
                    inFile = new String(Files.readAllBytes(Paths.get(gate.getPath())));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!inFile.equals("")) {
                    Graph graph = gson.fromJson(inFile, Graph.class);

                    if (this.gates.size() > 0) {
                        if (!containsGate(graph.getFilepath()))
                            this.gates.add(graph);
                    } else
                        this.gates.add(graph);
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
        if (ImGui.button("Navigate to content"))
            NodeEditor.navigateToContent(1);

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
        if (ImGui.button("Clear Graph")) {
            selectedPins.clear();

            this.gateName = "";
            currentGraph = new Graph(currentGraph.getFilepath(), currentGraph.getGateName());
            currentGraph.createInputAndOutput();
            currentGraph.getGateColor().set(43.0f, 45.0f, 52.0f);
        }

        ImGui.setCursorPosY(ImGui.getCursorPosY() + 3.0f);
        ImGui.text("Gate Name");
        ImGui.sameLine();
        ImGui.setCursorPosY(ImGui.getCursorPosY() - 3.0f);
        ImString gateNameTmp = new ImString(this.gateName, 256);
        if (ImGui.inputText("##GateName", gateNameTmp))
            this.gateName = gateNameTmp.get();

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
            if (!gateName.equals("")) {
                selectedPins.clear();

                this.gates.clear();

                for (GraphNodePin pin : currentGraph.getInputNode().outputPins)
                    pin.setValue(false);
                for (GraphNodePin pin : currentGraph.getOutputNode().inputPins)
                    pin.setValue(false);

                currentGraph.saveAsGate(gateName);

                currentGraph = new Graph(currentGraph.getFilepath(), currentGraph.getGateName());
                currentGraph.createInputAndOutput();
                currentGraph.getGateColor().set(43.0f, 45.0f, 52.0f);
                this.gateName = "";
            } else {
                System.out.println("Empty Gate Name!!!");
            }
        }

        ImGui.separator();
        if (ImGui.button("And")) {
            startMousePos = ImGui.getMousePos();
            Gate_And createdNode = (Gate_And) currentGraph.copyCreateGraphNode(new Gate_And(), startMousePos);
            final float canvasX = NodeEditor.toCanvasX(startMousePos.x + 50.0f);
            final float canvasY = NodeEditor.toCanvasY(startMousePos.y + 40.0f);
            startMousePos = null;
            NodeEditor.setNodePosition(createdNode.getId(), canvasX, canvasY);
        }
        ImGui.sameLine();
        if (ImGui.button("Not")) {
            startMousePos = ImGui.getMousePos();
            Gate_Not createdNode = (Gate_Not) currentGraph.copyCreateGraphNode(new Gate_Not(), startMousePos);
            final float canvasX = NodeEditor.toCanvasX(startMousePos.x + 50.0f);
            final float canvasY = NodeEditor.toCanvasY(startMousePos.y + 40.0f);
            startMousePos = null;
            NodeEditor.setNodePosition(createdNode.getId(), canvasX, canvasY);
        }

        ImGui.separator();
        for (int i = 0; i < this.gates.size(); i++) {
            if (ImGui.button(this.gates.get(i).getGateName())) {
                startMousePos = ImGui.getMousePos();
                GraphNode_Graph createdNode = currentGraph.loadCreateGate(this.gates.get(i), startMousePos);
                final float canvasX = NodeEditor.toCanvasX(startMousePos.x + 50.0f);
                final float canvasY = NodeEditor.toCanvasY(startMousePos.y + 40.0f);
                startMousePos = null;
                NodeEditor.setNodePosition(createdNode.getId(), canvasX, canvasY);
            }
            if (i < this.gates.size() - 1)
                ImGui.sameLine();
        }
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
            }

        } else if (currentGraph.getOutputNode().getId() == targetNode) {
            if (ImGui.menuItem("Group selected pins")) {
                showGroupNameDialog = true;
                isInputNodeSelected = false;
            }

        } else if (ImGui.menuItem("Delete " + currentGraph.nodes.get(targetNode).getName())) {
            currentGraph.deleteNodeById(targetNode);
            ImGui.closeCurrentPopup();
        }
    }

    private boolean showGroupNameDialog = false;
    private boolean isInputNodeSelected = false;
    private String groupName = "";

    private void showGroutNameInputPopup() {
        if (selectedPins.size() > 0) {
            ImGui.setNextWindowSize(438.0f, 130.0f);
            ImGui.setNextWindowPos(ImGui.getWindowViewport().getWorkSizeX() / 2.0f - 438.0f / 2.0f + ImGui.getWindowViewport().getPosX(),
                    ImGui.getWindowViewport().getWorkSizeY() / 2.0f - 130.0f / 2.0f + ImGui.getWindowViewport().getPosY());
        } else {
            ImGui.setNextWindowSize(438.0f, 100.0f);
            ImGui.setNextWindowPos(ImGui.getWindowViewport().getWorkSizeX() / 2.0f - 438.0f / 2.0f + ImGui.getWindowViewport().getPosX(),
                    ImGui.getWindowViewport().getWorkSizeY() / 2.0f - 100.0f / 2.0f + ImGui.getWindowViewport().getPosY());
        }

        if (showGroupNameDialog && ImGui.beginPopupModal("Enter group name", ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove)) {
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

                ImGui.popStyleVar();
            } else {
                ImGui.calcTextSize(tmp, "No pins selected");
                ImGui.setCursorPos((ImGui.getContentRegionAvailX() / 2.0f) - (tmp.x / 2.0f), ImGui.getCursorPosY() + 5.0f);
                ImGui.text("No pins selected");
            }
            ImGui.setCursorPos(ImGui.getContentRegionAvailX() / 6.0f, ImGui.getCursorPosY() + 10.0f);
            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImGui.getContentRegionAvailX() / 6.0f, ImGui.getStyle().getFramePaddingY());
            if (ImGui.button("Close")) {
                groupName = "";
                showGroupNameDialog = false;
            }

            ImGui.sameLine();
            if (ImGui.button("Ok")) {
                if (!groupName.equals("")) {
                    groupSelectedPins();

                    groupName = "";
                    showGroupNameDialog = false;
                }
            }
            ImGui.popStyleVar();
            ImGui.endPopup();
        }
    }

    private void groupSelectedPins() {
        List<GraphNodePin> currentNodePins = new ArrayList<>(selectedPins);

        if (isInputNodeSelected) {
            GraphNode parent = currentGraph.getInputNode();
            currentNodePins.removeIf(pin -> !parent.outputPins.contains(pin));
        } else {
            GraphNode parent = currentGraph.getOutputNode();
            currentNodePins.removeIf(pin -> !parent.inputPins.contains(pin));
        }

        if (isInputNodeSelected)
            currentGraph.getInputNode().addGroup(groupName, currentNodePins);
        else
            currentGraph.getOutputNode().addGroup(groupName, currentNodePins);
    }

    private static List<GraphNodePin> selectedPins = new ArrayList<>();

    public static void setPinSelected(GraphNodePin pin) {
        if (!ImGui.isKeyDown(GLFW_KEY_LEFT_SHIFT) && !ImGui.isKeyDown(GLFW_KEY_LEFT_CONTROL))
            selectedPins.clear();

        if (ImGui.isKeyDown(GLFW_KEY_LEFT_CONTROL))
            selectedPins.remove(pin);
        else if (!selectedPins.contains(pin))
            selectedPins.add(pin);
    }

    public static boolean isPinSelected(GraphNodePin pin) { return selectedPins.contains(pin); }
}
