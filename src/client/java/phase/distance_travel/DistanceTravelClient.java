package phase.distance_travel;

import com.mojang.brigadier.context.CommandContext;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class DistanceTravelClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("distance_travel");

	Timer timer = new Timer();

	public static boolean isDistanceTravelModeOn = false;

	public static boolean isTimerActive = false;

	public static double timerAmount = 0;

	public static int currentXPosition = 0;

	public static int currentZPosition = 0;

	public static int lastXPosition = 0;

	public static int lastZPosition = 0;

	public static int endXPosition = 0;

	public static int endZPosition = 0;

	public static int currentSectionDistanceX = 0;

	public static int currentSectionDistanceZ = 0;

	public static int finalDistanceX = 0;

	public static int finalDistanceZ = 0;

	public static int finalFinalDistance = 0;

	public static BlockPos startPosition = new BlockPos(0, 0, 0);
	public static BlockPos finalPosition = new BlockPos(0, 0, 0);

	//we do not care about y position at all

	@Override
	public void onInitializeClient() {

		MidnightConfig.init("distance_travel", DT_Config.class);

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

		LOGGER.info("Distance Travel client finished starting! Have fun with all your epic calculations!");
	}

	public void start_DT_track(CommandContext<FabricClientCommandSource> context)
	{
		if(isDistanceTravelModeOn || isTimerActive)
		{
			context.getSource().sendFeedback(Text.of("Distance Travel Mode is already on. Please use /dt_end to end tracking."));
			return;
		}
		lastXPosition = MinecraftClient.getInstance().player.getBlockPos().getX();
		lastZPosition = MinecraftClient.getInstance().player.getBlockPos().getZ();
		finalDistanceX = 0;
		finalDistanceZ = 0;
		//LOGGER.info("Start position set to: " + startXPosition + ", " + startZPosition);
		isDistanceTravelModeOn = true;
		isTimerActive = true;
		startPosition = MinecraftClient.getInstance().player.getBlockPos();
		context.getSource().sendFeedback(Text.of("Tracking started!"));
		timer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				timerStuff(context);
			}
		}, DT_Config.timerInterval, DT_Config.timerInterval);
	}

	public void end_DT_track(CommandContext<FabricClientCommandSource> context)
	{
		if(!isTimerActive)
		{
			context.getSource().sendFeedback(Text.of("Distance Travel Mode is not on. Please use /dt_start to start tracking."));
			return;
		}
		endXPosition = MinecraftClient.getInstance().player.getBlockPos().getX();
		endZPosition = MinecraftClient.getInstance().player.getBlockPos().getZ();
		//LOGGER.info("End position set to: " + endXPosition + ", " + endZPosition);
		//finalDistance = Math.abs(endXPosition - startXPosition);
		context.getSource().sendFeedback(Text.of("Wrapping up tracking..."));
		isDistanceTravelModeOn = false;
	}

	public void DT_stats(CommandContext<FabricClientCommandSource> context)
	{
		if(isDistanceTravelModeOn)
		{
			context.getSource().sendFeedback(Text.of("Distance Travel Mode is currently on. Please use /dt_end to end tracking."));
			return;
		}
		if(isTimerActive)
		{
			context.getSource().sendFeedback(Text.of("Please wait. Wrapping up tracking..."));
			return;
		}
		else
		{
			context.getSource().sendFeedback(Text.of("Stats of last tracking session..."));
			context.getSource().sendFeedback(Text.of("Total distance traveled: " + convertDistanceToActualDistance(0)));
			context.getSource().sendFeedback(Text.of("Tracking time: " + convertTimerAmountToActualTime()));
			context.getSource().sendFeedback(Text.of("Start position: " + startPosition.getX() + ", " + startPosition.getY() + ", " + startPosition.getZ()));
			context.getSource().sendFeedback(Text.of("End position: " + finalPosition.getX() + ", " + finalPosition.getY() + ", " + finalPosition.getZ()));
		}

	}

	public void timerStuff(CommandContext<FabricClientCommandSource> context)
	{
		if(isDistanceTravelModeOn)
		{
			currentXPosition = MinecraftClient.getInstance().player.getBlockPos().getX();
			currentZPosition = MinecraftClient.getInstance().player.getBlockPos().getZ();
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
			if(DT_Config.printTrackingMessages)
			{
				context.getSource().sendFeedback(Text.of("Tracking..."));
			}
			if(DT_Config.odoMode) //do odometer stuff
			{
				int odoDistance = (int) Math.sqrt(Math.pow(currentSectionDistanceX, 2) + Math.pow(currentSectionDistanceZ, 2));
				context.getSource().sendFeedback(Text.of("Distance since last track: " + convertDistanceToActualDistance(odoDistance)));
			}
			timerAmount += DT_Config.timerInterval;
		}
		else
		{
			isTimerActive = false;
			timer.cancel();
			timer = new Timer();
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
			finalFinalDistance = Math.abs(finalDistanceX) + Math.abs(finalDistanceZ);
			LOGGER.info("Final distance is: " + finalFinalDistance);
			currentSectionDistanceX = 0;
			currentSectionDistanceZ = 0;
			timerAmount += DT_Config.timerInterval;
			finalPosition = MinecraftClient.getInstance().player.getBlockPos();
			if(DT_Config.goToStatsAfterDone)
			{
				DT_stats(context);
			}
			else
			{
				context.getSource().sendFeedback(Text.of("Tracking finished! Please use /dt_stats to see the results!"));
			}
		}
	}

	//converts the distance to meters or kilometers
	public String convertDistanceToActualDistance(int distance)
	{
		if(distance == 0) //final tracking
		{
			if(finalFinalDistance >= 1000)
			{
				//return the km value plus two decimal places
				return String.format("%.2f", (double)finalFinalDistance / 1000) + " km";
			}
			else
			{
				return finalFinalDistance + " m";
			}
		}
		else
		{
			if(distance >= 1000)
			{
				//return the km value plus two decimal places
				return String.format("%.2f", (double)distance / 1000) + " km";
			}
			else
			{
				return distance + " m";
			}
		}
	}

	public String convertTimerAmountToActualTime()
	{
		//convert from milliseconds to seconds
		timerAmount = timerAmount / 1000;

		if(timerAmount >= 60)
		{
			//convert from seconds to minutes
			timerAmount = timerAmount / 60;

			if(timerAmount >= 60)
			{
				//convert from minutes to hours
				timerAmount = timerAmount / 60;

				return timerAmount + " hours";
			}
			else
			{
				return timerAmount + " minutes";
			}
		}
		else
		{
			return timerAmount + " seconds";
		}
	}
}