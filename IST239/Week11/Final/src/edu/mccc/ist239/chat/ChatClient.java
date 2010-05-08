package edu.mccc.ist239.chat;
import java.awt.EventQueue;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.event.EventListenerList;

import com.cbthinkx.util.Debug;

/**
 * The headless client class
 *
 * Should have three threads by the time we're done: UI Thread, message thread, file thread?
 */
public class ChatClient {
    /** The local socket */
	private DatagramSocket socket;
    /** The server IP address */
	private InetAddress ipaddr;
    public ChatClient(String username, String password, String ip) {
        //Use a regular expression to break incoming message into NAME, MESSAGE
        //Note that "hi:" and "bye:" are commands for log on, log off
		try {
			//String hello = "hi:" + username;
			String hello = String.format(
                "hi:%s:%s", 
                username,
                password
            );

			this.ipaddr = InetAddress.getByName(ip);
			this.socket = new DatagramSocket();
			this.socket.send(
				new DatagramPacket(
					hello.getBytes(),
					hello.length(),
					ipaddr,
					5972
				)
			);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		new Thread() {
			public void run() {
				Debug.println("Thread:run()");
				try {
                    //Sit around forever, waiting for packets
					for (;;) {
						DatagramPacket dp = new DatagramPacket(new byte[256], 256);
						socket.receive(dp);
						String msg = new String(dp.getData()).trim();
						Debug.println(msg);
						try {
                            if (msg.startsWith("login")) {
                                String[] args = msg.split(":");
                                if (args[1].equals("true")) {
                                    fireLoginEvent(true);
                                } else {
                                    fireLoginEvent(false);
                                }
                            } else {
                                fireChatClientMessageReceived(msg);
                            }
						} catch (Exception ex) {
							System.err.println(ex.getMessage());
							System.err.println(msg);
						}
					}
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
				}
			}
		}.start();
    }

    public void login(String username, String md5Password) {
        //Send to server
        String msg = String.format(
            "hi:%s:%s",
            username,
            md5Password
        );
        sendMessage(msg);
        //Success?
    }

    public void logOut() {
        sendMessage("bye");
    }

    /**
     * Send a message to the central server
     */
    public void sendMessage(String message) {
        try {
        this.socket.send(
            new DatagramPacket(
                message.getBytes(),
                message.length(),
                ipaddr,
                5972
            )
        );
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    //Event Listener code copied from earlier MVC example
	private EventListenerList listenerList = new EventListenerList();
	//public void addChatClientListener(ChatClientListener l) {
	public void addChatClientListener(ChatClientListener l) {
		Debug.println("ChatClient.addChatClientListener()");
		listenerList.add(ChatClientListener.class, l);
	}
	public void removeChatClientListener(ChatClientListener l) {
		Debug.println("ChatClient.removeChatClientListener()");
		listenerList.remove(ChatClientListener.class, l);
	}
    
	public void addChatLoginListener(ChatLoginListener l) {
		Debug.println("ChatLogin.addChatLoginListener()");
		listenerList.add(ChatLoginListener.class, l);
	}
	public void removeChatLoginListener(ChatLoginListener l) {
		Debug.println("ChatLogin.removeChatLoginListener()");
		listenerList.remove(ChatLoginListener.class, l);
	}

	protected void fireChatClientMessageReceived(final String msg) {
		Debug.println("ChatClient.fireChatClientMessageReceived()");
		if (!EventQueue.isDispatchThread()) {
			try {
				EventQueue.invokeAndWait(
					new Runnable() {
						public void run() {
							notifyListeners(msg);
						}
					}
				);
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
		} else {
			notifyListeners(msg);
		}
	}

	protected void fireLoginEvent(final boolean success) {
		Debug.println("ChatClient.fireLoginEvent()");
		if (!EventQueue.isDispatchThread()) {
			try {
				EventQueue.invokeAndWait(
					new Runnable() {
						public void run() {
							notifyLogin(success);
						}
					}
				);
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
		} else {
			notifyLogin(success);
		}
	}

	private void notifyListeners(String msg) {
		Debug.println("ChatClient.notifyListeners()");
		ChatClientEvent e = new ChatClientEvent(this, msg);
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChatClientListener.class) {
                ((ChatClientListener) listeners[i + 1]).messageReceived(e);
			}
		}
	}

	private void notifyLogin(boolean success) {
		Debug.println("ChatClient.notifyLogin()");
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChatLoginListener.class) {
                ((ChatLoginListener) listeners[i + 1]).loginEvent(success);
			}
		}
	}
}
