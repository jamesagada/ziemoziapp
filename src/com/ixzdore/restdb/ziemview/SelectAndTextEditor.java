/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.components.SpanLabel;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.log;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.LEFT;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.Label;
import com.codename1.ui.PickerComponent;
import static com.codename1.ui.TextArea.NUMERIC;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.spinner.DateSpinner;
import com.codename1.ui.spinner.Picker;
import com.codename1.util.StringUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author jamesagada The singleselectioneditor presents the list of items in
 * the options list of the attribute definition as a selection it is also
 * possible that the option list is defined by a url or that the list is
 * calculated
 */
public class SelectAndTextEditor extends BaseEditorImpl {

    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public final TextField numberField = new TextField();
    public final Container headerContainer = new Container();
    public final Label textLabel = new Label();
    public final Container selectContainer = new Container();
    //public final PickerComponent datePicker = PickerComponent.createDate(new Date()).label("Date");
    public final PickerComponent booleanPicker = PickerComponent.createStrings("");//this is the picker
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public String optionList = ""; //string delimited list
    public String[] optionArray;
        SelectAndTextEditor selfRef;
    Component selfref;    
    public SelectAndTextEditor() {
        editContainer.putClientProperty("editor", this);
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        editContainer.getStyle().setBorder(Border.createBevelRaised());
        //editContainer.setLayout(new GridLayout(3));        
        selectContainer.setLayout(new GridLayout(2));
        headerContainer.setLayout(new GridLayout(3));
        ////System.out.println("SelectAndNumber Editor\n");
        //numberField.setConstraint(NUMERIC);
        //numberField.setUIID("TabZ");
        selfRef = this;
        this.setUIID("TabZ");
        booleanPicker.setUIID("TabZ");
        selfref = this;
    }

    @Override
    public Container edit(ServiceAttribute attr) {
        // we are editing an attribute that does not exist
        // we just create the field

        setUpOptions();
        ////System.out.println("Options List is " + optionList);
        String s = optionArray[0];

        if (this.requestParameter == null) {
            createRequestParameter(attr.type_of_attribute.get(0));            
        } else {
            //requestParameter exists
            //so we now set the value attribute to the value of the component
            if (this.requestParameter.value.get() != null) {
                //typically the value consists of the number field and the selected field separated by comma e.g. "PDP",10
                //so we tokenize and then set the option and the value
                //if the option does not exist we add it and if the the number is not numeric we set to zero
                textField.setText(this.requestParameter.value.get());
                String[] valueArray = Util.split(textField.getText(), ",");
                s = valueArray[0];
                numberField.setText(valueArray[1]);

            }
        }

        Picker stringPicker = booleanPicker.getPicker();
        stringPicker.setType(Display.PICKER_TYPE_STRINGS);
        stringPicker.setStrings(optionArray);

        stringPicker.setSelectedString(s);
        Picker p = stringPicker;
        ////////////Log.p("\n PickerCompnent Visibility " + booleanPicker.getUIID());
        // p.setSelectedString(attr.default_value.get());

        p.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //requestParameter.value.set(booleanPicker.getPicker().getSelectedString());
                requestParameter.value.set(booleanPicker.getPicker().getSelectedString() + "," + numberField.getText());                
                //requestParameter.value.set(textField.getText());
                ////System.out.println("Attribute " + attr.display_label.get() + " is "
                       // + requestParameter.getPropertyIndex().toJSON());
            }
        });
        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        textField.setHint(attr.description.get());
        //helpButton.setUIID("TabZ");

        //setEditorConstraints();
        numberField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //      requestParameter.value.set(datePicker.getPicker().getValue().toString());               
                //requestParameter.value.set(textField.getText());
                requestParameter.value.set(booleanPicker.getPicker().getSelectedString() + "," + numberField.getText());
                ////System.out.println("Attribute " + attr.display_label.get() + " is "
                     //   + requestParameter.getPropertyIndex().toJSON());
            }

        });
        ////System.out.println("Attribute to edit is " + attr.getPropertyIndex().toJSON());
        // textField.setText(attr.default_value.get().toString());
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
               // //////////Log.p(editContainer.getParent().getParent().toString());
                Component c= new AttributeEditor(serviceAttribute, true);
                            $(c).addTags("attribute");
                            c.putClientProperty("attribute", "attribute");
                editContainer.getParent().getParent().addComponent(c);
                editContainer.getParent().getParent().revalidate();
                editContainer.getParent().getParent().repaint();
                ////////////Log.p(editContainer.getParent().getParent().toString());
            }
        
        });
           removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
               // //////////Log.p(editContainer.getParent().getParent().toString());
               selfref.setVisible(false);
               
                //////////Log.p("Remove this component \n"  + selfref);
                //////////Log.p("Container " + editContainer.getParent().getParent());
                //////////Log.p("Parent Container " + editContainer.getParent().getParent().getParent());
                editContainer.getParent().getParent().removeComponent(selfref);
                editContainer.getParent().getParent().revalidate();
                editContainer.getParent().getParent().repaint();
                editContainer.getParent().getParent().getParent().revalidate();                
                ////////////Log.p(editContainer.getParent().getParent().toString());
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
        if (attr.multiplicity.getBoolean()) headerContainer.add(addAnotherButton);
        selectContainer.add(booleanPicker).add(numberField);
        editContainer.add(headerContainer).add(selectContainer);
        editContainer.revalidate();
        editContainer.repaint();
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
        /**
         * //set the constraints //the constraints are standard constraints and
         * are specified in //the editorConstraints array. The constraints if
         * (editorConstraints.size() > 0) { //run through it //for the text
         * editor, the constraints are numerical TextArea constraints //and also
         * constraints for minimum_size and maximum_size for (Object c :
         * editorConstraints) { int c1 = textField.getConstraint(); int c2 =
         * Util.toIntValue(c); textField.setConstraint(c2 | c1); } } //set the
         * maximum size and minimum size //if
         * textField.setMaxSize(this.serviceAttribute.maximum_size.getInt());
         * //Text does not have a minimum but we can store the minimum_value
         * //in a validation context. //////System.out.println("Maximum Field Size
         * is " + textField.getMaxSize());
         */
    }
    @Override
    public void setUpOptions() {
        //setup the options to be selected
        //retrieve the string from the attribute
        //check if it contains a url - if it does, retrieve the url and
        //use it as the list
        //or if not, use it as the list
        String options = this.serviceAttribute.option_list.get();
        ////System.out.println("Options " + options);
        Date today = new Date();
        String todayString = SimpleDateFormat.getDateInstance(3).format(today);
        //check if it is a url
        if (options != null) {
            if (options.startsWith("http") || options.startsWith("Http") || options.startsWith("ftp")) {
                //it is a supported url
                //check if we already downloaded it before.
                //actually we should make sure it is not too old
                String fileName = "options-" + this.serviceAttribute._id.get() +"_"+ 
                        this.serviceAttribute._parent_id
                        ;
                //cheeck if there is any file like this already and if it is not too lod
                //////////Log.p(FileSystemStorage.getInstance().getAppHomePath()+ fileName);
                if (!FileSystemStorage.getInstance()
                        .exists(FileSystemStorage.getInstance().getAppHomePath()+"/" 
                                + fileName)) 
                {
                    Util.downloadUrlToFile(options,
                            FileSystemStorage.getInstance().getAppHomePath() + "/" + fileName
                                    , true);
                }else {
                    //check whether it is stale by trying to get the 
                    //date it was downloaded
                    String fullFilename = FileSystemStorage.getInstance().getAppHomePath()+"/" 
                                + fileName;
                    long lastModified =
                            FileSystemStorage.getInstance().getLastModified(fullFilename);
                    if ((lastModified - today.getTime()) > 86400000) {
                   Util.downloadUrlToFile(options,
                            FileSystemStorage.getInstance().getAppHomePath() + "/" + fileName
                                    , true);                        
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
        optionList = options;
        //convert options to a string array
        optionList = StringUtil.replaceAll(optionList, "\n", "");  
         optionList = StringUtil.replaceAll(optionList, "*", ",");           
        Object[] opa = StringUtil.tokenize(optionList, ",").toArray();
        optionArray = new String[opa.length];
        for (int i = 0; i < opa.length; i++) {
            optionArray[i] = (String) opa[i];
            ////System.out.println("\nOption in OptionList " + opa[i]);
        }
    }
 
}
