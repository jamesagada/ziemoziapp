/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

/**
 *
 * @author jamesagada We use this to display a propertybusinessobject by
 * creating a servicedefinition with serviceAttributes and ServiceTypes and then
 * marshalling it into a request object
 */
import com.ixzdore.restdb.ziemobject.Provider;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.properties.ListProperty;
import com.codename1.properties.PropertyBase;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.properties.UiBinding;
import com.codename1.ui.ButtonGroup;
import com.codename1.ui.CheckBox;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.RadioButton;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.spinner.Picker;
import com.codename1.ui.table.TableLayout;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.local.localAPI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Instant UI generates a user interface for editing a property business object
 * based on common conventions and settings within the properties. UI's are
 * automatically bound and work seamlessly.
 * <strong>Important</strong>: These UI's are subject to change, e.g. a
 * generated UI might not have validation for a specific property in one build
 * and might introduce it in an update. We try to generate great UI's seamlessly
 * and some improvements might break functionality.
 *
 * @author Shai Almog
 */
public class PropertyBusinessObjectUI {

    Service businessObjectDefinition = new Service();
    Request businessObject = new Request();

    /**
     * Excludes the property from the generated UI
     *
     * @param exclude the property to exclude
     */
    public void excludeProperty(PropertyBase exclude) {
        exclude.putClientProperty("cn1$excludeFromUI", Boolean.TRUE);
    }

    /**
     * Returns true if the property was excluded from the GUI
     *
     * @param exclude the property
     * @return true if the property was excluded from the GUI
     */
    public boolean isExcludedProperty(PropertyBase exclude) {
        return exclude.getClientProperty("cn1$excludeFromUI") == Boolean.TRUE;
    }

    /**
     * A property that's a multi-choice can use this API to define the options
     * used e.g.: * {@code
     * iui.setMultiChoiceLabels(c.gender, "Male", "Female", "Undefined");
     * iui.setMultiChoiceValues(c.gender, "M", "F", "U");
     * }
     *
     * @param p the property
     * @param labels label for each option
     */
    public void setMultiChoiceLabels(PropertyBase p, String... labels) {
        p.putClientProperty("cn1$multiChceLbl", labels);
        if (p.getClientProperty("cn1$multiChceVal") == null) {
            p.putClientProperty("cn1$multiChceVal", labels);
        }
    }

    /**
     * A property that's a multi-choice can use this API to define the options
     * used, notice that this API won't work correctly without
     * {@link #setMultiChoiceLabels(com.codename1.properties.PropertyBase, java.lang.String...)}
     *
     * @param p the property
     * @param values actual values used for each label
     */
    public void setMultiChoiceValues(PropertyBase p, Object... values) {
        p.putClientProperty("cn1$multiChceVal", values);
    }

    /**
     * The component class used to map this property
     *
     * @param p the property
     * @param cmpCls class of the component e.g. {@code Button.class}
     */
    public void setComponentClass(PropertyBase p, Class cmpCls) {
        p.putClientProperty("cn1$cmpCls", cmpCls);
    }

    /**
     * Sets the text field constraint for the property explicitly, notice that
     * some constraints are implicit unless set manually e.g. numeric for
     * numbers or password for fields with password in the name
     *
     * @param p the property
     * @param cons the text field constraint
     */
    public void setTextFieldConstraint(PropertyBase p, int cons) {
        p.putClientProperty("cn1$tconstraint", cons);
    }

    /**
     * The text field constraint for the property. notice that some constraints
     * are implicit unless set manually e.g. numeric for numbers or password for
     * fields with password in the name
     *
     * @param p the property
     * @return the constraint matching this property
     */
    public int getTextFieldConstraint(PropertyBase p) {
        Integer v = (Integer) p.getClientProperty("cn1$tconstraint");
        if (v != null) {
            return v;
        }

        Class t = p.getGenericType();
        if (t != null) {
            if (t == Integer.class || t == Long.class || t == Short.class || t == Byte.class) {
                return TextArea.NUMERIC;
            }
            if (t == Double.class || t == Float.class) {
                return TextArea.DECIMAL;
            }
        }
        String n = p.getName().toLowerCase();
        if (n.indexOf("password") > -1) {
            return TextArea.PASSWORD;
        }
        if (n.indexOf("url") > -1 || n.indexOf("website") > -1 || n.indexOf("blog") > -1) {
            return TextArea.URL;
        }
        if (n.indexOf("email") > -1) {
            return TextArea.EMAILADDR;
        }
        if (n.indexOf("phone") > -1 || n.indexOf("mobile") > -1) {
            return TextArea.PHONENUMBER;
        }
        return TextArea.ANY;
    }

    /**
     * Creates editing UI for the given business object
     *
     * @param bo the business object
     * @param autoCommit true if the bindings used should be auto-committed
     * @return a UI container that can be used to edit the business object
     */
    public Container createEditUI(PropertyBusinessObject bo, boolean autoCommit) {
        //////log.p(bo.getPropertyIndex().toString());
        Container cnt;
        if (Display.getInstance().isTablet()) {
            TableLayout tl = new TableLayout(1, 2);
            tl.setGrowHorizontally(true);
            cnt = new Container(tl);
        } else {
            cnt = new Container(BoxLayout.y());
        }
        //Get list of properties
        for (PropertyBase b : bo.getPropertyIndex()) {
            if (isExcludedProperty(b)) {
                continue;
            }
            //for each property oblect, we need to determine the type and make it into a ServiceAttribute
            //question is what if the property is a list property
            //then we need to determine what the property is really
            //////log.p(b.getName());
            ServiceAttribute sa = makeServiceAttributeFromProperty(b);
            //////log.p("Before Setting Value \n" + bo.getPropertyIndex().toString());
            ////////log.p("BO business object \n" + bo.getPropertyIndex().);
            //HashMap m = (HashMap) bo.getPropertyIndex().toMapRepresentation();       
            String r = getValueOfProperty(bo, b);
            //////log.p("Value " + r);
            sa.default_value.set(getValueOfProperty(bo, b));
            businessObjectDefinition.service_attributes.add(sa);
            //////////log.p(businessObjectDefinition.getPropertyIndex().toString());
            RequestParameter rp = makeRequestParameterFromProperty(sa, b);
            businessObject.request_parameters.add(rp);
        }
        businessObjectDefinition.name.set(bo.getPropertyIndex().getName());
        businessObjectDefinition.description.set(bo.getPropertyIndex().getName());
        // businessObject.service.set(businessObjectDefinition);
        businessObject.service.add(businessObjectDefinition);
        ////////log.p("Object To Display" + businessObjectDefinition.getPropertyIndex().toString());
        ZiemView zv = new ZiemView();
        zv.request = businessObject;
        zv.showPanels = false;
        zv.showFloatingButton = false;
        cnt = zv.createRequestView(businessObjectDefinition);
        return cnt;
    }

    /**
     * Returns the Binding object for the given container which allows us
     * control over the widgets and their commit status
     *
     * @param cnt the container returned by the {@link #createUI(boolean)}
     * method
     * @return a binding object
     */
    public UiBinding.Binding getBindings(Container cnt) {
        return (UiBinding.Binding) cnt.getClientProperty("cn1$iui-binding");
    }

    private ServiceAttribute makeServiceAttributeFromProperty(PropertyBase b) {
        ServiceAttribute sa = new ServiceAttribute();
        sa.name.set(b.getName());
        sa.display_label.set(b.getLabel());
        sa.multiplicity.set(Boolean.FALSE);
        sa.maximum_size.set("80");
        sa.required.set(true);

        sa.type_of_attribute.add(
                makeServiceAttributeTypeFor(
                        b));
        makeAttributeOptions(sa, b);
        ////////log.p("PBOView Service Attribute" + sa.getPropertyIndex().toString());
        return sa;
    }

    private RequestParameter makeRequestParameterFromProperty(ServiceAttribute sa, PropertyBase b) {
        RequestParameter rp = new RequestParameter();
        rp.service_attribute.add(sa);
        return rp;
    }

    private ServiceAttributeType makeServiceAttributeTypeFor(PropertyBase p) {
        ServiceAttributeType st = new ServiceAttributeType();
        String typeName = "Text";
        String objectType = "";
        Class genericType = p.getGenericType();
        int t = this.getTextFieldConstraint(p);
        if (genericType != null) {
            typeName = genericType.getName();
            //we check here
            //if it is a collection then it has to be a selection
            //if it is a ziemview object then we have to have a recursion
            //We have to have a generic viewer that will work for any tyoe it does not understand.
            //we look into the custom type 
            if (p.getClientProperty("ziem-type") != null) {
                //we have a ziem-type property
                typeName = (String) p.getClientProperty("ziem-type");
                ////////log.p("ziem-type is " + typeName);
            } else {
                //retrieve the last 
                String bType = typeName;
                String[] tt = Util.split(typeName, ".");
                typeName = tt[tt.length - 1];
                //////log.p("Decoded type " + typeName);
                //We 
                if (typeName.indexOf("long") >= 0) {
                    typeName = "Number";
                }
                if (typeName.indexOf("Long") >= 0) {
                    typeName = "Number";
                }
                if (bType.indexOf("ixzdore") >= 0) {
                    //this is now most likely a list of a given type
                    //The list will be pointing to preferably another business object
                    //so we will render it as a businessobjecteditor which will
                    //allow us to pick from predefined business objects
                    //So it will be rendered as a SingleSelect list of the particular business
                    //object
                    objectType = typeName;
                    typeName = "MultiObject";

                    //let us add the option list to it here
                    //should actually be from the default_value
                    HashMap m = new HashMap();
                    m.put("option_list", getObjectSelectionList(p));
                    p.putClientProperty("ziem-property-options", m);

                }
            }
        } else {

            switch (t) {
                case TextArea.NUMERIC:
                    ////////log.p("Numeric");
                    typeName = "Number";
                    break;
                case TextArea.DECIMAL:
                    ////////log.p("Numeric");
                    typeName = "Number";
                case TextArea.PASSWORD:
                    ////System.out.println("PassWord");
                    typeName = "Text";
                    break;
                case TextArea.PHONENUMBER:
                ////System.out.println("Phone");
                case TextArea.URL:
                ////System.out.println("URL");
            }
        }
        if (p.getClientProperty("ziem-type") != null) {
            //we have a ziem-type property
            typeName = (String) p.getClientProperty("ziem-type");
            ////////log.p("ziem-type is " + typeName);
        }

        st.name.set(typeName);
        st.description.set(typeName);
        st.surveyjs_type.set(objectType);
        ////////log.p(p.getName() +" " + typeName);
        ////////log.p(typeName);
        // ////////log.p("PBOView Service Attribute Type" + st.getPropertyIndex().toString());

        return st;
    }

    private void makeAttributeOptions(ServiceAttribute sa, PropertyBase b) {
        //we setup the rest of the attributes 
        //////log.p(b.getName());
        HashMap options = (HashMap) b.getClientProperty("ziem-property-options");

        if (options != null) {
            localAPI.showMapp(options);
            //int mx = Integer.parseInt(options.get("maximum_size").toString());
            if (options.get("maximum_size") != null) {
                sa.maximum_size.set(((options.get("maximum_size").toString())));
            }
            if (options.get("minimum_size") != null) {
                sa.minimum_size.set((options.get("minimum_size").toString()));
            }
            if (options.get("option_list") != null) {
                sa.option_list.set((String) options.get("option_list"));
            }
            if (options.get("mhelp_text") != null) {
                sa.help_text.set((String) options.get("help_text"));
            }
        }
        //if this type is a propertybusinessobject 
        //then we have to set the options from
        //the contents of the propertybase which in this case is really a list and the 
        //selected members 
    }

    private String getValueOfProperty(PropertyBusinessObject bo, PropertyBase b) {
        //we need to determine what the value of the property is given the property
        String pValue = "";
        ////////log.p("BO business object \n" + bo.getPropertyIndex().);
        //bo.getPropertyIndex().
        //////log.p("BO From propertyindex \n" + bo.getPropertyIndex().get(b.getName().toString()));
        //HashMap m = (HashMap) bo.getPropertyIndex().toMapRepresentation();
        PropertyIndex m = bo.getPropertyIndex();
        //////log.p("BO From propertyindex \n" + bo.getPropertyIndex().get(b.getName().toString()));
        //////log.p("BO Type From propertyindex \n" + bo.getPropertyIndex().get(b.getName().toString()));
        if (m.get(b.getName()) != null) {
            Class c = b.getGenericType();
            if (c == null) {
                //////log.p("Class property " + m.get(b.getName()));
                //////log.p("Cannonical " + b.getClass().getCanonicalName());
                //if the type is a list then we have to 
                pValue = m.get(b.getName()).toString();
                //if (b.getClass().getCanonicalName().contains("List")) {
                //                       pValue = m.get(b.getName()).toString();
                //   ArrayList cc = (ArrayList) m.get(b.getName());
                //  for (Object o : cc) {
                //      pValue = pValue + ";" + o.toString();
                // }
            } else {
                pValue = m.get(b.getName()).toString();
                //pValue =  m.get(b.getName());
            }
        } else {
            // it is not one of the generic types that can be cast into string
            //and we now have to know how to get the values
            //we may also need to do some conversions
                        Class c = b.getGenericType();
            if (c.getName().indexOf("long") > 0) {
                pValue = String.valueOf(m.get(b.getName()));
            }
            //what if it is a list, how will it be represented?
            //////log.p("Getting Value of " + c.getName());
            //////log.p(b.getClass().getName());
            if (c.getName().indexOf("ixzdore") >= 0) {
                //this is a reference and in this case a multilist
                //the question then is how does a multilist display
                //we leave that up to the multilist to display it
                //in that case we just give a list of _ids or 
                //json rep of the objects. Then setup options will
                //Or we have a multilistobject viewer
                //this a propertyobject we know that should have name and description
                //we just return the list of items as a json array
                // ArrayList a = bo.getPropertyIndex().get(b.getName());

                ListProperty l = (ListProperty) b;
                for (Object o : l) {
                    //if there is an object then we need to convert it to a po
                    PropertyBusinessObject po = (PropertyBusinessObject) o;
                    pValue = po.getPropertyIndex().toJSON() + ",";
                }
                if (pValue.length() > 2) {
                    pValue = "[" + pValue.substring(0, pValue.length() - 2) + "]";
                }

            }
        }
    //}
    return pValue ;
}

public void updatePropertyBusinessObject(Container cnt, PropertyBusinessObject po) {
        ZiemView zv = (ZiemView) cnt.getClientProperty("ziemView");
        if (zv != null) {
            Request request = zv.getRequest();
            if (request != null) {
                //we now have to match the po to the request object.
                //////////log.p("Saved " + request.getPropertyIndex().toString());
                //we have to extract value from the request and put it in the 
                //propertybusinessobject
                Object[] rps = request.request_parameters.asList().toArray();
                //each object here contains the request parameter and its value
                //so we know the name and the value

                for (Object r : request.request_parameters.asList()) {
                    RequestParameter rp = (RequestParameter) r;
                    assignPropertyValue(po, rp);
                }

            }
        }
    }

    private void assignPropertyValue(PropertyBusinessObject po, RequestParameter rp) {
        //given request parameter rp
        //find corresponding propertyobject and set its value
        HashMap m = (HashMap) po.getPropertyIndex().toMapRepresentation();
        HashMap p = (HashMap) rp.getPropertyIndex().toMapRepresentation();

        // ////////log.p(rp.getPropertyIndex().toString());
        String parameter = rp.service_attribute.get(0).name.get();// name of the parameter
        // ////////log.p(parameter);
        String value = rp.value.get();
        //we need to convert the value based on the type of the property
        //for instance image, date, long, numeric
        if (parameter != null) {
            if (value != null) {
                m.remove(parameter);
                m.put(parameter, value);
            }
        }
        po.getPropertyIndex().populateFromMap(m);
        // ////////log.p("Assigned PropertyBusinessObject \n" + po.getPropertyIndex().toString());

    }

    private String getObjectSelectionList(PropertyBase p) {

        String objectList = "";
        //////log.p("property base type " + p.getClass().getCanonicalName());
        ////log.p("Property Value " + p.toString());
         
        if (!p.toString().equalsIgnoreCase("NULL") ){
        ListProperty l = (ListProperty) p;
        for (Object o : l) {
            //if there is an object then we need to convert it to a po
            PropertyBusinessObject po = (PropertyBusinessObject) o;
            objectList = po.getPropertyIndex().toJSON() + "," + objectList;
            //////log.p("Objectlist " + objectList);
        }
        if (objectList.lastIndexOf(",") > 0) {
            objectList = "[" + objectList.substring(0, objectList.lastIndexOf(",")) + "]";
        } else {
            objectList = "[" + objectList + "]";
        }
        //////log.p("Objectlist " + objectList);
        }else {
            //if it is null, we need to just maake a list of all posssbile
            //we need to be sure that we can get the list of the items
            
        }
        return objectList;
    }
}
