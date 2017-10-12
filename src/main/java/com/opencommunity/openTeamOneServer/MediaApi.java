package com.opencommunity.openTeamOneServer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/sap/sports/fnd/api")
public class MediaApi {

	@RequestMapping(method = RequestMethod.GET, value = "/picture/v1/service/rest/picture/{itemId}")
	@ResponseBody
	public ResponseEntity<String> picture(HttpServletRequest request, @PathVariable String itemId) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/preview")
	@ResponseBody
	public ResponseEntity<String> mediaFilePreview(HttpServletRequest request, @PathVariable String itemId) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/content")
	@ResponseBody
	public ResponseEntity<String> mediaFileContent(HttpServletRequest request, @PathVariable String itemId) {
		String sessionId = request.getHeader("Cookie");
		Session session = sessionId == null ? null : Session.getSession(sessionId);
		if (session == null)
			return Util.defaultStringResponse(HttpStatus.UNAUTHORIZED);
		//
		return Util.defaultStringResponse(HttpStatus.SERVICE_UNAVAILABLE); // TODO
	}

}
