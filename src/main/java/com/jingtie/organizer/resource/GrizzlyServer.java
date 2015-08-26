package com.jingtie.organizer.resource;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Created by jingtie on 8/25/15.
 */
public class GrizzlyServer {

    public static HttpServer startServer(String baseUri) {
        final ResourceConfig rc = new ResourceConfig().packages("com.jingtie.organizer.resource");
        rc.register(JacksonFeature.class);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc);
    }

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            System.out.println("Usage: GrizzlyServer baseUri(e.g. http://localhost:8888/organizer)");
        }
        String baseUri = args[0];
        final HttpServer server = startServer(baseUri);
        System.in.read();
        server.stop();
    }
}