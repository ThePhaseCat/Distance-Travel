package phase.distance_travel;

import static net.minecraft.server.command.CommandManager.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistanceTravelClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("distance_travel");

	public static int startXPosition = 0;

	public static int startZPosition = 0;


	@Override
	public void onInitializeClient() {

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dt_start")
				.executes(context -> {
					startXPosition = MinecraftClient.getInstance().player.getBlockPos().getX();
					LOGGER.info("Start position set to: " + startXPosition);
					return 1;
				})));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dt_end")
				.executes(context -> {
					int endXPosition = MinecraftClient.getInstance().player.getBlockPos().getX();
					LOGGER.info("End position set to: " + endXPosition);
					int distance = Math.abs(endXPosition - startXPosition);
					context.getSource().sendFeedback(Text.of("Distance Travelled: " + distance));
					return 1;
				})));

		LOGGER.info("Distance Travel client initialized!");
	}
}