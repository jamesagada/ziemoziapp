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
 * name	type	description
 * obj['name']	text	descriptive name of the fare
 * obj['fixed_fare']	float_number	fixed fare to be applied to the route
 * obj['distance_fare']	float_number	the fare calculated as a function of distance travelled
 * obj['duration_fare']	float_number	fare based on the duration of the ride
 * obj['active']	bool	is it active?
 * obj['effective_from_date']	date	effective from which date
 * obj['effective_to_date']	date	effective to which date
 * obj['effective_days_of_the_week']	text	days of the week to apply the fare
 * obj['apply_to_customer_group']	text	Apply to which customer groups
 * obj['effective_time_of_day']	text	Effective at what times of the day
})
 */
public class Fare implements PropertyBusinessObject{
    public final Property<String, Fare> _id = new Property<>("_id");
    public final Property<String, Fare> name = new Property<>("name");
    public final Property<String, Fare> fixed_fare = new Property<>("fixed_fare");
    public final Property<String, Fare> distance_fare = new Property<>("distance_fare");
    public final Property<String, Fare> duration_fare = new Property<>("duration_fare");
    public final Property<String, Fare> active = new Property<>("active");
    public final Property<String, Fare> effective_from_date =
            new Property<>("effective_from_date");
    public final Property<String, Fare> effective_to_date =
            new Property<>("effective_to_date");
    public final Property<String, Fare> effective_days_of_the_week =
            new Property<>("effective_days_of_the_week");
    public final Property<String, Fare> effective_time_of_the_day =
            new Property<>("effective_time_of_the_day");
    public final ListProperty<Group, Fare> applies_to_customer_group =
            new ListProperty<>("applies_to_customer_group", Group.class);
    public final Property<String,Fare> _created = new Property<>("_created");
    public final PropertyIndex idx = new PropertyIndex(this, "Fare",
            _id, name, fixed_fare, distance_fare,duration_fare,active,
            effective_from_date, effective_to_date,effective_days_of_the_week,
            effective_time_of_the_day,
            applies_to_customer_group,_created);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
 
    }
        public Fare(){
        name.setLabel("Name");
        fixed_fare.setLabel("Fare");
        active.setLabel("Active");
        effective_to_date.setLabel("Effective To");
    }
}
