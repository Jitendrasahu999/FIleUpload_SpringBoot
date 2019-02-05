package com.fileUpload.fileUpload.Utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessOrErrorResponse {
    Boolean success = false;
    String message = "Some error occurred!";
}
