package net.mrbt0907.configex.event;

import net.minecraft.client.Minecraft;
import net.mrbt0907.configex.ConfigManager;
import net.mrbt0907.configex.ConfigModEX;
import net.mrbt0907.configex.gui.GuiConfigEditor;

public class ClientHandler
{
	private static final Minecraft MC = Minecraft.getMinecraft();
	private static int permissionLevel;
	private static boolean inGame;
	
	public static void onTick()
	{
		if (MC.world == null && inGame)
		{
			ConfigModEX.debug("World was closed. Resetting client handler...");
			inGame = false;
			permissionLevel = ConfigManager.getPermissionLevel();
			ConfigManager.reset(true);
		}
		else if (MC.world != null && !inGame)
		{
			if (MC.player == null) return;
			
			ConfigModEX.debug("Detected new world. Preparing to client handler...");
			inGame = true;
			permissionLevel = ConfigManager.getPermissionLevel();
		}
		
		if (inGame)
			onGameTick();
	}
	
	public static void onGameTick()
	{
		if (MC.player == null) return;
		
		int permission;
		if (permissionLevel != (permission = MC.player.getPermissionLevel()))
		{
			permissionLevel = permission;
			ConfigManager.reset(false);
			if (MC.currentScreen instanceof GuiConfigEditor) 
				((GuiConfigEditor)MC.currentScreen).scrollPane.populateData();
		}
	}
	
	public static boolean inGame()
	{
		return inGame;
	}
}
