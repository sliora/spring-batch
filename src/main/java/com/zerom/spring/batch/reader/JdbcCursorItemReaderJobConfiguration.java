package com.zerom.spring.batch.reader;

import com.fasterxml.jackson.core.JsonParser;
import com.zerom.spring.batch.entity.Authority;
import com.zerom.spring.batch.entity.Pay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    private List<Authority> collectData = new ArrayList<>(); //Rest로 가져온 데이터를 리스트에 넣는다.

    private static final int chunkSize = 10;

    @Bean
    public Job jdbcCursorItemReaderJob() {
        return jobBuilderFactory.get("jdbcCursorItemReaderJob")
                .start(jdbcCursorItemReaderStep())
                .build();
    }

    @Bean
    public Step jdbcCursorItemReaderStep() {
        return stepBuilderFactory.get("jdbcCursorItemReaderStep")
                .<Authority, Authority>chunk(chunkSize)
                .reader(restItemReader(null, null))
                .writer(jdbcCursorItemWriter())
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
    @JobScope
    public ItemReader<Authority> restItemReader(@Value("#{jobParameters[empNo]}") String empNo, @Value("#{jobParameters[platformCd]}") String platformCd) {
        return new ItemReader<Authority>() {
            @Override
            public Authority read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                log.info("empNo = {}", empNo);
                log.info("platformCd = {}", platformCd);
                String uri = "http://localhost:8080/authority/"+platformCd+ "/" + empNo;
                RestTemplate restTemplate = new RestTemplate();
                Object forObject = restTemplate.getForObject(uri, Object.class);//호출 결과를 우선 배열로 받고, 리스트로 변환



                //collectData = Arrays.asList(retArray);//배열을 리스트로 변환
                log.info("Rest Call result : >>>>>>>" + forObject);
                return null;
            }
        };
    }

    private ItemWriter<Authority> jdbcCursorItemWriter() {
        return list -> {
            for (Authority authority : list) {
                log.info("Current authority = {}", authority);
            }
        };
    }


}
