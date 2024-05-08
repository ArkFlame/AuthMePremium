package com.arkflame.authmepremium.listeners;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.protocol.packet.EncryptionRequest;

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

    private void handleInitialHandler(PreLoginEvent event, InitialHandler initialHandler) throws NoSuchFieldException, IllegalAccessException {
        initialHandler.setOnlineMode(true);
        ChannelWrapper ch = getChannel(initialHandler);
        setPremiumPacketHandler(ch, initialHandler);
        setPreLoginEventCallback(event, initialHandler);
    }

    private ChannelWrapper getChannel(InitialHandler initialHandler) throws NoSuchFieldException, IllegalAccessException {
        return (ChannelWrapper) HandlerReflectionUtil.getFieldValue(initialHandler, "ch");
    }

    private void setPremiumPacketHandler(ChannelWrapper ch, InitialHandler initialHandler) {
        if (ch != null) {
            ch.getHandle().pipeline().get(HandlerBoss.class).setHandler(new PremiumPacketHandler(initialHandler));
        }
    }

    private void setPreLoginEventCallback(PreLoginEvent event, InitialHandler initialHandler) {
        Callback<PreLoginEvent> callback = createCallback(initialHandler);
        setCallbackForPreLoginEvent(event, callback);
    }

    private Callback<PreLoginEvent> createCallback(InitialHandler initialHandler) {
        return new Callback<PreLoginEvent>() {
            @Override
            public void done(PreLoginEvent result, Throwable error) {
                try {
                    handleCallbackResult(result, error, initialHandler);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                    initialHandler.disconnect("Server-side error.");
                }
            }
        };
    }

    private void handleCallbackResult(PreLoginEvent result, Throwable error, InitialHandler initialHandler) throws NoSuchFieldException, IllegalAccessException {
        if (result.isCancelled()) {
            handleCancellation(result, initialHandler);
        } else if (!isChannelClosing(initialHandler)) {
            handleChannelNotClosing(result, initialHandler);
        }
    }

    private void handleCancellation(PreLoginEvent result, InitialHandler initialHandler) {
        BaseComponent reason = result.getReason();
        initialHandler.disconnect((reason != null) ? reason
                : TextComponent.fromLegacy(BungeeCord.getInstance().getTranslation("kick_message")));
    }

    private boolean isChannelClosing(InitialHandler initialHandler) throws NoSuchFieldException, IllegalAccessException {
        return getChannel(initialHandler).isClosing();
    }

    private void handleChannelNotClosing(PreLoginEvent result, InitialHandler initialHandler) {
        if (isPremium(initialHandler) || (initialHandler.isOnlineMode() && !isNotPremium(initialHandler))) {
            handlePremium(initialHandler);
        } else {
            handleNonPremium(initialHandler);
        }
    }

    private boolean isPremium(InitialHandler initialHandler) {
        return PreLoginListener.premium.contains(initialHandler.getName());
    }

    private boolean isNotPremium(InitialHandler initialHandler) {
        return PreLoginListener.notPremium.contains(initialHandler.getName());
    }

    private void handlePremium(InitialHandler initialHandler) {
        try {
            sendEncryptionRequest(initialHandler);
            PreLoginListener.notPremium.add(initialHandler.getName());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void sendEncryptionRequest(InitialHandler initialHandler)
            throws IllegalAccessException, NoSuchFieldException {
        EncryptionRequest request = EncryptionUtil.encryptRequest();
        Field requestField = InitialHandler.class.getDeclaredField("request");
        requestField.setAccessible(true);
        requestField.set(initialHandler, request);
        initialHandler.unsafe().sendPacket(request);
    }

    private void handleNonPremium(InitialHandler initialHandler) {
        try {
            invokeFinishMethod(initialHandler);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void invokeFinishMethod(InitialHandler initialHandler)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method finishMethod = InitialHandler.class.getDeclaredMethod("finish");
        finishMethod.setAccessible(true);
        finishMethod.invoke(initialHandler);
    }

    private void setCallbackForPreLoginEvent(PreLoginEvent preLoginEvent, Callback<PreLoginEvent> callback) {
        try {
            Field doneField = AsyncEvent.class.getDeclaredField("done");
            doneField.setAccessible(true);
            doneField.set(preLoginEvent, callback);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
