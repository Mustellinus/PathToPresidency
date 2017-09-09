/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PtP_Core;

import java.util.HashMap;

/**The class from which all objects holding an opinion on issues inherit.
 *
 * @author Greg
 */
public abstract class OpinionHolder 
{
    public OpinionHolder()
    {
        m_aIssueNames=new String[]{"Abortion","Balanced Budget","Business Subsidies",
        "Censorship","Church/State Separation","Death Penalty","Drug Laws",
        "Environment","Free Market","Free Trade","Gay Marriage","Gun Control",
        "Immigration","Military Spending","National Health Care",
        "National Security","Welfare"};
        
        m_aIssues=new HashMap<String,Integer>();
        for(int issue=0;issue<m_aIssueNames.length;issue++)
        {
            m_aIssues.put(m_aIssueNames[issue],0);
        }    
    }
    public void setIssues(int[] issues)
    {
        for(int issue=0;issue<m_aIssueNames.length;issue++)
        {
            m_aIssues.put(m_aIssueNames[issue],issues[issue]);
        }  
    }
    public HashMap<String,Integer> getIssues()
    {
        return m_aIssues;
    }
    public String[] getIssueNames()
    {
        return m_aIssueNames;
    }
    public void setIssue(String str,Integer value)
    {
        m_aIssues.put(str,value);
    }
    protected HashMap<String,Integer> m_aIssues; 
    protected String[] m_aIssueNames;
}
