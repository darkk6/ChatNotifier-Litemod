package tw.darkk6.mcmod.notifier;

import java.awt.TrayIcon;
import java.io.File;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import tw.darkk6.litemod.chatnotifier.util.Lang;
import tw.darkk6.litemod.chatnotifier.util.Log;

public class RuleParser{
	
	public Vector<Rule> ruleList;
	
	public RuleParser(File cfgFile){
		ruleList=new Vector<Rule>();
		init(cfgFile);
	}
	
	public Result parse(String input){
		for(Rule rule:ruleList){
			Result res=rule.parse(input);
			if(res!=null) return res;
		}
		return null;
	}
	
	private void init(File cfgFile){
		Scanner ipt=null;
		try{
			ipt=new Scanner(cfgFile);
			Rule tmpRule=null;
			while(ipt.hasNextLine()){
				if(tmpRule==null) tmpRule=new Rule(null,"Minecraft","$0",true,"INFO");
				String str=ipt.nextLine(),tmp=null;
				
				if(str.startsWith("#")){
					continue;//註解，跳過此行
				}else if(str.startsWith("---")){
					//一筆資料到底了，配置好並且放進 Rule list
					if(tmpRule.getPattern()==null){
						//沒指定 regexp , 忽略這筆資料
						Log.info(Lang.get("chatnotifier.msg.noregexp"));
					}else{
						ruleList.add(tmpRule);
					}
					tmpRule=null;
					continue;
				}else if(str.startsWith("regexp:")){
					tmp=str.replaceAll("regexp:(.*)","$1");
					try{
						if(!"*".equals(tmp)) Pattern.compile(tmp);//檢查語法是否正確
						tmpRule.setPattern(tmp);
					}catch(PatternSyntaxException e){
						Log.info(Lang.get("chatnotifier.msg.invalid.regexp")+tmp);
					}
				}else if(str.startsWith("title:")){
					tmp=str.replaceAll("title:(.*)","$1");
					tmpRule.setTitle(tmp);
				}else if(str.startsWith("msg:")){
					tmp=str.replaceAll("msg:(.*)","$1");
					tmpRule.setMsg(tmp);
				}else if(str.startsWith("show:")){
					tmp=str.replaceAll("show:(.*)","$1");
					tmpRule.setIsShow("true".equalsIgnoreCase(tmp));
				}else if(str.startsWith("icon:")){
					tmp=str.replaceAll("icon:(.*)","$1");
					tmpRule.setIconType(tmp);
				}
			}
		}catch(Exception e){
			Log.info(Lang.get("chatnotifier.msg.ruleloaderror"));
			e.printStackTrace();
		}
		if(ipt!=null) ipt.close();
		Log.info(Lang.get("chatnotifier.msg.rule.done"));
	}
	
	
	public static class Rule{
		private static final String IS_REGEXP_PTN=".*?(?<!\\\\)\\$[0-9].*";
		private String ptn;
		private String title;
		private String msg;
		private int iconType=1;
		private boolean isShowAll=false;
		private boolean isShow=true;
		
		public Rule(String ptn,String title,String msg,boolean show,String icon){
			if("*".equals(ptn)){
				//代表要顯示所有訊息
				isShowAll=true;
			}else{
				isShowAll=false;
				this.ptn=checkPattern(ptn);
				this.title=title;
				this.msg=msg;
				this.isShow=show;
				setIconType(icon);
			}
		}
		
		public String getPattern(){ return this.ptn; }
		public void setPattern(String ptn){
			if("*".equals(ptn)){
				isShowAll=true;
				this.ptn="*";
			}else{
				isShowAll=false;
				this.ptn=checkPattern(ptn);
			}
		}
		public void setTitle(String title){ this.title=title; }
		public void setMsg(String msg){ this.msg=msg; }
		public void setIsShow(boolean isShow){ this.isShow=isShow; }
		public void setIconType(String icon){
			if("NONE".equalsIgnoreCase(icon)) iconType=0;
			else if("WARNING".equalsIgnoreCase(icon)) iconType=2;
			else if("ERROR".equalsIgnoreCase(icon)) iconType=3;
			else iconType=1;
		}
		
		public Result parse(String input){
			String resTitle="Minecraft",resMsg=input;
			if(input==null) return null;
			if(isShowAll) return new Result(resTitle,resMsg,iconType,true);
			
			if(title==null) title="Minecraft";
			if(msg==null) msg="$0";
			
			//不是 showAll , 開始檢查 pattern
			if(input.matches(ptn)){
				try{
					if(isShow){
						if(!title.matches(IS_REGEXP_PTN)){//title 的部分不需要 regexp replace
							resTitle=title;
						}else{
							resTitle=input.replaceAll(ptn,title);
						}
						if(!msg.matches(IS_REGEXP_PTN)){//msg 的部分不需要 regexp replace
							resMsg=msg;
						}else{
							resMsg=input.replaceAll(ptn,msg);
						}
					}else{
						//沒有要顯示，隨便給就好
						resTitle="";resMsg="";
					}
					return new Result(resTitle,resMsg,iconType,isShow);
				}catch(Exception e){
					Log.info(e.getMessage());
					return null;
				}
			}else
				return null;
		}
		
		private String checkPattern(String ptn){
			if(ptn==null) return null;
			StringBuilder res=new StringBuilder();
			if( (!ptn.startsWith(".*")) && (!ptn.startsWith("^")) )
				res.append(".*?");
			
			res.append(ptn);
			
			if( (!ptn.endsWith(".*")) && (!ptn.endsWith("$")) )
				res.append(".*");
			
			return res.toString();
		}
	}
	
	public static class Result{
		public String title;
		public String msg;
		public boolean show;
		public TrayIcon.MessageType icon;
		
		public Result(String title,String msg,int icon){
			this(title,msg,icon,true);
		}
		public Result(String title,String msg,int icon,boolean show){
			this.title=title;
			this.msg=msg;
			this.show=show;
			switch(icon){
				case 0: this.icon=TrayIcon.MessageType.NONE;break;
				case 2: this.icon=TrayIcon.MessageType.WARNING;break;
				case 3: this.icon=TrayIcon.MessageType.ERROR;break;
				default: this.icon=TrayIcon.MessageType.INFO;
			}
		}
	}
}