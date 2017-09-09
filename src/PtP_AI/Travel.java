package PtP_AI;

import PtP_Core.AI_Player;
import PtP_Core.Main;
import PtP_Core.US_State;
import java.util.ArrayList;

/**Evaluates and executes Traveling to another state for the AI Player
 *
 * @author Greg
 */
public class Travel extends Strategy{
    
    public Travel(Main app,AI_Player player)
    {
        super(app,player);
    }
    @Override
    public boolean process()
    {
        //pick best state to travel the go there
        float bestValue=0;
        US_State bestState=null;
        if(m_oPlayer.getFunds()>=2)//player can afford to fly Team member
        {
            float newValue=0;
            for(int s=0;s<m_oApp.getGameMap().getStates().length;s++)
            {
                US_State state=m_oApp.getGameMap().getStates()[s];                
                if(!m_oPlayer.stateIsOccupied(state))//shouldn't waste travel to a state already occupied
                {
                    if(m_oPlayer==state.getControllingPlayer())//choose the state with the most votes and the smallest support to shore up lead
                    {
                        float lead=state.getSupport().get(m_oPlayer.getName());
                        newValue=((float)state.getVotes()/55)*(1-(lead/100));
                    }
                    else//choose a state based on number of votes and how close player is to being in the lead
                    {
                        float lead=state.getHighestSupportValue()
                                -state.getSupport().get(m_oPlayer.getName());
                        newValue=((float)state.getVotes()/55)*(1-(lead/100));
                    }
                    if(newValue>bestValue)
                    {
                        bestValue=newValue;
                        bestState=state;
                    } 
                }                             
            }
            int fundMod=-2;
            //drive if the state is close enough
            for(int s=0;s<m_oPlayer.getCurrentState().getNeighbors().size();s++)
            {
                String neighbor=m_oPlayer.getCurrentState().getNeighbors().get(s);
                if(bestState.getName().equals(neighbor))
                {
                    fundMod=-1;
                    break;
                }
            }
            m_oPlayer.setFunds(fundMod);
            m_oPlayer.getActiveTeamMember().setTraveled(true);
            m_oApp.getGameMap().moveAI_Avatar(bestState);
            return true;
        }
        if(m_oPlayer.getFunds()>=1)//player can only afford to drive
        { 
            //set up an array of neighbor states to judge
            ArrayList<US_State> states=new ArrayList<US_State>();
            for(int s=0;s<m_oApp.getGameMap().getStates().length;s++)
            { 
                US_State state=m_oApp.getGameMap().getStates()[s];
                for(int n=0;n<m_oPlayer.getCurrentState().getNeighbors().size();n++)
                {  
                    String name=m_oPlayer.getCurrentState().getNeighbors().get(n);
                    if(state.getName().equals(name))
                    {
                        states.add(state);
                    }
                }
            }
            float newValue=0; 
            for(int s=0;s<states.size();s++)
            {
                US_State state=states.get(s);   
                if(m_oPlayer.stateIsOccupied(state))
                {
                    newValue=-1;
                }//make sure the occupied state ins't picked
                else
                {
                    if(m_oPlayer==state.getControllingPlayer())//choose the state with the most votes and the smallest support to shore up lead
                    {
                        float lead=state.getSupport().get(m_oPlayer.getName());
                        newValue=((float)state.getVotes()/55)*(1-(lead/100));
                    }
                    else//choose a state based on number of votes and how close player is to being in the lead
                    {
                        float lead=state.getHighestSupportValue()
                                -state.getSupport().get(m_oPlayer.getName());
                        newValue=((float)state.getVotes()/55)*(1-(lead/100));
                    }
                }
                if(newValue>bestValue)
                {
                    bestValue=newValue;
                    bestState=state;
                }
            }
            m_oPlayer.setFunds(-1);
            m_oPlayer.getActiveTeamMember().setTraveled(true);
            m_oApp.getGameMap().moveAI_Avatar(bestState);
            return true;
        }
        return false;
    }
    @Override
    public float desirability()
    { 
        float result=-1;
        if(m_oPlayer.getActiveTeamMember().traveled()||m_oPlayer.getFunds()<1)//player can't afford this action
        {
            return result;
        }   
        US_State state=m_oPlayer.getCurrentState();
        float issueDifference=m_oApp.getGameMap().compareIssueOfDay(m_oPlayer, state);
        float support=m_oPlayer.getCurrentState().getSupport().get(m_oPlayer.getName());
        float votes=(float)m_oPlayer.getCurrentState().getVotes()/55;
        float campaignResult=(1-(support/100))*(votes/55)*(1-(issueDifference/4));
        
        float fundsResult=(1/(m_oPlayer.getFunds())+.5f)*(support/100);
        
        if(campaignResult >= fundsResult)
        {
            result=1-campaignResult;
        }
        else
        {
            result=1-fundsResult;
        }
        
        return result;
    }
        @Override
    public String posText()
    {
       String state=m_oPlayer.getCurrentState().getName();
        String player=m_oPlayer.getName();
        String result="One of "+player +"'s team has traveled to "+state;
        return result; 
    }
    @Override
    public String negText()
    {
        String player=m_oPlayer.getName();
        String result=player +" couldn't afford to travel";
        return result;
    }
}
