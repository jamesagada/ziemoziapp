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
public class UserPreference implements PropertyBusinessObject{
    public final Property<String,UserPreference> _id = new Property<>("_id");
    public final Property<String, UserPreference> name = new Property<>("name");
    public final Property<String, UserPreference> description = new Property<>("description");
    public final Property<String, UserPreference> value = new Property<>("value");    
    public final PropertyIndex idx = new PropertyIndex(this, "UserPreference",
            _id, name, value, description);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
 
    }
        public UserPreference(){
        name.setLabel("Name");
        description.setLabel("Description");
    }
}
