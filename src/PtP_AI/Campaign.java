package PtP_AI;

import PtP_Core.AI_Player;
import PtP_Core.Main;
import PtP_Core.US_State;

/**Evalutates and executes campaigning in a State for 
 * AI player
 *
 * @author Greg
 */
public class Campaign extends Strategy{
    
    public Campaign(Main app,AI_Player player)
    {
        super(app,player);
    }
    @Override
    public boolean process()
    {
        if(m_oPlayer.getFunds()>1)
        {
           m_oPlayer.setFunds(-1);
           int result=m_oApp.getGameMap().campaign();
           m_oApp.updatePlayerStats(m_oPlayer);
           m_oPlayer.getActiveTeamMember().setTraveled(false);
           if(result>0)
           {
               return true;
           }
           return false;
        }
        m_oPlayer.getActiveTeamMember().setTraveled(false);
        return false;
    }
    @Override
    public float desirability()
    { 
        float result=0;
        if(m_oPlayer.getFunds()<1)//player can't afford this action
        {
            return result;
        }
        US_State state=m_oPlayer.getCurrentState();
        float issueDifference=m_oApp.getGameMap().compareIssueOfDay(m_oPlayer, state);
        float support=m_oPlayer.getCurrentState().getSupport().get(m_oPlayer.getName());
        float votes=m_oPlayer.getCurrentState().getVotes();
        result=(1-(support/100))*(votes/55)*(1-(issueDifference/4));
        
        return result;
    }
        @Override
    public String posText()
    {
       String state=m_oPlayer.getCurrentState().getName();
       String player=m_oPlayer.getName();
       String result=player+" has successfully campaigned in "+state +"!";
       return result; 
    }
    @Override
    public String negText()
    {
       String state=m_oPlayer.getCurrentState().getName();
       String player=m_oPlayer.getName();
       String result=player+"'s campaign in "+state+" was a falure!";
       return result; 
    }
}
