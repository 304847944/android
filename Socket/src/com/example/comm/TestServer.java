package com.example.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {
	private static final int SOCKET_PORT = 51000;

	private void initServer() {
		try {
			// 创建一个ServerSocket，用于监听客户端Socket的连接请求
			ServerSocket server = new ServerSocket(SOCKET_PORT);
			while (true) {
				Socket socket = server.accept();
				new Thread(new ServerThread(socket)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		TestServer server = new TestServer();
		server.initServer();
	}
	
	private class ServerThread implements Runnable {
		private Socket mSocket;
		private BufferedReader mReader;
		
		public ServerThread(Socket socket) throws IOException {
			mSocket = socket;
			mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
		}

		@Override
		public void run() {
			try {
				String content = null;
				// 循环不断地从Socket中读取客户端发送过来的数据
				while ((content = mReader.readLine()) != null) {
					System.out.println("收到客户端消息："+content);
					PrintStream ps = new PrintStream(mSocket.getOutputStream());
					ps.println("hi，很高兴认识你");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
