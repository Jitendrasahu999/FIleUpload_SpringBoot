package com.fileUpload.fileUpload.Controller;

import com.fileUpload.fileUpload.Model.Employee;
import com.fileUpload.fileUpload.Repository.EmployeeRepository;
import com.fileUpload.fileUpload.Utils.FileStorageService;
import com.fileUpload.fileUpload.Utils.MyFileNotFoundException;
import com.fileUpload.fileUpload.Utils.SuccessOrErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/employees")// It is a base ULR
public class EmployeeController {
    private static final String FILE_SERVICE_STORAGE_DIRECTORY = "employee"; // It is a path where we can save our file to store in our system

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    FileStorageService fileStorageService;

    // Add a file in database API
    @PostMapping(path = "/add")
    public @ResponseBody
    Employee addNewEmployee(@RequestParam String name,
                            @RequestParam(required = false) MultipartFile file) {
        Employee employee = new Employee();
        employee.setName(name);
        employeeRepository.save(employee);

        if (file != null) {
            String fileName = fileStorageService.storeMultipartFile(file, FILE_SERVICE_STORAGE_DIRECTORY, employee.getId() + "");
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/employee/" + FileStorageService.FILE_DOWNLOAD_API_ENDPOINT + "/")
                    .path(fileName)
                    .toUriString();
            employee.setFileUrl(fileDownloadUri);
            employeeRepository.save(employee);
        }

        return employee;
    }


    //Update file in database API
    @PutMapping(path = "/update/{id}")
    public @ResponseBody
    Employee updateEmployee(@PathVariable Long id,
                            @RequestParam String name,
                            @RequestParam(required = false) MultipartFile file) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();
            if (name != null) {
                employee.setName(name);
            }

            if (file != null) {
                String fileName = fileStorageService.storeMultipartFile(file, FILE_SERVICE_STORAGE_DIRECTORY, employee.getId() + "");
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/employee/" + FileStorageService.FILE_DOWNLOAD_API_ENDPOINT + "/")
                        .path(fileName)
                        .toUriString();
                employee.setFileUrl(fileDownloadUri);
                employeeRepository.save(employee);
            }

            employeeRepository.save(employee);
            return employee;
        } else {
            throw new MyFileNotFoundException("Resource not found");
        }
    }

    // Get all Employee API
    @GetMapping(path = "/all")
    public @ResponseBody
    Iterable<Employee> getAllEmployee(@RequestParam(required = false, defaultValue = "0") int pageNum,
                                      @RequestParam(required = false, defaultValue = "20") int pageSize) {

        return employeeRepository.findAll(PageRequest.of(pageNum, pageSize));
    }

    @GetMapping(path = "/{id}")
    public @ResponseBody
    Employee getEmployeeById(@PathVariable Long id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (optionalEmployee.isPresent()) {
            return optionalEmployee.get();
        } else {
            throw new MyFileNotFoundException("File not found");
        }
    }

    //Delete BY id API
    @PutMapping(path = "/deleteFile/{id}")
    public @ResponseBody
    SuccessOrErrorResponse deleteFile(@PathVariable Long id) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();
            employee.setFileUrl(null);
            employeeRepository.save(employee);
            return new SuccessOrErrorResponse(true, "File deleted!");
        } else {
            throw new MyFileNotFoundException("File not found");
        }
    }


    // File Download API
    @GetMapping(FileStorageService.FILE_DOWNLOAD_API_ENDPOINT + "/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(FILE_SERVICE_STORAGE_DIRECTORY, fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            throw new MyFileNotFoundException("Invalid content type!");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


}
