package org.example.backend.user.note.favoriteNote.dto;

import lombok.Data;

@Data
public class FavoriteFolderDto {
    private Long id;
    private String folderName;

    public FavoriteFolderDto(Long id, String folderName) {
        this.id = id;
        this.folderName = folderName;
    }
}