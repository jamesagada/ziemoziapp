package com.ziemozi.forms;

import com.codename1.components.MultiButton;
import com.codename1.io.Log;
import com.codename1.ui.Button;
import com.ixzdore.restdb.ziemobject.Post;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.ServerAPI;
import com.codename1.ui.Component;
import com.codename1.ui.Form;
import com.codename1.ui.TextField;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.InfiniteContainer;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Container;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.UITimer;
import com.codename1.util.StringUtil;
import com.ixzdore.restdb.ziemobject.Request;
import com.ziemozi.server.local.localAPI;
import java.util.ArrayList;
import java.util.List;


public class SearchForm extends Form {
    private boolean searchPeople = false;
    private String lastSearchValue;
    private UITimer pendingTimer;
    private long lastSearchTime;
    public     boolean geoSearch = false;
    public Button showOnMap;
    public TextField searchField = new TextField("Enter Search");
    public List<Request>         allRequests = localAPI.newsfeed(0, 9999,false);
    
    public InfiniteContainer ic = new InfiniteContainer() {
        @Override
        public Component[] fetchComponents(int index, int amount) {
          List<Component> response = new ArrayList<>(); 
             List<Request> requests = new ArrayList<Request>();        
        if(index == 0) {                //we are just starting so load it all up
                if (searchField.getText() != null && searchField.getText().length() > 0) {
                    ArrayList<Request> ctx = new ArrayList<Request>();
                    for (Request sc : allRequests) {
                        if (filterRequest(sc, searchField.getText())) {
                            sc.refreshUser();
                            if (sc.ziemozi_user.size() > 0 ) {
                                ctx.add(sc);
                            }else {
                                //////Log.p(sc.summary.get());
                            }
                        }
                    }
                    requests = new ArrayList<Request>();
                    requests.addAll(ctx);
                }           
            }
            if ((requests == null) || (requests.size() < 1)) {
                return null;
            }            
            if ( index + amount > requests.size()) {
                amount = requests.size() - index;
                if ( amount <=0) return null;
            }

            for (int iter =0; iter < amount; iter++) { 
             int offset = index+iter;
             response.add(createEntry(requests.get(offset)));
            }
            if (response.isEmpty()) {
                return null;
            }
            return UIUtils.toArray(response);
        
        }} ;
private Boolean filterRequest(Request c, String s) {
            Boolean filterin = false;
            List<String> textTerms = StringUtil.tokenize(s, ",");
            String rs = c.summary.get().toLowerCase();
            ////////Log.p(rs);
            for (String t : textTerms) {
                //////Log.p(t);
                if (rs.lastIndexOf(t.toLowerCase()) > 0) {
                    //////Log.p("found match for " + t);
                    filterin = true;
                    break;
                }
            }

            return filterin;
        }
    
    public SearchForm() {
        super(new BorderLayout());
        //////////Log.p("\n\n Searching initiated \n\n");

        showOnMap = new Button("Map");
        searchField.setUIID("SmallLabel");
        searchField.getAllStyles().setAlignment(LEFT);
        searchField.addDataChangedListener((i, ii) -> updateSearch());
        showOnMap.setUIID("SmallLabel");
        Container c = new Container();
        c.setLayout(new BoxLayout(BoxLayout.X_AXIS));
        c.getAllStyles().setBgColor((0x99CCCC));
        showOnMap.addActionListener(e-> new MapShowForm(localAPI.searchRequests(
                searchField.getText(),1,100)).show());
        Toolbar tb = getToolbar();
        c.add(searchField);
        tb.setTitleComponent(c);
        Form previous = getCurrentForm();
        tb.addMaterialCommandToLeftBar("", MATERIAL_CLOSE, e -> 
            previous.showBack());
       /*
            tb.addMaterialCommandToRightBar("", MATERIAL_PERSON, e -> {
            searchPeople = true;
            ic.refresh();
        });
        */
        tb.addMaterialCommandToRightBar("", MATERIAL_SEARCH, e -> {
            searchPeople = false;
            ic.refresh();
        });
        add(CENTER, ic);
        setEditOnShow(searchField);
    }
    
    private void updateSearch() {
        String text = searchField.getText();
        if(text.length() > 2) {
            if(lastSearchValue != null) {
                if(lastSearchValue.equalsIgnoreCase(text)) {
                    return;
                }
                if(pendingTimer != null) {
                    pendingTimer.cancel();
                }
                long t = System.currentTimeMillis();
                if(t - lastSearchTime < 300) {
                    pendingTimer = UITimer.timer((int)(t - lastSearchTime), 
                        false, this, () -> {
                            lastSearchTime = System.currentTimeMillis();
                            ic.refresh();
                        });
                    return;
                }
            }
            lastSearchTime = System.currentTimeMillis();
            ic.refresh();
        }
    }
    
    private Component createEntry(User u) {
        MultiButton mb = new MultiButton(u.fullName());
        mb.setIcon(u.getAvatar(8));
        mb.addActionListener(e -> new UserForm(u).show());
        return mb;
    }

    private Component createEntry(Request p) {
        p.refreshService();
        if (p.service.size() > 0 ){
        MultiButton mb = new MultiButton(p.service.get(0).description.get());
        String summary = p.summary.get();       
        mb.setTextLine2(p.plain_summary());
        //////////Log.p(p.plain_summary());
        //if ( )
        p.refreshUser();
        if (p.ziemozi_user.size() > 0 ) {
        mb.setIcon(p.ziemozi_user.get(0).getAvatar(8));
        }
        mb.addActionListener(e -> new PostForm(p).show());
        return mb;
        }else{
            return null;
        }
    }
}