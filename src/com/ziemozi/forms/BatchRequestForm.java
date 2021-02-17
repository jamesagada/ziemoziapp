package com.ziemozi.forms;

import ca.weblite.codename1.json.JSONArray;
import ca.weblite.codename1.json.JSONException;
import ca.weblite.codename1.json.JSONObject;
import com.codename1.components.InfiniteProgress;
import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.components.xmlview.DefaultXMLViewKit;
import com.codename1.components.xmlview.XMLView;
import com.codename1.ext.filechooser.FileChooser;
import com.codename1.io.CSVParser;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.EAST;
import static com.codename1.ui.CN.WEST;
import com.ixzdore.restdb.ziemobject.Post;
import static com.codename1.ui.CN.getCurrentForm;
import com.codename1.ui.CheckBox;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.PickerComponent;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.spinner.Picker;
import com.codename1.ui.table.DefaultTableModel;
import com.codename1.ui.table.Table;
import com.codename1.ui.table.TableModel;
import com.codename1.util.StringUtil;
import com.ixzdore.restdb.ziemobject.Request;
import com.ixzdore.restdb.ziemobject.RequestParameter;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemobject.ServiceContact;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.ServerAPI;
import com.ziemozi.server.local.localAPI;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BatchRequestForm extends Form {

    Service selectedService;
    String selectedFile;
    public final PickerComponent servicePicker = PickerComponent.createStrings("");
    public final ArrayList<Service> servicelist = localAPI.getServices();
    public final Label status = new Label("Waiting To Process");
        Form bForm = this;
        
    public BatchRequestForm() {
        super("Batch Upload ", BoxLayout.y());

        add(serviceAndSourceSelector());
        Form previous = getCurrentForm();
        getToolbar().addMaterialCommandToLeftBar("",
                MATERIAL_ARROW_BACK, e -> previous.showBack());
    }

    private Component serviceAndSourceSelector() {
        Container sSelector = new Container();

        //sselector will contain a picker to be able to pick the type of 
        Container chooseAndProcess = new Container();
        chooseAndProcess.setLayout(new GridLayout(2));
        chooseAndProcess.add(chooseService());
        chooseAndProcess.add(proceedOrCancel());
        chooseAndProcess.revalidate();
        //////log.p(chooseAndProcess.toString());
        sSelector.setLayout(new BoxLayout(BoxLayout.Y_AXIS));        
        sSelector.add(chooseAndProcess);
        sSelector.add(status);        
        sSelector.add(chooseFile());
        sSelector.revalidate();
        return sSelector;
    }

    private Component proceedOrCancel() {
        Container pc = new Container();
        Button b = new Button("Upload Batch");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //We already parsed the CSV
                //what we do is to go through the passed rows
                batchProcess(selectedFile);
                selectedFile = null;
            }
        });
        pc.add(b);
        pc.revalidate();
        return pc;
    }

    private void batchProcess(String file) {
        if (selectedFile == null) {
            ToastBar.showErrorMessage("No File Selected");
            return;
        }

        ToastBar.showInfoMessage("Processing ....");
        //Dialog ip = new InfiniteProgress().showInifiniteBlocking();        
        FileSystemStorage fs = FileSystemStorage.getInstance();
        try {
            InputStream fis = fs.openInputStream(file);

            CSVParser parser = new CSVParser();
            try {

                Reader r = new InputStreamReader(fis);
                String[][] data = parser.parse(r);
                processData(data);
            } catch (Exception err) {
                Log.e(err);
            }

        } catch (Exception ex) {
            Log.e(ex);
        }
        //ip.dispose();        
        ToastBar.showInfoMessage("Done ");

    }

    private Component chooseFile() {
        Container cf = new Container();
        Button c = new Button("Choose File");
        c.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (FileChooser.isAvailable()) {
                    FileChooser.showOpenDialog(".xls, .csv, text/plain", e2 -> {
                        if (e2 != null){
                        String file = (String) e2.getSource();

                        if (file == null) {
                            ToastBar.showInfoMessage("No file was selected");
                        } else {
                            String extension = null;
                            if (file.lastIndexOf(".") > 0) {
                                extension = file.substring(file.lastIndexOf(".") + 1);
                            }
                            if ("csv".equals(extension)) {
                                selectedFile = file;
                                FileSystemStorage fs = FileSystemStorage.getInstance();
                                try {
                                    InputStream fis = fs.openInputStream(file);
                                    //Parse the CSV and put into tree model
                                    CSVParser parser = new CSVParser();
                                    try {

                                        Reader r = new InputStreamReader(fis);
                                        String[][] data = parser.parse(r);
                                        TableModel tm = new DefaultTableModel(data[0], data);
                                        Table t = new Table(tm);
                                        cf.removeAll();
                                        c.setText("Chosen " + file);
                                        cf.add(c);
                                        cf.add(t);
                                        cf.revalidate();
                                    } catch (Exception err) {
                                        Log.e(err);
                                    }

                                } catch (Exception ex) {
                                    Log.e(ex);
                                }
                            } else {
                                ToastBar.showInfoMessage("Selected file " + file + " cannot be processed");
                            }
                        }
                        }
                    });
                }
            }
        });
        cf.add(c);
        cf.revalidate();
        return cf;
    }

    private Component chooseService() {
        Container x = new Container();
        
        Picker stringPicker = servicePicker.getPicker();
        stringPicker.setText("Choose Service");
        stringPicker.setType(Display.PICKER_TYPE_STRINGS);
        stringPicker.setStrings(serviceNames());
        String s=serviceNames()[0].toString();
        stringPicker.setSelectedString(s);
        Picker p = stringPicker;
        p.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                selectedService = serviceByName(servicePicker.getPicker().getSelectedString());
            }
        });
        x.add(servicePicker);
        x.revalidate();
        //////log.p(x.toString());
        return x;
    }

    private Service serviceByName(String name) {
        Service sbyName = null;
        for (Service s : servicelist) {
            if (s.name.get().equalsIgnoreCase(name)) {
                sbyName = s;
            }
        }
        return sbyName;
    }

    private String[] serviceNames() {
        //extract service names into a comma delimited array
        String serviceString = "";
        for (Service s : servicelist) {
            serviceString = serviceString + s.name.get() + ",";
        }
        //////log.p("Services " + serviceString);
        Object[] sa = StringUtil.tokenize(serviceString, ",").toArray();
        String[] optionArray = new String[sa.length +1];
        optionArray[0]="Choose A Service";
        for (int i = 1; i < sa.length+1; i++) {
            optionArray[i] = (String) sa[i-1];
            ////System.out.println("\nOption in OptionList " + opa[i]);
            ////////log.p(sa[i-1].toString());
        }
        return optionArray;
    }

    private void processData(String[][] data) {
        //each row represents a record
        //first row has the names of the 
        //we need to create request for the type of 
        //service we want
        
        TableModel tm = new DefaultTableModel(data[0], data);
        ArrayList<Request> requestList = new ArrayList<Request>();
        int records = tm.getRowCount();
        //the first row contains the column names
        int i =1;
        while (i < tm.getRowCount()){
            int k=0;
        Request rq = new Request();
        rq.service.add(selectedService);
        rq.createRequest();           
            while (k < tm.getColumnCount()){
              String value = tm.getValueAt(i, k).toString();
              String field = tm.getValueAt(0,k).toString();
              //////log.p(field + " -- " + value);
              for (RequestParameter rp:rq.request_parameters){
                  if (rp.service_attribute.get(0).name.get().equalsIgnoreCase(field)) {
                      rp.value.set(value);
                      //////log.p("Matching " + rp.service_attribute.get(0).name.get() + "--" + value); 
                  }                  
              }
              k++;
            }
            rq.saveLocalNoSync();
            ////log.p("Saved " + rq.summary.get());
            i++;
            String message = "Processed " + String.valueOf(i) + " out of " + String.valueOf(records) + "records.";
            status.setText(message);
            bForm.revalidate();
            //ToastBar.getInstance().setVisible(false);
            //ToastBar.showInfoMessage(message);
            //ToastBar.getInstance().setVisible(true);            
        }
    }
    
}
