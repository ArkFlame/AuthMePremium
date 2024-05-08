package com.arkflame.authmepremium.handlers;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.arkflame.authmepremium.callbacks.AuthCallback;
import com.arkflame.authmepremium.utils.HandlerReflectionUtil;
import com.google.common.base.Preconditions;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.connection.InitialHandler;
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

/**
 * This class extends PacketHandler and is responsible for handling premium packet-related operations.
 * It manages encryption, decryption, and authentication requests.
 * Key functionalities include constructing authentication URLs, adding cipher handlers, and closing channels.
 */
public class PremiumPacketHandler extends PacketHandler {
    private static final String MOJANG_AUTH_URL = System.getProperty("waterfall.auth.url",
            "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s%s");

    private final InitialHandler oldHandler;
    private final ChannelWrapper ch;
    private final BungeeCord bungee;

    public PremiumPacketHandler(InitialHandler oldHandler) throws NoSuchFieldException, IllegalAccessException {
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

        Callback<String> handler = new AuthCallback(oldHandler, bungee);
        HttpClient.get(authURL, ch.getHandle().eventLoop(), handler);
    }

    private ChannelWrapper getChannel(InitialHandler initialHandler) throws NoSuchFieldException, IllegalAccessException {
        return (ChannelWrapper) HandlerReflectionUtil.getFieldValue(initialHandler, "ch");
    }

    private EncryptionRequest getRequest() throws NoSuchFieldException, IllegalAccessException {
        return (EncryptionRequest) HandlerReflectionUtil.getFieldValue(oldHandler, "request");
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

    private String encodeHash(String serverId, byte[] sharedKey) throws UnsupportedEncodingException, NoSuchAlgorithmException {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(serverId.getBytes(StandardCharsets.ISO_8859_1));
            sha.update(sharedKey);
            sha.update(EncryptionUtil.keys.getPublic().getEncoded());
            return URLEncoder.encode(new BigInteger(sha.digest()).toString(16), "UTF-8");
    }

    private String buildAuthURL(String encName, String encodedHash) throws UnsupportedEncodingException {
        boolean preventProxy = bungee.config.isPreventProxyConnections()
                && getSocketAddress() instanceof InetSocketAddress;
        String preventProxyParam = preventProxy
                ? "&ip=" + URLEncoder.encode(getAddress().getAddress().getHostAddress(), "UTF-8")
                : "";
        return String.format(MOJANG_AUTH_URL, encName, encodedHash, preventProxyParam);
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
