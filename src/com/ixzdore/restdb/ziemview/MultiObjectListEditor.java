/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.components.CheckBoxList;
import com.codename1.components.SpanLabel;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Storage;
import com.codename1.io.Util;
import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.log;
import com.codename1.ui.CheckBox;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.LEFT;
import com.codename1.ui.ComponentSelector;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.List;
import com.codename1.ui.PickerComponent;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.events.DataChangedListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.RoundBorder;
import com.ixzdore.restdb.ziemobject.Fare;
import com.ixzdore.restdb.ziemobject.NbsCategory;
import com.ixzdore.restdb.ziemobject.NbsCity;
import com.ixzdore.restdb.ziemobject.NbsCountry;
import com.ixzdore.restdb.ziemobject.NbsDataSet;
import com.ixzdore.restdb.ziemobject.NbsDetail;
import com.ixzdore.restdb.ziemobject.NbsDivision;
import com.ixzdore.restdb.ziemobject.NbsItem;
import com.ixzdore.restdb.ziemobject.NbsLGA;
import com.ixzdore.restdb.ziemobject.NbsLocation;
import com.ixzdore.restdb.ziemobject.NbsMinistry;
import com.ixzdore.restdb.ziemobject.NbsPeriod;
import com.ixzdore.restdb.ziemobject.NbsPeriodtype;
import com.ixzdore.restdb.ziemobject.NbsState;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.Route;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.list.DefaultListModel;
import com.codename1.ui.list.ListModel;
import com.codename1.ui.list.MultiList;
import com.codename1.ui.list.MultipleSelectionListModel;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.spinner.DateSpinner;
import com.codename1.ui.spinner.Picker;
import com.codename1.util.StringUtil;
import com.ixzdore.restdb.ziemobject.Category;
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceContact;
import com.ixzdore.restdb.ziemobject.Stop;
import com.ixzdore.restdb.ziemobject.User;
import com.ixzdore.restdb.ziemobject.UserPreference;
import com.ixzdore.restdb.ziemobject.Group;
import com.ixzdore.restdb.ziemobject.Wallet;
import com.ziemozi.server.local.localAPI;

//import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jamesagada The singleselectioneditor presents the list of items in
 * the options list of the attribute definition as a selection it is also
 * possible that the option list is defined by a url or that the list is
 * calculated
 */
public class MultiObjectListEditor extends BaseEditorImpl {

    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public final Label textLabel = new Label();
    //public final PickerComponent datePicker = PickerComponent.createDate(new Date()).label("Date");
    public final PickerComponent booleanPicker = PickerComponent.createStrings("");
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public String optionList = ""; //string delimited list
    public String[] optionArray;
    public final Container multiSelect = new Container();
    public final ComponentSelector cs = new ComponentSelector();
    public final Container headerContainer = new Container();
    public final List mlist = new MultiList(new DefaultListModel());
    HashMap<String, Class<? extends PropertyBusinessObject>> objectFactory = 
                new HashMap<String, Class<? extends PropertyBusinessObject>>();
    public final ArrayList<String> moptions = new ArrayList<String>();

    public MultiObjectListEditor() {
        editContainer.putClientProperty("editor", this);
        editContainer.getAllStyles().setPaddingBottom(10);
        editContainer.getAllStyles().setMarginBottom(10);
        mlist.getUnselectedStyle().setBgColor(0xf0f0);
        //headerContainer.setLayout(new GridLayout(1,4));
        headerContainer.setLayout(new BoxLayout(BoxLayout.X_AXIS));
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        editContainer.setScrollableY(false);

        //editContainer.getAllStyles().setBorder(Border.createRidgeBorder(2));
        multiSelect.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        initializeObjectFactory();
        mlist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //if selected, we open a new
                HashMap m = (HashMap) mlist.getModel().getItemAt(mlist.getSelectedIndex());
                //inside the map is a propertybusinessobject which we now pass to the propertybusinessobjectui
                PropertyBusinessObject pb = (PropertyBusinessObject) m.get("businessobject");
                PropertyBusinessObjectUI po = new PropertyBusinessObjectUI();
                Container cnt = po.createEditUI(pb, false);
                cnt.revalidate();//make this a pop up so we can close to come back to the parent edit.
                Dialog d = new Dialog("Edit");
                //add a button to remove the object from the list
                Button rmv = new Button("Remove");
                rmv.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        int i = mlist.getSelectedIndex();
                        mlist.getModel().removeItem(i);
                        mlist.repaint();
                        cnt.revalidate();
                    }
                });

                d.setLayout(new BorderLayout());
                d.add(BorderLayout.CENTER, cnt);
                d.add(BorderLayout.SOUTH, rmv);
                d.showPopupDialog(mlist);
                //Form edit = new Form("Edit", new BorderLayout());
                //edit.add(BorderLayout.CENTER, cnt);
                //edit.revalidate();
                //edit.show();
            }
        });
        //  datePicker.getPicker().setDate(new Date());
        //booleanPicker.getPicker().setSelectedString("Yes");
        ////System.out.println("Multi Select Editor\n");
    }

    @Override
    public Container edit(ServiceAttribute attr) {
        // we are editing an attribute that does not exist
        // we just create the field

        setUpOptions();

        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        textField.setHint(attr.description.get());

        //helpButton.setUIID("SmallLabel");

        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Dialog.show(attr.display_label.get(),
                        attr.help_text.get(), "ok", "");
            }
        });
        helpButton.setText(textLabel.getText());
        helpButton.setTextPosition(LEFT);

        addAnotherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // //////////Log.p(editContainer.getParent().getParent().toString());
                // Here we addnother object to the list
                addOptionsFromObjectList();
            }

        });
        //editContainer.add(textLabel).add(helpButton).add(textField);
        //editContainer.add(helpButton).add(textField);
        headerContainer.add(helpButton);
        //editContainer.add(helpButton);
        //editContainer.add(helpButton).add(p);

        if (attr.required.getBoolean()) {
            //editContainer.add(requiredButton);
            helpButton.setText(helpButton.getText() + "*");
            //    textLabel.setText(textLabel.getText()+"*");
        }
        //if ((Boolean) attr.multiplicity.getBoolean()) {
            headerContainer.add(addAnotherButton);
        //}

        editContainer.add(headerContainer).add(mlist);
        editContainer.revalidate();
        return editContainer;
    }

    @Override
    public Container view(ServiceAttribute attr) {
        textLabel.setText(attr.display_label.get());
        textView.setText(attr.display_label.get());
        textView.setLabelForComponent(textLabel);
        editContainer.add(textLabel).add(textField);
        editContainer.revalidate();
        return editContainer;
    }

    @Override
    public void createRequestParameter(ServiceAttributeType serviceType) {
        //request a parameter
        requestParameter.service_attribute.add(serviceAttribute);
        requestParameter.value.set(serviceAttribute.default_value.get());
    }

    @Override
    public Container view(RequestParameter req) {
        //create a view from the requestparameter
        this.requestParameter = req;
        textLabel.remove();
        textView.remove();
        editContainer.removeAll();
        if (req.service_attribute.get(0) == this.serviceAttribute) {
            textLabel.setText(this.serviceAttribute.display_label.get());
            textView.setText(req.value.get());
            editContainer.add(textLabel).add(textView);
            editContainer.revalidate();
        }
        return editContainer;
    }

    @Override
    public Container edit(RequestParameter req) {
        if (req.service_attribute.get(0) == this.serviceAttribute) {
            edit(req.service_attribute.get(0));
            textField.setText(req.value.get());
        }
        editContainer.revalidate();
        return editContainer;
    }

    public void assignTo(RequestParameter requestParameter) {
        this.requestParameter = requestParameter;
    }

    public RequestParameter getFrom() {
        return this.requestParameter;
    }

    @Override
    public void setEditorConstraints() {
        //set the constraints
        //the constraints are standard constraints and are specified in
        //the editorConstraints array. The constraints 
        if (editorConstraints.size() > 0) {
            //run through it
            //for the text editor, the constraints are numerical TextArea constraints
            //and also constraints for minimum_size and maximum_size
            for (Object c : editorConstraints) {
                int c1 = textField.getConstraint();
                int c2 = Util.toIntValue(c);
                textField.setConstraint(c2 | c1);
            }
        }
        //set the maximum size and minimum size
        textField.setMaxSize(Util.toIntValue(this.serviceAttribute.maximum_size.get()));
        //Text does not have a minimum but we can store the minimum_value
        //in a validation context.
        //System.out.println("Maximum Field Size is " + textField.getMaxSize());
    }

    @Override
    public void setUpOptions() {
        //setup the options to be selected
        //retrieve the string from the attribute
        //check if it contains a url - if it does, retrieve the url and
        //use it as the list
        //or if not, use it as the list
        //the options are expected to be such that we can create the
        //businessobjects
        String options = this.serviceAttribute.option_list.get();
        //System.out.println("Options " + options);
        Date today = new Date();
        String todayString = SimpleDateFormat.getDateInstance(3).format(today);
        //check if it is a url
 
        if (options != null) {
            if (options.startsWith("http") || options.startsWith("Http") || options.startsWith("ftp")) {
                //it is a supported url
                //check if we already downloaded it before.
                //actually we should make sure it is not too old
                String fileName = "options-" + this.serviceAttribute._id.get() + "_"
                        + this.serviceAttribute._parent_id;
                //cheeck if there is any file like this already and if it is not too lod
                //////////Log.p(FileSystemStorage.getInstance().getAppHomePath()+ fileName);
                if (!FileSystemStorage.getInstance()
                        .exists(FileSystemStorage.getInstance().getAppHomePath() + "/"
                                + fileName)) {
                    Util.downloadUrlToFile(options,
                            FileSystemStorage.getInstance().getAppHomePath() + "/" + fileName,
                            true);
                } else {
                    //check whether it is stale by trying to get the 
                    //date it was downloaded
                    String fullFilename = FileSystemStorage.getInstance().getAppHomePath() + "/"
                            + fileName;
                    long lastModified
                            = FileSystemStorage.getInstance().getLastModified(fullFilename);
                    if ((lastModified - today.getTime()) > 86400000) {
                        Util.downloadUrlToFile(options,
                                FileSystemStorage.getInstance().getAppHomePath() + "/" + fileName,
                                true);
                    }
                }
                //////////Log.p(FileSystemStorage.getInstance().getAppHomePath()+ fileName);
                try {
                    //read the contents of the file to options
                    options = Util.readToString(FileSystemStorage.getInstance().
                            openInputStream(FileSystemStorage.getInstance().
                                    getAppHomePath() + "/" + fileName));
                } catch (Exception ex) {
                    options = "";
                }
            }
        }
        String objType = serviceAttributeType.surveyjs_type.get();
        if (objType.equalsIgnoreCase("NULL")) objType = serviceAttribute.surveyjs_type.get();        
        if (objType == null) objType = serviceAttribute.surveyjs_type.get();
        ArrayList<PropertyBusinessObject> pba = new ArrayList<PropertyBusinessObject>();
        ArrayList<Map<String, Object>> data = new ArrayList<>();
        optionList = options;
        if (options.lastIndexOf("}") > 1) {
            //options has to be a json array of the particular business object
            //so the type of object has to be specified somewhere
            //or given a name we look it up cos we can only deal with what we know
            //for now we keep the type in the surveyjs_type field of the serviceAttribute
            //serviceAttributeType.surveyjs_type.get()

            if (objType != null) {
                //try and lookup the obj class with a factory 
                ////////Log.p("Object type is " + objType);
                ////////Log.p("MultiObjectList is " + optionList);
                Object cClass = objectFactory.get(objType);
                if (cClass != null) {
                    try {
                        PropertyBusinessObject pp = (PropertyBusinessObject) ((Class) cClass).newInstance();
                        Storage.getInstance().writeObject("optionList", optionList);
                        java.util.List pbos = pp.getPropertyIndex().loadJSONList("optionList");

                        pba.addAll(pbos);
                    } catch (Exception e) {
                        ////////Log.p(e.getMessage());
                    }
                }
            }
            //pba now contains all the option list
            //so we now put them into the ListModel

            for (PropertyBusinessObject px : pba) {
                //String name = "" + px.getPropertyIndex().get("name").toString();
                //String description = "" + px.getPropertyIndex().get("description").toString();
                //String value = "" + px.getPropertyIndex().get("value");
                //////////Log.p("Multi object desc " + description);
            String name ="";
            String first_name ="";
            String last_name ="";
            String phone_number = "";
            String description ="";
            String summary ="";
            String value = "";
            String _id = "" + px.getPropertyIndex().get("_id").toString();
            if (px.getPropertyIndex().get("name") != null)
                name = "" + px.getPropertyIndex().get("name").toString();
            if (px.getPropertyIndex().get("first_name") != null)
                    first_name = "" + px.getPropertyIndex().get("name").toString();
            if (px.getPropertyIndex().get("description") != null)            
                description = "" + px.getPropertyIndex().get("description").toString();
            if (px.getPropertyIndex().get("summary") != null)            
                summary = "" + px.getPropertyIndex().get("summary").toString();  
             if (px.getPropertyIndex().get("value") != null)             
                value = "" + px.getPropertyIndex().get("value");
            if (value.length() <2 ) {
                value = first_name + " " + last_name;
             }
            if (name.length() > 4) {
                description = name;
            }
            if (name.length() < 2){
                name = first_name + " " + last_name;
            }
            if (name == "NULL") name=description;
            //description = description + ".." + summary ;
                //data.add(createListEntry(name, description + " " + value, px));
                data.add(createListEntry(name, " " + value, px));                
            }
        }
        DefaultListModel<Map<String, Object>> model = new DefaultListModel<>(data);
        mlist.setModel(model);
    }

    private Map<String, Object> createListEntry(String name, String value, PropertyBusinessObject p) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("Line1", name);
        entry.put("Line2", value);
        entry.put("icon", null);
        entry.put("businessobject", p);
        return entry;
    }

    private void initializeObjectFactory() {
        //load the editor factory

        objectFactory.put("User", User.class);
        objectFactory.put("UserPreference", UserPreference.class);
        objectFactory.put("Category", Category.class);
        objectFactory.put("Request", Request.class);
        objectFactory.put("ServiceAttribute", ServiceAttribute.class);
        objectFactory.put("ServiceAttributeType", ServiceAttributeType.class);
        objectFactory.put("Provider", Provider.class);
        objectFactory.put("ServiceContact", ServiceContact.class);
        objectFactory.put("Service", Service.class);
        objectFactory.put("Group",Group.class);
        objectFactory.put("Wallet",Wallet.class);
        objectFactory.put("Route", Route.class);
        objectFactory.put("Fare", Fare.class);
        objectFactory.put("Stop", Stop.class);
        // nbs specific
        objectFactory.put("NbsDataSet", NbsDataSet.class);
        objectFactory.put("NbsDataset", NbsDataSet.class);
        objectFactory.put("NbsDivision", NbsDivision.class);
        objectFactory.put("NbsCategory", NbsCategory.class);
        objectFactory.put("NbsMinistry", NbsMinistry.class);
        objectFactory.put("NbsItem", NbsItem.class);
        objectFactory.put("NbsDetail", NbsDetail.class);
        objectFactory.put("NbsLocation", NbsLocation.class);
        objectFactory.put("NbsPeriod", NbsPeriod.class);
        objectFactory.put("NbsPeriodtype", NbsPeriodtype.class);
        objectFactory.put("NbsState", NbsState.class);
        objectFactory.put("NbsLGA", NbsLGA.class);
        objectFactory.put("NbsCity", NbsCity.class);
        objectFactory.put("NbsCountry", NbsCountry.class);


    }

    private void addOptionsFromObjectList() {
        //display a dialog with a list of the 
        //objects that can be picked
        CheckBoxList ml = new CheckBoxList(new DefaultListModel());
        ml.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        String objType = serviceAttributeType.surveyjs_type.get();
        ////////Log.p("Object type " + objType);
        if (objType.isEmpty()) objType = serviceAttribute.surveyjs_type.get();
        //Log.p("Object type " + objType);
        Class<? extends PropertyBusinessObject> cClass =  objectFactory.get(objType);
        
        //(String url,
        //    Class<PropertyBusinessObject> type,
        //    String text, int page, int size, String hint, String filter)                
        ArrayList<PropertyBusinessObject> ol = localAPI.genericPBOZiemSearch(objType,
                cClass,
                "", 0, 9999, "", "");
        //Log.p(objType + "  PBOs found " + ol.size());
        ArrayList<Map<String, Object>> data = new ArrayList<>();
        ArrayList<Map<String, Object>> selectedData = new ArrayList<>();
        for (PropertyBusinessObject px : ol) {
            //if the object does not have a name property, try the summary
            //or just use the _id
            String name ="";
            String description ="";
            String summary ="";
            String _id = "" + px.getPropertyIndex().get("_id").toString();
            if (px.getPropertyIndex().get("name") != null)
                name = "" + px.getPropertyIndex().get("name").toString();
            if (px.getPropertyIndex().get("first_name") != null)
                name = name + " " + px.getPropertyIndex().get("first_name").toString();
            if (px.getPropertyIndex().get("lastt_name") != null)
                name = name + " " + px.getPropertyIndex().get("last_name").toString();  
            if (px.getPropertyIndex().get("phone_number") != null)
                name = name + " " + px.getPropertyIndex().get("phone_number").toString();            
            if (px.getPropertyIndex().get("description") != null)            
                description = "" + px.getPropertyIndex().get("description").toString();
            if (px.getPropertyIndex().get("summary") != null)            
                description = "" + px.getPropertyIndex().get("summary").toString(); 
             if (px.getPropertyIndex().get("plain_summary") != null)            
                description = "" + px.getPropertyIndex().get("plain_summary").toString();            
            //String value = "" + px.getPropertyIndex().get("value");
            if ((name == null) || (name == "NULL") || (name.contains("NULL"))) name = description;
            description =  name + ".." + description + ".." + summary;
            ////////
            
            data.add(createListEntry(name, description, px));
            if (name.length() > 4 ) description = name;
            //this is used to track the options even when search may change the order
            //Log.p("Multi object desc " + description);
            String rDesc = ml.getMultiListModel().getSize()+"."+description;
            //ml.getMultiListModel().addItem( description);
            //moptions.add(description);
                        ml.getMultiListModel().addItem( rDesc);
            moptions.add(rDesc);
        }
        //DefaultListModel<Map<String, Object>> model = new DefaultListModel<>(data);
        //ml.setModel(model);
        ml.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //if selected add to value
                String s = "";
                int[] selected = ml.getMultiListModel().getSelectedIndices();
                for (int i : selected) {
                    //mlist.addItem(data.get(i));
                    //extract the actual index
                   String sIndex = ml.getMultiListModel().getItemAt(i).toString();
                    int dotIndex = sIndex.indexOf(".");
                    String str = sIndex.substring(0, dotIndex);
                    
                    selectedData.add(data.get(Integer.valueOf(str))); 
                    //selectedData.add(data.get(i));
                    
                }
                //lets add to mList provided
                //They dont duplicate
                ////////Log.p("selected " + selectedData.size());
                int i=0;
                while( i < mlist.size()) {
                    ////////Log.p("mlist item " + mlist.getModel().getItemAt(i).toString());
                    selectedData.remove(mlist.getModel().getItemAt(i));
                 i++;   
                }
                ////////Log.p("updated selected " + selectedData.size());
                i=0;
                while (i<selectedData.size()){
                    mlist.addItem(selectedData.get(i));
                    i++;
                }
             selectedData.clear();
             StringBuffer sb = new StringBuffer();
             sb.append("[");
             for (int k=0;k<mlist.size();k++){
                 HashMap entry = (HashMap)mlist.getModel().getItemAt(k);
                 PropertyBusinessObject p = (PropertyBusinessObject)entry.get("businessobject");
                 ////////Log.p("Bussiness Object " + p.getPropertyIndex().toString());
                 ////////Log.p(p.getPropertyIndex().toJSON());
                 if (p != null)  sb.append(p.getPropertyIndex().toJSON()+",");
             }
             String ss = sb.toString();
             requestParameter.value.set(ss.substring(0, ss.lastIndexOf(",")) + "]");
             ////////Log.p("Request Parameter Value " + requestParameter.value.get());
            }
           //update the requestparameter value
           //the data is in the form of an array of jjson
        });
        //Take selected options and add the list of options
        //ensure no duplicates and thats that
        Container c = new Container();
        c.setLayout(new BorderLayout());
        c.add(BorderLayout.CENTER,ml);
        ml.setScrollableY(true);
        //c.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        //c.setScrollableY(true);
 
        
        TextField tf = new TextField();
        tf.setHint("Search");
        tf.addActionListener(new ActionListener() {
            @Override public void actionPerformed(final ActionEvent evt) {
                {
                    //if it is changed we need to go through the ml list and
                    //mark them as visible or invisible
                    ArrayList<String> searchResult = new ArrayList<String>();
                    MultipleSelectionListModel mm = ml.getMultiListModel();

                    while (mm.getSize() > 0) {
                        mm.removeItem(0);
                    }
                    for (int i = 0; i < moptions.size(); i++) {
                        //String ls = moptions.get(i);
                        ml.getMultiListModel().addItem(moptions.get(i));
                        ml.getMultiListModel().removeSelectedIndices(i);
                    }
                    mm = ml.getMultiListModel();

                    for (int i = 0; i < mm.getSize(); i++) {
                        ml.getMultiListModel().removeSelectedIndices(i);
                        ////////Log.p("Match " + tf.getText() + "::" + mm.getItemAt(i).toString());
                        if (mm.getItemAt(i).toString().contains(tf.getText())) {
                            searchResult.add(mm.getItemAt(i).toString());
                        }
                    }
                    while (mm.getSize() > 0) {
                        mm.removeItem(0);
                    }
                    for (int i = 0; i < searchResult.size(); i++) {
                        //String ls = moptions.get(i);
                        ml.getMultiListModel().addItem(searchResult.get(i));
                    }

                    ml.revalidateWithAnimationSafety();
                    ml.refresh();
                    ml.repaint();
                }
            }
        });
        tf.addDataChangedListener(new DataChangedListener(){
            @Override
            public void dataChanged(int type, int index) {
                //if it is changed we need to go through the ml list and 
                //mark them as visible or invisible 
                ArrayList<String> searchResult = new ArrayList<String>();
                 MultipleSelectionListModel mm = ml.getMultiListModel();
             
               while (mm.getSize() > 0){
                   mm.removeItem(0);
               }
                for (int i=0; i<moptions.size();i++){
                    //String ls = moptions.get(i);
                    ml.getMultiListModel().addItem(moptions.get(i));
                    ml.getMultiListModel().removeSelectedIndices(i);
                }
               mm = ml.getMultiListModel();

               for (int i= 0; i < mm.getSize(); i++){
                   ml.getMultiListModel().removeSelectedIndices(i);                
                   ////////Log.p("Match " + tf.getText() + "::" + mm.getItemAt(i).toString());
                   if (mm.getItemAt(i).toString().contains(tf.getText())){
                       searchResult.add(mm.getItemAt(i).toString());
                   }
               }
               while (mm.getSize() > 0){
                   mm.removeItem(0);
               }
                for (int i=0; i<searchResult.size();i++){
                    //String ls = moptions.get(i);
                    ml.getMultiListModel().addItem(searchResult.get(i));
                }

               ml.revalidateWithAnimationSafety();
               ml.refresh();
               ml.repaint();
              }
        });
        Dialog d = new Dialog("Select");
   
           
        d.setLayout(new BorderLayout());
        d.add(BorderLayout.NORTH,tf);
        d.add(BorderLayout.CENTER, c);
        d.showPopupDialog(mlist);                
                //Form edit = new Form("Edit", new BorderLayout());
                //edit.add(BorderLayout.CENTER, cnt);
                //edit.revalidate();
                //edit.show();

     

    }
}
