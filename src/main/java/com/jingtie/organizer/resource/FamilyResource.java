package com.jingtie.organizer.resource;

import com.jingtie.organizer.dao.FamilyDao;
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
import java.util.LinkedList;
import java.util.List;

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
            @FormParam("memberIds") String memberIds)
    {
        assertTrue("name is empty", name != null && !name.equals(""));

        try
        {
            final IDataStore dataStore = H2Impl.getInstance();
            final List<Integer> memberIdList = new LinkedList<>();
            if(memberIds != null && !memberIds.equals(""))
            {
                String[] ids = memberIds.split(";");
                for(String idString : ids)
                {
                    idString = idString.trim();
                    if(idString != null && !idString.equals(""))
                    {
                        int id = Integer.parseInt(idString);
                        memberIdList.add(id);
                    }
                }
            }
            final FamilyDao family = dataStore.createFamily(name, memberIdList);
            logger.info("Family created: " + family.getId());

            final HashMap props = new HashMap<>(1);
            props.put("familyId", family.getId());
            final DataEntity dataEntity = new DataEntity(props);
            return dataEntity;
        }
        catch (Throwable t)
        {
            logger.error("Create new family exception", t);

            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            final String errorMessage = t.getMessage() + ", StackTrace: " + sw.toString();

            throw new OrganizerException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }


    @GET
    @Path("{familyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DataEntity getFamily(@PathParam("familyId") String familyId)
    {
        assertTrue("familyId is empty", familyId != null && !familyId.equals(""));

        try
        {
            final int id = Integer.parseInt(familyId);
            final IDataStore dataStore = H2Impl.getInstance();
            final FamilyDao family = dataStore.getFamily(id);
            if(family == null)
            {
                logger.error("Family Not Found or Been Deleted");
                throw new NotFoundException("Family Not Found or Been Deleted");
            }

            List<PersonDao> members = dataStore.getMembers(id);

            HashMap<String, Object> props = new HashMap<>();
            props.put("name", family.getName());
            props.put("members", members);
            props.put("createdTime", family.getCreatedTime() + "");

            final DataEntity dataEntity = new DataEntity(props);
            return dataEntity;
        }
        catch (Throwable t)
        {
            logger.error("Get family exception", t);

            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            final String errorMessage = t.getMessage() + ", StackTrace: " + sw.toString();

            throw new OrganizerException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }


    private static final Logger logger = Logger.getLogger(FamilyResource.class);
}
