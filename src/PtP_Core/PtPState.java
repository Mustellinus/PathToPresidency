
package PtP_Core;

import com.jme3.app.state.AbstractAppState;
import de.lessvoid.nifty.screen.Screen;

/**
 *
 * @author Greg
 */
public class PtPState extends AbstractAppState {
    
    public PtPState(Main app,String guiName)
    {
       m_oMainApp=app;
       m_wScreenName=guiName;
       m_gScreen=m_oMainApp.getGuiController().getScreen(guiName);
    }
    public PtPState()
    {}
    /**Adds this game state to the scene graph**/
    public void attach()
    {}
    /**Removes this game state from the scene graph**/   
    public void detach()
    {
    }
    @Override
    public void update(float tpf)
    {} 
    public String getScreenName()
    {
        return m_wScreenName;
    }
    //ivars
    protected Main m_oMainApp;
    protected Screen m_gScreen;//the screen used by this game state    
    protected String m_wScreenName;// name of the above screen
}
