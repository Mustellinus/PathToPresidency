package PtP_AI;

import PtP_Core.AI_Player;
import PtP_Core.Main;

/**Evaluates and executes a fundraising action 
 * by the AI player
 *
 * @author Greg
 */
public class Fundraise extends Strategy{
    
    public Fundraise(Main app,AI_Player player)
    {
        super(app,player);
    }        
    @Override
    public boolean process()
    {
        int funds=m_oApp.getGameMap().fundraise();
        m_oApp.updatePlayerStats(m_oPlayer);
        m_oPlayer.getActiveTeamMember().setTraveled(false);
        if(funds>0)
        {
            return true;
        }
        return false;
    }
    @Override
    public float desirability()
    { 
        float result=1;
        if(m_oPlayer.getFunds()<=0)//player needs money for further actions
        {
            return result;
        }
        
        float support=m_oPlayer.getCurrentState().getSupport().get(m_oPlayer.getName());
        result=(1/(m_oPlayer.getFunds())+.5f)*(support/100);
        
        return result;
    } 
        @Override
    public String posText()
    {
        String state=m_oPlayer.getCurrentState().getName();
        String player=m_oPlayer.getName();
        String result=player +" has successfully raised funds in "+state;
        return result; 
    }
    @Override
    public String negText()
    {
        String state=m_oPlayer.getCurrentState().getName();
        String player=m_oPlayer.getName();
        String result=player +" has falied to raise funds in "+state;
        return result;
    }
}
