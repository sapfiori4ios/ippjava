package com.neoprojectdemo;

import java.net.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.PrinterURI;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import com.neoprojectdemo.BrlaBackendSystemClient;
import com.sap.cloud.account.TenantContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import de.lohndirekt.print.IppPrintService;
import de.lohndirekt.print.IppPrintServiceLookup;
import de.lohndirekt.print.attribute.auth.RequestingUserPassword;
import de.lohndirekt.print.attribute.ipp.printerdesc.PrinterDriverInstaller;
import de.lohndirekt.print.attribute.ipp.printerdesc.supported.PrinterUriSupported;

/**
 * Servlet implementation class Hello
 */
@WebServlet("/Hello")
public class Hello extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource
	private TenantContext tenantContext;
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Hello() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// response.getWriter().append("Served at:
		// ").append(request.getContextPath());
		PrintWriter out = response.getWriter();
		String destinationName = "BRLA_PRNT_01";
		String output = "";
		try {
			// Look up the connectivity configuration API
			Context ctx = new InitialContext();
			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx
					.lookup("java:comp/env/connectivityConfiguration");

			// Get destination configuration
			// String destinationName = "BRLA_PRNT_01";
			DestinationConfiguration destConfiguration = configuration.getConfiguration(destinationName);

			if (destConfiguration == null) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						String.format(
								"Destination %s is not found. Hint:" + " Make sure to have the destination configured.",
								destinationName));
				return;
			}

			String url = destConfiguration.getProperty("URL");
			String proxyType = destConfiguration.getProperty("ProxyType");

			// out.println("<p>Destination: " + url + "</p><p>Proxy type: " +
			// proxyType + "</p>");

			URL url1 = null;
			URI uri = null;
			// String output = null;

			try {

				// create a URL
				url1 = new URL("https://mfes300705.eu.merckgroup.com");

				// display the URL
				output += "<p>url of net url: " + url + "</p><p>Proxy type: " + proxyType + "</p>";

				// convert the URL to URI
				uri = url1.toURI();
				output += "<p>uri: " + uri + "</p><p>Proxy type: " + proxyType + "</p>";

				// String s=
				// getUrlContents("https://brlacmisaa4d355e1.hana.ondemand.com/brlacmis/DocumentManagement?action=getfile&docid=FI7WlZMwc64FpaiAC_D2lVFUjqRv-wJVJDL5L4VISUs",response);

				IppPrintService svc = new IppPrintService(uri);
				IppPrintServiceLookup ip = new IppPrintServiceLookup(uri, "", "");
				output += " Start Call";
				HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
				connection.setConnectTimeout(6 * 1000);
				connection.setRequestMethod("POST");
				connection.addRequestProperty("Content-type", "application/ipp");
				connection.setChunkedStreamingMode(0);
				// connection.setDoOutput(true);
				output += " Start Set POST";
				String json = "{\"operation-attributes-tag\" : \"{ \"requesting-user-name\": \"William\",\"job-name\": \"My Test Job\",\"document-format\": \"application/pdf\"}\" }";
				OutputStream os = connection.getOutputStream();
				os.write(json.getBytes("UTF-8"));
				os.close();
				output += " Set Close";
				BufferedReader br = null;
				output += "Reader :" + br;
				if (100 <= connection.getResponseCode() && connection.getResponseCode() <= 399) {
					output += "Connection Response Code Success:" + connection.getResponseCode();
					br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					output += "Reader :" + br;
					String strCurrentLine;
					while ((strCurrentLine = br.readLine()) != null) {
						output += "connection Success:" + strCurrentLine;
					}
				} else {
					output += "Connection Response Code Error:" + connection.getResponseCode();
					br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
					String strCurrentLine;
					output += "Reader :" + br;
					while ((strCurrentLine = br.readLine()) != null) {
						output += "connection Error:" + strCurrentLine;
					}
				}
				output += "End";
				//
				//
				// AttributeSet aset = new HashAttributeSet();
				// aset.add(new PrinterUriSupported(uri));

				// IppPrintServiceLookup svc= new
				// IppPrintServiceLookup(uri,"SAP-Test","34249");

				// out.println("Number of print services: " +
				// svc.getName().length());

				// Doc myDoc = new
				// SimpleDoc((getUrlContents("https://brlacmisaa4d355e1.hana.ondemand.com/brlacmis/DocumentManagement?action=getfile&docid=FI7WlZMwc64FpaiAC_D2lVFUjqRv-wJVJDL5L4VISUs",response)),
				// flavor, null);
				//// LOGGER.log(Level.SEVERE,"simple doc print"+myDoc);
				// DocPrintJob job = svc.createPrintJob();
				PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
				// aset.add(new JobName("TESTPRINT123", Locale.getDefault()));
				////// LOGGER.log(Level.SEVERE,"job success "+job);
				// job.print(myDoc, null);
				//
				// }

				// display the URI
				// out.println("<p>out put of the document content : " + output
				// + "</p><p>Proxy type: " + proxyType + "</p>");

			}
			// if any error occurs
			catch (Exception e) {

				// display the error
				System.out.println(e);
			}

		} catch (Exception e) {
			// Connectivity operation failed
			String errorMessage = "Connectivity operation failed with reason: " + e.getMessage();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
		}
		try (SCCHttpClient sccClient = createSCCHttpClient();
				BrlaBackendSystemClient client = new BrlaBackendSystemClient(sccClient, destinationName)) {
			output += "Scc CLient :" + sccClient;
			output += "Scc Content :" + request.getContentLength();
			client.post(readPayload(request));

		}
		response.setContentType("text/plain");
		out.print(output);
	}

	private String readPayload(HttpServletRequest request) throws IOException {
		StringBuilder payload = new StringBuilder(request.getContentLength());
		try (BufferedReader reader = new BufferedReader(request.getReader())) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				payload.append(line);
			}
		}
		return payload.toString();
	}

	public static String getUrlContents(String theUrl, HttpServletResponse response) {
		StringBuilder content = new StringBuilder();
		InputStream stream = null;
		byte[] buf = new byte[4096];
		int len = -1;

		// Use try and catch to avoid the exceptions
		try {
			OutputStream responseOutputStream = response.getOutputStream();
			response.setContentType("application/pdf");
			response.addHeader("Content-Disposition", "inline; filename=Documentation.pdf");
			URL url = new URL(theUrl); // creating a url object
			URLConnection urlConnection = url.openConnection(); // creating a
																// urlconnection
																// object

			// wrapping the urlconnection in a bufferedreader
			stream = new BufferedInputStream(url.openStream());
			String line;
			// reading from the urlconnection using the bufferedreader
			while ((len = stream.read(buf)) != -1) {

				responseOutputStream.write(buf, 0, len);

			}
			stream.close();
			responseOutputStream.flush();
			responseOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// doGet(request, response);
		PrintWriter out = response.getWriter();
		// String payloadRequest = getBody(request);

		String destinationName = "BRLA_PRNT_01";
		String output = "";
		// output += payloadRequest;
		SCCHttpClient sccClient = createSCCHttpClient();
		BrlaBackendSystemClient client = new BrlaBackendSystemClient(sccClient, destinationName);
		output += "Scc CLient :" + sccClient.toString();
		output += "Scc Content :" + request.getContentLength();
		StringBuilder payload = new StringBuilder(request.getContentLength());
		try (BufferedReader reader = new BufferedReader(request.getReader())) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				payload.append(line);
			}
		}
		LOGGER.log(Level.SEVERE, "payload" + payload.toString());
		output += payload.toString();
		client.post(payload.toString());
		client.close();
		response.setContentType("text/plain");
		out.print(output);
	}

	public static String getBody(HttpServletRequest request) throws IOException {

		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}

	@SuppressWarnings("deprecation")
	private SCCHttpClient createSCCHttpClient() {
		return new SCCHttpClient(HttpClientBuilder.create(), tenantContext.getAccountName());
	}

}
