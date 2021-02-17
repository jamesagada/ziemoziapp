/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.components.FloatingActionButton;
import com.codename1.components.ToastBar;
import com.codename1.io.Log;
//import com.codename1.components.Button;
import com.ixzdore.restdb.ziemview.AttributeEditor;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.BOTTOM;
import static com.codename1.ui.Component.TOP;
import com.codename1.ui.ComponentSelector;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.TextField;
import com.codename1.ui.URLImage;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.events.FocusListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.ziemozi.server.ServerAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jamesagada
 */
public class ZiemView {

    Container requestContainer;
    Container contentContainer;
    Container parameterTypeContainer;
    Container serviceContainer;
    Container contentAndParameter;//contian content and parameter   
    HashMap<String, Container> parameterPanel;
    Button txtRequestButton = new Button("What has happened?");
    ZiemValidator zValidator = new ZiemValidator();
    Request request;
    public Boolean showPanels = true; //use to determine if the panels should show. If panels wont show then
    //we have to put each attribute into the container panel
    Boolean showFloatingButton =true;

    public Container createRequestView(Service service) {
        ArrayList<Service> services = new ArrayList<Service>();
        services.add(service);
        txtRequestButton.setText(service.name.get());
        return createRequestView(services);
    }

    public Container createRequestView(ArrayList<Service> services) {
        requestContainer = new Container();
        requestContainer.putClientProperty("ziemView", this);
        contentContainer = new Container();
        parameterTypeContainer = new Container();
        serviceContainer = new Container();
        contentAndParameter = new Container();//contian content and parameter
        parameterPanel = new HashMap<String, Container>(); //holds the parameter panels
        contentContainer.getStyle().setBorder(Border.createBevelRaised());
        serviceContainer.getStyle().setBorder(Border.createBevelRaised());
        parameterTypeContainer.getStyle().setBorder(Border.createBevelRaised());
        parameterTypeContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        contentAndParameter.getStyle().setBorder(Border.createBevelRaised());
        TextField txtRequestDescription = new TextField("What has happened?");

        parameterTypeContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        parameterTypeContainer.setScrollableY(true);
        parameterTypeContainer.revalidate();
        contentContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        contentContainer.setScrollableY(true);
        contentContainer.setSmoothScrolling(true);
        contentContainer.add(txtRequestButton);
        contentContainer.revalidate();
        contentAndParameter.setLayout(new BorderLayout());

        if ( showPanels ) contentAndParameter.add(BorderLayout.WEST, parameterTypeContainer);
        contentAndParameter.add(BorderLayout.CENTER, contentContainer);
        contentAndParameter.revalidate();
        requestContainer.setLayout(new BorderLayout());
        requestContainer.add(BorderLayout.CENTER, contentAndParameter);
        if ( showPanels ) requestContainer.add(BorderLayout.SOUTH, serviceContainer);
        //
        serviceContainer.setLayout(new FlowLayout());
        fillServiceContainer(services, serviceContainer);
        //add floating action button

        requestContainer.revalidate();
        //return requestContainer;
        if (showFloatingButton ) {
            return addFloatingActionButton(requestContainer);
        }else {
            return requestContainer;
        }
    }

    private void fillServiceContainer(ArrayList<Service> services, Container serviceContainer) {
        //fill in the services into the service container
        //extract the icons for each service and then
        //build a button group and attach the listener
        //services may have hierarchies implemented in parent child hierarchy
        //
        //what is the type of contents in services
        //if we are not showing panels then we assume there is only one service 
        //and fill it into the contentContainer
        ArrayList<Service> newServices = new ArrayList<Service>();
        for (Object o : services) {
            //System.out.println("type of service " + o.getClass().getCanonicalName());
            //System.out.println("\\Service " + o.toString());  
            if (o.getClass().getCanonicalName().equalsIgnoreCase("java.lang.String")){
                //it is likely that this the ids of the services so let us try and replace it 
                //with the actual service
                
                Service s = ServerAPI.getService(o.toString());
                if (s != null ) newServices.add(s);
            }
        }
        ArrayList<Service> servicesToUse;
        ////////log.p("newServices " + newServices.size() );
        if (newServices.size() > 0) {
            servicesToUse = newServices;
        }else {
            servicesToUse = services;
        }
        for (Service service : servicesToUse) {
            //add button to represent this service in the service list
            //this is only if the service has a service definition
            if (!service.getClass().getCanonicalName().equalsIgnoreCase("java.lang.String")){
            //System.out.println(service.name.get() + " has " + service.service_attributes.size() + " attributes");
            ////System.out.println(service.service_attributes);
            //System.out.println(service.getPropertyIndex().toJSON());
            if (service.service_attributes.size() > 0) {
                if (showPanels){
                    serviceContainer.add(makeServiceButton(service));
                addToParameterPanel(service);//add parameter type panel
                //we will have a vector or HashMap of containers
                //and we can replace the content of the current panel
                }else {
                    //we are not showing the panels
                    //so we fill the contentContainer
                    //contentContainer.putClientProperty("service", service);
                    addToContentContainer(service);
                }
            }
        }
        }
    }

    private Component makeServiceButton(Service service) {
        Button serviceButton = new Button();
        //set the icon

        //set the action listener so that when we click on this the service
        //parameterContainer shows the parameters for this service.
        if (service.logo.size() > 0) {
            String logo = service.logo.get(0);
            serviceButton = makeLogoButton(service.name.get(), logo);
            //serviceButton.setText(service.name.get()); 
            //serviceButton.set;
            //Image roundMask = Image.createImage(placeholder.getWidth(), placeholder.getHeight(), 0xff000000);
            //Graphics gr = roundMask.getGraphics();
            //gr.setColor(0xffffff);
            //gr.fillArc(0, 0, placeholder.getWidth(), placeholder.getHeight(), 0, 360);

            //URLImage.ImageAdapter ada = URLImage.createMaskAdapter(roundMask);
            //Image i = URLImage.createToStorage(placeholder, attr.name.get(),
            //       "https://ziemozi-a3ef.restdb.io/media/" + logo + "?s=t");
            //parameterButton.setIcon(i);
        } else {
            serviceButton.setIcon(FontImage.createMaterial(FontImage.MATERIAL_ADD_ALERT, serviceButton.getSelectedStyle()));
        }

        //serviceButton.setIcon(makeLogoButton(service.name.get(),service.logo))
        serviceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //make the parameterContainer to switch view to the parameters of this 
                switchParameterPanel(service.name.get());
                txtRequestButton.setText(service.name.get());
                contentContainer.removeAll();
                contentContainer.add(txtRequestButton);
                  //  addToContentContainer(service);            
                contentContainer.putClientProperty("service", service);
                //if (service.providers.get(0) != null ) 
                //    contentContainer.putClientProperty("provider", service.providers.get(0));
                contentAndParameter.revalidate();
                contentAndParameter.repaint();

            }
        });
        return serviceButton;
    }

    private void switchParameterPanel(String get) {

        if (parameterTypeContainer.getComponentCount() < 1) {
            Component nextPanel = parameterPanel.get(get);
            if (nextPanel != null) {
                parameterTypeContainer.add(nextPanel);
            }
        } else {
            Component currentPanel = parameterTypeContainer.getComponentAt(0);
            //if the component is null or is not a container then 
            //we will try and add instead of replace.
            Component nextPanel = parameterPanel.get(get);
            if ((currentPanel != null)) {
                parameterTypeContainer.replace(currentPanel, nextPanel, null);
                parameterTypeContainer.repaint();
            } else {
                //currentPanel contains nothing so we add
                if (nextPanel != null) {
                    parameterTypeContainer.add(nextPanel);
                }
            }

        }
        //refresh the components
        parameterTypeContainer.setEnabled(true);
        parameterTypeContainer.revalidate();
        parameterTypeContainer.repaint();
    }

    private Image makeIconFromServiceLogo(Property<Image, Service> logo) {
        Button myButton = new Button();
        return FontImage.createMaterial(FontImage.MATERIAL_FACE, myButton.getSelectedStyle());
    }

    private void addToParameterPanel(Service service) {
        //create a panel which is just a container
        //but from the service attributes
        //System.out.println("Processing --> " + service.getPropertyIndex().toString());
        Container attributePanel = new Container();
        attributePanel.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        attributePanel.setScrollableY(true);
        parameterPanel.put(service.name.get(), attributePanel);
        Boolean getService = false;
        for (Object ss:service.service_attributes){
            if (ss.getClass().getCanonicalName().indexOf("String") > 0) {
                //it is a string, so we really need to retrieve the service all over again
                getService = true;
            }
        }
        String _id = service._id.get();
        if (getService){
            service = ServerAPI.getService(_id);
        }
        for (Object sattr : service.service_attributes) {
            //for each service attribute
            //add a button to the container
            //so we will have one button for each. IF it is a nested we need to worry
            //also we need to keep them in sequence
            //and also worry about the type. We should get the icon from the type?
            // it will be good not to put types we dont know about 
            //ServiceAttribute serviceAttribute = new ServiceAttribute();
            //ServiceAttribute withAttributeType = new ServiceAttribute();
            ////System.out.println("Map is --> " + sattr.toString());
            //withAttributeType.getPropertyIndex().populateFromMap((HashMap) sattr, ServiceAttributeType.class);
            //serviceAttribute.getPropertyIndex().populateFromMap((HashMap) sattr);
            ////////log.p("\n\nClass type of attribute " + sattr.getClass().getCanonicalName());
            ////////log.p("\n"+ sattr.toString());
            ServiceAttribute serviceAttribute = (ServiceAttribute)sattr;
            //serviceAttribute.getPropertyIndex().populateFromMap((HashMap)sattr, ServiceAttributeType.class);
            ////System.out.println("Attribute Type " + withAttributeType.getPropertyIndex().toJSON());
            ////System.out.println(" Attribute Type " + withAttributeType.type_of_attribute.get(0).name.get());
            //ServiceAttributeType attributeType = withAttributeType.type_of_attribute.get(0);
            ////System.out.println(" Attribute Type extracted"
            //        + attributeType.getPropertyIndex().toJSON());
            Button parameterButton = makeParameterButton(
                    serviceAttribute);
            //parameterButton.setIcon(serviceAttribute.serviceAttributeType.get().name.get(););
            if (parameterButton != null) {
                parameterButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        contentContainer.revalidate();
                        //when we click on any parameter, if it is a parameter that 
                        //can be multiple, create a similar component and insert next to the 
                        //last one of same type
                        //if it is not then just find the component and highlight it
                        //when we insert it or change it
                        //the content panel will just show the objects and will not do inplace editing
                        //to edit a specific component, you select it
                        // if the parameter multiplicity is not set to true 
                        // disable the button
                        if (!serviceAttribute.multiplicity.getBoolean()) {
                            parameterButton.setEnabled(false);
                        }
                        Component attr = editAttribute(serviceAttribute);
                        attr.requestFocus();
                        attr.addFocusListener(new FocusListener() {
                            @Override
                            public void focusGained(Component cmp) {
                                editAttribute(attr); //To change body of generated methods, choose Tools | Templates.
                            }

                            @Override
                            public void focusLost(Component cmp) {
                            }
                        });
                        contentContainer.add(attr);
                        contentContainer.revalidate();
                    }
                });
                attributePanel.add(parameterButton);
            }
        }
    }

    private void editAttribute(Component comp) {
        //when we touch an attribute, we bring up the editor for the attribute
    }

    private Component editAttribute(ServiceAttribute serviceAttribute) {
        //open an editor for this type
        //return the edited value into the representative component.
        Component attr = null;
        //determine the attribute type and then render it
        //into a component.
        //the issue is how do we deal with group 
        //if it is a group then we simply let the group editor handle it.
        //first determine the type of attribute
        if (serviceAttribute != null ) attr = new AttributeEditor(serviceAttribute, true);
        return attr;
    }

    private Button makeLogoButton(String ref, String logo) {
        Button b = new Button();
        Style s = UIManager.getInstance().getComponentStyle("MultiLine1");
        FontImage p = FontImage.createMaterial(FontImage.MATERIAL_PORTRAIT, s);
        EncodedImage placeholder = EncodedImage.createFromImage(p.scaled(p.getWidth(), p.getHeight()), false);
        if (logo != null) {
            String url = ServerAPI.mediaUrl(logo);
            Image i = URLImage.createToStorage(placeholder, ref,
                    url);
            b.setIcon(i);
        } else {
            b.setIcon(p);
        }
        b.setUIID("Label");
        return b;
    }

    private Button makeParameterButton(ServiceAttribute attr) {
        //extract the logo

        //System.out.println("Attribute " + attr.name.get());
        Button parameterButton = new Button();

        if (attr.logo.size() > 0) {
            String logo = attr.logo.get(0);
            parameterButton = makeLogoButton(attr.name.get(), logo);
            //Image roundMask = Image.createImage(placeholder.getWidth(), placeholder.getHeight(), 0xff000000);
            //Graphics gr = roundMask.getGraphics();
            //gr.setColor(0xffffff);
            //gr.fillArc(0, 0, placeholder.getWidth(), placeholder.getHeight(), 0, 360);

            //URLImage.ImageAdapter ada = URLImage.createMaskAdapter(roundMask);
            //Image i = URLImage.createToStorage(placeholder, attr.name.get(),
            //       "https://ziemozi-a3ef.restdb.io/media/" + logo + "?s=t");
            //parameterButton.setIcon(i);
        } else {
            parameterButton.setIcon(FontImage.createMaterial(FontImage.MATERIAL_ADD_ALERT, parameterButton.getSelectedStyle()));
        }
        parameterButton.setUIID("Label");
        return parameterButton;
    }

    public Request makeNewRequest(Service srv) {
        //create a request 
        //make a view
        //have it edited
        //retrieve the request object 
        //return it
        //request object can be any thing that can be defined with service definition
        Request r = new Request();

        return r;
    }

    public Container viewRequest() {
        //return the view of a request
        //the view of the request will be none editable
        //if the request has a parent, a link to the parent will be provided
        //if the request has children, the number of children will be indicated
        Container cnt = new Container();
        return cnt;
    }

    private Container addFloatingActionButton(Container cnt) {
        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_EXPAND_MORE);
        FloatingActionButton save = fab.createSubFAB(FontImage.MATERIAL_SAVE, ""); 
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // create the request from the display
                // call the save function which should determine how the 
                //object will be saved.
                ToastBar.showInfoMessage("Saving ...");
                saveRequestFromContainer(contentContainer);
                
             }
        });
        FloatingActionButton comment = fab.createSubFAB(FontImage.MATERIAL_COMMENT, ""); 
        comment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //comment can only be made if we were viewing
                //and the parent already exists
                //we have to retrieve the Request
             }
        });
        FloatingActionButton follow = fab.createSubFAB(FontImage.MATERIAL_LINK, "");
    return fab.bindFabToContainer(cnt, Component.RIGHT, Component.TOP);

    }
    public void saveRequestFromContainer(Container cnt){
        //go over container
        //take 
        updateRequestFromContainer(cnt);
        //this.request now holds the request
        // the actual saving should be done by the request.
        this.request.save();
    }
    public void saveRequestFromDefaultContainer(){
        //assume the default container is contentContainer
        saveRequestFromContainer(contentContainer);
    }
    public Request getRequest(){
        updateRequestFromContainer();
        return this.request;
    }
    public void updateRequestFromContainer(Container cnt){
        //retrieve the service definition
        //create the request based on the requestComponents of the container
        Service service = (Service)cnt.getClientProperty("service");

        Request request = new Request();
        request.parent.set((Request) cnt.getClientProperty("parent"));
        //request.provider.set((Provider) cnt.getClientProperty("provider"));
        //request.service.set(service);//if service is null then it is null.
        request.service.add(service);
        //if we are creating then there are a few things that we need to get but we will assume they 
        //are there
        //we have things like priority, etc.

        int i = 0;
        while  (i < cnt.getComponentCount()) {
            ////////log.p("Component Count is " + cnt.getComponentCount());
            ////////log.p("Component number "+i);
            ////////log.p(cnt.getComponentAt(i).toString());
            ////////log.p(cnt.getComponentAt(i).getClass().getName());
            if (cnt.getComponentAt(i).getClass().getName().endsWith("AttributeEditor")){

             AttributeEditor a = (AttributeEditor)cnt.getComponentAt(i);
             RequestParameter r = (RequestParameter)a.getAttributeValue();
             if (r != null) {
             ServiceAttribute s = r.service_attribute.get(0);
 //            ////////log.p("Request Parameter " + r.getPropertyIndex().toString());
//             ////////log.p("Service Attribute is From Request Parameter " + s.getPropertyIndex().toString());
             
                if (s != null) {
                    request.request_parameters.add(r);
                   ////////log.p("Service Attribute is From Request Parameter " + s.getPropertyIndex().toString()); 
                }
            }
            }
            i++;
         //how do we deal with values not in the container or do we mark those as hidden and let it 
         //be processed automatically?
        }    
        ////////log.p("Service " +service.getPropertyIndex().toJSON());
        ////////log.p("Request " + request.getPropertyIndex().toString());
        this.request=request;
    }
    public void updateRequestFromContainer(){
        updateRequestFromContainer(contentContainer);
    }

    private void addToContentContainer(Service service) {

        //create a panel which is just a container
        //but from the service attributes
        //System.out.println("Processing --> " + service.getPropertyIndex().toString());
        contentContainer.putClientProperty("service", service);
        for (Object sattr : service.service_attributes) {
            //for each service attribute
            //add a button to the container
            //so we will have one button for each. IF it is a nested we need to worry
            //also we need to keep them in sequence
            //and also worry about the type. We should get the icon from the type?
            // it will be good not to put types we dont know about 
            ServiceAttribute sa = (ServiceAttribute)sattr;
            ServiceAttribute serviceAttribute = (ServiceAttribute)sattr;
            ServiceAttribute withAttributeType = (ServiceAttribute)sattr;
            Map attributeMap = sa.getPropertyIndex().toMapRepresentation();
            ////System.out.println("Map is --> " + sattr.toString());
            /**
             * try {
                withAttributeType.getPropertyIndex().populateFromMap((HashMap) sattr, ServiceAttributeType.class);
            }catch(Exception e){
            serviceAttribute.getPropertyIndex().populateFromMap((HashMap) sattr);
            }
            //serviceAttribute.getPropertyIndex().populateFromMap((HashMap)sattr, ServiceAttributeType.class);
            */
            //
            //System.out.println("Attribute Type " + withAttributeType.getPropertyIndex().toJSON());
            ////System.out.println(" Attribute Type " + withAttributeType.type_of_attribute.get(0).name.get());
            //ServiceAttributeType attributeType = withAttributeType.type_of_attribute.get(0);
            ////System.out.println(" Attribute Type extracted"
            //        + attributeType.getPropertyIndex().toJSON());
            //Button parameterButton = makeParameterButton(
            //        serviceAttribute);
            //parameterButton.setIcon(serviceAttribute.serviceAttributeType.get().name.get(););
            
            //if (parameterButton != null) {
                        Component attr = editAttribute(serviceAttribute);
                        attr.requestFocus();
                        attr.addFocusListener(new FocusListener() {
                            @Override
                            public void focusGained(Component cmp) {
                                editAttribute(attr); //To change body of generated methods, choose Tools | Templates.
                            }

                            @Override
                            public void focusLost(Component cmp) {
                            }
                        });
                        contentContainer.add(attr);

            //        }
        }
                                contentContainer.revalidate();
    }    

    public Container createRequestView(List<Service> asList) {
        ArrayList<Service> services = new ArrayList<Service>();
        services.addAll(asList);
        return createRequestView(services);
    }

    public void setRequestParent(Request parent) {
        contentContainer.putClientProperty("parent", parent);
      }
}
