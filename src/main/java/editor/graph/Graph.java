package editor.graph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import editor.gates.Gate_7SegmentDisplay;
import editor.node.GraphNode;
import editor.node.GraphNodeDeserializer;
import editor.node.GraphNodePin;
import editor.gates.GraphNode_Graph;
import editor.gates.GraphNode_Input;
import editor.gates.GraphNode_Output;
import imgui.ImVec2;
import org.joml.Vector3f;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class Graph {

    private String filepath;
    private String gateName;
    private Vector3f gateColor = new Vector3f(43.0f, 45.0f, 52.0f);

    public int nextNodeId = 1;
    public int nextPinId = 1000;

    public Map<Integer, GraphNode> nodes = new HashMap<>();

    private GraphNode_Input inputNode;
    private GraphNode_Output outputNode;

    private Gate_7SegmentDisplay display = null;

    public Graph(String filepath, String gateName) {
        this.filepath = filepath;
        this.gateName = gateName;
    }

    public void createInputAndOutput() {
        this.inputNode = new GraphNode_Input();
        this.inputNode.init(nextNodeId++, new ImVec2(0.0f, 0.0f));
        this.nextPinId = this.inputNode.initPins(nextPinId++);
        this.nodes.put(inputNode.getId(), inputNode);

        this.outputNode = new GraphNode_Output();
        this.outputNode.init(nextNodeId++, new ImVec2(0.0f, 0.0f));
        this.nextPinId = this.outputNode.initPins(nextPinId++);
        this.nodes.put(outputNode.getId(), outputNode);
    }

    public void save() { saveAs(this.filepath); }

    public void saveAs(String filepath) {
        try {
            FileWriter writer = new FileWriter(filepath);
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(Graph.class, new GraphDeserializer())
                    .registerTypeAdapter(GraphNode.class, new GraphNodeDeserializer())
                    .create();

            this.filepath = filepath;
            String json = gson.toJson(this);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Graph load(String filepath) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Graph.class, new GraphDeserializer())
                .registerTypeAdapter(GraphNode.class, new GraphNodeDeserializer())
                .create();

        String inFile = "";
        try {
            inFile = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!inFile.equals("")) {
            Graph graph = gson.fromJson(inFile, Graph.class);
            return graph;
        }
        return null;
    }

    public void saveAsGate(String gateName) {
        this.gateName = gateName;
        saveAs("projects/sample/gates/" + gateName + ".gate");
    }

    public GraphNode copyCreateGraphNode(GraphNode node, ImVec2 position) {
        GraphNode copy = node.copy();
        copy.init(nextNodeId++, position);
        this.nextPinId = copy.initPins(nextPinId++);
        this.nodes.put(copy.getId(), copy);
        return copy;
    }

    public GraphNode_Graph loadCreateGate(Graph gate, ImVec2 position) {
        GraphNode_Graph node = new GraphNode_Graph();
        if (gate.hasDisplay())
            node.setDisplay(gate.getDisplay());
        node.init(nextNodeId++, gate, position);
        this.nextPinId = node.initPins(nextPinId++);
        this.nodes.put(node.getId(), node);
        return node;
    }

    public GraphNode findById(int id) {
        if (nodes.containsKey(id))
            return nodes.get(id);

        return null;
    }

    public GraphNode findByInput(long inputPinId) {
        for (GraphNode node : nodes.values())
            for (int pinIndex = 0; pinIndex < node.inputPins.size(); pinIndex++)
                if (node.getInputPinId(pinIndex) == inputPinId)
                    return node;

        return null;
    }

    public GraphNode findByOutput(long outputPinId) {
        for (GraphNode node : nodes.values())
            for (int pinIndex = 0; pinIndex < node.outputPins.size(); pinIndex++)
                if (node.getOutputPinId(pinIndex) == outputPinId)
                    return node;

        return null;
    }

    public GraphNode findByPin(int pinId) {
        for (GraphNode node : nodes.values())
            for (GraphNodePin pin : node.inputPins)
                if (pin.getId() == pinId)
                    return node;

        for (GraphNode node : nodes.values())
            for (GraphNodePin pin : node.outputPins)
                if (pin.getId() == pinId)
                    return node;

        return null;
    }

    public GraphNodePin findInputPin(long inputPinId) {
        for (GraphNode node : nodes.values())
            for (GraphNodePin pin : node.inputPins)
                if (pin.getId() == inputPinId)
                    return pin;

        return null;
    }

    public GraphNodePin findOutputPin(long outputPinId) {
        for (GraphNode node : nodes.values())
            for (GraphNodePin pin : node.outputPins)
                if (pin.getId() == outputPinId)
                    return pin;

        return null;
    }

    public void deleteNodeById(int nodeId) {
        GraphNode node = nodes.get(nodeId);

        if (node != null) {
            for (GraphNodePin inputPin : node.inputPins)
                if (inputPin.getConnectedPins() != null)
                    for (GraphNodePin connectedPin : inputPin.getConnectedPins())
                        connectedPin.removeConnectedPin(inputPin); // Clear output pins of connected nodes

            for (GraphNodePin outputPin : node.outputPins) {
                if (outputPin.getConnectedPins() != null)
                    for (GraphNodePin connectedPin : outputPin.getConnectedPins()) {
                        connectedPin.setValue(false);
                        connectedPin.removeConnectedPin(outputPin); // Clear input pins of connected nodes
                    }
            }

            nodes.remove(nodeId);
        }
    }

    public int getNextNodeId()  { return nextNodeId++; }

    public int getNextPinId() { return nextPinId++; }

    public String getFilepath() { return this.filepath; }

    public String getGateName() { return this.gateName; }

    public Vector3f getGateColor() { return this.gateColor; }

    public GraphNode getInputNode() { return this.inputNode; }

    public void setInputNode(GraphNode_Input node) { this.inputNode = node; }

    public GraphNode getOutputNode() { return this.outputNode; }

    public void setOutputNode(GraphNode_Output node) { this.outputNode = node; }

    public boolean hasDisplay() { return this.display != null; }

    public void setDisplay(Gate_7SegmentDisplay display) { this.display = display; }

    public Gate_7SegmentDisplay getDisplay() { return this.display; }
}
