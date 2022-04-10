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
	
	
	class ConnectionHandler implements Runnable {
		
		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		private String nickname;
		
		
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
			
			
				while((message = in.readLine()) != null) {
					
				System.out.println("Client " + nickname + ": " + message);
				if(message.startsWith("/nick ")) {
				   String[] messageSplit = message.split(" ",2);
				   System.out.println(nickname + message);
				   if(messageSplit.length == 2) {
					   broadcast(nickname + " renamed themselves to "+ messageSplit[1]);
					   System.out.println(nickname + "renamed themselves to" + messageSplit[1]);
					   nickname = messageSplit[1];
					   //hi i know its a mess.
					   out.println("Nickname Changed to " + nickname);
				   }else {
					   out.println("No nickname provided!");
				   }
				}
				else if(message.equals("/quit")) {
					broadcast(nickname + " left!");
				}
				else if(message.startsWith("/cmd read")){
					String[] rarr = message.split(" ",5);
					
				       String[] rcmd = {"",""};
				       rcmd[0] = rarr[1];
				       rcmd[1]= rarr[2];
					OutputStream ros = client.getOutputStream();
			        OutputStreamWriter rosw = new OutputStreamWriter(ros);
			        BufferedWriter rbw = new BufferedWriter(rosw);
			      
			        try {
			            File file = new File("C:\\\\Users\\\\madY\\\\eclipse-workspace\\\\Socket_Programming\\\\src\\\\sockets\\\\programming\\\\"+rcmd[1]);
			            file.setReadable(true);
			            Scanner myReader = new Scanner(file);
			            while (myReader.hasNextLine()) {
			              String data = myReader.nextLine();
			              System.out.println(data);
			              
			              rbw.write(data);
					      rbw.flush();
					        
			            }
			            rbw.write("\n");
					      rbw.flush();
			            myReader.close();
			          } catch (FileNotFoundException e) {
			            System.out.println("An error occurred.");
			          }		
					
				}

				else if(message.startsWith("/cmd showPerms")) {
					OutputStream pos = client.getOutputStream();
			        OutputStreamWriter posw = new OutputStreamWriter(pos);
			        BufferedWriter pibw = new BufferedWriter(posw);
			        
					if(nickname.equals("kosovo") && ipOfkosovo.equals(client.getRemoteSocketAddress().toString())){
						 pibw.write("Your permissions : \n");
						 pibw.write("> Get file info    (  /cmd readFile fileName   )\n");
					     pibw.write("> Read file   	   (  /cmd readFile fileName   )\n");
					     pibw.write("> Write file       (  /cmd writeFile fileName  )\n");
					     pibw.write("> Create file      (  /cmd createFile fileName )\n");
					     pibw.write("> Delete file      (  /cmd deleteFile fileName )\n");
					     pibw.flush();
					}
					else {
						pibw.write("Your permissions : \n");
						pibw.write("> Read file   	    (  /cmd readFile fileName   )\n");
						pibw.flush();
					}
					
				}
				
				else if(message.startsWith("/cmd getFileInfo")) {
					
					String[] garr = message.split(" ",5);
					
				       String[] gcmd = {"",""};
				       gcmd[0] = garr[1];
				       gcmd[1]= garr[2];
					OutputStream ios = client.getOutputStream();
			        OutputStreamWriter iosw = new OutputStreamWriter(ios);
			        BufferedWriter ibw = new BufferedWriter(iosw);
					if(nickname.equals("kosovo") && ipOfkosovo.equals(client.getRemoteSocketAddress().toString())){
						
						
						    File myObj = new File("C:\\\\\\\\Users\\\\\\\\madY\\\\\\\\eclipse-workspace\\\\\\\\Socket_Programming\\\\\\\\src\\\\\\\\sockets\\\\\\\\programming\\\\\\\\"+ gcmd[1]);
						    if (myObj.exists()) {
						     
						      ibw.write("File name: " + myObj.getName() +"\n");
						      ibw.write("Absolute path: " + myObj.getAbsolutePath()+"\n");
						      ibw.write("Writeable: " + myObj.canWrite() + "\n" );
						      ibw.write("Readable " + myObj.canRead() + "\n");
						      ibw.write("File size in bytes " + myObj.length()+"\n");
							     
						      ibw.flush();
						    
						    } else {
						      System.out.println("The file does not exist.");
						    }
						  }
					else {	
					ibw.write("You dont have permission! \n");	
					ibw.flush();
					System.out.println("An error occurred.");
				}
			}
				else if(message.startsWith("/cmd writeFile")) {
					
					String[] warr = message.split(" ",5);
					
				       String[] wcmd = {"",""};
				       wcmd[0] = warr[1];
				       wcmd[1]= warr[2];
				     
				       String[] writeMessage = message.split("'",2);
				       
				
					OutputStream wos = client.getOutputStream();
			        OutputStreamWriter wosw = new OutputStreamWriter(wos);
			        BufferedWriter wbw = new BufferedWriter(wosw);
					if(nickname.equals("kosovo") && ipOfkosovo.equals(client.getRemoteSocketAddress().toString())){
						 try {
						      FileWriter myWriter = new FileWriter("C:\\\\\\\\Users\\\\\\\\madY\\\\\\\\eclipse-workspace\\\\\\\\Socket_Programming\\\\\\\\src\\\\\\\\sockets\\\\\\\\programming\\\\\\\\"+ wcmd[1]);
						      myWriter.write(writeMessage[1]);
						      myWriter.close();
						      wbw.write("Successfully wrote to the file. \n");
						      wbw.flush();
						      
						    } catch (IOException e) {
						      System.out.println("An error occurred.");
						      wbw.write("An error occured. \n");
						      wbw.flush();
						    }
							
					}else {
						wbw.write("You dont have permission! \n");
						wbw.flush();
					}
					
				}
				else if(message.startsWith("/cmd createFile")) {
					
					String[] carr = message.split(" ",5);
					
				       String[] ccmd = {"",""};
				       ccmd[0] = carr[1];
				       ccmd[1]= carr[2];
					
					OutputStream cos = client.getOutputStream();
			        OutputStreamWriter cosw = new OutputStreamWriter(cos);
			        BufferedWriter cbw = new BufferedWriter(cosw);
			        if(nickname.equals("kosovo") && ipOfkosovo.equals(client.getRemoteSocketAddress().toString())){
			        try {
			            File myObj = new File("C:\\\\\\\\Users\\\\\\\\madY\\\\\\\\eclipse-workspace\\\\\\\\Socket_Programming\\\\\\\\src\\\\\\\\sockets\\\\\\\\programming\\\\\\\\"+ccmd[1]);
			            if (myObj.createNewFile()) {
			              System.out.println("File created: " + myObj.getName());
			              cbw.write("File created: " + myObj.getName() +"\n");
			              cbw.flush();
			            } else {
			              System.out.println("File already exists.");
			              cbw.write("File already exists. \n");
			              cbw.flush();
			            }
			          } catch (IOException e) {
			            System.out.println("An error occurred.");
			          }
			        }else {
			        	cbw.write("You dont have permission \n");
			        	cbw.flush();
			        	}
					
				}
				else if(message.startsWith("/cmd deleteFile")) {
					String[] darr = message.split(" ",5);
					
				       String[] dcmd = {"",""};
				       dcmd[0] = darr[1];
				       dcmd[1]= darr[2];
					
					OutputStream dos = client.getOutputStream();
			        OutputStreamWriter dosw = new OutputStreamWriter(dos);
			        BufferedWriter dbw = new BufferedWriter(dosw);
				
					 if(nickname.equals("kosovo") && ipOfkosovo.equals(client.getRemoteSocketAddress().toString())){
						 File myObj = new File("C:\\\\\\\\\\\\\\\\Users\\\\\\\\\\\\\\\\madY\\\\\\\\\\\\\\\\eclipse-workspace\\\\\\\\\\\\\\\\Socket_Programming\\\\\\\\\\\\\\\\src\\\\\\\\\\\\\\\\sockets\\\\\\\\\\\\\\\\programming\\\\\\\\\\\\\\\\"+dcmd[1]); 
						    if (myObj.delete()) { 
						    	dbw.write("Deleted the file:" + myObj.getName() +"\n");
						    	dbw.flush();
						      System.out.println("Deleted the file: " + myObj.getName());
						    } else {
						      System.out.println("Failed to delete the file.");
						      dbw.write("Failed to delete the file. \n");
						      dbw.flush();
						    } 
					 }
					 else {
					dbw.write("You dont have permission! \n");
					dbw.flush();
				}
					 }
				
				else if(message.startsWith("/cmd ping ") ) {
					
					String[] arr = message.split(" ",5);
					
				       String[] cmd = {"",""};
				       cmd[0] = arr[1];
				       cmd[1]= arr[2];
					ProcessBuilder prBuilder = new ProcessBuilder(cmd);
					prBuilder.directory(new File(System.getProperty("user.home")));
					
					try {
						Process process = prBuilder.start();
						BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
						
						String line;
					
					while ((line=reader.readLine()) != null) {
						System.out.println(line);
					}
					System.out.println("Exited with error code");
					}
					catch (Exception e) {
						System.out.println("Error bro!");
					}
					
					
				}
				else {
					broadcast("\n" + nickname + ": " + message +"\n");
				}
			}
			
			}
			catch (IOException e) {
				//TODO handle
			}
		}		
			
		    
		public void sendMessage(String msg) {
			out.println(msg);
		}
		
		
	    public void shutdown() {
	
		try {
		in.close();
		out.close();
		if(!server.isClosed()) 
			 server.close();
		}
		catch(Exception e) {}
	  }
	    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		System.out.println("------------Server Started------------");
		Server s = new Server();
		s.run();		
		}
		
	}
	
	
