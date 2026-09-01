package ${package};

import org.springframework.web.bind.annotation.GetMapping;

import com.github.dsquare68.homeforgeapi.spi.HubApi;
import com.github.dsquare68.homeforgeapi.user.HubUser;

/**
 * Example REST controller, contributed to HUB via
 * {@link HubPluginImpl#restControllers()}.
 *
 * <p>Write methods exactly like a normal Spring MVC controller
 * ({@code @GetMapping}, {@code @PostMapping}, {@code @RequestBody}, ...) -
 * the one difference from a textbook Spring Boot controller is that nothing
 * here is {@code @Autowired}: this plugin has no Spring context of its own,
 * so {@link HubApi} (and anything else this controller needs) is passed in
 * through the constructor by hand, the same way {@link HubPluginImpl} stores
 * it during {@code onActivate}.
 *
 * <p>Whatever path you declare below, HUB always serves it at
 * {@code /api/plugins/${pluginId}/hello} - not at {@code /hello} and not at
 * any other prefix. Delete this class if your plugin has no REST API of its
 * own.
 */
public class ExampleController {

    private final HubApi api;

    public ExampleController(HubApi api) {
        this.api = api;
    }

    @GetMapping("/hello")
    public String hello() {
        HubUser me = api.user().currentUser();
        return "Hello, " + me.displayName() + "!";
    }
}
