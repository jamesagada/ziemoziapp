/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.properties.BooleanProperty;
import com.codename1.properties.IntProperty;
import com.codename1.properties.Property;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import java.util.ArrayList;

/**
 *
 * @author jamesagada
 * * @JsonPropertyOrder({
    "_id",
    "description",
    "type_of-attribute", -- attributetypeobject
    "multiplicity",
    "required",
    "help_text",
    "display_label",
    "action_url",
    "default_value",
    "display_group",
    "display_sequence",
    "name",
    "minimum_size",
    "maximum_size",
    "watch_this_attribute", -- serviceattribute reference
    "watch_formula", -- json string
    "child_attributes", -- the child attributes of this attribute
    "_parent_id", -- its own parent
    "_parent_def",
    "_parent_field",
    "_created",
    "_changed",
    "option_list" - list of items if this was going to be displayed as a pick list
})
 */
public interface BaseEditor{
    //we will have a label for the component
    //and also have the various default parameters which can be set when it is being created
    //we will also have a function to create an editor from a serviceAttribute
    //
    //the serviceAttribute being edited
   
    Container edit(ServiceAttribute attr) ;
    Container view(ServiceAttribute attr);//edit or view from service attribute
    Container view(RequestParameter req); //
    Container edit(RequestParameter req); //edit from request parameter
    void createRequestParameter(ServiceAttributeType serviceType);
    void createRequestParameter();
    void checkWatch();
    RequestParameter getRequestParameter();//retrieve the request parameter for this attribute
    void  executeWatch(BaseEditorImpl b);
    void updateList();//if it is a list, update the list with new information.
    void setEditorConstraints();
    ServiceAttribute getServiceAttribute();
    void setupOptions(Object o);
    void setValue(Object v);
    void setRelevance(Object v);
    void setUpOptions();
    void setSummary();
}
