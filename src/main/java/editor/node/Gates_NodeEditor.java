package editor.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import imgui.type.ImLong;
import imgui.type.ImString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
                if (targetNode != 1 && targetNode != 2)
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
                this.gates.clear();
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
        if (ImGui.button("Delete " + currentGraph.nodes.get(targetNode).getName())) {
            currentGraph.deleteNodeById(targetNode);
//            this.currentGraph.nodes.remove(targetNode);
            ImGui.closeCurrentPopup();
        }
    }
}
