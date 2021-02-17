/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;

import com.codename1.io.Log;
import com.codename1.properties.BooleanProperty;
import com.codename1.properties.IntProperty;
import com.codename1.properties.ListProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.ui.Image;
import com.ziemozi.server.ServerAPI;
import com.ziemozi.server.local.localAPI;
import java.util.ArrayList;
import java.util.List;

/*
 * @JsonPropertyOrder({
    "name",
    "description",
    "service_url",
    "apikey",
    "api_key_name",
    "api-user-name",
    "api-user",
    "_created",
    "_mock",
    "parent",
    "_changed",
    "service_attributes",
    "_children",
    "providers",
    "category",
    "_id"
})
 * @author jamesagada
 */
public class Service implements PropertyBusinessObject {
    public final Property<String,Service> _id = new Property<>("_id");
   // public final IntProperty<Service> _children = new IntProperty<>("_children");    
    public final Property<String, Service> name = new Property<>("name");
    public final Property<String, Service> description = new Property<>("description");
    public final Property<String, Service> apiKeyName = new Property<>("apiKeyName");
    public final Property<String, Service> apiKey = new Property<>("apiKey");  
    public final Property<String, Service> apiUserName = new Property<>("apiUserName");  
    public final Property<String, Service> apiUser = new Property<>("apiUser");    
    public final Property<String, Service> serviceUrl = new Property<>("serviceUrl");
    public final ListProperty<Category, Service> category
            = new ListProperty<>("category", Category.class);
    public final ListProperty<Service, Service> parent = new ListProperty<>("parent",
            Service.class);
    public final ListProperty<Provider,Service> providers = 
            new ListProperty<>("providers",Provider.class);
    public final ListProperty<Service,Service> comment_services = 
            new ListProperty<>("comment_services",Service.class);
//    public final Property<List<Provider>, Service> providers = new Property<>(
//            "providers", List.class);
    public final ListProperty<ServiceAttribute, Service> service_attributes = 
            new ListProperty<>(
            "service_attributes", ServiceAttribute.class);
    public final ListProperty<Group, Service> groups
            = new ListProperty<>("groups", Group.class);
    public final ListProperty<String,Service> logo = 
            new ListProperty<>("logo");
    public final Property<String,Service> label = new Property<>("label");
        public final Property<String,Service> _created = new Property<>("_created");
        
    public final BooleanProperty enabled = new BooleanProperty<>("enabled");
                
        
    public final PropertyIndex idx = new PropertyIndex(this, "Service",
            _id, name, description,
            apiKeyName, label,apiUser,_created, 
            enabled, apiKey,comment_services,serviceUrl,groups, 
            apiUserName, service_attributes,category,parent,providers,logo);

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }
    public Service(){
        name.setLabel("Name");
        description.setLabel("Description");
        category.setLabel("Category");
        providers.setLabel("Providers");
        service_attributes.setLabel("Attributes");
        apiKey.setLabel("API Key");
        apiKeyName.setLabel("API Key Name");
        apiUserName.setLabel("API User Name");
        apiUser.setLabel("API User");
        _created.setLabel("_created");
    }
        public void refreshIcon(){
        //populate the comments
        //comments.clear();
        ArrayList<String> aa = localAPI.getServiceIcon(this._id.get());
        //////////log.p("Comments For " + this._id.get() + " "+ aa.size() +"\n");
        if (aa != null ) {
            logo.clear();
            logo.addAll(aa);
        }else {
            logo.clear();
            logo.add("");//get default
        }
}
        public void refreshAttributes(){
        //populate the comments
        //comments.clear();
        service_attributes.set(new ArrayList<ServiceAttribute>());
        ArrayList<ServiceAttribute> aa = localAPI.getServiceAttributeForService(this._id.get());
        //////////log.p("Comments For " + this._id.get() + " "+ aa.size() +"\n");
        if (aa != null ) {
            //service_attributes.clear();
            service_attributes.addAll(aa);
        }else {
            //service_attributes.clear();
            service_attributes.add(new ServiceAttribute());
        }
}
        public void refreshCategory(){
        //populate the comments
        //comments.clear();
        //category.set(new ArrayList<Category>());
        ArrayList<Category> aa = localAPI.getServiceCategory(this._id.get());
        //////////log.p("Comments For " + this._id.get() + " "+ aa.size() +"\n");
        if (aa != null ) {
            category.clear();
            category.addAll(aa);
        }else {
            //category.clear();
            category.add(new Category());
        }
}
        public void refreshProviders(){
        //populate the comments
        //comments.clear();
        ArrayList<Provider> aa = localAPI.getServiceProvider(this._id.get());
        //////////log.p("Comments For " + this._id.get() + " "+ aa.size() +"\n");
        if (aa != null ) {
            providers.clear();
            providers.addAll(aa);
        }else{
            providers.clear();
           // providers.add(new Provider());
        }
}  
         public void refreshGroups(){
        //populate the comments
        //comments.clear();
        ArrayList<Group> aa = localAPI.getServiceGroup(this._id.get());
        ////log.p("Groups For " + this._id.get() + " "+ aa.size() +"\n");
        if (aa != null ) {
            groups.clear();
            groups.addAll(aa);
        }else{
            groups.clear();
           // providers.add(new Provider());
        }
}
        public void refreshCommentServices(){
        //populate the comments
        //comments.clear();
        ArrayList<Service> aa = localAPI.getCommentServicesFor(this._id.get());
        //////////log.p("Comments For " + this._id.get() + " "+ aa.size() +"\n");
        if (aa != null ) {
            comment_services.clear();
            comment_services.addAll(aa);
        }else{
            comment_services.clear();
            //comment_services.add(new Service());
        }
}
        public void refreshParent(){
            if (parent == null){
                parent.add(new Service());
            }else{
                parent.clear();
                if (parent.size() < 1) parent.add(new Service());
            }
        }
        public void refresh(){
            refreshCommentServices();
            refreshAttributes();
            refreshIcon();
            refreshCategory();
            refreshProviders();
            refreshParent();
            refreshGroups();
            ////////log.p("\n\n I am refreshed \n\n" + this.getPropertyIndex().toJSON());
        }
        public String getSurveyJS(){
            String survey ="";
            //create a surveyJS javascript from the service definition
            return survey;
        }
}