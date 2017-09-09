package PtP_Core;




/**Political party class
 *
 * @author Greg
 */
public class Party extends OpinionHolder
{
    public static final int INDEPENDENT=5;
    public static final int CONSTITUTION=4;
    public static final int GREEN=3;
    public static final int LIBERTARIAN=2;
    public static final int REPUBLICAN=1;
    public static final int DEMOCRATIC=0;
    public Party(int partyName)
    {
       super(); 
       m_bUsed=false;
       setParameters(partyName);
    }
    public int getGoferBonus() 
    {
        return m_iGoferBonus;
    }
    public int getIncomeBonus() 
    {
        return m_iIncomeBonus;
    }
    public String getName()
    {
        return m_wName;
    }
    public int getPointCost() 
    {
        return m_iPointCost;
    }
    public String getTexturePath() 
    {
        return m_wTexturePath;
    }
    public String getFlagPath() 
    {
        return m_wFlagPath;
    }
    public String getTeamModel()
    {
        return m_wTeamModel;
    }
    public void setUsed(boolean used)
    {
        m_bUsed=used;
    }
    public boolean isUsed()
    {
        return m_bUsed;
    }
    public boolean isCorrupt()
    {
        return m_bCorrupt;
    }
    private void setParameters(int partyName)
    {
       switch(partyName)
       {
           case DEMOCRATIC:
           {
               m_wName="Democratic";
               m_iPointCost=3;
               m_iGoferBonus=2;
               m_iIncomeBonus=2;
               m_bCorrupt=true;  
               m_wTexturePath="Textures/Democrat_Sigil.PNG";
               m_wFlagPath="Interface/Democrat_Flag.PNG";
               m_wTeamModel="Models/Volunteer/Volunteer_Democrat.j3o";           
               int[] issueMods={1,-1,1,1,0,0,1,0,-1,1,0,1,0,1,0,1,1};
               setIssues(issueMods);
               break;
           }
           case REPUBLICAN:
           {
               m_wName="Republican";
               m_iPointCost=3;
               m_iGoferBonus=1;
               m_iIncomeBonus=4;
               m_bCorrupt=true;
               m_wTexturePath="Textures/Republican_Sigil.PNG";
               m_wFlagPath="Interface/Republican_Flag.PNG";
               m_wTeamModel="Models/Volunteer/Volunteer_Republican.j3o";
               int[] issueMods={-1,-1,2,1,-1,2,2,-1,-1,1,-1,-1,-1,2,-2,2,-1};
               setIssues(issueMods); 
               break;
           }
           case LIBERTARIAN:
           {
               m_wName="Libertarian";
               m_iPointCost=1;
               m_iGoferBonus=0;
               m_iIncomeBonus=1;
               m_bCorrupt=false;
               m_wTexturePath="Textures/Libertarian_Sigil.PNG";
               m_wFlagPath="Interface/Libertarian_Flag.PNG";
               m_wTeamModel="Models/Volunteer/Volunteer_Libertarian.j3o";
               int[] issueMods={0,2,-2,-2,1,0,-2,0,2,2,2,-2,2,0,-2,-2,-2};
               setIssues(issueMods); 
               break;
           }
           case GREEN:
           {
               m_wName="Green";
               m_iPointCost=1;
               m_iGoferBonus=1;
               m_iIncomeBonus=0;
               m_bCorrupt=false;
               m_wTexturePath="Textures/Green_Sigil.PNG";
               m_wFlagPath="Interface/Green_Flag.PNG";
               m_wTeamModel="Models/Volunteer/Volunteer_Green.j3o";
               int[] issueMods={2,1,-1,1,2,-2,-1,2,-2,-2,2,2,1,-2,2,-2,2};
               setIssues(issueMods); 
               break;
           }
           case CONSTITUTION:
           {
               m_wName="Constitution";
               m_iPointCost=1;
               m_iGoferBonus=0;
               m_iIncomeBonus=1;
               m_bCorrupt=false;
               m_wTexturePath="Textures/Constitution_Sigil.PNG";
               m_wFlagPath="Interface/Constitution_Flag.PNG";
               m_wTeamModel="Models/Volunteer/Volunteer_Constitution.j3o";
               int[] issueMods={-2,1,0,1,-2,2,2,0,1,-2,-2,-2,-2,1,-2,-1,-2};
               setIssues(issueMods); 
               break;
           }    
           default:
           {
               m_wName="Independent";
               m_iPointCost=0;
               m_iGoferBonus=0;
               m_iIncomeBonus=0;
               m_bCorrupt=false;
               m_wTexturePath="Textures/Independant_Sigil.PNG";
               m_wFlagPath="Interface/Independent_Flag.PNG";
               m_wTeamModel="Models/Volunteer/Volunteer_Independent.j3o";               
           }
       }
    }
   //ivars
    private int m_iPointCost;
    private int m_iIncomeBonus;
    private int m_iGoferBonus;
    private boolean m_bCorrupt;//does this party suffer penalties when corruption is the issue of the day?
    private boolean m_bUsed; //has this party been picked by a candidate?
    private String m_wTexturePath;//location of party texture applied to state waypoints
    private String m_wFlagPath;//location of image to display with cadidate stats
    private String m_wTeamModel;
    private String m_wName;
}
