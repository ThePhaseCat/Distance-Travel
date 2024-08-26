package phase.distance_travel;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class DistanceTravelClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("distance_travel");

	Timer timer = new Timer();

	public static boolean isDistanceTravelModeOn = false;

	public static boolean isTimerActive = false;

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
					start_DT_track(context);
					return 1;
				})));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dt_end")
				.executes(context -> {
					end_DT_track(context);
					return 1;
				})));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dt_stats")
				.executes(context -> {
					DT_stats(context);
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

	public void start_DT_track(CommandContext<FabricClientCommandSource> context)
	{
		startXPosition = MinecraftClient.getInstance().player.getBlockPos().getX();
		startZPosition = MinecraftClient.getInstance().player.getBlockPos().getZ();
		LOGGER.info("Start position set to: " + startXPosition + ", " + startZPosition);
		isDistanceTravelModeOn = true;
	}

	public void end_DT_track(CommandContext<FabricClientCommandSource> context)
	{
		int endXPosition = MinecraftClient.getInstance().player.getBlockPos().getX();
		int endZPosition = MinecraftClient.getInstance().player.getBlockPos().getZ();
		LOGGER.info("End position set to: " + endXPosition + ", " + endZPosition);
		int distance = Math.abs(endXPosition - startXPosition);
		//context.getSource().sendFeedback(Text.of("Distance Travelled: " + distance));
		isDistanceTravelModeOn = false;
	}

	public void DT_stats(CommandContext<FabricClientCommandSource> context)
	{
		if(isTimerActive)
		{
			context.getSource().sendFeedback(Text.of("Please wait. Wrapping tracking up."));
		}
		if(isDistanceTravelModeOn)
		{
			context.getSource().sendFeedback(Text.of("Distance Travel Mode is currently on. Please use /dt_end to end tracking."));
		}
		else
		{
			context.getSource().sendFeedback(Text.of("Distance Travel Mode is currently off. Please use /dt_start to begin tracking."));
		}

	}

	public void timerStuff()
	{
		if(isDistanceTravelModeOn)
		{
			//add stuff here
		}
		else
		{
			//run logic to end it
		}
	}
}