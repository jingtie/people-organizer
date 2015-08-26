package com.jingtie.organizer.resource;

import com.jingtie.organizer.dao.PersonDao;
import com.jingtie.organizer.db.H2Impl;
import com.jingtie.organizer.db.IDataStore;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Created by jingtie on 8/23/15.
 */
@Path("/person")
public class PersonResource {

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public DataEntity createPerson(
            @FormParam("name") String name,
            @FormParam("email") String email)
    {
        assertTrue("name is empty", name != null && !name.equals(""));
        assertTrue("email is empty", email != null && !email.equals(""));

        try
        {
            final IDataStore dataStore = H2Impl.getInstance();
            final PersonDao person = dataStore.createPerson(name, email);
            logger.debug("Person created: " + person.getId());

            HashMap<String, Object> props = new HashMap<>(1);
            props.put("personId", person.getId());
            DataEntity dataEntity = new DataEntity(props);
            return dataEntity;
        }
        catch (Throwable t)
        {
            logger.error("Create new person exception", t);

            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            final String errorMessage = t.getMessage() + ", StackTrace: " + sw.toString();

            throw new OrganizerException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }


    @GET
    @Path("{personId}")
    @Encoded
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public DataEntity getPerson(@PathParam("personId") String personId)
    {
        assertTrue("personId is empty", personId != null && !personId.equals(""));

        try
        {
            int id = Integer.parseInt(personId);
            final IDataStore dataStore = H2Impl.getInstance();
            final PersonDao person = dataStore.getPerson(id);
            if(person == null)
            {
                logger.error("Person Not Found or Been Deleted. personId=" + personId);
                throw new OrganizerException(Response.Status.NOT_FOUND, "Person Not Found or Been Deleted");
            }

            HashMap<String, Object> props = new HashMap<>();
            props.put("name", person.getName());
            props.put("email", person.getEmail());
            props.put("createdTime", person.getCreatedTime() + "");

            DataEntity dataEntity = new DataEntity(props);
            return dataEntity;
        }
        catch (Throwable t)
        {
            logger.error("Get person exception", t);

            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            final String errorMessage = t.getMessage() + ", StackTrace: " + sw.toString();

            throw new OrganizerException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }


    private static final Logger logger = Logger.getLogger(PersonResource.class);
}
