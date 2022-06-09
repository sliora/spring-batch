package com.zerom.spring.batch.reader;

import com.fasterxml.jackson.core.JsonParser;
import com.zerom.spring.batch.entity.Pay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcCursorItemReaderJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    private List<OrderApiReadDto> collectData = new ArrayList<>(); //Rest로 가져온 데이터를 리스트에 넣는다.

    private static final int chunkSize = 1;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Job jdbcCursorItemReaderJob() {
        return jobBuilderFactory.get("jdbcCursorItemReaderJob")
                .start(jdbcCursorItemReaderStep())
                .build();
    }

    @Bean
    public Step jdbcCursorItemReaderStep() {
        return stepBuilderFactory.get("jdbcCursorItemReaderStep")
                .<OrderApiReadDto, OrderApiReadDto>chunk(chunkSize)
                .reader(ItemReader(restTemplate()))
                .writer(tttt())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Pay> jdbcCursorItemReader() {
        return new JdbcCursorItemReaderBuilder<Pay>()
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
                .sql("SELECT id, amount, tx_name, tx_date_time FROM pay")
                .name("jdbcCursorItemReader")
                .build();
    }

    @Bean
    public ItemReader<OrderApiReadDto> ItemReader(RestTemplate restTemplate) {
        return new OrderApiItemReader(restTemplate, "http://localhost:8080/test/api", 1, "", "");

    }

/*    @Bean
    public OrderApiItemReader tt() {
        RestTemplate restTemplate = new RestTemplate();
        return new OrderApiItemReader(restTemplate, "http://localhost:8080/test/api", 1, "", "");
    }*/


    private ItemWriter<OrderApiReadDto> jdbcCursorItemWriter() {
        return list -> {
            for (OrderApiReadDto authority : list) {
                log.info("Current authority = {}", authority.getEmpId());
            }
        };


    }

    @Bean
    public JdbcBatchItemWriter<OrderApiReadDto> tttt() {
        return new JdbcBatchItemWriterBuilder<OrderApiReadDto>()
                .dataSource(dataSource)
                .sql("insert into emp(emp_no, emp_id, emp_nm) values  (:empNo, :empId, :empNm)")
                .beanMapped()
                .build();

    }


}
