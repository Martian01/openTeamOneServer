package com.opencommunity.openTeamOneServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
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

	@Autowired
	private ResourceLoader resourceLoader;

	/* API implementation */

	@RequestMapping(method = RequestMethod.GET, value = "/picture/v1/service/rest/picture/{fileId}")
	public ResponseEntity<Resource> picture(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		return sendFileContent(request, fileId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{fileId}/content")
	public ResponseEntity<Resource> mediaFileContent(HttpServletRequest request, @PathVariable String fileId) throws Exception {
		return sendFileContent(request, fileId);
	}

	/* The following API calls are intentionally not implemented */

	//@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{fileId}/preview")
	//@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{fileId}/details")
	//@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{fileId}/url")

	/* helper functions */

	public ResponseEntity<Resource> sendFileContent(HttpServletRequest request, String fileId) throws Exception {
		User user = Util.getSessionContact(request, userRepository);
		if (user == null)
			return Util.httpResourceResponse(HttpStatus.UNAUTHORIZED);
		//
		SymbolicFile symbolicFile = symbolicFileRepository.findOne(fileId);
		if (symbolicFile == null)
			return Util.httpResourceResponse(HttpStatus.NOT_FOUND);
		File file = Util.getFile(tenantParameterRepository, symbolicFile.directory, symbolicFile.fileId);
		if (file == null)
			return Util.httpResourceResponse(HttpStatus.INTERNAL_SERVER_ERROR);
		if (!file.canRead()) {
			return Util.httpResourceResponse(HttpStatus.NOT_FOUND);
		}
		Resource resource = new ByteArrayResource(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
		return Util.httpResourceResponse(resource, MediaType.parseMediaType(symbolicFile.mimeType));
	}

}
