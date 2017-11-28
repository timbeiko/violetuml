package com.horstmann.violet.framework.statistics;
import java.io.File;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import com.horstmann.violet.product.diagram.classes.*;
import com.horstmann.violet.product.diagram.sequence.*;

import com.horstmann.violet.framework.dialog.DialogFactory;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.BeanInjector;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.InjectedBean;
import com.horstmann.violet.framework.injection.resources.ResourceBundleInjector;
import com.horstmann.violet.framework.injection.resources.annotation.ResourceBundleBean;
import com.horstmann.violet.product.diagram.abstracts.IGraph;

@ResourceBundleBean(resourceReference = StatisticsPanel.class)
public class StatisticsEngine
{

    public StatisticsEngine(IGraph graph)
    {
        BeanInjector.getInjector().inject(this);
    	ResourceBundleInjector.getInjector().inject(this);
        this.graph = graph;
    }

    public void start()
    {
        StatisticsPanel statsPanel;
        if (this.graph instanceof ClassDiagramGraph) {
            String classStatsPath = Paths.get("./class-statistics").toAbsolutePath().normalize().toString();
            File classStatsDir = new File(classStatsPath);
            File[] classFiles = classStatsDir.listFiles();
            statsPanel = new StatisticsPanel("class", classFiles);
        } else if (this.graph instanceof SequenceDiagramGraph) {
            String seqStatsPath = Paths.get("./sequencediagram-statistics").toAbsolutePath().normalize().toString();
            File seqStatsDir = new File(seqStatsPath);
            File[] seqFiles = seqStatsDir.listFiles();
            statsPanel = new StatisticsPanel("sequence", seqFiles);
        } else {
            throw new java.lang.Error("Need a Class or Sequence diagram");
        }

        JOptionPane optionPane = new JOptionPane();
        optionPane.setOptions(new String[]
        {
            this.statsCloseText
        });
        optionPane.setMessage(statsPanel);
        optionPane.setBorder(new EmptyBorder(0, 0, 10, 0));
        this.dialogFactory.showDialog(optionPane, this.statsTitle, true);
    }

    @ResourceBundleBean(key = "dialog.statistics.title.text", resourceReference = StatisticsPanel.class)
    private String statsTitle;
    
    @ResourceBundleBean(key = "dialog.statistics.close.text", resourceReference = StatisticsPanel.class)
    private String statsCloseText;
        
    @InjectedBean
    private DialogFactory dialogFactory;

    private IGraph graph;

}
