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
public class Subscription implements PropertyBusinessObject{
    public final IntProperty<Category> _id = new IntProperty<>("_id");
    public final Property<String, Category> name = new Property<>("name");
    public final Property<String, Category> description = new Property<>("description");
    public final Property<Image, Provider> icon = new Property<>("icon", Image.class);
    public final PropertyIndex idx = new PropertyIndex(this, "Category",
            _id, name, description);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
 
    }
        public Subscription(){
        name.setLabel("Name");
        description.setLabel("Description");
        icon.setLabel("Icon");
    }
}
