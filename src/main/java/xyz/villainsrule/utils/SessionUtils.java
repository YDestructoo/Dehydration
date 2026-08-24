package xyz.villainsrule.utils;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

import xyz.villainsrule.TokenLoginMod;

public class SessionUtils {
    public static String getUsername() { return Minecraft.getInstance().getUser().getName(); }
    public static User getSession() { return Minecraft.getInstance().getUser(); }

    @SuppressWarnings("null")
    public static User createSession(String username, String uuidString, String ssid) {
        if (uuidString.length() == 32) uuidString = uuidString.substring(0, 8) + "-" + uuidString.substring(8, 12) + "-" + uuidString.substring(12, 16) + "-" + uuidString.substring(16, 20) + "-" + uuidString.substring(20);
        return new User(username, UUID.fromString(uuidString), ssid, Optional.empty(), Optional.empty());
    }

    @SuppressWarnings("null")
    public static User createSession(String username, UUID uuid, String ssid) { return new User(username, uuid, ssid, Optional.empty(), Optional.empty()); }
    public static void setSession(User session) { TokenLoginMod.currentSession = session; }
    public static void restoreSession() { TokenLoginMod.currentSession = TokenLoginMod.originalSession; }
}
