package com.github.dsquare68.homeforgeplugin.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Main view of this plugin.
 *
 * <p>The route value here is a placeholder – HUB registers the actual route
 * dynamically via {@link com.github.dsquare68.homeforgeplugin.TemplateHubPlugin#registerRoutes}
 * so this {@code @Route} annotation is used only during local development /
 * unit tests.
 *
 * <p><b>Plugin authors:</b> replace the content of this view with your UI.
 * You can inject services via constructor injection once you wire your own
 * Spring context inside the plugin.
 */
@PageTitle("My Plugin")
@Route("my-plugin")   // overridden at runtime by TemplateHubPlugin#registerRoutes
public class TemplateMainView extends VerticalLayout {

    public TemplateMainView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
            new H2("My Plugin"),
            new Paragraph("Welcome! Replace this view with your own content.")
        );
    }
}
