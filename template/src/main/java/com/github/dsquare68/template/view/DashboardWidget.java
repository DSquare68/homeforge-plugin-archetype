package com.github.dsquare68.template.view;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Dashboard widget contributed to the HUB home screen.
 *
 * <p>HUB instantiates this component and places it inside a card on the
 * dashboard. Keep it lightweight - it renders on every dashboard load.
 */
public class DashboardWidget extends VerticalLayout {

    public DashboardWidget() {
        setSpacing(false);
        setPadding(false);
        setWidth("100%");

        add(
            new Paragraph("Replace this widget with a summary of your plugin's data."),
            new Span("-> Configure in DashboardWidget.java")
        );
    }
}
