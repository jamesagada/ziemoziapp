/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.codename1.ui.TextField;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;




public class BaseEditorImpl extends Component implements BaseEditor{
      public ServiceAttribute serviceAttribute = new ServiceAttribute();
    //the edited parameter is stored here//
    public RequestParameter requestParameter = new RequestParameter();
    //
    public ArrayList<Object> editorConstraints = new ArrayList<Object>();
    public String _id = "";
    public ServiceAttributeType serviceAttributeType = new ServiceAttributeType();
    public Boolean multiplicity = false;
    public Boolean required = false;
    public int  displaySequence = 0;
    public int minimumSize = 0;
    public int  maximumSize = 99;
    public  ArrayList<Object> optionList = new ArrayList<Object>();
    public  String _parent_id = "";
    public int child_attributes = 0;    
    public String name = "";
    public String description = "";
    public String helpText = "";
    public String displayLabel = "";  
    public String   actionUrl = "";  
    public String defaultValue = "";    
    public String displayGroup = "";
    public  ServiceAttribute  watchThisAttribute
            = new  ServiceAttribute();
    public String watchFormula = "";
    public boolean editorMode = true;
    public final Button helpButton = helpButton();
    public final Button requiredButton = new Button("*");
    public final Button addAnotherButton = anotherButton();
    public final Button removeButton = new Button("-");
    public FieldWatcher fieldWatcher = new FieldWatcher();
    public FieldBroadcast fieldBroadcast = new FieldBroadcast();
    public final Container editContainer = new Container();
    @Override
    public Container edit(ServiceAttribute attr) {
        requestParameter.value.set(attr.default_value.get());
        return null;
    }

    @Override
    public Container view(ServiceAttribute attr) {
        editorMode=false;
         this.serviceAttribute = attr;       
        return edit(this.serviceAttribute);
    }

    @Override
    public void setRelevance(Object relevance){
        editContainer.setEnabled(Boolean.parseBoolean(relevance.toString()));
        editContainer.setVisible(Boolean.parseBoolean(relevance.toString()));
    }
    @Override
    public void createRequestParameter(ServiceAttributeType serviceType) {
     }

    @Override
    public Container view(RequestParameter req) {
        editorMode=false;
        this.requestParameter = req;
        this.serviceAttribute = req.service_attribute.get(0);
        return edit(this.serviceAttribute);
    }

    @Override
    public Container edit(RequestParameter req) {
        //edit a given request parameter - retrieve the ServiceAttribute
        //and edit
        editorMode=true;
        this.requestParameter = req;
        this.serviceAttribute = req.service_attribute.get(0);
        this.requestParameter.value.set(this.serviceAttribute.default_value.get());
        return edit(this.serviceAttribute);
    }

    @Override
    public void checkWatch() {

    }

    @Override
    public RequestParameter getRequestParameter() {
        return requestParameter;
    }

    @Override
    public void executeWatch( BaseEditorImpl b) {
        //check if there is any watch function defined in the watch formula
        //if there is, then we put it into a javascript context
        //put in the values of the parameters and run it
        //the value it returns is then set to the value of this parameter and redisplayed.
     }

    @Override
    public void updateList() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setEditorConstraints() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public Button helpButton(){
        //create a help button
        Button h = new Button();
        h.setUIID("SmallLabel");
        h.setIcon(FontImage.createMaterial(FontImage.MATERIAL_HELP,
                h.getSelectedStyle()));
        h.getStyle().setAlignment(LEFT);
        return h;
    }
    public Button anotherButton(){    //create add button
        Button h = new Button();
        h.setUIID("SmallLabel");
        h.setIcon(FontImage.createMaterial(FontImage.MATERIAL_ADD_CIRCLE,
                h.getSelectedStyle()));
        h.setTextPosition(RIGHT);
        h.getStyle().setAlignment(RIGHT);
        h.setText(" ");
        return h;
    }    
    public Button removeButton(){    //create add button
        Button h = new Button();
        h.setIcon(FontImage.createMaterial(FontImage.MATERIAL_REMOVE_CIRCLE_OUTLINE,
                h.getSelectedStyle()));
        return h;
    }  
    @Override
    public void createRequestParameter() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ServiceAttribute getServiceAttribute() {
        return this.serviceAttribute;
    }
    @Override
    public void setupOptions(Object o){
         this.serviceAttribute.option_list.set(o.toString());  
        setUpOptions();       
    }
    @Override
    public void setValue(Object v){
        
    }
    @Override
    public void setUpOptions(){
        
    }
    @Override
    public void setSummary(){
        
    }
}
