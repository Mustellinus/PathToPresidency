package PtP_AI;

import PtP_Core.AI_Player;
import PtP_Core.Main;

/**An abtract class for processsing goals by the AI
 *
 * @author Greg
 */
public abstract class Strategy {
    
    public Strategy(Main app,AI_Player player)
    { 
        m_oApp=app;
        m_oPlayer=player;
    }
    /**attempt to execute goal
     * 
     * @return a bool indictaing success of failure
    */
    public boolean process()
    {
        return true;
    }
    /** determine the desirability of pursuing this goal 
     * vs others 
     * 
     * @return a float quantifying desirability
     */
    public float desirability()
    {
        return 0;
    }
    /**produces a string announcing the results of the action if it is
     * a success
     * 
     * @return the String
     */
    public String posText()
    {
        return "";
    }
     /**produces a string announcing the results of the action if it is
     * a failure
     * 
     * @return the String
     */ 
    public String negText()
    {
        return "";
    }
    //ivars 
    protected Main m_oApp;
    protected AI_Player m_oPlayer;
}
