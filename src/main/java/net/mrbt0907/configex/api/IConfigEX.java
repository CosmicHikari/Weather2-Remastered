package net.mrbt0907.configex.api;

public interface IConfigEX {

    String getName();

    String getDescription();

    default String getSaveLocation() {
        return getName();
    }

    void onConfigChanged(Phase phase, int variables);

    void onValueChanged(String variable, Object oldValue, Object newValue);

    enum Phase {
        START, END
    }
}
