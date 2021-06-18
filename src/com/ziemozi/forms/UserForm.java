package com.ziemozi.forms;

import com.ixzdore.restdb.ziemobject.Post;
import com.ixzdore.restdb.ziemobject.User;
import com.ziemozi.server.ServerAPI;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Component;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.Form;
import com.codename1.ui.InfiniteContainer;
import com.codename1.ui.layouts.BorderLayout;
import com.ixzdore.restdb.ziemobject.Request;
import java.util.ArrayList;
import java.util.List;

public class UserForm extends Form {
    private final User user;
    private final InfiniteContainer ic = new InfiniteContainer() {
        @Override
        public Component[] fetchComponents(int index, int amount) {
            ArrayList<Component> components = new ArrayList<>();
            int page = index / amount;
            if(index % amount > 0) {
                page++;
            }
            List<Request> response = ServerAPI.requestsOf(user._id.get(), 
                page, amount);
            if(response == null) {
                if(index == 0) {
                    return UIUtils.toArray(components);
                }
                return null;
            }

            for(Request p : response) {
                components.add(NewsfeedContainer.
                    createNewsItem(p.ziemozi_user.get(0), p));
                components.add(UIUtils.createHalfSpace());
            }
            return UIUtils.toArray(components);
            
        }
    };
    
    public UserForm(User user) {
        super(user.fullName(), new BorderLayout());
        this.user = user;
        add(CENTER, ic);
        Form previous = getCurrentForm();
        getToolbar().addMaterialCommandToLeftBar("", MATERIAL_CLOSE, e -> 
            previous.showBack());
    }
}
