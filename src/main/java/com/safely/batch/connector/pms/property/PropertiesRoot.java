package com.safely.batch.connector.pms.property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.safely.batch.connector.pms.Links;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertiesRoot {
    @JsonProperty("_links")
    private Links _links;
    @JsonProperty("_embedded")
    private RootEmbeddedUnits _embedded;

    @JsonProperty("page_count")
    private Integer page_count;
    @JsonProperty("page_size")
    private Integer page_size;
    @JsonProperty("total_items")
    private Integer total_items;
    @JsonProperty("page")
    private Integer page;
}

