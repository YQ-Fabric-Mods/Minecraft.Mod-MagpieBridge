package io.github.wen_wen520.magpie_bridge;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.StringUtil;

public final class MessagePipeline {

	private static final String PLAYER_JOINED_KEY = "multiplayer.player.joined";
	private static final String PLAYER_JOINED_RENAMED_KEY = "multiplayer.player.joined.renamed";
	private static final String PLAYER_LEFT_KEY = "multiplayer.player.left";

	// 1. 处理带括号包裹的格式: <...> msg, [...] msg
	// 我们只捕获最外层括号内的完整内容和之后的消息
	// 分隔符支持 : > >> 或空格
	private static final Pattern BRACKET_FORMAT = Pattern.compile(
			"^[<\\[{(](.+)[>\\]})](?:\\s*[:>]+\\s*|\\s+)(.+)$"
	);

	// 2. 处理无外层包裹的格式: [Tag] Player > Msg 或 Player: Msg
	// 这里优化了捕获组，确保能跳过前面的所有 [Tag] 标签
	private static final Pattern SPLIT_FORMAT = Pattern.compile(
			"^(?:\\[.*?\\]\\s*)*([^\\s:>]+)\\s*[:>]{1,2}\\s*(.+)$"
	);

	// 3. 用于剔除名称中残留的 [Prefix] 标签
	private static final Pattern PREFIX_CLEANER = Pattern.compile("\\[.*?\\]");

	/**
	 * 清理并获取玩家名称，会自动剔除内部的 [Prefix] 标签
	 */
	public static String ClearNameStyle(String name) {
		name = StringUtil.stripColor(name);
		name = PREFIX_CLEANER.matcher(name).replaceAll("");
		name = name.replaceAll("[<>\\[\\]{}()]", "").trim();

		return name.isEmpty() ? "Unknown" : name;
	}

	/**
	 * 清理并获取消息内容
	 */
	public static String ClearBodyStyle(String body) {
		body = StringUtil.stripColor(body);

		Matcher m1 = BRACKET_FORMAT.matcher(body);
		if (m1.matches()){
			return m1.group(2).trim();
		}

		Matcher m2 = SPLIT_FORMAT.matcher(body);
		if (m2.matches()){
			return m2.group(2).trim();
		}

		return body;
	}

	/**
	 * 处理系统消息并提取发送者与内容
	 */
	public static void ProcessSystemMessage(Component message) {
		if (message == null) {
			return;
		}

		if (ProcessPlayerEventMessage(message)) {
			return;
		}

		ProcessSystemMessage(message.getString());
	}

	/**
	 * 处理系统消息并提取发送者与内容
	 */
	public static void ProcessSystemMessage(String text) {

		text = StringUtil.stripColor(text);
		String rawName = "";
		String rawBody = "";

		Matcher m1 = BRACKET_FORMAT.matcher(text);
		if (m1.matches()){
			rawName = ClearNameStyle(m1.group(1));
			rawBody = m1.group(2).trim();
		}

		Matcher m2 = SPLIT_FORMAT.matcher(text);
		if (m2.matches()){
				rawName = ClearNameStyle(m2.group(1));
				rawBody = m2.group(2).trim();
		}

		if (rawName.isEmpty() || rawBody.isEmpty()) {
			return;
		}

		final String senderName = rawName;
		final String messageBody = rawBody;

		SendPlayerNotification(senderName, messageBody);
	}

	private static boolean ProcessPlayerEventMessage(Component message) {
		ComponentContents contents = message.getContents();

		if (!(contents instanceof TranslatableContents translatable)) {
			return false;
		}

		String key = translatable.getKey();
		if (!IsPlayerEventKey(key)) {
			return false;
		}

		Object[] args = translatable.getArgs();
		if (args.length == 0) {
			return false;
		}

		String rawName = ComponentArgumentToString(args[0]);
		if (rawName.isBlank()) {
			return false;
		}

		String senderName = ClearNameStyle(rawName);
		String fullText = StringUtil.stripColor(message.getString()).trim();
		String messageBody = RemoveLeadingName(fullText, rawName, senderName);

		SendPlayerNotification(senderName, messageBody);
		return true;
	}

	private static boolean IsPlayerEventKey(String key) {
		return PLAYER_JOINED_KEY.equals(key) || PLAYER_JOINED_RENAMED_KEY.equals(key) || PLAYER_LEFT_KEY.equals(key);
	}

	private static String ComponentArgumentToString(Object arg) {
		if (arg == null) {
			return "";
		}

		if (arg instanceof Component component) {
			return component.getString();
		}

		return String.valueOf(arg);
	}

	private static String RemoveLeadingName(String text, String rawName, String senderName) {
		String body = RemoveLeadingText(text, rawName);
		if (!body.equals(text)) {
			return body;
		}

		body = RemoveLeadingText(text, senderName);
		return body.isEmpty() ? text : body;
	}

	private static String RemoveLeadingText(String text, String leadingText) {
		if (!leadingText.isBlank() && text.startsWith(leadingText)) {
			return text.substring(leadingText.length()).trim();
		}

		return text;
	}

	private static void SendPlayerNotification(String senderName, String messageBody) {
		SkinResource.getPlayerHead(senderName).whenComplete((path, throwable) -> {
			Path icon = path != null ? path : Main.DEFAULT_HEAD;
			GeneralMessage.Builder builder = GeneralMessage.builder()
					.title(senderName)
					.body(messageBody)
					.icon(icon);

			try {
				Notifier.send(builder.build());
			}
			catch (Exception notifyException) {
				Main.LOGGER.error("Failed to send notification for chat message from {}: {}", senderName, notifyException.getMessage());
			}
		});
	}

}
