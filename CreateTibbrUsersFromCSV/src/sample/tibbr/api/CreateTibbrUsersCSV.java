/*
 * 
 *   Creates a user in tibbr (server specified at bottom) using local csv file
 *   User is created as deactivated - go in as admin to activate
 */

package sample.tibbr.api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVReader;



// FORMAT OF CSV is:
//			usrname  pwd  fname  lname  email
public class CreateTibbrUsersCSV {
	

	private String username;
	private String password;
	private String urlBase;
	private String auth_token=null;
	private String client_key=null;
	
	
public CreateTibbrUsersCSV(String _urlbase, String _username, String _password){
		
		this.urlBase=_urlbase;
		this.username=_username;
		this.password=_password;
		client_key=UUID.randomUUID().toString();
		
}

public static void main(String[] args) {
	
	CreateTibbrUsersCSV tibbr = new CreateTibbrUsersCSV("https://tigr.tibbr.com/","tibbradmin","Tibbr2013");

	try {
		tibbr.loginUser();
		tibbr.createUsersFromFile("sample_users.csv");
		
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

}

public void loginUser() throws UnsupportedEncodingException{
	
	DefaultHttpClient httpClient = new DefaultHttpClient();
	System.out.println("URL: "+urlBase+"a/users/login.xml");
	HttpPost postRequest = new HttpPost(urlBase+"/a/users/login.xml");
	StringEntity input = new StringEntity("params[login]=" + URLEncoder.encode(this.username,"UTF-8") + 
            							  "&params[password]=" + URLEncoder.encode(this.password,"UTF-8") + 
            							  "&client_key=" + URLEncoder.encode(client_key,"UTF-8"));

	
	input.setContentType("application/x-www-form-urlencoded");
	postRequest.setEntity(input);
	
	try {
		HttpResponse response=httpClient.execute(postRequest);
		String body = readStream(response.getEntity().getContent());
		Document user = parseXml(body);
        Node auth_token_node = firstElementByTag(user, "auth-token");
		this.auth_token=auth_token_node.getNodeValue();
		System.out.println("Auth Token: "+auth_token);
		
       
         
        
	} catch (ClientProtocolException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
}

public void createUsersFromFile(String csvFilename){
	
	
	URL server;
	try {
		
		CSVReader csvReader = new CSVReader(new FileReader(csvFilename));
		String[] row = null;
		
		while((row = csvReader.readNext()) != null) {		

			server = new URL(this.urlBase+"/a"+"/users.xml?params[activate_user]=true&params[dont_notify]=true");

			// &[dont_notify]=true
			
			HttpURLConnection conn= (HttpURLConnection)server.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("content-type", "text/xml");
			conn.setRequestProperty("auth_token", this.auth_token);
			conn.setRequestProperty("client_key", this.client_key);
	
			OutputStream out = conn.getOutputStream();
				
			String message="<user>"+
	            		"<login>"+row[0]+"</login>"+
	            		"<password>"+row[1]+"</password>"+
	            		"<password-confirmation>"+row[1]+"</password-confirmation>"+
	            		"<email>"+row[4]+"</email>"+
	            		"<first-name>"+row[2]+"</first-name>"+
	            		"<last-name>"+row[3]+"</last-name>"+
	            		"</user>";			
				
			System.out.println(message);
			System.out.println("============");
			
			out.write(message.getBytes());
			out.flush();  // actually send data
			
			out.close();
			
			System.out.println(conn.getResponseCode());   // 422 (user not created - already exists ? 
															// or 201  ??
		
		}
		csvReader.close();
		
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

private String readStream(InputStream is) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(is));

         StringBuffer sb = new StringBuffer();
         String output;
         while ((output = br.readLine()) != null) {
             sb.append(output);
         }   
         return sb.toString();
 }

private Document parseXml(String xml) {
    InputSource is = new InputSource(new StringReader(xml));
    DocumentBuilderFactory builderFactory =
            DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
            builder = builderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
            e.printStackTrace();  
    }

    try {
            return  builder.parse(is);
    } catch (SAXException e) {
            e.printStackTrace();
    } catch (IOException e) {
            e.printStackTrace();
    }
    return null;
}


private Node firstElementByTag(Document d, String tag) {
    NodeList list = d.getElementsByTagName(tag);
    if (list == null) {
            return null;
    }
    Element e = (Element)list.item(0);
    list = e.getChildNodes();
    if (list == null) {
            return null;
    }
    return list.item(0);
}






}
