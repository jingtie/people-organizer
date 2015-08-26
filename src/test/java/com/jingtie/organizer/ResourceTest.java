package com.jingtie.organizer;

import com.jingtie.organizer.dao.PersonDao;
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
import java.util.LinkedList;
import java.util.List;

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
    public void tests()
    {
        int firstPersonId = createPerson();
        int secondPersonId = createPerson();

        List<Integer> memberIds = new LinkedList<>();
        memberIds.add(firstPersonId);

        int firstFamily = createFamily(memberIds);
        List<Integer> familyIds = new LinkedList<>();
        familyIds.add(firstFamily);
        List<Integer> resultFamilyIds = groupPersonIntoFamilies(secondPersonId, familyIds);
        assert resultFamilyIds.size() == 1;

        int secondFamily = createFamily(memberIds);
        familyIds.clear();
        familyIds.add(secondFamily);
        resultFamilyIds = groupPersonIntoFamilies(secondPersonId, familyIds);
        assert resultFamilyIds.size() == 2;
    }


    public int createPerson()
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

        assertNotNull("get person response should not be null", dataEntity);
        String gottenName = (String)dataEntity.getProperties().get("name");
        assert gottenName.equalsIgnoreCase(name);
        System.out.println("Got person's name: " + gottenName);

        return personId;
    }


    public int createFamily(List<Integer> memberIds)
    {
        Form input = new Form();
        String name = "family" + System.currentTimeMillis();
        input.putSingle("name", name);

        StringBuilder stringBuilder = new StringBuilder();
        if(memberIds != null && memberIds.size() > 0)
        {
            for(int personId : memberIds)
            {
                stringBuilder.append(personId).append(";");
            }
        }
        input.putSingle("memberIds", stringBuilder.toString());

        DataEntity dataEntity = webClient
                .target(baseUri)
                .path("/family/add")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(input, MediaType.APPLICATION_FORM_URLENCODED), DataEntity.class);

        assertNotNull("family creation response should not be null", dataEntity);
        Integer familyId = (Integer)dataEntity.getProperties().get("familyId");
        System.out.println("created familyId: " + familyId);

        dataEntity = webClient
                .target(baseUri)
                .path("/family/" + familyId)
                .request(MediaType.APPLICATION_JSON)
                .get(DataEntity.class);

        assertNotNull("get family response should not be null", dataEntity);
        String gottenName = (String)dataEntity.getProperties().get("name");
        assert gottenName.equalsIgnoreCase(name);
        System.out.println("Got family's name: " + gottenName);

        List<PersonDao> gottenMembers = (List<PersonDao>)dataEntity.getProperties().get("members");
        assert gottenMembers.size() == memberIds.size();
        System.out.println("Got family's members, size: " + gottenMembers.size());

        return familyId;
    }


    public List<Integer> groupPersonIntoFamilies(int personId, List<Integer> familyIds)
    {
        Form input = new Form();
        StringBuilder stringBuilder = new StringBuilder();
        if(familyIds != null && familyIds.size() > 0)
        {
            for(int familyId : familyIds)
            {
                stringBuilder.append(familyId).append(";");
            }
        }
        input.putSingle("familyIds", stringBuilder.toString());

        DataEntity dataEntity = webClient
                .target(baseUri)
                .path("/person/group/" + personId)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(input, MediaType.APPLICATION_FORM_URLENCODED), DataEntity.class);

        assertNotNull("person should not be null", dataEntity);
        List<Integer> resultFamilyIds = (List<Integer>)dataEntity.getProperties().get("familyIds");
        System.out.println("result familyIds: " + resultFamilyIds.size());

        return resultFamilyIds;
    }



    private HttpServer server;
    private WebTarget target;
    private Client webClient = ClientBuilder.newClient().register(JacksonFeature.class);
    private static String baseUri = null;
    private static Logger logger = Logger.getLogger(ResourceTest.class);

}

