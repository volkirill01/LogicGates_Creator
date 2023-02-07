package editor.node;

import java.util.ArrayList;
import java.util.List;

public class GraphNodePin {

    private int id;
    private boolean isInput;
    private String label;
    private String description;
    private boolean value = false;
    private transient List<GraphNodePin> connectedPins = new ArrayList<>();
    private List<Integer> connectedPinsIds = new ArrayList<>();

    public GraphNodePin(boolean isInput, String label, String description) {
        this.isInput = isInput;
        this.label = label;
        this.description = description;
    }

    public void init(final int pinId) { this.id = pinId; }

    public void update() {
        if (isInput && hasConnections())
            setValue(connectedPins.get(0).getValue());
    }

    public String getLabel() { return this.label; }

    public void setLabel(String label) { this.label = label; }

    public String getDescription() { return this.description; }

    public int getId() { return this.id; }

    public List<GraphNodePin> getConnectedPins() { return this.connectedPins; }

    public List<Integer> getConnectedPinsIds() { return this.connectedPinsIds; }

    public void addConnectedPin(GraphNodePin pin) {
        if (this.connectedPins == null)
            this.connectedPins = new ArrayList<>();

        if (!this.connectedPins.contains(pin)) {
            this.connectedPins.add(pin);
            this.connectedPinsIds.add(pin.getId());
        }
    }

    public GraphNodePin getConnectedPin(int index) { return this.connectedPins.get(index); }

    public void removeConnectedPin(GraphNodePin pin) { this.connectedPins.remove(pin); }

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

    public GraphNodePin copy() { return new GraphNodePin(this.isInput, this.label, this.description); }

    public void set(boolean isInput) { this.isInput = isInput; }
}
