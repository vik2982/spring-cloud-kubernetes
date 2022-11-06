package com.vik.spring.cloud.kubernetes.department.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.vik.spring.cloud.kubernetes.department.client.EmployeeClient;
import com.vik.spring.cloud.kubernetes.department.model.Department;
import com.vik.spring.cloud.kubernetes.department.model.Employee;
import com.vik.spring.cloud.kubernetes.department.repository.DepartmentRepository;

@RestController
public class DepartmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentController.class);

    DepartmentRepository repository;
    EmployeeClient employeeClient;

    public DepartmentController(DepartmentRepository repository, EmployeeClient employeeClient) {
        this.repository = repository;
        this.employeeClient = employeeClient;
    }

    @GetMapping("/feign")
    public List<Employee> listRest() {
        return employeeClient.findByDepartment("1");
    }

    @PostMapping("/")
    public Department add(@RequestBody Department department) {
        LOGGER.info("Department add: {}", department);
        return repository.save(department);
    }

    @GetMapping("/{id}")
    public Department findById(@PathVariable("id") String id) {
        LOGGER.info("Department find: id={}", id);
        return repository.findById(id).get();
    }

    @GetMapping("/")
    public Iterable<Department> findAll() {
        LOGGER.info("Department find");
        return repository.findAll();
    }

}
