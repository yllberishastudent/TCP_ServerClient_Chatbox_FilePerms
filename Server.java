package sockets.programming;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
	
	private ArrayList<ConnectionHandler> connections;
	private ServerSocket server;
	private boolean done;
	private ExecutorService pool;
	
	
	public Server() {
		connections = new ArrayList<>();
		done = false;
	}
	
	@Override
	public void run()
	{
		try {
			server = new ServerSocket(9999);
			pool = Executors.newCachedThreadPool();
			while (!done) {
				Socket client = server.accept();
				ConnectionHandler handler = new ConnectionHandler(client);
				connections.add(handler);
				pool.execute(handler);
			}
		} catch (Exception e) {
			shutdown();
		}
	}
	

	public void broadcast(String message) {
		for(ConnectionHandler ch : connections) {
			if(ch!=null) {
				ch.sendMessage(message);
			}
		}
	}
	
	public void shutdown() {
		try {
			done=true;
			if(!server.isClosed()) {
				server.close();
				
			}
			for(ConnectionHandler ch : connections) {
				ch.shutdown();
			}
		}
		catch(Exception e) {}
		}
	
	
	public ConnectionHandler(Socket client) {
		this.client = client;
	}	
	
	@Override
	public void run() {
		
		
		try {
		out = new PrintWriter(client.getOutputStream(),true);
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		
		out.println("Please enter your username: ");
		nickname = in.readLine();
		
		 
		System.out.println(nickname + " with ip: " + client.getRemoteSocketAddress().toString()  + " connected!");
	
		String ipOfkosovo = null;
		if(nickname.equals("kosovo")) {
			 ipOfkosovo = client.getRemoteSocketAddress().toString();
		}
		
		broadcast(nickname + " Connected to the server!");
		

		
		String message;
	