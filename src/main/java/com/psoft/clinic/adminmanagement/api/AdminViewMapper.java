package com.psoft.clinic.adminmanagement.api;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.psoft.clinic.adminmanagement.model.Admin;

@Mapper(componentModel = "spring")

public interface AdminViewMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "baseUser.fullName", target = "fullName")

    AdminView toAdminView(Admin admin);
    List<AdminView> toAdminView(List<Admin> admins);


}