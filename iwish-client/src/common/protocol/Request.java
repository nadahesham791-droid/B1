package common.protocol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private ActionType action;
    private int userId;
    private String token;
    private Map<String, Object> data = new HashMap<>();

    public Request() {}

    public Request(ActionType action) {
        this.action = action;
    }

    public Request(ActionType action, int userId) {
        this.action = action;
        this.userId = userId;
    }

    public ActionType getAction() { return action; }
    public void setAction(ActionType action) { this.action = action; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public Request put(String key, Object value) {
        if (this.data == null) this.data = new HashMap<>();
        this.data.put(key, value);
        return this;
    }

    public Object get(String key) {
        return data != null ? data.get(key) : null;
    }

    public String getString(String key) {
        Object val = get(key);
        return val != null ? String.valueOf(val) : null;
    }

    public int getInt(String key, int defaultValue) {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val != null) {
            try { return Integer.parseInt(val.toString()); } catch (Exception ignored) {}
        }
        return defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val != null) {
            try { return Double.parseDouble(val.toString()); } catch (Exception ignored) {}
        }
        return defaultValue;
    }
}
