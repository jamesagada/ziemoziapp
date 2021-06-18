/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import ca.weblite.codename1.json.JSONException;
import ca.weblite.codename1.json.JSONObject;
import com.codename1.bluetoothle.Bluetooth;
import com.codename1.components.MultiButton;
import com.codename1.components.SignatureComponent;
import com.codename1.components.SpanButton;
import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.ext.codescan.ScanResult;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.LEFT;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.PickerComponent;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.spinner.DateSpinner;
import com.codename1.ui.spinner.Picker;
import com.codename1.ui.util.ImageIO;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import com.codename1.ext.codescan.CodeScanner;
import com.codename1.processing.Result;
import com.codename1.ui.plaf.Border;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jamesagada Component to wrap around sensors or devices communicating
 * via BLE. The full LE parameters have to be specified
 */
public class LESensor extends BaseEditorImpl {

    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public final Label textLabel = new Label();
    public final SignatureComponent sig = new SignatureComponent();
    public final SpanButton scanCode = new SpanButton("BLE Scan");
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public final Container headerContainer = new Container();
    private Bluetooth bt;
    private final Container devicesCnt = new Container();
    private Map devices = new HashMap();

    public LESensor() {
        ////////Log.p("LESensor");
        editContainer.putClientProperty("editor", this);
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        devicesCnt.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        scanCode.getAllStyles().setBorder(Border.createBevelRaised());
        scanCode.addActionListener((evt) -> {
            //
            //devices.clear();
            devices = new HashMap();
            bt = new Bluetooth();
            ////////Log.p("Enable Ble");
            try {
                if (!bt.isEnabled()) {
                    bt.enable();
                }
                if (!bt.hasPermission()) {
                    bt.requestPermission();
                }
            } catch (Exception ex) {
                ////////Log.p(ex.getMessage());
            }
            //initialize bluetooth
            ////////Log.p("Initialize");
            try {
                bt.initialize(true, false, "bluetoothleplugin");
            } catch (Exception ex) {
               ////////Log.p(ex.getMessage());
            }
            //

            ////////Log.p("Scan for bluetooth");
            try {

                bt.startScan(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            JSONObject res = (JSONObject) evt.getSource();
                            System.out.println("response " + res);

                            if (res.getString("status").equals("scanResult")) {
                                //if this is a new device add it
                                if (!devices.containsKey(res.getString("address"))) {
                                    devices.put(res.getString("address"), res);
                                    requestParameter.value.set(
                                            Result.fromContent(devices).toString());
                                    updateUI();
                                }
                            }
                        } catch (JSONException ex) {
              ////////Log.p(ex.getMessage());
                        }
                    }
                }, null, true, Bluetooth.SCAN_MODE_LOW_POWER, Bluetooth.MATCH_MODE_STICKY,
                        Bluetooth.MATCH_NUM_MAX_ADVERTISEMENT, Bluetooth.CALLBACK_TYPE_ALL_MATCHES);
            } catch (Exception ex) {
              ////////Log.p(ex.getMessage());
            }
        });
    }

    private void updateUI() throws JSONException {
        devicesCnt.removeAll();
        final TextField detail= new TextField("");
        Set keys = devices.keySet();
        for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
            String address = (String) iterator.next();
            JSONObject obj = (JSONObject) devices.get(address);
            try {
                detail.setText(obj.toString(1));
            }catch(Exception e){
                ////////Log.p(e.getMessage());
            }
            MultiButton mb = new MultiButton(obj.getString("name"));
            mb.setTextLine2(address);
            mb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    Dialog.show( "BLE Device" , detail.getText(), "ok", "");
                  }
                
            });
            devicesCnt.add(mb);
        }
        devicesCnt.revalidate();
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
                textField.setText(this.requestParameter.value.get());

            }
        }

        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        scanCode.setText(attr.display_label.get());
        textField.setHint(attr.description.get());
        helpButton.setUIID("Label");

        //////System.out.println("Attribute to edit is " + attr.getPropertyIndex().toJSON());
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
                Component c = new AttributeEditor(serviceAttribute, true);
                $(c).addTags("attribute");
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
        if (attr.multiplicity.getBoolean()) {
            headerContainer.add(addAnotherButton);
        }
        ////////Log.p("Setting up edit container");
        editContainer.add(headerContainer).add(scanCode).add(devicesCnt);
        ////////Log.p("Refreshing View");
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

}
