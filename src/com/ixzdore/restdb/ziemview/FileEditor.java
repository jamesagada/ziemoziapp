/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.capture.Capture;
import com.codename1.components.ImageViewer;
import com.codename1.components.ScaleImageButton;
import com.codename1.components.SpanLabel;
import com.codename1.ext.filechooser.FileChooser;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.LEFT;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import java.io.IOException;

/**
 *
 * @author jamesagada
 */
public class FileEditor extends BaseEditorImpl {

    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public final SpanLabel textLabel = new SpanLabel();
    public final ScaleImageButton imageButton;
    public final Button pickImage;
    public final Button captureImage;
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public final ImageViewer imageViewer;
    public final Container imageControls;
    public final FileEditor selfref;

    public FileEditor() {
        selfref=this;
        Style s = UIManager.getInstance().getComponentStyle("MultiLine1");
        FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PORTRAIT, s);
        editContainer.putClientProperty("editor", this);
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        //this is the button to pick image
        pickImage = new Button();
        pickImage.setIcon(FontImage.createMaterial(
                FontImage.MATERIAL_LIBRARY_BOOKS, s));
        //pickImage.setUIID("Label");
        //this is the buttoon to capture through the camera
        captureImage = new Button();
        captureImage.setIcon(FontImage.createMaterial(
                FontImage.MATERIAL_CAMERA, s));
        //captureImage.setUIID("Label");
        //
        imageButton = new ScaleImageButton();
        imageButton.setIcon(FontImage.createMaterial(
                FontImage.MATERIAL_PHOTO_LIBRARY, s));
        //initialize the viewer
        imageViewer = new ImageViewer();
        //imageViewer.setImage(FontImage.createMaterial(
        //        FontImage.MATERIAL_PHOTO, imageButton.getSelectedStyle()));
        //       imageButton.setAutoSizeMode(true);
        imageControls = new Container();
        GridLayout l = new GridLayout(3);
        //l.setAutoFit(true);
        pickImage.setText("Choose File");
        pickImage.setTextPosition(BOTTOM);
        captureImage.setText("Capture");
        captureImage.setTextPosition(BOTTOM);
        imageControls.setLayout(l);
        imageControls.add(textLabel).add(pickImage);

    }

    @Override
    public Container edit(ServiceAttribute attr) {
        // we are editing an attribute that does not exist
        // we just create the field
        //textLabel.setText(_id);
        //requestParameter.value.set(s);
        //System.out.println("Opening Parameter is " + requestParameter.getPropertyIndex().toJSON());

        if (this.requestParameter.value.get() == null) {
            createRequestParameter();
        } else {
            //requestParameter exists
            //so we now set the value attribute to the value of the component
            if (this.requestParameter.value.get() != null) {
                setImage(this.requestParameter.value.get(), imageViewer);
                textField.setText(this.requestParameter.value.get());
            }
        }

        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        //pickImage.setText(attr.display_label.get());
        textField.setHint(attr.description.get());
        setEditorConstraints();

        //System.out.println("Attribute to edit is " + attr.getPropertyIndex().toJSON());
        // textField.setText(attr.default_value.get().toString());
        //imageButton.getStyle().setAlignment(LEFT);
        ActionListener callback = e->{
            if (e != null && e.getSource() != null) {

                String filePath = (String)e.getSource();
                //if it is an image show it as an image in 
                //in the image viewer if possible
                
                requestParameter.value.set(filePath);
                if (!setImage(filePath,imageViewer)){
                    //most likely not an image.
                    
                }
            }
           
            fieldBroadcast.fieldChanged(selfref);
          };


        pickImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (FileChooser.isAvailable()) {
//                    FileChooser.showOpenDialog(".pdf,application/pdf,.gif,"
//                + "image/gif,.png,image/png,.jpg,image/jpg,.tif,image/tif,.jpeg", callback);
                     FileChooser.showOpenDialog("*/*", callback);                   
            } else {
                Display.getInstance().openGallery(callback, Display.GALLERY_IMAGE);
            }

        }
        });
        //pickImage.setText(textLabel.getText());
        //pickImage.setTextPosition(LEFT);
        //imageButton.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        if (this.requestParameter != null) {
            textField.setText(this.requestParameter.value.get());
        }
        //editContainer.add(textLabel).add(helpButton).add(textField);


        addAnotherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
               // ////////Log.p(editContainer.getParent().getParent().toString());
                Component c= new AttributeEditor(serviceAttribute, true);
                            $(c).addTags("attribute");
                editContainer.getParent().getParent().addComponent(c);
                editContainer.getParent().getParent().revalidate();
                editContainer.getParent().getParent().repaint();
                //////////Log.p(editContainer.getParent().getParent().toString());
            }
        
        });
        //editContainer.add(textLabel).add(helpButton).add(textField);
        //editContainer.add(helpButton).add(textField);
        imageControls.add(helpButton);
        //editContainer.add(helpButton);
        //editContainer.add(helpButton).add(p);
        
        if (attr.required.getBoolean()) {
            //editContainer.add(requiredButton);
            helpButton.setText(helpButton.getText() + "*");
            //    textLabel.setText(textLabel.getText()+"*");
        }
        if (attr.multiplicity.getBoolean()) imageControls.add(addAnotherButton);

        editContainer.add(imageControls).add(imageViewer);
        editContainer.revalidate();
        return editContainer;
    }

    @Override
    public Container view(ServiceAttribute attr) {
        textLabel.setText(attr.display_label.get());
        textView.setText(attr.display_label.get());
        //textView.setLabelForComponent(textLabel);
        editContainer.add(textLabel).add(textField);
        editContainer.revalidate();
        return editContainer;
    }

    @Override
    public void createRequestParameter() {
        //request a parameter
        //System.out.println("Attribute Parameter opening " + requestParameter.getPropertyIndex().toString());
        requestParameter.service_attribute.add(serviceAttribute);
        requestParameter.value.set(serviceAttribute.default_value.get());
        //System.out.println("Attribute Parameter going forward" + requestParameter.getPropertyIndex().toString());
        
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
        textField.setMaxSize(Integer.parseInt(this.serviceAttribute.maximum_size.get()));
        //Text does not have a minimum but we can store the minimum_value
        //in a validation context.
        //System.out.println("Maximum Field Size is " + textField.getMaxSize());
    }

    public boolean setImage(String filePath, ImageViewer iv) {
        boolean set = true;
        try {
            Image i1 = Image.createImage(filePath);
            iv.setVisible(true);
            iv.setImage(i1);
            iv.getParent().revalidate();
        } catch (Exception ex) {
            imageViewer.setVisible(false);
            Log.e(ex);
            //Dialog.show("Error", "Error during image loading: " + ex, "OK", null);
            set = false;
        }
        return set;
    }
}
