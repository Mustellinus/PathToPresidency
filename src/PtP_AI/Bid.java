package PtP_AI;

import PtP_Core.AI_Player;
import PtP_Core.Main;
import PtP_Core.US_State;

/**Evaluates and executes a bid for the next turn's issue of
 * the day by an AI player
 *
 * @author Greg
 */
public class Bid extends Strategy{

    public Bid(Main app,AI_Player player)
    { 
        super(app,player);
    }
     @Override
    public boolean process()
    {
        int bidAmount=m_oApp.getGameMap().getNumGenerator().nextInt
            (m_oPlayer.getFunds()+1);
        m_oPlayer.setBid(bidAmount);
        m_oApp.updatePlayerStats(m_oPlayer);
        return true;
    }
    @Override
    public float desirability()
    { 
        float result=0;
        US_State state=m_oPlayer.getCurrentState();
        for(int i=0;i<m_oPlayer.getIssueNames().length;i++)
        {
            String issue=m_oPlayer.getIssueNames()[i];
            if(m_oPlayer.getIssues().get(issue)==state.getIssues().get(issue)
                    &&m_oPlayer.getActiveTeamMember().traveled())
            {
                    m_oPlayer.setFavoriteIssue(issue);
                    result=.4f+((float)m_oPlayer.getFunds()/10);
            }
        }      
        return result;
    }
    @Override
    public String posText()
    {
        String player=m_oPlayer.getName();
        String result=player+" has bid for the next issue of the day";
        return result; 
    }
    @Override
    public String negText()
    {
        String player=m_oPlayer.getName();
        String result=player+" can't bid";
        return result; 
    }
}
