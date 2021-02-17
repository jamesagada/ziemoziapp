/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

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
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.sensors.SensorListener;
import com.codename1.sensors.SensorsManager;
import com.codename1.ui.Display;
import com.codename1.ui.TextArea;
import com.codename1.ui.list.MultiList;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.util.UITimer;
import com.ziemozi.server.ServerAPI;
import com.ziemozi.server.local.localAPI;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author jamesagada Component to wrap around Sensors scanner It will read the
 * various sensors and also read the bluetoothLE components Scanning starts when
 * you click on scan. Values are repeated and put in a multibutton list with
 * name and value and timestamp. If marked as repeat, it will repeat. The value
 * is a json array with value for each sensor
 */
public class SensorScanner extends BaseEditorImpl {

    public final Container editContainer = new Container();
    public final TextArea textField = new TextField(); //to be used for editing
    public final Label textLabel = new Label();
    public final SignatureComponent sig = new SignatureComponent();
    public final SpanButton scanCode = new SpanButton("Scan");
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public final Container headerContainer = new Container();
 public final SensorsManager gyroscope = SensorsManager.getSensorsManager(SensorsManager.TYPE_GYROSCOPE);
public final SensorsManager accelerometer = SensorsManager.getSensorsManager(SensorsManager.TYPE_ACCELEROMETER);
public final SensorsManager magnetic = SensorsManager.getSensorsManager(SensorsManager.TYPE_MAGNETIC);
public final Container sensorDataList = new Container();

    public SensorScanner() {
        
        textField.setRows(5);
        textField.setColumns(20);
        textField.setGrowByContent(true);
        textField.setEditable(false);
        editContainer.putClientProperty("editor", this);
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        sensorDataList.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        sensorDataList.setScrollableY(true);
        sensorDataList.setScrollVisible(true);
        if (magnetic == null) textField.setText(textField.getText()+ "No Magnetic Sensor ");
        if (gyroscope == null) textField.setText(textField.getText()+ "No Gyroscope Sensor ");    
        if (accelerometer == null) textField.setText(textField.getText()+ "No Accelerometer Sensor ");        
        if (magnetic != null) {
            textField.setText(textField.getText()+" magnetic sensor" );
            magnetic.registerListener(new SensorListener() {
                public void onSensorChanged(long timeStamp, float x, float y, float z) {
                    //do your stuff here...
                    String sensorData = "{" + '"' + "timestamp" + '"' + ":" + '"' + Long.toString(timeStamp) + '"';
                    sensorData = sensorData + "," + '"' + "x-axis" + '"' + ":" + '"' + Float.toString(x) + '"';
                    sensorData = sensorData + "," + '"' + "y-axis" + '"' + ":" + '"' + Float.toString(y) + '"';
                    sensorData = sensorData + "," + '"' + "z-axis" + '"' + ":" + '"' + Float.toString(z) + '"';
                    sensorData = "{" + '"' + "magnetic" + '"' + ":" + sensorData + "},";
                    sensorData = sensorData + senseLocation() + "}";
                    if (textField.getText().contains("{")) {
                        textField.setText(textField.getText() + "," + sensorData);
                    } else {
                        textField.setText(sensorData);
                    }
                    requestParameter.value.set(textField.getText());
                    MultiButton sensorButton = new MultiButton();
                    sensorButton.setTextLine1("Magnetic");
                    sensorButton.setTextLine2(sensorData);
                    sensorButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            Dialog.show(sensorButton.getTextLine1(),
                                    sensorButton.getTextLine2(), "ok", "");
                        }
                    });
                    sensorDataList.add(sensorButton);
                    editContainer.revalidate();
                    ////////log.p(textField.getText());                    
                }
            });
        }

        if (accelerometer != null) {
            textField.setText(textField.getText()+" accelerometer sensor" );
            accelerometer.registerListener(new SensorListener() {
                public void onSensorChanged(long timeStamp, float x, float y, float z) {
                    //do your stuff here...
                    String sensorData = "{" + '"' + "timestamp" + '"' + ":" + '"' + Long.toString(timeStamp) + '"';
                    sensorData = sensorData + "," + '"' + "x-axis" + '"' + ":" + '"' + Float.toString(x) + '"';
                    sensorData = sensorData + "," + '"' + "y-axis" + '"' + ":" + '"' + Float.toString(y) + '"';
                    sensorData = sensorData + "," + '"' + "z-axis" + '"' + ":" + '"' + Float.toString(z) + '"';
                    sensorData = "{" + '"' + "accelerometer" + '"' + ":" + sensorData + "},";
                    sensorData = sensorData + senseLocation() + "}";
                    if (textField.getText().contains("{")) {
                        textField.setText(textField.getText() + "," + sensorData);
                    } else {
                        textField.setText(sensorData);
                    }
                    requestParameter.value.set(textField.getText());
                    MultiButton sensorButton = new MultiButton();
                    sensorButton.setTextLine1("Accelerometer");
                    sensorButton.setTextLine2(sensorData);
                    sensorButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            Dialog.show(sensorButton.getTextLine1(),
                                    sensorButton.getTextLine2(), "ok", "");
                        }
                    });
                    sensorDataList.add(sensorButton);                    
                    editContainer.revalidate();
                    ////////log.p(textField.getText());                    
                }
            });
        }
        if (gyroscope != null) {
            textField.setText(textField.getText()+" gyroscope sensor" );
            gyroscope.registerListener(new SensorListener() {
                public void onSensorChanged(long timeStamp, float x, float y, float z) {
                    //do your stuff here...
                    String sensorData = "{" + '"' + "timestamp" + '"' + ":" + '"' + Long.toString(timeStamp) + '"';
                    sensorData = sensorData + "," + '"' + "x-axis" + '"' + ":" + '"' + Float.toString(x) + '"';
                    sensorData = sensorData + "," + '"' + "y-axis" + '"' + ":" + '"' + Float.toString(y) + '"';
                    sensorData = sensorData + "," + '"' + "z-axis" + '"' + ":" + '"' + Float.toString(z) + '"';
                    sensorData = "{" + '"' + "gyroscope" + '"' + ":" + sensorData + "},";
                    sensorData = sensorData + senseLocation() + "}";
                    if (textField.getText().contains("{")) {
                        textField.setText(textField.getText() + "," + sensorData);
                    } else {
                        textField.setText(sensorData);
                    }
                    requestParameter.value.set(textField.getText());
                    MultiButton sensorButton = new MultiButton();
                    sensorButton.setTextLine1("Gyroscope");
                    sensorButton.setTextLine2(sensorData);
                    sensorButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            Dialog.show(sensorButton.getTextLine1(),
                                    sensorButton.getTextLine2(), "ok", "");
                        }
                    });
                    sensorDataList.add(sensorButton);
                    editContainer.revalidate();
                    ////////log.p(textField.getText());                    
                }
            });
        }
        if ((gyroscope == null ) || (accelerometer == null ) || (magnetic == null)) {
            //if there is no gyroscope then we have to actually a timer that 
            //reads the gps every second and update
            //////log.p("Setup location timer");
            //UITimer.timer(100, true, () -> { 
                updateLocation();
            //}).schedule(3000, true, Display.getInstance().getCurrent());;

        }
    }
    private void updateLocation(){
             
        Timer t = new Timer();
          TimerTask ta = new TimerTask(){

             public void run() 
             {
//                ToastBar.showInfoMessage("Syncing messages ...");
      

        //////log.p("Updating Location");
              textField.setText(textField.getText() + " " + senseLocation() +"}");
                    requestParameter.value.set(textField.getText());
                    MultiButton sensorButton = new MultiButton();
                    sensorButton.setTextLine1("Location");
                    sensorButton.setTextLine2(textField.getText());
                    sensorButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            Dialog.show(sensorButton.getTextLine1(),
                                    sensorButton.getTextLine2(), "ok", "");
                        }
                    });
                    sensorDataList.add(sensorButton);                    
                    editContainer.revalidate();       
                   //////log.p(textField.getText());      
             }
          };
        t.schedule(ta,1000,100000);  
 
    }
    private String senseLocation() {
        LocationManager loc = LocationManager.getLocationManager();
        Location currentLocation = loc.getCurrentLocationSync(1L);
        String locData = "{" + '"' + "timestamp" + '"' + ":" + '"'
                + Long.toString(currentLocation.getTimeStamp()) + '"';
        locData = locData + "," + '"' + "longitude" + '"' + ":" + '"'
                + Double.toString(currentLocation.getLongitude()) + '"';
        locData = locData + "," + '"' + "latitude" + '"' + ":" + '"'
                + Double.toString(currentLocation.getLatitude()) + '"';
        locData = locData + "," + '"' + "direction" + '"' + ":" + '"'
                + Float.toString(currentLocation.getDirection()) + '"';
        locData = locData + "," + '"' + "altitude" + '"' + ":" + '"'
                + Double.toString(currentLocation.getAltitude()) + '"';
        locData = locData + "," + '"' + "velocity" + '"' + ":" + '"'
                + Float.toString(currentLocation.getVelocity()) + '"';
        locData = "{" + '"' + "location" + '"' + ":" + locData + "}";

        return locData;
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
                // ////////log.p(editContainer.getParent().getParent().toString());                
                Component c = new AttributeEditor(serviceAttribute, true);
                $(c).addTags("attribute");
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
        if ((Boolean) attr.multiplicity.getBoolean()) {
            headerContainer.add(addAnotherButton);
        }

        editContainer.add(headerContainer).add(sensorDataList).add(textField);
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

}
