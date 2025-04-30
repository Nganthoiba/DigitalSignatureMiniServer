package application.server;

import com.sun.net.httpserver.HttpExchange;

import application.utils.FileContext;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.fileupload.FileItem;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequestContext {
    private final HttpExchange exchange;
    private final Map<String, String> getParams = new HashMap<>();
    private final Map<String, String> postParams = new HashMap<>();
    private final Map<String, List<FileContext>> fileParams = new HashMap<>();

    public HttpRequestContext(HttpExchange exchange) throws IOException {
        this.exchange = exchange;
        parseGetParams();
        parsePostParams();
    }

    private void parseGetParams() throws UnsupportedEncodingException {
        String query = exchange.getRequestURI().getRawQuery();
        if (query != null) {
            parseQuery(query, getParams);
        }
    }

    private void parsePostParams() throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            return;
        }

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null) {
            contentType = "application/x-www-form-urlencoded";
        }

        if (contentType.contains("application/x-www-form-urlencoded")) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            parseQuery(body, postParams);
        } else if (contentType.contains("multipart/form-data")) {
            try {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);                
                List<FileItem> items = upload.parseRequest(new HttpExchangeRequestContext(exchange));  
                for (FileItem item : items) {
                    if (item.isFormField()) {
                        postParams.put(item.getFieldName(), item.getString());
                    } else {
                    	FileContext fileContext = new FileContext(item);
                        fileParams.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>()).add(fileContext);                    	
                    }
                }
            } catch (FileUploadException e) {
                e.printStackTrace();
            }
        }
        else if(contentType.contains("application/json")) {
        	try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
    	         BufferedReader reader = new BufferedReader(isr)) {

    	        StringBuilder requestBody = new StringBuilder();
    	        String line;
    	        while ((line = reader.readLine()) != null) {
    	            requestBody.append(line);
    	        }

    	        String jsonString = requestBody.toString();
    	        JSONObject jsonObject = new JSONObject(jsonString);

    	        for (String key : jsonObject.keySet()) {
    	            postParams.put(key, jsonObject.get(key).toString());
    	        }
    	    } catch (IOException | JSONException e) {
    	        e.printStackTrace();
    	        throw e;
    	    }
        }
    }

    private void parseQuery(String query, Map<String, String> params) throws UnsupportedEncodingException {
        for (String pair : query.split("&")) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
    }

    public String get(String key, String defaultValue) {
        return getParams.getOrDefault(key, defaultValue);
    }

    public String post(String key, String defaultValue) {
        return postParams.getOrDefault(key, defaultValue);
    }

    public List<FileContext> files(String fieldName) {
        return fileParams.getOrDefault(fieldName, Collections.emptyList());
    }

    public FileContext file(String fieldName) {
        List<FileContext> fileContexts = fileParams.get(fieldName);
        return (fileContexts != null && !fileContexts.isEmpty()) ? fileContexts.get(0) : null;
    }
}
