package PtP_AI;

import PtP_Core.AI_Player;
import PtP_Core.Main;
import PtP_Core.US_State;

/** Evaluates and executes a flip-flop on issues by the AI
 * player. The AI will move toward neutral positions if the
 * election is giong badly
 *
 * @author Greg
 */
public class FlipFlop extends Strategy{
    
    public FlipFlop(Main app, AI_Player player)
    {
        super(app,player);
    }
    @Override
    public boolean process()
    {
        return false;
    }
    @Override
    public float desirability()
    { 
        float result=0;
        float votes=0;
        US_State[] states=m_oApp.getGameMap().getStates();
        for(int s=0;s<states.length;s++)
        {
            if(states[s].getControllingPlayer()==m_oPlayer)
            {
                votes+=states[s].getVotes();
            }
        }
        float success=votes/538;
        float timeLeft=m_oApp.getGameMap().getTurnsLeft()/m_oApp.getGameMap().NUM_TURNS;
        result=(1-success)*(1-timeLeft);
        return result;
    }
        @Override
    public String posText()
    {
       String player=m_oPlayer.getName();
       String result=player+"'s position on the issues has been 'clarified'";
       return result; 
    }
    @Override
    public String negText()
    {
        return "";
    }
}
