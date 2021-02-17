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
import com.ixzdore.restdb.ziemobject.ServiceContact;
import com.ziemozi.server.local.localAPI;
import java.util.ArrayList;
import java.util.List;

public class SearchContacts extends Form {

    private boolean searchPeople = false;
    private String lastSearchValue;
    private UITimer pendingTimer;
    private long lastSearchTime;
    public boolean geoSearch = false;
    public Button showOnMap;
    public TextField searchField = new TextField("Enter Search");
    public InfiniteContainer ic = new InfiniteContainer() {
    public    ArrayList<ServiceContact>             allcontacts = localAPI.searchContacts(
                        "", 0, 9999);
        @Override
        public Component[] fetchComponents(int index, int amount) {
            ////////log.p(searchField.getText());
            ////////log.p("SearchPeople " + searchPeople);
            ArrayList<ServiceContact> contacts = new ArrayList<ServiceContact>();

            if (index == 0) {
                //we are just starting so load it all up
                contacts = allcontacts;
                if (searchField.getText() != null && searchField.getText().length() > 0) {
                    ArrayList<ServiceContact> ctx = new ArrayList<ServiceContact>();
                    for (ServiceContact sc : contacts) {
                        if (filterContact(sc, searchField.getText())) {
                            ctx.add(sc);
                        }
                    }
                    contacts = new ArrayList<ServiceContact>();
                    contacts.addAll(ctx);
                }
            }
            if ((contacts == null) || (contacts.size() < 1)) {
                return null;
            }            
            if ( index + amount > contacts.size()) {
                amount = contacts.size() - index;
                if ( amount <=0) return null;
            }

            List<Component> response = new ArrayList<>();

            for (int iter =0; iter < amount; iter++) { 
             int offset = index+iter;
             response.add(createEntry(contacts.get(offset)));
            }
            if (response.isEmpty()) {
                return null;
            }
            return UIUtils.toArray(response);
        }
        } ;
        
private Boolean filterContact(ServiceContact c, String s) {
            Boolean filterin = false;
            List<String> textTerms = StringUtil.tokenize(s, ",");
            String rs = c.summary().toLowerCase();
            //////log.p(rs);
            for (String t : textTerms) {
                //////log.p(t);
                if (rs.lastIndexOf(t.toLowerCase()) > 0) {
                    //////log.p("found match for " + t);
                    filterin = true;
                    break;
                }
            }

            return filterin;
        }

        public SearchContacts() {
            super(new BorderLayout());
            ////////log.p("\n\n Searching initiated \n\n");
            showOnMap = new Button("Map");
            searchField.setUIID("Title");
            searchField.getAllStyles().setAlignment(LEFT);
            searchField.getAllStyles().setBgColor((0x99CCCC));
            searchField.addDataChangedListener((i, ii) -> updateSearch());
            showOnMap.setUIID("SmallLabel");
            Container c = new Container();
            c.setLayout(new BoxLayout(BoxLayout.X_AXIS));
            c.getAllStyles().setBgColor((0x99CCCC));
            showOnMap.addActionListener(e -> new MapShowForm(localAPI.searchRequests(
                    searchField.getText(), 1, 100)).show());
            Toolbar tb = this.getToolbar();
            c.add(searchField);
            tb.setTitleComponent(c);
            Form previous = getCurrentForm();
            tb.addMaterialCommandToLeftBar("", MATERIAL_CLOSE, e
                    -> previous.showBack());
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
            this.setEditOnShow(searchField);
        }

        private void updateSearch() {
            String text = searchField.getText();
            if (text.length() > 2) {
                if (lastSearchValue != null) {
                    if (lastSearchValue.equalsIgnoreCase(text)) {
                        return;
                    }
                    if (pendingTimer != null) {
                        pendingTimer.cancel();
                    }
                    long t = System.currentTimeMillis();
                    if (t - lastSearchTime < 100) {
                        pendingTimer = UITimer.timer((int) (t - lastSearchTime),
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

        private Component createEntry(ServiceContact p) {
            if (p != null) {
                //////log.p("Service Contact name " + p.name.get());
                //if (p.services.size() > 0) {
                //////log.p("Creating Entry for " + p.summary());
                p.refresh();

                MultiButton mb = new MultiButton(p.name.get());
                mb.setUIID("SmallLabel");
                //String summary = p.summary();    
                mb.setTextLine1(p.name.get());
                mb.setTextLine2(p.fullAddress());
                mb.setTextLine3(p.extendedDescription());
                ////////log.p(p.plain_summary());
                //if ( )

                mb.setIcon(p.getAvatar(8));
                mb.addActionListener(e -> new ContactForm(p).show());
                return mb;

            } else {
                return new MultiButton("-");
            }
        }
    }
