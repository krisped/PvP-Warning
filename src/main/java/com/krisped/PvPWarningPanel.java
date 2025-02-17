package com.krisped;

import javax.inject.Inject;
import javax.swing.JLabel;
import net.runelite.client.ui.PluginPanel;
import java.awt.BorderLayout;

public class PvPWarningPanel extends PluginPanel
{
    @Inject
    public PvPWarningPanel(PvPWarningConfig config)
    {
        setLayout(new BorderLayout());
        add(new JLabel("PvP Warning Plugin Panel"), BorderLayout.NORTH);
    }
}
