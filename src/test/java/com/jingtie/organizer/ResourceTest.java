package com.jingtie.organizer;

import com.jingtie.organizer.resource.DataEntity;
import com.jingtie.organizer.resource.GrizzlyServer;
import com.sun.jersey.api.representation.Form;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertNotNull;

/**
 * Created by jingtie on 8/23/15.
 */
public class ResourceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        try
        {
            System.out.println("Starting setup");

            // start the server
            baseUri = OrganizerProperties.getInstance().getString("grizzly_uri", "http://localhost:8888/organizer");
            server = GrizzlyServer.startServer(baseUri);
            target = webClient.target(baseUri);

            System.out.println("Setup successful");
        }
        catch (Exception e)
        {
            logger.error("Setup failed", e);
            throw e;
        }
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void addPerson() throws Exception
    {
        Form input = new Form();
        String name = "person" + System.currentTimeMillis();
        input.putSingle("name", name);
        input.putSingle("email", name + "@jingtie.com");

        DataEntity dataEntity = webClient
                .target(baseUri)
                .path("/person/add")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(input, MediaType.APPLICATION_FORM_URLENCODED), DataEntity.class);

        assertNotNull("person creation response should not be null", dataEntity);
        Integer personId = (Integer)dataEntity.getProperties().get("personId");
        System.out.println("created personId: " + personId);

        dataEntity = webClient
                .target(baseUri)
                .path("/person/" + personId)
                .request(MediaType.APPLICATION_JSON)
                .get(DataEntity.class);

        assertNotNull("person creation response should not be null", dataEntity);
        String gottenName = (String)dataEntity.getProperties().get("name");
        assert gottenName.equalsIgnoreCase(name);
        System.out.println("Got created person's name: " + gottenName);
    }


    private HttpServer server;
    private WebTarget target;
    private Client webClient = ClientBuilder.newClient().register(JacksonFeature.class);
    private static String baseUri = null;
    private static Logger logger = Logger.getLogger(ResourceTest.class);

}

