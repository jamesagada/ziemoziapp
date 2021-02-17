package com.ziemozi.forms;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.ToastBar;
import com.codename1.io.Log;
import com.ixzdore.restdb.ziemobject.Post;
import static com.codename1.ui.CN.getCurrentForm;
import static com.codename1.ui.CN.log;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BoxLayout;
import com.ixzdore.restdb.ziemobject.Service;
import com.ixzdore.restdb.ziemview.ZiemView;
import com.ixzdore.restdb.ziemview.ZiemViewTab;
import com.ziemozi.server.ServerAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ZiemForm extends Form {
    
    Form previous;
    public ZiemForm(Object p,String title) {
        super(title, BoxLayout.y());
        add(createService(p));
        previous = getCurrentForm();
        getToolbar().addMaterialCommandToLeftBar("", 
            MATERIAL_ARROW_BACK, e -> previous.showBack());
    }
    private Component createService(String p) {
        //ZiemView zv = new ZiemView();
        //ToastBar.showInfoMessage("Loading ..");
        //InfiniteProgress ip = new InfiniteProgress();
        //Dialog id = ip.showInfiniteBlocking();
        ZiemViewTab zv = new ZiemViewTab();        
        ArrayList<Service> services = ServerAPI.serviceList(p);
        Component cs = zv.createRequestView(services);
        //id.dispose();
        return cs;
    }
    private Component createService(Object p) {
        //we don't really know what p is. it can be a service or a service group or\
        if (p.getClass().getCanonicalName().indexOf("Map") > 0 ) {
        HashMap m = (HashMap)p;
        //ZiemView zv = new ZiemView();
        ZiemViewTab zv = new ZiemViewTab();        
        //zv.showPanels=false;
        ////////log.p("Selector is " + m.get("selector"));
        ArrayList<Service> services = ServerAPI.serviceList(m.get("selector"));
        //if there is no entry, then add the default service or return a component that says there
        //is no service to show or just show back or do nothing
        if ((services == null) || (services.size() < 1 )){
                ToastBar.showInfoMessage("Services Not Available Yet");
                Container cnt = new Container();
                Label t = new Label("Services Not Available Yet");
                cnt.add(t);
                return cnt;
        }        
        return zv.createRequestView(services);
        }else {
            if (p.getClass().getCanonicalName().indexOf("List") > 0 ) {
                return createService((ArrayList) p);
            }
        }
        return new Container();
    }
    private Component createService( ArrayList<Service> list){
          if ((list == null) || (list.size() < 1 )){
                ToastBar.showInfoMessage("Services Not Available Yet");
                Container cnt = new Container();
                Label t = new Label("Services Not Available Yet");
                cnt.add(t);
               
               return cnt;
        }    
            ZiemViewTab zv = new ZiemViewTab();      
        return zv.createRequestView(list);      
    }
   
}
