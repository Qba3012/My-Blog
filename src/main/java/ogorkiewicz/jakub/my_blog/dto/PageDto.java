package ogorkiewicz.jakub.my_blog.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageDto {

    private final int page;
    private final int totalPages;
    private final List<PostDto> posts;

}