/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

//import ca.weblite.codename1.json.JSONException;
//import ca.weblite.codename1.json.JSONObject;
import com.codename1.io.Log;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.layouts.BoxLayout;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;

import java.util.HashMap;

//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 *
 * @author jamesagada
 */
public class AttributeEditor extends Container {

    private HashMap<String, BaseEditorImpl> editorFactory;
    public ServiceAttribute attribute;
    public ServiceAttributeType attributeType;
    public FieldWatcher fieldWatcher;
    public FieldBroadcast fieldBroadcast;
    public BaseEditorImpl baseEditor;
    HashMap<String,Object> editorClassFactory = new HashMap<String, Object>();
    
    public AttributeEditor(ServiceAttribute serviceAttribute, boolean editMode) {
        //what type of attribute is this
        //we have a set of standard attributes we support
        //Text, image, location, and we will simple determine which one to use
        //by using its class name?  
        //The preferred mode is that we have standard set of display components that we 
        //support like InstantUI only supports a few. And that is all. Any one we dont understand
        //we leave it out.
        //core components will therefore be Text,Image,Media,Location,List or they will be inside 
        //containers with similar names as the components
        //we can keep a HashMap that contains the name of the classes
        //HashMap can then be loaded from storage - these can be updated as needed
        //Each Display type will then have an entry
        this.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        ServiceAttributeType st;
        this.attribute = serviceAttribute;
        
        //////log.p("Setting up to show attribute " + attribute._id.get() + attribute.name.get());
        //List at = serviceAttribute.type_of_attribute.asList();
        ////////////log.p("\nService Attribute " + serviceAttribute.getPropertyIndex().toJSON());
        //////////////log.p("This is attribute type " + at.get(0));
        //////////////log.p(at.get(0).getClass().getName());
        //if (at.get(0).getClass().getName().indexOf("ServiceAttributeType") > 0) {
            //the type is serviceAttributeType
        //    st = (ServiceAttributeType)at.get(0);
        //}else {
        //st = new ServiceAttributeType();
        //st.getPropertyIndex().populateFromMap((Map) at);
        //}
        //////log.p("Service Attribute To Show " + serviceAttribute.getPropertyIndex().toString());
        if (serviceAttribute.type_of_attribute.size() <= 0)serviceAttribute.refreshAttribute();
        Log.p("Service Attribute To Show " + serviceAttribute.getPropertyIndex().toString());
        Object so = serviceAttribute.type_of_attribute.get(0);
        if (so.getClass().getCanonicalName().indexOf("String") > 0 ) {
            //this is a string and not a service type
            //we need to get the service type
            //so we ask the serviceAttribute to load the service type
            //////log.p("String Attribute Type " + so.toString());
            //serviceAttribute.refreshAttribute();
            //if this fails then what? the attribute type is unknow and we should actually return
            serviceAttribute.refreshTypeOfAttribute();
        }
        if (serviceAttribute.type_of_attribute.get(0).getClass().getCanonicalName().indexOf("String") < 0 ){
        st = serviceAttribute.type_of_attribute.get(0);
        ////System.out.println("AttributeType xxx " + st.getPropertyIndex().toJSON());
        initializeEditorFactory();
        attributeType = st;

        ////System.out.println("Attribute Type to be edited --> " + attributeType.getPropertyIndex().toJSON());
        //////log.p("Attribute Type Name is " + attributeType.name.get());
        //BaseEditorImpl cmpClass = editorFactory.get(attributeType.name.get());
        BaseEditorImpl cmpClass = null;
        Object cClass = editorClassFactory.get(attributeType.name.get());
            try {
                 cmpClass = (BaseEditorImpl)((Class)cClass).newInstance();
            } catch (Exception e){
                //////log.p(e.getMessage());
            }
           // ////log.p(cClass.getClass().getCanonicalName());
        if (cmpClass != null) {
            //cmp class will have all the attributes of the type
            //so we will have a base component
            
            cmpClass.serviceAttribute = serviceAttribute;
            cmpClass.serviceAttributeType = attributeType;
            cmpClass.setName(attributeType.name.get());
            //we need to modify to take care of multiple 
            cmpClass.editorMode = editMode;
            this.fieldBroadcast = cmpClass.fieldBroadcast;
            this.fieldWatcher = cmpClass.fieldWatcher;
            baseEditor = cmpClass;
            this.add(cmpClass.edit(serviceAttribute));
        }
        this.revalidate();
        this.repaint();
       }   
    }

    public RequestParameter getAttributeValue() {
        //retrieve the request parameter from the editor object
        RequestParameter r = null;
        
        int i = 0;
        ////////log.p("Component Count " + this.getComponentCount());
        while (i < this.getComponentCount()) {
            Component c = this.getComponentAt(i);
          if ( c.getClientProperty("attribute") != null ){
 //                      BaseEditorImpl b = (BaseEditorImpl) this.getComponentAt(i).getClientProperty("editor");
 //          
            ////////log.p("component " + c.getName());
          }
            ////////////log.p("AttributeEditor.getAttributeValue" + this.getComponentAt(i).getClass().getCanonicalName());
            if (this.getComponentAt(i).getClass().getName().endsWith("Container")) {
                BaseEditorImpl b = (BaseEditorImpl) this.getComponentAt(i).getClientProperty("editor");
                if (b != null) {
                    //////////////log.p(" AttributorGetAttributeValue - request parameter " + b.getRequestParameter().getPropertyIndex().toString());
                    //////////////log.p("AttributeGetAttribute.getAttributeValue - service Attribute " + b.serviceAttribute.getPropertyIndex().toString());
                    r = b.getRequestParameter();
                    ServiceAttribute s = b.getServiceAttribute();
                    ////////////log.p("Service Attribute "+ s.getPropertyIndex().toString());
                    r.service_attribute.clear();
                    r.service_attribute.add(s);
                    return r;
                    //ServiceAttribute sa = new ServiceAttribute();
                    //sa.getPropertyIndex().populateFromMap(
                    //        b.serviceAttribute.getPropertyIndex().toMapRepresentation());
                    //////////////log.p("Service Attribute " + sa.getPropertyIndex().toJSON());
                    
                    //r.service_attribute.set(b.serviceAttribute);
                    ////////////log.p("Retrieved Request Parameter \n" + r.getPropertyIndex().toJSON());
                   ////////////log.p("BaseEditor at Component number " + i); 
                }
                
                i++;
            }
        }
        return r;
    }

    private void initializeEditorFactory() {
        //load the editor factory
        initializeEditorClassFactory();
        /*
        editorFactory = new HashMap<String, BaseEditorImpl>();
        editorFactory.put("Number", new NumberEditor());
        editorFactory.put("TextArea", new TextAreaEditor());
        editorFactory.put("Text", new TextEditor());
        editorFactory.put("Date", new DateEditor());
        editorFactory.put("Image", new ImageEditor());
        editorFactory.put("Video", new MediaEditor());
        editorFactory.put("location", new LocationEditor());
        editorFactory.put("SingleSelectList", new SingleSelectionEditor());
        editorFactory.put("MultipleSelection", new MultipleSelectionEditor());
        editorFactory.put("Boolean", new BooleanEditor());
        editorFactory.put("Signature", new SignatureEditor());
        editorFactory.put("SelectAndNumber", new SelectAndNumberEditor());
        editorFactory.put("File", new FileEditor());  
*/
    }
    private void initializeEditorClassFactory() {
        //load the editor factory

        editorClassFactory.put("Number", NumberEditor.class);
        editorClassFactory.put("TextArea", TextAreaEditor.class);
        editorClassFactory.put("Text",   TextEditor.class);
        editorClassFactory.put("Date",  DateEditor.class);
        editorClassFactory.put("Image",  ImageEditor.class);
        editorClassFactory.put("Video",  VideoEditor.class);
        editorClassFactory.put("location",  LocationEditor.class);
        editorClassFactory.put("SingleSelectList",  SingleSelectionEditor.class);
        editorClassFactory.put("MultipleSelection",   MultipleSelectionEditor.class);
        editorClassFactory.put("MultiList",   MultiListEditor.class);   
        editorClassFactory.put("MultiObject",   MultiObjectListEditor.class);         
        editorClassFactory.put("Boolean",   BooleanEditor.class);
        editorClassFactory.put("Signature",   SignatureEditor.class);
        editorClassFactory.put("SelectAndNumber",   SelectAndNumberEditor.class);
         editorClassFactory.put("File",  FileEditor.class);  
         editorClassFactory.put("qrcode",QrcodeScanner.class);
         editorClassFactory.put("barcode",BarcodeScanner.class);
        editorClassFactory.put("SelectAndText",   SelectAndTextEditor.class);       
        editorClassFactory.put("Rank",RankEditor.class);
        editorClassFactory.put("TransportStop",TransportStop.class);   
               //editorClassFactory.put("Sensor",SensorScanner.class);  
               editorClassFactory.put("Sensor",LESensor.class);  
               editorClassFactory.put("Browser",BrowserEditor.class);
               editorClassFactory.put("TextAndNumber",TextAndNumberEditor.class);  
               editorClassFactory.put("Panel",PanelEditor.class);
               editorClassFactory.put("Audio",AudioEditor.class);
    }
        

}
