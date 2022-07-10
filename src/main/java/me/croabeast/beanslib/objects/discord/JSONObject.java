package me.croabeast.beanslib.objects.discord;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The object that handles JSON messages.
 * Only accessible inside the package.
 * @author Kihsomray
 * @forkBy CroaBeast
 * @since 1.1
 */
class JSONObject {

    /**
     * The map to store all the values.
     */
    private final HashMap<String, Object> map = new HashMap<>();

    /**
     * Adds an object in the {@link #map} of the object.
     * @param key a key for the object
     * @param value the object
     * @return the JSON object instance
     */
    JSONObject put(String key, Object value) {
        if (value != null) map.put(key, value);
        return this;
    }

    /**
     * Converts all the stored values of the {@link #map} to it string format.
     * @return the converted string format
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Set<Map.Entry<String, Object>> entrySet = map.entrySet();

        builder.append("{");
        int i = 0;

        for (Map.Entry<String, Object> entry : entrySet) {
            Object val = entry.getValue();
            builder.append(quote(entry.getKey())).append(":");

            if (val instanceof Boolean || val instanceof JSONObject)
                builder.append(val);
            else if (val instanceof String)
                builder.append(quote(String.valueOf(val)));
            else if (val instanceof Integer)
                builder.append(Integer.valueOf(String.valueOf(val)));
            else if (val.getClass().isArray()) {
                builder.append("[");
                int len = Array.getLength(val);
                for (int j = 0; j < len; j++) {
                    String temp = Array.get(val, j).toString();
                    builder.append(temp).append(j != len - 1 ? "," : "");
                }
                builder.append("]");
            }

            builder.append(++i == entrySet.size() ? "}" : ",");
        }
        return builder.toString();
    }

    private String quote(String string) {
        return "\"" + string + "\"";
    }
}
