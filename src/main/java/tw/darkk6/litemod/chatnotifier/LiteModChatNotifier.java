package tw.darkk6.litemod.chatnotifier;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.ITextComponent;

import org.lwjgl.input.Keyboard;

import tw.darkk6.litemod.chatnotifier.util.Lang;
import tw.darkk6.litemod.chatnotifier.util.Log;
import tw.darkk6.litemod.chatnotifier.util.Reference;
import tw.darkk6.mcmod.notifier.Notifier;
import tw.darkk6.mcmod.notifier.RuleParser;

import com.mumfrey.liteloader.ChatListener;
import com.mumfrey.liteloader.ShutdownListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;

public class LiteModChatNotifier implements ChatListener,Tickable,ShutdownListener {
	private static KeyBinding hotkey = new KeyBinding("key.chatnotifier.hotkey", Keyboard.KEY_F12, "key.chatnotifier.categories");
	private static File cfgFile=null;
	private static RuleParser parser;
	private static long lastCfgModify=0L;
	
	private static boolean isEnabled=false;
	
	@Override
	public void init(File configPath) {
		LiteLoader.getInput().registerKeyBinding(hotkey);
		cfgFile=new File(configPath,Reference.MOD_CONFIG);
		checkConfigFile();
	}
	
	@Override
	public void onTick(Minecraft mc, float partialTicks, boolean inGame, boolean clock) {
		if(hotkey.isPressed()){
			boolean isOpenFile = (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) );
			if(isOpenFile){
				try{Desktop.getDesktop().open(cfgFile);}
				catch(IOException e){
					Log.infoChat(Lang.get("chatnotifier.msg.unableopenfile"));
				}
			}else{
				isEnabled=!isEnabled;
				if(isEnabled){
					Log.infoChat(Lang.get("chatnotifier.msg.enabled"));
					checkConfigFile();
					long tmp=cfgFile.lastModified();
					if(parser==null || tmp!=lastCfgModify){
						parser=new RuleParser(cfgFile);
						Log.infoChat(Lang.get("chatnotifier.msg.configloaded"));
						lastCfgModify=tmp;
					}
					Notifier.showIcon();
				}else{
					Log.infoChat(Lang.get("chatnotifier.msg.disabled"));
					Notifier.hideIcon();
				}
			}
		}
		if(mc.currentScreen instanceof GuiMainMenu){
			Notifier.hideIcon();
			isEnabled=false;
		}
	}
	@Override
	public void onShutDown() {
		Notifier.hideIcon();
	}
	
	@Override
	public void onChat(ITextComponent chat, String msg) {
		if(!isEnabled || parser==null) return;
		if(msg==null) return;
		//把格式碼取代掉
		String str=msg.replaceAll("§[0-9a-fklmnor]","");
		Notifier.showMsg(parser.parse(str));
	}


	private void checkConfigFile(){
		if(!cfgFile.exists()){  
			try{
				//用這種方式來產生平台預設的 FileEncoding
				Scanner ipt=new Scanner(getClass().getResourceAsStream("/assets/chatnotifier/ChatNotifyRules.cfg"),"UTF-8");
				PrintStream opt=new PrintStream(cfgFile);
				while(ipt.hasNextLine()){
					opt.println(ipt.nextLine());
				}
				opt.close();
				ipt.close();
			}catch(Exception e){
				Log.info(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
/********* Mod 基本資訊 *********/
	@Override
	public String getVersion(){ return Reference.MOD_VER; }
	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {}
	@Override
	public String getName() { return Reference.MOD_NAME; }
}
