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
import static com.codename1.ui.CN.convertToPixels;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.URLImage;
import com.codename1.ui.plaf.Style;
import com.ziemozi.server.local.localAPI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jamesagada Provider business object "name", "description",
 * "service-url", "icon", "public-url", "default_contact", "category",
 * "services", "_created", "_changed", "parent", "_id"
 */
public class Provider implements PropertyBusinessObject {

    public final Property<String,Provider> _id = new Property<>("_id");
    public final Property<String, Provider> name = new Property<>("name");
    public final Property<String, Provider> email = new Property<>("email");
    public final Property<String, Provider> phone = new Property<>("phone");
    public final Property<String, Provider> serviceUrl = new Property<>("serviceUrl");
    public final ListProperty<ServiceContact, Provider> serviceContact
            = new ListProperty<>("serviceContact", ServiceContact.class);
    public final ListProperty<Category, Provider> category
            = new ListProperty<>("category", Category.class);
    public final ListProperty<Provider, Provider> parent = new ListProperty<>("parent",
            Provider.class);
 
    public final Property<String,Provider> rating = new Property<>("rating");
    public final ListProperty<Service, Provider> services = new ListProperty<>(
            "services", Service.class);

    public final ListProperty<String, Provider> icon = 
            new ListProperty<>("icon");
        public final Property<String,Provider> _created = new Property<>("_created");
    public final PropertyIndex idx = new PropertyIndex(this, "Provider",
            _id, name, email,
            phone, serviceUrl, _created, serviceContact,rating, category,parent,services,icon);

    @Override
    public PropertyIndex getPropertyIndex() {
    return idx;
            }
    public Provider(){
        name.setLabel("Name");
        email.setLabel("E-Mail");
        phone.setLabel("Phone");
        serviceUrl.setLabel("ServiceUrl");
        serviceContact.setLabel("Default Contact");
        parent.setLabel("Parent");
        icon.setLabel("Logo");
        _created.setLabel("_created");
    }
        public void refreshIcon(){
        //populate the comments
        //comments.clear();
        ArrayList<String> aa = localAPI.getProviderIcon(this._id.get());
        ////////////Log.p("Comments For " + this._id.get() + " "+ aa.size() +"\n");
        if (aa != null ) {
            icon.clear();
            icon.addAll(aa);
        }
    }
    public String servicelist() {
        refresh();
        String sl="";
        for (Service s:services){
           sl=sl+s.name.get()+",";
        }
        for (Category c:category) {
            sl=sl+c.name.get() +",";
        }
        return sl;
    }
    public String summary() {
    String s = "";
     //format the service contact details for display in a richview component
      String hdr = "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n" +
            "<doc>\n" + "<body>"  ;            
     hdr = hdr + "<p uiid=\"headline\">  " + this.name.get()+ "  </p>";
     String desc = "<p> Attending to " + servicelist() + "</p>";
     String contact = "<p> Telephone: " + phone.get() + ", Email: " + email.get() + ", ServiceUrl: " 
             + serviceUrl.get() + "</p>";
  
      String rating = "<p> Service is rated at  " + this.rating.get() + " stars</p>";    
             
     return hdr + desc + contact +rating ;
    }

    public void refresh() {
        //refresh servicecontacts
        //refresh services
        //refresh categories
        refreshCategories();
        refreshServices();
    }
    public void refreshServices(){

        ArrayList<Service> aa = localAPI.getServiceForProvider(this._id.get());
        if (aa != null ) {
            services.clear();
            services.addAll(aa);
        }
    }
    public void refreshCategories(){
          ArrayList<Category> aa = localAPI.getCategoryForProvider(this._id.get());
        if (aa != null ) {
            category.clear();
            category.addAll(aa);
        }      
    }
    public Image getAvatarMask(int size) {
        Image temp = Image.createImage(size, size, 0xff000000);
        Graphics g = temp.getGraphics();
        g.setAntiAliased(true);
        g.setColor(0xffffff);
        g.fillArc(0, 0, size, size, 0, 360);
        return temp;
    }

    public Image getAvatar(float imageSize) {
        //////////Log.p("User getting Avatar from " + avatar.get());
        refreshIcon();
        String filename = "providert-"+name.get();
        int size = convertToPixels(imageSize);
        Image temp = getAvatarMask(size);
        Object mask = temp.createMask();
        Style s = new Style();
        s.setFgColor(0xc2c2c2);
        s.setBgTransparency(255);
        s.setBgColor(0xe9e9e9);
        FontImage x = FontImage.createMaterial(
                FontImage.MATERIAL_PERSON, s, size);
        Image avatarImg = x.fill(size, size);
        if (avatarImg instanceof FontImage) {
            avatarImg = ((FontImage) avatarImg).toImage();
        }
        avatarImg = avatarImg.applyMask(mask);
        ////////Log.p("Provider logo" + icon.toString());
        if ((icon.size() > 0) ) {
            ////////Log.p("Icon " + icon.get(0).toString());
            
           //if   (logo.get(0).indexOf("http") >= 0 )
            return URLImage.createToStorage(
                    EncodedImage.createFromImage(avatarImg, false),
                    filename,
                    localAPI.mediaUrl(icon.get(0)),
                    URLImage.createMaskAdapter(temp));
        }       
        return avatarImg;
    }


    public String fullAddress() {
        return "Phone " + phone + "Email " + email + "Service Url " + serviceUrl;
    }
}
