/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;
import com.codename1.io.Log;
import com.codename1.properties.IntProperty;
import com.codename1.properties.ListProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.ui.Image;
import com.ziemozi.server.local.localAPI;
import java.util.ArrayList;
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
public class Category implements PropertyBusinessObject{
    public final Property<String,Category> _id = new Property<>("_id");
    public final Property<String, Category> name = new Property<>("name");
    public final Property<String, Category> description = new Property<>("description");
    public final ListProperty<String, Category> icon = new ListProperty<>("icon");
    public final Property<String,Category> _created = new Property<>("_created"); 
    public final Property<String,Category> isgroup = new Property<>("isgroup");
    public final Property<String,Category> inmenu = new Property<>("inmenu");    
        public final ListProperty<Category, Category> parent
            = new ListProperty<>("parent", Category.class);
        
 
        
        
        
        
    public final PropertyIndex idx = new PropertyIndex(this, "Category",
            _id, name,_created, icon,description,parent,isgroup,inmenu);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
 
    }
        public Category(){
        name.setLabel("Name");
        description.setLabel("Description");
        icon.setLabel("Icon");
        _created.setLabel("_created");
    }
    public void refreshIcon(){
        //populate the comments
        //comments.clear();
        ArrayList<String> aa = localAPI.getCategoryIcon(this._id.get());
        //////////log.p("Comments For " + this._id.get() + " "+ aa.size() +"\n");
        if (aa != null ) {
            icon.clear();
            icon.addAll(aa);
        }
    }
    public void refresh(){
        refreshIcon();
        ArrayList<Category> aa = localAPI.getCategoryParent(this._id.get());
        if (aa != null ) {
            parent.clear();
            parent.addAll(aa);
        }
    }

  
}
