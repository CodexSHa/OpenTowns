package xaos.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UtilsServer {

    public static String getServerName(String serverURL) {
        String sXML;

        try {
            sXML = getUrlSource(serverURL);
            if (sXML == null || sXML.trim().length() == 0) {
                Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.0"), "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }

            Document doc = UtilsXML.loadXMLFileFromString(sXML);
            if (doc == null || doc.getDocumentElement() == null || doc.getDocumentElement().getChildNodes() == null) {
                Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.2"), "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }

            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;

            String sServerName = null;
            String sAux;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // Obtenemos el nameID, width i height
                    sAux = node.getNodeName();

                    if (sAux.equalsIgnoreCase("SERVER")) { //$NON-NLS-1$
                        // Server Tag
                        NodeList nodeListServer = node.getChildNodes();
                        sServerName = UtilsXML.getChildValue(nodeListServer, "name"); //$NON-NLS-1$
                    }
                }
            }

            if (sServerName != null && sServerName.trim().length() > 0) {
                return sServerName;
            }
        } catch (Exception e) {
            // log the error
            Log.log(Log.LEVEL_DEBUG, Messages.getString("UtilsServer.21") + " [" + e.toString() + "]", "UtilsServer"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        return Messages.getString("UtilsServer.1") + " [" + serverURL + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static String getBuriedTown(String serverURL, String buryFolder) {
        // townsmods.net is offline; bury maps are managed locally.
        return null;
    }

    private static String getUrlSource(String url) throws Exception {
        StringBuilder a = new StringBuilder();
        if (!url.startsWith("http://")) { //$NON-NLS-1$
            url = "http://" + url; //$NON-NLS-1$
        }

        URL urlObject = URI.create(url).toURL();
        URLConnection uc = urlObject.openConnection();

        try {
            // Bound BOTH phases. The connect timeout is the important one: with
            // it unset, getInputStream() below blocks on the OS default (about
            // 21s of TCP SYN retries on Windows) whenever the host is dead or
            // unreachable, which is exactly the case for the defunct
            // townsmods.net. The read timeout was already set but never helped,
            // since the stall was in connect, not read.
            uc.setConnectTimeout (400);
            uc.setReadTimeout (400);
        }
        catch (Exception e) {
        }

        InputStream iStream = uc.getInputStream();

        try {
        	uc.setReadTimeout (0);
        }
        catch (Exception e) {
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(iStream, "UTF-8")); //$NON-NLS-1$
        String inputLine;

        while ((inputLine = br.readLine()) != null) {
            a.append(inputLine);
        }
        br.close();

        return a.toString();
    }

    private static boolean downloadBuryFile(String dlURL, String sPathToFile) throws Exception {
        /*
         * Get a connection to the URL and start up a buffered reader.
         */
        URL url = URI.create(dlURL).toURL();
        url.openConnection();
        InputStream reader = url.openStream();

        /*
         * Setup a buffered file writer to write out what we read from the website.
         */
        FileOutputStream writer = new FileOutputStream(sPathToFile);
        byte[] buffer = new byte[32 * 1024];
        int bytesRead = 0;

        while ((bytesRead = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, bytesRead);
            buffer = new byte[32 * 1024];
        }

        writer.close();
        reader.close();

        return true;
    }

    public static void main(String[] args) {
        getBuriedTown("http://townsmods.net/api/bury", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
