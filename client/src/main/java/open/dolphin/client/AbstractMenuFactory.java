package open.dolphin.client;

import javax.swing.ActionMap;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import open.dolphin.helper.MenuSupport;

/**
 *
 * @author kazm
 */
public abstract class AbstractMenuFactory {
    
    public static AbstractMenuFactory getFactory() {
        
//masuda^   大した差がないのでまとめる
        //return ClientContext.isMac() ? new MacMenuFactory() : new WindowsMenuFactory();
        return new WindowsMenuFactory();
//masuda$
    }
    
    public abstract void setMenuSupports(MenuSupport main, MenuSupport chart);
    
    public abstract JMenuBar getMenuBarProduct();
    
    public abstract JPanel getToolPanelProduct();
    
    public abstract ActionMap getActionMap();
    
    public abstract void build(JMenuBar menuBar);
}
