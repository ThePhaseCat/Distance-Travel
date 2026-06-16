package phase.distance_travel;

import com.mojang.brigadier.context.CommandContext;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DistanceTravelClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("distance_travel");

	private ScheduledExecutorService executor;

	public static boolean isDistanceTravelModeOn = false;

	public static boolean isTimerActive = false;

	public static double timerAmount = 0;

	public static double currentXPosition = 0;

	public static double currentZPosition = 0;

	public static double lastXPosition = 0;

	public static double lastZPosition = 0;

	public static double endXPosition = 0;

	public static double endZPosition = 0;

	public static double currentSectionDistanceX = 0;

	public static double currentSectionDistanceZ = 0;

	public static double finalDistanceX = 0;

	public static double finalDistanceZ = 0;

	public static double finalFinalDistance = 0;

	public static BlockPos startPosition = new BlockPos(0, 0, 0);
	public static BlockPos finalPosition = new BlockPos(0, 0, 0);

	//we do not care about y position at all

	@Override
	public void onInitializeClient() {

		MidnightConfig.init("distance_travel", DT_Config.class);

		LOGGER.info("Distance Travel client started!");

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommands.literal("dt_start")
				.executes(context -> {
					start_DT_track(context);
					return 1;
				})));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommands.literal("dt_end")
				.executes(context -> {
					end_DT_track(context);
					return 1;
				})));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommands.literal("dt_stats")
				.executes(context -> {
					DT_stats(context);
					return 1;
				})));

		LOGGER.info("Distance Travel client finished starting! Have fun with all your epic calculations!");
	}

	public void start_DT_track(CommandContext<FabricClientCommandSource> context)
	{
		if(isDistanceTravelModeOn || isTimerActive)
		{
			Minecraft.getInstance().execute(() ->
					context.getSource().sendFeedback(Component.nullToEmpty("Distance Travel Mode is already on. Please use /dt_end to end tracking."))
			);
			return;
		}
		lastXPosition = Minecraft.getInstance().player.blockPosition().getX();
		lastZPosition = Minecraft.getInstance().player.blockPosition().getZ();
		finalDistanceX = 0;
		finalDistanceZ = 0;
		//LOGGER.info("Start position set to: " + startXPosition + ", " + startZPosition);
		isDistanceTravelModeOn = true;
		isTimerActive = true;
		startPosition = Minecraft.getInstance().player.blockPosition();
		context.getSource().sendFeedback(Component.nullToEmpty("Tracking started!"));
		executor = Executors.newSingleThreadScheduledExecutor();

		executor.scheduleAtFixedRate(() -> {
			timerStuff(context);
		}, DT_Config.timerInterval, DT_Config.timerInterval, TimeUnit.MILLISECONDS);
	}

	public void end_DT_track(CommandContext<FabricClientCommandSource> context)
	{
		if(!isTimerActive)
		{
			context.getSource().sendFeedback(Component.nullToEmpty("Distance Travel Mode is not on. Please use /dt_start to start tracking."));
			return;
		}
		endXPosition = Minecraft.getInstance().player.blockPosition().getX();
		endZPosition = Minecraft.getInstance().player.blockPosition().getZ();
		//LOGGER.info("End position set to: " + endXPosition + ", " + endZPosition);
		//finalDistance = Math.abs(endXPosition - startXPosition);
		context.getSource().sendFeedback(Component.nullToEmpty("Wrapping up tracking..."));
		isDistanceTravelModeOn = false;
	}

	public void DT_stats(CommandContext<FabricClientCommandSource> context)
	{
		if(isDistanceTravelModeOn)
		{
			Minecraft.getInstance().execute(() ->
					context.getSource().sendFeedback(Component.nullToEmpty("Distance Travel Mode is currently on. Please use /dt_end to end tracking."))
			);
			return;
		}
		if(isTimerActive)
		{
			Minecraft.getInstance().execute(() ->
					context.getSource().sendFeedback(Component.nullToEmpty("Please wait. Wrapping up tracking..."))
			);
			return;
		}
		else
		{
			Minecraft.getInstance().execute(() -> {
				context.getSource().sendFeedback(Component.nullToEmpty("Stats of last tracking session..."));
				context.getSource().sendFeedback(Component.nullToEmpty("Total distance traveled: " + convertDistanceToActualDistance(0)));
				context.getSource().sendFeedback(Component.nullToEmpty("Tracking time: " + convertTimerAmountToActualTime()));
				context.getSource().sendFeedback(Component.nullToEmpty("Start position: " + startPosition.getX() + ", " + startPosition.getY() + ", " + startPosition.getZ()));
				context.getSource().sendFeedback(Component.nullToEmpty("End position: " + finalPosition.getX() + ", " + finalPosition.getY() + ", " + finalPosition.getZ()));
			});
		}

	}

	public void timerStuff(CommandContext<FabricClientCommandSource> context)
	{
		if(isDistanceTravelModeOn) //do all tracking stuff
		{
			currentXPosition = Minecraft.getInstance().player.blockPosition().getX();
			currentZPosition = Minecraft.getInstance().player.blockPosition().getZ();

			//LOGGER.info("Current position set to: " + currentXPosition + ", " + currentZPosition);

			currentSectionDistanceX = Math.abs(currentXPosition - lastXPosition);
			currentSectionDistanceZ = Math.abs(currentZPosition - lastZPosition);
			finalDistanceX += currentSectionDistanceX;
			finalDistanceZ += currentSectionDistanceZ;

			//LOGGER.info("Current x distance is: " + currentSectionDistanceX);
			//LOGGER.info("Current z distance is: " + currentSectionDistanceZ);

			lastXPosition = currentXPosition;
			lastZPosition = currentZPosition;

			//LOGGER.info("final x distance is: " + finalDistanceX);
			//LOGGER.info("final z distance is: " + finalDistanceZ);

			if(DT_Config.printTrackingMessages) //prints the tracking message
			{
				Minecraft.getInstance().execute(() ->
						context.getSource().sendFeedback((Component.nullToEmpty("Tracking...")))
				);
			}

			if(DT_Config.odoMode) //do odometer stuff
			{
				double odoDistance = Math.sqrt(Math.pow(finalDistanceX, 2) + Math.pow(finalDistanceZ, 2));
				Minecraft.getInstance().execute(() ->
						context.getSource().sendFeedback((Component.nullToEmpty("Distance since start: " + convertDistanceToActualDistance(odoDistance))))
				);
			}

			timerAmount += DT_Config.timerInterval;
		}
		else
		{
			isTimerActive = false;
			if(executor != null)
			{
				executor.schedule(() -> executor.shutdownNow(), 10, TimeUnit.MILLISECONDS);
			}
			//System.out.println("endXPosition: " + endXPosition);
			//System.out.println("lastXPosition: " + lastXPosition);
			currentSectionDistanceX = Math.abs(endXPosition - lastXPosition);
			currentSectionDistanceZ = Math.abs(endZPosition - lastZPosition);
			//LOGGER.info("Current x distance is: " + currentSectionDistanceX);
			//LOGGER.info("Current z distance is: " + currentSectionDistanceZ);
			finalDistanceX += currentSectionDistanceX;
			finalDistanceZ += currentSectionDistanceZ;
			//LOGGER.info("final x distance is: " + finalDistanceX);
			//LOGGER.info("final z distance is: " + finalDistanceZ);
			finalFinalDistance = Math.sqrt(Math.pow(finalDistanceX, 2) + Math.pow(finalDistanceZ, 2));
			LOGGER.info("Final distance is: " + finalFinalDistance);
			currentSectionDistanceX = 0;
			currentSectionDistanceZ = 0;
			timerAmount += DT_Config.timerInterval;
			finalPosition = Minecraft.getInstance().player.blockPosition();
			if(DT_Config.goToStatsAfterDone)
			{
				Minecraft.getInstance().execute(() ->
						DT_stats(context)
				);
			}
			else
			{
				Minecraft.getInstance().execute(() ->
						context.getSource().sendFeedback(Component.nullToEmpty("Tracking finished! Please use /dt_stats to see the results!"))
				);
			}
		}
	}

	//converts the distance to meters or kilometers
	public String convertDistanceToActualDistance(double distance)
	{
		if(distance == 0) //final tracking
		{
			if(finalFinalDistance >= 1000)
			{
				//return the km value plus two decimal places
				return String.format("%.2f", finalFinalDistance / 1000) + " km";
			}
			else
			{
				return String.format("%.2f", finalFinalDistance) + " m";
			}
		}
		else
		{
			if(distance >= 1000)
			{
				//return the km value plus two decimal places
				return String.format("%.2f", distance / 1000) + " km";
			}
			else
			{
				return String.format("%.2f", distance) + " m";
			}
		}
	}

	public String convertTimerAmountToActualTime()
	{
		//convert from milliseconds to seconds
		double seconds = timerAmount / 1000.0;

		if(seconds >= 3600)
		{
			return String.format("%.2f", seconds/3600) + " hours";
		}
		if(seconds >= 60)
		{
			return String.format("%.2f", seconds / 60) + " minutes";
		}

		//default case
		return seconds + " seconds";
	}
}