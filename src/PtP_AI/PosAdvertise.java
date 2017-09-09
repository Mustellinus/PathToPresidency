package PtP_AI;

import PtP_Core.AI_Player;
import PtP_Core.Main;
import PtP_Core.US_State;

/**Evaluates and Executes a positive ad for the AI player
 *
 * @author Greg
 */
public class PosAdvertise extends Strategy{
    
    public PosAdvertise(Main app,AI_Player player)
    { 
        super(app,player);
        m_oTargetState=null;
    }
    @Override
    public boolean process()
    {
        if(m_oTargetState !=null)
        {
            int result=m_oApp.getGameMap().runPosAd(m_oTargetState.getName());
            if(result>0)
            {
                m_oApp.updatePlayerStats(m_oPlayer);
                m_oPlayer.getActiveTeamMember().setTraveled(false);
                return true;
            }
            m_oApp.updatePlayerStats(m_oPlayer);
            m_oPlayer.getActiveTeamMember().setTraveled(false);    
            return false;
        }
        m_oPlayer.getActiveTeamMember().setTraveled(false);
        return false;
    }
    @Override
    public float desirability()
    {
        float result=0;
      //choose prospective state
        US_State[] states=m_oApp.getGameMap().getStates();
        float value=0;
        for(int i=0;i<states.length;i++)
        {
            if(states[i].getSupport().get(m_oPlayer.getName())<51)// if player has commanding lead no need to advertise in this state
            {
                float difference=(4-m_oApp.getGameMap().compareIssueOfDay(m_oPlayer, states[i]))/4;
                float support=1-((float)states[i].getSupport().get(m_oPlayer.getName())/50);
                float newValue=((float)states[i].getVotes()/55)*difference*support;
            
                if(newValue>value)
                {
                    value=newValue;
                    m_oTargetState=states[i];
                }
            }
        }
        result=value;
        return result;
    }
        @Override
    public String posText()
    {
        String player=m_oPlayer.getName();
        String result=player +" has successfully advertised in "+m_oTargetState.getName();
        return result; 
    }
    @Override
    public String negText()
    {
        String player=m_oPlayer.getName();
        String result=player +"'s advertisement has backfired in "+m_oTargetState.getName();
        return result;
    }
    //ivars
    private US_State m_oTargetState;
}
