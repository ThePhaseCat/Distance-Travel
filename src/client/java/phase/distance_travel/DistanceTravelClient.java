package phase.distance_travel;

import static net.minecraft.server.command.CommandManager.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistanceTravelClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("distance_travel");


	@Override
	public void onInitializeClient() {

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("foo")
				.executes(context -> {
					context.getSource().sendFeedback(() -> Text.literal("you just called this command"), false);
					return 1;
				})));

		LOGGER.info("Distance Travel client initialized!");
	}
}