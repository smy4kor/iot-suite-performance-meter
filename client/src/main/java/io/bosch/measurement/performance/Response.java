package io.bosch.measurement.performance;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Response {
    @JsonAlias("request_id")
    private String requestId;
    @JsonAlias("total_count")
    private int totalCount;
    @JsonAlias("current_no")
    private int currentNo;
    @JsonIgnore
    private final long receivedTs = System.currentTimeMillis();
}
