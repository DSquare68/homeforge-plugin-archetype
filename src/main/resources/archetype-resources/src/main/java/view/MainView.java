package ${package}.view;

import com.github.dsquare68.homeforgeapi.ui.BaseLayout;
import ${package}.PluginInfo;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Main view of this plugin.
 *
 * <p>{@code layout = BaseLayout.class} nests this view inside the HUB's shared
 * {@link BaseLayout} shell (navbar + drawer) so the plugin page renders within
 * the host chrome. {@code BaseLayout} lives in the {@code homeforge-api} jar,
 * which the HUB host loads with its parent classloader, so the plugin and host
 * see the same class - the requirement for Vaadin to match the layout across
 * the PF4J classloader boundary.
 *
 * <p>The route value here mirrors {@link PluginInfo#PLUGIN_PATH} without its
 * leading slash - HUB registers the actual route dynamically via
 * {@link ${package}.HubPluginImpl#routes()} so this
 * {@code @Route} annotation is used only during local development / unit tests.
 */
@PageTitle(PluginInfo.TITLE)
@Route(layout = BaseLayout.class, value = "${pluginPath}")
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
