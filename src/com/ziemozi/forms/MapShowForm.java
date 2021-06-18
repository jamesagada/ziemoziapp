package com.ziemozi.forms;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.ToastBar;
import com.codename1.googlemaps.MapContainer;
import com.codename1.googlemaps.MapLayout;
import com.codename1.io.Log;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.maps.Coord;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.convertToPixels;
import com.ixzdore.restdb.ziemobject.Post;
import static com.codename1.ui.CN.getCurrentForm;
import static com.codename1.ui.CN.getDisplayWidth;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.FontImage;
import static com.codename1.ui.FontImage.*;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.URLImage;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Effects;
import com.ixzdore.restdb.ziemobject.Request;
import static com.ixzdore.restdb.ziemview.LocationEditor.getFormattedAddress;
import com.ziemozi.server.ServerAPI;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MapShowForm extends Form {
    private static final String HTML_API_KEY = "AIzaSyBWeRU02YUYPdwRuMFyTKIXUbHjq6e35Gw";
       final float COORDINATE_OFFSET = 0.00002f;
       final int MAX_NUMBER_OF_MARKERS = 12;
    private final int shadowHeight;
    private Image dropShadow;
    //private final MapContainer mc;
    //private final Container mapLayer;
       
    public MapShowForm(List<Request> req ) {
        super("MapView", new BorderLayout());
        //
         //       super("MapView",new LayeredLayout());
        setScrollableY(false);
        
        shadowHeight = convertToPixels(4);
        Display.getInstance().callSeriallyOnIdle(() -> {
            dropShadow = Effects.squareShadow(getDisplayWidth() + shadowHeight * 2, shadowHeight * 2, shadowHeight, 0.3f);
        });
        
        setTransitionOutAnimator(CommonTransitions.createEmpty());
        
        //mc = new MapContainer(HTML_API_KEY);
        //mc.setShowMyLocation(true);
        //add(mc);
        
        //mapLayer = new Container();
        //mapLayer.setName("Map Layer");
        //mapLayer.setLayout(new MapLayout(mc, mapLayer));
       
        //
        final MapContainer cnt = new MapContainer(HTML_API_KEY);
        
       
        //final MapContainer cnt = new MapContainer();
        //put camera on current position and set zoom?
        cnt.setShowMyLocation(true);
        cnt.setCameraPosition(currentPosition());
        //putMarkers(cnt,req);
        cnt.zoom(currentPosition(),16);
        add(CENTER,cnt);
        //mapLayer.revalidate();
        //add(mapLayer);
        Form previous = getCurrentForm();
        getToolbar().addMaterialCommandToLeftBar("", 
            MATERIAL_ARROW_BACK, e -> previous.showBack());
    }

    private void putMarkers(MapContainer cnt, List<Request> req) {
        //go through the list and put a marker on the map
        

       HashMap markers = new HashMap();
       
        for ( Request r:req){
            Coord c = new Coord(Double.valueOf(r.request_latitude.get()),Double.valueOf(r.request_longitude.get()));
            
            //put marker in this coord
        Style s = new Style();
        s.setFgColor(0xff0000);
        s.setBgTransparency(0);
        //FontImage mImg = FontImage.createMaterial(FontImage.MATERIAL_PLACE, s, Display.getInstance().convertToPixels(3));
        //markerImage should be the service logo!
        //////////Log.p(r.plain_summary());
        r.refreshService();        
        //////////Log.p("Number of Services " + r.service.size());
        //////////Log.p(c.toString());
        Image serviceImage = ServerAPI.getServiceImage(r.service.get(0));

               c = getMarkerPosition(c,markers);
               cnt.setCameraPosition(c);
               
            ////////Log.p("Placing marker here " + c.toString());
            cnt.addMarker(
                    EncodedImage.createFromImage(serviceImage, false),
                    cnt.getCameraPosition(),
                    r.service.get(0).name.get(),
                    r.plain_summary(),
                     evt -> {
                            //if we click on it, it should open an interactiveDialog or a Dialog
                            //or a form providing full details
                             ToastBar.showMessage(r.plain_summary(), FontImage.MATERIAL_PLACE);
                     }
            );

        }
    }

    private Coord currentPosition() {
        Coord c = new Coord(7.6,7.5);
       InfiniteProgress ip = new InfiniteProgress();
        Dialog ipDlg = ip.showInfiniteBlocking();
        Location location = LocationManager.getLocationManager().getCurrentLocationSync(30000);
        ipDlg.dispose();
        if (location == null) {
            try {
                location = LocationManager.getLocationManager().getCurrentLocation();
            } catch (IOException err) {
                ToastBar.showErrorMessage("Could not fix current location");
                return c;
            }
        }
        c.setLatitude(location.getLatitude());
        c.setLongitude(location.getLongitude());
        return c;
    }

    private Coord getMarkerPosition(Coord c, HashMap markers) {
        //
    //////////Log.p("Requested Position is " + c.toString());
    for (int i = 0; i <= MAX_NUMBER_OF_MARKERS; i++) {
        
        if (markers.containsValue(c)) {
            //c.setLatitude(c.getLatitude()+i*COORDINATE_OFFSET);
            c.setLongitude(c.getLongitude()+i*COORDINATE_OFFSET);
        }else {
            break;
        }
    }
    //////////Log.p("Provided Marker Position is " + c.toString());
    markers.put(c.getLongitude(), c);
    return c;
     }

}
