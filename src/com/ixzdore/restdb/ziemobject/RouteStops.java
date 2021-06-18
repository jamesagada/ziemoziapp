/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

/**
 *
 * @author jamesagada
Collection properties

name	type	description
obj['sequence']	number	sequence number of the stop
obj['stop']	stop	stop
obj['distance-to_end']	float_number	distance to the end of the route
obj['Zone']	text	Zone of the Stop

 */
public class RouteStops implements PropertyBusinessObject{
    public final Property<String, RouteStops> _id = new Property<>("_id");
    public final Property<String, RouteStops> sequence = new Property<>("sequence");
    public final Property<String, RouteStops> distance_to_end =
            new Property<>("distance_to_end");
    public final Property<String, RouteStops> zone = new Property<>("zone");
    public final Property<Stop, RouteStops> stop = new Property<>("stop");
    public final Property<String,RouteStops> _created = new Property<>("_created");
    public final PropertyIndex idx = new PropertyIndex(this, "RouteStop",
            _id, sequence, distance_to_end, zone,stop,_created);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;

    }
        public RouteStops(){
        sequence.setLabel("Name");
        zone.setLabel("zone");
        distance_to_end.setLabel("Distance To End");
    }
}
