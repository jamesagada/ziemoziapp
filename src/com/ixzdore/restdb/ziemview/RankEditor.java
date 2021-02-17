/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.components.SpanLabel;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.LEFT;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.Font;
import com.codename1.ui.FontImage;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Slider;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Dimension;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.validation.RegexConstraint;
import com.codename1.ui.validation.Validator;

/**
 *
 * @author jamesagada
 */
public class RankEditor extends BaseEditorImpl{
    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public final Label textLabel = new Label();
    public final Container headerContainer = new Container();
    public final Container sliderContainer = new Container();
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public     Slider starRank = new Slider();
    public RankEditor(){
         editContainer.putClientProperty("editor", this);  
           editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
         //editContainer.setLayout(new GridLayout(2));
         sliderContainer.setLayout(new FlowLayout());
         sliderContainer.add(FlowLayout.encloseCenter(createStarRankSlider()));
         //editorConstraints.add(TextArea.NUMERIC);
         //Validator val = new Validator();
         //String fieldRegEx = "^[0-9]+$";
         //val.addConstraint(textField, new RegexConstraint(fieldRegEx, "NOT-VALID-NUMBER"));
    }
@Override
    public Container edit(ServiceAttribute attr) {
        // we are editing an attribute that does not exist
        // we just create the field
        //textLabel.setText(_id);
        
        if (this.requestParameter == null ) {
            createRequestParameter(attr.type_of_attribute.get(0));
            textField.setText(attr.default_value.get());
            starRank.setProgress(Integer.parseInt(textField.getText()));
        }else{
            //requestParameter exists
            //so we now set the value attribute to the value of the component
            if (this.requestParameter.value.get() != null ){
                textField.setText(this.requestParameter.value.get());
            starRank.setProgress(Integer.parseInt(textField.getText()));                
            }else {
                ////////log.p(attr.getPropertyIndex().toString());
            textField.setText(attr.default_value.get());     
            starRank.setProgress(Integer.parseInt(textField.getText()));            
            }
        }

        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        textField.setHint(attr.description.get());
        setEditorConstraints();
        starRank.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt) {
                requestParameter.value.set(String.valueOf(starRank.getProgress()));
                //////log.p(" Rank is " + starRank.getProgress());
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
               // ////////log.p(editContainer.getParent().getParent().toString());                
                Component c= new AttributeEditor(serviceAttribute, true);
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
        if ((Boolean) attr.multiplicity.getBoolean()) headerContainer.add(addAnotherButton);

 
        editContainer.add(headerContainer).add(sliderContainer);
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
     };  

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
        //////log.p("Max Size " + max);
        if (max.indexOf(".") >= 0) max = max.substring(0,max.indexOf("."));
        try {
            textField.setMaxSize(Integer.parseInt(max));
        }catch(Exception e){
            textField.setMaxSize(9999);
        }
        //Text does not have a minimum but we can store the minimum_value
        //in a validation context.
        ////System.out.println("Maximum Field Size is " + textField.getMaxSize());
        ////System.out.println("Constraints on the field is " + textField.getConstraint()
        //);
    }
private void initStarRankStyle(Style s, Image star) {
    s.setBackgroundType(Style.BACKGROUND_IMAGE_TILE_BOTH);
    s.setBorder(Border.createEmpty());
    s.setBgImage(star);
    s.setBgTransparency(0);
}

private Slider createStarRankSlider() {

    starRank.setEditable(true);
    starRank.setMinValue(0);
    starRank.setMaxValue(10);
        Style s ;
   // if (Font.isNativeFontSchemeSupported()) {
   // Font fnt = Font.createTrueTypeFont("native:mainLight", "native:mainLight").
    //        derive(Display.getInstance().convertToPixels(5, true), Font.STYLE_PLAIN);
    //  s = new Style(0xffff33, 0, fnt, (byte)0);
  //  }else { 
     s = new Style(textField.getAllStyles());
   // }
    Image fullStar = FontImage.createMaterial(FontImage.MATERIAL_STAR, s).toImage();
    s.setOpacity(100);
    s.setFgColor(0);
    Image emptyStar = FontImage.createMaterial(FontImage.MATERIAL_STAR, s).toImage();
    initStarRankStyle(starRank.getSliderEmptySelectedStyle(), emptyStar);
    initStarRankStyle(starRank.getSliderEmptyUnselectedStyle(), emptyStar);
    initStarRankStyle(starRank.getSliderFullSelectedStyle(), fullStar);
    initStarRankStyle(starRank.getSliderFullUnselectedStyle(), fullStar);
    starRank.setPreferredSize(new Dimension(fullStar.getWidth() * 5, fullStar.getHeight()));
    return starRank;
}    
}
