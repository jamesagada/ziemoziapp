/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.components.FloatingActionButton;
import com.codename1.components.InfiniteProgress;
import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.io.Log;
import com.codename1.properties.ListProperty;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;



//Read more: https://javarevisited.blogspot.com/2017/09/java-8-sorting-hashmap-by-values-in.html#ixzz6m5AvLPeC
//import com.codename1.components.Button;
import com.ixzdore.restdb.ziemview.AttributeEditor;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.NORTH;
import com.codename1.ui.Component;
import static com.codename1.ui.Component.BOTTOM;
import static com.codename1.ui.Component.CENTER;
import static com.codename1.ui.Component.TOP;
import com.codename1.ui.ComponentSelector;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Tabs;
import com.codename1.ui.TextField;
import com.codename1.ui.URLImage;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.events.FocusListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.Layout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.ziemozi.forms.MainForm;
import com.ziemozi.forms.NewsfeedContainer;
import com.ziemozi.forms.UIUtils;
import com.ziemozi.server.ServerAPI;
import com.ziemozi.server.local.localAPI;
import com.ziemozi.ziemozi.UIController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author jamesagada
 */
public class ZiemViewTab {

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
    Boolean showFloatingButton = true;

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
        contentContainer.setUIID("Label");
        parameterTypeContainer = new Container();
        serviceContainer = new Container();
        contentAndParameter = new Container();//contian content and parameter
        parameterPanel = new HashMap<String, Container>(); //holds the parameter panels
        //contentContainer.getStyle().setBorder(Border.createBevelRaised());
       // serviceContainer.getStyle().setBorder(Border.createBevelRaised());
        //parameterTypeContainer.getStyle().setBorder(Border.createBevelRaised());
        parameterTypeContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        //contentAndParameter.getStyle().setBorder(Border.createBevelRaised());
        TextField txtRequestDescription = new TextField("What has happened?");
        serviceContainer.setUIID("TabZ");
        parameterTypeContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        parameterTypeContainer.setScrollableY(true);
        parameterTypeContainer.revalidate();
        contentContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        contentContainer.setScrollableY(true);
        contentContainer.setSmoothScrolling(true);
        contentContainer.add(txtRequestButton);
        contentContainer.revalidate();
        contentAndParameter.setLayout(new BorderLayout());
        contentAndParameter.getAllStyles().setMarginBottom(10);
        contentContainer.getAllStyles().setMarginBottom(10);

        if (showPanels) {
            //contentAndParameter.add(BorderLayout.WEST, parameterTypeContainer);
        }
        contentAndParameter.add(BorderLayout.CENTER, contentContainer);
        contentAndParameter.revalidate();
        BorderLayout l = new BorderLayout();
        l.setCenterBehavior(BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE);
        requestContainer.setLayout(l);
        requestContainer.add(BorderLayout.CENTER, contentAndParameter);

        if (showPanels) {
            requestContainer.add(BorderLayout.NORTH, serviceContainer);
        }
        //
        serviceContainer.setLayout(new FlowLayout());
        fillServiceContainer(services, serviceContainer);
        //add floating action button

        requestContainer.revalidate();
        //return requestContainer;
        if (showFloatingButton) {
            return addFloatingActionButton(requestContainer);
        } else {
            return requestContainer;
        }
    }

    private void fillServiceTabContainer(ArrayList<Service> services, Container serviceContainer) {
        //fill in the services into the service container
        //extract the icons for each service and then
        //build a button group and attach the listener
        //services may have hierarchies implemented in parent child hierarchy
        //
        //what is the type of contents in services
        //if we are not showing panels then we assume there is only one service 
        //and fill it into the contentContainer
        ArrayList<Service> servicesToUse = services;
        /*
        for (Object o : services) {
            //////System.out.println("type of service " + o.getClass().getCanonicalName());
            //////System.out.println("\\Service " + o.toString());
            if (o.getClass().getCanonicalName().equalsIgnoreCase("java.lang.String")) {
                //it is likely that this the ids of the services so let us try and replace it 
                //with the actual service

                Service s = ServerAPI.getService(o.toString());
                if (s != null) {
                    newServices.add(s);
                }
            }
        }
        ArrayList<Service> servicesToUse;
        ////////////////Log.p("newServices " + newServices.size());
        if (newServices.size() > 0) {
            servicesToUse = newServices;
        } else {
            servicesToUse = services;
        }
         */
        servicesToUse = services;
        Tabs serviceTabContainer = new Tabs();
        serviceTabContainer.setUIID("TabZ");
        serviceTabContainer.getTabsContainer().setUIID("TabZ");
        for (Service service : servicesToUse) {
            // //////Log.p("Service " + service.name.get() + "is enabled " + service.enabled.getBoolean());
            //add button to represent this service in the service list
            //this is only if the service has a service definition
            //Tabs serviceTabContainer = new Tabs();
            //serviceTabContainer.setUIID("TabZ");
            //serviceTabContainer.getTabsContainer().setUIID("TabZ");
            if (!service.getClass().getCanonicalName().equalsIgnoreCase("java.lang.String")) {
                //////System.out.println(service.name.get() + " has " + service.service_attributes.size() + " attributes");
                //////System.out.println(service.service_attributes);
                //////System.out.println(service.getPropertyIndex().toJSON());

                if ((service.service_attributes.size() > 0) && (service.enabled.getBoolean())) {
                    if (showPanels) {
                        serviceTabContainer.add(makeServiceButton(service));
                        serviceContainer.add(serviceTabContainer);
                        //addToParameterPanel(service);//add parameter type panel
                        //we will have a vector or HashMap of containers
                        //and we can replace the content of the current panel
                    } else {
                        //we are not showing the panels
                        //so we fill the contentContainer
                        //contentContainer.putClientProperty("service", service);
                        addToContentContainer(service);
                    }
                }
            }
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
        /*for (Service o : services) {
            o.refresh();
            /*
            ////System.out.println("type of service " + o.getClass().getCanonicalName());
            ////System.out.println("\\Service " + o.toString());
            if (o.getClass().getCanonicalName().equalsIgnoreCase("java.lang.String")) {
                //it is likely that this the ids of the services so let us try and replace it 
                //with the actual service

                Service s = ServerAPI.getService(o.toString());
                if (s != null) {
                    newServices.add(s);
                }
            }
            
        }
        ArrayList<Service> servicesToUse;
        //////////////Log.p("newServices " + newServices.size());
        if (newServices.size() > 0) {
            servicesToUse = newServices;
        } else {
            servicesToUse = services;
        }
         */

        for (Service service : services) {
            //add button to represent this service in the service list
            //this is only if the service has a service definition
            ////////Log.p("Service " + service.name.get() + "is enabled " + service.enabled.get());
            if (service.enabled.get() == null) {
                service.enabled.set("FALSE");
            }
            Boolean isEnabled = service.enabled.getBoolean();
            if (service.service_attributes.size() <= 0) {
                ////////Log.p("Refresh Service");
                service.refresh();
            }
            if ((!service.getClass().getCanonicalName().equalsIgnoreCase("java.lang.String"))
                    && (isEnabled)) {
                ////System.out.println(service.name.get() + " has " + service.service_attributes.size() + " attributes");
                //////System.out.println(service.service_attributes);
                ////System.out.println("\n Service To Create \n" + service.getPropertyIndex().toString());
                if (service.service_attributes.size() > 0) {
                    if (showPanels) {
                        serviceContainer.add(makeServiceButton(service));
                        //addToParameterPanel(service);//add parameter type panel
                        //we will have a vector or HashMap of containers
                        //and we can replace the content of the current panel
                    } else {
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
        serviceButton.setUIID("SmallLabel");
        serviceButton.getAllStyles().setBorder(Border.createBevelRaised());
        //set the action listener so that when we click on this the service
        //parameterContainer shows the parameters for this service.
        if (service.logo.size() > 0) {
            String logo = service.logo.get(0);
            serviceButton = makeLogoButton(service.label.get(), logo);
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
        serviceButton.setTextPosition(BOTTOM);
        //serviceButton.setIcon(makeLogoButton(service.name.get(),service.logo))
        serviceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //make the parameterContainer to switch view to the parameters of this 
                //just clear the contentpanel and add new tabs
                //switchParameterPanel(service.name.get());
                InfiniteProgress ip = new InfiniteProgress();
                Dialog ipd = ip.showInfiniteBlocking();
                //ToastBar.showInfoMessage("Setting up Form");
                //txtRequestButton.setText(service.name.get());
                txtRequestButton.setText(service.label.get());
                contentContainer.removeAll();
                requestContainer.repaint();
                requestContainer.getParent().repaint();
                contentContainer.add(txtRequestButton);
                //create tabs for each group of attributescontainer
                //first create the collection of groups
                //in the right order
                contentContainer.revalidate();
                service.refreshAttributes();
                Vector groups = getGroupsOfAttributes(service.service_attributes);
                //groups now holds the list of groups and associated attributes
                //in proper order. So if we take anyone we can properly create its tab
                //if there are no groups, each attribute is a group of its own and will
                // be in its own tab
                contentContainer.add(createTabsForAttributesGroups(groups, contentContainer));
                //at this point we have created the fields fo this service
                //now we will implement the watching by cycling through the 
                //components, and picking out which ones are watching what 
                //and setting the observer and observable appropriately
                ////////////Log.p("Check for Watched in " + $(".attribute").size() + " Components");
                ComponentSelector sel = $(".attribute");
                ArrayList<Component> c_attr = findAttributes(contentContainer);
                ////////////Log.p("Number of components " + c_attr.size());
                for (Component c : c_attr) {
                    AttributeEditor a = (AttributeEditor) c;
                    //////Log.p("Component with attribute" + c.getName());
                    RequestParameter r = a.getAttributeValue();
                    ServiceAttribute s = a.attribute;
                    if (s.watch_this_attribute.size() <= 0) {
                        //                   ////////Log.p("Refresh Service Attribute For Watch");
                        //                    s.refreshAttribute();
                    }
                    //                  ////////Log.p("Refresh Attribute Size is " + s.watch_this_attribute.size());
                    //ServiceAttribute ss = s;
                    //r.service_attribute.get();

                    ////////////Log.p("Service attribute fom request " + ss.getPropertyIndex().toString());
                    //////Log.p("Should we watch " + s.getPropertyIndex().toString());
                    if (s != null) {
                        //ss.refreshAttribute();
                        ////////////Log.p(ss.name.get() + "is watching " + ss.watch_this_attribute.size()  + " attributes");
                        if (s.watch_this_attribute.size() > 0) {
                            /// if (ss.watch_this_attribute.asList().get(0)
                            //        .getClass().getCanonicalName().endsWith("String")) {
                            //    ss.refreshAttribute();
                            //}
                            List<ServiceAttribute> w = s.watch_this_attribute.asList();
                            ////////////Log.p("Watching " + w.size() + "attributes");
                            ////////////Log.p("Watch this attribute " + w.get(0).getPropertyIndex().toString());
                            //////////////Log.p("We are going to watch " + w.get(0).name.get());
                            //we need to set the watching on for this attribute
                            a.fieldWatcher.watchThisField(w.get(0), watchContext(findAttributes(
                                    contentContainer)));
                            a.fieldWatcher.watchingOnBehalfOf = a.baseEditor;
                            addObserver(a, w.get(0));
                        }
                    }
                }
                //  addToContentContainer(service);            
                contentContainer.putClientProperty("service", service);
                //if (service.providers.get(0) != null ) 
                //    contentContainer.putClientProperty("provider", service.providers.get(0));
                contentAndParameter.revalidate();
                contentAndParameter.repaint();
                requestContainer.revalidate();
                requestContainer.getParent().revalidate();
                ipd.dispose();
                ip.remove();
            }

            private void addObserver(AttributeEditor a, ServiceAttribute s) {
                for (Component c : findAttributes(contentContainer)) {
                    AttributeEditor ao = (AttributeEditor) c;
                    //////////////Log.p(ao.attribute.name.get() + ":" + s.name.get());

                    if (ao.attribute.name.get().equalsIgnoreCase(s.name.get())) {
                        ao.fieldBroadcast.addObserver(a.fieldWatcher);

                    }
                }
            }

            private Map<String, Object> watchContext(ArrayList<Component> s) {
                Map<String, Object> c = new HashMap<String, Object>();
                for (Object o : s) {
                    AttributeEditor ao = (AttributeEditor) o;
                    //////////////Log.p(ao.attribute.name.get());
                    c.put(ao.attribute.name.get(), ao.baseEditor);
                }
                return c;
            }

        });
        return serviceButton;
    }

    public ArrayList<Component> findAttributes(Container contentContainer) {
        ArrayList<Component> fa = new ArrayList<Component>();
        for (Component c : contentContainer.getChildrenAsList(true)) {
            if (c instanceof Container) {
                if (c.getClientProperty("attribute") != null) {
                    fa.add(c);
                } else {
                    fa.addAll(findAttributes((Container) c));
                }
            } else {
                if (c.getClientProperty("attribute") != null) {
                    fa.add(c);
                }
            }
        }
        return fa;
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
        ////System.out.println("Processing --> " + service.getPropertyIndex().toString());
        Container attributePanel = new Container();
        attributePanel.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        attributePanel.setScrollableY(true);
        parameterPanel.put(service.name.get(), attributePanel);
        Boolean getService = false;
        for (Object ss : service.service_attributes) {
            if (ss.getClass().getCanonicalName().indexOf("String") > 0) {
                //it is a string, so we really need to retrieve the service all over again
                getService = true;
            }
        }
        String _id = service._id.get();
        if (getService) {
            service = ServerAPI.getService(_id);
        }
        //create tabs for each group of attributescontainer
        //first create the collection of groups
        //in the right order
        Vector groups = getGroupsOfAttributes(service.service_attributes);
        //groups now holds the list of groups and associated attributes
        //in proper order. So if we take anyone we can properly create its tab
        //if there are no groups, each attribute is a group of its own and will
        // be in its own tab
        createTabsForAttributesGroups(groups, contentContainer);
        //having created tabs, now go through the tabs one by one and add
        //the components for each. This is necessary since the attributes are 
        //not ordered
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
        attr = new AttributeEditor(serviceAttribute, true);
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
        b.setUIID("SmallLabel");
        b.setText(ref);
        b.getAllStyles().setBorder(Border.createEtchedRaised());
        return b;
    }

    private Button makeParameterButton(ServiceAttribute attr) {
        //extract the logo

        ////System.out.println("Attribute " + attr.name.get());
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
        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_SAVE);
        FloatingActionButton save = fab.createSubFAB(FontImage.MATERIAL_SAVE, "");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // create the request from the display
                // call the save function which should determine how the 
                //object will be saved.
                //ToastBar.showInfoMessage("Saving ...");
                
                saveRequestFromContainer(contentContainer);

            }
        });
        /*
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
         */
        return fab.bindFabToContainer(cnt, Component.RIGHT, Component.TOP);
    }

    public void saveRequestFromContainer(Container cnt) {
        //go over container
        //take 

        //this.request now holds the request
        // the actual saving should be done by the request.
        //////////////Log.p("Save Request " + this.request.getPropertyIndex().toString());
        Service service = (Service) cnt.getClientProperty("service");        //service.refresh();

        if (service != null) {
            updateRequestFromContainer(cnt);
            Dialog dlg = new Dialog();
            dlg.setLayout(new BorderLayout());
            Request p = this.request;
            HashMap m = new HashMap();
            m.put("save", false);
//

            if (this.request.service.size() < 1) {
                service.refresh();
                this.request.service.add(service);
            }
            this.request.makeReadyForSaving();
//
            ArrayList<String> errors = this.request.validateRequest();

            if (errors.size() > 0) {
                String err = " Correction needed\n";
                for (String s:errors){
                    err = err+s+"\n";
                }
                ToastBar.showErrorMessage(err, 10);
                return;
            }
            //Log.p(this.request.service.get(0).name.get());
            dlg.add(CENTER, NewsfeedContainer.createSimpleNewsItem(p.ziemozi_user.get(0), this.request));
            Container saveControl = new Container();
            Button b = new Button("Save");
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    //p.saveLocal(); 
                    m.put("save", true);
                    dlg.dispose();
                }
            });
            Button c = new Button("Cancel");
            c.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    m.put("save", false);
                    dlg.dispose();
                }
            });
            saveControl.add(b).add(c);
            dlg.add(NORTH, saveControl);
            dlg.isDisposeWhenPointerOutOfBounds();
            dlg.show(0, 0, 0, 0);
            if (this.request.service.size() < 1) {
                service.refresh();
                this.request.service.add(service);
            }
            ////////Log.p("This Request " + this.request.getPropertyIndex().toJSON());
            if (Boolean.parseBoolean(m.get("save").toString())) {
                dlg.dispose();
                this.request.saveLocal();
            MainForm main = new MainForm();
            main.refresh();
            main.show();
            }else{
                //not saved
                //just return to the view we are in
                dlg.dispose();
                ToastBar.showInfoMessage("Request Not Saved");
            }
        } else {

            ToastBar.showInfoMessage("Create a message first");
        }
    }

    public void saveRequestFromDefaultContainer() {
        //assume the default container is contentContainer
        saveRequestFromContainer(contentContainer);
    }

    public Request getRequest() {
        updateRequestFromContainer();
        return this.request;
    }

    public void updateRequestFromContainer(Container cnt) {
        //retrieve the service definition
        //create the request based on the requestComponents of the container
        Service service = (Service) cnt.getClientProperty("service");
        if (service == null) {
            //most likely nothing to save
            ToastBar.showInfoMessage("Ziem ozi first");
        } else {
            service.refresh();
            Request req = new Request();
            req.service.add(service);
            req.createRequest();
            req.parent.set((Request) cnt.getClientProperty("parent"));
            //request.provider.set((Provider) cnt.getClientProperty("provider"));
            //request.service.set(service);//if service is null then it is null.

            //if we are creating then there are a few things that we need to get but we will assume they 
            //are there
            //we have things like priority, etc.
            req.request_parameters.clear();
            ArrayList<Component> coms = findAttributes(contentContainer);
            for (Component c : coms) {
                //Log.p("\n\nComponent to extract " + c.getName());
                AttributeEditor a = (AttributeEditor) c;
                RequestParameter r = a.getAttributeValue();
                //Log.p("Attributes " + r.service_attribute.size());
                if ((r != null) && (r.service_attribute.size() > 0)) {
                    ServiceAttribute s = r.service_attribute.get(0);
                    if (s != null) {
                        req.request_parameters.add(r);
                        //Log.p("Inside rquest parameter "
                                //+req.request_parameters.get(0).service_attribute.size());
                        ////Log.p("\n\nService Attribute" + s.name.get() + " has value " + r.value.get() + "\n");
                        //is From Request Parameter " + r.getPropertyIndex().toString());
                    }
                }
            }

            this.request = req;

        }
    }

    public void updateRequestFromContainer() {
        updateRequestFromContainer(contentContainer);
    }

    private void addToContentContainer(Service service) {

        //create a panel which is just a container
        //but from the service attributes
        ////System.out.println("Processing --> " + service.getPropertyIndex().toString());
        contentContainer.putClientProperty("service", service);
        for (Object sattr : service.service_attributes) {
            //for each service attribute
            //add a button to the container
            //so we will have one button for each. IF it is a nested we need to worry
            //also we need to keep them in sequence
            //and also worry about the type. We should get the icon from the type?
            // it will be good not to put types we dont know about 
            ServiceAttribute sa = (ServiceAttribute) sattr;
            ServiceAttribute serviceAttribute = (ServiceAttribute) sattr;
            ServiceAttribute withAttributeType = (ServiceAttribute) sattr;
            Map attributeMap = sa.getPropertyIndex().toMapRepresentation();
            //////System.out.println("Map is --> " + sattr.toString());
            /**
             * try {
             * withAttributeType.getPropertyIndex().populateFromMap((HashMap)
             * sattr, ServiceAttributeType.class); }catch(Exception e){
             * serviceAttribute.getPropertyIndex().populateFromMap((HashMap)
             * sattr); }
             * //serviceAttribute.getPropertyIndex().populateFromMap((HashMap)sattr,
             * ServiceAttributeType.class);
             */
            //
            ////System.out.println("Attribute Type " + withAttributeType.getPropertyIndex().toJSON());
            //////System.out.println(" Attribute Type " + withAttributeType.type_of_attribute.get(0).name.get());
            //ServiceAttributeType attributeType = withAttributeType.type_of_attribute.get(0);
            //////System.out.println(" Attribute Type extracted"
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

    private Component createTabsForAttributesGroups(Vector groups, Container contentContainer) {
        //we build a tab for each group
        //add then when a parameter button is clicked, it has to
        //go to that tab or the tab named for its group
        Tabs t = new Tabs();
        t.setUIID("TabZ");
        t.getTabsContainer().setUIID("TabZ");
        t.getTabsContainer().getAllStyles().setMarginBottom(10);
        t.setTabPlacement(Component.TOP);
        for (Object group : groups) {
            HashMap gm = (HashMap) group;
            t.addTab(getTabTitle(gm), createTab(gm));
        }
        //we have instantiated all the components
        //now we should setup watchers and broadcasters
        //each component that is watching is setup as observer
        //for the observable it is watching

        return t;
    }

    private Container createTab(HashMap gm) {
        //gm holds the tab as well as the 
        //first create a tab for this group which might
        //just be a single attribute
        Container tab = new Container();
        tab.setUIID("TabZ");
        tab.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        tab.setName(getTabTitle(gm));

        //Label l = UIUtils.createHalfSpace();
        //l.setName("Space");
        //tab.add(l);
        //tab.add(new Label(""));//this puts a gap between the header and the rest;
        //
        HashMap gms = sortAttributeMap(gm);
        Set attributeKeys = gms.keySet();
        //add dummy components which we can swapout later
        ////////////Log.p("Number of attributes to tab "+attributeKeys.size());
        addDummyComponents(tab, attributeKeys.size());
        for (Object k : attributeKeys) {

            ServiceAttribute attribute = (ServiceAttribute) gms.get(k);
            if (attribute._id.get() != null) {

                ////////Log.p("\n\n----\n\nThe service attribute before refresh "
                //       + attribute._id);                
                if (attribute.type_of_attribute.size() <= 0) {
                    ////////Log.p("Refresh Type of Attribute");
                    attribute.refreshAttribute();
                }
                ////////Log.p("\n\n----\n\nThe service attribute " +  attribute._id);
                //Component attr = editAttribute(attribute);
                //$(attr).addTags("attribute");
                Component attr = $(editAttribute(attribute)).addTags("attribute").asComponent();
                ////////////Log.p("Components " + $(".attribute").size());
                attr.putClientProperty("attribute", "yes");
                attr.setName(k.toString());
                ////Log.p("Attribute Name " + k.toString() + attribute.display_sequence.get()) ;
                try {
                    tab.addComponent(getAttributePosition(
                            Integer.parseInt(attribute.display_sequence.get()), tab), attr);
                } catch (Exception e) {
                    //Log.p(e.getMessage());
                    tab.add(attr);
                }
            }
        }//
        $(".Dummy").remove();
        int i = 0;
        while (i < tab.getComponentCount()) {

            Component c = tab.getComponentAt(i);
            ////////////Log.p(c.getName());
            if (c.getName().startsWith("Dummy")) {
                tab.removeComponent(c);
                i--;
            }
            i++;
        }
        //tab.addComponent(0, new Label("________________________________________________________"));
        tab.repaint();
        return tab;
    }
    private static HashMap sortAttributeMap(HashMap map)
    {
        List list = new LinkedList(map.entrySet());
        //Custom Comparator
        Collections.sort(list, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                int rtval =0;
                ServiceAttribute s1 = (ServiceAttribute) ((Map.Entry) (o1)).getValue();
                ServiceAttribute s2 =  (ServiceAttribute) ((Map.Entry) (o2)).getValue();
                String ss1 = s1.display_sequence.get();
                if ( ss1.contains(".") ) ss1= s1.display_sequence.get().substring(0,
                        s1.display_sequence.get().indexOf(".")-1);
                String ss2 = s2.display_sequence.get();
                if ( ss2.contains(".")) ss2 = s2.display_sequence.get().substring(0,
                        s2.display_sequence.get().indexOf(".")-1);

                rtval = Integer.parseInt(ss1) -
                        Integer.parseInt(ss2);
                return rtval;
            }
        });
        //copying the sorted list in HashMap to preserve the iteration order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    private int getAttributePosition(int sequence, Container t) {
        //Log.p("Number of Components is " + t.getComponentCount());
        int i = 0;
        if (sequence <= t.getComponentCount()) {
            i = sequence;
        }
        if (sequence > t.getComponentCount()) {
            i = t.getComponentCount();
        }
        //Log.p("Sequence is " + sequence + "; Modified is " + i);
        return i;
    }

    private void addDummyComponents(Container tab, int size) {
        int i = 0;
        while (i < size) {
            SpanLabel l = new SpanLabel("---");
            l.setName("Dummy" + i);
            $(l).addTags("Dummy");
            tab.add(l);
            i++;
        }
    }

    private String getTabTitle(HashMap gm) {
        String tabTitle = "General";
        Set attributeKeys = gm.keySet();
        for (Object k : attributeKeys) {
            ServiceAttribute attribute = (ServiceAttribute) gm.get(k);
            if (attribute.display_group.get() == null) {
                return tabTitle;
                // return attribute.display_label.get();
            }
            if (!attribute.display_group.get().isEmpty()) {
                return attribute.display_group.get();
            } else {
                //return attribute.name.get();
                return tabTitle;
            }

        }
        return tabTitle;
    }

    private Vector getGroupsOfAttributes(ListProperty<ServiceAttribute, Service> service_attributes) {
        Vector vgroups = new Vector<HashMap>();
        HashMap defaultGroupMap = new HashMap();
        vgroups.add(defaultGroupMap);
        for (Object sattr : service_attributes) {

            ServiceAttribute sAttribute = (ServiceAttribute) sattr;
            String grp = sAttribute.display_group.get();
            ////////Log.p(grp + " --> " + sAttribute.name.get());
            if (grp == null) {
                //this goes into a new group all by itself
                //actually should go into the defaultGroup

                //HashMap gm = new HashMap();
                defaultGroupMap.put(sAttribute.name.get(), sAttribute);
                //vgroups.add(sAttribute.display_sequence.get(),gm);
            } else {
                if (grp.isEmpty()) {
                    //this goes into a new group all by itself
                    //into the default group
                    //HashMap gm = new HashMap();
                    defaultGroupMap.put(sAttribute.name.get(), sAttribute);
                    //vgroups.add(gm);                    
                } else {
                    //we have to check if this group already exists
                    //if it exists we just add the attribute to it
                    HashMap g = groupIn(vgroups, sAttribute.display_group.get());
                    if (g != null) {
                        g.put(sAttribute.name.get(), sAttribute);
                    } else {
                        //no group found so we create a new group
                        g = new HashMap();
                        g.put(sAttribute.name.get(), sAttribute);
                        try {
                            vgroups.add(Integer.parseInt(sAttribute.display_sequence.get()), g);
                        } catch (Exception e) {
                            vgroups.add(g);
                        }
                    }

                }
            }
        }
        return vgroups;
    }

    private HashMap groupIn(Vector vgroups, String get) {
        for (Object v : vgroups) {
            //each object in vgroups is a hashmap
            //we check through and see if any has the same group
            HashMap h = (HashMap) v;
            Set k = h.keySet();
            for (Object o : k) {
                ServiceAttribute s = (ServiceAttribute) h.get(o);
                //s.refreshAttribute();
                //////////////Log.p(s.getPropertyIndex().toString());
                //////////////Log.p("Display Group " + s.display_group.get());
                if (s.display_group.get() != null) {
                    if (s.display_group.get().equalsIgnoreCase(get)) {
                        //we have found the group
                        return h;
                    }
                }
            }

        }
        return null;
    }

}
