package com.ziemozi.forms;

import com.codename1.components.InfiniteProgress;
import com.ixzdore.restdb.ziemobject.Post;
import static com.codename1.ui.CN.getCurrentForm;
import com.codename1.ui.Component;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.Form;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.tree.Tree;
import com.codename1.ui.tree.TreeModel;
import java.util.Vector;
import com.codename1.io.Log;
import com.codename1.ui.Dialog;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.ServerAPI;
import java.util.HashMap;

public class SelectServiceTableForm extends Form {

    public SelectServiceTableForm() {
        super("Report", BoxLayout.y());
        Dialog ip = new InfiniteProgress().showInifiniteBlocking();
        add(ServiceSelector());
        ip.dispose();
        Form previous = getCurrentForm();
        getToolbar().addMaterialCommandToLeftBar("",
                MATERIAL_ARROW_BACK, e -> previous.showBack());
    }

    private Component makeServiceSelector() {
        class StringArrayTreeModel implements TreeModel {
            
            String[][] arr = new String[][]{
                {"Categories", "Providers", "Services"},
                {"Civic", "Election", "Business"},
                {"Police", "LASG", "AKSG"},
                {"Crime", "Environment", "Banking"}
            };


            public Vector getChildren(Object parent) {
                if (parent == null) {
                    Vector v = new Vector();
                    for (int iter = 0; iter < arr[0].length; iter++) {
                        v.addElement(arr[0][iter]);
                    }
                    return v;
                }
                Vector v = new Vector();
                for (int iter = 0; iter < arr[0].length; iter++) {
                    if (parent == arr[0][iter]) {
                        if (arr.length > iter + 1 && arr[iter + 1] != null) {
                            for (int i = 0; i < arr[iter + 1].length; i++) {
                                v.addElement(arr[iter + 1][i]);
                            }
                        }
                    }
                }
                return v;
            }

            public boolean isLeaf(Object node) {
                Vector v = getChildren(node);
                return v == null || v.size() == 0;
            }
        }

        Tree dt = new Tree(new StringArrayTreeModel());
        return dt;
    }
    private Component ServiceSelector() {
        class ServiceSelectorTreeModel implements TreeModel {

            public Vector getChildren(Object parent) {
                 
                return ServerAPI.serviceSelector(parent);
            }

            public boolean isLeaf(Object node) {
                Vector v = getChildren(node);
                return v == null || v.size() == 0;
            }
        }

        Tree dt = new Tree(new ServiceSelectorTreeModel()) 
{
    @Override
    protected String childToDisplayLabel(Object child) {
        HashMap m = (HashMap)child;
        String n = (String)m.get("name");
        return n;
    }
};        
        dt.addLeafListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //call the ServicePostForm
                //which will create an array of services 
                //and  then display it in a service request form
                ////////log.p("Selected Object: " + dt.getSelectedItem());
               ZiemForm f = new ZiemForm(dt.getSelectedItem(),"New Report Request");
               f.show();
             }
        });
        return dt;
    }
}
