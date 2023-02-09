package editor.gates;

import editor.graph.Graph;
import editor.node.GraphNode;
import editor.node.GraphNodePin;
import imgui.ImVec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphNode_Graph extends GraphNode {

    private Graph gate;

    private transient Map<Integer, GraphNodePin> tmpMap = new HashMap<>();

    public void init(int nodeId, Graph gate, ImVec2 position) {
        super.init(nodeId, position);
        this.gate = gate;

        this.getNodeColor().set(this.gate.getGateColor().x, this.gate.getGateColor().y, this.gate.getGateColor().z);

        for (GraphNodePin pin : this.gate.findById(1).outputPins) {
            pin.setValue(false);
            GraphNodePin newPin = new GraphNodePin(true, pin.getLabel());

            this.inputPins.add(newPin);

            this.tmpMap.put(pin.getId(), newPin);
        }
        for (GraphNodePin pin : this.gate.findById(2).inputPins) {
            pin.setValue(false);
            GraphNodePin newPin = new GraphNodePin(false, pin.getLabel());

            this.outputPins.add(newPin);

            this.tmpMap.put(pin.getId(), newPin);
        }

        this.pinGroups = null;
    }

    private void initGroups() {
        this.pinGroups = new HashMap<>();
        Map<String, List<Integer>> tmpGroups = this.gate.findById(1).getGroups();
        tmpGroups.putAll(this.gate.findById(2).getGroups());

        for (String groupName : tmpGroups.keySet()) {
            List<GraphNodePin> pins = new ArrayList<>();
            for (int id : tmpGroups.get(groupName))
                pins.add(tmpMap.get(id));

            addGroup(groupName, pins);
        }
        this.tmpMap = null;
    }

    @Override
    public void update() {
        if (this.pinGroups == null)
            initGroups();

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
    public GraphNode_Graph copy() { return new GraphNode_Graph(); }
}
