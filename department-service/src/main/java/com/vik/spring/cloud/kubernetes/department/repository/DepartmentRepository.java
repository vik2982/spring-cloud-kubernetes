package com.vik.spring.cloud.kubernetes.department.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.vik.spring.cloud.kubernetes.department.model.Department;

public interface DepartmentRepository extends CrudRepository<Department, String> {

  List<Department> findByOrganizationId(String organizationId);

}
