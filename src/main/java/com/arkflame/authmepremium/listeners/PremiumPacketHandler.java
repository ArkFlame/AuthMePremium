package com.arkflame.authmepremium.listeners;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.logging.Level;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.base.Preconditions;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;

public class PremiumPacketHandler extends PacketHandler {
    private static final String MOJANG_AUTH_URL = System.getProperty("waterfall.auth.url",
            "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s%s");

    private final InitialHandler oldHandler;
    private final ChannelWrapper ch;
    private final BungeeCord bungee;

    public PremiumPacketHandler(InitialHandler oldHandler) {
        this.oldHandler = oldHandler;
        this.ch = getChannel(oldHandler);
        this.bungee = BungeeCord.getInstance();
    }

    @Override
    public String toString() {
        return "Premium-Packet-Handler";
    }

    @Override
    public void handle(EncryptionResponse encryptionResponse) throws Exception {
        LoginRequest loginRequest = oldHandler.getLoginRequest();
        EncryptionRequest request = getRequest();
        Preconditions.checkState(EncryptionUtil.check(loginRequest.getPublicKey(), encryptionResponse, request),
                "Invalid verification");
        SecretKey sharedKey = EncryptionUtil.getSecret(encryptionResponse, request);
        if (!isValidSharedKey(sharedKey)) {
            closeChannel();
            return;
        }
        addCipherHandlers(sharedKey);

        String encName = URLEncoder.encode(oldHandler.getName(), "UTF-8");
        String encodedHash = encodeHash(request.getServerId(), sharedKey.getEncoded());

        String authURL = buildAuthURL(encName, encodedHash);

        Callback<String> handler = createAuthHandler();
        HttpClient.get(authURL, ch.getHandle().eventLoop(), handler);
    }

    private ChannelWrapper getChannel(InitialHandler initialHandler) {
        try {
            return (ChannelWrapper) HandlerReflectionUtil.getFieldValue(initialHandler, "ch");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private EncryptionRequest getRequest() {
        try {
            return (EncryptionRequest) HandlerReflectionUtil.getFieldValue(oldHandler, "request");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isValidSharedKey(SecretKey sharedKey) {
        return sharedKey instanceof SecretKeySpec && sharedKey.getEncoded().length == 16;
    }

    private void addCipherHandlers(SecretKey sharedKey) throws GeneralSecurityException {
        BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
        ch.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.DECRYPT_HANDLER, new CipherDecoder(decrypt));
        BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
        ch.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.ENCRYPT_HANDLER, new CipherEncoder(encrypt));
    }

    private String encodeHash(String serverId, byte[] sharedKey) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(serverId.getBytes("ISO_8859_1"));
            sha.update(sharedKey);
            sha.update(EncryptionUtil.keys.getPublic().getEncoded());
            return URLEncoder.encode(new BigInteger(sha.digest()).toString(16), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String buildAuthURL(String encName, String encodedHash) throws UnsupportedEncodingException {
        boolean preventProxy = bungee.config.isPreventProxyConnections()
                && getSocketAddress() instanceof InetSocketAddress;
        String preventProxyParam = preventProxy
                ? "&ip=" + URLEncoder.encode(getAddress().getAddress().getHostAddress(), "UTF-8")
                : "";
        return String.format(MOJANG_AUTH_URL, encName, encodedHash, preventProxyParam);
    }

    private Callback<String> createAuthHandler() {
        return new Callback<String>() {
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
        };
    }

    private void handleAuthSuccess(String result) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, SecurityException, NoSuchFieldException {
        LoginResult obj = bungee.gson.fromJson(result, LoginResult.class);
        if (obj != null && obj.getId() != null) {
            updateHandlerWithLoginResult(obj);
            invokeFinishMethod();
            PreLoginListener.premium.add(oldHandler.getName());
            PreLoginListener.notPremium.remove(oldHandler.getName());
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
        Field uniqueIdField = InitialHandler.class.getDeclaredField("uniqueId");
        uniqueIdField.setAccessible(true);

        // Set the uniqueId field if necessary
        if (true) { // Replace true with your condition
            uniqueIdField.set(oldHandler, Util.getUUID(obj.getId()));
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

    private InetSocketAddress getAddress() {
        return oldHandler.getAddress();
    }

    private SocketAddress getSocketAddress() {
        return oldHandler.getSocketAddress();
    }

    private void closeChannel() {
        ch.close();
    }
}
