package com.jingtie.organizer.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Created by jingtie on 8/23/15.
 */
public class OrganizerException extends WebApplicationException {

    public OrganizerException(Response.Status status) {
        super(Response.status(status).build());
    }

    public OrganizerException(Response.Status status, String message) {
        super(Response.status(status).entity(message).type("text/plain").build());
    }
}
