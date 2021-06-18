//this is the server class that is called 
//we implement the same functions as in the local and 
//the online
//but this is where we implement the logic to determine whether to 
//get from the server or to get from the local.
//The rest of the app can continue to call services fom here and 
//not care whether it is being served locally or from online.
package com.ziemozi.server;

import ca.weblite.codename1.db.DAOProvider;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.codename1.components.ToastBar;
import com.codename1.contacts.Contact;
import com.codename1.db.Cursor;
import com.codename1.db.Database;
import com.codename1.io.File;
import com.codename1.io.FileSystemStorage;
import com.ixzdore.restdb.ziemobject.Comment;
import com.ixzdore.restdb.ziemobject.Fare;
import com.ixzdore.restdb.ziemobject.Notification;
import com.ixzdore.restdb.ziemobject.Post;
import com.ixzdore.restdb.ziemobject.Route;
import com.ixzdore.restdb.ziemobject.RouteStops;
import com.ixzdore.restdb.ziemobject.Stop;
import com.ixzdore.restdb.ziemobject.User;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.MultipartRequest;
import com.codename1.io.Preferences;
import com.codename1.io.Storage;
import com.codename1.io.Util;
import com.codename1.io.rest.RequestBuilder;
import com.codename1.io.rest.Response;
import com.codename1.io.rest.Rest;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.properties.PropertyBase;
import com.codename1.properties.PropertyBusinessObject;

import ca.weblite.codename1.json.JSONException;
import static com.codename1.ui.CN.*;

import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Image;
import com.codename1.ui.URLImage;
import com.codename1.ui.plaf.Style;
import com.codename1.util.Callback;
import com.codename1.util.StringUtil;
import com.codename1.util.SuccessCallback;
import com.ixzdore.restdb.ziemobject.Category;
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.ixzdore.restdb.ziemobject.ServiceContact;
import com.ixzdore.restdb.ziemobject.Group;
import com.ixzdore.restdb.ziemobject.Wallet;
import com.ziemozi.server.local.localAPI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class ServerAPI {

    private static User me;
    public static final String BASE_URL = "https://ziemozi-a3ef.restdb.io/rest/";
    public static final String BASE_URL_STATIC = "https://ziemozi-a3ef.restdb.io/";
    public static String API_KEY = "5a0755c03d5e3147a77ba2486df4ea34e6b59";
    public static String AUTHY = "3IMDN8qW1tTuLdI7h8FMgjaw9YdVVQFe";
    public static final String authyUrl = "https://api.authy.com/protected/json/phones/verification/check";
    private static String token;
    private static final String sosService = "SOS";
    private static final String amFineService = "IAMFINE";
    private static final String likeService = "LIKE";
    private static final int aroundMeDistance = 10;//Distance in km to consider as around me
    public static Database db = null;
    public static DAOProvider dbProvider = null;
    public static final String dbConfig = "/setup.sql";
    public static final String dbname = "ziemozi";

    private static final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "okwui",
            "api_key", "839478323249426",
            "api_secret", "2sKLEGlDj_qwtJGHsifS9fZ_m8I"));

    // Disable private CDN URLs as this doesn't seem to work with free accounts
    private static RequestBuilder verifyGet(String path) {
        return Rest.get(path).
                header("X-Authy-API-Key", AUTHY).jsonContent();
    }

    private static RequestBuilder get(String path) {
        if (token != null) {
            return Rest.get(BASE_URL + path).
                    header("auth", token).header("x-apikey", API_KEY).jsonContent();
        }
        return Rest.get(BASE_URL + path).header("x-apikey", API_KEY).jsonContent();
    }

    private static RequestBuilder post(String path) {
        if (token != null) {
            return Rest.post(BASE_URL + path).
                    header("auth", token).header("x-apikey", API_KEY).jsonContent();
        }
        return Rest.post(BASE_URL + path).header("x-apikey", API_KEY).jsonContent();

    }

    private static RequestBuilder put(String path) {
        if (token != null) {
            return Rest.put(BASE_URL + path).
                    header("auth", token).header("x-apikey", API_KEY).jsonContent();
        }
        return Rest.post(BASE_URL + path).header("x-apikey", API_KEY).jsonContent();

    }

    private static RequestBuilder patch(String path) {
        if (token != null) {
            return Rest.patch(BASE_URL + path).
                    header("auth", token).header("x-apikey", API_KEY)
                    .header("X-HTTP-Method-Override", "PATCH").jsonContent();
        }
        return Rest.patch(BASE_URL + path).header("x-apikey", API_KEY).
                header("X-HTTP-Method-Override", "PATCH").jsonContent();

    }
    /*
    private static RequestBuilder patch(String path) {
        if (token != null) {
            return Rest.post(BASE_URL + path).
                    header("auth", token).header("x-apikey", API_KEY)
                    .header("X-HTTP-Method-Override", "PATCH").jsonContent();
        }
        return Rest.post(BASE_URL + path).header("x-apikey", API_KEY).
                header("X-HTTP-Method-Override", "PATCH").jsonContent();

    }

     */
    public static boolean isLoggedIn() {
        //ServerAPI.refreshMe();
        boolean loggedIn = false;
        if (me != null) {
            //////////////////Log.p(me.getPropertyIndex().toString());
            if ((me.authtoken.get() != null) && (me.firstName.get() != null)) {
                loggedIn = true;
            }
        }
        token = Preferences.get("authtoken", null);
        ////////////////////Log.p("token " + token);
        return loggedIn;
    }

    public static void login(User u, Callback<User> callback) {
        //signupOrLogin("user/login", u, callback);
        //to login, we look for the combination of 
        //password and phone number
        String fetchUrl = "{\"phone\": \""
                + u.phone.get() + "\" ," + "\"password\": \"" + u.password.get() + "\" }";

        //////////////////Log.p(fetchUrl);
        User s = null;

        ArrayList<User> users = genericZiemSearch("ziemozi-users",
                User.class,
                fetchUrl, 0, 10, "", "");
        if (users == null) {
            callback.onError(null, null, 402, "Failed login");
        } else {
            if ((users.size() > 1) || (users.size() < 1)) {
                callback.onError(null, null, 402, "Too many records");
            }

            s = users.get(0);
            s.authtoken.set(s._id.get());
            Preferences.set("authtoken", s.authtoken.
                    get());
            token = s.authtoken.get();
            s.getPropertyIndex().storeJSON("me.json");
            me = s;
            //////Log.p(s.getPropertyIndex().toJSON());
            loadUsers(me._id.get());
                        callback.onSucess(me);

            //signupOrLogin("ziemozi-users", u, callback);
        }
    }

    private static void signupOrLogin(String url, User u,
            final Callback<User> callback) {
        //////////////////Log.p(u.getPropertyIndex().toString());
        //////////////////Log.p("URL " + url);
        post(url).
                body(u.getPropertyIndex().toJSON()).
                getAsJsonMap(new Callback<Response<Map>>() {
                    @Override
                    public void onSucess(Response<Map> value) {
                        //////////////////Log.p("Response Code " + value.getResponseCode());
                        //////////////////Log.p(value.getResponseData().toString());
                        if (value.getResponseCode() != 201) {
                            callback.onError(u, null, value.
                                    getResponseCode(),
                                    "Login Error");
                            return;
                        }
                        me = new User();
                        me.getPropertyIndex().
                                populateFromMap(value.getResponseData());
                        me.authtoken.set(me._id.get());
                        Preferences.set("authtoken", me.authtoken.
                                get());
                        token = me.authtoken.get();
                        me.getPropertyIndex().storeJSON("me.json");
                        //////////////////Log.p(me.getPropertyIndex().toString());
                        callback.onSucess(me);
                        callSerially(() -> registerPush());
                    }

                    @Override
                    public void onError(Object sender, Throwable err,
                            int errorCode, String errorMessage) {
                        callback.onError(sender, err, errorCode,
                                errorMessage);
                    }
                });
    }

    public static void refreshMe() {
        if (me == null) {
            me = new User();
            me.getPropertyIndex().loadJSON("me.json");
        }
        //////////////////Log.p("Refreshing Me " + me.getPropertyIndex().toString());
        if ( me._id.get() != null){
        Response<Map> map = get("ziemozi-users/" + me._id.get()).getAsJsonMap();
        //////////////////Log.p("Refreshing user " + map.getResponseData().toString());
        //////////////////Log.p("Refreshing User " + map.getResponseCode());
        if (map.getResponseCode() == 200) {
            me = new User();
            me.getPropertyIndex().
                    populateFromMap(map.getResponseData());
            //////////////////Log.p("Refreshed me " + me.getPropertyIndex().toString());
            me.getPropertyIndex().storeJSON("me.json");
            me.authtoken.set(Preferences.get("authtoken", ""));
            token = me.authtoken.get();
        }
    }
    }

    public static void signup(User u, Callback<User> callback) {
        signupOrLogin("ziemozi-users", u, callback);
    }

    public static boolean verifyUser(String code, boolean email, String phone) {
        Response<Map> s = verifyGet(authyUrl).
                queryParam("verification_code", code).
                queryParam("country_code", "234").
                queryParam("phone_number", phone).getAsJsonMap();
        //////////////////Log.p("Verify Result " + s.getResponseData());
        return "true".equals(s.getResponseData().get("success"));
    }

    public static boolean update(User u) {
        //first check if there is any need to update
        //check if the image file has http, if it does not have
        //then use cloudinary to save the image first
        //before proceeding
        //////////////////Log.p("Refresh user " + u.getPropertyIndex().toString());
        //////////////////Log.p("Me is " + me.getPropertyIndex().toString());
        boolean posted = true;
        //////////////////Log.p("Avatar " + u.avatar.get());
        if (u.avatar.get() != null) {
        if (u.avatar.get().indexOf("file:") >= 0) {

            try {
                Map uploadResult = cloudinary.uploader().
                        upload(u.avatar.get(), ObjectUtils.emptyMap());
                String uploadUrl = uploadResult.get("url").toString();
                u.avatar.set(uploadUrl);
                //////////////////Log.p("Avatar url " + uploadUrl);
            } catch (Exception e) {
                //////////////////Log.p("Image upload failed \n" + e.getMessage());
                ToastBar.showErrorMessage("Failed to upload profile image");
                posted = false;
            }
        }
        //me.getPropertyIndex().storeJSON("me.json");
        //Map uMap = u.getPropertyIndex().toMapRepresentation();
        //uMap.remove("_id"); // remove id field so the update can happen properly
        //u.getPropertyIndex().populateFromMap(uMap);
        //////////////////Log.p("Refresjing User Id " + me._id.get());
        //////////////////Log.p("Refreshing with " + u.getPropertyIndex().toString());
        Response<String> s = patch("ziemozi-users/" + me._id.get()).
                body(u.getPropertyIndex().toJSON()).getAsString();
        posted = posted && (s.getResponseCode() == 201);
        ////////////////////Log.p(u.getPropertyIndex().toJSON());
        //////////////////Log.p("User Patch Response Code " + s.getResponseCode());
        //////////////////Log.p("User Patch Response " + s.getResponseData());
        //u.getPropertyIndex().storeJSON("me.json");
        }
        ServerAPI.refreshMe();
        return (posted );
    }

    public static boolean setAvatar(String media) {
        //this is just to update the avatar and 
        //save it. We should just set the avatar

        Response<String> s = get("user/set-avatar").
                queryParam("media", media).getAsString();
        me.getPropertyIndex().storeJSON("me.json");
        return "OK".equals(s.getResponseData());
    }

    public static boolean setCover(String media) {
        Response<String> s = get("user/set-cover").
                queryParam("media", media).getAsString();
        me.getPropertyIndex().storeJSON("me.json");
        return "OK".equals(s.getResponseData());
    }

    public static boolean sendFriendRequest(String userId) {
        Response<String> s = get("user/send-friend-request").
                queryParam("userId", userId).getAsString();
        return "OK".equals(s.getResponseData());
    }

    public static boolean acceptFriendRequest(String userId) {
        Response<String> s = get("user/accept-friend-request").
                queryParam("userId", userId).getAsString();
        return "OK".equals(s.getResponseData());
    }

    private static String contactsToJSON(Contact[] contacts) {
        StringBuilder content = new StringBuilder("[");
        boolean first = true;
        for (Contact c : contacts) {
            String dname = c.getDisplayName();
            if (dname != null) {
                if (!first) {
                    content.append(",");
                }
                first = false;
                Map<String, String> data = new HashMap();
                data.put("fullName", dname);
                String phone = c.getPrimaryPhoneNumber();
                if (phone != null) {
                    data.put("phone", phone);
                    Map phones = c.getPhoneNumbers();
                    if (phones != null && phones.size() > 1) {
                        for (Object p : phones.values()) {
                            if (!p.equals(phone)) {
                                data.put("secondaryPhone", phone);
                                break;
                            }
                        }
                    }
                }
                String email = c.getPrimaryEmail();
                if (email != null) {
                    data.put("email", email);
                }
                content.append(JSONParser.mapToJson(data));
            }
        }
        content.append("]");
        return content.toString();
    }

    public static boolean uploadContacts(Contact[] contacts) {
        Response<String> s = post("user/contacts").
                body(contactsToJSON(contacts)).getAsString();
        return "OK".equals(s.getResponseData());
    }
    public static void pictureUpload(final Callback<String> resultURL,String picture) {
        if(picture!=null){
            MultipartRequest request = new MultipartRequest() {
                protected void readResponse(InputStream input) throws IOException  {
                    JSONParser jp = new JSONParser();
                    Map<String, Object> result = jp.parseJSON(new InputStreamReader(input, "UTF-8"));
                    String url = (String)result.get("url");
                    if(url == null) {
                        resultURL.onError(null, null, 1, result.toString());
                        return;
                    }
                    resultURL.onSucess(url);
                }
            };
            request.setUrl(BASE_URL + "media");
            try {
                request.addData("fileUpload", picture, "image/jpeg");
                request.setFilename("fileUpload", "myPicture.jpg");
                addToQueue(request);
            } catch(IOException err) {
                err.printStackTrace();
            }
        }
    }
    public static MultipartRequest uploadMedia(String mime, String role,
            String visibility, String fileName, byte[] data,
            SuccessCallback<String> callback) {
        MultipartRequest mp = new MultipartRequest() {
            private String mediaId;

            @Override
            protected void readResponse(InputStream input)
                    throws IOException {
                mediaId = Util.readToString(input);
            }

            @Override
            protected void postResponse() {
                callback.onSucess(mediaId);
            }
        };
        mp.setUrl(BASE_URL + "media/upload");
        mp.addRequestHeader("auth", token);
        mp.addRequestHeader("Accept", "application/json");
        mp.addArgument("role", role);
        mp.addArgument("visibility", visibility);
        if (data == null) {
            try {
                mp.addData("file", fileName, mime);
            } catch (IOException err) {
                Log.e(err);
                throw new RuntimeException(err);
            }
        } else {
            mp.addData("file", data, mime);
        }
        mp.setFilename("file", fileName);
        addToQueue(mp);
        return mp;
    }

    public static User me() {
        if (me == null) {
            me = new User();
            if (Storage.getInstance().exists("me.json")) 
            me.getPropertyIndex().loadJSON("me.json");
            me.authtoken.set(Preferences.get("authtoken", ""));            
        }
        String fullname = me.fullName();
        return me;
    }

    public static List<Notification> listNotifications(int page, int size) {
        Response<Map> response = get("user/notifications").
                queryParam("page", "" + page).
                queryParam("size", "" + size).getAsJsonMap();
        if (response.getResponseCode() == 200) {
            List<Map> l = (List<Map>) response.getResponseData().get("root");
            List<Notification> responseList = new ArrayList<>();
            for (Map m : l) {
                Notification n = new Notification();
                n.getPropertyIndex().populateFromMap(m);
                responseList.add(n);
            }
            return responseList;
        }
        return null;
    }

    private static List<Post> processPostResponse(Response<Map> response) {
        if (response.getResponseCode() == 200) {
            List<Map> l = (List<Map>) response.getResponseData().get("root");
            List<Post> responseList = new ArrayList<>();
            for (Map m : l) {
                Post p = new Post();
                p.getPropertyIndex().populateFromMap(m);
                responseList.add(p);
            }
            return responseList;
        }
        return null;
    }

    private static List<Request> processRequestResponse(Response<Map> response) {
        //////////////////Log.p("\n\nRequest ResponseCode \n" + response.getResponseCode());
        //////////////////Log.p("\n\nRequest Response data \n" + response.getResponseData());
        if (response.getResponseCode() == 200) {
            List<Map> l = (List<Map>) response.getResponseData().get("root");
            List<Request> responseList = new ArrayList<>();
            for (Map m : l) {
                Request p = new Request();
                p.getPropertyIndex().populateFromMap(m);
                responseList.add(p);
            }
            return responseList;
        }
        return null;
    }

    public static List<Post> postsOf(String user, int page, int size) {
        return processPostResponse(
                get("post/list").
                        queryParam("user", user).
                        queryParam("page", "" + page).
                        queryParam("size", "" + size).getAsJsonMap());
    }

    public static List<Request> newsfeed(int page, int size) {
        List<Request> news
                = genericZiemSearch("requests",
                        Request.class,
                        "", page, size, "", "");
//        for (Request r:news){
//            //////////////////Log.p("\nNews user " + r.ziemozi_user.get(0)._id.get() + " " + r.ziemozi_user.get(0).fullName());
//        }
        /**
         * return processRequestResponse( get("requests"). queryParam("page", ""
         * + page). queryParam("size", "" + size).getAsJsonMap());
         */
        return news;
    }
    

    public static boolean post(Post pd) {
        String key = post("post/new").body(pd.getPropertyIndex().toJSON()).
                getAsString().getResponseData();
        pd.id.set(key);
        return key != null;
    }

    public static boolean postRequest(Request pd) {

        ////////////Log.p("\n " + pd.getPropertyIndex().toJSON());
        Request newR = new Request();
        newR.getPropertyIndex().populateFromMap(pd.getPropertyIndex().toMapRepresentation());
        boolean post = false;
        newR.request_parameters.clear();
        //newR.service.clear();
        //newR.provider.clear();
        newR.status.set("");
        newR.ziemozi_user.clear();
        try {
            Response<Map> res = post("requests").body(newR)
                .getAsJsonMap();
        if ((res.getResponseCode() == 201)
                || (res.getResponseCode() == 200)) {
            post = true;
            Request r = new Request();
            //r.getPropertyIndex().populateFromMap(l);
            //r.getPropertyIndex().loadJSON("postRequestParameter");
            r.getPropertyIndex().populateFromMap(res.getResponseData());
            ////////////////////Log.p("RequestParameter retrieved " + r.getPropertyIndex().toString());
            pd._id.set(r._id.get());
        }
        }catch (Exception e){
            e.printStackTrace();
        }

        return post;
    }

    public static boolean comment(Comment c) {
        Response<String> cm = post("post/comment").
                body(c.getPropertyIndex().toJSON()).
                getAsString();
        if (cm.getResponseCode() != 200) {
            return false;
        }
        c.id.set(cm.getResponseData());
        return true;
    }

    public static boolean like(Post p) {
        String ok = get("post/like").queryParam("postId", p.id.get()).
                getAsString().getResponseData();
        return ok != null && ok.equals("OK");
    }

    public static List<User> searchPeople(
            String text, int page, int size) {
        return genericSearch("search/people", User.class,
                text, page, size);
    }

    public static List<Post> searchPosts(
            String text, int page, int size) {
        return genericSearch("search/posts", Post.class,
                text, page, size);
    }

    public static <T> List<T> genericSearch(String url,
            Class<? extends PropertyBusinessObject> type,
            String text, int page, int size) {
        Response<Map> result = get(url).
                queryParam("page", "" + page).
                queryParam("size", "" + size).
                queryParam("q", text).getAsJsonMap();
        if (result.getResponseCode() == 200) {
            List<Map> l = (List<Map>) result.getResponseData().get("root");
            if (l.size() == 0) {
                return null;
            }
            List<T> responseList = new ArrayList<>();
            for (Map m : l) {
                try {
                    PropertyBusinessObject pb
                            = (PropertyBusinessObject)type.newInstance();
                    pb.getPropertyIndex().populateFromMap(m);
                    responseList.add((T) pb);
                } catch (Exception err) {
                    Log.e(err);
                    throw new RuntimeException(err);
                }
            }
            return responseList;
        }
        return null;
    }

    public static String mediaUrl(String mediaId) {
        //https://rdb-simpledb.restdb.io/media/560263607f98025500000000?s=t
        mediaId = StringUtil.replaceAll(mediaId, "[", "");
        mediaId = StringUtil.replaceAll(mediaId, "]", ""); 
        return BASE_URL_STATIC + "media/" + mediaId + "?s=t";
    }

    public static boolean updatePushKey(String key) {
        Response<String> s = get("user/updatePush").
                queryParam("key", key).getAsString();
        return "OK".equals(s.getResponseData());
    }
    public static ArrayList<Category> categories(Category ct) {
        //get those categories for which this category is their parent
        ArrayList<Category> cc = new ArrayList<Category>();
        ArrayList<Category> c = new ArrayList<Category>();
        //////////Log.p("Looking for children of " + ct.getPropertyIndex().toString());
        c = localAPI.genericZiemSearch("category", Category.class, "", 0, 99, "", "");
       for (Category cx:c){
           cx.refresh();
           List<Category> l = cx.parent.asList();
           //////////Log.p("Checking " + ct.name.get() + " against " + cx.name.get());
           if (!l.isEmpty())
                if (l.get(0)._id.get().equals(ct._id.get())) {
                     cc.add(cx);
                }
       }
       return cc;
    }
    public static ArrayList<Category> categories() {
        ArrayList<Category> c = new ArrayList<Category>();
        //check the last update date so we can pull from storage
        c = localAPI.genericZiemSearch("category", Category.class, "", 0, 99, "", "");
       // c = processCategoryResponse(
       //         get("category").getAsJsonMap());
        return c;
    }
       public static ArrayList<Category> categories(Boolean withParent) {
        ArrayList<Category> cc = new ArrayList<Category>();
        ArrayList<Category> c = new ArrayList<Category>();
        c = localAPI.genericZiemSearch("category", Category.class, "", 0, 99, "", "");
       for (Category cx:c){
           cx.refresh();
           List<Category> l = cx.parent.asList();
           if (l.isEmpty())
               //we are looking for only parents
                     cc.add(cx);

       }
       return cc;
    }
     

    public static ArrayList<Provider> providers() {
        ArrayList<Provider> c = new ArrayList<Provider>();
                c = localAPI.genericZiemSearch("provider", Provider.class, "", 0, 99, "", "");
        //c = processProviderResponse(
        //        get("providers").getAsJsonMap());
        return c;
    }

    public static ArrayList<Service> serviceGroups() {
        ArrayList<Service> c = new ArrayList<Service>();
        c = processServiceDefintionResponse(
                get("service-definition").getAsJsonMap());
        return c;
    }

    public static ArrayList<Service> serviceList(Object p) {
        ArrayList<Service> serviceList = new ArrayList<Service>();
        //the string p may represent name of a category, provider or service
        //so we check each one and see if the names match it may also be 
        //a provider or a category or a service or a string or a user
        //so what we do is to form a query using the object.
        String url = "service-definition";
        int max = 0;
        int page = 0;
        int size = 100;
        String hint = "";
        String filter = "";
        String query = "";
        //////////////////Log.p("Type of Selector: " + p.getClass().getCanonicalName());
        //////////////////Log.p("\nFilter is: " + filter);
        ArrayList<Service> services = genericZiemSearch(url,
                Service.class,
                query, page, size, hint, filter);

        for (Service s : services) {
            if (p.getClass().getCanonicalName().indexOf("Category") >= 0) {
                Category c = (Category) p;
                List<Category> cl = s.category.asList();
                if (cl.size() > 0) {
                    //////////////////Log.p("Number of categories " + cl.size());
                    ////////////////////Log.p("\nService Category: \n" + s.category.get(0).getPropertyIndex().toString() +"\n" );
                    for (Category cc : cl) {
                        //////////////////Log.p("Category " + cc._id.get());
                        //////////////////Log.p("Service Category " + c._id.get());
                        if (cc._id.get().equalsIgnoreCase(c._id.get())) {
                            //////////////////Log.p("Matched Category " + c._id.get());
                            serviceList.add(s);
                            //////////////////Log.p("Services in Category " + serviceList.size());
                        }
                    }
                }
            }
            if (p.getClass().getCanonicalName().indexOf("Provider") >= 0) {
                Provider pr = (Provider) p;
                List<Provider> pl = s.providers.asList();
                if (pl.size() > 0) {
                    for (Provider pp : pl) {
                        if (pp._id.get().equalsIgnoreCase(pr._id.get())) {
                            serviceList.add(s);
                        }
                    }
                }
            }
            if (p.getClass().getCanonicalName().indexOf("Service") >= 0) {
                //we have a provider selected   
                //query = serviceForServiceQuery(p);
                Service ss = (Service) p;
                if (ss._id.get().equalsIgnoreCase(s._id.get())) {
                    serviceList.add(s);
                }
            }

        }
        return serviceList;
    }

    public static Vector serviceSelector(Object parent) {
        Vector v = new Vector();
        //by looking at the parent we know what to do next
        if (parent == null) {
            //This is the top and we will simply fill it for now with fixed values
            //First is to check and see if there is any topLevelServiceSelector
            //that is cached or to simply

            //String ok = get("files/serviceselector").getAsString().getResponseData();
            String ok = "Categories,Providers,ServiceGroup";
            String[] s = Util.split(ok, ",");
            int i = 0;
            for (i = 0; i < s.length; i++) {
                HashMap m = new HashMap();
                m.put("name", s[i]);
                v.add(m);
            }
            return v;
        } else {
            //in this case we know which one it is. so most likely
            //it is either Categories,Providers,ServiceGroup
            ////////////////////Log.p("Parent " + parent.toString());
            ////////////////////Log.p("Is Categories " + parent.toString().indexOf("Categories"));
            ////////////////////Log.p("Is Providers " + parent.toString().indexOf("Providers"));
            ////////////////////Log.p("Is ServiceGroup " + parent.toString().indexOf("ServiceGroup"));
            if (parent.toString().indexOf("Categories") >= 0) {
                //categories
                v = new Vector();
                ArrayList<Category> c = ServerAPI.categories();
                int i = 0;
                for (i = 0; i < c.size(); i++) {
                    HashMap m = new HashMap();
                    m.put("name", c.get(i).name.get());
                    m.put("selector", c.get(i));
                    v.add(m);
                }
            }
            if (parent.toString().indexOf("Providers") >= 0) {
                //providers
                v = new Vector();
                ArrayList<Provider> c = ServerAPI.providers();
                int i = 0;
                for (i = 0; i < c.size(); i++) {
                    HashMap m = new HashMap();
                    m.put("name", c.get(i).name.get());
                    m.put("selector", c.get(i));
                    v.add(m);
                }

            }
            if (parent.toString().indexOf("ServiceGroup") >= 0) {
                //service groups
                v = new Vector();
                ArrayList<Service> c = ServerAPI.serviceGroups();
                int i = 0;
                for (i = 0; i < c.size(); i++) {
                    HashMap m = new HashMap();
                    m.put("name", c.get(i).name.get());
                    m.put("selector", c.get(i));
                    v.add(m);
                }

            }

        }
        return v;

    }

    private static ArrayList<Category> processCategoryResponse(Response<Map> response) {
        if (response.getResponseCode() == 200) {

            ArrayList<Map> l = (ArrayList<Map>) response.getResponseData().get("root");
            ArrayList<Category> responseList = new ArrayList<>();
            for (Map m : l) {
                //////////////////Log.p("Category Map\n ");
                showMap(m);
                Category p = new Category();
                p.getPropertyIndex().populateFromMap(m);
                responseList.add(p);
                ////////////////////Log.p(p.getPropertyIndex().toString());
            }
            return responseList;
        }
        return null;
    }

    private static ArrayList<Provider> processProviderResponse(Response<Map> response) {
        if (response.getResponseCode() == 200) {
            ArrayList<Map> l = (ArrayList<Map>) response.getResponseData().get("root");
            ArrayList<Provider> responseList = new ArrayList<>();
            for (Map m : l) {
                showMap(m);
                Provider p = new Provider();
                p.getPropertyIndex().populateFromMap(m);
                responseList.add(p);
                ////////////////////Log.p(p.getPropertyIndex().toString());
            }
            return responseList;
        }
        return null;
    }

    private static ArrayList<Service> processServiceDefintionResponse(Response<Map> response) {
        if (response.getResponseCode() == 200) {
            ////////////////////Log.p(response.toString());
            ArrayList<Map> l = (ArrayList<Map>) response.getResponseData().get("root");
            ArrayList<Service> responseList = new ArrayList<>();
            for (Map m : l) {
                Service p = new Service();
                p.getPropertyIndex().populateFromMap(m);
                responseList.add(p);
            }
            return responseList;
        }
        return null;
    }

    public static <T> ArrayList<T> genericZiemSearch(String url,
            Class<? extends PropertyBusinessObject> type,
            String text, int page, int size, String hint, String filter) {
        //////////////////Log.p("Query " + text);
        //{"$orderby": "{" ,"_created": "-1" }}
        String fetchUrl = "{\"$orderby\":"
                + "{" + "\"_created\": " + "-1" + " }}";
        hint = fetchUrl;
        //////////////////Log.p("Hint " + hint);
        Response<Map> result = get(url).
                queryParam("q", text).
                queryParam("h", hint).
                queryParam("filter", filter).
                queryParam("skip", "" + page).
                queryParam("max", "" + size).
                queryParam("metafields", "true").
                queryParam("fetchChildren", "true").
                getAsJsonMap();
        ////////////////Log.p("\nResponse for " + url + " is " + result.getResponseCode());
        ////////Log.p("\n Response Data" + result.getResponseData(), Log.DEBUG);

        if ((result.getResponseCode() == 200)&&(result.getResponseData() != null)) {
            ArrayList<Map> l = new ArrayList<Map>();
             l.addAll((ArrayList<Map>) result.getResponseData().get("root"));
            if (l.size() == 0) {
                return null;
            }
            ArrayList<T> responseList = new ArrayList<>();
            for (Map mm : l) {
                try {
                    //showMap(mm);
                     //                   ////////////Log.p("Ziemozi User from m" + m.get("ziemozi_user"));
                    PropertyBusinessObject pb
                            = (PropertyBusinessObject)type.newInstance();
                    //////////////Log.p(type.getSimpleName() +" plain -->\n" + pb.getPropertyIndex().toString());
                    
                    pb.getPropertyIndex().populateFromMap(mm);
                    ////////////Log.p(pb.getPropertyIndex().get("ziemozi_user").toString());
                    //
                    ////////Log.p(type.getSimpleName() +" -->\n" + pb.getPropertyIndex().toJSON());
                    responseList.add((T) pb);
                } catch (Exception err) {
                    Log.e(err);
                    throw new RuntimeException(err);
                }
            }
            return responseList;
        }
        return null;
    }

    private static String serviceForProviderQuery(Object p) {
        String query = "";
        //given the provider, find all the service definitions for the provider
        // or all services for which this is a direct provider.
        //we re searching in service-defintions where the 
        return query;
    }

    private static String serviceForCategoryQuery(Object p) {
        String query = "";
        HashMap m = new HashMap();
        Category c = (Category) p;
        query = "{" + '"' + "category._id" + '"' + ": " + '"' + c._id.get() + '"' + "}";

        return c._id.get();
    }

    private static String serviceForServiceQuery(Object p) {
        Service s = (Service) p;
        String query = "{ " + '"' + "parent" + '"' + ":" + '"';
        query = query + "[" + s._id.get() + "]" + '"' + "}";
        return query;
    }

    public static Service getDefaultService() {
        Service s = new Service();
        return s;
    }

    public static Boolean postRequestParameter(RequestParameter pd) {
        // String key = post("request_parameter").body(pd.getPropertyIndex().toJSON()).
        //       getAsString().getResponseData();
        // //////////////////Log.p("Saving Request PArameter ..\n");
        //  HashMap l = (HashMap) post("request_parameter").body(pd.getPropertyIndex().toJSON()).getAsJsonMap().getResponseData();
        // Storage.getInstance().writeObject("postRequestParameter", key);
        //  RequestParameter r = new RequestParameter();
        //  r.getPropertyIndex().populateFromMap(l);
        //  //////////////////Log.p("RP Save Response \n" + r.getPropertyIndex().toString());
        //  pd._id.set(r._id.get());
        //  return r._id.get() != null;  
//        String key = post("request-parameters").body(pd.getPropertyIndex().toJSON()).
        //              getAsString().getResponseData();
        //////////////////Log.p("\n Saving Request Parameter " + pd.getPropertyIndex().toJSON(), Log.DEBUG);
        Response<Map> res = post("request-parameters").body(pd.getPropertyIndex()
                .toJSON()).getAsJsonMap();
        //HashMap l = (HashMap) post("request_parameter").body(pd.getPropertyIndex().toJSON()).getAsJsonMap().getResponseData();
        //  Storage.getInstance().writeObject("postRequestParameter", key);

        //    //////////////////Log.p("\n RequestParameter response data is \n" + key);
        //try {
        //    JSONParser j = new JSONParser();
        //    Reader r = new InputStreamReader(
        //             Storage.getInstance().createInputStream("postRequestParameter"));
        //    Map<String,Object> l = j.parseJSON(r ) ;                  
        // } catch (Exception ex) {
        //     //////////////////Log.p(ex.getMessage());
        // }
        //Display.getInstance().getResourceAsStream(getClass(), "/anapioficeandfire.json"), "UTF-8")) {
        //Map<String, Object> data = json.parseJSON(r);
        //    Map<String,Object> l = j.parseJSON(new StreamReader )
        //Storage.getInstance().writeObject("postRequest", key);
        RequestParameter r = new RequestParameter();
        //r.getPropertyIndex().populateFromMap(l);
        //r.getPropertyIndex().loadJSON("postRequestParameter");
        r.getPropertyIndex().populateFromMap(res.getResponseData());
        ////////////////////Log.p("RequestParameter retrieved " + r.getPropertyIndex().toString());
        pd._id.set(r._id.get());
        return r._id.get() != null;
    }

    public static Location getCurrentLocation() {
        return LocationManager.getLocationManager().getCurrentLocationSync();
    }

       public static void like(Request p){
        ToastBar.showInfoMessage("Liking this  message ..");
        Service like = ServerAPI.getLikeService();
                    if (like == null ) {
                        ToastBar.showErrorMessage("Liking not yet available");
                     }else {
                        Request likeRequest = new Request();
                        likeRequest.service.add(like);
                        likeRequest.createRequest("likedrequest",p._id.get());
                        //go through the request parameters and find  the one that corresponds
                        //to the request_id attribute
                        //and set it to this request
                        
                        likeRequest.save();
                     
                    }
    }

    public static List<Request> searchRequests(String text, int page, int amount) {

        return ServerAPI.genericZiemSearch("requests",
                Request.class,
                "", page, amount, "", text);
    }

    public static List<Request> requestsOf(String get, int page, int amount) {
        return genericZiemSearch("requests",
                Request.class,
                "", page, amount, "", get);
    }

    private static void showMap(Map m) {
        for (Object k : m.keySet()) {
           ////////Log.p(k.toString() + " -- " + m.get(k).toString());
        }
    }
private static void showMapC(Map m) {
        for (Object k : m.keySet()) {
           ////////////Log.p(k.toString() + " -- " + m.get(k).toString());
        }
    }    

    public static Service getService(String _id) {
        Service s = new Service();
        Response<Map> map = get("service-definition/" + _id).getAsJsonMap();
        //////////////////Log.p("Response " + map.getResponseData());
        if (map.getResponseCode() == 200) {
            s.getPropertyIndex().
                    populateFromMap(map.getResponseData());
        }
        return s;
    }

    public static ServiceAttributeType getServiceAttributeType(String _id) {
        //////////////////Log.p("Get Service Attribute Type " + _id);
        ServiceAttributeType s = new ServiceAttributeType();
        Response<Map> map = get("service-attribute-type/" + _id).getAsJsonMap();
        //////////////////Log.p("Response " + map.getResponseData());
        if (map.getResponseCode() == 200) {
            s.getPropertyIndex().
                    populateFromMap(map.getResponseData());
        }
        return s;
    }

    public static ArrayList<Request> getCommentsFor(String id) {
        String url = "requests/" + id + "/comments";
        Response<Map> map = get(url).getAsJsonMap();
        if (map.getResponseCode() == 200) {
            ArrayList<Map> l = (ArrayList<Map>) map.getResponseData().get("root");
            if (l.size() == 0) {
                return null;
            }
            ArrayList<Request> responseList = new ArrayList<>();
            for (Map m : l) {
                try {
                    showMap(m);
                    PropertyBusinessObject pb
                            = (Request)Request.class.newInstance();
                    pb.getPropertyIndex().populateFromMap(m);
                    //////////////////Log.p(pb.getPropertyIndex().toString());
                    responseList.add((Request) pb);
                } catch (Exception err) {
                    Log.e(err);
                    //throw new RuntimeException(err);
                }
            }
            return responseList;
        }
        return null;
    }

    public static Boolean saveRequestParameters(ArrayList<RequestParameter> summedRp, Request rq) {
        Request rr = rq;
        boolean posted = false;
        //we will post them as one batch. So we create them as a string and then save
        //first we have to pre_process before saving
        //media has to be saved first and the media has to be saved
        //////////Log.p("Saving Parameters for this request \n"  + rq.getPropertyIndex().toJSON());
        //rq.refreshRequestParameters();
        //rq.refreshService();
        cloudinary.config.privateCdn = false;
        String jsonRequestParameter = "[\n";
        for (RequestParameter rqp : summedRp) {
            //Log.p("RequestParameter  attribute size " + rqp.service_attribute.size());
            if (rqp.service_attribute.size() > 0 ) {
            //rqp.refreshServiceAttribute();
            //////////Log.p("Request parameter to check for images \n"  + r.getPropertyIndex().toJSON());
            String value = "";
            ServiceAttribute st = rqp.service_attribute.get(0);
            //
            //////////Log.p("Saving Request Parameters attribute\n" + st.getPropertyIndex().toString());
            st.refreshTypeOfAttribute();
            //ServiceAttributeType tt = st.type_of_attribute.get(0);
            //String base_type = r.service_attribute.get(0).type_of_attribute.get(0).base_type.get();
            //
            //////////Log.p("Saving parameter for " + st.name.get());
            String base_type ="text";
            if (st.type_of_attribute.size() > 0) {
 
            base_type = st.type_of_attribute.get(0).base_type.get();
            if ((base_type == null) || (base_type.length() < 1)) {
                base_type = "text";
            }
            }
            //
            if (base_type.equalsIgnoreCase("image")
                    || base_type.equalsIgnoreCase("video")
                    || base_type.equalsIgnoreCase("media")
                    || base_type.equalsIgnoreCase("file")) {
                String[] imageList = Util.split(rqp.value.get(), "||");
                for (String img : imageList) {
                    //for each image, save and append back to value
                    //////////Log.p("Image to save " + img);
                    if (!img.isEmpty()) {
                        try {
                            Map uploadResult = cloudinary.uploader().
                                    upload(img, ObjectUtils.emptyMap());
                            String uploadUrl = uploadResult.get("url").toString();
                            //////////Log.p("Image is saved as " + uploadUrl);
                            if (value.length() < 2) {
                                value = uploadUrl;
                            } else {
                                value = value + " || " + uploadUrl;
                            }
                        } catch (Exception e) {
                            //////////Log.p("Image upload failed \n" + e.getMessage());
                            posted = false;
                        }
                    }
                }
                //////////Log.p("Value of attribute is " + value);
                rqp.value.set(value);
                rqp.summarize(false);
            }
                //////////Log.p("Summarized with image " + r.getPropertyIndex().toJSON());
            jsonRequestParameter = jsonRequestParameter + rqp.getPropertyIndex().toJSON() + ",";
            }   
        }
        //////////Log.p("Now summarizing " + rq.getPropertyIndex().toJSON());
        //////////Log.p("Alter ego is " + rr.getPropertyIndex().toJSON());
        String summedSummary = rq.requestSummary(summedRp,false);
        rq.summary.set(summedSummary);
        rq.f_summary.set(rq.requestSummary(summedRp,true));

        jsonRequestParameter = jsonRequestParameter.substring(0,
                jsonRequestParameter.lastIndexOf("}") + 1) + "\n]";
        ////////////////Log.p(jsonRequestParameter);
        Response res = post("request-parameters").body(
                jsonRequestParameter).getAsString();

        if (res.getResponseCode()
                == 201 || res.getResponseCode() == 200) {
            posted = true;
        }
        ////////////////Log.p("\n bulk parameter save response" + res.getResponseData().toString());
        //let us update the summary
        Request newR = new Request();
        newR.getPropertyIndex().populateFromMap(rq.getPropertyIndex().toMapRepresentation());
        
        //String s ="";
        //s=s+"{" + '"' + "summary" + '"' + ":" + '"'   + rq.summary.get().replaceAll("", '\') + '"' +"}";
        //////////Log.p("Update Summary to " + newR.summary.get());
        
        res = patch("requests/" + rq._id.get()).body(newR).getAsString();
        //////////Log.p(res.getResponseCode() + "\n" + res.getResponseData());
        return posted;
    }

    public static ServiceAttribute getServiceAttribute(String _id) {
        //////////////////Log.p("Get Service Attributexx " + _id);
        ServiceAttribute s = new ServiceAttribute();
        Response<Map> map = get("service-attributes/" + _id).getAsJsonMap();
        //////////////////Log.p("Response " + map.getResponseData());
        if (map.getResponseCode() == 200) {
            s.getPropertyIndex().
                    populateFromMap(map.getResponseData());
        }
        //////////////////Log.p("Attributexx " + s.getPropertyIndex().toString());
        return s;
    }

    public static void updateRequestSummary(Request aThis) {
        //////////////////Log.p("Updating this request " + aThis._id.get());
        Response res = patch("requests/" + aThis._id.get()).
                body(aThis.getPropertyIndex().toJSON()).getAsString();
        //////////////////Log.p(res.getResponseData().toString());
//        
    }

    public static ArrayList<Service> favoriteServiceList(Category ct) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return null;
    }

    public static Service getSosService() {
        //SOS service is the service that has been identified as 
        //the service to create when there is an SOS
        Service sos = null;
        String fetchUrl = "{\"name\": \""
                + sosService + "\" }";

        //////////////////Log.p(fetchUrl);

        ArrayList<Service> services = genericZiemSearch("service-definition",
                Service.class,
                fetchUrl, 0, 10, "", "");
        if (services != null) {
            sos = services.get(0);
        }
        return sos;
    }

    public static Service getAmFineService() {
        Service amFine = null;
        String fetchUrl = "{\"name\": \""
                + amFineService + "\" }";

        //////////////////Log.p(fetchUrl);

        ArrayList<Service> services = genericZiemSearch("service-definition",
                Service.class,
                fetchUrl, 0, 10, "", "");
        if (services != null) {
            amFine = services.get(0);
        }
        return amFine;
    }
//
    public static Service getLikeService() {
        Service like = null;
        String fetchUrl = "{\"name\": \""
                + likeService + "\" }";

        //////////////////Log.p(fetchUrl);

        ArrayList<Service> services = genericZiemSearch("service-definition",
                Service.class,
                fetchUrl, 0, 10, "", "");
        if (services != null) {
            like = services.get(0);
        }
        return like;
    }
//    
    public static List<Request> geoSearchRequests(String text, int page, int amount) {
        //we need to implement a geo search request functionality but not yet
        return ServerAPI.searchRequests(text, page, amount);
    }

    public static DAOProvider dataProvider() {

        if (dbProvider == null) {
            //////////////////Log.p("Setting up database provider");
            try {
                db = Database.openOrCreate(dbname);
                ServerAPI.dbProvider = new DAOProvider(db,1);
                //////////////////Log.p("Schema Version " + dbProvider.getSchemaVersion());
                //////////////////Log.p("Initialized database " + dbProvider.toString());
            } catch (Exception e) {
                e.printStackTrace();
                ////////////////////Log.p(e.getMessage());
            }
        }
        return dbProvider;

    }
    public static void loadDefinitions() {
        //open database
        //take each loadable object
        //download them and put it in the database
        //Category
        //////////////////Log.p("Load To Local Database");
        //dbProvider = ServerAPI.dataProvider();
       // Log.p("Updating definitions");
        loadServices();
        loadCategories();
        loadServiceAttributes();
        loadServoceAttributeTypes();
        loadWallets();        
        loadProviders();
        loadServiceContacts();
        loadGroups();
        loadFares();
        loadRoutes();
        loadRouteStops();
        loadStops();

    }
    public static void loadToLocalDatabase() {
        //open database
        //take each loadable object
        //download them and put it in the database
        //Category
        //////////////////Log.p("Load To Local Database");
        //Log.p("Syncing with Server");
        //Log.p("load Services");
        loadServices();
        //Log.p("Load Categories");
        loadCategories();
        //Log.p("Load Service Attributes");
        loadServiceAttributes();
        //Log.p("Load Attribute Typres");
        loadServoceAttributeTypes();
        //Log.p("Load Providers");
        loadProviders();
        //Log.p("Load Contacts");
        loadServiceContacts();
        //Log.p("Load Requests");
        loadRequests();
        //Log.p("Load Groups");
        loadGroups();
        //load users?
        loadUsers(ServerAPI.me()._id.get());
        //Log.p("Load Request Parameters");
        loadRequestParameters();        
//        
        loadWallets();
        loadFares();
        loadRoutes();
        loadRouteStops();
        loadStops();
    }
//
        public static void loadWallets(){
       ArrayList<PropertyBusinessObject> wallets = 
               ServerAPI.genericZiemSearch("wallets",
                       Wallet.class, "",0, 9999, "", "");  
       //Log.p("Wallets are " + wallets.size());

        Boolean loaded=localAPI.saveLocalBatch(wallets);
        //Log.p("Loaded Wallets" + loaded);
    } 
//    
    
    public static void loadCategories(){
       ArrayList<PropertyBusinessObject> categories = 
               ServerAPI.genericZiemSearch("category",
                       Category.class, "",0, 9999, "", "");
                //if (categories != null ) //////Log.p("There are  " + categories.size() + " category records.");
       /*
        for (Category c:categories){
           
            localAPI.saveLocal(c,true);
       }
       */
        localAPI.saveLocalBatch(categories);
    }
    public static void loadGroups(){
       ArrayList<PropertyBusinessObject> groups = 
               ServerAPI.genericZiemSearch("group",
                       Group.class, "",0, 9999, "", "");

        localAPI.saveLocalBatch(groups);
    }    
    public static void loadServices(){
          ArrayList<PropertyBusinessObject> services = 
               ServerAPI.genericZiemSearch("service-definition",
                       Service.class, "",0, 99, "", "");
       // if (services != null ) //////Log.p("There are  " + services.size() + " service records.");
       /*
        if (services != null ) {
            for (Service s:services){
                         localAPI.saveLocal(s,true);
            }
        } 
        */
       Boolean saved = localAPI.saveLocalBatch(services);
     //  //////Log.p("Saved Services " + saved);
    }  

    public static void loadRequests() {
              ArrayList<PropertyBusinessObject> requests = 
               ServerAPI.genericZiemSearch("requests",
                       Request.class, "",0, 99, "", "");
        /*
        if (requests != null) {      
        for (Request r:requests){
                      localAPI.saveLocal(r,true);
        }      
        }
        */
       //Log.p("There are  " + requests.size() + " request records.");
        // ArrayList<PropertyBusinessObject> filteredRequests = privacyCheckRequests(requests);
        // ////Log.p("There are  " + filteredRequests.size() + " request records.");
        localAPI.saveLocalBatch(requests);
    }  
public static ArrayList<PropertyBusinessObject> privacyCheckRequests(ArrayList<PropertyBusinessObject> toCheck){
    //we cycle through  the list and drop the objects for which the privacy parameter is there
    //and it is set to false
    return toCheck;
  }


    public static void loadRequestParameters() {
       ArrayList<PropertyBusinessObject> requestParameters = 
               ServerAPI.genericZiemSearch("request-parameters",
                       RequestParameter.class, "",0, 99, "", "");
      /*if (requestParameters != null ){
          for (RequestParameter rp:requestParameters){
            localAPI.saveLocal(rp,true);           
       }
      }
      */
  //      ////////////Log.p("There are  " + requestParameters.size() + " Parameter records.");
     localAPI.saveLocalBatch(requestParameters);
    }  


    private static void loadServiceAttributes() {
      ArrayList<PropertyBusinessObject> serviceAttributes = 
               ServerAPI.genericZiemSearch("service-attributes",
                       ServiceAttribute.class, "",0, 9999, "", "");
      /*
        for (ServiceAttribute sa:serviceAttributes){
          localAPI.saveLocal(sa,true);          
      }
      */
    //    ////////////Log.p("There are  " + serviceAttributes.size() + " Attribute records.");
     localAPI.saveLocalBatch(serviceAttributes);
    }  

    public static void loadUsers(String userid) {
               String fetchUrl = "{\"_id\": \"" + userid + "\" }"; 
               //////Log.p("Fetch User " + userid);
       ArrayList<PropertyBusinessObject> Users = 
               ServerAPI.genericZiemSearch("ziemozi-users",
                       User.class, fetchUrl,0, 9999, "", "");
       /*
            for (ServiceAttributeType st:serviceAttrTypes){
                      localAPI.saveLocal(st,true);
       }
       */
      //////Log.p("There are  " + Users.size() + " user records.");
      if (Users != null ) localAPI.saveLocalBatch(Users);
    }

    private static void loadServoceAttributeTypes() {
       ArrayList<PropertyBusinessObject> serviceAttrTypes = 
               ServerAPI.genericZiemSearch("service-attribute-type",
                       ServiceAttributeType.class, "",0, 9999, "", "");
       /*
            for (ServiceAttributeType st:serviceAttrTypes){
                      localAPI.saveLocal(st,true);
       }
       */
      //  ////////////Log.p("There are  " + serviceAttrTypes.size() + " Attribute records.");
       localAPI.saveLocalBatch(serviceAttrTypes);
    }

    private static void loadProviders() {
               ArrayList<PropertyBusinessObject> providers = 
               ServerAPI.genericZiemSearch("providers",
                       Provider.class, "",0, 9999, "", "");
     /*
        if (providers != null ) for (Provider p:providers){
                    localAPI.saveLocal(p,true);                 
               }
     */
        //////////////Log.p("There are  " + providers.size() + " provider records.");
     localAPI.saveLocalBatch(providers);
    }
    private static void loadServiceContacts() {
               ArrayList<PropertyBusinessObject> contacts = 
               ServerAPI.genericZiemSearch("service-contacts",
                       ServiceContact.class, "",0, 9999, "", "");
  
    localAPI.saveLocalBatch(contacts);  
    }
    private static void loadRoutes() {
        ArrayList<PropertyBusinessObject> routes =
                ServerAPI.genericZiemSearch("busroute",
                        Route.class, "",0, 9999, "", "");
        //Log.p("Routes Found " + routes.size());
        localAPI.saveLocalBatch(routes);
    }
    private static void loadFares() {
        ArrayList<PropertyBusinessObject> fares =
                ServerAPI.genericZiemSearch("fares",
                        Fare.class, "",0, 9999, "", "");

        localAPI.saveLocalBatch(fares);
    }
    private static void loadRouteStops() {
        ArrayList<PropertyBusinessObject> routestops =
                ServerAPI.genericZiemSearch("route-stops",
                        RouteStops.class, "",0, 9999, "", "");

        localAPI.saveLocalBatch(routestops);
    }
    private static void loadStops() {
        ArrayList<PropertyBusinessObject> stops =
                ServerAPI.genericZiemSearch("stop",
                        Stop.class, "",0, 9999, "", "");

        localAPI.saveLocalBatch(stops);
    }
    public static void logout() {
        //////Log.p("Logging out");
        if (Storage.getInstance().exists("me.json")) {
            Storage.getInstance().deleteStorageFile("me.json");
            ServerAPI.me = new User();
        }
        //clear the cache
        Storage.getInstance().clearCache();
        Storage.getInstance().clearStorage();
        //////Log.p("Clearing database");
        
        cleardb();
        //////Log.p("Initializing Database");
        initDb();
        //////Log.p("Done with Server logout");
       
    }
    public static void initDb() {
        //we check if the datbase exists. IF so, 
        //check if it has been refreshed lately
        //if not refresh and also set timer for refreshing
        //if db does not exist, then we create it
        //we create it from a list of the Persistable Classes
        //
        //Check if DB exists - look for /setup.sql and read the contents
        //
        ArrayList<Object> dbClasses = new ArrayList<Object>();
        dbClasses.add(new Request());
        dbClasses.add(new Service());
        dbClasses.add(new Category());
        dbClasses.add(new Provider());
        dbClasses.add(new ServiceAttribute());
        dbClasses.add(new ServiceAttributeType());
        dbClasses.add(new ServiceContact());
        dbClasses.add(new RequestParameter());
        dbClasses.add(new User());
        dbClasses.add(new Group());
        dbClasses.add(new Request());
        dbClasses.add(new Wallet());
        dbClasses.add(new Route());
        dbClasses.add(new Fare());
        dbClasses.add(new Stop());
        dbClasses.add(new RouteStops());
        //
        //if db does not exist,let us create it
        //create a new configuration file /setup.sql
        //Dialog d = new InfiniteProgress().showInfiniteBlocking();
        File dbConfig = new File("/config.sql");
        createDb(dbConfig, dbClasses);
        //ServerAPI.dataProvider();
        //ToastBar.showInfoMessage("Syncing with the server. Wait ..");
        ServerAPI.loadToLocalDatabase();
        //ServerAPI.testdb();
        //localAPI.saveRequestsToServer();
       // d.dispose();
    }

    private static void writeStringToFile(File file, String content) throws IOException {
        FileSystemStorage fs = FileSystemStorage.getInstance();
        try (OutputStream os = fs.openOutputStream(file.getAbsolutePath())) {
            Util.copy(new ByteArrayInputStream(content.getBytes(
                    "UTF-8")), os);
        }

    }

    private static void createDb(File dbConfig, ArrayList<Object> dbClasses) {
        String config = "--Version:1\n";
        Cursor cur = null;
        Database db = null;
        try {
            db = Display.getInstance().openOrCreate("ziemozi");
        }catch (IOException ex) {
            ////Log.p("Failed to Open Database");
            ex.printStackTrace();
                Log.sendLogAsync();            
        }
        //////Log.p(db.getDatabasePath("ziemozi"));
        if (db != null) {
            String query = "";
            for (Object o : dbClasses) {
                query = createTables((PropertyBusinessObject) o, config);

                config = config + query + "\n--\n";
                try {
                    //Log.p("query \n" + query);
                    db.execute(query);
                    //cur = db.executeQuery("select sql from sqlite_master where name='foo';");
                    //

                } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.sendLogAsync();
                    //return;
                }
                query = "";
            }
            try {
                db.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.sendLogAsync();
            }
        }
        /*
        int lastComma = config.lastIndexOf(";");
        config = config.substring(0, lastComma);
          try {
           writeStringToFile(dbConfig, config);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
       ////////////Log.p(config);
        */
    }

    private static String createTables(PropertyBusinessObject pbo, String config) {
        String c = "";
        c = c + "CREATE TABLE IF NOT EXISTS '" + pbo.getPropertyIndex().getName().toLowerCase() + "'(\n";
        //c= c + "id INTEGER PRIMARY KEY AUTOINCREMENT, \n";
        for (PropertyBase b : pbo.getPropertyIndex()) {
            if (b.getGenericType() != null) {
                //
                if (b.getGenericType().getCanonicalName().indexOf("java.") >= 0) {
                    String bType = b.getGenericType().getCanonicalName();
                    List<String> types = StringUtil.tokenize(bType, ".");
                    c = c + b.getName().toLowerCase() + " " + types.get(types.size() - 1) + ",\n";
                } else {
                    if (b.getName().equalsIgnoreCase("_id")) {
                        c = c + b.getName().toLowerCase() + "  NOT NULL TEXT UNIQUE PRIMARY KEY, \n";
                    } else {
                        c = c + b.getName().toLowerCase() + " TEXT ,\n";
                    }
                }
            } else {

                if (b.getName().equalsIgnoreCase("_id")) {
                    c = c + b.getName().toLowerCase() + " TEXT NOT NULL PRIMARY KEY , \n";
                } else {
                    c = c + b.getName().toLowerCase() + " TEXT ,\n";
                }
            }
        }
        int lastComma = c.lastIndexOf(",");
        c = c.substring(0, lastComma);
        c = c + ");\n";
        //////////////Log.p(c);
        return c;
    }    

    public static Image getServiceImage(Service service) {
        Style s = new Style();
        s.setFgColor(0xff0000);
        s.setBgTransparency(0);
        FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PLACE, s, Display.getInstance().convertToPixels(3));
        EncodedImage placeholder = EncodedImage.createFromImage(p.scaled(p.getWidth()/16, p.getHeight()/16), false);
      if ( service == null ) {
            return p;
      }
          service.refreshIcon();
      if ( service.logo == null){
          return p;
      }
      if ( service.logo.size() < 1){
          return p;
      } 
  
       String logo = service.logo.get(0);
        if (logo != null) {
            String url=ServerAPI.mediaUrl(logo);           

            if (Storage.getInstance().exists(service.label.get())) {
                try {
                    //we have the image and we can just load it
                    Image i =  Image.createImage(Storage.getInstance().createInputStream(service.label.get()));
                    return i.scaled(p.getWidth()/16, p.getHeight()/16);
                } catch (IOException ex) {
                    //dont do anything and try again
                }
            }
            Image i = URLImage.createToStorage(placeholder, service.label.get(),
                    url );
               return i.scaled(p.getWidth()/16, p.getHeight()/16);
        } else {
           return p;
        }

    }    

public static void cleardb(){
            try {
            db = Display.getInstance().openOrCreate("ziemozi");
            Database.delete("ziemozi");
            db.close();
            
        } catch (IOException ex) {
            ex.printStackTrace();
                        ////Log.p("Failed to Delete Database");
                Log.sendLogAsync();            
        }
        db=null;
}

    public static String postTo(final String url, final String body) {
        Response res = post(url).body(
                body).getAsString();
        return res.getResponseData().toString();
    }
}
