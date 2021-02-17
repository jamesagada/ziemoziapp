/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.components.SpanLabel;
import com.codename1.io.Util;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.NORTH;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.LEFT;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.ui.layouts.BoxLayout;

/**
 *
 * @author jamesagada
 */
public class BrowserEditor extends BaseEditorImpl{
    public final Container editContainer = new Container();
    public final BrowserComponent browser = new BrowserComponent();
    public final Label textLabel = new Label();
    public final Container headerContainer = new Container();
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    
    public BrowserEditor(){
         editContainer.putClientProperty("editor", this);  
         editContainer.setLayout(new BorderLayout());
    }
@Override
    public Container edit(ServiceAttribute attr) {
        // we are editing an attribute that does not exist
        // we just create the field
        //textLabel.setText(_id);
        
        if (this.requestParameter == null ) {
            createRequestParameter(attr.type_of_attribute.get(0));
        }

        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        browser.setURL(attr.default_value.get());
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

        headerContainer.add(helpButton);
        //editContainer.add(helpButton);
        //editContainer.add(helpButton).add(p);
        
        if ((Boolean) attr.required.getBoolean()) {
            //editContainer.add(requiredButton);
            helpButton.setText(helpButton.getText() + "*");
            //    textLabel.setText(textLabel.getText()+"*");
        } 
        editContainer.add(NORTH,headerContainer);
        editContainer.add(CENTER,browser);
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
    
}
