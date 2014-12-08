package hu.berzsenyi.exchange.net;

import hu.berzsenyi.exchange.net.cmd.ICmdHandler;
import hu.berzsenyi.exchange.net.cmd.TCPCommand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class TCPConnection {
	public Socket socket;
	public boolean open = true;
	public LinkedList<TCPCommand> commandList = new LinkedList<TCPCommand>();
	public DataInputStream din;
	public DataOutputStream dout;
	public ICmdHandler cmdHandler;
	
	public TCPConnection(ICmdHandler cmdHandler) {
		this.cmdHandler = cmdHandler;
	}
	
	public String getAddrString() {
		return this.socket.getInetAddress().toString()+":"+this.socket.getPort();
	}
	
	public void writeCommand(TCPCommand cmd) {
		try {
			this.dout.writeInt(cmd.id);
			this.dout.writeInt(cmd.length);
			cmd.write(this.dout);
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public int available() {
		try {
			synchronized (this.commandList) {
				return this.commandList.size();
			}
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
			return -1;
		}
	}
	
	public TCPCommand getCommand() {
		try {
			synchronized (this.commandList) {
				return this.commandList.poll();
			}
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
			return null;
		}
	}
	
	public void update() {
		try {
			while(0 < this.available())
				this.cmdHandler.handleCmd(this.getCommand(), this);
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public void close() {
		if(!this.open)
			return;
		this.open = false;
		try {
			if(this.din != null)
				this.din.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			if(this.dout != null)
				this.dout.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			if(this.socket != null)
				this.socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
