/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;
import com.codename1.properties.IntProperty;
import com.codename1.properties.ListProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.ui.Image;
import java.util.Date;
/**
 *
 * @author jamesagada
 * Collection properties
 *
 * name	type	description
 * obj['name']	text	name of the route
 * obj['ad_hoc']	bool	is it an ad_hoc route
 * obj['distance']	float_number	average distance for this route
 * obj['duration']	float_number	normal duration of a trip on the route
 * obj['fares']	fares	Fare to be charged on this route
 * obj['laststop']	stop	the last stop
 * obj['startlocation']	stop	starting stop
 */
public class Route implements PropertyBusinessObject{
    public final Property<String, Route> _id = new Property<>("_id");
    public final Property<String, Route> name = new Property<>("name");
    public final Property<String, Route> ad_hoc = new Property<>("ad_hoc");
    public final Property<String, Route> description = new Property<>("description");
    public final Property<String, Route> distance = new Property<>("distance");
    public final Property<String, Route> duration = new Property<>("duration");
    public final ListProperty<Fare, Route> fares = new ListProperty<>("fares");
    public final Property<Stop, Route> laststop = new Property<>("laststop");
    public final Property<Stop, Route> startLocation = new Property<>("startLocation");
    public final ListProperty<RouteStops, Route> stops =
            new ListProperty<>("stops",RouteStops.class);
    public final Property<String,Route> _created = new Property<>("_created");
    public final PropertyIndex idx = new PropertyIndex(this, "Route",
            _id, name, description, distance,
             duration,fares, laststop, startLocation,ad_hoc,stops,_created);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
 
    }
        public Route(){
        name.setLabel("Name");
        description.setLabel("Description");
        ad_hoc.setLabel("Ad Hoc?");
    }
}
