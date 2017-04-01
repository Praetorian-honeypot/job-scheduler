package api;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import server.Server;
import java.net.URI;

/**
 * Main class for the REST API taken from 
 * the grizzly2 Maven archetype
 *
 */
public class RestAPI {
    // Base URI the Grizzly HTTP server will listen on
    
    final HttpServer rest;
    Server server;
    
    public RestAPI(Server server, String address)  {
    	this.rest = startServer(server, address);
    	
    }
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(Server server, String address) {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.underdog.jersey.grizzly package
        final ResourceConfig rc = new ResourceConfig().packages("api");
        rc.property("server", server);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(address), rc);
    }
    
}