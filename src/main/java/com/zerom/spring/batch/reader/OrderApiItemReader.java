package com.zerom.spring.batch.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.springframework.http.HttpStatus.OK;

/**
 * Created by jojoldu@gmail.com on 05/11/2019
 * Blog : http://jojoldu.tistory.com
 * Github : http://github.com/jojoldu
 */
@Slf4j
public class OrderApiItemReader implements ItemReader<OrderApiReadDto> {
    private final RestTemplate restTemplate;
    private final String url;
    private final int pageSize;
    private final String parameterA;
    private final String parameterB;

    private List<OrderApiReadDto> orderData;
    private int nextOrderIndex = 0;

    public OrderApiItemReader(RestTemplate restTemplate, String url, int pageSize, String parameterA, String parameterB) {
        if (pageSize > 1000) {
            throw new IllegalArgumentException("해당 API는 1000개이상 한번에 호출할 수 없습니다.");
        }
        this.restTemplate = restTemplate;
        this.url = url;
        this.pageSize = pageSize;
        this.parameterA = parameterA;
        this.parameterB = parameterB;
        nextOrderIndex = 0;

    }

    // 1건씩 읽어오는 메소드
    @Override
    public OrderApiReadDto read() throws Exception {

        if (resultsIsNotInitialized()) {
            orderData = getForEntity();
        }


        OrderApiReadDto nextOrder = null;

        if (nextOrderIndex < orderData.size()) {
            nextOrder = orderData.get(nextOrderIndex);
            nextOrderIndex++;
        } else {
            nextOrderIndex = 0;
            nextOrder = null; // null 이면 Reader 종료
        }

        return nextOrder;
    }

    private boolean resultsIsNotInitialized() {
        return this.orderData == null;
    }

    private List<OrderApiReadDto> getForEntity() {
        try {
            Map<String, Object> variables = new HashMap<>();

            variables.put("pageSize", pageSize);
            variables.put("parameterA", parameterA);
            variables.put("parameterB", parameterB);

            ResponseEntity<OrderApiReadDto[]> responseEntity = restTemplate.getForEntity(url, OrderApiReadDto[].class, variables);
            HttpStatus httpStatus = responseEntity.getStatusCode();

            if (!OK.equals(httpStatus)) {
                log.error("API 비정상 응답: httpStatus={}, pageSize={}, parameterA={}, parameterB={}", httpStatus, pageSize, parameterA, parameterB);
            }

            OrderApiReadDto[] body = responseEntity.getBody();
            return Arrays.asList(body);
        } catch (HttpClientErrorException hce) {
            log.error("API 요청 실패: statusCode={}, body={}", hce.getStatusCode(), hce.getResponseBodyAsString(), hce);
            return null;
        }
    }


}
