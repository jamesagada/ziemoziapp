package com.ziemozi.server.local;

import ca.weblite.codename1.db.DAO;
import ca.weblite.codename1.db.DAOProvider;
import com.ziemozi.server.*;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.codename1.components.ToastBar;
import com.codename1.contacts.Contact;
import com.codename1.db.Cursor;
import com.codename1.db.Database;
import com.codename1.db.Row;
import com.ixzdore.restdb.ziemobject.Comment;
import com.ixzdore.restdb.ziemobject.Notification;
import com.ixzdore.restdb.ziemobject.Post;
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
import com.codename1.io.services.CachedData;
import com.codename1.io.services.CachedDataService;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBase;
import com.codename1.properties.PropertyBusinessObject;
//import com.codename1.properties.SQLMap;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.util.Callback;
import com.codename1.util.StringUtil;
import com.codename1.util.SuccessCallback;
import com.codename1.util.regex.StringReader;
import com.ixzdore.properties.SQLMap;
import com.ixzdore.restdb.ziemobject.Category;
import com.ixzdore.restdb.ziemobject.Group;
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.ixzdore.restdb.ziemobject.ServiceContact;
import com.ixzdore.restdb.ziemobject.UserPreference;
import com.ziemozi.forms.ImagePicker;
import static com.ziemozi.server.ServerAPI.dbProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

////import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.print.DocFlavor;
public class localAPI {

    private static User me;
    public static final String BASE_URL = "https://ziemozi-a3ef.restdb.io/rest/";
    public static final String BASE_URL_STATIC = "https://ziemozi-a3ef.restdb.io/";
    public static String API_KEY = "5a0755c03d5e3147a77ba2486df4ea34e6b59";
    public static String AUTHY = "3IMDN8qW1tTuLdI7h8FMgjaw9YdVVQFe";
    public static final String authyUrl = "https://api.authy.com/protected/json/phones/verification/check";
    private static String token;
    private static String sosService = "SOS";
    private static String rateService = "RateService";
    private static String amFineService = "IAMFINE";
    private static int aroundMeDistance = 10;//Distance in km to consider as around me
    private static String configPath = "/config.sql";
    private static String dbname = "ziemozi";

    private static Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
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
            return Rest.post(BASE_URL + path).
                    header("auth", token).header("x-apikey", API_KEY)
                    .header("X-HTTP-Method-Override", "PATCH").jsonContent();
        }
        return Rest.post(BASE_URL + path).header("x-apikey", API_KEY).
                header("X-HTTP-Method-Override", "PATCH").jsonContent();

    }

    public static boolean isLoggedIn() {
        //ServerAPI.refreshMe();
        boolean loggedIn = false;
        if (me != null) {
            ////////////////////log.p(me.getPropertyIndex().toString());
            if ((me.authtoken.get() != null) && (me.firstName.get() != null)) {
                loggedIn = true;
            }
        }
        token = Preferences.get("authtoken", null);
        //////////////////////log.p("token " + token);
        return loggedIn;
    }

    public static void login(User u, Callback<User> callback) {
        //signupOrLogin("user/login", u, callback);
        //to login, we look for the combination of 
        //password and phone number
        String fetchUrl = "{\"phone\": \""
                + u.phone.get() + "\" ," + "\"password\": \"" + u.password.get() + "\" }";

        ////////////////////log.p(fetchUrl);
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
            me = me();
            callback.onSucess(me);

            //signupOrLogin("ziemozi-users", u, callback);
        }
    }

    private static void signupOrLogin(String url, User u,
            final Callback<User> callback) {
        ////////////////////log.p(u.getPropertyIndex().toString());
        ////////////////////log.p("URL " + url);
        post(url).
                body(u.getPropertyIndex().toJSON()).
                getAsJsonMap(new Callback<Response<Map>>() {
                    @Override
                    public void onSucess(Response<Map> value) {
                        ////////////////////log.p("Response Code " + value.getResponseCode());
                        ////////////////////log.p(value.getResponseData().toString());
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
                        ////////////////////log.p(me.getPropertyIndex().toString());
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
        ////////////////////log.p("Refreshing Me " + me.getPropertyIndex().toString());
        Response<Map> map = get("ziemozi-users/" + me._id.get()).getAsJsonMap();
        ////////////////////log.p("Refreshing user " + map.getResponseData().toString());
        ////////////////////log.p("Refreshing User " + map.getResponseCode());
        if (map.getResponseCode() == 200) {
            me = new User();
            me.getPropertyIndex().
                    populateFromMap(map.getResponseData());
            ////////////////////log.p("Refreshed me " + me.getPropertyIndex().toString());
            me.getPropertyIndex().storeJSON("me.json");
            me.authtoken.set(Preferences.get("authtoken", ""));
            token = me.authtoken.get();
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
        ////////////////////log.p("Verify Result " + s.getResponseData());
        return "true".equals(s.getResponseData().get("success"));
    }

    public static boolean update(User u) {
        //first check if there is any need to update
        //check if the image file has http, if it does not have
        //then use cloudinary to save the image first
        //before proceeding
        ////////////////////log.p("Refresh user " + u.getPropertyIndex().toString());
        ////////////////////log.p("Me is " + me.getPropertyIndex().toString());
        boolean posted = true;
        ////////////////////log.p("Avatar " + u.avatar.get());
        if (u.avatar.get().indexOf("file:") >= 0) {

            try {
                Map uploadResult = cloudinary.uploader().
                        upload(u.avatar.get(), ObjectUtils.emptyMap());
                String uploadUrl = uploadResult.get("url").toString();
                u.avatar.set(uploadUrl);
                ////////////////////log.p("Avatar url " + uploadUrl);
            } catch (Exception e) {
                ////////////////////log.p("Image upload failed \n" + e.getMessage());
                ToastBar.showErrorMessage("Failed to upload profile image");
                posted = false;
            }
        }
        //me.getPropertyIndex().storeJSON("me.json");
        //Map uMap = u.getPropertyIndex().toMapRepresentation();
        //uMap.remove("_id"); // remove id field so the update can happen properly
        //u.getPropertyIndex().populateFromMap(uMap);
        ////////////////////log.p("Refresjing User Id " + me._id.get());
        ////////////////////log.p("Refreshing with " + u.getPropertyIndex().toString());
        Response<String> s = patch("ziemozi-users/" + me._id.get()).
                body(u.getPropertyIndex().toJSON()).getAsString();
        //////////////////////log.p(u.getPropertyIndex().toJSON());
        ////////////////////log.p("User Patch Response Code " + s.getResponseCode());
        ////////////////////log.p("User Patch Response " + s.getResponseData());
        //u.getPropertyIndex().storeJSON("me.json");
        localAPI.refreshMe();
        return (posted && (s.getResponseCode() == 201));
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
        if (!Storage.getInstance().exists("me.json")) {
            return me;
        }
        if (me == null) {
            me = new User();
            try {
                me.getPropertyIndex().loadJSON("me.json");
            } catch (Exception e) {
                me = null;
            }
        } else {
            me.getPropertyIndex().loadJSON("me.json");
        }
        if (me != null) {
            me.authtoken.set(Preferences.get("authtoken", ""));
        }
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
        ////////////////////log.p("\n\nRequest ResponseCode \n" + response.getResponseCode());
        ////////////////////log.p("\n\nRequest Response data \n" + response.getResponseData());
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

    public static List<ServiceContact> contactsfeed(int page, int size) {
        List<ServiceContact> news
                = genericZiemSearch("servicecontact",
                        ServiceContact.class,
                        "", page, size, "", "");
        return news;
    }

    public static ArrayList<Request> privacyCheckRequests(ArrayList<Request> toCheck) {
        //we cycle through  the list and drop the objects for which the privacy parameter is there
        //and it is set to false
        ////log.p("Start Filtering  " + new Date().toString());
        ArrayList filtered = new ArrayList<Request>();
        if (localAPI.me() == null) {
            return filtered;
        }
        for (Request p : toCheck) {
            //p.refreshComments();
            //p.refreshRequestParameters();
            p.refreshUser();
            //p.refreshService();
            String u_string = "";
            if ( p.ziemozi_user.size() > 0 )  u_string = p.ziemozi_user.get(0).getPropertyIndex().toString();
            String ptoString = p.getPropertyIndex().toString() + " " + u_string;
            if (!isPrivate(p)) {

                filtered.add(p);
            } else {

                if ((ptoString.contains(localAPI.me()._id.get()))
                        && (ptoString.contains(localAPI.me().authtoken.get()))
                        && (ptoString.contains(localAPI.me().phone.get()))) {
                    //filter this in
                    filtered.add(p);
                }
            }
        }
        ////log.p("Done filtering " +  new Date().toString());
        //now filter for group membership
        //Log.p("Checking For Group Membership " + filtered.size());
        User u = localAPI.me();
        u.refreshGroups();
        ArrayList<Request> s_filtered = new ArrayList<Request>();
        for (Object z : filtered) {
            //take user list and check if the user has access.
            Request r = (Request) z;
            r.refreshService();
            List<Service> sz = r.service.asList();
            //Log.p("Request " + r._id.get() + "has " + sz.size() + " services");
            for (Service s : sz) {
                ////log.p("Check for service " + s.name.get());
                s.refreshGroups();
                if (checkUserAccess(u, s)) {
                    s_filtered.add(r);
                }
            }
        }
        ////log.p("Done checking for group " + new Date().toString());
        //Log.p("After filtering we have  " + s_filtered.size());        
        return s_filtered;
    }

    private static boolean checkUserAccess(User u, Service z) {
        Boolean return_value = false;
        //u.refreshGroups();
        
        for (Group g : u.groups.asList()) {
            //Log.p(g.name.get());
            for (Group sg:z.groups.asList()) {
                //////log.p("Checking against " + sg.name.get());
                //Log.p(g._id.get() + "against " + sg._id.get());
                if (g._id.get().equalsIgnoreCase(sg._id.get())) {
                    //Log.p("Found A Match");
                    return_value = true;
                }
            }
        }
        return return_value;
    }

    public static boolean isPrivate(Request p) {
        //to determine if it is private
        //look at the summary and extract the private  setting
        Boolean isprivate = false;
        //////////log.p(p.summary.get());
        List l = StringUtil.tokenize(p.summary.get(), "<p>");
        ////log.p("Number of tokens in request summary is " + l.size());
        for (Object s : l) {
            String ss = s.toString();
            ////log.p(s.toString());
            if (ss.contains("Private")) {
                ////log.p(ss);            
                if (ss.contains("true")
                        || ss.contains("yes") 
                        || ss.contains("Yes") || ss.contains("YES")) {
                    isprivate = true;
                }
            }
        }
        return isprivate;
    }

    public static List<Request> newsfeed(int page, int size) {
        ArrayList<Request> news
                = genericZiemSearch("request",
                        Request.class,
                        "", page, size, "", "");
//        for (Request r:news){
//            ////////////////////log.p("\nNews user " + r.ziemozi_user.get(0)._id.get() + " " + r.ziemozi_user.get(0).fullName());
//        }
        /**
         * return processRequestResponse( get("requests"). queryParam("page", ""
         * + page). queryParam("size", "" + size).getAsJsonMap());
         */
//        for (Request r:news){
        //           news.remove(r);
        //       }

        return privacyCheckRequests(news);
    }

    public static List<Request> newsfeed(int page, int size, boolean withComments) {
        if (withComments) {
            return newsfeed(page, size);
        }
        List<Request> news
                = genericZiemSearch("request",
                        Request.class,
                        "", page, size, "", "");
//        for (Request r:news){
//            ////////////////////log.p("\nNews user " + r.ziemozi_user.get(0)._id.get() + " " + r.ziemozi_user.get(0).fullName());
//        }
        /**
         * return processRequestResponse( get("requests"). queryParam("page", ""
         * + page). queryParam("size", "" + size).getAsJsonMap());
         */
        //Log.p("Number of requests in feed " + news.size());
        ArrayList rnews = new ArrayList();
        for (Request r : news) {
            if ((r._parent_id.get() == null) || (r._parent_id.get().indexOf("NULL") >= 0)) {
                rnews.add(r);
            }
        }
        //Log.p("Number of proper requests in feed " + rnews.size());
        return privacyCheckRequests(rnews);
    }

    /**
     * algoritmicNewsfeed Retrieve requests based on algorithm We will have to
     * eventually find a way to modify algorithm on the server For now we make
     * it based on first retrieving based on whether they are newest then check
     * whether they are marked private and if this is the person that sent them
     * out next we prioritize those within the home region of the user next we
     * prioritize those requests whose parents or parents parents he has
     * indicated interest in This will be implemented much later on the server
     * The newsfeed on the client will be considered as having already been
     * filtered and no further action will be taken we have to filter and order
     * - i think the only one we can do on the client for now is to remove the
     * ones that are confidential
     *
     * @param page
     * @param size
     * @return
     */
    public static List<Request> algorithmicNewsfeed(int page, int size) {

        ArrayList<Request> news
                = genericZiemSearch("request",
                        Request.class,
                        "", page, size, "", "");
        return privacyCheckRequests(news);
    }

    public static boolean post(Post pd) {
        String key = post("post/new").body(pd.getPropertyIndex().toJSON()).
                getAsString().getResponseData();
        pd.id.set(key);
        return key != null;
    }

    public static boolean postRequest(Request pd) {

        //////////////////////log.p("\n " + pd.getPropertyIndex().toJSON());
        //we want to save locally first
        //but this means we will not have the _id. we can only have the id
        //so we save the request and the request parameters but we use the id as the unique
        //references 
        boolean post = false;
        showMap(pd.getPropertyIndex().toMapRepresentation());
        Response<Map> res = post("requests").body(pd.getPropertyIndex().toJSON())
                .getAsJsonMap();
        if ((res.getResponseCode() == 201)
                || (res.getResponseCode() == 200)) {
            post = true;
            Request r = new Request();
            //r.getPropertyIndex().populateFromMap(l);
            //r.getPropertyIndex().loadJSON("postRequestParameter");
            r.getPropertyIndex().populateFromMap(res.getResponseData());
            //////////////////////log.p("RequestParameter retrieved " + r.getPropertyIndex().toString());
            pd._id.set(r._id.get());
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
                            = (PropertyBusinessObject) type.newInstance();
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

    public static ArrayList<Category> categories() {
        ArrayList<Category> c = new ArrayList<Category>();
        //check the last update date so we can pull from storage
        c = processCategoryResponse(
                get("category").getAsJsonMap());
        return c;
    }

    public static ArrayList<Provider> providers() {
        ArrayList<Provider> c = new ArrayList<Provider>();
        c = processProviderResponse(
                get("providers").getAsJsonMap());
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
        String url = "service";
        int max = 0;
        int page = 0;
        int size = 100;
        String hint = "";
        String filter = "";
        String query = "";
        //////////////////log.p("Type of Selector: " + p.getClass().getCanonicalName());
        ////////////////////log.p("\nFilter is: " + filter);
        if (p.getClass().getCanonicalName().indexOf("Provider") >= 0) {
            Provider pr = (Provider) p;
            pr.refresh();
            serviceList.addAll(pr.services.asList());
            return serviceList;
        }
        ArrayList<Service> services = genericZiemSearch(url,
                Service.class,
                query, page, size, hint, filter);
        //////////log.p("Found " + services.size() + "services");
        for (Service s : services) {
            s.refresh();
            //////////log.p(" Service " + s.getPropertyIndex().toString());
            if (p.getClass().getCanonicalName().indexOf("Category") >= 0) {
                Category c = (Category) p;
                //////////log.p("This is the service being checked " + s.name.get());
                List<Category> cl = s.category.asList();
                if (cl.size() > 0) {
                    ////////////log.p("Number of categories for service " + cl.size());
                    ////////////log.p("\nService Category: \n" + s.category.get(0).getPropertyIndex().toString() +"\n" );
                    for (Category cc : cl) {
                        ////////////log.p("\nCategory attached to service" + cc._id.get() + " " + cc.name.get());
                        ////////////log.p("\nCategory to match " + c._id.get() + " " + c.name.get());
                        if (cc._id.get() != null) {
                            if (cc._id.get().equalsIgnoreCase(c._id.get())) {
                                //////////log.p("Matched Category " + cc._id.get());
                                serviceList.add(s);
                                //////////log.p("Services in Category " + serviceList.size());
                            }
                        } else {
                            //////////log.p("Null id " + cc.getPropertyIndex().toString(true));
                            //////////log.p("Service " + s.getPropertyIndex().toString(true));
                        }
                    }
                }
            }
            if (p.getClass().getCanonicalName().indexOf("Provider") >= 0) {
                Provider pr = (Provider) p;
                //////////////////log.p("Provider " + pr.name.get() + " " + pr._id.get());
                //pr.refresh();
                //serviceList.addAll(pr.services.asList());

                s.refresh();
                List<Provider> pl = s.providers.asList();
                if (pl.size() > 0) {
                    for (Provider pp : pl) {
                        //////////log.p("Service Provider to compare  " + pp.name.get() + " " + pp._id.get());
                        if (pp._id.get() != null) {
                            if (pp._id.get().equalsIgnoreCase(pr._id.get())) {
                                serviceList.add(s);
                            }
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
            //////////////////////log.p("Parent " + parent.toString());
            //////////////////////log.p("Is Categories " + parent.toString().indexOf("Categories"));
            //////////////////////log.p("Is Providers " + parent.toString().indexOf("Providers"));
            //////////////////////log.p("Is ServiceGroup " + parent.toString().indexOf("ServiceGroup"));            
            if (parent.toString().indexOf("Categories") >= 0) {
                //categories
                v = new Vector();
                ArrayList<Category> c = localAPI.categories();
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
                ArrayList<Provider> c = localAPI.providers();
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
                ArrayList<Service> c = localAPI.serviceGroups();
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
                ////////////////////log.p("Category Map\n ");
                showMap(m);
                Category p = new Category();
                p.getPropertyIndex().populateFromMap(m);
                responseList.add(p);
                //////////////////////log.p(p.getPropertyIndex().toString());                
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
                //////////////////////log.p(p.getPropertyIndex().toString());
            }
            return responseList;
        }
        return null;
    }

    private static ArrayList<Service> processServiceDefintionResponse(Response<Map> response) {
        if (response.getResponseCode() == 200) {
            //////////////////////log.p(response.toString());
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
        ArrayList<T> responseList = new ArrayList<>();
        //////////////////////log.p("Query " + text);
        //{"$orderby": "{" ,"_created": "-1" }}
        // String fetchUrl = "{\"$orderby\":"
        //       + "{" + "\"_created\": " + "-1" + " }}";
        // hint = fetchUrl;
        //
        String query = " Select * from " + url;
        HashMap t = new HashMap();
        if (!text.isEmpty()) {
            //the text query should be delimited with , 
            //name=u,p=z

            //t.putAll(mapFromText(text));
            query = query + " where " + formattedQuery(text) + " ORDER BY _created DESC ";
        } else {
            query = query + " " + "ORDER BY _created DESC ";
        }
        query = query + " LIMIT " + page + "," + size + ";";
        //////log.p(query);
        //HashMap mquery = new HashMap();
        //mquery.put("ORDER BY", "_created");
        //mquery.put("LIMIT", size );
        //mquery.put("OFFSET", page );
        //mquery.putAll(t);
        //////////////////////log.p("\nMapped Query\n" + testQuery(mquery));

        try {
            Database db = Database.openOrCreate(dbname);
            Cursor cur = null;
            Row currentRow = null;
            //DAOProvider dbProvider = new DAOProvider(db, 1);
            //DAO<Map> objectDAO = (DAO<Map>) dbProvider.get(url);
            //List<Map> l = objectDAO.fetch(mquery);
            ArrayList<Map> l = new ArrayList<Map>();
            HashMap mRow = new HashMap();
            cur = db.executeQuery(query);
            int columns = cur.getColumnCount();
            boolean next = cur.next();

            while (next) {
                mRow = new HashMap();
                currentRow = cur.getRow();
                String[] currentRowArray = new String[columns];
                for (int iter = 0; iter < columns; iter++) {
                    String columnName = cur.getColumnName(iter);
                    String columnValue = cur.getRow().getString(iter);
                    //////log.p(columnName + " is " + columnValue);
                    mRow.put(columnName, columnValue);
                    //                               currentRowArray[iter] = currentRow.getString(iter);
                }

                l.add(mRow);
                next = cur.next();
            }

            cur.close();
            db.close();
            //////////////////log.p("Generic Search Found " + url + " " + l.size() + " records");

            for (Map m : l) {
                try {
                    showMap(m);
                    PropertyBusinessObject pb
                            = (PropertyBusinessObject) type.newInstance();
                    pb.getPropertyIndex().populateFromMap(m);
                    //
                    //////////////////////log.p(pb.getPropertyIndex().toString());
                    responseList.add((T) pb);
                } catch (Exception err) {
                    ////////////////log.p(err.getMessage());
                }
            }
            // cur.close();

        } catch (Exception ex) {
            ////////////////log.p(ex.getMessage());
        }

        return responseList;
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
        // ////////////////////log.p("Saving Request PArameter ..\n");
        //  HashMap l = (HashMap) post("request_parameter").body(pd.getPropertyIndex().toJSON()).getAsJsonMap().getResponseData();
        // Storage.getInstance().writeObject("postRequestParameter", key);
        //  RequestParameter r = new RequestParameter();
        //  r.getPropertyIndex().populateFromMap(l);
        //  ////////////////////log.p("RP Save Response \n" + r.getPropertyIndex().toString());
        //  pd._id.set(r._id.get());
        //  return r._id.get() != null;  
//        String key = post("request-parameters").body(pd.getPropertyIndex().toJSON()).
        //              getAsString().getResponseData();
        ////////////////////log.p("\n Saving Request Parameter " + pd.getPropertyIndex().toJSON(), Log.DEBUG);
        Response<Map> res = post("request-parameters").body(pd.getPropertyIndex()
                .toJSON()).getAsJsonMap();
        //HashMap l = (HashMap) post("request_parameter").body(pd.getPropertyIndex().toJSON()).getAsJsonMap().getResponseData();
        //  Storage.getInstance().writeObject("postRequestParameter", key);

        //    ////////////////////log.p("\n RequestParameter response data is \n" + key);
        //try {
        //    JSONParser j = new JSONParser();
        //    Reader r = new InputStreamReader(
        //             Storage.getInstance().createInputStream("postRequestParameter"));
        //    Map<String,Object> l = j.parseJSON(r ) ;                  
        // } catch (Exception ex) {
        //     ////////////////////log.p(ex.getMessage());
        // }
        //Display.getInstance().getResourceAsStream(getClass(), "/anapioficeandfire.json"), "UTF-8")) {
        //Map<String, Object> data = json.parseJSON(r);
        //    Map<String,Object> l = j.parseJSON(new StreamReader )
        //Storage.getInstance().writeObject("postRequest", key);
        RequestParameter r = new RequestParameter();
        //r.getPropertyIndex().populateFromMap(l);
        //r.getPropertyIndex().loadJSON("postRequestParameter");
        r.getPropertyIndex().populateFromMap(res.getResponseData());
        //////////////////////log.p("RequestParameter retrieved " + r.getPropertyIndex().toString());
        pd._id.set(r._id.get());
        return r._id.get() != null;
    }

    public static Location getCurrentLocation() {
        return LocationManager.getLocationManager().getCurrentLocationSync();
    }

    public static void like(Request p) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static List<Request> searchRequests(String text, int page, int amount) {
        ////////////////////log.p("Searching For " + text);
        //    https://mydb-fafc.restdb.io/rest/people?q={}&h={"$orderby": {"name": 1, "age": -1}}
        ArrayList<Request> results = new ArrayList<Request>();
        ArrayList<Request> requests = new ArrayList<Request>();
        requests = genericZiemSearch("request",
                Request.class,
                "", page, amount, "", text.toLowerCase());
        //we have to use a different search algorithm since we are always searching local
        //we just filter the records based on whether we have text in the summary
        for (Request r : requests) {
            r.refreshService();
            String rs = r.summary.get().toLowerCase();
            String ts = text.toLowerCase();
            if (r.service.size() > 0) {
                String hs = r.service.get(0).name.get().toLowerCase();
                String ds = r.service.get(0).description.get().toLowerCase();
                if ((rs.indexOf(ts) >= 0)
                        || (hs.indexOf(ts) >= 0)
                        || (ds.indexOf(ts) >= 0)) {
                    results.add(r);
                }
            } else {
                if (rs.indexOf(ts) >= 0) {
                    results.add(r);
                }
            }
        }
        return results;
    }

    public static List<Request> requestsOf(String get, int page, int amount) {
        return genericZiemSearch("requests",
                Request.class,
                "", page, amount, "", get);
    }

    public static void showMap(Map m) {
        for (Object k : m.keySet()) {
            //////log.p(k.toString() + " -- " + m.get(k).toString());
        }
    }

    public static void showMapp(Map m) {
        for (Object k : m.keySet()) {
            //////////log.p(k.toString() + " -- " + m.get(k).toString());
        }
    }

    public static Service getService(String _id) {
        Service s = new Service();
        Response<Map> map = get("service-definition/" + _id).getAsJsonMap();
        ////////////////////log.p("Response " + map.getResponseData());
        if (map.getResponseCode() == 200) {
            s.getPropertyIndex().
                    populateFromMap(map.getResponseData());
        }
        return s;
    }

    public static ServiceAttributeType getServiceAttributeType(String _id) {
        ////////////////////log.p("Get Service Attribute Type " + _id);
        ServiceAttributeType s = new ServiceAttributeType();
        Response<Map> map = get("service-attribute-type/" + _id).getAsJsonMap();
        ////////////////////log.p("Response " + map.getResponseData());
        if (map.getResponseCode() == 200) {
            s.getPropertyIndex().
                    populateFromMap(map.getResponseData());
        }
        return s;
    }

    public static ArrayList<Request> getCommentsFor(String id) {
        //just get the request
        //the comments will be attached to the record
        //so all we need to do is to retrieve the service
        //or just retrieve the records whose parent id is the same as the string id.
        // do ziemsearch on the request
        ArrayList<Request> l
                = localAPI.genericZiemSearch("request",
                        Request.class, "_parent_id:" + id, 0, 9999, "", "");
        return l;
    }

    public static Boolean saveRequestParameters(ArrayList<RequestParameter> summedRp, Request rq) {
        boolean posted = false;
        //we will post them as one batch. So we create them as a string and then save
        //first we have to pre_process before saving
        //media has to be saved first and the media has to be saved
        cloudinary.config.privateCdn = false;
        String jsonRequestParameter = "[\n";
        for (RequestParameter r : summedRp) {
            String value = "";
            String base_type = r.service_attribute.get(0).type_of_attribute.get(0).base_type.get();
            if (base_type.equalsIgnoreCase("image")
                    || base_type.equalsIgnoreCase("video")
                    || base_type.equalsIgnoreCase("media")
                    || base_type.equalsIgnoreCase("file")) {
                String[] imageList = Util.split(r.value.get(), "||");
                for (String img : imageList) {
                    //for each image, save and append back to value
                    ////////////////////log.p("Image to save " + img);
                    if (!img.isEmpty()) {
                        try {
                            Map uploadResult = cloudinary.uploader().
                                    upload(img, ObjectUtils.emptyMap());
                            String uploadUrl = uploadResult.get("url").toString();
                            ////////////////////log.p("Image is saved as " + uploadUrl);
                            if (value.length() < 2) {
                                value = uploadUrl;
                            } else {
                                value = value + " || " + uploadUrl;
                            }
                        } catch (Exception e) {
                            ////////////////////log.p("Image upload failed \n" + e.getMessage());
                            posted = false;
                        }
                    }
                }
                r.value.set(value);
                r.summarize();
            };

            jsonRequestParameter = jsonRequestParameter + r.getPropertyIndex().toJSON() + ",";
        }
        String summedSummary = rq.requestSummary(summedRp);
        rq.summary.set(summedSummary);

        jsonRequestParameter = jsonRequestParameter.substring(0,
                jsonRequestParameter.lastIndexOf("}") + 1) + "\n]";
        ////////////////////log.p(jsonRequestParameter);
        Response res = post("request-parameters").body(
                jsonRequestParameter).getAsString();

        if (res.getResponseCode()
                == 201 || res.getResponseCode() == 200) {
            posted = true;
        }
        ////////////////////log.p("\n bulk parameter save response" + res.getResponseData().toString());

        res = patch("requests/" + rq._id.get()).body(rq).getAsString();
        ////////////////////log.p(res.getResponseCode() + "\n" + res.getResponseData());
        return posted;
    }

    public static ServiceAttribute getServiceAttribute(String post) {
        ////////////////////log.p("Get Service Attributexx " + _id);
        ServiceAttribute a = new ServiceAttribute();
        String fetchUrl = "{_id:"
                + post + "}";
        ////////////////log.p("Fetching Attribute " + fetchUrl);
        ArrayList<ServiceAttribute> l = genericZiemSearch("serviceattribute",
                ServiceAttribute.class,
                fetchUrl, 0, 1, "", "");
        if (l.size() > 0) {
            return l.get(0);
        } else {
            return a;
        }
    }

    public static void updateRequestSummary(Request aThis) {
        ////////////////////log.p("Updating this request " + aThis._id.get());
        Response res = patch("requests/" + aThis._id.get()).
                body(aThis.getPropertyIndex().toJSON()).getAsString();
        ////////////////////log.p(res.getResponseData().toString());
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

        ////////////////////log.p(fetchUrl);
        ArrayList<Service> services = genericZiemSearch("service",
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

        ////////////////////log.p(fetchUrl);
        ArrayList<Service> services = genericZiemSearch("service",
                Service.class,
                fetchUrl, 0, 10, "", "");
        if (services != null) {
            amFine = services.get(0);
        }
        return amFine;
    }

    public static List<Request> geoSearchRequests(String text, int page, int amount) {
        //we need to implement a geo search request functionality but not yet
        return localAPI.searchRequests(text, page, amount);
    }

    private static Map mapFromText(String text) {
        //text is of the form
        //fielfname = fieldvalue, fieldname=fieldvalue
        //so we break them up using the , separator and then for each one of the terms we extract
        text = StringUtil.replaceAll(text, "{", "");
        text = StringUtil.replaceAll(text, "}", "");
        HashMap m = new HashMap();
        List fields = StringUtil.tokenize(text, ",");
        for (Object f : fields) {

            String s = f.toString();
            int eqsign = s.indexOf(":");
            if (eqsign >= 0) {
                String fieldname = s.substring(eqsign + 1).trim();
                String fieldvalue = s.substring(0, eqsign).trim();
                m.put(fieldname, fieldvalue);
            }
        }
        return m;
    }

    private static Map mapFromText(String text, String sep) {
        //text is of the form
        //fielfname = fieldvalue, fieldname=fieldvalue
        //so we break them up using the , separator and then for each one of the terms we extract
        text = StringUtil.replaceAll(text, "{", "");
        text = StringUtil.replaceAll(text, "}", "");
        HashMap m = new HashMap();
        List fields = StringUtil.tokenize(text, ",");
        for (Object f : fields) {
            ////////////////////log.p("\n\n" + f.toString()+" \n");
            String s = f.toString();
            int eqsign = s.indexOf(sep);
            ////////////////////log.p("\n Eq sign at " + eqsign );
            if (eqsign >= 0) {
                String fieldname = s.substring(eqsign + 1).trim();
                String fieldvalue = s.substring(0, eqsign).trim();
                m.put(fieldvalue, fieldname);
            }
        }
        return m;
    }

    private static String formattedQuery(String text) {
        String q = "";
        Map m = mapFromText(text);
        for (Object k : m.keySet()) {
            q = " " + m.get(k).toString() + " = " + "'" + k.toString() + "'";
        }
        return q;
    }

    private static String testQuery(HashMap mquery) {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from \"").append("TABLENAME").append("\" where ");
        int len = mquery.size();
        int i = 0;
        String[] vals = new String[len];
        for (Object key : mquery.keySet()) {
            String strKey = (String) key;
            vals[i] = ("" + mquery.get(strKey));
            sb.append("\"").append(strKey).append("\"=? ");
            if (i++ < len - 1) {
                sb.append("AND ");
            }
        }
        return sb.toString();

    }

    public static ArrayList<User> getUserFor(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<User> l = new ArrayList<User>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select ziemozi_user from request where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Log.p(userString);
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                User cs = new User();
                Log.p(m.getClass().getName());
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        //////////////log.p(post);
        /*
        if (userString.indexOf("}") > 0) {
            userString = StringUtil.replaceAll(userString, "}{", "},{");
            //////////log.p("UserString " + userString);
            User cc = new User();
            Random r = new Random(64088);
            String ts = r.nextInt() + "us" + r.nextInt();
            Storage.getInstance().writeObject(ts, userString);
            List<User> k = cc.getPropertyIndex().loadJSONList(ts);
            l.addAll(k);
            Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    /**
     * getServiceForRequest from the database
     *
     * @param post
     * @return
     */
    public static ArrayList<Service> getServiceForRequest(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<Service> l = new ArrayList<Service>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select service from request where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                //////////log.p("Parsed ==>\n" + m.toString());
                Service cs = new Service();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        /*
        if (userString.indexOf("{") >= 0){
        Service cc = new Service();
        Random r = new Random(6488000);
        String ts = "ser"+r.nextInt();
        Storage.getInstance().writeObject(ts,userString);
        List<Service> k =cc.getPropertyIndex().loadJSONList(ts);
        l.addAll(k);
        Storage.getInstance().deleteStorageFile(ts);  
        }
         */
        return l;
    }

    /*
    Parse the string and return it as map of objects
    
     */
    public static List<Map<String, Object>> parseString(String str) {
        JSONParser json = new JSONParser();
        Map<String, Object> data = new HashMap();
        try {
            Reader r = new StringReader(str);
            data = json.parseJSON(r);
        } catch (IOException ex) {
            Log.p("Parsing Failed " + ex.getMessage());
        }
        Log.p("Parsed Data \n" + data.toString());
        java.util.List<Map<String, Object>> content = (java.util.List<Map<String, Object>>) data.get("root");
        //////////log.p("Parse result is " + content.toString());
        return content;
    }

    /**
     * get the service before it is saved to the server
     *
     * @param post
     * @return
     */
    public static ArrayList<Service> getServiceForRequestLocal(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<Service> l = new ArrayList<Service>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select service from request where id = " + post);

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ////////////////log.p(userString);
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                Service cs = new Service();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        /*        
        if (userString.indexOf("}") > 0){ 
        Service cc = new Service();
        Random r = new Random(6488);
        String ts = "us"+r.nextInt();
        Storage.getInstance().writeObject(ts,userString);
        List<Service> k =cc.getPropertyIndex().loadJSONList(ts);
        l.addAll(k);
        Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    public static ArrayList<String> getCategoryIcon(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<String> l = new ArrayList<String>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select icon from category where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                l.add(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return l;
    }

    public static ArrayList<String> getProviderIcon(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<String> l = new ArrayList<String>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select icon from provider where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                l.add(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return l;
    }

    public static ArrayList<Service> getCommentServicesFor(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<Service> l = new ArrayList<Service>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select comment_services from service where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //////////log.p("Comment For Service text \n" + userString);
        if (userString.indexOf("{") >= 0) {
            ////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                ////////log.p("Parsed ==>\n" + m.toString());
                Service cs = new Service();
                cs.getPropertyIndex().populateFromMap(m);
                ////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        /*        
        if (userString.indexOf("}") > 0) {        
        Service cc = new Service();
        Random r = new Random(6488);
        String ts = "us"+r.nextInt();
        Storage.getInstance().writeObject(ts,userString);
       
        List<Service> k =cc.getPropertyIndex().loadJSONList(ts);
        l.addAll(k);
        Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    public static ArrayList<ServiceAttribute> getServiceAttributeForService(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<ServiceAttribute> l = new ArrayList<ServiceAttribute>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select service_attributes from service where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // we have to process the string ourself
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the serviceattribute to be refreshed from \n" + userString
            //        + "\n for  " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                //////////log.p("Parsed ==>\n" + m.toString());
                ServiceAttribute cs = new ServiceAttribute();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From ServiceAttribute \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        /*
        if (userString.indexOf("}") > 0) {
                ServiceAttribute cc = new ServiceAttribute();
        Random r = new Random(64888);
        String ts = "cat"+r.nextInt();
        Storage.getInstance().writeObject(ts,userString);
        List<ServiceAttribute> k =cc.getPropertyIndex().loadJSONList(ts);
        l.addAll(k);
        Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    public static ArrayList<String> getServiceIcon(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<String> l = new ArrayList<String>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select logo from service where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                l.add(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return l;
    }

    public static ArrayList<Category> getServiceCategory(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<Category> l = new ArrayList<Category>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select category from service where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // we have to process the string ourself
        if (userString.indexOf("{") >= 0) {
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                Category cs = new Category();
                cs.getPropertyIndex().populateFromMap(m);
                l.add(cs);
            }
        }
        /*        
        if (userString.indexOf("}") > 0 ){
            //userString.replaceAll("}{", "},{");
            
            userString = StringUtil.replaceAll(userString, "}{", "},{");
        ////////log.p(userString);    
        Category cc = new Category();
        Random r = new Random(648800);
        String ts = "scat"+r.nextInt();
        Storage.getInstance().writeObject(ts,userString);
        List<Category> k =cc.getPropertyIndex().loadJSONList(ts);
        l.addAll(k);
        Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    public static ArrayList<Provider> getServiceProvider(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<Provider> l = new ArrayList<Provider>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select providers from service where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // we have to process the string ourself
        if (userString.indexOf("{") >= 0) {
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                Provider cs = new Provider();
                cs.getPropertyIndex().populateFromMap(m);
                l.add(cs);
            }
        }
        /*        
        if (userString.indexOf("}") > 0) {
                        Provider cc = new Provider();
        Random r = new Random(6488);
        String ts = "cat"+r.nextInt();
        Storage.getInstance().writeObject(ts,userString);
        List<Provider> k =cc.getPropertyIndex().loadJSONList(ts);
        l.addAll(k);
        Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    public static ArrayList<ServiceAttributeType> getAttributeTypeFor(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<ServiceAttributeType> l = new ArrayList<ServiceAttributeType>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select type_of_attribute from serviceattribute where _id = "
                    + "'" + post + "'");
            //////////log.p("select type_of_attribute from serviceattribute where _id = "
            //       + "'" + post + "'");
            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                //////////log.p("Attribute type \n" + userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // we have to process the string ourself
        if (userString.indexOf("{") >= 0) {
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                ServiceAttributeType cs = new ServiceAttributeType();
                cs.getPropertyIndex().populateFromMap(m);
                l.add(cs);
            }
        }
        /*        
        if (userString.indexOf("}") >0) {
        ServiceAttributeType cc = new ServiceAttributeType();
        Random r = new Random(6488);
        String ts = "cat"+r.nextInt();
        Storage.getInstance().writeObject(ts,userString);
        List<ServiceAttributeType> k =cc.getPropertyIndex().loadJSONList(ts);
        l.addAll(k);
        Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    public static ArrayList<ServiceAttribute> getWatchAttributeFor(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<ServiceAttribute> l = new ArrayList<ServiceAttribute>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select watch_this_attribute from serviceattribute where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // we have to process the string ourself
        if (userString.indexOf("{") >= 0) {
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                ServiceAttribute cs = new ServiceAttribute();
                cs.getPropertyIndex().populateFromMap(m);
                l.add(cs);
            }
        }
        /*        
        if (userString.indexOf("}") > 0 ){
        ServiceAttribute cc = new ServiceAttribute();
        Random r = new Random(6488);
        String ts = "attr"+r.nextInt();
        Storage.getInstance().writeObject(ts,userString);
        List<ServiceAttribute> k =cc.getPropertyIndex().loadJSONList(ts);
        l.addAll(k);
        Storage.getInstance().deleteStorageFile(ts);
    }
         */
        return l;
    }

    /**
     * saveLocal Save a businessobject to local database
     *
     * @param p
     * @return
     */
    public static boolean saveLocal(PropertyBusinessObject p) {
        Boolean saved = false;

        try {
            Database db = Display.getInstance().openOrCreate("ziemozi");
            com.ixzdore.properties.SQLMap sm = SQLMap.create(db);
            sm.setVerbose(true);
            sm.createTable(p);
            sm.insert(p);
            saved = true;
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.sendLogAsync();
        }
        return saved;
    }

    /**
     * saveLocalBatch Save a businessobject to local database
     *
     * @param p = list of parameters
     * @return
     */
    public static boolean saveLocalBatch(ArrayList<PropertyBusinessObject> p) {
        if (p == null) {
            return true;
        }
        Boolean saved = false;
        Vector<ArrayList<PropertyBusinessObject>> v = new Vector<ArrayList<PropertyBusinessObject>>();
        int batchsize = 10;
        int i = 0;
        while (i < p.size()) {
            int k = 0;
            ArrayList<PropertyBusinessObject> batch = new ArrayList<PropertyBusinessObject>();
            while (k < batchsize) {
                if ((i + k) < p.size()) {
                    batch.add(p.get(i + k));
                }
                k++;
            }
            v.add(batch);
            i = i + batchsize;
        }
        for (ArrayList<PropertyBusinessObject> pp : v) {
            try {
                Database db = Display.getInstance().openOrCreate("ziemozi");
                com.ixzdore.properties.SQLMap sm = SQLMap.create(db);
//            db.beginTransaction();
                ////////////log.p("Saving " + p.size() + "Records");
                sm.bulkInsert(pp);
//            sm.setVerbose(true);
//            sm.createTable(p);
//            sm.insert(p);
                saved = true;
//            db.commitTransaction();
                sm = null;
                db.close();
            } catch (Exception e) {
                //e.printStackTrace();
                saved = false;
                Log.p("\n\n" + e.getMessage());
                //Log.sendLogAsync();           
            }
            //if for some reason we did not save any batch we still continue
        }
        return saved;
    }

    /**
     * saveLocalAndShow Save a businessobject to local database
     *
     * @param p
     * @return
     */
    public static void saveLocalAndShow(PropertyBusinessObject p, Form f) {
        Boolean saved = false;

        try {
            Database db = Display.getInstance().openOrCreate("ziemozi");
            com.ixzdore.properties.SQLMap sm = SQLMap.create(db);
            sm.setVerbose(true);
            sm.createTable(p);
            sm.insert(p);
            saved = true;
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        f.show();
        return;
    }

    /**
     * saveLocal Save a businessobject to local database and ensure it is unique
     *
     * @param p
     * @return
     */
    public static boolean saveLocal(PropertyBusinessObject p, Boolean unique) {

        Boolean saved = false;
        Database db = null;
        String _id = p.getPropertyIndex().toMapRepresentation().get("_id").toString();
        try {
            db = Display.getInstance().openOrCreate("ziemozi");
            com.ixzdore.properties.SQLMap sm = SQLMap.create(db);
            PropertyBusinessObject nob = null;

            if (unique) {
                //first get the _id of the object
                //check if the object exists in local
                //and if it exists, then
                //delete it before adding 
                //the new one.
                sm.createTable(p);
                /*
           Class pc = p.getClass();
           String pname = pc.getCanonicalName();
                      ////////////////log.p(pname);
           int nc = pname.lastIndexOf(".");
           pname = pname.substring(nc+1).toUpperCase();
                 */
                String stmnt = "DELETE FROM " + p.getPropertyIndex().getName() + " WHERE _id = '" + _id + "'";
                ////////////////log.p(stmnt);
                db.execute(stmnt);

            }

            sm.setVerbose(true);
            sm.createTable(p);
            sm.insert(p);
            saved = true;
            db.close();
            db = null;
        } catch (Exception e) {
            ////////////////log.p(e.getMessage());
            e.printStackTrace();
            Log.sendLogAsync();
        }

        return saved;
    }

    public static boolean saveWithDao(PropertyBusinessObject p) {
        Boolean saved = false;
        Request r = (Request) p;
        Database db = null;
        //DAOProvider dbProvider = new DAOProvider(db, 1);        
        DAO<Map> requestDAO = null;
        try {
            ////////////////log.p("\n\nSaving to local\n\n" + p.getPropertyIndex().toJSON());
            //
            db = Database.openOrCreate(dbname);
            DAOProvider dbProvider = new DAOProvider(db, 1);
            requestDAO = (DAO<Map>) dbProvider.get("REQUEST");
            // for(Request r:requests){
            Map o = requestDAO.newObject();
            Map m = r.getPropertyIndex().toMapRepresentation();
            ////////////////log.p("\n\n Map of request to save \n\n");
            showMap(m);
            //////////////////log.p("\nFrom\n");showMap(m);
            ////////////////////log.p("\nTo\n");showMap(o);
            ////////////////log.p("\n\n Map of new request\n\n");
            showMap(o);
            requestDAO.unmap(o, m);
            ////////////////log.p("\n\n Map of new request mapped \n\n");
            showMap(o);
            //////////////////log.p("\nMapped\n");showMap(o);
            requestDAO.save(o);
            // List<Map> cat = requestDAO.fetchAll(); 
            // for (Map ml:cat){
            //     showMap(ml);
            // }
            saved = true;
            //}
            db.close();

        } catch (Exception e) {
            //////////////////log.p("Request load had an exception");
            e.printStackTrace();
        }
        return saved;
    }

    /**
     * save the request records to the server
     */
    public static void saveRequestsToServer() {
        //lets load from database and see if we can reconstruct the 
        //request
        //////log.p("Saving Requests to Server");

        //List<Request> newsfeed = newsfeed(0, 9999);
        List<Request> newsfeed = genericZiemSearch("request",
                Request.class,
                "", 0, 9999, "", "");
        // ////log.p("Requests to check " + newsfeed.size());
        for (Request r : newsfeed) {
            // ////log.p(r._id.get());
            if (r._id.get().indexOf("local_") >= 0) {
                r.refreshUser();
                r.refreshService();
                r.refreshRequestParameters();
                //////////////////log.p("\n\nService \n\n" + r.service.get(0).getPropertyIndex().toJSON());
                r.service.get(0).refresh();
                //r.service.get(0).service_attributes.clear();
                //r.service.get(0).comment_services.clear();                
                //////////log.p("\n\nService in request to be saved \n\n" + r.service.get(0).getPropertyIndex().toJSON());
                //r.refreshComments();
                //r.comments.clear();
//                r.provider.set(null);
                //r.parent.set(null);
                //r.provider.add(new Provider());
                //if (r.parent.get() == null ) r.parent.set(new Request());
                //r._id.set(null);
                ////////////////log.p("Request to save in plain string " + r.getPropertyIndex().toString());
                ////////////////log.p(" Request to save as json " + r.getPropertyIndex().toJSON());

                String localId = r._id.get();
                r._id.set(null);

                Boolean saved = ServerAPI.postRequest(r);
                Log.p("Saved Request " + r._id.get()+ saved);
                if (saved) {
                    ArrayList<RequestParameter> summedRp = r.batchRequestParameters();
                    for (RequestParameter rp : summedRp) {
                        //////////////////log.p("Parent Id " + r._id.get());
                        rp._parent_id.set(r._id.get());
                        //rp._parent_field.set("request_parameters");
                        //rp._parent_def.set("request");
                        //   post = rp.save();
                    }
                    saved = ServerAPI.saveRequestParameters(summedRp, r);
                    //////log.p("Saved RequestParameters " + saved);
                }
                //postREquest updates the request._id. But we need to be able
                //to also save the request parameters
                //and then have the 1
                //save each request one after the other since we cannot be able to 
                //match server records to local records.
                //now delete the record
                if (saved) {
                    String stmnt = "DELETE FROM REQUEST WHERE _id = " + "'" + localId + "'";
                    String stmntp = " DELETE FROM REQUESTPARAMETER WHERE _parent_id = " + "'" + localId + "'";
                    ////////////////log.p(stmnt);
                    try {
                        Database db = Database.openOrCreate(dbname);
                        db.execute(stmnt);
                        db.execute(stmntp);
                        db.close();
                        db = null;
                    } catch (Exception e) {
                        ////////////////log.p("Delete Failed ");
                        e.printStackTrace();
                        Log.sendLogAsync();

                    }
                }
            }
        }

    }

    public static ArrayList<RequestParameter> getRequestParametersForRequest(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<RequestParameter> l = new ArrayList<RequestParameter>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select request_parameters from request where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.sendLogAsync();
        }
        ////////////////log.p(userString);
        if (userString.indexOf("{") >= 0) {
            ////////log.p("This is the Request Parameter  \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                ////////log.p("Parsed ==>\n" + m.toString());
                RequestParameter cs = new RequestParameter();
                cs.getPropertyIndex().populateFromMap(m);
                ////////log.p("From Request Parameter \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        /*        
        if (userString.indexOf("}") > 0){
        try {    
        RequestParameter cc = new RequestParameter();
        Random r = new Random(64088);
        String ts = "rqp"+r.nextInt();
        Storage.getInstance().writeObject(ts,userString);
        List<RequestParameter> k =cc.getPropertyIndex().loadJSONList(ts);
        l.addAll(k);
        Storage.getInstance().deleteStorageFile(ts);    
        }catch(Exception e){
            ////////////////log.p(e.getMessage());
            e.printStackTrace();
                            Log.sendLogAsync();
            
        }finally{
            //swallow the error
        }
        }
         */
        return l;

    }

    public static ArrayList<ServiceAttribute> getServiceAttributesForParameter(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<ServiceAttribute> l = new ArrayList<ServiceAttribute>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select service_attribute from requestparameter where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();
            Log.p("Found service attribute for requestparameter "+  post+  " " + next);
            while (next) {
                userString = c.getRow().getString(0);
                Log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.sendLogAsync();
        }
        //////////log.p(userString);
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                ServiceAttribute cs = new ServiceAttribute();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        //

        return l;

    }

    public static ArrayList<RequestParameter> getServiceForServiceAttribute(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<RequestParameter> l = new ArrayList<RequestParameter>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select request_parameters from request where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.sendLogAsync();
        }
        ////////////////log.p(userString);
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                RequestParameter cs = new RequestParameter();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        ///
        /*
        if (userString.indexOf("}") > 0) {
            try {
                RequestParameter cc = new RequestParameter();
                Random r = new Random(64088);
                String ts = "rqp" + r.nextInt();
                Storage.getInstance().writeObject(ts, userString);
                List<RequestParameter> k = cc.getPropertyIndex().loadJSONList(ts);
                l.addAll(k);
                Storage.getInstance().deleteStorageFile(ts);
            } catch (Exception e) {
                ////////////////log.p(e.getMessage());
                e.printStackTrace();
                Log.sendLogAsync();

            } finally {
                //swallow the error
            }
        }
         */
        return l;

    }

    public static List<ServiceContact> geoSearchContacts(String text, int page, int amount) {
        return searchContacts(text, page, amount);
    }

    public static ArrayList<ServiceContact> searchContacts(String text, int page, int amount) {
        //////////log.p("Search contacts for " + text);
        ArrayList<ServiceContact> results = new ArrayList<ServiceContact>();
        ArrayList<ServiceContact> requests = new ArrayList<ServiceContact>();
        requests = genericZiemSearch("servicecontact",
                ServiceContact.class,
                "", page, amount, "", "");
        //we have to use a different search algorithm since we are always searching local
        //we just filter the records based on whether we have text in the summary
        ////////////log.p(requests.toString());
        //////log.p("Service Contacts " + page + " " + amount + " " + requests.size());
        List<String> textTerms = StringUtil.tokenize(text, ",");
        for (ServiceContact r : requests) {
            // ////log.p("This Contact " + r.getPropertyIndex().toJSON());            
            r.refresh();
            String rs = r.summary().toLowerCase();

            for (String t : textTerms) {
                //////log.p(t);
                if (rs.lastIndexOf(t.toLowerCase()) > 0) {
                    //////log.p("found match for " + t);
                    results.add(r);
                    break;
                }
            }
        }
        return requests;
        //return results;

    }

    public static void like(ServiceContact p) {
        //like a service contact. Implementation should actually be a like message
    }

    public static ArrayList<Provider> getProviderForServiceContact(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<Provider> l = new ArrayList<Provider>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select providers from servicecontact where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // we have to process the string ourself
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                Provider cs = new Provider();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        //
        /*
        if (userString.indexOf("}") > 0) {
            Provider cc = new Provider();
            Random r = new Random(6488);
            String ts = "cat" + r.nextInt();
            Storage.getInstance().writeObject(ts, userString);
            List<Provider> k = cc.getPropertyIndex().loadJSONList(ts);
            l.addAll(k);
            Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    public static ArrayList<Category> getCategoryForServiceContact(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<Category> l = new ArrayList<Category>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select categories from servicecontact where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // we have to process the string ourself
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                Category cs = new Category();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        return l;
    }

    public static ArrayList<Service> getServiceForServiceContact(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<Service> l = new ArrayList<Service>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select services from servicecontact where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // we have to process the string ourself
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                Service cs = new Service();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        //
        /*
        if (userString.indexOf("}") > 0) {
            Service cc = new Service();
            Random r = new Random(6488);
            String ts = "cat" + r.nextInt();
            Storage.getInstance().writeObject(ts, userString);
            List<Service> k = cc.getPropertyIndex().loadJSONList(ts);
            l.addAll(k);
            Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    public static ArrayList<String> getServiceContactIcon(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<String> l = new ArrayList<String>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select logo from servicecontact where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                l.add(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return l;
    }

    public static ArrayList<UserPreference> getUserPreferenceFor(String get) {
        Database db = null;
        Cursor c = null;
        ArrayList<UserPreference> l = new ArrayList<UserPreference>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select preference from user where _id = "
                    + "'" + get + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.sendLogAsync();
        }
        ////////////////log.p(userString);
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                UserPreference cs = new UserPreference();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        //
        /*
        if (userString.indexOf("}") > 0) {
            try {
                UserPreference cc = new UserPreference();
                Random r = new Random(64088);
                String ts = "rqp" + r.nextInt();
                Storage.getInstance().writeObject(ts, userString);
                List<UserPreference> k = cc.getPropertyIndex().loadJSONList(ts);
                l.addAll(k);
                Storage.getInstance().deleteStorageFile(ts);
            } catch (Exception e) {
                ////////////////log.p(e.getMessage());
                e.printStackTrace();
                Log.sendLogAsync();

            } finally {
                //swallow the error
            }
        }
         */
        return l;

    }

    public static ArrayList<PropertyBusinessObject> genericPBOZiemSearch(String url,
            Class type,
            String text, int page, int size, String hint, String filter) {
        ArrayList<PropertyBusinessObject> responseList = new ArrayList<>();
        //////////////////////log.p("Query " + text);
        //{"$orderby": "{" ,"_created": "-1" }}
        // String fetchUrl = "{\"$orderby\":"
        //       + "{" + "\"_created\": " + "-1" + " }}";
        // hint = fetchUrl;
        //
        String query = " Select * from " + url;
        HashMap t = new HashMap();
        if (!text.isEmpty()) {
            //the text query should be delimited with , 
            //name=u,p=z

            //t.putAll(mapFromText(text));
            query = query + " where " + formattedQuery(text) + " ORDER BY _created DESC ";
        } else {
            query = query + " " + "ORDER BY _created DESC ";
        }
        query = query + " LIMIT " + page + "," + size + ";";
        Log.p(query);
        //HashMap mquery = new HashMap();
        //mquery.put("ORDER BY", "_created");
        //mquery.put("LIMIT", size );
        //mquery.put("OFFSET", page );
        //mquery.putAll(t);
        //////////////////////log.p("\nMapped Query\n" + testQuery(mquery));

        try {
            Database db = Database.openOrCreate(dbname);
            Cursor cur = null;
            Row currentRow = null;
            //DAOProvider dbProvider = new DAOProvider(db, 1);
            //DAO<Map> objectDAO = (DAO<Map>) dbProvider.get(url);
            //List<Map> l = objectDAO.fetch(mquery);
            ArrayList<Map> l = new ArrayList<Map>();
            HashMap mRow = new HashMap();
            cur = db.executeQuery(query);
            int columns = cur.getColumnCount();
            boolean next = cur.next();

            while (next) {
                mRow = new HashMap();
                currentRow = cur.getRow();
                String[] currentRowArray = new String[columns];
                for (int iter = 0; iter < columns; iter++) {
                    String columnName = cur.getColumnName(iter);
                    String columnValue = cur.getRow().getString(iter);
                    mRow.put(columnName, columnValue);
                    //                               currentRowArray[iter] = currentRow.getString(iter);
                }

                l.add(mRow);
                next = cur.next();
            }

            cur.close();
            db.close();
            //////////log.p("Generic Search Found " + url + " " + l.size() + " records");

            for (Map m : l) {
                try {
                    showMapp(m);
                    //////////log.p("Type " + type.getCanonicalName());
                    PropertyBusinessObject pb
                            = (PropertyBusinessObject) type.newInstance();
                    //////////log.p("\n\n Creating property object " + pb.getPropertyIndex().toString());
                    pb.getPropertyIndex().populateFromMap(m);
                    //
                    //////////log.p("PropetyBusinessObject \n" + pb.getPropertyIndex().toString() +"\n");
                    responseList.add(pb);
                } catch (Exception err) {
                    //////////log.p("Error " + err.getMessage());
                    // err.printStackTrace();
                }
            }
            // cur.close();

        } catch (Exception ex) {
            Log.p(ex.getMessage());
        }

        return responseList;
    }

    public static ArrayList<Category> getCategoryParent(String post) {
        Database db = null;
        Cursor c = null;
        ArrayList<Category> l = new ArrayList<Category>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select parent from category where _id = "
                    + "'" + post + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////////////////////log.p(userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // we have to process the string ourself
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                Category cs = new Category();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        //
        /*
        if (userString.indexOf("}") > 0) {
            Category cc = new Category();
            Random r = new Random(6488);
            String ts = "category" + r.nextInt();
            Storage.getInstance().writeObject(ts, userString);
            List<Category> k = cc.getPropertyIndex().loadJSONList(ts);
            l.addAll(k);
            Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    public static ArrayList<Provider> searchProviders(String text, int page, int amount) {
        //////////log.p("Search contacts for " + text);
        ArrayList<Provider> results = new ArrayList<Provider>();
        ArrayList<Provider> requests = new ArrayList<Provider>();
        requests = genericZiemSearch("provider",
                Provider.class,
                "", page, amount, "", "");
        //we have to use a different search algorithm since we are always searching local
        //we just filter the records based on whether we have text in the summary
        ////////////log.p(requests.toString());
        List<String> textTerms = StringUtil.tokenize(text, ",");
        for (Provider r : requests) {
            r.refresh();
            String rs = r.summary().toLowerCase();
            //////////log.p(rs);
            for (String t : textTerms) {
                ////////////log.p(t);
                if (rs.lastIndexOf(t.toLowerCase()) > 0) {
                    ////////////log.p("found match for " + t);
                    results.add(r);
                    break;
                }
            }
        }
        return requests;
    }

    public static ArrayList<Category> getCategoryForProvider(String id) {
        Database db = null;
        Cursor c = null;
        ArrayList<Category> l = new ArrayList<Category>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select category from provider where _id = "
                    + "'" + id + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                //////////log.p("Service For Provider " + userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                Category cs = new Category();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        return l;
    }

    public static ArrayList<Service> getServiceForProvider(String id) {
        Database db = null;
        Cursor c = null;
        ArrayList<Service> l = new ArrayList<Service>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select services from provider where _id = "
                    + "'" + id + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                //////////log.p("Service For Provider " + userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                Service cs = new Service();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }
        //
        /*
        if (userString.indexOf("{") >= 0) {
            ////////////log.p("Provider Service " + userString);
            userString = StringUtil.replaceAll(userString, "}{", "},{");
            //userString.replaceAll("}{", "},{");
            //////////log.p("Provider Service " + userString);    
            Service cc = new Service();
            Random r = new Random(6488);
            String ts = "provider" + r.nextInt();
            Storage.getInstance().writeObject(ts, userString);
            List<Service> k = cc.getPropertyIndex().loadJSONList(ts);
            l.addAll(k);
            Storage.getInstance().deleteStorageFile(ts);
        }
         */
        return l;
    }

    public static ArrayList<Service> getServices() {
        //just return all services
        String url = "service";
        int max = 0;
        int page = 0;
        int size = 9999;
        String hint = "";
        String filter = "";
        String query = "";
        //////////////////log.p("Type of Selector: " + p.getClass().getCanonicalName());
        ////////////////////log.p("\nFilter is: " + filter);

        ArrayList<Service> services = genericZiemSearch(url,
                Service.class,
                query, page, size, hint, filter);
        return services;

    }

    public static ArrayList<Service> getRatingService() {
        //SOS service is the service that has been identified as 
        //the service to create when there is an SOS
        String fetchUrl = "{\"name\": \""
                + rateService + "\" }";

        ArrayList<Service> serviceslist = genericZiemSearch("service",
                Service.class,
                "", 0, 9999, "", "");
        ArrayList<Service> services = new ArrayList<Service>();
        for (Service s : serviceslist) {

            if (s.name.get().toLowerCase().indexOf(rateService.toLowerCase()) >= 0) {

                services.add(s);
            }
        }
        return services;
    }

    public static ArrayList<Group> getServiceGroup(String serviceid) {
        Database db = null;
        Cursor c = null;
        ArrayList<Group> l = new ArrayList<Group>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            String query = "select groups from service where _id = "
                    + "'" + serviceid + "'";
                 ////log.p("Group For Service Query " + query);           
            c = db.executeQuery("select groups from service where _id = "
                    + "'" + serviceid + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////log.p("Group For Service " + userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //
        if (userString.indexOf("{") >= 0) {
            //////////log.p("This is the service to be refreshed from \n" + userString + "\n for request " + post);
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                Group cs = new Group();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }

        return l;
    }

    public static ArrayList<Group> getUserGroup(String userid) {
        Database db = null;
        Cursor c = null;
        ArrayList<Group> l = new ArrayList<Group>();
        String userString = "";
        try {
            db = Database.openOrCreate(dbname);
            c = db.executeQuery("select groups from user where _id = "
                    + "'" + userid + "'");

            Boolean next = c.next();

            while (next) {
                userString = c.getRow().getString(0);
                ////log.p("Group For User " + userString);
                next = c.next();
            }
            c.close();
            db.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //
           ////log.p("This is the group to be refreshed from \n" + userString + "\n  " );        
        if (userString.indexOf("{") >= 0) {
            ////log.p("This is the group to be refreshed from \n" + userString + "\n  " );
            List<Map<String, Object>> ps = parseString(userString);
            for (Map<String, Object> m : ps) {
                // ////////log.p("Parsed ==>\n" + m.toString());
                Group cs = new Group();
                cs.getPropertyIndex().populateFromMap(m);
                //////////log.p("From Service \n" + cs.getPropertyIndex().toJSON());
                l.add(cs);
            }
        }

        return l;
    }

    public static ServiceAttribute getServiceAttributeByNameAndService(String service, String name) {
        //get serviceattributes with this name
       ServiceAttribute a = new ServiceAttribute();
        String fetchUrl = "{name:"
                + name + "}";
        ////////////////log.p("Fetching Attribute " + fetchUrl);
        ArrayList<ServiceAttribute> l = genericZiemSearch("serviceattribute",
                ServiceAttribute.class,
                fetchUrl, 0, 1, "", "");
        if (l.size() > 0) {
            return l.get(0);
        } else {
            return a;
        }
 
    }

}
