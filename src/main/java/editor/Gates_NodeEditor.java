package editor;

import editor.nodes.*;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.NodeEditorStyle;
import imgui.extension.nodeditor.flag.NodeEditorStyleColor;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.flag.ImGuiMouseButton;
import imgui.internal.ImGui;
import imgui.type.ImLong;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Gates_NodeEditor {

    private static final NodeEditorContext CONTEXT;
    private Graph currentGraph = new Graph("graphs/sample.graph");

    private ImVec2 startMousePos;

    private final Map<String, List<GraphNode>> createNodesList = new LinkedHashMap<>(){{
        put("Simple", new ArrayList<>(){{
            add(new Gate_Toggle());
            add(new Gate_Not());
            add(new Gate_And());
            add(new Gate_Test());
            add(new Test2());
        }});
    }};

    static {
        NodeEditorConfig config = new NodeEditorConfig();
        config.setSettingsFile("graph.ini");
        CONTEXT = new NodeEditorContext(config);
    }

    public void imgui() {
        drawMenuBar();

        NodeEditor.setCurrentEditor(CONTEXT);
        NodeEditor.begin("Node Editor");
        NodeEditor.pushStyleVar(NodeEditorStyleVar.NodePadding, 4.0f, 3.0f, 4.0f, 4.0f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.NodeRounding, 5.0f);
        NodeEditor.pushStyleColor(NodeEditorStyleColor.PinRect, 0.0f, 0.0f, 0.0f, 0.0f);
        NodeEditor.pushStyleColor(NodeEditorStyleColor.NodeBg, 0.206f, 0.214f, 0.225f, 1.0f);
//        NodeEditor.pushStyleColor(NodeEditorStyleColor.PinRect, color[0], color[1], color[2], color[3]);

        for (GraphNode node : this.currentGraph.nodes.values()) {
            NodeEditor.beginNode(node.getId());
            ImGui.pushID(node.getId());

            node.updatePins();
            node.update();

            node.imgui();

            ImGui.popID();

//            ImGui.text(node.getName());
//
//            NodeEditor.beginPin(node.getInputPinId(), NodeEditorPinKind.Input);
//            ImGui.text("-> In");
//            NodeEditor.endPin();
//
//            ImGui.sameLine();
//
//            NodeEditor.beginPin(node.getOutputPinId(), NodeEditorPinKind.Output);
//            ImGui.text("Out ->");
//            NodeEditor.endPin();

            NodeEditor.endNode();
        }

//        if (ImNodes.isLinkCreated(LINK_A, LINK_B)) {
//            isLinkDragged = false;
//
//            GraphNode sourceNode = currentGraph.findByOutput(LINK_A.get());
//            GraphNodePin sourcePin = currentGraph.findOutputPin(LINK_A.get());
//            GraphNode targetNode = currentGraph.findByInput(LINK_B.get());
//            GraphNodePin targetPin = currentGraph.findInputPin(LINK_B.get());
//
//            if (sourceNode != null && sourcePin != null && targetNode != null && targetPin != null) {
//                if (sourceNode != targetNode) {
//                    for (GraphNodePin connectedPin : targetPin.getConnectedPins())
//                        connectedPin.removeConnectedPin(targetPin);
//
//                    targetPin.setConnectedPin(sourcePin);
//                    sourcePin.addConnectedPin(targetPin);
//                }
//            }
//        }

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
                            for (GraphNodePin connectedPin : targetPin.getConnectedPins())
                                connectedPin.removeConnectedPin(targetPin);

                            targetPin.setConnectedPin(sourcePin);
                            sourcePin.addConnectedPin(targetPin);
                        }
                    }
                }
            }
//            final ImLong a = new ImLong();
//            final ImLong b = new ImLong();
//            if (NodeEditor.queryNewLink(a, b)) {
//                final GraphNode source = this.currentGraph.findByOutput(a.get());
//                final GraphNode target = this.currentGraph.findByInput(b.get());
//                if (source != null && target != null && source.outputNodeId != target.getId() && NodeEditor.acceptNewItem()) {
//                    source.outputNodeId = target.getId();
//                }
//            }
        }
        NodeEditor.endCreate();

        int uniqueLinkId = 1;
        for (GraphNode node : currentGraph.nodes.values()) {
            for (GraphNodePin inputPin : node.inputPins)
                for (GraphNodePin connectedPin : inputPin.getConnectedPins()) {
                    GraphNode tmpNode = currentGraph.findByOutput(connectedPin.getId());
                    if (tmpNode != null && currentGraph.nodes.containsKey(tmpNode.getId()))
                        NodeEditor.link(uniqueLinkId++, connectedPin.getId(), inputPin.getId());
                }
        }

//        int uniqueLinkId = 1;
//        for (GraphNode node : this.currentGraph.nodes.values()) {
//            if (this.currentGraph.nodes.containsKey(node.outputNodeId)) {
//                NodeEditor.link(uniqueLinkId++, node.getOutputPinId(), this.currentGraph.nodes.get(node.outputNodeId).getInputPinId());
//            }
//        }

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

        if (NodeEditor.showBackgroundContextMenu())
            ImGui.openPopup("node_editor_context");

        if (ImGui.beginPopup("node_editor_context")) {
            drawEditorContextPopup();
            ImGui.endPopup();
        }

        NodeEditor.resume();
//        NodeEditor.popStyleColor(1);
        NodeEditor.end();
    }

    private void drawMenuBar() {
        if (ImGui.button("Navigate to content"))
            NodeEditor.navigateToContent(1);

        ImGui.sameLine();
        if (ImGui.button("Save"))
            this.currentGraph.save();

        ImGui.sameLine();
        if (ImGui.button("Load"))
            this.currentGraph = this.currentGraph.load();
    }

    private void drawEditorContextPopup() {
        if (startMousePos == null)
                startMousePos = ImGui.getMousePos();

            if (ImGui.beginMenu("Create Node")) {
                for (String collectionName : this.createNodesList.keySet()) {
                    if (ImGui.beginMenu(collectionName)) {
                        for (GraphNode node : this.createNodesList.get(collectionName)) {
                            if (ImGui.menuItem(node.getName())) {
                                GraphNode createdNode = this.currentGraph.copyCreateGraphNode(node, startMousePos);
                                final float canvasX = NodeEditor.toCanvasX(startMousePos.x);
                                final float canvasY = NodeEditor.toCanvasY(startMousePos.y);
                                NodeEditor.setNodePosition(createdNode.getId(), canvasX, canvasY);
                                startMousePos = null;
                                ImGui.closeCurrentPopup();
                            }
//                            ImGui.sameLine();
//                            if (EditorImGui.beginHelpMarker()) {
//                                node.drawTooltip();
//                                EditorImGui.endHelpMarker();
//                            }
                        }
                        ImGui.endMenu();
                    }
                }
                ImGui.endMenu();
            }
    }

    private void drawNodeContextPopup(int targetNode) {
        if (ImGui.button("Delete " + this.currentGraph.nodes.get(targetNode).getName())) {
            this.currentGraph.deleteNodeById(targetNode);
//            this.currentGraph.nodes.remove(targetNode);
            ImGui.closeCurrentPopup();
        }
    }
}
