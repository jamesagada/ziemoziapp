/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.InteractionDialog;
import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.googlemaps.MapContainer;
import com.codename1.googlemaps.MapContainer.MapObject;
import com.codename1.googlemaps.MapLayout;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.io.Util;
import com.codename1.location.Location;
import com.codename1.maps.Coord;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Button;
import static com.codename1.ui.Component.LEFT;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Rectangle;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.util.Callback;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import com.codename1.location.LocationManager;
import com.codename1.ui.Component;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.events.DataChangedListener;
import com.codename1.ui.layouts.BorderLayout;
import java.util.Vector;

/**
 *
 * @author jamesagada
 */
public class TransportStop extends BaseEditorImpl {

    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public Location locationField;
    public final Label textLabel = new Label();
    public final Button showMap = new Button("Show Map");
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public final MapContainer mapView = new MapContainer("AIzaSyDKDcbchGoq1NyuOipeClZKpyeFvlXI2yg");
    public static String MAP_API_KEY = "AIzaSyDKDcbchGoq1NyuOipeClZKpyeFvlXI2yg";
    public final Container headerContainer = new Container();
    Boolean tapDisabled = false;

    public TransportStop() {
        editContainer.putClientProperty("editor", this);
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        //setCurrentPosition();
        showMap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //Open the window and show the map in the location
                
                if (locationField != null) {
                    showOnMap(new Coord(locationField.getLatitude(), locationField.getLongitude()),
                            showMap.getComponentForm());
                }
            }
        });
    }

    @Override
    public Container edit(ServiceAttribute attr) {
        // we are editing an attribute that does not exist
        // we just create the field
        //textLabel.setText(_id);
        setCurrentPosition();
        //////////Log.p("Address coming to edit is " + textField.getText());
        if (this.requestParameter == null) {
            createRequestParameter(attr.type_of_attribute.get(0));
            setCurrentPosition();
            //////////Log.p("Address for null requestParameter is " + textField.getText());
        } else {
            //requestParameter exists
            //so we now set the value attribute to the value of the component
            //////////Log.p("Request Parameter Value is " + requestParameter.value.get());
            if (this.requestParameter.value.get() != null) {
                textField.setText(this.requestParameter.value.get());
            }
            //////////Log.p("Address for non null requestParmeter value is " + textField.getText());
        }
        //////////Log.p("Address is " + textField.getText());
        this.serviceAttribute = attr;
        textLabel.setText(attr.display_label.get());
        textField.setHint(attr.description.get());
        //textField.setSingleLineTextArea(false);
        //textField.setRows(5);
       // textField.setColumns(20);
       // textField.setScrollVisible(true);
        //setEditorConstraints();
        //////////Log.p("Address is " + textField.getText());
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                requestParameter.value.set(textField.getText());
                ////System.out.println("Attribute " + attr.display_label.get() + " is "
                 //       + requestParameter.getPropertyIndex().toJSON());
                       //////////Log.p("Address is " + textField.getText());
                if (textField.getText().length() > 0) {
                    try {
                        
                        Coord c = getCoords(textField.getText());
                        if ( c == null ) {
                            locationField= LocationManager.getLocationManager().getCurrentLocation();
                        }else {
                        locationField.setLatitude(c.getLatitude());
                        locationField.setLongitude(c.getLongitude());
                        }
                        //////////Log.p("Co-ordinates is " + c.toString());
                    }catch(Exception e){
                        e.printStackTrace();
                        ToastBar.showErrorMessage("Something went wrong");
                    }
                }
            }

        });

        //System.out.println("Attribute to edit is " + attr.getPropertyIndex().toJSON());
        // textField.setText(attr.default_value.get().toString());
        //////////Log.p("Address is " + textField.getText());
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

 
        editContainer.add(headerContainer).add(showMap).add(textField);
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
        //textField.setMaxSize(this.serviceAttribute.maximum_size.getInt());
        //Text does not have a minimum but we can store the minimum_value
        //in a validation context.
        //System.out.println("Maximum Field Size is " + textField.getMaxSize());
    }

    public static String getFormattedAddress(Coord coord) {
        String ret = "";
        try {
            ConnectionRequest request = new ConnectionRequest("https://maps.googleapis.com/maps/api/geocode/json", false);
            request.addArgument("key", MAP_API_KEY);
            request.addArgument("latlng", coord.getLatitude() + "," + coord.getLongitude());

            NetworkManager.getInstance().addToQueueAndWait(request);
            Map<String, Object> response = new JSONParser().parseJSON(new InputStreamReader(new ByteArrayInputStream(request.getResponseData()),
                    "UTF-8"));
            if (response.get("results") != null) {
                ArrayList results = (ArrayList) response.get("results");
                if (results.size() > 0) {
                    ret = (String) ((LinkedHashMap) results.get(0)).get("formatted_address");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void getFormattedAddressAsync(Coord coord, Callback callback) {
        ConnectionRequest request = new ConnectionRequest("https://maps.googleapis.com/maps/api/geocode/json", false) {
            @Override
            protected void readResponse(InputStream input) throws IOException {
                String ret = "";
                Map<String, Object> response = new JSONParser().parseJSON(new InputStreamReader(input,
                        "UTF-8"));
                if (response.get("results") != null) {
                    ArrayList results = (ArrayList) response.get("results");
                    if (results.size() > 0) {
                        ret = (String) ((LinkedHashMap) results.get(0)).get("formatted_address");
                    }
                }
                callback.onSucess(ret);
            }

        };
        request.addArgument("key", MAP_API_KEY);
        request.addArgument("latlng", coord.getLatitude() + "," + coord.getLongitude());

        NetworkManager.getInstance().addToQueue(request);
    }

    public static Coord getCoords(String address) {
        Coord ret = null;
        try {
            ConnectionRequest request = new ConnectionRequest("https://maps.googleapis.com/maps/api/geocode/json", false);
            request.addArgument("key", MAP_API_KEY);
            request.addArgument("address", address);

            NetworkManager.getInstance().addToQueueAndWait(request);
            Map<String, Object> response = new JSONParser().parseJSON(new InputStreamReader(new ByteArrayInputStream(request.getResponseData()),
                    "UTF-8"));
            if (response.get("results") != null) {
                ArrayList results = (ArrayList) response.get("results");
                if (results.size() > 0) {
                    LinkedHashMap location = (LinkedHashMap) ((LinkedHashMap) ((LinkedHashMap) results.get(0)).get("geometry")).get("location");
                    ret = new Coord((double) location.get("lat"), (double) location.get("lng"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastBar.showErrorMessage(e.getMessage());
            Log.sendLogAsync();
        }
        return ret;
    }

    public static void getCoordsAsync(String address, Callback callback) {
        ConnectionRequest request = new ConnectionRequest("https://maps.googleapis.com/maps/api/geocode/json", false) {
            @Override
            protected void readResponse(InputStream input) throws IOException {
                Coord ret = null;
                Map<String, Object> response = new JSONParser().parseJSON(new InputStreamReader(input,
                        "UTF-8"));
                if (response.get("results") != null) {
                    ArrayList results = (ArrayList) response.get("results");
                    if (results.size() > 0) {
                        LinkedHashMap location = (LinkedHashMap) ((LinkedHashMap) ((LinkedHashMap) results.get(0)).get("geometry")).get("location");
                        ret = new Coord((double) location.get("lat"), (double) location.get("lng"));
                    }
                }
                callback.onSucess(ret);
            }

        };
        request.addArgument("key", MAP_API_KEY);
        request.addArgument("address", address);

        NetworkManager.getInstance().addToQueue(request);
    }

    public void showOnMap(Coord moscone, Form parent) {
        //Form mapForm = new Form("Maps", new LayeredLayout());
        Form mapForm = new Form("Select Location", new BorderLayout());
        MapContainer mc = new MapContainer(MAP_API_KEY);
            Button mosconeButton = new Button("Done");
            mosconeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    //close the window and return
                    parent.showBack();
                }
            });
            mosconeButton.setUIID("Label");            
        if (BrowserComponent.isNativeBrowserSupported()) {
            //System.out.println("Browser Component Supported");

            //Container markers = new Container();
            //markers.setLayout(new MapLayout(mc, markers));
            //mapForm.add(markers);
            Vector vm = new Vector<MapObject>();
            //Coord moscone = new Coord(37.7831, -122.401558);
            //Coord moscone = new Coord(location);
            String locationTxt = getFormattedAddress(moscone);
            //System.out.println(locationTxt);


            //FontImage.setMaterialIcon(mosconeButton, FontImage.MATERIAL_PLACE);
            //markers.add(moscone, mosconeButton);
            Image markerImg = FontImage.createMaterial(FontImage.MATERIAL_PLACE, mosconeButton.getStyle());

            MapObject o = mc.addMarker(EncodedImage.createFromImage(markerImg, false),
                    moscone, locationTxt, "", e3 -> {
                        ToastBar.showMessage(locationTxt, FontImage.MATERIAL_PLACE);
                    });

            vm.add(o);
            mc.addTapListener(e -> {
                if (tapDisabled) {
                    return;
                }
                tapDisabled = true;
                //clear markers
                for (Object m : vm) {
                    MapObject mo = (MapObject) m;
                    mc.removeMapObject(mo);
                }
                TextField enterName = new TextField();
                Container wrapper = BoxLayout.encloseY(new Label("Name:"), enterName);
                InteractionDialog dlg = new InteractionDialog("Location");
                Coord m = mc.getCoordAtPosition(e.getX(), e.getY());

                ToastBar.showInfoMessage("Selected Address " + enterName.getText());
                Image mImg = FontImage.createMaterial(FontImage.MATERIAL_PLACE, mosconeButton.getStyle());
                MapObject om = mc.addMarker(EncodedImage.createFromImage(mImg, false),
                        m, enterName.getText(), "", e3 -> {
                    ToastBar.showMessage("You clacked and clicked " + enterName.getText(), FontImage.MATERIAL_PLACE);

                });
                locationField.setLatitude(m.getLatitude());
                locationField.setLongitude(m.getLongitude());
                vm.add(om);
                //enterName.setText(getFormattedAddress(m));   
                enterName.setText(getFormattedAddress(m));
                textField.setText(enterName.getText());
                tapDisabled = false;
                dlg.getContentPane().add(wrapper);
                enterName.setDoneListener(e2 -> {
                    //String txt = enterName.getText();
                    //Coord m = mc.getCoordAtPosition(e.getX(), e.getY());

                    textField.setText(enterName.getText());
                    dlg.dispose();
                    tapDisabled = false;
                });
                dlg.showPopupDialog(new Rectangle(e.getX(), e.getY(), 10, 10));
                //enterName.startEditingAsync();
            });

            mc.zoom(moscone, 16);
            mc.setCameraPosition(moscone);
            //mc.setShowMyLocation(true);
        } else {
            // iOS Screenshot process...
            mapForm.add(new Label("Loading, please wait...."));
        }
        mapForm.add(BorderLayout.CENTER,mc).add(BorderLayout.NORTH,mosconeButton);
        mapForm.show();
    }

    public void setCurrentPosition() {
        //////////Log.p("Set current position");
        InfiniteProgress ip = new InfiniteProgress();
        Dialog ipDlg = ip.showInfiniteBlocking();
        Location location = LocationManager.getLocationManager().getCurrentLocationSync(30000);
        ipDlg.dispose();
        if (location == null) {
            try {
                location = LocationManager.getLocationManager().getCurrentLocation();
            } catch (IOException err) {
                Dialog.show("Location Error", "Unable to find your current location, please be sure that your GPS is turned on", "OK", null);
                return;
            }
        }

        textField.setText(getFormattedAddress(new Coord(location.getLatitude(),
                location.getLongitude())));
        locationField = location;
        Double loc1 = location.getLatitude();
        Double loc2 = location.getLongitude();
        //////////Log.p("Latitude: " + loc1);
        //////////Log.p("Longitude: " + loc2);
        //////////Log.p("Current Address " + textField.getText());
    }

}
