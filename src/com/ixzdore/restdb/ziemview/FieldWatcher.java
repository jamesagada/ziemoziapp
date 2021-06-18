/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ixzdore.restdb.ziemview;

import com.codename1.io.CharArrayReader;
import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.ComponentSelector;
import com.codename1.util.StringUtil;
import com.codename1.util.regex.RE;
import java.io.IOException;
import static java.lang.Double.NaN;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author jamesagada
 * watches fields and updates dependencies and relationships
 * input is the servicedefinition and the current value of the fields
 * update service attributes as needed and compnnents that implement them
 * so we will have as input componentSelector for the fields
 * build also a hierarchy and then 
 */
public class FieldWatcher implements Observer{
 
    public Object watchField;
    public BaseEditorImpl watchingOnBehalfOf;
    public Map<String, Object> context;
    public BrowserComponent bexec = new BrowserComponent();
   public FieldWatcher(Object watchField){
       this.watchField = watchField;

   } 
  public FieldWatcher( ){

  }
   public void watchThisField(Object watchingField,Map<String,Object> c){
       this.watchField = watchingField;
      this.context = c;       
       //////////Log.p("Watching  This Field " + watchField.toString());
   }
    @Override
    public void update(Observable o, Object arg) {
        //the update is called from the observable items
        //here we need to determine if the object o is being watched
        //by this watcher. if so then we have to trigger 
        //the update the value of the observer.
        //arg is an editor
        BaseEditorImpl b = (BaseEditorImpl)arg;
        //////////Log.p(" I have been Informed that " + b.serviceAttribute.name.get() + " has changed");
        //I am watching on behalf of watchingOnBehalfOf.
        //now I have to update the one I am watching for by calling her
        //executeWatch and passing along the watched object.
        //watchingOnBehalfOf.executeWatch(b);
        String watchformula = watchingOnBehalfOf.serviceAttribute.watch_formula.get();
        //////////Log.p("Watching with this formula " + watchformula);
        if (watchformula.length() > 0 ){
       
        Map<String,Object> parsedFormula = new HashMap<String,Object>();
        JSONParser p = new JSONParser();
        try {
            parsedFormula = p.parseJSON(new CharArrayReader(watchformula.toCharArray()));
        } catch (IOException ex) {
            //parsing failed and that is it;
        }
        applyRelevance(watchingOnBehalfOf,parsedFormula.get("Relevance"),b);
        applyValueUpdate(watchingOnBehalfOf,parsedFormula.get("Value"),b);
        applyLookUp(watchingOnBehalfOf,parsedFormula.get("LookUp"),b);
        watchingOnBehalfOf.editContainer.revalidate();
        }
    }

    private void applyRelevance(BaseEditorImpl watchingOnBehalfOf, Object formula, BaseEditorImpl b) {
  //      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //relevance means if you are not relevant, you are hidden
        //We use the calculateFunction which returns an object
        //////////Log.p("Setting relevance");
        //convert formula to string and parse it to
        //extract Operator, and parameters
        //
        Boolean relevance = Boolean.parseBoolean(calculateFormula(formula));
        
        //////////Log.p("Relevance is " + relevance);
                if (relevance  != null ) watchingOnBehalfOf.setRelevance(relevance);
                
    }

    private void applyValueUpdate(BaseEditorImpl watchingOnBehalfOf, Object formula, BaseEditorImpl b) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //////////Log.p("Setting value");
        Object updatedValue = calculateFormula(formula);
        //////////Log.p("Updated Value " + updatedValue.toString());
                if (updatedValue != null ) watchingOnBehalfOf.setValue(updatedValue);
    }

    private void applyLookUp(BaseEditorImpl watchingOnBehalfOf, Object formula, BaseEditorImpl b) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //////////Log.p("Setting lookup");
        if (formula != null) {
            Object option = calculateFormula(formula);
            if (option != null ) watchingOnBehalfOf.setupOptions(option);
        }
    }
    private String calculateFormula(Object f){
        //calculate a value for the formula f.
        //f is a map with values for operator and parameters
        //the parameters are references to the values of particular elements in the context
        //the context holds a list of the components of the form
        //o e simply compute values of the parameters and insert into the expression
        
        String formatedExpression = formatExpression(f.toString(),context);
        //////////Log.p("Formated Expression " + formatedExpression);
        String result = bexec.executeAndReturnString(formatedExpression);
        //////////Log.p("Calculated Result " + result);
        return result;
    }


    public  String formatExpression(String format, Map<String, Object> objects) {

    String fieldStart = "@@";
    final String fieldEnd = "@@";
     String regex = fieldStart + "([^}]+)" + fieldEnd;
       RE r = new RE(regex);
       
        String result = format;
        String field ="";
        //////////Log.p("Found a match for the pattern  in " + format + "? " + r.match(result) );
        
        while (r.match(result)) {
            //the right thing is to find the first item
              String wholeExpr = r.getParen(0);        // wholeExpr will be 'aaaab'
              //////////Log.p("wholeExpr " + wholeExpr);
                String insideParens = r.getParen(1);     // insideParens will be 'aaaa'
              //////////Log.p("insideParens "+ insideParens);
                int startWholeExpr = r.getParenStart(0); // startWholeExpr will be index 1
                //////////Log.p("startWholeExpr "+ startWholeExpr);
            int endWholeExpr = r.getParenEnd(0);     // endWholeExpr will be index 6
                //////////Log.p("endWholeExpr "+ endWholeExpr);
            int lenWholeExpr = r.getParenLength(0);
                //////////Log.p("lenWholeExpr "+ lenWholeExpr);
            Object o = objects.get(insideParens); //match name to context
            //////////Log.p("Objects From Context " + o.toString());
            if (o!=null){
                BaseEditorImpl b = (BaseEditorImpl) o;                
                field = '"' + b.getRequestParameter().value.get() + '"';
                field = StringUtil.replaceAll(field, "\n", "");
                result = StringUtil.replaceAll(result, wholeExpr, field);
            }else {
                result = StringUtil.replaceAll(result, wholeExpr, " ");
            }
            //////////Log.p("Result " + result);
        }
        //(function () { return "abc" }())
        return "( " +  result + " )";
        
        //return  "{var r= new String(" + result +"); return eval(r)}";
    }
public static String replaceFirst(String s, String pattern, String replacement) {
     int idx = s.indexOf(pattern);
     return s.substring(0, idx) + replacement + s.substring(idx + pattern.length());
}    
}
