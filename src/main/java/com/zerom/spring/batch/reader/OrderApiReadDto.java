package com.zerom.spring.batch.reader;

import com.google.gson.JsonObject;
import lombok.*;

import java.util.List;

/**
 * Created by jojoldu@gmail.com on 05/11/2019
 * Blog : http://jojoldu.tistory.com
 * Github : http://github.com/jojoldu
 */

@Getter
@Setter
@Data
public class OrderApiReadDto {
    private String empNo;
    private String empId;
    private String empNm;

}
