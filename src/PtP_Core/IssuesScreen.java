/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PtP_Core;

import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.Label;
import java.util.ArrayList;

/**
 *
 * @author Greg
 */
public class IssuesScreen extends PtPState{
    
    public IssuesScreen( Main app)
    {
        super(app,"issues");
        m_aIssueNames=new String[]{"Abortion","Balanced Budget","Business Subsidies",
        "Censorship","Church/State Separation","Death Penalty","Drug Laws",
        "Environment","Free Market","Free Trade","Gay Marriage","Gun Control",
        "Immigration","Military Spending","National Health Care",
        "National Security","Welfare"};
        
        m_aIssueVals=new int[m_aIssueNames.length];
        
        for(int i=0;i<m_aIssueVals.length;i++)
        {
            m_aIssueVals[i]=0;
        }       
        m_iPartySupport=-1;
    }
    @Override
    public void update(float tpf)
    {
          for(int i=0;i<m_aIssueNames.length;i++)
        {
            String issueValue=(String)m_gScreen.findNiftyControl
                    (m_aIssueNames[i],DropDown.class).getSelection();
            if(issueValue !=null)
            {
                m_aIssueVals[i]=Integer.parseInt(issueValue);
            }
        } 
        m_iPartySupport=m_oMainApp.calculatePartySupport();
        m_gScreen.findNiftyControl("partySupportText", Label.class).setText
                ("party support:"+Integer.toString(m_iPartySupport)+"%");
    }
    @Override
    public void attach()
    {
        setDropDowns();
    }
    @Override
    public void detach()
    {
         
    } 
    public int getPartySupport()
    {
        return m_iPartySupport;
    }
    public int[] getValues()
    {
        return m_aIssueVals;
    }
    public void setValues(Player player)
    {
        for(int i=0;i<m_aIssueNames.length;i++)
        {
            int value=player.getIssues().get(m_aIssueNames[i]);
            m_gScreen.findNiftyControl(m_aIssueNames[i],DropDown.class).
                    selectItemByIndex(value+2);            
        }
    }
    //called during player generation to reset issue to 0
    public void clearValues()
    {  
        for(int i=0;i<m_aIssueVals.length;i++)
        {
            m_aIssueVals[i]=0;
        }        
    }
    private void setDropDowns()
    {
        ArrayList<String> values =new ArrayList<String>();
        values.add("-2");
        values.add("-1");
        values.add("0");
        values.add("1");
        values.add("2");        
        for(int i=0;i<m_aIssueNames.length;i++)
        {
            //clear dropdown
            m_gScreen.findNiftyControl(m_aIssueNames[i],DropDown.class).clear();            
            m_gScreen.findNiftyControl(m_aIssueNames[i],DropDown.class).addAllItems(values);
            m_gScreen.findNiftyControl(m_aIssueNames[i],DropDown.class).
                    selectItemByIndex(m_aIssueVals[i]+2);//keep the last configuration                 
        }
    }
    public void matchParty(Party party)
    {         
        for(int i=0;i<m_aIssueNames.length;i++)
        {   
            int val=party.getIssues().get(m_aIssueNames[i]);
            m_gScreen.findNiftyControl(m_aIssueNames[i],DropDown.class).selectItemByIndex(val+2);
        }
    }
    //ivars
    private int[] m_aIssueVals;
    private String[] m_aIssueNames;
    private int m_iPartySupport;
}
