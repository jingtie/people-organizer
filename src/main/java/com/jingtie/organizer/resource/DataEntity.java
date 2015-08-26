package com.jingtie.organizer.resource;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

/**
 * Created by jingtie on 8/23/15.
 */
@XmlRootElement
public class DataEntity
{
    @XmlElement(name="properties")
    private HashMap properties;

    public DataEntity() {}

    public DataEntity(HashMap<String, Object> properties)
    {
        this.properties = properties;
    }

    public HashMap getProperties()
    {
        return properties;
    }

    public void setProperties(HashMap properties)
    {
        this.properties = properties;
    }
}
