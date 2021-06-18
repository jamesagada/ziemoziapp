/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.codename1.components.InfiniteProgress;

import static com.codename1.ui.CN.CENTER_BEHAVIOR_CENTER_ABSOLUTE;
import static com.codename1.ui.CN.getCurrentForm;

import com.codename1.components.SpanButton;
import com.codename1.components.SpanLabel;
import com.codename1.io.Log;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.ComponentSelector;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.URLImage;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.ziemozi.server.ServerAPI;

import static com.codename1.ui.CN.NORTH;
import static com.codename1.ui.CN.getCurrentForm;
import static com.codename1.ui.ComponentSelector.$;
import static com.codename1.ui.FontImage.MATERIAL_ARROW_BACK;

/**
 *
 * @author jamesagada
 */
public class BrowserViewer extends BaseEditorImpl{
    public final Container editContainer = new Container();
    //public final BrowserComponent browser = new BrowserComponent();
    public final Label textLabel = new Label();
    public final Container headerContainer = new Container();
    public final SpanLabel textView = new SpanLabel(); //to be used for view

    public BrowserViewer(){
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
        textLabel.setTextPosition(TOP);
        String logo="";
        if (attr.logo.size() < 1) {
            logo=null;
        }else {
            logo = attr.logo.get(0);
        }
//        Form previous = this.getParent().getComponentForm();
        SpanButton browsebutton = new SpanButton();
        browsebutton.setTextPosition(TOP);
        browsebutton.setText(attr.help_text.get());
        browsebutton.setIcon(makeLogo(logo,logo));
        browsebutton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(final ActionEvent evt) {
                Form browse = new Form("Browser", new BorderLayout());
                BrowserComponent browser = new BrowserComponent();
                browser.setURL(attr.default_value.get());
                browse.add(BorderLayout.CENTER, browser);
                Form previous = browsebutton.getParent().getComponentForm();
                browse.getToolbar().addMaterialCommandToLeftBar("",
                        MATERIAL_ARROW_BACK, e -> previous.showBack());
                browse.show();
            }
        });
        //browser.setURL(attr.default_value.get());
       // Log.p("Target URL "+ attr.default_value.get());
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
        
        if (attr.required.getBoolean()) {
            //editContainer.add(requiredButton);
            helpButton.setText(helpButton.getText() + "*");
            //    textLabel.setText(textLabel.getText()+"*");
        } 
        editContainer.add(NORTH,headerContainer);
        editContainer.add(CENTER_BEHAVIOR_CENTER_ABSOLUTE,browsebutton);
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


    private Image makeLogo(String ref, String logo) {
        Style s = UIManager.getInstance().getComponentStyle("TITLE");
        FontImage p = FontImage.createMaterial(FontImage.MATERIAL_OPEN_IN_BROWSER, s);
        EncodedImage placeholder = EncodedImage.createFromImage(p.scaled(p.getWidth()*8, p.getHeight()*8), false);
        if (logo != null) {
            String url = ServerAPI.mediaUrl(logo);
            Image i = URLImage.createToStorage(placeholder, ref,
                    url);

            return i.scaled(placeholder.getWidth()*2,placeholder.getHeight()*2);
        } else {
            return p;
        }
    }
}



