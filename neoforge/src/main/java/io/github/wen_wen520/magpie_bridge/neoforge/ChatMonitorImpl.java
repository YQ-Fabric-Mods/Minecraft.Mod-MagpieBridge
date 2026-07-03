package io.github.wen_wen520.magpie_bridge.neoforge;

import java.util.UUID;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import io.github.wen_wen520.magpie_bridge.*;

@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public class ChatMonitorImpl {

	public static void init() {
		Main.LOGGER.info("[NeoForge] ChatMonitorImpl initialized.");
	}

	@SubscribeEvent
	public static void onClientChatReceived(ClientChatReceivedEvent event) {

		if (!Utils.isNotificationOn()) {
			return;
		}

		if (event instanceof ClientChatReceivedEvent.Player playerEvent) {
			handlePlayerChat(playerEvent);
		}
		else if (Main.Settings.include_system && event instanceof ClientChatReceivedEvent.System systemEvent && !event.isCanceled()) {
			handleSystemChat(systemEvent);
		}
	}

	// Received Player Chat Message
	private static void handlePlayerChat(ClientChatReceivedEvent.Player event) {

		String rawName = event.getBoundChatType().name().getString();
		String rawBody = event.getMessage().getString();

		if (rawName.isEmpty()){
			rawName = "Unknown";
		}

		final UUID senderUUID = event.getPlayerChatMessage().sender();
		final String senderName = MessagePipeline.ClearNameStyle(rawName);
		final String messageBody = MessagePipeline.ClearBodyStyle(rawBody);

		SkinResource.getPlayerHead(senderUUID).whenComplete((path, throwable) -> {
			GeneralMessage.Builder builder = GeneralMessage.builder()
					.title(senderName)
					.body(messageBody)
					.icon(path);

			try {
				Notifier.send(builder.build());
			}
			catch (Exception notifyException) {
				Main.LOGGER.error("Failed to send notification for chat message from {}: {}", senderName, notifyException.getMessage());
			}
		});
	}

	// Received System Message
	private static void handleSystemChat(ClientChatReceivedEvent.System event) {

		MessagePipeline.ProcessSystemMessage(event.getMessage());

	}
}
