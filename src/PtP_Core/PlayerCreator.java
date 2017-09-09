/*This is the Candidate creation screen. The player spends developement points 
 *on different attributes. When the player has a satifactory candidate he clicks 
 *"accept",the candiates are created,and the screen switches to the game map*/

package PtP_Core;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.TextField;
import java.util.ArrayList;
import java.util.HashMap;

/**The player creation screen
 *
 * @author Greg
 */
public class PlayerCreator extends PtPState implements AnimEventListener{
    
    public static final int DEV_POINTS=6;
    public PlayerCreator(Main app)
    {
        super(app,"playerSetup");       
        m_iPointsLeft=DEV_POINTS;
        m_iCharValue=0;
        m_iIncomeVal=0;
        m_iModelIndex=0;
        m_iPartyIndex=0;
        m_wPartyName="Independent(0)";
        m_wPlayerName="Namless Fool";
        m_cam=app.getCamera();
        
        m_aModelNames=new ArrayList<String>();
        m_aModelMap=new HashMap<String,Spatial>();
        loadModels();
        addLight();
        //Display developement points
        BitmapFont font=app.getAssetManager().loadFont
            ("Interface/Fonts/Default.fnt"); 
        setDisplay(font);
    }
    @Override
    public void attach()
    {
        //set camera
        m_cam.setLocation(new Vector3f(0,0,6));
        m_cam.lookAt(Vector3f.ZERO,Vector3f.UNIT_Y);
        m_oMainApp.getFlyByCamera().setEnabled(false);
        
        
        m_oMainApp.getRootNode().attachChild(m_mCurrentModel);
        
        m_oMainApp.getRootNode().addLight(m_Light);
        //display developement points 
        m_oMainApp.getGuiNode().detachAllChildren(); 
        m_oMainApp.getGuiNode().attachChild(m_sPlayerNumber);        
        m_oMainApp.getGuiNode().attachChild(m_sPointsTitle);
        m_oMainApp.getGuiNode().attachChild(m_sDevPoints); 
        setDropDowns();//populate the dopdown menus
        setPartyDropdown();
        m_gScreen.findNiftyControl("charisma menu",DropDown.class).selectItemByIndex
                (m_iCharValue);
        m_gScreen.findNiftyControl("income menu",DropDown.class).selectItemByIndex
                (m_iIncomeVal); 
        m_gScreen.findNiftyControl("party menu",DropDown.class).selectItemByIndex
                (m_iPartyIndex);
        m_gScreen.findNiftyControl("name field", TextField.class).setText(m_wPlayerName);
    }
    @Override
    public void detach()
    {   
        m_oMainApp.getGuiNode().detachAllChildren();         
        m_oMainApp.getRootNode().detachAllChildren();
        m_oMainApp.getRootNode().removeLight(m_Light);        
        m_oMainApp.getFlyByCamera().setEnabled(true); 
    }
    @Override
    public void update(float tpf)
    {
        //grab current character values on display
        parseMenuValues();
        int partyVal=parsePartyValue(m_wPartyName);
        
        //update player being created
        String playerNum=Integer.toString(m_oMainApp.getPlayers().size()+1);
        m_sPlayerNumber.setText("PLAYER "+ playerNum);
        //update developement points left and display current value
        m_iPointsLeft =DEV_POINTS-(m_iCharValue)-(m_iIncomeVal)-partyVal;
        m_oMainApp.getGuiNode().detachChild(m_sDevPoints);
        m_sDevPoints.setText("Total/Unused: "+Integer.toString(DEV_POINTS)+" / "
                +Integer.toString(m_iPointsLeft));
        
        //change to a warning color if alotted developement points are exceeded
        if(m_iPointsLeft<0)
        {
            m_sDevPoints.setColor(ColorRGBA.Orange);
        }
        else
        {
            m_sDevPoints.setColor(ColorRGBA.Green);
        }
        m_oMainApp.getGuiNode().attachChild(m_sDevPoints);
        //
        if(m_iPointsLeft<0)
        {
            m_gScreen.findNiftyControl("finish button",Button.class).disable();
            if(m_oMainApp.getGuiNode().hasChild(m_sPointsExceeded)==false)
            {
                m_oMainApp.getGuiNode().attachChild(m_sPointsExceeded);
            }
        }   
        else
        {
            m_gScreen.findNiftyControl("finish button",Button.class).enable();
            if(m_oMainApp.getGuiNode().hasChild(m_sPointsExceeded))
            {
                m_oMainApp.getGuiNode().detachChild(m_sPointsExceeded);
            }
        }
    } 
    @Override
    public void onAnimChange(AnimControl control,AnimChannel channel,String animName)
    {}
    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName)
    {}
    public int setNumPlayers()
    {
        String players=(String)m_gScreen.findNiftyControl("number", Label.class).getText();
        int numPlayers=Integer.parseInt(players);
        return numPlayers;
    }
    //cycle forward to the next modle and display it
    public void nextModel()
    {
        m_oMainApp.getRootNode().detachChild(m_mCurrentModel);
        if(m_iModelIndex<m_aModelNames.size()-1)
        {
            m_iModelIndex++;
        }
        else
        {
            m_iModelIndex=0;
        }
        m_mCurrentModel= m_aModelMap.get(m_aModelNames.get(m_iModelIndex));
        m_oMainApp.getRootNode().attachChild(m_mCurrentModel);
    }
    //cycle backwrad to the prev. model and display it
    public void prevModel()
    {
        m_oMainApp.getRootNode().detachChild(m_mCurrentModel);
        if(m_iModelIndex>0)
        {
            m_iModelIndex--;
        }
        else
        {
            m_iModelIndex=m_aModelNames.size()-1;
        }
        m_mCurrentModel= m_aModelMap.get(m_aModelNames.get(m_iModelIndex));
        m_oMainApp.getRootNode().attachChild(m_mCurrentModel);        
    }
    //after a player is created this function is called to remove options and prevent duplicate use of a party or avatar
    public void resetValues()
    {
        //remove the party that was picked from the list of available parites
        m_gScreen.findNiftyControl("charisma menu", DropDown.class).selectItemByIndex(0);
        m_gScreen.findNiftyControl("income menu", DropDown.class).selectItemByIndex(0);
        setPartyDropdown();
        //remove the chosen avatar
        m_oMainApp.getRootNode().detachChild(m_mCurrentModel);
        for(int i=0;i<m_aModelNames.size();i++)
        {
            if(m_mCurrentModel==m_aModelMap.get(m_aModelNames.get(i)))
            {
                m_aModelMap.remove(m_aModelNames.get(i));
                m_aModelNames.remove(i);
                break;
            }
        }
        m_mCurrentModel=m_aModelMap.get(m_aModelNames.get(0));
        m_iModelIndex=0;
        m_oMainApp.getRootNode().attachChild(m_mCurrentModel);
        //The next human player better come up with a name
        int index=m_oMainApp.getPlayers().size();
        String str=Integer.toString(index);
        m_gScreen.findNiftyControl("name field", TextField.class).setText("Nameless Fool "+str);
    }
    public int getCharismaValue() 
    {
        return m_iCharValue;
    }
    public int getIncomeVal() 
    {
        return m_iIncomeVal;
    }
    public Spatial getModel() 
    {
        return m_mCurrentModel;
    }
    public HashMap<String,Spatial> getModels()
    {
        return m_aModelMap;
    }
    public ArrayList<String> getModelNames()
    {
        return m_aModelNames;
    }
    public String getPartyName() 
    {
        return m_wPartyName;
    }
    public int getPartyIndex()
    {
        return m_iPartyIndex;
    }
    public String getPlayerName()
    {
        return m_wPlayerName;
    }
    public int getPointsLeft()
    {
        return m_iPointsLeft;
    }
    private void addLight()
    {
        m_Light=new DirectionalLight();
        m_Light.setDirection(new Vector3f(1.8f,-3.8f,-2.8f).normalizeLocal());
        m_Light.setColor(ColorRGBA.White);
    }
    /*Sets up values fo the drop down menus on this screen*/
    private void setDropDowns()
    {
        //clear the dropdowns
          m_gScreen.findNiftyControl("charisma menu", DropDown.class).clear();
          m_gScreen.findNiftyControl("income menu", DropDown.class).clear();  
          m_gScreen.findNiftyControl("numbersDropdown",DropDown.class).clear();
          m_gScreen.findNiftyControl("AI_numbersDropdown",DropDown.class).clear(); 
        //numbers for charisma and income
        ArrayList<String> values=new ArrayList<String>();
        values.add("0");
        values.add("1");
        values.add("2");
        values.add("3");
        m_gScreen.findNiftyControl("charisma menu", DropDown.class).addAllItems(values);
        m_gScreen.findNiftyControl("income menu", DropDown.class).addAllItems(values);  
        //add the number of human players the user can choose
        values.clear();
        values.add("1");
        values.add("2");
        values.add("3");
        values.add("4");
        values.add("5");
        values.add("6");
        m_gScreen.findNiftyControl("numbersDropdown",DropDown.class).addAllItems(values);
        //and the number of chosen AI players
        values.clear();
        values.add("0");
        values.add("1");
        values.add("2");
        values.add("3");
        values.add("4");
        values.add("5");
        m_gScreen.findNiftyControl("AI_numbersDropdown",DropDown.class).addAllItems(values);
    }
    private void setPartyDropdown()
    {
        m_gScreen.findNiftyControl("party menu",DropDown.class).clear();
        ArrayList<Party> parties=m_oMainApp.getParties();
        ArrayList<String> values=new ArrayList<String>();
        for(int i=0;i<parties.size();i++)
        {
            if(!parties.get(i).isUsed())
            { 
                String number=Integer.toString(parties.get(i).getPointCost());
                String name=parties.get(i).getName()+"("+number+")";
                values.add(name);
            }
        }
        m_gScreen.findNiftyControl("party menu",DropDown.class).addAllItems(values);
    }
    /*Sets up a text field to display current and unused developement points */
    private void setDisplay(BitmapFont font)
    {
        int height=m_oMainApp.getSettings().getHeight();
        int width=m_oMainApp.getSettings().getWidth();
        
        m_sPlayerNumber=new BitmapText(font,false);
        m_sPlayerNumber.setLocalTranslation(width*.6f,height,0);
        String playerNum="1";
        m_sPlayerNumber.setText("PLAYER "+ playerNum);
        m_sPlayerNumber.setColor(ColorRGBA.Cyan);
        
        m_sPointsTitle=new BitmapText(font,false);
        m_sPointsTitle.setLocalTranslation(width*.6f,height-m_sPointsTitle.getLineHeight(),0);
        m_sPointsTitle.setText("Developement Points");
        m_sPointsTitle.setColor(ColorRGBA.Yellow);
        
        m_sDevPoints=new BitmapText(font,false);
        m_sDevPoints.setLocalTranslation(width/2,height-(m_sPointsTitle.getLineHeight()*2),0);
        m_sDevPoints.setText("Total/Unused: "+Integer.toString(DEV_POINTS)+" / "
                +Integer.toString(m_iPointsLeft));
        m_sDevPoints.setColor(ColorRGBA.Green);
        
        m_sPointsExceeded=new BitmapText(font,false);
        m_sPointsExceeded.setLocalTranslation(0,height/2,0);
        m_sPointsExceeded.setText("You have exceeded the allowed developement points!");
        m_sPointsExceeded.setColor(ColorRGBA.Orange);
    }
    /* Grab the values selected in each dropdwn*/
    private void parseMenuValues()
    {
        String charisma=(String)m_gScreen.findNiftyControl("charisma menu",DropDown.class).getSelection();
        if (charisma !=null)
        {
            m_iCharValue=Integer.parseInt(charisma);
        }
        String income=(String)m_gScreen.findNiftyControl("income menu",DropDown.class).getSelection();
        if(income !=null)
        {
            m_iIncomeVal=Integer.parseInt(income);
        }
        String partyName=(String)m_gScreen.findNiftyControl("party menu",DropDown.class).getSelection();
        if(partyName != null)
        {
            m_wPartyName=partyName;
        }
        String playerName=(String)m_gScreen.findNiftyControl("name field",TextField.class).getText();
        if(playerName !=null)
        {
            m_wPlayerName=playerName;
        }
        m_iPartyIndex=m_gScreen.findNiftyControl("party menu",DropDown.class)
            .getSelectedIndex();
    }
    /* Grab the point cost for the selected party*/
    private int parsePartyValue(String party) 
    {
        if(party.equals("Democratic(3)")||party.equals("Republican(3)"))
        {
            return 3;
        }
        if(party.equals("Independent(0)"))
        {
            return 0;
        }
        return 1;
    }
    /*Set up the models for viewing*/    
    private void loadModels()
    {
        Node node=new Node("candidates");
        ArrayList<String> paths=new ArrayList<String>();
        m_aModelNames.add("Robert 'Pretty' Boyd");
        paths.add("Scenes/Pretty Boyd.j3o");
        m_aModelNames.add("Beth Simplestom");
        paths.add("Scenes/Beth Simplestrom.j3o");
        m_aModelNames.add("Bubba Folsom");
        paths.add("Scenes/Bubba Folsom.j3o");
        m_aModelNames.add("Leonard Pinkopolus");
        paths.add("Scenes/Leonard Pinkopolus.j3o");
        m_aModelNames.add("Maria Ochoa");
        paths.add("Scenes/Maria Ochoa.j3o");
        m_aModelNames.add("Ollie Oldman");
        paths.add("Scenes/Ollie Oldman.j3o");
        m_aModelNames.add("Susan Stern-Anderson");
        paths.add("Scenes/Susan Stern.j3o");
        m_aModelNames.add("Tony Token");
        paths.add("Scenes/Tony Token.j3o");
        for(int i=0;i<paths.size();i++)
        {
            Spatial model=m_oMainApp.getAssetManager().loadModel(paths.get(i));
            model.scale(.2f,.2f,.2f);
            model.setLocalTranslation(1.5f,-2f,0);
            node.attachChild(model);
            AnimControl control=node.getChild("Body").getControl(AnimControl.class);
            AnimChannel channel=control.createChannel();
            channel.setAnim("Standing");
            node.detachAllChildren();
            m_aModelMap.put(m_aModelNames.get(i),model);
        }
        m_mCurrentModel=m_aModelMap.get(m_aModelNames.get(0));
    }
    //ivars
    private DirectionalLight m_Light;
    private BitmapText m_sPointsTitle;//Dev point header
    private BitmapText m_sDevPoints;//Display for dev point total/unused
    private BitmapText m_sPointsExceeded;//Tells player he has spent too many points
    private BitmapText m_sPlayerNumber;//displays human player to be created
    private int m_iPointsLeft;//current number of unused dev points
    private int m_iCharValue;//currently selected charisma value 
    private int m_iIncomeVal;//currently selected base income
    private int m_iModelIndex;//the current index of model being displayed;
    private String m_wPartyName;//name of currently selected party
    private String m_wPlayerName;
    private Camera m_cam;
    private ArrayList<String> m_aModelNames;
    private HashMap<String,Spatial> m_aModelMap;
    private Spatial m_mCurrentModel;
    private int m_iPartyIndex;//the index of party on the dropdown list,mathces the index for the list of parties held by Main
}
