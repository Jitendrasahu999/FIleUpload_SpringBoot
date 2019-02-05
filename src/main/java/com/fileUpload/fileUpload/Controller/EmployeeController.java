package com.fileUpload.fileUpload.Controller;

import com.fileUpload.fileUpload.Model.Employee;
import com.fileUpload.fileUpload.Repository.EmployeeRepository;
import com.fileUpload.fileUpload.Utils.FileStorageService;
import com.fileUpload.fileUpload.Utils.MyFileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
                String fileName = fileStorageService.storeMultipartFile(file, FileStorageService.FILE_DOWNLOAD_API_ENDPOINT, employee.getId() + "");
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


}
