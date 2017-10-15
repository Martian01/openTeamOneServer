package com.opencommunity.openTeamOneServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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

	/* API implementation */

	@RequestMapping(method = RequestMethod.GET, value = "/picture/v1/service/rest/picture/{itemId}")
	public ResponseEntity<Resource> picture(HttpServletRequest request, @PathVariable String itemId) throws Exception {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResourceResponse(HttpStatus.UNAUTHORIZED);
		//
		Attachment attachment = attachmentRepository.findOne(itemId);
		if (attachment == null)
			return Util.httpResourceResponse(HttpStatus.NOT_FOUND);
		if (getDataDirectory() == null)
			return Util.httpResourceResponse(HttpStatus.INTERNAL_SERVER_ERROR);
		String filename = dataDirectory + "/picture/" + itemId;
		File file = new File(filename);
		if (!file.canRead())
			return Util.httpResourceResponse(HttpStatus.NOT_FOUND);
		Resource resource = new ByteArrayResource(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
		return Util.httpResourceResponse(resource, MediaType.parseMediaType(attachment.mimeType));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/content")
	public ResponseEntity<Resource> mediaFileContent(HttpServletRequest request, @PathVariable String itemId) throws Exception {
		User user = Util.getCurrentUser(request, userRepository);
		if (user == null)
			return Util.httpResourceResponse(HttpStatus.UNAUTHORIZED);
		//
		Attachment attachment = attachmentRepository.findOne(itemId);
		if (attachment == null)
			return Util.httpResourceResponse(HttpStatus.NOT_FOUND);
		if (getDataDirectory() == null)
			return Util.httpResourceResponse(HttpStatus.INTERNAL_SERVER_ERROR);
		String filename = dataDirectory + "/file/" + itemId;
		File file = new File(filename);
		if (!file.canRead())
			return Util.httpResourceResponse(HttpStatus.NOT_FOUND);
		Resource resource = new ByteArrayResource(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
		return Util.httpResourceResponse(resource, MediaType.parseMediaType(attachment.mimeType));
	}

	/* The following API calls are intentionally not implemented */

	//@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/preview")
	//@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/details")
	//@RequestMapping(method = RequestMethod.GET, value = "/media/v1/service/rest/media/file/{itemId}/url")

	/* helper functions */

	private String dataDirectory = null;

	public String getDataDirectory() {
		if (dataDirectory == null) {
			TenantParameter tp = tenantParameterRepository.findOne("dataDirectory");
			if (tp == null)
				System.out.println("Error: data directory not configured");
			else
				dataDirectory = tp.value;
		}
		return dataDirectory;
	}

}
