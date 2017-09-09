package PtP_AI;

import PtP_Core.AI_Player;
import PtP_Core.Main;
import PtP_Core.Player;
import PtP_Core.US_State;
import java.util.ArrayList;

/**Evaluates and executes running a negative ad against an
 * AI player's opponent
 *
 * @author Greg
 */
public class NegAdvertise extends Strategy{
    
    public NegAdvertise(Main app,AI_Player player)
    {
        super(app,player);
        m_oTargetState=null;
        m_oTargetPlayer=null;
    }
    @Override
    public boolean process()
    {
        if(m_oTargetState !=null && m_oTargetPlayer !=null)
        {
            int result=m_oApp.getGameMap().runNegAd(m_oTargetState.getName(), m_oTargetPlayer);
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
      //choose prospective state and opponent
        US_State[] states=m_oApp.getGameMap().getStates();
        ArrayList<Player> players=m_oApp.getAllPlayers();
        float value=0;
        for(int i=0;i<states.length;i++)
        {
            //find the leading players and pick one
            ArrayList<Player> leaders=new ArrayList<Player>();
            for(int p=0;p<players.size();p++)
            {
                // player should target a leading candidate that is not himself 
                if(!players.get(p).equals(m_oPlayer)&& 
                   states[i].getSupport().get(players.get(p).getName())==states[i].getHighestSupportValue())
                {
                   leaders.add(players.get(p)); 
                }    
            } 
            if(leaders.size()>0)//if suitable candidates have been found in this state consider an add against them
            {
                int leaderIndex=m_oApp.getGameMap().getNumGenerator().nextInt(leaders.size());
                Player leadPlayer=leaders.get(leaderIndex);//radomly chosen leading cnadidate
                float difference=(m_oApp.getGameMap().compareIssueOfDay(leadPlayer, states[i]))/4;
                float support=((float)states[i].getSupport().get(leadPlayer.getName())/100);
                float newValue=((float)states[i].getVotes()/55)*difference*support;
                //compare the desirablility of a neg. add in this state with the best candidate state so far, and choose the best candidate
                if(newValue>value)
                {
                    value=newValue;
                    m_oTargetState=states[i];
                    m_oTargetPlayer=leadPlayer;
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
        String opponent=m_oTargetPlayer.getName();
        String result=player +" has smeared "+opponent+" in "+m_oTargetState.getName();
        return result;  
    }
    @Override
    public String negText()
    {
        String player=m_oPlayer.getName();
        String result=player +"'s smear attmept has backfired in "+m_oTargetState.getName();
        return result; 
    }
     //ivars
    private US_State m_oTargetState;
    private Player m_oTargetPlayer;
}
