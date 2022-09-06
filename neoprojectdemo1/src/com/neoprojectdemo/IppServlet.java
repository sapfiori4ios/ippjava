package com.neoprojectdemo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.print.CancelablePrintJob;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.attribute.standard.Sides;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import de.lohndirekt.print.IppPrintService;
import de.lohndirekt.print.IppPrintServiceLookup;
import de.lohndirekt.print.attribute.IppStatus;
import de.lohndirekt.print.attribute.auth.RequestingUserPassword;
import de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import de.lohndirekt.print.attribute.ipp.printerdesc.supported.PrinterUriSupported;

/**
 * Servlet implementation class IppServlet
 */
public class IppServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private Map attributes;
	private OperationsSupported operationId;
	private byte[] jobdata;

	private static String readStdIn() {
		StringBuilder sb = new StringBuilder();
		int ch;
		try {
			while ((ch = System.in.read()) != 10) {
				sb.append((char) ch);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		// get the PrintServices
		System.getProperties().setProperty(IppPrintServiceLookup.URI_KEY, Messages.getString("cups.uri"));
		System.getProperties().setProperty(IppPrintServiceLookup.USERNAME_KEY, Messages.getString("cups.username")); //$NON-NLS-1$
		System.getProperties().setProperty(IppPrintServiceLookup.PASSWORD_KEY, Messages.getString("cups.password")); //$NON-NLS-1$
		PrintService[] services = new IppPrintServiceLookup().getPrintServices();
		LOGGER.log(Level.SEVERE, "No. of Printers :" + services.length);
		PrintService service = null;
		System.out.println("Please choose a print service:");
		for (int i = 0; i < services.length; i++) {
			PrintService service2 = services[i];
			System.out.println("[" + i + "] : " + service2.getName());
		}
		// let the user choose a service
		while (true) {
			int serviceToUse = Integer.valueOf(readStdIn()).intValue();
			if (serviceToUse < 0 || serviceToUse >= services.length) {
				System.out.println("Bitte eine Zahl zwischen 0 und " + (services.length - 1) + " eingeben.");
			} else {
				service = services[serviceToUse];
				break;
			}
		}

		// ask for username
		System.out.print("Username : ");
		String username = readStdIn().trim();
		String password = null;
		if (username != null && username.trim().length() > 0) {
			System.out.print("Password : ");
			password = readStdIn().trim();
		}

		System.out.println("Printing on: " + service.getName());
		// create a job
		CancelablePrintJob job = (CancelablePrintJob) service.createPrintJob();

		// set the job attributes
		PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

		if (username != null && username.trim().length() > 0) {
			RequestingUserName usernameAttr = new RequestingUserName(username, Locale.GERMANY);
			RequestingUserPassword userpassAttr = new RequestingUserPassword(password, Locale.GERMANY);
			attributes.add(usernameAttr);
			attributes.add(userpassAttr);
		}
		// we just want one copy
		Copies copies = new Copies(1);
		attributes.add(copies);
		// we want duplex printing
		Sides sides = Sides.DUPLEX;
		attributes.add(sides);
		// print it on the main media tray
		// Media media = MediaTray.MAIN;
		// If you have special Mediatrays (like most printers)
		// you can use the class LdMediaTray and give a String to the
		// constructor like
		// new LdMediaTray("Optional2");
		// attributes.add(media);

		// Now create a document
		File testFile = new File("C:\\Users\\kkc\\Downloads\\Samples\\Test.pdf");
		InputStream stream = new FileInputStream(testFile);
		Doc doc = new SimpleDoc(stream, DocFlavor.INPUT_STREAM.PDF, new HashDocAttributeSet());

		// finally the print action
		try {
			// we are setting the doc and the job attributes
			job.print(doc, attributes);
			System.out.println("printing successfull...");
		} catch (PrintException e) {
			e.printStackTrace();
		}
	//doPost(request, response);
}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		try {
			// Look up the connectivity configuration API
			Context ctx = new InitialContext();
			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx
					.lookup("java:comp/env/connectivityConfiguration");

			// Get destination configuration
			String destinationName = "BRLA_PRNT_01";
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
			URL url2 = null;
			String output = null;

			// try {
			//
			// // create a URL
			// url1 = new URL(url);
			//
			// // display the URL
			out.println("<p>url of net url: " + url + "</p><p>Proxy type: " + proxyType + "</p>");
			//
			// // convert the URL to URI
			// uri = url1.toURI();
			// out.println("<p>uri: " + uri + "</p><p>Proxy type: " + proxyType
			// + "</p>");
			//
			// // String s=
			// getUrlContents("https://brlacmisaa4d355e1.hana.ondemand.com/brlacmis/DocumentManagement?action=getfile&docid=FI7WlZMwc64FpaiAC_D2lVFUjqRv-wJVJDL5L4VISUs",response);
			//// url2 = new
			// URL("ipps://mfes300705.eu.merckgroup.com/ipp/print");
			//// IppPrintService svc = new IppPrintService(url2.toURI());

			System.out.println("");
			System.out.println("H E A D E R S");
			System.out.println("");
			Enumeration headerNames = request.getHeaderNames();
			// LOGGER.log(Level.SEVERE,"Headers:" + headerNames);
			while (headerNames.hasMoreElements()) {
				String headerName = (String) headerNames.nextElement();
				System.out.println(headerName + ":" + request.getHeader(headerName));
				// LOGGER.log(Level.SEVERE,"Header 1"+headerName + ":" +
				// request.getHeader(headerName));
			}
			System.out.println("");
			System.out.println("B O D Y");
			System.out.println("");
			ServletInputStream inputStream = request.getInputStream();
			parseRequest(inputStream);
			System.out.println("Operazione richiesta : 0x" + Integer.toHexString(this.operationId.getValue()));
			LOGGER.log(Level.SEVERE, "WEB STATUS CUPS:" + OperationsSupported.CUPS_GET_PRINTERS);
			// if (this.operationId.getValue() ==
			// OperationsSupported.CUPS_GET_PRINTERS
			// .getValue()) {
			IppResponseIppImpl ippresponse = new IppResponseIppImpl(IppStatus.SUCCESSFUL_OK);
			try {

				ippresponse.getPrinterAttributes()
						.add(new PrinterUriSupported(new URI("https://mfes300705.eu.merckgroup.com/ipp/IppServlet")));
				LOGGER.log(Level.SEVERE, "WEB Output Stream Test:" + response.getOutputStream());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			ippresponse.write(response.getOutputStream());

			// }
			LOGGER.log(Level.SEVERE, "WEB STATUS CODE:" + OperationsSupported.PRINT_JOB);
			LOGGER.log(Level.SEVERE, "Operator:" + this.operationId.getValue());
			// if (this.operationId.getValue() ==
			// OperationsSupported.PRINT_JOB.getValue()) {

			IppResponseIppImpl ippresponse1 = new IppResponseIppImpl(IppStatus.SUCCESSFUL_OK);
			ippresponse1.write(response.getOutputStream());
			java.io.File tempFile = java.io.File.createTempFile("unijob", "tmp");
			FileOutputStream faos = new FileOutputStream(tempFile);
			faos.write(this.jobdata);
			faos.close();
			System.out
					.println("Scritti : [" + this.jobdata.length + "] bytes in : [" + tempFile.getAbsolutePath() + "]");
			// }

			response.getOutputStream().close();
		}

		catch (Exception e) {

			// display the error
			System.out.println(e);
		}
	}

	private void parseRequest(InputStream request) throws IOException {
		byte[] header = new byte[4096];
		request.read(header);
		LOGGER.log(Level.SEVERE, "Print Header :" + header[0]);
		this.operationId = new OperationsSupported(header[2]);
		if (request.available() != 0) {
			LOGGER.log(Level.SEVERE, "Print Request");
			this.attributes = AttributeParser.parseRequest(request);
		} else {
			this.attributes = new HashMap();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (request.available() != 0) {
			baos.write(request.read());
		}
		baos.close();
		this.jobdata = baos.toByteArray();
		request.close();
	}

}
