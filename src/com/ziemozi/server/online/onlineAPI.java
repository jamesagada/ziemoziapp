package com.ziemozi.server.online;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.codename1.components.ToastBar;
import com.codename1.contacts.Contact;
import com.ixzdore.restdb.ziemobject.Comment;
import com.ixzdore.restdb.ziemobject.Notification;
import com.ixzdore.restdb.ziemobject.Post;
import com.ixzdore.restdb.ziemobject.User;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.MultipartRequest;
import com.codename1.io.Preferences;
import com.codename1.io.Util;
import com.codename1.io.rest.RequestBuilder;
import com.codename1.io.rest.Response;
import com.codename1.io.rest.Rest;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.properties.PropertyBusinessObject;
import static com.codename1.ui.CN.*;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
////import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.print.DocFlavor;

public class onlineAPI {

    private static User me;
    public static final String BASE_URL = "https://ziemozi-a3ef.restdb.io/rest/";
    public static final String BASE_URL_STATIC = "https://ziemozi-a3ef.restdb.io/";
    public static String API_KEY = "5a0755c03d5e3147a77ba2486df4ea34e6b59";
    public static String AUTHY = "3IMDN8qW1tTuLdI7h8FMgjaw9YdVVQFe";
    public static final String authyUrl = "https://api.authy.com/protected/json/phones/verification/check";
    private static String token;
    private static final String sosService = "SOS";
    private static final String amFineService = "IAMFINE";
    private static final int aroundMeDistance = 10;//Distance in km to consider as around me

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
            //////////Log.p(me.getPropertyIndex().toString());
            if ((me.authtoken.get() != null) && (me.firstName.get() != null)) {
                loggedIn = true;
            }
        }
        token = Preferences.get("authtoken", null);
        ////////////Log.p("token " + token);
        return loggedIn;
    }

    public static void login(User u, Callback<User> callback) {
        //signupOrLogin("user/login", u, callback);
        //to login, we look for the combination of 
        //password and phone number
        String fetchUrl = "{\"phone\": \""
                + u.phone.get() + "\" ," + "\"password\": \"" + u.password.get() + "\" }";

        //////////Log.p(fetchUrl);
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
        //////////Log.p(u.getPropertyIndex().toString());
        //////////Log.p("URL " + url);
        post(url).
                body(u.getPropertyIndex().toJSON()).
                getAsJsonMap(new Callback<Response<Map>>() {
                    @Override
                    public void onSucess(Response<Map> value) {
                        //////////Log.p("Response Code " + value.getResponseCode());
                        //////////Log.p(value.getResponseData().toString());
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
                        //////////Log.p(me.getPropertyIndex().toString());
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
        //////////Log.p("Refreshing Me " + me.getPropertyIndex().toString());
        Response<Map> map = get("ziemozi-users/" + me._id.get()).getAsJsonMap();
        //////////Log.p("Refreshing user " + map.getResponseData().toString());
        //////////Log.p("Refreshing User " + map.getResponseCode());
        if (map.getResponseCode() == 200) {
            me = new User();
            me.getPropertyIndex().
                    populateFromMap(map.getResponseData());
            //////////Log.p("Refreshed me " + me.getPropertyIndex().toString());
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
        //////////Log.p("Verify Result " + s.getResponseData());
        return "true".equals(s.getResponseData().get("success"));
    }

    public static boolean update(User u) {
        //first check if there is any need to update
        //check if the image file has http, if it does not have
        //then use cloudinary to save the image first
        //before proceeding
        //////////Log.p("Refresh user " + u.getPropertyIndex().toString());
        //////////Log.p("Me is " + me.getPropertyIndex().toString());
        boolean posted = true;
        //////////Log.p("Avatar " + u.avatar.get());
        if (u.avatar.get().indexOf("file:") >= 0) {

            try {
                Map uploadResult = cloudinary.uploader().
                        upload(u.avatar.get(), ObjectUtils.emptyMap());
                String uploadUrl = uploadResult.get("url").toString();
                u.avatar.set(uploadUrl);
                //////////Log.p("Avatar url " + uploadUrl);
            } catch (Exception e) {
                //////////Log.p("Image upload failed \n" + e.getMessage());
                ToastBar.showErrorMessage("Failed to upload profile image");
                posted = false;
            }
        }
        //me.getPropertyIndex().storeJSON("me.json");
        //Map uMap = u.getPropertyIndex().toMapRepresentation();
        //uMap.remove("_id"); // remove id field so the update can happen properly
        //u.getPropertyIndex().populateFromMap(uMap);
        //////////Log.p("Refresjing User Id " + me._id.get());
        //////////Log.p("Refreshing with " + u.getPropertyIndex().toString());
        Response<String> s = patch("ziemozi-users/" + me._id.get()).
                body(u.getPropertyIndex().toJSON()).getAsString();
        ////////////Log.p(u.getPropertyIndex().toJSON());
        //////////Log.p("User Patch Response Code " + s.getResponseCode());
        //////////Log.p("User Patch Response " + s.getResponseData());
        //u.getPropertyIndex().storeJSON("me.json");
        onlineAPI.refreshMe();
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
        if (me == null) {
            me = new User();
            me.getPropertyIndex().loadJSON("me.json");
        } else {
            me.getPropertyIndex().loadJSON("me.json");
        }
        me.authtoken.set(Preferences.get("authtoken", ""));
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
        //////////Log.p("\n\nRequest ResponseCode \n" + response.getResponseCode());
        //////////Log.p("\n\nRequest Response data \n" + response.getResponseData());
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
//            //////////Log.p("\nNews user " + r.ziemozi_user.get(0)._id.get() + " " + r.ziemozi_user.get(0).fullName());
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
        boolean post = false;
        Response<Map> res = post("requests").body(pd.getPropertyIndex().toJSON())
                .getAsJsonMap();
        if ((res.getResponseCode() == 201)
                || (res.getResponseCode() == 200)) {
            post = true;
            Request r = new Request();
            //r.getPropertyIndex().populateFromMap(l);
            //r.getPropertyIndex().loadJSON("postRequestParameter");
            r.getPropertyIndex().populateFromMap(res.getResponseData());
            ////////////Log.p("RequestParameter retrieved " + r.getPropertyIndex().toString());
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
        String url = "service-definition";
        int max = 0;
        int page = 0;
        int size = 100;
        String hint = "";
        String filter = "";
        String query = "";
        //////////Log.p("Type of Selector: " + p.getClass().getCanonicalName());
        //////////Log.p("\nFilter is: " + filter);
        ArrayList<Service> services = genericZiemSearch(url,
                Service.class,
                query, page, size, hint, filter);

        for (Service s : services) {
            if (p.getClass().getCanonicalName().indexOf("Category") >= 0) {
                Category c = (Category) p;
                List<Category> cl = s.category.asList();
                if (cl.size() > 0) {
                    //////////Log.p("Number of categories " + cl.size());
                    ////////////Log.p("\nService Category: \n" + s.category.get(0).getPropertyIndex().toString() +"\n" );
                    for (Category cc : cl) {
                        //////////Log.p("Category " + cc._id.get());
                        //////////Log.p("Service Category " + c._id.get());
                        if (cc._id.get().equalsIgnoreCase(c._id.get())) {
                            //////////Log.p("Matched Category " + c._id.get());
                            serviceList.add(s);
                            //////////Log.p("Services in Category " + serviceList.size());
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
            ////////////Log.p("Parent " + parent.toString());
            ////////////Log.p("Is Categories " + parent.toString().indexOf("Categories"));
            ////////////Log.p("Is Providers " + parent.toString().indexOf("Providers"));
            ////////////Log.p("Is ServiceGroup " + parent.toString().indexOf("ServiceGroup"));
            if (parent.toString().indexOf("Categories") >= 0) {
                //categories
                v = new Vector();
                ArrayList<Category> c = onlineAPI.categories();
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
                ArrayList<Provider> c = onlineAPI.providers();
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
                ArrayList<Service> c = onlineAPI.serviceGroups();
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
                //////////Log.p("Category Map\n ");
                showMap(m);
                Category p = new Category();
                p.getPropertyIndex().populateFromMap(m);
                responseList.add(p);
                ////////////Log.p(p.getPropertyIndex().toString());
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
                ////////////Log.p(p.getPropertyIndex().toString());
            }
            return responseList;
        }
        return null;
    }

    private static ArrayList<Service> processServiceDefintionResponse(Response<Map> response) {
        if (response.getResponseCode() == 200) {
            ////////////Log.p(response.toString());
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
        //////////Log.p("Query " + text);
        //{"$orderby": "{" ,"_created": "-1" }}
        String fetchUrl = "{\"$orderby\":"
                + "{" + "\"_created\": " + "-1" + " }}";
        hint = fetchUrl;
        //////////Log.p("Hint " + hint);
        Response<Map> result = get(url).
                queryParam("q", text).
                queryParam("h", hint).
                queryParam("filter", filter).
                queryParam("skip", "" + page).
                queryParam("max", "" + size).
                queryParam("metafields", "true").
                //queryParam("idtolink", "true").
                //queryParam("flatten", "true").
                queryParam("fetchChildren", "true").
                getAsJsonMap();
        //////////Log.p("\nResponse for " + url + " is " + result.getResponseCode(), Log.DEBUG);
        //////////Log.p("\n Response Data" + result.getResponseData(), Log.DEBUG);

        if (result.getResponseCode() == 200) {
            ArrayList<Map> l = (ArrayList<Map>) result.getResponseData().get("root");
            if (l.size() == 0) {
                return null;
            }
            ArrayList<T> responseList = new ArrayList<>();
            for (Map m : l) {
                try {
                    showMap(m);
                    PropertyBusinessObject pb
                            = (PropertyBusinessObject)type.newInstance();
                    pb.getPropertyIndex().populateFromMap(m);
                    //
                    ////////////Log.p(pb.getPropertyIndex().toString());
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
        // //////////Log.p("Saving Request PArameter ..\n");
        //  HashMap l = (HashMap) post("request_parameter").body(pd.getPropertyIndex().toJSON()).getAsJsonMap().getResponseData();
        // Storage.getInstance().writeObject("postRequestParameter", key);
        //  RequestParameter r = new RequestParameter();
        //  r.getPropertyIndex().populateFromMap(l);
        //  //////////Log.p("RP Save Response \n" + r.getPropertyIndex().toString());
        //  pd._id.set(r._id.get());
        //  return r._id.get() != null;  
//        String key = post("request-parameters").body(pd.getPropertyIndex().toJSON()).
        //              getAsString().getResponseData();
        //////////Log.p("\n Saving Request Parameter " + pd.getPropertyIndex().toJSON(), Log.DEBUG);
        Response<Map> res = post("request-parameters").body(pd.getPropertyIndex()
                .toJSON()).getAsJsonMap();
        //HashMap l = (HashMap) post("request_parameter").body(pd.getPropertyIndex().toJSON()).getAsJsonMap().getResponseData();
        //  Storage.getInstance().writeObject("postRequestParameter", key);

        //    //////////Log.p("\n RequestParameter response data is \n" + key);
        //try {
        //    JSONParser j = new JSONParser();
        //    Reader r = new InputStreamReader(
        //             Storage.getInstance().createInputStream("postRequestParameter"));
        //    Map<String,Object> l = j.parseJSON(r ) ;                  
        // } catch (Exception ex) {
        //     //////////Log.p(ex.getMessage());
        // }
        //Display.getInstance().getResourceAsStream(getClass(), "/anapioficeandfire.json"), "UTF-8")) {
        //Map<String, Object> data = json.parseJSON(r);
        //    Map<String,Object> l = j.parseJSON(new StreamReader )
        //Storage.getInstance().writeObject("postRequest", key);
        RequestParameter r = new RequestParameter();
        //r.getPropertyIndex().populateFromMap(l);
        //r.getPropertyIndex().loadJSON("postRequestParameter");
        r.getPropertyIndex().populateFromMap(res.getResponseData());
        ////////////Log.p("RequestParameter retrieved " + r.getPropertyIndex().toString());
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
        //////////Log.p("Searching For " + text);
        //    https://mydb-fafc.restdb.io/rest/people?q={}&h={"$orderby": {"name": 1, "age": -1}}

        return genericZiemSearch("requests",
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
            //////////Log.p(k.toString() + " -- " + m.get(k).toString());
        }
    }

    public static Service getService(String _id) {
        Service s = new Service();
        Response<Map> map = get("service-definition/" + _id).getAsJsonMap();
        //////////Log.p("Response " + map.getResponseData());
        if (map.getResponseCode() == 200) {
            s.getPropertyIndex().
                    populateFromMap(map.getResponseData());
        }
        return s;
    }

    public static ServiceAttributeType getServiceAttributeType(String _id) {
        //////////Log.p("Get Service Attribute Type " + _id);
        ServiceAttributeType s = new ServiceAttributeType();
        Response<Map> map = get("service-attribute-type/" + _id).getAsJsonMap();
        //////////Log.p("Response " + map.getResponseData());
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
                    //////////Log.p(pb.getPropertyIndex().toString());
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
                r.value.set(value);
                r.summarize(false);
            }

            jsonRequestParameter = jsonRequestParameter + r.getPropertyIndex().toJSON() + ",";
        }
        String summedSummary = rq.requestSummary(summedRp,false);
        rq.summary.set(summedSummary);
        rq.f_summary.set(rq.requestSummary(summedRp,true));
        jsonRequestParameter = jsonRequestParameter.substring(0,
                jsonRequestParameter.lastIndexOf("}") + 1) + "\n]";
        //////////Log.p(jsonRequestParameter);
        Response res = post("request-parameters").body(
                jsonRequestParameter).getAsString();

        if (res.getResponseCode()
                == 201 || res.getResponseCode() == 200) {
            posted = true;
        }
        //////////Log.p("\n bulk parameter save response" + res.getResponseData().toString());

        res = patch("requests/" + rq._id.get()).body(rq).getAsString();
        //////////Log.p(res.getResponseCode() + "\n" + res.getResponseData());
        return posted;
    }

    public static ServiceAttribute getServiceAttribute(String _id) {
        //////////Log.p("Get Service Attributexx " + _id);
        ServiceAttribute s = new ServiceAttribute();
        Response<Map> map = get("service-attributes/" + _id).getAsJsonMap();
        //////////Log.p("Response " + map.getResponseData());
        if (map.getResponseCode() == 200) {
            s.getPropertyIndex().
                    populateFromMap(map.getResponseData());
        }
        //////////Log.p("Attributexx " + s.getPropertyIndex().toString());
        return s;
    }

    public static void updateRequestSummary(Request aThis) {
        //////////Log.p("Updating this request " + aThis._id.get());
        Response res = patch("requests/" + aThis._id.get()).
                body(aThis.getPropertyIndex().toJSON()).getAsString();
        //////////Log.p(res.getResponseData().toString());
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

        //////////Log.p(fetchUrl);

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

        //////////Log.p(fetchUrl);

        ArrayList<Service> services = genericZiemSearch("service-definition",
                Service.class,
                fetchUrl, 0, 10, "", "");
        if (services != null) {
            amFine = services.get(0);
        }
        return amFine;
    }

    public static List<Request> geoSearchRequests(String text, int page, int amount) {
        //we need to implement a geo search request functionality but not yet
        return onlineAPI.searchRequests(text, page, amount);
    }
}
