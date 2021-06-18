/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemobject;

import com.codename1.io.Storage;
import com.codename1.io.Util;
import com.codename1.properties.BooleanProperty;
import com.codename1.properties.ListProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.util.StringUtil;
import com.ziemozi.server.local.localAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



/**
 *
 * @author jamesagada
 * @JsonPropertyOrder({ "_id", "description", "type_of-attribute", --
 * attributetypeobject "multiplicity", "required", "help_text", "display_label",
 * "action_url", "default_value", "display_group", "display_sequence", "name",
 * "minimum_size", "maximum_size", "watch_this_attribute", -- serviceattribute
 * reference "watch_formula", -- json string "child_attributes", -- the child
 * attributes of this attribute "_parent_id", -- its own parent "_parent_def",
 * "_parent_field", "_created", "_changed", "option_list" - list of items if
 * this was going to be displayed as a pick list })
 */
public class ServiceAttribute implements PropertyBusinessObject {

    public final Property<String, ServiceAttribute> _id = new Property<>("_id");
        public final BooleanProperty<ServiceAttribute> include_in_summary = new BooleanProperty<>("include_in_summary");
    //public final Property<ServiceAttributeType,ServiceAttribute>
    //        type_of_attribute = new Property<>("type_of_attribute",ServiceAttributeType.class);
       public final BooleanProperty<ServiceAttribute> visibleif
            = new BooleanProperty<>("visibleif"); 
       public final BooleanProperty<ServiceAttribute> rowvisibleif
            = new BooleanProperty<>("rowvisibleif");
       public final BooleanProperty<ServiceAttribute> enableif
            = new BooleanProperty<>("enableif");  
       public final BooleanProperty<ServiceAttribute> inputtype
            = new BooleanProperty<>("inputtype"); 
       public final BooleanProperty<ServiceAttribute> displaytype
            = new BooleanProperty<>("displaytype");  
              public final BooleanProperty<ServiceAttribute> valuename
            = new BooleanProperty<>("valuename"); 
       public final BooleanProperty<ServiceAttribute> expression
            = new BooleanProperty<>("expression");               
       public final BooleanProperty<ServiceAttribute> columnvisibleif
            = new BooleanProperty<>("columnvisibleif"); 
       public final BooleanProperty<ServiceAttribute> choicesvisibleif
            = new BooleanProperty<>("choicesvisibleif");
       public final BooleanProperty<ServiceAttribute> choicesorder
            = new BooleanProperty<>("choicesorder"); 
       public final BooleanProperty<ServiceAttribute> triggers
            = new BooleanProperty<>("triggers");  
              public final BooleanProperty<ServiceAttribute> validators
            = new BooleanProperty<>("validators"); 
    public final ListProperty<ServiceAttributeType, ServiceAttribute> type_of_attribute
            = new ListProperty<>("type_of_attribute", ServiceAttributeType.class);
    public final ListProperty<String, ServiceAttribute> logo
            = new ListProperty<>("logo");
    public final BooleanProperty<ServiceAttribute> multiplicity = new BooleanProperty<>("multiplicity");
    public final BooleanProperty<ServiceAttribute> required
            = new BooleanProperty<>("required");
    public final Property<String, ServiceAttribute> display_sequence
            = new Property<>("display_sequence");
    public final Property<String, ServiceAttribute>minimum_size
            = new Property<>("minimum_size");
    public final Property<String, ServiceAttribute> maximum_size
            = new Property<>("maximum_size");
    public final Property<String, ServiceAttribute> option_list
            = new Property<>("option_list");
    public final Property<String, ServiceAttribute> _parent_id = new Property<>("_parent_id");
    public final Property<String, ServiceAttribute> child_attributes = new Property<>("child_attributes");
    public final Property<String, ServiceAttribute> name = new Property<>("name");
    public final Property<String, ServiceAttribute> description = new Property<>("description");
    public final Property<String, ServiceAttribute> help_text = new Property<>("help_text");
    public final Property<String, ServiceAttribute> display_label = new Property<>("display_label");
    public final Property<String, ServiceAttribute> action_url = new Property<>("action_url");
    public final Property<String, ServiceAttribute> default_value = new Property<>("default_value");
    public final Property<String, ServiceAttribute> display_group = new Property<>("display_group");
    public final Property<String, ServiceAttribute> summary_template = new Property<>("summary_template");
    public final ListProperty<ServiceAttribute, ServiceAttribute> watch_this_attribute
            = new ListProperty<>("watch_this_attribute", ServiceAttribute.class);
    public final Property<String, ServiceAttribute> watch_formula = new Property<>("watch_formula");
     public final Property<String, ServiceAttribute> surveyjs_type
            = new Property<>("surveyjs_type");   
     public final Property<String, ServiceAttribute> panel
            = new Property<>("panel");   
     
    public final Property<String, ServiceAttribute> title
            = new Property<>("title");    
    public final Property<String, ServiceAttribute> colcount
            = new Property<>("colcount");
    public final Property<String, ServiceAttribute> choicesByUrl
            = new Property<>("choicesByUrl");
     public final Property<String, ServiceAttribute> hasNone
            = new Property<>("hasNone");   
      public final Property<String, ServiceAttribute> label
            = new Property<>("label");   
        public final Property<String, ServiceAttribute> minwidth
            = new Property<>("minwidth"); 
     public final Property<String, ServiceAttribute> maxwidth
            = new Property<>("maxwidth");
    public final Property<String, ServiceAttribute> choicesmin
            = new Property<>("choicesmin"); 
    public final Property<String, ServiceAttribute> choicesmax
            = new Property<>("choicesmax"); 
        public final Property<String, ServiceAttribute> columns
            = new Property<>("columns");
     public final Property<String, ServiceAttribute> rows
            = new Property<>("rows");  
    public final Property<String, ServiceAttribute> cells
            = new Property<>("cells");   
    public final Property<String, ServiceAttribute> celltype
            = new Property<>("celltype"); 
    public final Property<String, ServiceAttribute> confirmdelete
            = new Property<>("confirmdelete"); 
     public final Property<String, ServiceAttribute> addRowText
            = new Property<>("addRowText"); 
     public final Property<String, ServiceAttribute> horizontalscroll
            = new Property<>("horizontalscroll");                          
    public final Property<String, ServiceAttribute> titleLocation
            = new Property<>("titleLocation");  
      public final Property<String, ServiceAttribute> columnlayout
            = new Property<>("columnlayout");
    public final Property<String, ServiceAttribute> items_name
            = new Property<>("items_name");
        public final Property<String, ServiceAttribute> items_title
            = new Property<>("items_title");  
    public final Property<String, ServiceAttribute> minRateDescription
            = new Property<>("minRateDesciption");
        public final Property<String, ServiceAttribute> maxratedescription
            = new Property<>("maxratedescription");   
       public final Property<String, ServiceAttribute> storeDataAsText
            = new Property<>("storeDataAsText"); 
     public final Property<String, ServiceAttribute> showpreview
            = new Property<>("showpreview"); 
         public final Property<String, ServiceAttribute> imagewidth
            = new Property<>("imagewidth");
     public final Property<String, ServiceAttribute> maxsize
            = new Property<>("maxsize");  
     public final Property<String, ServiceAttribute> collapsed_state
            = new Property<>("collapsed_state");   
          public final Property<String, ServiceAttribute> rendermode
            = new Property<>("rendermode");   
     public final Property<String, ServiceAttribute> templatetitle
            = new Property<>("templatetitle");  
     public final Property<String, ServiceAttribute> panelAddText
            = new Property<>("panelAddText"); 
      public final Property<String, ServiceAttribute> panelRemoveText
            = new Property<>("panelRemoveText");                                                              
      public final Property<String, ServiceAttribute> rowcount
            = new Property<>("rowcount");  
                                                                                                                                                                              
        public final Property<String,ServiceAttribute> _created = new Property<>("_created");
    public final PropertyIndex idx = new PropertyIndex(this, "ServiceAttribute",panel,
            _id,
            include_in_summary,
                        name,
            inputtype,expression,displaytype,enableif,triggers,validators,
            choicesorder, choicesvisibleif,columnvisibleif,rowvisibleif,valuename,
            description,
            type_of_attribute,
            title,
            colcount,
            choicesByUrl,
            hasNone,
            label,
            minwidth,
            choicesmin,
            choicesmax,
            columns,
            rows,
            cells,
            celltype,
            confirmdelete,
            addRowText,
            horizontalscroll,
            rowcount,
            titleLocation,
            columnlayout,
            items_name,
            items_title,
            surveyjs_type,
            minRateDescription,
            maxratedescription,
            storeDataAsText,
            imagewidth,
            maxsize,
            collapsed_state,
            rendermode,
            templatetitle,
            panelAddText,
            panelRemoveText,
            multiplicity,
            required,
            help_text,
            display_label,
            action_url,
            default_value,
            display_group,
            display_sequence,
            minimum_size,
            maximum_size,
            watch_this_attribute,
            watch_formula,
            child_attributes,
            _parent_id,
            summary_template,
            option_list,_created, logo);

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }

    public ServiceAttribute() {
        description.setLabel("Description");
        type_of_attribute.setLabel("serviceAttributeType");
        multiplicity.setLabel("Multiplicity");
        required.setLabel("required");
        help_text.setLabel("Help Text");
        display_label.setLabel("Display_label");
        action_url.setLabel("Action Url");
        default_value.setLabel("Default Value");
        display_group.setLabel("DisplayGroup");
        display_sequence.setLabel("Display Sequence");
        name.setLabel("name");
        minimum_size.setLabel("MinimumSize");
        maximum_size.setLabel("Maximum Size");
        watch_this_attribute.setLabel("Watch ");
        watch_formula.setLabel("Watch Formula");
        child_attributes.setLabel("Child Attributes");
        _parent_id.setLabel("parent");
        option_list.setLabel("options");
        logo.setLabel("logo");
        maximum_size.set("80");
        _created.setLabel("_ceated");
        summary_template.set("");
    }

    public void refreshAttribute() {
        
        ServiceAttribute a = localAPI.getServiceAttribute(this._id.get());
        ////////////Log.p("Options List Refreshed is " + this.option_list.get());
        ////////Log.p("Refreshing this attribute \n" + this.getPropertyIndex().toString());
        ////////Log.p("Refreshing with this \n" + a.getPropertyIndex().toString());
        this.getPropertyIndex().populateFromMap(a.getPropertyIndex().toMapRepresentation());
         refreshTypeOfAttribute();
        refreshWatchAttribute();               
        ////////Log.p("Type  " + this.getPropertyIndex().toJSON());
    }
public void refreshTypeOfAttribute(){
        ArrayList<ServiceAttributeType> aa = localAPI.getAttributeTypeFor(this._id.get());
        //////Log.p("Attribute Types For " + this._id.get() + " has  "+ aa.size() +"\n");
        if (aa != null ) {
            type_of_attribute.clear();
            type_of_attribute.addAll(aa);
        }    
}
public void refreshWatchAttribute(){
        ArrayList<ServiceAttribute> ww = localAPI.getWatchAttributeFor(this._id.get());
        //////////////Log.p("Comments For " + this._id.get() + " "+ aa.size() +"\n");
        if ((ww != null )&&(ww.size() > 0)) {
            //////////////Log.p(ww.size());
            ////////////Log.p(this.name.get() + " is Watching " + ww.get(0).name.get());
            watch_this_attribute.clear();
            watch_this_attribute.addAll(ww);
        }else{
            watch_this_attribute.clear();
        }    
}
    String getSummary(String value, final Boolean functional) {
        //what we do is to take the summary_template and
        //merge with the value. If there is no template
        // then we just take the label or the help_text and
        // append the value to it. If it is an image, we put it in <img>
        this.refreshAttribute();
        String template = this.summary_template.get();
        if (template != null ) {
            if ( template.indexOf("NULL") >= 0 ) template = null;
        }
        String pVal = "" + value;
        //////////////Log.p("\n Summary Template  " + template);
        //if ((template == null)||(template.length() < 2)) template = this.help_text.get();
        //if ((template == null)||(template.length() < 2))template = this.description.get();
        //if ((template == null)||(template.length() < 2)) template = this.display_label.get();

        if (!functional) {
            if ((template == null)||(template.length() < 2)) template = this.description.get();
        }else {
            if ((template == null)||(template.length() < 2)) template = this.name.get();
        }
        ////////////Log.p("Formating " + pVal);
        //////////////Log.p("\n Summary Template  " + template);
        if (template !=null) {
            //We need to rewrite this so that
            //the summarization will depend also on the type of attribute
            //which means either wiring it into service_attribute_type or 
            //into a display component and getting the summary from there
            //Service attribute has the survey_js type which identifies which object it 
            //is of, it can then recreate the object and get the summary by calling
            //a summary on the object itself.Or we can do it at the display level and this becomes
            //And we have no introspection so we will have to create a ZiemObjectFactory
            if ((this.surveyjs_type.get() != null ) && !(this.surveyjs_type.get().contains("NULL") )){
                //it means the value is of this type and we need to load it into a list of that type
                ////Log.p("SurveyJS Type " + this.surveyjs_type.get());
                Class<? extends PropertyBusinessObject> po = ZiemObjectFactory.getObjectFor(this.surveyjs_type.get());
                //now go ahead and get a list or a single object
                if (po != null ) {
                    try {
                        //take value of which should be
                        PropertyBusinessObject pb = (PropertyBusinessObject)po.newInstance();
                        if ((pVal.trim().startsWith("[")) && (pVal.indexOf("{") > 0)) {
                            //we have a list of objects
                            //save to storage to be able to load it back
                            Random r = new Random(64000);
                            String ts = r.nextInt() + "us"+r.nextInt();
                            Storage.getInstance().writeObject(ts, pVal);
                            List<PropertyBusinessObject> l = pb.getPropertyIndex().loadJSONList(ts);
                            pVal ="";
                            int i=0;
                            for (PropertyBusinessObject p:l){
                                if ( i > 0 ) {
                                pVal = pVal + "||" +  p.getPropertyIndex().get("name").toString();
                                }else {
                                pVal =   p.getPropertyIndex().get("name").toString();                                   
                                }
                                i++;
                            }
                           
                        }else {
                            //its just one object

                            ////Log.p("Set property object from " + pVal);
                            if(pVal.indexOf("{") > 0)   {                         
                                pb.getPropertyIndex().fromJSON(pVal);
                                //we need to have a field called summary whose getting
                                //
                                pVal = pb.getPropertyIndex().get("summary").toString();
                            }  
                        }
                    } 
                    catch (Exception ex) 
                    {
                    ////////Log.p(ex.getMessage());
                    }
                    
                }
            }
            //we have a template
            // how we do the templating will depend on the attribute type
            //so we the attribute type will say how it is to be treated
            //we have basic type field which is either text or image or media
            //how do we handle when we have multiple items
            //we have to split the values 
            this.refreshTypeOfAttribute();
            //////Log.p("ServiceAttribute to get summary for-->" + this.name.get());
            ////////Log.p(this.getPropertyIndex().toString());
            String base_type = "text";
            if (this.type_of_attribute.size() > 0) {
 
            base_type = this.type_of_attribute.get(0).base_type.get();
            if ((base_type == null) || (base_type.length() < 1)) {
                base_type = "text";
            }
            }
            String[] vals = Util.split(pVal, "||");
            switch (base_type) {
                case "text":
                    // this is for text. We just substitute the $v$ pattern with this value
                    int i=0;
                    while (i < vals.length) {
                        String idx = "$v$"+i;
                    if (template.indexOf(idx) > 0) {
//                        ////////////Log.p("\n Template is " + template + " and value is " + value);
                        template = StringUtil.replaceAll(template, idx, vals[i]);
//                        ////////////Log.p("\n Converted template to " + template);
                    } else {
                        template = template + " \n " + vals[i]  ;
                    }
                    i++;}
                    template = " <p> "+template+" </p> ";
                    break; // optional

                case "image":
                    // We will take the value which is likely to be a url of the image
                    // we will replace the $v$ in the template with <img src = " </img>
                    
                    i=0;
                    template="";
                    while (i < vals.length) {      
                        if (vals[i] != null )  {
                            ////////Log.p("Image " + vals[i]);
                            ////////Log.p("Index of http " + vals[i].indexOf("http"));
                            if (vals[i].indexOf("http") >= 0) {
                                String idx = "$v$"+i;
                                String img = " <img src=" + '"' + vals[i] + '"' + "/> ";                    
                                if (template.indexOf(idx) > 0) {
                                      StringUtil.replaceAll(template, idx, img);
                                 } else {
                                      template = template + " " + img ;
                                 }
                            }
                        }    
                        i++;
                   
                    }
                    if ( template.indexOf("img src") >=0 ) {
                        template = " <carousel> " + template + " </carousel> ";
                    }
                    break; // optional

                case "boolean":
                    // Statements
                    if (template.indexOf("$v$") > 0) {
                        StringUtil.replaceAll(template, "$v$", value);
                    } else {
                        template = " <p> " + template + " is " + value + " </p> ";
                    }
                    break; // optional

                case "media":
                    // this is video etc. 
                    break; // optional

                // You can have any number of case statements.
                default: // Optional
                // treat as text
            }
                    ////////Log.p("\n Base typ is " + base_type);
        }
        ////////Log.p("\n Updated Template " + template);

        return template;
    }
public String getBaseType(){
            this.refreshTypeOfAttribute();
            ServiceAttributeType st = this.type_of_attribute.get(0);
            String btype = st.base_type.get();
            if ((btype == null) || (btype.length() < 1)) {
                btype = "text";
            }
            return btype;
}
public String getSurveyType(){
            String btype = this.type_of_attribute.get(0).surveyjs_type.get();
            if ((btype == null) || (btype.length() < 1)) {
                btype = "text";
            }
            return btype;
}


}
