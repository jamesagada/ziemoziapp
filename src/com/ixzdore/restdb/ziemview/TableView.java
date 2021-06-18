/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.codename1.components.SpanLabel;
import com.codename1.io.CharArrayReader;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.PickerComponent;
import com.codename1.ui.TextField;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.table.Table;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.ServiceAttribute;
import com.ixzdore.restdb.ziemobject.ServiceAttributeType;
import com.ziemozi.server.local.localAPI;

import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.table.DefaultTableModel;

/**
 *
 * @author jamesagada The singleselectioneditor presents the list of items in
 * the options list of the attribute definition as a selection it is also
 * possible that the option list is defined by a url or that the list is
 * calculated
 */
public class TableView extends BaseEditorImpl {

    public final Container editContainer = new Container();
    public final TextField textField = new TextField(); //to be used for editing
    public final TextField numberField = new TextField();
    public final Container headerContainer = new Container();
    public final Label textLabel = new Label();
    public final Container panelContainer = new Container();
    public final Container tableContainer = new Container();
    public final Table tableField = new Table();
    //public final PickerComponent datePicker = PickerComponent.createDate(new Date()).label("Date");
    public final PickerComponent booleanPicker = PickerComponent.createStrings("");//this is the picker
    public final SpanLabel textView = new SpanLabel(); //to be used for view
    public String optionList = ""; //string delimited list
    public String[] optionArray;
        TableView selfRef;
    Component selfref;
    public TableView() {
        editContainer.putClientProperty("editor", this);
        editContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        panelContainer.setLayout(new BoxLayout(BoxLayout.Y_AXIS));       
        editContainer.getStyle().setBorder(Border.createBevelRaised());
        tableContainer.getStyle().setBorder(Border.createBevelRaised());
        //editContainer.setLayout(new GridLayout(3));        
        headerContainer.setLayout(new GridLayout(3));
        ////System.out.println("SelectAndNumber Editor\n");
        //numberField.setConstraint(NUMERIC);
        //numberField.setUIID("TabZ");
        textLabel.getStyle().setBorder(Border.createBevelRaised());
        selfRef = this;
        this.setUIID("TabZ");
        booleanPicker.setUIID("TabZ");
        selfref = this;
    }

    @Override
    public Container edit(ServiceAttribute attr) {
        // we are editing an attribute that does not exist
        // we just create the fiel
        
        this.serviceAttribute = attr; 
        createRequestParameter(attr.type_of_attribute.get(0));        
        optionList = attr.option_list.get();
        setUpFields();
        
        textLabel.setText(attr.display_label.get());
        textField.setHint(attr.description.get());
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
                            c.putClientProperty("attribute", "attribute");
                editContainer.getParent().getParent().addComponent(c);
                editContainer.getParent().getParent().revalidate();
                editContainer.getParent().getParent().repaint();
                ////////////Log.p(editContainer.getParent().getParent().toString());
            }
        
        });
           removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
               // //////////Log.p(editContainer.getParent().getParent().toString());
               selfref.setVisible(false);
               
                //////////Log.p("Remove this component \n"  + selfref);
                //////////Log.p("Container " + editContainer.getParent().getParent());
                //////////Log.p("Parent Container " + editContainer.getParent().getParent().getParent());
                editContainer.getParent().getParent().removeComponent(selfref);
                editContainer.getParent().getParent().revalidate();
                editContainer.getParent().getParent().repaint();
                editContainer.getParent().getParent().getParent().revalidate();                
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
        editContainer.add(headerContainer).add(tableField);
        editContainer.revalidate();
        editContainer.repaint();
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
        //we have to go through all the cells and extract the values
        // and return the requestparameters to gether
        ////Log.p("Getting RequestParameter");
        String value="[";
        List<Component> content = panelContainer.getChildrenAsList(true);
            for (Component c : content) {
                ////////////Log.p("\n\nComponent to extract " + c.getName());
                String v="";
                AttributeEditor a = (AttributeEditor) c;                
                RequestParameter r = null;
                r = a.getAttributeValue();
                if ((r != null) && (r.service_attribute.size() > 0)) {
                    ServiceAttribute s = r.service_attribute.get(0);
                    if (s != null) {
                        if ( value.length() > 2) value = value +","; 
                        value = value  + "{" + "'" + s.display_label.get() + "':'" + r.value.get() + "'}";
                        ////Log.p(value);
                    }
                }
            }
        value = value + "]";
        ////Log.p(value);
        this.requestParameter.value.set(value);
        return this.requestParameter;
    }

    @Override
    public void setEditorConstraints() {
        /**
         * //set the constraints //the constraints are standard constraints and
         * are specified in //the editorConstraints array. The constraints if
         * (editorConstraints.size() > 0) { //run through it //for the text
         * editor, the constraints are numerical TextArea constraints //and also
         * constraints for minimum_size and maximum_size for (Object c :
         * editorConstraints) { int c1 = textField.getConstraint(); int c2 =
         * Util.toIntValue(c); textField.setConstraint(c2 | c1); } } //set the
         * maximum size and minimum size //if
         * textField.setMaxSize(this.serviceAttribute.maximum_size.getInt());
         * //Text does not have a minimum but we can store the minimum_value
         * //in a validation context. //////System.out.println("Maximum Field Size
         * is " + textField.getMaxSize());
         */
    }
 

    private void setUpFields() {
        //we try to create the fields in the panel and add it here
        //the fields are specified in the options field of the component using
        //either jsonstructure or a xmlstructure - we will use a json structure
        //similar to jsonchema
        //
        //We find the name of the attribute or its id or servicename
        //then we use attribute editor to create it and add it to panelContainer
        //our json schema
        /*
        [{
        "attribute":"Location",
        "service":"EnuguGaming"},
        {"attribute":"Picture",
        "service":"Whatever"
        */
        //
        //we get the service 
        //so first thing is to parse the optionlist
////Log.p("OptionList is " + optionList);
JSONParser json = new JSONParser();
try {
    Reader r = new CharArrayReader(optionList.toCharArray());
    Map<String, Object> data = json.parseJSON(r);
    List<Map<String, Object>> content = (List<Map<String, Object>>)data.get("root");
    ////Log.p(content.toString());
    ArrayList<String> columnNames = new ArrayList<>();
    ArrayList<Object> cells = new ArrayList<>();
    HashMap<Integer,Object> rows = new HashMap<>();
    for(Map<String, Object> obj : content) {
       // //Log.p(obj.toString());
        String columnName = (String)obj.get("column");
        int row_id  = Integer.parseInt((String)obj.get("row"));
        ArrayList<Object> cells_a = new ArrayList<>();
        if (!columnNames.contains(columnName)){
            columnNames.add(columnName);
        }
        if (rows.get(row_id) == null) {
            cells_a.add(obj);
            rows.put(row_id,cells_a);
        }else {
            //we have an existing row, so we now have to convert it to an array

            cells_a.addAll((Collection)rows.get(row_id));
            cells_a.add(obj);
            rows.put(row_id,cells_a);
        }

    }
    //now we create the TableModel using the default table model
    DefaultTableModel tm = new DefaultTableModel(getStringArray(columnNames),
            makeTableRows(columnNames,rows));
    tableField.setModel(tm);
    tableField.setUIID("Tab");
    List<Component> tl = tableField.getChildrenAsList(true);
    for (Component c:tl){
        c.setUIID("SmallLabel");
    }

} catch(IOException err) {
    Log.e(err);
}
panelContainer.revalidate();
    }

    private Object[][] makeTableRows(ArrayList<String> columns, final HashMap<Integer, Object> rows) {
        //each row object
        //Log.p("Columns " + columns.size() + " Rows " + rows.size());
        Object[][] tableObject = new Object[rows.size()][columns.size()];
        for (String c:columns){
            int col = columns.indexOf(c);
            //Log.p("Column is " + c + " at " + col);
            Set rowset=rows.keySet();
            for (Object rowid:rowset) {
                //the value is the entire row
                //but we are interested in the particular coulmn
                int rownum = (int)rowid;
                if (rownum < rows.size()) {
                    ArrayList<Object> row_object = (ArrayList<Object>) rows.get(rowid);
                    for (Object cell_object : row_object) {
                        Map<String, Object> cell = (Map<String, Object>) cell_object;
                        String cell_column = (String) cell.get("column");
                        if (c.equalsIgnoreCase(cell_column)) {
                            //Log.p("Row " + rownum + " Col " + col);
                            tableObject[rownum][col] = cell.get("value");
                        }
                    }
                }
            }

        }

    return tableObject;
    }

    private Component getAttributeComponent(String service, String name) {
        
        //given service name and attribute name find it 
        Component attr = null;
        ServiceAttribute s = localAPI.getServiceAttributeByNameAndService(service,name);
       // //Log.p("Service Attribute " + s.getPropertyIndex().toJSON());
        if (s != null){
                     attr = new AttributeEditor(s, true);
        }
        return attr;
    }
   @Override
    public RequestParameter getRequestParameter() {
        return getFrom();
    }
    // Function to convert ArrayList<String> to String[]
    public static String[] getStringArray(ArrayList<String> arr)
    {

        // declaration and initialise String Array
        String[] str = new String[arr.size()];

        // ArrayList to Array Conversion
        for (int j = 0; j < arr.size(); j++) {

            // Assign each value to String array
            str[j] = arr.get(j);
        }

        return str;
    }
}
