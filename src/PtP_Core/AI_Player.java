package PtP_Core;

import PtP_AI.Bid;
import PtP_AI.Campaign;
import PtP_AI.FlipFlop;
import PtP_AI.Fundraise;
import PtP_AI.NegAdvertise;
import PtP_AI.Pander;
import PtP_AI.PosAdvertise;
import PtP_AI.Recruit;
import PtP_AI.Strategy;
import PtP_AI.Travel;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import java.io.IOException;
import java.util.ArrayList;

/**An AI player
 *
 * @author Greg
 */
public class AI_Player extends Player implements Savable{
    
    public AI_Player(String name,int charisma, int income,Party party,
            Spatial avatar,Main app)
    {
        super(name,charisma,income,party,avatar,app.getAssetManager());
        
        m_oMainApp=app;
        m_iIndex=0;
        m_oBrain=new Brain(this);
    }
    public AI_Player()
    {}
    @Override
    public void write(JmeExporter ex) throws IOException
    {
        super.write(ex);
    }
    @Override
    public void read(JmeImporter im) throws IOException
    {
       super.read(im);
       m_oMainApp=null;
       m_oBrain=null;
       m_oFlipFlop=null;
       m_oBid=null;
    }
    //restores a pointer to the Main app when a game is loaded
    public void restoreAppPointer(Main app)
    {
        m_oMainApp=app;
        m_oBrain=new Brain(this);
    }
    /**AI player executes all actions of its turn
     */
    public void doTurn()
    {
        //do appropriate single actions
       for(int p=0;p<m_aTeam.size();p++)
       {
           setActiveMember(p);
           m_iIndex=p;
           m_oBrain.evaluateSingle();
       }
       //consider bidding
       if(m_iFunds>0)
       {
           float bidValue=m_oBid.desirability()*100;
           int randNum=m_oMainApp.getGameMap().getNumGenerator().nextInt(101);
           if(randNum<bidValue)
           {
                boolean result=m_oBid.process();
                Screen screen=m_oMainApp.getGuiController().getCurrentScreen();
                Element panel=screen.findElementByName("logPanel");
                if(result)
                {
                        final String str="  "+m_oBid.posText();
                        new LabelBuilder(this.getName()+"b",str){{
                            color("#0f0f");
                            align(Align.Left);
                        }}.build(m_oMainApp.getGuiController(), screen, panel);
                }
           }
       }
       //consider advertising
       if(m_iFunds>1)
       {
           m_oBrain.evaluateRepeatable();
       }
       //consider flipflopping
       
    }
    public class Brain{
        
        public Brain(AI_Player player)
        {
            m_aSingleActions=new ArrayList<Strategy>();
            m_aSingleActions.add(new Campaign(m_oMainApp,player));
            m_aSingleActions.add(new Travel(m_oMainApp,player));
            m_aSingleActions.add(new Fundraise(m_oMainApp,player));
            m_aSingleActions.add(new Pander(m_oMainApp,player));
            m_aSingleActions.add(new Recruit(m_oMainApp,player));
            
            m_aRepeatableActions=new ArrayList<Strategy>();
            m_aRepeatableActions.add(new NegAdvertise(m_oMainApp,player));
            m_aRepeatableActions.add(new PosAdvertise(m_oMainApp,player));
            
            m_oBid=new Bid(m_oMainApp,player);
            
            m_oFlipFlop=new FlipFlop(m_oMainApp,player);
            
            m_oPlayer=player;
        }
        /**Decide which single action to do and execute it
         */
        public void evaluateSingle()
        {
            float highestValue=-1;
            Strategy bestChoice=null;
            for(int s=0;s<m_aSingleActions.size();s++)
            {
                float value=m_aSingleActions.get(s).desirability();
                if(highestValue<value)
                {
                    highestValue=value;
                    bestChoice=m_aSingleActions.get(s);
                }
            }
            if(bestChoice !=null)
            {
                boolean result=bestChoice.process();
                Screen screen=m_oMainApp.getGuiController().getCurrentScreen();
                Element panel=screen.findElementByName("logPanel");
                String number=Integer.toString(m_iIndex);
                String id=m_oPlayer.getName()+number;
                if(result)
                {
                        final String str="  "+bestChoice.posText();
                        new LabelBuilder(id+"t",str){{
                            color("#0f0f");
                            align(Align.Left);
                        }}.build(m_oMainApp.getGuiController(), screen, panel);
                }
                else
                {
                        final String str="  "+bestChoice.negText();
                        new LabelBuilder(id+"f",str){{
                            color("#f00f");
                            align(Align.Left);
                        }}.build(m_oMainApp.getGuiController(), screen, panel);
                } 
            }               
        }
        /**Decide which repeatable action to do and execute it
         */
        public void evaluateRepeatable()
        {
           while(m_oPlayer.getFunds()>1)
           {
               float highestValue=0;
               Strategy bestChoice=null;
               for(int s=0;s<m_aRepeatableActions.size();s++)
               {
                    float value=m_aRepeatableActions.get(s).desirability();
                    if(highestValue<value)
                    {
                        highestValue=value;
                        bestChoice=m_aRepeatableActions.get(s);
                    }
               }
               if(bestChoice !=null)
                {
                    boolean result=bestChoice.process();
                    Screen screen=m_oMainApp.getGuiController().getCurrentScreen();
                    Element panel=screen.findElementByName("logPanel");
                    String number=Integer.toString(m_iIndex);
                    String id=m_oPlayer.getName()+number;
                    if(result)
                    {
                        final String str="  "+bestChoice.posText();
                        new LabelBuilder(id+"t",str){{
                            color("#0f0f");
                            align(Align.Left);
                        }}.build(m_oMainApp.getGuiController(), screen, panel);
                    }
                    else
                    {
                        final String str="  "+bestChoice.negText();
                        new LabelBuilder(id+"f",str){{
                            color("#f00f");
                            align(Align.Left);
                        }}.build(m_oMainApp.getGuiController(), screen, panel);
                    }    
                } 
               else
               {
                   break;
               }
           }
        }
        //ivars
        private AI_Player m_oPlayer;
        private ArrayList<Strategy> m_aSingleActions;
        private ArrayList<Strategy> m_aRepeatableActions;
    }
    //ivars
    private Brain m_oBrain;
    private Main m_oMainApp;
    private FlipFlop m_oFlipFlop;
    private Bid m_oBid;
    private int m_iIndex;
}
