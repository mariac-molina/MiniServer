package Server;

import java.net.*;
import java.io.*;
import java.util.*;

class Connection implements Runnable {

	private Thread thread;
	private Socket socket;
	private String uagent;
	private boolean requeteEstUnPost = false;
	private boolean isSafari = false;
	private int bodyLength;
	private String code = "";
	private String testHTML = "/Users/professionnal/Documents/eclipse_workspace/Mini_Server/test.html";
	private boolean fileExists = false;

	public Connection(Socket socket) {
		this.socket = socket;
		thread = new Thread(this);
		thread.start();
	}

	// rspCode 200 OK
	void sendResponseHeader(PrintStream ps, int len, String rspCode) {
		ps.println("HTTP/1.0 " + rspCode);
		ps.println("Date: " + new Date());
		ps.println("Server: nfa016Server/1.0");
		ps.println("Content-type: text/html");
		ps.println("Content-length: " + len);
		ps.println("Set-Cookie: name=1ag837; expires=Wed, 21 Oct 2072 07:28:00 GMT");
		ps.println("");

	}

	String litHeaderDeLaRequete() {
		String header = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = br.readLine();
			header = line;
			StringTokenizer st = new StringTokenizer(line);

			requeteEstUnPost = st.nextToken().equals("POST");
			
			File f = new File(testHTML);
			if (f.exists()) {
				fileExists = true;
				code = "200 OK";
				System.out.println("File has been found");
			}
			else {
				code = "404 Not found";
				System.out.println("file not found");
			}

			System.out.println(line);

			while (!line.equals("")) {
				line = br.readLine();
				header = header + '\n' + line;
				System.out.println(line);
				st = new StringTokenizer(line);
				String ung;
				while (st.hasMoreTokens()) {
					ung = st.nextToken();
					System.out.print(ung + '#');
					if (ung.equals("Content-Length:")) {
						requeteEstUnPost = true;
						System.out.println("on est dedans");
						bodyLength = Integer.parseInt(st.nextToken());
					}

					if (ung.equals("User-Agent:")) {

						isSafari = line.contains("Safari");
						// System.out.println("here" + isSafari);
					}
				}
			}
			// If browser is Chrome OK, anything else won't work
			if (!isChrome) {
				code = "403";
			} else {
				code = "200";
			}
		} catch (IOException e) {
			System.err.println("I/O error " + e);
		}
		return header;
	}

	String litBodyDeLaRequete() {
		// Dans le cas où la requête est un post, il faut lire la suite
		// du header pour avoir par ex les paramètres "postés" par un
		// formulaire !
		byte[] line = new byte[bodyLength];
		try {
			for (int i = 0; i < bodyLength; i++) {
				line[i] = (byte) socket.getInputStream().read();
			}
		} catch (IOException e) {
			System.err.println("I/O error " + e);
		}
		return new String(line);
	}
	
	public String getPageContent() throws IOException {
	/*	File f = new File(testHTML);
		if (f.exists() && !f.isDirectory()) {
			fileExists = true;
			code = "200 OK";
			System.out.println("Fila has been found");
		}
		else {
			System.out.println("file not found");
		}*/
		
		StringBuilder contentBuilder = new StringBuilder();
		BufferedReader bf = new BufferedReader(new FileReader(testHTML));
		String str;
		while ((str = bf.readLine()) != null)
			contentBuilder.append(str);

		bf.close();
		
		String content = contentBuilder.toString();
		
		return content;
	}

	public void run() {
		try {
			System.out.println("connection reçue du client");
			// out est le flux sur lequel on va écrire : un socket vers le client !
			PrintStream out = new PrintStream(socket.getOutputStream());
			
			
			// Préparation de la réponse
			
			String reponse = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"fr\">\n<head>\n<title>Requete</title>\n</head>\n<body><p><pre>\n ";
			
			reponse = reponse + litHeaderDeLaRequete() + getPageContent();
			
			//String error = reponse + getPageContent(errorHTML);
			
			if (requeteEstUnPost) {
				reponse = reponse + '\n' + litBodyDeLaRequete();
			}
			reponse = reponse + "\n</pre>\n</p>\n</body>\n</html>\n";
			
		

				
			//reponse = reponse + "\n</pre>\n</p>\n</body>\n</html>\n";

			// L'en-tête de la réponse part sur le socket, direction le client !

			sendResponseHeader(out, reponse.length(), code);

			// et le body !
			//website sent to favourite browser only
			if (isChrome){
				out.print(reponse);
			}
			//If not chrome, website is not sent out
			else{
				System.out.println("Website only works with our favourite browser : Chrome ");
			}
			//System.out.println(fileExists);
			//System.out.println("reponse = " + reponse);

			socket.close();
		} catch (IOException e) {
			System.err.println("I/O error " + e);
		}
	}
}
