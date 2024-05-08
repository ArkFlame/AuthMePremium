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
import net.md_5.bungee.api.connection.PendingConnection;
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
        BungeeCord bungee = BungeeCord.getInstance();
        PendingConnection pendingConnection = event.getConnection();

        if (pendingConnection instanceof InitialHandler) {
            InitialHandler initialHandler = (InitialHandler) pendingConnection;
            initialHandler.setOnlineMode(true);

            ChannelWrapper ch;
            try {
                ch = (ChannelWrapper) HandlerReflectionUtil.getFieldValue(initialHandler, "ch");
                ch.getHandle().pipeline().get(HandlerBoss.class).setHandler(new PremiumPacketHandler(initialHandler));

                Callback<PreLoginEvent> callback = new Callback<PreLoginEvent>() {

                    @Override
                    public void done(PreLoginEvent result, Throwable error) {
                        if (result.isCancelled()) {
                            BaseComponent reason = result.getReason();
                            initialHandler.disconnect((reason != null) ? reason
                                    : TextComponent.fromLegacy(bungee.getTranslation("kick_message")));
                            return;
                        }
                        if (ch.isClosing()) {
                            return;
                        }
                        if (premium.contains(initialHandler.getName())
                                || (initialHandler.isOnlineMode() && !notPremium.contains(initialHandler.getName()))) {
                            try {
                                EncryptionRequest request = EncryptionUtil.encryptRequest();
                                Field requestField = InitialHandler.class.getDeclaredField("request");
                                requestField.setAccessible(true);
                                requestField.set(initialHandler, request);
                                initialHandler.unsafe().sendPacket(request);
                                notPremium.add(initialHandler.getName());
                            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                                    | SecurityException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Method finishMethod = initialHandler.getClass().getDeclaredMethod("finish");
                                finishMethod.setAccessible(true);
                                finishMethod.invoke(initialHandler);
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException
                                    | SecurityException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };

                Field doneField = AsyncEvent.class.getDeclaredField("done");
                doneField.setAccessible(true);
                doneField.set(event, callback);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}
