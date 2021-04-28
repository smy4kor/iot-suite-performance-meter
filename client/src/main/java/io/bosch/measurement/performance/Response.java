package io.bosch.measurement.performance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Response {
    private String id;
    private int expected;
    private int current;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return id.equals(response.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
