package com.opencommunity.openTeamOneServer.api;

import com.opencommunity.openTeamOneServer.data.Session;
import com.opencommunity.openTeamOneServer.data.SymbolicFile;
import com.opencommunity.openTeamOneServer.data.User;
import com.opencommunity.openTeamOneServer.persistence.SymbolicFileRepository;
import com.opencommunity.openTeamOneServer.persistence.TenantParameterRepository;
import com.opencommunity.openTeamOneServer.persistence.UserRepository;
import com.opencommunity.openTeamOneServer.util.StreamUtil;
import com.opencommunity.openTeamOneServer.util.Util;
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
import java.nio.file.Files;
import java.nio.file.Paths;

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

	/* API implementation */

	@RequestMapping(method = RequestMethod.GET, value = "/picture/v1/service/rest/picture/{fileId}")
	public ResponseEntity<Resource> picture(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		return handleFileRequest(request, fileId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{fileId}/content")
	public ResponseEntity<Resource> mediaFileContent(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		return handleFileRequest(request, fileId);
	}

	/* helper functions */

	private ResponseEntity<Resource> handleFileRequest(HttpServletRequest request, String fileId) throws Exception {
		Session session = Util.getSession(request);
		User user = session == null ? Util.getBasicAuthContact(request, userRepository) : Util.getSessionContact(session, userRepository); // iOS vs. Android app
		if (user == null)
			return Util.httpUnauthorizedResourceResponse;
		//
		SymbolicFile symbolicFile = symbolicFileRepository.findById(fileId).orElse(null);
		if (symbolicFile == null)
			return Util.httpNotFoundResourceResponse;
		File file = StreamUtil.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId);
		if (file == null)
			return Util.httpInternalErrorResourceResponse;
		if (!file.canRead()) {
			return Util.httpNotFoundResourceResponse;
		}
		//
		byte[] targetUriBytes = StreamUtil.readFile(file);
		String targetUri = new String(targetUriBytes, StandardCharsets.UTF_8);
		Resource body = new ByteArrayResource(targetUriBytes);
		return "application/vnd.sap.sports.link".equals(symbolicFile.mimeType) ? // Special treatment for compatibility
				Util.httpForwardResourceResponse(targetUri, body):
				Util.httpResourceResponse(body, MediaType.parseMediaType(symbolicFile.mimeType));
	}

}
