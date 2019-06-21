package com.future.apix.service.command;

import com.future.apix.entity.ApiProject;
import com.future.apix.response.RequestResponse;
import org.springframework.web.multipart.MultipartFile;

public interface Swagger2ImportCommand extends Command<ApiProject, MultipartFile> {

}
