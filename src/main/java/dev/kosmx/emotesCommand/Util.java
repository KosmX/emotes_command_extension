package dev.kosmx.emotesCommand;

import com.google.gson.JsonElement;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class Util {
    public static String textToString(Object text) {
        if (text == null) return "";
        if (text instanceof JsonElement json) {
            return fromJson(json.toString());
        }
        if (text instanceof String) {
            try {
                return fromJson((String) text);
            } catch(Exception ignore){}
            return (String) text;
        }
        return "";
    }

    private static String fromJson(String str) {
        BaseComponent[] components = ComponentSerializer.parse(str);
        return Arrays.stream(components).map(baseComponent -> baseComponent.toPlainText()).collect(Collectors.joining("-"));
    }
}
