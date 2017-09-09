
package PtP_Core;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import de.lessvoid.nifty.controls.DropDown;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** A state that controls the game map
 *
 * @author Greg
 */
public class GameMap extends PtPState implements AnalogListener, Savable{
    
    public static final float CAM_SPEED=10;
    public static final float Y_POS=.05f;//height pos for avatars
    public static final float JET_Y_POS=.2f;//height pos for the jet
    public static final float AVATAR_OFFSET_Z=.2f;//places the avatar immediately in front of waypoints
    public static final int NUM_TURNS=25;

    public GameMap()
    {}
    public GameMap(Main app)
    {
        super(app,"gameMap");
        m_iTurnsLeft=NUM_TURNS-1;
        m_rNumGenerator=new Random();
        m_aActiveStateIssues=new ArrayList<BitmapText>();

        //load static models
        m_mSceneModel=app.getAssetManager().loadModel
            ("Scenes/GameMap.j3o");
        m_mBus=app.getAssetManager().loadModel
            ("Models/Campaign_Bus/Campaign_Bus.mesh.j3o");
        m_mBus.setLocalScale(.08f);
        m_mBus.setLocalTranslation(100f,Y_POS,0);
        m_mBus.addControl(new TravelControl(m_oMainApp));
        m_mBus.getControl(TravelControl.class).setSound("Sounds/Bus.wav");
        m_mJet=app.getAssetManager().loadModel
            ("Models/Global8000/Global8000.mesh.j3o");
        m_mJet.setLocalScale(.2f);
        m_mJet.setLocalTranslation(100f,JET_Y_POS,0);
        m_mJet.addControl(new TravelControl(m_oMainApp));
        m_mJet.getControl(TravelControl.class).setSound("Sounds/Jet.wav");
        
        addLight();
        m_nStateTargets=new Node("State Targets");
        m_nPlayerTargets=new Node("Player Targets");
        m_oMainApp=app;
        m_Cam=app.getCamera();
        
        m_wStateName=new BitmapText
                (m_oMainApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),false);
        m_wStateIssues=new BitmapText
                (m_oMainApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),false);
        setGameControls();
  
        generateStates();
        
        m_aIssueNames=new String[]{"Abortion","Balanced Budget","Business Subsidies",
        "Censorship","Church/State Separation","Death Penalty","Drug Laws",
        "Environment","Free Market","Free Trade","Gay Marriage","Gun Control",
        "Immigration","Military Spending","National Health Care",
        "National Security","Welfare"};
        //set up Issue of the Day billboard
        m_wIssueOfDay=setIssueOfDay();
        m_wIssueBillboard=createBitmapText("Issue of the Day: "+m_wIssueOfDay,ColorRGBA.Cyan);
        float width=m_oMainApp.getSettings().getWidth();
        float height=m_oMainApp.getSettings().getHeight();
        m_wIssueBillboard.setLocalTranslation(width/10, height,0);
        
        setTurnsBillboard(width,height);//display the curent turn  
        
        setPickTarget();
    }
    @Override
    public void write(JmeExporter ex) throws IOException
    {
        m_wActivePlayerName=m_oActivePlayer.getName();
        OutputCapsule cap=ex.getCapsule(this);
        cap.write(m_iTurnsLeft,"Turns Left",NUM_TURNS-1);
        cap.write(m_wIssueOfDay,"Issue of Day","Abortion");
        cap.write(m_wActivePlayerName, "Active Player Name", "no one");
        cap.write(m_aStates, "The States", new US_State[51]);     
    }
    @Override
    public void read(JmeImporter im) throws IOException
    {
       InputCapsule cap=im.getCapsule(this);
       m_iTurnsLeft=cap.readInt("Turns Left",NUM_TURNS-1);
       m_wIssueOfDay=cap.readString("Issue of Day","Abortion");
       m_aStates=new US_State[51];
       Savable[] states=cap.readSavableArray("The States", new US_State[51]);
       for(int s=0;s<states.length;s++)
       {
           m_aStates[s]=(US_State)states[s];
       }    
       m_wActivePlayerName=cap.readString("Active Player Name", "no one");
       
       m_wScreenName="gameMap";
       m_rNumGenerator=new Random();
       m_aActiveStateIssues=new ArrayList<BitmapText>();
       
       m_nStateTargets=new Node("State Targets");
       m_nPlayerTargets=new Node("Player Targets");
       addLight();
       m_aIssueNames=new String[]{"Abortion","Balanced Budget","Business Subsidies",
        "Censorship","Church/State Separation","Death Penalty","Drug Laws",
        "Environment","Free Market","Free Trade","Gay Marriage","Gun Control",
        "Immigration","Military Spending","National Health Care",
        "National Security","Welfare"};
               
       m_oMainApp=null;
    }
    //restore pointers to Main, active player, and gui controller after loading a game
    public void restorePointers(Main app)
    {
        m_oMainApp=app;
        m_gScreen=app.getGuiController().getScreen(m_wScreenName);
        m_Cam=app.getCamera();
        setGameControls();
        ArrayList<Player> players=app.getPlayers();
        for(int p=0;p<players.size();p++)
        {
            if(players.get(p).getName().equals(m_wActivePlayerName))
            {
                m_oActivePlayer=players.get(p);
                break;
            }    
        }
        //if active player from saved game wasn't found set the first player as active
        if(m_oActivePlayer==null)
        {
            m_oActivePlayer=players.get(0);
        }
        //load map
        m_mSceneModel=app.getAssetManager().loadModel
            ("Scenes/GameMap.j3o");
        //load jet and bus models for transit animations
        m_mBus=app.getAssetManager().loadModel
            ("Models/Campaign_Bus/Campaign_Bus.mesh.j3o");
        m_mBus.setLocalScale(.08f);
        m_mBus.setLocalTranslation(100f,Y_POS,0);
        m_mBus.addControl(new TravelControl(m_oMainApp));
        m_mBus.getControl(TravelControl.class).setSound("Sounds/Bus.wav");
        m_mJet=app.getAssetManager().loadModel
            ("Models/Global8000/Global8000.mesh.j3o");
        m_mJet.setLocalScale(.2f);
        m_mJet.setLocalTranslation(100f,JET_Y_POS,0);
        m_mJet.addControl(new TravelControl(m_oMainApp));
        m_mJet.getControl(TravelControl.class).setSound("Sounds/Jet.wav");
        //states need to recover pointer to controlling player
        for(int s=0;s<m_aStates.length;s++)
        {
           m_aStates[s].recoverControllingPlayer(app.getAllPlayers());
           m_aStates[s].recoverWaypoints(app);
        }
        
        m_wIssueBillboard=createBitmapText("Issue of the Day: "+m_wIssueOfDay,ColorRGBA.Cyan);
        float width=m_oMainApp.getSettings().getWidth();
        float height=m_oMainApp.getSettings().getHeight();
        m_wIssueBillboard.setLocalTranslation(width/10, height,0);
        setTurnsBillboard(width,height); 
        m_wStateName=new BitmapText
                (m_oMainApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),false);
        m_wStateIssues=new BitmapText
                (m_oMainApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),false);
        //set the proper neighbor states for the active players model
        m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).clear();
        m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).addAllItems
         (m_oActivePlayer.getCurrentState().getNeighbors());
        
        setPickTarget();
    }
    @Override
    public void attach()
    {
        m_oMainApp.getFlyByCamera().setEnabled(false);
      
        m_oMainApp.getRootNode().attachChild(m_mSceneModel);
        //set up for collision detection
        m_oMainApp.getRootNode().attachChild(m_nStateTargets); 
        m_oMainApp.getRootNode().attachChild(m_nPlayerTargets);
        for(int state=0;state<m_aStates.length;state++)
        {
            //Find the node that matches the states name and place its 
            //waypoint/control flag at that nodes location
            Spatial targetNode=m_oMainApp.getRootNode().getChild(m_aStates[state].getName());
            m_aStates[state].getWayPoint().setLocalTranslation(targetNode.getLocalTranslation());
            m_nStateTargets.attachChild(m_aStates[state].getWayPoint());
            m_aStates[state].setVotes((Integer)targetNode.getUserData("VOTES"));
            for(int issue=0;issue< m_aStates[state].getIssueNames().length;issue++)
            {    
                m_aStates[state].setIssue(m_aStates[state].getIssueNames()[issue]
                ,(Integer)targetNode.getUserData(m_aStates[state].getIssueNames()[issue]));
            }
        }
        
        m_Cam.setLocation(new Vector3f(0,6f,3.2f));
        m_Cam.lookAt(Vector3f.ZERO,Vector3f.UNIT_Y);
        
        m_oMainApp.getRootNode().addLight(m_Light); 
        if(m_oActivePlayer==null)//new game
        {            
            m_oActivePlayer=m_oMainApp.getPlayers().get(0);           
            setPlayers(); 
            setInitialSupport();    
        }
        else
        {
            for(int player=0;player<m_oMainApp.getAllPlayers().size();player++)
            {
                for(int peice=0;peice<m_oMainApp.getAllPlayers().get(player).getTeamMembers().size();peice++)
                {
                    Spatial avatar=m_oMainApp.getAllPlayers().get(player).getTeamMembers()
                            .get(peice).getModel();
                    m_nPlayerTargets.attachChild(avatar);
                }
            }
            //center the camera on the active player
            Vector3f newPos=m_Cam.getLocation().add
               ( m_oActivePlayer.getModel().getLocalTranslation());
            m_Cam.setLocation(new Vector3f(newPos.x,m_Cam.getLocation().y,newPos.z));
            
            placeTarget();
        }
        //add listeners
        m_oMainApp.getInputManager().addListener(this, new String[]
            {"North","South","East","West"});
        m_oMainApp.getInputManager().addListener(m_lActionListener, new String[]
        {"Target State","Target Player","Switch Unit"}); 
        
        setDropdowns();//populate the dopdown menus
    }
    @Override
    public void detach()
    { 
        m_oMainApp.getRootNode().detachAllChildren();
        m_oMainApp.getGuiNode().detachAllChildren();
        m_oMainApp.getRootNode().removeLight(m_Light);
        m_oMainApp.getInputManager().removeListener(m_lActionListener);
        m_oMainApp.getInputManager().removeListener(this);
    }
    @Override
    public void update(float tpf)
    {
        //update issue of the day
        m_oMainApp.getGuiNode().detachChild(m_wIssueBillboard);
        m_wIssueBillboard.setText("Issue of the Day: "+m_wIssueOfDay);
        m_oMainApp.getGuiNode().attachChild(m_wIssueBillboard);
        //update turn header
        m_oMainApp.getGuiNode().detachChild(m_wTurn);
        m_wTurn.setText(m_iTurnsLeft+" turns left");
        m_oMainApp.getGuiNode().attachChild(m_wTurn);
    }
    public void onAnalog(String trigger,float value,float tpf)
    {       
        if(trigger.equals("North"))
        {
          Vector3f pos=m_Cam.getLocation();
          if(pos.z >.1f)
          {
              m_Cam.setLocation(new Vector3f(pos.x,pos.y,pos.z-CAM_SPEED*value));
          }
        }
        if(trigger.equals("South"))
        {
          Vector3f pos=m_Cam.getLocation();
          if(pos.z < 7.6)
          {
              m_Cam.setLocation(new Vector3f(pos.x,pos.y,pos.z+CAM_SPEED*value));
          }
        }
        if(trigger.equals("East"))
        {
          Vector3f pos=m_Cam.getLocation(); 
          if(pos.x < 4.6)
          {
              m_Cam.setLocation(new Vector3f(pos.x+CAM_SPEED*value,pos.y,pos.z));
          }           
        }
        if(trigger.equals("West"))           
        {          
          Vector3f pos=m_Cam.getLocation();
          if(pos.x > -6.2)
          {
              m_Cam.setLocation(new Vector3f(pos.x-CAM_SPEED*value,pos.y,pos.z));
          }           
        }     
    }
    public Random getNumGenerator()
    {
        return m_rNumGenerator;
    }
    public Node getPlayersNode()
    {
        return m_nPlayerTargets;
    }
    private void addLight()
    {
        m_Light=new DirectionalLight();
        m_Light.setDirection(new Vector3f(1.8f,-2.8f,-2.8f).normalizeLocal());
        m_Light.setColor(ColorRGBA.White);
    }
    /** Create an array of all 50 states and DC **/
    private void generateStates()
    {
        //An array of state names, in sorted order, used to initialize the states
        String[] states={"Alabama","Alaska","Arkansas","Arizona","California",
    "Colorado","Connecticut","D.C.","Delaware","Florida","Georgia","Hawaii",
    "Idaho","Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine",
    "Maryland","Massachusetts","Michigan","Minnesota","Mississippi","Missouri"
    ,"Montana","Nebraska","New Hampshire","New Jersey","New Mexico","New York"
    ,"Nevada","North Carolina","North Dakota","Ohio","Oklahoma","Oregon","Pennsylvania"
    ,"Rhode Island","South Carolina","South Dakota","Tennessee","Texas","Utah",
    "Vermont","Virginia","Washington","West Virginia","Wisconsin","Wyoming"};
                
        m_aStates=new US_State[51];
        
        for(int state=0;state<states.length;state++)
        {
            m_aStates[state]=new US_State(m_oMainApp,states[state]);
        }    
    }
    /**Sets the initial camera position and key bindings for camera movement **/
    public void setGameControls()
    {
        m_oMainApp.getInputManager().clearMappings();
        m_oMainApp.getInputManager().addMapping("North", new KeyTrigger(KeyInput.KEY_W));
        m_oMainApp.getInputManager().addMapping("South", new KeyTrigger(KeyInput.KEY_S));
        m_oMainApp.getInputManager().addMapping("East", new KeyTrigger(KeyInput.KEY_D));
        m_oMainApp.getInputManager().addMapping("West", new KeyTrigger(KeyInput.KEY_A));
        m_oMainApp.getInputManager().addMapping("Switch Unit",new MouseButtonTrigger(0));
        m_oMainApp.getInputManager().addMapping("Target Player",new MouseButtonTrigger(2)); 
        m_oMainApp.getInputManager().addMapping("Target State",new MouseButtonTrigger(1));        
        
        m_lActionListener=new actionListener();
    }
    public void moveAvatar(String dropdown)
    {
      //set up variables for caculation and update
      US_State oldState=m_oActivePlayer.getCurrentState();
      String targetName=(String)m_gScreen.findNiftyControl(dropdown, DropDown.class).getSelection();
      Vector3f targetLoc=m_oMainApp.getRootNode().getChild(targetName).getLocalTranslation();
      //update the players pointer to the State it will occupy
      for(int state=0;state<m_aStates.length;state++)
      {
          if(m_aStates[state].getName().equals(targetName))
          {
              m_oActivePlayer.setCurrentState(m_aStates[state]);
              break;
          }    
      }
      //run the appropriat drive or fly animation
      Vector3f loc=new Vector3f(oldState.getWayPoint().getLocalTranslation());
      m_nPlayerTargets.detachChild(m_oActivePlayer.getModel());
      //update the border state dropdown menu
      m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).clear();
      m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).addAllItems
         (m_oActivePlayer.getCurrentState().getNeighbors());
      //set up for traveling animation
      Spatial model=null;
      if(dropdown.equals("stateDropDown"))
      {
          model=m_mJet;
      }
      else
      {
          model=m_mBus;
      }
      model.getControl(TravelControl.class).setParameters(loc, targetLoc);
      m_oMainApp.getRootNode().attachChild(model);
      model.getControl(TravelControl.class).setEnabled(true);
      model.getControl(TravelControl.class).playSound();
    }
    public void moveAI_Avatar(US_State newState)
    {
        //set up variables for caculation and update
      US_State oldState=m_oActivePlayer.getCurrentState();
      //update the players pointer to the State it will occupy
      m_oActivePlayer.setCurrentState(newState);
      //move the avatar to the state chosen in the dropdown menu
      // and adjust the position to avoid modles occupying the same space
      adjustAvatar(m_oActivePlayer.getModel(),newState);
      oldState.setOccupants(-1);
    }
    //place avatar at its target location according to home many avatars are already there
    public void adjustAvatar(Spatial model,US_State state)
    {
      Vector3f loc=state.getWayPoint().getLocalTranslation();
      int occupants=state.getOccupants();
      switch(occupants)
      {
          case 0:
            model.setLocalTranslation(loc.x,m_oActivePlayer.getModel().
              getLocalTranslation().y,loc.z+AVATAR_OFFSET_Z);
              break;  
          case 1:
              model.setLocalTranslation(loc.x+AVATAR_OFFSET_Z,m_oActivePlayer.getModel().
                getLocalTranslation().y,loc.z);
              break;
          case 2:
              model.setLocalTranslation(loc.x-AVATAR_OFFSET_Z,m_oActivePlayer.getModel().
                getLocalTranslation().y,loc.z);
              break;
          case 3:
              model.setLocalTranslation(loc.x,m_oActivePlayer.getModel().
                getLocalTranslation().y,loc.z-AVATAR_OFFSET_Z);
              break;
          case 4:
              model.setLocalTranslation(loc.x+AVATAR_OFFSET_Z,m_oActivePlayer.getModel().
                getLocalTranslation().y,loc.z+AVATAR_OFFSET_Z);
              break;      
          case 5:
              model.setLocalTranslation(loc.x+AVATAR_OFFSET_Z,m_oActivePlayer.getModel().
                getLocalTranslation().y,loc.z-AVATAR_OFFSET_Z);
              break;
          case 6:
              model.setLocalTranslation(loc.x-AVATAR_OFFSET_Z,m_oActivePlayer.getModel().
                getLocalTranslation().y,loc.z+AVATAR_OFFSET_Z);
              break;
          case 7:
              model.setLocalTranslation(loc.x-AVATAR_OFFSET_Z,m_oActivePlayer.getModel().
                getLocalTranslation().y,loc.z-AVATAR_OFFSET_Z);
              break;
          default:
              model.setLocalTranslation(loc.x,m_oActivePlayer.getModel().
                getLocalTranslation().y,loc.z+AVATAR_OFFSET_Z);
      }
      m_oActivePlayer.getCurrentState().setOccupants(1);
      m_oActivePlayer.setLocation(m_oActivePlayer.getModel().getWorldTranslation());//store new location for player picking
      //reset the dropdown for neighbors states in the "Drive" dialog to match the new State
      m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).clear();
      m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).addAllItems
         (m_oActivePlayer.getCurrentState().getNeighbors());
      m_nPlayerTargets.attachChild(model);
      placeTarget();
    }
    public int campaign()
    {
        int issueMod=compareIssueOfDay(m_oActivePlayer.getCurrentState())*10;
        int charismaMod=m_oActivePlayer.getCharMod()*10;
        int campaignRoll=m_rNumGenerator.nextInt(50)+charismaMod-issueMod;
        int updatedSupport=m_oActivePlayer.getCurrentState().calculateSupport(m_oMainApp.getAllPlayers()                
                ,m_oActivePlayer, campaignRoll);       
        return updatedSupport;
    }
    public int fundraise()
    {
        int funds=0;
        int issueMod=compareIssueOfDay(m_oActivePlayer.getCurrentState());
        int charismaMod=m_oActivePlayer.getCharMod();
        int support=m_oActivePlayer.getCurrentState().getSupport().get(m_oActivePlayer.getName());
        int attempt=m_rNumGenerator.nextInt(100);
        if(attempt<(support-issueMod +(charismaMod*10)))
        {
            funds=m_rNumGenerator.nextInt(support/10)+1+charismaMod;
            m_oActivePlayer.setFunds(funds);
        }
        return funds;
    }
    public boolean pander(SpecialInterest interest)
    {
        int playerMod=compareIssues(interest,m_oActivePlayer);
        float charMod=(m_oActivePlayer.getCharMod()+1)*.5f;
        float chance=(playerMod/2)*charMod; 
        int roll=m_rNumGenerator.nextInt(101);
        if(interest.getEndorsed()==null)// if SI isn't endosing anyone it's a straight roll
        {
            if(roll<chance)
            {
                return true;
            }
            return false;
        }
        // otherwise the player must also have a better chance than the currently endosed candidate
        int opponentMod=compareIssues(interest,interest.getEndorsed());
        float oppCharMod=(interest.getEndorsed().getCharisma()+1)*.5f;
        float oppChance=opponentMod * oppCharMod;
        if(chance>oppChance && roll<chance)
        {
            return true;
        }
        return false;
    }
    public int runPosAd(String state)
    {
        //pay for the add
        m_oActivePlayer.setFunds(-2);
        
        US_State target=null;
        for(int s=0;s<m_aStates.length;s++)
        {
            if(m_aStates[s].getName().equals(state))
            {
                target=m_aStates[s];
                break;
            }         
        }
        //success depends on stance on issues
        int issueMod=compareIssueOfDay(target)*5;
        int supportChange=m_rNumGenerator.nextInt(25)-issueMod;
        int updatedSupport=target.calculateSupport(m_oMainApp.getAllPlayers(),
                m_oActivePlayer, supportChange);
        return updatedSupport;
    }
    public int runNegAd(String state,Player opponent)
    {
        //pay for the add
        m_oActivePlayer.setFunds(-2); 
        
        US_State target=null;
        for(int s=0;s<m_aStates.length;s++)
        {
            if(m_aStates[s].getName().equals(state))
            {
                target=m_aStates[s];
                break;
            }         
        }
        //success depends on stance on issues
        int issueMod=compareIssueOfDay(opponent,target)*10;
        int supportChange=m_rNumGenerator.nextInt(10)-issueMod;
        int updatedSupport=target.calculateSupport(m_oMainApp.getAllPlayers(),
                opponent, supportChange);
        return updatedSupport;        
    }
    public int getTurnsLeft()
    {
        return m_iTurnsLeft;
    }
    public Player getActivePlayer()
    {
        return m_oActivePlayer;
    }
    public US_State getState(String name)
    {
        US_State state=null;
        for(int s=0;s<m_aStates.length;s++)
        {
            if(m_aStates[s].getName().equals(name))
            {
                state=m_aStates[s];
                break;
            }         
        } 
        return state;
    }
    public US_State[] getStates()
    { 
        return m_aStates;
    }
    //switches to the next player when its their turn
    public void updateTurn(Player player)
    {
        m_oActivePlayer=player;
        m_oActivePlayer.setActiveMember(0);
        placeTarget();
       //center camera on new active player 
       Vector3f change=m_oActivePlayer.getModel().getLocalTranslation();
       m_Cam.setLocation(new Vector3f(change.x,m_Cam.getLocation().y,change.z+3.2f));
       //update the bordser state dropdown to the state the active player is on
       m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).clear();
       m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).addAllItems
         (m_oActivePlayer.getCurrentState().getNeighbors()); 
    }
    //advances to the next turn. Called when all players have had their turn
    public void advanceTurn()
    {
        if(m_iTurnsLeft>0)
        {
            m_oActivePlayer=m_oMainApp.getPlayers().get(0);
            //reset the dropdown for neighbors states in the "Drive" dialog to match the new State
            m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).clear();
            m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).addAllItems
              (m_oActivePlayer.getCurrentState().getNeighbors());
            
            m_oActivePlayer.setActiveMember(0);
            //center the camera on the active player
            Vector3f change=m_oActivePlayer.getModel().getLocalTranslation();
            m_Cam.setLocation(new Vector3f(change.x,m_Cam.getLocation().y,change.z+3.2f));
            
            placeTarget();
            m_iTurnsLeft--;
            // if no player bids on the issue of the day, this random result will be used
            m_wIssueOfDay=setIssueOfDay();
            //process bids for issue of the day from the prev. turn
            compareBids(); 
        }
        else
        {
            //end the game
            m_oMainApp.electionDay();
        }
    }
    public void changeIssueOfDay(String issue)
    {
        m_wIssueOfDay=issue;
    }
    public void updatePlayerTeam(int newTeamSize)
    {  
        int oldTeamSize=m_oActivePlayer.getTeamMembers().size()-1;
        if(newTeamSize>oldTeamSize)//player gains party support
        {
            int increase=newTeamSize-oldTeamSize;
            for(int i=0;i<increase;i++)
            {
                int state=m_rNumGenerator.nextInt(50);
                Spatial Volunteer=m_oActivePlayer.addVolunteer(m_oMainApp.getAssetManager(),m_aStates[state]);
                m_nPlayerTargets.attachChild(Volunteer);
            }
        }
        else if(newTeamSize<oldTeamSize)//player loses partySupport
        {
            int decrease=oldTeamSize-newTeamSize;
            for(int i=0;i<decrease;i++)
            {
                Spatial volunteer=m_oActivePlayer.removeVolunteer();
                if(m_oActivePlayer.getTeamMembers().size()>1)
                {
                    m_nPlayerTargets.detachChild(volunteer);
                }
            }
        }
    }
    public class actionListener implements ActionListener
    {
      public void onAction(String trigger,boolean keyPressed,float tpf)
        {
            if(trigger.equals("Target State")&& keyPressed)
            {
                if(!m_oMainApp.getGuiNode().hasChild(m_wStateName))
                {    
                    CollisionResults results=new CollisionResults();
                    Vector2f mouseLoc=new Vector2f(m_oMainApp.getInputManager().getCursorPosition());
                    Vector3f origin=m_Cam.getWorldCoordinates(mouseLoc,0);
                    Vector3f direction=m_Cam.getWorldCoordinates(mouseLoc,1).subtractLocal(origin).normalizeLocal();
                    Ray ray=new Ray(origin,direction);
                    m_nStateTargets.collideWith(ray, results);
                    if(results.size()>0)
                    { 
                        CollisionResult target=results.getClosestCollision();
                        String stateName=target.getGeometry().getName();
                        m_wStateName.setText(stateName);
                        m_wStateName.setLocalTranslation
                                ((m_oMainApp.getSettings().getWidth()/2)-(m_wStateName.getLineWidth()/2),(m_oMainApp.getSettings().getHeight()/2)+(m_wStateName.getLineHeight()),0);
                        m_wStateName.setColor(ColorRGBA.Green);
                        m_oMainApp.getGuiNode().attachChild(m_wStateName);
                        setIssuesBillboard(stateName);
                    }
                }
            }
            if(trigger.equals("Target State")&& !keyPressed) 
            {    
                m_oMainApp.getGuiNode().detachChild(m_wStateName);
                m_oMainApp.getGuiNode().detachChild(m_wStateIssues);
                for(int i=0;i<m_aActiveStateIssues.size();i++)
                { 
                    m_oMainApp.getGuiNode().detachChild(m_aActiveStateIssues.get(i));
                }
                m_aActiveStateIssues.clear();
            } 
            if(trigger.equals("Target Player")&& !keyPressed)
            {
                CollisionResults results=new CollisionResults();
                Vector2f mouseLoc=new Vector2f(m_oMainApp.getInputManager().getCursorPosition());
                Vector3f origin=m_Cam.getWorldCoordinates(mouseLoc,10);
                Vector3f direction=m_Cam.getWorldCoordinates(mouseLoc,0).subtractLocal(origin).normalizeLocal();
                Ray ray=new Ray(origin,direction);
                m_nPlayerTargets.collideWith(ray, results);
                if(results.size()>0)
                {
                    CollisionResult target=results.getClosestCollision();
                    for(int p=0;p<m_oMainApp.getAllPlayers().size();p++)
                    {
                        for(int t=0;t<m_oMainApp.getAllPlayers().get(p).getTeamMembers().size();t++)
                        {  
                            Vector3f location=m_oMainApp.getAllPlayers().get(p).getTeamMembers().get(t).getModel().getWorldTranslation();
                            if(location.equals(target.getGeometry().getWorldTranslation()))
                            {  
                                if(!m_gScreen.findElementByName("playerInfoWindow").isVisible())
                                {
                                    m_gScreen.findElementByName("playerInfoWindow").enable();
                                    m_gScreen.findElementByName("playerInfoWindow").setVisible(true);
                                }    
                                m_oMainApp.updatePlayerStats(m_oMainApp.getAllPlayers().get(p));
                                break;
                            }
                        }
                    }
                }
            }
            if(trigger.equals("Switch Unit")&& !keyPressed)
            {
                CollisionResults results=new CollisionResults();
                Vector2f mouseLoc=new Vector2f(m_oMainApp.getInputManager().getCursorPosition());
                Vector3f origin=m_Cam.getWorldCoordinates(mouseLoc,0);
                Vector3f direction=m_Cam.getWorldCoordinates(mouseLoc,1).subtractLocal(origin).normalizeLocal();
                Ray ray=new Ray(origin,direction);
                m_nPlayerTargets.collideWith(ray, results);
                if(results.size()>0)
                {
                    CollisionResult target=results.getClosestCollision();
                    for(int unit=0;unit<m_oActivePlayer.getTeamMembers().size();unit++)
                    {  
                        Vector3f loc=m_oActivePlayer.getTeamMembers().get(unit).getLocation();
                        if(loc.equals(target.getGeometry().getWorldTranslation()))
                        {    
                            switchTeamMember(unit);
                        }
                    }
                }
            }    
        }      
    }
    //position the selection target over the avatar
    public void placeTarget()
    {
       if(m_oMainApp.getRootNode().hasChild(m_gTarget))
       {
           m_oMainApp.getRootNode().detachChild(m_gTarget);
       }
       m_gTarget.setLocalTranslation(0,0,0);
       Vector3f loc=m_oActivePlayer.getModel().getLocalTranslation();
       m_gTarget.move(loc.x-.25f,0.01f,loc.z+.25f);
       m_oMainApp.getRootNode().attachChild(m_gTarget);
    }
    private void setPlayers()
    {
       ArrayList<Player> allPlayers=m_oMainApp.getAllPlayers();
       for(int i=0;i<allPlayers.size();i++)
       {
           //put the players game peices over they waypoint of a random state       
           int randomState=m_rNumGenerator.nextInt(51);
           Spatial avatar=allPlayers.get(i).addCandidate(m_aStates[randomState]);
           adjustAvatar(avatar,m_aStates[randomState]);
           m_aStates[randomState].setOccupants(1);
           int teamSize=(allPlayers.get(i).getParty().getGoferBonus()
                       *allPlayers.get(i).getPartySupport())/100;
           if(teamSize>0)
           {
              for(int j=0;j<teamSize;j++)
              {
                   randomState=m_rNumGenerator.nextInt(51);
                   Spatial volunteer=allPlayers.get(i).addVolunteer(m_oMainApp.getAssetManager(),m_aStates[randomState]);                   
                   adjustAvatar(volunteer,m_aStates[randomState]);
                   m_aStates[randomState].setOccupants(1);
               }
            }
       }
       m_oActivePlayer.setActiveMember(0);
      //center the camera on the avatar of the first player
       Vector3f newPos=m_Cam.getLocation().add
               ( m_oActivePlayer.getAvatar().getLocalTranslation());
       m_Cam.setLocation(new Vector3f(newPos.x,m_Cam.getLocation().y,newPos.z));
       
       placeTarget();     
    }
    private void setDropdowns()
    {
        //clear the dropdowns
        m_gScreen.findNiftyControl("stateDropDown",DropDown.class).clear();
        m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).clear();
        m_gScreen.findNiftyControl("preferredIssueDropDown",DropDown.class).clear();
        m_gScreen.findNiftyControl("chosenStateDropdown",DropDown.class).clear();
        //populate state dropdowns for move-to dialogs
        ArrayList<String> states=new ArrayList<String>();
        for(int state=0;state<m_aStates.length;state++)
        {
            states.add(m_aStates[state].getName());
        }
        m_gScreen.findNiftyControl("stateDropDown",DropDown.class).addAllItems(states);
        m_gScreen.findNiftyControl("chosenStateDropdown",DropDown.class).addAllItems(states); 
        m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).addAllItems
                (m_oActivePlayer.getCurrentState().getNeighbors());
        //populate special interest dropdown for pandering
        m_oMainApp.updatePanderDropDown();
        //populate issues dropdown of issue bidding
        String[] issues=m_oActivePlayer.getIssueNames();
        for(int i=0;i<issues.length;i++)
        {
            m_gScreen.findNiftyControl("preferredIssueDropDown",DropDown.class).
                    addItem(issues[i]);            
        }
    }
    private void setIssuesBillboard(String name)
    {
        int width=m_oMainApp.getSettings().getWidth();
        int height=m_oMainApp.getSettings().getHeight();
        m_wStateIssues.setText(name+"'s Issues");
        m_wStateIssues.setColor(ColorRGBA.DarkGray);
        
        float locX=width-width/8-m_wStateIssues.getLineWidth();
        float offsetY=m_wStateIssues.getLineHeight();
        m_wStateIssues.setLocalTranslation(locX,height-offsetY,0); 
        
        if(!m_oMainApp.getGuiNode().hasChild(m_wStateIssues))
        {
            m_oMainApp.getGuiNode().attachChild(m_wStateIssues);
        }
        //list the issues for selected state that are > or < 0
        int lineCount=2;        
        for(int i=0;i<m_aStates.length;i++)
        {
            if(m_aStates[i].getName().equals(name))
            { 
                for(int j=0;j<m_aStates[i].getIssueNames().length;j++)
                {
                    String issueName=m_aStates[i].getIssueNames()[j];
                    if(!m_aStates[i].getIssues().get(issueName).equals(0))
                    {
                        String number=Integer.toString(m_aStates[i].getIssues().get(issueName));
                        BitmapText issueText=createBitmapText(issueName+": "+number,ColorRGBA.Cyan);
                        issueText.setLocalTranslation(locX,height-(offsetY*lineCount),0);
                        
                        m_aActiveStateIssues.add(issueText);
                        lineCount++;
                    }     
                }
                BitmapText end=createBitmapText("Other Issues: 0",ColorRGBA.Cyan);
                end.setLocalTranslation(locX, height-(offsetY*lineCount),0);
                m_aActiveStateIssues.add(end);
                lineCount++;
                BitmapText candidateHeader=createBitmapText("Projected Support",ColorRGBA.DarkGray);
                candidateHeader.setLocalTranslation(locX, height-(offsetY*lineCount), 0);
                lineCount++;
                m_aActiveStateIssues.add(candidateHeader);
                for(int p=0;p<m_oMainApp.getAllPlayers().size();p++)
                {
                    String playerName=m_oMainApp.getAllPlayers().get(p).getName();
                    int support=m_aStates[i].getSupport().get(m_oMainApp.getAllPlayers().get(p).getName());
                    String supportText=Integer.toString(support);
                    BitmapText playerSupport=createBitmapText(playerName+"  "+supportText+"%",ColorRGBA.Cyan);
                    playerSupport.setLocalTranslation(locX, height-(offsetY*lineCount), 0);
                    m_aActiveStateIssues.add(playerSupport);
                    lineCount++;
                }
                break;
            }
        }
        for(int i=0;i<m_aActiveStateIssues.size();i++)
        { 
            if(!m_oMainApp.getGuiNode().hasChild(m_aActiveStateIssues.get(i)))
            {
                m_oMainApp.getGuiNode().attachChild(m_aActiveStateIssues.get(i));
            }
        }
    }
    private BitmapText createBitmapText(String text, ColorRGBA color)
    {
        BitmapText txt=new BitmapText(m_oMainApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),false);
        txt.setText(text);
        txt.setColor(color);
        return txt;
    }
    private String setIssueOfDay()
    {
       int randomName=m_rNumGenerator.nextInt(m_aIssueNames.length);
       String issue=m_aIssueNames[randomName];
       return issue;
    }
    private void setTurnsBillboard(float width,float height)
    {
        String turn=Integer.toString(NUM_TURNS-m_iTurnsLeft);
        m_wTurn=createBitmapText("Turn "+turn,ColorRGBA.Green);
        m_wTurn.setLocalTranslation(width-(width/3),height,0);
    }
    //set up initial candidate support for each state
    private void setInitialSupport()
    {
        for(int s=0;s<m_aStates.length;s++)
        {
            for(int c=0;c<m_oMainApp.getAllPlayers().size();c++)
            {
                Party party=m_oMainApp.getAllPlayers().get(c).getParty();
                int support=compareIssues(m_aStates[s],party);
                //penalize 3rd party candidates
                if(!party.getName().equals("Republican")||!party.getName().equals("Democratic"))
                {
                    support/=2;
                }
                m_aStates[s].calculateSupport(m_oMainApp.getAllPlayers(), m_oMainApp.getAllPlayers().get(c),support);
            }
        }
    }
    //create a target to put under the active unit in the game
    private void setPickTarget()
    {
      Quad quad=new Quad(.5f,.5f);
      m_gTarget=new Geometry("Target",quad);
      Material mat=new Material(m_oMainApp.getAssetManager(),"Common/MatDefs/Misc/Unshaded.j3md");
      Texture t=m_oMainApp.getAssetManager().loadTexture("Textures/Target.PNG");
      mat.setTexture("ColorMap", t);
      mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
      m_gTarget.setMaterial(mat);
      m_gTarget.setQueueBucket(Bucket.Transparent);
      m_gTarget.rotate(-1.57f, 0f, 0f); 
    }
    /**creates a modifier for campaigning based of the difference between the 
    players stance and the occupied state's stance on an issue
    * 
    @Return the absolute difference between the stances **/
    private int compareIssueOfDay(US_State state)
    {
        int playerStance=m_oActivePlayer.getIssues().get(m_wIssueOfDay);
        int stateStance=state.getIssues().get(m_wIssueOfDay);
        int result=Math.abs(stateStance-playerStance);
        return result;
    }
    public int compareIssueOfDay(Player player, US_State state)
    {  
        int playerStance=player.getIssues().get(m_wIssueOfDay);
        int stateStance=state.getIssues().get(m_wIssueOfDay);
        int result=Math.abs(stateStance-playerStance);
        return result;       
    }
    /** compares the special interests stance on the issues they care about with 
     the players stance on those issues
     * 
     @Return a modifier to the candidate chances of getting an endorsement from this Special interest **/
    public int compareIssues(SpecialInterest interest,Player player)
    {
        int result=0;
        Set set=interest.getIssues().entrySet();
        Iterator iter=set.iterator();
        while(iter.hasNext())
        {
            Map.Entry entry=(Map.Entry<String, Integer>)iter.next();
            String name=(String)entry.getKey();
            int playerStance=player.getIssues().get(name);
            int interestStance=interest.getIssues().get(name);
            int difference=Math.abs(playerStance-interestStance);
            result+=difference;
        }
        int average=result/set.size();
        int mod=100-(average*25);
        return mod;
    }
    /**compares issues that matter to a state with a ginven parties stance on theose issues
     * called when setting up initial support for candidates in each state
     * @return a value modifing a candidates support in that state, the larger the number the greater the support
    **/
    private int compareIssues(US_State state,Party party)
    {
        int difference=0;
        int issueCount=0;
        for(int i=0;i<party.getIssueNames().length;i++)
        {
            if(state.getIssues().get(party.getIssueNames()[i])!=0)
            {
                int partyStance=party.getIssues().get(party.getIssueNames()[i]);
                int stateStance=state.getIssues().get(party.getIssueNames()[i]);
                difference +=Math.abs(partyStance-stateStance);
                issueCount++;
            }
        }    
        int support=(issueCount*4)-difference;
        return support;
    }
    /**determine who won control of the issue of the day, Chances are based
     on bid amount plus players charisma, but only if player bid something.**/
    private void compareBids()
    {
        int bidTotal=0;
        // tally up total bids + charisma of players who bid
        for(int player=0;player<m_oMainApp.getPlayers().size();player++)
        {
            if(m_oMainApp.getPlayers().get(player).getBid()>0)
            {
                bidTotal+=m_oMainApp.getPlayers().get(player).getBid()+
                   m_oMainApp.getPlayers().get(player).getCharisma();
            }
        }
        // if any players bid then chance of player success in controlling issue of day = players bid/total bids *100
        if(bidTotal>0)
        {
            int result=m_rNumGenerator.nextInt(bidTotal)+1;// result between 1 and 100% of bid total
            int lastBid=0;// used to shift range of probability
            for(int i=0;i<m_oMainApp.getPlayers().size();i++)
            {
                Player player=m_oMainApp.getPlayers().get(i);
                int playerRange=player.getBid()+player.getCharisma();
                // check if player may a bid and if result falls with in range of players chance 
                // if not check in the range(shifted up by prev. range) of next player
                if(player.getBid()>0 && result<=playerRange+lastBid)
                {
                    m_wIssueOfDay=player.getFavoriteIssue();
                    break;
                }
                lastBid+=playerRange;
            }
        }         
    }
    private void switchTeamMember(int index)
    {
        m_oActivePlayer.setActiveMember(index);
        placeTarget();
        //enable single actions if this team member hasn't done one
        if(m_oActivePlayer.isUsed())
        {
            m_oMainApp.getGuiController().getScreen("gameMap").findElementByName
                    ("singleActionPanel").disable();
        }
        else
        {
            m_oMainApp.getGuiController().getScreen("gameMap").findElementByName
                    ("singleActionPanel").enable();
        }
        //set the proper neighbor states for this member
        m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).clear();
        m_gScreen.findNiftyControl("borderStateDropDown",DropDown.class).addAllItems
         (m_oActivePlayer.getCurrentState().getNeighbors());
    }
    //ivars
    private Spatial m_mSceneModel;
    private Spatial m_mBus;
    private Spatial m_mJet;
    private Node m_nStateTargets;//a node for collision detection of state waypoints
    private Node m_nPlayerTargets;// a node for collion detection of candidates and their gofers
    private Player m_oActivePlayer;// the player to wich gui commands will apply
    private DirectionalLight m_Light;
    private US_State[] m_aStates;
    private Camera m_Cam;
    private Random m_rNumGenerator;
    private ActionListener m_lActionListener;
    private BitmapText m_wStateName;
    private BitmapText m_wStateIssues;//a header for listing the states importantissues
    private BitmapText m_wIssueBillboard;//displays Issue of the day
    private BitmapText m_wTurn;//dislplays current turn
    private String[] m_aIssueNames;
    private String m_wIssueOfDay;
    private String m_wActivePlayerName;
    private ArrayList<BitmapText> m_aActiveStateIssues;//list of issues for the given state that are < or> 0
    private int m_iTurnsLeft;
    private Geometry m_gTarget;
}
