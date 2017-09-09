package PtP_Core;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.TextField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** A popup the lets you manage saving and or loading games in game.
 *
 * @author Greg
 */
public class SaveLoadScreen extends PtPState{
    
    public SaveLoadScreen(Main app)
    {
        super(app,"save/LoadGame");
        //creat overwrite warning
        BitmapFont font=m_oMainApp.getAssetManager().loadFont
            ("Interface/Fonts/Default.fnt"); 
        m_wOverwriteWarning=new BitmapText(font);
        float height=m_oMainApp.getSettings().getHeight();
        float width=m_oMainApp.getSettings().getWidth();
        
        m_wOverwriteWarning.setText("FILE TO BE SAVED EXISTS,AND WILL BE OVERWRITTEN!");
        m_wOverwriteWarning.setLocalTranslation(width/100, height/5, 0);
        m_wOverwriteWarning.setColor(ColorRGBA.Yellow);
    }
    @Override
    public void attach()
    {
        if(m_oPrevState!=m_oMainApp.getGameMap())//this scrren was accessed from the main menu
        {
            m_gScreen.findNiftyControl("saveName", TextField.class).disable();
            m_gScreen.findNiftyControl("saveButton", Button.class).disable();
        }
        else
        {
            m_gScreen.findNiftyControl("saveButton", Button.class).enable();
            String name=m_oMainApp.getGameMap().getActivePlayer().getName();
            Date date=new Date();           
            String dateStamp=date.toString();
            String clippedStamp=dateStamp.substring(0, 20);//get rid of the year and time zone display
            clippedStamp=clippedStamp.replace(':', '_');
            m_wSaveName=name+"_"+clippedStamp;
            m_gScreen.findNiftyControl("saveName", TextField.class).setText(m_wSaveName);
            m_gScreen.findNiftyControl("saveName", TextField.class).enable();
        }
        ArrayList<String> saves=m_oMainApp.getSavedGames();
        if(saves.isEmpty())
        {
            m_gScreen.findNiftyControl("loadButton", Button.class).disable();
            m_gScreen.findNiftyControl("saveList", ListBox.class).disable();
            sayEmpty();
        }
        else
        {
            for(int g=0;g<saves.size();g++)
            {
                m_gScreen.findNiftyControl("saveList", ListBox.class).addItem(saves.get(g));
            }
            m_gScreen.findNiftyControl("loadButton", Button.class).enable();
            m_gScreen.findNiftyControl("saveList", ListBox.class).enable();
        }
    }
    @Override
    public void detach()
    {
        m_gScreen.findNiftyControl("saveList", ListBox.class).clear();
        m_oMainApp.getGuiNode().detachAllChildren();
    }
    @Override
    public void update(float tpf)
    {
        //only activate the load button when a gane is selected
        if(m_gScreen.findNiftyControl("saveList", ListBox.class).getSelection().isEmpty()&&
                m_gScreen.findNiftyControl("loadButton", Button.class).isEnabled())
        {
            m_gScreen.findNiftyControl("loadButton", Button.class).disable();
        }
        else if(!m_gScreen.findNiftyControl("saveList", ListBox.class).getSelection().isEmpty()&&
                !m_gScreen.findNiftyControl("loadButton", Button.class).isEnabled())
        {
            m_gScreen.findNiftyControl("loadButton", Button.class).enable();
        }
        //notify user if file to be saved exists
        if(m_gScreen.findNiftyControl("saveName", TextField.class).isEnabled())
        {
            String name=m_gScreen.findNiftyControl("saveName", TextField.class).getText();
            List<String> files=m_gScreen.findNiftyControl("saveList", ListBox.class).getItems();
            boolean match=false;
            for(String f:files)
            {
                if(f.equals(name))
                {                   
                    match=true;
                    break;
                }    
            }
            if(match)
            {
                m_oMainApp.getGuiNode().attachChild(m_wOverwriteWarning);
            }
            else if(m_oMainApp.getGuiNode().hasChild(m_wOverwriteWarning))
            {
                m_oMainApp.getGuiNode().detachChild(m_wOverwriteWarning);
            }
        }
    }    
    public String getSaveName()
    {
        return m_wSaveName;
    }
    public void setPrevState(PtPState state)
    {
       m_oPrevState=state;
    }
    public PtPState getPrevState()
    {
       return m_oPrevState;
    }
    public String getLoadName()
    {
        List<String> names=m_gScreen.findNiftyControl("saveList", ListBox.class).getSelection();
        return names.get(0);
    }
    public void sayNoFile()
    {
        BitmapFont font=m_oMainApp.getAssetManager().loadFont
            ("Interface/Fonts/Default.fnt"); 
        BitmapText noFile=new BitmapText(font);
        float height=m_oMainApp.getSettings().getHeight();
        float width=m_oMainApp.getSettings().getWidth();
        
        noFile.setText("FILE NOT FOUND");
        noFile.setLocalTranslation(width/2-(noFile.getLineWidth()/2), height/4, 0);
        noFile.setColor(ColorRGBA.Orange);
        
        m_oMainApp.getGuiNode().attachChild(noFile);
    }
    private void sayEmpty()
    {
        BitmapFont font=m_oMainApp.getAssetManager().loadFont
            ("Interface/Fonts/Default.fnt"); 
        BitmapText empty=new BitmapText(font);
        float height=m_oMainApp.getSettings().getHeight();
        float width=m_oMainApp.getSettings().getWidth();
        
        empty.setText("NO GAMES SAVED");
        empty.setLocalTranslation(width/2-(empty.getLineWidth()/2), height*3/4, 0);
        empty.setColor(ColorRGBA.Orange);
        
        m_oMainApp.getGuiNode().attachChild(empty);
    }
    //ivars
    private String m_wSaveName;//name of the game to be saved
    private PtPState m_oPrevState;//the game state from which this screen was entered
    private BitmapText m_wOverwriteWarning;//text warning user he will overwrite a file if he saves
}
