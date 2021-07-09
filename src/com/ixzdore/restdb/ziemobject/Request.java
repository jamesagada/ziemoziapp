/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;

import com.codename1.components.ToastBar;
import com.codename1.io.Util;
import com.codename1.location.Location;
import com.codename1.properties.BooleanProperty;
import com.codename1.properties.IntProperty;
import com.codename1.properties.ListProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.ui.Dialog;
import com.codename1.util.StringUtil;
import com.ziemozi.server.ServerAPI;
import com.ziemozi.server.local.localAPI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author jamesagada
 * @JsonPropertyOrder({ "_id", "parent", "service", "provider", "priority",
 * "keep_private", "escalations", ziemuser "status", "request_parameters",
 * "_created", "_changed", "_createdby", "_changedby", "_keywords", "_tags",
 * "_version", "_children", "_haschildren" })
 */
public class Request implements PropertyBusinessObject {

    public final Property<String, Request> _id = new Property<>("_id");
    public final Property<String, Request> request_location = new Property<>("request_location");
    public final IntProperty<Request> id = new IntProperty<>("id");
    public final Property<String, Request> _parent_id = new Property<>("_parent_id");
    public final Property<String, Request> _parent_def = new Property<>("_parent_def");
    public final Property<String, Request> _parent_field = new Property<>("_parent_field");
    public final Property<String, Request> summary = new Property<>("summary");
    //public final ListProperty<Request,Request> _children = new ListProperty<>("_children",Request.class);
    public final BooleanProperty<Request> keep_private = new BooleanProperty<>("keep_private");
    public final Property<String, Request> escalations = new Property<>("escalations");
    public final Property<String, Request> priority = new Property<>("priority");
    public final ListProperty<User, Request> ziemozi_user = new ListProperty<>("ziemozi_user", User.class);
    public final ListProperty<User, Request> likes = new ListProperty<>("likes", User.class);
    public final Property<Request, Request> parent
            = new Property<>("parent", Request.class);
    public final ListProperty<Provider, Request> provider = new ListProperty<>("provider",
            Provider.class);
    public final ListProperty<Service, Request> service = new ListProperty<>(
            "service", Service.class);
    public final ListProperty<RequestParameter, Request> request_parameters
            = new ListProperty<>(
                    "request_parameters", RequestParameter.class);
    public final ListProperty<Request, Request> comments
            = new ListProperty<>(
                    "comments", Request.class);
    public final Property<String, Request> f_summary = new Property<>("f_summary");
    public final Property<String, Request> status = new Property<>("status");
    public final Property<String, Request> _created = new Property<>("_created");
    public final Property<String, Request> request_latitude = new Property("request_latitude");
    public final Property<String, Request> request_longitude = new Property("request_longitude");
    public final Property<String, Request> request_address = new Property("request_address");
    public final PropertyIndex idx = new PropertyIndex(this, "Request",
            _id, keep_private, escalations, parent, status, provider,request_location,
            service, summary, request_parameters, _created, likes, comments, ziemozi_user,
            _parent_id, _parent_def, _parent_field, request_address, f_summary,request_longitude, request_latitude);

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }

    public Request() {
        parent.setLabel("Parent");
        keep_private.setLabel("Keep Private");
        escalations.setLabel("Escalations");
        service.setLabel("Service");
        provider.setLabel("Provider");
        status.setLabel("Status");
        request_parameters.setLabel("Request_Parameters");
        _created.setLabel("Created");
        summary.setLabel("Summary");
        comments.setLabel("Comments");
        this.ziemozi_user.add(ServerAPI.me());

    }

    public void save() {
        //we need to convert the whole thing into a proper map
        // Updates are not allowed. No edits. So we just save 
        //the critical thing is to check and see if there are image objects
        //or file attachements that need to be saved separately.
        //so go through, save the image objects  and then
        //save the request objects with the image references embedded.
        //just save and let us see
        this._created.set(new Date().toString());
        this.status.set("Open");
        this.priority.set(getPriority());
        HashMap<String, Object> a = new HashMap<String, Object>();
        //////////Log.p("\nBegin Request As A Map\n");
        //
        localAPI.showMap(this.getPropertyIndex().toMapRepresentation());
        a.putAll(this.getPropertyIndex().toMapRepresentation());
        //if (this.service.get() != null ) a.put("service", this.service.get()._id.get());
        //if (this.provider.get() != null ) a.put("provider", this.provider.get()._id.get());  
        //if (this.parent.get() != null )a.put("parent", this.parent.get()._id.get());

        ArrayList<User> aa = new ArrayList<User>();
        a.put("priority", getPriority());
        a.put("status", getStatus());
        a.put("keep_private", getKeepPrivate());
        aa.add(ServerAPI.me());
        a.put("ziemozi_user", aa);
        a.put("request_parameters", new ArrayList());
        //summarize this parameter and  add it into the field
        //////////Log.p("Adding Summary \n");
        a.put("summary", requestSummary());
        //also get the current location from the ServerAPI
        //and put it into the map
        Location loc = ServerAPI.getCurrentLocation();
        String location = ServerAPI.getCurrentLocationName();
        //////////Log.p("Current Location: " + loc);
        //////////Log.p("Current Latitude: " + loc.getLatitude());
        a.put("request_latitude", loc.getLatitude());
        a.put("request_longitude", loc.getLongitude());
        a.put("request_location",location);
        //////////Log.p("Latitude: " + a.get("request_latitude"));
        //////////Log.p("Summarized as " + a.get("summary"));
        // we save the request first here
        //but before saving we have to determine if this is a child request
        //essentially a comment.
        //this happens if the parent is not null
        //if the parent is not null then we set the metafields 
        //and save it
        //or the fields would have been set?
        //we set it here
        if (this.parent.get() != null) {
            //it has a parent
            //so we set the metafields
            a.put("_parent_id", this.parent.get()._id.get());
            a.put("_parent_def", "requests");
            a.put("_parent_field", "comments");
            a.put("parent", null);
        }

        //////////Log.p("\n\n This is requestmap to save\n");
        localAPI.showMap(a);
        Request r = new Request();
        r.getPropertyIndex().populateFromMap(a);
        //////////Log.p("saving request \n" + r.getPropertyIndex().toString());
        //////////Log.p("\n\n This is request map transformed to save\n");
        localAPI.showMap(r.getPropertyIndex().toMapRepresentation());
        Boolean post = localAPI.postRequest(r);
        String msg = "Could not save the request/report";
        if (post) {
            //we need to save the array in one go
            //question is how do we create the array and how do we get it to be saved
            //saving individually will take too much time.
            //we have to do them one by one until we figure out something
            // we should create a batch and save. Create as an ArrayList
            //we need to sort request-parameters 
            ArrayList<RequestParameter> rps = new ArrayList<RequestParameter>(request_parameters.asList());
            Collections.sort(rps);
            //we have sorted the request parameters
            //now we create sublists such that request_parameters with the 
            //same service_attributes are on the same sublist
            int i = 0;
            ArrayList rpbag = new ArrayList();
            while (i < rps.size()) {
                if (i == 0) {
                    ArrayList<RequestParameter> rq = new ArrayList<RequestParameter>();
                    rq.add(rps.get(i));
                    rpbag.add(rq);

                } else {
                    //if rpBag has an array that contains the same, add it to that array
                    if (!findAndAdd(rps.get(i), rpbag)) {
                        ArrayList<RequestParameter> rq = new ArrayList<RequestParameter>();
                        rq.add(rps.get(i));
                        rpbag.add(rq);
                        //////////Log.p("RpBag Size " + rpbag.size());
                    }

                }
                i++;
            }
            //now we have rpbag holding arrays of rps so we 
            //can now recreate the list of rp with the multiple
            //items values stored as arrays in the value field of the rp
            ArrayList<RequestParameter> summedRp = new ArrayList<RequestParameter>();
            i = 0;
            while (i < rpbag.size()) {
                summedRp.add(sumRequests((ArrayList<RequestParameter>) rpbag.get(i)));
                i++;
            }
            //now we have the request parameters properly
            //summed and we can save. To save
            //we send the entire batch to the server

            for (RequestParameter rp : summedRp) {
                //////////Log.p("Parent Id " + r._id.get());
                rp._parent_id.set(r._id.get());

                //   post = rp.save();
            }
            post = ServerAPI.saveRequestParameters(summedRp, r);
            //then go through the arraylist and see if there are duplications for multiple
            //items. Combine all multiple items to comma separated lists 
            //update the summary

        }

        if (post) {
            msg = "Saved successfully";
        }
        //////////Log.p(msg);
        ToastBar.showInfoMessage(msg);
        //////////Log.p("saved request \n" + r.getPropertyIndex().toJSON());
    }

    public ArrayList<RequestParameter> batchRequestParameters() {
        //we need to save the array in one go
        //question is how do we create the array and how do we get it to be saved
        //saving individually will take too much time.
        //we have to do them one by one until we figure out something
        // we should create a batch and save. Create as an ArrayList
        //we need to sort request-parameters 
        ArrayList<RequestParameter> rps = new ArrayList<RequestParameter>();
        rps.addAll(request_parameters.asList());
        ////////Log.p("Request parameters  " + rps.size());
        Collections.sort(rps);
        //we have sorted the request parameters
        //now we create sublists such that request_parameters with the 
        //same service_attributes are on the same sublist
        int i = 0;
        ArrayList rpbag = new ArrayList();
        while (i < rps.size()) {
            if (i == 0) {
                ArrayList<RequestParameter> rq = new ArrayList<RequestParameter>();
                rq.add(rps.get(i));
                rpbag.add(rq);

            } else {
                //if rpBag has an array that contains the same, add it to that array
                if (!findAndAdd(rps.get(i), rpbag)) {
                    ArrayList<RequestParameter> rq = new ArrayList<RequestParameter>();
                    rq.add(rps.get(i));
                    rpbag.add(rq);
                    //////////Log.p("RpBag Size " + rpbag.size());
                }

            }
            i++;
        }
        //now we have rpbag holding arrays of rps so we 
        //can now recreate the list of rp with the multiple
        //items values stored as arrays in the value field of the rp
        ArrayList<RequestParameter> summedRp = new ArrayList<RequestParameter>();
        i = 0;
        while (i < rpbag.size()) {
            summedRp.add(sumRequests((ArrayList<RequestParameter>) rpbag.get(i)));
            i++;
        }
        //now we have the request parameters properly
        //summed and we can save. To save
        //we send the entire batch to the server

        return summedRp;
    }

    private RequestParameter sumRequests(ArrayList<RequestParameter> a) {
        RequestParameter r = a.get(0);
        //////////Log.p("Summing " + r.service_attribute.getName());
        String rValue = r.value.get();
        int i = 1;
        while (i < a.size()) {
            rValue = rValue + "||" + a.get(i).value.get();
            //r.value.set(r.value.get() +"||" + a.get(i).value.get());
            //////////Log.p("Updated " + rValue);
            i++;
        }
        r.value.set(rValue);
        //////////Log.p("Fully summed " + r.value.get());
        return r;
    }

    private boolean findAndAdd(RequestParameter r, ArrayList rb) {
        boolean found = false;
        //go through the contents of rb which are all lists
        //if the compareTo matches anyone, then found is tru
        //add r to that list
        //////////Log.p("Parameter to match \n" + r.getPropertyIndex().toString());
        //r.refreshServiceAttribute();
        int i = 0;
        while (i < rb.size()) {
            ArrayList f = (ArrayList) rb.get(i);
            int j = 0;
            while (j < f.size()) {
                RequestParameter p = (RequestParameter) f.get(j);
                //p.refreshServiceAttribute();
                //////////Log.p("Parameter testing to match\n" + p.getPropertyIndex().toString());
                if (p.compareTo(r) == 0) {
                    //there is a match
                    //////////Log.p("Matched " + r.service_attribute.get(0).name.get());
                    //////////Log.p("With " + p.service_attribute.get(0).name.get());
                    found = true;
                    f.add(r);
                    break;
                }
                j++;
            }
            i++;
        }
        return found;
    }
    public String title(){
        //return a title
        //will consist of service name + who did ut
        String title = "";
        try {
            title = this.ziemozi_user.get(0).firstName.get() + " "
                    + this.ziemozi_user.get(0).familyName.get() + "'s ";
            title = title + this.service.get(0).name.get();
        }catch(Exception e){
            //for some reason it failed to get a valid title
            title = "Unknown Title";
        }
        return title;
    }
    private String requestSummary() {
        List<RequestParameter>p = batchRequestParameters();
        if (p.size() < 1) return this.summary.get();
        if (p==null) return this.summary.get();
        return requestSummary(p,false);
    }
    public String simpleRequestSummary() {
        if (!this._id.get().contains("local") )
            this.refreshRequestParameters();
        //List<RequestParameter>p = batchRequestParameters();
        List<RequestParameter> p = this.request_parameters.asList();
        if (p.size() < 1) return this.summary.get();
        if (p==null) return this.summary.get();
        //Log.p("Request Parameters " + p.size());
        return requestSimpleSummary(p);
    }
    public String requestSimpleSummary(List<RequestParameter> rList) {
        //if (!this._id.get().contains("local"))
            if (this.service.size() < 1 )this.refreshService();

        String rs = "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n"
                + "<doc>\n" + "<body>";
        //this.refreshService();
        //String l = this.service.get(0).label.get();

        //rs = rs + "<p uiid=\"headline\">  " + l + "  </p>";
        Vector v = new Vector();
        //////////Log.p("\n" + rs+"\n");
        int i = 0;
        for (RequestParameter rp : rList) {
            //Log.p("Summarizing this parameter "
                  //+ rp.getPropertyIndex().toJSON());

            //Log.p("Number of service attributes before refreshing " + rp.service_attribute.size());
            if (!this._id.get().contains("local")) rp.refreshServiceAttribute();
            //Log.p("Number of service attributes after refreshing " + rp.service_attribute.size());
            if (rp.service_attribute.size() > 0) {
                //Log.p(rp.getPropertyIndex().toJSON());
                //Log.p(rp.service_attribute.asExplodedList().toString());
                String ds = rp.service_attribute.get(0).display_sequence.get();
                Boolean insummary = rp.service_attribute.get(0).include_in_summary.get();
                if (insummary) {
                    if (ds.indexOf(".") > 0) {
                        ds = ds.substring(0, ds.indexOf("."));
                    }
                    i = Integer.parseInt(ds);
                    //  i = Util.toIntValue(rp.service_attribute.get(0).display_sequence.get());
                    //i=Integer.parseInt(rp.service_attribute.get(0).display_sequence.get());

                    try {
                        v.add(i, rp.summarize(false));
                    } catch (Exception e) {
                        v.add(rp.summarize(false));
                    }
                }
                //                rs = rs + rp.summarize() + ". <br>";
                //                //////////Log.p(rs);
            }
        }
        i = 0;
        while (i < v.size()) {
            ////////Log.p("Summarizing " + v.get(i));
            rs = rs + v.get(i);
            i++;
        }
        /*
        //add the tags for service, location, provider,category,reporter
        rs = rs + " <p> ReportedFrom -- " + this.request_latitude + "," + this.request_latitude + " </p>";
        rs = rs + " <p> Service -- " + this.service.get(0).name + " </p>";
        rs = rs + " <p> ReportedBy -- " + this.ziemozi_user.get(0).fullName() + ","
                + this.ziemozi_user.get(0).email + "," + this.ziemozi_user.get(0).phone + " </p>";
        rs = rs + " <p> ReportedAt -- " + this._created.get() + " </p>";
        rs = rs + " <p> Reference -- " + this._id.get() + " </p>";

         */
        return rs + " </body></doc>";
    }

    public String requestSummary(List<RequestParameter> rList,Boolean functional) {

        //this.refreshRequestParameters();
        String rs = "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n"
                + "<doc>\n" + "<body>";
        //this.refreshService();
        if (this.service.size() < 1 ) return rs;
        String l = this.service.get(0).label.get();
        
        rs = rs + "<p uiid=\"headline\">  " + l + "  </p>";
        Vector v = new Vector();
        //////////Log.p("\n" + rs+"\n");
        int i = 0;
        for (RequestParameter rp : rList) {
            ////////Log.p("Summarizing this parameter "
             //       + rp.getPropertyIndex().toJSON());
            if (rp.service_attribute.size() > 0) {
                String ds = rp.service_attribute.get(0).display_sequence.get();
                Boolean insummary = rp.service_attribute.get(0).include_in_summary.get();
                if (insummary) {
                    if (ds.indexOf(".") > 0) {
                        ds = ds.substring(0, ds.indexOf("."));
                    }
                    i = Integer.parseInt(ds);
                    //  i = Util.toIntValue(rp.service_attribute.get(0).display_sequence.get());    
                    //i=Integer.parseInt(rp.service_attribute.get(0).display_sequence.get());

                    try {
                        v.add(i, rp.summarize(functional));
                    } catch (Exception e) {
                        v.add(rp.summarize(functional));
                    }
                }
                //                rs = rs + rp.summarize() + ". <br>";
                //                //////////Log.p(rs);
            }
        }
        i = 0;
        while (i < v.size()) {
            ////////Log.p("Summarizing " + v.get(i));
            rs = rs + v.get(i);
            i++;
        }
        //add the tags for service, location, provider,category,reporter
        rs = rs + " <p> ReportedAround -- " + this.request_location + " </p>";
        rs = rs + " <p> ReportedFrom -- " + this.request_latitude + "," + this.request_longitude + " </p>";
        rs = rs + " <p> Service -- " + this.service.get(0).name + " </p>";
        rs = rs + " <p> ReportedBy -- " + this.ziemozi_user.get(0).fullName() + ","
                + this.ziemozi_user.get(0).email + "," + this.ziemozi_user.get(0).phone + " </p>";
        rs = rs + " <p> ReportedAt -- " + this._created.get() + " </p>";
        rs = rs + " <p> Reference -- " + this._id.get() + " </p>";
        return rs + " </body></doc>";
    }

    public Map getAttachments() {
        //Cycle through the requestparameters and return list of 
        //media 
        //map consists of a type field and the url
        
        HashMap h = new HashMap();
        for (RequestParameter rp : request_parameters) {
            if (!(this._id.get().contains("local"))) rp.refreshServiceAttribute();
            String base_type = rp.service_attribute.get(0).getBaseType();
            if ((base_type != null)
                    && ((base_type.equalsIgnoreCase("image")
                    || base_type.equalsIgnoreCase("media")))) {
                h.put(rp.service_attribute.get(0).getBaseType(), rp.value.get());
            }
        }
        return h;
    }

    public List<Request> getComments() {
        refreshComments();
        return comments.asList();
    }

    public void refreshComments() {
        //populate the comments
        //comments.clear();
        //if (this._id.get().contains("local")) return;
        ArrayList<Request> aa = localAPI.getCommentsFor(this._id.get());
        //////////Log.p("Comments For " + this._id.get() + " "+ aa.size() +"\n");
        if (aa != null) {
            comments.clear();
            comments.addAll(localAPI.getCommentsFor(this._id.get()));
        }
    }

    public void refreshUser() {
        //////////Log.p("refreshing user in request" );
        //if (this._id.get().contains("local")) return;
        ArrayList<User> aa = localAPI.getUserFor(this._id.get());
        ////////Log.p(" Number of Users on refreshing is  " + aa.size());
        if (aa != null) //////////Log.p(" Number of Users " + aa.size());
        {
            if (aa != null) {
                ziemozi_user.clear();
                if (aa.size() > 1) {
                    ziemozi_user.add(aa.get(1));
                }else{
                     if (aa.size() > 0) ziemozi_user.add(aa.get(0));                   
                }   
            }
        }
    }

    public void refreshService() {
        //if (this._id.get().contains("local")) return;
        ArrayList<Service> aa = localAPI.getServiceForRequest(this._id.get());
        //service.clear();
        ////////Log.p("Refreshing Service found " + aa.size());
        if (aa != null) {
            service.clear();
            service.addAll(aa);
        } else {
            //service.add(new Service());
            //check if there is any service already
            if (service.size() > 0) {
                if (service.get(0) instanceof Service) {
                    service.get(0).refresh();
                } else {
                    String service_id = service.get(0).toString();
                    service.add(localAPI.getService(service_id));
                }
            }
        }

    }

    public void refreshRequestParameters() {
        //////////Log.p("refreshing services in request" );
        //if (this._id.get().contains("local")) return;
        ArrayList<RequestParameter> aa = localAPI.getRequestParametersForRequest(this._id.get());
        //request_parameters.clear();
        //service.set(new ArrayList<Service>());
        if (aa != null) {
            request_parameters.clear();
            request_parameters.addAll(aa);
        } else {
            //service.add(new Service());
        }
    }

    private String getPriority() {
        String priority = "Low";
        //extract the priority request parameter and assign the value
        for (RequestParameter rq : request_parameters) {
            String an = rq.service_attribute.get(0).name.get();
            if (an.equalsIgnoreCase("priority")) {
                priority = rq.value.get();
            }
        }
        return priority;
    }

    private String getStatus() {
        String status = "Open";
        //extract the priority request parameter and assign the value
        for (RequestParameter rq : request_parameters) {
            String an = rq.service_attribute.get(0).name.get();
            if (an.equalsIgnoreCase("status")) {
                status = rq.value.get();
            }
        }
        return status;
    }

    private String getKeepPrivate() {
        String keep_private = "no";
        //extract the priority request parameter and assign the value
        for (RequestParameter rq : request_parameters) {
            String an = rq.service_attribute.get(0).name.get();
            if (an.equalsIgnoreCase("keep_private")) {
                keep_private = rq.value.get();
            }
        }
        return keep_private;
    }

    public String plain_summary() {
        String plain = "";
        String plain_summary = "";
        //String[] split = Util.split(summary.get(), "p>");
        //String s = requestSummary();
        String s = summary.get();
        //////Log.p("Request Summary for plaining is " + s);
        String[] split = Util.split(s, "p>");        
        for (String term : split) {
            //////Log.p("Term -" + term);
            if ((term.indexOf("doc>") < 1) || (term.indexOf("body") < 1) || (term.indexOf("xml") < 1)) {
                plain = StringUtil.replaceAll(term, "<", "");
                plain = StringUtil.replaceAll(plain, "</", "");
                plain_summary = plain_summary + " " + plain;
                //////Log.p(plain_summary);
            }
        }
        return plain_summary;
    }

    public void createRequest() {
        //create a request
        //the request definition would be attached to the service
        //we create the request and then send out the message
        //then show the request in the searchform
        if (this.service == null) {
            return;
        }
        if (this.service.size() < 1) {
            return;
        }
        //we have a valid service
        //we need to get the service definition if it does not exist
        //and then create request parameters with the default values
        //Service service = localAPI.getService(this.service.get(0)._id.get());
        this.service.get(0).refresh();

        if (service == null) {
            return;
        }
        if (this.request_parameters == null) {
            this.request_parameters.set(new ArrayList<RequestParameter>());
        }
        for (ServiceAttribute s : service.get(0).service_attributes) {
            //create a RequestParmeter for each
            ////////Log.p("Setting up Request Parameters");
            ////////Log.p(s.name.get() + " with id  " + s._id.get() );
            if (s._id.get() != null){
            RequestParameter rq = new RequestParameter();
            rq.service_attribute.add(s);
            rq.value.set(s.default_value.get());
            rq.summary.set(rq.summarize(false));
            this.request_parameters.add(rq);
            }
        }
    }
public void createRequest(String parameter, Object value) {
        //create a request
        //the request definition would be attached to the service
        //we create the request and then send out the message
        //then show the request in the searchform
        if (this.service == null) {
            return;
        }
        if (this.service.size() < 1) {
            return;
        }
        //we have a valid service
        //we need to get the service definition if it does not exist
        //and then create request parameters with the default values
        //Service service = localAPI.getService(this.service.get(0)._id.get());
        this.service.get(0).refresh();

        if (service == null) {
            return;
        }
        if (this.request_parameters == null) {
            this.request_parameters.set(new ArrayList<RequestParameter>());
        }
        for (ServiceAttribute s : service.get(0).service_attributes) {
            //create a RequestParmeter for each
            RequestParameter rq = new RequestParameter();
            rq.service_attribute.add(s);
            rq.value.set(s.default_value.get());
            if (s.name.get().equalsIgnoreCase(parameter)) {  
                    rq.value.set(value.toString());
            }           
            rq.summary.set(rq.summarize(false));
            this.request_parameters.add(rq);
        }
    }
    public void saveLocal() {
        //save this request locally
        //we need to extract the values such that 
        //we can insert it into the database 
        //we will try using the DAO
       // Random r = new Random(63546);
       // this._id.set("local_" + r.nextInt());//we use this to mark new records yet to be shared
        this._id.set("local_" + this.hashCode() + System.currentTimeMillis());//we use this to mark new records yet to be shared
        
        this._created.set(new Date().toString());
        this.status.set(getStatus());
        this.priority.set(getPriority());
        this.keep_private.set(Boolean.parseBoolean(getKeepPrivate()));

        this.ziemozi_user.clear();
        this.ziemozi_user.add(ServerAPI.me());
        Location loc = ServerAPI.getCurrentLocation();
        if (loc != null) {
            this.request_latitude.set("" + loc.getLatitude());
            this.request_longitude.set("" + loc.getLongitude());
        }
        if (this.parent.get() != null) {
            //it has a parent
            //so we set the metafields
            //a.put("_parent_id",this.parent.get()._id.get());
            this._parent_id.set(this.parent.get()._id.get());
            //  a.put("_parent_def","requests");
            this._parent_def.set("requests");
            // a.put("_parent_field","comments");
            this._parent_field.set("comments");
            this.parent.set(null);
            // a.put("parent", null);
        }
        this.service.get(0).refresh();
        this.summary.set(requestSummary());
        ////////////Log.p( " Saved " + localAPI.saveLocal(this));
        if (localAPI.saveLocal(this)) {
            //Ask if we should update to server now?
            Boolean sync = Dialog.show("", "Save To Server ?",
                    Dialog.TYPE_CONFIRMATION, null, "Now", "Later");

            if (sync) {
                ToastBar.showInfoMessage("Syncing ..");
                localAPI.saveRequestsToServer();
                ToastBar.showInfoMessage("Done");
            }
        }
    }
    public void saveLocalNoSync() {
        //save this request locally
        //we need to extract the values such that 
        //we can insert it into the database 
        //we will try using the DAO
        //Random r = new Random(63546);
        //this._id.set("local_" + r.nextInt());//we use this to mark new records yet to be shared
        this._id.set("local_" + this.hashCode() + System.currentTimeMillis());//we use this to mark new records yet to be shared
        
        this._created.set(new Date().toString());
        this.status.set(getStatus());
        this.priority.set(getPriority());
        this.keep_private.set(Boolean.parseBoolean(getKeepPrivate()));

        this.ziemozi_user.clear();
        this.ziemozi_user.add(ServerAPI.me());
        Location loc = ServerAPI.getCurrentLocation();
        if (loc != null) {
            this.request_latitude.set("" + loc.getLatitude());
            this.request_longitude.set("" + loc.getLongitude());
        }
        if (this.parent.get() != null) {
            //it has a parent
            //so we set the metafields
            //a.put("_parent_id",this.parent.get()._id.get());
            this._parent_id.set(this.parent.get()._id.get());
            //  a.put("_parent_def","requests");
            this._parent_def.set("requests");
            // a.put("_parent_field","comments");
            this._parent_field.set("comments");
            this.parent.set(null);
            // a.put("parent", null);
        }
        this.service.get(0).refresh();
        this.summary.set(requestSummary());
        ////////////Log.p( " Saved " + localAPI.saveLocal(this));
        ////Log.p("Saving Locally " + this._id);
        if (localAPI.saveLocal(this)) {
            //Ask if we should update to server now?
                //Log.p("Successfully Saved Locally");
                //ToastBar.showInfoMessage("Saved");

        }else {
            //Log.p("Failed To Save Locally");
            //ToastBar.showInfoMessage("Failed to save local");
        }
    }
    

    public void makeReadyForSaving() {
 //       Random r = new Random();
//        this._id.set("local_" + r.nextLong() + System.currentTimeMillis());//we use this to mark new records yet to be shared
        this._id.set("local_" + this.hashCode() + System.currentTimeMillis());//we use this to mark new records yet to be shared
        this._created.set(new Date().toString());
        this.status.set(getStatus());
        this.priority.set(getPriority());
        this.keep_private.set(Boolean.parseBoolean(getKeepPrivate()));

        this.ziemozi_user.clear();
        this.ziemozi_user.add(ServerAPI.me());
        Location loc = ServerAPI.getCurrentLocation();
        if (loc != null) {
            this.request_latitude.set("" + loc.getLatitude());
            this.request_longitude.set("" + loc.getLongitude());
        }
        if (this.parent.get() != null) {
            if (this.parent.get() != null) {
                //it has a parent
                //so we set the metafields
                //a.put("_parent_id",this.parent.get()._id.get());
                this._parent_id.set(this.parent.get()._id.get());
                //  a.put("_parent_def","requests");
                this._parent_def.set("requests");
                // a.put("_parent_field","comments");
                this._parent_field.set("comments");
                this.parent.set(null);
                // a.put("parent", null);
            }
        }
     //   ArrayList<RequestParameter> rp = new ArrayList<RequestParameter>();
      //  rp.addAll(this.request_parameters.asList());
     //   request_parameters.clear();
     //   request_parameters.addAll(rp);
        //this.service.get(0).refresh();
        ////////Log.p(this.getPropertyIndex().toJSON());
        ////////Log.p("Number of attributes From as list " + this.request_parameters.asList().size());
        ////////Log.p("Number of attributes " + this.request_parameters.size());
        this.summary.set(requestSummary());
    }
    public ArrayList<String> validateRequest(){
        ArrayList<String> errors = new ArrayList<String>();

        for (RequestParameter r:this.request_parameters.asList()) {
            //Log.p("service attributes " + r.service_attribute.size());
            errors.addAll(r.validate());
            //Log.p(errors.size() +
                    //" errors found" + "found for " + r.service_attribute.get(0).name.get());
        }
        if ((this.request_longitude.get() == null) || ( this.request_latitude == null )){
            errors.add("Could not get GPS address of location  fixed");
        }
        return errors;
    }
}
