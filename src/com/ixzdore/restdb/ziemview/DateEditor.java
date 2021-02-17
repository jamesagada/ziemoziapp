/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.components.SpanLabel;
import com.codename1.io.Util;
import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.LEFT;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.PickerComponent;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.RoundBorder;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.spinner.DateSpinner;
import com.codename1.ui.spinner.Picker;
import java.util.Date;



/**
 *
 * @author jamesagada
 */
public class DateEditor extends BaseEditorImpl {

    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public final Label textLabel = new Label();
    public final PickerComponent datePicker = PickerComponent.createDate(new Date());
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public final Container headerContainer = new Container();

    public DateEditor() {
        editContainer.putClientProperty("editor", this);
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        //editContainer.setLayout(new GridLayout(2));
        editContainer.getAllStyles().setPaddingBottom(5);
 //       editContainer.getAllStyles().setBorder(Border.createEtchedLowered());
        datePicker.getPicker().setDate(new Date());
    }

    @Override
    public Container edit(ServiceAttribute attr) {
        // we are editing an attribute that does not exist
        // we just create the field
        //textLabel.setText(_id);
        Date d = new Date();

        if (this.requestParameter == null) {
            createRequestParameter(attr.type_of_attribute.get(0));
            requestParameter.value.set(d.toString());
        } else {
            //requestParameter exists
            //so we now set the value attribute to the value of the component
            if (this.requestParameter.value.get() != null) {
                textField.setText(this.requestParameter.value.get());
                try {
                    d = new SimpleDateFormat("dd/MM/YYYY").parse(this.requestParameter.value.get());
                } catch (ParseException ex) {
                    try {
                        d = new SimpleDateFormat("dd/MM/YYYY").parse(d.toString());
                    } catch (ParseException ex1) {
                        //Logger.getLogger(DateEditor.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        }
        datePicker.getPicker().setDate(d);
        //datePicker.label(attr.display_label.get());
        Picker p = datePicker.getPicker();
        p.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                requestParameter.value.set(datePicker.getPicker().getValue().toString());               
                //requestParameter.value.set(textField.getText());
                //System.out.println("Attribute " + attr.display_label.get() + " is "
//                        + requestParameter.getPropertyIndex().toJSON());
            }
        });
        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        textField.setHint(attr.description.get());
        helpButton.setUIID("Label");

        setEditorConstraints();
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                requestParameter.value.set(datePicker.getPicker().getValue().toString());               
                //requestParameter.value.set(textField.getText());
                //System.out.println("Attribute " + attr.display_label.get() + " is "
                  //      + requestParameter.getPropertyIndex().toJSON());
            }

        });
        //System.out.println("Attribute to edit is " + attr.getPropertyIndex().toJSON());
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
               // //////log.p(editContainer.getParent().getParent().toString());                
                Component c= new AttributeEditor(serviceAttribute, true);
                            $(c).addTags("attribute");
                                                        c.putClientProperty("attribute", "attribute");
                editContainer.getParent().getParent().addComponent(c);
                editContainer.getParent().getParent().revalidate();
                editContainer.getParent().getParent().repaint();
                ////////log.p(editContainer.getParent().getParent().toString());
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

 
        editContainer.add(headerContainer).add(datePicker);
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
        //textField.setMaxSize(Integer.parseInt(this.serviceAttribute.maximum_size.get()));
        //Text does not have a minimum but we can store the minimum_value
        //in a validation context.
        //System.out.println("Maximum Field Size is " + textField.getMaxSize());
    }
}
