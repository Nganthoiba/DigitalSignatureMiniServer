package application.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HttpHeaderBuilder {
	private final List<HttpHeader> headers = new ArrayList<>();
	
	public HttpHeaderBuilder() {
		
	}
	
	public static HttpHeaderBuilder create() {
		return new HttpHeaderBuilder();
	}
	
	public HttpHeaderBuilder add(String key, String value) {
		headers.add(new HttpHeader(key, value));
		return this;
	}
	
	public List<HttpHeader> build(){
		return Collections.unmodifiableList(headers);
	}
}
