package PtP_Core;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.io.IOException;

/**a class for the players "game peices"
 *
 * @author Greg
 */
public class TeamMember implements Savable
    {
        public TeamMember(int charisma,Spatial model,US_State state)
        {
            m_iCharMod=charisma;
            m_sModel=model;
            m_wModelPath=model.getKey().getName();
            m_oCurrentState=state;
            m_wStateName=state.getName();
            m_bUsed=false;
            m_bJustTraveled=false;
            Vector3f location=state.getWayPoint().getLocalTranslation();
            m_vLocation=new Vector3f(location.x,GameMap.Y_POS,location.z+GameMap.AVATAR_OFFSET_Z);
            setModel();
        }
        public TeamMember()
        {}
        @Override
        public void write(JmeExporter ex) throws IOException
        {  
            OutputCapsule cap=ex.getCapsule(this);
            cap.write(m_wStateName,"Current State","Alabama");
            cap.write(m_iCharMod,"Charisma Modifier",0);
            cap.write(m_bUsed,"Used this turn?",false);
            cap.write(m_bJustTraveled,"Traveled This Turn?",false);
            cap.write(m_wModelPath,"Team Member Model","Models/Volunteer/Volunteer_Independant.j3o");
            cap.write(m_vLocation,"Modle Location",new Vector3f(0,0,0));
        }
        @Override
        public void read(JmeImporter im) throws IOException
        { 
            InputCapsule cap=im.getCapsule(this);
            m_wStateName=cap.readString("Current State","Alabama");
            m_iCharMod=cap.readInt("Charisma Modifier",0);
            m_bUsed=cap.readBoolean("Used this turn?",false);
            m_bJustTraveled=cap.readBoolean("Traveled This Turn?",false);
            m_wModelPath=cap.readString("Team Member Model","Models/Volunteer/Volunteer_Independant.j3o");
            m_vLocation=(Vector3f)cap.readSavable("Modle Location",new Vector3f(0,0,0));
        }
        public void restoreModels(Main app)
        {
            m_sModel=app.getAssetManager().loadModel(m_wModelPath);
            setModel();
        }
        public int getCharisma()
        {
            return m_iCharMod;
        }
        public Vector3f getLocation()
        {
            return m_vLocation;
        }
        public Spatial getModel()
        {
            return m_sModel;
        }
        public US_State getState()
        {
            return m_oCurrentState;
        }
        public boolean traveled()
        {
            return m_bJustTraveled;
        }
        public boolean used()
        {
            return m_bUsed;
        }
        public void setUsed(boolean used)
        {
            m_bUsed=used;
        }
        public void setLocation(Vector3f location)
        {
            m_vLocation=location;
        }
        public void setTraveled(boolean traveled)
        {
            m_bJustTraveled=traveled;
        }
        public void setState(US_State state)
        {
            m_oCurrentState=state;
            m_wStateName=state.getName();
            Vector3f location=state.getWayPoint().getLocalTranslation();
            m_vLocation=new Vector3f(location.x,GameMap.Y_POS,location.z+GameMap.AVATAR_OFFSET_Z);
        }
        //recover the pointer to an occupied state after loading a game
        public void recoverState(US_State[] states)
        {
            for(int s=0;s<states.length;s++)
            {
                if(m_wStateName.equals(states[s].getName()))
                {
                    m_oCurrentState=states[s];
                    states[s].setOccupants(1);
                    break;
                }
            }
        }
        public void talkingAnimation()
        {    
        }
        //set the initial parameters for this game peices model 
        private void setModel()
        {
            m_sModel.setLocalScale(.06f);
            m_sModel.setLocalTranslation(m_vLocation);
            Node node=new Node("playerPiece");
            node.attachChild(m_sModel);
            m_oAnimControl=node.getChild("Body").getControl(AnimControl.class);                    
            m_oAnimChannel=m_oAnimControl.createChannel();
            m_oAnimChannel.setAnim("Standing");
            node.detachAllChildren();
        }
        //ivars 
       private int m_iCharMod;
       private Spatial m_sModel;
       private US_State m_oCurrentState;
       private String m_wStateName;
       private String m_wModelPath;
       private boolean m_bUsed;
       private boolean m_bJustTraveled;//has one of the AI spatials just arrived at a state
       private Vector3f m_vLocation;//stores location of avatar to be used to id players when picking 
       protected AnimControl m_oAnimControl;
       protected AnimChannel m_oAnimChannel;
}