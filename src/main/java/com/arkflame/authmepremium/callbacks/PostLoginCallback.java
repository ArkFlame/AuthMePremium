package com.arkflame.authmepremium.callbacks;

import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.packet.EncryptionRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.arkflame.authmepremium.AuthMePremiumPlugin;
import com.arkflame.authmepremium.utils.HandlerReflectionUtil;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * This class implements the Callback interface for handling post-login events.
 * It performs actions based on the outcome of the pre-login event, such as
 * sending an encryption request
 * for premium users or invoking the finish method for non-premium users.
 */
public class PostLoginCallback implements Callback<PreLoginEvent> {
    private InitialHandler initialHandler;

    public PostLoginCallback(InitialHandler initialHandler) {
        this.initialHandler = initialHandler;
    }

    @Override
    public void done(PreLoginEvent result, Throwable error) {
        try {
            handleCallbackResult(result, initialHandler);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            initialHandler.disconnect("Server-side error.");
        }
    }

    private void handleCallbackResult(PreLoginEvent result, InitialHandler initialHandler)
            throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (result.isCancelled()) {
            handleCancellation(result, initialHandler);
        } else if (!isChannelClosing(initialHandler)) {
            handleChannelNotClosing(initialHandler);
        }
    }

    private void handleCancellation(PreLoginEvent result, InitialHandler initialHandler) {
        BaseComponent reason = result.getReason();
        initialHandler.disconnect((reason != null) ? reason
                : TextComponent.fromLegacy(BungeeCord.getInstance().getTranslation("kick_message")));
    }

    private ChannelWrapper getChannel(InitialHandler initialHandler)
            throws NoSuchFieldException, IllegalAccessException {
        return (ChannelWrapper) HandlerReflectionUtil.getFieldValue(initialHandler, "ch");
    }

    private boolean isChannelClosing(InitialHandler initialHandler)
            throws NoSuchFieldException, IllegalAccessException {
        return getChannel(initialHandler).isClosing();
    }

    private void handleChannelNotClosing(InitialHandler initialHandler)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        Boolean isPremium = AuthMePremiumPlugin.getDataProvider().getPremium(initialHandler.getName());

        if (isPremium != null && isPremium || (isPremium == null && initialHandler.isOnlineMode())) {
            handlePremium(initialHandler, isPremium);
        } else {
            handleNonPremium(initialHandler);
        }
    }

    private void handlePremium(InitialHandler initialHandler, Boolean isPremium) throws IllegalAccessException, NoSuchFieldException {
        sendEncryptionRequest(initialHandler);
        if (isPremium == null) {
            AuthMePremiumPlugin.getDataProvider().setPremium(initialHandler.getName(), false);
        }
    }

    private void sendEncryptionRequest(InitialHandler initialHandler)
            throws IllegalAccessException, NoSuchFieldException {
        EncryptionRequest request = EncryptionUtil.encryptRequest();
        HandlerReflectionUtil.setFieldValue(initialHandler, "request", request);
        initialHandler.unsafe().sendPacket(request);
    }

    private void handleNonPremium(InitialHandler initialHandler)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        invokeFinishMethod(initialHandler);
        initialHandler.setOnlineMode(false);
    }

    private void invokeFinishMethod(InitialHandler initialHandler)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method finishMethod = InitialHandler.class.getDeclaredMethod("finish");
        finishMethod.setAccessible(true);
        finishMethod.invoke(initialHandler);
    }
}