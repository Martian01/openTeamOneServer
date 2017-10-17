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
	private AttachmentRepository attachmentRepository;

	@Autowired
	private ResourceLoader resourceLoader;

	/* API implementation */

	@RequestMapping(method = RequestMethod.GET, value = "/picture/v1/service/rest/picture/logo")
	public ResponseEntity<Resource> pictureDefault(HttpServletRequest request) throws Exception {
		Resource resource = resourceLoader.getResource("classpath:/static/admin/logo256.png");
		return Util.httpResourceResponse(resource, MediaType.IMAGE_PNG);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/picture/v1/service/rest/picture/{itemId}")
	public ResponseEntity<Resource> picture(HttpServletRequest request, @PathVariable String itemId) throws Exception {
		return sendFileContent(request, itemId, "/picture/");
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/content")
	public ResponseEntity<Resource> mediaFileContent(HttpServletRequest request, @PathVariable String itemId) throws Exception {
		return sendFileContent(request, itemId, "/file/");
	}

	/* The following API calls are intentionally not implemented */

	//@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/preview")
	//@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/details")
	//@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/url")

	/* helper functions */

	public ResponseEntity<Resource> sendFileContent(HttpServletRequest request, String itemId, String directory) throws Exception {
		User user = Util.getSessionContact(request, userRepository);
		if (user == null)
			return Util.httpResourceResponse(HttpStatus.UNAUTHORIZED);
		//
		Attachment attachment = attachmentRepository.findOne(itemId);
		if (attachment == null)
			return Util.httpResourceResponse(HttpStatus.NOT_FOUND);
		TenantParameter tp = tenantParameterRepository.findOne("dataDirectory");
		if (tp == null)
			return Util.httpResourceResponse(HttpStatus.INTERNAL_SERVER_ERROR);
		String filename = tp.value + directory + itemId;
		File file = new File(filename);
		if (!file.canRead())
			return Util.httpResourceResponse(HttpStatus.NOT_FOUND);
		Resource resource = new ByteArrayResource(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
		return Util.httpResourceResponse(resource, MediaType.parseMediaType(attachment.mimeType));
	}

}
