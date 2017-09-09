package PtP_AI;

import PtP_Core.AI_Player;
import PtP_Core.Main;
import PtP_Core.SpecialInterest;

/**Evaluates and executes an attempt to pander to a 
 * special interest by an AI player
 *
 * @author Greg
 */
public class Pander extends Strategy{
    
    public Pander(Main app,AI_Player player)
    {
        super(app,player);
    }
    @Override
    public boolean process()
    {
        if(m_oApp.getGameMap().pander(m_oTargetInterest))
        {
           m_oApp.updatePlayerStats(m_oPlayer);
           m_oTargetInterest.setEndorsed(m_oPlayer);
           m_oPlayer.getActiveTeamMember().setTraveled(false);
           return true;
        }
        m_oPlayer.getActiveTeamMember().setTraveled(false);
        return false;
    }
    @Override
    public float desirability()
    { 
        float issueMod=0;//the measure of how close the player and SI match on issues important to the SI
        //choose prospective Special Interest
        m_oTargetInterest=null;
        for(int i=0;i<m_oApp.getSpecialInterests().size();i++)
        {
            SpecialInterest interest=m_oApp.getSpecialInterests().get(i);
            if(interest.getEndorsed()==null)
            {
                float mod=m_oApp.getGameMap().compareIssues(interest, m_oPlayer);
                if(mod>issueMod)
                {
                    issueMod=mod;
                    m_oTargetInterest=interest;
                }
            }           
        }    
        float result=-1;
        if(m_oTargetInterest==null)//no prospective interest,action is pointless
        {
            return result;
        }
        if(m_oPlayer.getFunds()<=0)//player needs money for further actions 
        {    
            result=issueMod/100;
            return result;
        }
        result=(1/(m_oPlayer.getFunds())+.5f)*(issueMod/100);
        return result;
    }
        @Override
    public String posText()
    {
        String interest=m_oTargetInterest.getName();
        String player=m_oPlayer.getName();
        String result=player +" has gained the endorsement of "+interest;
        return result;  
    }
    @Override
    public String negText()
    {
        String interest=m_oTargetInterest.getName();
        String player=m_oPlayer.getName();
        String result=player +" has failed to gain the endorsement of "+interest;
        return result;
    }
    //ivars
    SpecialInterest m_oTargetInterest;
}
