package com.opencommunity.openTeamOneServer.api;

import com.opencommunity.openTeamOneServer.data.Session;
import com.opencommunity.openTeamOneServer.data.SymbolicFile;
import com.opencommunity.openTeamOneServer.data.User;
import com.opencommunity.openTeamOneServer.persistence.SymbolicFileRepository;
import com.opencommunity.openTeamOneServer.persistence.TenantParameterRepository;
import com.opencommunity.openTeamOneServer.persistence.UserRepository;
import com.opencommunity.openTeamOneServer.util.RestLib;
import com.opencommunity.openTeamOneServer.util.StreamUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/sap/sports/fnd/api")
public class MediaApi {

	/* database interface */

	@Autowired
	private TenantParameterRepository tenantParameterRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private SymbolicFileRepository symbolicFileRepository;
	@Autowired
	private RestLib restLib;

	/* API implementation */

	@RequestMapping(method = RequestMethod.GET, value = "/picture/v1/service/rest/picture/{fileId}")
	public ResponseEntity<Resource> picture(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		return handleFileRequest(request, fileId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{fileId}/content")
	public ResponseEntity<Resource> mediaFileContent(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		return handleFileRequest(request, fileId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/token/{fileId}")
	public ResponseEntity<Resource> mediaTokenContent(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		return handleTokenRequest(request, fileId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{fileId}/details")
	public ResponseEntity<String> mediaFileDetails(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		return handleFileDetailsRequest(request, fileId);
	}

	/* helper functions */

	private ResponseEntity<Resource> handleFileRequest(HttpServletRequest request, String fileId) throws Exception {
		Session session = restLib.getSession(request);
		User user = session == null ? restLib.getBasicAuthContact(request, userRepository) : restLib.getSessionContact(session, userRepository); // fallback to Basic Auth
		if (user == null)
			return restLib.httpUnauthorizedResourceResponse;
		//
		SymbolicFile symbolicFile = symbolicFileRepository.findById(fileId).orElse(null);
		if (symbolicFile == null)
			return restLib.httpNotFoundResourceResponse;
		return sendSymbolicFile(symbolicFile);
	}

	private ResponseEntity<Resource> handleTokenRequest(HttpServletRequest request, String token) throws Exception {
		SymbolicFile symbolicFile = symbolicFileRepository.findById(token).orElse(null); // TODO: proper token handling
		if (symbolicFile == null)
			return restLib.httpNotFoundResourceResponse;
		if (!symbolicFile.mimeType.startsWith("video/"))
			return restLib.httpBadRequestResourceResponse;
		return sendSymbolicFile(symbolicFile);
	}

	private ResponseEntity<Resource> sendSymbolicFile(SymbolicFile symbolicFile) throws Exception {
		File file = StreamUtil.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId);
		if (file == null)
			return restLib.httpInternalErrorResourceResponse;
		if (!file.canRead()) {
			return restLib.httpNotFoundResourceResponse;
		}
		//
		byte[] targetUriBytes = StreamUtil.readFile(file);
		//
		if ("application/vnd.sap.sports.link".equals(symbolicFile.mimeType)) { // Special treatment for compatibility
			String targetUri = new String(targetUriBytes, StandardCharsets.UTF_8);
			return restLib.httpForwardResourceResponse(targetUri);
		}
		Resource body = new ByteArrayResource(targetUriBytes);
		return restLib.httpResourceResponse(body, MediaType.parseMediaType(symbolicFile.mimeType));
	}

	private ResponseEntity<String> handleFileDetailsRequest(HttpServletRequest request, String fileId) throws Exception {
		Session session = restLib.getSession(request);
		User user = session == null ? restLib.getBasicAuthContact(request, userRepository) : restLib.getSessionContact(session, userRepository); // fallback to Basic Auth
		if (user == null)
			return restLib.httpUnauthorizedResponse;
		//
		SymbolicFile symbolicFile = symbolicFileRepository.findById(fileId).orElse(null);
		if (symbolicFile == null)
			return restLib.httpNotFoundResponse;
		if (!symbolicFile.mimeType.startsWith("video/"))
			return restLib.httpBadRequestResponse;
		File file = StreamUtil.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId);
		if (file == null)
			return restLib.httpInternalErrorResponse;
		if (!file.canRead()) {
			return restLib.httpNotFoundResponse;
		}
		//
		JSONObject tempUrl = new JSONObject();
		tempUrl.put("url", restLib.getServerUrl(request) + "/sap/sports/fnd/api/media/v1/token/" + symbolicFile.fileId); // TODO: proper token handling
		tempUrl.put("validUntil", "2659-12-31T23:59:59.999Z"); // 640 years ought to be enough for anybody
		JSONObject source = new JSONObject();
		source.put("sourceName", symbolicFile.fileId);
		source.put("mimeType", symbolicFile.mimeType);
		source.put("path", symbolicFile.fileId);
		source.put("size", file.length());
		source.put("tempUrl", tempUrl);
		source.put("isOriginal", true);
		source.put("isAdaptive", false);
		source.put("isDownloadable", true);
		//source.put("isTrimmed", false);
		//source.put("fourCC", "avc1");
		//source.put("duration", 0);
		//source.put("width", 0);
		//source.put("height", 0);
		//source.put("bitrate", 0);
		//source.put("targetBitrate", 0);
		//source.put("framerate", 0);
		//source.put("trimOffset", 0);
		//source.put("trimDuration", 0);
		JSONArray sources = new JSONArray();
		sources.put(source); // note: no preview pic
		JSONObject v2 = new JSONObject();
		v2.put("isFinal", true);
		v2.put("sources", sources);
		JSONObject details = new JSONObject();
		details.put("v2", v2);
		JSONObject body = new JSONObject();
		body.put("fileId", symbolicFile.fileId);
		body.put("details", details);
		//
		return restLib.httpOkResponse(body);
	}

}
