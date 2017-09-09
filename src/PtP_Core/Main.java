package PtP_Core;

import com.jme3.app.SimpleApplication;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.RadioButton;
import de.lessvoid.nifty.controls.RadioButtonGroupStateChangedEvent;
import de.lessvoid.nifty.controls.ScrollPanel;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/*IVAR NOMENCLATURE-All ivars begin with "m_" plus a lower case letter 
 * indicating its type;
 * "i"=any integer type (signed,unsigned,long,ect..)
 * "f"=any floating point type (float,double)
 * "b"=booleans
 * "w"=strings
 * "a"=arrays and containers
 * "e"= java enumerations, c style enumerations start with "i"
 * "r"=random number generator
 * "m"=Meshes
 * "n"=nodes
 * "g"=gui objects
 * "l"=listeners
 * "s"=sounds
 * "o"=abstract objects such as the Main class or game state objects
 * @author Greg
 */
public class Main extends SimpleApplication implements ScreenController{

    public static final int NUM_PARTIES=6;
    public static final int MAX_PLAYERS=6;
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Path to the Presidency");
        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }
    @Override
    public void simpleInitApp() {
        
        
        
        inputManager.setCursorVisible(true);
        flyCam.setEnabled(false);
        m_aPlayers=new ArrayList<Player>();
        m_aAI_Players=new ArrayList<AI_Player>();
        m_aAllPlayers=new ArrayList<Player>();
        m_aSavedFileNames=new ArrayList<String>();
        m_iPlayerIndex=0;
        m_iNumPlayers=1;
        m_iNumAIPlayers=0;
        generateParties();        
        m_guiDisplay=new NiftyJmeDisplay
           (assetManager,inputManager,audioRenderer,guiViewPort);
        m_gNiftyGui=m_guiDisplay.getNifty();
        m_gNiftyGui.fromXml("Interface/PtP_gui.xml","start",this);      
        
        m_oGameMap=null;
        m_oMainMenu=new MainMenu(this);
        m_oSaveLoad=new SaveLoadScreen(this);
        m_oPlayerCreator=new PlayerCreator(this);
        m_oIssueScreen=new IssuesScreen(this);        
        m_oEndGame=new EndGame(this);
        
        m_oCurrentState=m_oMainMenu;
        guiViewPort.addProcessor(m_guiDisplay);
        stateManager.attach(m_oCurrentState);
        
        m_bSingleAction=false;
        
        generateSpecialInterests();
        
        //set up a directory to save files if one does not already exist
        m_wSaveDir=System.getProperty("user.home") +"/PtP_SavedGames";// a convenience for loading and saving games
        m_wSaveDir=m_wSaveDir.replace('/', File.separatorChar);
        File file=new File(m_wSaveDir);
        //make a filter so only saved game files are listed for loading in the save/load screen
        m_fFilter= new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".ptp")) {
					return true;
				} else {
					return false;
				}
			}
		};
        if(!file.exists())
        {
            file.mkdir();
        }
        String[] files=file.list(m_fFilter);
        for(int f=0;f<files.length;f++)
        {
           //leave off filter suffix in names displayed
           String name=files[f];
           String clippedName=name.substring(0, name.length()-4);
           m_aSavedFileNames.add(clippedName);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {

    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    @Override
    public void bind(Nifty nifty,Screen screen)
    {}
    @Override
    public void onStartScreen()
    {}
    @Override
    public void onEndScreen()
    {}
    public void newGame()
    {
        m_oGameMap=new GameMap(this);
        startPlayerCreator();
    }
    public void loadGame()
    {
        SaveFile game=null;
        BinaryImporter im=BinaryImporter.getInstance();
        File file=new File(m_wSaveDir+"/"+m_oSaveLoad.getLoadName()+".ptp");
        try {
            game=(SaveFile)im.load(file);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "No such file found!", ex);
            m_oSaveLoad.sayNoFile();
        }
        m_iPlayerIndex=game.getPlayerIndex();
        m_iNumPlayers=game.getNumPlayers();
        m_iNumAIPlayers=game.getNumAI_Players();
        m_bSingleAction=game.isSingleAction();
        m_aPlayers=game.getPlayers();
        m_aAI_Players=game.getAI_Players();
        for(int p=0;p<m_aAI_Players.size();p++)
            {
                m_aAI_Players.get(p).restoreAppPointer(this);
            }   
        m_aAllPlayers.clear();
        m_aAllPlayers.addAll(m_aPlayers);
        m_aAllPlayers.addAll(m_aAI_Players);
        m_oGameMap=game.getGameMap();
        for(int p=0;p<m_aAllPlayers.size();p++)
        {
            m_aAllPlayers.get(p).recoverParty(this);
        }
        for(int i=0;i<m_aSpecialInterests.size();i++)
        {
            m_aSpecialInterests.get(i).recoverEndorsed(m_aAllPlayers);
        }
        m_oGameMap.restorePointers(this);
        if(m_oGameMap.getActivePlayer().isUsed())
        {
            m_gNiftyGui.getScreen("gameMap").findElementByName("singleActionPanel").disable();
        }
        closeSaveDialog();
    }
    public void openSaveDialog()
    {
      m_oSaveLoad.setPrevState(m_oCurrentState);
      switchState("Save/Load");
    }
    public void closeSaveDialog()
    {
        PtPState state=m_oSaveLoad.getPrevState();
        switchState("Game Map");
        closeMenuPopup();               

    }
    public void saveGame() throws IOException
    {
        SaveFile game=new SaveFile(this);
        BinaryExporter exporter=BinaryExporter.getInstance();
        File file=new File(m_wSaveDir+"/"+m_oSaveLoad.getSaveName()+".ptp");
        if(file.exists())//overwrite existing file
        {
            file.delete();
        }
        try {
            exporter.save(game, file);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Error: Failed to save game!", ex);
        }
        m_aSavedFileNames.add(m_oSaveLoad.getSaveName());
        closeSaveDialog();
    }
    public void Quit()
    {
       this.stop();
    }
    public void openMenuPopup()
    {
       m_gNiftyGui.getCurrentScreen().findElementByName("menuPanel").enable();       
       m_gNiftyGui.getCurrentScreen().findElementByName("menuPanel").setVisible(true);
    }
    public void closeMenuPopup()
    {
       m_gNiftyGui.getCurrentScreen().findElementByName("menuPanel").disable();       
       m_gNiftyGui.getCurrentScreen().findElementByName("menuPanel").setVisible(false);
    }
    public void switchState(String newState)
    {
        m_oCurrentState.detach();
        stateManager.detach(m_oCurrentState);        
        if(newState.equals("Main Menu"))
        {
            m_oCurrentState=m_oMainMenu;
        }
        else if(newState.equals("Game Map"))
        {
            m_oCurrentState=m_oGameMap;
        }
        else if(newState.equals("Save/Load"))
        {
            m_oCurrentState=m_oSaveLoad;
        }
        else if(newState.equals("Player Setup"))
        {
            m_oCurrentState=m_oPlayerCreator;
        }
        else if(newState.equals("Issue Screen"))
        {
            m_oCurrentState=m_oIssueScreen;
        }
        else if(newState.equals("End Game"))
        {
            m_oCurrentState=m_oEndGame;
        }
        stateManager.attach(m_oCurrentState);
        m_gNiftyGui.gotoScreen(m_oCurrentState.getScreenName());        
        m_oCurrentState.attach();
    }
    //bring up the player popup and ask for numbers of human and AI players
    public void startPlayerCreator()
    {
        switchState("Player Setup");
        m_gNiftyGui.getScreen("playerSetup").findElementByName("playerPopup").enable();
        m_gNiftyGui.getScreen("playerSetup").findElementByName("panelsLayer").disable(); 
        m_gNiftyGui.getScreen("playerSetup").findNiftyControl("number", Label.class).setText("1");
    }
    //increas number of human players
    public void addPlayer()
    {
        String str=m_gNiftyGui.getScreen("playerSetup").findNiftyControl("number", Label.class).getText();
        int num=Integer.parseInt(str);
        if(num<MAX_PLAYERS)
        {
            num++;
            m_iNumPlayers=num;
            str=Integer.toString(num);
            m_gNiftyGui.getScreen("playerSetup").findNiftyControl("number", Label.class).setText(str);
            //make sure AI+human players < MAX_PLAYRS
            str=m_gNiftyGui.getScreen("playerSetup").findNiftyControl("AInumber", Label.class).getText();
            num= Integer.parseInt(str);
            if(num>MAX_PLAYERS-m_iNumPlayers)
            {
                num=MAX_PLAYERS-m_iNumPlayers;
                str=Integer.toString(num);
                m_gNiftyGui.getScreen("playerSetup").findNiftyControl("AInumber", Label.class).setText(str);
            }
        }
    }
    //decrease number of human players
    public void subtractPlayer()
    {
        String str=m_gNiftyGui.getScreen("playerSetup").findNiftyControl("number", Label.class).getText();
        int num=Integer.parseInt(str);
        if(num>1)
        {
            num--;         
            str=Integer.toString(num);
            m_gNiftyGui.getScreen("playerSetup").findNiftyControl("number", Label.class).setText(str);       
        }
    }
    //increas number of AI players
    public void addAIPlayer()
    {
        String str=m_gNiftyGui.getScreen("playerSetup").findNiftyControl("AInumber", Label.class).getText();
        int num=Integer.parseInt(str);
        str=m_gNiftyGui.getScreen("playerSetup").findNiftyControl("number", Label.class).getText();
        m_iNumPlayers=Integer.parseInt(str);
        if(num<MAX_PLAYERS-m_iNumPlayers)
        {
            num++;
            str=Integer.toString(num);
            m_gNiftyGui.getScreen("playerSetup").findNiftyControl("AInumber", Label.class).setText(str);
        }
    }
    //decrease number of AI players
    public void subtractAIPlayer()
    {
        String str=m_gNiftyGui.getScreen("playerSetup").findNiftyControl("AInumber", Label.class).getText();
        int num=Integer.parseInt(str);
        if(num>0)
        {
            num--;
            str=Integer.toString(num);
            m_gNiftyGui.getScreen("playerSetup").findNiftyControl("AInumber", Label.class).setText(str);
        }
    } 
    //enable human player setup
    public void startPlayerSetup()
    {
        //get number of human players
        String numPlayers=m_gNiftyGui.getScreen("playerSetup").findNiftyControl("number", Label.class).getText();
        m_iNumPlayers=Integer.parseInt(numPlayers);
        //get number of AI players
        String numAI=m_gNiftyGui.getScreen("playerSetup").findNiftyControl("AInumber", Label.class).getText();
        m_iNumAIPlayers=Integer.parseInt(numAI);
        
        m_gNiftyGui.getScreen("playerSetup").findElementByName("playerPopup").disable();
        m_gNiftyGui.getScreen("playerSetup").findElementByName("playerPopup").setVisible(false);       
        m_gNiftyGui.getScreen("playerSetup").findElementByName("panelsLayer").enable();        
    }
    public void exitIssuesScreen()
    {
        if(m_oGameMap.getActivePlayer()!=null)//player is using "flip flop" option
        {
            m_oGameMap.getActivePlayer().setIssues(m_oIssueScreen.getValues());
            m_oGameMap.getActivePlayer().updatePartySupport(m_oIssueScreen.getPartySupport());           
            switchState("Game Map");
        }
        else
        {
            switchState("Player Setup");
        }//player is creating candidate
    }
    public void cyclePlayerGeneration()
    {
        generatePlayer();
        if(m_iNumPlayers==m_aPlayers.size())
        {
            
            if(m_iNumAIPlayers>0)
            {
                generateAIPlayers();
            }
            m_aAllPlayers.addAll(m_aPlayers);
            m_aAllPlayers.addAll(m_aAI_Players);
            switchState("Game Map"); 
        } 
        else
        {
            m_oPlayerCreator.resetValues();
            m_oIssueScreen.clearValues();
        }    
    } 
    public void nextPlayer()
    {
        m_iPlayerIndex++;
        if(m_iPlayerIndex==m_aPlayers.size())
        {
            m_gNiftyGui.getScreen("gameMap").findElementByName("singleActionPanel")
                 .disable(); 
            cycleAIPlayers();
            m_iPlayerIndex=0;
            m_oGameMap.advanceTurn();
            updatePlayers();
            m_gNiftyGui.getScreen("gameMap").findElementByName("singleActionPanel")
                 .enable();  
        }
        else
        {
            m_oGameMap.updateTurn(m_aPlayers.get(m_iPlayerIndex));
            m_gNiftyGui.getScreen("gameMap").findElementByName("singleActionPanel")
                 .enable();            
        }
    }
    private void cycleAIPlayers()
    { 
        m_gNiftyGui.getScreen("gameMap").findElementByName("actionLog").setVisible(true);
        m_gNiftyGui.getScreen("gameMap").findNiftyControl("AI_logScroller",ScrollPanel.class).enable();
        m_gNiftyGui.getScreen("gameMap").findElementByName("logPanel").getElements().clear();        
        for(int p=0;p<m_aAI_Players.size();p++)
        {
            m_oGameMap.updateTurn(m_aAI_Players.get(p));
            m_aAI_Players.get(p).doTurn();
        }
    }
    private void generatePlayer()
    {
        //select player party from the array of parties and mark it as used
        Party party=null;
        if(m_oPlayerCreator.getPartyName().equals("Democratic(3)"))
        {
            party=m_aParties.get(Party.DEMOCRATIC);
        }
        else if(m_oPlayerCreator.getPartyName().equals("Republican(3)"))
        {
            party=m_aParties.get(Party.REPUBLICAN);
        }
        else if(m_oPlayerCreator.getPartyName().equals("Libertarian(1)"))
        {
            party=m_aParties.get(Party.LIBERTARIAN);
        }
        else if(m_oPlayerCreator.getPartyName().equals("Green(1)"))
        {
            party=m_aParties.get(Party.GREEN);
        }
        else if(m_oPlayerCreator.getPartyName().equals("Consitution(1)"))
        {
            party=m_aParties.get(Party.CONSTITUTION);
        }
        else
        {
            party=m_aParties.get(Party.INDEPENDENT);
        }
        party.setUsed(true);
         
        Player player=new Player(
                m_oPlayerCreator.getPlayerName(),
                m_oPlayerCreator.getCharismaValue(),
                m_oPlayerCreator.getIncomeVal(),
                party,m_oPlayerCreator.getModel(),
                assetManager);
        
        player.setIssues(m_oIssueScreen.getValues());
        if(m_oIssueScreen.getPartySupport()==-1)//issues screen was never used
        {
            player.AdjustValues(player.getPartySupport());
        }
        else//issues screen was accessed
        {
            player.AdjustValues(m_oIssueScreen.getPartySupport());
        }
        m_aPlayers.add(player);
    }
    public void generateAIPlayers()
    {
        m_oPlayerCreator.resetValues();
        Random rand=m_oGameMap.getNumGenerator();
        ArrayList<String> modelSet=m_oPlayerCreator.getModelNames();
        for(int p=0;p<m_iNumAIPlayers;p++)
        {
            int points=6;//creation points for the player
            //pick a random name and model from those available
            int modelNum=rand.nextInt(modelSet.size());
            String name=modelSet.get(modelNum);
            Spatial model=m_oPlayerCreator.getModels().get(name);
            //choose a party
            Party party=m_aParties.get(m_aParties.size()-1);// default party is Independent which is last on the list
            for(int i=0;i<m_aParties.size();i++)
            {  
                if(!m_aParties.get(i).isUsed())
                {
                    party=m_aParties.get(i);
                    break;
                }
            }
            party.setUsed(true);
            points-=party.getPointCost();
            //determine charisma
            int charisma=3;
            points-=charisma;
            //remaining points will be income
            AI_Player player=new AI_Player(name,charisma,points,party,model,this);
            //set AI players issues to that of their party
            for(int i=0;i<player.getIssueNames().length;i++)
            {
                String iName=player.getIssueNames()[i];
                player.setIssue(iName, party.getIssues().get(iName));
            }
            player.setPartySupport();//recalculate party support now that candiidates stance matches party
            player.AdjustValues(player.getPartySupport());//adjust income and funds to reflect party support
            m_aAI_Players.add(player);
            //remove the name and modle from the list
            m_oPlayerCreator.getModels().remove(name);
            modelSet.remove(modelNum);       
        }
        
    }
    //open an action dialog box by name
    public void openDialog(String dialog)
    {
        m_gNiftyGui.getScreen("gameMap").findElementByName(dialog)
                .setVisible(true);
        m_gNiftyGui.getScreen("gameMap").findElementByName(dialog)
                .enable();
        m_gNiftyGui.getScreen("gameMap").findElementByName("topPanel")
                .disable();
        m_gNiftyGui.getScreen("gameMap").findElementByName("centerPanel")
                .disable();        
        m_gNiftyGui.getScreen("gameMap").findElementByName("bottomPanel")
                .disable();    
    }
    //close an action dialog box by name
    public void closeDialog(String dialog)
    { 
        m_gNiftyGui.getScreen("gameMap").findElementByName(dialog)
                .setVisible(false);
        m_gNiftyGui.getScreen("gameMap").findElementByName(dialog)
                .disable(); 
        m_gNiftyGui.getScreen("gameMap").findElementByName("topPanel")
                .enable();
        m_gNiftyGui.getScreen("gameMap").findElementByName("centerPanel")
                .enable();         
        m_gNiftyGui.getScreen("gameMap").findElementByName("bottomPanel")
                .enable();
        if(m_oGameMap.getActivePlayer().isUsed())
        { 
            m_oGameMap.getActivePlayer().nextTeamMemeber();
            //update the border state dropdown menu
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("borderStateDropDown",DropDown.class).clear();
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("borderStateDropDown",DropDown.class).addAllItems
            (m_oGameMap.getActivePlayer().getCurrentState().getNeighbors());
            //target camera on next team member
            this.getCamera().setLocation(new Vector3f(0,this.getCamera().getLocation().y,3.2f));
            Vector3f newPos=this.getCamera().getLocation().add
                    (m_oGameMap.getActivePlayer().getModel().getLocalTranslation());
            this.getCamera().setLocation(new Vector3f(newPos.x,this.getCamera().getLocation().y,newPos.z));
            m_oGameMap.placeTarget();
            if(m_oGameMap.getActivePlayer().isUsed())
            {
                m_gNiftyGui.getScreen("gameMap").findElementByName
                        ("singleActionPanel").disable();
            }
        }
    }
    public void toggleLog()
    {
        if(m_gNiftyGui.getScreen("gameMap").findElementByName("actionLog").isVisible())
        {  
            m_gNiftyGui.getScreen("gameMap").findElementByName("actionLog").setVisible(false);
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("AI_logScroller",ScrollPanel.class).disable();
        }
        else
        {
           m_gNiftyGui.getScreen("gameMap").findElementByName("actionLog").setVisible(true);
           m_gNiftyGui.getScreen("gameMap").findNiftyControl("AI_logScroller",ScrollPanel.class).enable();
        }
    }
    public void startCostlyAction(String dialog)
    {
        if(dialog.equals("flyPanel"))
        {
            if(m_oGameMap.getActivePlayer().getFunds()>1)
            {
                openDialog(dialog);
            }
            else
            {
                openDialog("resultDialog");        
                m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                        setText("Not enough funds!"); 
            }            
        }
        else
        {
            if(m_oGameMap.getActivePlayer().getFunds()>0)
            {
                openDialog(dialog);
            }
            else
            {
                openDialog("resultDialog");        
                m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                        setText("Not enough funds!");            
            }              
        }
    }
    public void flyAvatar()
    {
        m_gNiftyGui.getScreen("gameMap").findElementByName("flyPanel")
                .setVisible(false);
        m_gNiftyGui.getScreen("gameMap").findElementByName("flyPanel")
                .disable(); 
        m_gNiftyGui.getScreen("gameMap").findElementByName("topPanel")
                .disable();
        m_gNiftyGui.getScreen("gameMap").findElementByName("centerPanel")
                .disable();        
        m_gNiftyGui.getScreen("gameMap").findElementByName("bottomPanel")
                .disable(); 
        m_oGameMap.getActivePlayer().setFunds(-2);        
        m_oGameMap.moveAvatar("stateDropDown");
        m_oGameMap.getActivePlayer().setUsed();
        updatePlayerStats(m_oGameMap.getActivePlayer());
    }
    public void driveAvatar()
    {
        m_gNiftyGui.getScreen("gameMap").findElementByName("drivePanel")
                .setVisible(false);
        m_gNiftyGui.getScreen("gameMap").findElementByName("drivePanel")
                .disable(); 
        m_gNiftyGui.getScreen("gameMap").findElementByName("topPanel")
                .disable();
        m_gNiftyGui.getScreen("gameMap").findElementByName("centerPanel")
                .disable();        
        m_gNiftyGui.getScreen("gameMap").findElementByName("bottomPanel")
                .disable(); 
        m_oGameMap.getActivePlayer().setFunds(-1);        
        m_oGameMap.moveAvatar("borderStateDropDown");
        m_oGameMap.getActivePlayer().setUsed();       
        updatePlayerStats(m_oGameMap.getActivePlayer());
    }
    public void doCampaign()
    { 
        if(m_oGameMap.getActivePlayer().getFunds()>0)
        {
            m_oGameMap.getActivePlayer().setFunds(-1);
            Player player=m_oGameMap.getActivePlayer();
            int prevSupport=new Integer(player.getCurrentState().getSupport().get(player.getName()));
            int newSupport=m_oGameMap.campaign();
            String start=Integer.toString(prevSupport);
            String end=Integer.toString(newSupport);
            String state=m_oGameMap.getActivePlayer().getCurrentState().getName();
            openDialog("resultDialog");        
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                      setText("Your support in "+ state+" has gon from "+start + "% to "+end+"%");
            m_oGameMap.getActivePlayer().setUsed();
            updatePlayerStats(m_oGameMap.getActivePlayer());
        }
        else
        {
            openDialog("resultDialog");        
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                      setText("Not enough funds!");             
        }
    }
    public void doFundraise()
    {
        int addedFunds=m_oGameMap.fundraise();
        String newFunds=Integer.toString(addedFunds);
        openDialog("resultDialog");
        m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                setText("You have raised "+newFunds+" campaign credits");
        m_oGameMap.getActivePlayer().setUsed();
        updatePlayerStats(m_oGameMap.getActivePlayer());
    }
    public void doPander()
    {
       SpecialInterest interest=null;
       String name=(String)m_gNiftyGui.getScreen("gameMap").findNiftyControl
               ("specialInterestDropdown", DropDown.class).getSelection();
       for(int i=0;i<m_aSpecialInterests.size();i++)
       {
           if(m_aSpecialInterests.get(i).getName().equals(name))
           {
               interest=m_aSpecialInterests.get(i);
               break;
           }
       }
       String playerName=m_oGameMap.getActivePlayer().getName();
       String interestName=interest.getName();
       closeDialog("panderPanel");
       boolean result=m_oGameMap.pander(interest);
       if(result)
       {
        openDialog("resultDialog");
        m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                setText("You've gained the endorsement of "+interestName);
        interest.setEndorsed(m_oGameMap.getActivePlayer());
        updatePanderDropDown();
       }
       else
       {
        openDialog("resultDialog");
        m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                setText("You failed to gain an endorsement");           
       }
       m_oGameMap.getActivePlayer().setUsed();
       updatePlayerStats(m_oGameMap.getActivePlayer());
    }
    public void updatePanderDropDown()
    {
        //set appopriate dropdown options.Start by clearing the previous entries
       m_gNiftyGui.getScreen("gameMap").findNiftyControl
                ("specialInterestDropdown",DropDown.class).clear();
       //create a list of special interests that are not already endorsing the candidate.
       ArrayList<String> interests=new ArrayList<String>();
       for(int i=0;i<m_aSpecialInterests.size();i++)
       {
           if(m_aSpecialInterests.get(i).getEndorsed()!=m_oGameMap.getActivePlayer())
           {
               interests.add(m_aSpecialInterests.get(i).getName());
           }
       }
       m_gNiftyGui.getScreen("gameMap").findNiftyControl
               ("specialInterestDropdown",DropDown.class).addAllItems(interests);        
    }
    //recruit a volunteer
    public void doRecruit()
    {
        int randNum=m_oGameMap.getNumGenerator().nextInt(101);
        int charMod=m_oGameMap.getActivePlayer().getCharisma()*5;
        int targetNum=100-(m_oGameMap.getActivePlayer().getCurrentState().getSupport()
                .get(m_oGameMap.getActivePlayer().getName())/4);
        if((randNum+charMod)>targetNum)
        {
            openDialog("resultDialog");        
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                      setText("You have a new Volunteer!");
            US_State state=m_oGameMap.getActivePlayer().getCurrentState();
            Spatial Volunteer=m_oGameMap.getActivePlayer().addVolunteer(assetManager, state);
            m_oGameMap.getPlayersNode().attachChild(Volunteer);
            m_oGameMap.adjustAvatar(Volunteer,state);
            state.setOccupants(1);
        }
        else
        {
            openDialog("resultDialog");        
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                      setText("Recruitment Failed!"); 
        }
        m_oGameMap.getActivePlayer().setUsed(); 
    }
    public boolean doAI_Recruit()
    {
        int randNum=m_oGameMap.getNumGenerator().nextInt(101);
        int charMod=m_oGameMap.getActivePlayer().getCharisma()*5;
        int targetNum=100-(m_oGameMap.getActivePlayer().getCurrentState().getSupport()
                .get(m_oGameMap.getActivePlayer().getName())/4);
        if((randNum+charMod)>targetNum)
        {
            US_State state=m_oGameMap.getActivePlayer().getCurrentState();
            Spatial Volunteer=m_oGameMap.getActivePlayer().addVolunteer(assetManager, state);
            m_oGameMap.getPlayersNode().attachChild(Volunteer);
            m_oGameMap.adjustAvatar(Volunteer,state);
            state.setOccupants(1);
            return true;
        } 
        return false;
    }
    //open the bid dialog and populate it with the right information
    public void doBid()
    {        
        String issue=m_oGameMap.getActivePlayer().getFavoriteIssue();
        String lastBid=Integer.toString(m_oGameMap.getActivePlayer().getBid());
        openDialog("bidDialog");        
        m_gNiftyGui.getScreen("gameMap").findNiftyControl
                ("issueText", Label.class).setText("Your preferred issue is "+issue);
        m_gNiftyGui.getScreen("gameMap").findNiftyControl
                ("bidText", Label.class).setText("Your bid was "+lastBid);
        m_gNiftyGui.getScreen("gameMap").findNiftyControl
                ("bidDisplay", Label.class).setText(lastBid); 
        int bidMoney=m_oGameMap.getActivePlayer().getFunds()+m_oGameMap.getActivePlayer().getBid();
        String bidMoneyTxt=Integer.toString(bidMoney);
        m_gNiftyGui.getScreen("gameMap").findNiftyControl
                ("moneyTotal", Label.class).setText("You have "+bidMoneyTxt+" credits to bid with");        
    }
    //modify the bid with the + and - buttons in the bid dialog, add=add other=subratct
    public void changeBid(String add)
    {
        String currentBidTxt=m_gNiftyGui.getScreen("gameMap").findNiftyControl
                ("bidDisplay", Label.class).getText(); 
        int currentBid=Integer.parseInt(currentBidTxt);
        int maxBid=m_oGameMap.getActivePlayer().getFunds()+m_oGameMap.getActivePlayer().getBid();
        //as long as the bid stays between 0 and the most the player can bid, 
        //adjust it and reset the display
        if(add.equals("add"))//+button was pressed
        {
            if(currentBid<maxBid)//don't let player bid more than he has
            {
                currentBid++;
                currentBidTxt=Integer.toString(currentBid);
                m_gNiftyGui.getScreen("gameMap").findNiftyControl
                    ("bidDisplay", Label.class).setText(currentBidTxt);
            }
        }
        else if(currentBid>0)//- button was pressed, and don't allow negative bids
        {
            currentBid--;
            currentBidTxt=Integer.toString(currentBid);
            m_gNiftyGui.getScreen("gameMap").findNiftyControl
                ("bidDisplay", Label.class).setText(currentBidTxt);
        }
        //do nothing if the bid can't be changed
    }
    //replace players old choice for issue of the day and bid with the new one
    public void acceptBid()
    {
        String issue=(String)m_gNiftyGui.getScreen("gameMap").findNiftyControl
                ("preferredIssueDropDown",DropDown.class).getSelection();
        String bidTxt=m_gNiftyGui.getScreen("gameMap").findNiftyControl
                ("bidDisplay", Label.class).getText();
        int bid=Integer.parseInt(bidTxt);
        //refund players old bid then set the bid to the new amount
        m_oGameMap.getActivePlayer().setFunds(m_oGameMap.getActivePlayer().getBid());
        m_oGameMap.getActivePlayer().setBid(bid);
        m_oGameMap.getActivePlayer().setFavoriteIssue(issue);
        closeDialog("bidDialog");
        updatePlayerStats(m_oGameMap.getActivePlayer());
    }
    public void openAdPanel()
    {
        if(m_oGameMap.getActivePlayer().getFunds()>1)
        {
           openDialog("adPanel");
            //update the opponent dropdown 
           m_gNiftyGui.getScreen("gameMap").findNiftyControl("opponentDropdown", DropDown.class).clear();       
           for(int name=0;name<m_aAllPlayers.size();name++)
            {
            // add name to the list of opponents if it is not the active player
                if(m_aAllPlayers.get(name)!=m_oGameMap.getActivePlayer())
                {  
                    m_gNiftyGui.getScreen("gameMap").findNiftyControl
                    ("opponentDropdown", DropDown.class).addItem(m_aAllPlayers.get(name).getName());
                }
            }
            //set default ad type to pos
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("posButton",
                    RadioButton.class).select();        
            //default setting for opponent dropdown should be inactive since default ad type is positive
            m_gNiftyGui.getScreen("gameMap").findElementByName("adTargetPanel")
                    .disable();
            m_gNiftyGui.getScreen("gameMap").findElementByName("adTargetPanel")
                    .setVisible(false); 
        }
        else
        {
            openDialog("resultDialog");
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText", Label.class)
                    .setText("Not enough funds");
        }
        
    }
    public void advertise()
    {
        int prevSupport=0;
        int newSupport=0;
        String state=(String)m_gNiftyGui.getScreen("gameMap").findNiftyControl
                ("chosenStateDropdown",DropDown.class).getSelection();      
        if(m_gNiftyGui.getScreen("gameMap").findElementByName("adTargetPanel").isVisible())
        { 
            String opponent=(String)m_gNiftyGui.getScreen("gameMap").findNiftyControl("opponentDropdown", DropDown.class)
                    .getSelection();
            prevSupport=new Integer(m_oGameMap.getState(state).getSupport().get(opponent));
            newSupport=m_oGameMap.runNegAd(state, getPlayer(opponent));
            String start=Integer.toString(prevSupport);
            String end=Integer.toString(newSupport);
            closeDialog("adPanel");
            openDialog("resultDialog");        
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                setText(opponent+"'s support in "+ state+" has gon from "+start + "% to "+end+"%");             
        }
        else
        {
            prevSupport=new Integer(m_oGameMap.getState(state).getSupport().get
                    (m_oGameMap.getActivePlayer().getName()));
            newSupport=m_oGameMap.runPosAd(state);
            String start=Integer.toString(prevSupport);
            String end=Integer.toString(newSupport);
            closeDialog("adPanel");
            openDialog("resultDialog");        
            m_gNiftyGui.getScreen("gameMap").findNiftyControl("dialogText",Label.class).
                setText("Your support in "+ state+" has gon from "+start + "% to "+end+"%");      
        }
        updatePlayerStats(m_oGameMap.getActivePlayer());
    }
    @NiftyEventSubscriber(id="adRadioButtonGroup")
    public void onAdSelected(final String id, final RadioButtonGroupStateChangedEvent event)
    {
        if(event.getSelectedId().equals("posButton"))
        {
            m_gNiftyGui.getScreen("gameMap").findElementByName("adTargetPanel")
                    .disable();
            m_gNiftyGui.getScreen("gameMap").findElementByName("adTargetPanel")
                    .setVisible(false);
        }
        else
        {
            m_gNiftyGui.getScreen("gameMap").findElementByName("adTargetPanel")
                    .enable(); 
            m_gNiftyGui.getScreen("gameMap").findElementByName("adTargetPanel")
                    .setVisible(true);            
        }
    }
    @NiftyEventSubscriber(id="saveList")
    public void onSelection(final String id, final ListBoxSelectionChangedEvent<String> event)
    {
        List<String> selections=event.getSelection();
        if(!selections.isEmpty())
        {    
            String fileName=selections.get(0);
            m_gNiftyGui.getCurrentScreen().findNiftyControl("saveName", TextField.class).setText(fileName);
        }
    }
    public void matchParty()
    {
        Party party=null;
        
        if(m_oGameMap.getActivePlayer()==null)//enetered issue screen during player creation
        { 
            if(m_oPlayerCreator.getPartyName().equals("Democratic(3)"))
            {
                party=m_aParties.get(Party.DEMOCRATIC);
            }
            else if(m_oPlayerCreator.getPartyName().equals("Republican(3)"))
            {
                party=m_aParties.get(Party.REPUBLICAN);
            }
            else if(m_oPlayerCreator.getPartyName().equals("Libertarian(1)"))
            {
                party=m_aParties.get(Party.LIBERTARIAN);
            }
            else if(m_oPlayerCreator.getPartyName().equals("Green(1)"))
            {
                party=m_aParties.get(Party.GREEN);
            }
            else if(m_oPlayerCreator.getPartyName().equals("Constitution(1)"))
            {
                party=m_aParties.get(Party.CONSTITUTION);
            }
            else
            {
                party=m_aParties.get(Party.INDEPENDENT);
            }
            m_oIssueScreen.matchParty(party);
        }
        else//entered issue screen during the game
        {
            party=m_oGameMap.getActivePlayer().getParty();
            m_oIssueScreen.matchParty(party);
        }
    }
    public void doFlipFlop()
    {
        switchState("Issue Screen");
        m_oIssueScreen.setValues(m_oGameMap.getActivePlayer());
        updatePlayerStats(m_oGameMap.getActivePlayer());
    }
    /** Figures out what percentage of party support the candidate gets,
     * based on how close the candidates stance on issues is to his party
     * @return a value between 0-100%
     **/
    public int calculatePartySupport()
    {
        Party party=null;
        if(m_oGameMap.getActivePlayer()==null)//game hasn't started,issue screen is being accessed from player setup
        { 
            if(m_oPlayerCreator.getPartyName().equals("Democratic(3)"))
            {
                party=m_aParties.get(Party.DEMOCRATIC);
            }
            else if(m_oPlayerCreator.getPartyName().equals("Republican(3)"))
            {
                party=m_aParties.get(Party.REPUBLICAN);
            }
            else if(m_oPlayerCreator.getPartyName().equals("Libertarian(1)"))
            {
                party=m_aParties.get(Party.LIBERTARIAN);
            }
            else if(m_oPlayerCreator.getPartyName().equals("Green(1)"))
            {
                party=m_aParties.get(Party.GREEN);
            }
            else if(m_oPlayerCreator.getPartyName().equals("Constitution(1)"))
            {
                party=m_aParties.get(Party.CONSTITUTION);
            }
            else
            {
                party=m_aParties.get(Party.INDEPENDENT);
            }
        }
        else //game has started, issue sceen is being accessed by active player
        {
            party=m_oGameMap.getActivePlayer().getParty();
        }
        int support=compareIssues(party);
        return support;
    }
    //update the display of player stats
    public void updatePlayerStats(Player player)
    {
        int votes=0;
        for(int s=0;s<m_oGameMap.getStates().length;s++)//tally the players projected votes
        {
            if(m_oGameMap.getStates()[s].getControllingPlayer()==player)
            { 
                votes+=m_oGameMap.getStates()[s].getVotes();
            }
        }
        String totVotes=Integer.toString(votes);
        String charisma=Integer.toString(player.getCharisma());
        String income=Integer.toString(player.getIncome());
        String funds=Integer.toString(player.getFunds());
        m_gNiftyGui.getScreen("gameMap").findNiftyControl("nameText", Label.class)
                .setText(player.getName());
        m_gNiftyGui.getScreen("gameMap").findNiftyControl("partyText", Label.class)
                .setText(player.getParty().getName());
        m_gNiftyGui.getScreen("gameMap").findNiftyControl("charismaText", Label.class)
                .setText(charisma);
        m_gNiftyGui.getScreen("gameMap").findNiftyControl("incomeText", Label.class)
                .setText(income);
        m_gNiftyGui.getScreen("gameMap").findNiftyControl("fundsText", Label.class)
                .setText(funds);
        m_gNiftyGui.getScreen("gameMap").findNiftyControl("votesText", Label.class)
                .setText(totVotes+" / 538");
        for(int i=0;i<player.getIssueNames().length;i++)
        {
            int value=player.getIssues().get(player.getIssueNames()[i]);
            String issueValue=Integer.toString(value);
            m_gNiftyGui.getScreen("gameMap").findNiftyControl(player.getIssueNames()[i], Label.class)
                .setText(issueValue);           
        }
        NiftyImage image=m_gNiftyGui.getRenderEngine().createImage( m_gNiftyGui.getScreen("gameMap"),player.getParty().getFlagPath(), false);
        m_gNiftyGui.getScreen("gameMap").findElementByName("partyImage").getRenderer(ImageRenderer.class).setImage(image);
    }
    public void electionDay()
    {
        //pick projected winner
        Player projectedWinner=null;
        for(int p=0;p<m_aAllPlayers.size();p++)
        {
            int votes=0;
            for(int s=0;s<m_oGameMap.getStates().length;s++)//tally the players projected votes
            {
                if(m_oGameMap.getStates()[s].getControllingPlayer()==m_aAllPlayers.get(p))
                { 
                    votes+=m_oGameMap.getStates()[s].getVotes();
                }
            }
            m_aAllPlayers.get(p).setProjectedVotes(votes);
            if(projectedWinner==null)
            {
                projectedWinner=m_aAllPlayers.get(p);
            }
            else if(m_aAllPlayers.get(p).getProjectedVotes()>projectedWinner.getProjectedVotes())
            {
                projectedWinner=m_aAllPlayers.get(p);
            }
        }
        //pick actual winner
        Player actualWinner=m_aAllPlayers.get(0);
        for(int s=0;s<m_oGameMap.getStates().length;s++)
        {  
            m_oGameMap.getStates()[s].pickWinner(m_aAllPlayers, m_oGameMap.getNumGenerator());
        }
        for(int p=0;p<m_aAllPlayers.size();p++)
        {
            int votes=0;
            for(int s=0;s<m_oGameMap.getStates().length;s++)
            {  
                if(m_oGameMap.getStates()[s].getWinningPlayer()==m_aAllPlayers.get(p))
                {
                    votes+=m_oGameMap.getStates()[s].getVotes();
                }
            }
            m_aAllPlayers.get(p).setActualVotes(votes);
            if(actualWinner==null)
            {
                actualWinner=m_aAllPlayers.get(p);
            }
            else if(m_aAllPlayers.get(p).getActualVotes()>actualWinner.getActualVotes())
            {
                actualWinner=m_aAllPlayers.get(p);
            }
        }
        m_oEndGame.setBillboards(projectedWinner, actualWinner);
        switchState("End Game");
    }
    public void winner()
    {
        m_oEndGame.declareWinner();
    }
    public void endGame()
    {
        m_aPlayers.clear();
        m_aAI_Players.clear();
        m_aAllPlayers.clear();
        
        m_oPlayerCreator.cleanup();
        m_oPlayerCreator=new PlayerCreator(this);
        m_oIssueScreen.cleanup();
        m_oIssueScreen=new IssuesScreen(this);
        m_oEndGame.cleanup();
        m_oEndGame=new EndGame(this);
        m_oGameMap.cleanup();
        m_oGameMap=null;
        
        switchState("Main Menu");
    }
    public void viewAvatar(String direction)
    {
        if(direction.equals("forward"))
        {
            m_oPlayerCreator.nextModel();
        }
        else
        {
            m_oPlayerCreator.prevModel();
        }
    }
    public boolean isSingleAction()
    {
        return m_bSingleAction;
    }
    public int getPlayerIndex()
    {
        return m_iPlayerIndex;
    }
    public int getNumPlayers()
    {
        return m_iNumPlayers;
    }
    public int getNumAI_Players()
    {
        return m_iNumAIPlayers;
    }
    public AppSettings getSettings()
    {
        return settings;
    }
    public ArrayList<Player> getPlayers()
    {
        return m_aPlayers;
    }
    public ArrayList<AI_Player> getAIPlayers()
    {
        return m_aAI_Players;
    }
    public ArrayList<Player> getAllPlayers()
    {
        return m_aAllPlayers;
    }
    public ArrayList<Party> getParties()
    {
        return m_aParties;
    }
    public ArrayList<String> getSavedGames()
    {
        return m_aSavedFileNames;
    }
    public Player getPlayer(String name)
    {
        Player player=null;
        for(int p=0;p<m_aAllPlayers.size();p++)
        {
            if(m_aAllPlayers.get(p).getName().equals(name))
            {
                player=m_aAllPlayers.get(p);
                break;
            }
        }
        return player;
    }
    public Nifty getGuiController()
    {
        return m_gNiftyGui;
    }
    public ArrayList<SpecialInterest> getSpecialInterests()
    {
        return m_aSpecialInterests;
    }
    public GameMap getGameMap()
    {
        return m_oGameMap;
    }
    private void generateParties()
    {
        m_aParties=new ArrayList<Party>();
        for(int i=0;i<NUM_PARTIES;i++)
        {
            m_aParties.add(new Party(i));
        }
    }
    private void generateSpecialInterests()
    {
        String[] names={"Agribusiness","Aerospace Industry","Banks/Investment Firms",
        "Big Three Auto","Christian Fundamentalists","Civil Rights Groups","Coal Industry",
        "Drug Industry","Environmentalists","Insurance Companies","Labor Unions"
        ,"Left Wing Celebrity","NRA","Oil/Gas Industry","Right Wing Celebrity"};
        
        m_aSpecialInterests=new ArrayList<SpecialInterest>();
        for(int i=0;i<names.length;i++)
        {
            m_aSpecialInterests.add(new SpecialInterest(names[i]));
        }
    }
    private void updatePlayers()
    {
        for(int i=0;i<m_aAllPlayers.size();i++)
        {
        // give players their income  
           m_aAllPlayers.get(i).pay();
        //reset all players bids to 0            
           m_aAllPlayers.get(i).setBid(0);
        //rset single action allowance
           m_aAllPlayers.get(i).setUnused();
        }
    }
    private int compareIssues(Party party)
    {
        int maxDifference=party.getIssueNames().length*4;
        int tally=0;
        for(int i=0;i<party.getIssueNames().length;i++)
        {
            String issue=party.getIssueNames()[i];
            String issueVal=(String)m_gNiftyGui.getScreen("issues").findNiftyControl
                    (issue, DropDown.class).getSelection();
            int val=Integer.parseInt(issueVal);
            int partyVal=party.getIssues().get(issue);
            tally+=Math.abs(val-partyVal);
        }
        float percent=(float)(maxDifference-tally)/maxDifference;
        int result=(int)(percent*100);
        return result;
    }
    //ivars
    private String m_wSaveDir;//A convenience string for saving and loading files
    //the game states
    private PtPState m_oCurrentState;
    private MainMenu m_oMainMenu;
    private GameMap m_oGameMap;
    private IssuesScreen m_oIssueScreen;
    private PlayerCreator m_oPlayerCreator;
    private SaveLoadScreen m_oSaveLoad;
    private EndGame m_oEndGame;//final election screen
    
    private ArrayList<String> m_aSavedFileNames;//a list of saved game files
    private ArrayList<Party> m_aParties;
    private ArrayList<Player> m_aPlayers;//a list of human players
    private ArrayList<AI_Player> m_aAI_Players;//a list of AI players
    private ArrayList<Player> m_aAllPlayers;//combined list of AI and Human players
    private ArrayList<SpecialInterest> m_aSpecialInterests;
    private NiftyJmeDisplay m_guiDisplay;
    private Nifty m_gNiftyGui; 
    private int m_iNumPlayers;
    private int m_iNumAIPlayers;
    private Integer m_iPlayerIndex; //index of current player in array
    private boolean m_bSingleAction; // was a once-per-turn action taken
    private FilenameFilter m_fFilter;// used to prevent non-saved game files from being listed as loadable
}
