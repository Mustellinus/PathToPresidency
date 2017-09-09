package PtP_Core;

import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.Label;
import java.util.HashMap;

/** This class holds the end game panel with election results
 *
 * @author Greg
 */
public class EndGame extends PtPState{
    
    public EndGame(Main app)
    {
        super(app,"endGameDisplay");
        
        m_aCandidateVotes=new HashMap<String,BitmapText>();
        m_oProjectedWinner=null;
        m_oActualWinner=null;
    }   
    @Override
    public void attach()
    {
        String projectedWinner=m_oProjectedWinner.getName();
        m_oMainApp.getGuiController().getScreen("endGameDisplay").findNiftyControl
                ("winnerText",Label.class).setText(projectedWinner+" is the projected winner");       
        m_oMainApp.getGuiController().getScreen("endGameDisplay").findNiftyControl
                ("doneButton",Button.class).disable();
         //create table of candidates and their electoral votes
        float width=m_oMainApp.getSettings().getWidth();
        float height=m_oMainApp.getSettings().getHeight();
        
        BitmapText title=new BitmapText(m_oMainApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),false);
        title.setColor(ColorRGBA.Yellow);
        title.setText("CANDIDATE");
        title.setLocalTranslation(width/3,(height/4)*3, 0);
        m_oMainApp.getGuiNode().attachChild(title);
        
        BitmapText votes=new BitmapText(m_oMainApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),false);
        votes.setColor(ColorRGBA.Yellow);
        votes.setText("VOTES");
        votes.setLocalTranslation((width/3)*2-votes.getLineWidth(),(height/4)*3, 0);
        m_oMainApp.getGuiNode().attachChild(votes);
        
        for(int i=0;i<m_oMainApp.getAllPlayers().size();i++)
        {
           BitmapText name=new BitmapText(m_oMainApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),false);
           name.setColor(ColorRGBA.Green);
           name.setText(m_oMainApp.getAllPlayers().get(i).getName());
           name.setLocalTranslation(width/3,((height/4)*3)-(title.getLineHeight()*(i+1)), 0);
           m_oMainApp.getGuiNode().attachChild(name);
           
           m_aCandidateVotes.get(m_oMainApp.getAllPlayers().get(i).getName()).
                   setLocalTranslation((width/3)*2-(votes.getLineWidth()/2),
                   ((height/4)*3)-(title.getLineHeight()*(i+1)), 0);
           m_oMainApp.getGuiNode().attachChild
                   (m_aCandidateVotes.get(m_oMainApp.getAllPlayers().get(i).getName()));
        }    
    }
    @Override
    public void detach()
    {
        m_oMainApp.getGuiNode().detachAllChildren();
    }
    public void setBillboards(Player projectedWinner,Player actualWinner)
    {
        m_oProjectedWinner=projectedWinner;
        m_oActualWinner=actualWinner;
        for(int p=0;p<m_oMainApp.getAllPlayers().size();p++)
        {
            String name=m_oMainApp.getAllPlayers().get(p).getName();            
            BitmapText votes=new BitmapText(m_oMainApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),false);
            votes.setColor(ColorRGBA.Green);
            votes.setText("0");
            m_aCandidateVotes.put(name, votes);
        }
    }
    public HashMap<String,BitmapText> getVotes()
    {
        return m_aCandidateVotes;
    }
    public void declareWinner()
    {
       String actualWinner=m_oActualWinner.getName();
       m_oMainApp.getGuiController().getScreen("endGameDisplay").findNiftyControl
                ("winnerText",Label.class).setText(actualWinner+" is the new President");
       for(int i=0;i<m_oMainApp.getAllPlayers().size();i++)
       { 
           String votes=Integer.toString(m_oMainApp.getAllPlayers().get(i).getActualVotes());
           m_oMainApp.getGuiNode().detachChild
                   (m_aCandidateVotes.get(m_oMainApp.getAllPlayers().get(i).getName()));
           m_aCandidateVotes.get(m_oMainApp.getAllPlayers().get(i).getName()).setText(votes);
           m_oMainApp.getGuiNode().attachChild
                   (m_aCandidateVotes.get(m_oMainApp.getAllPlayers().get(i).getName()));
       }
       m_oMainApp.getGuiController().getScreen("endGameDisplay").findNiftyControl
                ("doneButton",Button.class).enable();
       m_oMainApp.getGuiController().getScreen("endGameDisplay").findNiftyControl
                ("voteButton",Button.class).disable();
    }
    //ivars
    private Player m_oProjectedWinner;
    private Player m_oActualWinner;
    private HashMap<String,BitmapText> m_aCandidateVotes;
}
