package common.protocol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private ResponseStatus status;
    private ActionType action;
    private String message;
    private Map<String, Object> data = new HashMap<>();

    public Response() {}

    public Response(ResponseStatus status, ActionType action, String message) {
        this.status = status;
        this.action = action;
        this.message = message;
    }

    public static Response ok(ActionType action, String message) {
        return new Response(ResponseStatus.SUCCESS, action, message);
    }

    public static Response error(ActionType action, String message) {
        return new Response(ResponseStatus.ERROR, action, message);
    }

    public static Response pushEvent(String message) {
        return new Response(ResponseStatus.PUSH_EVENT, ActionType.PUSH_NOTIFICATION, message);
    }

    public ResponseStatus getStatus() { return status; }
    public void setStatus(ResponseStatus status) { this.status = status; }

    public ActionType getAction() { return action; }
    public void setAction(ActionType action) { this.action = action; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public Response put(String key, Object value) {
        if (this.data == null) this.data = new HashMap<>();
        this.data.put(key, value);
        return this;
    }

    public Object get(String key) {
        return data != null ? data.get(key) : null;
    }

    public boolean isSuccess() {
        return ResponseStatus.SUCCESS.equals(status);
    }
}
