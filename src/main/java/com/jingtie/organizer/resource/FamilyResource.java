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
@Path("/family")
public class FamilyResource {

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public DataEntity createFamily(
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

            HashMap props = new HashMap<>(1);
            props.put("id", person.getId());
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
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFamily(@PathParam("id") int id)
    {
        assertTrue("id should larger than 0", id > 0);

        try
        {
            final IDataStore dataStore = H2Impl.getInstance();
            final PersonDao person = dataStore.getPerson(id);
            if(person == null)
            {
                return Response.status(Response.Status.NOT_FOUND).entity("Person Not Found or Been Deleted").type("text/plain").build();
            }

            HashMap<String, String> result = new HashMap<>();
            result.put("name", person.getName());
            result.put("email", person.getEmail());
            result.put("createdTime", person.getCreatedTime() + "");

            Response.ResponseBuilder builder = Response.ok().entity(result);
            return builder.build();
        }
        catch (Throwable t)
        {
            logger.error("Get person exception", t);

            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            final String errorMessage = t.getMessage() + ", StackTrace: " + sw.toString();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).type("text/plain").build();
        }
    }


    private static final Logger logger = Logger.getLogger(FamilyResource.class);
}
