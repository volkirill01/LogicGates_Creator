package editor;

import com.google.gson.*;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;

import java.lang.reflect.Type;
import java.util.List;

public class GraphDeserializer implements JsonSerializer<Graph>, JsonDeserializer<Graph> {

    @Override
    public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String filepath = jsonObject.get("filepath").getAsString();
        JsonArray nodes = jsonObject.getAsJsonArray("nodes");

        int lastNodeId = 0;
        int lastPinId = 0;

        Graph graph = new Graph(filepath);
        for (JsonElement element: nodes) {
            GraphNode node = context.deserialize(element, GraphNode.class);
            graph.nodes.put(node.getId(), node);
            NodeEditor.setNodePosition(node.getId(), node.getPosition().x, node.getPosition().y);
            lastNodeId = node.getId();
            lastPinId = node.getOutputPinId(node.outputPins.size() - 1);
        }

        for (GraphNode node : graph.nodes.values()) {
            for (GraphNodePin inputPin : node.inputPins)
                for (int i = 0; i < inputPin.getConnectedPinsIds().size(); i++) {
                    inputPin.addConnectedPin(graph.findOutputPin(inputPin.getConnectedPinsIds().get(i)));
                    i++;
                }
            for (GraphNodePin outputPin : node.outputPins)
                for (int i = 0; i < outputPin.getConnectedPinsIds().size(); i++) {
                    outputPin.addConnectedPin(graph.findInputPin(outputPin.getConnectedPinsIds().get(i)));
                    i++;
                }
        }

        graph.nextNodeId = lastNodeId + 1;
        graph.nextPinId = lastPinId + 1;

        return graph;
    }

    @Override
    public JsonElement serialize(Graph src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("filepath", new JsonPrimitive(src.getFilepath()));

        GraphNode[] nodes = new GraphNode[src.nodes.size()];
        for (int i = 0; i < src.nodes.size(); i++) {
            nodes[i] = (GraphNode) src.nodes.values().toArray()[i];
            nodes[i].setPosition(new ImVec2(NodeEditor.getNodePositionX(nodes[i].getId()), NodeEditor.getNodePositionY(nodes[i].getId())));
        }

        result.add("nodes", context.serialize(nodes, GraphNode[].class));
        return result;
    }
}
