package PtP_Core;

import com.jme3.audio.AudioNode;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**A control for animating the bus/jet during
 * a drive/fly action
 *
 * @author Greg Ostroy
 */
public class TravelControl extends AbstractControl{
    public static final float SPEED=2;
    public TravelControl(Main app)
    {
        m_fMoveX=0;
        m_fMoveZ=0;
        m_oMainApp=app;
    }
    
    @Override
    public void controlRender(RenderManager manager,ViewPort port)
    {}
    @Override
    public void controlUpdate(float tpf)
    {
        if(m_mModel!=null && m_vDistance !=null && m_vNormDistance != null)
        {
           if(Math.abs(m_fMoveX)<Math.abs(m_vDistance.x)&& Math.abs(m_fMoveZ) <Math.abs(m_vDistance.y))
           {
               m_fMoveX+=m_vNormDistance.x*SPEED*tpf;
               m_fMoveZ+=m_vNormDistance.y*SPEED*tpf;
               m_mModel.setLocalTranslation(m_vStart.x+m_fMoveX,m_fYpos,m_vStart.z+m_fMoveZ);
               m_sSound.setLocalTranslation(m_mModel.getLocalTranslation());
           }
           else
           {
               m_sSound.stop();
               m_oMainApp.getRootNode().detachChild(m_mModel);
               m_oMainApp.getGameMap().adjustAvatar(m_oMainApp.getGameMap().getActivePlayer().getModel(),m_oMainApp.getGameMap().getActivePlayer().getCurrentState());
               m_oMainApp.closeDialog("flyPanel");
               this.setEnabled(false);
           }
        }
    }
    @Override 
    public Control cloneForSpatial(Spatial model)
    {
        final TravelControl control=new TravelControl(m_oMainApp);
        
        return control;
    }
    @Override
    public void setSpatial(Spatial spatial) 
    {
       m_mModel=spatial; 
       m_fYpos=spatial.getLocalTranslation().y;
    }
    public void setSound(String source)
    {
        m_sSound=new AudioNode(m_oMainApp.getAssetManager(),source,false);
        m_sSound.setLooping(true);
        m_sSound.setDirectional(true);
        m_sSound.setLocalTranslation(m_mModel.getLocalTranslation());
        m_oMainApp.getRootNode().attachChild(m_sSound);     
    }
    public void setParameters(Vector3f source, Vector3f target)
    {
        //reset parameters in case the were used
        m_fMoveX=0;
        m_fMoveZ=0;
        m_vStart=source;
        m_vEnd=target;
        m_mModel.setLocalTranslation(source.x,m_fYpos,source.z);
        //make sure model starts with a zero turn angle to ease calculations
        Quaternion quat=new Quaternion();
        quat=quat.fromAngles(0, 0, 0);
        m_mModel.setLocalRotation(quat);
        // set the parameters needed for travel
        Vector2f start=new Vector2f(source.x,source.z);
        Vector2f end=new Vector2f(target.x,target.z);
        m_vDistance=end.subtract(start);
        m_vNormDistance=m_vDistance.normalize();        
        //rotate the modle to face the direction it will be traveling
        Vector2f startTurnVector=new Vector2f(0,1);
        double turnAngle=Math.acos(m_vNormDistance.dot(startTurnVector));
        float rot=(float)turnAngle;
        if(source.x<=target.x)
        {    
            m_mModel.rotate(0, rot, 0);
        }
        else
        {
            m_mModel.rotate(0, rot*-1, 0);
        }
    }
    public void playSound()
    {
        m_sSound.play();
    }
    //Ivars
    private Spatial m_mModel;
    private float m_fMoveX;
    private float m_fMoveZ;
    private float m_fYpos;
    private Vector3f m_vStart;
    private Vector3f m_vEnd;
    private Vector2f m_vDistance;
    private Vector2f m_vNormDistance;
    private Main m_oMainApp;
    private AudioNode m_sSound;
}
