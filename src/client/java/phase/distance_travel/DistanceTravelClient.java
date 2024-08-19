package phase.distance_travel;

import static net.minecraft.server.command.CommandManager.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class DistanceTravelClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("distance_travel");

	Timer timer = new Timer();

	public static boolean isDistanceTravelModeOn = false;

	public static int startXPosition = 0;

	public static int startZPosition = 0;

	public static int endXPosition = 0;

	public static int endZPosition = 0;

	//we do not care about y position at all

	@Override
	public void onInitializeClient() {

		LOGGER.info("Distance Travel client started!");

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dt_start")
				.executes(context -> {
					startXPosition = MinecraftClient.getInstance().player.getBlockPos().getX();
					startZPosition = MinecraftClient.getInstance().player.getBlockPos().getZ();
					LOGGER.info("Start position set to: " + startXPosition + ", " + startZPosition);
					isDistanceTravelModeOn = true;
					return 1;
				})));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dt_end")
				.executes(context -> {
					int endXPosition = MinecraftClient.getInstance().player.getBlockPos().getX();
					int endZPosition = MinecraftClient.getInstance().player.getBlockPos().getZ();
					LOGGER.info("End position set to: " + endXPosition + ", " + endZPosition);
					int distance = Math.abs(endXPosition - startXPosition);
					context.getSource().sendFeedback(Text.of("Distance Travelled: " + distance));
					isDistanceTravelModeOn = false;
					return 1;
				})));

		timer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				LOGGER.info("this should be printing!");
			}
		}, 5000, 5000);

		LOGGER.info("Distance Travel client finished starting! Have fun with all your epic calculations!");
	}
}