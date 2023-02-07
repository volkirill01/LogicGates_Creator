package editor.gates;

import editor.graph.Graph;
import editor.node.GraphNode;
import editor.node.GraphNodePin;
import imgui.ImVec2;

public class GraphNode_Graph extends GraphNode {

    private Graph gate;

    public void init(int nodeId, Graph gate, ImVec2 position) {
        super.init(nodeId, position);
        this.gate = gate;

        this.getNodeColor().set(this.gate.getGateColor().x, this.gate.getGateColor().y, this.gate.getGateColor().z);

        for (GraphNodePin pin : this.gate.findById(1).outputPins) {
            pin.setValue(false);
            this.inputPins.add(new GraphNodePin(true, pin.getLabel()));
        }
        for (GraphNodePin pin : this.gate.findById(2).inputPins) {
            pin.setValue(false);
            this.outputPins.add(new GraphNodePin(false, pin.getLabel()));
        }
    }

    @Override
    public void update() {
        for (GraphNode node : this.gate.nodes.values()) {
            if (node.getName().equals("Input"))
                for (int i = 0; i < this.inputPins.size(); i++)
                    node.outputPins.get(i).setValue(this.inputPins.get(i).getValue());
            else if (node.getName().equals("Output"))
                for (int i = 0; i < this.outputPins.size(); i++)
                    this.outputPins.get(i).setValue(node.inputPins.get(i).getValue());

            node.updatePins();
            node.update();
        }
    }

    @Override
    public void drawNode() { }

    @Override
    public String getName() { return this.gate.getGateName(); }

    @Override
    public String getDescription() { return "Description"; }

    @Override
    public GraphNode copy() { return new GraphNode_Graph(); }
}
