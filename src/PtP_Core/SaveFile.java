package PtP_Core;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Greg
 */
public class SaveFile implements Savable
    {
        public SaveFile(Main app)
        {
            m_iPlayerIndex=app.getPlayerIndex();
            m_iNumPlayers=app.getNumPlayers();
            m_iNumAIPlayers=app.getNumAI_Players();
            m_bSingleAction=app.isSingleAction();
            m_oGameMap=app.getGameMap();
            m_aPlayers=app.getPlayers();
            m_aAI_Players=app.getAIPlayers();
        }
        public SaveFile()
        {}
        @Override
        public void write(JmeExporter ex) throws IOException
        {
            OutputCapsule cap=ex.getCapsule(this);
            cap.write(m_iPlayerIndex, "Player Index",0);
            cap.write(m_iNumPlayers, "Number of Humans",1);
            cap.write(m_iNumAIPlayers, "Number of Bots",0);
            cap.write(m_bSingleAction, "Single Action ?",false);
            cap.write(m_oGameMap, "Game Map", null);
            cap.writeSavableArrayList(m_aPlayers, "HumanPlayers", new ArrayList<Player>());
            cap.writeSavableArrayList(m_aAI_Players, "AI_Players", new ArrayList<AI_Player>());
        }
        @Override
        public void read(JmeImporter im) throws IOException
        {
            InputCapsule cap=im.getCapsule(this);
            m_iPlayerIndex=cap.readInt("Player Index",0);
            m_iNumPlayers=cap.readInt("Number of Humans",1);
            m_iNumAIPlayers=cap.readInt("Number of Bots",0);
            m_bSingleAction=cap.readBoolean("Single Action ?",false);
            m_oGameMap=(GameMap)cap.readSavable("Game Map", null);
            m_aPlayers=cap.readSavableArrayList("HumanPlayers", new ArrayList<Player>());
            m_aAI_Players=cap.readSavableArrayList("AI_Players", new ArrayList<Player>());
        }
        public ArrayList<AI_Player> getAI_Players() {
            return m_aAI_Players;
        }

        public ArrayList<Player> getPlayers() {
            return m_aPlayers;
        }

        public boolean isSingleAction() {
            return m_bSingleAction;
        }

        public int getNumAI_Players() {
            return m_iNumAIPlayers;
        }

        public Integer getPlayerIndex() {
            return m_iPlayerIndex;
        }
        
        public int getNumPlayers() {
            return m_iNumPlayers;
        }
        
        public GameMap getGameMap() {
            return m_oGameMap;
        }
    //ivars
        private int m_iNumPlayers;
        private int m_iNumAIPlayers;
        private Integer m_iPlayerIndex;
        private boolean m_bSingleAction;
        private ArrayList<Player> m_aPlayers;//a list of human players
        private ArrayList<AI_Player> m_aAI_Players;//a list of AI players
        private GameMap m_oGameMap;
    }
