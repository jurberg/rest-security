package filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class RestSignatureFilter extends OncePerRequestFilter {


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
			FilterChain filterChain) throws ServletException, IOException {

		String url = SignatureHelper.createSortedUrl(request);
		String signature = request.getHeader(SignatureHelper.SIGNATURE_HEADER);
		String apiKey = request.getHeader(SignatureHelper.APIKEY_HEADER);

		try {
			if (!SignatureHelper.validateSignature(url, signature, apiKey)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "REST signature failed validation.");
				return;
			}
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The REST Security Server experienced an internal error.");
			return;
		}

		filterChain.doFilter(request, response);
	}
	
}
