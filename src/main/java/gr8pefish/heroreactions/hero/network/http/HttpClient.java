package gr8pefish.heroreactions.hero.network.http;

import gr8pefish.heroreactions.common.Common;
import gr8pefish.heroreactions.hero.data.UserData;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * A "simple" HTTP client to send messages to a server
 *
 * TODO: Rewrite using Apache Commons
 */
public final class HttpClient {

    /**
     * Send a basic HTTP message to the server
     *
     * @param messageAction - one of the {@link httpMessageActions}
     * @param extraData - authorization code, access token, or account ID
     * @throws Exception - if connection is not successful
     */
    public static void sendHttpMessage(@Nonnull httpMessageActions messageAction, @Nullable String extraData) throws Exception {
        String URL = messageAction.equals(httpMessageActions.GET_URL_HASH_FOR_USER) ? messageAction.url + extraData + "/hash-id" : messageAction.url;
        URI uri = new URI(System.getProperty("url", URL));
        String scheme = uri.getScheme() == null? "http" : uri.getScheme();
        String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            Common.LOGGER.error("Only HTTP(S) is supported.");
            return;
        }

        // Configure SSL context if necessary.
        final boolean ssl = "https".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new HttpClientInitializer(sslCtx)); //initialize handler

            //timeout option
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

            // Make the connection attempt.
            Channel ch = b.connect(host, port).sync().channel();

            //different action depending on message type
            HttpRequest request;
            if (messageAction.equals(httpMessageActions.GET_ACCOUNT_ID_FROM_ACCESS_TOKEN)) {
                //add auth token header
                request = new DefaultFullHttpRequest(
                        HttpVersion.HTTP_1_1, messageAction.httpType, uri.getRawPath());
                request.headers().set("Authorization", "Token "+extraData);
            } else if (messageAction.equals(httpMessageActions.GET_ACCESS_TOKEN_FROM_AUTHCODE)){
                //setup message
                ByteBuf byteMessage = wrappedBuffer(messageAction.jsonData.concat(extraData+"\"}").getBytes(CharsetUtil.UTF_8));
                //add message to body, and add content detail headers (necessary)
                request = new DefaultFullHttpRequest(
                        HttpVersion.HTTP_1_1, messageAction.httpType, uri.getRawPath(),
                        byteMessage);
                request.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                request.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteMessage.readableBytes());
            } else { // GET_URL_HASH_FROM_USER
                request = new DefaultFullHttpRequest(
                        HttpVersion.HTTP_1_1, messageAction.httpType, uri.getRawPath());
            }

            // Prepare the HTTP request with a body dependent on the message type.
            request.headers().set(HttpHeaderNames.HOST, host);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

            //Debug printing
            Common.LOGGER.debug("Message sent to server: " + messageAction.jsonData.concat(extraData+"\"}"));

            // Send the HTTP request.
            ch.writeAndFlush(request);

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();
        }
    }


    /**
     * Inner enum for determining message action
     */
    public enum httpMessageActions {

        GET_ACCESS_TOKEN_FROM_AUTHCODE("https://api.outpostgames.com/api/access-token/auth-code", HttpMethod.POST, "{\"code\":\""),
        GET_ACCOUNT_ID_FROM_ACCESS_TOKEN("https://api.outpostgames.com/api/account", HttpMethod.GET, "{\"token\":\""),
        GET_URL_HASH_FOR_USER("https://api.outpostgames.com/api/account/", HttpMethod.GET, "");

        /** The URL to connect to */
        private final String url;
        /** The type of {@link HttpMethod} to use (i.e. GET/POST/etc) */
        private final HttpMethod httpType;
        /** The static data to send */
        private final String jsonData;

        httpMessageActions(String url, HttpMethod type, String jsonData) {
            this.url = url;
            this.httpType = type;
            this.jsonData = jsonData;
        }

        public String getUrl() {
            return url;
        }

        public HttpMethod getHttpType() {
            return httpType;
        }

        public String getJsonData() {
            return jsonData;
        }
    }

}

