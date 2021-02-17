/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;

import com.codename1.components.ToastBar;
import com.codename1.io.Log;
import com.codename1.properties.IntProperty;
import com.codename1.properties.ListProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.ziemozi.server.ServerAPI;
import com.ziemozi.server.local.localAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jamesagada
 * @JsonPropertyOrder({
    "_id",
    "service_attribute",
    "value",
    "_parent_id",
    "_parent_def",
    "_parent_field",
    "_created",
    "_changed"
})
 */
public class RequestParameter implements PropertyBusinessObject,Comparable{
    public final Property<String,RequestParameter> _id = new Property<>("_id");
    public final Property<String, RequestParameter> value = new Property<>("value");
    public final Property<String,RequestParameter> _parent_id = new Property<>("_parent_id");
    public final Property<String,RequestParameter> _parent_def= new Property<>("_parent_def");
    public final Property<String,RequestParameter> _parent_field = new Property<>("_parent_field");
    public final Property<String,RequestParameter> summary = new Property<>("summary");
        public final Property<String,RequestParameter> _created = new Property<>("_created");
    public final ListProperty<ServiceAttribute, RequestParameter> service_attribute = 
            new ListProperty<>("service_attribute",ServiceAttribute.class);
    public final PropertyIndex idx = new PropertyIndex(this, "RequestParameter",
            _id, service_attribute,_parent_id, _created,summary,_parent_def,_parent_field,value);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
 
    }
        public RequestParameter(){
        value.setLabel("Value");
        service_attribute.setLabel("Attribute");
        _parent_def.set("request");
        _parent_field.set("request_parameters");
    }

    Boolean save() {
        Boolean post = true;
        if (value.get() != null ) {
        ////////log.p("\nOriginal Rp \n" + this.getPropertyIndex().toString());
        Map<String, Object> m = this.getPropertyIndex().toMapRepresentation();
        m.put("service_attribute", this.service_attribute.get(0).getPropertyIndex().toMapRepresentation());
        m.put("_parent_id",this._parent_id.get());
        //
        //////////log.p(" Summary is " + this.service_attribute.get().getSummary(value.get()));
        m.put("summary",this.service_attribute.get(0).getSummary(value.get()));
        RequestParameter rp = new RequestParameter();
        rp.getPropertyIndex().populateFromMap(m);
        ////////log.p("\nrequest parameter to save \n" + rp.getPropertyIndex().toString());
        post = ServerAPI.postRequestParameter(rp);
        ToastBar.showInfoMessage("Request Parameter" + this.service_attribute.get(0).name.get()+" Saved");
        }
    return post;    
    }
    Map<String,Object> asMap() {
        Map<String, Object> m = this.getPropertyIndex().toMapRepresentation();        
        if (value.get() != null ) {
        ////////log.p("\nOriginal Rp \n" + this.getPropertyIndex().toString());
        m.put("service_attribute", this.service_attribute.get(0).getPropertyIndex().toMapRepresentation());
        m.put("_parent_id",this._parent_id.get());
        //
        //////////log.p(" Summary is " + this.service_attribute.get().getSummary(value.get()));
        m.put("summary",this.service_attribute.get(0).getSummary(value.get()));
        //RequestParameter rp = new RequestParameter();
        //rp.getPropertyIndex().populateFromMap(m);
        //////////log.p("\nrequest parameter to save \n" + rp.getPropertyIndex().toString());
        } 
        return m;
    }

    public String summarize() {
        ServiceAttribute s = this.service_attribute.get(0);
        if ((s._id == null) || (s == null)) return null;
        this.summary.set(s.getSummary(value.get()));
        return this.summary.get();
        
    }
   @Override
  public int compareTo(Object o) {
      //we use this to sort RequestParameters
      RequestParameter c = (RequestParameter)o;
      //this.refreshServiceAttribute();
      //c.refreshServiceAttribute();
      int result =1;
      //////log.p("This attribute " + this.getPropertyIndex().toJSON());
      try {
            //////log.p("Comparing attribute " + c.getPropertyIndex().toJSON());    
      result = this.service_attribute.get(0)._id.get().compareTo(
              c.service_attribute.get(0)._id.get());
      }catch(Exception e){
          //e.printStackTrace();
          result =1;
      }
  return  result;
 }

      public void refreshServiceAttribute(){
        ArrayList<ServiceAttribute> sa = new ArrayList<ServiceAttribute>();
        if (this.service_attribute.size() > 0) {
            for (Object os:this.service_attribute){
                if (os.getClass().getCanonicalName().contains("String")) {
                    // it is a string and we need to turn it into a service attrivute
                    Log.p("Service Attribute " + os.toString());
                    sa.add(localAPI.getServiceAttribute(os.toString()));
                }
            }
        }
       //Log.p("Request Parameter Id Is "  + this._id.get());
       //ArrayList<ServiceAttribute> aa = localAPI.getServiceAttributesForParameter(this._id.get());
        //service_attribute.clear();
        if (sa != null){
            service_attribute.clear();
            service_attribute.addAll(sa);
        }
        
    }  
      public ArrayList<String> validate(){
          //Log.p("Validating " + this.service_attribute.asList().get(0).description);
          ArrayList<String> errors = new ArrayList<String>();
          //whether the parameter is required. If so, its value cannot be null
          //this.refreshServiceAttribute();
          Log.p("Attribute for value " + this.service_attribute.size());
          if (this.service_attribute.size() > 0){
              Boolean required = this.service_attribute.get(0).required.getBoolean();
              ////log.p(this.service_attribute.get(0).display_label.get()  +  "is required " + required);
              if (required) {
                  if (this.value.get() != null) {
                      if (this.value.get().equalsIgnoreCase("NULL")) {
                          errors.add(this.service_attribute.get(0).display_label.get() + " is required");
                      }
                  } else {
                      errors.add(this.service_attribute.get(0).display_label.get() + " is required");
                  }
              }
              //check the size limits
              //check the dependencies if any and confirm
          }
          return errors;
      }
}