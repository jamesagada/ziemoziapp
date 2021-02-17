/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;
import com.codename1.properties.IntProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.ui.Image;
import java.util.Date;
/**
 *
 * @author jamesagada
 * ({
    "_id",
    "icon",
    "description",
    "name",
    "_created",
    "_mock"
})
 */
public class ServiceAttributeType implements PropertyBusinessObject{
    public final Property<String,ServiceAttributeType> _id = new Property<>("_id");
    public final Property<String, ServiceAttributeType> name = new Property<>("name");
    public final Property<String, ServiceAttributeType> description = new Property<>("description");
    public final Property<String,ServiceAttributeType> base_type = new Property<>("base_type");
        public final Property<String,ServiceAttributeType> surveyjs_type = new Property<>("surveyjs_type");
        public final Property<String,ServiceAttributeType> _created = new Property<>("_created");
    public final PropertyIndex idx = new PropertyIndex(this, "ServiceAttributeType",
            _id, name,_created,surveyjs_type, description,base_type);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
 
    }
        public ServiceAttributeType(){
        name.setLabel("Name");
        description.setLabel("Description");
        base_type.set("text");
        _created.setLabel("_created");
    }

}
