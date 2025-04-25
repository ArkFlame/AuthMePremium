package com.arkflame.authmepremium.callbacks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import com.arkflame.authmepremium.AuthMePremiumPlugin;
import com.arkflame.authmepremium.events.PremiumPreLoginEvent;
import com.arkflame.authmepremium.utils.HandlerReflectionUtil;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

/**
 * This class implements the Callback interface for handling authentication
 * callbacks.
 * It performs actions based on the result of the authentication request, such
 * as updating the handler
 * with login information, invoking the finish method, and firing a
 * PremiumLoginEvent for premium users.
 */
public class AuthCallback implements Callback<String> {
    private InitialHandler oldHandler;
    private BungeeCord bungee;

    public AuthCallback(InitialHandler oldHandler, BungeeCord bungee) {
        this.oldHandler = oldHandler;
        this.bungee = bungee;
    }

    @Override
    public void done(String result, Throwable error) {
        if (error == null) {
            try {
                handleAuthSuccess(result);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException
                    | SecurityException | NoSuchFieldException e) {
                e.printStackTrace();
                oldHandler.disconnect("Server-side error");
            }
        } else {
            handleAuthFailure(error);
        }
    }

    private void handleAuthSuccess(String result) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        LoginResult obj = LoginResult.GSON.fromJson(result, LoginResult.class);
        if (obj != null && obj.getId() != null) {
            updateHandlerWithLoginResult(obj);
            invokeFinishMethod();
            AuthMePremiumPlugin.getDataProvider().setPremium(oldHandler.getName(), true);
            bungee.getPluginManager().callEvent(new PremiumPreLoginEvent(oldHandler));
        } else {
            oldHandler.disconnect("Use another account");
        }
    }

    private void handleAuthFailure(Throwable error) {
        oldHandler.disconnect("Mojang failed");
        bungee.getLogger().log(Level.SEVERE, "Error authenticating " + oldHandler.getName() + " with minecraft.net",
                error);
    }

    private void updateHandlerWithLoginResult(LoginResult obj) throws NoSuchFieldException, IllegalAccessException {
        // Only update if premium uuids are enabled
        // Otherwise, BungeeCord will set the offline uuid
        boolean alwaysOffline = AuthMePremiumPlugin.getConfig().getBoolean("always-offline");
        if (!alwaysOffline) {
            Boolean premiumUUID = AuthMePremiumPlugin.getDataProvider().getPremiumUUID(oldHandler.getName());
            if (premiumUUID == null || premiumUUID) {
                HandlerReflectionUtil.setFieldValue(oldHandler, "uniqueId", Util.getUUID(obj.getId()));
            }
        }

        HandlerReflectionUtil.setFieldValue(oldHandler, "loginProfile", obj);
        HandlerReflectionUtil.setFieldValue(oldHandler, "name", obj.getName());
    }

    private void invokeFinishMethod()
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Method finishMethod = InitialHandler.class.getDeclaredMethod("finish");
        finishMethod.setAccessible(true);
        finishMethod.invoke(oldHandler);
    }
}
