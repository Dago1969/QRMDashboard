package com.qtm.dashboard.user.dto;

/**
 * DTO per la rappresentazione esterna di un ruolo utente.
 */
public class RoleDto {

    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
