package PtP_AI;

import PtP_Core.AI_Player;
import PtP_Core.Main;

/**Evaluates and executes an attempt to recruit a volunteer
 * by the AI player
 *
 * @author Greg
 */
public class Recruit extends Strategy{
    
    public Recruit(Main app,AI_Player player)
    {
        super(app,player);
    }
    @Override
    public boolean process()
    {
        if(m_oApp.doAI_Recruit())
        {
            m_oPlayer.getActiveTeamMember().setTraveled(false);
            return true;
        }
        m_oPlayer.getActiveTeamMember().setTraveled(false);
        return false;
    }
    @Override
    public float desirability()
    { 
        float result=0;
        
        float saturation=1/m_oPlayer.getTeamMembers().size();
        float support=m_oPlayer.getCurrentState().getSupport().get(m_oPlayer.getName());
        result=saturation*(support/100);
        return result;
    }
        @Override
    public String posText()
    {
       String state=m_oPlayer.getCurrentState().getName();
        String player=m_oPlayer.getName();
        String result=player +" has recruited a volunteer in "+state;
        return result;
    }
    @Override
    public String negText()
    {
        String state=m_oPlayer.getCurrentState().getName();
        String player=m_oPlayer.getName();
        String result=player +" found no volunteers in "+state;
        return result;
    }
}
