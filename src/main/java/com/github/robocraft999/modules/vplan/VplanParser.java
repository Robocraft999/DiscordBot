package com.github.robocraft999.modules.vplan;

import com.github.robocraft999.DiscordBot;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.io.CloseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class VplanParser {
    private final String pwd;
    private final int schoolId;
    private final Logger logger = LoggerFactory.getLogger("VplanParser");

    public VplanParser(String pwd, int schoolId) {
        this.pwd = pwd;
        this.schoolId = schoolId;

        fetchVplan();
        //fetchSplan();
    }
    private String request(String url){
        String response = "";
        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpGet httpGet = new HttpGet(url);

            String auth = "schueler:" + this.pwd;
            String base64 = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            httpGet.addHeader("Authorization", "Basic " + base64);

            response = httpClient.execute(httpGet, new BasicHttpClientResponseHandler());
            httpClient.close(CloseMode.GRACEFUL);
        }catch(HttpHostConnectException | HttpResponseException e){
            logger.warn("Could not connect to: " + url);
            logger.error(e.toString());
        }catch(IOException ioe){
            logger.warn("Could not read");
        }

        return response;
    }

    private void fetchSplan(){
        int version = 10;
        String response = request("https://www.stundenplan24.de/" + this.schoolId + "/wplan/wdatenk/SPlanKl_Sw" + version + ".xml");
        logger.info(response);
        //logger.info(request("https://www.stundenplan24.de/" + this.schoolId + "/wplan/wdatenk/SPlanKl_Basis.xml"));
    }

    private void fetchVplan(){
        String formattedDate = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        //TODO add date logic
        String url = "https://www.stundenplan24.de/" + this.schoolId + "/wplan/wdatenk/WPlanKl_" + "20230612" + ".xml";
        logger.info(url);
        String response = request(url);
        Document document = read(response);

        String periodKey = "Std";
        if (document != null){
            NodeList periods = document.getElementsByTagName(periodKey);
            for(int i = 0; i < periods.getLength(); i++){
                String className = periods.item(i).getParentNode().getParentNode().getFirstChild().getTextContent();
                StringBuilder periodInfoString = new StringBuilder();
                NodeList periodInfo = periods.item(i).getChildNodes();
                for(int j = 0; j < periodInfo.getLength(); j++){
                    if(!periodInfo.item(j).getNodeName().equals("Nr")){
                        /*for(int k = 0; k < periodInfo.item(j).getAttributes().getLength(); k++){
                            logger.info(periodInfo.item(j).getAttributes().item(k).getNodeName() + ": " + periodInfo.item(j).getAttributes().item(k).getNodeValue());
                        }*/
                        periodInfoString.append(periodInfo.item(j).getTextContent()).append(" ");
                    }
                }
                logger.info(className + ": " + periodInfoString.toString().replaceAll("&nbsp; ", " "));
            }
        }

    }

    private Document read(String raw){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        try{
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));
            document.getDocumentElement();
            return document;
        }catch (ParserConfigurationException | IOException | SAXException e){
            logger.error(e.toString());
        }
        return null;
    }

}
