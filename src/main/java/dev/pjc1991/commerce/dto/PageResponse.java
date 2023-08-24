package dev.pjc1991.commerce.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> extends PageImpl<T> implements Page<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    private long total;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PageResponse(@JsonProperty("content") List<T> content,
                        @JsonProperty("number") int number,
                        @JsonProperty("size") int size,
                        @JsonProperty("totalElements") Long totalElements,
                        @JsonProperty("pageable") JsonNode pageable,
                        @JsonProperty("last") boolean last,
                        @JsonProperty("totalPages") int totalPages,
                        @JsonProperty("sort") JsonNode sort,
                        @JsonProperty("first") boolean first,
                        @JsonProperty("numberOfElements") int numberOfElements) {
        super(content, PageRequest.of(number, size), totalElements);
    }


    public PageResponse(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public PageResponse(List<T> content) {
        super(content);
    }

    public PageResponse() {
        super(new ArrayList<>());
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return new PageResponse<>(getConvertedContent(converter), getPageable(), total);
    }
}
