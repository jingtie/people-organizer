package com.jingtie.organizer;

import com.jayway.awaitility.Awaitility;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.expect;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertNotNull;

/**
 * Created by jingtie on 8/23/15.
 */
public class ResourceTest {

    @BeforeClass
    public static void setUp() throws Exception
    {
        try
        {
            logger.info("Starting setup");

            CompositeConfiguration configuration = OrganizerProperties.getInstance();
            RestAssured.defaultParser = Parser.JSON;
            RestAssured.baseURI = "http://";
            String tomcatHost = configuration.getString("tomcat_host", "localhost");
            RestAssured.baseURI += tomcatHost;
            RestAssured.port = configuration.getInteger("tomcat_port", 8080);
            String tomcatContext = configuration.getString("tomcat_context", "organizer");
            RestAssured.basePath = tomcatContext + "/v1";

            Awaitility.setDefaultTimeout(5, MINUTES);
            Awaitility.setDefaultPollInterval(1, MINUTES);
            Awaitility.setDefaultPollDelay(10, SECONDS);

            logger.info("Setup successful");
        }
        catch (Exception e)
        {
            logger.error("Setup failed", e);
            throw e;
        }
    }

    @Test
    public void addClient() throws Exception
    {
        HashMap<String, Object> input = new HashMap<>();
        String name = "person" + System.currentTimeMillis();
        input.put("name", name);
        input.put("email", name + "@jingtie.com");

        Response response = expect()
                .statusCode(javax.ws.rs.core.Response.Status.OK.getStatusCode())
                .given()
                .formParameters(input)
                .when()
                .post("/person/add");

        JsonPath json = response.getBody().jsonPath();
        logger.info("created person json: " + json.prettify());

        Map person = json.getObject("properties", HashMap.class);
        Object idObj = person.get("id");
        assertNotNull("id of the created person is null", idObj);
        logger.info("id of created person: " + idObj.toString());

        Integer personId = (Integer)idObj;

        response = expect()
                .statusCode(javax.ws.rs.core.Response.Status.OK.getStatusCode())
                .given()
                .urlEncodingEnabled(true)
                .when()
                .get("/person/{personId}" + personId);

        json = response.getBody().jsonPath();
        logger.info("found person json: " + json.prettify());

        person = json.getObject("properties", HashMap.class);
        Object nameObj = person.get("name");
        assertNotNull("name of the person is null", nameObj);
        logger.info("name of the person: " + nameObj.toString());
    }


    private static Logger logger = Logger.getLogger(ResourceTest.class);

}
