package com.safely.batch.connector.terraform;

import lombok.Data;

import java.util.Map;

@Data
public class TerraformRemoteState {

    private Map<String, Output> outputs;
}
