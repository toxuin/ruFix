package me.repeat.ruFix;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ruFixPlayerListener implements Listener{

	public static ruFix plugin; public ruFixPlayerListener(ruFix instance) {
        plugin = instance;
    }
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		if (event.isCancelled()) return;
//		String fix = fixFromGame(event.getMessage());
		String fix = ruFix.fixUseTable(event.getMessage());
		if (!event.getMessage().equals(fix)) {
			if (ruFix.ruFixDebug)
	        	System.out.print("[ruFixDebug]:" + event.getMessage() + ":");
			event.setMessage("/");
			event.setCancelled(true);
			event.getPlayer().chat(fix);
		}
} 
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event){
		if (ruFix.ruFixDebug)
        	System.out.print("[ruFixDebug]:" + event.getMessage() + ":");
		event.setMessage(ruFix.fixUseTable(event.getMessage()));
//		event.setMessage(fixFromGame(event.getMessage()));
	}

/*
	public String newFix(String msg){
    	try {
			return new String(msg.getBytes("ISO-8859-1"),"CP1251");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msg;
	}
*/

/*	
	public String fixFromGame(String original_message){
    	byte[] message = original_message.getBytes();
		String text = "";
		for (int n = 0; n < message.length; n++) {

            int c = (int)message[n] & 0xFF;
            int c1 = 0;
            if (c >= 128 && (int)(n+1) <= (int)(message.length-1)) {
            	c1 = (int)message[n+1] & 0xFF;
            	if (c == 195 && c1 >= 128 && c1 <= 191) { //195 -  ������ ���� ���������
            		c = c1+1040-128; //1040-128 // 1040 - ������ �������� ������, 128 - �
            		text += (char)(c); 
            		n++;
            	} else if (c == 194 && c1 == 184 ) { //194 184 fix '�'
            		text += (char)((int)1105);
            		n++;
                } else {
					text += new String(new byte[] {message[n], message[n+1]});
					n++;
            	}
            } else {
            	text += (char)c;
            }
        }
		return text;
	}
*/
}
