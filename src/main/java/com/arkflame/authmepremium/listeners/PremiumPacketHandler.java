package com.arkflame.authmepremium.listeners;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
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

    private InitialHandler oldHandler;
    private LoginRequest loginRequest;
    private BungeeCord bungee;
    private ChannelWrapper ch;

    public PremiumPacketHandler(InitialHandler oldHandler) {
        try {
            this.oldHandler = oldHandler;
            this.loginRequest = oldHandler.getLoginRequest();
            this.bungee = BungeeCord.getInstance();
            this.ch = (ChannelWrapper) HandlerReflectionUtil.getFieldValue(oldHandler, "ch");
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Premium-Packet-Handler";
    }

    @Override
    public void handle(EncryptionResponse encryptionResponse) throws Exception {
        System.out.println("Handling encryption...");
        EncryptionRequest request = (EncryptionRequest) HandlerReflectionUtil.getFieldValue(oldHandler, "request");
        Preconditions.checkState( EncryptionUtil.check( loginRequest.getPublicKey(), encryptionResponse, request ), "Invalid verification" );
        SecretKey sharedKey = EncryptionUtil.getSecret(encryptionResponse, request);
        // Waterfall start
        if (sharedKey instanceof SecretKeySpec) {
            if (sharedKey.getEncoded().length != 16) {
                this.ch.close();
                return;
            }
        }
        // Waterfall end
        BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
        ch.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.DECRYPT_HANDLER, new CipherDecoder(decrypt));
        BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
        ch.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.ENCRYPT_HANDLER, new CipherEncoder(encrypt));

        String encName = URLEncoder.encode(oldHandler.getName(), "UTF-8");

        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        for (byte[] bit : new byte[][] {
                request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(),
                EncryptionUtil.keys.getPublic().getEncoded()
        }) {
            sha.update(bit);
        }
        String encodedHash = URLEncoder.encode(new BigInteger(sha.digest()).toString(16), "UTF-8");

        String preventProxy = (bungee.config.isPreventProxyConnections()
                && getSocketAddress() instanceof InetSocketAddress)
                        ? "&ip=" + URLEncoder.encode(getAddress().getAddress().getHostAddress(), "UTF-8")
                        : "";
        String authURL = String.format(MOJANG_AUTH_URL, encName, encodedHash, preventProxy);

        Callback<String> handler = new Callback<String>() {
            @Override
            public void done(String result, Throwable error) {
                if (error == null) {
                    LoginResult obj = bungee.gson.fromJson(result, LoginResult.class);
                    if (obj != null && obj.getId() != null) {
                        try {
                            Field uniqueIdField = InitialHandler.class.getDeclaredField("uniqueId");
                            uniqueIdField.setAccessible(true);

                            // Set the uniqueId field if necessary
                            if (true) {
                                uniqueIdField.set(oldHandler, Util.getUUID(obj.getId()));
                            }
                            HandlerReflectionUtil.setFieldValue(oldHandler, "loginProfile", obj);
                            HandlerReflectionUtil.setFieldValue(oldHandler, "name", obj.getName());

                            Method finishMethod = InitialHandler.class.getDeclaredMethod("finish");
                            finishMethod.setAccessible(true);
                            finishMethod.invoke(oldHandler);
                            System.out.println(oldHandler.getName() + " is premium!");
                            PreLoginListener.premium.add(oldHandler.getName());
                        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        return;
                    }
                    oldHandler.disconnect("Bro! You are offline :D");
                } else {
                    oldHandler.disconnect("Mojang failed xd");
                    bungee.getLogger().log(Level.SEVERE,
                            "Error authenticating " + oldHandler.getName() + " with minecraft.net",
                            error);
                }
            }
        };
        // thisState = State.FINISHING; // Waterfall - move earlier
        HttpClient.get(authURL, ch.getHandle().eventLoop(), handler);
    }

    private InetSocketAddress getAddress() {
        return oldHandler.getAddress();
    }

    private SocketAddress getSocketAddress() {
        return oldHandler.getSocketAddress();
    }
}
