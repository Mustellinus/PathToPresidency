package PtP_Core;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Greg
 */
public class SpecialInterest {
   
    public SpecialInterest(String name)
    {
       m_wName=name;
       m_iIncomeBonus=0;
       m_iGoferBonus=0;
       m_oEndorsed=null;
       m_aIssues=new HashMap<String,Integer>();
       setParameters();
    }
    public String getName()
    {
        return m_wName;
    }
    public int getIncomeBonus()
    {
        return m_iIncomeBonus;
    }
    public int getGoferBonus()
    {
        return m_iGoferBonus;
    }
    public HashMap<String,Integer> getIssues()
    {
       return m_aIssues;
    }
    public Player getEndorsed()
    {
        return m_oEndorsed;
    }
    public void setEndorsed(Player player)
    {
        //if a cadidate was already being endorsed remove the bonuses for endorsement 
        if(m_oEndorsed != null)
        {
            m_oEndorsed.setIncome(-1*m_iIncomeBonus);
              
        }
        //add the appropriate bonuses to endorsed candidate
        m_oEndorsed=player;
        player.getEndorsers().add(m_wName);
        m_oEndorsed.setIncome(m_iIncomeBonus);        
    }
    //recover pointer to player when loading a game
    public void recoverEndorsed(ArrayList<Player> players)
    {
        boolean endorsedFound=false;
        for(int p=0;p<players.size();p++)
        {
            ArrayList <String> interests=players.get(p).getEndorsers();
            for(int e=0;e<interests.size();e++)
            {
                if(interests.get(e).equals(m_wName))
                { 
                    m_oEndorsed=players.get(e);
                    endorsedFound=true;
                    break;
                }
            }
            if(endorsedFound)
            {
                break;
            }
        }
    }      
    private void setParameters()
    {
        if(m_wName.equals("Agribusiness"))
        { 
            m_iIncomeBonus=2;
            m_aIssues.put("Business Subsidies", 2);
            m_aIssues.put("Censorship", 1);
            m_aIssues.put("Environment", -2); 
            m_aIssues.put("Free Market", 1);
            m_aIssues.put("Free Trade", -1); 
            m_aIssues.put("Immigration", 1);              
        }
        else if (m_wName.equals("Aerospace Industry"))
        {
            m_iIncomeBonus=2;
            m_aIssues.put("Free Market", 1);            
            m_aIssues.put("Military Spending",2);
        }        
        else if(m_wName.equals("Banks/Investment Firms"))
        {
            m_iIncomeBonus=4;
            m_aIssues.put("Business Subsidies", 2);
            m_aIssues.put("Free Market", 2);
            m_aIssues.put("Free Trade", 2);
            m_aIssues.put("Military Spending", 1);             
        }
        else if(m_wName.equals("Civil Rights Groups"))
        {
            m_iGoferBonus=2;
            m_aIssues.put("Censorship", 1); 
            m_aIssues.put("Death Penalty", -2); 
            m_aIssues.put("Gun Control", 2);
            m_aIssues.put("Immigration", 2);            
            m_aIssues.put("National Security", -1); 
            m_aIssues.put("Welfare", 1);             
        } 
        else if(m_wName.equals("Big Three Auto"))
        {
            m_iIncomeBonus=1;
            m_iGoferBonus=1;
            m_aIssues.put("Business Subsidies", 2);
            m_aIssues.put("Environment", -2);
            m_aIssues.put("Free Market", 1);
            m_aIssues.put("Free Trade", -1); 
            m_aIssues.put("Military Spending", 2);            
        }
        else if(m_wName.equals("Christian Fundamentalists"))
        {
            m_iIncomeBonus=1;
            m_iGoferBonus=1;
            m_aIssues.put("Abortion", -2);             
            m_aIssues.put("Censorship", 2);
            m_aIssues.put("Church/State Separation", -2);
            m_aIssues.put("Gay Marriage", -2);            
            m_aIssues.put("National Security", 1);             
        }
        else if(m_wName.equals("Coal Industry"))
        {
            m_iIncomeBonus=2;
            m_aIssues.put("Environment", -2);
            m_aIssues.put("Free Market", 2);            
        } 
        else if(m_wName.equals("Drug Industry"))
        {
            m_iIncomeBonus=3;           
            m_aIssues.put("Environment", -1);            
            m_aIssues.put("Free Trade", -1);
            m_aIssues.put("National Health Care", -1);             
        }
        else if(m_wName.equals("Environmentalists"))
        {
            m_iIncomeBonus=1;
            m_iGoferBonus=1;
            m_aIssues.put("Environment", 2); 
            m_aIssues.put("Free Market", -2);
            m_aIssues.put("Free Trade", -2);
            m_aIssues.put("Military Spending", -1);             
        }
        else if(m_wName.equals("Insurance Companies"))
        {
            m_iIncomeBonus=2; 
            m_aIssues.put("Free Market", -1);
            m_aIssues.put("National Health Care", -2);             
        }
        else if(m_wName.equals("Labor Unions"))
        {
            m_iIncomeBonus=1;
            m_iGoferBonus=1;
            m_aIssues.put("Business Subsidies", 2);
            m_aIssues.put("Free Market", -1);
            m_aIssues.put("Free Trade", -1);
            m_aIssues.put("Immigration", -2);            
        }
        else if(m_wName.equals("Left Wing Celebrity"))
        {
            m_iIncomeBonus=1;
            m_iGoferBonus=1;
            m_aIssues.put("Abortion", 2);
            m_aIssues.put("Censorship", 1);            
            m_aIssues.put("Church/State Separation", 1); 
            m_aIssues.put("Death Penalty", -2);
            m_aIssues.put("Drug Laws", -2);            
            m_aIssues.put("Environment", 2); 
            m_aIssues.put("Free Market", -2);
            m_aIssues.put("Free Trade", -2);
            m_aIssues.put("Gay Marriage", 2);            
            m_aIssues.put("Immigration", 2);
            m_aIssues.put("Gun Control", 2);
            m_aIssues.put("Military Spending", -2);
            m_aIssues.put("National Health Care", 2);             
            m_aIssues.put("National Security", -2);
            m_aIssues.put("Welfare", 2);            
        }
        else if (m_wName.equals("NRA"))
        {
            m_iGoferBonus=1;
            m_aIssues.put("Censorship", 2);
            m_aIssues.put("Gun Control",-2);
        }
        else if(m_wName.equals("Oil/Gas Industry"))
        {
            m_iIncomeBonus=3;
            m_aIssues.put("Business Subsidies", 1);
            m_aIssues.put("Environment", -2);
            m_aIssues.put("Free Market", 1);
            m_aIssues.put("Military Spending", 2);             
        } 
        else if(m_wName.equals("Right Wing Celebrity"))
        {
            m_iIncomeBonus=1; 
            m_iGoferBonus=1;
            m_aIssues.put("Abortion", -2);            
            m_aIssues.put("Censorship", 2);
            m_aIssues.put("Church/State Separation", -2);
            m_aIssues.put("Death Penalty", 2);
            m_aIssues.put("Drug Laws", 2);             
            m_aIssues.put("Environment", -1);
            m_aIssues.put("Free Market", 2);
            m_aIssues.put("Free Trade", -2);
            m_aIssues.put("Gay Marriage", -2);             
            m_aIssues.put("Immigration", -2);
            m_aIssues.put("Gun Control", -2);
            m_aIssues.put("Military Spending", 2);
            m_aIssues.put("National Health Care", -2);             
            m_aIssues.put("National Security", 2);
            m_aIssues.put("Welfare", -2);            
        }      
    }
    //ivars
    private String m_wName;
    private int m_iIncomeBonus;
    private int m_iGoferBonus;
    private HashMap<String,Integer> m_aIssues;
    private Player m_oEndorsed;
}
