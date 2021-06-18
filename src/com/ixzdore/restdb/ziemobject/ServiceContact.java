/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;

import com.codename1.components.ToastBar;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.properties.DoubleProperty;
import com.codename1.properties.IntProperty;
import com.codename1.properties.ListProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import static com.codename1.ui.CN.convertToPixels;
import static com.codename1.ui.CN.createStorageInputStream;
import static com.codename1.ui.CN.deleteStorageFile;
import static com.codename1.ui.CN.existsInStorage;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.URLImage;
import com.codename1.ui.plaf.Style;
import com.ziemozi.server.local.localAPI;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 *
 * @author jamesagada
    @JsonProperty("_id")
    @JsonProperty("name")
    @JsonProperty("phoneNumber")
    @JsonProperty("emailAddress")
    @JsonProperty("street")
    @JsonProperty("city")
    @JsonProperty("country")
    @JsonProperty("state")
    @JsonProperty("service_url")
    @JsonProperty("public_url")
    @JsonProperty("twitter")
    @JsonProperty("facebook")
    @JsonProperty("latitude")
    public double latitude;
    @JsonProperty("longitude")
    public double longitude;
    @JsonProperty("zone_of_authority")
    public int zoneOfAuthority;
    @JsonProperty("providers")
    public List<Provider> providers = null;
    @JsonProperty("services")
    public List<Service_> services = null;
    @JsonProperty("_created")
    public String created;
    @JsonProperty("_changed")
    public String changed;
    @JsonProperty("_createdby")
    public String createdby;
    @JsonProperty("_changedby")
    public String changedby;
    @JsonProperty("_keywords")
    public List<String> keywords = null;
    @JsonProperty("_tags")
    public String tags;
    @JsonProperty("_version")
    public int version;
 */
public class ServiceContact implements PropertyBusinessObject{
    public final Property<String,ServiceContact> _id = new Property<>("_id");   
    public final Property<String,ServiceContact> _created = new Property<>("_created");     
    public final Property<String, ServiceContact> name = new Property<>("name");
    public final ListProperty<String,ServiceContact> logo = new ListProperty("logo");
    public final Property<String, ServiceContact> description = new Property<>("description");
    public final Property<String, ServiceContact> apiKeyName = new Property<>("apiKeyName");
    public final Property<String, ServiceContact> apiKey = new Property<>("apiKey");  
    public final Property<String, ServiceContact> apiUserName = new Property<>("apiUserName");  
    public final Property<String, ServiceContact> apiUser = new Property<>("apiUser");    
    public final Property<String, ServiceContact> serviceUrl = new Property<>("serviceUrl");
    public final Property<String, ServiceContact> public_url = new Property<>("public_url");
    //statistics
    public final Property<String,ServiceContact> likes = new Property<>("likes");
    public final Property<String,ServiceContact> comments = new Property<>("comments");
    //contact
    public final Property<String,ServiceContact> emailaddress = new Property<>("emailaddress");
    public final Property<String, ServiceContact> phonenumber = new Property<>("phonenumber");
    public final Property<String,ServiceContact> street = new Property<>("street");
    public final Property<String,ServiceContact> city = new Property<>("city");
    public final Property<String,ServiceContact> postCode = new Property<>("postCode");
    public final Property<String,ServiceContact> state = new Property<>("state");
    public final Property<String,ServiceContact> country = new Property<>("country");
    //location
    public final Property<String,ServiceContact> latitude = new Property<>("latitude");
    public final Property<String,ServiceContact> longitude = new Property<>("longitude");
    //socialmedia
    public final Property<String,ServiceContact> twitter = new Property<>("twitter"); 
    public final Property<String,ServiceContact>facebook = new Property<>("facebook");
    //authority
    public final IntProperty<ServiceContact> zoneOfAuthority = new IntProperty<>("zoneOfAuthority");
    //providers attached to
    public final ListProperty<Provider, ServiceContact> providers = new ListProperty(
            "providers", Provider.class);
    public final ListProperty<Service, ServiceContact> services = new ListProperty(
            "services", Service.class);
        public final ListProperty<Category, ServiceContact> categories = new ListProperty(
            "categories", Category.class);

    public final Property<String,ServiceContact> rank = new Property<>("rank");   
 
        
    public final PropertyIndex idx = new PropertyIndex(this, "ServiceContact",
            _id, name, description,
            apiKeyName, apiUser, apiKey,public_url,
            serviceUrl, apiUserName, 
            services,providers,emailaddress,categories,
            phonenumber,street,city,postCode,state,country,latitude,longitude,
            zoneOfAuthority,twitter,logo,_created,facebook,rank);

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }
    public ServiceContact(){
        name.setLabel("Name");
        description.setLabel("Description");
        providers.setLabel("Providers");
        services.setLabel("Services");
        apiKey.setLabel("API Key");
        apiKeyName.setLabel("API Key Name");
        apiUserName.setLabel("API User Name");
        apiUser.setLabel("API User");
        zoneOfAuthority.setLabel("Zone of Authority");
        twitter.setLabel("Twitter Handle");
        facebook.setLabel("Facebook Name");
        city.setLabel("City");
        country.setLabel("Country");
        street.setLabel("Street");
        postCode.setLabel("Post Code");
        longitude.setLabel("Longitude");
        latitude.setLabel("Latitude");
        phonenumber.setLabel("Phone");
        emailaddress.setLabel("Email");
        rank.setLabel("Rating");
    }
 public String summary(){
     //////Log.p(this.getPropertyIndex().toString(true));
     String s = "";
     //format the service contact details for display in a richview component
      String hdr = "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n" +
            "<doc>\n" + "<body>"  ;            
     hdr = hdr + "<p uiid=\"headline\">  " + this.name.get()+ "  </p>";
     String desc = "<p>  Attending to " + servicelist() + " for " + providerlist() + "</p>";
     String addr = "<p> Operating From " + street.get() + ", " + city.get() + ", " + 
             state.get() + ", " + postCode.get() +", " + country.get() + "</p>";
     String contact = "<p> Telephone: " + phonenumber.get() +". Email:  " + emailaddress.get() + ", Twitter: " + twitter.get() + ", Facebook: " 
             + facebook.get() + "</p>";
     String logos = servicelogos() + providerlogos();
      String rating = "<p> Service is rated at  " + this.rank.get() + " stars</p>";    
             
     return hdr + desc + addr + contact +rating;// + logos;
 }
    public String extendedDescription(){
        return  "Attending to " + servicelist() + " for " + providerlist() + "</p>";

    }
    public String fullAddress(){
        return  street.get() + ", " + city.get() + ", " + 
             state.get() + ", " + postCode.get() +", " + country.get() + " Telephone: " + phonenumber.get() + ", Twitter: " + twitter.get() + ", Facebook: " 
             + facebook.get();
    }
    public String servicelist() {
        String sl="";
        for (Service s:services){
           sl=sl+s.name.get()+",";
        }
        for (Category c:categories){
            sl=sl+c.name.get()+",";
        }
        return sl;
    }

    private String providerlist() {
        String pl="";
        for (Provider p:providers){
            pl=pl+p.name.get();
        }
        return pl;
      }

    private String servicelogos() {
        String sl="";
        for (Service s:services){
            s.refreshIcon();
            if (s.logo.get(0) != null ) {
                if (s.logo.get(0).length() > 0) {
                    if (sl.lastIndexOf("img") < 0) {
                        sl="<carousel> <img src=" + '"'   + localAPI.mediaUrl(s.logo.get(0)) + '"'+ " />";
                    }else{
                        sl = sl + " " + "<img src=" + '"'   + localAPI.mediaUrl(s.logo.get(0)) + '"'+ " />";
                    }
                }
            }
            
        }
        if (sl.lastIndexOf("img") > 0 ) sl = sl + "</carousel>";
        return sl;
    }

    private String providerlogos() {
         String sl="";
        for (Provider p:providers){
            p.refreshIcon();
            if (p.icon.get(0) != null ) {
                if (p.icon.get(0).length() > 0) {
                    if (sl.lastIndexOf("img") < 0) {
                        sl="<carousel> <img src=" + '"'   + localAPI.mediaUrl(p.icon.get(0)) + '"'+ " />";
                    }else{
                        sl = sl + " " + "<img src=" + '"'   + localAPI.mediaUrl(p.icon.get(0)) + '"'+ " />";
                    }
                }
            }
            
        }
        if (sl.lastIndexOf("img") > 0 ) sl = sl + "</carousel>";
        return sl;
    }
    public void refresh(){
        //refresh the service-contact
        refreshService();
        refreshProvider();
        refreshIcon();
        refreshCategory();
        
    }
    public void refreshCategory(){

        ArrayList<Category> aa = localAPI.getCategoryForServiceContact(this._id.get());
        if (aa != null ) {
            categories.clear();
            categories.addAll(aa);
        }
    }    
    public void refreshService(){

        ArrayList<Service> aa = localAPI.getServiceForServiceContact(this._id.get());
        if (aa != null ) {
            services.clear();
            services.addAll(aa);
        }
    }
    public void refreshIcon(){
        ArrayList<String> aa = localAPI.getServiceContactIcon(this._id.get());
        if (aa != null ) {
            logo.clear();
            logo.addAll(aa);
        }
    }
    public void refreshProvider(){

        ArrayList<Provider> aa = localAPI.getProviderForServiceContact(this._id.get());
        if (aa != null ) {
            providers.clear();
            providers.addAll(aa);
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
        String filename = "contact-"+name.get();
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
        ////////Log.p("ServiceContact logo" + logo.toString());
        if ((logo.size() > 0) ) {
            ////////Log.p("Logo " + logo.get(0).toString());
            
           //if   (logo.get(0).indexOf("http") >= 0 )
            return URLImage.createToStorage(
                    EncodedImage.createFromImage(avatarImg, false),
                    filename,
                    localAPI.mediaUrl(logo.get(0)),
                    URLImage.createMaskAdapter(temp));
        }       
        return avatarImg;
    }

    public void refreshComments() {
        //calculate the number of comments
    }

    public ArrayList<Category> getCategoryList() {
        //Get the list of categories that this contact is attached to
        //either through themselves or through their providers
        ArrayList<Category>c = new ArrayList<Category>();
        //add my categories
        c.addAll(categories.asList());
        //loop through my providers and add all the categories of each one
        for (Provider p:providers) {
            p.refreshCategories();
            for (Category cx:p.category) {
                 if (!c.contains(cx)) c.add(cx);
            }
        }
        return c;
    }
    
}
