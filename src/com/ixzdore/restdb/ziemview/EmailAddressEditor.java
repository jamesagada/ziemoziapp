/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.components.SpanLabel;
import com.codename1.io.Util;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.validation.RegexConstraint;
import com.codename1.ui.validation.RegexConstraint.*;
import com.codename1.ui.validation.Validator;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;

import static com.codename1.ui.ComponentSelector.$;

/**
 *
 * @author jamesagada
 */
public class EmailAddressEditor extends BaseEditorImpl{
    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public final Label textLabel = new Label();
    public final Container headerContainer = new Container();
    public final SpanLabel textView = new SpanLabel(); //to be used for view

    public EmailAddressEditor(){
         editContainer.putClientProperty("editor", this);
         editContainer.getAllStyles().setPaddingBottom(10);
        editContainer.getAllStyles().setBgColor(0x0f);
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        editorConstraints.add(TextArea.EMAILADDR);
        //editorConstraints.add(RegexConstraint.validEmail());
        textField.setConstraint(TextArea.EMAILADDR);
        Validator val = new Validator();
        val.addConstraint(textField, RegexConstraint.validEmail());
        val.setShowErrorMessageForFocusedComponent(true);
        headerContainer.setLayout(new BoxLayout(BoxLayout.X_AXIS));
          //        editContainer.setLayout(new GridLayout(2));
        textField.getAllStyles().setBgTransparency(255);
        textField.addActionListener(new ActionListener() {
            @Override public void actionPerformed(final ActionEvent evt) {
                if (!val.isValid()){
                    textField.getAllStyles().setBgColor(0xFF0000);
                }else{
                    textField.getAllStyles().setBgColor(0xFFFFFF);
                }
            }
        });
    }
@Override
    public Container edit(ServiceAttribute attr) {
        // we are editing an attribute that does not exist
        // we just create the field
        //textLabel.setText(_id);
        
        if (this.requestParameter == null ) {
            createRequestParameter(attr.type_of_attribute.get(0));
            textField.setText(attr.default_value.get());
        }else{
            //requestParameter exists
            //so we now set the value attribute to the value of the component
            if (this.requestParameter.value.get() != null ){
                textField.setText(this.requestParameter.value.get());
            }else {
                //////////Log.p(attr.getPropertyIndex().toString());
            textField.setText(attr.default_value.get());                
            }
        }

        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        textField.setHint(attr.description.get());
        setEditorConstraints();

        textField.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt) {
                requestParameter.value.set(textField.getText());
                ////System.out.println("Attribute "+attr.display_label.get() + " is " + 
                       // requestParameter.getPropertyIndex().toJSON());
              }
            
        });
        ////System.out.println("Attribute to edit is " + attr.getPropertyIndex().toJSON());
       // textField.setText(attr.default_value.get().toString());
        textField.repaint();
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

 
        editContainer.add(headerContainer).add(textField);
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
        if (req.service_attribute.get(0) == this.serviceAttribute){
        textLabel.setText(this.serviceAttribute.display_label.get());
        textView.setText(req.value.get());
        editContainer.add(textLabel).add(textView);  
        editContainer.revalidate();
        }
        return editContainer;
    }

    @Override
    public Container edit(RequestParameter req) {
        if (req.service_attribute.get(0) == this.serviceAttribute){
            edit(req.service_attribute.get(0));
            textField.setText(req.value.get());
        }
        editContainer.revalidate();
        return editContainer;
    }
    public void assignTo(RequestParameter requestParameter){
        this.requestParameter = requestParameter;
    }
    public RequestParameter getFrom(){
        return this.requestParameter;
    }
    @Override
    public void setEditorConstraints(){
        //set the constraints
        //the constraints are standard constraints and are specified in
        //the editorConstraints array. The constraints 
        if (editorConstraints.size() > 0 ) {
            //run through it
            //for the text editor, the constraints are numerical TextArea constraints
            //and also constraints for minimum_size and maximum_size
            for (Object c:editorConstraints){
                    int c1 = textField.getConstraint();
                    int c2 = Util.toIntValue(c);
                    ////System.out.println(" Original Constraints is " + c1);
                    if (c1 != 0 ) { 
                        textField.setConstraint(c2|c1);
                    }else {
                        textField.setConstraint(c2);                       
                    }
            }
        }
        //set the maximum size and minimum size
        String max = this.serviceAttribute.maximum_size.get();
        //Log.p("Max Size " + max);
        if (max.indexOf(".") >= 0) max = max.substring(0,max.indexOf("."));
        try {
            textField.setMaxSize(Integer.parseInt(max));
        }catch(Exception e){
            textField.setMaxSize(9999999);
        }
        //Text does not have a minimum but we can store the minimum_value
        //in a validation context.
        ////System.out.println("Maximum Field Size is " + textField.getMaxSize());
        ////System.out.println("Constraints on the field is " + textField.getConstraint()
        //);
    }
}
