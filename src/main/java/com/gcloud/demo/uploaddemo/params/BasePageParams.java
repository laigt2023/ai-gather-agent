package com.gcloud.demo.uploaddemo.params;

import lombok.Data;

@Data
public class BasePageParams {
    private int pageNo = 1;
    private int pageSize = 10;
}
