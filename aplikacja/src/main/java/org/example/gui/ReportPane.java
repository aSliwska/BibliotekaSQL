package org.example.gui;

import org.example.gui.report.DebtReport;
import org.example.gui.report.GenreReport;
import org.example.gui.report.ItemsOverTimeLimitReport;
import org.example.gui.report.Report;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ReportPane extends JPanel {

    private class ReportTab {
        private String title;
        private JPanel tab;
        private JPanel tableWrapper;

        ReportTab(String title, JPanel tab, JPanel tableWrapper) {
            this.title = title;
            this.tab = tab;
            this.tableWrapper = tableWrapper;
        }
    }
    private ArrayList<ReportTab> tabs = new ArrayList<>();

    public ReportPane() {
        super(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        tabs.add(getReportTab("Najczęściej występujące gatunki", new GenreReport()));
        tabs.add(getReportTab("Nieoddane na czas egzemplarze (nadal wypożyczone)", new ItemsOverTimeLimitReport()));
        tabs.add(getReportTab("Dłużnicy", new DebtReport()));

        for (ReportTab tab : tabs) {
            JScrollPane scrollPane = new JScrollPane(tab.tab, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            tabbedPane.addTab(tab.title, scrollPane);
        }
        this.add(tabbedPane, BorderLayout.CENTER);
    }



    private ReportTab getReportTab(String title, Report report) {
        JPanel tab = new JPanel(new BorderLayout());

        // guzik
        JButton refreshButton = new JButton("Odśwież");
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.add(refreshButton, BorderLayout.LINE_START);
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        tab.add(buttonWrapper, BorderLayout.PAGE_START);

        JPanel tableWrapper = new JPanel();
        tab.add(tableWrapper, BorderLayout.CENTER);

        ReportTab reportTab = new ReportTab(title, tab, tableWrapper);

        // funkcjonalnosc guzikow
        refreshButton.addActionListener(e -> displaySearchResults(report, reportTab));

        return reportTab;
    }

    public void displaySearchResults(Report report, ReportTab reportTab) {
        // perform the search and wrap the result
        JPanel tableWrapper = new JPanel(new BorderLayout());
        JTable table = report.fetch();

        tableWrapper.add(table.getTableHeader(), BorderLayout.PAGE_START);
        tableWrapper.add(table, BorderLayout.CENTER);

        // re-add result to pane
        reportTab.tab.remove(reportTab.tableWrapper);
        reportTab.tableWrapper = tableWrapper;
        reportTab.tab.add(reportTab.tableWrapper, BorderLayout.CENTER);
        reportTab.tab.invalidate();
        reportTab.tab.validate();
        reportTab.tab.repaint();
    }
}
