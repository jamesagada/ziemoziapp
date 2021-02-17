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
public class Fare implements PropertyBusinessObject{
    public final Property<String, Fare> _id = new Property<>("_id");
    public final Property<String, Fare> first_name = new Property<>("first_name");
    public final Property<String, Fare> description = new Property<>("description");
    public final Property<String, Fare> balance = new Property<>("balance");
    public final Property<String, Fare> ble_code = new Property<>("ble_code");
    public final Property<String, Fare> qr_code = new Property<>("qr_code");
    public final Property<String, Fare> phone_number = new Property<>("phone_number");
    public final Property<String, Fare> email = new Property<>("email");
    public final Property<String, Fare> last_name = new Property<>("last_name");
    public final PropertyIndex idx = new PropertyIndex(this, "Wallet",
            _id, phone_number, balance, email, first_name,last_name, description, ble_code,qr_code);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
 
    }
        public Fare(){
        first_name.setLabel("Name");
        balance.setLabel("Balance");
        phone_number.setLabel("Phone");
        description.setLabel("Description");
    }
}
