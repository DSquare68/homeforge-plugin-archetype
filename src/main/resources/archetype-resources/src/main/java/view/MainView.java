#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.view;

import ${package}.PluginInfo;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Main view of this plugin.
 *
 * <p>The route value here is a placeholder - HUB registers the actual route
 * dynamically via {@code ${package}.HubPluginImpl#registerRoutes} so this
 * {@code @Route} annotation is used only during local development / unit tests.
 */
@PageTitle(PluginInfo.TITLE)
@Route("${pluginPath.substring(1)}")
public class MainView extends VerticalLayout {

    public MainView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
            new H2(PluginInfo.PLUGIN_NAME),
            new Paragraph("Welcome! Replace this view with your own content.")
        );
    }
}
