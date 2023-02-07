package editor.graph;

import com.google.gson.*;
import editor.node.GraphNode;
import editor.node.GraphNodePin;
import editor.gates.GraphNode_Input;
import editor.gates.GraphNode_Output;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.text.NumberFormat;

public class GraphDeserializer implements JsonSerializer<Graph>, JsonDeserializer<Graph> {

    @Override
    public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String filepath = jsonObject.get("filepath").getAsString();
        String gateName = jsonObject.get("gateName").getAsString();
        String gateColorStr = jsonObject.get("gateColor").getAsString().replace(",", ".").replace("(", "").replace(")", "");
        Vector3f gateColor = new Vector3f(
                Float.parseFloat(gateColorStr.split(" ")[0]),
                Float.parseFloat(gateColorStr.split(" ")[1]),
                Float.parseFloat(gateColorStr.split(" ")[2])
        );
        int nextNodeId = jsonObject.get("nextNodeId").getAsInt();
        int nextPinId = jsonObject.get("nextPinId").getAsInt();
        JsonArray nodes = jsonObject.getAsJsonArray("nodes");

        Graph graph = new Graph(filepath, gateName);
        graph.getGateColor().set(gateColor);
        graph.nextNodeId = nextNodeId;
        graph.nextPinId = nextPinId;

        for (JsonElement element: nodes) {
            GraphNode node = context.deserialize(element, GraphNode.class);
            graph.nodes.put(node.getId(), node);
            if (node.getId() == 1)
                graph.setInputNode((GraphNode_Input) node);
            if (node.getId() == 2)
                graph.setOutputNode((GraphNode_Output) node);
            if (!filepath.endsWith(".gate"))
                NodeEditor.setNodePosition(node.getId(), node.getPosition().x, node.getPosition().y);
        }

        for (GraphNode node : graph.nodes.values()) {
            for (GraphNodePin inputPin : node.inputPins)
                for (int i = 0; i < inputPin.getConnectedPinsIds().size(); i++) {
                    GraphNodePin connectedPin = graph.findOutputPin(inputPin.getConnectedPinsIds().get(i));
                    if (connectedPin != null) {
                        inputPin.addConnectedPin(connectedPin);
                        i++;
                    }
                }
            for (GraphNodePin outputPin : node.outputPins)
                for (int i = 0; i < outputPin.getConnectedPinsIds().size(); i++) {
                    GraphNodePin connectedPin = graph.findInputPin(outputPin.getConnectedPinsIds().get(i));
                    if (connectedPin != null) {
                        outputPin.addConnectedPin(connectedPin);
                        i++;
                    }
                }
        }

        return graph;
    }

    @Override
    public JsonElement serialize(Graph src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("filepath", new JsonPrimitive(src.getFilepath()));
        result.add("gateName", new JsonPrimitive(src.getGateName()));
        result.add("gateColor", new JsonPrimitive(src.getGateColor().toString(NumberFormat.getNumberInstance())));
        result.add("nextNodeId", new JsonPrimitive(src.nextNodeId));
        result.add("nextPinId", new JsonPrimitive(src.nextPinId));

        GraphNode[] nodes = new GraphNode[src.nodes.size()];
        for (int i = 0; i < src.nodes.size(); i++) {
            nodes[i] = (GraphNode) src.nodes.values().toArray()[i];
            nodes[i].setPosition(new ImVec2(NodeEditor.getNodePositionX(nodes[i].getId()), NodeEditor.getNodePositionY(nodes[i].getId())));
        }

        result.add("nodes", context.serialize(nodes, GraphNode[].class));
        return result;
    }
}
