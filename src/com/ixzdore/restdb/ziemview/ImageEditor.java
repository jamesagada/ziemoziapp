/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.capture.Capture;
import com.codename1.components.ImageViewer;
import com.codename1.components.ScaleImageButton;
import com.codename1.components.ScaleImageLabel;
import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.convertToPixels;
import static com.codename1.ui.CN.createStorageInputStream;
import static com.codename1.ui.CN.deleteStorageFile;
import static com.codename1.ui.CN.existsInStorage;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.LEFT;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.URLImage;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import java.io.IOException;
import java.io.InputStream;
//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 *
 * @author jamesagada
 */
public class ImageEditor extends BaseEditorImpl {

    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public final SpanLabel textLabel = new SpanLabel();
    public final ScaleImageButton imageButton;
    public final Button pickImage;
    public final Button captureImage;
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public final ImageViewer imageViewer;
    public final Container imageControls;
    public final Component selfref;
    public final Container headerContainer = new Container();
    public final Container imageContainer = new Container();
    public final ScaleImageLabel imageLabel = new ScaleImageLabel();

    public ImageEditor() {
        Style s = UIManager.getInstance().getComponentStyle("MultiLine1");
        FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PORTRAIT, s);
        editContainer.putClientProperty("editor", this);
        editContainer.setLayout(new BorderLayout());
        //editContainer.getStyle().setBorder(Border.createBevelRaised());
        //this is the button to pick image

        pickImage = new Button();
        pickImage.setIcon(FontImage.createMaterial(
                FontImage.MATERIAL_PHOTO_LIBRARY, s));
        //pickImage.setUIID("Label");
        //this is the buttoon to capture through the camera
        captureImage = new Button();
        captureImage.setIcon(FontImage.createMaterial(
                FontImage.MATERIAL_CAMERA_ENHANCE, s));
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
        l.setAutoFit(true);
        //pickImage.setText("Pick");
        //pickImage.setTextPosition(BOTTOM);
        //captureImage.setText("Capture");
        //captureImage.setTextPosition(BOTTOM);
        imageControls.setLayout(l);
        GridLayout m = new GridLayout(2);
        m.setAutoFit(true);
        //headerContainer.setLayout(m);
        headerContainer.setLayout(new BoxLayout(BoxLayout.X_AXIS));
        imageControls.add(pickImage).add(captureImage);
        selfref = this;

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
                 setImage(this.requestParameter.value.get(), imageLabel);
                textField.setText(this.requestParameter.value.get());           
        } else {
            //requestParameter exists
            //so we now set the value attribute to the value of the component
            if (this.requestParameter.value.get() != null) {
                setImage(this.requestParameter.value.get(), imageLabel);
                textField.setText(this.requestParameter.value.get());
            }
        }
        //////Log.p("Image Set");
        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        //pickImage.setText(attr.display_label.get());
        textField.setHint(attr.description.get());
        setEditorConstraints();

        //System.out.println("Attribute to edit is " + attr.getPropertyIndex().toJSON());
        // textField.setText(attr.default_value.get().toString());
        //imageButton.getStyle().setAlignment(LEFT);
        pickImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().openGallery(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        String s = (String) evt.getSource();
                        setImage(s, imageLabel);
                        requestParameter.value.set(s);
                        //System.out.println("Parameter is " + requestParameter.getPropertyIndex().toJSON());

                        imageLabel.repaint();
                    }
                }, Display.GALLERY_IMAGE);
            }
        });

        captureImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {            
                String s = Capture.capturePhoto(375,500);
                setImage(s, imageLabel);
                requestParameter.value.set(s);
                //System.out.println("Parameter is " + requestParameter.getPropertyIndex().toJSON());
                imageLabel.repaint();

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
                Component c = new AttributeEditor(serviceAttribute, true);
                $(c).addTags("attribute");
                                            c.putClientProperty("attribute", "attribute");
                editContainer.getParent().getParent().addComponent(c);
                editContainer.getParent().getParent().revalidate();
                editContainer.getParent().getParent().repaint();
                //////////Log.p(editContainer.getParent().getParent().toString());
            }

        });
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // ////////Log.p(editContainer.getParent().getParent().toString());
                editContainer.getParent().getParent().removeComponent(selfref);
                editContainer.getParent().getParent().revalidate();
                editContainer.getParent().getParent().repaint();
                //////////Log.p(editContainer.getParent().getParent().toString());
            }

        });
        //editContainer.add(textLabel).add(helpButton).add(textField);
        //editContainer.add(helpButton).add(textField);
        helpButton.setText(textLabel.getText().trim());
        helpButton.setTextPosition(Label.LEFT);
        headerContainer.add(helpButton);
        //editContainer.add(helpButton);
        //editContainer.add(helpButton).add(p);

        if (attr.required.getBoolean()) {
            //editContainer.add(requiredButton);
            helpButton.setText(helpButton.getText() + "*");
            //    textLabel.setText(textLabel.getText()+"*");
        }
        if (attr.multiplicity.getBoolean()) {
            headerContainer.add(addAnotherButton);
        }
        editContainer.add(BorderLayout.NORTH, headerContainer);
        editContainer.add(BorderLayout.CENTER, imageLabel);
        editContainer.add(BorderLayout.SOUTH, imageControls);
        //editContainer.add(headerContainer).add(imageControls).add(imageViewer);
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
        //textField.setMaxSize(Integer.parseInt(this.serviceAttribute.maximum_size.get()));
        //Text does not have a minimum but we can store the minimum_value
        //in a validation context.
        //System.out.println("Maximum Field Size is " + textField.getMaxSize());
    }

    public void setImage(String filePath, ScaleImageLabel iv) {
        //////Log.p("Checking Image");
        if ((filePath == null) || (filePath.length() < 1)) {
            //ToastBar.showInfoMessage("No Image Selected");
            iv.setIcon(FontImage.createMaterial
                    (FontImage.MATERIAL_IMAGE_NOT_SUPPORTED,iv.getStyle()).toImage());
        } 
        else {
            if (filePath.indexOf("http") >= 0) {
            //get image from cache or from the url
                iv.setIcon(getImage(9f,filePath));
                iv.getParent().revalidate();
            } 
            else {
                if (filePath.indexOf("file:") >= 0) {
                    try {
                    //Image i1 = Image.createImage(filePath);
                 
                        Image i1 = Image.createImage(FileSystemStorage.getInstance().openInputStream(filePath));
                        iv.setIcon(i1);
                        iv.getParent().revalidate();
                    } catch (Exception ex) {
                       // ToastBar.showErrorMessage("Image load failed for " + this.name);
                    }
            }
        }
        }
        //ToastBar.showErrorMessage("Image load failed for " + this.name);
    }

    public Image getImageMask(int size) {
        Image temp = Image.createImage(size, size, 0xff000000);
        Graphics g = temp.getGraphics();
        g.setAntiAliased(true);
        g.setColor(0xffffff);
        g.fillArc(0, 0, size, size, 0, 360);
        return temp;
    }

    public Image getImage(float imageSize, String filename) {
        //String filename = "round-avatar-" + imageSize + "-" + _id.get();
        if (filename.indexOf("file:") >= 0){
            try {
                return Image.createImage(filename);
            } catch (IOException ex) {
                //Logger.getLogger(ImageEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (existsInStorage(filename)) {
            try (InputStream is
                    = createStorageInputStream(filename)) {
                return Image.createImage(is);
            } catch (IOException err) {
                Log.e(err);
                deleteStorageFile(filename);
            }
        }
        int size = convertToPixels(imageSize);
        Image temp = getImageMask(size);
        Object mask = temp.createMask();
        Style s = new Style();
        s.setFgColor(0xc2c2c2);
        s.setBgTransparency(255);
        s.setBgColor(0xe9e9e9);
        FontImage x = FontImage.createMaterial(
                FontImage.MATERIAL_PERSON, s, size);
        Image avatarImg = x.fill(size, size);
        if (avatarImg instanceof FontImage) {
            avatarImg = ((FontImage) avatarImg).toImage();
        }
        avatarImg = avatarImg.applyMask(mask);
        if ((filename != null) && (filename.indexOf("http") >= 0 )) {
            return URLImage.createToStorage(
                    EncodedImage.createFromImage(avatarImg, false),
                    filename,
                    filename,
                    URLImage.createMaskAdapter(temp));
        }       
        return avatarImg;
    }
    }
