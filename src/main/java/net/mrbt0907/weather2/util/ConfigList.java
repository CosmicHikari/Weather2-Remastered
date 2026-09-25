package net.mrbt0907.weather2.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigList {
    private final Map<String, Object[]> map = new LinkedHashMap<String, Object[]>();
    private boolean replace = false;

    public ConfigList() {
    }

    public ConfigList(String... entries) {
        for (String entry : entries)
            parse(entry);
    }

    public ConfigList addAll(ConfigList list) {
        if (list != null)
            map.putAll(list.toMap());
        return this;
    }

    /**
     * Adds an entry with specified values. Values are optional.
     */
    public ConfigList add(String id, Object... values) {
        if (map.containsKey(id)) {
            Object[] oldTable = map.get(id), table = null;
            int sizeA = oldTable.length, sizeB;

            if (replace)
                table = values;
            else {
                sizeB = sizeA + values.length;
                table = new Object[sizeB];

                for (int i = 0; i < sizeB; i++) {
                    if (i < sizeA)
                        table[i] = oldTable[i];
                    else
                        table[i] = values[i - sizeA];
                }
            }

            map.put(id, table);
        } else
            map.put(id, values);
        return this;
    }

    /**
     * Removes an entry from this list.
     */
    public ConfigList remove(String... ids) {
        for (String id : ids) {
            if (!id.contains(":"))
                id = "minecraft:" + id;
            map.remove(id);
        }

        return this;
    }

    /**
     * Returns a table of values from the specified id.
     */
    public Object get(String id) {
        return get(id, 0);
    }

    /**
     * Returns a table of values from the specified id.
     */
    public Object get(String id, int index) {
        Object[] a = map.get(id);
        return a != null && a.length > index ? a[index] : null;
    }

    /**
     * Returns a table of values from the specified id.
     */
    public Object[] getValues(String id) {
        return map.get(id);
    }

    /**
     * Returns true if the id is in this list.
     */
    public boolean exists(String id) {
        return map.containsKey(id);
    }

    /**
     * Clears the list from all inputed values
     */
    public void clear() {
        map.clear();
    }

    public Map<String, Object[]> toMap() {
        return map;
    }

    public int size() {
        return map.size();
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object[] value) {
        return map.containsValue(value);
    }

    public boolean isReplaceOnly() {
        return replace;
    }

    public ConfigList setReplaceOnly() {
        replace = true;
        return this;
    }

    /**
     * Parses a given string to add and remove entries from this list. Format goes as follows:
     * <br>- <b>"!"</b>  Remove this id from the list.
     * <br>- <b>":"</b>  Modid prefix. If no modid is present and partialMatches is set to true, will grab all entires that contain this name, otherwise will default to "minecraft:".
     * <br>- <b>"="</b>  Adds a replacement block to the block in front. Can be stacked any amount of times. Unused if id is 0 or 2.
     * <br>- <b>"#"</b>  Metadata of the block.
     * <br>- <b>","</b> and/or <b>" "</b>  Separates each entry in the string.
     */
    public void parse(String str) {
        String[] entries = str.split("[\\s\\,\\;\\\\]+");

        for (String entry : entries) {
            String id = "";
            List<Object> values = new ArrayList<Object>();

            String[] items = entry.split("\\=");
            int size = items.length;

            for (int i = 0; i < size; i++) {
                if (i == 0) {
                    id = items[i];

                    if (id.contains("!")) {
                        remove(id.replaceAll("\\!", ""));
                        id = "";
                        break;
                    }
                } else {
                    values.add(items[i]);
                    remove(id.replaceAll("\\!", "").replaceAll("\\#.*", ""));
                }
            }

            if (id != "")
                add(id, values.toArray());
        }
    }
}