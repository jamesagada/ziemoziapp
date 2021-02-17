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
import com.codename1.ui.layouts.BorderLayout;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.list.DefaultListModel;
import com.codename1.ui.list.MultiList;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.spinner.DateSpinner;
import com.codename1.ui.spinner.Picker;
import com.codename1.util.StringUtil;
import com.ixzdore.restdb.ziemobject.Category;
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.ServiceContact;
import com.ixzdore.restdb.ziemobject.User;
import com.ixzdore.restdb.ziemobject.UserPreference;
import com.ixzdore.restdb.ziemobject.Wallet;
import com.ziemozi.server.local.localAPI;
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
public class ObjectListAndAddEditor extends BaseEditorImpl {

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
    HashMap<String, PropertyBusinessObject> objectFactory = new HashMap<String, PropertyBusinessObject>();

    public ObjectListAndAddEditor() {
        editContainer.putClientProperty("editor", this);
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        editContainer.getAllStyles().setBorder(Border.createRidgeBorder(2));
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
        rmv.addActionListener(new ActionListener (){
                @Override
                public void actionPerformed(ActionEvent evt){
                        int i = mlist.getSelectedIndex();
                        mlist.getModel().removeItem(i);
                        mlist.repaint();
                        cnt.revalidate();
                     }
        });
           
        d.setLayout(new BorderLayout());
        d.add(BorderLayout.CENTER, cnt);
        d.add(BorderLayout.SOUTH,rmv);
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
        helpButton.setUIID("Label");
        helpButton.getStyle().setAlignment(LEFT);
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
                // ////////log.p(editContainer.getParent().getParent().toString());                
                Component c = new AttributeEditor(serviceAttribute, true);
                $(c).addTags("attribute");
                c.putClientProperty("attribute", "attribute");
                editContainer.getParent().getParent().addComponent(c);
                editContainer.getParent().getParent().revalidate();
                editContainer.getParent().getParent().repaint();
                //////////log.p(editContainer.getParent().getParent().toString());
            }

        });
        //editContainer.add(textLabel).add(helpButton).add(textField);
        //editContainer.add(helpButton).add(textField);
        headerContainer.add(helpButton);
        //editContainer.add(helpButton);
        //editContainer.add(helpButton).add(p);

        if ((Boolean) attr.required.getBoolean()) {
            //editContainer.add(requiredButton);
            helpButton.setText(helpButton.getText() + "*");
            //    textLabel.setText(textLabel.getText()+"*");
        }
        if ((Boolean) attr.multiplicity.getBoolean()) {
            headerContainer.add(addAnotherButton);
        }

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

    ;  

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
                String objType = serviceAttributeType.surveyjs_type.get();
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
                ////////log.p(FileSystemStorage.getInstance().getAppHomePath()+ fileName);
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
                ////////log.p(FileSystemStorage.getInstance().getAppHomePath()+ fileName);                
                try {
                    //read the contents of the file to options
                    options = Util.readToString(FileSystemStorage.getInstance().
                            openInputStream(FileSystemStorage.getInstance().
                                    getAppHomePath() + "/" + fileName));
                } catch (Exception ex) {
                    options = "";
                }
            }
        }else {
               options ="[";
               PropertyBusinessObject cClass = objectFactory.get(objType);
               Class oClass = cClass.getClass();
            java.util.List<PropertyBusinessObject> objectlist = localAPI.genericSearch(objType, 
                   oClass, "", 0, 100);
            for (PropertyBusinessObject po : objectlist){
                options = options + po.getPropertyIndex().toJSON() + ",";
                
            }
            options = options.substring(options.length()-1) + "]";
        }
        

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
                //////log.p("Object type is " + objType);
                //////log.p("MultiObjectList is " + optionList);
                Object cClass = objectFactory.get(objType);
                if (cClass != null) {
                    try {
                        PropertyBusinessObject pp = (PropertyBusinessObject) ((Class) cClass).newInstance();
                        Storage.getInstance().writeObject("optionList", optionList);
                        java.util.List pbos = pp.getPropertyIndex().loadJSONList("optionList");

                        pba.addAll(pbos);
                    } catch (Exception e) {
                        //////log.p(e.getMessage());
                    }
                }
            }
            //pba now contains all the option list
            //so we now put them into the ListModel

            for (PropertyBusinessObject px : pba) {
                String name = "" + px.getPropertyIndex().get("name").toString();
                String description = "" + px.getPropertyIndex().get("description").toString();
                String value = "" + px.getPropertyIndex().get("value");
                //////log.p("Multi object desc " + description);
                data.add(createListEntry(name, description + " " + value, px));
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

        objectFactory.put("User", new User());
        objectFactory.put("UserPreference", new UserPreference());
        objectFactory.put("Category", new Category());
        objectFactory.put("Request", new Request());
        objectFactory.put("ServiceAttribute", new ServiceAttribute());
        objectFactory.put("ServiceAttributeType", new ServiceAttributeType());
        objectFactory.put("Provider", new Provider());
        objectFactory.put("ServiceContact", new ServiceContact());
        objectFactory.put("Wallet", new Wallet());

    }
}
