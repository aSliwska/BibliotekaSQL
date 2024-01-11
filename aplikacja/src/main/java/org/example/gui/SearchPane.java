package org.example.gui;

import org.example.gui.searchform.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SearchPane extends JPanel {

    private class SearchTab {
        private String title;
        private JPanel tab;
        private JPanel tableWrapper;

        SearchTab(String title, JPanel tab, JPanel tableWrapper) {
            this.title = title;
            this.tab = tab;
            this.tableWrapper = tableWrapper;
        }
    }
    private ArrayList<SearchTab> tabs = new ArrayList<SearchTab>();
//    private final Color backgroundColor = new Color(200, 200, 200);

    public SearchPane() {
        super(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        tabs.add(getSearchTab("Czytelnicy", new ReaderSearchForm()));
        tabs.add(getSearchTab("Wpłaty", new PaymentSearchForm()));
        tabs.add(getSearchTab("Wypożyczenia", new BorrowLogSearchForm()));
        tabs.add(getSearchTab("Egzemplarze", new ItemSearchForm()));
        tabs.add(getSearchTab("Książki", new BookSearchForm()));
        tabs.add(getSearchTab("Autorzy", new AuthorSearchForm()));
        tabs.add(getSearchTab("Wydawnictwa", new PublisherSearchForm()));
        tabs.add(getSearchTab("Gatunki", new GenreSearchForm()));
        tabs.add(getSearchTab("Gatunki danych książek", new GenreToBookSearchForm()));

        for (SearchTab tab : tabs) {
            JScrollPane scrollPane = new JScrollPane(tab.tab, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            tabbedPane.addTab(tab.title, scrollPane);
        }

        this.add(tabbedPane, BorderLayout.CENTER);
    }

    private SearchTab getSearchTab(String title, SearchForm form) {
        JPanel tab = new JPanel(new BorderLayout());
//        tab.setBackground(backgroundColor);

        // forma do wprowadzenia parametrow szukania
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.add(form, BorderLayout.LINE_START);
        formWrapper.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // guziki
        JButton searchButton = new JButton("Szukaj");
        JButton clearButton = new JButton("Wyczyść");
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.add(searchButton, BorderLayout.LINE_START);
        buttonWrapper.add(clearButton, BorderLayout.LINE_END);
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // wiadomosc o bledzie zwrocona przez SQL
        JLabel errorMessage = new JLabel();
        JPanel messageWrapper = new JPanel(new BorderLayout());
        messageWrapper.add(errorMessage, BorderLayout.LINE_START);
        messageWrapper.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // wrapper
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.PAGE_AXIS));
        wrapper.add(formWrapper);
        wrapper.add(buttonWrapper);
        wrapper.add(messageWrapper);

        tab.add(wrapper, BorderLayout.PAGE_START);

        JPanel tableWrapper = new JPanel();
        tab.add(tableWrapper, BorderLayout.CENTER);

        SearchTab searchTab = new SearchTab(title, tab, tableWrapper);

        // funkcjonalnosc guzikow
        searchButton.addActionListener(e -> displaySearchResults(form, errorMessage, searchTab));
        clearButton.addActionListener(e -> form.clearFields());

        return searchTab;
    }

    public void displaySearchResults(Searchable form, JLabel errorMessage, SearchTab searchTab) {
        // perform the search and wrap the result
        JPanel tableWrapper = new JPanel(new BorderLayout());
        JTable table = form.search(errorMessage);

        tableWrapper.add(table.getTableHeader(), BorderLayout.PAGE_START);
        tableWrapper.add(table, BorderLayout.CENTER);

        // re-add result to pane
        searchTab.tab.remove(searchTab.tableWrapper);
        searchTab.tableWrapper = tableWrapper;
        searchTab.tab.add(searchTab.tableWrapper, BorderLayout.CENTER);
        searchTab.tab.invalidate();
        searchTab.tab.validate();
    }
}
