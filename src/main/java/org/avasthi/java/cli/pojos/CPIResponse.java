package org.avasthi.java.cli.pojos;


import java.util.Arrays;

public class CPIResponse {
  public static record SIngleInstance(String baseyear, int year, String month, String state, String sector, String group, String subgroup, String index, String inflation, String status) {

  }
  public static record MetaData(int page, int totalRecords, int totalPages, int recordPerPage) {

  }

  public SIngleInstance[] getData() {
    return data;
  }

  public MetaData getMeta_data() {
    return meta_data;
  }

  public String getMsg() {
    return msg;
  }

  public boolean isStatusCode() {
    return statusCode;
  }

  @Override
  public String toString() {
    return "CPIResponse{" +
            "data=" + Arrays.toString(data) +
            ", meta_data=" + meta_data +
            ", msg='" + msg + '\'' +
            ", statusCode=" + statusCode +
            '}';
  }

  private SIngleInstance[]  data;
  private MetaData meta_data;
  private String msg;
  private boolean statusCode;
}
