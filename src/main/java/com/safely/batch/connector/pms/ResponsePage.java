package com.safely.batch.connector.pms;

import java.util.List;
import lombok.Data;

@Data
public class ResponsePage<T> {

  private int count;
  private int limit;
  private int offset;
  private String next;
  private String previous;
  private List<T> results;
}
