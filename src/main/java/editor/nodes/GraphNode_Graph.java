package editor.nodes;

import editor.Graph;
import editor.GraphNode;
import editor.GraphNodePin;
import imgui.ImVec2;
import imgui.internal.ImGui;
import imgui.type.ImBoolean;

public class GraphNode_Graph extends GraphNode {

    private Graph gate;

    public void init(int nodeId, Graph gate, ImVec2 position) {
        super.init(nodeId, position);
        this.gate = gate;

        for (GraphNodePin pin : this.gate.findById(1).outputPins)
            this.inputPins.add(new GraphNodePin(true, pin.getLabel(), pin.getDescription()));
        for (GraphNodePin pin : this.gate.findById(2).inputPins)
            this.outputPins.add(new GraphNodePin(false, pin.getLabel(), pin.getDescription()));

//        this.inputPins.addAll(this.gate.findById(1).outputPins);
//        this.outputPins.addAll(this.gate.findById(2).inputPins);

//        for (GraphNodePin pin : this.inputPins)
//            pin.set(true);
//        for (GraphNodePin pin : this.outputPins)
//            pin.set(false);
    }

    @Override
    public void update() {
        for (GraphNode node : this.gate.nodes.values()) {
            if (node.getName().equals("Input")) {
//                if (node.outputPins.get(0).hasConnections())
//                    System.out.println(node.outputPins.get(0).getConnectedPin(0).getValue());

//                System.out.println("Input (" + node.outputPins.get(0).getId() + ") - " + node.outputPins.get(0).getValue());
//
//                if (node.outputPins.get(0).hasConnections())
//                    System.out.println("Input InConnected(" + node.outputPins.get(0).getConnectedPin(0).getId() + ") - " + node.outputPins.get(0).getConnectedPin(0).getValue());
//                if (this.outputPins.size() > 0)
//                    if (node.outputPins.size() > 0)
                for (int i = 0; i < this.inputPins.size(); i++) {
                    node.outputPins.get(i).setValue(this.inputPins.get(i).getValue());
                }
//                System.out.println(node.inputPins);
//                if (node.outputPins.size() > 0)
//                    System.out.println("Input " + node.getId() + " " + node.outputPins.get(0).getValue() + ", " + node.outputPins.get(1).getValue());
            }
            if (node.getName().equals("Output")) {
//                System.out.println("Output (" + node.inputPins.get(0).getId() + ") - " + node.inputPins.get(0).getValue());

//                System.out.println(node.inputPins.get(0).getValue() + " | " + node.inputPins.get(1).getValue());
//                if (this.inputPins.size() > 0)
//                    if (node.inputPins.size() > 0)

                for (int i = 0; i < this.outputPins.size(); i++) {
                    this.outputPins.get(i).setValue(node.inputPins.get(i).getValue());
                }
////                System.out.println(node.inputPins);
//                if (node.inputPins.size() > 0)
//                    System.out.println("Output " + node.getId() + " " + node.inputPins.get(0).getValue() + ", " + node.inputPins.get(1).getValue());
            }
//            if (node.getName().equals("Not")) {
////                if (node.inputPins.get(0).hasConnections())
////                    System.out.println(node.inputPins.get(0).getConnectedPin(0).getValue());
//
//                if (node.inputPins.get(0).hasConnections())
//                    System.out.println("Not InConnected(" + node.inputPins.get(0).getConnectedPin(0).getId() + ") - " + node.inputPins.get(0).getConnectedPin(0).getValue());
//                if (node.outputPins.get(0).hasConnections())
//                    System.out.println("Not OutConnected(" + node.outputPins.get(0).getConnectedPin(0).getId() + ") - " + node.outputPins.get(0).getConnectedPin(0).getValue());
//                System.out.println("Not In(" + node.inputPins.get(0).getValue() + ") - " + node.inputPins.get(0).getValue());
//                System.out.println("Not Out(" + node.outputPins.get(0).getValue() + ") - " + node.outputPins.get(0).getValue());
////                System.out.println("Not " + node.getId() + " " + node.inputPins.get(0).getValue() + " | " + node.outputPins.get(0).getValue());
//            }
//            if (node.getName().equals("And")) {
////                if (node.inputPins.get(0).hasConnections())
////                    System.out.println(node.inputPins.get(0).getConnectedPin(0).getValue());
//
//                if (node.inputPins.get(0).hasConnections())
//                    System.out.println(node.inputPins.get(0).getConnectedPin(0).getLabel());
//                System.out.println("And In1(" + node.inputPins.get(0).getId() + ") - " + node.inputPins.get(0).getValue());
//
//                if (node.inputPins.get(1).hasConnections())
//                    System.out.println(node.inputPins.get(1).getConnectedPin(0).getLabel());
//                System.out.println("And In2(" + node.inputPins.get(1).getId() + ") - " + node.inputPins.get(1).getValue());
//
//                System.out.println("And Out(" + node.outputPins.get(0).getId() + ") - " + node.outputPins.get(0).getValue());
////                System.out.println("Not " + node.getId() + " " + node.inputPins.get(0).getValue() + " | " + node.outputPins.get(0).getValue());
//            }

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
