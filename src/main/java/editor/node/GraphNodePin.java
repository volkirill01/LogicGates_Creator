package editor.node;

import java.util.ArrayList;
import java.util.List;

public class GraphNodePin {

    private int id;
    private boolean isInput;
    private String label;

    private boolean value = false;

    private transient List<GraphNodePin> connectedPins = new ArrayList<>();
    private List<Integer> connectedPinsIds = new ArrayList<>();

    public GraphNodePin(boolean isInput, String label) {
        this.isInput = isInput;
        this.label = label;
    }

    public void init(final int pinId) { this.id = pinId; }

    public void update() {
        if (isInput && hasConnections())
            setValue(connectedPins.get(0).getValue());
    }

    public String getLabel() { return this.label; }

    public void setLabel(String label) { this.label = label; }

    public int getId() { return this.id; }

    public List<GraphNodePin> getConnectedPins() { return this.connectedPins; }

    public List<Integer> getConnectedPinsIds() { return this.connectedPinsIds; }

    public void addConnectedPin(GraphNodePin pin) {
        if (this.connectedPins == null)
            this.connectedPins = new ArrayList<>();

        if (!hasConnectedPinId(pin.getId())) {
            this.connectedPins.add(pin);
            this.connectedPinsIds.add(pin.getId());
        }
    }

    private boolean hasConnectedPinId(int id) {
        for (GraphNodePin connectedPin : this.connectedPins)
            if (connectedPin.getId() == id)
                return true;
        return false;
    }

    public GraphNodePin getConnectedPin(int index) { return this.connectedPins.get(index); }

    public void removeConnectedPin(GraphNodePin pin) {
        if (this.connectedPins == null)
            this.connectedPins = new ArrayList<>();

        this.connectedPins.remove(pin);
        for (int i = 0; i < this.connectedPinsIds.size(); i++)
            if (this.connectedPinsIds.get(i) == pin.getId()) {
                this.connectedPinsIds.remove(i);
                break;
            }
    }

    public void setConnectedPin(GraphNodePin pin) {
        if (this.hasConnections()) {
            this.connectedPins.set(0, pin);
            this.connectedPinsIds.set(0, pin.getId());
        } else {
            if (this.connectedPins == null)
                this.connectedPins = new ArrayList<>();

            this.connectedPins.add(pin);
            this.connectedPinsIds.add(pin.getId());
        }
    }

    public boolean hasConnections() {
        if (this.connectedPins != null)
            return this.connectedPins.size() > 0;

        return false;
    }

    public boolean isInput() { return this.isInput; }

    public boolean getValue() { return this.value; }

    public void setValue(boolean value) { this.value = value; }

    public GraphNodePin copy() { return new GraphNodePin(this.isInput, this.label); }

    public void set(boolean isInput) { this.isInput = isInput; }
}
