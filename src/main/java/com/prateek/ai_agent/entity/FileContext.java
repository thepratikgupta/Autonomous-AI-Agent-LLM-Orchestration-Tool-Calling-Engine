package com.prateek.ai_agent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class FileContext implements Serializable {

    private String lastOpenedFile;
    private List<String> recentFiles = new ArrayList<>();

}