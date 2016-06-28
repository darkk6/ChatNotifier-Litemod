package tw.darkk6.mcmod.notifier;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import tw.darkk6.litemod.chatnotifier.util.Lang;
import tw.darkk6.litemod.chatnotifier.util.Log;

public class Notifier{
	private static TrayIcon icon;
	private static SystemTray tray;
	private static int trayIconWidth=-1;
	private static boolean isInTray=false;
	private static boolean isSupport=SystemTray.isSupported();
	private static NotifyThread thread;
	private static LinkedList<Message> msgPool;
	
	static{
		if(isSupport){
			try{
				msgPool=new LinkedList<Message>();
				thread=new NotifyThread();
				thread.start();
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
		showMsg(result.title,result.msg,result.wait,result.icon);
	}
	public static void showMsg(String str){
		showMsg("Minecraft",str,500L,TrayIcon.MessageType.INFO);
	}
	public static void showMsg(String title, String str){
		showMsg(title,str,500L,TrayIcon.MessageType.INFO);
	}
	public static void showMsg(String title, String str, long wait, TrayIcon.MessageType type){
		if( !isSupport || !isInTray ) return;
		Message m=new Message(title,str,wait,type);
		msgPool.add(m);
		thread.doNotify();		
		//icon.displayMessage();
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
	
	private static class Message{
		public String title,msg;
		public TrayIcon.MessageType type;
		public long wait=500L;//預設等待 500 ms
		public Message(String title,String msg,long wait,TrayIcon.MessageType type){
			this.title=title;
			this.msg=msg;
			this.wait=wait;
			this.type=type;
		}
	}
	
	private static class NotifyThread extends Thread{
		private boolean isDead = false;
		
		public NotifyThread(){
			super("NotifyThread");
			setDaemon(true);
		}
		
		@SuppressWarnings("unused")
		public boolean isDead(){ return isDead; }
		
		@SuppressWarnings("unused")
		public synchronized void stopThread(){ 
			isDead=true;
			this.notify();
		}
		
		public synchronized void doNotify(){
			this.notify();
		}
		
		public void run(){
			while(!isDead){
				//如果 queue 為空則等待
				if(msgPool.isEmpty()){
					synchronized(this){
						try {wait();} catch (InterruptedException e) {}
					}
				}
				//當有通知的時候繼續
				Message m=msgPool.poll();
				if( isSupport && isInTray && m!=null){
					icon.displayMessage(m.title, m.msg,m.type);
					if(m.wait>0L){
						try{Thread.sleep(m.wait);}catch(Exception e){}
					}
				}
			}
		}
	}
}