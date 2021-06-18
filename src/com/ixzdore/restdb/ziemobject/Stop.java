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
 *
 * name	type	description
 * obj['name']	text	name of the stop
 * obj['zone']	text	- missing description -
 * obj['longitude']	text	longitude
 * obj['latitude']	text	latitude
 */
public class Stop implements PropertyBusinessObject{
    public final Property<String, Stop> _id = new Property<>("_id");
    public final Property<String, Stop> name = new Property<>("name");
    public final Property<String, Stop> zone = new Property<>("zone");
    public final Property<String, Stop> longitude = new Property<>("longitude");
    public final Property<String, Stop> latitude = new Property<>("latitude");

    public final PropertyIndex idx = new PropertyIndex(this, "Stop",
            _id, name, zone, longitude, latitude);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
 
    }
        public Stop(){
        name.setLabel("Name");
        zone.setLabel("Zone");
        longitude.setLabel("Longitude");
        latitude.setLabel("Latitude");
    }
}
