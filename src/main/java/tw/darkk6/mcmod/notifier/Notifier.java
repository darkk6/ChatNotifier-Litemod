package tw.darkk6.mcmod.notifier;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import tw.darkk6.litemod.chatnotifier.util.Lang;
import tw.darkk6.litemod.chatnotifier.util.Log;

public class Notifier{
	private static TrayIcon icon;
	private static SystemTray tray;
	private static int trayIconWidth=-1;
	private static boolean isInTray=false;
	private static boolean isSupport=SystemTray.isSupported();
	
	static{
		if(isSupport){
			try{
				Image img=getScaledImage(Notifier.class.getResource("/assets/chatnotifier/icon.png"));
				tray=SystemTray.getSystemTray();
				icon=new TrayIcon(img,"MC Notifier");
			}catch(Exception e){
				e.printStackTrace();
				Log.info(Lang.get("chatnotifier.msg.trayicon.fail"));
				isSupport=false;
			}
		}else{
			Log.info(Lang.get("chatnotifier.msg.trayicon.unsupport"));
		}
	}
	
	public static boolean isSupport(){ return isSupport;}

	public static boolean getShowState(){ return isInTray; }
	public static boolean showIcon(){
		if(!isSupport) return false;
		if(isInTray) return true;
		try{
			tray.add(icon);
			isInTray=true;
		}catch(Exception e){
			e.printStackTrace();
			Log.info(Lang.get("chatnotifier.msg.trayicon.showfail"));
			return false;
		}
		return true;
	}
	public static void hideIcon(){
		if(!isSupport) return;
		if(!isInTray) return;
		tray.remove(icon);
		isInTray=false;
	}
	
	public static void showMsg(RuleParser.Result result){
		if(result==null || !result.show) return;
		showMsg(result.title,result.msg,result.icon);
	}
	public static void showMsg(String str){
		showMsg("Minecraft",str,TrayIcon.MessageType.INFO);
	}
	public static void showMsg(String title, String str){
		showMsg(title,str,TrayIcon.MessageType.INFO);
	}
	public static void showMsg(String title, String str, TrayIcon.MessageType type){
		if( !isSupport || !isInTray ) return;
		icon.displayMessage(title,str,type);
	}
	
	
	private static Image getScaledImage(URL path){
		try{
			BufferedImage image = ImageIO.read(path);
			if(trayIconWidth==-1) trayIconWidth = new TrayIcon(image).getSize().width;
			return image.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH);
		}catch(Exception e){
			Log.info(Lang.get("chatnotifier.msg.trayicon.unscaled"));
			return Toolkit.getDefaultToolkit().getImage(path);
		}
	}
}