package com.vik.spring.cloud.kubernetes.department.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.vik.spring.cloud.kubernetes.department.model.Employee;

import java.util.List;

@FeignClient(name = "employee")
public interface EmployeeClient {

  @GetMapping("/department/{departmentId}")
  List<Employee> findByDepartment(@PathVariable("departmentId") String departmentId);

}
