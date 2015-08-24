package com.jingtie.organizer.resource;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

/**
 * Created by jingtie on 8/23/15.
 */
@XmlRootElement
public class DataEntity {
    private HashMap properties;

    public DataEntity(HashMap properties)
    {
        this.properties = properties;
    }

    public HashMap getProperties() {
        return properties;
    }
}
