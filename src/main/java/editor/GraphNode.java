package editor;

import imgui.ImVec2;

public abstract class GraphNode {

    private int nodeId;

    private ImVec2 position;

    private int inputPinId;
    private int outputPinId;

    public int outputNodeId = -1;

    public void init(final int nodeId, ImVec2 position, final int inputPinId, final int outputPintId) {
        this.nodeId = nodeId;
        this.position = position;
        this.inputPinId = inputPinId;
        this.outputPinId = outputPintId;
    }

    public abstract void drawNode();

    public abstract GraphNode copy();

    public int getId() { return this.nodeId; }

    public ImVec2 getPosition() { return this.position; }

    public void setPosition(ImVec2 position) { this.position.set(position); }

    public int getInputPinId() { return this.inputPinId; }

    public int getOutputPinId() { return this.outputPinId; }

    public abstract String getName();
}
