package com.fileUpload.fileUpload.Controller;

import com.fileUpload.fileUpload.Model.Employee;
import com.fileUpload.fileUpload.Repository.EmployeeRepository;
import com.fileUpload.fileUpload.Utils.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;

@RestController
@RequestMapping(path = "/api/employees")
public class EmployeeController {
    private static final String FILE_SERVICE_STORAGE_DIRECTORY = "employee";

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    FileStorageService fileStorageService;

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
                    .path("/api/permits/" + FileStorageService.FILE_DOWNLOAD_API_ENDPOINT + "/")
                    .path(fileName)
                    .toUriString();
            employee.setFileUrl(fileDownloadUri);
            employeeRepository.save(employee);
        }

        return employee;
    }


}
