package xyz.villainsrule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.User;

import net.fabricmc.api.ModInitializer;

import xyz.villainsrule.utils.SessionUtils;

public class TokenLoginMod implements ModInitializer {
    public static final String MOD_ID = "tklogin";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static User originalSession;
    public static User currentSession;
    public static boolean overrideSession = false;

    @Override
    public void onInitialize() {
        originalSession = SessionUtils.getSession();
        currentSession = originalSession;
        overrideSession = true;
    }
}
