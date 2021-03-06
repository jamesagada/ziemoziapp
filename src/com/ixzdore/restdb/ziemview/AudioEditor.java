/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import java.io.InputStream;
import java.io.OutputStream;

import com.codename1.capture.Capture;
import com.codename1.components.ImageViewer;
import com.codename1.components.MediaPlayer;
import com.codename1.components.SpanButton;
import com.codename1.components.SpanLabel;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.media.Media;
import com.codename1.media.MediaManager;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Image;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;

import static com.codename1.ui.ComponentSelector.$;

/**
 *
 * @author jamesagada
 */
public class AudioEditor extends BaseEditorImpl {

    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public final SpanLabel textLabel = new SpanLabel();
    public final SpanButton imageButton = new SpanButton();
    public final Button pickImage;
    public final Button captureImage;
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public final ImageViewer imageViewer;
    public MediaPlayer mp = new MediaPlayer();
    public Media mediaP;
    public final String mime_type = "audio/aac";
    public final Container imageControls;
    public final Container headerContainer = new Container();

    public AudioEditor() {

        Style s = UIManager.getInstance().getComponentStyle("MultiLine1");
        FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PORTRAIT, s);
        editContainer.putClientProperty("editor", this);
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
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
        //imageButton = new Button();
        imageButton.setIcon(FontImage.createMaterial(
                FontImage.MATERIAL_PLAY_CIRCLE_OUTLINE, s));
        imageButton.setIconPosition(BorderLayout.CENTER);
        //imageButton.setTextPosition(BOTTOM);
        //imageButton.setText("Play");
        //imageButton.setUIID("Label");
        //initialize the viewer
        imageViewer = new ImageViewer();
        //imageViewer.setImage(FontImage.createMaterial(
        //        FontImage.MATERIAL_PHOTO, imageButton.getSelectedStyle()));
        //       imageButton.setAutoSizeMode(true);
        imageControls = new Container();
        //GridLayout l = new GridLayout(3);
        //l.setAutoFit(true);
        //pickImage.setText("Pick");
        pickImage.setTextPosition(BOTTOM);
        //captureImage.setText("Capture");
        captureImage.setTextPosition(BOTTOM);
        //imageControls.setLayout(l);
        imageControls.add(textLabel).add(pickImage).add(captureImage);
        mp.setAutoplay(true);

    }

    @Override
    public Container edit(ServiceAttribute attr) {
        // we are editing an attribute that does not exist
        // we just create the field
        //textLabel.setText(_id);

        if (this.requestParameter == null) {
            createRequestParameter(attr.type_of_attribute.get(0));
        } else {
            //requestParameter exists
            //so we now set the value attribute to the value of the component
            if (this.requestParameter.value.get() != null) {
                //setVideo(this.requestParameter.value.get(),mp);
                textField.setText(this.requestParameter.value.get());
            }
        }

        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        //pickImage.setText(attr.display_label.get());
        textField.setHint(attr.description.get());
        setEditorConstraints();

        ////System.out.println("Attribute to edit is " + attr.getPropertyIndex().toJSON());
        // textField.setText(attr.default_value.get().toString());
        //imageButton.getStyle().setAlignment(LEFT);
        pickImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().openGallery(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {

                        String s = "";
                        try {
                            s = (String) evt.getSource();

                        } catch (Exception e) {
                            s = "";
                        }
                        ////System.out.println("Selected Video is " + s);
                        //setVideo(s,mp);                        
                        textField.setText(s);
                        if ((s != "") && (s != null)) {
                            imageButton.setText("Play");
                            imageButton.setIcon(FontImage.createMaterial(
                                    FontImage.MATERIAL_MIC, imageButton.getStyle()));
                        } else {
                            imageButton.setIcon(FontImage.createMaterial(
                                    FontImage.MATERIAL_MIC_OFF, imageButton.getStyle()));
                            imageButton.setText("");
                        }
                        imageButton.repaint();
                        imageButton.getParent().revalidate();
                    }
                }, Display.GALLERY_VIDEO_MULTI);

            }
        });

        captureImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String s = "";
                try {
                    //s = Capture.captureVideo();
                    s=Capture.captureAudio();
                    ////System.out.println("Captured Video is " + s);
                    //setVideo(s,mp);
                } catch (Exception e) {
                    s = "";
                }
                if ((s != "") && (s != null)) {
                    imageButton.setText("Play");
                    imageButton.setIcon(FontImage.createMaterial(
                            FontImage.MATERIAL_PLAY_CIRCLE_OUTLINE, imageButton.getStyle()));
                } else {
                    imageButton.setIcon(FontImage.createMaterial(
                            FontImage.MATERIAL_PLAY_DISABLED, imageButton.getStyle()));
                    imageButton.setText("");
                }
                //imageButton.setText(s);
                //imageButton.remove();
                //mp.remove();
                //editContainer.add(imageButton);
                textField.setText(s);
                imageButton.repaint();
                imageButton.getParent().revalidate();

            }
        });
        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (textField.getText().length() > 0) {

                    setVideo(textField.getText(), mp);
 
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
                Component c = new AttributeEditor(serviceAttribute, true);
                $(c).addTags("attribute");
                editContainer.getParent().getParent().addComponent(c);
                editContainer.getParent().getParent().revalidate();
                editContainer.getParent().getParent().repaint();
                //////////Log.p(editContainer.getParent().getParent().toString());
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
        if (attr.multiplicity.getBoolean()) {
            imageControls.add(addAnotherButton);
        }

        if (textLabel.getText() == null) {
            imageButton.setEnabled(false);
        }
        //imageControls.add(addAnotherButton);

        editContainer.add(headerContainer).add(imageControls).add(imageButton);

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
        textField.setMaxSize(Integer.parseInt(this.serviceAttribute.maximum_size.get()));
        //Text does not have a minimum but we can store the minimum_value
        //in a validation context.
        ////System.out.println("Maximum Field Size is " + textField.getMaxSize());
    }

    public void setImage(String filePath, ImageViewer iv) {
        try {
            Image i1 = Image.createImage(filePath);
            iv.setImage(i1);
            iv.getParent().revalidate();
        } catch (Exception ex) {
            Log.e(ex);
            //ToastBar.showInfoMessage("Error during video loading: " );
        }
    }

    private void setVideo(String video, MediaPlayer mp) {
        //////Log.p("Video to play " + video);
        //mp.setDataSource("");
        String hpath = FileSystemStorage.getInstance().getAppHomePath() + "video";
        try {
            OutputStream o = FileSystemStorage.getInstance().openOutputStream(hpath);
            InputStream v = FileSystemStorage.getInstance().openInputStream(video);
            String _mtype =             _mtype = "video/mp4";
            Util.copy(v, o);
            o.close();
            v.close();
            InputStream iv = FileSystemStorage.getInstance().openInputStream(hpath);
            mediaP = MediaManager.createMedia(iv, _mtype);
            mediaP.setNativePlayerMode(true);
            mediaP.prepare();
            mediaP.play();
            //mp = new MediaPlayer(mediaP);
            ////////Log.p("Ready to play");

        } catch (Exception e) {
            e.printStackTrace();
            //ToastBar.showInfoMessage("Error showing video OR no video to show");
        }
    }
    private void setAudio(String video, MediaPlayer mp) {
        //////Log.p("Video to play " + video);
        //mp.setDataSource("");
        String hpath = FileSystemStorage.getInstance().getAppHomePath() + "video";
        try {
            OutputStream o = FileSystemStorage.getInstance().openOutputStream(hpath);
            InputStream v = FileSystemStorage.getInstance().openInputStream(video);
            String _mtype =             _mtype = "audio/aac";
            Util.copy(v, o);
            o.close();
            v.close();
            InputStream iv = FileSystemStorage.getInstance().openInputStream(video);
            mediaP = MediaManager.createMedia(iv, _mtype);
            mediaP.setNativePlayerMode(true);
            mediaP.prepare();
            mediaP.play();
            //mp = new MediaPlayer(mediaP);
            ////////Log.p("Ready to play");

        } catch (Exception e) {
            e.printStackTrace();
            //ToastBar.showInfoMessage("Error showing video OR no video to show");
        }
    }
}
