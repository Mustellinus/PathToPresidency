
package PtP_Core;

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**The class that controls the players candidate and his gofers
 *
 * @author Greg
 */
public class Player extends OpinionHolder implements Savable{
    
    public static final String VOL_MODEL="Models/Volunteer/Volunteer.j3o";
    public Player(String name,int charisma, int income,Party party,
            Spatial avatar,AssetManager manager)
    {
        super();
        m_wName=name;
        m_iCharisma=charisma; 
        m_iProjectedVotes=0;
        m_iActualVotes=0;
        m_sAvatar=avatar;
        m_wAvatarPath=avatar.getKey().getName();
        m_oPartyAffiliation=party;
        m_wPartyName=m_oPartyAffiliation.getName();
        m_tFlag=manager.loadTexture(m_oPartyAffiliation.getTexturePath());
        m_iBid=0;
        m_wFavoriteIssue="not chosen";
        setPartySupport();
        m_iIncome=income; 
        m_iFunds=income;
        m_aTeam=new ArrayList<TeamMember>();
        m_aEndorsers=new ArrayList<String>();
    }
    public Player()
    {}
    @Override
    public void write(JmeExporter ex) throws IOException
   {
        OutputCapsule cap=ex.getCapsule(this);
        cap.write(m_wName,"Player Name", "Namless Dork");
        cap.write(m_wFavoriteIssue, "Favorite Issue", "not chosen");
        cap.write(m_wPartyName, "Political Party", "Independant");
        String[] endorsers=new String[m_aEndorsers.size()];
        m_aEndorsers.toArray(endorsers);
        cap.write(endorsers, "Special Interests", new String[m_aEndorsers.size()]);
        cap.write(m_iBid, "Bid", 0);
        cap.write(m_iCharisma,"Charisma", 0);
        cap.write(m_iProjectedVotes, "Projected Votes", 0);
        cap.write(m_iActualVotes, "Actual Votes", 0);
        cap.write(m_iIncome, "Income", 0);
        cap.write(m_iFunds, "Funds", 0);
        cap.write(m_iPartySupport,"Party Support",100);
        m_iTeamIndex=m_aTeam.indexOf(m_oActiveMember);
        cap.write(m_iTeamIndex, "ActiveMember",0);
        cap.write(m_wAvatarPath, "Candidate Model", "Models/Pretty_Boyd/Pretty_Boyd.j3o");
        cap.writeSavableArrayList(m_aTeam,"Team Members", new ArrayList<TeamMember>()); 
        //no savable object-integer hashmap so save the issue values in arrays
        int[] issueValues=new int[m_aIssueNames.length];
        for(int i=0;i<m_aIssueNames.length;i++)
        {
            issueValues[i]=m_aIssues.get(m_aIssueNames[i]);
        }
                
        cap.write(issueValues,"Issue Stances", new int[m_aIssueNames.length]);
    }
    @Override
    public void read(JmeImporter im) throws IOException
    {
        InputCapsule cap=im.getCapsule(this);
        m_wName=cap.readString("Player Name", "Namless Dork");
        m_wFavoriteIssue=cap.readString("Favorite Issue", "not chosen");
        m_wPartyName=cap.readString("Political Party", "Independant");
        String[] endorsers=cap.readStringArray("Special Interests", new String[1]);
        m_aEndorsers=new ArrayList<String>();
        m_aEndorsers.addAll(Arrays.asList(endorsers));
        m_iBid=cap.readInt("Bid", 0);
        m_iCharisma=cap.readInt("Charisma", 0);
        m_iProjectedVotes=cap.readInt("Projected Votes", 0);
        m_iActualVotes=cap.readInt("Actual Votes", 0);
        m_iIncome=cap.readInt("Income", 0);
        m_iFunds=cap.readInt("Funds", 0);
        m_iPartySupport=cap.readInt("Party Support",100);
        m_iTeamIndex=cap.readInt("ActiveMember",0);
        m_wAvatarPath=cap.readString("Candidate Model", "Models/Pretty_Boyd/Pretty_Boyd.j3o");
        m_aTeam=cap.readSavableArrayList("Team Members", new ArrayList<TeamMember>());
        m_oActiveMember=m_aTeam.get(m_iTeamIndex);
        //reconstitute hashmaps from saved arrays
        m_aIssueNames=new String[]{"Abortion","Balanced Budget","Business Subsidies",
        "Censorship","Church/State Separation","Death Penalty","Drug Laws",
        "Environment","Free Market","Free Trade","Gay Marriage","Gun Control",
        "Immigration","Military Spending","National Health Care",
        "National Security","Welfare"};
        
        int[] issueValues=cap.readIntArray("Issue Stances", new int[m_aIssueNames.length]);        
        for(int i=0;i<m_aIssueNames.length;i++)
        {
            m_aIssues.put(m_aIssueNames[i], issueValues[i]);
        }
        
        m_oPartyAffiliation=null;
    }
    //recover pointer to party after loading a game
    public void recoverParty(Main app)
    {
        ArrayList<Party> parties=app.getParties();
        for(int p=0;p<parties.size();p++)
        {
            if(m_wPartyName.equals(parties.get(p).getName()))
            { 
                m_oPartyAffiliation=parties.get(p);
                break;
            }
        }
        m_tFlag=app.getAssetManager().loadTexture(m_oPartyAffiliation.getTexturePath());
        m_sAvatar=app.getAssetManager().loadModel(m_wAvatarPath);
        for(int t=0;t<m_aTeam.size();t++)
        {    
            m_aTeam.get(t).restoreModels(app);
            m_aTeam.get(t).recoverState(app.getGameMap().getStates());
        }
    }
    public Spatial getAvatar()
    {
        return m_sAvatar;
    }
    public Spatial getModel()
    {
        return m_oActiveMember.getModel();
    }
    public US_State getCurrentState()
    {
        return m_oActiveMember.getState();
    }
    public boolean stateIsOccupied(US_State state)
    {
       for(int t=0;t<m_aTeam.size();t++)
       {
           if(state==m_aTeam.get(t).getState())
           {
               return true;
           }
       }
       return false;
    }
    public String getName()
    {
        return m_wName;
    }
    public int getCharisma()
    {
        return m_iCharisma;
    }
    public int getCharMod()
    {
        return m_oActiveMember.getCharisma();
    }
    public int getFunds()
    {
        return m_iFunds;
    }
    public int getIncome()
    {
        return m_iIncome;
    }
    public int getProjectedVotes()
    {
        return m_iProjectedVotes;
    }
    public int getActualVotes()
    {
        return m_iActualVotes;
    }
    public int getBid()
    {
       return m_iBid;
    }
    public String getFavoriteIssue()
    {
        return m_wFavoriteIssue;
    }
    public Texture getFlag()
    {
        return m_tFlag;
    }
    public Party getParty()
    {
        return m_oPartyAffiliation;
    }
    public ArrayList<String> getEndorsers()
    {
        return m_aEndorsers;
    }
    public TeamMember getActiveTeamMember()
    {
        return m_oActiveMember;
    }
    public int getPartySupport()
    {
        return m_iPartySupport;
    }
    public ArrayList<TeamMember> getTeamMembers()
    {
        return m_aTeam;
    }
    public boolean isUsed()
    {
        return m_oActiveMember.used();
    }
    public void setCurrentState(US_State newState)
    {
        m_oActiveMember.setState(newState);
    }
    // this is called at the end of each turn cycle
    public void pay()
    {
        m_iFunds+=m_iIncome;
    }
    //update party support each turn
    public int updatePartySupport(int newSupport)
    {
        int oldSupport=m_iPartySupport;
        m_iPartySupport=newSupport;
        //adjust income
        m_iIncome -=m_oPartyAffiliation.getIncomeBonus()*(oldSupport/100);
        m_iIncome +=m_oPartyAffiliation.getIncomeBonus()*(m_iPartySupport/100);
        //adjust team size
        int teamSize=m_oPartyAffiliation.getGoferBonus()*(m_iPartySupport/100);
        return teamSize;
    }
    //initial value adjustments for party support
    public void AdjustValues(int newSupport)
    { 
        m_iPartySupport=newSupport;
        m_iIncome+=((m_oPartyAffiliation.getIncomeBonus()*newSupport)/100);
        m_iFunds+=((m_oPartyAffiliation.getIncomeBonus()*newSupport)/100);
    }
    public void setFunds(int fundMod)
    {
       m_iFunds+=fundMod;
    }
    public void setIncome(int incomeMod)
    {
        m_iIncome+=incomeMod;
    }
    public void setProjectedVotes(int votes)
    {
        m_iProjectedVotes=votes;
    }
    public void setActualVotes(int votes)
    {
        m_iActualVotes=votes;
    }
    public void setBid(int bid)
    {
        m_iBid=bid;
        m_iFunds-=bid;//new bid is paid 
    }
    public void setFavoriteIssue(String issue)
    {
        m_wFavoriteIssue=issue;
    }
    public void setUsed()
    {
        m_oActiveMember.setUsed(true);
    }
    //resets all player peices to unused, called at the beginning of a new turn
    public void setUnused()
    {
        for(int i=0;i<m_aTeam.size();i++)
        {
            m_aTeam.get(i).setUsed(false);
        }
    }
    public void setActiveMember(int index)
    { 
        m_oActiveMember=m_aTeam.get(index);
    }
    public void setLocation(Vector3f location)
    {
        m_oActiveMember.setLocation(location);
    }
    public void setParty(Party party)
    {
        m_oPartyAffiliation=party;
    }
    public void setPartySupport()
    {
        String[] partyIssues=m_oPartyAffiliation.getIssueNames();
        int maxDifference=partyIssues.length*4;
        int tally=0;
        for(int i=0;i<partyIssues.length;i++)
        {
            String name=partyIssues[i];
            int partyVal=m_oPartyAffiliation.getIssues().get(name);
            int playerVal=this.getIssues().get(name);
            int difference=Math.abs(partyVal-playerVal);
            tally+=difference;
        }
        float percent=(float)(maxDifference-tally)/maxDifference;
        m_iPartySupport=(int)(percent*100);
    }
    
    public Spatial addCandidate(US_State state)
    {
        m_aTeam.add(new TeamMember(m_iCharisma,m_sAvatar,state));
        m_oActiveMember=m_aTeam.get(0);
        return m_sAvatar;
    }
    public Spatial addVolunteer(AssetManager manager, US_State state)
    { 
        Spatial model=manager.loadModel(m_oPartyAffiliation.getTeamModel());
        m_aTeam.add(new TeamMember(0,model,state));
        return m_aTeam.get(m_aTeam.size()-1).getModel();
    }
    public Spatial removeVolunteer()
    {
        Spatial volunteer=m_aTeam.get(m_aTeam.size()-1).getModel();
        if(m_aTeam.size()>1)
        {
            m_aTeam.remove(m_aTeam.size()-1);
        }
        return volunteer;
    }
    public void nextTeamMemeber()
    {
        int index=0;
        for(int i=0;i<m_aTeam.size();i++)
        {
            if(m_oActiveMember==m_aTeam.get(i))
            {
                index=i;
                break;
            }
        }
        if(index>=m_aTeam.size()-1)
        {
            m_oActiveMember=m_aTeam.get(0);
        }
        else
        {
            m_oActiveMember=m_aTeam.get(index+1);
        }
    }
    //ivars
    protected String m_wName;
    protected String m_wPartyName;
    protected String m_wAvatarPath;
    protected ArrayList<String> m_aEndorsers;
    protected int m_iCharisma;
    protected int m_iIncome;
    protected int m_iFunds;
    protected int m_iBid; //the players bid on issue of the day
    protected int m_iPartySupport;
    protected int m_iProjectedVotes;
    protected int m_iActualVotes;
    protected int m_iTeamIndex;//index in array of team members of active member
    protected String m_wFavoriteIssue; //the issue the player bids to make the issue of the day
    protected Spatial m_sAvatar;
    protected TeamMember m_oActiveMember; //pointer to the players active spatial on the board
    protected ArrayList<TeamMember> m_aTeam;
    protected Party m_oPartyAffiliation;
    protected Texture m_tFlag;   
}
