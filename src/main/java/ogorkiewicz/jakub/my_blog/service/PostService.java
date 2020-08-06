package ogorkiewicz.jakub.my_blog.service;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import lombok.AllArgsConstructor;
import ogorkiewicz.jakub.my_blog.dto.MultipartFile;
import ogorkiewicz.jakub.my_blog.dto.PageDto;
import ogorkiewicz.jakub.my_blog.dto.PostDto;
import ogorkiewicz.jakub.my_blog.dto.PostLikeDto;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.model.Comment;
import ogorkiewicz.jakub.my_blog.model.Post;
import ogorkiewicz.jakub.my_blog.model.PostImage;
import ogorkiewicz.jakub.my_blog.model.PostLike;
import ogorkiewicz.jakub.my_blog.model.PostToken;

@ApplicationScoped
@AllArgsConstructor(onConstructor = @__(@Inject))
public class PostService {

    private PostImageService postImageService;
    private MailService mailService;
    private final int pageCount = 5;

    public PageDto getPage(int pageNumber) throws MyBlogException{
        PanacheQuery<Post> postsQuery = Post.find("is_confirmed",true);
        int availablePosts = (int) postsQuery.count();
        int availablePages = availablePosts / pageCount + (availablePosts % pageCount == 0 ? 0 : 1);
        int firstIndex = availablePosts - pageNumber * pageCount < 0 ? 0 : availablePosts - pageNumber * pageCount;
        int lastIndex = availablePosts - pageNumber * pageCount + pageCount -1 > availablePosts ? availablePosts -1 : availablePosts - pageNumber * pageCount + pageCount -1 ;

        if(pageNumber > 0){
            List<Post> posts = postsQuery.range(firstIndex,lastIndex).list();
            Collections.sort(posts,new Comparator<Post>(){
				@Override
				public int compare(Post p1, Post p2) {
                    return p1.createDate.isAfter(p2.createDate) ? -1 : 1;
				}                
            });
            List<Long> postsIds = posts.stream().map(p -> p.id).collect(Collectors.toList());
            List<PostImage> postImages = PostImage.findByIdList(postsIds);

            List<PostDto> postDtoList = posts.stream().map( post -> {
                PostImage postImage =
                        postImages.stream().filter(img -> img.id.equals(post.id)).findFirst().orElse(null);
                return new PostDto(post,postImage);
            }).collect(Collectors.toList());

            return new PageDto(pageNumber,availablePages,postDtoList);

        }else{
            throw new MyBlogException(ErrorCode.OUT_OF_INDEX,Post.class);
        }

    }

    public void addPost(MultipartFile multipartRequest) throws MyBlogException{
        Post post = PostDto.toEntity(multipartRequest);
        Post.persist(post);

        Path imagePath = postImageService.addPostImage(multipartRequest,post);

        String token = UUID.randomUUID().toString();
        PostToken.persist(new PostToken(token, post));

        mailService.sendPostConfirmationEmail(post.email,post.content,imagePath,token);

    }

    public long addLike(PostLikeDto postLikeDto) throws MyBlogException{
        PostLike postLike = PostLike.find("email=?1 and post_id=?2",postLikeDto.getEmail(),postLikeDto.getPostId()).firstResult();
        if(postLike != null){
            throw new MyBlogException(ErrorCode.ALREADY_VOTED, PostLike.class);
        }else{
            PostLike.persist(postLikeDto.toEntity());
            return PostLike.count("post_id", postLikeDto.getPostId());
        }
    }
    
    public void deletePost(Long postId){
        Comment.delete("post_id", postId);
        Post.deleteById(postId);
    }

    public void confirmPost(String token) throws MyBlogException{
        PostToken postToken = PostToken.find("token", token).firstResult();
        if(postToken != null){
            Post.update("is_confirmed = true where id =?1", postToken.id);
            PostToken.deleteById(postToken.id);
        }else{
            throw new MyBlogException(ErrorCode.NOT_EXIST, PostToken.class);
        }

    }

}