package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;

public class Proxy extends Controller {

	public static void doProxy() throws IOException {
		internalAsync("http://www.mercadolibre.com.ar");
	}

	private static void internalAsync(String base) throws IOException {

		String url = base + Request.current().url;
		WSRequest wsRequest = WS.url(url);

		HttpResponse httpResponse;

		if ("GET".equals(request.method)) {
			Promise<HttpResponse> responsePromise = wsRequest.getAsync();
			try {
				await(responsePromise);
				httpResponse = responsePromise.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			wsRequest.body = request.body;
			Promise<HttpResponse> responsePromise = wsRequest.postAsync();
			try {
				await(responsePromise);
				httpResponse = responsePromise.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		for (Http.Header header : httpResponse.getHeaders()) {
			Response.current().setHeader(header.name, header.value());
		}

		response.current().status = httpResponse.getStatus();

		String contentLenght = httpResponse.getHeader("Content-Length");
		//System.out.println(" Content-Length: " + contentLenght);
		if (contentLenght != null) {
			renderBinary(httpResponse.getStream());
		} else {
			// This is for cases with chunked responses 
			response.chunked = true;
			BufferedReader is = new BufferedReader(new InputStreamReader(httpResponse.getStream()));
			String line = "";
			while ((line = is.readLine()) != null) {
				response.writeChunk(line + "\n");
			}
		}

	}


}