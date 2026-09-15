package common.protocol;

import common.dto.*;
import java.util.*;

/**
 * Lightweight, zero-dependency JSON utility for serializing and deserializing
 * i-Wish protocol Requests, Responses, and DTOs over TCP Sockets.
 */
public class JsonHelper {

    // ==========================================
    // SERIALIZATION
    // ==========================================

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escapeString((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Enum<?>) return "\"" + ((Enum<?>) obj).name() + "\"";
        
        if (obj instanceof Request) {
            Request req = (Request) obj;
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"action\":").append(req.getAction() != null ? "\"" + req.getAction().name() + "\"" : "null").append(",");
            sb.append("\"userId\":").append(req.getUserId()).append(",");
            sb.append("\"token\":").append(toJson(req.getToken())).append(",");
            sb.append("\"data\":").append(toJson(req.getData()));
            sb.append("}");
            return sb.toString();
        }

        if (obj instanceof Response) {
            Response res = (Response) obj;
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"status\":").append(res.getStatus() != null ? "\"" + res.getStatus().name() + "\"" : "null").append(",");
            sb.append("\"action\":").append(res.getAction() != null ? "\"" + res.getAction().name() + "\"" : "null").append(",");
            sb.append("\"message\":").append(toJson(res.getMessage())).append(",");
            sb.append("\"data\":").append(toJson(res.getData()));
            sb.append("}");
            return sb.toString();
        }

        if (obj instanceof UserDTO) {
            UserDTO u = (UserDTO) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", u.getUserId());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            map.put("fullName", u.getFullName());
            map.put("balance", u.getBalance());
            map.put("isOnline", u.isOnline());
            return toJson(map);
        }

        if (obj instanceof ProductDTO) {
            ProductDTO p = (ProductDTO) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("productId", p.getProductId());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("price", p.getPrice());
            map.put("category", p.getCategory());
            map.put("imageUrl", p.getImageUrl());
            return toJson(map);
        }

        if (obj instanceof WishListItemDTO) {
            WishListItemDTO w = (WishListItemDTO) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("itemId", w.getItemId());
            map.put("userId", w.getUserId());
            map.put("ownerName", w.getOwnerName());
            map.put("product", w.getProduct());
            map.put("status", w.getStatus());
            map.put("paidAmount", w.getPaidAmount());
            map.put("remainingAmount", w.getRemainingAmount());
            map.put("contributions", w.getContributions());
            return toJson(map);
        }

        if (obj instanceof ContributionDTO) {
            ContributionDTO c = (ContributionDTO) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("contributionId", c.getContributionId());
            map.put("itemId", c.getItemId());
            map.put("contributorId", c.getContributorId());
            map.put("contributorName", c.getContributorName());
            map.put("amount", c.getAmount());
            map.put("contributedAt", c.getContributedAt());
            return toJson(map);
        }

        if (obj instanceof FriendRequestDTO) {
            FriendRequestDTO f = (FriendRequestDTO) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("requestId", f.getRequestId());
            map.put("senderId", f.getSenderId());
            map.put("senderUsername", f.getSenderUsername());
            map.put("senderFullName", f.getSenderFullName());
            map.put("receiverId", f.getReceiverId());
            map.put("receiverUsername", f.getReceiverUsername());
            map.put("receiverFullName", f.getReceiverFullName());
            map.put("status", f.getStatus());
            map.put("createdAt", f.getCreatedAt());
            return toJson(map);
        }

        if (obj instanceof NotificationDTO) {
            NotificationDTO n = (NotificationDTO) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("notificationId", n.getNotificationId());
            map.put("userId", n.getUserId());
            map.put("type", n.getType());
            map.put("message", n.getMessage());
            map.put("isRead", n.isRead());
            map.put("createdAt", n.getCreatedAt());
            return toJson(map);
        }

        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeString(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        if (obj instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }

        return "\"" + escapeString(obj.toString()) + "\"";
    }

    private static String escapeString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ==========================================
    // DESERIALIZATION
    // ==========================================

    public static Request parseRequest(String json) {
        Object parsed = parse(json);
        if (!(parsed instanceof Map<?, ?>)) return null;
        Map<?, ?> map = (Map<?, ?>) parsed;

        Request req = new Request();
        String actionStr = (String) map.get("action");
        if (actionStr != null) {
            try { req.setAction(ActionType.valueOf(actionStr)); } catch (Exception ignored) {}
        }
        Object uid = map.get("userId");
        if (uid instanceof Number) req.setUserId(((Number) uid).intValue());
        req.setToken((String) map.get("token"));

        Object dataObj = map.get("data");
        if (dataObj instanceof Map<?, ?>) {
            Map<String, Object> castMap = new HashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) dataObj).entrySet()) {
                castMap.put(String.valueOf(e.getKey()), e.getValue());
            }
            req.setData(castMap);
        }
        return req;
    }

    public static Response parseResponse(String json) {
        Object parsed = parse(json);
        if (!(parsed instanceof Map<?, ?>)) return null;
        Map<?, ?> map = (Map<?, ?>) parsed;

        Response res = new Response();
        String statusStr = (String) map.get("status");
        if (statusStr != null) {
            try { res.setStatus(ResponseStatus.valueOf(statusStr)); } catch (Exception ignored) {}
        }
        String actionStr = (String) map.get("action");
        if (actionStr != null) {
            try { res.setAction(ActionType.valueOf(actionStr)); } catch (Exception ignored) {}
        }
        res.setMessage((String) map.get("message"));

        Object dataObj = map.get("data");
        if (dataObj instanceof Map<?, ?>) {
            Map<String, Object> castMap = new HashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) dataObj).entrySet()) {
                castMap.put(String.valueOf(e.getKey()), e.getValue());
            }
            res.setData(castMap);
        }
        return res;
    }

    /**
     * Recursive Descent JSON Parser
     */
    public static Object parse(String json) {
        if (json == null) return null;
        String trimmed = json.trim();
        if (trimmed.isEmpty()) return null;
        return new JsonParser(trimmed).parseValue();
    }

    private static class JsonParser {
        private final String src;
        private int idx = 0;

        public JsonParser(String src) {
            this.src = src;
        }

        private void skipWhitespace() {
            while (idx < src.length() && Character.isWhitespace(src.charAt(idx))) {
                idx++;
            }
        }

        public Object parseValue() {
            skipWhitespace();
            if (idx >= src.length()) return null;
            char c = src.charAt(idx);

            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();

            throw new RuntimeException("Unexpected token at " + idx + ": " + c);
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            idx++; // consume '{'
            skipWhitespace();
            if (idx < src.length() && src.charAt(idx) == '}') {
                idx++;
                return map;
            }

            while (idx < src.length()) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                if (idx < src.length() && src.charAt(idx) == ':') {
                    idx++;
                }
                Object val = parseValue();
                map.put(key, val);
                skipWhitespace();
                if (idx < src.length() && src.charAt(idx) == ',') {
                    idx++;
                } else if (idx < src.length() && src.charAt(idx) == '}') {
                    idx++;
                    break;
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            idx++; // consume '['
            skipWhitespace();
            if (idx < src.length() && src.charAt(idx) == ']') {
                idx++;
                return list;
            }

            while (idx < src.length()) {
                Object val = parseValue();
                list.add(val);
                skipWhitespace();
                if (idx < src.length() && src.charAt(idx) == ',') {
                    idx++;
                } else if (idx < src.length() && src.charAt(idx) == ']') {
                    idx++;
                    break;
                }
            }
            return list;
        }

        private String parseString() {
            skipWhitespace();
            if (idx >= src.length() || src.charAt(idx) != '"') return "";
            idx++; // skip opening quote
            StringBuilder sb = new StringBuilder();
            while (idx < src.length()) {
                char c = src.charAt(idx++);
                if (c == '"') break;
                if (c == '\\' && idx < src.length()) {
                    char esc = src.charAt(idx++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (idx + 4 <= src.length()) {
                                String hex = src.substring(idx, idx + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                idx += 4;
                            }
                            break;
                        default: sb.append(esc); break;
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", idx)) {
                idx += 4;
                return true;
            }
            if (src.startsWith("false", idx)) {
                idx += 5;
                return false;
            }
            return false;
        }

        private Object parseNull() {
            if (src.startsWith("null", idx)) {
                idx += 4;
            }
            return null;
        }

        private Number parseNumber() {
            int start = idx;
            if (src.charAt(idx) == '-') idx++;
            boolean isDecimal = false;
            while (idx < src.length()) {
                char c = src.charAt(idx);
                if (Character.isDigit(c)) {
                    idx++;
                } else if (c == '.' || c == 'e' || c == 'E') {
                    isDecimal = true;
                    idx++;
                } else {
                    break;
                }
            }
            String numStr = src.substring(start, idx);
            if (isDecimal) return Double.parseDouble(numStr);
            try {
                return Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                return Long.parseLong(numStr);
            }
        }
    }
}
