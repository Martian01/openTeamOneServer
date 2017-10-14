package com.opencommunity.openTeamOneServer;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/sap/sports/fnd/api")
public class MediaApi {

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(method = RequestMethod.GET, value = "/picture/v1/service/rest/picture/{itemId}")
	public ResponseEntity<String> picture(HttpServletRequest request, @PathVariable String itemId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/preview")
	public ResponseEntity<String> mediaFilePreview(HttpServletRequest request, @PathVariable String itemId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/content")
	public ResponseEntity<String> mediaFileContent(HttpServletRequest request, @PathVariable String itemId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/details")
	public ResponseEntity<String> mediaFileDetails(HttpServletRequest request, @PathVariable String itemId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpResponse(HttpStatus.SERVICE_UNAVAILABLE); // video not implemented
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/url")
	public ResponseEntity<String> mediaFileUrl(HttpServletRequest request, @PathVariable String itemId) throws JSONException {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.httpResponse(HttpStatus.SERVICE_UNAVAILABLE); // video not implemented
	}

}
