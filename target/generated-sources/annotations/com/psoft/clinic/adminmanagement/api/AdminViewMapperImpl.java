package com.psoft.clinic.adminmanagement.api;

import com.psoft.clinic.adminmanagement.model.Admin;
import com.psoft.clinic.model.BaseUser;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-01T16:20:43+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Amazon.com Inc.)"
)
@Component
public class AdminViewMapperImpl implements AdminViewMapper {

    @Override
    public AdminView toAdminView(Admin admin) {
        if ( admin == null ) {
            return null;
        }

        AdminView adminView = new AdminView();

        if ( admin.getId() != null ) {
            adminView.setId( String.valueOf( admin.getId() ) );
        }
        adminView.setFullName( adminBaseUserFullName( admin ) );

        return adminView;
    }

    @Override
    public List<AdminView> toAdminView(List<Admin> admins) {
        if ( admins == null ) {
            return null;
        }

        List<AdminView> list = new ArrayList<AdminView>( admins.size() );
        for ( Admin admin : admins ) {
            list.add( toAdminView( admin ) );
        }

        return list;
    }

    private String adminBaseUserFullName(Admin admin) {
        if ( admin == null ) {
            return null;
        }
        BaseUser baseUser = admin.getBaseUser();
        if ( baseUser == null ) {
            return null;
        }
        String fullName = baseUser.getFullName();
        if ( fullName == null ) {
            return null;
        }
        return fullName;
    }
}
