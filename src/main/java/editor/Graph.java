package editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import imgui.ImVec2;
import org.joml.Vector2f;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class Graph {

    private final String filepath;

    public int nextNodeId = 1;
    public int nextPinId = 1000;

    public final Map<Integer, GraphNode> nodes = new HashMap<>();

    public Graph(String filepath) { this.filepath = filepath; }

    public void save() {
        try {
            FileWriter writer = new FileWriter(this.filepath);
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(Graph.class, new GraphDeserializer())
                    .registerTypeAdapter(GraphNode.class, new GraphNodeDeserializer())
                    .create();

            String json = gson.toJson(this);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Graph load() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Graph.class, new GraphDeserializer())
                .registerTypeAdapter(GraphNode.class, new GraphNodeDeserializer())
                .create();

        String inFile = "";
        try {
            inFile = new String(Files.readAllBytes(Paths.get(this.filepath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!inFile.equals("")) {
            Graph graph = gson.fromJson(inFile, Graph.class);
            return graph;
        }
        return null;
    }

    public GraphNode copyCreateGraphNode(GraphNode node, ImVec2 position) {
        GraphNode copy = node.copy();
        copy.init(nextNodeId++, position, nextPinId++, nextPinId++);
        this.nodes.put(copy.getId(), copy);
        return copy;
    }

    public GraphNode findById(int id) {
        if (nodes.containsKey(id))
            return nodes.get(id);

        return null;
    }

    public GraphNode findByInput(final long inputPinId) {
        for (GraphNode node : nodes.values()) {
            if (node.getInputPinId() == inputPinId) {
                return node;
            }
        }
        return null;
    }

    public GraphNode findByOutput(final long outputPinId) {
        for (GraphNode node : nodes.values()) {
            if (node.getOutputPinId() == outputPinId) {
                return node;
            }
        }
        return null;
    }

    public String getFilepath() { return this.filepath; }
}
