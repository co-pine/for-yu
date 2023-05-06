package com.pine.foryu;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class ResponseBody {
    private Integer code;
    private Page data;
    private String message;

    @Data
    class Page{
        private List<Post> records;
        private Integer total;
        private Integer size;
        private Integer current;
        private List<Object> orders;
        private Boolean optimizeCountSql;
        private Boolean searchCount;
        private Integer countId;
        private Integer maxLimit;
        private Integer pages;
    }
    @Getter
    @Setter
    public class Post{
        private String content;
        private String title;

        @Override
        public String toString(){
            return "{\"content\": \"" + content + "\",\"title\": \"" + title + "\"}";
        }
    }
}
