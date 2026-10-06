package dev.pawtism.client.discord;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/** Discord RPC framing. Uses Minecraft's existing JSON library. */
final class DiscordProtocol {
    static final int HANDSHAKE = 0, FRAME = 1, CLOSE = 2, PING = 3, PONG = 4;
    static final int MAX_PAYLOAD = 64 * 1024;
    record Frame(int opcode, byte[] payload) {
        Frame {
            if (opcode < HANDSHAKE || opcode > PONG) throw new IllegalArgumentException("Invalid IPC opcode");
            if (payload == null || payload.length > MAX_PAYLOAD) throw new IllegalArgumentException("IPC payload is too large");
            payload = payload.clone();
        }
        @Override public byte[] payload() { return payload.clone(); }
    }

    private DiscordProtocol() {}

    static int payloadLength(byte[] header) throws IOException {
        if (header.length != 8) throw new EOFException("Incomplete IPC header");
        ByteBuffer bytes = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        int opcode = bytes.getInt(), size = bytes.getInt();
        if (opcode < HANDSHAKE || opcode > PONG) throw new IOException("Invalid IPC opcode");
        if (size < 0 || size > MAX_PAYLOAD) throw new IOException("Invalid IPC payload length");
        return size;
    }

    static Frame fromHeader(byte[] header, byte[] payload) throws IOException {
        if (payloadLength(header) != payload.length) throw new EOFException("Incomplete IPC payload");
        return new Frame(ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN).getInt(), payload);
    }

    static byte[] encode(Frame frame) {
        byte[] payload = frame.payload;
        return ByteBuffer.allocate(8 + payload.length).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(frame.opcode).putInt(payload.length).put(payload).array();
    }

    static Frame json(int opcode, JsonObject payload) {
        return new Frame(opcode, payload.toString().getBytes(StandardCharsets.UTF_8));
    }

    static JsonObject json(Frame frame) throws IOException {
        try {
            String text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(frame.payload)).toString();
            JsonElement value = JsonParser.parseString(text);
            if (!value.isJsonObject()) throw new IOException("IPC payload must be a JSON object");
            return value.getAsJsonObject();
        } catch (CharacterCodingException | com.google.gson.JsonParseException | IllegalStateException invalid) {
            throw new IOException("Invalid IPC JSON payload", invalid);
        }
    }

    static Frame handshake(String id) {
        if (!PresenceData.validApplicationId(id)) throw new IllegalArgumentException("Invalid application ID");
        JsonObject message = new JsonObject();
        message.addProperty("v", 1); message.addProperty("client_id", id);
        return json(HANDSHAKE, message);
    }

    static Frame activity(long pid, long startSeconds, PresenceData.ScreenState state, String nonce) {
        JsonObject activity = new JsonObject();
        activity.addProperty("type", 0);
        activity.addProperty("details", "Pawtism Client • Minecraft 26.2");
        activity.addProperty("state", state.label());
        JsonObject timestamps = new JsonObject(); timestamps.addProperty("start", startSeconds);
        activity.add("timestamps", timestamps);
        return command(pid, activity, nonce);
    }

    static Frame clear(long pid, String nonce) { return command(pid, null, nonce); }

    private static Frame command(long pid, JsonObject activity, String nonce) {
        JsonObject args = new JsonObject(); args.addProperty("pid", pid); args.add("activity", activity);
        JsonObject message = new JsonObject(); message.addProperty("cmd", "SET_ACTIVITY");
        message.add("args", args); message.addProperty("nonce", nonce);
        return json(FRAME, message);
    }

    static boolean ready(JsonObject message) {
        return "DISPATCH".equals(string(message, "cmd")) && "READY".equals(string(message, "evt"));
    }

    static boolean acknowledged(JsonObject message, String nonce) {
        JsonElement event = message.get("evt");
        return nonce != null && "SET_ACTIVITY".equals(string(message, "cmd"))
            && nonce.equals(string(message, "nonce")) && (event == null || event.isJsonNull());
    }

    static boolean error(JsonObject message) { return "ERROR".equals(string(message, "evt")); }

    static int errorCode(JsonObject message) {
        try {
            JsonObject data = message.getAsJsonObject("data");
            return data != null && data.has("code") ? data.get("code").getAsInt() : 0;
        } catch (RuntimeException invalid) { return 0; }
    }

    static String string(JsonObject message, String name) {
        JsonElement value = message.get(name);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
            ? value.getAsString() : "";
    }
}
