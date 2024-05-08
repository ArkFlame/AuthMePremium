package com.arkflame.authmepremium.listeners;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

import com.arkflame.authmepremium.callbacks.PostLoginCallback;
import com.arkflame.authmepremium.handlers.PremiumPacketHandler;
import com.arkflame.authmepremium.utils.HandlerReflectionUtil;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;

/**
 * This class implements the Listener interface and handles pre-login events.
 * It sets online mode, manages premium packet handlers, and defines event callbacks.
 * Key functionalities include setting premium packet handlers and defining event callbacks.
 */
public class PreLoginListener implements Listener {
    public static Collection<String> notPremium = new HashSet<>();
    public static Collection<String> premium = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(PreLoginEvent event) {
        if (event.getConnection() instanceof InitialHandler) {
            InitialHandler initialHandler = (InitialHandler) event.getConnection();
            try {
                handleInitialHandler(event, initialHandler);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                initialHandler.disconnect("Server-side error.");
            }
        }
    }

    private void handleInitialHandler(PreLoginEvent event, InitialHandler initialHandler)
            throws NoSuchFieldException, IllegalAccessException {
        initialHandler.setOnlineMode(true);
        ChannelWrapper ch = getChannel(initialHandler);
        setPremiumPacketHandler(ch, initialHandler);
        setPreLoginEventCallback(event, initialHandler);
    }

    private ChannelWrapper getChannel(InitialHandler initialHandler)
            throws NoSuchFieldException, IllegalAccessException {
        return (ChannelWrapper) HandlerReflectionUtil.getFieldValue(initialHandler, "ch");
    }

    private void setPremiumPacketHandler(ChannelWrapper ch, InitialHandler initialHandler) throws NoSuchFieldException, IllegalAccessException {
        if (ch != null) {
            ch.getHandle().pipeline().get(HandlerBoss.class).setHandler(new PremiumPacketHandler(initialHandler));
        }
    }

    private void setPreLoginEventCallback(PreLoginEvent event, InitialHandler initialHandler)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Callback<PreLoginEvent> callback = new PostLoginCallback(initialHandler);
        setCallbackForPreLoginEvent(event, callback);
    }

    private void setCallbackForPreLoginEvent(PreLoginEvent preLoginEvent, Callback<PreLoginEvent> callback)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field doneField = AsyncEvent.class.getDeclaredField("done");
        doneField.setAccessible(true);
        doneField.set(preLoginEvent, callback);
    }
}
