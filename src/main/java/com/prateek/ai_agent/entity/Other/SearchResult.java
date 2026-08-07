package com.prateek.ai_agent.entity.Other;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {
    private int rank;

    private String title;

    private String url;

    private String snippet;
}
