package com.ixzdore.restdb.ziemobject;

import com.codename1.components.ToastBar;
import com.ziemozi.server.ServerAPI;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.properties.BooleanProperty;
import com.codename1.properties.ListProperty;
import com.codename1.properties.LongProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import static com.codename1.ui.CN.*;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.URLImage;
import com.codename1.ui.plaf.Style;
import com.codename1.util.SuccessCallback;
import com.ziemozi.server.local.localAPI;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
//import java.util.logging.Level;
//import java.util.logging.Logger;

public class User implements PropertyBusinessObject {
   /** 
  "ziemozi_user":[  
      {  
         "_id":"5b1a39c4aec6963800000648",
         "subscriptions":[  ],
         "requests":[  ],
         "email":"okwui@okwuiagada.com",
         "friendRequests":[  ],
         "password":"ixzdore",
         "active":true,
         "groups":[  ],
         "peopleYouMayKnow":[  ],
         "friends":[  ],
         "_created":"2018-06-08T08:09:40.846Z",
         "_changed":"2018-06-19T07:47:06.511Z",
         "birthday":1528820661556,
         "gender":"Male",
         "authtoken":"5b1a39c4aec6963800000648",
         "avatar":"file:///var/folders/mh/m7x9vrjx7dg5ypsnsfnnz16r0000gn/T/temp694464739846559369..JPG",
         "firstName":"okwuixided",
         "phone":"+2348034021268",
         "familyName":"Agada",
         "cover":"No access"
      }
   ]
   */
    public final Property<String, User> _id = new Property<>("_id");
    public final Property<String, User> firstName = new Property<>("firstName");
    public final Property<String, User> familyName = new Property<>("familyName");
    public final Property<String, User> email = new Property<>("email");
    public final Property<String, User> phone = new Property<>("phone");
    public final Property<String, User> gender = new Property<>("gender");
    public final LongProperty<User> birthday
            = new LongProperty<>("birthday");
    public final LongProperty<User> home_latitude
            = new LongProperty<>("home_latitude");
    public final LongProperty<User> home_longitude
            = new LongProperty<>("home_longitude");

    public final Property<String, User> avatar = new Property<>("avatar");
    public final Property<String, User> cover = new Property<>("cover");
    public final ListProperty<User, User> friends
            = new ListProperty<>("friends", User.class);
    public final ListProperty<Request, User> requests
            = new ListProperty<>("requests", Request.class);
    public final ListProperty<Subscription, User> subscriptions
            = new ListProperty<>("subscriptions", Subscription.class);
    public final ListProperty<UserPreference, User> preferences
            = new ListProperty<>("preferences", UserPreference.class);    
    public final ListProperty<Group, User> groups
            = new ListProperty<>("groups", Group.class);
    public final ListProperty<Request, User> following
            = new ListProperty<>("following", Request.class);
    public final ListProperty<Provider, User> following_providers
            = new ListProperty<>("following_providers", Provider.class);
    public final ListProperty<Category, User> following_categories
            = new ListProperty<>("following_categories", Category.class);
    public final ListProperty<Wallet, User> wallets
            = new ListProperty<>("wallets", Wallet.class);
    public final ListProperty<User, User> friendRequests
            = new ListProperty<>("friendRequests", User.class);
    public final ListProperty<User, User> peopleYouMayKnow
            = new ListProperty<>("peopleYouMayKnow", User.class);
    public final Property<String, User> authtoken = new Property<>("authtoken");
    public final Property<String, User> password = new Property<>("password");
    //
    public final BooleanProperty<User> active = new BooleanProperty<>("active");
    public final Property<Date, User> birthdayDate
            = new Property<Date, User>("birthdayDate", Date.class) {
        @Override
        public Date get() {
            if (birthday.get() == null) {
                return null;
            }
            return new Date(birthday.get());
        }

        @Override
        public User set(Date value) {
            if (value == null) {
                return birthday.set(null);
            }
            return birthday.set(value.getTime());
        }
    };

    private final PropertyIndex idx = new PropertyIndex(this, "User",
            _id, firstName, familyName, email, phone, gender, avatar, cover,
            birthday, birthdayDate, friends, friendRequests,
            peopleYouMayKnow, authtoken,following,following_categories,
            following_providers, password, wallets,preferences,active,home_longitude,home_latitude,groups,requests,subscriptions);

    public User() {
        firstName.setLabel("First Name");
        familyName.setLabel("Family Name");
        email.setLabel("E-Mail");
        phone.setLabel("Phone");
        gender.setLabel("Gender");
        birthdayDate.setLabel("Birthday");
        password.setLabel("Password");
        active.setLabel("Activated");
        avatar.setLabel("Avatar");
        idx.setExcludeFromJSON(birthdayDate, true);
        idx.setExcludeFromMap(birthdayDate, true);
    }

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }
    public void refresh(){
        //update the friends etc
        //refresh preferences
        //retrieve record from database and then
        //////log.p("refreshing services in request" );
        ArrayList<UserPreference> up = localAPI.getUserPreferenceFor(this._id.get());
        //request_parameters.clear();
        //service.set(new ArrayList<Service>());
        if (up != null){
            preferences.clear();
            preferences.addAll(up);
        }else{
            //service.add(new Service());
        }
        refreshGroups();
             
    }
        public void refreshGroups(){
        //populate the comments
        //comments.clear();
        ArrayList<Group> aa = localAPI.getUserGroup(this._id.get());
        ////log.p("Groups For " + this.fullName() + " is "+ aa.size() +"\n");
        if (aa != null ) {
           groups.clear();
            groups.addAll(aa);
        }else{
            groups.clear();
           // providers.add(new Provider());
        }
}    
    public String fullName() {
        return firstName.get() + " " + familyName.get();
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
        ////log.p("User getting Avatar from " + avatar.get());
        String filename = avatar.get() + "";
        //////log.p(" IndexOf File in filename " + filename.indexOf("file:"));
        if (filename.indexOf("file:") >= 0){
            try {
                //return Image.createImage(filename);
                return Image.createImage(FileSystemStorage.getInstance().openInputStream(filename));
            } catch (IOException ex) {
                ////////log.p(ex.getMessage());
                ToastBar.showErrorMessage("Failed to load avatar.");
            }
        }
        
        if (existsInStorage(filename) && filename.length() > 1) {

            try (InputStream is
                    = createStorageInputStream(filename);) {
                return Image.createImage(is);
            } catch (IOException err) {
                Log.e(err);
                //deleteStorageFile(filename);
            }
        }
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
        if ((avatar.get() != null)) {
           if   (avatar.get().indexOf("http") >= 0 )
            return URLImage.createToStorage(
                    EncodedImage.createFromImage(avatarImg, false),
                    filename,
                    avatarUrl(),
                    URLImage.createMaskAdapter(temp));
        }       
        return avatarImg;
    }

    public String coverUrl() {
        
        return cover.get();
    }

    public String avatarUrl() {
        //
        return avatar.get();
        //we should save the image using cloudinary and therefore the content of 
        // this will be the actual URL of the image
    }

    public void fetchCoverImage(SuccessCallback<Image> c) {
        if ((cover.get() != null) && (cover.get().indexOf("http") > 0)) {
            ConnectionRequest cr = new ConnectionRequest(coverUrl(), false);
            cr.downloadImageToStorage(cover.get(), c);
        }
    }

    public String location() {
        return home_latitude +"," +home_longitude;
     }
}
