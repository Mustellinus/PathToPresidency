package PtP_Core;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**A class representing the US states
 *
 * @author Greg
 */
public class US_State extends OpinionHolder implements Savable{
    
    public US_State(Main app,String nodeName)
    {
      super();
      m_wName=nodeName;
      m_aCandidateSupport=new HashMap<String,Integer>();    
      m_oControllingPlayer=null;
      m_oWinningPlayer=null;
      m_tSwingState=app.getAssetManager().loadTexture("Textures/SwingState.PNG");
      //create a mesh to serve as the waypoint/control flag for a given state
      Sphere ball=new Sphere(32,32,.1f);
      m_gSphere=new Geometry(nodeName,ball);
      ball.setTextureMode(Sphere.TextureMode.Projected);
      TangentBinormalGenerator.generate(ball); 
      Material mat=new Material(app.getAssetManager(),"Common/MatDefs/Misc/Unshaded.j3md");
      mat.setTexture("ColorMap", m_tSwingState);
      m_gSphere.setMaterial(mat);
          
      m_iElectoralVotes=3;
      m_iOccupants=0;
      m_iHighestSupportValue=0;
      
      setNeighbors();
    }
    public US_State()
    {}
    @Override
    public void write(JmeExporter ex) throws IOException
    {
        if(m_gSphere.getMaterial().getTextureParam("ColorMap").getTextureValue()==m_tSwingState)
        {
            m_bSwingState=true;
        }
        else
        {
            m_bSwingState=false;
        }
        OutputCapsule cap=ex.getCapsule(this);
        cap.write(m_iElectoralVotes,"Electoral Votes",3);
        cap.write(m_iOccupants,"Occupants",0);
        cap.write(m_iHighestSupportValue, "Highest Support Value", 0);
        cap.write(m_wName,"State Name","Alabama");
        cap.write(m_bSwingState, "Swing State?", true);
        String[] neighbors=new String[m_sNeighbors.size()];
        m_sNeighbors.toArray(neighbors);
        cap.write(neighbors, "Neighbors", new String[m_sNeighbors.size()]);
        if(m_oControllingPlayer!=null)
        {
            m_wControllerName=m_oControllingPlayer.getName();
        }
        cap.write(m_wControllerName, "Controlling Player", "swing state");
        //no savable object-integer hashmap so save the issue and candidate support values in arrays
        int[] issueValues=new int[m_aIssueNames.length];
        for(int i=0;i<m_aIssueNames.length;i++)
        {
            issueValues[i]=m_aIssues.get(m_aIssueNames[i]);
        }
                
        cap.write(issueValues,"Issue Stances", new int[m_aIssueNames.length]);
        
        String[] candidateNames=new String[m_aCandidateSupport.keySet().size()];
        m_aCandidateSupport.keySet().toArray(candidateNames);
        int[] supportValues=new int[m_aCandidateSupport.size()];
        for(int c=0;c<candidateNames.length;c++)
        {
            supportValues[c]=m_aCandidateSupport.get(candidateNames[c]);
        }
        
        cap.write(candidateNames, "Candidate Names",new String[2]);
        cap.write(supportValues, "Candidate Support",new int[2]);
    } 
    @Override
    public void read(JmeImporter im) throws IOException
    {
        InputCapsule cap=im.getCapsule(this);
        m_iElectoralVotes=cap.readInt("Electoral Votes",3);
        m_iOccupants=cap.readInt("Occupants",0);
        m_iHighestSupportValue=cap.readInt("Highest Support Value",0);
        m_wName=cap.readString("State Name","Alabama");
        m_wControllerName=cap.readString("Controlling Player", "swing state");
        m_bSwingState=cap.readBoolean("Swing State?", true);
        String[] neighbors=cap.readStringArray("Neighbors", new String[4]);
        m_sNeighbors=new ArrayList<String>();
        m_sNeighbors.addAll(Arrays.asList(neighbors));
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
        String[] candidateNames=cap.readStringArray("Candidate Names",new String[2]);
        int[] supportValues=cap.readIntArray("Candidate Support",new int[2]);
        m_aCandidateSupport=new HashMap<String,Integer>();
        for(int c=0;c<candidateNames.length;c++)
        {
            m_aCandidateSupport.put(candidateNames[c],supportValues[c]);
        }
        
        m_oWinningPlayer=null;
    }
    //restore pointer to controlling player when loading a saved game
    public void recoverControllingPlayer(ArrayList<Player> players)
    {
        for(int p=0;p<players.size();p++)
        {
            if(players.get(p).getName().equals(m_wControllerName))
            {  
                m_oControllingPlayer=players.get(p);
                break;
            }
        }
    }
    public void recoverWaypoints(Main app)
    {
        m_tSwingState=app.getAssetManager().loadTexture("Textures/SwingState.PNG");
        //create a mesh to serve as the waypoint/control flag for a given state
        Sphere ball=new Sphere(32,32,.1f);
        m_gSphere=new Geometry(m_wName,ball);
        ball.setTextureMode(Sphere.TextureMode.Projected);
        TangentBinormalGenerator.generate(ball); 
        Material mat=new Material(app.getAssetManager(),"Common/MatDefs/Misc/Unshaded.j3md");
        if(m_bSwingState)
        {
            mat.setTexture("ColorMap", m_tSwingState);
        }
        else
        {
            mat.setTexture("ColorMap", m_oControllingPlayer.getFlag());
        }
        m_gSphere.setMaterial(mat);
    }
    public String getName()
    {
        return m_wName;
    }
    public Geometry getWayPoint() 
    {
        return m_gSphere;
    }
    public Integer getVotes() 
    {
        return m_iElectoralVotes;
    }
    public Integer getOccupants()
    {
        return m_iOccupants;
    }
    public ArrayList<String> getNeighbors()
    { 
        return m_sNeighbors;
    }
    public HashMap<String,Integer> getSupport()
    {
        return m_aCandidateSupport;
    }
    public Player getControllingPlayer()
    {
        return m_oControllingPlayer;
    }
    public int getHighestSupportValue()
    {
        return m_iHighestSupportValue;
    }
    public Player getWinningPlayer()
    {
        return m_oWinningPlayer;
    }
    public void setVotes(int votes)
    {
        m_iElectoralVotes=votes;
    }
    public void setOccupants(int modifier)
    {
        m_iOccupants +=modifier;
    }
    public void setCandidateSupport(ArrayList<Player> players)
    {
       for(int candidate=0;candidate<players.size();candidate++)
       {
           m_aCandidateSupport.put(players.get(candidate).getName(), 0);
       }
    }
    /**Adds the active candidates support modifier created after a campaign or advertisinbg attempt.
     * It draws first from the undedicated portion of support,then evenly from
     * all the other candidates.
     * 
    @param pack the list of all the players
    @param player the active player
    @param support the campaign result**/
    public int calculateSupport(ArrayList<Player> pack,Player player,int support)
    {
        int unusedSupport=100;
        int prevSupport=0;
        if(m_aCandidateSupport.isEmpty())//do this on setup
        {
            for(int c=0;c<pack.size();c++)
            {
                m_aCandidateSupport.put(pack.get(c).getName(), 0);
            }
        }
        else//can only do this after intial setup
        {
            unusedSupport=100-addTotalDedidcatedSupport(pack);          
        }
        if(m_aCandidateSupport.containsKey(player.getName()))//can only do this after intial setup
        {    
            prevSupport=new Integer(m_aCandidateSupport.get(player.getName()));
        }   
        int updatedSupport=prevSupport+support;//the candidates new support value
        // change candidates support to the new result within the limits of 0-100
        if(updatedSupport>100)
        {
            m_aCandidateSupport.put(player.getName(),100);
            support-=updatedSupport-100;//support gain cant be > 100-prevSupport 
        }
        else if(updatedSupport<0)
        {
            m_aCandidateSupport.put(player.getName(),0);
            support=prevSupport*-1;//support loss cant be > prevSupport
        }
        else
        {
            m_aCandidateSupport.put(player.getName(),updatedSupport);
        }
        //if the corrected support increase exceeds the unused support take the difference
        //away from the other candidates
        if(support>0 && m_aCandidateSupport.size()>1)
        {
            if(support>unusedSupport)
            {
                int overage=support-unusedSupport;
                while(overage>0)
                {
                    for(int candidate=0;candidate<pack.size();candidate++)
                    {
                        if(overage<=0)
                        {
                            break;
                        }
                        if(!pack.get(candidate).equals(player))
                        {
                            int prevScore=m_aCandidateSupport.get(pack.get(candidate).getName());
                            if(prevScore>0)
                            {
                            int newScore=prevScore-1;
                            m_aCandidateSupport.put(pack.get(candidate).getName(), newScore);
                            overage--;
                            }
                        }
                    }    
                }
            }
        }
        //change the flag to the player with the greatest support if any candiate has any support
        boolean tie=false; //if there is a tie the swing state flag will be rendered
        m_iHighestSupportValue=0;
        for(int c=0;c<pack.size();c++)
        {
            String testPlayer=pack.get(c).getName();
            if(m_aCandidateSupport.get(testPlayer)>m_iHighestSupportValue)
            {
                m_iHighestSupportValue=m_aCandidateSupport.get(testPlayer);
                m_oControllingPlayer=pack.get(c);
                tie=false;
            }
            else if(m_aCandidateSupport.get(testPlayer)==m_iHighestSupportValue)
            {
                tie=true;
            }
        }
        if(tie)
        {
            m_gSphere.getMaterial().setTexture("ColorMap", m_tSwingState);
            m_oControllingPlayer=null;
        }
        else
        {
            m_gSphere.getMaterial().setTexture("ColorMap", m_oControllingPlayer.getFlag()); 
        }
        return m_aCandidateSupport.get(player.getName());
    }
    /** choose the winner by choosing the cadidate with the highest sum of his 
     * projected percentage of support plus 0-20% 
     * @param pack the list of all the players
     * @param numGenerator a random number generator**/
    public void pickWinner(ArrayList<Player>pack,Random numGenerator)
    {
        int prevHighNumber=0;
        for(int p=0;p<pack.size();p++)
        {
            int marginOfError=numGenerator.nextInt(11);
            int finalSupport=m_aCandidateSupport.get(pack.get(p).getName())+marginOfError;
            if(finalSupport>prevHighNumber)
            {
                prevHighNumber=finalSupport;
                m_oWinningPlayer=pack.get(p);
            }
            else if(finalSupport==prevHighNumber)//there's a tie
            {
                if(p==0)//in the case of 0 support across the board make sure a winner is still picked
                {
                    m_oWinningPlayer=pack.get(p);
                }
                else //in all other cases of a tie flip a coin to pick the winner
                {
                    int rand=numGenerator.nextInt();
                    if(rand%2==0)
                    {
                        m_oWinningPlayer=pack.get(p);
                    } 
                }
            }
        }
    }
    private int addTotalDedidcatedSupport(ArrayList<Player> pack)
    {
        int total=0;
        for(int p=0;p<pack.size();p++)
        {
            if(m_aCandidateSupport.containsKey(pack.get(p).getName()))
            {
                total+=m_aCandidateSupport.get(pack.get(p).getName());
            }
        }
        return total;    
    }
    private void setNeighbors()
    {
        m_sNeighbors=new ArrayList<String>();
        if(m_wName.equals("Alabama"))
        {
            m_sNeighbors.add("Florida");
            m_sNeighbors.add("Mississippi");
            m_sNeighbors.add("Tennessee");
            m_sNeighbors.add("Georgia");
        }
        else if(m_wName.equals("Alaska"))
        {
            m_sNeighbors.add("No Neighbors");
        }
        else if(m_wName.equals("Arkansas"))
        {
            m_sNeighbors.add("Mississippi");
            m_sNeighbors.add("Louisiana");
            m_sNeighbors.add("Texas");
            m_sNeighbors.add("Oklahoma");
            m_sNeighbors.add("Missouri");
            m_sNeighbors.add("Kentucky");            
            m_sNeighbors.add("Tennessee");           
        }
        else if(m_wName.equals("Arizona"))
        {
            m_sNeighbors.add("New Mexico");
            m_sNeighbors.add("Califorina");
            m_sNeighbors.add("Nevada");
            m_sNeighbors.add("Utah");
            m_sNeighbors.add("Colorado");            
        }
        else if(m_wName.equals("California"))
        {
            m_sNeighbors.add("Arizona");
            m_sNeighbors.add("Nevada");
            m_sNeighbors.add("Oregon");            
        }
        else if(m_wName.equals("Colorado"))
        {
            m_sNeighbors.add("Oklahoma");
            m_sNeighbors.add("New Mexico");
            m_sNeighbors.add("Arizona");            
            m_sNeighbors.add("Utah");
            m_sNeighbors.add("Wyoming");
            m_sNeighbors.add("Nebraska");
            m_sNeighbors.add("Kansas");            
        }
        else if(m_wName.equals("Connecticut"))
        {
            m_sNeighbors.add("New Jersey");
            m_sNeighbors.add("Pennsylvania");            
            m_sNeighbors.add("New York");
            m_sNeighbors.add("Vermont");
            m_sNeighbors.add("New Hampshire");
            m_sNeighbors.add("Maine");            
            m_sNeighbors.add("Massachusetts");
            m_sNeighbors.add("Rhode Island");             
        }
        else if(m_wName.equals("D.C."))
        {
            m_sNeighbors.add("Virginia");
            m_sNeighbors.add("Maryland");
            m_sNeighbors.add("West Virginia");
            m_sNeighbors.add("Pennsylvania");
            m_sNeighbors.add("New Jersey");
            m_sNeighbors.add("Delaware");            
        }
        else if(m_wName.equals("Delaware"))
        {
            m_sNeighbors.add("Maryland");
            m_sNeighbors.add("D.C.");
            m_sNeighbors.add("Virginia");
            m_sNeighbors.add("West Virginia");            
            m_sNeighbors.add("Pennsylvania");            
            m_sNeighbors.add("New Jersey");            
        }
        else if(m_wName.equals("Florida"))
        {
            m_sNeighbors.add("Alabama");            
            m_sNeighbors.add("Georgia");            
        }
        else if(m_wName.equals("Georgia"))
        {
            m_sNeighbors.add("Florida");             
            m_sNeighbors.add("Alabama");
            m_sNeighbors.add("Tennessee");             
            m_sNeighbors.add("North Carolina");
            m_sNeighbors.add("South Carolina");             
        }
        else if(m_wName.equals("Hawaii"))
        {
            m_sNeighbors.add("No Neighbors");            
        }
        else if(m_wName.equals("Idaho"))
        {
            m_sNeighbors.add("Nevada");
            m_sNeighbors.add("Oregon");
            m_sNeighbors.add("Washington");
            m_sNeighbors.add("Montana");
            m_sNeighbors.add("Wyoming");            
            m_sNeighbors.add("Utah");            
        }
        else if(m_wName.equals("Illinois"))
        {
            m_sNeighbors.add("Kentucky");
            m_sNeighbors.add("Missouri");
            m_sNeighbors.add("Iowa");
            m_sNeighbors.add("Wisconsin");
            m_sNeighbors.add("Michigan");            
            m_sNeighbors.add("Indiana");             
        }        
        else if(m_wName.equals("Indiana"))
        {
            m_sNeighbors.add("Kentucky");             
            m_sNeighbors.add("Illinois");
            m_sNeighbors.add("Michigan");
            m_sNeighbors.add("Ohio");
            m_sNeighbors.add("Wisconsin");
        }
        else if(m_wName.equals("Iowa"))
        {
            m_sNeighbors.add("Missouri");
            m_sNeighbors.add("Nebraska");             
            m_sNeighbors.add("South Dakota");
            m_sNeighbors.add("Minnesota");
            m_sNeighbors.add("Wisconsin");
            m_sNeighbors.add("Illinois");  
        }
        else if(m_wName.equals("Kansas"))
        {
            m_sNeighbors.add("Oklahoma");
            m_sNeighbors.add("Colorado");
            m_sNeighbors.add("Nebraska");
            m_sNeighbors.add("Missouri");            
        }
        else if(m_wName.equals("Kentucky"))
        {
            m_sNeighbors.add("Tennessee");
            m_sNeighbors.add("Arkansas");            
            m_sNeighbors.add("Missouri");
            m_sNeighbors.add("Illinois");
            m_sNeighbors.add("Indiana");
            m_sNeighbors.add("Ohio");
            m_sNeighbors.add("West Virginia");
            m_sNeighbors.add("Virginia");            
        }
        else if(m_wName.equals("Louisiana"))
        {
            m_sNeighbors.add("Mississippi");
            m_sNeighbors.add("Texas");
            m_sNeighbors.add("Arkansas");            
        }
        else if(m_wName.equals("Maine"))
        {
            m_sNeighbors.add("Massachusetts");
            m_sNeighbors.add("Rode Island");
            m_sNeighbors.add("Connecticut");
            m_sNeighbors.add("Vermont");            
            m_sNeighbors.add("New Hampshire");            
        }
        else if(m_wName.equals("Maryland"))
        {
            m_sNeighbors.add("Virginia"); 
            m_sNeighbors.add("West Virginia");            
            m_sNeighbors.add("Pennsylvania");
            m_sNeighbors.add("New Jersey");            
            m_sNeighbors.add("Delaware");
            m_sNeighbors.add("D.C.");            
        }        
        else if(m_wName.equals("Massachusetts"))
        {
            m_sNeighbors.add("Connecticut");            
            m_sNeighbors.add("New York");
            m_sNeighbors.add("Vermont");
            m_sNeighbors.add("New Hampshire");
            m_sNeighbors.add("Rhode Island");            
        }
        else if(m_wName.equals("Michigan"))
        {
            m_sNeighbors.add("Indiana");
            m_sNeighbors.add("Illinois");            
            m_sNeighbors.add("Wisconsin");
            m_sNeighbors.add("Ohio");            
        }
        else if(m_wName.equals("Minnesota"))
        {
            m_sNeighbors.add("Iowa");
            m_sNeighbors.add("South Dakota");
            m_sNeighbors.add("North Dakota");
            m_sNeighbors.add("Wisconsin");            
        }
        else if(m_wName.equals("Mississippi"))
        {
            m_sNeighbors.add("Alabama");
            m_sNeighbors.add("Louisiana");
            m_sNeighbors.add("Arkansas");
            m_sNeighbors.add("Tennessee");            
        }        
        else if(m_wName.equals("Missouri"))
        {          
            m_sNeighbors.add("Arkansas");
            m_sNeighbors.add("Oklahoma");
            m_sNeighbors.add("Kansas");
            m_sNeighbors.add("Nebraska");            
            m_sNeighbors.add("Iowa");
            m_sNeighbors.add("Illinois");            
            m_sNeighbors.add("Tennessee");            
        }
        else if(m_wName.equals("Montana"))
        {
            m_sNeighbors.add("Wyoming");             
            m_sNeighbors.add("Idaho");
            m_sNeighbors.add("North Dakota");
            m_sNeighbors.add("South Dakota");              
        }
        else if(m_wName.equals("Nebraska"))
        {
            m_sNeighbors.add("Kansas");
            m_sNeighbors.add("Colorado");
            m_sNeighbors.add("Wyoming");             
            m_sNeighbors.add("South Dakota"); 
            m_sNeighbors.add("Iowa");
            m_sNeighbors.add("Missouri");             
        }
        else if(m_wName.equals("New Hampshire"))
        {
            m_sNeighbors.add("Massachusetts");
            m_sNeighbors.add("Rhode Island");
            m_sNeighbors.add("Connecdticut");
            m_sNeighbors.add("New York");            
            m_sNeighbors.add("Vermont");
            m_sNeighbors.add("Maine");             
        }
        else if(m_wName.equals("New Jersey"))
        {
            m_sNeighbors.add("Delaware");
            m_sNeighbors.add("Maryland");
            m_sNeighbors.add("D.C.");            
            m_sNeighbors.add("Pennsylvania");
            m_sNeighbors.add("New York");
            m_sNeighbors.add("Massachusetts");
            m_sNeighbors.add("Rhode Island");            
            m_sNeighbors.add("Connecticut");
            
        }        
        else if(m_wName.equals("New Mexico"))
        {
            m_sNeighbors.add("Oklahoma");
            m_sNeighbors.add("Texas");
            m_sNeighbors.add("Arizona");
            m_sNeighbors.add("Utah");
            m_sNeighbors.add("Colorado");            
        }
        else if(m_wName.equals("New York"))
        {
            m_sNeighbors.add("New Jersey");          
            m_sNeighbors.add("Pennsylvania");
            m_sNeighbors.add("Vermont");
            m_sNeighbors.add("New Hampshire");            
            m_sNeighbors.add("Massachusetts");             
            m_sNeighbors.add("Connecticut");
            m_sNeighbors.add("Rhode Island");            
        }
        else if(m_wName.equals("Nevada"))
        {
            m_sNeighbors.add("Arizona");
            m_sNeighbors.add("California");
            m_sNeighbors.add("Oregon");
            m_sNeighbors.add("Idaho"); 
            m_sNeighbors.add("Utah");            
        }
        else if(m_wName.equals("North Carolina"))
        {
            m_sNeighbors.add("Virginia");
            m_sNeighbors.add("West Viriginia");            
            m_sNeighbors.add("Tennessee");
            m_sNeighbors.add("Georgia");
            m_sNeighbors.add("South Carolina");            
        }
        else if(m_wName.equals("North Dakota"))
        {
            m_sNeighbors.add("South Dakota");
            m_sNeighbors.add("Montana");
            m_sNeighbors.add("Minnesota");            
        }
        else if(m_wName.equals("Ohio"))
        {
            m_sNeighbors.add("Kentucky");
            m_sNeighbors.add("Indiana");
            m_sNeighbors.add("Michigan");
            m_sNeighbors.add("Pennsylvania");
            m_sNeighbors.add("Maryland");            
            m_sNeighbors.add("West Virginia");              
        }
        else if(m_wName.equals("Oklahoma"))
        {
            m_sNeighbors.add("Texas");
            m_sNeighbors.add("New Mexico");
            m_sNeighbors.add("Colorado");
            m_sNeighbors.add("Missouri");
            m_sNeighbors.add("Arkansas");            
        }
        else if(m_wName.equals("Oregon"))
        {
            m_sNeighbors.add("Nevada");
            m_sNeighbors.add("California");
            m_sNeighbors.add("Washington");
            m_sNeighbors.add("Idaho");            
        }
        else if(m_wName.equals("Pennsylvania"))
        {
            m_sNeighbors.add("Virginia");            
            m_sNeighbors.add("Maryland");
            m_sNeighbors.add("West Virginia");
            m_sNeighbors.add("Ohio");
            m_sNeighbors.add("New York");            
            m_sNeighbors.add("Connecticut");            
            m_sNeighbors.add("New Jersey");
            m_sNeighbors.add("Delaware");
            m_sNeighbors.add("D.C.");            
        }
        else if(m_wName.equals("Rhode Island"))
        {
            m_sNeighbors.add("Connecticut");
            m_sNeighbors.add("New York");
            m_sNeighbors.add("Vermont");
            m_sNeighbors.add("New Hampshire");
            m_sNeighbors.add("Maine");            
            m_sNeighbors.add("Massachusetts");             
        }
        else if(m_wName.equals("South Carolina"))
        {
            m_sNeighbors.add("North Carolina");             
            m_sNeighbors.add("Georgia");
            m_sNeighbors.add("Tennessee");            
        } 
        else if(m_wName.equals("South Dakota"))
        {
            m_sNeighbors.add("Nebraska");
            m_sNeighbors.add("Wyoming");            
            m_sNeighbors.add("Montana");
            m_sNeighbors.add("North Dakota");            
            m_sNeighbors.add("Minnesota");
            m_sNeighbors.add("Iowa");            
        }
        else if(m_wName.equals("Tennessee"))
        {
            m_sNeighbors.add("Alabama");             
            m_sNeighbors.add("Mississippi");
            m_sNeighbors.add("Arkansas");
            m_sNeighbors.add("Missouri");
            m_sNeighbors.add("Kentucky");
            m_sNeighbors.add("West Virginia");            
            m_sNeighbors.add("Virginia"); 
            m_sNeighbors.add("North Carolina");
            m_sNeighbors.add("South Carolina");            
            m_sNeighbors.add("Georgia");            
        }
        else if(m_wName.equals("Texas"))
        {
            m_sNeighbors.add("New Mexico");
            m_sNeighbors.add("Oklahoma");
            m_sNeighbors.add("Arkansas");
            m_sNeighbors.add("Louisiana");            
        } 
        else if(m_wName.equals("Utah"))
        {
            m_sNeighbors.add("Arizona");            
            m_sNeighbors.add("Nevada");
            m_sNeighbors.add("Idaho");
            m_sNeighbors.add("Wyoming");
            m_sNeighbors.add("Colorado");
            m_sNeighbors.add("New Mexico");            
        }
        else if(m_wName.equals("Vermont"))
        {
            m_sNeighbors.add("Massachusetts");
            m_sNeighbors.add("Rhode Island");
            m_sNeighbors.add("Connecticut");            
            m_sNeighbors.add("New York");
            m_sNeighbors.add("New Hampshire");
            m_sNeighbors.add("Maine");            
        }
        else if(m_wName.equals("Virginia"))
        {
            m_sNeighbors.add("North Carolina"); 
            m_sNeighbors.add("Tennessee");
            m_sNeighbors.add("Kentucky");
            m_sNeighbors.add("West Virginia");
            m_sNeighbors.add("Maryland");
            m_sNeighbors.add("D.C.");
            m_sNeighbors.add("Delaware");
            m_sNeighbors.add("Pennsylvania");
        }
        else if(m_wName.equals("Washington"))
        {
            m_sNeighbors.add("Oregon");
            m_sNeighbors.add("Idaho");            
        }
        else if(m_wName.equals("West Virginia"))
        {
            m_sNeighbors.add("Virginia");
            m_sNeighbors.add("North Carolina");            
            m_sNeighbors.add("Tennessee");            
            m_sNeighbors.add("Kentucky");
            m_sNeighbors.add("Ohio");
            m_sNeighbors.add("Pennsylvania");
            m_sNeighbors.add("Delaware");            
            m_sNeighbors.add("Maryland");
            m_sNeighbors.add("D.C.");                         
        }
        else if(m_wName.equals("Wisconsin"))
        {
            m_sNeighbors.add("Illinois");
            m_sNeighbors.add("Indiana");
            m_sNeighbors.add("Iowa");
            m_sNeighbors.add("Minnesota");
            m_sNeighbors.add("Michigan");            
        }
        else if(m_wName.equals("Wyoming"))
        {
            m_sNeighbors.add("Colorado");
            m_sNeighbors.add("Utah");              
            m_sNeighbors.add("Idaho");
            m_sNeighbors.add("Montana");
            m_sNeighbors.add("South Dakota");
            m_sNeighbors.add("Nebraska");              
        }        
    } 
   //ivars
       private String m_wName;
       private String m_wControllerName;
       private Geometry m_gSphere;
       private ArrayList<String> m_sNeighbors;
       private Integer m_iElectoralVotes;
       private Integer m_iOccupants;
       private int m_iHighestSupportValue;//the highest level of supportany candidate has in that state
       private Player m_oControllingPlayer;//the player with the highest support in the state
       private Player m_oWinningPlayer;
       private HashMap<String,Integer> m_aCandidateSupport;
       private Texture m_tSwingState;
       private boolean m_bSwingState;//Is a state loaded from a saved gam a swing state?
}
